package org.intelehealth.app.ayu.visit.pocdevice;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.ble.Device;
import com.ayudevice.ayusynksdk.ble.constants.DeviceConnectionState;
import com.ayudevice.ayusynksdk.ble.constants.DeviceStrength;
import com.ayudevice.ayusynksdk.ble.listener.AyuDeviceListener;
import com.ayudevice.ayusynksdk.ble.listener.DeviceScanListener;

import org.intelehealth.app.R;

/**
 * Standalone dialog that walks the user through scanning + connecting to an
 * AyuSynk device. When the user clicks Continue (only enabled once connected)
 * it posts a fragment result to whoever opened it.
 *
 * Result key:   {@link #RESULT_KEY}
 * Result bool:  {@link #RESULT_CONNECTED}  (true = ready to record, false = cancelled)
 */
public class AyuConnectDialogFragment extends DialogFragment
        implements AyuDeviceListener, DeviceScanListener {

    public static final String RESULT_KEY = "ayu_connect_result";
    public static final String RESULT_CONNECTED = "connected";

    private TextView tvStatus;
    private ProgressBar pbScanning;
    private Button btnScan, btnContinue, btnCancel;

    private boolean resultPosted = false;

    public static AyuConnectDialogFragment newInstance() {
        return new AyuConnectDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_ayu_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatus    = view.findViewById(R.id.tvStatus);
        pbScanning  = view.findViewById(R.id.pbScanning);
        btnScan     = view.findViewById(R.id.btnScan);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnCancel   = view.findViewById(R.id.btnCancel);

        btnScan.setOnClickListener(v -> startScanFlow());

        btnContinue.setOnClickListener(v -> {
            postResult(true);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            postResult(false);
            dismiss();
        });

        renderState(AyuSynk.getBleInstance().isDeviceConnected());
    }

    @Override
    public void onResume() {
        super.onResume();
        AyuSynk.getBleInstance().setAyuDeviceListener(this);
        renderState(AyuSynk.getBleInstance().isDeviceConnected());
    }

    @Override
    public void onPause() {
        super.onPause();
        AyuSynk.getBleInstance().setAyuDeviceListener(null);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        // If the dialog is dismissed by other means (back button, outside touch),
        // make sure the host always gets a result.
        postResult(AyuSynk.getBleInstance().isDeviceConnected()
                == DeviceConnectionState.DEVICE_CONNECTED);
        try { AyuSynk.getBleInstance().stopScan(); } catch (Exception ignored) {}
        super.onDismiss(dialog);
    }

    private void postResult(boolean connected) {
        if (resultPosted) return;
        resultPosted = true;
        Bundle out = new Bundle();
        out.putBoolean(RESULT_CONNECTED, connected);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, out);
    }

    private void startScanFlow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !checkLocation()) return;

        if (!AyuSynk.getBleInstance().isAllBluetoothPermissionGranted()) {
            AyuSynk.getBleInstance().requestBluetoothPermission(getActivity(), 11);
            return;
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(getContext(),
                    R.string.bluetooth_notsupported_device,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!adapter.isEnabled()) {
            Toast.makeText(getContext(),
                    R.string.turn_on_bluetooth,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("Scanning...");
        pbScanning.setVisibility(View.VISIBLE);
        AyuSynk.getBleInstance().startScan(this);
        AyuSynk.getBleInstance().setDeviceScanListener(this);
    }

    private boolean checkLocation() {
        if (!AyuSynk.getBleInstance().isLocationEnabled()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Location services disabled")
                    .setMessage("BLE scanning requires location services on Android < 12.")
                    .setPositiveButton("Enable", (d, w) -> startActivity(
                            new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", (d, w) -> d.cancel())
                    .show();
            return false;
        }
        return true;
    }

    private void renderState(DeviceConnectionState state) {
        if (tvStatus == null || btnContinue == null || pbScanning == null) return;
        boolean connected = state == DeviceConnectionState.DEVICE_CONNECTED;
        tvStatus.setText(connected ? "Connected" : "Disconnected");
        btnContinue.setEnabled(connected);
        if (connected) pbScanning.setVisibility(View.GONE);
    }

    // --- AyuDeviceListener ---
    @Override public void deviceConnectionStrength(DeviceStrength s) {}

    @Override
    public void deviceConnectionState(DeviceConnectionState state) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> renderState(state));
        }
    }

    @Override public void deviceBatteryUpdate(int level) {}

    // --- DeviceScanListener ---
    @Override public void onScanStart() {}

    @Override
    public void onDeviceFound(Device device) {
        if (device != null && device.getAddress() != null) {
            AyuSynk.getBleInstance().connect(device.getAddress());
            AyuSynk.getBleInstance().stopScan();
        }
    }

    @Override
    public void onScanFinish() {
        if (getActivity() != null && pbScanning != null) {
            getActivity().runOnUiThread(() -> pbScanning.setVisibility(View.GONE));
        }
    }

    @Override
    public void onScanFailed(int i) {
        if (getActivity() != null && pbScanning != null) {
            getActivity().runOnUiThread(() -> {
                pbScanning.setVisibility(View.GONE);
                tvStatus.setText("Scan failed");
            });
        }
    }
}
