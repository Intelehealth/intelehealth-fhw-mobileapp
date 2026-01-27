package org.intelehealth.app.database.dao;

import static org.intelehealth.app.utilities.UuidDictionary.ADDITIONAL_NOTES;
import static org.intelehealth.app.utilities.UuidDictionary.IS_NCD_VISIT_ATTRIBUTE;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;
import static org.intelehealth.app.utilities.UuidDictionary.SPECIALITY;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.notification.LocalPrescriptionInfo;
import org.intelehealth.app.ayu.visit.notification.ReminderReceiver;
import org.intelehealth.app.ayu.visit.notification.ReminderWorker;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DeviceUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Prajwal Waingankar
 * on 20-Jul-20.
 * Github: prajwalmw
 */


public class VisitAttributeListDAO {
    private long createdRecordsCount = 0;
    private static final String TAG = "VisitAttributeListDAO";

    private List<LocalPrescriptionInfo> prescriptionDataList = new ArrayList<>();
    private List<String> prevVisitIdList = new ArrayList<>();
    private int unsharedPrescriptionCount;
    SharedPreferences mSharedPreference;


    public boolean insertProvidersAttributeList(List<VisitAttributeDTO> visitAttributeDTOS)
            throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            mSharedPreference = IntelehealthApplication.getAppContext().getSharedPreferences(IntelehealthApplication.getAppContext().getString(R.string.prescription_share_key), Context.MODE_PRIVATE);
            String prescriptionListJson = mSharedPreference.getString(AppConstants.PRESCRIPTION_DATA_LIST, "");
            if (!prescriptionListJson.isEmpty()) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<LocalPrescriptionInfo>>() {
                }.getType();
                prescriptionDataList = gson.fromJson(prescriptionListJson, type);
                getUnsharedPrescriptionCount();
            }
            for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
                createVisitAttributeList(visitDTO, db);
            }
            updateSharedPrefForPrescriptionData();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;
    }

    private static final int BATCH_SIZE = DeviceUtils.getOptimalBatchSize();

    /**
     * @param visitAttributeDTOS
     * @return
     * @throws DAOException
     */
    public boolean insertVisitAttributeListV2(List<VisitAttributeDTO> visitAttributeDTOS) throws DAOException {

        // ---------- PREPROCESS (NO DB LOCK) ----------
        mSharedPreference = IntelehealthApplication
                .getAppContext()
                .getSharedPreferences(
                        IntelehealthApplication.getAppContext()
                                .getString(R.string.prescription_share_key),
                        Context.MODE_PRIVATE
                );

        String prescriptionListJson =
                mSharedPreference.getString(AppConstants.PRESCRIPTION_DATA_LIST, "");

        if (!prescriptionListJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<LocalPrescriptionInfo>>() {
            }.getType();
            prescriptionDataList = gson.fromJson(prescriptionListJson, type);
            getUnsharedPrescriptionCount();
        }

        // ---------- DB WORK ----------
        SQLiteDatabase db = null;
        try {
            db = IntelehealthApplication
                    .inteleHealthDatabaseHelper
                    .getWriteDb();

            ContentValues values = new ContentValues();
            int processed = 0;

            for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {

                // Skip early if not needed
                if (!isRelevantVisitAttribute(visitDTO)) {
                    continue;
                }

                if (processed % BATCH_SIZE == 0) {
                    db.beginTransaction();
                }

                values.clear();
                bindVisitAttribute(values, visitDTO);

                db.insertWithOnConflict(
                        "tbl_visit_attribute",
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );

                // Collect prescription info (NO DB logic)
                if (PRESCRIPTION_LINK.equalsIgnoreCase(
                        visitDTO.getVisit_attribute_type_uuid())) {
                    updatePrescriptionList(visitDTO);
                }

                processed++;

                if (processed % BATCH_SIZE == 0) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            }

            if (processed % BATCH_SIZE != 0) {
                db.setTransactionSuccessful();
                db.endTransaction();
            }

            // ---------- POST DB ----------
            updateSharedPrefForPrescriptionData();

            return true;

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    /**
     * @param dto
     * @return
     */
    private boolean isRelevantVisitAttribute(VisitAttributeDTO dto) {
        String type = dto.getVisit_attribute_type_uuid();
        return SPECIALITY.equalsIgnoreCase(type)
                || ADDITIONAL_NOTES.equalsIgnoreCase(type)
                || PRESCRIPTION_LINK.equalsIgnoreCase(type)
                || IS_NCD_VISIT_ATTRIBUTE.equalsIgnoreCase(type);
    }

    /**
     * @param values
     * @param visitDTO
     */
    private void bindVisitAttribute(
            ContentValues values,
            VisitAttributeDTO visitDTO
    ) {
        values.put("uuid", visitDTO.getUuid());
        values.put("visit_uuid", visitDTO.getVisit_uuid());
        values.put("value", visitDTO.getValue());
        values.put("visit_attribute_type_uuid",
                visitDTO.getVisit_attribute_type_uuid());
        values.put("voided", visitDTO.getVoided());
        values.put("sync", "1");
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
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(ADDITIONAL_NOTES) || visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(PRESCRIPTION_LINK) || visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(IS_NCD_VISIT_ATTRIBUTE)) {
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                /*if (createdRecordsCount != -1) {
                    CustomLog.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
                } else {
                    CustomLog.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
                }*/

                if (visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(PRESCRIPTION_LINK)) {
                    updatePrescriptionList(visitDTO);
                }
            }

        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public void scheduleReminder() {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(IntelehealthApplication.getAppContext()).enqueue(workRequest);
    }

    public void getUnsharedPrescriptionCount() {
        for (LocalPrescriptionInfo lpi : prescriptionDataList) {
            prevVisitIdList.add(lpi.getVisitUUID());
            if (!lpi.getShareStatus()) {
                unsharedPrescriptionCount++;
            }
        }
    }

    public void updatePrescriptionList(VisitAttributeDTO visitDTO) {
        boolean isNew = true;
        if (!prevVisitIdList.isEmpty()) {
            if (prevVisitIdList.contains(visitDTO.getVisit_uuid())) {
                isNew = false;
            }
        }
        if (isNew) {
            prescriptionDataList.add(new LocalPrescriptionInfo(visitDTO.getVisit_uuid(), false, System.currentTimeMillis()));
            unsharedPrescriptionCount++;
        }
    }

    public void updateSharedPrefForPrescriptionData() {
        if (prescriptionDataList.size() > prevVisitIdList.size()) {
            Gson gson = new Gson();
            String prescriptionDataListJson = gson.toJson(prescriptionDataList);
            mSharedPreference.edit().putString(AppConstants.PRESCRIPTION_DATA_LIST, prescriptionDataListJson).apply();
            scheduleNotification();
        }
    }

    public void scheduleNotification() {
        AlarmManager alarmManager = (AlarmManager) IntelehealthApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(IntelehealthApplication.getAppContext(), ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                IntelehealthApplication.getAppContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000;
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            mSharedPreference.edit().putBoolean(AppConstants.SHARED_ANY_PRESCRIPTION, false).apply();
            mSharedPreference.edit().putBoolean(AppConstants.SECOND_NOTIFICATION_FIRED, false).apply();
        }
    }

    public String getVisitAttributesList_specificVisit(String VISITUUID, String visit_attribute_type_uuid) {
        String isValue = "";

        if (VISITUUID != null) {
            CustomLog.d("specc", "spec_fun: " + VISITUUID);
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        CustomLog.d("isInserted", "isInserted: " + isInserted);
        return isInserted;
    }

    public boolean updateVisitAttribute(String visitUuid, String value, String attributeTypeUUID) throws DAOException {
        boolean isUpdated = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        db.beginTransaction();

        try {
            values.put("value", value);
            long count = db.update("tbl_visit_attribute", values, "visit_uuid=? and visit_attribute_type_uuid=?", new String[]{visitUuid, attributeTypeUUID});

            if (count != -1) {
                isUpdated = true;
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
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

    public boolean insertIsNcdVisitAttribute(String visitUuid, String isNcdVisit) throws DAOException {
        boolean isInserted = false;

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", isNcdVisit);

            values.put("visit_attribute_type_uuid", AppConstants.IS_NCD_VISIT_ATTRIBUTE);

            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (count != -1) {
                isInserted = true;
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;
    }

    public static int deleteVisitAttributeUsingVisitUuid(String visitUuid) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        String table = "tbl_visit_attribute";
        String whereClause = "visit_uuid=?";
        String[] whereArgs = new String[]{String.valueOf(visitUuid)};
        return db.delete(table, whereClause, whereArgs);
    }

    public static boolean isVisitNCD(String visitUuid) {
        boolean isNcdVisit = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid=? and visit_attribute_type_uuid=? and voided=0",
                new String[]{visitUuid, AppConstants.IS_NCD_VISIT_ATTRIBUTE});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                if (value.equalsIgnoreCase("true")) {
                    isNcdVisit = true;
                }
            }
        }
        cursor.close();
        return isNcdVisit;
    }

    // check is visit arrtibue is alreday exists or not for  visitid
    public static boolean isVisitAttributeExists(String visitUuid, String attributeTypeUUID) {
        boolean exists = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM tbl_visit_attribute WHERE visit_uuid=? and visit_attribute_type_uuid=? and voided=0 LIMIT 1",
                new String[]{visitUuid, attributeTypeUUID});
        if (cursor.getCount() > 0) {
            exists = true;
        }
        cursor.close();
        return exists;
    }

    public Boolean checkInfoShareInsertedOrNot(String visitUuid) {
        boolean exists = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM tbl_visit_attribute WHERE visit_uuid=? and visit_attribute_type_uuid=? and voided=0 LIMIT 1",
                new String[]{visitUuid, UuidDictionary.HEALTH_INFO_SHARE_ATTRIBUTE});
        if (cursor.getCount() > 0) {
            exists = true;
        }
        cursor.close();
        return exists;
    }
}
