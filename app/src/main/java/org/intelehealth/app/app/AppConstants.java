package org.intelehealth.app.app;


import android.content.Context;
import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.syncModule.LastSyncWork;
import org.intelehealth.app.syncModule.SyncWorkManager;
import org.intelehealth.app.syncModule.VisitSummaryWork;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NotificationUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidGenerator;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class AppConstants {
    //Constants
    public static final String DATABASE_NAME = BuildConfig.FLAVOR_client + "-localrecords.db";
    public static final int DATABASE_VERSION = 4;
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static final String IMAGE_APP_ID = "app2";
    public static final String dbfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" + File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
    public static final int FORGOT_USER_NAME_ACTION = 50001;
    public static final int FORGOT_USER_PASSWORD_ACTION = 50002;
    public static String CONFIG_FILE_NAME = "config.json";
    public static final String IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    public static final String MESSAGE_PROGRESS = "message_progress";

    public static final String DOCTOR_NOT_NEEDED = "Specialist doctor not needed";

    public static final int PAGE_LIMIT = 50;

    public static final long FOLLOW_UP_SCHEDULE_ONE_DURATION = 2;
    public static final long FOLLOW_UP_SCHEDULE_TWO_DURATION = 24;

    //App vitals constants

    public static String getMinWeightByAge(int ageInMonth) {
        if (ageInMonth <= 1) {
            return "0.8";
        } else if (ageInMonth <= 12) {
            return "1";
        } else if (ageInMonth <= 12 * 12) {
            return "4";
        } else if (ageInMonth <= 18 * 12) {
            return "15";
        } else {
            return "20";
        }
    }

    public static String getMaxWeightByAge(int ageInMonth) {

        if (ageInMonth <= 1) {
            return "6";
        } else if (ageInMonth <= 12) {
            return "16";
        } else if (ageInMonth <= 12 * 12) {
            return "80";
        } else if (ageInMonth <= 18 * 12) {
            return "150";
        } else {
            return "500";
        }
    }

    public static final String MAXIMUM_HEIGHT = "272";
    public static final String MINIMUM_HEIGHT = "40";

    public static final String MAXIMUM_WEIGHT = "500";
    public static final String MINIMUM_WEIGHT = "1";

    public static int APP_VERSION_CODE = 26;

    public static final String MAXIMUM_BP_SYS = "220";
    public static final String MINIMUM_BP_SYS = "70";

    public static final String MAXIMUM_BP_DSYS = "120";
    public static final String MINIMUM_BP_DSYS = "50";

    public static final String MAXIMUM_PULSE = "200";
    public static final String MINIMUM_PULSE = "30";

    public static final String MAXIMUM_TEMPERATURE_CELSIUS = "43";
    public static final String MINIMUM_TEMPERATURE_CELSIUS = "25";
    public static final String MINIMUM_TEMPERATURE_FARHENIT = "95";
    public static final String MAXIMUM_TEMPERATURE_FARHENIT = "104";

    public static final String MAXIMUM_SPO2 = "100";
    public static final String MINIMUM_SPO2 = "85";

    public static final String MAXIMUM_RESPIRATORY = "30";
    public static final String MINIMUM_RESPIRATORY = "10";

    public static final String MAXIMUM_HAEMOGLOBIN = "17";
    public static final String MINIMUM_HAEMOGLOBIN = "5";

    public static final String MAXIMUM_SUGAR = "500";
    public static final String MINIMUM_SUGAR = "60";

    public static final int RISK_LIMIT_SPO2 = 95;

    public static final int RISK_LIMIT_PULSE_UPPER_200 = 200;
    public static final int RISK_LIMIT_PULSE_LOWER_60 = 60;
    public static final int RISK_LIMIT_PULSE_UPPER_150 = 150;
    public static final int RISK_LIMIT_PULSE_LOWER_58 = 58;
    public static final int RISK_LIMIT_PULSE_UPPER_140 = 140;
    public static final int RISK_LIMIT_PULSE_LOWER_40 = 40;

    public static final int RISK_LIMIT_RESPIRATORY_UPPER = 25;
    public static final int RISK_LIMIT_RESPIRATORY_LOWER = 12;

    public static final double RISK_LIMIT_TEMPERATURE_UPPER_100 = 100.4;
    public static final double RISK_LIMIT_TEMPERATURE_UPPER_103 = 103;
    public static final double RISK_LIMIT_TEMPERATURE_LOWER_95 = 95;

    public static final double RISK_LIMIT_HAEMOGLOBIN_LOWER = 7.0;
    public static final double RISK_LIMIT_HAEMOGLOBIN_UPPER = 20.0;

    public static final int RISK_LIMIT_SUGAR_RANDOM_LOWER = 80;
    public static final int RISK_LIMIT_SUGAR_RANDOM_UPPER = 130;

    public static final int BEFORE_DECIMAL_PLACE_MAX_COUNT = 3;
    public static final int AFTER_DECIMAL_PLACE_MAX_ONE_COUNT = 1;

    //functions constants

    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().UuidGenerator();
    public static NotificationUtils notificationUtils = new NotificationUtils();


    //  Image Conversion Ratio
    public static int IMAGE_JPG_QUALITY = 70;


    public static int REPEAT_INTERVAL = 15;
    public static Constraints MY_CONSTRAINTS = new Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build();

    public static PeriodicWorkRequest PERIODIC_WORK_REQUEST =
            new PeriodicWorkRequest.Builder(SyncWorkManager.class, REPEAT_INTERVAL, TimeUnit.MINUTES)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();


    // Added by Venu to make the Sync Issue Solutions as intele_safe.
    public static OneTimeWorkRequest VISIT_SUMMARY_WORK_REQUEST =
            new OneTimeWorkRequest.Builder(VisitSummaryWork.class)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();

    public static OneTimeWorkRequest LAST_SYNC_WORK_REQUEST =
            new OneTimeWorkRequest.Builder(LastSyncWork.class)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();

    /*@Lincon*/
    public static final String SYNC_INTENT_ACTION = "org.intelehealth.app.LAST_SYNC";
    public static final String SYNC_NOTIFY_INTENT_ACTION = "org.intelehealth.app.SYNC_DONE_REFRESH_ACTION";
    public static final String SYNC_INTENT_DATA_KEY = "SYNC_JOB_TYPE";
    public static final int SYNC_FAILED = 0;
    public static final int SYNC_PULL_DATA_DONE = 1;
    public static final int SYNC_PUSH_DATA_DONE = 2;

    public static final int ALL_SYNC_DONE = 0;

    public static final int SYNC_PUSH_DATA_TO_LOCAL_DB_DONE = 5;
    public static final int SYNC_APPOINTMENT_PULL_DATA_DONE = 22;
    public static final int SYNC_PULL_PUSH_APPOINTMENT_PULL_DATA_DONE = 25;
    public static final int SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE = 3;
    public static final int SYNC_OBS_IMAGE_PUSH_DONE = 4;

    public static final String FIREBASE_REAL_TIME_DB_BASE_URL = "https://intelehealth-3-0-default-rtdb.firebaseio.com/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF = "rtc_notify/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE = "device_info/";


    public static String getFirebaseRTDBUrl() {
        return BuildConfig.REAL_TIME_FB_URL;

    }

    public static String getFirebaseRTDBRootRef() {
        String serverUrl = SessionManager.getInstance(IntelehealthApplication.getAppContext()).getServerUrl();
        return new UrlModifiers().getCleanUrl(serverUrl).replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF;

    }

    public static String getFirebaseRTDBRootRefForDeviceInfo() {
        String serverUrl = SessionManager.getInstance(IntelehealthApplication.getAppContext()).getServerUrl();
        return new UrlModifiers().getCleanUrl(serverUrl).replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE;

    }

    //UI2.0 Constants
    //public static final String DEMO_URL = "uiux.intelehealth.org";
    //public static final String DEMO_URL = "demo2.intelehealth.org";
    //public static final String DEMO_URL = "demo.intelehealth.org";

//    public static final String DEMO_URL = "dev.intelehealth.org";
//    public static final String DEMO_URL = "revamp.intelehealth.org";
    //public static final String DEMO_URL = "testing.intelehealth.org";

    public static final int INTENT_FROM_AYU_FOR_SETUP = 1;
    public static final int INTENT_FROM_HOME_FOR_PATIENT_CREATION = 2;

    public static final int TERMS_CONDITIONS_ACCEPT = 1;
    public static final int TERMS_CONDITIONS_DECLINE = 2;
    public static final int PRIVACY_POLICY_ACCEPT = 3;
    public static final int PRIVACY_POLICY_DECLINE = 4;

    public static final int TELECONSULTATION_CONSENT_ACCEPT = 5;
    public static final int TELECONSULTATION_CONSENT_DECLINE = 6;

    public static final int PERSONAL_CONSENT_ACCEPT = 5;

    public static final int PERSONAL_CONSENT_DECLINE = 6;


    public static final int EVENT_FLAG_START = 0;
    public static final int EVENT_FLAG_SUCCESS = 1;
    public static final int EVENT_FLAG_FAILED = 2;

    // OBS IMAGE TYPE
    public static final String IMAGE_ADDITIONAL_DOC = "ADDITIONAL_DOC";

    public static final int EVENT_APPOINTMENT_BOOKING_FROM_VISIT_SUMMARY = 3992;
    public static final int EVENT_APPOINTMENT_BOOKING_APPOINTMENT_DETAILS = 3993;

    //status of appointment
    public static final String CANCELLED = "cancelled";
    public static final String BOOKED = "booked";

    public static String getAppPlayStoreUrl(Context context) {
        return "https://play.google.com/store/apps/details?id=" + context.getApplicationContext().getPackageName();
    }

    public static String getAppMarketUrl(Context context) {
        return "market://details?id=" + context.getApplicationContext().getPackageName();
    }

    public static final String INTENT_SERVER_URL = "server_url";

    public static final String VILLAGE_TYPE_UUID = "b9d762cb-ea7f-4347-8d8b-f21ca908bf82";
    public static final String DISTANCE_TO_SUB_CENTRE_UUID = "ff2b4105-608a-4ef9-b3b8-f57367f6c68d";
    public static final String DISTANCE_TO_PRIMARY_HEALTHCARE_CENTRE_UUID = "dfe1dd68-c1e8-4a98-892f-152acc87bea4";
    public static final String DISTANCE_TO_NEAREST_COMMUNITY_HEALTHCARE_CENTRE_UUID = "c47db34b-c0ec-4045-9b05-7c6a0a9edcf3";
    public static final String DISTANCE_TO_NEAREST_DISTRICT_HOSPITAL_UUID = "b983240c-16e0-4eea-a878-9caf11fbdc95";
    public static final String DISTANCE_TO_NEAREST_MEDICAL_STORE_UUID = "84f40747-c8ac-4f53-81e8-cd3076bf4a06";
    public static final String DISTANCE_TO_NEAREST_PATHOLOGICAL_LAB_UUID = "143b1dd3-a21e-44bf-82fa-c3843a8dbd46";
    public static final String DISTANCE_TO_NEAREST_PRIVATE_CLINIC_UUID = "e9aa35b4-2c74-414d-b727-f80d217a78b1";
    public static final String DISTANCE_TO_NEAREST_PRIVATE_CLINIC_WITH_ALTERNATIVE_MEDICINE_UUID = "0d7de309-05cf-433a-bb66-2be6d28ab4de";
    public static final String JAL_JEEVAN_YOJANA_UUID = "0edf2076-0b94-45f7-845d-1918ed017ae7";
    public static final String SECONDARY_VILLAGE_UUID = "b9d762cb-ea7f-4347-8d8b-f21ca908bf82";

    public static final String DISTANCE_TO_SUB_CENTRE_UUID_TEXT = "distance_to_sub_centre";
    public static final String DISTANCE_TO_PRIMARY_HEALTHCARE_CENTRE_UUID_TEXT = "distance_to_primary_health_centre";
    public static final String DISTANCE_TO_NEAREST_COMMUNITY_HEALTHCARE_CENTRE_UUID_TEXT = "distance_to_nearest_community_health_centre";
    public static final String DISTANCE_TO_NEAREST_DISTRICT_HOSPITAL_UUID_TEXT = "distance_to_nearest_district_hospital";
    public static final String DISTANCE_TO_NEAREST_MEDICAL_STORE_UUID_TEXT = "distance_to_nearest_medical_store";
    public static final String DISTANCE_TO_NEAREST_PATHOLOGICAL_LAB_UUID_TEXT = "distance_to_nearest_pathological_lab";
    public static final String DISTANCE_TO_NEAREST_PRIVATE_CLINIC_UUID_TEXT = "distance_to_nearest_private_clinic";
    public static final String DISTANCE_TO_NEAREST_PRIVATE_CLINIC_WITH_ALTERNATIVE_MEDICINE_UUID_TEXT = "distance_to_nearest_private_clinic_with_alternate_medicine";
    public static final String JAL_JEEVAN_YOJANA_UUID_TEXT = "does_the_village_get_water_under_jal_jeevan_yojana_scheme";

    public static final String INTENT_IS_DIFFERENT_LOCATION_PRESCRIPTION = "isDifferentLocationPrescriptionReceived";
    public static final String INTENT_PATIENT_ID = "patientId";
    public static final String INTENT_VISIT_UUID = "visitUuid";

    public static final String SECOND_NOTIFICATION_FIRED = "second_notification_fired";

    public static final String PRESCRIPTION_DATA_LIST = "prescriptionDataList";
    public static final String SHARED_ANY_PRESCRIPTION = "sharedAnyPrescription";
    public static final String UNSHARED_PRESCRIPTION_COUNT = "unshared_prescription_count";

    public static final int PRESCRIPTION_SHARED = 999;
    public static final int PRESCRIPTION_NOT_SHARED = 998;


    public static final String INTENT_PRIMARY_VILLAGE = "primary_village";
    public static final String VISIT_TYPE_SEVIKA = "sevika";
    public static final String VISIT_TYPE_DOCTOR = "doctor";
    public static final String COMPLAINT_SEVIKA_VISIT = "Sevika Visit";

    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_HI = "hi";
    public static final String LANGUAGE_BN = "bn";
    public static final String LANGUAGE_GU = "gu";
    public static final String LANGUAGE_MR = "mr";
    public static final String LANGUAGE_KN = "kn";
    public static final String LANGUAGE_OR = "or";
    public static final String LANGUAGE_AS = "as";
    public static final String LANGUAGE_TA = "ta";
    public static final String LANGUAGE_TE = "te";

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DD_MMM_YYYY = "dd MMM, yyyy";
    public static final String DATE_FORMAT_DD_MMM_YYYY_FULL_MONTH = "dd MMMM, yyyy";
    public static final String IS_NCD_VISIT_ATTRIBUTE = "bc79d2ab-3c83-48f2-820d-08a02b32faab";
    public static final String NCD_PROTOCOL_DIRECTORY = "engines/ncd";
    public static final String PROTOCOL_DIRECTORY = "engines";
    public static final String NCD_REPORT_BASE_URL = "https://afitraining.ekalarogya.org/intelehealth/index.html#/r/";
}