package org.intelehealth.ezazi.database.dao;

import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.builder.QueryBuilder;
import org.intelehealth.ezazi.models.dto.VisitAttributeDTO;
import org.intelehealth.ezazi.models.dto.VisitAttribute_Speciality;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class VisitsDAO {
    private static final String TAG = "VisitsDAO";


    private long createdRecordsCount = 0;

    public boolean insertVisit(List<VisitDTO> visitDTOS) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {

            for (VisitDTO visit : visitDTOS) {
                createVisits(visit, db);
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

    private boolean createVisits(VisitDTO visit, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", visit.getUuid());
            values.put("patientuuid", visit.getPatientuuid());
            values.put("locationuuid", visit.getLocationuuid());
            values.put("visit_type_uuid", visit.getVisitTypeUuid());
            values.put("creator", visit.getCreatoruuid());
//            values.put("startdate", DateAndTimeUtils.formatDateFromOnetoAnother(visit.getStartdate(), "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
//            values.put("enddate", DateAndTimeUtils.formatDateFromOnetoAnother(visit.getEnddate(), "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
//            values.put("modified_date", DateAndTimeUtils.formatDateFromOnetoAnother(visit.getModifiedDate(), "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
            values.put("startdate", visit.getStartdate());
            values.put("enddate", visit.getEnddate());
            values.put("modified_date", visit.getModifiedDate());
            values.put("sync", visit.getSyncd());
            createdRecordsCount = db.insertWithOnConflict("tbl_visit", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }


    public boolean insertPatientToDB(VisitDTO visit) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        List<VisitAttributeDTO> visitAttributeDTOS = new ArrayList<>();
        try {

            values.put("uuid", visit.getUuid());
            values.put("patientuuid", visit.getPatientuuid());
            values.put("locationuuid", visit.getLocationuuid());
            values.put("visit_type_uuid", visit.getVisitTypeUuid());
            values.put("creator", visit.getCreatoruuid());
            values.put("startdate", visit.getStartdate());
            values.put("enddate", visit.getEnddate());
            values.put("modified_date", visit.getModifiedDate());
            values.put("sync", false);

            visitAttributeDTOS = visit.getVisitAttributeDTOS();
            if (visitAttributeDTOS != null) {
                insertVisitAttribToDB(visitAttributeDTOS, db);
            }

            createdRecordsCount1 = db.insert("tbl_visit", null, values);
            isCreated = createdRecordsCount1 > 0;
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
        return isCreated;

    }

    public boolean insertVisitAttribToDB(List<VisitAttributeDTO> visitAttributeDTOS, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (VisitAttributeDTO visit : visitAttributeDTOS) {
                values.put("uuid", visit.getUuid());
                values.put("value", visit.getValue());
                values.put("visit_attribute_type_uuid", visit.getVisitAttributeTypeUuid());
                values.put("visituuid", visit.getVisitUuid());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", visit.getSync());
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;
    }


    //update condition for speciality
/*
    public boolean update_visitTbl_speciality(String spinner_value, String visitUUID) throws DAOException {
        boolean isupdatedone = false;
//        String cursor_uuid = "", cursor_value="";
        Log.d("SPINNER", "SPINNER_Selected_valuelogs: "+ spinner_value);
        Log.d("SPINNER", "SPINNER_Selected_uuidlogs: "+ visitUUID);

       */
/* SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_dr_speciality WHERE value = ?",
                new String[]{spinner_value});

        if(idCursor.getCount() != 0)
        {
            while(idCursor.moveToNext())
            {
                 cursor_uuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();*//*



        SQLiteDatabase db_update = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db_update.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] selectionArgs = {visitUUID};
        try
        {
//            values.put("speciality_uuid", cursor_uuid);
            values.put("speciality_value", spinner_value);
            values.put("sync", "0");

            Logger.logD("visit", "updated_specilaity_values " +
                    values.get("speciality_value"));

            int i = db_update.update("tbl_visit", values, whereclause, selectionArgs);

            Logger.logD("visit", "updated_specilaity" + i);
            db_update.setTransactionSuccessful();
            if(i != -1)
                isupdatedone = true;

        }
        catch (SQLException e)
        {
            isupdatedone = false;
            Logger.logD("visit", "updated" + e.getMessage());
            throw new DAOException(e.getMessage());

        }
     finally {
            db_update.endTransaction();
//            db_update.close(); Closing the db was causing the crash on visit onCreate() in update.
            //while updating, do not close the db instance,.

    }

        //Sqlite Db Browser bug isnt showing the updated records...
        //To re-check and confirm that the records are updated & stored in the local db, below is
        //the code....
      */
/*  SQLiteDatabase db_aa = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db_aa.beginTransaction();
        Cursor idCursor_aa = db_aa.rawQuery("SELECT speciality_uuid, speciality_value FROM tbl_visit WHERE uuid = ?", new String[]{visitUUID});

        if(idCursor_aa.getCount() != 0)
        {
            while(idCursor_aa.moveToNext())
            {
                String aa_uuid = idCursor_aa.getString(idCursor_aa.getColumnIndexOrThrow("speciality_uuid"));
                String aa_value = idCursor_aa.getString(idCursor_aa.getColumnIndexOrThrow("speciality_value"));
                Log.d("PRAJ", "PRAJ: "+ aa_uuid + " :: " + aa_value);
            }
        }
        idCursor_aa.close();
        db_aa.setTransactionSuccessful();
        db_aa.endTransaction();*//*


        return  isupdatedone;
}
*/


    //update - end....

    public List<VisitDTO> unsyncedVisits() {
        List<VisitDTO> visitDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit where sync IN (?,?,?) COLLATE NOCASE", new String[]{"0", "false", "FALSE"});
        VisitDTO visitDTO = new VisitDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                visitDTO = new VisitDTO();
                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));

                List<VisitAttribute_Speciality> list = new ArrayList<>();
                list = fetchVisitAttr_Speciality(visitDTO.getUuid());
                Log.e(TAG, "unsyncedVisits: attributes=============");
                Log.e(TAG, "=>" + new Gson().toJson(list));
                visitDTO.setAttributes(list);
                Log.e(TAG, "unsyncedVisits: ====================");
//                visitDTOList.add(visitDTO);

                //adding visit attribute list in the visit data.
//               List<VisitAttribute_Speciality> list = new ArrayList<>();
//               VisitAttribute_Speciality speciality = new VisitAttribute_Speciality();
//               speciality.setAttributeType("3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
//               speciality.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality_value")));
//               list.add(speciality);
//
//
//                visitDTO.setAttributes(list);
                //need a return value as list so that I can then add it to visitDTO.setAttributes(list);
//               list =  fetchVisitAttr_Speciality();
//               visitDTO.setAttributes(list);

                visitDTOList.add(visitDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

//        List<VisitAttribute_Speciality> list = new ArrayList<>();
//        list = fetchVisitAttr_Speciality();
//        visitDTO.setAttributes(list);
//        visitDTOList.add(visitDTO);

        return visitDTOList;
    }

    private List<VisitAttribute_Speciality> fetchVisitAttr_Speciality(String visit_uuid) {
        List<VisitAttribute_Speciality> list = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

//        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE sync=? AND visit_uuid=?",
//                new String[] {"0", visit_uuid});

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=? group by value", new String[]{visit_uuid});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                VisitAttribute_Speciality speciality = new VisitAttribute_Speciality();
                speciality.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                speciality.setAttributeType(cursor.getString(cursor.getColumnIndexOrThrow("visit_attribute_type_uuid")));
                speciality.setValue(cursor.getString(cursor.getColumnIndexOrThrow("value")));
                list.add(speciality);
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return list;
    }

    public List<VisitDTO> getAllVisits() {
        List<VisitDTO> visitDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit", null);
        VisitDTO visitDTO = new VisitDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                visitDTO = new VisitDTO();
                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));
                visitDTOList.add(visitDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        // db.close();
        return visitDTOList;
    }

    public boolean updateVisitSync(String uuid, String synced) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("visitdao", "updatesynv visit " + uuid + synced);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);
            values.put("uuid", uuid);
            int i = db.update("tbl_visit", values, whereclause, whereargs);
            Logger.logD("visit", "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD("visit", "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }

        return isUpdated;
    }


    public boolean updateVisitEnddate(String uuid, String enddate) throws DAOException {
        boolean isUpdated = true;
        if (enddate.length() == 0) return isUpdated;
        Logger.logD("visitdao", "updatesynv visit uuid:" + uuid + " EndDate:" + enddate);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("enddate", enddate);
            values.put("sync", "0");
            int i = db.update("tbl_visit", values, whereclause, whereargs);
            isUpdated = i > 0;
            Logger.logD("visit", "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD("visit", "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }

        return isUpdated;
    }

//    public boolean updateVisitCreator(String visitUUID, String createUUID) throws DAOException {
//        boolean isUpdated = true;
//        Logger.logD("visitdao", "updateVisitCreator visit -" + visitUUID + " createUUID - " + createUUID);
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
//        db.beginTransaction();
//        ContentValues values = new ContentValues();
//        String whereclause = "uuid=?";
//        String[] whereargs = {visitUUID};
//        try {
//            values.put("creator", createUUID);
//            values.put("sync", "0");
//            int i = db.update("tbl_visit", values, whereclause, whereargs);
//            Logger.logD("visit", "updated" + i);
//            db.setTransactionSuccessful();
//        } catch (SQLException sql) {
//            Logger.logD("visit", "updated" + sql.getMessage());
//            throw new DAOException(sql.getMessage());
//        } finally {
//            db.endTransaction();
//        }
//
//        return isUpdated;
//    }

    public String fetchVisitUUIDFromPatientUUID(String patientUUID) {
        String visitUUID = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_visit where patientuuid = ? ", new String[]{patientUUID});

        if (cursor.getCount() > 0) { // ie. visit is created for this patient
            while (cursor.moveToNext()) {
                visitUUID = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        } else { // ie. visit is not yet started ie. user has not clicked on Start new visit button.
            visitUUID = "";
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();


        return visitUUID;
    }


    public String patientUuidByViistUuid(String visituuid) {
        String patientUuidByViistUuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT patientuuid FROM tbl_visit where uuid = ? ", new String[]{visituuid});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                patientUuidByViistUuid = cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"));
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();


        return patientUuidByViistUuid;
    }

    public boolean isUpdatedDownloadColumn(String visitUuid, boolean isupdated) throws DAOException {
        boolean isUpdated = false;
        int updatedcount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {visitUuid};
        try {
            values.put("isdownloaded", isupdated);
            updatedcount = db.update("tbl_visit", values, whereclause, whereargs);
            if (updatedcount != 0) isUpdated = true;
            Logger.logD("visit", "updated isdownloaded" + updatedcount);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(sql);
            Logger.logD("visit", "updated isdownloaded" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

    public String getDownloadedValue(String visituuid) throws DAOException {
        String isDownloaded = null;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        try {
            Cursor cursor = db.rawQuery("SELECT isdownloaded FROM tbl_visit where uuid = ? ", new String[]{visituuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    isDownloaded = cursor.getString(cursor.getColumnIndexOrThrow("isdownloaded"));
                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }
        return isDownloaded;
    }

//    public List<VisitDTO> getAllActiveVisits() {
//        List<VisitDTO> visitDTOList = new ArrayList<>();
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();
//        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit where enddate is NULL OR enddate='' GROUP BY uuid ORDER BY startdate DESC", null);
//        VisitDTO visitDTO = new VisitDTO();
//        if (idCursor.getCount() != 0) {
//            while (idCursor.moveToNext()) {
//                visitDTO = new VisitDTO();
//                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
//                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
//                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
//                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
//                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
//                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
//                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));
//                visitDTOList.add(visitDTO);
//            }
//        }
//        idCursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        // db.close();
//        return visitDTOList;
//    }

//    public List<VisitDTO> getAllActiveVisitsForMe(String creatorID) {
//        List<VisitDTO> visitDTOList = new ArrayList<>();
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
////        db.beginTransaction();
//        //  String query = new QueryBuilder().select("uuid, patientuuid, locationuuid, startdate, enddate, creator, visit_type_uuid").from(" tbl_visit ").where("uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " + "AND voided = '0' AND creator = '" + creatorID + "'").groupBy("uuid").orderBy("startdate").orderIn("DESC").build();
////        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit where creator='" + creatorID + "' and enddate is NULL OR enddate='' GROUP BY uuid ORDER BY startdate DESC", null);
//
//        String query = "select * from (select * from  tbl_visit where uuid IN(select  visit_uuid from tbl_visit_attribute where visit_attribute_type_uuid = 'a0378be4-d9c6-4cb2-bbf5-777e27a32efc' and value ='" + creatorID + "' )  and  voided = '0') as T where  uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid='bd1fbfaa-f5fb-4ebd-b75c-564506fc309e') group by uuid order by startdate desc ";
//        Cursor idCursor = db.rawQuery(query, null);
//        VisitDTO visitDTO = new VisitDTO();
//        if (idCursor.getCount() != 0) {
//            while (idCursor.moveToNext()) {
//                visitDTO = new VisitDTO();
//                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
//                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
//                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
//                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
//                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
//                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
//                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));
//                visitDTOList.add(visitDTO);
//            }
//        }
//        idCursor.close();
////        db.setTransactionSuccessful();
////        db.endTransaction();
//        // db.close();
//        return visitDTOList;
//    }

    public List<VisitDTO> getAllActiveVisitByProviderId(String providerId) {
        List<VisitDTO> visitDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        db.beginTransaction();
        String query = new QueryBuilder()
                .select("V.uuid, V.patientuuid, V.locationuuid, V.startdate, V.enddate, V.creator, V.visit_type_uuid")
                .from("tbl_visit V")
                .join(" LEFT OUTER JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid ")
                .where("V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided = '0' AND VA.value = '" + providerId + "' AND (V.enddate IS NULL OR  V.enddate = '') ")
                .groupBy("V.uuid").orderBy("V.startdate")
                .orderIn("DESC")
                .build();
        Log.e(TAG, "getAllActiveVisitByProviderId: query => " + query);
//        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit where creator='" + creatorID + "' and enddate is NULL OR enddate='' GROUP BY uuid ORDER BY startdate DESC", null);
        Cursor idCursor = db.rawQuery(query, null);
        VisitDTO visitDTO = new VisitDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                visitDTO = new VisitDTO();
                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));
                visitDTOList.add(visitDTO);
            }
        }
        idCursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
        // db.close();
        return visitDTOList;
    }

//    public String getPatientVisitUuid(String patientUuid) {
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_visit where patientuuid = ?", new String[]{patientUuid});
//        if (idCursor.getCount() != 0) {
//            while (idCursor.moveToNext()) {
//                return idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
//            }
//        }
//        idCursor.close();
//        // db.close();
//        return null;
//    }

    public boolean checkLoggedInUserAccessVisit(String visitId, String providerId) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        String query = new QueryBuilder().select("V.uuid")
                .from("tbl_visit V")
                .join("INNER JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid")
                .where("V.uuid = '" + visitId + "' AND VA.value = '" + providerId + "'").build();

        Log.e(TAG, "checkLoggedInUserAccessVisit: query=>" + query);
        Cursor idCursor = db.rawQuery(query, null);
        if (idCursor.getCount() > 0) {
            return true;
        }
        idCursor.close();
        // db.close();
        return false;
    }

}