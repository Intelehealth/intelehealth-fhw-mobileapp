package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.models.dto.ConceptAttributeTypeDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;

public class ConceptAttributeTypeDAO {

    private long createdRecordsCount = 0;

    public boolean insertConcepts(List<ConceptAttributeTypeDTO> conceptAttributeTypeDTO) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (ConceptAttributeTypeDTO conceptAttributeTypeDTO1 : conceptAttributeTypeDTO) {
                createConcepts(conceptAttributeTypeDTO1, db);
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

    public boolean createConcepts(ConceptAttributeTypeDTO conceptAttributeTypeDTO, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", conceptAttributeTypeDTO.getUuid());
            values.put("name", conceptAttributeTypeDTO.getName());
            values.put("retired", conceptAttributeTypeDTO.getRetired());
            createdRecordsCount = db.insertWithOnConflict("tbl_uuid_dictionary", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;
    }

}
