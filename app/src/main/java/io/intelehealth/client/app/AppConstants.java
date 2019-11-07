package io.intelehealth.client.app;


import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;

//import com.snatik.storage.Storage;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.networkApiCalls.ApiClient;
import io.intelehealth.client.networkApiCalls.ApiInterface;
import io.intelehealth.client.syncModule.SyncWorkManager;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.NotificationUtils;
import io.intelehealth.client.utilities.SqliteDbCloseHelper;
import io.intelehealth.client.utilities.UuidGenerator;

public class AppConstants {
    //Constants
    public static final String DATABASE_NAME = "localrecords.db";
    public static final int DATABASE_VERSION = 4;
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static final String MIND_MAP_SERVER_URL = "http://mindmaps.intelehealth.io/";
    public static final String IMAGE_APP_ID = "app2";
    public static final String dbfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" + File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
    public static String CONFIG_FILE_NAME = "config.json";
    public static final String IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    public static final String PATIENT_IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "Patient_Images/";
    public static final String MESSAGE_PROGRESS = "message_progress";


    //App vitals constants
    public static final String MAXIMUM_BP_SYS = "150";
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
    public static final String MINIMUM_TEMPERATURE_FARHENIT = "80";
    public static final String MAXIMUM_TEMPERATURE_FARHENIT = "120";
    public static final String MAXIMUM_SPO2 = "100";
    public static final String MINIMUM_SPO2 = "1";
    public static final String MAXIMUM_RESPIRATORY = "80";
    public static final String MINIMUM_RESPIRATORY = "10";

    //functions constants
//    public static Storage storage = new Storage(IntelehealthApplication.getAppContext());
    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static SqliteDbCloseHelper sqliteDbCloseHelper = new SqliteDbCloseHelper();
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().UuidGenerator();
    public static SQLiteDatabase sqLiteDatabase = inteleHealthDatabaseHelper.getWritableDatabase();
    public static NotificationUtils notificationUtils = new NotificationUtils();


    //  Image Conversion Ratio
    public static int IMAGE_JPG_QUALITY = 70;

    //    syncBackground Timings

    public static int SYNC = 1000 * 60 * 3;
    public static int MAXIMUM_DELAY = 1000 * 60 * 3;

    public static int REPEAT_INTERVAL = 15;
    public static Constraints MY_CONSTRAINTS = new Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build();

    public static PeriodicWorkRequest PERIODIC_WORK_REQUEST =
            new PeriodicWorkRequest.Builder(SyncWorkManager.class, REPEAT_INTERVAL, TimeUnit.MINUTES)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();
}
