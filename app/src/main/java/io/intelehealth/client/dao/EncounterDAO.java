package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.EncounterDTO;
import io.intelehealth.client.exception.DAOException;

public class EncounterDAO {

    long createdRecordsCount = 0;
    int updatecount = 0;
    private SQLiteDatabase db = null;

    public boolean insertEncounter(List<EncounterDTO> encounterDTOS) throws DAOException {
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        try {
            for (EncounterDTO encounter : encounterDTOS) {
                Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_encounter where uuid = ?", new String[]{encounter.getUuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
//                        Logger.logD("update", "update has to happen");
                        updateEncounters(encounter);
                    }
                } else {
//                    Logger.logD("insert", "insert has to happen");
                    createEncounters(encounter);
                }
                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isInserted;
    }

    private boolean createEncounters(EncounterDTO encounter) throws DAOException {
        boolean isCreated = false;
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {

//            for (EncounterDTO encounter : encounterDTOS) {
//                Logger.logD("insert", "insert has to happen");
                values.put("uuid", encounter.getUuid());
                values.put("visituuid", encounter.getVisituuid());
                values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", encounter.getSyncd());
                values.put("voided", encounter.getVoided());
//                Logger.logD("pulldata", "datadumper" + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            db.setTransactionSuccessful();
//            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;
    }

    private boolean updateEncounters(EncounterDTO encounter) throws DAOException {
        boolean isCreated = false;
        db.beginTransaction();

        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
//            for (EncounterDTO encounter : encounterDTOS) {
//                Logger.logD("update", "update has to happen");
                values.put("visituuid", encounter.getVisituuid());
                values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", encounter.getSyncd());
                values.put("voided", encounter.getVoided());
//                Logger.logD("pulldata", "datadumper" + values);
                updatecount = db.updateWithOnConflict("tbl_encounter", values, selection, new String[]{encounter.getUuid()}, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            db.setTransactionSuccessful();
//            Logger.logD("updated", "updatedrecords count" + updatecount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;
    }

}
