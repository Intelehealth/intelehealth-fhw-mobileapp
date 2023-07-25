package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.Uuid_Value;
import org.intelehealth.app.models.dto.ProviderAttributeListDTO;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

/**
 * Created by Prajwal Waingankar
 * on 14-Jul-20.
 * Github: prajwalmw
 */


public class ProviderAttributeLIstDAO {
    private long createdRecordsCount = 0;

    public boolean insertProvidersAttributeList(List<ProviderAttributeListDTO> providerAttributeListDTOS)
            throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (ProviderAttributeListDTO providerAttributeListDTO : providerAttributeListDTOS) {
                createProvidersAttributeList(providerAttributeListDTO, db);
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

    private boolean createProvidersAttributeList(ProviderAttributeListDTO attributeListDTO, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", attributeListDTO.getUuid());
            values.put("provideruuid", attributeListDTO.getProvideruuid());
            values.put("attributetypeuuid", attributeListDTO.getAttributetypeuuid());
            values.put("value", attributeListDTO.getValue());
            values.put("voided", attributeListDTO.getVoided());

            if (attributeListDTO.getVoided() == 0)
            createdRecordsCount = db.insertWithOnConflict("tbl_provider_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            if (attributeListDTO.getVoided() == 0 &&
                    attributeListDTO.getAttributetypeuuid().equalsIgnoreCase("ed1715f5-93e2-404e-b3c9-2a2d9600f062")) {
                createdRecordsCount = db.insertWithOnConflict("tbl_dr_speciality", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                if (createdRecordsCount != -1) {
                    Log.d("SPECI", "SIZEXXX: " + createdRecordsCount);
                } else {
                    Log.d("SPECI", "SIZEXXX: " + createdRecordsCount);
                }

            }

        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public List<String> getAllValues(String appLanguage) {
        List<String> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        String selectionArgs[] = {"ed1715f5-93e2-404e-b3c9-2a2d9600f062", "0"};
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_dr_speciality WHERE " +
                "attributetypeuuid = ? AND voided = ?", selectionArgs); //checking....

        ProviderAttributeListDTO dto = new ProviderAttributeListDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                boolean isDoctorConsultSpecialty = false;

                dto = new ProviderAttributeListDTO();
                String specialtyValue = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                // Checks if the specialty is Doctor (General Consult) or not.
                // If it is - we'll mark the isDoctorConsultSpecialty as true;
                if (specialtyValue.trim().equalsIgnoreCase("Doctor (General Consult)"))
                    isDoctorConsultSpecialty = true;

                if (appLanguage.equalsIgnoreCase("ar")) {
                    dto.setValue(StringUtils.getProviderNameInArabic(specialtyValue));
                } else {
                    dto.setValue(specialtyValue);
                }

                // If isDoctorConsultSpecialty is true, we'll simply add the elements at the top of the Arraylist - requirement as per Programs Team - Added by Arpan Sircar
                if (isDoctorConsultSpecialty) {
                    listDTOArrayList.add(0, dto.getValue());
                } else {
                    if(!dto.getValue().equalsIgnoreCase("Admin") && !dto.getValue().equalsIgnoreCase("إداري"))
                        listDTOArrayList.add(dto.getValue());
                }
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return listDTOArrayList;
    }

    public List<String> getAllValuesForSecond(String appLanguage) {
        List<String> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        String selectionArgs[] = {"ed1715f5-93e2-404e-b3c9-2a2d9600f062", "0"};
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_dr_speciality WHERE " +
                "attributetypeuuid = ? AND voided = ?", selectionArgs); //checking....

        ProviderAttributeListDTO dto = new ProviderAttributeListDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                dto = new ProviderAttributeListDTO();
                String specialtyValue = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                if(specialtyValue.equalsIgnoreCase("Admin")) {
                    if (appLanguage.equalsIgnoreCase("ar")) {
                        dto.setValue(StringUtils.getProviderNameInArabic(specialtyValue));
                    } else {
                        dto.setValue(specialtyValue);
                    }
                    listDTOArrayList.add(dto.getValue());
                }
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return listDTOArrayList;
    }



    public List<Uuid_Value> getSpeciality_Uuid_Value() {
        List<Uuid_Value> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        String selectionArgs[] = {"ed1715f5-93e2-404e-b3c9-2a2d9600f062", "0"};
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_dr_speciality WHERE " +
                "attributetypeuuid = ? AND voided = ?", selectionArgs);

        ProviderAttributeListDTO dto = new ProviderAttributeListDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                dto = new ProviderAttributeListDTO();
                dto.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                dto.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                listDTOArrayList.add(new Uuid_Value(dto.getUuid(), dto.getValue()));
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return listDTOArrayList;
    }


}
