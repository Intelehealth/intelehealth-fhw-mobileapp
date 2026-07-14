package org.intelehealth.app.ayu.visit;

import static org.intelehealth.app.knowledgeEngine.Node.bullet_arrow;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.diagnostics.DiagnosticsCollectionFragment;
import org.intelehealth.app.ayu.visit.diagnostics.DiagnosticsCollectionSummaryFragment;
import org.intelehealth.app.ayu.visit.familyhist.FamilyHistoryFragment;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.ayu.visit.pastmedicalhist.MedicalHistorySummaryFragment;
import org.intelehealth.app.ayu.visit.pastmedicalhist.PastMedicalHistoryFragment;
import org.intelehealth.app.ayu.visit.physicalexam.PhysicalExamSummaryFragment;
import org.intelehealth.app.ayu.visit.physicalexam.PhysicalExaminationFragment;
import org.intelehealth.app.ayu.visit.pocdevice.ConnectPocDeviceFragment;
import org.intelehealth.app.ayu.visit.pocdevice.PocDeviceListFragment;
import org.intelehealth.app.ayu.visit.pocdevice.RecordHeartSoundsFragment;
import org.intelehealth.app.ayu.visit.pocdevice.RecordLungSoundsFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonCaptureFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonQuestionsFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonSummaryFragment;
import org.intelehealth.app.ayu.visit.vital.VitalCollectionFragment;
import org.intelehealth.app.ayu.visit.vital.VitalCollectionSummaryFragment;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.models.DiagnosticsModel;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.intelehealth.app.ayu.visit.hba1c.HbA1cLiveViewModel;

import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;

import timber.log.Timber;

import biosense.sreyasvpariyath.com.biosenselib.helper.ControlCentre;
import biosense.sreyasvpariyath.com.biosenselib.helper.Constants;
import biosense.sreyasvpariyath.com.biosenselib.helper.Communicator;

public class VisitCreationActivity extends BaseActivity implements
        VisitCreationActionListener,
        ConnectPocDeviceFragment.OnDigitalScopeCompleteListener,
        RecordHeartSoundsFragment.OnRecordingCompleteListener,
        RecordLungSoundsFragment.OnRecordingCompleteListener,
        Communicator {

    private static final String TAG = VisitCreationActivity.class.getSimpleName();
    private static final String VITAL_FRAGMENT = "VITAL";
    private static final String POC_DEVICE_FRAGMENT = "POC_DEVICE_FRAGMENT";
    private static final String POC_DEVICELIST_FRAGMENT = "POC_DEVICELIST_FRAGMENT";
    private static final String VITAL_SUMMARY_FRAGMENT = "VITAL_SUMMARY";
    private static final String VISIT_REASON_FRAGMENT = "VISIT_REASON";
    private static final String VISIT_REASON_QUESTION_FRAGMENT = "VISIT_REASON_QUESTION";
    private static final String VISIT_REASON_SUMMARY_FRAGMENT = "VISIT_REASON_SUMMARY";
    private static final String PHYSICAL_EXAM_FRAGMENT = "PHYSICAL_EXAM";
    private static final String PHYSICAL_EXAM_SUMMARY_FRAGMENT = "PHYSICAL_EXAM_SUMMARY";
    private static final String PAST_MEDICAL_HISTORY_FRAGMENT = "PAST_MEDICAL_HISTORY";
    private static final String PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT = "PAST_MEDICAL_HISTORY_SUMMARY";
    private static final String FAMILY_HISTORY_SUMMARY_FRAGMENT = "FAMILY_HISTORY_SUMMARY";
    private static final String DIGITAL_SETHSCOPE_FRAGMENT = "DIGITAL_SETHA_SCOPE";
    private static final String DIGITAL_SUMMARY_FRAGMENT = "DIGITAL_SUMMARY";

    public static final int STEP_1_VITAL = 1;
    public static final int STEP_1_VITAL_SUMMARY = 1001;
    public static final int STEP_2_DIAGNOSTICS = 2;
    public static final int STEP_2_DIAGNOSTICS_SUMMARY = 1002;
    public static final int STEP_3_VISIT_REASON = 3;
    public static final int STEP_3_VISIT_REASON_QUESTION = 4;
    public static final int STEP_3_VISIT_REASON_QUESTION_ASSOCIATE_SYMPTOMS = 5;
    public static final int STEP_3_VISIT_REASON_QUESTION_SUMMARY = 44;
    public static final int STEP_4_PHYSICAL_EXAMINATION = 6;
    public static final int STEP_4_PHYSICAL_SUMMARY_EXAMINATION = 55;
    public static final int STEP_5_PAST_MEDICAL_HISTORY = 7;
    public static final int STEP_6_FAMILY_HISTORY = 8;
    public static final int STEP_6_HISTORY_SUMMARY = 9;
    public static final int STEP_7_VISIT_SUMMARY = 10;
    public static final int STEP_7_VISIT_SUMMARY_FINAL = 1000;
    public static final int STEP_11_DEVICE_CONNECT = 11;
    public static final int STEP_12_DEVICE_LIST = 12;
    public static final int STEP_8_DIGITAL_SETHA_SCOPE = 25;
    public static final int SELECT_HEART = 13;
    public static final int SELECT_LUNG = 14;
    public static final int FROM_SUMMARY_RESUME_BACK_FOR_EDIT = 33;
    private static final String DIAGNOSTICS_FRAGMENT = "DIAGNOSTICS";
    private static final String DIAGNOSTICS_SUMMARY_FRAGMENT = "DIAGNOSTICS_SUMMARY";

    // ── HbA1c BLE fields ──────────────────────────────────────────────────────
    private HbA1cLiveViewModel mHba1cViewModel;
    private ControlCentre mControlCentre;
    private String mHba1cDeviceAddress;
    private static final String PREF_BLE_ADDRESS = "hba1c_ble_address";

    // Reconnect / resilience
    private final Handler mBleHandler = new Handler(Looper.getMainLooper());
    private int mReconnectAttempt = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long BASE_RECONNECT_DELAY_MS = 2000L;
    private boolean mUserInitiatedDisconnect = false;
    private volatile boolean mControlCentreStarting = false;
    private BroadcastReceiver mBluetoothStateReceiver;

    /**
     * Delay after setConnectionStatus(true) before marking the app as ready
     * to receive. The BioHermes meter auto-broadcasts its result ONCE when the
     * test countdown ends. If the CCCD write inside ControlCentre has not
     * completed by then, the packet is silently lost.
     *
     * Currently 8000 ms. Increase to 12000 if failures continue.
     * Tune using: adb logcat | grep HBA1C_DEBUG, compare timestamps of
     * "connected=true" and "setHbA1cReading" in a failing session.
     */
    private static final long SUBSCRIPTION_READY_DELAY_MS = 8000L;

    // ── Visit / screen state ──────────────────────────────────────────────────
    private int mCurrentStep = STEP_1_VITAL;
    private int totalScreen = 5;

    SessionManager sessionManager;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag = "new";
    private String state;
    public String patientUuid;
    public String visitUuid;
    public String encounterVitals;
    public final java.util.Set<String> completedSoundTypes = new java.util.HashSet<>();
    private float float_ageYear_Month;
    private int mAgeInMonth;
    private String mAgeAndMonth;
    public String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";

    private FrameLayout mSummaryFrameLayout;
    private ProgressBar mStep1ProgressBar, mStep2ProgressBar, mStep3ProgressBar, mStep4ProgressBar, mStep5ProgressBar;

    private List<Node> mChiefComplainRootNodeList = new ArrayList<>();
    private List<Node> mAssociateSymptomsNodeList = new ArrayList<>();
    private int mCurrentComplainNodeIndex = 0;
    private int mCurrentComplainNodeOptionsIndex = 0;
    private List<ReasonData> mSelectedComplainList = new ArrayList<ReasonData>();

    private boolean mIsEditMode = false;
    private boolean mIsEditTriggerFromVisitSummary = false;
    private int mEditFor = 0;
    private String privacy_value_selected = "";
    private PatientDTO patientDTO;
    private CommonVisitData mCommonVisitData;

    private boolean mHasLicence = false;
    private FeatureActiveStatus featureActiveStatus;

    private boolean isHeartRecorded = false;
    private boolean isLungRecorded = false;
    private boolean isDigitalFlowCompleted = false;

    private String mLastSelectedImageName = "";

    private static final String PREFS_HBA1C          = "hba1c_prefs";
    private static final String PREF_LAST_READING    = "hba1c_last_reading";
    private static final String PREF_LAST_TIME       = "hba1c_last_reading_time";
    private static final String PREF_LAST_VISIT      = "hba1c_last_reading_visit";

    private void startVisit() {
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);

        visitUuid = UUID.randomUUID().toString();
        mCommonVisitData.setVisitUuid(visitUuid);

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        CustomLog.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value(privacy_value_selected);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setUuid(visitUuid);
        visitDTO.setPatientuuid(patientDTO.getUuid());
        visitDTO.setStartdate(thisDate);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setLocationuuid(sessionManager.getLocationUuid());
        visitDTO.setSyncd(false);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        encounterVitals = encounterDTO.getUuid();
        mCommonVisitData.setEncounterUuidVitals(encounterVitals);
        encounterAdultIntials = UUID.randomUUID().toString();
        mCommonVisitData.setEncounterUuidAdultIntial(encounterAdultIntials);
        EncounterAdultInitial_LatestVisit = encounterAdultIntials;
        mCommonVisitData.setEncounterAdultInitialLatestVisit(EncounterAdultInitial_LatestVisit);
    }

    private boolean mIsInitilaFeaturesLoading = true;

    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        featureActiveStatus = activeStatus;
        if (mIsInitilaFeaturesLoading) {
            if (featureActiveStatus != null) {
                boolean isVitalsActive = featureActiveStatus.getVitalSection();
                boolean isDiagnosticsActive = featureActiveStatus.getActiveStatusDiagnosticsSection();
                if (!isVitalsActive) { mStep1ProgressBar.setVisibility(View.GONE); mCurrentStep = STEP_2_DIAGNOSTICS; totalScreen = 4; }
                if (!isDiagnosticsActive) { mStep2ProgressBar.setVisibility(View.GONE); mCurrentStep = STEP_1_VITAL; totalScreen = 4; }
                if (isVitalsActive && isDiagnosticsActive) { mStep1ProgressBar.setVisibility(View.VISIBLE); mStep2ProgressBar.setVisibility(View.VISIBLE); mCurrentStep = STEP_1_VITAL; totalScreen = 5; }
                if (!isVitalsActive && isDiagnosticsActive) { mStep1ProgressBar.setVisibility(View.GONE); mStep2ProgressBar.setVisibility(View.VISIBLE); mCurrentStep = STEP_2_DIAGNOSTICS; totalScreen = 4; }
                if (isVitalsActive && !isDiagnosticsActive) { mStep1ProgressBar.setVisibility(View.VISIBLE); mStep2ProgressBar.setVisibility(View.GONE); mCurrentStep = STEP_1_VITAL; totalScreen = 4; }
                if (!isVitalsActive && !isDiagnosticsActive) { mStep1ProgressBar.setVisibility(View.GONE); mStep2ProgressBar.setVisibility(View.GONE); mCurrentStep = STEP_3_VISIT_REASON; totalScreen = 3; }
            }
            if (!mIsEditMode) onFormSubmitted(mCurrentStep, mIsEditMode, mCommonVisitData);
            else makeReadyForEdit();
        }
        mIsInitilaFeaturesLoading = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_creation);
        SessionManager.getInstance(this).clearVitalPreference();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

        if (!sessionManager.getLicenseKey().isEmpty()) mHasLicence = true;
        mSummaryFrameLayout = findViewById(R.id.fl_steps_summary);
        mStep1ProgressBar = findViewById(R.id.prog_bar_step1);
        mStep2ProgressBar = findViewById(R.id.prog_bar_step2);
        mStep3ProgressBar = findViewById(R.id.prog_bar_step3);
        mStep4ProgressBar = findViewById(R.id.prog_bar_step4);
        mStep5ProgressBar = findViewById(R.id.prog_bar_step5);

        Intent intent = this.getIntent();
        if (intent != null) {
            mCommonVisitData = intent.getExtras().getParcelable("CommonVisitData");
            patientUuid = mCommonVisitData.getPatientUuid();
            patientDTO = PatientsDAO.getPatientDetailsByPatientUUID(patientUuid);
            intentTag = mCommonVisitData.getIntentTag();
            if (intentTag.equalsIgnoreCase("new")) {
                privacy_value_selected = mCommonVisitData.getPrivacyNote();
                startVisit();
            } else {
                visitUuid = mCommonVisitData.getVisitUuid();
                encounterVitals = mCommonVisitData.getEncounterUuidVitals();
                encounterAdultIntials = mCommonVisitData.getEncounterUuidAdultIntial();
                EncounterAdultInitial_LatestVisit = mCommonVisitData.getEncounterAdultInitialLatestVisit();
                mEditFor = mCommonVisitData.getEditFor();
            }
            patientName = patientDTO.getFirstname() + " " + patientDTO.getLastname();
            mCommonVisitData.setPatientName(patientName);
            patientGender = patientDTO.getGender();
            mCommonVisitData.setPatientGender(patientGender);
            float_ageYear_Month = DateAndTimeUtils.getFloat_Age_Year_Month(patientDTO.getDateofbirth());
            mCommonVisitData.setPatientAgeYearMonth(float_ageYear_Month);
            String[] temp = String.valueOf(float_ageYear_Month).split("\\.");
            mAgeInMonth = Integer.parseInt(temp[0]) * 12 + Integer.parseInt(temp[1]);
            if (Integer.parseInt(temp[0]) == 0) mAgeAndMonth = temp[1] + " " + getResources().getString(R.string.months);
            else if (Integer.parseInt(temp[0]) == 0) mAgeAndMonth = temp[0] + " " + getResources().getString(R.string.years);
            else mAgeAndMonth = temp[0] + " " + getResources().getString(R.string.years) + " " + temp[1] + " " + getResources().getString(R.string.months);
            if (intentTag.equalsIgnoreCase("edit")) { mIsEditMode = true; mIsEditTriggerFromVisitSummary = true; }
            ((TextView) findViewById(R.id.tv_title)).setText(patientName);
            ((TextView) findViewById(R.id.tv_title_desc)).setText(String.format("%s/%s", patientGender, mAgeAndMonth));
        }

        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();
            mCommonVisitData.setEncounterUuidAdultIntial(encounterAdultIntials);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterAdultIntials);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL"));
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        handleDeviceBackPress();
        initHba1cBle();
        registerBluetoothStateReceiver();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HbA1c BLE — connection management
    // ─────────────────────────────────────────────────────────────────────────

    private void initHba1cBle() {
        mHba1cViewModel = new ViewModelProvider(this).get(HbA1cLiveViewModel.class);

        SharedPreferences prefs = getSharedPreferences(PREFS_HBA1C, MODE_PRIVATE);
        String savedAddress = prefs.getString(PREF_BLE_ADDRESS, null);

        Log.d("HBA1C_DEBUG", "initHba1cBle: savedAddress=" + savedAddress
                + " visitUuid=" + visitUuid);

        // Start BLE connection first (this only resets connection flags — see fix below)
        if (savedAddress != null && !savedAddress.isEmpty()) {
            startHba1cControlCentre(savedAddress);
        } else {
            Log.d("HBA1C_DEBUG", "initHba1cBle: no saved address — user must scan");
        }

        // ── Restore reading AFTER startHba1cControlCentre() ──────────────────
        // Order matters: startHba1cControlCentre calls resetConnectionOnly()
        // which does NOT touch the reading. But we still restore here to handle
        // the case where the ViewModel was cleared by the OS (low memory).
        String lastReading = prefs.getString(PREF_LAST_READING, null);
        String lastVisit   = prefs.getString(PREF_LAST_VISIT, null);
        String lastTime    = prefs.getString(PREF_LAST_TIME, "");

        Log.d("HBA1C_DEBUG", "initHba1cBle: lastReading=" + lastReading
                + " lastVisit=" + lastVisit);

        if (lastReading != null
                && !lastReading.isEmpty()
                && visitUuid != null
                && visitUuid.equals(lastVisit)) {

            // Restore into ViewModel — LiveData observers in the fragment
            // will fire immediately and populate the field
            mHba1cViewModel.onHba1cReading(lastReading, lastTime);
            Log.d("HBA1C_DEBUG", "✅ initHba1cBle: restored reading=" + lastReading);
        } else {
            Log.d("HBA1C_DEBUG", "initHba1cBle: no reading to restore"
                    + " (new visit or no reading captured yet)");
        }
    }

    private void startHba1cControlCentre(String deviceAddress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("HBA1C_DEBUG", "startHba1cControlCentre: BLUETOOTH_CONNECT not granted");
            return;
        }
        if (mControlCentreStarting) {
            Log.w("HBA1C_DEBUG", "startHba1cControlCentre: already starting — skip");
            return;
        }
        mControlCentreStarting = true;
        try {
            if (mControlCentre != null) {
                try { mControlCentre.stopReceiver(); } catch (Exception ignored) {}
            }
            mHba1cDeviceAddress = deviceAddress;

            // ✅ KEY FIX — resetConnectionOnly() preserves mHba1cReading
            // The old code called reset() here which wiped the reading on every
            // reconnect, causing the SharedPreferences restore to be the only
            // recovery path — and even that failed if the visit UUID changed.
            if (mHba1cViewModel != null) {
                mHba1cViewModel.resetConnectionOnly();
            }

            mControlCentre = new ControlCentre(
                    this, this, this,
                    deviceAddress, Constants.devId_A1Chek, "HbA1c");
            mControlCentre.startReceiver();
            Log.d("HBA1C_DEBUG", "startHba1cControlCentre: started → " + deviceAddress);

        } catch (SecurityException e) {
            Log.e("HBA1C_DEBUG", "startHba1cControlCentre SecurityException: "
                    + e.getMessage());
        } finally {
            mControlCentreStarting = false;
        }
    }

    public HbA1cLiveViewModel getHba1cViewModel() { return mHba1cViewModel; }

    public void saveAndStartBleDevice(String deviceAddress) {
        getSharedPreferences(PREFS_HBA1C, MODE_PRIVATE)
                .edit()
                .putString(PREF_BLE_ADDRESS, deviceAddress)
                .apply();
        mUserInitiatedDisconnect = false;
        mReconnectAttempt = 0;
        mBleHandler.removeCallbacksAndMessages(null);
        Log.d("HBA1C_DEBUG", "saveAndStartBleDevice: " + deviceAddress);
        // startHba1cControlCentre calls resetConnectionOnly() — reading preserved
        startHba1cControlCentre(deviceAddress);
    }

    private void scheduleReconnect() {
        if (mReconnectAttempt >= MAX_RECONNECT_ATTEMPTS) {
            Log.w("HBA1C_DEBUG", "scheduleReconnect: giving up after "
                    + mReconnectAttempt + " attempts");
            runOnUiThread(() -> Toast.makeText(
                    this,
                    "Lost connection to HbA1c device. Please tap Scan to reconnect.",
                    Toast.LENGTH_LONG).show());
            return;
        }
        // Exponential backoff: 2s → 4s → 8s → 16s → 32s
        long delay = BASE_RECONNECT_DELAY_MS * (1L << mReconnectAttempt);
        mReconnectAttempt++;
        Log.d("HBA1C_DEBUG", "scheduleReconnect: attempt "
                + mReconnectAttempt + " in " + delay + "ms");
        mBleHandler.postDelayed(() -> {
            if (mHba1cDeviceAddress != null && !mUserInitiatedDisconnect) {
                // reading preserved because startHba1cControlCentre
                // now uses resetConnectionOnly()
                startHba1cControlCentre(mHba1cDeviceAddress);
            }
        }, delay);
    }

    private void registerBluetoothStateReceiver() {
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (btState == BluetoothAdapter.STATE_OFF) {
                    Log.w("HBA1C_DEBUG", "Bluetooth OFF — halting all reconnects");
                    mUserInitiatedDisconnect = true;
                    mBleHandler.removeCallbacksAndMessages(null);
                    // Only update connection flags — reading preserved
                    if (mHba1cViewModel != null) {
                        mHba1cViewModel.resetConnectionOnly();
                    }
                    Toast.makeText(
                            VisitCreationActivity.this,
                            "Bluetooth turned off. Turn it on and tap Scan to reconnect.",
                            Toast.LENGTH_LONG).show();

                } else if (btState == BluetoothAdapter.STATE_ON) {
                    Log.d("HBA1C_DEBUG", "Bluetooth ON — auto-reconnecting");
                    mUserInitiatedDisconnect = false;
                    if (mHba1cDeviceAddress != null) {
                        mReconnectAttempt = 0;
                        // reading preserved because startHba1cControlCentre
                        // uses resetConnectionOnly()
                        startHba1cControlCentre(mHba1cDeviceAddress);
                    }
                }
            }
        };
        registerReceiver(
                mBluetoothStateReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Communicator interface — ALL methods from the actual SDK
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void setHbA1cReading(String reading, String date, String time, String srno) {
        Log.d("HBA1C_DEBUG", "setHbA1cReading: " + reading
                + " at " + time + " (serial " + srno + ")");

        // SharedPreferences.apply() is thread-safe — persist immediately
        // so the value survives process death, screen rotation, Activity destroy
        getSharedPreferences(PREFS_HBA1C, MODE_PRIVATE)
                .edit()
                .putString(PREF_LAST_READING, reading)
                .putString(PREF_LAST_TIME,    time)
                .putString(PREF_LAST_VISIT,   visitUuid)
                .apply();

        Log.d("HBA1C_DEBUG", "✅ SharedPreferences updated:"
                + " reading=" + reading
                + " time=" + time
                + " visit=" + visitUuid);

        // LiveData.setValue() must run on UI thread
        runOnUiThread(() -> {
            if (mHba1cViewModel != null) {
                mHba1cViewModel.onHba1cReading(reading, time);
            }
        });
    }

    @Override
    public void setConnectionStatus(String status, boolean isConnected) {
        Log.d("HBA1C_DEBUG", "setConnectionStatus: status=" + status
                + " isConnected=" + isConnected);

        // All ViewModel and Handler calls must be on UI thread
        runOnUiThread(() -> {
            if (mHba1cViewModel != null) {
                mHba1cViewModel.onConnectionStatus(isConnected);
            }

            if (isConnected) {
                mReconnectAttempt = 0;
                mBleHandler.removeCallbacksAndMessages(null);

                // Delay before signalling ready — CCCD write needs time to complete
                // BioHermes broadcasts ONCE when countdown ends — if CCCD not done,
                // the packet is silently lost
                mBleHandler.postDelayed(() -> {
                    if (mHba1cViewModel != null) {
                        mHba1cViewModel.onReadyToReceive(true);
                    }
                    Log.d("HBA1C_DEBUG", "Subscription assumed ready after "
                            + SUBSCRIPTION_READY_DELAY_MS + "ms");
                }, SUBSCRIPTION_READY_DELAY_MS);

            } else {
                // Cancel any pending ready signal
                mBleHandler.removeCallbacksAndMessages(null);
                if (mHba1cViewModel != null) {
                    mHba1cViewModel.onReadyToReceive(false);
                }

                // ── KEY FIX ───────────────────────────────────────────────────
                // Check if a reading was already captured.
                // If YES → meter powered off normally after test → skip reconnect.
                // If NO  → unexpected disconnect → schedule reconnect.
                boolean hasReading = mHba1cViewModel != null
                        && mHba1cViewModel.hba1cReading().getValue() != null
                        && !mHba1cViewModel.hba1cReading().getValue().isEmpty();

                Log.d("HBA1C_DEBUG", "disconnected"
                        + " | hasReading=" + hasReading
                        + " | userInitiated=" + mUserInitiatedDisconnect
                        + " | willReconnect="
                        + (!hasReading && !mUserInitiatedDisconnect));

                if (!mUserInitiatedDisconnect
                        && mHba1cDeviceAddress != null
                        && !hasReading) {
                    scheduleReconnect();
                }
            }
        });
    }

    @Override
    public boolean go(String s) {
        // Per the official BioHermes A1C EZ 2.0 manual, no user button press
        // is required — the device auto-broadcasts once its test completes.
        Log.d("HBA1C_DEBUG", "go(): " + s + " → false");
        return false;
    }

    @Override
    public void testStarted(boolean started) {
        Log.d("HBA1C_DEBUG", "testStarted: " + started);
    }

    @Override
    public void setBPReading(String systolic, String diastolic, String pulse) { /* no-op */ }

    @Override
    public void setGlucoseReading(String text) { /* no-op */ }

    @Override
    public void setHB(String s) { /* no-op */ }

    @Override
    public void setSwitchActivity() { /* no-op */ }

    @Override
    public void setBatteryLevel(int i) { /* no-op */ }

    @Override
    public void setManufacturerName(String s) { /* no-op */ }

    @Override
    public void setSerialNumber(String s) { /* no-op */ }

    @Override
    public void setModelNumber(String s) { /* no-op */ }

    @Override
    public void getOfflineResults(ArrayList<String> arrayList) { /* no-op */ }

    @Override
    public void onBpDeviceError() { /* no-op */ }

    @Override
    public void stopNotiFication() { /* no-op */ }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        mUserInitiatedDisconnect = true;
        mBleHandler.removeCallbacksAndMessages(null);
        if (mBluetoothStateReceiver != null) {
            try { unregisterReceiver(mBluetoothStateReceiver); }
            catch (Exception ignored) {}
            mBluetoothStateReceiver = null;
        }
        if (mControlCentre != null) {
            try { mControlCentre.stopReceiver(); }
            catch (SecurityException e) {
                Log.e("HBA1C_DEBUG", "onDestroy SecurityException: " + e.getMessage());
            } catch (Exception ignored) {}
            mControlCentre = null;
        }
        super.onDestroy();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Everything below is unchanged from your original file
    // ─────────────────────────────────────────────────────────────────────────

    private void handleDeviceBackPress() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mIsEditTriggerFromVisitSummary)
                    showConfirmationDialog(getString(R.string.confirm_discard_changes_content));
            }
        });
    }

    public boolean isEditTriggerFromVisitSummary() { return mIsEditTriggerFromVisitSummary; }

    private void makeReadyForEdit() {
        findViewById(R.id.ll_progress_steps).setVisibility(View.GONE);
        mSelectedComplainList = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid), new TypeToken<List<ReasonData>>() {}.getType());
        mChiefComplainRootNodeList = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid), new TypeToken<List<Node>>() {}.getType());
        if (!sessionManager.getVisitEditCache(SessionManager.PHY_EXAM + visitUuid).isEmpty()) {
            physicalExamMap = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.PHY_EXAM + visitUuid), PhysicalExam.class);
            physicalExamMap.refreshOnlyLocaleTitle();
        } else loadPhysicalExam();
        if (BuildConfig.FLAVOR_client != FlavorKeys.UNFPA) {
            if (!sessionManager.getVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid).isEmpty())
                mPastMedicalHistoryNode = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid), Node.class);
            else mPastMedicalHistoryNode = loadPastMedicalHistory();
        }
        if (!sessionManager.getVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid).isEmpty())
            mFamilyHistoryNode = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid), Node.class);
        else mFamilyHistoryNode = loadFamilyHistory();
        setTitle(mEditFor);
        switch (mEditFor) {
            case STEP_1_VITAL: getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(mCommonVisitData, mIsEditMode, null), VITAL_FRAGMENT).commit(); break;
            case STEP_2_DIAGNOSTICS: getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, DiagnosticsCollectionFragment.newInstance(mCommonVisitData, mIsEditMode, null), DIAGNOSTICS_FRAGMENT).commit(); break;
            case STEP_3_VISIT_REASON: setTitle(STEP_3_VISIT_REASON_QUESTION); getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(mCommonVisitData, mIsEditMode, mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).commit(); break;
            case STEP_4_PHYSICAL_EXAMINATION: mStep4ProgressBar.setProgress(10); mSummaryFrameLayout.setVisibility(View.GONE); getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(mCommonVisitData, mIsEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).commit(); break;
            case STEP_5_PAST_MEDICAL_HISTORY: showPastMedicalHistoryFragment(mIsEditMode); break;
            case STEP_6_FAMILY_HISTORY: showFamilyHistoryFragment(mIsEditMode); break;
        }
    }

    public void backPress(View view) {
        if (!mIsEditTriggerFromVisitSummary) showConfirmationDialog(getString(R.string.confirm_discard_changes_content));
    }

    private VitalsObject mVitalsObject;
    private DiagnosticsModel mDiagnosticsModel;
    public VitalsObject getVitalsObject() { return mVitalsObject; }

    @Override
    public void onFormSubmitted(int nextAction, boolean isEditMode, Object object) {
        mCurrentStep = nextAction;
        Timber.tag(TAG).d("first screen=>%s", nextAction);
        switch (nextAction) {
            case STEP_1_VITAL_SUMMARY:
                if (object != null) mVitalsObject = (VitalsObject) object;
                if (mVitalsObject != null) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    mStep1ProgressBar.setProgress(100);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, VitalCollectionSummaryFragment.newInstance(mVitalsObject, isEditMode, visitUuid), VITAL_SUMMARY_FRAGMENT).commit();
                }
                break;
            case STEP_1_VITAL:
                mStep1ProgressBar.setProgress(100); mStep2ProgressBar.setProgress(0); mStep3ProgressBar.setProgress(0); mStep4ProgressBar.setProgress(0); mStep5ProgressBar.setProgress(0);
                mSummaryFrameLayout.setVisibility(View.GONE); setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(mCommonVisitData, isEditMode, mVitalsObject), VITAL_FRAGMENT).commit();
                break;
            case STEP_2_DIAGNOSTICS:
                mStep2ProgressBar.setProgress(100); mSummaryFrameLayout.setVisibility(View.GONE); setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, DiagnosticsCollectionFragment.newInstance(mCommonVisitData, isEditMode, mDiagnosticsModel), DIAGNOSTICS_FRAGMENT).commit();
                break;
            case STEP_2_DIAGNOSTICS_SUMMARY:
                if (object != null) mDiagnosticsModel = (DiagnosticsModel) object;
                if (mDiagnosticsModel != null) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE); mStep2ProgressBar.setProgress(100);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, DiagnosticsCollectionSummaryFragment.newInstance(mDiagnosticsModel, isEditMode, visitUuid), DIAGNOSTICS_SUMMARY_FRAGMENT).commit();
                }
                break;
            case STEP_3_VISIT_REASON:
                getSupportFragmentManager().popBackStack(); mStep3ProgressBar.setProgress(30); setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, VisitReasonCaptureFragment.newInstance(mCommonVisitData, isEditMode, false), VISIT_REASON_FRAGMENT).commit();
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
            case STEP_3_VISIT_REASON_QUESTION:
                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) { getSupportFragmentManager().popBackStack(); mSummaryFrameLayout.setVisibility(View.GONE); }
                mSelectedComplainList = (List<ReasonData>) object;
                loadChiefComplainNodeForSelectedNames(mSelectedComplainList);
                mStep3ProgressBar.setProgress(60); setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(mCommonVisitData, isEditMode, mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).commit();
                break;
            case FROM_SUMMARY_RESUME_BACK_FOR_EDIT:
                mSummaryFrameLayout.setVisibility(View.GONE);
                if (object != null) {
                    int caseNo = (int) object;
                    if (caseNo == STEP_5_PAST_MEDICAL_HISTORY) { showPastMedicalHistoryFragment(isEditMode); setTitle(STEP_5_PAST_MEDICAL_HISTORY); }
                    else if (caseNo == STEP_6_FAMILY_HISTORY) { showFamilyHistoryFragment(isEditMode); setTitle(STEP_6_FAMILY_HISTORY); }
                    else if (caseNo == STEP_4_PHYSICAL_EXAMINATION) {
                        mStep4ProgressBar.setProgress(100); mSummaryFrameLayout.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(mCommonVisitData, isEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).commit();
                        setTitle(STEP_4_PHYSICAL_EXAMINATION);
                    } else if (caseNo == STEP_3_VISIT_REASON_QUESTION) { setTitle(STEP_3_VISIT_REASON_QUESTION); }
                }
                break;
            case STEP_3_VISIT_REASON_QUESTION_SUMMARY:
                if (isSavedVisitReason()) {
                    mStep3ProgressBar.setProgress(100); mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, VisitReasonSummaryFragment.newInstance(mCommonVisitData, insertionWithLocaleJsonString, isEditMode, visitUuid), VISIT_REASON_QUESTION_FRAGMENT).commit();
                }
                break;
            case STEP_4_PHYSICAL_EXAMINATION:
                getSupportFragmentManager().popBackStack(); mStep4ProgressBar.setProgress(10); mSummaryFrameLayout.setVisibility(View.GONE);
                loadPhysicalExam(); setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(mCommonVisitData, isEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).commit();
                break;
            case STEP_4_PHYSICAL_SUMMARY_EXAMINATION:
                if (isSavedPhysicalExam()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, PhysicalExamSummaryFragment.newInstance(mCommonVisitData, physicalStringLocale, isEditMode, visitUuid), PHYSICAL_EXAM_SUMMARY_FRAGMENT).commit();
                }
                break;
            case STEP_5_PAST_MEDICAL_HISTORY: showPastMedicalHistoryFragment(isEditMode); setTitle(nextAction); break;
            case STEP_6_FAMILY_HISTORY: showFamilyHistoryFragment(isEditMode); setTitle(nextAction); break;
            case STEP_6_HISTORY_SUMMARY:
                if (isSavedPastHistory()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, MedicalHistorySummaryFragment.newInstance(mCommonVisitData, patientHistoryLocale, familyHistoryLocale, isEditMode, visitUuid), PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT).commit();
                }
                break;
            case STEP_7_VISIT_SUMMARY: onFormSubmitted(STEP_7_VISIT_SUMMARY_FINAL, isEditMode, null); break;
            case STEP_7_VISIT_SUMMARY_FINAL:
                insertLocalEnFormatQAValues();
                Intent intent1 = new Intent(VisitCreationActivity.this, VisitSummaryActivity_New.class);
                mCommonVisitData.setHasPrescription(false);
                intent1.putExtra("CommonVisitData", mCommonVisitData);
                Log.d("HBA1C_DEBUG", "mHba1cViewModel null? " + (mHba1cViewModel == null));
                if (mHba1cViewModel != null) {
                    Log.d("HBA1C_DEBUG", "connected? " + mHba1cViewModel.connected().getValue());
                    Log.d("HBA1C_DEBUG", "readyToReceive? " + mHba1cViewModel.readyToReceive().getValue());
                    Log.d("HBA1C_DEBUG", "hba1cReading = " + mHba1cViewModel.hba1cReading().getValue());
                }
                Log.d("HBA1C_DEBUG", "mDiagnosticsModel hba1c = " + (mDiagnosticsModel != null ? mDiagnosticsModel.getDiabetesbba1c() : "null"));
                String latestHba1c = null;
                if (mHba1cViewModel != null && mHba1cViewModel.hba1cReading().getValue() != null && !mHba1cViewModel.hba1cReading().getValue().isEmpty()) {
                    latestHba1c = mHba1cViewModel.hba1cReading().getValue();
                    Log.d("HBA1C_DEBUG", "source: ViewModel → " + latestHba1c);
                } else if (mDiagnosticsModel != null && mDiagnosticsModel.getDiabetesbba1c() != null && !mDiagnosticsModel.getDiabetesbba1c().isEmpty()) {
                    latestHba1c = mDiagnosticsModel.getDiabetesbba1c();
                    Log.d("HBA1C_DEBUG", "source: DiagnosticsModel → " + latestHba1c);
                }
                if (latestHba1c != null && !latestHba1c.isEmpty()) {
                    intent1.putExtra("hba1c_live_value", latestHba1c);
                    Log.d("HBA1C_DEBUG", "✅ Extra added: " + latestHba1c);
                } else {
                    Log.d("HBA1C_DEBUG", "❌ No HbA1c value available");
                }
                startActivity(intent1);
                finish();
                break;
            case STEP_8_DIGITAL_SETHA_SCOPE:
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, ConnectPocDeviceFragment.newInstance(mCommonVisitData, isEditMode, mVitalsObject), DIGITAL_SETHSCOPE_FRAGMENT).commit();
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
            case STEP_12_DEVICE_LIST:
                mSummaryFrameLayout.setVisibility(View.VISIBLE); mStep1ProgressBar.setProgress(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, PocDeviceListFragment.newInstance(isEditMode, mVitalsObject), POC_DEVICELIST_FRAGMENT).commit();
                break;
            case SELECT_HEART: mSummaryFrameLayout.setVisibility(View.VISIBLE); mStep1ProgressBar.setProgress(100); break;
            case SELECT_LUNG:
                mSummaryFrameLayout.setVisibility(View.VISIBLE); mStep1ProgressBar.setProgress(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_summary, RecordLungSoundsFragment.newInstance(mCommonVisitData, isEditMode, mVitalsObject, visitUuid), POC_DEVICELIST_FRAGMENT).addToBackStack("LUNG").commit();
                break;
        }
    }

    private void showPastMedicalHistoryFragment(boolean isEditMode) {
        mStep5ProgressBar.setProgress(10); mSummaryFrameLayout.setVisibility(View.GONE);
        if (mPastMedicalHistoryNode == null) { mPastMedicalHistoryNode = loadPastMedicalHistory(); isEditMode = false; }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, PastMedicalHistoryFragment.newInstance(mCommonVisitData, isEditMode, mPastMedicalHistoryNode), PAST_MEDICAL_HISTORY_FRAGMENT).commit();
        setTitle(STEP_5_PAST_MEDICAL_HISTORY);
    }

    private void showFamilyHistoryFragment(boolean isEditMode) {
        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) mStep3ProgressBar.setProgress(100);
        mStep5ProgressBar.setProgress(50); mSummaryFrameLayout.setVisibility(View.GONE);
        if (mFamilyHistoryNode == null) { mFamilyHistoryNode = loadFamilyHistory(); isEditMode = false; }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_steps_body, FamilyHistoryFragment.newInstance(mCommonVisitData, isEditMode, mFamilyHistoryNode), FAMILY_HISTORY_SUMMARY_FRAGMENT).commit();
        setTitle(STEP_6_FAMILY_HISTORY);
    }

    private boolean isSavedPastHistory() { return savePastHistoryData(); }
    private boolean isSavedPhysicalExam() { return savePhysicalExamData(); }

    private boolean isSavedVisitReason() {
        sessionManager.setVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid, new Gson().toJson(mSelectedComplainList));
        sessionManager.setVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid, new Gson().toJson(mChiefComplainRootNodeList));
        insertion = ""; insertionLocale = ""; insertionLocaleEn = "";
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderEn = new StringBuilder();
        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            Node node = mChiefComplainRootNodeList.get(i);
            boolean isAssociateSymptomsType = node.getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS);
            String val = formatComplainRecord(node, isAssociateSymptomsType);
            stringBuilder.append(bullet_arrow + node.findDisplay() + "::" + node.formQuestionAnswer(0, isAssociateSymptomsType));
            stringBuilderEn.append(bullet_arrow + node.findDisplay("en") + "::" + node.formQuestionAnswer(0, isAssociateSymptomsType, "en"));
            if (val == null) return false;
        }
        insertionLocale = stringBuilder.toString();
        insertionLocaleEn = stringBuilderEn.toString();
        if (insertion.contains("<br/> ►<b>" + Node.ASSOCIATE_SYMPTOMS + "</b>: <br/>►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>"))
            insertion = insertion.replace("<br/> ►<b>" + Node.ASSOCIATE_SYMPTOMS + "</b>: <br/>►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>", "<br/>►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>");
        JSONObject jsonObject = new JSONObject();
        try {
            insertionLocale = VisitUtils.replaceEnglishCommonString(insertionLocale, sessionManager.getAppLanguage());
            insertionLocaleEn = VisitUtils.replaceEnglishCommonString(insertionLocaleEn, "en");
            String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(insertionLocale);
            if (matchDate != null) for (String date : matchDate) insertionLocale = insertionLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
            insertion = VisitUtils.replaceToEnglishCommonString(insertion, sessionManager.getAppLanguage());
            jsonObject.put("en", insertion);
            jsonObject.put("l-" + sessionManager.getAppLanguage(), insertionLocale);
            insertionWithLocaleJsonString = jsonObject.toString().replace("\\/", "/");
        } catch (JSONException e) { e.printStackTrace(); }
        return insertChiefComplainToDb(insertionWithLocaleJsonString);
    }

    private Node mPhysicalExamNode;
    private String mLastChiefComplainPhysicalString = "";

    private static final String STETHOSCOPE_LOCATION_HEART = "Sound Heart";
    private static final String STETHOSCOPE_LOCATION_LUNG = "Sound Lung";

    /**
     * When several selected protocols each define digital-stethoscope content
     * (Sound Heart / Sound Lung entries), only one of them can drive the
     * stethoscope workflow. We pick the protocol with the highest combined
     * heart+lung sound count so the recording flow always matches the
     * richest available protocol (e.g. Difficulty Breathing's 4+16 over
     * Cough's 4+6), instead of an accidental first-selected default.
     */
    private int findWinningStethoscopeProtocolIndex(List<ArrayList<String>> perProtocolExams) {
        int winningIndex = -1;
        int winningSoundCount = -1;
        for (int idx = 0; idx < perProtocolExams.size(); idx++) {
            int soundCount = 0;
            for (String exam : perProtocolExams.get(idx)) {
                if (isStethoscopeExamEntry(exam)) soundCount++;
            }
            if (soundCount > winningSoundCount) { winningSoundCount = soundCount; winningIndex = idx; }
        }
        return winningIndex;
    }

    private boolean isStethoscopeExamEntry(String exam) {
        if (exam == null || !exam.contains(":")) return false;
        String location = exam.split(":")[0].trim();
        return location.equalsIgnoreCase(STETHOSCOPE_LOCATION_HEART) || location.equalsIgnoreCase(STETHOSCOPE_LOCATION_LUNG);
    }

    private List<Node> loadPhysicalExam() {
        ArrayList<String> physicalExams = new ArrayList<>();
        List<ArrayList<String>> perProtocolExams = new ArrayList<>();
        for (int idx = 0; idx < mChiefComplainRootNodeList.size(); idx++) {
            Node protocolNode = mChiefComplainRootNodeList.get(idx);
            ArrayList<String> protocolExams = new ArrayList<>();
            ArrayList<String> childNodeSelectedPhysicalExams = protocolNode.getPhysicalExamList();
            if (childNodeSelectedPhysicalExams != null && !childNodeSelectedPhysicalExams.isEmpty()) protocolExams.addAll(childNodeSelectedPhysicalExams);
            ArrayList<String> rootNodePhysicalExams = parseExams(protocolNode);
            if (rootNodePhysicalExams != null && !rootNodePhysicalExams.isEmpty()) protocolExams.addAll(rootNodePhysicalExams);
            perProtocolExams.add(protocolExams);
        }
        int winningProtocolIndex = findWinningStethoscopeProtocolIndex(perProtocolExams);
        for (int idx = 0; idx < perProtocolExams.size(); idx++) {
            for (String exam : perProtocolExams.get(idx)) {
                if (isStethoscopeExamEntry(exam) && idx != winningProtocolIndex) continue;
                physicalExams.add(exam);
            }
        }
        Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
        int chiefComplainIndexForFilterMap = winningProtocolIndex >= 0 ? winningProtocolIndex : mCurrentComplainNodeIndex;
        mLastChiefComplainPhysicalString = mChiefComplainRootNodeList.get(chiefComplainIndexForFilterMap).getPhysicalExams();
        String[] exm = mLastChiefComplainPhysicalString.split(";");
        HashMap<String, List<String>> map = new HashMap<>();
        for (String s : exm) {
            if (s.contains(":") && s.split(":").length >= 2) {
                String rootNodeName = s.split(":")[0]; String childNodeName = s.split(":")[1];
                List<String> list = new ArrayList<>();
                if (map.containsKey(rootNodeName)) list = map.get(rootNodeName);
                list.add(childNodeName); map.put(rootNodeName, list);
            }
        }
        String fileLocation = "physExam.json";
        Node filterNode = loadFileToNode(fileLocation, true);
        ArrayList<String> selectedExamsList = new ArrayList<>(selectedExams);
        physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, fileLocation), selectedExamsList);
        physicalExamMap.refreshOnlyLocaleTitle();
        physicalExamMap.setEngineVersion(filterNode.getEngineVersion());
        List<Node> optionsList = new ArrayList<>();
        for (int i = 0; i < filterNode.getOptionsList().size(); i++) {
            if (map.containsKey(filterNode.getOptionsList().get(i).getText()) && filterNode.getOptionsList().get(i).getOptionsList() != null) {
                for (int j = 0; j < filterNode.getOptionsList().get(i).getOptionsList().size(); j++) {
                    Node innerNode = filterNode.getOptionsList().get(i).getOptionsList().get(j);
                    if (innerNode.getOptionsList() != null && !innerNode.getOptionsList().isEmpty()) optionsList.add(innerNode.getOptionsList().get(0));
                }
            }
        }
        filterNode.setOptionsList(optionsList);
        return physicalExamMap.getSelectedNodes();
    }

    private Node mPastMedicalHistoryNode;
    private Node loadPastMedicalHistory() { return loadFileToNode("patHist.json", true); }
    private Node mFamilyHistoryNode;
    private Node loadFamilyHistory() { return loadFileToNode("famHist.json", true); }

    private Node loadFileToNode(String fileName, boolean isForRootFile) {
        JSONObject currentFile = null;
        if (!sessionManager.getLicenseKey().isEmpty()) {
            if (isForRootFile) { try { currentFile = new JSONObject(Objects.requireNonNull(FileUtils.readFileRoot(fileName, this))); } catch (JSONException e) { throw new RuntimeException(e); } }
            else currentFile = FileUtils.encodeJSONFromFile(this, fileName);
        } else currentFile = FileUtils.encodeJSON(this, fileName);
        Node mainNode = new Node(currentFile);
        mainNode.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
        return mainNode;
    }

    private Node mCommonAssociateSymptoms = null;

    private void loadChiefComplainNodeForSelectedNames(List<ReasonData> selectedComplains) {
        for (int i = 0; i < selectedComplains.size(); i++) {
            String fileName = selectedComplains.get(i).getReasonName() + ".json";
            if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) fileName = selectedComplains.get(i).getDefaultReasonName() + ".json";
            String fileLocation = "engines/" + fileName;
            JSONObject currentFile = !sessionManager.getLicenseKey().isEmpty() ? FileUtils.encodeJSONFromFile(this, fileName) : FileUtils.encodeJSON(this, fileLocation);
            Node mainNode = new Node(currentFile);
            if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) { mainNode.setDisplay(selectedComplains.get(i).getReasonName()); mainNode.setText(selectedComplains.get(i).getReasonName()); mainNode.setCompareDuplicateNode(selectedComplains.get(i).getReasonName()); }
            List<Node> optionList = new ArrayList<>();
            for (int j = 0; j < mainNode.getOptionsList().size(); j++) {
                if (mainNode.getOptionsList().get(j).getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS)) {
                    if (mCommonAssociateSymptoms == null) mCommonAssociateSymptoms = mainNode.getOptionsList().get(j);
                    else mCommonAssociateSymptoms.getOptionsList().addAll(mainNode.getOptionsList().get(j).getOptionsList());
                } else {
                    if (VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, mainNode.getOptionsList().get(j).getGender(), mainNode.getOptionsList().get(j).getMin_age(), mainNode.getOptionsList().get(j).getMax_age())) {
                        if (mainNode.getOptionsList().get(j).getOptionsList() != null) mainNode.getOptionsList().get(j).getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
                        optionList.add(mainNode.getOptionsList().get(j));
                    }
                }
            }
            mainNode.setOptionsList(optionList);
            mChiefComplainRootNodeList.add(mainNode);
        }
        if (mCommonAssociateSymptoms != null) {
            mCommonAssociateSymptoms.setOptionsList(getNodeWithoutDuplicates(mCommonAssociateSymptoms.getOptionsList()));
            mCommonAssociateSymptoms.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
            mChiefComplainRootNodeList.add(mCommonAssociateSymptoms);
        }
    }

    private static List<Node> getNodeWithoutDuplicates(final List<Node> nodes) {
        Set<Node> nodeSet = new TreeSet<>(new NodeComparator());
        nodeSet.addAll(nodes);
        return new ArrayList<>(nodeSet);
    }

    static class NodeComparator implements Comparator<Node> {
        @Override public int compare(Node n1, Node n2) { return n1.getText().compareToIgnoreCase(n2.getText()); }
    }

    @Override
    public void onProgress(int progress) {
        switch (mCurrentStep) {
            case STEP_3_VISIT_REASON_QUESTION: mStep3ProgressBar.setProgress(mStep3ProgressBar.getProgress() + progress); break;
            case STEP_4_PHYSICAL_EXAMINATION: mStep4ProgressBar.setProgress(mStep3ProgressBar.getProgress() + progress); break;
        }
    }

    @Override public void onTitleChange(String title) { Timber.tag(TAG).d("onTitleChange=>%s", mCurrentStep); }

    @Override
    public void onManualClose() {
        if (mCurrentStep == STEP_1_VITAL_SUMMARY) mSummaryFrameLayout.setVisibility(View.GONE);
    }

    @Override public void onCameraOpenRequest() { openCamera(); }
    @Override public void onImageRemoved(int nodeIndex, int imageIndex, String image) { deleteImageFromDatabase(nodeIndex, imageIndex, image); }
    @Override public void onAyuDeviceRequest(Node node) {}

    boolean nodeComplete = false;
    public void filterNodeQuestions() {}

    String insertion = "";
    String insertionLocale = "";
    String insertionLocaleEn = "";
    String insertionWithLocaleJsonString = "";

    public String formatComplainRecord(Node currentNode, boolean isAssociateSymptom) {
        AnswerResult answerResult = isAssociateSymptom ? currentNode.checkAllRequiredAnsweredRootNode(this) : currentNode.checkAllRequiredAnswered(this);
        if (!answerResult.result) {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), answerResult.requiredStrings, true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() { @Override public void onDialogActionDone(int action) {} });
            return null;
        }
        String complaintString = isAssociateSymptom ? currentNode.generateLanguageSingleNode() : currentNode.generateLanguage();
        if (complaintString != null && !complaintString.isEmpty()) insertion = insertion.concat(bullet_arrow + "<b>" + currentNode.getText() + "</b>: " + Node.next_line + complaintString + " ");
        else if (!currentNode.getText().equalsIgnoreCase(getResources().getString(R.string.associated_symptoms))) insertion = insertion.concat(bullet_arrow + "<b>" + currentNode.getText() + "</b>: " + Node.next_line + " ");
        return insertion;
    }

    private void showNextComplainQueries() { mCurrentComplainNodeIndex++; mStep2ProgressBar.setProgress(mStep2ProgressBar.getProgress() + 10); }

    private boolean insertChiefComplainToDb(String value) {
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO(); ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT); obsDTO.setEncounteruuid(encounterAdultIntials); obsDTO.setCreator(sessionManager.getCreatorID()); obsDTO.setValue(StringUtils.getValue1(value));
            if (uuidOBS != null) { obsDTO.setUuid(uuidOBS); isInserted = obsDAO.updateObs(obsDTO); } else isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
        return isInserted;
    }

    private void updateDatabase(String string) {
        ObsDTO obsDTO = new ObsDTO(); ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT); obsDTO.setEncounteruuid(encounterAdultIntials); obsDTO.setCreator(sessionManager.getCreatorID()); obsDTO.setValue(string); obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT));
            obsDAO.updateObs(obsDTO);
        } catch (DAOException dao) { FirebaseCrashlytics.getInstance().recordException(dao); }
        EncounterDAO encounterDAO = new EncounterDAO();
        try { encounterDAO.updateEncounterSync("false", encounterAdultIntials); encounterDAO.updateEncounterModifiedDate(encounterAdultIntials); }
        catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
    }

    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getPhysicalExams();
        if (rawExams != null) { examList.addAll(Arrays.asList(rawExams.split(";"))); return examList; }
        return null;
    }

    public static void openCamera(Activity activity, String imagePath, String imageName) {
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) filePath.mkdirs();
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        activity.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean insertDbPhysicalExam(String value) {
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO(); ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PHYSICAL_EXAMINATION);
            obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION); obsDTO.setEncounteruuid(encounterAdultIntials); obsDTO.setCreator(sessionManager.getCreatorID()); obsDTO.setValue(StringUtils.getValue(value));
            if (uuidOBS != null) { obsDTO.setUuid(uuidOBS); isInserted = obsDAO.updateObs(obsDTO); } else isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
        return isInserted;
    }

    String physicalString;
    String physicalStringLocale = "";
    String physicalStringLocaleEn = "";
    String physicalStringWithLocaleJsonString = "";
    Boolean complaintConfirmed = false;
    PhysicalExam physicalExamMap;

    private boolean savePhysicalExamData() {
        sessionManager.setVisitEditCache(SessionManager.PHY_EXAM + visitUuid, new Gson().toJson(physicalExamMap));
        complaintConfirmed = physicalExamMap.areRequiredAnswered();
        if (complaintConfirmed) {
            physicalString = physicalExamMap.generateFindings();
            physicalStringLocale = physicalExamMap.generateFindingsByLocale(sessionManager.getAppLanguage());
            physicalStringLocaleEn = physicalExamMap.generateFindingsByLocale("en");
            while (physicalString.contains("[Describe")) physicalString = physicalString.replace("[Describe]", "");
            for (int i = 0; i < physicalExamMap.getTotalNumberOfExams(); i++) {
                Node l1Node = physicalExamMap.getExamNode(i);
                for (int j = 0; j < l1Node.getOptionsList().size(); j++) {
                    Node l2Node = l1Node.getOptionsList().get(j);
                    List<String> imagePathList = l2Node.getImagePathList();
                    if (imagePathList != null && imagePathList.size() > 0) {
                        if (l2Node.isImageUploaded()) { for (String imagePath : imagePathList) { String comments = l2Node.getImagePathListWithSectionTag().get(imagePath); String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1).split("\\.")[0]; updateImageDatabase(fileName, comments); } }
                        else { Toast.makeText(this, getString(R.string.image_upload_pending_alert), Toast.LENGTH_SHORT).show(); return false; }
                    }
                }
            }
            JSONObject jsonObject = new JSONObject();
            try {
                physicalStringLocale = VisitUtils.replaceEnglishCommonString(physicalStringLocale, sessionManager.getAppLanguage());
                physicalStringLocaleEn = VisitUtils.replaceEnglishCommonString(physicalStringLocaleEn, "en");
                if (physicalStringLocale != null && !sessionManager.getAppLanguage().equals("en")) physicalStringLocale = physicalStringLocale.replaceAll("picture taken", getString(R.string.picture_taken));
                String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(physicalStringLocale);
                if (matchDate != null) for (String date : matchDate) physicalStringLocale = physicalStringLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                physicalString = VisitUtils.replaceToEnglishCommonString(physicalString, sessionManager.getAppLanguage());
                jsonObject.put("en", physicalString); jsonObject.put("l-" + sessionManager.getAppLanguage(), physicalStringLocale);
                physicalStringWithLocaleJsonString = jsonObject.toString().replace("\\/", "/");
            } catch (JSONException e) { e.printStackTrace(); }
        } else { questionsMissing(); return false; }
        return insertDbPhysicalExam(physicalStringWithLocaleJsonString);
    }

    private String patientHistory, familyHistory;
    String patientHistoryLocale = "", familyHistoryLocale = "";
    String patientHistoryLocaleEn = "", familyHistoryLocaleEn = "";
    String patientHistoryWithLocaleJsonString = "", familyHistoryWithLocaleJsonString = "";

    private boolean savePastHistoryData() {
        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) return saveOnlyFamilyHistory();
        sessionManager.setVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid, new Gson().toJson(mPastMedicalHistoryNode));
        sessionManager.setVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid, new Gson().toJson(mFamilyHistoryNode));
        patientHistory = mPastMedicalHistoryNode.generateLanguage();
        patientHistoryLocale = mPastMedicalHistoryNode.formQuestionAnswer(0, false);
        patientHistoryLocaleEn = mPastMedicalHistoryNode.formQuestionAnswer(0, false, "en");
        while (patientHistory != null && patientHistory.contains("[Describe")) patientHistory = patientHistory.replace("[Describe]", "");
        familyHistory = generateFamilyHistoryAns(false, "en");
        if (familyHistory == null || familyHistory.trim().isEmpty()) {
            new DialogUtils().showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), getString(R.string.you_missed_the_compulsory_questions_please_answer_them), true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), action -> {});
            return false;
        }
        familyHistoryLocale = generateFamilyHistoryAns(true, sessionManager.getAppLanguage());
        familyHistoryLocaleEn = generateFamilyHistoryAns(true, "en");
        familyHistory = familyHistory.replaceAll("null.", "");
        while (familyHistory.contains("[Describe")) familyHistory = familyHistory.replace("[Describe]", "");
        List<String> imagePathList = mFamilyHistoryNode.getImagePathList();
        if (imagePathList != null) for (String imagePath : imagePathList) updateImageDatabase(imagePath, mFamilyHistoryNode.getImagePathListWithSectionTag().get(imagePath));
        JSONObject jsonObject = new JSONObject(); JSONObject jsonObject1 = new JSONObject();
        try {
            patientHistoryLocale = VisitUtils.replaceEnglishCommonString(patientHistoryLocale, sessionManager.getAppLanguage());
            patientHistoryLocaleEn = VisitUtils.replaceEnglishCommonString(patientHistoryLocaleEn, "en");
            String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(patientHistoryLocale);
            if (matchDate != null) for (String date : matchDate) patientHistoryLocale = patientHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
            patientHistory = VisitUtils.replaceToEnglishCommonString(patientHistory, sessionManager.getAppLanguage());
            jsonObject.put("en", patientHistory); jsonObject.put("l-" + sessionManager.getAppLanguage(), patientHistoryLocale);
            patientHistoryWithLocaleJsonString = jsonObject.toString().replace("\\/", "/");
            familyHistoryLocale = VisitUtils.replaceEnglishCommonString(familyHistoryLocale, sessionManager.getAppLanguage());
            familyHistoryLocaleEn = VisitUtils.replaceEnglishCommonString(familyHistoryLocaleEn, "en");
            String[] matchDate1 = DateAndTimeUtils.findDateFromStringDDMMMYYY(familyHistoryLocale);
            if (matchDate1 != null) for (String date : matchDate1) familyHistoryLocale = familyHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
            familyHistory = VisitUtils.replaceToEnglishCommonString(familyHistory, sessionManager.getAppLanguage());
            jsonObject1.put("en", familyHistory); jsonObject1.put("l-" + sessionManager.getAppLanguage(), familyHistoryLocale);
            familyHistoryWithLocaleJsonString = jsonObject1.toString().replace("\\/", "/");
        } catch (JSONException e) { e.printStackTrace(); }
        return insertDbPastHistory(patientHistoryWithLocaleJsonString, familyHistoryWithLocaleJsonString);
    }

    private boolean saveOnlyFamilyHistory() {
        sessionManager.setVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid, new Gson().toJson(mFamilyHistoryNode));
        familyHistory = generateFamilyHistoryAns(false, "en");
        if (familyHistory == null || familyHistory.trim().isEmpty()) {
            new DialogUtils().showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), getString(R.string.you_missed_the_compulsory_questions_please_answer_them), true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), action -> {});
            return false;
        }
        familyHistoryLocale = generateFamilyHistoryAns(true, sessionManager.getAppLanguage());
        familyHistoryLocaleEn = generateFamilyHistoryAns(true, "en");
        familyHistory = familyHistory.replaceAll("null.", "");
        while (familyHistory.contains("[Describe")) familyHistory = familyHistory.replace("[Describe]", "");
        List<String> imagePathList = mFamilyHistoryNode.getImagePathList();
        if (imagePathList != null) for (String imagePath : imagePathList) updateImageDatabase(imagePath, mFamilyHistoryNode.getImagePathListWithSectionTag().get(imagePath));
        JSONObject jsonObject1 = new JSONObject();
        try {
            familyHistoryLocale = VisitUtils.replaceEnglishCommonString(familyHistoryLocale, sessionManager.getAppLanguage());
            familyHistoryLocaleEn = VisitUtils.replaceEnglishCommonString(familyHistoryLocaleEn, "en");
            String[] matchDate1 = DateAndTimeUtils.findDateFromStringDDMMMYYY(familyHistoryLocale);
            if (matchDate1 != null) for (String date : matchDate1) familyHistoryLocale = familyHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
            familyHistory = VisitUtils.replaceToEnglishCommonString(familyHistory, sessionManager.getAppLanguage());
            jsonObject1.put("en", familyHistory); jsonObject1.put("l-" + sessionManager.getAppLanguage(), familyHistoryLocale);
            familyHistoryWithLocaleJsonString = jsonObject1.toString().replace("\\/", "/");
        } catch (JSONException e) { e.printStackTrace(); }
        return insertDbPastHistory(null, familyHistoryWithLocaleJsonString);
    }

    private String generateFamilyHistoryAns(boolean isLocale, String locale) {
        String familyHistory = "";
        ArrayList<String> familyInsertionList = new ArrayList<>();
        for (Node node : mFamilyHistoryNode.getOptionsList()) { if (!node.checkIsAnswered()) return null; }
        if (mFamilyHistoryNode.anySubSelected()) {
            for (Node node : mFamilyHistoryNode.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = !isLocale ? node.generateLanguage() : node.formQuestionAnswer(0, false, locale);
                    String toInsert = (!isLocale ? node.getText() : node.findDisplay(locale)) + " : " + familyString;
                    toInsert = toInsert.replaceAll(" - ", ", ").replaceAll("<br/>", "");
                    if (org.apache.commons.lang3.StringUtils.right(toInsert, 2).equals(", ")) toInsert = toInsert.substring(0, toInsert.length() - 2);
                    familyInsertionList.add(toInsert + ".<br/>");
                }
            }
        }
        for (int i = 0; i < familyInsertionList.size(); i++) familyHistory = i == 0 ? familyInsertionList.get(i) : familyHistory + " " + Node.bullet + familyInsertionList.get(i);
        return familyHistory;
    }

    private boolean insertDbPastHistory(String patientHistory, String familyHistory) {
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO(); ObsDTO obsDTO = new ObsDTO();
            if (patientHistory != null) {
                String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB); obsDTO.setEncounteruuid(encounterAdultIntials); obsDTO.setCreator(sessionManager.getCreatorID()); obsDTO.setValue(StringUtils.getValue(patientHistory));
                if (uuidOBS != null) { obsDTO.setUuid(uuidOBS); isInserted = obsDAO.updateObs(obsDTO); } else isInserted = obsDAO.insertObs(obsDTO);
            }
            if (familyHistory != null) {
                String uuidOBS1 = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB); obsDTO.setEncounteruuid(encounterAdultIntials); obsDTO.setCreator(sessionManager.getCreatorID()); obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(familyHistory));
                if (uuidOBS1 != null) { obsDTO.setUuid(uuidOBS1); isInserted = obsDAO.updateObs(obsDTO); } else isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
        return isInserted;
    }

    private boolean insertLocalEnFormatQAValues() {
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            String insertDbEnValue = "Visit Reason (Chief Complaint)\n" + insertionLocaleEn + "\nPhysical Examination:\n" + physicalStringLocaleEn + "\nPatient Medical History:\n" + patientHistoryLocaleEn + "\nFamily History:\n" + familyHistoryLocaleEn;
            String uuidOBS1 = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.AI_VISIT_SUMMARY_CONCEPT_UUID);
            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.AI_VISIT_SUMMARY_CONCEPT_UUID); obsDTO.setEncounteruuid(encounterAdultIntials); obsDTO.setCreator(sessionManager.getCreatorID()); obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(insertDbEnValue));
            if (uuidOBS1 != null) { obsDTO.setUuid(uuidOBS1); isInserted = obsDAO.updateObs(obsDTO); } else isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
        return isInserted;
    }
    ImageUtilsListener imageUtilsListener;
    public void questionsMissing() {
        new DialogUtils().showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), getResources().getString(R.string.question_answer_all_phy_exam), true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() { @Override public void onDialogActionDone(int action) {} });
    }

    private void updateImageDatabase(String imageName, String comments) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try { imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE, comments); }
        catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
    }

    private void deleteImageFromDatabase(int nodeIndex, int imageIndex, String imageName) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            String obsUUID = imageName.substring(imageName.lastIndexOf("/") + 1).split("\\.")[0];
            imagesDAO.deleteImageFromDatabase(obsUUID);
            imageUtilsListener.onImageReadyForDelete(nodeIndex, imageIndex, imageName);
        } catch (DAOException e) { FirebaseCrashlytics.getInstance().recordException(e); }
    }

    ActivityResultLauncher<Intent> mStartForCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            String mCurrentPhotoPath = result.getData().getStringExtra("RESULT");
            Bundle bundle = new Bundle(); bundle.putString("image", mCurrentPhotoPath);
            imageUtilsListener.onImageReady(bundle); CustomLog.i(TAG, mCurrentPhotoPath);
        }
    });

    ActivityResultLauncher<Intent> mStartForGalleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri selectedImage = result.getData().getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst(); String picturePath = c.getString(c.getColumnIndex(filePath[0])); c.close();
            mLastSelectedImageName = UUID.randomUUID().toString();
            String currentPhotoPath = AppConstants.IMAGE_PATH + mLastSelectedImageName + ".jpg";
            File file = new File(currentPhotoPath);
            if (file.length() / 1024 / 1024 > 2) { String compressedPath = AppConstants.IMAGE_PATH + mLastSelectedImageName + "_compressed.jpg"; compressImage(currentPhotoPath, compressedPath); currentPhotoPath = compressedPath; }
            BitmapUtils.copyFile(picturePath, currentPhotoPath);
            Bundle bundle = new Bundle(); bundle.putString("image", currentPhotoPath);
            imageUtilsListener.onImageReady(bundle);
        } else { Toast.makeText(VisitCreationActivity.this, getResources().getString(R.string.unable_to_pick_data), Toast.LENGTH_SHORT).show(); }
    });

    private void compressImage(String inputPath, String outputPath) {
        Bitmap bitmap = BitmapFactory.decodeFile(inputPath, new BitmapFactory.Options());
        try { FileOutputStream out = new FileOutputStream(outputPath); bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); out.flush(); out.close(); } catch (Exception e) { e.printStackTrace(); }
    }


    public void openCamera() { validatePermissionAndIntent(); }

    private void cameraStart() {
        File file = new File(AppConstants.IMAGE_PATH);
        final String imagePath = file.getAbsolutePath();
        final String imageName = UUID.randomUUID().toString();
        mLastSelectedImageName = imageName;
        Intent cameraIntent = new Intent(VisitCreationActivity.this, CameraActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) filePath.mkdirs();
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        mStartForCameraResult.launch(cameraIntent);
    }

    private void galleryStart() { mStartForGalleryResult.launch(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)); }

    private static final int MY_CAMERA_REQUEST_CODE = 1001;
    private AlertDialog mImagePickerAlertDialog;

    private void selectImage() {
        if (mImagePickerAlertDialog != null && mImagePickerAlertDialog.isShowing()) mImagePickerAlertDialog.dismiss();
        mImagePickerAlertDialog = DialogUtils.showCommonImagePickerDialog(this, getString(R.string.add_image_by), new DialogUtils.ImagePickerDialogListener() {
            @Override public void onActionDone(int action) {
                mImagePickerAlertDialog.dismiss();
                if (action == DialogUtils.ImagePickerDialogListener.CAMERA) cameraStart();
                else if (action == DialogUtils.ImagePickerDialogListener.GALLERY) galleryStart();
            }
        });
    }

    private void validatePermissionAndIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        else selectImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) selectImage();
            else Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
        }
    }


    public void setImageUtilsListener(ImageUtilsListener imageUtilsListener) { this.imageUtilsListener = imageUtilsListener; }

    private ObjectAnimator syncAnimator;

    public void syncNow(View view) {
        if (mIsEditTriggerFromVisitSummary) { if (NetworkConnection.isOnline(this)) SyncUtils.syncNow(this, view, syncAnimator); }
        else showConfirmationDialog(getString(R.string.confirm_discard_changes_content_on_sync));
    }

    public void showInfo(View view) {}

    public interface ImageUtilsListener {
        void onImageReady(Bundle bundle);
        void onImageReadyForDelete(int nodeIndex, int imageIndex, String imageName);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    public FeatureActiveStatus getFeatureActiveStatus() { return featureActiveStatus; }

    public void setTitle(int screenId) {
        Timber.tag(TAG).d("setTitle=>%s", screenId);
        int currentScreenIndex = 1; String title = "";
        boolean isVitalEnabled = featureActiveStatus.getVitalSection();
        boolean isDiagnosticsEnabled = featureActiveStatus.getActiveStatusDiagnosticsSection();
        int vitalScreenIndex = isVitalEnabled ? 1 : 0;
        int diagnosticsScreenIndex = isDiagnosticsEnabled ? vitalScreenIndex + 1 : vitalScreenIndex;
        int visitReasonScreenIndex = Math.max(vitalScreenIndex, diagnosticsScreenIndex) + 1;
        int adjustedTotalScreen = 5;
        if (!isVitalEnabled) adjustedTotalScreen--;
        if (!isDiagnosticsEnabled) adjustedTotalScreen--;
        switch (screenId) {
            case STEP_1_VITAL: if (isVitalEnabled) { currentScreenIndex = vitalScreenIndex; title = getString(R.string._1_4_vitals, currentScreenIndex, adjustedTotalScreen); } break;
            case STEP_2_DIAGNOSTICS: if (isDiagnosticsEnabled) { currentScreenIndex = diagnosticsScreenIndex; title = getString(R.string.diagnostics_section, currentScreenIndex, adjustedTotalScreen); } break;
            case STEP_3_VISIT_REASON: currentScreenIndex = visitReasonScreenIndex; title = getString(R.string.visit_reason, currentScreenIndex, adjustedTotalScreen); if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) title = getString(R.string.visit_reason, currentScreenIndex, totalScreen); break;
            case STEP_3_VISIT_REASON_QUESTION:
                currentScreenIndex = visitReasonScreenIndex;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < mSelectedComplainList.size(); i++) { builder.append(mSelectedComplainList.get(i).getReasonNameLocalized()); if (i < mSelectedComplainList.size() - 1) builder.append(", "); }
                title = getString(R.string.visit_reason, currentScreenIndex, adjustedTotalScreen) + " : " + builder; break;
            case STEP_3_VISIT_REASON_QUESTION_SUMMARY: currentScreenIndex = visitReasonScreenIndex; title = getString(R.string._visit_reason_summary, currentScreenIndex, adjustedTotalScreen) + " : " + mSelectedComplainList.get(0).getReasonNameLocalized(); break;
            case STEP_4_PHYSICAL_EXAMINATION: currentScreenIndex = visitReasonScreenIndex + 1; title = getString(R.string._phy_examination, currentScreenIndex, adjustedTotalScreen); if (BuildConfig.FLAVOR_client == FlavorKeys.KCDO) title = getString(R.string._relapse, currentScreenIndex, totalScreen); else if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) title = getString(R.string._obstetric_history, currentScreenIndex, totalScreen); break;
            case STEP_5_PAST_MEDICAL_HISTORY: currentScreenIndex = visitReasonScreenIndex + 2; title = getString(R.string.patinet_history, currentScreenIndex, adjustedTotalScreen); break;
            case STEP_6_FAMILY_HISTORY: currentScreenIndex = visitReasonScreenIndex + 2; title = getString(R.string._medical_family_history, currentScreenIndex, adjustedTotalScreen); break;
            default: Log.w(TAG, "Unknown screenId: " + screenId);
        }
        ((TextView) findViewById(R.id.tv_sub_title)).setText(title);
    }

    private void showConfirmationDialog(String content) {
        new DialogUtils().showCommonDialog(this, R.drawable.fingerprint_dialog_error, getResources().getString(R.string.confirm_discard_changes_title), content, false,
                getResources().getString(R.string.confirm_continue_changes_button_dialog), getResources().getString(R.string.confirm_discard_changes_button_dialog), action -> {
                    if (action == DialogUtils.CustomDialogListener.NEGATIVE_CLICK) { new VisitsDAO().deleteAllDataForOngoingIncompleteVisit(visitUuid); finish(); }
                });
    }

    @Override public void onDigitalScopeCompleted() { onFormSubmitted(STEP_7_VISIT_SUMMARY_FINAL, false, null); }
    public boolean isHeartRecorded() { return isHeartRecorded; }
    public boolean isLungRecorded() { return isLungRecorded; }

    @Override
    public void onRecordingCompleted(String type) {
        if ("heart".equals(type)) isHeartRecorded = true; else isLungRecorded = true;
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) getSupportFragmentManager().popBackStack();
        else super.onBackPressed();
    }
}