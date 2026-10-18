package it.nicodesy.hisenseocl.tv;

import android.view.KeyEvent;

/**
 * Android key codes that should be forwarded to the panel while a Hisense source is
 * on screen. Key names follow the hisensetv client (SOURCES.md S-40).
 */
final class RemoteKeys {
    private RemoteKeys() {}

    static String toVidaa(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                return "KEY_VOLUMEUP";
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return "KEY_VOLUMEDOWN";
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return "KEY_MUTE";
            case KeyEvent.KEYCODE_CHANNEL_UP:
                return "KEY_CHANNELUP";
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                return "KEY_CHANNELDOWN";
            case KeyEvent.KEYCODE_POWER:
                return "KEY_POWER";
            default:
                return null;
        }
    }
}
