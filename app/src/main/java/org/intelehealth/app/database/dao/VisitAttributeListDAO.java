package org.intelehealth.app.database.dao;

import static org.intelehealth.app.utilities.UuidDictionary.ADDITIONAL_NOTES;
import static org.intelehealth.app.utilities.UuidDictionary.CONSULTATION_TYPE;
import static org.intelehealth.app.utilities.UuidDictionary.DIAGNOSIS;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;
import static org.intelehealth.app.utilities.UuidDictionary.SPECIALITY;
import static org.intelehealth.app.utilities.UuidDictionary.VISIT_UPLOAD_TIME;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Prajwal Waingankar
 * on 20-Jul-20.
 * Github: prajwalmw
 */


public class VisitAttributeListDAO extends BaseDao{
    private long createdRecordsCount = 0;
    private static final String TAG = "VisitAttributeListDAO";
    List<VisitAttributeDTO> visitAttributeDTOListForUpdate = new ArrayList<>();
    List<HashMap<String, Object>> visitAttributeDTOListForAdd = new ArrayList<>();
    public boolean insertProvidersAttributeList(List<VisitAttributeDTO> visitAttributeDTOS)
            throws DAOException {
        boolean isInserted = true;
        List<HashMap<String, Object>> visitsList = new ArrayList<>();
        for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
            if (visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(SPECIALITY) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(ADDITIONAL_NOTES) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(PRESCRIPTION_LINK) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(DIAGNOSIS) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(CONSULTATION_TYPE) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(VISIT_UPLOAD_TIME)) {
                visitsList.add(createVisitAttributeMap(visitDTO));
            }
        }
        executeInBackground(bulkInsert(visitsList));

       /* boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
                createVisitAttributeList(visitDTO, db);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
*/
        return isInserted;
    }

    private boolean createVisitAttributeList(VisitAttributeDTO visitDTO, SQLiteDatabase db) throws DAOException {

        boolean isCreated = true;
        ContentValues values = new ContentValues();
        String where = "visit_uuid=?";
        String whereArgs[] = {visitDTO.getVisit_uuid()};
        try {

//            values.put("speciality_value", visitDTO.getValue());
            values.put("uuid", visitDTO.getUuid());
            values.put("visit_uuid", visitDTO.getVisit_uuid());
            values.put("value", visitDTO.getValue());
            values.put("visit_attribute_type_uuid", visitDTO.getVisit_attribute_type_uuid());
            values.put("voided", visitDTO.getVoided());
            values.put("sync", "1");

            if (visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(SPECIALITY) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(ADDITIONAL_NOTES) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(PRESCRIPTION_LINK) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(DIAGNOSIS) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(CONSULTATION_TYPE) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(VISIT_UPLOAD_TIME)) {
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

//                if (createdRecordsCount != -1) {
//                    CustomLog.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
//                } else {
//                    CustomLog.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
//                }
            }
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public String getVisitAttributesList_specificVisit(String VISITUUID, String visit_attribute_type_uuid) {
        String isValue = "";

        if (VISITUUID != null) {
            CustomLog.d("specc", "spec_fun: " + VISITUUID);
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
            //db.beginTransaction();

            Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ? and " +
                            "visit_attribute_type_uuid = ? and voided = 0",
                    new String[]{VISITUUID, visit_attribute_type_uuid});

            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    isValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                    CustomLog.d("specc", "spec_3: " + isValue);
                }
            } else {
                isValue = "";
            }
            cursor.close();
            //db.setTransactionSuccessful();
            //db.endTransaction();
//            db.close();

            CustomLog.d("specc", "spec_4: " + isValue);
        }

        return isValue;
    }

    /**
     * Inserting Visit Attributes...
     *
     * @param visitUuid
     * @param value
     * @param attributeTypeUUID
     * @return
     * @throws DAOException
     */
    public boolean insertVisitAttributes(String visitUuid, String value, String attributeTypeUUID) throws
            DAOException {
        boolean isInserted = false;

        CustomLog.d("SPINNER", "SPINNER_Selected_visituuid_logs: " + visitUuid);
        CustomLog.d("SPINNER", "SPINNER_Selected_value_logs: " + value);

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", value);
            values.put("visit_attribute_type_uuid", attributeTypeUUID);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);

            if (count != -1)
                isInserted = true;

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        CustomLog.d("isInserted", "isInserted: " + isInserted);
        return isInserted;
    }

    /**
     * Whether this visit already carries the given attribute type. insertVisitAttributes generates a
     * fresh row uuid, so its CONFLICT_REPLACE cannot dedupe by visit and type — callers that must not
     * duplicate have to check first and update instead.
     */
    public boolean isAttributeExistForVisit(String visitUuid, String attributeTypeUUID) {
        boolean exists = false;
        if (visitUuid == null) return false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT uuid FROM tbl_visit_attribute WHERE visit_uuid = ? AND " +
                        "visit_attribute_type_uuid = ? AND voided = 0 LIMIT 1",
                new String[]{visitUuid, attributeTypeUUID})) {
            exists = cursor.moveToFirst();
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());
        }
        return exists;
    }

    /**
     * Updates an existing attribute value for a visit and marks the row unsynced so the next push
     * carries it.
     */
    public boolean updateVisitAttributes(String visitUuid, String value, String attributeTypeUUID)
            throws DAOException {
        boolean isUpdated = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            values.put("value", value);
            values.put("sync", "0");
            db.update("tbl_visit_attribute", values,
                    "visit_uuid = ? AND visit_attribute_type_uuid = ?",
                    new String[]{visitUuid, attributeTypeUUID});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isUpdated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

    /**
     * Fetching speciality value for the visit.
     *
     * @param visitUUID
     * @return
     */
    public static String fetchSpecialityValue(String visitUUID) {
        String specialityValue = "No data found";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        //db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT distinct(value) FROM tbl_visit_attribute WHERE visit_uuid=? and visit_attribute_type_uuid = ? and voided = 0",
                new String[]{visitUUID, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d"});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                specialityValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
            }
        }
        cursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();

        return specialityValue;
    }

    @Override
    String tableName() {
        return "tbl_visit_attribute";
    }
    public HashMap<String, Object> createVisitAttributeMap(VisitAttributeDTO visitDTO) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("uuid", visitDTO.getUuid());
        values.put("visit_uuid", visitDTO.getVisit_uuid());
        values.put("value", visitDTO.getValue());
        values.put("visit_attribute_type_uuid", visitDTO.getVisit_attribute_type_uuid());
        values.put("voided", visitDTO.getVoided());
        values.put("sync", "1");
        return values;
    }
    public boolean insertProvidersAttributeListAfterSetup(List<VisitAttributeDTO> visitAttributeDTOS)
            throws DAOException {
        Log.d(TAG, "insertProvidersAttributeListAfterSetup: visitAttributeDTOS : "+new Gson().toJson(visitAttributeDTOS));
        boolean isInserted = true;
        List<HashMap<String, Object>> visitsList = new ArrayList<>();
        List<VisitAttributeDTO> visitsListForUpdate = new ArrayList<>();
        for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
            if (visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(SPECIALITY) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(ADDITIONAL_NOTES) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(PRESCRIPTION_LINK) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(DIAGNOSIS) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(CONSULTATION_TYPE) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(VISIT_UPLOAD_TIME)) {
                // Repeat visit attributes with different uuids -
                //1 value and visit attribute type - both same - update new record  with pull api record -uuid
                //2 Value and visit attribute type both different then insert new record
                boolean isRecordExist =checkWhetherRecordExistOrNot(visitDTO);
                if(isRecordExist){
                    // cant update uuid because its primary key
                    visitsListForUpdate.add(visitDTO);
                }else{
                    visitsList.add(createVisitAttributeMap(visitDTO));
                }
            }
        }
        updateRecord(visitsListForUpdate,visitsList);
        return isInserted;
    }
/*
    public boolean checkWhetherRecordExistOrNot(VisitAttributeDTO visitDTO) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean isRecordExist = false;

        try {
            db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
            db.beginTransaction();
            String query;
            if(visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(UuidDictionary.SPECIALITY)){
                query = "SELECT * FROM tbl_visit_attribute WHERE visit_uuid = ? " +
                        "AND visit_attribute_type_uuid = ? " + " AND value = ? " +
                        "AND voided = 0 ";*/
/*+
                        "AND (sync = ? OR sync = ?) COLLATE NOCASE";*//*

                cursor = db.rawQuery(query, new String[]{visitDTO.getVisit_uuid(), visitDTO.getVisit_attribute_type_uuid(),visitDTO.getValue()});
            }else{
                query = "SELECT * FROM tbl_visit_attribute WHERE visit_uuid = ? " +
                        "AND visit_attribute_type_uuid = ? " +
                        "AND voided = 0 ";*/
/*+
                        "AND (sync = ? OR sync = ?) COLLATE NOCASE";*//*

                cursor = db.rawQuery(query, new String[]{visitDTO.getVisit_uuid(), visitDTO.getVisit_attribute_type_uuid()});
            }

            isRecordExist = (cursor != null && cursor.getCount() > 0);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.endTransaction();
            }
        }
        return isRecordExist;
    }
*/
    private void updateRecord(List<VisitAttributeDTO> visitAttributeDTOList, List<HashMap<String, Object>> visitsList) {
        Log.d(TAG, "updateRecord: visitAttributeDTOList : " + new Gson().toJson(visitAttributeDTOList));

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
      /*  ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {*/
        if(db.inTransaction()){
            db.endTransaction();
        }
            db.beginTransaction();
            try {
                for (VisitAttributeDTO visitAttributeDTO : visitAttributeDTOList) {
              /*      String updateQuery = "UPDATE tbl_visit_attribute SET sync = ? WHERE visit_uuid = ? AND visit_attribute_type_uuid = ? AND voided = 0";
                    db.execSQL(updateQuery, new Object[]{"1", visitAttributeDTO.getVisit_uuid(), visitAttributeDTO.getVisit_attribute_type_uuid()});*/
                    ContentValues values = new ContentValues();
                    values.put("sync", "1");

                    int updatedRows = db.update(
                            "tbl_visit_attribute",
                            values,
                            "visit_uuid = ? AND visit_attribute_type_uuid = ? AND voided = 0",
                            new String[]{visitAttributeDTO.getVisit_uuid(), visitAttributeDTO.getVisit_attribute_type_uuid()}
                    );
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                }
            }
        executeInBackground(bulkInsert(visitsList));

      /*  });

        executorService.shutdown();*/
    }
    private void collectDataForInsertAndUpdate(List<VisitAttributeDTO> dtos, int index) {
        Log.d(TAG, "20kaveri collectDataForInsertAndUpdate: original dtos : "+new Gson().toJson(dtos));
        if (index >= dtos.size()){
            updateExistingVisitAttribute(dtos, visitAttributeDTOListForAdd, visitAttributeDTOListForUpdate);
            return;
        }
        VisitAttributeDTO visitDTO = dtos.get(index);
        if (isRelevantAttribute(visitDTO)) {
            boolean exists = checkWhetherRecordExistOrNot(visitDTO);
            Log.d(TAG, "20kaveri collectDataForInsertAndUpdate: exists : "+exists);
            if (exists) {
                visitAttributeDTOListForUpdate.add(visitDTO);
            } else {
                visitAttributeDTOListForAdd.add(createVisitAttributeMap(visitDTO));
            }
        }
        collectDataForInsertAndUpdate(dtos, index + 1);
    }

    private boolean isRelevantAttribute(VisitAttributeDTO dto) {
        String uuid = dto.getVisit_attribute_type_uuid();
        return uuid.equalsIgnoreCase(SPECIALITY)
                || uuid.equalsIgnoreCase(ADDITIONAL_NOTES)
                || uuid.equalsIgnoreCase(PRESCRIPTION_LINK)
                || uuid.equalsIgnoreCase(DIAGNOSIS)
                || uuid.equalsIgnoreCase(CONSULTATION_TYPE)
                || uuid.equalsIgnoreCase(VISIT_UPLOAD_TIME);
    }
     public void  insertOrUpdateVisitAttributes(List<VisitAttributeDTO> dtos, int currentIndex){
         visitAttributeDTOListForUpdate = new ArrayList<>();
         visitAttributeDTOListForAdd = new ArrayList<>();

         collectDataForInsertAndUpdate(dtos, currentIndex);
        /*if(isDataCollected){
            updateExistingVisitAttribute(dtos,visitAttributeDTOListForAdd,visitAttributeDTOListForUpdate);*/
        }

    public boolean checkWhetherRecordExistOrNot(VisitAttributeDTO visitDTO) {
        boolean isRecordExist = false;

        String query = "SELECT * FROM tbl_visit_attribute WHERE visit_uuid = ? " +
                "AND visit_attribute_type_uuid = ? AND value = ? LIMIT 1";
        Log.d(TAG, "kaveri checkWhetherRecordExistOrNot: query : "+query);

        try (SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{
                     visitDTO.getVisit_uuid(),
                     visitDTO.getVisit_attribute_type_uuid(),
                     visitDTO.getValue()
             })) {

            if (cursor != null && cursor.moveToFirst()) {
                isRecordExist = true;

                StringBuilder record = new StringBuilder("Matched Record => ");
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    record.append(cursor.getColumnName(i)).append(": ")
                            .append(cursor.getString(i)).append(" | ");
                }
                Log.d("VisitAttrCheck", "kaveri  : "+ record.toString());
            }

        } catch (Exception e) {
            Log.e("VisitAttrCheck", "Error checking record existence: ", e);
        }

        return isRecordExist;
    }

/*public boolean checkWhetherRecordExistOrNot(VisitAttributeDTO visitDTO) {
    boolean isRecordExist = false;

    String query = "SELECT * FROM tbl_visit_attribute WHERE visit_uuid = ? " +
            "AND visit_attribute_type_uuid = ? AND value = ?" +
            "AND (sync = ? OR sync = ?) LIMIT 1";

    try (SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
         Cursor cursor = db.rawQuery(query, new String[]{
                 visitDTO.getVisit_uuid(),
                 visitDTO.getVisit_attribute_type_uuid(),
                 visitDTO.getValue(),
                 "0", "false"
         })) {

        if (cursor != null && cursor.moveToFirst()) {
            isRecordExist = true;
        }

    } catch (Exception e) {
        Log.e("VisitAttrCheck", "Error checking record existence: ", e);
    }

    return isRecordExist;
}*/

    private void updateExistingVisitAttribute(
            List<VisitAttributeDTO> visitAttributeDTOList,
            List<HashMap<String, Object>> visitAttributeDTOListForAdd,
            List<VisitAttributeDTO> visitAttributeDTOListForUpdate
    ) {
        Log.d(TAG, "20kaveri updateRecord: visitAttributeDTOList : " + new Gson().toJson(visitAttributeDTOList));
        Log.d(TAG, "20kaveri updateRecord: visitAttributeDTOListForAdd : " + new Gson().toJson(visitAttributeDTOListForAdd));
        Log.d(TAG, "20kaveri updateRecord: visitAttributeDTOListForUpdate : " + new Gson().toJson(visitAttributeDTOListForUpdate));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            SQLiteDatabase db = null;

            try {
                db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
                db.beginTransaction();

                List<String> visitUuidsList= new ArrayList<>();
                List<String> attrTypeList= new ArrayList<>();
                List<String> valuesList= new ArrayList<>();

                for (VisitAttributeDTO dto : visitAttributeDTOListForUpdate) {
                    visitUuidsList.add(dto.getVisit_uuid());
                    attrTypeList.add(dto.getVisit_attribute_type_uuid());
                    valuesList.add(dto.getValue());
                }

                String visitUuids = joinWithQuotes(visitUuidsList);
                String attrTypes  = joinWithQuotes(attrTypeList);
                String values     = joinWithQuotes(valuesList);

                String sql = "UPDATE tbl_visit_attribute " +
                        "SET sync = '1' " +
                        "WHERE voided = 0 " +
                        "AND visit_uuid IN (" + visitUuids + ") " +
                        "AND visit_attribute_type_uuid IN (" + attrTypes + ")"+
                        "AND value IN (" + values + ") ";
                Log.d(TAG, "20kaveri updateExistingVisitAttribute: sql : "+sql);
                db.execSQL(sql);
              /*  ContentValues updateValues = new ContentValues();
                updateValues.put("sync", "1");

                for (VisitAttributeDTO dto : visitAttributeDTOListForUpdate) {
                    db.update(
                            "tbl_visit_attribute",
                            updateValues,
                            "visit_uuid = ? AND visit_attribute_type_uuid = ? AND voided = 0",
                            new String[]{dto.getVisit_uuid(), dto.getVisit_attribute_type_uuid()}
                    );
                }*/

                if (!visitAttributeDTOListForAdd.isEmpty()) {
                    try {
                        Thread.sleep(1000); // Delay for 1 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }

                    insertVisitAttributes(db, visitAttributeDTOListForAdd);
                }
                if (!visitAttributeDTOListForAdd.isEmpty()) {
                    insertVisitAttributes(db, visitAttributeDTOListForAdd);
                }

                db.setTransactionSuccessful();

            } catch (Exception e) {
                Log.e(TAG, "Error in updateExistingVisitAttribute: ", e);
            } finally {
                if (db != null) {
                    db.endTransaction();
                }
            }
        });

        executor.shutdown();
    }
    private void insertVisitAttributes(SQLiteDatabase db, List<HashMap<String, Object>> rows) {
        if (rows.isEmpty()) return;

        // Define column names (must match the columns in the table)
        List<String> columns = Arrays.asList("uuid", "visit_uuid", "value", "visit_attribute_type_uuid", "voided", "sync");

        // Prepare SQL query (INSERT query with placeholders for each column)
        StringBuilder sql = new StringBuilder("INSERT INTO tbl_visit_attribute (");
        sql.append(TextUtils.join(", ", columns));
        sql.append(") VALUES (");
        sql.append(new String(new char[columns.size()]).replace("\0", "?, ").replaceAll(", $", ""));
        sql.append(")");

        // Compile the SQL statement
        SQLiteStatement statement = db.compileStatement(sql.toString());

        // Loop through each row in the list
        for (HashMap<String, Object> row : rows) {
            statement.clearBindings();

            // Iterate through each column and bind the corresponding value from the row map
            int index = 1;
            for (String column : columns) {
                Object value = row.get(column);  // Get the value for this column

                if (value instanceof String) {
                    statement.bindString(index, (String) value);
                } else if (value instanceof Integer) {
                    statement.bindLong(index, (Integer) value);
                } else if (value instanceof Long) {
                    statement.bindLong(index, (Long) value);
                } else if (value instanceof Double) {
                    statement.bindDouble(index, (Double) value);
                } else if (value instanceof byte[]) {
                    statement.bindBlob(index, (byte[]) value);
                } else {
                    statement.bindNull(index); // Bind NULL if value is missing
                }
                index++;
            }

            // Execute the insert for this row
            try {
                statement.executeInsert();
            } catch (Exception e) {
                Log.e(TAG, "Insert failed for row: " + row, e);
            }
        }
    }
/*
    private void updateExistingVisitAttribute(List<VisitAttributeDTO> visitAttributeDTOList, List<HashMap<String, Object>> visitAttributeDTOListForAdd,List<VisitAttributeDTO> visitAttributeDTOListForUpdate) {
        Log.d(TAG, "updateRecord: visitAttributeDTOList : " + new Gson().toJson(visitAttributeDTOList));
        Log.d(TAG, "updateRecord: visitAttributeDTOListForAdd : " + new Gson().toJson(visitAttributeDTOListForAdd));
        Log.d(TAG, "updateRecord: visitAttributeDTOListForUpdate : " + new Gson().toJson(visitAttributeDTOListForUpdate));

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
      */
/*  ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {*//*

        if(db.inTransaction()){
            db.endTransaction();
        }
        db.beginTransaction();
        try {
            for (VisitAttributeDTO visitAttributeDTO : visitAttributeDTOList) {
              */
/*      String updateQuery = "UPDATE tbl_visit_attribute SET sync = ? WHERE visit_uuid = ? AND visit_attribute_type_uuid = ? AND voided = 0";
                    db.execSQL(updateQuery, new Object[]{"1", visitAttributeDTO.getVisit_uuid(), visitAttributeDTO.getVisit_attribute_type_uuid()});*//*

                ContentValues values = new ContentValues();
                values.put("sync", "1");

                int updatedRows = db.update(
                        "tbl_visit_attribute",
                        values,
                        "visit_uuid = ? AND visit_attribute_type_uuid = ? AND voided = 0",
                        new String[]{visitAttributeDTO.getVisit_uuid(), visitAttributeDTO.getVisit_attribute_type_uuid()}
                );
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
        executeInBackground(bulkInsert(visitAttributeDTOListForAdd));

      */
/*  });

        executorService.shutdown();*//*

    }
*/
String joinWithQuotes(List<String> list) {
    return list.stream()
            .map(s -> "'" + s + "'")
            .collect(Collectors.joining(","));
}
}
