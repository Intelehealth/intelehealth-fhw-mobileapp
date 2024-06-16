package org.intelehealth.ekalarogya.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.models.FamilyMemberRes;
import org.intelehealth.ekalarogya.models.dto.VisitAttributeDTO;
import org.intelehealth.ekalarogya.utilities.UuidDictionary;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

/**
 * Created by Prajwal Waingankar
 * on 20-Jul-20.
 * Github: prajwalmw
 */


public class VisitAttributeListDAO {
    private long createdRecordsCount = 0;

    public boolean insertVisitAttributeList(List<VisitAttributeDTO> visitAttributeDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
                createVisitAttributeList(visitDTO, db);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;
    }

    private boolean createVisitAttributeList(VisitAttributeDTO visitDTO, SQLiteDatabase db) throws DAOException {

        boolean isCreated = true;
        ContentValues values = new ContentValues();
        String where = "visit_uuid=?";
        String whereArgs[] = {visitDTO.getVisit_uuid()};
        try {
            values.put("uuid", visitDTO.getUuid());
            values.put("visit_uuid", visitDTO.getVisit_uuid());
            values.put("value", visitDTO.getValue());
            values.put("visit_attribute_type_uuid", visitDTO.getVisit_attribute_type_uuid());
            values.put("voided", visitDTO.getVoided());
            values.put("sync", "1");
            createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;
    }

    public String getVisitAttributesList_specificVisit(String VISITUUID) {
        String isValue = "";
        Log.d("specc", "spec_fun: " + VISITUUID);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ?", new String[]{VISITUUID});

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                isValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                Log.d("specc", "spec_3: " + isValue);
            }
        } else {
            isValue = "EMPTY";
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();

        Log.d("specc", "spec_4: " + isValue);
        return isValue;
    }

    public boolean insertVisitAttributes(String visitUuid, String speciality_selected) throws DAOException {
        boolean isInserted = false;

        Log.d("SPINNER", "SPINNER_Selected_visituuid_logs: " + visitUuid);
        Log.d("SPINNER", "SPINNER_Selected_value_logs: " + speciality_selected);

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", speciality_selected);
            values.put("visit_attribute_type_uuid", "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            if (count != -1) isInserted = true;

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        Log.d("isInserted", "isInserted: " + isInserted);
        return isInserted;
    }

    public boolean insertIsNcdVisitAttribute(String visitUuid, String isNcdVisit) throws DAOException {
        boolean isInserted = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", isNcdVisit);
            values.put("visit_attribute_type_uuid", AppConstants.IS_NCD_VISIT_ATTRIBUTE);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (count != -1) {
                isInserted = true;
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;
    }

    //Inserting Selected State in Visit Attribute
    public boolean insertVisitAttributesState(String visitUuid, String state) throws DAOException {

        boolean isInserted = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", state);
            values.put("visit_attribute_type_uuid", "0e798578-96c1-450b-9927-52e45485b151");
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            if (count != -1) isInserted = true;

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        Log.d("isInserted", "isInserted: " + isInserted);
        return isInserted;

    }

    public boolean insertVisitAttributesUploadTime(String visitUuid, String uploadTime) throws DAOException {
        boolean isInserted = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", uploadTime);
            values.put("visit_attribute_type_uuid", UuidDictionary.ATTRIBUTE_TIME_OF_UPLOAD_BUTTON_CLICK);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (count != -1) {
                isInserted = true;
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        Log.d("isInserted", "isInserted: " + isInserted);
        return isInserted;
    }

    public boolean insertVisitAttributesChiefComplaintTitle(String visitUuid, String value) throws DAOException {
        boolean isInserted = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", value);
            values.put("visit_attribute_type_uuid", UuidDictionary.VISIT_ATTRIBUTE_CHIEF_COMPLAINT_TITLE);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (count != -1) {
                isInserted = true;
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;
    }

    public static Boolean isNcdVisit(String visitUuid) {
        boolean isNcdVisit = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        try {
            Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ? AND visit_attribute_type_uuid = ?", new String[]{visitUuid, AppConstants.IS_NCD_VISIT_ATTRIBUTE});
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                if (value.equalsIgnoreCase("true")) {
                    isNcdVisit = true;
                }
            }
            cursor.close();
        } catch (SQLException exception) {
            FirebaseCrashlytics.getInstance().recordException(exception);
        } finally {
            db.endTransaction();
        }

        return isNcdVisit;
    }

    public static int deleteVisitAttributeUsingVisitUuid(String visitUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String table = "tbl_visit_attribute";
        String whereClause = "visit_uuid=?";
        String[] whereArgs = new String[]{String.valueOf(visitUuid)};
        return db.delete(table, whereClause, whereArgs);
    }
}
