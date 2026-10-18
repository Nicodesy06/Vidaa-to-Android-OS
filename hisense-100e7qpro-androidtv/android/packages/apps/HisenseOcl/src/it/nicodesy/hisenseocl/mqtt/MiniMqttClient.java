package it.nicodesy.hisenseocl.mqtt;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.net.SocketFactory;

/**
 * Minimal MQTT 3.1.1 client (QoS 0 publish, QoS 0/1 receive) with no external
 * dependencies, so it can ship inside an AOSP app and be unit-tested on a plain JVM.
 *
 * Only what the VIDAA control plane needs is implemented: CONNECT, PUBLISH,
 * SUBSCRIBE, PINGREQ, DISCONNECT, and PUBACK for inbound QoS 1 messages.
 */
public final class MiniMqttClient implements AutoCloseable {

    public interface Listener {
        void onMessage(String topic, byte[] payload);

        void onDisconnected(Exception cause);
    }

    private static final int CONNECT = 0x10;
    private static final int CONNACK = 0x20;
    private static final int PUBLISH = 0x30;
    private static final int PUBACK = 0x40;
    private static final int SUBSCRIBE = 0x82;
    private static final int SUBACK = 0x90;
    private static final int PINGREQ = 0xC0;
    private static final int PINGRESP = 0xD0;
    private static final int DISCONNECT = 0xE0;

    private final SocketFactory socketFactory;
    private final Listener listener;
    private final Object writeLock = new Object();
    private Socket socket;
    private OutputStream out;
    private Thread reader;
    private int nextPacketId = 1;
    private volatile boolean closing;

    public MiniMqttClient(SocketFactory socketFactory, Listener listener) {
        this.socketFactory = socketFactory;
        this.listener = listener;
    }

    /** Opens the socket and performs the CONNECT/CONNACK handshake. */
    public void connect(String host, int port, String clientId, String user,
            String password, int keepAliveSec, int timeoutMs) throws IOException {
        socket = socketFactory.createSocket();
        socket.connect(new InetSocketAddress(host, port), timeoutMs);
        socket.setSoTimeout(timeoutMs);
        out = socket.getOutputStream();
        DataInputStream in = new DataInputStream(socket.getInputStream());

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        writeString(body, "MQTT");
        body.write(4); // protocol level 3.1.1
        int flags = 0x02; // clean session
        if (user != null) flags |= 0x80;
        if (password != null) flags |= 0x40;
        body.write(flags);
        body.write((keepAliveSec >> 8) & 0xFF);
        body.write(keepAliveSec & 0xFF);
        writeString(body, clientId);
        if (user != null) writeString(body, user);
        if (password != null) writeString(body, password);
        sendPacket(CONNECT, body.toByteArray());

        int header = in.readUnsignedByte();
        byte[] ack = readBody(in);
        if ((header & 0xF0) != CONNACK || ack.length < 2) {
            throw new IOException("expected CONNACK, got 0x" + Integer.toHexString(header));
        }
        if (ack[1] != 0) {
            throw new IOException("CONNACK refused, return code " + ack[1]);
        }
        socket.setSoTimeout(0);
        reader = new Thread(() -> readLoop(in), "mini-mqtt-reader");
        reader.setDaemon(true);
        reader.start();
    }

    public void subscribe(String topicFilter) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        int id = takePacketId();
        body.write((id >> 8) & 0xFF);
        body.write(id & 0xFF);
        writeString(body, topicFilter);
        body.write(0); // requested QoS 0
        sendPacket(SUBSCRIBE, body.toByteArray());
    }

    public void publish(String topic, byte[] payload) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        writeString(body, topic);
        body.write(payload, 0, payload.length);
        sendPacket(PUBLISH, body.toByteArray());
    }

    public void publish(String topic, String payload) throws IOException {
        publish(topic, payload.getBytes(StandardCharsets.UTF_8));
    }

    public void ping() throws IOException {
        sendPacket(PINGREQ, new byte[0]);
    }

    @Override
    public void close() {
        closing = true;
        try {
            if (out != null) sendPacket(DISCONNECT, new byte[0]);
        } catch (IOException ignored) {
            // Best effort: the socket is closed below either way.
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
            // Nothing more to release.
        }
    }

    private void readLoop(DataInputStream in) {
        Exception cause = null;
        try {
            while (!closing) {
                int header = in.readUnsignedByte();
                byte[] body = readBody(in);
                int type = header & 0xF0;
                if (type == PUBLISH) {
                    handlePublish(header, body);
                } else if (type != SUBACK && type != PINGRESP && type != PUBACK) {
                    throw new IOException("unexpected packet 0x" + Integer.toHexString(header));
                }
            }
        } catch (Exception e) {
            cause = e;
        }
        if (!closing) listener.onDisconnected(cause);
    }

    private void handlePublish(int header, byte[] body) throws IOException {
        int qos = (header >> 1) & 0x03;
        int topicLen = ((body[0] & 0xFF) << 8) | (body[1] & 0xFF);
        String topic = new String(body, 2, topicLen, StandardCharsets.UTF_8);
        int pos = 2 + topicLen;
        if (qos > 0) {
            int id = ((body[pos] & 0xFF) << 8) | (body[pos + 1] & 0xFF);
            pos += 2;
            if (qos == 1) sendPacket(PUBACK, new byte[] {(byte) (id >> 8), (byte) id});
        }
        byte[] payload = new byte[body.length - pos];
        System.arraycopy(body, pos, payload, 0, payload.length);
        listener.onMessage(topic, payload);
    }

    private int takePacketId() {
        synchronized (writeLock) {
            int id = nextPacketId;
            nextPacketId = id == 0xFFFF ? 1 : id + 1;
            return id;
        }
    }

    private void sendPacket(int header, byte[] body) throws IOException {
        ByteArrayOutputStream packet = new ByteArrayOutputStream(body.length + 5);
        packet.write(header);
        encodeLength(packet, body.length);
        packet.write(body, 0, body.length);
        synchronized (writeLock) {
            out.write(packet.toByteArray());
            out.flush();
        }
    }

    static void encodeLength(ByteArrayOutputStream out, int length) {
        do {
            int digit = length % 128;
            length /= 128;
            if (length > 0) digit |= 0x80;
            out.write(digit);
        } while (length > 0);
    }

    static int decodeLength(InputStream in) throws IOException {
        int value = 0;
        int multiplier = 1;
        for (int i = 0; i < 4; i++) {
            int digit = in.read();
            if (digit < 0) throw new EOFException();
            value += (digit & 0x7F) * multiplier;
            if ((digit & 0x80) == 0) return value;
            multiplier *= 128;
        }
        throw new IOException("malformed remaining length");
    }

    private static byte[] readBody(DataInputStream in) throws IOException {
        byte[] body = new byte[decodeLength(in)];
        in.readFully(body);
        return body;
    }

    private static void writeString(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.write((bytes.length >> 8) & 0xFF);
        out.write(bytes.length & 0xFF);
        out.write(bytes, 0, bytes.length);
    }
}
