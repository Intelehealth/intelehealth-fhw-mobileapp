package org.intelehealth.app.app;


import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;


import java.io.File;
import java.util.concurrent.TimeUnit;

import org.intelehealth.app.R;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.syncModule.LastSyncWork;
import org.intelehealth.app.syncModule.VisitSummaryWork;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NotificationUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidGenerator;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.syncModule.SyncWorkManager;

public class AppConstants {
    //Constants
    public static final String DATABASE_NAME = "localrecords.db";
    public static final int DATABASE_VERSION = 5;
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static final String IMAGE_APP_ID = "app2";
    public static final String dbfilepath = Environment.getExternalStorageDirectory() +
            File.separator + "InteleHealth_DB" + File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
    public static final int CAMERA_PERMISSIONS = 897;
    public static String CONFIG_FILE_NAME = "config.json";
    public static final String IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    public static final String MESSAGE_PROGRESS = "message_progress";


    //App vitals constants
    public static final String MAXIMUM_HEIGHT = "272";
    public static final String MAXIMUM_WEIGHT = "150";
    public static int APP_VERSION_CODE = 26;
    public static final String MINIMUM_BP_SYS = "50";
    public static final String MAXIMUM_BP_SYS = "240";
    public static final String MINIMUM_BP_DSYS = "35";
    public static final String MAXIMUM_BP_DSYS = "140";
    public static final String MAXIMUM_PULSE = "200";
    public static final String MINIMUM_PULSE = "30";
    public static final String MAXIMUM_TEMPERATURE_CELSIUS = "43";
    public static final String MINIMUM_TEMPERATURE_CELSIUS = "25";
    public static final String MINIMUM_TEMPERATURE_FARHENIT = "77";
    public static final String MAXIMUM_TEMPERATURE_FARHENIT = "109";

    public static final String MINIMUM_GLUCOSE_NON_FASTING = "60";
    public static final String MAXIMUM_GLUCOSE_NON_FASTING = "600";

    public static final String MINIMUM_GLUCOSE_FASTING = "30";
    public static final String MAXIMUM_GLUCOSE_FASTING = "300";

    public static final String MINIMUM_GLUCOSE_RANDOM = "30";
    public static final String MAXIMUM_GLUCOSE_RANDOM = "500";

    public static final String MINIMUM_GLUCOSE_POST_PRANDIAL = "70";
    public static final String MAXIMUM_GLUCOSE_POST_PRANDIAL = "400";

    public static final String MINIMUM_HEMOGLOBIN = "4";
    public static final String MAXIMUM_HEMOGLOBIN = "18";

    public static final String MAXIMUM_SPO2 = "100";
    public static final String MINIMUM_SPO2 = "1";

    public static final String MAXIMUM_RESPIRATORY = "80";
    public static final String MINIMUM_RESPIRATORY = "10";

    public static final String MINIMUM_URIC_ACID = "2";
    public static final String MAXIMUM_URIC_ACID = "12";

    public static final String MINIMUM_TOTAL_CHOLSTEROL = "100";
    public static final String MAXIMUM_TOTAL_CHOLSTEROL = "350";

    //functions constants
    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().UuidGenerator();
    public static NotificationUtils notificationUtils = new NotificationUtils();


    //  Image Conversion Ratio
    public static int IMAGE_JPG_QUALITY = 70;

    // HealthCube Key
    public static final String key = "VmtaYVUxZHJNVlpPVlZaWFZrWmFUMXBYZEZabFJsSnpWV3RLYTAxRVJrVlVWV2h2VkRKV2MxSlVSbFZXTTBKMVZGUkJNVlpXV2xWU2F6VlRVbFZaZWc9PQ==";


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
    public static final String SYNC_INTENT_DATA_KEY = "SYNC_JOB_TYPE";
    public static final int SYNC_FAILED = 0;
    public static final int SYNC_PULL_DATA_DONE = 1;
    public static final int SYNC_PUSH_DATA_DONE = 2;
    public static final int SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE = 3;
    public static final int SYNC_OBS_IMAGE_PUSH_DONE = 4;

    public static final String FIREBASE_REAL_TIME_DB_BASE_URL = "https://nashik-arogya-sampada-master-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public static final String FIREBASE_REAL_TIME_DB_BASE_REF = "rtc_notify/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE = "device_info/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_TEXT_CHAT_CONN_INFO = "TEXT_CHAT/";

    public static String getFirebaseRTDBUrl() {
        return FIREBASE_REAL_TIME_DB_BASE_URL;

    }

    public static String getFirebaseRTDBRootRef() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl()
                .replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF;
    }

    public static String getFirebaseRTDBRootRefForTextChatConnInfo() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl()
                .replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_TEXT_CHAT_CONN_INFO;

    }

    public static String getFirebaseRTDBRootRefForDeviceInfo() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl()
                .replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE;
    }
}

