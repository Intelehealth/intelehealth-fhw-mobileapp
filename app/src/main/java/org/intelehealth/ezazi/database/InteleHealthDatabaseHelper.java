package org.intelehealth.ezazi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;

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
            "provider_uuid_cred TEXT" +
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
            "creatoruuid TEXT, " +
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
            "comment TEXT," +
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
            "role TEXT," +
            "useruuid TEXT," +
            "voided TEXT DEFAULT '0'," +
            "modified_date TEXT," +
            "sync TEXT DEFAULT 'false' " +
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
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('67a050c1-35e5-451c-a4ab-fff9d57b0db1','MEDICAL_ADVICE ')");
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
            // Epartogram Timeline Encounters Creation for 12 Hrs...
            // Stage 1
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ee560d18-34a1-4ad8-87c8-98aed99c663d','Stage1_Hour1_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('a367b296-601f-474b-afa4-c6989ef43f77','Stage1_Hour1_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('f540fdfb-9ad5-4b44-ae35-b941863b3439','Stage1_Hour2_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ae69e6f0-7bf4-4981-8742-c59ad0040630','Stage1_Hour2_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('b89a6a89-a61d-4416-ae3e-ec527db3607a','Stage1_Hour3_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ec2a1d58-c6f8-4aca-a182-e986d1cf6f15','Stage1_Hour3_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('62bc5735-ea58-4a6d-b818-11de18134f55','Stage1_Hour4_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('c20ab5de-b694-483b-bd31-2a0d84ffe32d','Stage1_Hour4_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('0b52a5ba-63dd-432f-9dec-856e4bb1e2f8','Stage1_Hour5_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('6be7c08e-1c99-4c25-826e-ce0fd4819b25','Stage1_Hour5_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('6730fd1e-82d7-4d2f-98c7-c5798edfd20e','Stage1_Hour6_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ea1bbd4a-5e59-4124-bd0f-aefe7095abaf','Stage1_Hour6_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('769c612f-88d7-4d23-8288-4c8bb107cbd1','Stage1_Hour7_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('9e3b1576-726f-4963-9d7e-9a8b543273c1','Stage1_Hour7_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('4e947877-1dde-4c5e-bf0b-7c26995a7966','Stage1_Hour8_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('92d6cc84-a720-4106-8519-f474c4a7cca9','Stage1_Hour8_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('f3641f3f-a83e-47c7-b734-e11be3998836','Stage1_Hour9_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('b8611dbb-5ea4-4839-a6a4-d33e5866d087','Stage1_Hour9_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('e91e88fe-6282-42c5-93fe-2646b995983d','Stage1_Hour10_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('596207d9-0d7a-4835-bdd2-3f2c9f8c34be','Stage1_Hour10_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('dd9612ad-6781-41ee-8579-180b99be87dd','Stage1_Hour11_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('736ec5de-d780-4e34-a1cb-2de28702b0de','Stage1_Hour11_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('03ecf7c8-0750-4916-9c2d-466509ae28e7','Stage1_Hour12_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('9d83f930-6d7f-4cbb-8bfc-b5ef34b2f584','Stage1_Hour12_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('4084c5f1-09e8-403f-b09c-7b2fe99b23e7','Stage1_Hour13_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('2cf6fe27-1ee1-4ecd-bf88-fff8c4aeda68','Stage1_Hour13_2')");

            // Stage 2
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('558cc1b8-c352-4b27-9ec2-131fc19c26f0','Stage2_Hour1_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('91a68b35-fc73-455d-9841-e0d2d726a973','Stage2_Hour1_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('49cb3bd8-26b3-47f8-b6da-743dbbf1a2db','Stage2_Hour1_3')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('4911f731-0c60-406a-bad5-d76f133ad535','Stage2_Hour1_4')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('402ab3f5-9e79-4e1c-bb51-4126cc3d10c0','Stage2_Hour2_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('93d8e600-1bd4-4ab4-a57b-b615eb96f05e','Stage2_Hour2_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('79d6bb12-e6d7-4d26-9641-405a88fbf57a','Stage2_Hour2_3')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('88e2c40f-996b-4031-8550-ad59f192ffee','Stage2_Hour2_4')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('0f4249c5-aa59-4d16-8b94-9b6ecf8dcbf7','Stage2_Hour3_1')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('76bdffcb-fd80-486c-9295-9dd874e88512','Stage2_Hour3_2')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('8800c3d1-7213-4498-87df-a8cb857a4064','Stage2_Hour3_3')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('d623832d-ea49-42e3-a33d-223407cfc1ce','Stage2_Hour3_4')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('b78b613b-daee-4ae6-92b0-cb1951afffe8','Stage2_Hour4_1')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('19365146-6bcd-4c79-9ebe-b12395d3e34c','Stage2_Hour4_2')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('c1ee353f-80ff-4e7b-a1d0-6b0fbed2bea9','Stage2_Hour4_3')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('f5900299-989c-450c-8b7f-7ce8dc213210','Stage2_Hour4_4')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('c9f20f1c-bf9f-4dd4-a009-bac9efffd5b4','Stage2_Hour5_1')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('ada81344-4a25-4b9b-9f63-7602673194e4','Stage2_Hour5_2')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('5a58c6dc-f009-4adc-a650-5a435799bc84','Stage2_Hour5_3')");
//            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('081e0959-ce88-414d-b4be-f6c1593395aa','Stage2_Hour5_4')");

            // For SOS encounter
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('d1fb190a-9ebb-448f-8d61-dfeeb20fd931','Encounter Status')");
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
}
