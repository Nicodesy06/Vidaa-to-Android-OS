package it.nicodesy.hisenseocl.vidaa;

import it.nicodesy.hisenseocl.mqtt.MiniMqttClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** {@link TvControlBackend} that drives VIDAA through its local MQTT broker. */
public final class VidaaMqttBackend implements TvControlBackend {
    private static final int KEEP_ALIVE_SEC = 30;
    private static final int TIMEOUT_MS = 5000;

    private final VidaaConfig config;
    private final SocketFactory socketFactory;
    private MiniMqttClient client;

    public VidaaMqttBackend(VidaaConfig config) {
        this(config, config.useTls ? lanTlsSocketFactory() : SocketFactory.getDefault());
    }

    /** Visible for tests, which inject a plain-socket factory. */
    public VidaaMqttBackend(VidaaConfig config, SocketFactory socketFactory) {
        this.config = config;
        this.socketFactory = socketFactory;
    }

    @Override
    public void connect(Callback callback) throws IOException {
        client = new MiniMqttClient(socketFactory, new MiniMqttClient.Listener() {
            @Override
            public void onMessage(String topic, byte[] payload) {
                String text = new String(payload, StandardCharsets.UTF_8);
                if (topic.endsWith(VidaaTopics.SOURCE_LIST_REPLY_SUFFIX)) {
                    callback.onSourceList(text);
                } else {
                    callback.onMessage(topic, text);
                }
            }

            @Override
            public void onDisconnected(Exception cause) {
                callback.onDisconnected(cause);
            }
        });
        client.connect(config.host, config.port, config.clientId, config.user, config.password,
                KEEP_ALIVE_SEC, TIMEOUT_MS);
        client.subscribe(String.format(VidaaTopics.REPLIES, config.clientId));
        client.subscribe(VidaaTopics.BROADCAST);
    }

    @Override
    public void requestSourceList() throws IOException {
        publish(VidaaTopics.SOURCE_LIST, "");
    }

    @Override
    public void changeSource(String sourceId, String sourceName) throws IOException {
        publish(VidaaTopics.CHANGE_SOURCE, "{\"sourceid\":\"" + escape(sourceId)
                + "\",\"sourcename\":\"" + escape(sourceName) + "\"}");
    }

    @Override
    public void sendKey(String key) throws IOException {
        publish(VidaaTopics.SEND_KEY, key);
    }

    @Override
    public void setVolume(int volume) throws IOException {
        publish(VidaaTopics.CHANGE_VOLUME, Integer.toString(Math.max(0, Math.min(100, volume))));
    }

    @Override
    public void sendPairingCode(String pin) throws IOException {
        publish(VidaaTopics.AUTH_CODE, "{\"authNum\":\"" + escape(pin) + "\"}");
    }

    /** Keeps the broker session alive; call roughly every KEEP_ALIVE_SEC / 2. */
    public void ping() throws IOException {
        requireClient().ping();
    }

    @Override
    public void close() {
        if (client != null) client.close();
        client = null;
    }

    private void publish(String template, String payload) throws IOException {
        requireClient().publish(String.format(template, config.clientId), payload);
    }

    private MiniMqttClient requireClient() throws IOException {
        if (client == null) throw new IOException("not connected");
        return client;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * The TV's broker presents a self-signed certificate, so the LAN connection
     * cannot be validated against a CA. It is only ever opened to the address the
     * user paired with.
     */
    private static SocketFactory lanTlsSocketFactory() {
        try {
            TrustManager trustTvCert = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] {trustTvCert}, null);
            return ctx.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("TLS unavailable", e);
        }
    }
}
