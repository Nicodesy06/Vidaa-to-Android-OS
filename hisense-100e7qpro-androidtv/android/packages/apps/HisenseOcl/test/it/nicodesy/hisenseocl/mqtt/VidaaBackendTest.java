package it.nicodesy.hisenseocl.mqtt;

import it.nicodesy.hisenseocl.vidaa.TvControlBackend;
import it.nicodesy.hisenseocl.vidaa.VidaaConfig;
import it.nicodesy.hisenseocl.vidaa.VidaaMqttBackend;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;

/**
 * Plain-JVM test (no JUnit needed): runs the VIDAA backend against a scripted fake
 * broker and checks the bytes on the wire. Run with tools/run_android_tests.sh.
 */
public final class VidaaBackendTest {

    public static void main(String[] args) throws Exception {
        testRemainingLength();
        testBackendAgainstFakeBroker();
        System.out.println("ALL TESTS PASSED");
    }

    static void testRemainingLength() throws Exception {
        int[] samples = {0, 1, 127, 128, 16383, 16384, 2097151, 2097152, 268435455};
        for (int n : samples) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MiniMqttClient.encodeLength(out, n);
            int decoded = MiniMqttClient.decodeLength(new ByteArrayInputStream(out.toByteArray()));
            check(decoded == n, "remaining length round trip for " + n);
        }
        System.out.println("ok remaining-length");
    }

    static void testBackendAgainstFakeBroker() throws Exception {
        BlockingQueue<String> sourceLists = new ArrayBlockingQueue<>(4);
        BlockingQueue<String[]> published = new ArrayBlockingQueue<>(8);
        BlockingQueue<String> connectInfo = new ArrayBlockingQueue<>(1);
        String sourceJson = "[{\"sourceid\":\"HDMI1\",\"sourcename\":\"HDMI 1\"}]";

        try (ServerSocket server = new ServerSocket(0)) {
            Thread broker = new Thread(() -> {
                try (Socket s = server.accept()) {
                    DataInputStream in = new DataInputStream(s.getInputStream());
                    OutputStream out = s.getOutputStream();

                    check(in.readUnsignedByte() == 0x10, "CONNECT header");
                    byte[] connect = readBody(in);
                    connectInfo.add(new String(connect, StandardCharsets.UTF_8));
                    out.write(new byte[] {0x20, 0x02, 0x00, 0x00});

                    for (int i = 0; i < 2; i++) {
                        check(in.readUnsignedByte() == 0x82, "SUBSCRIBE header");
                        byte[] sub = readBody(in);
                        out.write(new byte[] {(byte) 0x90, 0x03, sub[0], sub[1], 0x00});
                    }

                    // Expect a source-list request, answer it, then expect a source change.
                    for (int i = 0; i < 2; i++) {
                        check((in.readUnsignedByte() & 0xF0) == 0x30, "PUBLISH header");
                        byte[] body = readBody(in);
                        int tl = ((body[0] & 0xFF) << 8) | (body[1] & 0xFF);
                        String topic = new String(body, 2, tl, StandardCharsets.UTF_8);
                        String payload = new String(body, 2 + tl, body.length - 2 - tl,
                                StandardCharsets.UTF_8);
                        published.add(new String[] {topic, payload});
                        if (i == 0) {
                            out.write(publishPacket(
                                    "/remoteapp/mobile/test-client/ui_service/data/sourcelist",
                                    sourceJson, true));
                            check(in.readUnsignedByte() == 0x40, "PUBACK header");
                            byte[] ack = readBody(in);
                            check(ack.length == 2 && ack[1] == 0x07, "PUBACK packet id");
                        }
                    }
                    check(in.readUnsignedByte() == 0xE0, "DISCONNECT header");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            broker.start();

            VidaaConfig cfg = new VidaaConfig("127.0.0.1", server.getLocalPort(), "test-client",
                    VidaaConfig.DEFAULT_USER, VidaaConfig.DEFAULT_PASSWORD, false);
            VidaaMqttBackend backend = new VidaaMqttBackend(cfg, SocketFactory.getDefault());
            backend.connect(new TvControlBackend.Callback() {
                @Override
                public void onSourceList(String json) {
                    sourceLists.add(json);
                }

                @Override
                public void onMessage(String topic, String payload) {}

                @Override
                public void onDisconnected(Exception cause) {}
            });

            String connect = connectInfo.poll(2, TimeUnit.SECONDS);
            check(connect != null && connect.contains("hisenseservice")
                    && connect.contains("multimqttservice") && connect.contains("test-client"),
                    "CONNECT carries client id and credentials");

            backend.requestSourceList();
            String[] req = published.poll(2, TimeUnit.SECONDS);
            check(req != null && req[0].equals("/remoteapp/tv/ui_service/test-client/actions/sourcelist"),
                    "source list request topic");

            String got = sourceLists.poll(2, TimeUnit.SECONDS);
            check(sourceJson.equals(got), "QoS1 source list reply delivered to callback");

            backend.changeSource("HDMI2", "HDMI 2");
            String[] change = published.poll(2, TimeUnit.SECONDS);
            check(change != null
                    && change[0].equals("/remoteapp/tv/ui_service/test-client/actions/changesource")
                    && change[1].equals("{\"sourceid\":\"HDMI2\",\"sourcename\":\"HDMI 2\"}"),
                    "change source topic and payload");

            backend.close();
            broker.join(2000);
            check(!broker.isAlive(), "broker finished script");
        }
        System.out.println("ok backend-vs-fake-broker");
    }

    private static byte[] publishPacket(String topic, String payload, boolean qos1) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        byte[] t = topic.getBytes(StandardCharsets.UTF_8);
        body.write(t.length >> 8);
        body.write(t.length & 0xFF);
        body.write(t, 0, t.length);
        if (qos1) {
            body.write(0x00);
            body.write(0x07);
        }
        byte[] p = payload.getBytes(StandardCharsets.UTF_8);
        body.write(p, 0, p.length);
        ByteArrayOutputStream pkt = new ByteArrayOutputStream();
        pkt.write(qos1 ? 0x32 : 0x30);
        MiniMqttClient.encodeLength(pkt, body.size());
        byte[] b = body.toByteArray();
        pkt.write(b, 0, b.length);
        return pkt.toByteArray();
    }

    private static byte[] readBody(DataInputStream in) throws Exception {
        byte[] body = new byte[MiniMqttClient.decodeLength(in)];
        in.readFully(body);
        return body;
    }

    private static void check(boolean cond, String what) {
        if (!cond) throw new AssertionError("FAILED: " + what);
    }
}
