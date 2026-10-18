package it.nicodesy.hisenseocl.tv;

import android.content.Context;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

/**
 * Exposes the Hisense panel's sources as an Android TV input. Tuning a channel asks
 * the OEM stack to switch the panel to that source (HDMI 1-4, tuner, ...).
 *
 * In the current hybrid setup the panel itself renders the HDMI/tuner picture, so
 * this session draws only a status overlay. With a native backend the same session
 * would instead route the TV Input HAL stream to the surface.
 */
public final class OclTvInputService extends TvInputService {

    @Override
    public Session onCreateSession(String inputId) {
        Session session = new OclSession(this);
        session.setOverlayViewEnabled(true);
        return session;
    }

    private static final class OclSession extends Session {
        private final Context context;
        private final OclConnection connection;
        private TextView overlay;

        OclSession(Context context) {
            super(context);
            this.context = context;
            this.connection = OclConnection.get(context);
        }

        @Override
        public boolean onTune(Uri channelUri) {
            ChannelSync.Source source = ChannelSync.lookup(context, channelUri);
            if (source == null) {
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN);
                return false;
            }
            connection.changeSource(source.id, source.name);
            notifyContentAllowed();
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            if (overlay != null) overlay.setText(source.label);
            return true;
        }

        @Override
        public View onCreateOverlayView() {
            overlay = new TextView(context);
            overlay.setGravity(Gravity.CENTER);
            overlay.setTextSize(28);
            return overlay;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            String key = RemoteKeys.toVidaa(keyCode);
            if (key == null) return false;
            connection.sendKey(key);
            return true;
        }

        @Override
        public boolean onSetSurface(Surface surface) {
            return true;
        }

        @Override
        public void onSetStreamVolume(float volume) {
            // Panel volume is owned by the TV; Android's per-session volume does not map.
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {}

        @Override
        public void onRelease() {}
    }
}
