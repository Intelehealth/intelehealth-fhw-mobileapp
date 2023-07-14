package org.intelehealth.ezazi.database.dao;

import static org.intelehealth.ezazi.activities.patientDetailActivity.PatientDetailActivity.VISIT_HOLDER;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.models.dto.VisitAttributeDTO;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.exception.DAOException;

/**
 * Created by Prajwal Waingankar
 * on 20-Jul-20.
 * Github: prajwalmw
 */


public class VisitAttributeListDAO {
    private static final String TAG = "VisitAttributeListDAO";
    private long createdRecordsCount = 0;

    public boolean insertProvidersAttributeList(List<VisitAttributeDTO> visitAttributeDTOS)
            throws DAOException {
        Log.d(TAG, "insertProvidersAttributeList: " + new Gson().toJson(visitAttributeDTOS));

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            Log.d("SPECI", "SIZEVISTATTR: Total attr => " + visitAttributeDTOS.size());
            for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
                if (checkVisitAttributesExist(visitDTO, db)) updateVisitAttributes(visitDTO, db);
                else createVisitAttributeList(visitDTO, db);
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

    private boolean checkVisitAttributesExist(VisitAttributeDTO attribute, SQLiteDatabase db) {
        String[] args = {attribute.getVisitUuid(), attribute.getVisitAttributeTypeUuid()};
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_visit_attribute WHERE visit_uuid = ? AND visit_attribute_type_uuid = ?", args);
        return cursor.getCount() > 0;
    }

    private boolean updateVisitAttributes(VisitAttributeDTO attribute, SQLiteDatabase db) {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        String where = "visit_uuid=? AND visit_attribute_type_uuid=?";
        String whereArgs[] = {attribute.getVisitUuid(), attribute.getVisitAttributeTypeUuid()};
        try {
            Log.d("SPECI", "SIZEVISTATTR: Visit Id => " + attribute.getVisitUuid());
            Log.d("SPECI", "SIZEVISTATTR: VisitAttributeTypeUuid => " + attribute.getVisitAttributeTypeUuid());
//            values.put("speciality_value", visitDTO.getValue());
            values.put("voided", attribute.getVoided());
            values.put("sync", "1");
            values.put("value", attribute.getValue());

            createdRecordsCount = db.update("tbl_visit_attribute", values, where, whereArgs);

            if (createdRecordsCount != -1) {
                Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
            } else {
                Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
            }
        } catch (SQLException e) {
            isCreated = false;
        }
        return isCreated;
    }

    private boolean createVisitAttributeList(VisitAttributeDTO visitDTO, SQLiteDatabase db) throws DAOException {

        boolean isCreated = true;
        ContentValues values = new ContentValues();
        String where = "visit_uuid=?";
        String whereArgs[] = {visitDTO.getVisitUuid()};
        try {

//            values.put("speciality_value", visitDTO.getValue());
            values.put("uuid", visitDTO.getUuid());
            values.put("visit_uuid", visitDTO.getVisitUuid());
            values.put("value", visitDTO.getValue());
            values.put("visit_attribute_type_uuid", visitDTO.getVisitAttributeTypeUuid());
            values.put("voided", visitDTO.getVoided());
            values.put("sync", "1");

            if (visitDTO.getVisitAttributeTypeUuid().equalsIgnoreCase("3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d") || visitDTO.getVisitAttributeTypeUuid().equalsIgnoreCase(VISIT_HOLDER)) {
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            if (createdRecordsCount != -1) {
                Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
            } else {
                Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
            }
//            }
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public String getVisitAttributesList_specificVisit(String VISITUUID) {
        String isValue = "";
        Log.d("specc", "spec_fun: " + VISITUUID);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ?",
                new String[]{VISITUUID});

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
        db.close();

        Log.d("specc", "spec_4: " + isValue);
        return isValue;
    }

    public boolean insertVisitAttributes(String visitUuid, String speciality_selected, String visitAttrTypeUuid) throws
            DAOException {
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
            values.put("visit_attribute_type_uuid", visitAttrTypeUuid);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);

            if (count != -1)
                isInserted = true;

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

    public boolean updateVisitTypeAttributeUuid(String visitUUid, String providerUuid) throws DAOException {
        boolean isUpdated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        String whereclause = "visit_uuid=? AND visit_attribute_type_uuid=?";
        db.beginTransaction();
        try {
            values.put("value", providerUuid);
            values.put("voided", "0");
            values.put("sync", "0");
            createdRecordsCount1 = db.update("tbl_visit_attribute", values, whereclause, new String[]{visitUUid, VISIT_HOLDER});
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isUpdated;

    }


}
