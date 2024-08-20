package org.intelehealth.vikalphelpline.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import org.intelehealth.vikalphelpline.app.AppConstants;
import org.intelehealth.vikalphelpline.models.dto.VisitAttributeDTO;
import org.intelehealth.vikalphelpline.utilities.exception.DAOException;

/**
 * Created by Prajwal Waingankar
 * on 20-Jul-20.
 * Github: prajwalmw
 */


public class VisitAttributeListDAO {
    private long createdRecordsCount = 0;

    public boolean insertProvidersAttributeList(List<VisitAttributeDTO> visitAttributeDTOS)
            throws DAOException {

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

//            values.put("speciality_value", visitDTO.getValue());
            values.put("uuid", visitDTO.getUuid());
            values.put("visit_uuid", visitDTO.getVisit_uuid());
            values.put("value", visitDTO.getValue());
            values.put("visit_attribute_type_uuid", visitDTO.getVisit_attribute_type_uuid());
            values.put("voided", visitDTO.getVoided());
            values.put("sync", "1");

            if(visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase("3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d"))
            {
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                if(createdRecordsCount != -1)
                {
                    Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
                }
                else
                {
                    Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
                }
            }
        }
        catch (SQLException e)
        {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        finally {

        }

        return isCreated;
    }

    public String getVisitAttributesList_specificVisit(String VISITUUID)
    {
        String isValue = "";
        Log.d("specc", "spec_fun: "+ VISITUUID);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ?",
                new String[]{VISITUUID});

        if(cursor.getCount() != 0)
        {
            while (cursor.moveToNext())
            {
                isValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                Log.d("specc", "spec_3: "+ isValue);
            }
        }
        else
        {
            isValue = "EMPTY";
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        Log.d("specc", "spec_4: "+ isValue);
        return  isValue;
    }

    public boolean insertVisitAttributes(String visitUuid, String speciality_selected) throws
            DAOException {
        boolean isInserted = false;

        Log.d("SPINNER", "SPINNER_Selected_visituuid_logs: "+ visitUuid);
        Log.d("SPINNER", "SPINNER_Selected_value_logs: "+ speciality_selected);

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try
        {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", speciality_selected);
            values.put("visit_attribute_type_uuid", "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);

            if(count != -1)
                isInserted = true;

            db.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            db.endTransaction();
        }

        Log.d("isInserted", "isInserted: "+isInserted);
        return isInserted;
    }

    //Inserting Selected State in Visit Attribute
    public boolean insertVisitAttributesState(String visitUuid, String state) throws
            DAOException {

        boolean isInserted = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try
        {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", state);
            values.put("visit_attribute_type_uuid", "0e798578-96c1-450b-9927-52e45485b151");
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);

            if(count != -1)
                isInserted = true;

            db.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            db.endTransaction();
        }

        Log.d("isInserted", "isInserted: "+isInserted);
        return isInserted;

    }

    public boolean insertVisitAttribute(String visitUuid, String attributeUid, String value) throws
            DAOException {
        boolean isInserted = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try
        {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", value);
            values.put("visit_attribute_type_uuid", attributeUid);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);

            if(count != -1)
                isInserted = true;

            db.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            db.endTransaction();
        }

        Log.d("isInserted", "isInserted: "+isInserted);
        return isInserted;
    }
}
