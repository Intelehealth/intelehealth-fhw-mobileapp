package org.intelehealth.ekalarogya.app;


import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;


import java.io.File;
import java.util.concurrent.TimeUnit;

import org.intelehealth.ekalarogya.database.InteleHealthDatabaseHelper;
import org.intelehealth.ekalarogya.syncModule.LastSyncWork;
import org.intelehealth.ekalarogya.syncModule.VisitSummaryWork;
import org.intelehealth.ekalarogya.utilities.DateAndTimeUtils;
import org.intelehealth.ekalarogya.utilities.NotificationUtils;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UuidGenerator;
import org.intelehealth.ekalarogya.networkApiCalls.ApiClient;
import org.intelehealth.ekalarogya.networkApiCalls.ApiInterface;
import org.intelehealth.ekalarogya.syncModule.SyncWorkManager;

public class AppConstants {
    //Constants
    public static final String DATABASE_NAME = "localrecords.db";
    public static final int DATABASE_VERSION = 4;
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static final String IMAGE_APP_ID = "app2";
    public static final String dbfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" + File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
    public static final int CAMERA_PERMISSIONS = 499;
    public static String CONFIG_FILE_NAME = "config.json";
    public static final String IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    public static final String MESSAGE_PROGRESS = "message_progress";


    //App vitals constants

    public static final String MAXIMUM_HEIGHT = "272";
    public static final String MAXIMUM_WEIGHT = "500";

    public static final String MINIMUM_WEIGHT = "0.8";
    public static int APP_VERSION_CODE = 26;
    public static final String MINIMUM_BP_SYS = "70";
    public static final String MAXIMUM_BP_SYS = "220";
    public static final String MINIMUM_BP_DSYS = "50";
    public static final String MAXIMUM_BP_DSYS = "120";
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
    public static final String MAXIMUM_HEMOGLOBIN = "17";
    public static final String MINIMUM_HEMOGLOBIN = "5";
    public static final String MAXIMUM_SUGAR = "500";
    public static final String MINIMUM_SUGAR = "60";

    // BMI -- Color codes - start - ticket AEAT - 679

    public static final String BMI_ORANGE_MAX = "18.5";
    public static final String BMI_GREEN_MIN = "18.5";
    public static final String BMI_GREEN_MAX = "23.0";
    public static final String BMI_YELLOW_MIN = "23.0";
    public static final String BMI_YELLOW_MAX = "25.0";
    public static final String BMI_LIGHT_RED_MIN = "25.0";
    public static final String BMI_LIGHT_RED_MAX = "30.0";
    public static final String BMI_DARK_RED_MIN = "30.0";

    // SYS - DIA -- Color codes - start
    public static final String SYS_GREEN_MIN = "89";
    public static final String SYS_GREEN_MAX = "120";
    public static final String SYS_YELLOW_MIN = "119";
    public static final String SYS_YELLOW_MAX = "140";
    public static final String SYS_RED_MIN = "90";
    public static final String SYS_RED_MAX = "139";

    // Diastolic color code.
    public static final String DIA_GREEN_MIN = "80";
  //  public static final String DIA_GREEN_MAX = "120";
    public static final String DIA_YELLOW_MIN = "79";
    public static final String DIA_YELLOW_MAX = "100";
 //   public static final String DIA_RED_MIN = "90";
    public static final String DIA_RED_MAX = "99";
    // SYS - DIA -- Color codes - end



    //functions constants
    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().generateUuid();
    public static NotificationUtils notificationUtils = new NotificationUtils();
    public static final String DOCTOR_NOT_NEEDED="Specialist doctor not needed";

    //  Image Conversion Ratio
    public static int IMAGE_JPG_QUALITY = 70;


    public static int REPEAT_INTERVAL = 5;
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
    public static final String SYNC_INTENT_ACTION = "app.intelehealth.client.LAST_SYNC";
    public static final String SYNC_INTENT_DATA_KEY = "SYNC_JOB_TYPE";
    public static final int SYNC_FAILED = 0;
    public static final int SYNC_PULL_DATA_DONE = 1;
    public static final int SYNC_PUSH_DATA_DONE = 2;
    public static final int SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE = 3;
    public static final int SYNC_OBS_IMAGE_PUSH_DONE = 4;

    public static final String FIREBASE_REAL_TIME_DB_BASE_URL = "https://intelehealth-ekalarogya.firebaseio.com/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF = "rtc_notify/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE = "device_info/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_TEXT_CHAT_CONN_INFO = "TEXT_CHAT/";

    public static String getFirebaseRTDBUrl() {
        return FIREBASE_REAL_TIME_DB_BASE_URL;

    }

    public static String getFirebaseRTDBRootRef() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl().replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF;
    }

    public static String getFirebaseRTDBRootRefForTextChatConnInfo() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl().replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_TEXT_CHAT_CONN_INFO;
    }

    public static String getFirebaseRTDBRootRefForDeviceInfo() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl().replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE;
    }


}

