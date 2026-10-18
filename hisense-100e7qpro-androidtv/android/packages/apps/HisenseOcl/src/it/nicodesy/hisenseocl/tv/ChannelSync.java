package it.nicodesy.hisenseocl.tv;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Mirrors the TV's source list (HDMI 1-4, TV tuner, AV...) into the Android TV
 * channel database, one channel per source, so the launcher and Live Channels
 * show them next to app content.
 */
public final class ChannelSync {
    private ChannelSync() {}

    /** One TV source. {@code id}/{@code name} are what the TV expects back. */
    public static final class Source {
        public final String id;
        public final String name;
        public final String label;

        Source(String id, String name, String label) {
            this.id = id;
            this.name = name;
            this.label = label;
        }
    }

    /**
     * Parses VIDAA's source list. Field names follow the hisensetv client
     * (sourceid / sourcename / displayname); unknown fields are ignored.
     */
    public static Source[] parse(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        Source[] out = new Source[array.length()];
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            String id = o.optString("sourceid");
            String name = o.optString("sourcename", id);
            String label = o.optString("displayname", "");
            out[i] = new Source(id, name, label.isEmpty() ? name : label);
        }
        return out;
    }

    /** Replaces this input's channels with the given sources. Returns the count written. */
    public static int sync(Context context, String inputId, Source[] sources) {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(TvContract.buildChannelsUriForInput(inputId), null, null);
        int written = 0;
        for (int i = 0; i < sources.length; i++) {
            Source s = sources[i];
            if (s.id.isEmpty()) continue;
            ContentValues v = new ContentValues();
            v.put(TvContract.Channels.COLUMN_INPUT_ID, inputId);
            v.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, Integer.toString(i + 1));
            v.put(TvContract.Channels.COLUMN_DISPLAY_NAME, s.label);
            v.put(TvContract.Channels.COLUMN_TYPE, TvContract.Channels.TYPE_OTHER);
            v.put(TvContract.Channels.COLUMN_SERVICE_TYPE,
                    TvContract.Channels.SERVICE_TYPE_AUDIO_VIDEO);
            v.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, encode(s));
            resolver.insert(TvContract.Channels.CONTENT_URI, v);
            written++;
        }
        return written;
    }

    /** Reads back the source stored on a channel row, or null if the row is gone. */
    public static Source lookup(Context context, Uri channelUri) {
        String[] projection = {TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA};
        try (Cursor c = context.getContentResolver().query(channelUri, projection, null, null,
                null)) {
            if (c == null || !c.moveToFirst()) return null;
            return decode(c.getBlob(0));
        }
    }

    private static byte[] encode(Source s) {
        return (s.id + "\n" + s.name + "\n" + s.label).getBytes(StandardCharsets.UTF_8);
    }

    private static Source decode(byte[] data) {
        if (data == null) return null;
        String[] parts = new String(data, StandardCharsets.UTF_8).split("\n", 3);
        if (parts.length < 3) return null;
        return new Source(parts[0], parts[1], parts[2]);
    }
}
