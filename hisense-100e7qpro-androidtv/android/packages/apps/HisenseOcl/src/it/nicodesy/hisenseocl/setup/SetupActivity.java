package it.nicodesy.hisenseocl.setup;

import android.app.Activity;
import android.content.ComponentName;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import it.nicodesy.hisenseocl.R;
import it.nicodesy.hisenseocl.tv.ChannelSync;
import it.nicodesy.hisenseocl.tv.OclConnection;
import it.nicodesy.hisenseocl.tv.OclTvInputService;

/**
 * TV input setup: enter the panel's IP, pair with the PIN it shows, then import its
 * source list as channels.
 */
public final class SetupActivity extends Activity implements OclConnection.Listener {
    private OclConnection connection;
    private TextView status;
    private String inputId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        connection = OclConnection.get(this);

        inputId = getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
        if (inputId == null) {
            inputId = TvContract.buildInputId(new ComponentName(this, OclTvInputService.class));
        }

        EditText host = findViewById(R.id.host);
        EditText pin = findViewById(R.id.pin);
        status = findViewById(R.id.status);
        host.setText(connection.getHost());

        Button connect = findViewById(R.id.connect);
        connect.setOnClickListener(v -> {
            connection.setHost(host.getText().toString());
            connection.requestSourceList();
            status.setText(R.string.status_connecting);
        });

        Button sendPin = findViewById(R.id.send_pin);
        sendPin.setOnClickListener(v -> {
            connection.sendPairingCode(pin.getText().toString().trim());
            connection.requestSourceList();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        connection.setListener(this);
    }

    @Override
    protected void onPause() {
        connection.setListener(null);
        super.onPause();
    }

    @Override
    public void onSourceList(String json) {
        int count;
        try {
            count = ChannelSync.sync(this, inputId, ChannelSync.parse(json));
        } catch (Exception e) {
            onError(e);
            return;
        }
        runOnUiThread(() -> {
            status.setText(getString(R.string.status_synced, count));
            setResult(RESULT_OK);
        });
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> status.setText(getString(R.string.status_error, e.getMessage())));
    }
}
