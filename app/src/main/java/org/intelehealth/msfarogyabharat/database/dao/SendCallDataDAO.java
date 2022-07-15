package org.intelehealth.msfarogyabharat.database.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.models.SendCallData;
import org.intelehealth.msfarogyabharat.models.dto.PatientDTO;
import org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;

import java.util.List;

public class SendCallDataDAO  {

    private int updatecount = 0;
    private long createdRecordsCount = 0;

    public boolean insertCallData(SendCallData sendCallData) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            createCallData(sendCallData, db);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;
    }

    public boolean createCallData(SendCallData callData, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("id", 1);
            values.put("state", callData.getState());
            values.put("district", callData.getDistrict());
            values.put("facilityName", callData.getFacility());
            values.put("dateOfCalls", callData.getCallDate());
            values.put("status", callData.getCallStatus());
            values.put("actionIfCompleted", callData.getCallAction());
            values.put("callNumber", callData.getCallNumber());
            values.put("remarks", callData.getRemarks());
            values.put("callStartTime", callData.getCallStartTime());
            values.put("callEndTime", callData.getCallEndTime());
            createdRecordsCount = db.insertWithOnConflict("tbl_ivr_call_details", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;

    }

}
