package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.VisitDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.DateAndTimeUtils;

public class VisitsDAO {


    private SQLiteDatabase db = null;
    private long createdRecordsCount = 0;
    private int updatecount = 0;

    public boolean insertVisitTemp(List<VisitDTO> visitDTOS) throws DAOException {
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        db.beginTransaction();
        try {

            for (VisitDTO visit : visitDTOS) {
//                Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_visit where uuid = ?", new String[]{visit.getUuid()});
//                if (cursor.getCount() != 0) {
//                    while (cursor.moveToNext()) {
//                        updateVisits(visit);
//                    }
//                } else {
//                    Logger.logD("insert", "insert has to happen");
                createVisits(visit);
//                }
//                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
            db.close();
        }

        return isInserted;
    }

    private boolean createVisits(VisitDTO visit) throws DAOException {
        boolean isCreated = true;
//        (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase())
//        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
//        db.beginTransaction();
        try {
//            for (VisitDTO visit : visitDTOS) {
//                Logger.logD("insert", "insert has to happen");
            values.put("uuid", visit.getUuid());
            values.put("patientuuid", visit.getPatientuuid());
            values.put("locationuuid", visit.getLocationuuid());
            values.put("visit_type_uuid", visit.getVisitTypeUuid());
            values.put("creator", visit.getCreator());
            values.put("startdate", DateAndTimeUtils.formatDateFromOnetoAnother(visit.getStartdate(),"MMM dd, yyyy hh:mm:ss a", "dd-mm-yyyy"));
            values.put("enddate", visit.getEnddate());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("synced", visit.getSyncd());
//                Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount = db.insertWithOnConflict("tbl_visit", null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
//            db.setTransactionSuccessful();
//            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
//            db.endTransaction();
        }
        return isCreated;
    }

    private boolean updateVisits(VisitDTO visit) throws DAOException {
        boolean isUpdated = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        db.beginTransaction();
        try {
//            AppConstants.inteleHealthDatabaseHelper.onCreate(db);

//            for (VisitDTO visit : visitDTOS) {
//                Logger.logD("update", "update has to happen");
            values.put("patientuuid", visit.getPatientuuid());
            values.put("locationuuid", visit.getLocationuuid());
            values.put("visit_type_uuid", visit.getVisitTypeUuid());
            values.put("creator", visit.getCreator());
            values.put("startdate", visit.getStartdate());
            values.put("enddate", visit.getEnddate());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("synced", visit.getSyncd());
//                Logger.logD("pulldata", "datadumper" + values);
            updatecount = db.updateWithOnConflict("tbl_visit", values, selection, new String[]{visit.getUuid()}, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            db.setTransactionSuccessful();
//            Logger.logD("updated", "updatedrecords count" + updatecount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

    public List<VisitDTO> unsyncedVisits() {
        List<VisitDTO> visitDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit where synced = ?", new String[]{"0"});
        VisitDTO visitDTO = new VisitDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                visitDTO = new VisitDTO();
                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                visitDTO.setCreator(idCursor.getInt(idCursor.getColumnIndexOrThrow("gender")));
                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
//                visitDTO.setSyncd(idCursor.get(idCursor.getColumnIndexOrThrow("phone_number")));
                visitDTOList.add(visitDTO);
            }
        }

        return visitDTOList;
    }

}