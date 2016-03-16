package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Sends Identification data to OpenMRS and receives the OpenMRS ID of the newly-created patient
 */
public class IdService extends IntentService {

    public static final String LOG_TAG = "IdService";

    KnowledgeDatabaseHelper mDbHelper = new KnowledgeDatabaseHelper(this);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public IdService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String dataString = intent.getDataString(); // The dataString is the _id of the patient to send
        String jsonToSend = serialize(dataString);

    }

    // TODO: test this code segement
    public String serialize(String dataString) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String[] columnsToReturn = {
                "_id",
                "openmrs_id",
                "first_name",
                "middle_name",
                "last_name",
                "date_of_birth", // ISO 8601
                "phone_number",
                "address1",
                "address2",
                "city_village",
                "state_province",
                "postal_code",
                "country", // ISO 3166-1 alpha-2
                "gender",
                "patient_identifier1",
                "patient_identifier2",
                "patient_identifier3"
        };

        String selection = "_id = ?";
        String[] args = new String[1];
        args[0] = dataString;

        Cursor patientCursor = db.query("patient", columnsToReturn, selection, args, null, null, null);

        Gson gson = new GsonBuilder().serializeNulls().create();
        Patient patient = new Patient();
        patient.setId(Integer.parseInt(patientCursor.getString(0)));
        patient.setFirstName(patientCursor.getString(1));
        patient.setMiddleName(patientCursor.getString(2));
        patient.setLastName(patientCursor.getString(3));
        patient.setDateOfBirth(patientCursor.getString(4));
        patient.setPhoneNumber(patientCursor.getString(5));
        patient.setAddress1(patientCursor.getString(6));
        patient.setAddress2(patientCursor.getString(7));
        patient.setCityVillage(patientCursor.getString(8));
        patient.setStateProvince(patientCursor.getString(9));
        patient.setCountry(patientCursor.getString(10));
        patient.setGender(patientCursor.getString(11));
        patient.setPatientIdentifier1(patientCursor.getString(12));
        patient.setPatientIdentifier2(patientCursor.getString(13));
        patient.setPatientIdentifier3(patientCursor.getString(14));

        String json = gson.toJson(patient);
        Log.d(LOG_TAG + "/Gson", json);

        return json;
    }
}
