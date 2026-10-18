package it.nicodesy.hisenseocl.vidaa;

import java.io.IOException;

/**
 * What the Android side needs from the panel's OEM stack. Today the only
 * implementation is {@link VidaaMqttBackend} (VIDAA stays in control of the panel).
 * A native port would add a backend on top of the MediaTek TV Input / audio HALs
 * without touching the Android-facing code.
 */
public interface TvControlBackend extends AutoCloseable {

    interface Callback {
        /** Raw JSON source list as sent by the TV. */
        void onSourceList(String json);

        void onMessage(String topic, String payload);

        void onDisconnected(Exception cause);
    }

    void connect(Callback callback) throws IOException;

    void requestSourceList() throws IOException;

    void changeSource(String sourceId, String sourceName) throws IOException;

    /** Remote key name, e.g. "KEY_POWER", "KEY_VOLUMEUP". */
    void sendKey(String key) throws IOException;

    void setVolume(int volume) throws IOException;

    void sendPairingCode(String pin) throws IOException;

    @Override
    void close();
}
