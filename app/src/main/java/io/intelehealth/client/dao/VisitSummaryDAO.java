package io.intelehealth.client.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;

import io.intelehealth.client.api.retrofit.RestApi;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.network.ApiClient;
import io.intelehealth.client.utilities.SessionManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION.SDK_INT;

public class VisitSummaryDAO {
    private SessionManager sessionManger;

    public String getVisitUUID(String visitID, SQLiteDatabase db) {
        String visitUUID = "";
        String[] columnsToReturn = {"openmrs_visit_uuid"};
        String visitIDorderBy = "start_datetime";
        String visitIDSelection = "_id = ?";
        String[] visitIDArgs = {visitID};
        final Cursor visitIDCursor = db.query("visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        if(visitIDCursor !=null && visitIDCursor.moveToFirst() && visitIDCursor.getCount()>0)
        {
            visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("openmrs_visit_uuid"));
        }
        if(visitIDCursor!=null)
            visitIDCursor.close();
        if (visitUUID==null)
            visitUUID="";

        return visitUUID;
    }
    public boolean removeEncounterEmergency(final String visitID, final String visitUUID, final SQLiteDatabase db){
        RestApi apiInterface;
        sessionManger=new SessionManager(IntelehealthApplication.getAppContext());
        final boolean[] success = {false};
        apiInterface = ApiClient.getApiClient().create(RestApi.class);
//        apiInterface.DELETE_ENCOUNTER("").execute();
        if (visitID != null && visitUUID != null) {
            String selectQuery = "SELECT openmrs_encounter_id FROM encounter WHERE (visit_id='" + visitID + "' and openmrs_visit_uuid='" + visitUUID + "') and encounter_type='EMERGENCY'";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(cursor.getColumnIndex("openmrs_encounter_id"));
                        Call<Void> patientUUIDResponsemodelCall = apiInterface.DELETE_ENCOUNTER(name,"Basic "+sessionManger.getEncoded());
patientUUIDResponsemodelCall.enqueue(new Callback<Void>() {
    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.code()==204){
            String delteQuery = "DELETE FROM encounter WHERE visit_id='" + visitID + "' and openmrs_visit_uuid='" + visitUUID + "' and encounter_type='EMERGENCY'";
                                success[0] =true;
                                db.execSQL(delteQuery);
        }
    }

    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        Log.d("onfailure", "onFailure: "+t.getMessage());
    }
});
//                        try {
//                            patientUUIDResponsemodelCall.execute().body();
//                            if(patientUUIDResponsemodelCall.isExecuted()){
//                                    String delteQuery = "DELETE FROM encounter WHERE visit_id='" + visitID + "' and openmrs_visit_uuid='" + visitUUID + "' and encounter_type='EMERGENCY'";
//                                success=true;
//                                db.execSQL(delteQuery);
//}
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        try {
//                            if (patientUUIDResponsemodelCall.execute().isSuccessful()) {
//                                String delteQuery = "DELETE FROM encounter WHERE visit_id='" + visitID + "' and openmrs_visit_uuid='" + visitUUID + "' and encounter_type='EMERGENCY'";
//                                success=true;
//    //                            db.execSQL(delteQuery);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        cursor.moveToNext();
                    }
                }

            }
            cursor.close();
        }
        return success[0];
    }
}
