package io.intelehealth.client.services;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.models.Identifier;
import io.intelehealth.client.objects.Obs;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.utilities.ConceptId;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.UuidDictionary;

/**
 * Sends Identification data to OpenMRS and receives the OpenMRS ID of the newly-created patient
 */
public class ClientService extends IntentService {

    private static final String EXTRA_FAILED_ATTEMPTS = "io.intelehealth.client.EXTRA_FAILED_ATTEMPTS";
    private static final String EXTRA_LAST_DELAY = "io.intelehealth.client.EXTRA_LAST_DELAY";
    private static final int MAX_TRIES = 1;
    private static final int RETRY_DELAY = 5000;

    //For Upload Patient
    public static final int STATUS_PERSON_NOT_CREATED = 101;
    public static final int STATUS_PATIENT_NOT_CREATED = 102;


    //For Upload Visit
    public static final int STATUS_VISIT_NOT_CREATED = 301;
    public static final int STATUS_ENCOUNTER_NOT_CREATED = 302;
    public static final int STATUS_ENCOUNTER_NOTE_NOT_CREATED = 303;


    //For Sync Status
    public static final int STATUS_SYNC_STOPPED = 0;
    public static final int STATUS_SYNC_IN_PROGRESS = 1;

    private static int requestCode = 0;

    public static final String TAG = ClientService.class.getSimpleName();
    public int mId = 1;
    public int numMessages;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    Integer queueId;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    Integer statusCode = 0;

    Integer patientID;
    String patientName;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IntelehealthApplication.getAppContext());
    String location_name = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
    String location_uuid = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_UUID, null);
    String provider_uuid = prefs.getString("providerid", null);
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
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        Boolean success = false;
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        if (NetworkConnection.isOnline(this)) {
            String serviceCall = intent.getStringExtra("serviceCall");

            if (!intent.hasExtra("queueId")) {
                int id = addJobToQueue(intent);
                intent.putExtra("queueId", id);
            }

            Log.d(TAG, "Queue id: " + intent.getIntExtra("queueId", -1));
            queueId = intent.getIntExtra("queueId", -1);

            patientID = intent.getIntExtra("patientID", -1);
            patientName = intent.getStringExtra("name");
            Log.v(TAG, "Patient ID: " + patientID);
            Log.v(TAG, "Patient Name: " + patientName);
            switch (serviceCall) {
                case "patient": {
                    queueSyncStart(queueId);
                    createNotification("patient", patientName);
                    success = uploadPatient(patientID, intent);
                    if (success) endNotification(patientName, "patient");
                    else {
                        errorNotification(patientName, "patient");
                        queueSyncStop(queueId);
                    }
                    break;
                }
                case "visit": {
                    queueSyncStart(queueId);
                    String visitID = intent.getStringExtra("visitID");
                    Log.v(TAG, "Visit ID: " + visitID);
                    createNotification("visit", patientName);
                    success = uploadVisit(patientID, visitID, intent);
                    if (success) {
                        endNotification(patientName, "visit");
                    } else {
                        errorNotification(patientName, "visit");
                        queueSyncStop(queueId);
                    }
                    break;
                }
                case "endVisit": {
                    queueSyncStart(queueId);
                    String visitUUID = intent.getStringExtra("visitUUID");
                    createNotification("download", patientName);
                    success = endVisit(patientID, visitUUID, intent);
                    if (success) endNotification(patientName, "visit");
                    else {
                        errorNotification(patientName, "end visit");
                        queueSyncStop(queueId);
                    }
                    break;
                }
                case "survey": {
                    queueSyncStart(queueId);
                    Log.v(TAG, "Exit Survey uploading");
                    createNotification("survey", patientName);
                    String visitID = intent.getStringExtra("visitID");
                    success = uploadSurvey(patientID, visitID, intent);
                    if (success) endNotification(patientName, "survey");
                    else {
                        errorNotification(patientName, "survey");
                        queueSyncStop(queueId);
                    }
                    break;

                }
                default:
                    //something
                    break;
            }
        } else {
            addJobToQueue(intent);
        }

    }

    public String serialize(String dataString) {

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
        patient.setId(patientCursor.getInt(0));
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

        patientCursor.close();

        String json = gson.toJson(patient);
        Log.d(TAG + "/Gson", json);

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
            case "survey":
                title = "Exit Survey Upload";
                text = "Uploading survey data";
        }


        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_cloud_upload_v);
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
        final String serverAddress = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL_REST, "");

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
            Log.e(TAG, "Error in sending data: ", e);
        }

        return buffer.toString(); // returns the openMrsId OR "Picture received" (if picture)
    }

    /**
     * Checks whether upload of patient and person data to the server is a success.
     *
     * @param patientID      Unique id of the patient
     * @param current_intent this intent
     * @return uploadDone
     * @link https://wiki.openmrs.org/display/docs/Create+Patient
     */
    private boolean uploadPatient(Integer patientID, Intent current_intent) {

        String responseCode = null;
        String uploadDone = null;
        if (current_intent.hasExtra("status")) {
            int status = current_intent.getIntExtra("status", -1);
            if (status != -1) {
                switch (status) {
                    case STATUS_PERSON_NOT_CREATED: {
                        responseCode = uploadPersonData(patientID);
                        if (responseCode != null) {
                            if (!responseCode.isEmpty()) {
                                current_intent.putExtra("status", STATUS_PATIENT_NOT_CREATED);

                                current_intent.putExtra("status", STATUS_PATIENT_NOT_CREATED);
                                current_intent.putExtra("personResponse", responseCode);
                                uploadDone = uploadPatientData
                                        (patientID, responseCode);
                            }

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
        if (uploadDone == null) {
            retryAfterDelay(current_intent);
        } else if (current_intent.hasExtra("queueId")) {
            Intent uploadPersonPhoto = new Intent(this, PersonPhotoUploadService.class);
            uploadPersonPhoto.putExtra("patientID", patientID);
            uploadPersonPhoto.putExtra("patientUUID", uploadDone);
            uploadPersonPhoto.putExtra("name", patientName);
            Log.i(TAG, "uploadPatient: Starting Service");
            startService(uploadPersonPhoto);
            int queueId = current_intent.getIntExtra("queueId", -1);
            removeJobFromQueue(queueId);
        }

        if (uploadDone != null) return true;
        else return false;
    }

    /**
     * Stores person data locally then posts it to the OpenMRS server.
     *
     * @param patientID Unique id of the patient
     * @return responseString
     */
    private String uploadPersonData(Integer patientID) {

        Patient patient = new Patient();
        String patientSelection = "_id = ?";
        String[] patientArgs = {String.valueOf(patientID)};

        String table = "patient";
        String[] columnsToReturn = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province", "country",
                "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo", "economic_status",
                "education_status", "caste"};
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

        String[] arrOfStr=personString.split("birthdate",2);
        String othersting=arrOfStr[0];
        String convertDate=arrOfStr[1];
        String[] substring=convertDate.split(",",2);
        String convertDate1=substring[0];
        String othersting1=substring[1];
        String []substring1=convertDate1.split(":",2);
        String convert3=substring1[0];
        String date=substring1[1];
      personString=convertStringToDate(date)+othersting+othersting1;

        WebResponse responsePerson;
        responsePerson = HelperMethods.postCommand("person", personString, getApplicationContext());
        if (responsePerson != null && responsePerson.getResponseCode() != 201) {
            String newText = "Person was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Person posting was unsuccessful 1");
            return null;
        } else if (responsePerson == null) {
            Log.d(TAG, "Person posting was unsuccessful 1");
            return null;
        } else {
            String newText = "Person created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            return responsePerson.getResponseString();
        }
    }

    public static String convertStringToDate(String datastring) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = originalFormat.parse(datastring);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return formattedDate;
    }

    /**
     * Uploads Patient data on the OpenMRS server.
     *
     * @param patientID      Unique Id of the patient.
     * @param responseString Response JSON string
     * @return boolean value representing success or failure.
     */
    private String uploadPatientData(Integer patientID, String responseString) {

        String identifier = getOpenMrsIdentifier();

        if (identifier != null) {
            String patientString =
                    String.format("{\"person\":\"%s\", " +
                                    "\"identifiers\":[{\"identifier\":\"%s\", " +
                                    "\"identifierType\":\"%s\", " +
                                    "\"location\":\"%s\", " +
                                    "\"preferred\":true}]}",
                            responseString,
                            identifier,
                            UuidDictionary.IDENTIFIER_OPENMRS_ID,
                            location_uuid);

            Log.d(TAG, "Patient String: " + patientString);
            WebResponse responsePatient;
            responsePatient = HelperMethods.postCommand("patient", patientString, getApplicationContext());
            Log.d(TAG, "uploadPatientData: " + responsePatient.getResponseString());
            if (responsePatient == null || responsePatient.getResponseCode() != 201) {
                String newText = "Patient was not created. Please check your connection.";
                mBuilder.setContentText(newText).setNumber(++numMessages);
                mNotifyManager.notify(mId, mBuilder.build());
                Log.d(TAG, "Patient posting was unsuccessful 2");
                Log.d(TAG, responsePatient.getResponseString());
                return null;
            } else {
                String newText = "Patient created successfully.";
                mBuilder.setContentText(newText).setNumber(++numMessages);
                mNotifyManager.notify(mId, mBuilder.build());

                ContentValues contentValuesOpenMRSID = new ContentValues();
                Log.i(TAG, responsePatient.getResponseString());
                contentValuesOpenMRSID.put("openmrs_uuid", responsePatient.getResponseString());
                contentValuesOpenMRSID.put("openmrs_id", identifier);
                String selection = "_id = ?";
                String[] args = {String.valueOf(patientID)};

                db.update(
                        "patient",
                        contentValuesOpenMRSID,
                        selection,
                        args
                );
                return responsePatient.getResponseString();
            }
        }
        return null;
    }

    /**
     * Retrieves Patient medical details and vitals from local database for uploading to the OpenMRS server.
     *
     * @param patientID      Unique patient Id
     * @param visitID        Unique visit Id of the patient
     * @param current_intent this intent
     * @return uploadStatus
     */
    private boolean uploadVisit(Integer patientID, String visitID, Intent current_intent) {


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

        String[] columns = {"value", " concept_id"};
        String orderBy = "visit_id";

        try {
            String famHistSelection = "patient_id = ? AND concept_id = ?";
            String[] famHistArgs = {String.valueOf(patientID), String.valueOf(ConceptId.RHK_FAMILY_HISTORY_BLURB)};
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
            String[] medHistArgs = {String.valueOf(patientID), String.valueOf(ConceptId.RHK_MEDICAL_HISTORY_BLURB)};
            Cursor medHistCursor = db.query("obs", columns, medHistSelection, medHistArgs, null, null, orderBy);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            patHistory.setValue(medHistText);
            if (medHistText != null && !medHistText.isEmpty()) {
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
        String[] visitArgs = {String.valueOf(patientID), visitID};
        Cursor visitCursor = db.query("obs", columns, visitSelection, visitArgs, null, null, orderBy);
        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndex("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                switch (dbConceptID) {
                    case ConceptId.CURRENT_COMPLAINT: //Current Complaint
                        complaint.setValue(dbValue);
                        break;
                    case ConceptId.PHYSICAL_EXAMINATION: //Physical Examination
                        physFindings.setValue(dbValue);
                        break;
                    case ConceptId.HEIGHT: //Height
                        height.setValue(dbValue);
                        break;
                    case ConceptId.WEIGHT: //Weight
                        weight.setValue(dbValue);
                        break;
                    case ConceptId.PULSE: //Pulse
                        pulse.setValue(dbValue);
                        break;
                    case ConceptId.SYSTOLIC_BP: //Systolic BP
                        bpSys.setValue(dbValue);
                        break;
                    case ConceptId.DIASTOLIC_BP: //Diastolic BP
                        bpDias.setValue(dbValue);
                        break;
                    case ConceptId.TEMPERATURE: //Temperature
                        temperature.setValue(dbValue);
                        break;
                    case ConceptId.SPO2: //SpO2
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

        boolean uploadStatus = false;


        String patientSelection = "_id = ?";
        String[] patientArgs = {String.valueOf(patientID)};
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

        if (patient.getOpenmrsId() == null || patient.getOpenmrsId().isEmpty()) {


            Toast.makeText(this, "Pa", Toast.LENGTH_SHORT).show();
            return false;
        }

        Integer statusCode = STATUS_VISIT_NOT_CREATED;
        if (current_intent.hasExtra("status")) {
            statusCode = current_intent.getIntExtra("status", -1);
            if (statusCode > 0) {
                String visitUUID;

                if (statusCode == STATUS_VISIT_NOT_CREATED) {
                    visitUUID = uploadVisitData(patient, startDateTime, visitID);
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


                } else {
                    visitUUID = current_intent.getStringExtra("visitResponse");
                }


                if (visitUUID != null) {
                    current_intent.putExtra("visitResponse", visitUUID);
                    current_intent.putExtra("status", statusCode);
                    statusCode = STATUS_ENCOUNTER_NOT_CREATED;
                    if (statusCode == STATUS_ENCOUNTER_NOT_CREATED) {
                        boolean encounter_vitals = uploadEncounterVitals(visitID, visitUUID, patient, startDateTime,
                                temperature, weight, height, pulse, bpSys, bpDias, spO2);

                        if (encounter_vitals) {
                            statusCode = STATUS_ENCOUNTER_NOTE_NOT_CREATED;
                            current_intent.putExtra("status", statusCode);
                        }

                        boolean encounter_notes = uploadEncounterNotes(visitID, visitUUID, patient, startDateTime,
                                patHistory, famHistory, complaint, physFindings);

                        if (encounter_notes && encounter_vitals) uploadStatus = true;
                    } else if (statusCode == STATUS_ENCOUNTER_NOTE_NOT_CREATED) {
                        boolean encounter_notes = uploadEncounterNotes(visitID, visitUUID, patient, startDateTime,
                                patHistory, famHistory, complaint, physFindings);
                        uploadStatus = encounter_notes;
                    }
                }

                current_intent.putExtra("status", statusCode);

            } else if (patient.getOpenmrsId() == null || (patient.getOpenmrsId() != null && patient.getOpenmrsId().isEmpty())) {
                current_intent.putExtra("status", statusCode);
            }
        } else {
            String visitUUID;
            visitUUID = uploadVisitData(patient, startDateTime, visitID);

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
                boolean encounter_vitals = uploadEncounterVitals(visitID, visitUUID, patient, startDateTime,
                        temperature, weight, height, pulse, bpSys, bpDias, spO2);
                if (encounter_vitals) statusCode = STATUS_ENCOUNTER_NOTE_NOT_CREATED;
                boolean encounter_notes = uploadEncounterNotes(visitID, visitUUID, patient, startDateTime,
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

        if (uploadStatus) {
            sendResultMessage();
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
    private String uploadVisitData(Patient patient, String startDateTime, String visitID) {


        //TODO: Location UUID needs to be found before doing these
        String visitString =
                String.format("{\"startDatetime\":\"%s\"," +
                                "\"visitType\":\"" + UuidDictionary.VISIT_TELEMEDICINE + "\"," +
                                "\"patient\":\"%s\"," +
                                "\"location\":\"%s\"}",
                        startDateTime, patient.getOpenmrsId(), location_uuid);
        Log.d(TAG, "Visit String: " + visitString);
        WebResponse responseVisit;
        responseVisit = HelperMethods.postCommand("visit", visitString, getApplicationContext());
        Log.d(TAG, String.valueOf(responseVisit.getResponseCode()));
        if (responseVisit != null && responseVisit.getResponseCode() != 201) {
            String newText = "Visit was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Visit posting was unsuccessful");
        } else {
            String newText = "Visit created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, responseVisit.getResponseString());
        }

        Intent imageUpload = new Intent(this, ImageUploadService.class);
        imageUpload.putExtra("patientID", patientID);
        imageUpload.putExtra("name", patientName);
        imageUpload.putExtra("patientUUID", patient.getOpenmrsId());
        imageUpload.putExtra("visitUUID", responseVisit.getResponseString());
        imageUpload.putExtra("visitID", visitID);
        startService(imageUpload);

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
    private boolean uploadEncounterVitals(String visitID, String visitUUID, Patient patient, String startDateTime,
                                          Obs temperature, Obs weight, Obs height,
                                          Obs pulse, Obs bpSys, Obs bpDias, Obs spO2) {
        //---------------------;

        String quote = "\"";

        String formattedObs = "";

        //Weight
        if (weight.getValue() != null && !weight.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.WEIGHT + quote + "," +
                    quote + "value" + quote + ":" + weight.getValue() + "},";
        }

        //Height
        if (height.getValue() != null && !height.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.HEIGHT + quote + "," +
                    quote + "value" + quote + ":" + height.getValue() + "},";
        }

        //Temperature
        if (temperature.getValue() != null && !temperature.getValue().trim().isEmpty()) {
            Double fTemp = Double.parseDouble(temperature.getValue());
            Double cTemp = ((fTemp - 32) * 5 / 9);
            Log.i(TAG, "uploadEncounterVitals: " + cTemp + "//" + fTemp);
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.TEMPERATURE + quote + "," +
                    quote + "value" + quote + ":" + String.valueOf(cTemp) + "},";
        }

        //Pulse
        if (pulse.getValue() != null && !pulse.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.PULSE + quote + "," +
                    quote + "value" + quote + ":" + pulse.getValue() + "},";
        }

        //Systolic BP
        if (bpSys.getValue() != null && !bpSys.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.SYSTOLIC_BP + quote + "," +
                    quote + "value" + quote + ":" + bpSys.getValue() + "},";
        }

        //Diastolic BP
        if (bpDias.getValue() != null && !bpDias.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.DIASTOLIC_BP + quote + "," +
                    quote + "value" + quote + ":" + bpDias.getValue() + "},";
        }

        //Sp02
        if (spO2.getValue() != null && !spO2.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.SPO2 + quote + "," +
                    quote + "value" + quote + ":" + spO2.getValue() + "},";
        }

        if (!formattedObs.isEmpty()) {
            if (formattedObs.length() > 0 && formattedObs.charAt(formattedObs.length() - 1) == ',') {
                formattedObs = formattedObs.substring(0, formattedObs.length() - 1);
            }
        }

        String vitalsString =
                String.format("{" +
                                "\"encounterDatetime\":\"%s\"," +
                                "\"patient\":\"%s\"," +
                                "\"encounterType\":\"" + UuidDictionary.ENCOUNTER_VITALS + "\"," +
                                " \"visit\":\"%s\"," +
                                "\"obs\":[" + formattedObs +
                                "]," +
                                "\"encounterProviders\":[{" +
                                "\"encounterRole\":\"73bbb069-9781-4afc-a9d1-54b6b2270e04\"," +
                                "\"provider\":\"%s\"" +
                                "}]," +
                                "\"location\":\"%s\"}",
                        startDateTime,
                        patient.getOpenmrsId(),
                        visitUUID,
                        provider_uuid,
                        location_uuid
                );
        Log.d(TAG, "Vitals Encounter String: " + vitalsString);
        WebResponse responseVitals;
        responseVitals = HelperMethods.postCommand("encounter", vitalsString, getApplicationContext());
        if (responseVitals == null || responseVitals.getResponseCode() != 201) {
            String newText = "Encounter was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Encounter posting was unsuccessful");
            return false;
        } else {
            Log.i(TAG, "uploadEncounterVitals: " + responseVitals);
            try {
                JSONObject JSONResponse = new JSONObject(responseVitals.getResponseObject());
                JSONArray resultsArray = JSONResponse.getJSONArray("obs");
                JSONArray encounterProviders = JSONResponse.getJSONArray("encounterProviders");
                String encounterUUID = JSONResponse.getString("uuid");

                String providers = "";

                for (int i = 0; i < encounterProviders.length(); i++) {
                    if (providers.trim().isEmpty()) {
                        providers = encounterProviders.getJSONObject(i).getString("display");
                    } else {
                        providers = providers + ", " + encounterProviders.getJSONObject(i).getString("display");
                    }
                }

                ContentValues contentValuesEncounter = new ContentValues();
                contentValuesEncounter.put("openmrs_encounter_id", encounterUUID);
                contentValuesEncounter.put("patient_id", patientID);
                contentValuesEncounter.put("visit_id", visitID);
                contentValuesEncounter.put("openmrs_visit_uuid", visitUUID);
                contentValuesEncounter.put("encounter_type", "VITALS");
                if (!providers.trim().isEmpty()) {
                    contentValuesEncounter.put("encounter_provider", providers);
                }

                db.insert(
                        "encounter",
                        null,
                        contentValuesEncounter
                );

                if (resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject checking = resultsArray.getJSONObject(i);
                        String display = checking.getString("display");
                        String obsUUID = checking.getString("uuid");
                        String check = display.substring(0, display.indexOf(":"));

                        String obsUpdateSelection = "visit_id = ? AND concept_id = ?";
                        String concept_id = null;
                        ContentValues contentValuesObs = new ContentValues();
                        contentValuesObs.put("openmrs_encounter_id", encounterUUID);
                        contentValuesObs.put("openmrs_obs_id", obsUUID);
                        switch (check) {
                            case "Weight (kg)": {
                                concept_id = String.valueOf(ConceptId.WEIGHT);
                                break;
                            }
                            case "Height (cm)": {
                                concept_id = String.valueOf(ConceptId.HEIGHT);
                                break;
                            }
                            case "SYSTOLIC BLOOD PRESSURE": {
                                concept_id = String.valueOf(ConceptId.SYSTOLIC_BP);
                                break;
                            }
                            case "DIASTOLIC BLOOD PRESSURE": {
                                concept_id = String.valueOf(ConceptId.DIASTOLIC_BP);
                                break;
                            }
                            case "BLOOD OXYGEN SATURATION": {
                                concept_id = String.valueOf(ConceptId.SPO2);
                                break;
                            }
                            case "TEMPERATURE (C)": {
                                concept_id = String.valueOf(ConceptId.TEMPERATURE);
                                break;
                            }
                            case "Pulse": {
                                concept_id = String.valueOf(ConceptId.PULSE);
                                break;
                            }
                        }

                        String[] obsUpdateArgs = {visitID, concept_id};
                        if (visitID != null && concept_id != null) {
                            db.update(
                                    "obs",
                                    contentValuesObs,
                                    obsUpdateSelection,
                                    obsUpdateArgs
                            );
                        }
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            String newText = "Encounter created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            return true;
        }
        //---------------------
    }

    private boolean uploadEncounterNotes(String visitID, String visitUUID, Patient patient, String startDateTime,
                                         Obs patHistory, Obs famHistory, Obs complaint, Obs physFindings) {


        String quote = "\"";

        String formattedObs = "";

        //MedicalHistory
        if (patHistory.getValue() != null && !patHistory.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.RHK_MEDICAL_HISTORY_BLURB + quote + "," +
                    quote + "value" + quote + ":" + quote + patHistory.getValue() + quote + "},";
        }
        //FamilyHistory
        if (famHistory.getValue() != null && !famHistory.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.RHK_FAMILY_HISTORY_BLURB + quote + "," +
                    quote + "value" + quote + ":" + quote + famHistory.getValue() + quote + "},";
        }
        //CurrentComplaint
        if (complaint.getValue() != null && !complaint.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.CURRENT_COMPLAINT + quote + "," +
                    quote + "value" + quote + ":" + quote + complaint.getValue() + quote + "},";
        }
        //PhysicalExam
        if (physFindings.getValue() != null && !physFindings.getValue().trim().isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.PHYSICAL_EXAMINATION + quote + "," +
                    quote + "value" + quote + ":" + quote + physFindings.getValue() + quote + "},";
        }
        if (!formattedObs.isEmpty()) {
            if (formattedObs.length() > 0 && formattedObs.charAt(formattedObs.length() - 1) == ',') {
                formattedObs = formattedObs.substring(0, formattedObs.length() - 1);
            }
        }

        if (formattedObs.contains("%")) {
            formattedObs = formattedObs.replaceAll("%", "");
        }


        String noteString =
                String.format("{" +
                                "\"encounterDatetime\":\"%s\"," +
                                " \"patient\":\"%s\"," +
                                "\"encounterType\":\"" + UuidDictionary.ENCOUNTER_ADULTINITIAL + "\"," +
                                "\"visit\":\"%s\"," +
                                "\"obs\":[" + formattedObs
                                + "]," +
                                "\"encounterProviders\":[{" +
                                "\"encounterRole\":\"73bbb069-9781-4afc-a9d1-54b6b2270e04\"," +
                                "\"provider\":\"%s\"" +
                                "}]," +
                                "\"location\":\"%s\"}",

                        startDateTime,
                        patient.getOpenmrsId(),
                        visitUUID,
                        provider_uuid,
                        location_uuid
                );
        Log.d(TAG, "Notes Encounter String: " + noteString);
        WebResponse responseNotes;
        responseNotes = HelperMethods.postCommand("encounter", noteString, getApplicationContext());
        if (responseNotes != null && responseNotes.getResponseCode() != 201) {
            String newText = "Notes Encounter was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Notes Encounter posting was unsuccessful");
            return false;
        } else if (responseNotes == null) {
            Log.d(TAG, "Notes Encounter posting was unsuccessful");
            return false;
        } else {
            Log.i(TAG, "uploadEncounterNotes: " + responseNotes.getResponseString());
            try {
                JSONObject JSONResponse = new JSONObject(responseNotes.getResponseObject());
                JSONArray resultsArray = JSONResponse.getJSONArray("obs");
                JSONArray encounterProviders = JSONResponse.getJSONArray("encounterProviders");
                String encounterUUID = JSONResponse.getString("uuid");

                String providers = "";

                for (int i = 0; i > encounterProviders.length(); i++) {
                    if (providers.trim().isEmpty()) {
                        providers = encounterProviders.getJSONObject(i).getString("display");
                    } else {
                        providers = providers + "," + encounterProviders.getJSONObject(i).getString("display");
                    }
                }

                ContentValues contentValuesEncounter = new ContentValues();
                contentValuesEncounter.put("openmrs_encounter_id", encounterUUID);
                contentValuesEncounter.put("patient_id", patientID);
                contentValuesEncounter.put("visit_id", visitID);
                contentValuesEncounter.put("openmrs_visit_uuid", visitUUID);
                contentValuesEncounter.put("encounter_type", "ADULTINITIAL");
                if (!providers.trim().isEmpty()) {
                    contentValuesEncounter.put("encounter_provider", providers);
                }

                db.insert(
                        "encounter",
                        null,
                        contentValuesEncounter
                );

                if (resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject checking = resultsArray.getJSONObject(i);
                        String display = checking.getString("display");
                        String obsUUID = checking.getString("uuid");
                        String check = display.substring(0, display.indexOf(":"));

                        String obsUpdateSelection = "visit_id = ? AND concept_id = ?";
                        String concept_id = null;
                        ContentValues contentValuesObs = new ContentValues();
                        contentValuesObs.put("openmrs_encounter_id", encounterUUID);
                        contentValuesObs.put("openmrs_obs_id", obsUUID);
                        switch (check) {
                            case "PHYSICAL EXAMINATION": {
                                concept_id = String.valueOf(ConceptId.PHYSICAL_EXAMINATION);
                                break;
                            }
                            case "FAMILY HISTORY": {
                                concept_id = String.valueOf(ConceptId.RHK_FAMILY_HISTORY_BLURB);
                                break;
                            }
                            case "CURRENT COMPLAINT": {
                                concept_id = String.valueOf(ConceptId.CURRENT_COMPLAINT);
                                break;
                            }
                            case "MEDICAL HISTORY": {
                                concept_id = String.valueOf(ConceptId.RHK_MEDICAL_HISTORY_BLURB);
                                break;
                            }
                        }

                        String[] obsUpdateArgs = {visitID, concept_id};
                        if (visitID != null && concept_id != null) {
                            db.update(
                                    "obs",
                                    contentValuesObs,
                                    obsUpdateSelection,
                                    obsUpdateArgs
                            );
                        }
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            String newText = "Notes created successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            return true;
        }
    }

    private boolean uploadSurvey(Integer patientID, String visitID, Intent intent){
        Patient patient = new Patient();
        String patientSelection = "_id = ?";
        String[] patientArgs = {String.valueOf(patientID)};
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

        if (patient.getOpenmrsId() == null || patient.getOpenmrsId().isEmpty()) {


            Toast.makeText(this, "Pa", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] columnsToReturn = {"openmrs_visit_uuid", "start_datetime"};
        String visitIDorderBy = "start_datetime";
        String visitIDSelection = "_id = ?";
        String[] visitIDArgs = {visitID};
        final Cursor visitIDCursor = db.query("visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("start_datetime"));
        String visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("openmrs_visit_uuid"));
        visitIDCursor.close();

        String rating = intent.getStringExtra("rating");
        String comments = intent.getStringExtra("comments");

        String quote = "\"";
        String formattedObs = "";

        if (rating != null && !rating.isEmpty() && comments==null) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.RATING + quote + "," +
                    quote + "value" + quote + ":" + quote + rating + quote + "}";
        }

        if (rating != null && !rating.isEmpty() && comments!=null) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.RATING + quote + "," +
                    quote + "value" + quote + ":" + quote + rating + quote + "},";
        }
        if (comments != null && !comments.isEmpty()) {
            formattedObs = formattedObs + "{" + quote + "concept" + quote + ":" + quote + UuidDictionary.COMMENTS + quote + "," +
                    quote + "value" + quote + ":" + quote + comments + quote + "}";
        }

        String noteString =
                String.format("{" +
                                "\"encounterDatetime\":\"%s\"," +
                                " \"patient\":\"%s\"," +
                                "\"encounterType\":\"" + UuidDictionary.ENCOUNTER_PATIENT_EXIT_SURVEY + "\"," +
                                "\"visit\":\"%s\"," +
                                "\"obs\":[" + formattedObs
                                + "]," +
                                "\"encounterProviders\":[{" +
                                "\"encounterRole\":\"73bbb069-9781-4afc-a9d1-54b6b2270e04\"," +
                                "\"provider\":\"%s\"" +
                                "}]," +
                                "\"location\":\"%s\"}",

                        startDateTime,
                        patient.getOpenmrsId(),
                        visitUUID,
                        provider_uuid,
                        location_uuid
                );
        Log.d(TAG, "Survey Encounter String: " + noteString);
        WebResponse responseSurvey;
        responseSurvey = HelperMethods.postCommand("encounter", noteString, getApplicationContext());
        if (responseSurvey != null && responseSurvey.getResponseCode() != 201) {
            String newText = "Survey Encounter was not created. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Survey Encounter posting was unsuccessful");
            return false;
        } else if (responseSurvey == null) {
            Log.d(TAG, "Survey Encounter posting was unsuccessful");
            return false;
        }
        String newText = "Survey uploaded successfully.";
        mBuilder.setContentText(newText).setNumber(++numMessages);
        mNotifyManager.notify(mId, mBuilder.build());
        return true;
    }

    /**
     * Ends Patient visit session.
     *
     * @param patientID     Unique patient Id
     * @param visitUUID      visit UUID
     * @param current_intent this intent
     * @return boolean value representing success or failure.
     */
    private boolean endVisit(Integer patientID, String visitUUID, Intent current_intent) {

        Log.d(TAG, "endVisit: ");

        String urlModifier = "visit/" + visitUUID;

        SimpleDateFormat endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date rightNow = new Date();
        String endDateTime = endDate.format(rightNow);


        String endString =
                String.format("{\"stopDatetime\":\"%s\"," +
                                "\"visitType\":\"%s\"}",
                        endDateTime,
                        UuidDictionary.VISIT_TELEMEDICINE);

        Log.d("End String", endString);

        WebResponse endResponse = HelperMethods.postCommand(urlModifier, endString, getApplicationContext());

        Log.d(TAG, endResponse.getResponseCode() + "-" + endResponse.getResponseString());

        if (endResponse.getResponseString() != null && endResponse.getResponseCode() != 200) {
            String newText = "Visit ending was unsuccessful. Please check your connection.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Visit ending was unsuccessful ");
            return false;
        } else {
            String newText = "Visit ended successfully.";
            mBuilder.setContentText(newText).setNumber(++numMessages);
            mNotifyManager.notify(mId, mBuilder.build());

            ContentValues contentValuesVisit = new ContentValues();
            contentValuesVisit.put("end_datetime", endDateTime);
            String visitUpdateSelection = "openmrs_visit_uuid = ?";
            String[] visitUpdateArgs = {visitUUID};

            db.update(
                    "visit",
                    contentValuesVisit,
                    visitUpdateSelection,
                    visitUpdateArgs
            );


            if (current_intent.hasExtra("queueId")) {
                int queueId = current_intent.getIntExtra("queueId", -1);
                removeJobFromQueue(queueId);
            }
            return true;
        }


    }

    public void errorNotification(String patientName, String type) {

        // When the loop is finished, updates the notification
        String text = String.format("%s's %s data not uploaded.", patientName, type);
        mBuilder.setContentText(text)
                // Removes the progress bar
                .setProgress(0, 0, false);
        mNotifyManager.notify(mId, mBuilder.build());
    }


    private void retryAfterDelay(Intent intent) {
        retryAfterDelay(intent, MAX_TRIES, RETRY_DELAY);
    }

    private void retryAfterDelay(Intent intent, int max_retries, int retry_delay) {
        // Get the number of previously failed attempts, and add one.
        int failedAttempts = intent.getIntExtra(EXTRA_FAILED_ATTEMPTS, 0) + 1;
        // if we have failed less than the max retries, reschedule the intent
        Log.i(TAG, "Scheduling retry" + failedAttempts + "/" + max_retries);
        if (failedAttempts <= max_retries) {
            Log.i(TAG, "Retrying" + failedAttempts + "/" + max_retries);
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
        }


    }

    private static int incrementAndGetRequestCode() {
        requestCode = requestCode++;
        return requestCode;
    }

    private int addJobToQueue(Intent intent) {
        if (!intent.hasExtra("queueId")) {
            Log.d(TAG, "Adding to Queue");

            String serviceCall = intent.getStringExtra("serviceCall");

            // Add a new Delayed Job record
            ContentValues values = new ContentValues();
            values.put(DelayedJobQueueProvider.JOB_TYPE, serviceCall);
            values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
            values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, requestCode);
            values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));
            values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getIntExtra("patientID", 0));
            values.put(DelayedJobQueueProvider.SYNC_STATUS, 0);

            switch (serviceCall) {
                case "patient": {
                    if (intent.hasExtra("status")) values.put(DelayedJobQueueProvider.STATUS,
                            intent.getIntExtra("status", -1));
                    else values.put(DelayedJobQueueProvider.STATUS, STATUS_PERSON_NOT_CREATED);
                    values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("personResponse"));
                    break;
                }
                case "visit": {
                    values.put(DelayedJobQueueProvider.VISIT_ID, intent.getStringExtra("visitID"));
                    if (intent.hasExtra("status"))
                        values.put(DelayedJobQueueProvider.STATUS, intent.getIntExtra("status", -1));
                    else values.put(DelayedJobQueueProvider.STATUS, STATUS_VISIT_NOT_CREATED);
                    values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("visitResponse"));
                    break;
                }
                case "endVisit": {
                    values.put(DelayedJobQueueProvider.VISIT_UUID, intent.getStringExtra("visitUUID"));
                    break;
                }
                default:
                    Log.e(TAG, "Does not match any Job Type");
            }


            Uri uri = getContentResolver().insert(
                    DelayedJobQueueProvider.CONTENT_URI, values);

            return Integer.valueOf(uri.getLastPathSegment());
        } else {
            Log.i(TAG, "Queue id : " + intent.getIntExtra("queueId", -1));
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
                Log.i(TAG, result + " row updated");
            } else {
                Log.e(TAG, "Database error while updating row!");
            }
            return intent.getIntExtra("queueId", -1);
        }
    }

    private void removeJobFromQueue(int queueId) {
        Log.d(TAG, "Removing from Queue");
        if (queueId > -1) {
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = getContentResolver().delete(uri, null, null);
            if (result > 0) {
                Log.i(TAG, result + " row deleted");
            } else {
                Log.e(TAG, "Database error while deleting row!");
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
        super.onDestroy();
    }

    private void sendResultMessage() {
        Log.d("sender", "Broadcasting result message: ");
        Intent intent = new Intent(VisitSummaryActivity.FILTER);
        intent.putExtra("Restart", 200);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getOpenMrsIdentifier() {
        String returnString = null;
        try {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String serverAddress = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL_BASE, null);
            if (serverAddress != null) {

                AccountManager manager = AccountManager.get(this);

                String USERNAME;
                String PASSWORD;

                Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
                if (accountList.length == 1) {
                    Account authAccount = accountList[0];
                    USERNAME = authAccount.name;
                    PASSWORD = manager.getPassword(authAccount);

                    if (USERNAME != null && !USERNAME.isEmpty() && PASSWORD != null && !PASSWORD.isEmpty()) {


                        WebResponse responseIdentifier;
                        responseIdentifier = HelperMethods.getCommand(serverAddress + "/module/idgen/generateIdentifier.form",
                                "?source=1&username=" + USERNAME + "&password=" + PASSWORD,
                                getApplicationContext(), USERNAME, PASSWORD);

                        if (responseIdentifier.getResponseString() != null && responseIdentifier.getResponseCode() == 200) {

                            Gson gson = new Gson();
                            Identifier i = gson.fromJson(responseIdentifier.getResponseString(), Identifier.class);

                            List<String> identifiersList = i.getIdentifiers();
                            for (String string : identifiersList) {
                                Log.i(TAG, "getOpenMrsIdentifier: ++" + string);
                            }
                            returnString = identifiersList.get(0);
                            Log.i(TAG, "getOpenMrsIdentifier: " + returnString);

                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
            return null;
        }
        return returnString;
    }
}