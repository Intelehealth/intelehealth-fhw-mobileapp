package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class to manage input/output with the database.
 */

public class LocalRecordsDatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "localRecords.db";
    public static final String CREATE_PATIENT = "CREATE VIRTUAL TABLE IF NOT EXISTS patient USING fts3(" +
            "_id varchar(255) PRIMARY KEY," +
            "openmrs_id varchar," +
            // "first_name varchar(50) NOT NULL," +
            "first_name varchar(50)," +
            "middle_name varchar(50)," +
            "last_name varchar(50)," +
            // "date_of_birth TEXT NOT NULL," +
            "date_of_birth TEXT," +
            "phone_number integer(10)," +
            "address1 varchar(255)," +
            "address2 varchar(255)," +
            // "city_village varchar(255) NOT NULL," +
            // "state_province varchar(255) NOT NULL," +
            // "postal_code varchar(50) NOT NULL," +
            // "country varchar(50) NOT NULL," +
            // "gender varchar(50) NOT NULL," +
            "city_village varchar(255)," +
            "state_province varchar(255)," +
            "postal_code varchar(50)," +
            "country varchar(50)," +
            "gender varchar(50)," +
            "portrait_name varchar(255)," +
            "patient_identifier1 varchar(255)," +
            "patient_identifier2 varchar(255)," +
            "patient_identifier3 varchar(255)" +
            ")";
    public static final String CREATE_ATTRIB = "CREATE TABLE IF NOT EXISTS patient_attribute (" +
            "_id integer PRIMARY KEY," +
            "attribute_type_id integer(10) NOT NULL," +
            "patient_id integer(10) NOT NULL," +
            "value varchar(50)," +
            "FOREIGN KEY (patient_id) REFERENCES patient(_id)" +
            ")";
    public static final String CREATE_VISIT = "CREATE TABLE IF NOT EXISTS visit (" +
            "_id integer PRIMARY KEY," +
            "patient_id integer(10)," +
            "start_datetime TEXT NOT NULL," +
            "end_datetime TEXT," +
            "visit_type_id integer(10)," +
            "visit_location_id integer(10) NOT NULL," +
            "visit_creator integer(10) NOT NULL," +
            "openmrs_visit_id integer(10)" +
     //       "openmrs_visit_id integer(10)," +
     //       "FOREIGN KEY (patient_id) REFERENCES patient(_id)" +
            ")";
    public static final String CREATE_OBS = "CREATE TABLE IF NOT EXISTS obs (" +
            "_id INTEGER PRIMARY KEY," +
            "patient_id integer(10)," +
            "visit_id integer(10) NOT NULL," +
            "value text," +
            "concept_id integer(10) NOT NULL," +
            "creator integer(10) NOT NULL," +
            "openmrs_encounter_id integer(10)," +
            "openmrs_obs_id integer(10)" +
     //       "openmrs_obs_id integer(10)," +
     //       "FOREIGN KEY (visit_id) REFERENCES visit (_id)," +
     //       "FOREIGN KEY (patient_id) REFERENCES patient (_id)" +
            ")";
    public static final String CREATE_USER = "CREATE TABLE IF NOT EXISTS user_provider (" +
            "_id INTEGER PRIMARY KEY," +
            "openmrs_provider_id integer(10)," +
            "openmrs_user_id integer(10)," +
            "openmrs_role varchar(50)," +
            "first_name varchar(50) NOT NULL," +
            "middle_name varchar(50)," +
            "last_name varchar(50)," +
            "username varchar(50) NOT NULL," +
            "password varchar(128) NOT NULL," +
            "secret_question varchar(255) NOT NULL," +
            "secret_answer varchar(255) NOT NULL," +
            "date_created TEXT NOT NULL," +
            "creator integer(10) NOT NULL," +
            "date_changed TEXT NOT NULL," +
            "changed_by integer(10) NOT NULL" +
            ")";
    public static final String CREATE_LOCATION = "CREATE TABLE location_details (" +
            "_id INTEGER PRIMARY KEY," +
            "tablet_id integer(10) NOT NULL," +
            "location_name varchar(255) NOT NULL," +
            "openmrs_location_id integer(255) NOT NULL" +
            ")";
    public static final String DROP = "DROP TABLE IF EXISTS";

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
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: discuss upgrade policy
    }
}
