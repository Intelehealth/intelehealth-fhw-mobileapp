package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.ProviderAttributeDTO;
import org.intelehealth.app.utilities.exception.DAOException;

public class ProviderAttributeDAO {

    public boolean createProviderAttribute(ProviderAttributeDTO providerAttributeDTO) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues contentValues = new ContentValues();
        boolean isInserted = true;

        try {
            contentValues.put("uuid", providerAttributeDTO.getUuid());
            contentValues.put("provider_uuid", providerAttributeDTO.getProvider_uuid());
            contentValues.put("value", providerAttributeDTO.getValue());
            contentValues.put("provider_attribute_type_uuid", providerAttributeDTO.getProvider_attribute_type_uuid());
            contentValues.put("voided", providerAttributeDTO.getVoided());
            db.insertWithOnConflict("tbl_provider_attribute", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException exception) {
            isInserted = false;
            throw new DAOException(exception.getMessage(), exception);
        }

        return isInserted;
    }
}
