package io.intelehealth.client.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.UuidDictionary;

import static io.intelehealth.client.services.ClientService.STATUS_SYNC_IN_PROGRESS;
import static io.intelehealth.client.services.ClientService.STATUS_SYNC_STOPPED;

/**
 * Created by harshish on 28/6/17.
 */

public class PatientUpdateService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PatientUpdateService(String name) {
        super(name);
    }

    public PatientUpdateService() {
        super(TAG);
    }

    private static final String TAG = PatientUpdateService.class.getSimpleName();

    private static final String LOG_TAG = PatientUpdateService.class.getSimpleName();
    NotificationManager mNotifyManager;
    public int mId = 3;
    NotificationCompat.Builder mBuilder;
    private String patientId, patientName;
    public int numMessages;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    String location_uuid;

    int queueId;


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        patientId = intent.getStringExtra("patientID");
        patientName = intent.getStringExtra("name");

        Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IntelehealthApplication.getAppContext());
        location_uuid = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_UUID, null);

        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        if (!intent.hasExtra("queueId")) {
            int id = addJobToQueue(intent);
            intent.putExtra("queueId", id);
        }

        queueId = intent.getIntExtra("queueId", -1);

        queueSyncStart(queueId);

        String response = uploadPersonData(patientId);

        if (response == null || response.isEmpty()) {
            queueSyncStop(queueId);

        } else {
            queueSyncStop(queueId);
            removeJobFromQueue(queueId);
        }
    }


    private int addJobToQueue(Intent intent) {
        Log.d(LOG_TAG, "Adding to Queue");
        // Add a new Delayed Job record
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.JOB_TYPE, "patientUpdate");
        values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
        values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, 0);
        values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getStringExtra("patientID"));
        values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));

        Uri uri = getContentResolver().insert(
                DelayedJobQueueProvider.CONTENT_URI, values);


        Toast.makeText(getBaseContext(),
                uri.toString(), Toast.LENGTH_LONG).show();

        return Integer.valueOf(uri.getLastPathSegment());
    }

    private void removeJobFromQueue(int queueId) {
        Log.d(LOG_TAG, "Removing from Queue");
        if (queueId > -1) {
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = getContentResolver().delete(uri, null, null);
            if (result > 0) {
                Log.i(LOG_TAG, result + " row deleted");
            } else {
                Log.e(LOG_TAG, "Database error while deleting row!");
            }
        }

    }

    private void queueSyncStart(int queueId) {
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.SYNC_STATUS, STATUS_SYNC_IN_PROGRESS);
        String url = DelayedJobQueueProvider.URL + "/" + queueId;
        Uri uri = Uri.parse(url);
        getContentResolver().update(uri, values, null, null);
    }

    private void queueSyncStop(int queueId) {
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.SYNC_STATUS, STATUS_SYNC_STOPPED);
        String url = DelayedJobQueueProvider.URL + "/" + queueId;
        Uri uri = Uri.parse(url);
        int result = getContentResolver().update(uri, values, null, null);
    }

    @Override
    public void onDestroy() {
        queueSyncStop(queueId);
        super.onDestroy();
    }

    private String uploadPersonData(String patientID) {

        Patient patient = new Patient();
        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {patientID};

        String table = "patient";
        String[] columnsToReturn = {"openmrs_uuid", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province", "country",
                "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo", "economic_status",
                "education_status", "caste"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setFirstName(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_uuid")));
                patient.setMiddleName(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient.setLastName(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient.setDateOfBirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient.setCityVillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient.setStateProvince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient.setPostalCode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                patient.setEconomic_status(idCursor.getString(idCursor.getColumnIndexOrThrow("economic_status")));
                patient.setEducation_level(idCursor.getString(idCursor.getColumnIndexOrThrow("education_status")));
                patient.setCaste(idCursor.getString(idCursor.getColumnIndexOrThrow("caste")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();


        String personString =
                String.format("{\"gender\":\"%s\", " +
                                "\"names\":[" +
                                "{\"givenName\":\"%s\", " +
                                "\"middleName\":\"%s\", " +
                                "\"familyName\":\"%s\"}], " +
                                "\"birthdate\":\"%s\", " +
                                "\"attributes\":[" +
                                "{\"attributeType\":\"%s\", " +
                                "\"value\": \"%s\"}, " +
                                "{\"attributeType\":\"%s\", " +
                                "\"value\": \"%s\"}, " +
                                "{\"attributeType\":\"%s\", " +
                                "\"value\": \"%s\"}, " +
                                "{\"attributeType\":\"%s\", " +
                                "\"value\": \"%s\"}, " +
                                "{\"attributeType\":\"%s\", " +
                                "\"value\": \"%s\"}, " +
                                "{\"attributeType\":\"%s\", " +
                                "\"value\": \"%s\"}], " + //TODO: Change this attribute to the name of the clinic as listed in OpenMRS
                                "\"addresses\":[" +
                                "{\"address1\":\"%s\", " +
                                "\"address2\":\"%s\"," +
                                "\"cityVillage\":\"%s\"," +
                                "\"stateProvince\":\"%s\"," +
                                "\"country\":\"%s\"," +
                                "\"postalCode\":\"%s\"}]}",
                        patient.getGender(),
                        patient.getFirstName(),
                        patient.getMiddleName(),
                        patient.getLastName(),
                        patient.getDateOfBirth(),
                        UuidDictionary.ATTRIBUTE_PHONE_NUMBER,
                        patient.getPhoneNumber(),
                        UuidDictionary.ATTRIBUTE_CASTE,
                        patient.getCaste(),
                        UuidDictionary.ATTRIBUTE_ECONOMIC_STATUS,
                        patient.getEconomic_status(),
                        UuidDictionary.ATTRIBUTE_EDUCATION_LEVEL,
                        patient.getEducation_level(),
                        UuidDictionary.ATTRIBUTE_SON_WIFE_DAUGHTER,
                        patient.getSdw(),
                        UuidDictionary.ATTRIBUTE_OCCUPATION,
                        patient.getOccupation(),
                        patient.getAddress1(),
                        patient.getAddress2(),
                        patient.getCityVillage(),
                        patient.getStateProvince(),
                        patient.getCountry(),
                        patient.getPostalCode());

        Log.d(TAG, "Person String: " + personString);

        if (patient.getOpenmrsId() != null) {
            WebResponse responsePerson;
            responsePerson = HelperMethods.postCommand("person/" + patient.getOpenmrsId(), personString, getApplicationContext());
            if (responsePerson != null && responsePerson.getResponseCode() != 200) {
                String newText = "Person not updated. Please check your connection.";
                mBuilder.setContentText(newText).setNumber(++numMessages);
                mBuilder.setSmallIcon(R.drawable.ic_cloud_upload);
                mNotifyManager.notify(mId, mBuilder.build());
                Log.d(TAG, "Person update was unsuccessful");
                return null;
            } else if (responsePerson == null) {
                Log.d(TAG, "Person update was unsuccessful");
                return null;
            } else {
                String newText = "Person updated successfully.";
                mBuilder.setContentText(newText).setNumber(++numMessages);
                mBuilder.setSmallIcon(R.drawable.ic_cloud_upload);
                mNotifyManager.notify(mId, mBuilder.build());
                return responsePerson.getResponseString();
            }
        } else {
            return null;
        }
    }



}
