package io.intelehealth.client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;

public class InteleHealthDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = AppConstants.DATABASE_VERSION;
    public static final String DATABASE_NAME = AppConstants.DATABASE_NAME;
    /*"openmrs_obsuuid": "5e3e7c8d-e3c3-4a1d-a391-d3d04e45df0e",
                "openmrs_encounteruuid": "99835c05-8c5c-4d17-b96d-095fb12ebc53",
                "conceptid": 5090,
                "value": "54",
                "creator": 4
    * */
    public static final String CREATE_ENCOUNTER_MAIN = "CREATE TABLE IF NOT EXISTS tbl_encounter (" +
            "uuid TEXT PRIMARY KEY," +
            "visituuid TEXT," +
            "encounter_time TEXT," +
            "provider_uuid TEXT," +
            "encounter_type_uuid TEXT," +
            "modified_date TEXT," +
            "sync TEXT DEFAULT 'false' ," +
            "voided TEXT DEFAULT '0'," +
            "privacynotice_value TEXT" +
            ")";
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
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
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
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
            ")";
    public static final String CREATE_VISIT_MAIN = "CREATE TABLE IF NOT EXISTS tbl_visit (" +
            "uuid TEXT PRIMARY KEY," +
            "patientuuid TEXT," +
            "startdate TEXT," +
            "enddate TEXT," +
            "visit_type_uuid TEXT," +
            "locationuuid TEXT ," +
            "creator TEXT ," +
            "modified_date TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
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
            "uuid TEXT PRIMARY KEY ," +
            "encounteruuid TEXT," +
            "conceptuuid TEXT," +
            "value TEXT," +
            "creator TEXT," +
            "voided TEXT DEFAULT '0'," +
            "modified_date TEXT," +
            "sync TEXT DEFAULT 'false' " +
            ")";
    SessionManager sessionManager = null;
    /*"openmrs_encounteruuid": "99835c05-8c5c-4d17-b96d-095fb12ebc53",
                    "openmrs_visituuid": "8f80610c-2a8f-487e-8ee9-0c2c7ced4d89",
                    "encounter_type": "6"
    * */
    public static final String CREATE_PATIENT_ATTRIBUTE_MASTER_MAIN = "CREATE TABLE IF NOT EXISTS tbl_patient_attribute_master (" +
            "uuid TEXT PRIMARY KEY," +
            "name TEXT," +
            "modified_date TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
            ")";

    public static final String CREATE_VISIT_ATTRIBUTE_MASTER_MAIN = "CREATE TABLE IF NOT EXISTS tbl_visit_attribute_master (" +
            "uuid TEXT PRIMARY KEY," +
            "name TEXT," +
            "retired TEXT," +
            "modified_date TEXT" +
            ")";

    public static final String CREATE_VISIT_ATTRIB = "CREATE TABLE IF NOT EXISTS tbl_visit_attribute (" +
            "uuid TEXT PRIMARY KEY," +
            "value TEXT," +
            "visit_attribute_type_uuid TEXT ," +
            "visituuid TEXT," +
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
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
            ")";
    public static final String CREATE_PROVIDER = "CREATE TABLE IF NOT EXISTS tbl_provider (" +
            "uuid TEXT PRIMARY KEY," +
            "identifier TEXT," +
            "given_name TEXT," +
            "family_name TEXT," +
            "voided TEXT DEFAULT '0'," +
            "modified_date TEXT," +
            "sync TEXT DEFAULT 'false' " +
            ")";

    public static final String CREATE_SYNC = "CREATE TABLE IF NOT EXISTS tbl_sync (" +
            "locationuuid TEXT PRIMARY KEY," +
            "last_pull_execution_time TEXT," +
            "sync TEXT DEFAULT 'false' ," +
            "devices_sync TEXT" +
            ")";

    public static final String CREATE_UUID_DICTIONARY = "CREATE TABLE IF NOT EXISTS tbl_uuid_dictionary (" +
            "uuid TEXT  PRIMARY KEY," +
            "name TEXT" +
            ")";
    public static final String CREATE_IMAGE_RECORDS = "CREATE TABLE IF NOT EXISTS tbl_image_records(" +
            "uuid TEXT PRIMARY KEY," +
            "patientuuid TEXT," +
            "visituuid TEXT," +
            "encounteruuid TEXT," +
            "image_path TEXT," +
            "obs_time_date TEXT," +
            "image_type TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
            ")";


    public InteleHealthDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void uuidInsert(SQLiteDatabase db) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.isFirstTimeLaunched()) {
//            db.execSQL("Drop Table tbl_uuid_dictionary");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('3edb0e09-9135-481e-b8f0-07a26fa9a5ce','CURRENTCOMPLAINT')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('e1761e85-9b50-48ae-8c4d-e6b7eeeba084','PHYSICAL_EXAMINATION')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','HEIGHT')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','WEIGHT')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','PULSE')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','SYSTOLIC_BP')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','DIASTOLIC_BP')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','TEMPERATURE')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','RESPIRATORY')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','SPO2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('62bff84b-795a-45ad-aae1-80e7f5163a82','RHK_MEDICAL_HISTORY_BLURB')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('e8caffd6-5d22-41c4-8d6a-bc31a44d0c86','FOLLOW_UP_VISIT')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c','EMERGENCY')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('537bb20d-d09d-4f88-930b-cc45c7d662df','TELEMEDICINE_DIAGNOSIS')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('c38c0c50-2fd2-4ae3-b7ba-7dd25adca4ca','JSV_MEDICATIONS')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('0308000d-77a2-46e0-a6fa-a8c1dcbc3141','MEDICAL_ADVICE ')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('23601d71-50e6-483f-968d-aeef3031346d','REQUESTED_TESTS ')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','ADDITIONAL_COMMENTS')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('35c3afdd-bb96-4b61-afb9-22a5fc2d088e','SON_WIFE_DAUGHTER')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5fe2ef6f-bbf7-45df-a6ea-a284aee82ddc','OCCUPATION')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f','ENCOUNTER_ADULTINITIAL')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('67a71486-1a54-468f-ac3e-7091a9a79584','ENCOUNTER_VITALS')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('d7151f82-c1f3-4152-a605-2f9ea7414a79','ENCOUNTER_VISIT_NOTE')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('629a9d0b-48eb-405e-953d-a5964c88dc30','ENCOUNTER_PATIENT_EXIT_SURVEY')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('05a29f94-c0ed-11e2-94be-8c13b969e334','IDENTIFIER_OPENMRS_ID')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('14d4f066-15f5-102d-96e4-000c29c2a5d7','ATTRIBUTE_PHONE_NUMBER')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5a889d96-0c84-4a04-88dc-59a6e37db2d3','ATTRIBUTE_CASTE')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('1c718819-345c-4368-aad6-d69b4c267db7','ATTRIBUTE_EDUCATION_LEVEL')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('f4af0ef3-579c-448a-8157-750283409122','ATTRIBUTE_ECONOMIC_STATUS')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('1b2f34f7-2bf8-4ef7-9736-f5b858afc160','ATTRIBUTE_SON_WIFE_DAUGHTER ')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ecdaadb6-14a0-4ed9-b5b7-cfed87b44b87','ATTRIBUTE_OCCUPATION')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('8d87236c-c2cc-11de-8d13-0010c6dffd0f','ATTRIBUTE_HEALTH_CENTER')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('a86ac96e-2e07-47a7-8e72-8216a1a75bfd','VISIT_TELEMEDICINE')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('78284507-fb71-4354-9b34-046ab205e18f','RATING')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('36d207d6-bee7-4b3e-9196-7d053c6eddce','COMMENTS')");
//            patient attributes master
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('0f63da10-882f-4352-9507-8c2e295c4bf7','Commune')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('14d4f066-15f5-102d-96e4-000c29c2a5d7','Telephone Number')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('1b2f34f7-2bf8-4ef7-9736-f5b858afc160','Son/wife/daughter')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('1c718819-345c-4368-aad6-d69b4c267db7','Education Level')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('1d609ff3-6f45-44d3-a0b6-601976571979','Department')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('4f07985c-88a5-4abd-aa0c-f3ec8324d8e7','Test Patient')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('5a889d96-0c84-4a04-88dc-59a6e37db2d3','Caste')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('8d8718c2-c2cc-11de-8d13-0010c6dffd0f','Birthplace')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('8d871afc-c2cc-11de-8d13-0010c6dffd0f','Citizenship')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('8d871d18-c2cc-11de-8d13-0010c6dffd0f','Mothers Name')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('8d871f2a-c2cc-11de-8d13-0010c6dffd0f','Civil Status')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('8d871d18-c2cc-11de-8d13-0010c6dffd0f','Health District')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('8d871d18-c2cc-11de-8d13-0010c6dffd0f','Health Center')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('99d5b4ef-db5a-4ae4-a5e2-2bf1ea0a251e','Cell Number')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('cf17630b-b966-47e9-974b-8098ee87f773','Prison Name')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('ecdaadb6-14a0-4ed9-b5b7-cfed87b44b87','occupation')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_patient_attribute_master (uuid,name) VALUES('f4af0ef3-579c-448a-8157-750283409122','Economic Status')");




        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PATIENT_MAIN);
        db.execSQL(CREATE_ATTRIB_MAIN);
        db.execSQL(CREATE_ENCOUNTER_MAIN);
        db.execSQL(CREATE_IMAGE_RECORDS);
        db.execSQL(CREATE_OBS_MAIN);
        db.execSQL(CREATE_VISIT_MAIN);
//        db.execSQL(CREATE_VISIT_ATTRIBUTE_MASTER_MAIN);
//        db.execSQL(CREATE_VISIT_ATTRIB);
        db.execSQL(CREATE_PATIENT_ATTRIBUTE_MASTER_MAIN);
        db.execSQL(CREATE_LOCATION);
        db.execSQL(CREATE_PROVIDER);
        db.execSQL(CREATE_SYNC);
        db.execSQL(CREATE_UUID_DICTIONARY);
        uuidInsert(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                //upgrade logic from version 1 to 2
            case 2:
                //upgrade logic from version 2 to 3
            case 3:
                //upgrade logic from version 3 to 4
            case 4:
                //upgrade logic from version 4
                alterTables(db);
                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown oldVersion " + oldVersion);
        }

    }

    public void alterTables(SQLiteDatabase db) {
//        db.execSQL("Drop table  IF Exists patients");
    }

}
