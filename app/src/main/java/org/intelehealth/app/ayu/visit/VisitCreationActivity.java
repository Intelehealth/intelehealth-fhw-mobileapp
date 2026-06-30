package org.intelehealth.app.ayu.visit;

import static org.intelehealth.app.knowledgeEngine.Node.bullet_arrow;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

    /**
     * Activity-scoped ViewModel — survives fragment transactions.
     */
    private HbA1cLiveViewModel mHba1cViewModel;
    private ControlCentre mControlCentre;          // owns the actual BLE/SDK connection
    private String mHba1cDeviceAddress;            // kept so we can restart if needed

    /**
     * SharedPrefs key where BleScanActivity saves the chosen device address.
     */
    private static final String PREF_BLE_ADDRESS = "hba1c_ble_address";

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
    /**
     * Sound exam types already recorded in this visit (e.g. "heart", "lung").
     * Survives fragment recreation; lets us suppress the adapter's auto
     * showAyuDeviceDialog() trigger when returning from SoundFragment.
     */
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

        encounterAdultIntials = "";
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
                CustomLog.d(TAG, "featureActiveStatus vitals first screen : " + featureActiveStatus.getVitalSection());
                CustomLog.d(TAG, "featureActiveStatus diagnostics first screen : " + featureActiveStatus.getActiveStatusDiagnosticsSection());

                if (!isVitalsActive) {
                    mStep1ProgressBar.setVisibility(View.GONE);
                    mCurrentStep = STEP_2_DIAGNOSTICS;
                    totalScreen = 4;
                    Timber.tag(TAG).d("1 Feature first screen : " + mCurrentStep);
                }
                if (!isDiagnosticsActive) {
                    mStep2ProgressBar.setVisibility(View.GONE);
                    mCurrentStep = STEP_1_VITAL;
                    totalScreen = 4;
                    Timber.tag(TAG).d("2 Feature first screen : " + mCurrentStep);
                }
                if (isVitalsActive && isDiagnosticsActive) {
                    mStep1ProgressBar.setVisibility(View.VISIBLE);
                    mStep2ProgressBar.setVisibility(View.VISIBLE);
                    mCurrentStep = STEP_1_VITAL;
                    totalScreen = 5;
                    Timber.tag(TAG).d("3 Feature first screen : " + mCurrentStep);
                }
                if (!isVitalsActive && isDiagnosticsActive) {
                    mStep1ProgressBar.setVisibility(View.GONE);
                    mStep2ProgressBar.setVisibility(View.VISIBLE);
                    mCurrentStep = STEP_2_DIAGNOSTICS;
                    totalScreen = 4;
                    Timber.tag(TAG).d("4 Feature first screen : " + mCurrentStep);
                }
                if (isVitalsActive && !isDiagnosticsActive) {
                    mStep1ProgressBar.setVisibility(View.VISIBLE);
                    mStep2ProgressBar.setVisibility(View.GONE);
                    mCurrentStep = STEP_1_VITAL;
                    totalScreen = 4;
                    Timber.tag(TAG).d("5 Feature first screen : " + mCurrentStep);
                }
                if (!isVitalsActive && !isDiagnosticsActive) {
                    mStep1ProgressBar.setVisibility(View.GONE);
                    mStep2ProgressBar.setVisibility(View.GONE);
                    mCurrentStep = STEP_3_VISIT_REASON;
                    totalScreen = 3;
                    Timber.tag(TAG).d("6 Feature first screen : " + mCurrentStep);
                }
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
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

        if (!sessionManager.getLicenseKey().isEmpty())
            mHasLicence = true;
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
            if (Integer.parseInt(temp[0]) == 0) {
                mAgeAndMonth = temp[1] + " " + getResources().getString(R.string.months);
            } else if (Integer.parseInt(temp[0]) == 0) {
                mAgeAndMonth = temp[0] + " " + getResources().getString(R.string.years);
            } else {
                mAgeAndMonth = temp[0] + " " + getResources().getString(R.string.years) + " " + temp[1] + " " + getResources().getString(R.string.months);
            }

            if (intentTag.equalsIgnoreCase("edit")) {
                mIsEditMode = true;
                mIsEditTriggerFromVisitSummary = true;
            }
            CustomLog.v(TAG, "Patient ID: " + patientUuid);
            CustomLog.v(TAG, "Visit ID: " + visitUuid);
            CustomLog.v(TAG, "Patient Name: " + patientName);
            CustomLog.v(TAG, "Intent Tag: " + intentTag);
            CustomLog.v(TAG, "Intent float_ageYear_Month: " + float_ageYear_Month);
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
        CustomLog.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        Bundle bundle = new Bundle();
        bundle.putString("patientUuid", patientUuid);
        bundle.putString("visitUuid", visitUuid);
        bundle.putString("encounterUuidVitals", encounterVitals);

        handleDeviceBackPress();
        initHba1cBle();
    }

    private void initHba1cBle() {
        // ViewModel is scoped to this Activity — all fragments share the same instance.
        mHba1cViewModel = new ViewModelProvider(this).get(HbA1cLiveViewModel.class);

        SharedPreferences prefs = getSharedPreferences("hba1c_prefs", MODE_PRIVATE);
        String savedAddress = prefs.getString(PREF_BLE_ADDRESS, null);
        Log.d("HBA1C_DEBUG", "initHba1cBle: savedAddress = " + savedAddress);

        if (savedAddress != null && !savedAddress.isEmpty()) {
            startHba1cControlCentre(savedAddress);
        } else {
            Log.d("HBA1C_DEBUG", "initHba1cBle: no saved address, user must scan");
        }
    }

    /**
     * Starts (or restarts) the BioSense ControlCentre for the given device
     * address. Safe to call again with a new address — stops the previous
     * receiver first so we never have two ControlCentre instances racing.
     * Single definition only — checks BLUETOOTH_CONNECT permission first.
     */
    private void startHba1cControlCentre(String deviceAddress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("HBA1C_DEBUG", "startHba1cControlCentre: BLUETOOTH_CONNECT not granted — aborting");
            return;
        }

        try {
            if (mControlCentre != null) {
                mControlCentre.stopReceiver();
            }
            mHba1cDeviceAddress = deviceAddress;
            mHba1cViewModel.reset();

            mControlCentre = new ControlCentre(
                    this,                       // Communicator
                    this,                       // Context
                    this,                       // Activity
                    deviceAddress,               // BLE MAC address
                    Constants.devId_A1Chek,      // Device type
                    "HbA1c"                      // Device name label
            );
            mControlCentre.startReceiver();
            Log.d("HBA1C_DEBUG", "startHba1cControlCentre: started for " + deviceAddress);
        } catch (SecurityException e) {
            Log.e("HBA1C_DEBUG", "startHba1cControlCentre: SecurityException — " + e.getMessage());
        }
    }

    // ── Getter — fragments call this to get the shared ViewModel ──────────
    public HbA1cLiveViewModel getHba1cViewModel() {
        return mHba1cViewModel;
    }

    // ── Save device address when BleScanActivity returns ──────────────────
    public void saveAndStartBleDevice(String deviceAddress) {
        getSharedPreferences("hba1c_prefs", MODE_PRIVATE)
                .edit()
                .putString(PREF_BLE_ADDRESS, deviceAddress)
                .apply();

        Log.d("HBA1C_DEBUG", "saveAndStartBleDevice: saved + starting " + deviceAddress);
        startHba1cControlCentre(deviceAddress);
    }

    private void handleDeviceBackPress() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mIsEditTriggerFromVisitSummary) {
                    showConfirmationDialog(getString(R.string.confirm_discard_changes_content));
                }
            }
        });
    }

    public boolean isEditTriggerFromVisitSummary() {
        return mIsEditTriggerFromVisitSummary;
    }

    private void makeReadyForEdit() {
        findViewById(R.id.ll_progress_steps).setVisibility(View.GONE);
        mSelectedComplainList = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid), new TypeToken<List<ReasonData>>() {
        }.getType());

        mChiefComplainRootNodeList = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid), new TypeToken<List<Node>>() {
        }.getType());

        if (!sessionManager.getVisitEditCache(SessionManager.PHY_EXAM + visitUuid).isEmpty()) {
            physicalExamMap = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.PHY_EXAM + visitUuid), PhysicalExam.class);
            physicalExamMap.refreshOnlyLocaleTitle();
        } else
            loadPhysicalExam();

        if (BuildConfig.FLAVOR_client != FlavorKeys.UNFPA) {
            if (!sessionManager.getVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid).isEmpty())
                mPastMedicalHistoryNode = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid), Node.class);
            else
                mPastMedicalHistoryNode = loadPastMedicalHistory();
        }
        if (!sessionManager.getVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid).isEmpty())
            mFamilyHistoryNode = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid), Node.class);
        else
            mFamilyHistoryNode = loadFamilyHistory();
        int currentScreenIndex = 1;
        setTitle(mEditFor);
        switch (mEditFor) {
            case STEP_1_VITAL:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(mCommonVisitData, mIsEditMode, null), VITAL_FRAGMENT).
                        commit();
                break;
            case STEP_2_DIAGNOSTICS:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, DiagnosticsCollectionFragment.newInstance(mCommonVisitData, mIsEditMode, null), DIAGNOSTICS_FRAGMENT).
                        commit();
                break;
            case STEP_3_VISIT_REASON:
                setTitle(STEP_3_VISIT_REASON_QUESTION);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(mCommonVisitData, mIsEditMode, mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).
                        commit();
                break;
            case STEP_4_PHYSICAL_EXAMINATION:
                mStep4ProgressBar.setProgress(10);
                mSummaryFrameLayout.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(mCommonVisitData, mIsEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                        commit();
                break;
            case STEP_5_PAST_MEDICAL_HISTORY:
                showPastMedicalHistoryFragment(mIsEditMode);
                break;
            case STEP_6_FAMILY_HISTORY:
                showFamilyHistoryFragment(mIsEditMode);
                break;
        }
    }

    public void backPress(View view) {
        if (!mIsEditTriggerFromVisitSummary) {
            showConfirmationDialog(getString(R.string.confirm_discard_changes_content));
        }
    }

    private VitalsObject mVitalsObject;
    private DiagnosticsModel mDiagnosticsModel;

    public VitalsObject getVitalsObject() {
        return mVitalsObject;
    }

    @Override
    public void onFormSubmitted(int nextAction, boolean isEditMode, Object object) {
        mCurrentStep = nextAction;
        Timber.tag(TAG).d("first screen=>%s", nextAction);
        switch (nextAction) {
            case STEP_1_VITAL_SUMMARY:
                if (object != null)
                    mVitalsObject = (VitalsObject) object;
                if (mVitalsObject != null) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    mStep1ProgressBar.setProgress(100);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, VitalCollectionSummaryFragment.newInstance(mVitalsObject, isEditMode, visitUuid), VITAL_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_1_VITAL:
                mStep1ProgressBar.setProgress(100);
                mStep2ProgressBar.setProgress(0);
                mStep3ProgressBar.setProgress(0);
                mStep4ProgressBar.setProgress(0);
                mStep5ProgressBar.setProgress(0);
                mSummaryFrameLayout.setVisibility(View.GONE);
                setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(mCommonVisitData, isEditMode, mVitalsObject), VITAL_FRAGMENT).
                        commit();
                break;
            case STEP_2_DIAGNOSTICS:
                mStep2ProgressBar.setProgress(100);
                mSummaryFrameLayout.setVisibility(View.GONE);
                setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, DiagnosticsCollectionFragment.newInstance(mCommonVisitData, isEditMode, mDiagnosticsModel), DIAGNOSTICS_FRAGMENT).
                        commit();
                break;
            case STEP_2_DIAGNOSTICS_SUMMARY:
                if (object != null)
                    mDiagnosticsModel = (DiagnosticsModel) object;
                if (mDiagnosticsModel != null) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    mStep2ProgressBar.setProgress(100);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, DiagnosticsCollectionSummaryFragment.newInstance(mDiagnosticsModel, isEditMode, visitUuid), DIAGNOSTICS_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_3_VISIT_REASON:
                getSupportFragmentManager().popBackStack();
                mStep3ProgressBar.setProgress(30);
                setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonCaptureFragment.newInstance(mCommonVisitData, isEditMode, false), VISIT_REASON_FRAGMENT).
                        commit();
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;

            case STEP_3_VISIT_REASON_QUESTION:
                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    getSupportFragmentManager().popBackStack();
                    mSummaryFrameLayout.setVisibility(View.GONE);
                }
                mSelectedComplainList = (List<ReasonData>) object;
                loadChiefComplainNodeForSelectedNames(mSelectedComplainList);
                mStep3ProgressBar.setProgress(60);
                setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(mCommonVisitData, isEditMode, mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).
                        commit();
                break;
            case FROM_SUMMARY_RESUME_BACK_FOR_EDIT:
                mSummaryFrameLayout.setVisibility(View.GONE);
                if (object != null) {
                    int caseNo = (int) object;
                    Timber.tag(TAG).d("Title case no=>%s", caseNo);
                    if (caseNo == STEP_5_PAST_MEDICAL_HISTORY) {
                        showPastMedicalHistoryFragment(isEditMode);
                        setTitle(STEP_5_PAST_MEDICAL_HISTORY);
                    } else if (caseNo == STEP_6_FAMILY_HISTORY) {
                        showFamilyHistoryFragment(isEditMode);
                        setTitle(STEP_6_FAMILY_HISTORY);
                    } else if (caseNo == STEP_4_PHYSICAL_EXAMINATION) {
                        mStep4ProgressBar.setProgress(100);
                        mSummaryFrameLayout.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().
                                replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(mCommonVisitData, isEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                                commit();
                        setTitle(STEP_4_PHYSICAL_EXAMINATION);
                    } else if (caseNo == STEP_3_VISIT_REASON_QUESTION) {
                        setTitle(STEP_3_VISIT_REASON_QUESTION);
                    } else if (caseNo == STEP_3_VISIT_REASON_QUESTION_ASSOCIATE_SYMPTOMS) {
                        // no-op
                    }
                }
                break;
            case STEP_3_VISIT_REASON_QUESTION_SUMMARY:
                if (isSavedVisitReason()) {
                    mStep3ProgressBar.setProgress(100);
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, VisitReasonSummaryFragment.newInstance(mCommonVisitData, insertionWithLocaleJsonString, isEditMode, visitUuid), VISIT_REASON_QUESTION_FRAGMENT).
                            commit();
                }
                break;

            case STEP_4_PHYSICAL_EXAMINATION:
                getSupportFragmentManager().popBackStack();
                mStep4ProgressBar.setProgress(10);
                mSummaryFrameLayout.setVisibility(View.GONE);
                loadPhysicalExam();
                setTitle(nextAction);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(mCommonVisitData, isEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                        commit();
                break;
            case STEP_4_PHYSICAL_SUMMARY_EXAMINATION:
                if (isSavedPhysicalExam()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, PhysicalExamSummaryFragment.newInstance(mCommonVisitData, physicalStringLocale, isEditMode, visitUuid), PHYSICAL_EXAM_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_5_PAST_MEDICAL_HISTORY:
                showPastMedicalHistoryFragment(isEditMode);
                setTitle(nextAction);
                break;

            case STEP_6_FAMILY_HISTORY:
                showFamilyHistoryFragment(isEditMode);
                setTitle(nextAction);
                break;

            case STEP_6_HISTORY_SUMMARY:
                if (isSavedPastHistory()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, MedicalHistorySummaryFragment.newInstance(mCommonVisitData, patientHistoryLocale, familyHistoryLocale, isEditMode, visitUuid), PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_7_VISIT_SUMMARY:
                onFormSubmitted(STEP_7_VISIT_SUMMARY_FINAL, isEditMode, null);
                break;
            case STEP_7_VISIT_SUMMARY_FINAL:
                insertLocalEnFormatQAValues();
                Intent intent1 = new Intent(VisitCreationActivity.this, VisitSummaryActivity_New.class);
                mCommonVisitData.setHasPrescription(false);
                intent1.putExtra("CommonVisitData", mCommonVisitData);
                // ── DEBUG ─────────────────────────────────────────────────────────────
                Log.d("HBA1C_DEBUG", "mHba1cViewModel is null? " + (mHba1cViewModel == null));
                if (mHba1cViewModel != null) {
                    Log.d("HBA1C_DEBUG", "connected (live)? " + mHba1cViewModel.connected().getValue());
                    Log.d("HBA1C_DEBUG", "hba1cReading value = " + mHba1cViewModel.hba1cReading().getValue());
                }
                SharedPreferences prefs = getSharedPreferences("hba1c_prefs", MODE_PRIVATE);
                String savedAddr = prefs.getString(PREF_BLE_ADDRESS, null);
                Log.d("HBA1C_DEBUG", "saved BLE address = " + savedAddr);
                Log.d("HBA1C_DEBUG", "mDiagnosticsModel hba1c = " + (mDiagnosticsModel != null ? mDiagnosticsModel.getDiabetesbba1c() : "null"));
// ── END DEBUG ─────────────────────────────────────────────────────────

                String latestHba1c = null;

                if (mHba1cViewModel != null
                        && mHba1cViewModel.hba1cReading().getValue() != null
                        && !mHba1cViewModel.hba1cReading().getValue().isEmpty()) {
                    latestHba1c = mHba1cViewModel.hba1cReading().getValue();
                    Log.d("HBA1C_DEBUG", "source: ViewModel LiveData → " + latestHba1c);
                } else if (mDiagnosticsModel != null
                        && mDiagnosticsModel.getDiabetesbba1c() != null
                        && !mDiagnosticsModel.getDiabetesbba1c().isEmpty()) {
                    latestHba1c = mDiagnosticsModel.getDiabetesbba1c();
                    Log.d("HBA1C_DEBUG", "source: DiagnosticsModel → " + latestHba1c);
                }

                Log.d("HBA1C_DEBUG", "latestHba1c final = " + latestHba1c);
                if (latestHba1c != null && !latestHba1c.isEmpty()) {
                    intent1.putExtra("hba1c_live_value", latestHba1c);
                    Log.d("HBA1C_DEBUG", "✅ Extra added to intent: " + latestHba1c);
                } else {
                    Log.d("HBA1C_DEBUG", "❌ No HbA1c value available from any source");
                }
                startActivity(intent1);
                finish();
                break;
            case STEP_8_DIGITAL_SETHA_SCOPE:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, ConnectPocDeviceFragment.newInstance(mCommonVisitData, isEditMode, mVitalsObject), DIGITAL_SETHSCOPE_FRAGMENT).
                        commit();
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
            case STEP_12_DEVICE_LIST:
                mSummaryFrameLayout.setVisibility(View.VISIBLE);
                mStep1ProgressBar.setProgress(100);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_summary, PocDeviceListFragment.newInstance(isEditMode, mVitalsObject), POC_DEVICELIST_FRAGMENT).
                        commit();
                break;
            case SELECT_HEART:
                mSummaryFrameLayout.setVisibility(View.VISIBLE);
                mStep1ProgressBar.setProgress(100);
                break;
            case SELECT_LUNG:
                mSummaryFrameLayout.setVisibility(View.VISIBLE);
                mStep1ProgressBar.setProgress(100);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_steps_summary,
                                RecordLungSoundsFragment.newInstance(mCommonVisitData, isEditMode, mVitalsObject, visitUuid),
                                POC_DEVICELIST_FRAGMENT)
                        .addToBackStack("LUNG")
                        .commit();
                break;
        }
    }

    private void showPastMedicalHistoryFragment(boolean isEditMode) {
        mStep5ProgressBar.setProgress(10);
        mSummaryFrameLayout.setVisibility(View.GONE);

        if (mPastMedicalHistoryNode == null) {
            mPastMedicalHistoryNode = loadPastMedicalHistory();
            isEditMode = false;
        }
        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, PastMedicalHistoryFragment.newInstance(mCommonVisitData, isEditMode, mPastMedicalHistoryNode), PAST_MEDICAL_HISTORY_FRAGMENT).
                commit();
        setTitle(STEP_5_PAST_MEDICAL_HISTORY);
    }

    private void showFamilyHistoryFragment(boolean isEditMode) {
        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
            mStep3ProgressBar.setProgress(100);
        }
        mStep5ProgressBar.setProgress(50);
        mSummaryFrameLayout.setVisibility(View.GONE);
        if (mFamilyHistoryNode == null) {
            mFamilyHistoryNode = loadFamilyHistory();
            isEditMode = false;
        }

        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, FamilyHistoryFragment.newInstance(mCommonVisitData, isEditMode, mFamilyHistoryNode), FAMILY_HISTORY_SUMMARY_FRAGMENT).
                commit();
        setTitle(STEP_6_FAMILY_HISTORY);
    }

    private boolean isSavedPastHistory() {
        return savePastHistoryData();
    }

    private boolean isSavedPhysicalExam() {
        return savePhysicalExamData();
    }

    private boolean isSavedVisitReason() {
        sessionManager.setVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid, new Gson().toJson(mSelectedComplainList));
        sessionManager.setVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid, new Gson().toJson(mChiefComplainRootNodeList));
        insertion = "";
        insertionLocale = "";
        insertionLocaleEn = "";
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderEn = new StringBuilder();
        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            Node node = mChiefComplainRootNodeList.get(i);
            CustomLog.v(TAG, "mChiefComplainRootNodeList- " + node.findDisplay());
            boolean isAssociateSymptomsType = node.getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS);
            String val = formatComplainRecord(node, isAssociateSymptomsType);
            CustomLog.v(TAG, "val- " + val);
            String answerInLocale = bullet_arrow + node.findDisplay() + "::" + node.formQuestionAnswer(0, isAssociateSymptomsType);
            CustomLog.v(TAG, "answerInLocale- " + answerInLocale);
            String answerInLocaleEn = bullet_arrow + node.findDisplay("en") + "::" + node.formQuestionAnswer(0, isAssociateSymptomsType, "en");
            CustomLog.v(TAG, "answerInLocaleEn " + answerInLocaleEn);

            stringBuilder.append(answerInLocale);
            stringBuilderEn.append(answerInLocaleEn);
            if (val == null) {
                return false;
            }
        }
        insertionLocale = stringBuilder.toString();
        insertionLocaleEn = stringBuilderEn.toString();

        if (insertion.contains("<br/> ►<b>" + Node.ASSOCIATE_SYMPTOMS + "</b>: <br/>►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>")) {
            insertion = insertion.replace("<br/> ►<b>" + Node.ASSOCIATE_SYMPTOMS + "</b>: <br/>►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>", "<br/>►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>");
        }
        JSONObject jsonObject = new JSONObject();
        try {
            insertionLocale = VisitUtils.replaceEnglishCommonString(insertionLocale, sessionManager.getAppLanguage());
            insertionLocaleEn = VisitUtils.replaceEnglishCommonString(insertionLocaleEn, "en");
            String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(insertionLocale);
            if (matchDate != null) {
                for (String date : matchDate) {
                    insertionLocale = insertionLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }
            insertion = VisitUtils.replaceToEnglishCommonString(insertion, sessionManager.getAppLanguage());
            jsonObject.put("en", insertion);
            jsonObject.put("l-" + sessionManager.getAppLanguage(), insertionLocale);
            insertionWithLocaleJsonString = jsonObject.toString().replace("\\/", "/");
            CustomLog.v(TAG, insertionWithLocaleJsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return insertChiefComplainToDb(insertionWithLocaleJsonString);
    }

    private Node mPhysicalExamNode;
    private String mLastChiefComplainPhysicalString = "";

    private List<Node> loadPhysicalExam() {
        ArrayList<String> physicalExams = new ArrayList<>();
        ArrayList<String> childNodeSelectedPhysicalExams = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getPhysicalExamList();
        if (!childNodeSelectedPhysicalExams.isEmpty())
            physicalExams.addAll(childNodeSelectedPhysicalExams);

        ArrayList<String> rootNodePhysicalExams = parseExams(mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex));
        if (rootNodePhysicalExams != null && !rootNodePhysicalExams.isEmpty())
            physicalExams.addAll(rootNodePhysicalExams);
        Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
        mLastChiefComplainPhysicalString = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getPhysicalExams();
        String[] exm = mLastChiefComplainPhysicalString.split(";");
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        for (String s : exm) {
            if (s.contains(":") && s.split(":").length >= 2) {
                String rootNodeName = s.split(":")[0];
                String childNodeName = s.split(":")[1];

                List<String> list = new ArrayList<>();
                if (map.containsKey(rootNodeName)) {
                    list = map.get(rootNodeName);
                }
                list.add(childNodeName);
                map.put(rootNodeName, list);
            }
        }
        String fileLocation = "physExam.json";
        Node filterNode = loadFileToNode(fileLocation, true);
        ArrayList<String> selectedExamsList = new ArrayList<>(selectedExams);
        CustomLog.v(TAG, "selectedExamsList- " + new Gson().toJson(selectedExamsList));
        physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, fileLocation), selectedExamsList);
        physicalExamMap.refreshOnlyLocaleTitle();
        physicalExamMap.setEngineVersion(filterNode.getEngineVersion());
        List<Node> optionsList = new ArrayList<>();
        for (int i = 0; i < filterNode.getOptionsList().size(); i++) {
            if (map.containsKey(filterNode.getOptionsList().get(i).getText()) && filterNode.getOptionsList().get(i).getOptionsList() != null) {
                for (int j = 0; j < filterNode.getOptionsList().get(i).getOptionsList().size(); j++) {
                    Node innerNode = filterNode.getOptionsList().get(i).getOptionsList().get(j);
                    if (innerNode.getOptionsList() != null && !innerNode.getOptionsList().isEmpty()) {
                        optionsList.add(innerNode.getOptionsList().get(0));
                    }
                }
            }
        }
        filterNode.setOptionsList(optionsList);
        return physicalExamMap.getSelectedNodes();
    }

    private Node mPastMedicalHistoryNode;

    private Node loadPastMedicalHistory() {
        String fileLocation = "patHist.json";
        return loadFileToNode(fileLocation, true);
    }

    private Node mFamilyHistoryNode;

    private Node loadFamilyHistory() {
        String fileLocation = "famHist.json";
        return loadFileToNode(fileLocation, true);
    }

    private Node loadFileToNode(String fileName, boolean isForRootFile) {
        JSONObject currentFile = null;
        if (!sessionManager.getLicenseKey().isEmpty()) {
            if (isForRootFile) {
                try {
                    currentFile = new JSONObject(Objects.requireNonNull(FileUtils.readFileRoot(fileName, this)));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else
                currentFile = FileUtils.encodeJSONFromFile(this, fileName);
        } else {
            currentFile = FileUtils.encodeJSON(this, fileName);
        }

        Node mainNode = new Node(currentFile);
        mainNode.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
        return mainNode;
    }

    private Node mCommonAssociateSymptoms = null;

    private void loadChiefComplainNodeForSelectedNames(List<ReasonData> selectedComplains) {
        for (int i = 0; i < selectedComplains.size(); i++) {
            String fileName = selectedComplains.get(i).getReasonName() + ".json";
            if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                fileName = selectedComplains.get(i).getDefaultReasonName() + ".json";
            }
            String fileLocation = "engines/" + fileName;
            JSONObject currentFile = null;

            if (!sessionManager.getLicenseKey().isEmpty()) {
                currentFile = FileUtils.encodeJSONFromFile(this, fileName);
            } else {
                currentFile = FileUtils.encodeJSON(this, fileLocation);
            }

            Node mainNode = new Node(currentFile);
            if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                mainNode.setDisplay(selectedComplains.get(i).getReasonName());
                mainNode.setText(selectedComplains.get(i).getReasonName());
                mainNode.setCompareDuplicateNode(selectedComplains.get(i).getReasonName());
            }
            List<Node> optionList = new ArrayList<>();
            CustomLog.v(TAG, "optionList  mainNode- " + mainNode.getText());
            for (int j = 0; j < mainNode.getOptionsList().size(); j++) {
                if (mainNode.getOptionsList().get(j).getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS)) {
                    if (mCommonAssociateSymptoms == null)
                        mCommonAssociateSymptoms = mainNode.getOptionsList().get(j);
                    else {
                        mCommonAssociateSymptoms.getOptionsList().addAll(mainNode.getOptionsList().get(j).getOptionsList());
                    }
                } else {
                    if (VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, mainNode.getOptionsList().get(j).getGender(), mainNode.getOptionsList().get(j).getMin_age(), mainNode.getOptionsList().get(j).getMax_age())) {
                        if (mainNode.getOptionsList().get(j).getOptionsList() != null)
                            mainNode.getOptionsList().get(j).getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
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
        Set<Node> nodeSet = new TreeSet<Node>(new NodeComparator());
        nodeSet.addAll(nodes);
        return new ArrayList<Node>(nodeSet);
    }

    static class NodeComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            return n1.getText().compareToIgnoreCase(n2.getText());
        }
    }

    @Override
    public void onProgress(int progress) {
        switch (mCurrentStep) {
            case STEP_3_VISIT_REASON_QUESTION:
                mStep3ProgressBar.setProgress(mStep3ProgressBar.getProgress() + progress);
                break;
            case STEP_4_PHYSICAL_EXAMINATION:
                mStep4ProgressBar.setProgress(mStep3ProgressBar.getProgress() + progress);
                break;
        }
    }

    @Override
    public void onTitleChange(String title) {
        Timber.tag(TAG).d("onTitleChange=>%s", mCurrentStep);
    }

    @Override
    public void onManualClose() {
        switch (mCurrentStep) {
            case STEP_1_VITAL_SUMMARY:
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onCameraOpenRequest() {
        openCamera();
    }

    @Override
    public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
        deleteImageFromDatabase(nodeIndex, imageIndex, image);
    }

    @Override
    public void onAyuDeviceRequest(Node node) {
    }

    boolean nodeComplete = false;

    public void filterNodeQuestions() {
    }

    String insertion = "";
    String insertionLocale = "";
    String insertionLocaleEn = "";
    String insertionWithLocaleJsonString = "";

    public String formatComplainRecord(Node currentNode, boolean isAssociateSymptom) {
        AnswerResult answerResult = isAssociateSymptom ? currentNode.checkAllRequiredAnsweredRootNode(this) : currentNode.checkAllRequiredAnswered(this);
        if (!answerResult.result) {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), answerResult.requiredStrings, true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
                @Override
                public void onDialogActionDone(int action) {
                }
            });
            CustomLog.v(TAG, answerResult.requiredStrings);
            return null;
        }

        String complaintString = isAssociateSymptom ? currentNode.generateLanguageSingleNode() : currentNode.generateLanguage();

        CustomLog.v("formatComplainRecord", "Value - " + complaintString);
        if (complaintString != null && !complaintString.isEmpty()) {
            String complaint = currentNode.getText();
            insertion = insertion.concat(bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
        } else {
            String complaint = currentNode.getText();
            if (!complaint.equalsIgnoreCase(getResources().getString(R.string.associated_symptoms))) {
                insertion = insertion.concat(bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
            }
        }
        CustomLog.v("formatComplainRecord", "Value - " + insertion);
        return insertion;
    }

    private void showNextComplainQueries() {
        mCurrentComplainNodeIndex++;
        mStep2ProgressBar.setProgress(mStep2ProgressBar.getProgress() + 10);
    }

    private boolean insertChiefComplainToDb(String value) {
        boolean isInserted = false;
        try {
            CustomLog.i(TAG, "insertChiefComplainToDb: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
            CustomLog.i(TAG, "insertChiefComplainToDb: " + value);
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT);
            CustomLog.i(TAG, "insertChiefComplainToDb: uuidOBS - " + uuidOBS);
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue1(value));
            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                CustomLog.v("obsDTO update", new Gson().toJson(obsDTO));
                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                CustomLog.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    private void updateDatabase(String string) {
        CustomLog.i(TAG, "updateDatabase: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT));
            obsDAO.updateObs(obsDTO);
        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getPhysicalExams();
        if (rawExams != null) {
            String[] splitExams = rawExams.split(";");
            examList.addAll(Arrays.asList(splitExams));
            return examList;
        }
        return null;
    }

    public static void openCamera(Activity activity, String imagePath, String imageName) {
        CustomLog.d(TAG, "open Camera!");
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
            }
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        activity.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
            }
        }
    }

    private boolean insertDbPhysicalExam(String value) {
        CustomLog.i(TAG, "insertDb: ");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PHYSICAL_EXAMINATION);
            CustomLog.i(TAG, "insertDbPhysicalExam: uuidOBS - " + uuidOBS);

            obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue(value));

            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                CustomLog.v("obsDTO update", new Gson().toJson(obsDTO));
                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                CustomLog.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    String physicalString;
    String physicalStringLocale = "";
    String physicalStringLocaleEn = "";
    String physicalStringWithLocaleJsonString = "";
    Boolean complaintConfirmed = false;
    PhysicalExam physicalExamMap;

    private boolean savePhysicalExamData() {
        CustomLog.v(TAG, "savePhysicalExamData");
        sessionManager.setVisitEditCache(SessionManager.PHY_EXAM + visitUuid, new Gson().toJson(physicalExamMap));
        complaintConfirmed = physicalExamMap.areRequiredAnswered();

        if (complaintConfirmed) {
            physicalString = physicalExamMap.generateFindings();
            physicalStringLocale = physicalExamMap.generateFindingsByLocale(sessionManager.getAppLanguage());
            physicalStringLocaleEn = physicalExamMap.generateFindingsByLocale("en");

            CustomLog.v(TAG, "physicalStringLocale -" + physicalStringLocale);
            CustomLog.v(TAG, "physicalStringLocaleEn" + physicalStringLocaleEn);
            while (physicalString.contains("[Describe"))
                physicalString = physicalString.replace("[Describe]", "");

            for (int i = 0; i < physicalExamMap.getTotalNumberOfExams(); i++) {
                Node l1Node = physicalExamMap.getExamNode(i);
                CustomLog.v(TAG, "savePhysicalExamData, l1Node " + new Gson().toJson(l1Node));
                for (int j = 0; j < l1Node.getOptionsList().size(); j++) {
                    Node l2Node = l1Node.getOptionsList().get(j);
                    CustomLog.v(TAG, "savePhysicalExamData, l2Node " + new Gson().toJson(l2Node));
                    List<String> imagePathList = l2Node.getImagePathList();
                    CustomLog.v(TAG, "savePhysicalExamData, imagePathList " + imagePathList);
                    if (imagePathList != null && imagePathList.size() > 0) {
                        if (l2Node.isImageUploaded()) {
                            for (String imagePath : imagePathList) {
                                String comments = l2Node.getImagePathListWithSectionTag().get(imagePath);
                                String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1).split("\\.")[0];
                                updateImageDatabase(fileName, comments);
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.image_upload_pending_alert), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
            }

            JSONObject jsonObject = new JSONObject();
            try {
                physicalStringLocale = VisitUtils.replaceEnglishCommonString(physicalStringLocale, sessionManager.getAppLanguage());
                physicalStringLocaleEn = VisitUtils.replaceEnglishCommonString(physicalStringLocaleEn, "en");
                if (physicalStringLocale != null && !sessionManager.getAppLanguage().equals("en")) {
                    physicalStringLocale = physicalStringLocale.replaceAll("picture taken", getString(R.string.picture_taken));
                }
                String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(physicalStringLocale);
                if (matchDate != null) {
                    for (String date : matchDate) {
                        physicalStringLocale = physicalStringLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                    }
                }
                physicalString = VisitUtils.replaceToEnglishCommonString(physicalString, sessionManager.getAppLanguage());
                jsonObject.put("en", physicalString);
                jsonObject.put("l-" + sessionManager.getAppLanguage(), physicalStringLocale);
                physicalStringWithLocaleJsonString = jsonObject.toString().replace("\\/", "/");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            questionsMissing();
            return false;
        }
        return insertDbPhysicalExam(physicalStringWithLocaleJsonString);
    }

    private String patientHistory, familyHistory;
    String patientHistoryLocale = "", familyHistoryLocale = "";
    String patientHistoryLocaleEn = "", familyHistoryLocaleEn = "";
    String patientHistoryWithLocaleJsonString = "", familyHistoryWithLocaleJsonString = "";

    private boolean savePastHistoryData() {
        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
            return saveOnlyFamilyHistory();
        }
        sessionManager.setVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid, new Gson().toJson(mPastMedicalHistoryNode));
        sessionManager.setVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid, new Gson().toJson(mFamilyHistoryNode));
        patientHistory = mPastMedicalHistoryNode.generateLanguage();
        patientHistoryLocale = mPastMedicalHistoryNode.formQuestionAnswer(0, false);
        patientHistoryLocaleEn = mPastMedicalHistoryNode.formQuestionAnswer(0, false, "en");
        while (patientHistory != null && patientHistory.contains("[Describe"))
            patientHistory = patientHistory.replace("[Describe]", "");

        familyHistory = generateFamilyHistoryAns(false, "en");
        CustomLog.v(TAG, "familyHistory - " + familyHistory);
        if (familyHistory == null || familyHistory.trim().isEmpty()) {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(VisitCreationActivity.this,
                    0,
                    getString(R.string.alert_label_txt),
                    getString(R.string.you_missed_the_compulsory_questions_please_answer_them),
                    true,
                    getResources().getString(R.string.generic_ok),
                    getResources().getString(R.string.cancel),
                    action -> {
                    });

            return false;
        }
        familyHistoryLocale = generateFamilyHistoryAns(true, sessionManager.getAppLanguage());
        familyHistoryLocaleEn = generateFamilyHistoryAns(true, "en");

        familyHistory = familyHistory.replaceAll("null.", "");

        while (familyHistory.contains("[Describe"))
            familyHistory = familyHistory.replace("[Describe]", "");
        List<String> imagePathList = mFamilyHistoryNode.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                String comments = mFamilyHistoryNode.getImagePathListWithSectionTag().get(imagePath);
                updateImageDatabase(imagePath, comments);
            }
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        try {
            patientHistoryLocale = VisitUtils.replaceEnglishCommonString(patientHistoryLocale, sessionManager.getAppLanguage());
            patientHistoryLocaleEn = VisitUtils.replaceEnglishCommonString(patientHistoryLocaleEn, "en");

            String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(patientHistoryLocale);
            if (matchDate != null) {
                for (String date : matchDate) {
                    patientHistoryLocale = patientHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }

            patientHistory = VisitUtils.replaceToEnglishCommonString(patientHistory, sessionManager.getAppLanguage());
            jsonObject.put("en", patientHistory);
            jsonObject.put("l-" + sessionManager.getAppLanguage(), patientHistoryLocale);
            patientHistoryWithLocaleJsonString = jsonObject.toString().replace("\\/", "/");

            familyHistoryLocale = VisitUtils.replaceEnglishCommonString(familyHistoryLocale, sessionManager.getAppLanguage());
            familyHistoryLocaleEn = VisitUtils.replaceEnglishCommonString(familyHistoryLocaleEn, "en");

            String[] matchDate1 = DateAndTimeUtils.findDateFromStringDDMMMYYY(familyHistoryLocale);
            if (matchDate1 != null) {
                for (String date : matchDate1) {
                    familyHistoryLocale = familyHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }

            familyHistory = VisitUtils.replaceToEnglishCommonString(familyHistory, sessionManager.getAppLanguage());
            jsonObject1.put("en", familyHistory);
            jsonObject1.put("l-" + sessionManager.getAppLanguage(), familyHistoryLocale);
            familyHistoryWithLocaleJsonString = jsonObject1.toString().replace("\\/", "/");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return insertDbPastHistory(patientHistoryWithLocaleJsonString, familyHistoryWithLocaleJsonString);
    }

    private boolean saveOnlyFamilyHistory() {
        sessionManager.setVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid, new Gson().toJson(mFamilyHistoryNode));

        familyHistory = generateFamilyHistoryAns(false, "en");
        CustomLog.v(TAG, "familyHistory - " + familyHistory);
        if (familyHistory == null || familyHistory.trim().isEmpty()) {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(VisitCreationActivity.this,
                    0,
                    getString(R.string.alert_label_txt),
                    getString(R.string.you_missed_the_compulsory_questions_please_answer_them),
                    true,
                    getResources().getString(R.string.generic_ok),
                    getResources().getString(R.string.cancel),
                    action -> {
                    });

            return false;
        }
        familyHistoryLocale = generateFamilyHistoryAns(true, sessionManager.getAppLanguage());
        familyHistoryLocaleEn = generateFamilyHistoryAns(true, "en");

        familyHistory = familyHistory.replaceAll("null.", "");

        while (familyHistory.contains("[Describe"))
            familyHistory = familyHistory.replace("[Describe]", "");
        List<String> imagePathList = mFamilyHistoryNode.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                String comments = mFamilyHistoryNode.getImagePathListWithSectionTag().get(imagePath);
                updateImageDatabase(imagePath, comments);
            }
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        try {
            familyHistoryLocale = VisitUtils.replaceEnglishCommonString(familyHistoryLocale, sessionManager.getAppLanguage());
            familyHistoryLocaleEn = VisitUtils.replaceEnglishCommonString(familyHistoryLocaleEn, "en");

            String[] matchDate1 = DateAndTimeUtils.findDateFromStringDDMMMYYY(familyHistoryLocale);
            if (matchDate1 != null) {
                for (String date : matchDate1) {
                    familyHistoryLocale = familyHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }

            familyHistory = VisitUtils.replaceToEnglishCommonString(familyHistory, sessionManager.getAppLanguage());
            jsonObject1.put("en", familyHistory);
            jsonObject1.put("l-" + sessionManager.getAppLanguage(), familyHistoryLocale);
            familyHistoryWithLocaleJsonString = jsonObject1.toString().replace("\\/", "/");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return insertDbPastHistory(null, familyHistoryWithLocaleJsonString);
    }

    private String generateFamilyHistoryAns(boolean isLocale, String locale) {
        String familyHistory = "";
        ArrayList<String> familyInsertionList = new ArrayList<>();
        for (Node node : mFamilyHistoryNode.getOptionsList()) {
            if (!node.checkIsAnswered()) return null;
        }
        if (mFamilyHistoryNode.anySubSelected()) {
            for (Node node : mFamilyHistoryNode.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = !isLocale ? node.generateLanguage() : node.formQuestionAnswer(0, false, locale);
                    String toInsert = (!isLocale ? node.getText() : node.findDisplay(locale)) + " : " + familyString;
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (org.apache.commons.lang3.StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + ".<br/>";
                    familyInsertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < familyInsertionList.size(); i++) {
            if (i == 0) {
                familyHistory = familyInsertionList.get(i);
            } else {
                familyHistory = familyHistory + " " + Node.bullet + familyInsertionList.get(i);
            }
        }
        return familyHistory;
    }

    private boolean insertDbPastHistory(String patientHistory, String familyHistory) {
        CustomLog.i(TAG, "insertDb: ");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();

            if (patientHistory != null) {
                String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                CustomLog.i(TAG, "insertDbPastHistory patientHistory : uuidOBS - " + uuidOBS);

                obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                obsDTO.setEncounteruuid(encounterAdultIntials);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(StringUtils.getValue(patientHistory));

                if (uuidOBS != null) {
                    obsDTO.setUuid(uuidOBS);
                    isInserted = obsDAO.updateObs(obsDTO);
                } else {
                    isInserted = obsDAO.insertObs(obsDTO);
                }
            }

            if (familyHistory != null) {
                String uuidOBS1 = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                CustomLog.i(TAG, "insertDbPastHistory familyHistory : uuidOBS - " + uuidOBS1);
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                obsDTO.setEncounteruuid(encounterAdultIntials);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(familyHistory));

                if (uuidOBS1 != null) {
                    obsDTO.setUuid(uuidOBS1);
                    isInserted = obsDAO.updateObs(obsDTO);
                } else {
                    isInserted = obsDAO.insertObs(obsDTO);
                }
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    private boolean insertLocalEnFormatQAValues() {
        CustomLog.i(TAG, "insertLocalEnFormatQAValues");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            String insertDbEnValue = "Visit Reason (Chief Complaint)\n" + insertionLocaleEn + "\n" + "Physical Examination:\n" + physicalStringLocaleEn + "\n" + "Patient Medical History:\n" + patientHistoryLocaleEn + "\n" + "Family History:\n" + familyHistoryLocaleEn;

            String uuidOBS1 = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.AI_VISIT_SUMMARY_CONCEPT_UUID);
            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.AI_VISIT_SUMMARY_CONCEPT_UUID);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(insertDbEnValue));

            if (uuidOBS1 != null) {
                obsDTO.setUuid(uuidOBS1);
                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                isInserted = obsDAO.insertObs(obsDTO);
            }

        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    public void questionsMissing() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), getResources().getString(R.string.question_answer_all_phy_exam), true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {
            }
        });
    }

    private void updateImageDatabase(String imageName, String comments) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE, comments);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void deleteImageFromDatabase(int nodeIndex, int imageIndex, String imageName) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            String obsUUID = imageName.substring(imageName.lastIndexOf("/") + 1).split("\\.")[0];
            imagesDAO.deleteImageFromDatabase(obsUUID);
            imageUtilsListener.onImageReadyForDelete(nodeIndex, imageIndex, imageName);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    ActivityResultLauncher<Intent> mStartForCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String mCurrentPhotoPath = data.getStringExtra("RESULT");

                        Bundle bundle = new Bundle();
                        bundle.putString("image", mCurrentPhotoPath);
                        imageUtilsListener.onImageReady(bundle);
                        CustomLog.i(TAG, mCurrentPhotoPath);
                    }
                }
            });
    ActivityResultLauncher<Intent> mStartForGalleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String currentPhotoPath = "";
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            String[] filePath = {MediaStore.Images.Media.DATA};
                            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                            c.moveToFirst();
                            int columnIndex = c.getColumnIndex(filePath[0]);
                            String picturePath = c.getString(columnIndex);
                            c.close();
                            CustomLog.v("path", picturePath + "");

                            mLastSelectedImageName = UUID.randomUUID().toString();
                            currentPhotoPath = AppConstants.IMAGE_PATH + mLastSelectedImageName + ".jpg";

                            File file = new File(currentPhotoPath);
                            long fileSizeInBytes = file.length();
                            long fileSizeInKB = fileSizeInBytes / 1024;
                            long fileSizeInMB = fileSizeInKB / 1024;
                            Log.d("TAG", "onActivityResult: " + fileSizeInMB + " " + fileSizeInKB);
                            if (fileSizeInMB > 2) {
                                String compressedPath = AppConstants.IMAGE_PATH + mLastSelectedImageName + "_compressed.jpg";
                                compressImage(currentPhotoPath, compressedPath);
                                currentPhotoPath = compressedPath;
                            }
                            BitmapUtils.copyFile(picturePath, currentPhotoPath);

                            Bundle bundle = new Bundle();
                            bundle.putString("image", currentPhotoPath);
                            imageUtilsListener.onImageReady(bundle);

                            CustomLog.i(TAG, currentPhotoPath);
                        } else {
                            Toast.makeText(VisitCreationActivity.this, getResources().getString(R.string.unable_to_pick_data), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private void compressImage(String inputPath, String outputPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(inputPath, options);

        try {
            FileOutputStream out = new FileOutputStream(outputPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mLastSelectedImageName = "";

    public void openCamera() {
        validatePermissionAndIntent();
    }

    private void cameraStart() {
        File file = new File(AppConstants.IMAGE_PATH);
        final String imagePath = file.getAbsolutePath();
        final String imageName = UUID.randomUUID().toString();
        mLastSelectedImageName = imageName;
        Intent cameraIntent = new Intent(VisitCreationActivity.this, CameraActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
        }
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        mStartForCameraResult.launch(cameraIntent);
    }

    private void galleryStart() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mStartForGalleryResult.launch(intent);
    }

    private static final int MY_CAMERA_REQUEST_CODE = 1001;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private AlertDialog mImagePickerAlertDialog;

    private void selectImage() {
        if (mImagePickerAlertDialog != null && mImagePickerAlertDialog.isShowing()) {
            mImagePickerAlertDialog.dismiss();
        }
        mImagePickerAlertDialog = DialogUtils.showCommonImagePickerDialog(this, getString(R.string.add_image_by), new DialogUtils.ImagePickerDialogListener() {
            @Override
            public void onActionDone(int action) {
                mImagePickerAlertDialog.dismiss();
                if (action == DialogUtils.ImagePickerDialogListener.CAMERA) {
                    cameraStart();
                } else if (action == DialogUtils.ImagePickerDialogListener.GALLERY) {
                    galleryStart();
                }
            }
        });
    }

    private void validatePermissionAndIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    ImageUtilsListener imageUtilsListener;

    public void setImageUtilsListener(ImageUtilsListener imageUtilsListener) {
        this.imageUtilsListener = imageUtilsListener;
    }

    private ObjectAnimator syncAnimator;

    public void syncNow(View view) {
        if (mIsEditTriggerFromVisitSummary) {
            if (NetworkConnection.isOnline(this)) {
                SyncUtils.syncNow(this, view, syncAnimator);
            }
        } else {
            showConfirmationDialog(getString(R.string.confirm_discard_changes_content_on_sync));
        }
    }

    public void showInfo(View view) {
    }

    public interface ImageUtilsListener {
        void onImageReady(Bundle bundle);

        void onImageReadyForDelete(int nodeIndex, int imageIndex, String imageName);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    public FeatureActiveStatus getFeatureActiveStatus() {
        return featureActiveStatus;
    }

    public void setTitle(int screenId) {
        Timber.tag(TAG).d("setTitle=>%s", screenId);

        int currentScreenIndex = 1;
        String title = "";

        boolean isVitalEnabled = featureActiveStatus.getVitalSection();
        boolean isDiagnosticsEnabled = featureActiveStatus.getActiveStatusDiagnosticsSection();

        int vitalScreenIndex = isVitalEnabled ? 1 : 0;
        int diagnosticsScreenIndex = isDiagnosticsEnabled ? vitalScreenIndex + 1 : vitalScreenIndex;
        int visitReasonScreenIndex = Math.max(vitalScreenIndex, diagnosticsScreenIndex) + 1;

        int adjustedTotalScreen = 5;
        if (!isVitalEnabled) adjustedTotalScreen--;
        if (!isDiagnosticsEnabled) adjustedTotalScreen--;

        switch (screenId) {
            case STEP_1_VITAL:
                if (isVitalEnabled) {
                    currentScreenIndex = vitalScreenIndex;
                    title = getString(R.string._1_4_vitals, currentScreenIndex, adjustedTotalScreen);
                }
                break;

            case STEP_2_DIAGNOSTICS:
                if (isDiagnosticsEnabled) {
                    currentScreenIndex = diagnosticsScreenIndex;
                    title = getString(R.string.diagnostics_section, currentScreenIndex, adjustedTotalScreen);
                }
                break;

            case STEP_3_VISIT_REASON:
                currentScreenIndex = visitReasonScreenIndex;
                title = getString(R.string.visit_reason, currentScreenIndex, adjustedTotalScreen);
                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    title = getString(R.string.visit_reason, currentScreenIndex, totalScreen);
                }
                break;

            case STEP_3_VISIT_REASON_QUESTION:
                currentScreenIndex = visitReasonScreenIndex;
                StringBuilder builder = new StringBuilder();
                var reasonName = "";
                for (int i = 0; i < mSelectedComplainList.size(); i++) {
                    builder.append(mSelectedComplainList.get(i).getReasonNameLocalized());
                    if (i < mSelectedComplainList.size() - 1) {
                        builder.append(", ");
                    }
                }
                reasonName = builder.toString();
                Log.d(TAG, "setTitle: " + reasonName);
                title = getString(R.string.visit_reason, currentScreenIndex, adjustedTotalScreen)
                        + " : " + reasonName;
                break;
            case STEP_3_VISIT_REASON_QUESTION_SUMMARY:
                currentScreenIndex = visitReasonScreenIndex;
                title = getString(R.string._visit_reason_summary, currentScreenIndex, adjustedTotalScreen)
                        + " : " + mSelectedComplainList.get(0).getReasonNameLocalized();
                break;

            case STEP_4_PHYSICAL_EXAMINATION:
                currentScreenIndex = visitReasonScreenIndex + 1;
                title = getString(R.string._phy_examination, currentScreenIndex, adjustedTotalScreen);
                if (BuildConfig.FLAVOR_client == FlavorKeys.KCDO) {
                    title = getString(R.string._relapse, currentScreenIndex, totalScreen);
                } else if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    title = getString(R.string._obstetric_history, currentScreenIndex, totalScreen);
                }
                break;

            case STEP_5_PAST_MEDICAL_HISTORY:
                currentScreenIndex = visitReasonScreenIndex + 2;
                title = getString(R.string.patinet_history, currentScreenIndex, adjustedTotalScreen);
                break;

            case STEP_6_FAMILY_HISTORY:
                currentScreenIndex = visitReasonScreenIndex + 2;
                title = getString(R.string._medical_family_history, currentScreenIndex, adjustedTotalScreen);
                break;
            default:
                Log.w(TAG, "Unknown screenId: " + screenId);
        }

        ((TextView) findViewById(R.id.tv_sub_title)).setText(title);
    }

    private void showConfirmationDialog(String content) {
        Log.d(TAG, "showConfirmationDialog: visitUuid : " + visitUuid);
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(this, R.drawable.fingerprint_dialog_error, getResources().getString(R.string.confirm_discard_changes_title),
                content, false,
                getResources().getString(R.string.confirm_continue_changes_button_dialog), getResources().getString(R.string.confirm_discard_changes_button_dialog), action -> {
                    if (action == DialogUtils.CustomDialogListener.NEGATIVE_CLICK) {
                        new VisitsDAO().deleteAllDataForOngoingIncompleteVisit(visitUuid);
                        finish();
                    }
                });
    }

    @Override
    public void onDigitalScopeCompleted() {
        onFormSubmitted(STEP_7_VISIT_SUMMARY_FINAL, false, null);
    }

    public boolean isHeartRecorded() {
        return isHeartRecorded;
    }

    public boolean isLungRecorded() {
        return isLungRecorded;
    }

    @Override
    public void onRecordingCompleted(String type) {
        if ("heart".equals(type)) {
            isHeartRecorded = true;
        } else {
            isLungRecorded = true;
        }
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    // ── Communicator interface implementation (BioSense SDK) ───────────────

    @Override
    public void setHbA1cReading(String reading, String date, String time, String srno) {
        Log.d("HBA1C_DEBUG", "setHbA1cReading: " + reading + " at " + time + " (serial " + srno + ")");
        if (mHba1cViewModel != null) {
            mHba1cViewModel.onHba1cReading(reading, time);
        }
    }

    @Override
    public void setHB(String s) {

    }

    @Override
    public void setConnectionStatus(String status, boolean isConnected) {
        Log.d("HBA1C_DEBUG", "setConnectionStatus: " + status + " connected=" + isConnected);
        if (mHba1cViewModel != null) {
            mHba1cViewModel.onConnectionStatus(isConnected);
        }
    }

    @Override
    public void setSwitchActivity() {

    }

    @Override
    public void setBatteryLevel(int i) {

    }

    @Override
    public void setManufacturerName(String s) {

    }

    @Override
    public void setSerialNumber(String s) {

    }

    @Override
    public void setModelNumber(String s) {

    }

    @Override
    public void getOfflineResults(ArrayList<String> arrayList) {

    }

    @Override
    public boolean go(String s) {
        // Fires after the device's 1st button press. setHbA1cReading() will
        // NOT be called yet — the user must press the device button again.
        Log.d("HBA1C_DEBUG", "go(): 1st frame received, awaiting 2nd press — " + s);
        if (mHba1cViewModel != null) {
            mHba1cViewModel.onFirstFrameReceived();
        }
        return false;
    }

    @Override
    public void setBPReading(String systolic, String diastolic, String pulse) {
        // Not used for HbA1c device — no-op.
    }

    @Override
    public void onBpDeviceError() {

    }

    @Override
    public void setGlucoseReading(String text) {
        // Not used for HbA1c device — no-op.
    }

    @Override
    public void testStarted(boolean started) {
        // Fired when the device begins a measurement cycle. No-op is fine —
        // we don't need to react to this for HbA1c, but the interface
        // requires it to be implemented.
        Log.d("HBA1C_DEBUG", "testStarted: " + started);
    }

    @Override
    public void stopNotiFication() {

    }

    @Override
    protected void onDestroy() {
        if (mControlCentre != null) {
            try {
                mControlCentre.stopReceiver();
            } catch (SecurityException e) {
                Log.e("HBA1C_DEBUG", "onDestroy: SecurityException on stopReceiver — " + e.getMessage());
            } catch (Exception ignored) {
            }
            mControlCentre = null;
        }
        super.onDestroy();
    }
}