package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;
import java.util.UUID;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

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

            if(visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase("443d91e7-3897-4307-a549-787da32e241e"))
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

            if(visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase("ba1e259f-8911-439d-abde-fb6c24c1e3c2"))
            {
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid =? AND visit_attribute_type_uuid =?", new String[]{visitDTO.getVisit_uuid(),visitDTO.getVisit_attribute_type_uuid()});
                if(cursor.getCount() <= 0){
                    createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    if(createdRecordsCount != -1)
                    {
                        Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
                    }
                    else
                    {
                        Log.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
                    }
                }else{
                    try {
                        db.update("tbl_visit_attribute",
                                values,
                                "visit_uuid = ? AND " + " visit_attribute_type_uuid = ?",
                                new String[]{visitDTO.getVisit_uuid(), "ba1e259f-8911-439d-abde-fb6c24c1e3c2"});
                    } catch (SQLException sql) {
                        throw new DAOException(sql.getMessage());
                    }
                }
                cursor.close();
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

        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ? AND + visit_attribute_type_uuid = ?",
                new String[]{VISITUUID,"3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d"});

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

    public String getVisitAttributesList_caseModeVisit(String VISITUUID)
    {
        String isValue = "";
        Log.d("specc", "spec_fun: "+ VISITUUID);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ? AND + visit_attribute_type_uuid = ?",
                new String[]{VISITUUID,"443d91e7-3897-4307-a549-787da32e241e"});

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

    public String getVisitAttributesList_medicineProvideVisit(String VISITUUID)
    {
        String isValue = "";
        Log.d("specc", "spec_fun: "+ VISITUUID);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ? AND + visit_attribute_type_uuid = ?",
                new String[]{VISITUUID,"ba1e259f-8911-439d-abde-fb6c24c1e3c2"});

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

    public boolean insertVisitCaseModeAttributes(String visitUuid, String casemode_selected) throws
            DAOException {
        boolean isInserted = false;

        Log.d("SPINNER", "SPINNER_Selected_visituuid_logs: "+ visitUuid);
        Log.d("SPINNER", "SPINNER_Selected_value_logs: "+ casemode_selected);

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try
        {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", casemode_selected);
            values.put("visit_attribute_type_uuid", "443d91e7-3897-4307-a549-787da32e241e");
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

    public boolean insertVisitMedicineProvidedAttributes(String visitUuid, String medicineprovide) throws
            DAOException {
        boolean isInserted = false;

        Log.d("SPINNER", "SPINNER_Selected_visituuid_logs: "+ visitUuid);
        Log.d("SPINNER", "SPINNER_Selected_value_logs: "+ medicineprovide);

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try
        {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", medicineprovide);
            values.put("visit_attribute_type_uuid", "ba1e259f-8911-439d-abde-fb6c24c1e3c2");
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

    public void updateVisitMedicineProvidedAttributes(String visitUuid, String medicineprovide) throws
            DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("value", medicineprovide);
            values.put("sync", "0");
            db.update("tbl_visit_attribute",
                    values,
                    "visit_uuid = ? AND " + " visit_attribute_type_uuid = ?",
                    new String[]{visitUuid, "ba1e259f-8911-439d-abde-fb6c24c1e3c2"});
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD("tbl_visit_attribute", "tbl_visit_attribute" + sql.getMessage());
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }
    }
}
