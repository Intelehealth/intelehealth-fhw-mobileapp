package org.intelehealth.app.database;


import static org.intelehealth.app.database.AudioDbHelper.TABLE_NAME;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.model.HeartLungRecordModel;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;

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
            "slot_js_date TEXT," +
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
            "location_uuid TEXT," +
            "hw_uuid TEXT," +
            "reason TEXT," +
            "created_at TEXT," +
            "updated_at TEXT, " +
            "prev_slot_day TEXT, " +
            "prev_slot_date TEXT, " +
            "prev_slot_time TEXT," +
            "voided TEXT, " +
            "sync TEXT)";
    //newly added prev_slot_day, prev_slot_date,prev_slot_time

    /**
     * This will maintain all the types of notifications and its data.
     */
    public static final String CREATE_NOTIFICATIONS = "CREATE TABLE IF NOT EXISTS tbl_notifications (" +
            "uuid TEXT PRIMARY KEY," +
            "description TEXT," +
            "notification_type TEXT," +
            "obs_server_modified_date TEXT," +
            "isdeleted TEXT," +
            "UNIQUE(uuid) ON CONFLICT IGNORE)";

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
            "occupation TEXT," +
            "patient_photo TEXT," +
            "economic_status TEXT," +
            "education_status TEXT," +
            "caste TEXT," +
            "dead Text," +
            "guardian_name TEXT," +
            "guardian_type TEXT," +
            "contact_type TEXT," +
            "em_contact_name TEXT," +
            "em_contact_num TEXT," +
            "modified_date TEXT," +
            "tms_case_number TEXT," +
            "request_id TEXT," +
            "relative_phone_number TEXT," +
            "discipline TEXT," +
            "department TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false', " +
            "address3 TEXT," +
            "address6 TEXT," +
            "countyDistrict TEXT" +
            ")";

    public static final String CREATE_ATTRIB_MAIN = "CREATE TABLE IF NOT EXISTS tbl_patient_attribute (" +
            "uuid TEXT PRIMARY KEY," +
            "value TEXT," +
            "person_attribute_type_uuid TEXT ," +
            "patientuuid TEXT," +
            "modified_date TEXT," +
            "voided TEXT DEFAULT '0'," +
            "sync TEXT DEFAULT 'false'," +
            "UNIQUE(patientuuid, person_attribute_type_uuid)" + // added unique constraint to prevent duplicate insert of same attribute
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
            "sync TEXT DEFAULT 'false', " +
            "comments TEXT, " +
            "conceptsetuuid TEXT)";

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
            "role TEXT," +
            "useruuid TEXT," +
            "emailId TEXT," +
            "telephoneNumber TEXT," +
            "dateofbirth TEXT," +
            "gender TEXT," +
            "providerId TEXT," +
            "imagePath TEXT," +
            "middle_name TEXT," +
            "countryCode TEXT," +
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

    //provider attributes table - created primarily for storing the time spent by HW on the app.
    public static final String CREATE_PROVIDER_ATTRIBUTES =
            "CREATE TABLE IF NOT EXISTS tbl_provider_attribute (" +
                    "uuid TEXT PRIMARY KEY," +
                    "provider_uuid TEXT," +
                    "value TEXT," +
                    "provider_attribute_type_uuid TEXT," +
                    "voided TEXT," +
                    "sync TEXT)";

    public static final String FOLLOW_UP_NOTIFICATION_SCHEDULE =
            "CREATE TABLE IF NOT EXISTS tbl_follow_up_notification_schedule (" +
                    "id TEXT," +
                    "date_time TEXT," +
                    "duration TEXT," +
                    "value TEXT," +
                    "name TEXT," +
                    "openmrs_id TEXT," +
                    "patient_uuid TEXT," +
                    "visit_uuid TEXT," +
                    "request_code TEXT" +
                    ")";


    public static final String CREATE_HEART_LUNG_RECORDING =
            "CREATE TABLE IF NOT EXISTS tbl_follow_up_heart_lung_recoding (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_uuid TEXT," +
                    "visit_uuid TEXT," +
                    "encounter_uuid TEXT," +
                    "type TEXT," +
                    "position TEXT," +
                    "recordingStatus INTEGER," +
                    "audio_path TEXT," +
                    "result TEXT" +
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
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('b1a8fab4-d79f-44e6-8a90-2ac5e7ecd4d3','DIAGNOSTICS')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('be8e386b-ca22-447d-82a1-b80366e5f848','ATTRIBUTE_HOUSEHOLD_VALUE')");
            db.execSQL("INSERT OR REPLACE INTO tbl_uuid_dictionary (uuid,name) VALUES('20bc1535-fe95-46fb-9f45-37fb374ca851','ATTRIBUTE_BLOCK_SURVEY')");

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
        db.execSQL(CREATE_NOTIFICATIONS);
        db.execSQL(CREATE_PROVIDER_ATTRIBUTES);
        db.execSQL(FOLLOW_UP_NOTIFICATION_SCHEDULE);
        db.execSQL(CREATE_HEART_LUNG_RECORDING);
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

    private static InteleHealthDatabaseHelper sInstance;

    public static synchronized InteleHealthDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new InteleHealthDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public void saveEncounter(String visit, String encounter) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("encounter_uuid", encounter);
        db.update("tbl_follow_up_heart_lung_recoding",
                cv,
                "visit_uuid=?",
                new String[]{visit});
    }

    public String getEncounter(String visit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT encounter_uuid FROM tbl_obs WHERE visit_uuid=?",
                new String[]{visit}
        );
        if (c.moveToFirst()) {
            return c.getString(0);
        }
        return null;
    }

    // ✅ INSERT
    public void insertRecord(String patient, String visit, String encounter, String type,
                             String position, int recordingStatus, String audio, String result) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("patient_uuid", patient);
        values.put("visit_uuid", visit);
        values.put("encounter_uuid", encounter);
        values.put("type", type);
        values.put("position", position);
        values.put("recordingStatus", recordingStatus);
        values.put("audio_path", audio);
        values.put("result", result);
        db.insert("tbl_follow_up_heart_lung_recoding", null, values);
    }

    /* public List<HeartLungRecordModel> getAllHeartLungRecords(String visitUuid) {
         List<HeartLungRecordModel> list = new ArrayList<>();
         SQLiteDatabase db = getReadableDatabase();

         Cursor c = db.rawQuery(
                 "SELECT * FROM tbl_follow_up_heart_lung_recoding",
                 null
         );

         if (c.moveToFirst()) {
             do {
                 HeartLungRecordModel r = new HeartLungRecordModel();
                 r.id = String.valueOf(c.getInt(c.getColumnIndexOrThrow("id")));
                 r.patientUuid = c.getString(c.getColumnIndexOrThrow("patient_uuid"));
                 r.visitUuid = c.getString(c.getColumnIndexOrThrow("visit_uuid"));
                 r.encounterUuid = c.getString(c.getColumnIndexOrThrow("encounter_uuid"));
                 r.type = c.getString(c.getColumnIndexOrThrow("type"));
                 r.position = c.getString(c.getColumnIndexOrThrow("position"));
                 r.recordingStatus = c.getString(c.getColumnIndexOrThrow("recordingStatus"));
                 r.audioPath = c.getString(c.getColumnIndexOrThrow("audio_path"));
                 r.result = c.getString(c.getColumnIndexOrThrow("result"));
                 list.add(r);
             } while (c.moveToNext());
         }

         c.close();
         db.close();
         return list;
     }*/
// ✅ CORRECT
   /* public List<HeartLungRecordModel> getAllHeartLungRecords(String visitUuid) {
        List<HeartLungRecordModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                "tbl_follow_up_heart_lung_recoding",   // ✅ matches insertRecord()
                null,
                "visit_uuid = ?",
                new String[]{visitUuid},
                null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                HeartLungRecordModel model = new HeartLungRecordModel();
                model.type        = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                model.visitUuid   = cursor.getString(cursor.getColumnIndexOrThrow("visit_uuid"));
                model.position    = cursor.getString(cursor.getColumnIndexOrThrow("position"));
                model.patientUuid = cursor.getString(cursor.getColumnIndexOrThrow("patient_uuid"));
                model.audioPath   = cursor.getString(cursor.getColumnIndexOrThrow("audio_path"));
                model.result      = cursor.getString(cursor.getColumnIndexOrThrow("result"));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow("recordingStatus"));
                model.recordingStatus = String.valueOf(status);
                list.add(model);
            }
            cursor.close();
        }
        return list;
    }*/
    public List<HeartLungRecordModel> getAllHeartLungRecords(String visitUuid) {
        List<HeartLungRecordModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                "tbl_follow_up_heart_lung_recoding",
                null,
                "visit_uuid = ?",
                new String[]{visitUuid},
                null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                HeartLungRecordModel model = new HeartLungRecordModel();
                model.type            = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                model.visitUuid       = cursor.getString(cursor.getColumnIndexOrThrow("visit_uuid"));
                model.position        = cursor.getString(cursor.getColumnIndexOrThrow("position"));
                model.patientUuid     = cursor.getString(cursor.getColumnIndexOrThrow("patient_uuid"));
                model.audioPath       = cursor.getString(cursor.getColumnIndexOrThrow("audio_path"));
                model.result          = cursor.getString(cursor.getColumnIndexOrThrow("result"));

                // ✅ THIS LINE WAS MISSING — recordingStatus was never read!
                int status = cursor.getInt(cursor.getColumnIndexOrThrow("recordingStatus"));
                model.recordingStatus = String.valueOf(status);

                list.add(model);
            }
            cursor.close();
        }
        return list;
    }

    // UPDATE result after webhook
    public void updateResult(String visitUuid, String position, String result) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("result", result);
        db.update(
                "tbl_follow_up_heart_lung_recoding",   // table name ONLY
                cv,
                "visit_uuid=? AND position=?",
                new String[]{visitUuid, position}
        );
    }

    public boolean isRecordExists(String visitUuid, String position) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_NAME + " WHERE visit_uuid=? AND position=?",
                new String[]{visitUuid, position}
        );

        boolean exists = (cursor != null && cursor.moveToFirst());

        if (cursor != null) cursor.close();
        db.close();

        return exists;
    }

    // UPDATE
    public void updateRecord(String visitUuid,
                             String position,
                             String audioPath,
                             String result) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("audio_path", audioPath);
        values.put("result", result);

        db.update(TABLE_NAME,
                values,
                "visit_uuid=? AND position=?",
                new String[]{visitUuid, position});

        db.close();
    }


    public void insert(String tblFollowUpHeartLungRecoding, Object o, ContentValues values1) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(tblFollowUpHeartLungRecoding, null, values1);
    }
}
