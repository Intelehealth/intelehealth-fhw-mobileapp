package org.intelehealth.msfarogyabharat.app;


import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;


import java.io.File;
import java.util.concurrent.TimeUnit;

import org.intelehealth.msfarogyabharat.database.InteleHealthDatabaseHelper;
import org.intelehealth.msfarogyabharat.syncModule.LastSyncWork;
import org.intelehealth.msfarogyabharat.syncModule.VisitSummaryWork;
import org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils;
import org.intelehealth.msfarogyabharat.utilities.NotificationUtils;
import org.intelehealth.msfarogyabharat.utilities.UuidGenerator;
import org.intelehealth.msfarogyabharat.networkApiCalls.ApiClient;
import org.intelehealth.msfarogyabharat.networkApiCalls.ApiInterface;
import org.intelehealth.msfarogyabharat.syncModule.SyncWorkManager;

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


    //App vitals constants
    public static final String MAXIMUM_BP_SYS = "250";
    public static final String MAXIMUM_HEIGHT = "272";
    public static final String MAXIMUM_WEIGHT = "150";
    public static int APP_VERSION_CODE = 26;
    public static final String MINIMUM_BP_SYS = "50";
    public static final String MAXIMUM_BP_DSYS = "150";
    public static final String MINIMUM_BP_DSYS = "30";
    public static final String MAXIMUM_PULSE = "200";
    public static final String MINIMUM_PULSE = "30";
    public static final String MAXIMUM_TEMPERATURE_CELSIUS = "43";
    public static final String MINIMUM_TEMPERATURE_CELSIUS = "25";
    public static final String MINIMUM_TEMPERATURE_FARHENIT = "77";
    public static final String MAXIMUM_TEMPERATURE_FARHENIT = "109";
    public static final String MAXIMUM_SPO2 = "100";
    public static final String MINIMUM_SPO2 = "1";
    public static final String MAXIMUM_RESPIRATORY = "80";
    public static final String MINIMUM_RESPIRATORY = "10";

    //functions constants
    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().UuidGenerator();
    public static NotificationUtils notificationUtils = new NotificationUtils();

    // permissions
    public static final int CAMERA_PERMISSIONS = 69420;

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
    public static final String SYNC_INTENT_ACTION = "app.intelehealth.client.LAST_SYNC";
    public static final String SYNC_INTENT_DATA_KEY = "SYNC_JOB_TYPE";
    public static final int SYNC_FAILED = 0;
    public static final int SYNC_PULL_DATA_DONE = 1;
    public static final int SYNC_PUSH_DATA_DONE = 2;
    public static final int SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE = 3;
    public static final int SYNC_OBS_IMAGE_PUSH_DONE = 4;
    public static final String SPECIALIST_DOCTOR_NOT_NEEDED = "Specialist Doctor Not Needed";
    public static final String DOCTOR_NOT_NEEDED = "Doctor not needed";

    public static ApiInterface ivrApiInterface = ApiClient.createIVRService(ApiInterface.class);

    public static String SMS_API_KEY = "A39e1e65900618ef9b6e16da473f8894d";
    public static String SMS_TEMPLATE_ID = "1107162754943945813";
}

