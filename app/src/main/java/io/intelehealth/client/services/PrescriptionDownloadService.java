package io.intelehealth.client.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.utilities.ConceptId;
import io.intelehealth.client.utilities.HelperMethods;

import static io.intelehealth.client.services.ClientService.STATUS_SYNC_IN_PROGRESS;
import static io.intelehealth.client.services.ClientService.STATUS_SYNC_STOPPED;

/**
 * Created by Dexter Barretto on 7/25/17.
 * Github : @dbarretto
 */

public class PrescriptionDownloadService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PrescriptionDownloadService(String name) {
        super(name);
    }

    public PrescriptionDownloadService() {
        super(TAG);
    }

    Integer queueId = null;

    private static final String TAG = PrescriptionDownloadService.class.getSimpleName();
    NotificationManager mNotifyManager;
    public int mId = 4;
    NotificationCompat.Builder mBuilder;

    Integer patientID;
    String visitID;
    String visitUUID;
    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();


        if (!intent.hasExtra("queueId")) {
            int id = addJobToQueue(intent);
            intent.putExtra("queueId", id);
        }

        patientID = intent.getIntExtra("patientID",-1);
        visitID = intent.getStringExtra("visitID");
        visitUUID = intent.getStringExtra("visitUUID");
        queueId = intent.getIntExtra("queueId", -1);

        Log.i(TAG, "onHandleIntent: "+ visitUUID);

        String queryString = "/" + visitUUID;

        queueSyncStart(queueId);

        String newText = "Prescription Download Started.";
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Prescription Download")
                .setContentText(newText);
        mNotifyManager.notify(mId, mBuilder.build());

        WebResponse responseVisit;
        responseVisit = HelperMethods.getCommand("visit", queryString, getApplicationContext());
        if (responseVisit != null && responseVisit.getResponseCode() == 200) {
            Log.i(TAG, "onHandleIntent: Visit Checked");

            if (responseVisit != null) {
                JSONArray resultsArray = null;
                List<String> uriList = new ArrayList<>();
                try {
                    JSONObject JSONResponse = new JSONObject(responseVisit.getResponseString());
                    resultsArray = JSONResponse.getJSONArray("encounters");

                    String searchString = "Visit Note";

                    if (resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            JSONObject checking = resultsArray.getJSONObject(i);
                            String display = checking.getString("display");
                            if (display.length() > 9) {
                                String checkString = display.substring(0, 10);
                                Log.i(TAG, "onHandleIntent: "+ checkString);
                                if (checkString.equals(searchString)) {
                                    uriList.add("/" + checking.getString("uuid"));
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!uriList.isEmpty()) {
                    List<WebResponse> obsResponse = new ArrayList<>();
                    for (int i = 0; i < uriList.size(); i++) {
                        Log.i(TAG, "onHandleIntent: checking obs");
                        obsResponse.add(i, HelperMethods.getCommand("encounter", uriList.get(i), getApplicationContext()));
                        if (obsResponse.get(i) != null && obsResponse.get(i).getResponseCode() != 200) {
                            String errorMessage = "Obs get call number " + String.valueOf(i) + " of " + String.valueOf(uriList.size()) + " was unsuccessful";
//                    failedStep(errorMessage);
//                    Log.d(TAG, errorMessage);

                        }
                    }

                    JSONObject responseObj = null;
                    JSONArray obsArray = null;
                    JSONArray providersArray = null;


                    for (int i = 0; i < obsResponse.size(); i++) {
                        //Log.d(TAG, obsResponse.get(i).toString());
                        //Log.d(TAG, obsResponse.get(i).getResponseString());

                        try {
                            responseObj = new JSONObject(obsResponse.get(i).getResponseString());
                            obsArray = responseObj.getJSONArray("obs");
                            providersArray = responseObj.getJSONArray("encounterProviders");

                            for (int j = 0; j < providersArray.length(); j++) {
                                String providerName = null;

                                try {
                                    JSONObject providerObj = providersArray.getJSONObject(j);
                                    providerName = providerObj.getString("display");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                String[] providerSplit = providerName.split(":");
                                providerName = providerSplit[0];
                                if (!doctorName.contains(providerName) && !doctorName.isEmpty()) {
                                    doctorName = doctorName + "\n" + providerName;
                                } else {
                                    doctorName = providerName;
                                }

                                Log.i(TAG, "onHandleIntent: pn "+ providerName);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //Log.d(TAG, obsArray.toString());
                        for (int k = 0; k < obsArray.length(); k++) {
                            String obsString = "";
                            //Log.d(TAG, obsString);
                            try {
                                JSONObject obsObj = obsArray.getJSONObject(k);
                                obsString = obsObj.getString("display");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String index = obsString.substring(0, obsString.indexOf(":"));
                            Log.i(TAG, "onHandleIntent: "+index);
                            String indexText = obsString.substring(obsString.indexOf(":") + 1, obsString.length());

                            String[] columns = {"_id"};
                            String obsSelection = "concept_id = ? AND visit_id = ?";

                            if (index.contains("TELEMEDICINE DIAGNOSIS")) {
                                if (!diagnosisReturned.contains(indexText) && !diagnosisReturned.isEmpty()) {
                                    diagnosisReturned = diagnosisReturned + "\n" + indexText;
                                } else {
                                    diagnosisReturned = indexText;
                                }
                                String[] obsArgs = {String.valueOf(ConceptId.TELEMEDICINE_DIAGNOSIS), visitID};
                                Cursor cursor = queryDatabase(columns, obsSelection, obsArgs);
                                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    Integer obsID = cursor.getInt(cursor.getColumnIndex("_id"));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("value", diagnosisReturned);
                                    updateDatabase(obsID, contentValues);
                                    cursor.close();
                                } else {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("patient_id", patientID);
                                    contentValues.put("visit_id", visitID);
                                    contentValues.put("value", diagnosisReturned);
                                    contentValues.put("concept_id", ConceptId.TELEMEDICINE_DIAGNOSIS);
                                    insertDatabase(contentValues);
                                }
                            }

                            if (index.contains("JSV MEDICATIONS")) {
                                if (!rxReturned.contains(indexText) && !rxReturned.isEmpty()) {
                                    rxReturned = rxReturned + "\n" + indexText;
                                } else {
                                    rxReturned = indexText;
                                }
                                String[] obsArgs = {String.valueOf(ConceptId.JSV_MEDICATIONS), visitID};
                                Cursor cursor = queryDatabase(columns, obsSelection, obsArgs);
                                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    Integer obsID = cursor.getInt(cursor.getColumnIndex("_id"));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("value", rxReturned);
                                    updateDatabase(obsID, contentValues);
                                    cursor.close();
                                } else {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("patient_id", patientID);
                                    contentValues.put("visit_id", visitID);
                                    contentValues.put("value", rxReturned);
                                    contentValues.put("concept_id", ConceptId.JSV_MEDICATIONS);
                                    insertDatabase(contentValues);
                                }
                            }

                            if (index.contains("MEDICAL ADVICE")) {
                                if (!adviceReturned.contains(indexText) && !adviceReturned.isEmpty()) {
                                    adviceReturned = adviceReturned + "\n" + indexText;
                                } else {
                                    adviceReturned = indexText;
                                }
                                String[] obsArgs = {String.valueOf(ConceptId.MEDICAL_ADVICE), visitID};
                                Cursor cursor = queryDatabase(columns, obsSelection, obsArgs);
                                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    Integer obsID = cursor.getInt(cursor.getColumnIndex("_id"));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("value", adviceReturned);
                                    updateDatabase(obsID, contentValues);
                                    cursor.close();
                                } else {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("patient_id", patientID);
                                    contentValues.put("visit_id", visitID);
                                    contentValues.put("value", adviceReturned);
                                    contentValues.put("concept_id", ConceptId.MEDICAL_ADVICE);
                                    insertDatabase(contentValues);
                                }
                            }

                            if (index.contains("REQUESTED TESTS")) {
                                if (!testsReturned.contains(indexText) && !testsReturned.isEmpty()) {
                                    testsReturned = testsReturned + "\n" + indexText;
                                } else {
                                    testsReturned = indexText;
                                }
                                String[] obsArgs = {String.valueOf(ConceptId.REQUESTED_TESTS), visitID};
                                Cursor cursor = queryDatabase(columns, obsSelection, obsArgs);
                                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    Integer obsID = cursor.getInt(cursor.getColumnIndex("_id"));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("value", testsReturned);
                                    updateDatabase(obsID, contentValues);
                                    cursor.close();
                                } else {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("patient_id", patientID);
                                    contentValues.put("visit_id", visitID);
                                    contentValues.put("value", testsReturned);
                                    contentValues.put("concept_id", ConceptId.REQUESTED_TESTS);
                                    insertDatabase(contentValues);
                                }
                            }

                            if (index.contains("Additional Comments")) {
                                if (!additionalReturned.contains(indexText) && !additionalReturned.isEmpty()) {
                                    additionalReturned = additionalReturned + "\n" + indexText;
                                } else {
                                    additionalReturned = indexText;
                                }
                                String[] obsArgs = {String.valueOf(ConceptId.ADDITIONAL_COMMENTS), visitID};
                                Cursor cursor = queryDatabase(columns, obsSelection, obsArgs);
                                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    Integer obsID = cursor.getInt(cursor.getColumnIndex("_id"));
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("value", additionalReturned);
                                    updateDatabase(obsID, contentValues);
                                    cursor.close();
                                } else {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("patient_id", patientID);
                                    contentValues.put("visit_id", visitID);
                                    contentValues.put("value", additionalReturned);
                                    contentValues.put("concept_id", ConceptId.ADDITIONAL_COMMENTS);
                                    insertDatabase(contentValues);
                                }
                            }

                            sendResultMessage();

                        }
                    }
                    newText = "Prescription Download Complete";
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Prescription Download")
                            .setContentText(newText);
                    mNotifyManager.notify(mId, mBuilder.build());
                    removeJobFromQueue(queueId);

                } else {
                    newText = "No data to download";
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Prescription Download")
                            .setContentText(newText);
                    mNotifyManager.notify(mId, mBuilder.build());
                    removeJobFromQueue(queueId);
                }
            }
        }else{
            queueSyncStop(queueId);
        }
    }

    private int addJobToQueue(Intent intent) {

        Log.d(TAG, "Adding to Queue");
        // Add a new Delayed Job record
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.JOB_TYPE, "prescriptionDownload");
        values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
        values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, 0);
        values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));
        values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getIntExtra("patientID",-1));
        values.put(DelayedJobQueueProvider.VISIT_ID, intent.getStringExtra("visitID"));
        values.put(DelayedJobQueueProvider.VISIT_UUID, intent.getStringExtra("visitUUID"));
        values.put(DelayedJobQueueProvider.SYNC_STATUS, 0);

        Uri uri = getContentResolver().insert(
                DelayedJobQueueProvider.CONTENT_URI, values);

        return Integer.valueOf(uri.getLastPathSegment());

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

    private Cursor queryDatabase(String[] columns, String obsSelection, String[] obsArgs) {
        return db.query("obs", columns, obsSelection, obsArgs, null, null, null);
    }

    private void insertDatabase(ContentValues contentValues) {
        db.insert("obs", null, contentValues);
    }

    private void updateDatabase(Integer id, ContentValues contentValues) {
        String obsUpdateSelection = "_id = ?";
        String[] obsUpdateArgs = {String.valueOf(id)};
        updateDatabase(contentValues, obsUpdateSelection, obsUpdateArgs);
    }

    private void updateDatabase(ContentValues contentValues,
                                String obsUpdateSelection, String[] obsUpdateArgs) {
        db.update(
                "obs",
                contentValues,
                obsUpdateSelection,
                obsUpdateArgs
        );
    }

    private void queueSyncStart(int queueId) {
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.SYNC_STATUS, STATUS_SYNC_IN_PROGRESS);
        String url = DelayedJobQueueProvider.URL + "/" + queueId;
        Uri uri = Uri.parse(url);
        getContentResolver().update(uri, values, null, null);
    }

    private void queueSyncStop(int queueId) {
        String string = "Prescription Download Failed";
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Prescription Download")
                .setContentText(string);
        mNotifyManager.notify(mId, mBuilder.build());
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
        intent.putExtra("Restart", 100);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
