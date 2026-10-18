package it.nicodesy.hisenseocl.tv;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import it.nicodesy.hisenseocl.vidaa.TvControlBackend;
import it.nicodesy.hisenseocl.vidaa.VidaaConfig;
import it.nicodesy.hisenseocl.vidaa.VidaaMqttBackend;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Process-wide connection to the panel's OEM stack. All network I/O runs on one
 * background thread so callers (TV input sessions, setup UI) never block the main
 * thread and commands reach the TV in order.
 */
public final class OclConnection {
    private static final String TAG = "HisenseOcl";
    private static final String PREFS = "ocl";
    private static final String KEY_HOST = "host";
    private static final String KEY_CLIENT_ID = "client_id";

    public interface Listener {
        void onSourceList(String json);

        void onError(Exception e);
    }

    private static OclConnection instance;

    private final SharedPreferences prefs;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private TvControlBackend backend;
    private volatile Listener listener;

    public static synchronized OclConnection get(Context context) {
        if (instance == null) instance = new OclConnection(context.getApplicationContext());
        return instance;
    }

    private OclConnection(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_CLIENT_ID)) {
            prefs.edit().putString(KEY_CLIENT_ID, "ocl-" + UUID.randomUUID().toString()
                    .substring(0, 8)).apply();
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public String getHost() {
        return prefs.getString(KEY_HOST, "");
    }

    public void setHost(String host) {
        prefs.edit().putString(KEY_HOST, host.trim()).apply();
        io.execute(this::dropBackend);
    }

    public void requestSourceList() {
        run(TvControlBackend::requestSourceList);
    }

    public void changeSource(String sourceId, String sourceName) {
        run(b -> b.changeSource(sourceId, sourceName));
    }

    public void sendKey(String key) {
        run(b -> b.sendKey(key));
    }

    public void setVolume(int volume) {
        run(b -> b.setVolume(volume));
    }

    public void sendPairingCode(String pin) {
        run(b -> b.sendPairingCode(pin));
    }

    private interface Command {
        void apply(TvControlBackend backend) throws IOException;
    }

    private void run(Command command) {
        io.execute(() -> {
            try {
                command.apply(ensureBackend());
            } catch (IOException e) {
                Log.w(TAG, "command failed, will reconnect on next use", e);
                dropBackend();
                Listener l = listener;
                if (l != null) l.onError(e);
            }
        });
    }

    private TvControlBackend ensureBackend() throws IOException {
        if (backend != null) return backend;
        String host = getHost();
        if (host.isEmpty()) throw new IOException("TV address not configured");
        VidaaMqttBackend b = new VidaaMqttBackend(
                VidaaConfig.defaults(host, prefs.getString(KEY_CLIENT_ID, "ocl")));
        b.connect(new TvControlBackend.Callback() {
            @Override
            public void onSourceList(String json) {
                Listener l = listener;
                if (l != null) l.onSourceList(json);
            }

            @Override
            public void onMessage(String topic, String payload) {
                Log.d(TAG, "tv: " + topic + " " + payload);
            }

            @Override
            public void onDisconnected(Exception cause) {
                Log.w(TAG, "tv connection lost", cause);
                io.execute(OclConnection.this::dropBackend);
            }
        });
        backend = b;
        return b;
    }

    private void dropBackend() {
        if (backend != null) backend.close();
        backend = null;
    }
}
