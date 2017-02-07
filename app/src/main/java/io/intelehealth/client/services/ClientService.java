package io.intelehealth.client.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.intelehealth.client.HelperMethods;
import io.intelehealth.client.LocalRecordsDatabaseHelper;
import io.intelehealth.client.R;
import io.intelehealth.client.SettingsActivity;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.PatientImage;
import io.intelehealth.client.objects.WebResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Sends Identification data to OpenMRS and receives the OpenMRS ID of the newly-created patient
 */
public class ClientService extends IntentService {

    public static final String LOG_TAG = "ClientService";
    public int mId = 1;

    NotificationManager mNotifyManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

    LocalRecordsDatabaseHelper mDbHelper;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ClientService(String name) {
        super(name);
    }

    public ClientService(){
        super("Intent Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createNotification();
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());

        try {
            while (!isOnline()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Waiting interrupted?");
        }


        String dataString = intent.getDataString(); // The dataString is the _id of the patient to send
        int id = Integer.parseInt(dataString);
        String jsonToSend = serialize(dataString);

        String resultString = sendData(jsonToSend);
        boolean sent = sendPicture(id);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int openMrsId = Integer.parseInt(resultString);
        if(resultString != null) {
            String sql = "UPDATE patient SET opemrs_id = ? WHERE _id = ?";
            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindLong(1, openMrsId); // SQL indices start at 1
            stmt.bindLong(2, id);
            int numberOfRowsAffected = stmt.executeUpdateDelete();
            Log.v(LOG_TAG, "Updated Rows: " + numberOfRowsAffected);
            if(sent) endNotification();
            else errorNotification();
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

    public void createNotification() {
        mBuilder.setContentTitle("Patient Data Upload")
                .setContentText("Patient data upload in progress")
                .setSmallIcon(R.drawable.ic_cloud_upload);
        // Sets an activity indicator for an operation of indeterminate length
        mBuilder.setProgress(0, 0, true);
        // Issues the notification
        mNotifyManager.notify(mId, mBuilder.build());
    }

    public void endNotification() {
        // mNotifyManager.cancel(mId);

        // When the loop is finished, updates the notification
        mBuilder.setContentText("Upload complete")
                // Removes the progress bar
                .setProgress(0,0,false);
        mNotifyManager.notify(mId, mBuilder.build());
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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

            if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

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

    public boolean sendPicture(int patientId) {
        String jpg = patientId + ".jpg";
        Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/patients/" + jpg);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        PatientImage image = new PatientImage(patientId, encodedImage);
        Gson gson = new Gson();
        String json = gson.toJson(image);

        String result = sendData(json);

        return result != null;
    }

    public String uploadPatient(String... params) {


        //so this service will need the patientID and the visitID
        //EVERYTHING will need to be queried, and then stored into an object temporarily
        //all of these will be be posted to openMRS
        //once done, service should end



        String openMRSUUID = null;
        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {patientID};
        String[] patientColumns = {"openmrs_uuid"};
        final Cursor idCursor = db.query("patient", patientColumns, patientSelection, patientArgs, null, null, null);

        idCursor.moveToLast();
        openMRSUUID = idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_uuid"));
        idCursor.close();

        if (openMRSUUID == null) {
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
                                    "\"value\": \"Barhra\"}], " + //TODO: Change this attribute to the name of the clinic as listed in OpenMRS
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
                failedMessage = "Person posting was unsuccessful";
//                    failedStep(failedMessage);
                Log.d(LOG_TAG, "Person posting was unsuccessful");
                return null;
            }

            assert responsePerson != null;

            String patientString =
                    String.format("{\"person\":\"%s\", " +
                                    "\"identifiers\":[{\"identifier\":\"%s\", " +
                                    "\"identifierType\":\"05a29f94-c0ed-11e2-94be-8c13b969e334\", " +
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\", " +
                                    "\"preferred\":true}]}",

                            responsePerson.getResponseString(), identifierNumber);

            Log.d(LOG_TAG, "Patient String: " + patientString);
            WebResponse responsePatient;
            responsePatient = HelperMethods.postCommand("patient", patientString, getApplicationContext());
            if (responsePatient != null && responsePatient.getResponseCode() != 201) {
                failedMessage = "Patient posting was unsuccessful";
//                    failedStep(failedMessage);
                Log.d(LOG_TAG, "Patient posting was unsuccessful");
                return null;
            }

            assert responsePatient != null;
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

            openMRSUUID = responsePatient.getResponseString();
        }


        String table = "visit";
        String[] columnsToReturn = {"start_datetime"};
        String orderBy = "start_datetime";
        String visitSelection = "_id = ?";
        String[] visitArgs = {visitID};
        final Cursor visitCursor = db.query(table, columnsToReturn, visitSelection, visitArgs, null, null, orderBy);
        visitCursor.moveToLast();
        String startDateTime = visitCursor.getString(visitCursor.getColumnIndexOrThrow("start_datetime"));
        visitCursor.close();

        //TODO: Location UUID needs to be found before doing these
        String visitString =
                String.format("{\"startDatetime\":\"%s\"," +
                                "\"visitType\":\"Telemedicine\"," +
                                "\"patient\":\"%s\"," +
                                "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",
                        startDateTime, openMRSUUID);
        Log.d(LOG_TAG, "Visit String: " + visitString);
        WebResponse responseVisit;
        responseVisit = HelperMethods.postCommand("visit", visitString, getApplicationContext());
        if (responseVisit != null && responseVisit.getResponseCode() != 201) {
            failedMessage = "Visit posting was unsuccessful";
//                failedStep(failedMessage);
            Log.d(LOG_TAG, "Visit posting was unsuccessful");
            return null;
        }

        assert responseVisit != null;

        visitUUID = responseVisit.getResponseString();
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
                                "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",

                        startDateTime, openMRSUUID, responseVisit.getResponseString(),
//                            openMRSUUID,
                        weight.getValue(), height.getValue(), tempString,
                        pulse.getValue(), bpSys.getValue(),
                        bpDias.getValue(), spO2.getValue()
                );
        Log.d(LOG_TAG, "Vitals Encounter String: " + vitalsString);
        WebResponse responseVitals;
        responseVitals = HelperMethods.postCommand("encounter", vitalsString, getApplicationContext());
        if (responseVitals != null && responseVitals.getResponseCode() != 201) {
            failedMessage = "Encounter posting was unsuccessful";
//                failedStep(failedMessage);
            Log.d(LOG_TAG, "Encounter posting was unsuccessful");
            return null;
        }

        assert responseVitals != null;

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
                                "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",

                        startDateTime, openMRSUUID, responseVisit.getResponseString(),
//                            openMRSUUID,
                        patient.getSdw(), patient.getOccupation(),
                        //TODO: add logic to remove SDW and occupation when they are empty
                        patHistory.getValue(), famHistory.getValue(),
                        complaint.getValue(), physFindings.getValue()
                );
        Log.d(LOG_TAG, "Notes Encounter String: " + noteString);
        WebResponse responseNotes;
        responseNotes = HelperMethods.postCommand("encounter", noteString, getApplicationContext());
        if (responseNotes != null && responseNotes.getResponseCode() != 201) {
            failedMessage = "Notes posting was unsuccessful";
//                failedStep(failedMessage);
            Log.d(LOG_TAG, "Notes Encounter posting was unsuccessful");
            return null;
        }

        uploaded = true;

        return null;
    }


    public void errorNotification() {
        // TODO: determine error behavior
    }
}
