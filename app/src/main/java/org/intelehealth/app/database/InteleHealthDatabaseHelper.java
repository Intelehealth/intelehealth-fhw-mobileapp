package org.intelehealth.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;

public class InteleHealthDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = AppConstants.DATABASE_VERSION;
    public static final String DATABASE_NAME = AppConstants.DATABASE_NAME;
    public static SQLiteDatabase database;


    /**
     * This take will keep the log of the connection info wrt the visit-uuid
     */
    public static final String CREATE_RTC_LOGS = "CREATE TABLE IF NOT EXISTS tbl_rtc_connection_log (" +
            "uuid TEXT PRIMARY KEY," +
            "visit_uuid TEXT," +
            "connection_info TEXT )";
    /**
     * This will keep the appointment listing
     */
    public static final String CREATE_APPOINTMENTS = "CREATE TABLE IF NOT EXISTS tbl_appointments (" +
            "uuid TEXT PRIMARY KEY," +
            "appointment_id Integer," +
            "slot_day TEXT," +
            "slot_date TEXT," +
            "slot_duration Integer," +
            "slot_duration_unit TEXT," +
            "slot_time TEXT," +
            "speciality TEXT," +
            "user_uuid TEXT," +
            "dr_name TEXT," +
            "visit_uuid TEXT," +
            "patient_id TEXT," +
            "patient_name TEXT," +
            "open_mrs_id TEXT," +
            "status TEXT," +
            "created_at TEXT," +
            "updated_at TEXT )";


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

    public static final String CREATE_DR_SPECIALITY =
            "CREATE TABLE IF NOT EXISTS tbl_dr_speciality (" +
                    "uuid TEXT PRIMARY KEY," +
                    "provideruuid TEXT," +
                    "attributetypeuuid TEXT," +
                    "value TEXT UNIQUE," +
                    "voided TEXT" +
                    ")";

    //visit attributes tables
    public static final String CREATE_VISIT_ATTRIBUTES =
            "CREATE TABLE IF NOT EXISTS tbl_visit_attribute (" +
                    "uuid TEXT PRIMARY KEY," +
                    "visit_uuid TEXT," +
                    "value TEXT," +
                    "visit_attribute_type_uuid TEXT," +
                    "voided TEXT," +
                    "sync TEXT)";
    //sync column is maintained for internal checking on android side for update.

    public static final String CREATE_USER_CREDENTIALS = "CREATE TABLE IF NOT EXISTS tbl_user_credentials (" +
            "username TEXT," +
            "password TEXT UNIQUE," +
            "creator_uuid_cred TEXT," +
            "chwname TEXT," +
            "provider_uuid_cred TEXT"+
            ")";



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
            "isdownloaded TEXT DEFAULT 'false'," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' ," +
            "issubmitted Integer DEFAULT 0" +
            ")";


    public static final String CREATE_OBS_MAIN = "CREATE TABLE IF NOT EXISTS tbl_obs (" +
            "uuid TEXT PRIMARY KEY ," +
            "encounteruuid TEXT," +
            "conceptuuid TEXT," +
            "value TEXT," +
            "creator TEXT," +
            "voided TEXT DEFAULT '0'," +
            "obsservermodifieddate TEXT," +
            "modified_date TEXT," +
            "created_date TEXT DEFAULT CURRENT_TIMESTAMP ," +
            "sync TEXT DEFAULT 'false' " +
            ")";
    SessionManager sessionManager = null;

    public static final String CREATE_PATIENT_ATTRIBUTE_MASTER_MAIN = "CREATE TABLE IF NOT EXISTS tbl_patient_attribute_master (" +
            "uuid TEXT PRIMARY KEY," +
            "name TEXT," +
            "modified_date TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
            ")";

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


    public static final String CREATE_UUID_DICTIONARY = "CREATE TABLE IF NOT EXISTS tbl_uuid_dictionary (" +
            "uuid TEXT  PRIMARY KEY," +
            "name TEXT," +
            "price TEXT," +
            "retired Integer" +")";

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
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('3edb0e09-9135-481e-b8f0-07a26fa9a5ce','CURRENTCOMPLAINT', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('e1761e85-9b50-48ae-8c4d-e6b7eeeba084','PHYSICAL_EXAMINATION', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','HEIGHT', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','WEIGHT', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','PULSE', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','SYSTOLIC_BP', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','DIASTOLIC_BP', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','TEMPERATURE', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','RESPIRATORY', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','SPO2', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('62bff84b-795a-45ad-aae1-80e7f5163a82','RHK_MEDICAL_HISTORY_BLURB', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('e8caffd6-5d22-41c4-8d6a-bc31a44d0c86','FOLLOW_UP_VISIT', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c','EMERGENCY', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('537bb20d-d09d-4f88-930b-cc45c7d662df','TELEMEDICINE_DIAGNOSIS', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('c38c0c50-2fd2-4ae3-b7ba-7dd25adca4ca','JSV_MEDICATIONS', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('67a050c1-35e5-451c-a4ab-fff9d57b0db1','MEDICAL_ADVICE ', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('23601d71-50e6-483f-968d-aeef3031346d','REQUESTED_TESTS ', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','ADDITIONAL_COMMENTS', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('35c3afdd-bb96-4b61-afb9-22a5fc2d088e','SON_WIFE_DAUGHTER', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5fe2ef6f-bbf7-45df-a6ea-a284aee82ddc','OCCUPATION', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f','ENCOUNTER_ADULTINITIAL', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('67a71486-1a54-468f-ac3e-7091a9a79584','ENCOUNTER_VITALS', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('d7151f82-c1f3-4152-a605-2f9ea7414a79','ENCOUNTER_VISIT_NOTE', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('629a9d0b-48eb-405e-953d-a5964c88dc30','ENCOUNTER_PATIENT_EXIT_SURVEY', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('05a29f94-c0ed-11e2-94be-8c13b969e334','IDENTIFIER_OPENMRS_ID', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('14d4f066-15f5-102d-96e4-000c29c2a5d7','ATTRIBUTE_PHONE_NUMBER', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('5a889d96-0c84-4a04-88dc-59a6e37db2d3','ATTRIBUTE_CASTE', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('1c718819-345c-4368-aad6-d69b4c267db7','ATTRIBUTE_EDUCATION_LEVEL', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('f4af0ef3-579c-448a-8157-750283409122','ATTRIBUTE_ECONOMIC_STATUS', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('1b2f34f7-2bf8-4ef7-9736-f5b858afc160','ATTRIBUTE_SON_WIFE_DAUGHTER ', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('ecdaadb6-14a0-4ed9-b5b7-cfed87b44b87','ATTRIBUTE_OCCUPATION', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('8d87236c-c2cc-11de-8d13-0010c6dffd0f','ATTRIBUTE_HEALTH_CENTER', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('a86ac96e-2e07-47a7-8e72-8216a1a75bfd','VISIT_TELEMEDICINE', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('78284507-fb71-4354-9b34-046ab205e18f','RATING', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('36d207d6-bee7-4b3e-9196-7d053c6eddce','COMMENTS', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('7030c68e-eecc-4656-bb0a-e465aea6195f','Visit Billing Details', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('ff82d253-877a-4970-a03f-3da16bee2a4a','Blood Sugar (Non-Fasting)', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('d7670f6a-377f-4807-92c8-26dc339fb0fe','Blood Sugar (Random)', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('790cbe32-4a85-4953-a3a6-4f6b0e26336e','Blood Sugar ( Post-prandial)', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('f6763c8d-c5e5-4e51-945c-c3d991b12fe2','Blood Glucose (Fasting)', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('71f0aadf-2a15-420f-897a-145dfc87fcd1','Haemoglobin Test', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('e331ccc4-7995-41d5-abe6-15b3e5ead1b1','SERUM URIC ACID', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('9d2f0fcc-538f-11e6-9cfe-86f436325720','TOTAL CHOLESTEROL', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('82be928b-5b17-4f44-86f0-3b75ffc56839','Billing Visit Type Consultation', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('4702a502-15c3-4759-99f0-bb685c374541','Billing Visit Type Followup', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('ba8b1ed9-ec2f-4bab-9e0e-c60fcb44c918','Bill Paid Status', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('9fa03f61-8083-4cce-bbc0-d5f752a8ee7b','Receipt Number', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('caa20b34-48a2-4f79-b08e-7be82f379e49','Receipt Date', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('dd51ab03-12ef-43cb-9aef-2ec93a989816','BP Test', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('cb07c951-2cb5-41ca-b816-8cb104c9ae8b','Aadhar details', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('4a49bacc-3510-4700-9099-2b7aa788688e','Abdominal Girth', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('243dd7eb-e216-40bf-83fb-439723b22d8b','Patient Additional Documents', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('83d2e919-eeea-497b-b77b-69bed2770f37','SpO2_Bill', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('a77a921a-585f-4aba-9cc8-644bc8131947','Temperature_Bill', null, 0)");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name,price,retired) VALUES('29d36454-439d-4c53-b5eb-88f540cf6511','ECG_Bill', null, 0)");
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
        db.execSQL(CREATE_PATIENT_ATTRIBUTE_MASTER_MAIN);
        db.execSQL(CREATE_LOCATION);
        db.execSQL(CREATE_PROVIDER);
        db.execSQL(CREATE_UUID_DICTIONARY);
        db.execSQL(CREATE_USER_CREDENTIALS);
        db.execSQL(CREATE_DR_SPECIALITY);
        db.execSQL(CREATE_VISIT_ATTRIBUTES);
        db.execSQL(CREATE_RTC_LOGS);
        db.execSQL(CREATE_APPOINTMENTS);
        db.execSQL(CREATE_LOCATION_NEW);

        uuidInsert(db);
        database = db;

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
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown oldVersion " + oldVersion);
        }

    }


    public SQLiteDatabase getWriteDb() {
        if (database != null && database.isOpen())
            return database;
        else
            return getWritableDatabase();
    }

    public static final String CREATE_LOCATION_NEW = "CREATE TABLE IF NOT EXISTS tbl_location_new(" +
            "location_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT," +
            "country TEXT," +
            "state TEXT," +
            "district TEXT," +
            "tehsil TEXT," +
            "village TEXT," +
            "latitude TEXT," +
            "longitude TEXT," +
            "parent_location integer(11)," +
            "locationuuid TEXT," +
            "modified_date TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false' " +
            ")";

}
