package org.intelehealth.app.ayu.visit.diagnostics;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.hba1c.BleScanActivity;
import org.intelehealth.app.ayu.visit.hba1c.HbA1cLiveViewModel;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.vital.CoroutineProvider;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.databinding.FragmentDiagnosticsCollectionBinding;
import org.intelehealth.app.models.DiagnosticsModel;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository;
import org.intelehealth.config.presenter.fields.factory.DiagnosticsViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.Diagnostics;
import org.intelehealth.config.utility.PatientDiagnosticsConfigKeys;

import java.util.List;

public class DiagnosticsCollectionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG     = DiagnosticsCollectionFragment.class.getSimpleName();
    private static final int    REQ_BLE = 100;

    // ── Instance fields ───────────────────────────────────────────────────────
    private VisitCreationActionListener mActionListener;
    private String patientName       = "";
    private String patientGender     = "";
    private String intentTag, state, patientUuid, visitUuid, encounterVitals;
    private String encounterAdultIntials             = "";
    private String EncounterAdultInitial_LatestVisit = "";
    private SessionManager    sessionManager;
    private DiagnosticsModel  results     = new DiagnosticsModel();
    private boolean           mIsEditMode = false;
    private List<Diagnostics> mPatientDiagnosticsList;

    private StringBuilder mDebugLog = new StringBuilder();
    private FragmentDiagnosticsCollectionBinding mBinding;


    private HbA1cLiveViewModel mHba1cVm;
    private CommonVisitData mCommonVisitData;

    // ── Constructor / factory ─────────────────────────────────────────────────

    public DiagnosticsCollectionFragment() {}

    public static DiagnosticsCollectionFragment newInstance(
            CommonVisitData d, boolean isEditMode, DiagnosticsModel model) {
        DiagnosticsCollectionFragment f = new DiagnosticsCollectionFragment();
        f.mIsEditMode = isEditMode;
        f.results     = model;
        f.patientUuid                       = d.getPatientUuid();
        f.visitUuid                         = d.getVisitUuid();
        f.encounterVitals                   = d.getEncounterUuidVitals();
        f.encounterAdultIntials             = d.getEncounterUuidAdultIntial();
        f.EncounterAdultInitial_LatestVisit = d.getEncounterAdultInitialLatestVisit();
        f.state                             = d.getState();
        f.patientName                       = d.getPatientName();
        f.patientGender                     = d.getPatientGender();
        f.intentTag                         = d.getIntentTag();
        f.mCommonVisitData                  = d;
        return f;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager  = new SessionManager(context);
        new ConfigUtils(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_diagnostics_collection, container, false);

        mBinding.tvGlucoseRandomError.setVisibility(View.GONE);
        mBinding.tvGlucoseFastingError.setVisibility(View.GONE);
        mBinding.etvPostPrandialError.setVisibility(View.GONE);
        mBinding.etvUricAcidError.setVisibility(View.GONE);
        mBinding.etvCholestrolError.setVisibility(View.GONE);
        mBinding.tvHemoglobinError.setVisibility(View.GONE);
        mBinding.tvDiabetesHba1cError.setVisibility(View.GONE);

        // Live-update views — hidden until BLE connects
        mBinding.tvHba1cLiveBadge.setVisibility(View.GONE);
        mBinding.tvHba1cLastUpdated.setVisibility(View.GONE);

        //mBinding.etvNonFastingGlucose.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvNonFastingGlucose));
        mBinding.etvGlucoseRandom.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvGlucoseRandom));
        mBinding.etvGlucoseFasting.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvGlucoseFasting));
        mBinding.etvPostPrandial.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvPostPrandial));
        mBinding.etvHemoglobin.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvHemoglobin));
        mBinding.etvUricAcid.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvUricAcid));
        mBinding.etvCholesterol.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvCholesterol));
        mBinding.etvDiabetesHba1c.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvDiabetesHba1c));

        mBinding.btnSubmit.setOnClickListener(this);
        mBinding.btnCancel.setOnClickListener(this);

        if (mIsEditMode && results == null) loadSavedDateForEditFromDB();

        return mBinding.getRoot();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Get Activity-scoped ViewModel ─────────────────────────────────
        // requireActivity() ensures we get the SAME instance as the Activity
        // and every other fragment — BLE connection is shared, not per-fragment.
        mHba1cVm = new ViewModelProvider(requireActivity())
                .get(HbA1cLiveViewModel.class);

        // ── Observe HbA1c reading — auto-updates field whenever value arrives ─
        // This fires immediately with the last known value if BLE was already
        // running before this fragment was created (user navigated back, etc.)
        mHba1cVm.hba1cReading().observe(getViewLifecycleOwner(), reading -> {
            if (reading == null || mBinding == null) return;

            // Populate the field
            mBinding.etvDiabetesHba1c.setText(reading);
            mBinding.etvDiabetesHba1c.setSelection(reading.length());

            // Auto-save into model — Submit will always have the latest value
            if (results == null) results = new DiagnosticsModel();
            results.setDiabetesbba1c(reading);

            // Keep the shared CommonVisitData in sync too — same object the
            // Activity holds, so this reaches final submission automatically.
            if (mCommonVisitData != null) {
                mCommonVisitData.setDiabetesbba1c(reading);
            }

            // Green flash to signal live update
            flashField(mBinding.etvDiabetesHba1c);

            isValidForm();
            Log.d(TAG, "hba1cReading observer: updated to " + reading);
        });

        // ── Observe timestamp ─────────────────────────────────────────────
        mHba1cVm.lastUpdatedAt().observe(getViewLifecycleOwner(), ts -> {
            if (ts == null || mBinding == null) return;
            mBinding.tvHba1cLastUpdated.setVisibility(View.VISIBLE);
            mBinding.tvHba1cLastUpdated.setText("Last updated " + ts);
        });

        // ── Observe connection status ─────────────────────────────────────
        mHba1cVm.connected().observe(getViewLifecycleOwner(), connected -> {
            if (mBinding == null) return;
            boolean ready = mHba1cVm.readyToReceive().getValue() != null
                    && mHba1cVm.readyToReceive().getValue();
            updateConnectionStatus(connected != null && connected, ready);
        });

        // ── Observe "awaiting 2nd button press" — fires after go() on the
        // Activity. The device sends frame 1 on the 1st press but does NOT
        // call setHbA1cReading() until the 2nd press. Surface that to the
        // user so they don't think the device is stuck.
        mHba1cVm.readyToReceive().observe(getViewLifecycleOwner(), ready -> {
            if (mBinding == null) return;
            boolean isConnected = mHba1cVm.connected().getValue() != null
                    && mHba1cVm.connected().getValue();
            updateConnectionStatus(isConnected, ready != null && ready);
        });

        // ── Diagnostics config ────────────────────────────────────────────
        DiagnosticsRepository repository = new DiagnosticsRepository(
                ConfigDatabase.getInstance(requireActivity()).patientDiagnosticsDao());
        DiagnosticsViewModel vm = new ViewModelProvider(this,
                new DiagnosticsViewModelFactory(repository))
                .get(DiagnosticsViewModel.class);

        CoroutineProvider.usePatientDiagnosticsScope(
                LifecycleOwnerKt.getLifecycleScope(this), vm,
                data -> { mPatientDiagnosticsList = (List<Diagnostics>) data; updateUI(); });

        // ── Scan button — only needed if no device saved yet ──────────────
        mBinding.btnScanDevice.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(getActivity(), BleScanActivity.class), REQ_BLE));
        if (BuildConfig.DEBUG) {
            mBinding.tvDebugLog.setVisibility(View.VISIBLE);
            mBinding.btnCopyDebugLog.setVisibility(View.VISIBLE);
            mBinding.btnEmailDebugLog.setVisibility(View.VISIBLE);

            appendDebugLog("=== HbA1c Debug Session Started ===");
            appendDebugLog("Device model: " + android.os.Build.MODEL);
            appendDebugLog("Android version: " + android.os.Build.VERSION.RELEASE);
            appendDebugLog("App version: " + BuildConfig.VERSION_NAME);

            mHba1cVm.connected().observe(getViewLifecycleOwner(), c ->
                    appendDebugLog("connected = " + c));

            mHba1cVm.hba1cReading().observe(getViewLifecycleOwner(), r ->
                    appendDebugLog("hba1cReading = " + r));

            mHba1cVm.lastUpdatedAt().observe(getViewLifecycleOwner(), ts ->
                    appendDebugLog("lastUpdatedAt = " + ts));

            mHba1cVm.readyToReceive().observe(getViewLifecycleOwner(), r ->
                    appendDebugLog("readyToReceive = " + r));

            mBinding.btnCopyDebugLog.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) requireActivity()
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText("debug_log", mDebugLog.toString()));
                Toast.makeText(getContext(), "Log copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            mBinding.btnEmailDebugLog.setOnClickListener(v -> emailDebugLogFile());
        }
        manageBackButtonVisibility();
    }

    @Override public void onResume() { super.onResume(); initData(); }

    @Override
    public void onDestroyView() {
        // Do NOT stop the BLE connection here — it lives in the Activity
        // (ControlCentre) and must keep running across fragment transactions
        // so the value is ready on the summary screen. The Activity calls
        // mControlCentre.stopReceiver() in its own onDestroy().
        stopLivePulse();
        mBinding = null;
        super.onDestroyView();
    }
    private void appendDebugLog(String line) {
        String ts = new java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
                .format(new java.util.Date());
        mDebugLog.append("[").append(ts).append("] ").append(line).append("\n");
        if (mBinding != null && mBinding.tvDebugLog != null) {
            mBinding.tvDebugLog.setText(mDebugLog.toString());
        }
    }
    // ── Activity result (manual scan fallback) ────────────────────────────────

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_BLE || resultCode != RESULT_OK || data == null) return;

        String address = data.getStringExtra("device_address");
        if (address == null || address.isEmpty()) {
            android.widget.Toast.makeText(
                    getContext(), "No device selected", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Hand off to Activity which saves the address and starts ControlCentre
        // — not a new local thread/connection.
        ((VisitCreationActivity) requireActivity()).saveAndStartBleDevice(address);
    }

    // ── Connection-status UI ──────────────────────────────────────────────────

    private void updateConnectionStatus(boolean isConnected, boolean readyToReceive) {
        if (mBinding == null) return;

        boolean hasReading = mHba1cVm != null
                && mHba1cVm.hba1cReading().getValue() != null
                && !mHba1cVm.hba1cReading().getValue().isEmpty();

        if (isConnected && readyToReceive && !hasReading) {
            mBinding.tvConnectionStatus.setText("Ready — you may start the test now");
            mBinding.tvConnectionStatus.setTextColor(0xFF2E7D32);
            mBinding.statusDot.setBackgroundResource(R.color.btn_background);
            mBinding.tvHba1cLiveBadge.setVisibility(View.VISIBLE);
            startLivePulse();
            mBinding.btnScanDevice.setEnabled(false);
            mBinding.btnScanDevice.setAlpha(0.5f);
            mBinding.etvDiabetesHba1c.setFocusable(true);
            mBinding.etvDiabetesHba1c.setFocusableInTouchMode(true);
        } else if (isConnected && !readyToReceive) {
            mBinding.tvConnectionStatus.setText("Connecting — please wait before starting the test");
            mBinding.tvConnectionStatus.setTextColor(0xFFFF9800);
            mBinding.statusDot.setBackgroundResource(R.color.btn_background);
            mBinding.tvHba1cLiveBadge.setVisibility(View.VISIBLE);
            startLivePulse();
            mBinding.btnScanDevice.setEnabled(false);
            mBinding.btnScanDevice.setAlpha(0.5f);
            mBinding.etvDiabetesHba1c.setFocusable(true);
            mBinding.etvDiabetesHba1c.setFocusableInTouchMode(true);
        } else if (isConnected) {
            mBinding.tvConnectionStatus.setText("Connected");
            mBinding.tvConnectionStatus.setTextColor(0xFF000000);
            mBinding.statusDot.setBackgroundResource(R.color.btn_background);
            mBinding.tvHba1cLiveBadge.setVisibility(View.VISIBLE);
            startLivePulse();
            mBinding.btnScanDevice.setEnabled(false);
            mBinding.btnScanDevice.setAlpha(0.5f);
        } else {
            mBinding.tvConnectionStatus.setText("Disconnected");
            mBinding.tvConnectionStatus.setTextColor(0xFFD32F2F);
            mBinding.statusDot.setBackgroundResource(R.color.red);
            mBinding.tvHba1cLiveBadge.setVisibility(View.GONE);
            stopLivePulse();
            mBinding.btnScanDevice.setEnabled(true);
            mBinding.btnScanDevice.setAlpha(1.0f);
            mBinding.etvDiabetesHba1c.setFocusable(true);
            mBinding.etvDiabetesHba1c.setFocusableInTouchMode(true);
            mBinding.tvHba1cLastUpdated.setVisibility(View.GONE);
        }

        Log.d(TAG, "updateConnectionStatus → connected=" + isConnected
                + " ready=" + readyToReceive + " hasReading=" + hasReading);
    }

    private void startLivePulse() {
        if (mBinding == null) return;
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.2f);
        pulse.setDuration(800);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        mBinding.tvHba1cLiveBadge.startAnimation(pulse);
    }

    private void stopLivePulse() {
        if (mBinding == null) return;
        mBinding.tvHba1cLiveBadge.clearAnimation();
    }

    private void flashField(View v) {
        ObjectAnimator.ofArgb(v, "backgroundColor", 0x4400C853, 0x00000000)
                .setDuration(600)
                .start();
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void updateUI() {
        mBinding.llGlucoseRandomContainer.setVisibility(View.GONE);
        mBinding.llGlusoseFastingContainer.setVisibility(View.GONE);
        mBinding.llPostPrandialContainer.setVisibility(View.GONE);
        mBinding.llHemoglobinContainer.setVisibility(View.GONE);
        mBinding.llUricAcidContainer.setVisibility(View.GONE);
        mBinding.llCholestrolContainer.setVisibility(View.GONE);
        mBinding.llDiabetesHba1cContainer.setVisibility(View.GONE);

        for (Diagnostics diagnostics : mPatientDiagnosticsList) {
            CustomLog.v(TAG, diagnostics.getName() + "\t" + diagnostics.getDiagnosticsKey());

            if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.RANDOM_BLOOD_SUGAR)) {
                mBinding.llGlucoseRandomContainer.setVisibility(View.VISIBLE);
                mBinding.llGlucoseRandomContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvGlucoseRandomLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.FASTING_BLOOD_SUGAR)) {
                mBinding.llGlusoseFastingContainer.setVisibility(View.VISIBLE);
                mBinding.llGlusoseFastingContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvGlusoseFastingLbl);
            }/* else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.BLOOD_GLUCOSE)) {
                mBinding.llNonFastingContainer.setVisibility(View.VISIBLE);
                mBinding.llNonFastingContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvNonFastingLbl);
            }*/ else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.POST_PRANDIAL_BLOOD_SUGAR)) {
                mBinding.llPostPrandialContainer.setVisibility(View.VISIBLE);
                mBinding.llPostPrandialContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvPostPrandialLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.HEAMOGLOBIN)) {
                mBinding.llHemoglobinContainer.setVisibility(View.VISIBLE);
                mBinding.llHemoglobinContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvHemoglobinLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.URIC_ACID)) {
                mBinding.llUricAcidContainer.setVisibility(View.VISIBLE);
                mBinding.llUricAcidContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvUricAcidLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.TOTAL_CHOLESTEROL)) {
                mBinding.llCholestrolContainer.setVisibility(View.VISIBLE);
                mBinding.llCholestrolContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvCholestrolLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.DIABETES_HBA1C)) {
                mBinding.llDiabetesHba1cContainer.setVisibility(View.VISIBLE);
                mBinding.llDiabetesHba1cContainer.setTag(diagnostics);
                appendMandatory(diagnostics.isMandatory(), mBinding.tvDiabetesHba1cLabel);
            }
        }
    }

    private void appendMandatory(boolean mandatory, TextView tv) {
        if (mandatory) tv.append("*");
    }

    private void initData() {
        if (results == null) return;
        setIfNotEmpty(mBinding.etvGlucoseRandom,   results.getBloodGlucoseRandom());
        setIfNotEmpty(mBinding.etvGlucoseFasting,  results.getBloodGlucoseFasting());
        setIfNotEmpty(mBinding.etvPostPrandial,    results.getBloodGlucosePostPrandial());
        setIfNotEmpty(mBinding.etvHemoglobin,      results.getHemoglobin());
        setIfNotEmpty(mBinding.etvUricAcid,        results.getUricAcid());
        setIfNotEmpty(mBinding.etvCholesterol,     results.getCholesterol());
        setIfNotEmpty(mBinding.etvDiabetesHba1c,   results.getDiabetesbba1c());
    }

    private void setIfNotEmpty(EditText et, String val) {
        if (val != null && !val.isEmpty()) et.setText(val);
    }

    private boolean isValidForm() {
        boolean isValid = true;

        // Utility method to validate each field
        isValid &= validateField(
                mBinding.etvGlucoseRandom.getText().toString().trim(),
                (Diagnostics) mBinding.llGlucoseRandomContainer.getTag(),
                mBinding.tvGlucoseRandomError,
                mBinding.etvGlucoseRandom,
                R.string.error_field_required,
                R.string.glucose_random_error,
                AppConstants.MINIMUM_GLUCOSE_RANDOM,
                AppConstants.MAXIMUM_GLUCOSE_RANDOM
        );

        isValid &= validateField(
                mBinding.etvGlucoseFasting.getText().toString().trim(),
                (Diagnostics) mBinding.llGlusoseFastingContainer.getTag(),
                mBinding.tvGlucoseFastingError,
                mBinding.etvGlucoseFasting,
                R.string.error_field_required,
                R.string.glucose_fasting_error,
                AppConstants.MINIMUM_GLUCOSE_FASTING,
                AppConstants.MAXIMUM_GLUCOSE_FASTING
        );

        isValid &= validateField(
                mBinding.etvPostPrandial.getText().toString().trim(),
                (Diagnostics) mBinding.llPostPrandialContainer.getTag(),
                mBinding.etvPostPrandialError,
                mBinding.etvPostPrandial,
                R.string.error_field_required,
                R.string.post_prandial_error,
                AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL,
                AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL
        );

        isValid &= validateField(
                mBinding.etvHemoglobin.getText().toString().trim(),
                (Diagnostics) mBinding.llHemoglobinContainer.getTag(),
                mBinding.tvHemoglobinError,
                mBinding.etvHemoglobin,
                R.string.error_field_required,
                R.string.hemoglobin_error,
                AppConstants.MINIMUM_HEMOGLOBIN,
                AppConstants.MAXIMUM_HEMOGLOBIN
        );

        isValid &= validateField(
                mBinding.etvUricAcid.getText().toString().trim(),
                (Diagnostics) mBinding.llUricAcidContainer.getTag(),
                mBinding.etvUricAcidError,
                mBinding.etvUricAcid,
                R.string.error_field_required,
                R.string.uric_acid_error,
                AppConstants.MINIMUM_URIC_ACID,
                AppConstants.MAXIMUM_URIC_ACID
        );

        isValid &= validateField(
                mBinding.etvCholesterol.getText().toString().trim(),
                (Diagnostics) mBinding.llCholestrolContainer.getTag(),
                mBinding.etvCholestrolError,
                mBinding.etvCholesterol,
                R.string.error_field_required,
                R.string.cholestrol_acid_error,
                AppConstants.MINIMUM_TOTAL_CHOLSTEROL,
                AppConstants.MAXIMUM_TOTAL_CHOLSTEROL
        );
        isValid &= validateField(
                mBinding.etvDiabetesHba1c.getText().toString().trim(),
                (Diagnostics) mBinding.llDiabetesHba1cContainer.getTag(),
                mBinding.tvDiabetesHba1cError,
                mBinding.etvDiabetesHba1c,
                R.string.error_field_required,
                R.string.diabeteshba1c_error,
                AppConstants.MINIMUM_TOTAL_DIABETES_HBA1C,
                AppConstants.MAXIMUM_TOTAL_DIABETES_HBA1C
        );

        return isValid;
    }

    private boolean validateField(String value, Diagnostics diagnosticsTag, TextView errorTextView, EditText editText, int mandatoryErrorRes, int rangeErrorRes, String minValue, String maxValue) {
        if (diagnosticsTag != null && diagnosticsTag.isMandatory() && value.isEmpty()) {
            errorTextView.setText(getString(mandatoryErrorRes));
            errorTextView.setVisibility(View.VISIBLE);
            editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        }

        if (!value.isEmpty()) {
            double numericValue = Double.parseDouble(value);
            double min = Double.parseDouble(minValue);
            double max = Double.parseDouble(maxValue);

            if (numericValue < min || numericValue > max) {
                errorTextView.setText(getString(rangeErrorRes, minValue, maxValue));
                errorTextView.setVisibility(View.VISIBLE);
                editText.requestFocus();
                editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            }
        }

        // Clear any previous errors
        errorTextView.setVisibility(View.GONE);
        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        return true;
    }

    private void manageBackButtonVisibility() {
        boolean vitals =
                ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus().getVitalSection();
        mBinding.btnCancel.setVisibility(vitals ? View.VISIBLE : View.GONE);
        LinearLayout.LayoutParams p =
                (LinearLayout.LayoutParams) mBinding.btnSubmit.getLayoutParams();
        if (mBinding.btnCancel.getVisibility() == View.GONE) {
            p.width = LinearLayout.LayoutParams.MATCH_PARENT; p.weight = 0f;
            p.setMargins(0, p.topMargin, p.rightMargin, p.bottomMargin);
        } else {
            p.width = 0; p.weight = 1f;
            p.setMargins(16, p.topMargin, p.rightMargin, p.bottomMargin);
        }
        mBinding.btnSubmit.setLayoutParams(p);
    }

    // ── Click handler ─────────────────────────────────────────────────────────

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {
            mBinding.btnSubmit.setClickable(false);
            boolean valid = isValidForm();
            if (valid) {
                isDataReadyForSaving();
                mActionListener.onProgress(100);
                mActionListener.onFormSubmitted(
                        VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY, mIsEditMode, results);
            }
            setDisabledSubmit(!valid);
        }
        if (view.getId() == R.id.btn_cancel) {
            mActionListener.onFormSubmitted(
                    VisitCreationActivity.STEP_1_VITAL_SUMMARY, false, null);
        }
    }

    private void setDisabledSubmit(boolean disable) {
        mBinding.btnSubmit.setClickable(!disable);
        mBinding.btnSubmit.setEnabled(!disable);
    }

    // ── TextWatcher ───────────────────────────────────────────────────────────

    class MyTextWatcher implements android.text.TextWatcher {
        private final EditText et;
        MyTextWatcher(EditText et) { this.et = et; }
        @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
        @Override public void afterTextChanged(android.text.Editable e) {
            String v = e.toString().trim();
            if (v.equals(".")) { et.setText(""); return; }
            setDisabledSubmit(!isValidForm());
        }
    }

    // ── DB helpers ────────────────────────────────────────────────────────────

    public void loadSavedDateForEditFromDB() {
        SQLiteDatabase db =
                IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor c = db.query("tbl_obs", new String[]{"value", "conceptuuid"},
                "encounteruuid=? and voided!='1'", new String[]{encounterVitals},
                null, null, null);
        if (c.moveToFirst()) do {
            parseData(c.getString(c.getColumnIndex("conceptuuid")),
                    c.getString(c.getColumnIndex("value")));
        } while (c.moveToNext());
        c.close();
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.BLOOD_GLUCOSE_RANDOM:
                if (value != null && !value.isEmpty())
                    mBinding.etvGlucoseRandom.setText(value);
                break;
            case UuidDictionary.BLOOD_GLUCOSE_FASTING: //Pulse
                if (value != null && !value.isEmpty())
                    mBinding.etvGlucoseFasting.setText(value);
                break;
           /* case UuidDictionary.BLOOD_GLUCOSE: //Pulse
                if (value != null && !value.isEmpty())
                    mBinding.etvNonFastingGlucose.setText(value);
                break;*/
            case UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL: //Pulse
                if (value != null && !value.isEmpty())
                    mBinding.etvPostPrandial.setText(value);
                break;
            case UuidDictionary.HEMOGLOBIN: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvHemoglobin.setText(value);
                break;
            case UuidDictionary.URIC_ACID: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvUricAcid.setText(value);
                break;
            case UuidDictionary.TOTAL_CHOLESTEROL: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvCholesterol.setText(value);
                break;
            case UuidDictionary.DIABETES_HBA1C: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvDiabetesHba1c.setText(value);
                break;
            default:
                break;

        }
    }

    // ── Save / persist ────────────────────────────────────────────────────────

    public boolean isDataReadyForSaving() {
        try {
            if (results == null) results = new DiagnosticsModel();
            results.setBloodGlucoseRandom(mBinding.etvGlucoseRandom.getText().toString());
            results.setBloodGlucoseFasting(mBinding.etvGlucoseFasting.getText().toString());
            results.setBloodGlucosePostPrandial(mBinding.etvPostPrandial.getText().toString());
            results.setHemoglobin(mBinding.etvHemoglobin.getText().toString());
            results.setUricAcid(mBinding.etvUricAcid.getText().toString());
            results.setCholesterol(mBinding.etvCholesterol.getText().toString());
            // ── HbA1c value resolution — 3-tier fallback ─────────────────────────
            String vmReading   = mHba1cVm != null ? mHba1cVm.hba1cReading().getValue() : null;
            String fieldVal    = mBinding.etvDiabetesHba1c.getText().toString().trim();
            String modelVal    = results != null ? results.getDiabetesbba1c() : null;

// Priority: ViewModel > field text > what observer already stored in model
            String hba1cFinal  = null;

            if (vmReading != null && !vmReading.isEmpty()) {
                hba1cFinal = vmReading;
                Log.d(TAG, "HbA1c source: ViewModel → " + hba1cFinal);
            } else if (fieldVal != null && !fieldVal.isEmpty()) {
                hba1cFinal = fieldVal;
                Log.d(TAG, "HbA1c source: field text → " + hba1cFinal);
            } else if (modelVal != null && !modelVal.isEmpty()) {
                // Observer already stored it in results — DO NOT overwrite with empty
                hba1cFinal = modelVal;
                Log.d(TAG, "HbA1c source: model (from observer) → " + hba1cFinal);
            } else {
                // Last resort — SharedPreferences (survives process death)
                android.content.SharedPreferences prefs =
                        requireActivity().getSharedPreferences("hba1c_prefs", Context.MODE_PRIVATE);
                String savedReading = prefs.getString("hba1c_last_reading", null);
                String savedVisit   = prefs.getString("hba1c_last_reading_visit", null);
                if (savedReading != null && !savedReading.isEmpty()
                        && visitUuid != null && visitUuid.equals(savedVisit)) {
                    hba1cFinal = savedReading;
                    Log.d(TAG, "HbA1c source: SharedPreferences → " + hba1cFinal);
                }
            }

// Only update results if we actually found a value — never overwrite with null/empty
            if (hba1cFinal != null && !hba1cFinal.isEmpty()) {
                results.setDiabetesbba1c(hba1cFinal);
                if (mCommonVisitData != null) {
                    mCommonVisitData.setDiabetesbba1c(hba1cFinal);
                }
            }

            Log.d(TAG, "isDataReadyForSaving: hba1cFinal = " + hba1cFinal
                    + " | vmReading=" + vmReading
                    + " | fieldVal=" + fieldVal
                    + " | modelVal=" + modelVal);
        } catch (NumberFormatException e) {
            Log.d(TAG, "isDataReadyForSaving NFE: " + e.getMessage());
        }

        ObsDAO obsDAO = new ObsDAO();

        if (mIsEditMode) {
            ObsDAO.deleteExistingDiagnosticsDataIfExists(visitUuid);
            try {
                updateIfNeeded(obsDAO, mBinding.llGlucoseRandomContainer,
                        UuidDictionary.BLOOD_GLUCOSE_RANDOM,        results.getBloodGlucoseRandom());
                updateIfNeeded(obsDAO, mBinding.llGlusoseFastingContainer,
                        UuidDictionary.BLOOD_GLUCOSE_FASTING,       results.getBloodGlucoseFasting());
                updateIfNeeded(obsDAO, mBinding.llPostPrandialContainer,
                        UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL, results.getBloodGlucosePostPrandial());
                updateIfNeeded(obsDAO, mBinding.llUricAcidContainer,
                        UuidDictionary.URIC_ACID,                   results.getUricAcid());
                updateIfNeeded(obsDAO, mBinding.llCholestrolContainer,
                        UuidDictionary.TOTAL_CHOLESTEROL,           results.getCholesterol());
                updateIfNeeded(obsDAO, mBinding.llHemoglobinContainer,
                        UuidDictionary.HEMOGLOBIN,                  results.getHemoglobin());
                String hba1cVal = results.getDiabetesbba1c();
                if (hba1cVal != null && !hba1cVal.isEmpty()) {
                    ObsDTO hba1cDto = new ObsDTO();
                    hba1cDto.setConceptuuid(UuidDictionary.DIABETES_HBA1C);
                    hba1cDto.setEncounteruuid(encounterVitals);
                    hba1cDto.setCreator(sessionManager.getCreatorID());
                    hba1cDto.setValue(hba1cVal);
                    hba1cDto.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);
                    String existingUuid = obsDAO.getObsuuid(encounterVitals, UuidDictionary.DIABETES_HBA1C);
                    if (existingUuid != null) {
                        hba1cDto.setUuid(existingUuid);
                        obsDAO.updateObs(hba1cDto);
                    } else {
                     //   hba1cDto.setUuid(java.util.UUID.randomUUID().toString());
                        obsDAO.insertObs(hba1cDto);                    }
                    Log.d(TAG, "HbA1c saved (edit): " + hba1cVal);
                }

                //making flag to false in the encounter table so it will sync again
                EncounterDAO enc = new EncounterDAO();
                try {
                    enc.updateEncounterSync("false", encounterVitals);
                    enc.updateEncounterModifiedDate(encounterVitals);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.d(TAG, "isDataReadyForSaving(edit): " + e.getLocalizedMessage());
            }
        } else {
            try {
                ObsDAO.deleteExistingDiagnosticsDataIfExists(visitUuid);
                insertIfNeeded(obsDAO, mBinding.llGlucoseRandomContainer,   results.getBloodGlucoseRandom());
                insertIfNeeded(obsDAO, mBinding.llGlusoseFastingContainer,  results.getBloodGlucoseFasting());
                insertIfNeeded(obsDAO, mBinding.llPostPrandialContainer,    results.getBloodGlucosePostPrandial());
                insertIfNeeded(obsDAO, mBinding.llHemoglobinContainer,      results.getHemoglobin());
                insertIfNeeded(obsDAO, mBinding.llCholestrolContainer,      results.getCholesterol());
                insertIfNeeded(obsDAO, mBinding.llUricAcidContainer,        results.getUricAcid());

                // ── HbA1c ─────────────────────────────────────────────────────
                String hba1cVal = results.getDiabetesbba1c();
                Log.d(TAG, "HbA1c insert path: hba1cVal=" + hba1cVal
                        + " encounterVitals=" + encounterVitals);

                if (hba1cVal != null && !hba1cVal.isEmpty()) {
                    ObsDTO hba1cDto = new ObsDTO();

                    // FIX 1 — always set a UUID so the sync engine can send it
                    hba1cDto.setUuid(java.util.UUID.randomUUID().toString());

                    hba1cDto.setConceptuuid(UuidDictionary.DIABETES_HBA1C);
                    hba1cDto.setEncounteruuid(encounterVitals);
                    hba1cDto.setCreator(sessionManager.getCreatorID());
                    hba1cDto.setValue(hba1cVal);
                    hba1cDto.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(hba1cDto);
                        Log.d(TAG, "✅ HbA1c saved (insert): " + hba1cVal
                                + " uuid=" + hba1cDto.getUuid()
                                + " encounter=" + encounterVitals);

                        // FIX 2 — mark encounter as dirty so sync picks it up
                        EncounterDAO enc = new EncounterDAO();
                        enc.updateEncounterSync("false", encounterVitals);
                        enc.updateEncounterModifiedDate(encounterVitals);
                        Log.d(TAG, "✅ Encounter marked unsynced: " + encounterVitals);

                    } catch (DAOException e) {
                        // FIX 3 — report to Crashlytics so failures are visible
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e(TAG, "❌ HbA1c insertObs failed: " + e.getLocalizedMessage());
                    }
                } else {
                    Log.w(TAG, "❌ HbA1c insert skipped — hba1cVal is empty");
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(TAG, "isDataReadyForSaving(insert) outer: " + e.getLocalizedMessage());
            }
        }
        return true;
    }

    private void updateIfNeeded(ObsDAO dao, View container, String conceptUuid, String value)
            throws Exception {
        Diagnostics d = (Diagnostics) container.getTag();
        if (d == null) return;
        if (!d.isMandatory() && (value == null || value.isEmpty())) return;
        ObsDTO dto = new ObsDTO();
        dto.setConceptuuid(conceptUuid);
        dto.setEncounteruuid(encounterVitals);
        dto.setCreator(sessionManager.getCreatorID());
        dto.setValue(value != null ? value : "");
        dto.setUuid(dao.getObsuuid(encounterVitals, d.getUuid()));
        dto.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);
        dao.updateObs(dto);
    }

    private void insertIfNeeded(ObsDAO dao, View container, String value) {
        Diagnostics d = (Diagnostics) container.getTag();
        if (d == null || value == null || value.isEmpty()) return;
        ObsDTO dto = new ObsDTO();
        dto.setConceptuuid(d.getUuid());
        dto.setEncounteruuid(encounterVitals);
        dto.setCreator(sessionManager.getCreatorID());
        dto.setValue(value);
        dto.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);
        try { dao.insertObs(dto);
        }
        catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
    }

    // ── Email debug log as file attachment (DEBUG builds only) ──────────────
    private void emailDebugLogFile() {
        try {
            // 1. Write log to a file in cache/debug_logs/
            java.io.File logDir = new java.io.File(requireContext().getCacheDir(), "debug_logs");
            if (!logDir.exists()) logDir.mkdirs();

            String fileName = "hba1c_debug_" +
                    new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                            .format(new java.util.Date()) + ".txt";
            java.io.File logFile = new java.io.File(logDir, fileName);

            java.io.FileWriter writer = new java.io.FileWriter(logFile);
            writer.write(mDebugLog.toString());
            writer.write("\n\n=== Saved Device Address ===\n");
            android.content.SharedPreferences prefs =
                    requireActivity().getSharedPreferences("hba1c_prefs", Context.MODE_PRIVATE);
            writer.write("hba1c_ble_address = " + prefs.getString("hba1c_ble_address", "null") + "\n");
            writer.close();

            // 2. Get a content:// URI via FileProvider
            android.net.Uri logUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    logFile);

            // 3. Build email intent with attachment
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"nagarjuna@intelehealth.org"}); // ← your email
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HbA1c Debug Log — " + fileName);
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Attached: HbA1c BLE debug log.\n\nDevice: " + android.os.Build.MODEL +
                            "\nTested by: [QA name]\n\nSteps performed:\n");
            emailIntent.putExtra(Intent.EXTRA_STREAM, logUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 4. Force Gmail if installed, else show chooser
            emailIntent.setPackage("com.google.android.gm");
            if (emailIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                // Fallback: show chooser if Gmail not installed
                emailIntent.setPackage(null);
                startActivity(Intent.createChooser(emailIntent, "Send debug log via"));
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to prepare log: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "emailDebugLogFile error", e);
        }
    }
}