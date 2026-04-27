package org.intelehealth.app.ayu.visit.hba1c;

import android.Manifest;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import org.intelehealth.app.R;
import org.intelehealth.klivekit.utils.Constants;

import java.util.ArrayList;

import biosense.sreyasvpariyath.com.biosenselib.helper.Communicator;
import biosense.sreyasvpariyath.com.biosenselib.helper.ControlCentre;

public class ResultActivity extends AppCompatActivity implements Communicator {

    ControlCentre controlCentre;

    private TextView tvDeviceName;
    private TextView tvDeviceAddress;
    private TextView tvConnectionStatus;
    private android.view.View statusDot;
    private TextView tvHbA1cValue;
    private TextView tvTimestamp;
    private TextView tvSerialNo;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvStatusLog;
    private StringBuilder logBuilder = new StringBuilder();
    private boolean firstReadingReceived = false;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Measurement Result");
        }

        // Bind views
        tvDeviceName       = findViewById(R.id.tvDeviceName);
        tvDeviceAddress    = findViewById(R.id.tvDeviceAddress);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        statusDot          = findViewById(R.id.statusDot);
        tvHbA1cValue       = findViewById(R.id.tvHbA1cValue);
        tvTimestamp        = findViewById(R.id.tvTimestamp);
        tvSerialNo         = findViewById(R.id.tvSerialNo);
        tvDate             = findViewById(R.id.tvDate);
        tvTime             = findViewById(R.id.tvTime);
        tvStatusLog        = findViewById(R.id.tvStatusLog);

        // Get device info from Intent
        String deviceAddress = getIntent().getStringExtra("DEVICE_ADDRESS");
        String deviceName    = getIntent().getStringExtra("DEVICE_NAME");
        if (deviceAddress == null) deviceAddress = "";
        if (deviceName == null)    deviceName = "HbA1c Device";

        tvDeviceName.setText(deviceName);
        tvDeviceAddress.setText(deviceAddress);
        tvHbA1cValue.setText("—");

        appendLog("Connecting to " + deviceName + " [" + deviceAddress + "]");
        appendLog("ℹ Tip: After first sync, press device button ONCE MORE to see result.");

        // NOTE: Permissions already granted in SplashActivity/BleSearchActivity.
        // Do NOT call requestPermissions() here — it interferes with BLE startReceiver() timing.

        // Initialize ControlCentre with the selected device
      /*  controlCentre = new ControlCentre(
                this,                    // Communicator
                this,                    // Context
                this,                    // Activity
                deviceAddress,
                Constants.devId_A1Chek,  // HbA1c device
                deviceName
        );

        controlCentre.startReceiver();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controlCentre != null) {
            controlCentre.stopReceiver();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ──────────────────────────────────────────────
    // Communicator Callbacks
    // ──────────────────────────────────────────────

    @Override
    public void setConnectionStatus(String status, boolean isConnected) {
        runOnUiThread(() -> {
            tvConnectionStatus.setText(status);
            // Green dot when connected, red when disconnected
            GradientDrawable dot = (GradientDrawable) statusDot.getBackground();
            dot.setColor(isConnected ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            appendLog("Status: " + status);
        });
    }

    @Override
    public void setHbA1cReading(String reading, String date, String time, String srno) {
        runOnUiThread(() -> {
            firstReadingReceived = true;
            tvHbA1cValue.setText(reading);
            tvDate.setText(date.isEmpty() ? "—" : date);
            tvTime.setText(time.isEmpty() ? "—" : time);
            tvSerialNo.setText(srno.isEmpty() ? "—" : srno);
            tvTimestamp.setText(date.isEmpty() ? "" : "Measured on " + date + " at " + time);
            tvConnectionStatus.setText("Result received ✓");
            appendLog("✅ HbA1c: " + reading + "% | S/N: " + srno);
        });
    }

    @Override
    public void setHB(String s) {
        runOnUiThread(() -> appendLog("Hemoglobin: " + s));
    }

    @Override
    public void setBPReading(String systolic, String diastolic, String pulse) {
        runOnUiThread(() -> appendLog("BP: " + systolic + "/" + diastolic + " Pulse: " + pulse));
    }

    @Override
    public void onBpDeviceError() {
        runOnUiThread(() -> appendLog("BP device error"));
    }

    @Override
    public boolean go(String s) {
        // 'go()' fires during the FIRST BLE data frame (handshake/device-name frame).
        // The library needs this first burst to learn the device serial,
        // and only calls setHbA1cReading() on the SECOND burst.
        if (!firstReadingReceived) {
            runOnUiThread(() -> {
                appendLog("📡 Device handshake received.");
                appendLog("👉 Press the button on device ONCE MORE to get result.");
            });
        }
        return false;
    }

    @Override
    public void setGlucoseReading(String text) {
        runOnUiThread(() -> appendLog("Glucose: " + text));
    }

    @Override
    public void testStarted(boolean b) {
        runOnUiThread(() -> appendLog(b ? "Test started" : "Test stopped"));
    }

    @Override
    public void stopNotiFication() {
        runOnUiThread(() -> appendLog("Notification stopped"));
    }

    @Override
    public void setSwitchActivity() { }

    @Override
    public void setBatteryLevel(int level) {
        runOnUiThread(() -> appendLog("Battery: " + level + "%"));
    }

    @Override
    public void setManufacturerName(String s) {
        runOnUiThread(() -> appendLog("Manufacturer: " + s));
    }

    @Override
    public void setSerialNumber(String s) {
        runOnUiThread(() -> {
            tvSerialNo.setText(s);
            appendLog("Serial: " + s);
        });
    }

    @Override
    public void setModelNumber(String s) {
        runOnUiThread(() -> appendLog("Model: " + s));
    }

    @Override
    public void getOfflineResults(ArrayList<String> results) {
        runOnUiThread(() -> {
            if (results != null && !results.isEmpty()) {
                appendLog("Offline results (" + results.size() + "):");
                for (String r : results) appendLog("  " + r);
            }
        });
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private void appendLog(String message) {
        logBuilder.insert(0, message + "\n");
        tvStatusLog.setText(logBuilder.toString());
    }
}