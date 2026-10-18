package it.nicodesy.hisenseocl.vidaa;

/**
 * VIDAA MQTT topic layout, as used by the community hisensetv/pyvidaa clients
 * (SOURCES.md S-40, S-41). Not verified on the 100E7Q PRO: newer VIDAA builds may
 * change the layout or require per-device credentials, so every template is
 * overridable through {@link VidaaConfig}.
 */
public final class VidaaTopics {
    private VidaaTopics() {}

    public static final String SOURCE_LIST = "/remoteapp/tv/ui_service/%s/actions/sourcelist";
    public static final String CHANGE_SOURCE = "/remoteapp/tv/ui_service/%s/actions/changesource";
    public static final String SEND_KEY = "/remoteapp/tv/remote_service/%s/actions/sendkey";
    public static final String GET_VOLUME = "/remoteapp/tv/platform_service/%s/actions/getvolume";
    public static final String CHANGE_VOLUME = "/remoteapp/tv/platform_service/%s/actions/changevolume";
    public static final String AUTH_CODE = "/remoteapp/tv/ui_service/%s/actions/authenticationcode";

    /** Everything the TV sends back to this client, plus broadcasts. */
    public static final String REPLIES = "/remoteapp/mobile/%s/#";
    public static final String BROADCAST = "/remoteapp/mobile/broadcast/#";

    /** Reply carrying the JSON source list. */
    public static final String SOURCE_LIST_REPLY_SUFFIX = "/ui_service/data/sourcelist";
}
