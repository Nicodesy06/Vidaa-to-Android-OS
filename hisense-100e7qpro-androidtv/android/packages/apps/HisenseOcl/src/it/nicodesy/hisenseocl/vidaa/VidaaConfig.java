package it.nicodesy.hisenseocl.vidaa;

/** Connection settings for the TV's local MQTT broker. */
public final class VidaaConfig {
    public static final int DEFAULT_PORT = 36669;
    public static final String DEFAULT_USER = "hisenseservice";
    public static final String DEFAULT_PASSWORD = "multimqttservice";

    public final String host;
    public final int port;
    public final String clientId;
    public final String user;
    public final String password;
    public final boolean useTls;

    public VidaaConfig(String host, int port, String clientId, String user, String password,
            boolean useTls) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.user = user;
        this.password = password;
        this.useTls = useTls;
    }

    public static VidaaConfig defaults(String host, String clientId) {
        return new VidaaConfig(host, DEFAULT_PORT, clientId, DEFAULT_USER, DEFAULT_PASSWORD, true);
    }
}
