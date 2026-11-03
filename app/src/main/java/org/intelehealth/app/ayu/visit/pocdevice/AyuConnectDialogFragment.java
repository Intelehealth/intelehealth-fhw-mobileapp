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

import com.ayudevices.cardiosynksdk.AyuDevice;
import com.ayudevices.cardiosynksdk.ble.Device;
import com.ayudevices.cardiosynksdk.ble.constants.DeviceConnectionState;
import com.ayudevices.cardiosynksdk.ble.constants.DeviceStrength;
import com.ayudevices.cardiosynksdk.ble.listener.AyuDeviceListener;
import com.ayudevices.cardiosynksdk.ble.listener.DeviceScanListener;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.model.HeartLungRecordModel;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;

import java.util.List;

public class AyuConnectDialogFragment extends DialogFragment
        implements AyuDeviceListener, DeviceScanListener {

    public static final String RESULT_KEY = "ayu_connect_result";
    public static final String RESULT_CONNECTED = "connected";
    public static final String RESULT_TYPE = "selected_type";
    public static final String ARG_TYPE = "type";
    public static final String ARG_VISIT_UUID = "visit_uuid";
    public static final String ARG_TOTAL = "total"; // total positions for this type

    public static final String ARG_HEART_TOTAL = "heart_total";  // ADD THIS
    public static final String ARG_LUNG_TOTAL = "lung_total";   // ADD THIS
    public static final String RECORD_SAVED_KEY = "ayu_record_saved";

    // Views
    private View viewStatusDot;
    private TextView tvStatus, tvStatusSub;
    private TextView btnScan;
    private ProgressBar pbScanning;
    private ViewGroup cardHeart, cardLung;
    private TextView tvHeartSub, tvLungSub;
    private ProgressBar pbHeart, pbLung;
    private Button btnContinue, btnCancel;

    private boolean resultPosted = false;
    private String visitUuid = "";

    // FIX: totals come from sounds.size() passed by SoundFragment
    // NOT hardcoded — heart could be 4, lung could be 6/10/16
    private int heartTotal = 0;
    private int lungTotal = 0;


    public interface OnSoundTypeSelectedListener {
        void onHeartSelected();

        void onLungSelected();
    }

    private OnSoundTypeSelectedListener mListener;

    /**
     * Preferred factory — pass total so dialog shows correct "0 / N recorded"
     * total = sounds.size() from SoundFragment (actual positions for this type)
     */
    public static AyuConnectDialogFragment newInstance(String type, String visitUuid, int total) {
        AyuConnectDialogFragment f = new AyuConnectDialogFragment();
        Bundle args = new Bundle();
        if (type != null) args.putString(ARG_TYPE, type);
        if (visitUuid != null) args.putString(ARG_VISIT_UUID, visitUuid);
        args.putInt(ARG_TOTAL, total);
        f.setArguments(args);
        return f;
    }

    // ADD this new factory method alongside existing newInstance() methods
    public static AyuConnectDialogFragment newInstance(
            String type, String visitUuid, int heartTotal, int lungTotal) {
        AyuConnectDialogFragment f = new AyuConnectDialogFragment();
        Bundle args = new Bundle();
        if (type != null) args.putString(ARG_TYPE, type);
        if (visitUuid != null) args.putString(ARG_VISIT_UUID, visitUuid);
        args.putInt(ARG_HEART_TOTAL, heartTotal);
        args.putInt(ARG_LUNG_TOTAL, lungTotal);
        f.setArguments(args);
        return f;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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

        // Read args
        if (getArguments() != null) {
            visitUuid = getArguments().getString(ARG_VISIT_UUID, "");
            String argType = getArguments().getString(ARG_TYPE, "");

            // Try new dual-total args first
            int argHeartTotal = getArguments().getInt(ARG_HEART_TOTAL, 0);
            int argLungTotal = getArguments().getInt(ARG_LUNG_TOTAL, 0);

            if (argHeartTotal > 0 || argLungTotal > 0) {
                // New path — both totals passed directly
                heartTotal = argHeartTotal;
                lungTotal = argLungTotal;
            } else {
                // Legacy fallback — single ARG_TOTAL for active type only
                int argTotal = getArguments().getInt(ARG_TOTAL, 0);
                if ("heart".equalsIgnoreCase(argType)) {
                    heartTotal = argTotal;
                } else if ("lung".equalsIgnoreCase(argType)) {
                    lungTotal = argTotal;
                }
            }
        }

        Log.d("AYU_DIALOG", "onViewCreated: visitUuid=" + visitUuid
                + " | heartTotal=" + heartTotal + " | lungTotal=" + lungTotal);

        // Bind views
        viewStatusDot = view.findViewById(R.id.viewStatusDot);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvStatusSub = view.findViewById(R.id.tvStatusSub);
        btnScan = view.findViewById(R.id.btnScan);
        pbScanning = view.findViewById(R.id.pbScanning);
        cardHeart = view.findViewById(R.id.cardHeart);
        cardLung = view.findViewById(R.id.cardLung);
        tvHeartSub = view.findViewById(R.id.tvHeartSub);
        tvLungSub = view.findViewById(R.id.tvLungSub);
        pbHeart = view.findViewById(R.id.pbHeart);
        pbLung = view.findViewById(R.id.pbLung);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Hide card based on type
        // Show both cards — dim the inactive type, keep active one fully clickable
        String argType = getArguments() != null
                ? getArguments().getString(ARG_TYPE, null) : null;

        setCardActiveState(cardHeart, tvHeartSub, "heart", argType);
        setCardActiveState(cardLung, tvLungSub, "lung", argType);

        // FIX: Listen for RECORD_SAVED_KEY from SoundFragment.
        // Bundle carries type + visitUuid + total so we can update
        // the correct card with live count after each position is saved.
      /*  getParentFragmentManager().setFragmentResultListener(
                RECORD_SAVED_KEY,
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    String savedType = bundle.getString("type", "");
                    String savedVisitUuid = bundle.getString("visitUuid", "");
                    int savedTotal = bundle.getInt("total", 0);

                    // Update visitUuid if it was empty
                    if (!savedVisitUuid.isEmpty() && visitUuid.isEmpty()) {
                        visitUuid = savedVisitUuid;
                    }

                    // FIX: Update the total for this type dynamically
                    if ("heart".equalsIgnoreCase(savedType) && savedTotal > 0) {
                        heartTotal = savedTotal;
                    } else if ("lung".equalsIgnoreCase(savedType) && savedTotal > 0) {
                        lungTotal = savedTotal;
                    }
// Also pull the other type's total from the bundle if sent
                    int bundleHeartTotal = bundle.getInt("heartTotal", 0);
                    int bundleLungTotal = bundle.getInt("lungTotal", 0);
                    if (bundleHeartTotal > 0) heartTotal = bundleHeartTotal;
                    if (bundleLungTotal > 0) lungTotal = bundleLungTotal;

                    Log.d("AYU_DIALOG", "RECORD_SAVED: type=" + savedType
                            + " | total=" + savedTotal
                            + " | visitUuid=" + visitUuid);

                    // Reload counts from DB and refresh cards
                    loadAndDisplayCounts();
                });*/

        // Show initial counts (0/N at start)
        loadAndDisplayCounts();

        // Heart card tap
        // Heart card tap
        cardHeart.setOnClickListener(v -> {
            // If lung is currently active, this click switches to heart
            if (!isDeviceConnected()) {
                Toast.makeText(getContext(),
                        R.string.ayu_connect_device_required, Toast.LENGTH_SHORT).show();
                return;
            }
            // Visually switch active state
            setCardActiveState(cardHeart, tvHeartSub, "heart", "heart");
            setCardActiveState(cardLung, tvLungSub, "lung", "heart");

            if (mListener != null) mListener.onHeartSelected();
            postResult(true, "heart");
            dismiss();
        });

// Lung card tap
        cardLung.setOnClickListener(v -> {
            if (!isDeviceConnected()) {
                Toast.makeText(getContext(),
                        R.string.ayu_connect_device_required, Toast.LENGTH_SHORT).show();
                return;
            }
            // Visually switch active state
            setCardActiveState(cardHeart, tvHeartSub, "heart", "lung");
            setCardActiveState(cardLung, tvLungSub, "lung", "lung");

            if (mListener != null) mListener.onLungSelected();
            postResult(true, "lung");
            dismiss();
        });

        btnScan.setOnClickListener(v -> startScanFlow());
        btnContinue.setOnClickListener(v -> {
            postResult(true, null);
            dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            postResult(false, null);
            dismiss();
        });

        renderState(AyuDevice.getBleInstance().isDeviceConnected());
    }

    /**
     * activeType = the type currently in use ("heart" / "lung" / null = both active).
     * cardType   = which card this is ("heart" or "lung").
     * <p>
     * If activeType is null → both cards fully active.
     * If activeType matches cardType → this card is active (full opacity, clickable).
     * If activeType does NOT match → this card is dimmed (0.4f alpha) but still VISIBLE
     * and still clickable so user can switch types without closing the dialog.
     */
    private void setCardActiveState(ViewGroup card, TextView subText,
                                    String cardType, String activeType) {
        if (card == null) return;

        boolean isActive = activeType == null
                || activeType.equalsIgnoreCase(cardType);

        card.setVisibility(View.VISIBLE);      // always visible — never GONE
        card.setAlpha(isActive ? 1.0f : 0.4f);

        // Optional: show a small hint on the dimmed card
        if (!isActive && subText != null) {
            // Don't overwrite the "X / N recorded" text — just dim it via alpha above
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AyuDevice.getBleInstance().setAyuDeviceListener(this);
        renderState(AyuDevice.getBleInstance().isDeviceConnected());
        loadAndDisplayCounts();
    }

    @Override
    public void onPause() {
        super.onPause();
        AyuDevice.getBleInstance().setAyuDeviceListener(null);
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        postResult(isDeviceConnected());
        try { AyuDevice.getBleInstance().stopScan(); } catch (Exception ignored) {}
        super.onDismiss(dialog);
    }

    // ── Count loading ─────────────────────────────────────────────────────────

    /**
     * Reads DB for this visitUuid, counts heart rows and lung rows separately.
     * Uses heartTotal / lungTotal (set from sounds.size()) for the denominator.
     * Shows "0 / 4 recorded" for heart, "0 / 6 recorded" for lung etc.
     */
    private void loadAndDisplayCounts() {
        if (getContext() == null) return;

        int heartCount = 0;
        int lungCount = 0;

        if (visitUuid != null && !visitUuid.isEmpty()) {
            try {
                InteleHealthDatabaseHelper dbHelper =
                        new InteleHealthDatabaseHelper(getContext());
                List<HeartLungRecordModel> records =
                        dbHelper.getAllHeartLungRecords(visitUuid);

                if (records != null) {
                    for (HeartLungRecordModel r : records) {
                        if ("heart".equalsIgnoreCase(r.type)) heartCount++;
                        else if ("lung".equalsIgnoreCase(r.type)) lungCount++;
                    }
                }
                Log.d("AYU_DIALOG", "DB counts: heart=" + heartCount + "/" + heartTotal
                        + " | lung=" + lungCount + "/" + lungTotal);
            } catch (Exception e) {
                Log.e("AYU_DIALOG", "Error: " + e.getMessage());
            }
        }

        updateHeartCard(heartCount);
        updateLungCard(lungCount);
        updateContinueButtonVisibility(heartCount, lungCount);
    }

    /**
     * "Next" only appears once every sound this protocol requires has been
     * recorded (heart AND lung, whichever totals are non-zero) — while any
     * are still remaining, it stays hidden so the user can't skip past
     * unrecorded positions.
     */
    private void updateContinueButtonVisibility(int heartCount, int lungCount) {
        if (btnContinue == null) return;
        boolean heartDone = heartTotal <= 0 || heartCount >= heartTotal;
        boolean lungDone = lungTotal <= 0 || lungCount >= lungTotal;
        boolean allSoundsDone = (heartTotal > 0 || lungTotal > 0) && heartDone && lungDone;
        btnContinue.setVisibility(allSoundsDone ? View.VISIBLE : View.GONE);
    }

    private void updateHeartCard(int count) {
        if (tvHeartSub == null || pbHeart == null) return;

        // If heartTotal is 0 (not yet known), show just the count
        String label = heartTotal > 0
                ? getString(R.string.sound_recorded_count_format, count, heartTotal)
                : getString(R.string.sound_recorded_count_only_format, count);

        tvHeartSub.setText(label);
        pbHeart.setMax(heartTotal > 0 ? heartTotal : 1);
        pbHeart.setProgress(count);

        boolean done = heartTotal > 0 && count >= heartTotal;
        tvHeartSub.setTextColor(done
                ? android.graphics.Color.parseColor("#2E7D32")  // green — all done
                : android.graphics.Color.parseColor("#888888")); // grey  — in progress
    }

    private void updateLungCard(int count) {
        if (tvLungSub == null || pbLung == null) return;

        String label = lungTotal > 0
                ? getString(R.string.sound_recorded_count_format, count, lungTotal)
                : getString(R.string.sound_recorded_count_only_format, count);

        tvLungSub.setText(label);
        pbLung.setMax(lungTotal > 0 ? lungTotal : 1);
        pbLung.setProgress(count);

        boolean done = lungTotal > 0 && count >= lungTotal;
        tvLungSub.setTextColor(done
                ? android.graphics.Color.parseColor("#2E7D32")
                : android.graphics.Color.parseColor("#888888"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isDeviceConnected() {
        return AyuDevice.getBleInstance().isDeviceConnected()
                == DeviceConnectionState.DEVICE_CONNECTED;
    }

    private void postResult(boolean connected) {
        if (resultPosted) return;
        resultPosted = true;
        Bundle out = new Bundle();
        out.putBoolean(RESULT_CONNECTED, connected);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, out);
    }

    private void postResult(boolean connected, String selectedType) {
        if (resultPosted) return;
        resultPosted = true;
        Bundle out = new Bundle();
        out.putBoolean(RESULT_CONNECTED, connected);
        if (selectedType != null) out.putString(RESULT_TYPE, selectedType);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, out);
    }

    private void renderState(DeviceConnectionState state) {
        if (tvStatus == null) return;
        boolean connected = state == DeviceConnectionState.DEVICE_CONNECTED;
        viewStatusDot.setBackgroundResource(connected
                ? R.drawable.bg_dot_connected : R.drawable.bg_dot_disconnected);
        tvStatus.setText(connected ? R.string.ayu_connect_status_connected : R.string.ayu_connect_status_disconnected);
        tvStatusSub.setText(connected
                ? R.string.ayu_connect_subtext_connected
                : R.string.ayu_connect_subtext_disconnected);
        btnScan.setVisibility(connected ? View.GONE : View.VISIBLE);
        cardHeart.setAlpha(connected ? 1.0f : 0.5f);
        cardLung.setAlpha(connected ? 1.0f : 0.5f);
        if (connected) pbScanning.setVisibility(View.GONE);
    }

    private void startScanFlow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !checkLocation()) return;
        if (!AyuDevice.getBleInstance().isAllBluetoothPermissionGranted()) {
            AyuDevice.getBleInstance().requestBluetoothPermission(getActivity(), 11);
            return;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(getContext(), R.string.bluetooth_notsupported_device, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!adapter.isEnabled()) {
            Toast.makeText(getContext(), R.string.turn_on_bluetooth, Toast.LENGTH_SHORT).show();
            return;
        }
        tvStatus.setText(R.string.ayu_connect_scanning);
        pbScanning.setVisibility(View.VISIBLE);
        btnScan.setVisibility(View.GONE);
        AyuDevice.getBleInstance().startScan(this);
        AyuDevice.getBleInstance().setDeviceScanListener(this);
    }

    private boolean checkLocation() {
        if (!AyuDevice.getBleInstance().isLocationEnabled()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.ayu_connect_location_disabled_title)
                    .setMessage(R.string.ayu_connect_location_disabled_message)
                    .setPositiveButton(R.string.ayu_connect_enable, (d, w) -> startActivity(
                            new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.cancel, (d, w) -> d.cancel())
                    .show();
            return false;
        }
        return true;
    }

    // ── AyuDeviceListener ─────────────────────────────────────────────────────

    @Override
    public void deviceConnectionStrength(DeviceStrength s) {
    }

    @Override
    public void deviceConnectionState(DeviceConnectionState state) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> renderState(state));
    }

    @Override
    public void deviceBatteryUpdate(int level) {
    }

    // ── DeviceScanListener ────────────────────────────────────────────────────

    @Override
    public void onScanStart() {
    }

    @Override
    public void onDeviceFound(Device device) {
        if (device != null && device.getAddress() != null) {
            AyuDevice.getBleInstance().connect(device.getAddress());
            AyuDevice.getBleInstance().stopScan();
        }
    }

    @Override
    public void onScanFinish() {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                pbScanning.setVisibility(View.GONE);
                btnScan.setVisibility(View.VISIBLE);
            });
    }

    @Override
    public void onScanFailed(int i) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                pbScanning.setVisibility(View.GONE);
                btnScan.setVisibility(View.VISIBLE);
                tvStatus.setText(R.string.ayu_connect_scan_failed);
            });
    }
}
