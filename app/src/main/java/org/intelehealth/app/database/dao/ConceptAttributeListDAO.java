package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.ConceptAttributeDTO;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;

/**
 * Created by Nishita Goyal
 * on 08-Nov-22.
 * Github: nishitagoyal
 */

public class ConceptAttributeListDAO {

    private long createdRecordsCount = 0;

    public boolean insertConceptAttributeList(List<ConceptAttributeDTO> conceptAttributeDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (ConceptAttributeDTO conceptDTO : conceptAttributeDTOS) {
                createConceptAttributeList(conceptDTO, db);
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

    private boolean createConceptAttributeList(ConceptAttributeDTO conceptDTO, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        db.beginTransaction();
        String uuid = conceptDTO.getConcept_uuid();
        try {
            values.put("price", conceptDTO.getValue());
            String whereClause = "uuid = ?";
            String[] whereArgs = {uuid};
            createdRecordsCount = db.update("tbl_uuid_dictionary", values, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            db.endTransaction();
        }
        return isCreated;
    }

    public String getConceptPrice(String attr) {
        String price = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT price FROM tbl_uuid_dictionary where name = ? COLLATE NOCASE", new String[]{attr});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
            }
        }
        cursor.close();

        return price;
    }

}
