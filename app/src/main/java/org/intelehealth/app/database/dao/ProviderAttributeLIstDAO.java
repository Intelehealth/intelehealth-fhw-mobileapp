package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.Uuid_Value;
import org.intelehealth.app.models.dto.ProviderAttributeListDTO;
import org.intelehealth.app.utilities.DeviceUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

    private static final int BATCH_SIZE = DeviceUtils.getOptimalBatchSize();
    private static final String SPECIALITY_ATTR_UUID =
            "ed1715f5-93e2-404e-b3c9-2a2d9600f062";

    /**
     * @param list
     * @return
     * @throws DAOException
     */
    public boolean insertProvidersAttributeListV2(List<ProviderAttributeListDTO> list) throws DAOException {

        SQLiteDatabase db = null;
        try {
            db = IntelehealthApplication
                    .inteleHealthDatabaseHelper
                    .getWriteDb();

            ContentValues values = new ContentValues();

            int processed = 0;

            for (int i = 0; i < list.size(); i++) {

                ProviderAttributeListDTO dto = list.get(i);

                // Skip irrelevant rows BEFORE transaction
                if (dto.getVoided() != 0 ||
                        !SPECIALITY_ATTR_UUID.equalsIgnoreCase(dto.getAttributetypeuuid())) {
                    continue;
                }

                if (processed % BATCH_SIZE == 0) {
                    db.beginTransaction();
                }

                values.clear();
                bindProviderAttribute(values, dto);

                db.insertWithOnConflict(
                        "tbl_dr_speciality",
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );

                processed++;

                if (processed % BATCH_SIZE == 0) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            }

            // Close last open transaction
            if (processed % BATCH_SIZE != 0) {
                db.setTransactionSuccessful();
                db.endTransaction();
            }

            return true;

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param values
     * @param dto
     */
    private void bindProviderAttribute(
            ContentValues values,
            ProviderAttributeListDTO dto
    ) {
        values.put("uuid", dto.getUuid());
        values.put("provideruuid", dto.getProvideruuid());
        values.put("attributetypeuuid", dto.getAttributetypeuuid());
        values.put("value", dto.getValue());
        values.put("voided", dto.getVoided());
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

            if (attributeListDTO.getVoided() == 0 &&
                    attributeListDTO.getAttributetypeuuid().equalsIgnoreCase("ed1715f5-93e2-404e-b3c9-2a2d9600f062")) {
                createdRecordsCount = db.insertWithOnConflict("tbl_dr_speciality", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                /*if (createdRecordsCount != -1) {
                    CustomLog.d("SPECI", "SIZEXXX: " + createdRecordsCount);
                } else {
                    CustomLog.d("SPECI", "SIZEXXX: " + createdRecordsCount);
                }*/

            }


        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public List<String> getAllValues() {
        List<String> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        //db.beginTransaction();
        String selectionArgs[] = {"ed1715f5-93e2-404e-b3c9-2a2d9600f062", "0"};
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_dr_speciality WHERE " +
                "attributetypeuuid = ? AND voided = ?", selectionArgs); //checking....

        ProviderAttributeListDTO dto = new ProviderAttributeListDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                dto = new ProviderAttributeListDTO();
                dto.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                listDTOArrayList.add(dto.getValue());
            }
        }

        sortSpecialties(listDTOArrayList);
        idCursor.close();
        // db.setTransactionSuccessful();
        //db.endTransaction();
//        db.close();
        return listDTOArrayList;
    }


    public List<Uuid_Value> getSpeciality_Uuid_Value() {
        List<Uuid_Value> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
//        db.close();
        return listDTOArrayList;
    }


    private void sortSpecialties(List<String> specialtyList) {
        String gpSpecialtyString = "General Physician";
        Collections.sort(specialtyList);
        if (specialtyList.contains(gpSpecialtyString)) {
            specialtyList.remove(gpSpecialtyString);
            specialtyList.add(0, gpSpecialtyString);
        }
    }
}