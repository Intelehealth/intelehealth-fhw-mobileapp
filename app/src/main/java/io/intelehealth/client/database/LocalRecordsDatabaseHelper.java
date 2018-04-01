package io.intelehealth.client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class to manage input/output with the database.
 */

public class LocalRecordsDatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "localRecords.db";
    public static final String CREATE_PATIENT = "CREATE TABLE IF NOT EXISTS patient(" +
            "_id integer PRIMARY KEY," +
            "openmrs_uuid TEXT," +
            "openmrs_id TEXT," +
            "first_name TEXT," +
            "middle_name TEXT," +
            "last_name TEXT," +
            "date_of_birth TEXT," +
            "phone_number TEXT," +
            "address1 TEXT," +
            "address2 TEXT," +
            "city_village TEXT," +
            "state_province TEXT," +
            "postal_code TEXT," +
            "country TEXT," +
            "gender TEXT," +
            "sdw TEXT," + //Temporary
            "occupation TEXT," + //Temporary
            "patient_photo TEXT,"+
            "economic_status TEXT,"+
            "education_status TEXT,"+
            "caste TEXT"+
            ")";
    public static final String CREATE_ATTRIB = "CREATE TABLE IF NOT EXISTS patient_attribute (" +
            "_id integer PRIMARY KEY," +
            "attribute_type_id integer(10) NOT NULL," +
            "patient_id integer  NOT NULL," +
            "value varchar(255)," +
            "FOREIGN KEY (patient_id) REFERENCES patient(patient_id)" +
            ")";
    public static final String CREATE_VISIT = "CREATE TABLE IF NOT EXISTS visit (" +
            "_id integer PRIMARY KEY," +
            "patient_id integer," +
            "start_datetime TEXT NOT NULL," +
            "end_datetime TEXT," +
            "visit_type_id integer(10)," +
            "visit_location_id integer(10) NOT NULL," +
            "visit_creator TEXT NOT NULL," +
            "openmrs_visit_uuid TEXT" +
            ")";
    public static final String CREATE_OBS = "CREATE TABLE IF NOT EXISTS obs (" +
            "_id integer PRIMARY KEY," +
            "patient_id integer," +
            "visit_id integer(10) NOT NULL," +
            "value text," +
            "concept_id integer(10) NOT NULL," +
            "creator TEXT," +
            "openmrs_encounter_id integer(10)," +
            "openmrs_obs_id integer(10)" +
            ")";

    public static final String CREATE_ENCOUNTER = "CREATE TABLE IF NOT EXISTS encounter ("+
            "_id integer PRIMARY KEY," +
            "openmrs_encounter_id integer(10) NOT NULL," +
            "patient_id integer," +
            "visit_id integer(10) NOT NULL," +
            "openmrs_visit_uuid TEXT," +
            "encounter_type TEXT," +
            "encounter_provider TEXT" +
            ")";


    public static final String CREATE_USER = "CREATE TABLE IF NOT EXISTS providers (" +
            "_id integer PRIMARY KEY," +
            "openmrs_user_uuid integer(10)," +
            "name varchar(50)" +
            ")";

    public static final String CREATE_LOCATION = "CREATE TABLE location_details (" +
            "_id integer PRIMARY KEY," +
            "tablet_id integer(10) NOT NULL," +
            "location_name varchar(255) NOT NULL," +
            "openmrs_location_id integer(255) NOT NULL" +
            ")";

    public static final String CREATE_DELAYED_JOBS = "CREATE TABLE " + DelayedJobQueueProvider.DELAYED_JOBS_TABLE_NAME + " (" +
            DelayedJobQueueProvider._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DelayedJobQueueProvider.JOB_TYPE + " TEXT NOT NULL," +
            DelayedJobQueueProvider.JOB_PRIORITY + " INTEGER NOT NULL," +
            DelayedJobQueueProvider.JOB_REQUEST_CODE + " INTEGER NOT NULL," +
            DelayedJobQueueProvider.PATIENT_NAME + " TEXT NOT NULL," +
            DelayedJobQueueProvider.PATIENT_ID + " INTEGER NOT NULL," +
            DelayedJobQueueProvider.VISIT_ID + " TEXT," +
            DelayedJobQueueProvider.VISIT_UUID + " TEXT," +
            DelayedJobQueueProvider.STATUS + " INTEGER," +
            DelayedJobQueueProvider.DATA_RESPONSE + " TEXT," +
            DelayedJobQueueProvider.SYNC_STATUS + " INTEGER" +
            ")";
    public static final String DROP = "DROP TABLE IF EXISTS";

    public static final String CREATE_IMAGE_RECORDS = "CREATE TABLE image_records("+
            "_id integer PRIMARY KEY AUTOINCREMENT," +
            "patient_id integer NOT NULL," +
            "visit_id integer(10)," +
            "image_path TEXT NOT NULL,"+
            "image_type TEXT NOT NULL,"+
            "parse_id TEXT," +
            "delete_status integer NOT NULL"
            +")";



    public LocalRecordsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON");
        db.execSQL(CREATE_PATIENT);
        db.execSQL(CREATE_ATTRIB);
        db.execSQL(CREATE_VISIT);
        db.execSQL(CREATE_OBS);
        db.execSQL(CREATE_ENCOUNTER);
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_LOCATION);
        db.execSQL(CREATE_DELAYED_JOBS);
        db.execSQL(CREATE_IMAGE_RECORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: discuss upgrade policy
    }


}
