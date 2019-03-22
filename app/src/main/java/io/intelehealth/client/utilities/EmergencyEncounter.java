package io.intelehealth.client.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.api.retrofit.RestApi;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.network.ApiClient;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.WebResponse;
import retrofit2.Call;

public class EmergencyEncounter {
    String TAG="EmergencyEncounter class";
    private SessionManager sessionManger;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IntelehealthApplication.getAppContext());
    String location_uuid = prefs.getString(SettingsActivity.KEY_PREF_LOCATION_UUID, null);
    String provider_uuid = prefs.getString("providerid", null);
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    String channelId = "channel-01";
    String channelName = "Channel Name";
    String patientUUID="";
    int mId=1;
    int numMessages=1;

    public boolean uploadEncounterEmergency(String visitID, String visitUUID, String startDateTime,
                                             int patientID,
                                             SQLiteDatabase db,
                                            Context context) {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //mahiti added
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }
        mBuilder = new NotificationCompat.Builder(context,channelId);
        String selection_patient = "_id = ?";
        String[] coloumns_patient = {"openmrs_uuid"};
        String[] args_patient = {String.valueOf(patientID)};

        Cursor patientCursor = db.query("patient", coloumns_patient, selection_patient, args_patient, null, null, null);

        if (patientCursor != null && patientCursor.moveToFirst() && patientCursor.getCount() > 0) {
            patientUUID = patientCursor.getString(patientCursor.getColumnIndex("openmrs_uuid"));
        }

        patientCursor.close();


        String noteString =
                String.format("{" +
                                "\"encounterDatetime\":\"%s\"," +
                                " \"patient\":\"%s\"," +
                                "\"encounterType\":\"" + UuidDictionary.EMERGENCY + "\"," +
                                "\"visit\":\"%s\"," +
                                "\"obs\":[]," +
                                "\"encounterProviders\":[{" +
                                "\"encounterRole\":\"73bbb069-9781-4afc-a9d1-54b6b2270e04\"," +
                                "\"provider\":\"%s\"" +
                                "}]," +
                                "\"location\":\"%s\"}",

                        startDateTime,
                        patientUUID,
                        visitUUID,
                        provider_uuid,
                        location_uuid
                );
        Log.d(TAG, "Emergency Encounter String: " + noteString);
        WebResponse responseNotes;
        responseNotes = HelperMethods.postCommand("encounter", noteString, IntelehealthApplication.getAppContext());
        if (responseNotes != null && responseNotes.getResponseCode() != 201) {
            String newText = "Emergency Encounter was not created. Please check your connection.";
//            mBuilder.setContentText(newText).setNumber(++numMessages);
//            mNotifyManager.notify(mId, mBuilder.build());
            Log.d(TAG, "Emergency Encounter posting was unsuccessful");
            return false;
        } else if (responseNotes == null) {
            Log.d(TAG, "Emergency Encounter posting was unsuccessful");
            return false;
        } else {
            Log.i(TAG, "uploadEncounterEmergency: " + responseNotes.getResponseString());
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
                contentValuesEncounter.put("encounter_type", "EMERGENCY");
                if (!providers.trim().isEmpty()) {
                    contentValuesEncounter.put("encounter_provider", providers);
                }

                db.insert(
                        "encounter",
                        null,
                        contentValuesEncounter
                );



            } catch (JSONException e) {
                e.printStackTrace();
            }
            String newText = "Emergency created successfully.";
//            mBuilder.setContentText(newText).setNumber(++numMessages);
//            mNotifyManager.notify(mId, mBuilder.build());
            return true;
        }
    }
    public boolean removeEncounterEmergency(final String visitID, final String visitUUID, final SQLiteDatabase db){
        RestApi apiInterface;
        sessionManger=new SessionManager(IntelehealthApplication.getAppContext());
        boolean success = false;
        apiInterface = ApiClient.getApiClient().create(RestApi.class);
//        apiInterface.DELETE_ENCOUNTER("").execute();
        if (visitID != null && visitUUID != null) {
                String selectQuery = "SELECT openmrs_encounter_id FROM encounter WHERE (visit_id='" + visitID + "' and openmrs_visit_uuid='" + visitUUID + "') and encounter_type='EMERGENCY'";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(cursor.getColumnIndex("openmrs_encounter_id"));
                        Call<Void> patientUUIDResponsemodelCall = apiInterface.DELETE_ENCOUNTER(name,"Basic YWRtaW46QWRtaW4xMjM=");
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        try {

                            int responsecode=patientUUIDResponsemodelCall.execute().raw().code();
                            if(responsecode==204){
                                String delteQuery = "DELETE FROM encounter WHERE visit_id='" + visitID + "' and openmrs_visit_uuid='" + visitUUID + "' and encounter_type='EMERGENCY'";
                                success=true;
                                db.execSQL(delteQuery);
                            }
                            else if(responsecode==403){
                                Log.d("403","unauthorized"+responsecode);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cursor.moveToNext();
                    }
                }

            }
            cursor.close();
        }
        return success;
    }

//    private void createNotification(String message) {
//        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("Visit Data Update")
//                .setContentText(message);
//        mNotifyManager.notify(mId, mBuilder.build());
//    }
}
