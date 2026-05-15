package org.intelehealth.app.ayu.visit.pocdevice;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
 * AyuConnectDialogFragment — redesigned to match the Digital Auscultation UI.
 *
 * Shows:
 *  • Header with mic icon + title + subtitle
 *  • Description text
 *  • Device connection status bar (dot + status text + SCAN button + spinner)
 *  • Heart Sounds card  → launches SoundFragment for heart
 *  • Lung Sounds card   → launches SoundFragment for lung
 *  • "Continue without recording" button → posts result(true) i.e. skip sounds
 *  • Cancel button → posts result(false)
 *
 * Cards are ENABLED only when device is connected.
 * Status dot turns green when connected, grey when not.
 */
public class AyuConnectDialogFragment extends DialogFragment
        implements AyuDeviceListener, DeviceScanListener {

    public static final String RESULT_KEY       = "ayu_connect_result";
    public static final String RESULT_CONNECTED = "connected";

    // Optional: caller can pass type hint so we pre-highlight a card
    public static final String ARG_TYPE = "type"; // "heart" | "lung" | null = both

    // Views
    private View       viewStatusDot;
    private TextView   tvStatus, tvStatusSub;
    private TextView   btnScan;         // styled as chip
    private ProgressBar pbScanning;
    private ViewGroup  cardHeart, cardLung;
    private TextView   tvHeartSub, tvLungSub;
    private Button     btnContinue, btnCancel;

    private boolean resultPosted = false;

    public static final String RESULT_TYPE = "selected_type";


    // Listener so parent (VisitCreationActivity / PhysExamFragment) can react
    // to which card was tapped before device dialog posts its result
    public interface OnSoundTypeSelectedListener {
        void onHeartSelected();
        void onLungSelected();
    }
    private OnSoundTypeSelectedListener mListener;

    public void setOnSoundTypeSelectedListener(OnSoundTypeSelectedListener l) {
        mListener = l;
    }

    public static AyuConnectDialogFragment newInstance() {
        return new AyuConnectDialogFragment();
    }

    public static AyuConnectDialogFragment newInstance(String type) {
        AyuConnectDialogFragment f = new AyuConnectDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(
                    R.drawable.ui2_rounded_corners_dialog_bg);
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

        // Bind views
        viewStatusDot = view.findViewById(R.id.viewStatusDot);
        tvStatus      = view.findViewById(R.id.tvStatus);
        tvStatusSub   = view.findViewById(R.id.tvStatusSub);
        btnScan       = view.findViewById(R.id.btnScan);
        pbScanning    = view.findViewById(R.id.pbScanning);
        cardHeart     = view.findViewById(R.id.cardHeart);
        cardLung      = view.findViewById(R.id.cardLung);
        tvHeartSub    = view.findViewById(R.id.tvHeartSub);
        tvLungSub     = view.findViewById(R.id.tvLungSub);
        btnContinue   = view.findViewById(R.id.btnContinue);
        btnCancel     = view.findViewById(R.id.btnCancel);

        // Hide cards if caller specified a single type
        String argType = getArguments() != null
                ? getArguments().getString(ARG_TYPE, null) : null;
        if ("heart".equalsIgnoreCase(argType)) {
            cardLung.setVisibility(View.GONE);
        } else if ("lung".equalsIgnoreCase(argType)) {
            cardHeart.setVisibility(View.GONE);
        }
        // null → show both (default)

        // Heart card tap
        cardHeart.setOnClickListener(v -> {
            if (!isDeviceConnected()) {
                Toast.makeText(getContext(),
                        "Please connect AyuSynk device first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mListener != null) mListener.onHeartSelected();
            postResult(true, "heart");   // ← pass "heart"
            dismiss();
        });

        // Lung card tap
        cardLung.setOnClickListener(v -> {
            if (!isDeviceConnected()) {
                Toast.makeText(getContext(),
                        "Please connect AyuSynk device first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mListener != null) mListener.onLungSelected();
            postResult(true, "lung");    // ← pass "lung"
            dismiss();
        });

        // Scan
        btnScan.setOnClickListener(v -> startScanFlow());

        // Continue without recording = skip sounds, go next
        btnContinue.setOnClickListener(v -> {
            postResult(true, null);      // ← no type, just skip
            // true = allow continue, skip sounds
            dismiss();
        });

        // Cancel = go back
        btnCancel.setOnClickListener(v -> {
            postResult(false, null);      // ← no type, just skip
            dismiss();
        });

        // Initial state
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
        postResult(isDeviceConnected());
        try { AyuSynk.getBleInstance().stopScan(); } catch (Exception ignored) {}
        super.onDismiss(dialog);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean isDeviceConnected() {
        return AyuSynk.getBleInstance().isDeviceConnected()
                == DeviceConnectionState.DEVICE_CONNECTED;
    }

    private void postResult(boolean connected) {
        if (resultPosted) return;
        resultPosted = true;
        Bundle out = new Bundle();
        out.putBoolean(RESULT_CONNECTED, connected);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, out);
    }

    /**
     * Updates all status UI based on connection state.
     * Connected  → green dot, cards enabled, subtitle shows "Ready to record"
     * Disconnected → grey dot, cards show tap hint, subtitle shows scan prompt
     */
    private void renderState(DeviceConnectionState state) {
        if (tvStatus == null) return;

        boolean connected = state == DeviceConnectionState.DEVICE_CONNECTED;

        // Status dot color
        viewStatusDot.setBackgroundResource(
                connected ? R.drawable.bg_dot_connected
                        : R.drawable.bg_dot_disconnected);

        // Status text
        tvStatus.setText(connected ? "Device Connected" : "Device Disconnected");
        tvStatusSub.setText(connected
                ? "AyuSynk ready — tap a sound type below"
                : "Scan to connect AyuSynk device");

        // Scan button visibility
        btnScan.setVisibility(connected ? View.GONE : View.VISIBLE);

        // Cards — dim when not connected
        float alpha = connected ? 1.0f : 0.5f;
        cardHeart.setAlpha(alpha);
        cardLung.setAlpha(alpha);

        // Card subtitles
        String cardSub = connected ? "Tap to record" : "Connect device to record";
        tvHeartSub.setText(cardSub);
        tvLungSub.setText(cardSub);

        if (connected) pbScanning.setVisibility(View.GONE);
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
                    R.string.bluetooth_notsupported_device, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!adapter.isEnabled()) {
            Toast.makeText(getContext(),
                    R.string.turn_on_bluetooth, Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("Scanning...");
        pbScanning.setVisibility(View.VISIBLE);
        btnScan.setVisibility(View.GONE);
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

    // ── AyuDeviceListener ────────────────────────────────────────────────────

    @Override
    public void deviceConnectionStrength(DeviceStrength deviceStrength) {

    }

    @Override
    public void deviceConnectionState(DeviceConnectionState state) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> renderState(state));
        }
    }

    @Override public void deviceBatteryUpdate(int level) {}

    // ── DeviceScanListener ───────────────────────────────────────────────────

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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                pbScanning.setVisibility(View.GONE);
                btnScan.setVisibility(View.VISIBLE);
            });
        }
    }
    // In postResult, add a type parameter:
    private void postResult(boolean connected, String selectedType) {
        if (resultPosted) return;
        resultPosted = true;
        Bundle out = new Bundle();
        out.putBoolean(RESULT_CONNECTED, connected);
        if (selectedType != null) {
            out.putString(RESULT_TYPE, selectedType);  // ← NEW
        }
        getParentFragmentManager().setFragmentResult(RESULT_KEY, out);
    }
    @Override
    public void onScanFailed(int i) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                pbScanning.setVisibility(View.GONE);
                btnScan.setVisibility(View.VISIBLE);
                tvStatus.setText("Scan failed — try again");
            });
        }
    }
}
