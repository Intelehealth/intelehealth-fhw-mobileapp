package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.shared.builder.QueryBuilder;
import org.intelehealth.app.utilities.exception.DAOException;

import java.lang.annotation.Target;

public class RTCConnectionDAO {

    private String tag = RTCConnectionDAO.class.getSimpleName();
    private long createdRecordsCount = 0;

    public boolean insert(RTCConnectionDTO connectionDTO) throws DAOException {
        if (getByVisitUUID(connectionDTO.getVisitUUID()) != null) return false;
        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
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

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String query = new QueryBuilder()
                .select(" * ")
                .from(" tbl_rtc_connection_log ")
                .where(" visit_uuid = '" + visitUUID + "' ")
                .limit(1)
                .build();
        Timber.tag(RTCConnectionDAO.class.getSimpleName()).d("RTCConnectionDTO getByVisitUUID =>%s", query);
        //db.beginTransaction();
        Cursor idCursor = db.rawQuery(query, null);
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
        //db.setTransactionSuccessful();
        //db.endTransaction();
//        db.close();

        return connectionDTO;
    }

}
