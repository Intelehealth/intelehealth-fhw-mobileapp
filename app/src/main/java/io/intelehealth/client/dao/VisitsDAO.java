package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.application.AppConstants;
import io.intelehealth.client.dto.VisitDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;

public class VisitsDAO {


    private SQLiteDatabase db = null;
    private long createdRecordsCount = 0;
    private int updatecount = 0;

    public boolean insertVisitTemp(List<VisitDTO> visitDTOS) throws DAOException {
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        try {

            for (int i = 0; i < visitDTOS.size(); i++) {
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit where uuid = ?", new String[]{visitDTOS.get(i).getUuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        Logger.logD("updated", "update has to happen");
                        if (updateVisits(visitDTOS)) {
                            Logger.logD("updated", "update has to happen");
                        } else {
                            Logger.logD("failed", "failed to updated");
                        }
                    }
                } else {
                    Logger.logD("insert", "insert has to happen");
                    if (createVisits(visitDTOS)) {
                        Logger.logD("inserted", "sucessfully inserted");
                    } else {
                        Logger.logD("failed", "failed to inserted");
                    }
                }
                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;
    }

    private boolean createVisits(List<VisitDTO> visitDTOS) throws DAOException {
        boolean isCreated = true;
//        (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase())
//        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (VisitDTO visit : visitDTOS) {
//                Logger.logD("insert", "insert has to happen");
                values.put("uuid", visit.getUuid());
                values.put("patientuuid", visit.getPatientuuid());
                values.put("locationuuid", visit.getLocationuuid());
                values.put("visit_type_uuid", visit.getVisitTypeUuid());
                values.put("creator", visit.getCreator());
                values.put("startdate", visit.getStartdate());
                values.put("enddate", visit.getEnddate());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", visit.getSyncd());
//                Logger.logD("pulldata", "datadumper" + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_visit", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;
    }

    private boolean updateVisits(List<VisitDTO> visitDTOS) throws DAOException {
        boolean isUpdated = true;
//        (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        db.beginTransaction();
        try {
//            AppConstants.inteleHealthDatabaseHelper.onCreate(db);

            for (VisitDTO visit : visitDTOS) {
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
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + updatecount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

   /* private boolean mergeVisits(List<VisitDTO> visitsList) throws DAOException {
        boolean isMerged = true;
        try (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()) {
            String selectQuery = "SELECT * from tbl_patient";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {

                            cursor.moveToNext();
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return isMerged;
    }
*/
//    private boolean updateVisits() throws DAOException {
//        boolean isupdated = true;
//
//        try (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()) {
//            String selectQuery = "update tbl_visit set patient_uuid=(SELECT tbl_patient.uuid from tbl_patient where tbl_patient.openmrs_uuid=tbl_visit.openmrs_patientuuid)";
//            Cursor cursor = db.rawQuery(selectQuery, null);
//            if (cursor != null) {
//                if (cursor.moveToFirst()) {
//                    while (!cursor.isAfterLast()) {
//
//                        cursor.moveToNext();
//                    }
//                }
//            }
//            if (cursor != null) {
//                cursor.close();
//            }
//        } catch (SQLException e) {
//            throw new DAOException(e.getMessage(), e);
//        }
//        return isupdated;
//    }

}
