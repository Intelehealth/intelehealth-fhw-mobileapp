package org.intelehealth.ezazi.app;


import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.database.InteleHealthDatabaseHelper;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.syncModule.LastSyncWork;
import org.intelehealth.ezazi.syncModule.VisitSummaryWork;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.UuidGenerator;

import java.io.File;

public class AppConstants {
    //Constants
    public static final String DATABASE_NAME = "localrecords.db";
    public static final int DATABASE_VERSION = 4;
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static final String IMAGE_APP_ID = "app2";
    public static final String dbfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" + File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
    public static String CONFIG_FILE_NAME = "config.json";
    public static final String IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    public static final String MESSAGE_PROGRESS = "message_progress";
    public static final String HELP_NUMBER = "+919503692181";


    //App vitals constants
    public static final String MAXIMUM_BP_SYS = "250";
    public static final String MAXIMUM_HEIGHT = "272";
    public static final String MAXIMUM_WEIGHT = "150";
    public static int APP_VERSION_CODE = 26;
    public static final String MINIMUM_BP_SYS = "50";
    public static final String MAXIMUM_BP_DSYS = "150";
    public static final String MINIMUM_BP_DSYS = "30";
    public static final String MAXIMUM_PULSE = "240";
    public static final String MINIMUM_PULSE = "30";
    public static final String MAXIMUM_TEMPERATURE_CELSIUS = "42.8";
    public static final String MINIMUM_TEMPERATURE_CELSIUS = "32";
    public static final String MINIMUM_TEMPERATURE_FARHENIT = "77";
    public static final String MAXIMUM_TEMPERATURE_FARHENIT = "109";
    public static final String MAXIMUM_SPO2 = "100";
    public static final String MINIMUM_SPO2 = "1";
    public static final String MAXIMUM_RESPIRATORY = "80";
    public static final String MINIMUM_RESPIRATORY = "10";

    public static final String MAXIMUM_CONTRACTION_DURATION = "100";
    public static final String MINIMUM_CONTRACTION_DURATION = "0";

    public static final String MAXIMUM_BASELINE_FHR = "220";
    public static final String MINIMUM_BASELINE_FHR = "90";

//    public static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final String UTC_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String VISIT_FORMAT = "dd MMM, yyyy hh:mm a";

    //functions constants 2023-10-12 08:14:52
    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().UuidGenerator();
    public static NotificationUtils notificationUtils = new NotificationUtils();
    public static final long INTERVAL_TWENTY_FIVE_MINUTES = 1500000L;

    //App url
    public static final String APP_URL = "ezazi.intelehealth.org";
    public static final String PROTOCOL = "https://";

    public static final String PORT_NUMBER = "3004";

    //  Image Conversion Ratio
    public static int IMAGE_JPG_QUALITY = 70;


    public static int REPEAT_INTERVAL = 15;
    public static Constraints MY_CONSTRAINTS = new Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build();

//    public static PeriodicWorkRequest PERIODIC_WORK_REQUEST =
//            new PeriodicWorkRequest.Builder(SyncWorkManager.class, REPEAT_INTERVAL, TimeUnit.MINUTES)
//                    .setConstraints(MY_CONSTRAINTS)
//                    .build();


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
    public static final String NEW_CARD_INTENT_ACTION = "org.intelehealth.app.NEW_CARD";

    public static final String VISIT_DECISION_PENDING_ACTION = "org.intelehealth.app.VISIT_OUT_OF_TIME";

    public static final String SYNC_INTENT_ACTION = "org.intelehealth.app.LAST_SYNC";
    public static final String SYNC_INTENT_DATA_KEY = "SYNC_JOB_TYPE";

    public static final String SYNC_INTENT_SCREEN = "fromScreen";
    public static final int SYNC_FAILED = 0;
    public static final int SYNC_PULL_DATA_DONE = 1;
    public static final int SYNC_PUSH_DATA_DONE = 2;
    public static final int SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE = 3;
    public static final int SYNC_OBS_IMAGE_PUSH_DONE = 4;

    //    public static final String FIREBASE_REAL_TIME_DB_BASE_URL = "https://ezazi-8712a-default-rtdb.firebaseio.com/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF = "rtc_notify/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE = "device_info/";

    public static final String SCREEN_REFRESH_EVENT_RECEIVER = "REFRESH_SCREEN_RECEIVER";

    public static final String REFRESH_SCREEN_EVENT = "REFRESH_SCREEN";

    public static final String NOT_APPLICABLE = "NA";

    public static final String PRIMARY = "Primary";

    public static final String SECONDARY = "Secondary";

    public static final String NOT_APPLICABLE_FULL_TEXT = "Not Applicable";

    public static final String OTHER_OPTION = "Other";

    public static final int INPUT_MAX_LENGTH = 300;

    public static final String EVENT_SHIFT_CHANGED = "shiftChanged";

    public static final String TO_HW_USER_UUID = "toHwUserUuid";

    public static final String FROM_HW = "from_hw";

    public static final String SHIFTED_DATA = "shifted_data";

    public static final String SHIFTED_PATIENT_RECEIVER = "SHIFTED_PATIENT_RECEIVER";

    public static final String GENERAL_PHYSICIAN = "General Physician";

    public static final String OBSTETRICIAN_GYNECOLOGIST = "Obstetrician & Gynecologist";

    public static String getFirebaseRTDBUrl() {
        return BuildConfig.REAL_TIME_FB_URL;
    }

    public static String getScreenRefreshEventReceiver() {
        return IntelehealthApplication.getAppContext().getPackageName() + "." + SCREEN_REFRESH_EVENT_RECEIVER;
    }

    public static String getShiftedPatientReceiver() {
        return IntelehealthApplication.getAppContext().getPackageName() + "." + SHIFTED_PATIENT_RECEIVER;
    }

    public static String getFirebaseRTDBRootRef() {
        return BuildConfig.FB_RT_DB + "/" + FIREBASE_REAL_TIME_DB_BASE_REF;
    }

    public static String getFirebaseRTDBRootRefForDeviceInfo() {
        return BuildConfig.FB_RT_DB + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE;
    }

    public static final String CURRENT_ENC_EDIT_INTENT_ACTION = "org.intelehealth.app.CURRENT_CARD_EDIT";

}

