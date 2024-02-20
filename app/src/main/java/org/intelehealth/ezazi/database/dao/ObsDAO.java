package org.intelehealth.ezazi.database.dao;

import static org.intelehealth.ezazi.utilities.UuidDictionary.BIRTH_OUTCOME;
import static org.intelehealth.ezazi.utilities.UuidDictionary.END_2ND_STAGE_OTHER;
import static org.intelehealth.ezazi.utilities.UuidDictionary.LABOUR_OTHER;
import static org.intelehealth.ezazi.utilities.UuidDictionary.MISSED_ENCOUNTER;
import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_TYPE;
import static org.intelehealth.ezazi.utilities.UuidDictionary.MOTHER_DECEASED;
import static org.intelehealth.ezazi.utilities.UuidDictionary.MOTHER_DECEASED_FLAG;
import static org.intelehealth.ezazi.utilities.UuidDictionary.OUT_OF_TIME;
import static org.intelehealth.ezazi.utilities.UuidDictionary.REFER_TYPE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.prescription.PrescDataModel;
import org.intelehealth.ezazi.builder.QueryBuilder;
import org.intelehealth.ezazi.executor.TaskExecutor;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.ui.visit.model.CompletedVisitStatus;
import org.intelehealth.ezazi.ui.visit.model.VisitOutcome;
import org.intelehealth.ezazi.ui.visit.model.VisitOutcomeListener;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.utils.DateTimeUtils;

public class ObsDAO {


    private SQLiteDatabase db = null;
    SessionManager sessionManager = null;
    String TAG = ObsDAO.class.getSimpleName();

    public boolean insertObsTemp(List<ObsDTO> obsDTOS) throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        try {
            db.beginTransaction();
            Logger.logD("insert", " insert obs");
            for (ObsDTO obs : obsDTOS) {
                if (sessionManager.isFirstTimeSyncExcuted() && obs.getVoided() == 1)
                    continue;
                createObs(obs);
            }
            db.setTransactionSuccessful();
            Logger.logD("insert obs finished", " insert obs finished");
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;

    }

    private boolean createObs(ObsDTO obsDTOS) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount = 0;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", obsDTOS.getUuid());
            values.put("encounteruuid", obsDTOS.getEncounteruuid());
            values.put("creator", obsDTOS.getCreator());
            values.put("creatoruuid", obsDTOS.getCreatorUuid());
            values.put("conceptuuid", obsDTOS.getConceptuuid());
            values.put("comment", obsDTOS.getComment());
            values.put("value", obsDTOS.getValue());
            values.put("obsservermodifieddate", obsDTOS.getObsServerModifiedDate());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("created_date", obsDTOS.getCreatedDate(true));
            values.put("voided", obsDTOS.getVoided());
            values.put("sync", "TRUE");
            createdRecordsCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }

        return isCreated;

    }

    public boolean insertObs(ObsDTO obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("creatoruuid", obsDTO.getCreatorUuid());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("comment", obsDTO.getComment());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("created_date", obsDTO.getCreatedDate(true));
            values.put("voided", "0");
            values.put("sync", "false");
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
            Log.e(TAG, "insertObs: : total record" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public boolean insertObsNew(ObsDTO obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            //values.put("uuid", UUID.randomUUID().toString());
            values.put("uuid", obsDTO.getUuid());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("creatoruuid", obsDTO.getCreatorUuid());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("comment", obsDTO.getComment());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "false");
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
            Log.e(TAG, "insertObsNew: : total record" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public boolean updateObs(ObsDTO obsDTO) {
        Log.d(TAG, "1111updateObs: uuid for update : " + obsDTO.getUuid());
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = null;
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {

            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("creatoruuid", obsDTO.getCreatorUuid());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("comment", obsDTO.getComment());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            //values.put("created_date", obsDTO.getCreatedDate(true));
            values.put("voided", "0");
            values.put("sync", "false");

            updatedCount = db.update("tbl_obs", values, selection, new String[]{obsDTO.getUuid()});
            //String selectQuery = "update  nozzle_details set " + columnName + " = '" + columnValue + "' where shiftNozzleNoApp = '" + nozzleId + "'";

           /* String updateQuery = "update tbl_obs set " + "value" + " = '" + obsDTO.getValue() +"'"+ " and modified_date" + " = '" + AppConstants.dateAndTimeUtils.currentDateTime() + "' where uuid = '" + obsDTO.getUuid() + "'";
            Log.d(TAG, "updateObs:updateQuery :  "+updateQuery);
            db.rawQuery(updateQuery, null);

*/
            Log.d(TAG, "updateObs: updatedCount : " + updatedCount);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }
        //        If no value is not found, then update fails so insert instead.
       /* if (updatedCount == 0) {
            Log.d(TAG, "updateObs: insert logic in update");
            try {
                insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }*/

        return true;
    }

    public boolean insertObsToDb(List<ObsDTO> obsDTO, String TAG) throws DAOException {
       /* ##remove##kz if (obsDTO.size() > 0) {
            return false;
        }*/
        Log.e(TAG, "insertObsToDb: " + new Gson().toJson(obsDTO));
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            for (ObsDTO ob : obsDTO) {
                values.put("uuid", UUID.randomUUID().toString());
                values.put("encounteruuid", ob.getEncounteruuid());
                values.put("creator", ob.getCreator());
                values.put("creatoruuid", ob.getCreatorUuid());
                values.put("conceptuuid", ob.getConceptuuid());
                values.put("comment", ob.getComment());
                values.put("value", ob.getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("created_date", ob.getCreatedDate(true));
                values.put("voided", "0");
                values.put("sync", "false");    //Earlier was set to FALSE which caused the issue.
                insertedCount = db.insert("tbl_obs", null, values);
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
            Log.e(TAG, "insertObsToDb: FROM " + TAG + ": total record" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public boolean markedAsVoidedObsToDb(List<String> obsUuids) throws DAOException {
        Log.e(TAG, "markedAsVoidedObsToDb: " + new Gson().toJson(obsUuids));
        boolean isUpdated = true;
        int updatedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
            for (String uuid : obsUuids) {
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("voided", "1");
                values.put("sync", "false");    //Earlier was set to FALSE which caused the issue.
                updatedCount = db.update("tbl_obs", values, selection, new String[]{uuid});
                isUpdated = updatedCount > 0;
            }
            db.setTransactionSuccessful();
            Logger.logD("markedAsVoidedObsToDb", "updatedrecords count" + updatedCount);
            Log.e(TAG, "markedAsVoidedObsToDb: FROM " + TAG + ": total record" + updatedCount);
        } catch (SQLException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return isUpdated;

    }

    public List<ObsDTO> obsCommentList(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        ObsDTO obsDTO = new ObsDTO();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT comment FROM tbl_obs where encounteruuid = ? AND voided='0'",
                new String[]{encounteruuid});

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }


    public List<ObsDTO> obsDTOList(String encounteruuid) {
        Log.d(TAG, "obsDTOList: kzcheckencounteruuid : " + encounteruuid);
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //take All obs except image obs
        String query = "SELECT * FROM tbl_obs where encounteruuid = '" + encounteruuid + "' AND (conceptuuid != '07a816ce-ffc0-49b9-ad92-a1bf9bf5e2ba' AND conceptuuid != '200b7a45-77bc-4986-b879-cc727f5f7d5b') AND sync IN ('0', 'FALSE', 'false')";
        Log.d(TAG, "obsDTOList:kk query: " + query);
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND (conceptuuid != ? AND conceptuuid != ?) AND sync IN ('0', 'FALSE', 'false')",
                new String[]{encounteruuid, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE});

        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                String encounterId = idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid"));
                if (encounterId.equals(encounteruuid)) {
                    obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                    obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                    obsDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                    obsDTO.setVoided(Integer.parseInt(idCursor.getString(idCursor.getColumnIndexOrThrow("voided"))));
                    obsDTO.setCreatorUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creatoruuid")));
                    obsDTO.setCreatedDate(idCursor.getString(idCursor.getColumnIndexOrThrow("created_date")));
                    Log.d(TAG, "obsDTOList: creatoruuid : " + obsDTO.getCreatorUuid());
                    obsDTOList.add(obsDTO);
                }
            }
        }

        idCursor.close();

        return obsDTOList;
    }

    public List<ObsDTO> getOBSByEncounterUUID(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //UuidDictionary.IV_FLUIDS, UuidDictionary.OXYTOCIN_UL_DROPS_MIN

        //take All obs except image obs
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND voided='0' ORDER BY created_date DESC",
                new String[]{encounteruuid});
      /*  String query = "SELECT * FROM tbl_obs WHERE encounteruuid = ? AND (voided='0' OR (voided='1' AND (conceptuuid= ?  OR conceptuuid= ?)))";
        Cursor idCursor = db.rawQuery(query, new String[]{encounteruuid, UuidDictionary.IV_FLUIDS, UuidDictionary.OXYTOCIN_UL_DROPS_MIN});*/
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                obsDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                obsDTO.setCreatedDate(idCursor.getString(idCursor.getColumnIndexOrThrow("created_date")));
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }


    public List<ObsDTO> getOBSByEncounterUUIDWithoughtMedicines(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //take All obs except image obs
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND conceptuuid != ? AND voided='0'",
                new String[]{encounteruuid, UuidDictionary.MEDICINE});
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                obsDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }

    public List<ObsDTO> getMedicineOBSByEncounterUUID(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //take All obs except image obs
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND conceptuuid = ? AND voided='0'",
                new String[]{encounteruuid, UuidDictionary.MEDICINE});
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                obsDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }

    public List<String> getImageStrings(String conceptuuid, String encounterUuidAdultIntials) {
        List<String> rawStrings = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_obs where conceptuuid = ? AND encounteruuid = ? AND voided='0'", new String[]{conceptuuid, encounterUuidAdultIntials});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                rawStrings.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
            }
        }
        idCursor.close();


        return rawStrings;
    }

    public String getObsuuid(String encounterUuid, String conceptUuid) throws DAOException {
        String obsuuid = null;
        db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        Cursor obsCursoursor = db.rawQuery("Select uuid from tbl_obs where conceptuuid=? and encounteruuid=? and voided='0' order by created_date,obsservermodifieddate desc limit 1 ", new String[]{conceptUuid, encounterUuid});
        try {
            if (obsCursoursor.getCount() != 0) {
                while (obsCursoursor.moveToNext()) {
                    obsuuid = obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("uuid"));
                }

            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            obsCursoursor.close();
        }


        return obsuuid;
    }

    public List<PrescDataModel> fetchAllObsPrescData(String encounterVisitNote, String CONCEPTUUID, String sync) {
        List<PrescDataModel> prescDataModelList = new ArrayList<>();

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor obsCursoursor = db.rawQuery("Select * from tbl_obs where conceptuuid=? and encounteruuid=? and sync=? and voided =?",
                new String[]{CONCEPTUUID, encounterVisitNote, sync, "0"});
        try {
            if (obsCursoursor.getCount() != 0) {
                while (obsCursoursor.moveToNext()) {
                    prescDataModelList.add(new PrescDataModel(
                            obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("uuid")),
                            obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("value")),
                            obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("encounteruuid")),
                            obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("conceptuuid"))
                    ));
                }
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
        } finally {
            obsCursoursor.close();
        }

        return prescDataModelList;
    }

    /**
     * @param obsDTO This obsDTO contains the Prescription obs values that we dont want to push to the server but need to save in localdb
     *               so as when user goes back to the Presc activity then we an see all the data that he had provided as presc. Due to this
     *               I have set sync flag = true. Then when user comes back to Presc screen in the oncreate() we will call the fetch query of db
     *               and fetch all the obs against their conceptuuid and show in the RecyclerView...
     * @return boolean Is insertion was successful or not.
     * @throws DAOException
     */
    public boolean insertPrescObs(ObsDTO obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //  db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("creatoruuid", obsDTO.getCreatorUuid());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("comment", obsDTO.getComment());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "true");
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            //       db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
            Log.e(TAG, "insertPrescObs: total record" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            //       db.endTransaction();
        }

        return isUpdated;

    }


    /**
     * MISSED_ENCOUNTER --> MISSED_OBS
     *
     * @param encounterUuid
     * @param creatorID     since card is disabled that means that either the user has filled data or has forgotten to fill.
     *                      We need to check this by using the encounterUuid and checking in obs tbl if any obs is created.
     *                      If no obs created than create Missed Enc obs for this disabled encounter. Else its clear that the data was filled up.
     */
    public EncounterDTO.Status checkObsAndCreateMissedObs(String encounterUuid, String creatorID) {
        EncounterDTO.Status status = EncounterDTO.Status.PENDING;
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND voided='0' AND conceptuuid != ?",
                new String[]{encounterUuid, ENCOUNTER_TYPE});

        if (idCursor.getCount() <= 0) {
            // that means there is no obs for this enc which means that this encounter is missed...
            // now insert a new row in obs table against this encoutneruuid and set sync to false.
            status = EncounterDTO.Status.MISSED; // missed
            ContentValues values = new ContentValues();
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", encounterUuid);
            values.put("creator", creatorID);
            values.put("conceptuuid", MISSED_ENCOUNTER); // Missed Encounter
            values.put("comment", "");
            values.put("value", "-");
            values.put("modified_date", DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
            values.put("voided", "0");
            values.put("sync", "false");

            db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            //end
        } else {
            // if missed enc is present in that case send ismissed = 1 ie. missed enc will be dispalyed on card else send submitted.
            // this is done so as to avoid everytime replacig a new row in the db for missed enc as since earlier it was creating a new record
            // everytime for missed enc.
            String typeuuid = "";
            while (idCursor.moveToNext()) {
                typeuuid = idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid"));
                if (!typeuuid.equalsIgnoreCase("") && typeuuid.equalsIgnoreCase(MISSED_ENCOUNTER)) {
                    // ie. if typeuuid == MISSED_ENCOUNTER ie. missed enc already present than isMissed=1 else 2 ie. Submitted.
                    // already missed is created so check if 1 than only sync the record.
                    status = EncounterDTO.Status.MISSED;
                } else {
                    status = EncounterDTO.Status.SUBMITTED;
                    // submitted
                    // this means that this encounter is filled with obs ie. It was answered and then disabled.
                }
            }

        }

        idCursor.close();

        return status;
    }

    public EncounterDTO.Status checkObsAddedOrNt(String encounterUuid, String creatorID) {
        EncounterDTO.Status status = EncounterDTO.Status.MISSED;
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND voided='0' AND conceptuuid NOT IN (?, ?)",
                new String[]{encounterUuid, MISSED_ENCOUNTER, ENCOUNTER_TYPE});

//        if (idCursor.getCount() <= 0) {
//            // that means there is no obs for this enc which means that this encounter is missed... or not yet filled up.
//            // now insert a new row in obs table against this encoutneruuid and set sync to false.
//            isMissed = 1; // missed
//        } else {
//            isMissed = 2; // submitted
//            // this means that this encounter is filled with obs ie. It was answered and then disabled.
//        }

        if (idCursor.getCount() > 0) return EncounterDTO.Status.SUBMITTED;
        idCursor.close();

        return status;
    }

    public String checkBirthOutcomeObsExistsOrNot(String encounterUuid) {
        String valueData = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND voided='0' AND conceptuuid = ?",
                new String[]{encounterUuid, BIRTH_OUTCOME});

        if (idCursor.getCount() > 0) { // birth outcome present. This means that this encounter is filled with obs ie. Birth Outcome is present.
            while (idCursor.moveToNext()) {
                valueData = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
            }
        } else { // This means against this enc there is no obs. Which means this obs is not filled yet. no birth outcome present.
            valueData = "";
        }

        /*if (idCursor.getCount() <= 0) {
         *//* This means against this enc there is no obs. Which means this obs is not filled yet. *//*
            isMissed = 1; // no birth outcome present.
        }
        else {
            isMissed = 2; // birth outcome present.
            // this means that this encounter is filled with obs ie. Birth Outcome is present.
        }*/
        idCursor.close();

        return valueData;
    }

    public String getCompletedBirthStageStatus(String encounterUuid) {
        String valueData = "";
        db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        //do some insertions or whatever you need
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_obs where encounteruuid = ? AND voided='0' AND conceptuuid = ?",
                new String[]{encounterUuid, BIRTH_OUTCOME});

        if (idCursor.getCount() > 0) { // birth outcome present. This means that this encounter is filled with obs ie. Birth Outcome is present.
            while (idCursor.moveToNext()) {
                valueData = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
            }
        } else { // This means against this enc there is no obs. Which means this obs is not filled yet. no birth outcome present.
            valueData = "";
        }

        /*if (idCursor.getCount() <= 0) {
         *//* This means against this enc there is no obs. Which means this obs is not filled yet. *//*
            isMissed = 1; // no birth outcome present.
        }
        else {
            isMissed = 2; // birth outcome present.
            // this means that this encounter is filled with obs ie. Birth Outcome is present.
        }*/
        idCursor.close();

        return valueData;
    }


    public int checkObsExistsOrNot(String encounterUuid) {
        int isMissed = 0;
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND voided='0' AND conceptuuid != ?",
                new String[]{encounterUuid, UuidDictionary.ENCOUNTER_TYPE});

        if (idCursor.getCount() <= 0) {
            /* This means against this enc there is no obs. Which means this obs is not filled yet. */
            isMissed = 1; // yot filled yet
        } else {
            isMissed = 2; // submitted
            // this means that this encounter is filled with obs ie. It was answered and then disabled.
        }
        idCursor.close();

        return isMissed;
    }

    public boolean insertMotherDeceasedFlatObs(String encounteruuid, String creatorID, String value) throws DAOException {
        return insert_Obs(encounteruuid, creatorID, value, UuidDictionary.MOTHER_DECEASED_FLAG);
    }

    public boolean insert_Obs(String encounteruuid, String creatorID, String value, String conceptId) throws DAOException {
        boolean isUpdated = false;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //  db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", encounteruuid);
            values.put("creator", creatorID);
            values.put("conceptuuid", conceptId);
            values.put("comment", "");
            values.put("value", value);
            values.put("modified_date", DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
            values.put("voided", "0");
            values.put("sync", "0");
            values.put("creatoruuid", sessionManager.getCreatorID());
            db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            //   db.setTransactionSuccessful();
            isUpdated = true;
            //  Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            //   db.endTransaction();
        }

        return isUpdated;

    }

    public void createEncounterType(String encounterUuid, String value, String creatorId, String from) {
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setValue(value);
        obsDTO.setCreator(creatorId);
        obsDTO.setConceptuuid(UuidDictionary.ENCOUNTER_TYPE);
        obsDTO.setCreatorUuid(creatorId);
        obsDTO.setCreatedDate(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
        try {
            new ObsDAO().insertObs(obsDTO);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
        Log.e(TAG, "createEncounterType: from screen =>" + from);
//        new TaskExecutor<Boolean>().executeTask(() -> {
//            ObsDTO obsDTO = new ObsDTO();
//            obsDTO.setUuid(UUID.randomUUID().toString());
//            obsDTO.setEncounteruuid(encounterUuid);
//            obsDTO.setValue(value);
//            obsDTO.setCreator(creatorId);
//            obsDTO.setConceptuuid(UuidDictionary.ENCOUNTER_TYPE);
//            return new ObsDAO().insertObs(obsDTO);
//        });
    }

    public ObsDTO createObs(String encounterUuid, String value, String creatorId, String from) {
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setValue(value);
        obsDTO.setCreator(creatorId);
        obsDTO.setConceptuuid(UuidDictionary.ENCOUNTER_TYPE);
        obsDTO.setCreatorUuid(creatorId);
        obsDTO.setCreatedDate(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
        Log.e(TAG, "createEncounterType: from screen =>" + from);
        return obsDTO;
    }


    public EncounterDTO.Type getEncounterType(String encounterUuid, String creatorID) {
        EncounterDTO.Type type = EncounterDTO.Type.NORMAL;
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_obs where encounteruuid = ? " +
                        "AND voided='0' AND conceptuuid = ?",
                new String[]{encounterUuid, ENCOUNTER_TYPE});

        if (idCursor.getCount() > 0) {
            while (idCursor.moveToNext()) {
                String value = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                if (value.equals(EncounterDTO.Type.SOS.name())) type = EncounterDTO.Type.SOS;
            }
        }
        idCursor.close();

        return type;
    }

    private String getCompletedStatusIds() {
        return "conceptuuid IN ('" + UuidDictionary.BIRTH_OUTCOME + "', " +
                "'" + UuidDictionary.REFER_TYPE + "', " +
                "'" + UuidDictionary.MOTHER_DECEASED_FLAG + "', " +
                "'" + UuidDictionary.MOTHER_DECEASED + "', " +
                "'" + UuidDictionary.END_2ND_STAGE_OTHER + "', " +
                "'" + UuidDictionary.LABOUR_OTHER + "', " +
                "'" + UuidDictionary.OUT_OF_TIME + "')";

//        IN (
//                '23601d71-50e6-483f-968d-aeef3031346d',
//                '9414cc17-1f97-4a27-8066-17591c11e513',
//                '0a3d26ec-bdb8-4a27-badb-937f730bf89d',
//                '91c94e0b-b967-4dd0-9eec-75d770af7f5b ',
//                'd319ebbc-5260-4eff-aa3f-2af11b177ec4',
//                'ac99754c-d3a9-4736-a8b8-276c78953315',
//                '893b3d20-171b-4023-a6f5-3d3bdf81a094')
    }

    public VisitOutcome getCompletedVisitType(String encounterUuid) {
        HashMap<String, String> statusMap = new HashMap<>();
        Log.e(TAG, "getCompletedVisitType: encounterId => " + encounterUuid);
        db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        String query = new QueryBuilder().select("value, conceptuuid").from("tbl_obs")
//                .where("encounteruuid = '" + encounterUuid + "' AND " + getCompletedStatusIds())
//                .build();
//        findOutcome: {ac99754c-d3a9-4736-a8b8-276c78953315=Visit close with other comment,
//        23601d71-50e6-483f-968d-aeef3031346d=OTHER,
//        91c94e0b-b967-4dd0-9eec-75d770af7f5b=Mother is no more alive,
//        0a3d26ec-bdb8-4a27-badb-937f730bf89d=YES}
//        10:17:50.239  E  initUI: {"hasMotherDeceased":true,"outcome":"OTHER/Mother Deceased"}
//        String query = new QueryBuilder().select("O.value, O.conceptuuid").from("tbl_obs O")
//                .join("LEFT JOIN tbl_encounter E ON E.uuid =O.encounteruuid")
//                .where("E.uuid = '" + encounterUuid + "' AND E.encounter_type_uuid = '" + UuidDictionary.ENCOUNTER_VISIT_COMPLETE + "'")
//                .build();
//        Log.e(TAG, "getCompletedVisitType: " + query);
        String query = "SELECT value, conceptuuid FROM tbl_obs WHERE encounteruuid = ? AND conceptuuid IN (?,?,?,?,?,?,?)";
        final Cursor idCursor = db.rawQuery(query, new String[]{encounterUuid,
                UuidDictionary.BIRTH_OUTCOME,
                UuidDictionary.REFER_TYPE,
                UuidDictionary.MOTHER_DECEASED_FLAG,
                UuidDictionary.MOTHER_DECEASED,
                UuidDictionary.END_2ND_STAGE_OTHER,
                UuidDictionary.LABOUR_OTHER,
                UuidDictionary.OUT_OF_TIME});


        //do some insertions or whatever you need
//        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_obs where encounteruuid = ? AND voided='0' AND conceptuuid = ?",
//                new String[]{encounterUuid, BIRTH_OUTCOME});

        if (idCursor.getCount() > 0) { // birth outcome present. This means that this encounter is filled with obs ie. Birth Outcome is present.
            while (idCursor.moveToNext()) {
                String valueData = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                String conceptId = idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid"));
                statusMap.put(conceptId, valueData);
//                if (valueData.equals(CompletedVisitStatus.ReferType.REFER_TO_OTHER.value())) {
//                    valueData = CompletedVisitStatus.ReferType.REFER_TO_OTHER.sortValue();
//                } else if (valueData.equals(CompletedVisitStatus.ReferType.SELF_DISCHARGE.value())) {
//                    valueData = CompletedVisitStatus.ReferType.SELF_DISCHARGE.sortValue();
//                } else if (valueData.equals(CompletedVisitStatus.ReferType.SHIFT_TO_C_SECTION.value())) {
//                    valueData = CompletedVisitStatus.ReferType.SHIFT_TO_C_SECTION.sortValue();
//                } else if (valueData.equals(CompletedVisitStatus.ReferType.REFER_TO_ICU.value())) {
//                    valueData = CompletedVisitStatus.ReferType.REFER_TO_ICU.sortValue();
//                } else
//                if (conceptId.equals(CompletedVisitStatus.MotherDeceased.MOTHER_DECEASED_FLAG.uuid())) {
//                    valueData = CompletedVisitStatus.MotherDeceased.MOTHER_DECEASED_FLAG.sortValue();
//                } else if (conceptId.equals(CompletedVisitStatus.Labour.conceptUuid()) && valueData.equals(CompletedVisitStatus.Labour.OTHER.value())) {
//                    valueData = CompletedVisitStatus.Labour.OTHER.sortValue();
//                } else if (conceptId.equals(CompletedVisitStatus.OtherComment.OTHER.uuid())) {
//                    valueData = CompletedVisitStatus.OtherComment.OTHER.sortValue();
//                }
            }
        }

        /*if (idCursor.getCount() <= 0) {
         *//* This means against this enc there is no obs. Which means this obs is not filled yet. *//*
            isMissed = 1; // no birth outcome present.
        }
        else {
            isMissed = 2; // birth outcome present.
            // this means that this encounter is filled with obs ie. Birth Outcome is present.
        }*/
        idCursor.close();
        return findOutcome(statusMap);
    }

    private VisitOutcome findOutcome(HashMap<String, String> outcomeMap) {
        Log.e(TAG, "findOutcome: " + outcomeMap.toString());

        VisitOutcome visitOutcome = new VisitOutcome();
        if (outcomeMap.containsKey(BIRTH_OUTCOME) && outcomeMap.containsKey(MOTHER_DECEASED_FLAG)) {
            String birthOutcome = outcomeMap.get(BIRTH_OUTCOME);
            String motherFlag = outcomeMap.get(MOTHER_DECEASED_FLAG);
            String outcome = birthOutcome;
            visitOutcome.setOutcome(outcome);
            assert motherFlag != null;
            if (motherFlag.equalsIgnoreCase("YES")) {
                outcome = outcome + "/" + CompletedVisitStatus.MotherDeceased.MOTHER_DECEASED_REASON.sortValue();
                visitOutcome.setOutcome(outcome);
                visitOutcome.setHasMotherDeceased(true);
                visitOutcome.setMotherDeceasedReason(outcomeMap.get(MOTHER_DECEASED));
            }

            assert birthOutcome != null;
            if (birthOutcome.equalsIgnoreCase(CompletedVisitStatus.Labour.OTHER.value())) {
                visitOutcome.setOtherComment(outcomeMap.get(LABOUR_OTHER));
            }
        } else if (outcomeMap.containsKey(BIRTH_OUTCOME)) {
            String outcome = outcomeMap.get(BIRTH_OUTCOME);
            visitOutcome.setOutcome(outcome);
        } else if (outcomeMap.containsKey(REFER_TYPE)) {
            String outcome = outcomeMap.get(REFER_TYPE);
            visitOutcome.setOutcome(outcome);
            assert outcome != null;
            if (outcome.equalsIgnoreCase(CompletedVisitStatus.ReferType.OTHER.value())) {
                visitOutcome.setOtherComment(outcomeMap.get(END_2ND_STAGE_OTHER));
            }
        } else if (outcomeMap.containsKey(MOTHER_DECEASED)) {
            visitOutcome.setOutcome(CompletedVisitStatus.MotherDeceased.MOTHER_DECEASED_FLAG.sortValue());
            visitOutcome.setHasMotherDeceased(true);
            visitOutcome.setMotherDeceasedReason(outcomeMap.get(MOTHER_DECEASED));
        } else if (outcomeMap.containsKey(OUT_OF_TIME)) {
            visitOutcome.setOutcome(CompletedVisitStatus.OutOfTime.OUT_OF_TIME.sortValue());
            visitOutcome.setOtherComment(outcomeMap.get(OUT_OF_TIME));
        } else {
            visitOutcome.setOutcome("");
        }

        return visitOutcome;
    }


    public boolean checkIsOutOfTimeEncounter(String encounterUuid) {
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_obs where encounteruuid = ? " +
                        "AND voided IN ('0', 'false', 'FALSE') AND conceptuuid = ?",
                new String[]{encounterUuid, UuidDictionary.OUT_OF_TIME});

        if (idCursor.getCount() > 0) {
            return true;
        }
        idCursor.close();

        return false;
    }

    public int updateOutOfTimeEncounterReason(String value, String encounterUuid, String visitUuid) {
        try {
//            new VisitsDAO().updateVisitSync(visitUuid, "0");
            new EncounterDAO().updateEncounterSync("0", encounterUuid);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        values.put("value", value);
        values.put("sync", "0");
        String whereClause = " encounteruuid = ? AND conceptuuid = ? AND voided IN ('0', 'false', 'FALSE')";
        String[] whereArgs = {encounterUuid, UuidDictionary.OUT_OF_TIME};
        return db.update("tbl_obs", values, whereClause, whereArgs);
    }

    public List<ObsDTO> getELCGObsByEncounterUuid(String encounteruuid, LinkedHashMap<String, String> conceptIds) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        String[] ids = new String[conceptIds.size()];
        ids = conceptIds.keySet().toArray(ids);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String query = new QueryBuilder().from("tbl_obs")
                .select("uuid, encounteruuid, conceptuuid, value, comment")
                .where("encounteruuid = '" + encounteruuid
                        + "' AND conceptuuid IN ('" + TextUtils.join("','", ids) + "')").build();
        Log.d(TAG, "getELCGObsByEncounterUuid: " + query);
        //take All obs except image obs
        Cursor idCursor = db.rawQuery(query, null);
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                obsDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                obsDTO.setName(conceptIds.get(obsDTO.getConceptuuid()));
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }

    public int countEncounterAlert(String encounterUuid) {
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String query = new QueryBuilder().from("tbl_obs")
                .select("sum(CASE WHEN comment = 'R' THEN 2 WHEN comment = 'Y' THEN 1 ELSE 0 END) as total")
                .where("encounteruuid = '" + encounterUuid + "'").build();
        Log.d(TAG, "countEncounterAlert: " + query);
        //take All obs except image obs
        Cursor idCursor = db.rawQuery(query, null);
        int total = 0;
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                total = idCursor.getInt(idCursor.getColumnIndexOrThrow("total"));
            }
        }
        idCursor.close();

        return total;
    }

    public List<ObsDTO> getAllPrescribedMedicinesByDoctor(String visitUuid) {
        //query to get all prescribed medicines by dr
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        List<ObsDTO> prescribedObsList = new ArrayList<>();
        ObsDTO obsDTO;
        String query = "SELECT o.uuid,o.conceptuuid,o.value,o.obsservermodifieddate,created_date FROM tbl_obs as o LEFT JOIN tbl_encounter as e on o.encounteruuid = e.uuid where e.visituuid = ? and  o.voided='0' AND o.conceptuuid = ? order by e.encounter_time";
        Cursor idCursor = db.rawQuery(query, new String[]{visitUuid, UuidDictionary.MEDICINE_PRESCRIBED});

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                String value = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                obsDTO.setValue(value);
                String created_date = idCursor.getString(idCursor.getColumnIndexOrThrow("created_date"));
                obsDTO.setCreatedDate(created_date);

                if (!value.equalsIgnoreCase("No")) {
                    prescribedObsList.add(obsDTO);
                }
            }
        }
        idCursor.close();
        return prescribedObsList;
    }

    public List<ObsDTO> getAllPrescribedIvFluidsByDoctor(String visitUuid) {
        //query to get all prescribed IvFluids by dr
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        List<ObsDTO> prescribedObsList = new ArrayList<>();
        ObsDTO obsDTO;
        String query = "SELECT o.uuid,o.conceptuuid, o.value,o.modified_date,created_date FROM tbl_obs as o LEFT JOIN tbl_encounter as e on o.encounteruuid = e.uuid where e.visituuid = ? and  o.voided='0' AND o.conceptuuid = ? order by o.modified_date DESC";
        Cursor idCursor = db.rawQuery(query, new String[]{visitUuid, UuidDictionary.IV_FLUID_PRESCRIBED});

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                String value = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                String created_date = idCursor.getString(idCursor.getColumnIndexOrThrow("created_date"));
                obsDTO.setValue(value);
                obsDTO.setCreatedDate(created_date);

                if (!value.equalsIgnoreCase("No")) {
                    prescribedObsList.add(obsDTO);
                }
            }
        }
        idCursor.close();
        return prescribedObsList;
    }

    public List<ObsDTO> getAllPrescribedOxytocinsByDoctor(String visitUuid) {
        //query to get all prescribed Oxytocins by dr
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        List<ObsDTO> prescribedObsList = new ArrayList<>();
        ObsDTO obsDTO;
        String query = "SELECT o.uuid,o.conceptuuid, o.value,o.modified_date,created_date FROM tbl_obs as o LEFT JOIN tbl_encounter as e on o.encounteruuid = e.uuid where e.visituuid = ? and  o.voided='0' AND o.conceptuuid = ? order by o.modified_date DESC";
        Cursor idCursor = db.rawQuery(query, new String[]{visitUuid, UuidDictionary.OXYTOCIN_PRESCRIBED});

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                String value = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                String created_date = idCursor.getString(idCursor.getColumnIndexOrThrow("created_date"));
                obsDTO.setValue(value);
                obsDTO.setCreatedDate(created_date);

                if (!value.equalsIgnoreCase("No")) {
                    prescribedObsList.add(obsDTO);
                }
            }
        }
        idCursor.close();
        return prescribedObsList;
    }

    public String getCreatorNameByObsUuid(String obsUuid) {
        Log.d(TAG, "getCreatorNameByObsUuid: obsUuid : " + obsUuid);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String creatorName = "";
        String query = "select p.given_name from tbl_obs as o LEFT JOIN tbl_provider as p  ON P.useruuid = O.creatoruuid AND P.role = 'Organizational: Nurse' where o.uuid = ?";
        Cursor idCursor = db.rawQuery(query, new String[]{obsUuid});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                creatorName = idCursor.getString(idCursor.getColumnIndexOrThrow("given_name"));
                Log.d(TAG, "getCreatorNameByObsUuid: creatorName: " + creatorName);
            }
        }
        idCursor.close();
        return creatorName;
    }

    public String getCreatorNameByObsUuidMedicine(String obsUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String creatorName = "";
        String query = "select p.given_name from tbl_obs as o LEFT JOIN tbl_provider as p  ON P.useruuid = O.creatoruuid where o.uuid = ?";
        Cursor idCursor = db.rawQuery(query, new String[]{obsUuid});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                creatorName = idCursor.getString(idCursor.getColumnIndexOrThrow("given_name"));
            }
        }
        idCursor.close();
        return creatorName;
    }
}
