package edu.jhu.bme.cbid.healthassistantsclient.services;

import android.app.IntentService;
import android.app.NotificationManager;
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
import edu.jhu.bme.cbid.healthassistantsclient.LocalRecordsDatabaseHelper;
import edu.jhu.bme.cbid.healthassistantsclient.R;
import edu.jhu.bme.cbid.healthassistantsclient.SettingsActivity;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;
import edu.jhu.bme.cbid.healthassistantsclient.objects.PatientImage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Sends Identification data to OpenMRS and receives the OpenMRS ID of the newly-created patient
 */
public class IdService extends IntentService {

    public static final String LOG_TAG = "IdService";
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
    public IdService(String name) {
        super(name);
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

    public void errorNotification() {
        // TODO: determine error behavior
    }
}
