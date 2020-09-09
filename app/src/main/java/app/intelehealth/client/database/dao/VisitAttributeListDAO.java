package app.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.models.dto.ProviderAttributeListDTO;
import app.intelehealth.client.models.dto.VisitAttributeDTO;
import app.intelehealth.client.utilities.exception.DAOException;

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
        String where = "uuid=?";
        String whereArgs[] = {visitDTO.getVisit_uuid()};
        try {

            values.put("speciality_value", visitDTO.getValue());

            if(visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase("3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d"))
            {
                createdRecordsCount = db.update("tbl_visit", values, where, whereArgs);

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

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT speciality_value FROM tbl_visit WHERE uuid = ?",
                new String[]{VISITUUID});

        if(cursor.getCount() != 0)
        {
            while (cursor.moveToNext())
            {
                isValue = cursor.getString(cursor.getColumnIndexOrThrow("speciality_value"));
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
            values.put("uuid", AppConstants.NEW_UUID);
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
}
