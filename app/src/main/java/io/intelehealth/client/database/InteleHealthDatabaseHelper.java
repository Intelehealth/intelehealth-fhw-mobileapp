package io.intelehealth.client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import io.intelehealth.client.app.AppConstants;

public class InteleHealthDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = AppConstants.DATABASE_VERSION;
    public static final String DATABASE_NAME = AppConstants.DATABASE_NAME;
    /*
    * "openmrs_uuid": "8ab7f041-0b02-4ac0-a8f4-aa35f90ae3a1",
                    "openmrs_id": "10213-7",
                    "firstname": "John",
                    "middlename": "Mira",
                    "lastname": "Bora",
                    "dateofbirth": "Oct 27, 2018 12:00:00 AM",
                    "address1": "Everlasting Village",
                    "country": "Philippines",
                    "gender": "M"
    *
    * char(36) default (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' || substr(lower(hex(randomblob(2))),2) || '-' || substr('89ab',abs(random()) % 4 + 1, 1) || substr(lower(hex(randomblob(2))),2) || '-' || lower(hex(randomblob(6))))
    *
    * */
    public static final String CREATE_PATIENT_MAIN = "CREATE TABLE IF NOT EXISTS tbl_patient(" +
            "uuid TEXT PRIMARY KEY," +
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
            "sdw TEXT," +
            "occupation TEXT," +
            "patient_photo TEXT," +
            "economic_status TEXT," +
            "education_status TEXT," +
            "caste TEXT," +
            "dead Text," +
            "modified_date TEXT," +
            "synced TEXT" +
            ")";
    /* "openmrs_uuid": "834054d5-db4d-481c-8f2b-17da25aa93a2",
                "value": "123456789",
                "openmrs_person_attribute_type_uuid": "14d4f066-15f5-102d-96e4-000c29c2a5d7",
                "openmrs_patientuuid": "8ab7f041-0b02-4ac0-a8f4-aa35f90ae3a1"
    * */
    public static final String CREATE_ATTRIB_MAIN = "CREATE TABLE IF NOT EXISTS tbl_patient_attribute (" +
            "uuid TEXT PRIMARY KEY," +
            "value TEXT," +
            "person_attribute_type_uuid TEXT ," +
            "patientuuid TEXT," +
            "modified_date TEXT," +
            "sync TEXT" +
            ")";
    public static final String CREATE_VISIT_MAIN = "CREATE TABLE IF NOT EXISTS tbl_visit (" +
            "uuid TEXT PRIMARY KEY," +
            "patientuuid TEXT," +
            "startdate TEXT," +
            "enddate TEXT," +
            "visit_type_uuid TEXT," +
            "locationuuid TEXT(10) ," +
            "creator TEXT ," +
            "emergency TEXT," +
            "modified_date TEXT," +
            "synced TEXT" +
            ")";

    /*
      "openmrs_patientuuid": "8ab7f041-0b02-4ac0-a8f4-aa35f90ae3a1",
              "openmrs_visituuid": "8f80610c-2a8f-487e-8ee9-0c2c7ced4d89",
              "visit_type_id": 4,
              "startdate": "Nov 13, 2018 1:03:55 PM",
              "enddate": "Nov 14, 2018 6:37:24 AM",
              "locationuuid": "1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a",
              "creator": 4*/
    public static final String CREATE_OBS_MAIN = "CREATE TABLE IF NOT EXISTS tbl_obs (" +
            "uuid TEXT PRIMARY KEY," +
            "encounteruuid TEXT," +
            "conceptuuid integer(10)," +
            "value TEXT," +
            "creator TEXT," +
            "voided TEXT," +
            "modified_date TEXT," +
            "synced TEXT" +
            ")";
    /*"openmrs_obsuuid": "5e3e7c8d-e3c3-4a1d-a391-d3d04e45df0e",
                "openmrs_encounteruuid": "99835c05-8c5c-4d17-b96d-095fb12ebc53",
                "conceptid": 5090,
                "value": "54",
                "creator": 4
    * */
    public static final String CREATE_ENCOUNTER_MAIN = "CREATE TABLE IF NOT EXISTS tbl_encounter (" +
            "uuid TEXT PRIMARY KEY," +
            "visituuid TEXT," +
            "encounter_type_uuid TEXT," +
            "modified_date TEXT," +
            "synced TEXT," +
            "voided TEXT" +
            ")";
    /*"openmrs_encounteruuid": "99835c05-8c5c-4d17-b96d-095fb12ebc53",
                    "openmrs_visituuid": "8f80610c-2a8f-487e-8ee9-0c2c7ced4d89",
                    "encounter_type": "6"
    * */
    public static final String CREATE_PATIENT_ATTRIBUTE_MASTER_MAIN = "CREATE TABLE IF NOT EXISTS tbl_patient_attribute_master (" +
            "uuid TEXT PRIMARY KEY," +
            "name TEXT," +
            "modified_date TEXT," +
            "sync TEXT" +
            ")";
    /*  "openmrs_uuid": "8d871386-c2cc-11de-8d13-0010c6dffd0f",
                "name": "Race"
    * */
    public static final String CREATE_LOCATION = "CREATE TABLE IF NOT EXISTS tbl_location (" +
            "name TEXT," +
            "locationuuid TEXT PRIMARY KEY," +
            "retired integer(10)," +
            "modified_date TEXT," +
            "synced TEXT" +
            ")";
    public static final String CREATE_PROVIDER = "CREATE TABLE IF NOT EXISTS tbl_provider (" +
            "uuid TEXT PRIMARY KEY," +
            "identifier TEXT," +
            "given_name TEXT," +
            "family_name TEXT," +
            "voided integer(10)," +
            "modified_date TEXT," +
            "synced TEXT" +
            ")";

    public static final String CREATE_SYNC = "CREATE TABLE IF NOT EXISTS tbl_sync (" +
            "locationuuid TEXT PRIMARY KEY," +
            "last_pull_execution_time TEXT," +
            "synced TEXT," +
            "devices_sync TEXT" +
            ")";


    public InteleHealthDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PATIENT_MAIN);
        db.execSQL(CREATE_ATTRIB_MAIN);
        db.execSQL(CREATE_ENCOUNTER_MAIN);
        db.execSQL(CREATE_OBS_MAIN);
        db.execSQL(CREATE_VISIT_MAIN);
        db.execSQL(CREATE_PATIENT_ATTRIBUTE_MASTER_MAIN);
        db.execSQL(CREATE_LOCATION);
        db.execSQL(CREATE_PROVIDER);
        db.execSQL(CREATE_SYNC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

}
