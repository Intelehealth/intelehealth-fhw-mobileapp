package io.intelehealth.client.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.intelehealth.client.utilities.SessionManager;


public class VisitSummaryDAO {
    private SessionManager sessionManger;

    public String getVisitUUID(String visitID, SQLiteDatabase db) {
        String visitUUID = "";
        String[] columnsToReturn = {"openmrs_visit_uuid"};
        String visitIDorderBy = "start_datetime";
        String visitIDSelection = "_id = ?";
        String[] visitIDArgs = {visitID};
        final Cursor visitIDCursor = db.query("visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
            visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("openmrs_visit_uuid"));
        }
        if (visitIDCursor != null)
            visitIDCursor.close();
        if (visitUUID == null)
            visitUUID = "";

        return visitUUID;
    }

    public String getEmergencyUUID(String visitID, SQLiteDatabase db) {
        String visitUUID = "";
        String emergencyQuery = "select openmrs_visit_uuid from encounter where visit_id='" + visitID + "' and encounter_type='EMERGENCY'";
        final Cursor visitIDCursor = db.rawQuery(emergencyQuery, null);
        if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
            visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("openmrs_visit_uuid"));
        }
        if (visitIDCursor != null)
            visitIDCursor.close();
        if (visitUUID == null)
            visitUUID = "";

        return visitUUID;
    }

}
