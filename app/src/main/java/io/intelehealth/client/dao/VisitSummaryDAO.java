package io.intelehealth.client.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VisitSummaryDAO {

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
}
