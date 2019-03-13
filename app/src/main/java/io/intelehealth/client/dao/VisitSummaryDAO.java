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
    public String getEmergencyUUID(String visitID, SQLiteDatabase db) {
        String visitUUID = "";
        String emergencyQuery="select openmrs_visit_uuid from encounter where visit_id='"+ visitID +"' and encounter_type='EMERGENCY'";
        final Cursor visitIDCursor = db.rawQuery(emergencyQuery,null);
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

}
