package io.intelehealth.client.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.intelehealth.client.HelperMethods;
import io.intelehealth.client.IntelehealthApplication;
import io.intelehealth.client.R;
import io.intelehealth.client.SettingsActivity;
import io.intelehealth.client.db.DelayedJobQueueProvider;
import io.intelehealth.client.db.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.Obs;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.PatientImage;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.utils.NetworkConnection;

/**
 * Sends Identification data to OpenMRS and receives the OpenMRS ID of the newly-created patient
 */
public class ClientService extends IntentService {

    private static final String EXTRA_FAILED_ATTEMPTS = "io.intelehealth.client.EXTRA_FAILED_ATTEMPTS";
    private static final String EXTRA_LAST_DELAY = "io.intelehealth.client.EXTRA_LAST_DELAY";
    private static final int MAX_TRIES = 3;
    private static final int RETRY_DELAY = 30000;

    //For Upload Patient
    public static final int STATUS_PERSON_NOT_CREATED = 101;
    public static final int STATUS_PATIENT_NOT_CREATED = 102;

    //For Upload Visit
    public static final int STATUS_VISIT_NOT_CREATED = 101;
    public static final int STATUS_ENCOUNTER_NOT_CREATED = 102;
    public static final int STATUS_ENCOUNTER_NOTE_NOT_CREATED = 103;


    private static int requestCode = 0;

    public static final String LOG_TAG = ClientService.class.getSimpleName();
    public int mId = 1;
    public int numMessages;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    Integer statusCode = 0;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IntelehealthApplication.getAppContext());
    String location_name = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
    String location_uuid = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_UUID, null);
    String location_desc = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_DESCRIPTION, null);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ClientService(String name) {
        super(name);
    }

    public ClientService() {
        super("Intent Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        Boolean success = false;
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());

        if (NetworkConnection.isOnline(this)) {
            String serviceCall = intent.getStringExtra("serviceCall");

            switch (serviceCall) {
                case "patient":
                    String patientID = intent.getStringExtra("patientID");
                    String patientName = intent.getStringExtra("name");
                    Log.v(LOG_TAG, "Patient ID: " + patientID);
                    Log.v(LOG_TAG, "Patient Name: " + patientName);
                    createNotification("patient", patientName);
                    success = uploadPatient(patientID, intent);
                    if (success) endNotification(patientName, "patient");
                    else errorNotification();
                    break;
                case "visit":
                    patientID = intent.getStringExtra("patientID");
                    String visitID = intent.getStringExtra("visitID");
                    patientName = intent.getStringExtra("name");
                    Log.v(LOG_TAG, "Patient ID: " + patientID);
                    Log.v(LOG_TAG, "Visit ID: " + visitID);
                    Log.v(LOG_TAG, "Patient Name: " + patientName);
                    createNotification("visit", patientName);
                    success = uploadVisit(patientID, visitID, intent);
                    if (success) endNotification(patientName, "visit");
                    else errorNotification();
                    break;
                case "endVisit":
                    patientID = intent.getStringExtra("patientID");
                    String visitUUID = intent.getStringExtra("visitUUID");
                    patientName = intent.getStringExtra("name");
                    Log.v(LOG_TAG, "Patient ID: " + patientID);
                    Log.v(LOG_TAG, "Patient Name: " + patientName);
                    createNotification("download", patientName);
                    success = endVisit(patientID, visitUUID, intent);
                    if (success) endNotification(patientName, "visit");
                    else errorNotification();
                    break;
                default:
                    //something
                    break;
            }
        } else if (NetworkConnection.isConnecting(this)) {
            retryAfterDelay(intent, 1, 5000);
        } else {
            addJobToQueue(intent);
        }

    }

    public String serialize(String dataString) {
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
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

        Cursor patientCursor = db.query("patient", null, selection, args, null, null, null);

        Gson gson = new GsonBuilder().serializeNulls().create();
        Patient patient = new Patient();
        patient.setId(patientCursor.getString(0));
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

        String json = gson.toJson(patient);
        Log.d(LOG_TAG + "/Gson", json);

        return json;
    }

    public void createNotification(String type, String patientName) {
        String title = "";
        String text = "";

        switch (type) {
            case "patient":
                title = "Patient Data Upload";
                text = String.format("Uploading %s's data", patientName);
                break;
            case "visit":
                title = "Visit Data Upload";
                text = String.format("Uploading %s's visit data", patientName);
                break;
            case "download":

                title = "Visit Data Download";
                text = String.format("Downloading %s's visit data", patientName);
                break;
        }


        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_cloud_upload);
        // Sets an activity indicator for an operation of indeterminate length
        mBuilder.setProgress(0, 0, true);
        // Issues the notification
        mNotifyManager.notify(mId, mBuilder.build());
        numMessages = 0;
    }


    public void endNotification(String patientName, String type) {
        // mNotifyManager.cancel(mId);

        // When the loop is finished, updates the notification
        String text = String.format("%s's %s data upload complete.", patientName, type);
        mBuilder.setContentText(text)
                // Removes the progress bar
                .setProgress(0, 0, false);
        mNotifyManager.notify(mId, mBuilder.build());
    }

    public String sendData(String jsonString) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String serverAddress = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");

        HttpURLConnection urlConnection;
        DataOutputStream printout;
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader;
        InputStream inputStream;

        try {

            URL url = new URL(serverAddress);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(jsonString);
            printout.flush();
            printout.close();

            inputStream = urlConnection.getInputStream();
            if (inputStream == null) return null;


            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Appending the newline character helps with JSON debugging,
                // but will not interfere with JSON parsing.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) return null; // Stream was empty; no point in parsing.

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in sending data: ", e);
        }

        return buffer.toString(); // returns the openMrsId OR "Picture received" (if picture)
    }

    /**
     * Checks whether upload of patient and person data to the server is a success.
     *
     * @param patientID      Unique id of the patient
     * @param current_intent this intent
     * @return uploadDone
     */
    private boolean uploadPatient(String patientID, Intent current_intent) {

        String responseCode = null;
        boolean uploadDone = false;
        if (current_intent.hasExtra("status")) {
            int status = current_intent.getIntExtra("status", -1);
            if (status != -1) {
                switch (status) {
                    case STATUS_PERSON_NOT_CREATED: {
                        responseCode = uploadPersonData(patientID);
                        if (responseCode != null) {
                            current_intent.putExtra("status", STATUS_PATIENT_NOT_CREATED);
                            current_intent.putExtra("personResponse", responseCode);
                            uploadDone = uploadPatientData
                                    (patientID, responseCode);
                        } else {
                            current_intent.putExtra("status", STATUS_PERSON_NOT_CREATED);
                        }
                        break;
                    }
                    case STATUS_PATIENT_NOT_CREATED: {
                        responseCode = current_intent.getStringExtra("personResponse");
                        uploadDone = uploadPatientData(patientID, responseCode);
                        break;
                    }
                }
            }
        } else {
            responseCode = uploadPersonData(patientID);
            if (responseCode != null) {
                current_intent.putExtra("status", STATUS_PATIENT_NOT_CREATED);
                current_intent.putExtra("personResponse", responseCode);
                uploadDone = uploadPatientData
                        (patientID, responseCode);
            } else {
                current_intent.putExtra("status", STATUS_PERSON_NOT_CREATED);
            }
        }
        if (uploadDone == false) {
            retryAfterDelay(current_intent);
        } else if (current_intent.hasExtra("queueId")) {
            int queueId = current_intent.getIntExtra("queueId", -1);
            removeJobFromQueue(queueId);
        }

        return uploadDone;
    }

    /**
     * Stores person data locally then posts it to the OpenMRS server.
     *
     * @param patientID Unique id of the patient
     * @return responseString
     */
    private String uploadPersonData(String patientID) {

        Patient patient = new Patient();
        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {patientID};

        LocalRecordsDatabaseHelper mDbHelper;
        SQLiteDatabase db;
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        String table = "patient";
        String[] columnsToReturn = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province", "country",
                "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setFirstName(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
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
                                "{\"attributeType\":\"14d4f066-15f5-102d-96e4-000c29c2a5d7\", " +
                                "\"value\": \"%s\"}, " +
                                "{\"attributeType\":\"8d87236c-c2cc-11de-8d13-0010c6dffd0f\", " +
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
                        patient.getPhoneNumber(),
                        location_name,
                        patient.getAddress1(),
                        patient.getAddress2(),
                        patient.getCityVillage(),
                        patient.getStateProvince(),
                        patient.getCountry(),
                        patient.getPostalCode());

        Log.d(LOG_TAG, "Person String: " + personString);
        WebResponse responsePerson;
        responsePerson = HelperMethods.postCommand("person", personString, getApplicationContext());
        if (responsePerson != null && responsePerson.getResponseCode() != 201) {
            String newText = "Person was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(LOG_TAG, "Person posting was unsuccessful 1");
            return null;
        } else if (responsePerson == null) {
            Log.d(LOG_TAG, "Person posting was unsuccessful 1");
            return null;
        } else {
            String newText = "Person created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Intent uploadPersonPhoto = new Intent(this, PersonPhotoUploadService.class);
            uploadPersonPhoto.putExtra("person", responsePerson.getResponseString());
            uploadPersonPhoto.putExtra("patientID", patientID);
            uploadPersonPhoto.putExtra("name", patient.getFirstName() + patient.getLastName());
            return responsePerson.getResponseString();
        }
    }

    /**
     * Uploads Patient data on the OpenMRS server.
     *
     * @param patientID      Unique Id of the patient.
     * @param responseString Response JSON string
     * @return boolean value representing success or failure.
     */
    private boolean uploadPatientData(String patientID, String responseString) {
        String patientString =
                String.format("{\"person\":\"%s\", " +
                                "\"identifiers\":[{\"identifier\":\"%s\", " +
                                "\"identifierType\":\"05a29f94-c0ed-11e2-94be-8c13b969e334\", " +
                                "\"location\":\"%s\", " +
                                "\"preferred\":true}]}",
                        responseString,
                        patientID, location_uuid);

        Log.d(LOG_TAG, "Patient String: " + patientString);
        WebResponse responsePatient;
        responsePatient = HelperMethods.postCommand("patient", patientString, getApplicationContext());
        if (responsePatient != null && responsePatient.getResponseCode() != 201) {
            String newText = "Patient was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(LOG_TAG, "Patient posting was unsuccessful 2");
            return false;
        } else if (responsePatient == null) {
            Log.d(LOG_TAG, "Patient posting was unsuccessful 2");
            return false;
        } else {
            String newText = "Patient created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());

            ContentValues contentValuesOpenMRSID = new ContentValues();
            contentValuesOpenMRSID.put("openmrs_uuid", responsePatient.getResponseString());
            String selection = "_id = ?";
            String[] args = {patientID};

            db.update(
                    "patient",
                    contentValuesOpenMRSID,
                    selection,
                    args
            );
            return true;
        }

    }

    /**
     * Retrieves Patient medical details and vitals from local database for uploading to the OpenMRS server.
     *
     * @param patientID      Unique patient Id
     * @param visitID        Unique visit Id of the patient
     * @param current_intent this intent
     * @return uploadStatus
     */
    private boolean uploadVisit(String patientID, String visitID, Intent current_intent) {


        Patient patient = new Patient();
        Obs complaint = new Obs();
        Obs famHistory = new Obs();
        Obs patHistory = new Obs();
        String medHistory;
        Obs physFindings = new Obs();
        Obs height = new Obs();
        Obs weight = new Obs();
        Obs pulse = new Obs();
        Obs bpSys = new Obs();
        Obs bpDias = new Obs();
        Obs temperature = new Obs();
        Obs spO2 = new Obs();

        LocalRecordsDatabaseHelper mDbHelper;
        SQLiteDatabase db;
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();


        String[] columns = {"value", " concept_id"};
        String orderBy = "visit_id";

        try {
            String famHistSelection = "patient_id = ? AND concept_id = ?";
            String[] famHistArgs = {patientID, "163188"};
            Cursor famHistCursor = db.query("obs", columns, famHistSelection, famHistArgs, null, null, orderBy);
            famHistCursor.moveToLast();
            String famHistText = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistory.setValue(famHistText);
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            famHistory.setValue(""); // if family history does not exist
        }

        try {
            String medHistSelection = "patient_id = ? AND concept_id = ?";
            String[] medHistArgs = {patientID, "163187"};
            Cursor medHistCursor = db.query("obs", columns, medHistSelection, medHistArgs, null, null, orderBy);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            patHistory.setValue(medHistText);
            if (!medHistText.isEmpty()) {
                medHistory = patHistory.getValue();
                medHistory = medHistory.replace("\"", "");
                medHistory = medHistory.replace("\n", "");
                do {
                    medHistory = medHistory.replace("  ", "");
                } while (medHistory.contains("  "));
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            patHistory.setValue(""); // if medical history does not exist
        }

        String visitSelection = "patient_id = ? AND visit_id = ?";
        String[] visitArgs = {patientID, visitID};
        Cursor visitCursor = db.query("obs", columns, visitSelection, visitArgs, null, null, orderBy);
        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndex("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                switch (dbConceptID) {
                    case 163186: //Current Complaint
                        complaint.setValue(dbValue);
                        break;
                    case 163189: //Physical Examination
                        physFindings.setValue(dbValue);
                        break;
                    case 5090: //Height
                        height.setValue(dbValue);
                        break;
                    case 5089: //Weight
                        weight.setValue(dbValue);
                        break;
                    case 5087: //Pulse
                        pulse.setValue(dbValue);
                        break;
                    case 5085: //Systolic BP
                        bpSys.setValue(dbValue);
                        break;
                    case 5086: //Diastolic BP
                        bpDias.setValue(dbValue);
                        break;
                    case 163202: //Temperature
                        temperature.setValue(dbValue);
                        break;
                    case 5092: //SpO2
                        spO2.setValue(dbValue);
                        break;
                    default:
                        break;
                }
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

        String[] columnsToReturn = {"start_datetime"};
        String visitIDorderBy = "start_datetime";
        String visitIDSelection = "_id = ?";
        String[] visitIDArgs = {visitID};
        final Cursor visitIDCursor = db.query("visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("start_datetime"));
        visitIDCursor.close();

        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {patientID};
        String[] oMRSCol = {"openmrs_uuid", "sdw", "occupation"};
        final Cursor idCursor = db.query("patient", oMRSCol, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_uuid")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        boolean uploadStatus = false;
        Integer statusCode = STATUS_VISIT_NOT_CREATED;
        if (current_intent.hasExtra("status")) {
            statusCode = current_intent.getIntExtra("status", -1);
            if (statusCode > 0) {
                String visitUUID;

                if (statusCode == STATUS_VISIT_NOT_CREATED) {
                    visitUUID =
                            uploadVisitData(patient, startDateTime);
                    ContentValues contentValuesVisit = new ContentValues();
                    contentValuesVisit.put("openmrs_visit_uuid", visitUUID);
                    String visitUpdateSelection = "start_datetime = ?";
                    String[] visitUpdateArgs = {startDateTime};

                    db.update(
                            "visit",
                            contentValuesVisit,
                            visitUpdateSelection,
                            visitUpdateArgs
                    );
                } else visitUUID = current_intent.getStringExtra("visitResponse");


                if (visitUUID != null) {
                    current_intent.putExtra("visitResponse", visitUUID);
                    if (statusCode == STATUS_ENCOUNTER_NOT_CREATED) {
                        boolean encounter_vitals = uploadEncounterVitals(visitUUID, patient, startDateTime,
                                temperature, weight, height, pulse, bpSys, bpDias, spO2);

                        if (encounter_vitals) statusCode = STATUS_ENCOUNTER_NOTE_NOT_CREATED;

                        boolean encounter_notes = uploadEncounterNotes(visitUUID, patient, startDateTime,
                                patHistory, famHistory, complaint, physFindings);

                        if (encounter_notes && encounter_vitals) uploadStatus = true;
                    } else if (statusCode == STATUS_ENCOUNTER_NOTE_NOT_CREATED) {
                        boolean encounter_notes = uploadEncounterNotes(visitUUID, patient, startDateTime,
                                patHistory, famHistory, complaint, physFindings);
                        uploadStatus = encounter_notes;
                    }
                }

                current_intent.putExtra("status", statusCode);

            }
        } else {
            String visitUUID;
            visitUUID = uploadVisitData(patient, startDateTime);

            ContentValues contentValuesVisit = new ContentValues();
            contentValuesVisit.put("openmrs_visit_uuid", visitUUID);
            String visitUpdateSelection = "start_datetime = ?";
            String[] visitUpdateArgs = {startDateTime};

            db.update(
                    "visit",
                    contentValuesVisit,
                    visitUpdateSelection,
                    visitUpdateArgs
            );


            if (visitUUID != null) {
                current_intent.putExtra("visitResponse", visitUUID);
                statusCode = STATUS_ENCOUNTER_NOT_CREATED;
                boolean encounter_vitals = uploadEncounterVitals(visitUUID, patient, startDateTime,
                        temperature, weight, height, pulse, bpSys, bpDias, spO2);
                if (encounter_vitals) statusCode = STATUS_ENCOUNTER_NOTE_NOT_CREATED;
                boolean encounter_notes = uploadEncounterNotes(visitUUID, patient, startDateTime,
                        patHistory, famHistory, complaint, physFindings);
                if (encounter_notes && encounter_vitals) uploadStatus = true;
            }

            current_intent.putExtra("status", statusCode);
        }
        if (!uploadStatus) retryAfterDelay(current_intent);
        else if (current_intent.hasExtra("queueId")) {
            int queueId = current_intent.getIntExtra("queueId", -1);
            removeJobFromQueue(queueId);
        }
        return uploadStatus;

    }

    /**
     * Uploads visit details to the OpenMRS server.
     *
     * @param patient       {@link Patient}
     * @param startDateTime Start datetime in string
     * @return responseVisit
     */
    private String uploadVisitData(Patient patient, String startDateTime) {


        //TODO: Location UUID needs to be found before doing these
        String visitString =
                String.format("{\"startDatetime\":\"%s\"," +
                                "\"visitType\":\"Telemedicine\"," +
                                "\"patient\":\"%s\"," +
                                "\"location\":\"%s\"}",
                        startDateTime, patient.getOpenmrsId(), location_uuid);
        Log.d(LOG_TAG, "Visit String: " + visitString);
        WebResponse responseVisit;
        responseVisit = HelperMethods.postCommand("visit", visitString, getApplicationContext());
        if (responseVisit != null && responseVisit.getResponseCode() != 201) {
            String newText = "Visit was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(LOG_TAG, "Visit posting was unsuccessful");
        } else {
            String newText = "Visit created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
        }

        return responseVisit.getResponseString();

    }

    /**
     * Upload the vitals of the patient to the OpenMRS server.
     *
     * @param visitUUID
     * @param patient
     * @param startDateTime
     * @param temperature
     * @param weight
     * @param height
     * @param pulse
     * @param bpSys
     * @param bpDias
     * @param spO2
     * @return boolean value representing success or failure.
     */
    private boolean uploadEncounterVitals(String visitUUID, Patient patient, String startDateTime,
                                          Obs temperature, Obs weight, Obs height,
                                          Obs pulse, Obs bpSys, Obs bpDias, Obs spO2) {
        //---------------------
        Double fTemp = Double.parseDouble(temperature.getValue());
        Double cTemp = (fTemp - 32) * (5 / 9);
        String tempString = String.valueOf(cTemp);

        String vitalsString =
                String.format("{" +
                                "\"encounterDatetime\":\"%s\"," +
                                "\"patient\":\"%s\"," +
                                "\"encounterType\":\"VITALS\"," +
                                " \"visit\":\"%s\"," +
                                "\"obs\":[" +
                                "{\"concept\":\"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"value\":\"%s\"}," + //Weight
                                "{\"concept\":\"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}, " + //Height
                                "{\"concept\":\"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //Temperature
                                "{\"concept\":\"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //Pulse
                                "{\"concept\":\"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //BpSYS
                                "{\"concept\":\"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //BpDias
                                "{\"concept\":\"5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"value\":\"%s\"}]," + //Sp02
                                "\"location\":\"%s\"}",
                        startDateTime,
                        patient.getOpenmrsId(),
                        visitUUID,
//                            openMRSUUID,
                        weight.getValue(),
                        height.getValue(),
                        tempString,
                        pulse.getValue(),
                        bpSys.getValue(),
                        bpDias.getValue(),
                        spO2.getValue(),
                        location_uuid
                );
        Log.d(LOG_TAG, "Vitals Encounter String: " + vitalsString);
        WebResponse responseVitals;
        responseVitals = HelperMethods.postCommand("encounter", vitalsString, getApplicationContext());
        if (responseVitals != null && responseVitals.getResponseCode() != 201) {
            String newText = "Encounter was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(LOG_TAG, "Encounter posting was unsuccessful");
            return false;
        } else if (responseVitals == null) {
            Log.d(LOG_TAG, "Encounter posting was unsuccessful");
            return false;
        } else {
            String newText = "Encounter created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            return true;
        }
        //---------------------
    }

    private boolean uploadEncounterNotes(String visitUUID, Patient patient, String startDateTime,
                                         Obs patHistory, Obs famHistory, Obs complaint, Obs physFindings) {
        if (patHistory.getValue().isEmpty() || patHistory.getValue().equals("")) {
            patHistory.setValue("None");
        }
        if (famHistory.getValue().isEmpty() || famHistory.getValue().equals("")) {
            famHistory.setValue("None");
        }

        String noteString =
                String.format("{" +
                                "\"encounterDatetime\":\"%s\"," +
                                " \"patient\":\"%s\"," +
                                "\"encounterType\":\"ADULTINITIAL\"," +
                                "\"visit\":\"%s\"," +
                                "\"obs\":[" +
                                "{\"concept\":\"35c3afdd-bb96-4b61-afb9-22a5fc2d088e\", \"value\":\"%s\"}," + //son wife daughter
                                "{\"concept\":\"5fe2ef6f-bbf7-45df-a6ea-a284aee82ddc\",\"value\":\"%s\"}, " + //occupation
                                "{\"concept\":\"62bff84b-795a-45ad-aae1-80e7f5163a82\",\"value\":\"%s\"}," + //medical history
                                "{\"concept\":\"d63ae965-47fb-40e8-8f08-1f46a8a60b2b\",\"value\":\"%s\"}," + //family history
                                "{\"concept\":\"3edb0e09-9135-481e-b8f0-07a26fa9a5ce\",\"value\":\"%s\"}," + //current complaint
                                "{\"concept\":\"e1761e85-9b50-48ae-8c4d-e6b7eeeba084\",\"value\":\"%s\"}]," + //physical exam
                                "\"location\":\"%s\"}",

                        startDateTime,
                        patient.getOpenmrsId(),
                        visitUUID,
//                            openMRSUUID,
                        patient.getSdw(),
                        patient.getOccupation(),
                        //TODO: add logic to remove SDW and occupation when they are empty
                        patHistory.getValue(),
                        famHistory.getValue(),
                        complaint.getValue(),
                        physFindings.getValue(),
                        location_uuid
                );
        Log.d(LOG_TAG, "Notes Encounter String: " + noteString);
        WebResponse responseNotes;
        responseNotes = HelperMethods.postCommand("encounter", noteString, getApplicationContext());
        if (responseNotes != null && responseNotes.getResponseCode() != 201) {
            String newText = "Notes Encounter was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(LOG_TAG, "Notes Encounter posting was unsuccessful");
            return false;
        } else if (responseNotes == null) {
            Log.d(LOG_TAG, "Notes Encounter posting was unsuccessful");
            return false;
        } else {
            String newText = "Notes created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            return true;
        }
    }

    /**
     * Ends Patient visit session.
     *
     * @param patientIDs     Unique patient Id
     * @param visitUUID      visit UUID
     * @param current_intent this intent
     * @return boolean value representing success or failure.
     */
    private boolean endVisit(String patientIDs, String visitUUID, Intent current_intent) {

        String urlModifier = "visit/" + visitUUID;

        SimpleDateFormat endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date rightNow = new Date();
        String endDateTime = endDate.format(rightNow);


        String endString =
                String.format("{\"stopDatetime\":\"%s\"," +
                                "\"visitType\":\"a86ac96e-2e07-47a7-8e72-8216a1a75bfd\"}",
                        endDateTime);

        Log.d("End String", endString);

        WebResponse endResponse = HelperMethods.postCommand(urlModifier, endString, getApplicationContext());

        if (endResponse != null && endResponse.getResponseCode() != 200) {
            String newText = "Visit ending was unsuccessful. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(LOG_TAG, "Visit ending was unsuccessful");
            retryAfterDelay(current_intent);
            return false;
        } else if (endResponse == null) {
            Log.d(LOG_TAG, "Visit ending was unsuccessful");
            return false;
        } else {

            String newText = "Visit ended successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            if (current_intent.hasExtra("queueId")) {
                int queueId = current_intent.getIntExtra("queueId", -1);
                removeJobFromQueue(queueId);
            }
            return true;
        }


    }

    public void errorNotification() {
        // TODO: determine error behavior
    }

    private void retryAfterDelay(Intent intent) {
        retryAfterDelay(intent, MAX_TRIES, RETRY_DELAY);
    }

    private void retryAfterDelay(Intent intent, int max_retries, int retry_delay) {
        // Get the number of previously failed attempts, and add one.
        int failedAttempts = intent.getIntExtra(EXTRA_FAILED_ATTEMPTS, 0) + 1;
        // if we have failed less than the max retries, reschedule the intent
        Log.i(LOG_TAG, "Scheduling retry" + failedAttempts + "/" + max_retries);
        if (failedAttempts <= max_retries) {
            Log.i(LOG_TAG, "Retrying" + failedAttempts + "/" + max_retries);
            // calculate the next delay
            int lastDelay = intent.getIntExtra(EXTRA_LAST_DELAY, 0);
            int thisDelay;
            if (lastDelay == 0) {
                thisDelay = retry_delay;
            } else {
                thisDelay = lastDelay * 2;
            }
            // update the intent with the latest retry info
            intent.putExtra(EXTRA_FAILED_ATTEMPTS, failedAttempts);
            intent.putExtra(EXTRA_LAST_DELAY, thisDelay);

            // get the alarm manager
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int requestCode = incrementAndGetRequestCode();
            // make the pending intent
            PendingIntent pendingIntent = PendingIntent
                    .getService(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // schedule the intent for future delivery
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + thisDelay, pendingIntent);
        } else {
            addJobToQueue(intent);
        }


    }

    private static int incrementAndGetRequestCode() {
        requestCode = requestCode++;
        return requestCode;
    }

    private void addJobToQueue(Intent intent) {
        if (!intent.hasExtra("queueId")) {
            Log.d(LOG_TAG, "Adding to Queue");

            String serviceCall = intent.getStringExtra("serviceCall");

            // Add a new Delayed Job record
            ContentValues values = new ContentValues();
            values.put(DelayedJobQueueProvider.JOB_TYPE, serviceCall);
            values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
            values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, requestCode);
            values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));
            values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getStringExtra("patientID"));

            switch (serviceCall) {
                case "patient": {
                    values.put(DelayedJobQueueProvider.STATUS, intent.getIntExtra("status", -1));
                    values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("personResponse"));
                    break;
                }
                case "visit": {
                    values.put(DelayedJobQueueProvider.VISIT_ID, intent.getStringExtra("visitID"));
                    values.put(DelayedJobQueueProvider.STATUS, intent.getIntExtra("status", -1));
                    values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("visitResponse"));
                    break;
                }
                case "endVisit": {
                    values.put(DelayedJobQueueProvider.VISIT_UUID, intent.getStringExtra("visitUUID"));
                    break;
                }
                default:
                    Log.e(LOG_TAG, "Does not match any Job Type");
            }


            Uri uri = getContentResolver().insert(
                    DelayedJobQueueProvider.CONTENT_URI, values);

            Toast.makeText(getBaseContext(),
                    uri.toString(), Toast.LENGTH_LONG).show();
        } else {
            Log.i(LOG_TAG, "Queue id : " + intent.getIntExtra("queueId", -1));
            String serviceCall = intent.getStringExtra("serviceCall");
            ContentValues values = new ContentValues();
            switch (serviceCall) {
                case "patient": {
                    values.put(DelayedJobQueueProvider.STATUS, intent.getIntExtra("status", -1));
                    values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("personResponse"));
                    break;
                }
                case "visit": {
                    values.put(DelayedJobQueueProvider.STATUS, intent.getIntExtra("status", -1));
                    values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("visitResponse"));
                    break;
                }
            }
            int queueId = intent.getIntExtra("queueId", -1);
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = getContentResolver().update(uri, values, null, null);
            if (result > 0) {
                Log.i(LOG_TAG, result + " row deleted");
            } else {
                Log.e(LOG_TAG, "Database error while deleting row!");
            }
        }
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
}