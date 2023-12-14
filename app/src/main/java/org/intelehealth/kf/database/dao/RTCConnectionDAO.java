package org.intelehealth.kf.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.models.dto.RTCConnectionDTO;
import org.intelehealth.kf.utilities.exception.DAOException;

public class RTCConnectionDAO {

    private String tag = RTCConnectionDAO.class.getSimpleName();
    private long createdRecordsCount = 0;

    public boolean insert(RTCConnectionDTO connectionDTO) throws DAOException {
        if (getByVisitUUID(connectionDTO.getVisitUUID()) != null) return false;
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("uuid", connectionDTO.getUuid());
            values.put("visit_uuid", connectionDTO.getVisitUUID());
            values.put("connection_info", connectionDTO.getConnectionInfo());

            createdRecordsCount = db.insertWithOnConflict("tbl_rtc_connection_log", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();

        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
        return isInserted;
    }


    public RTCConnectionDTO getByVisitUUID(String visitUUID) {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_rtc_connection_log where visit_uuid = ?", new String[]{visitUUID});
        RTCConnectionDTO connectionDTO = null;
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                connectionDTO = new RTCConnectionDTO();
                connectionDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                connectionDTO.setVisitUUID(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                connectionDTO.setConnectionInfo(idCursor.getString(idCursor.getColumnIndexOrThrow("connection_info")));
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return connectionDTO;
    }

}
