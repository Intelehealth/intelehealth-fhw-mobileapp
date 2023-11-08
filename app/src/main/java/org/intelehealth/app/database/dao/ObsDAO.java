package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.intelehealth.app.activities.prescription.PrescDataModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;
import org.intelehealth.app.models.dispenseAdministerModel.PastNotesModel;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.exception.DAOException;

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
            values.put("conceptuuid", obsDTOS.getConceptuuid());
            values.put("value", obsDTOS.getValue());
            values.put("obsservermodifieddate", obsDTOS.getObsServerModifiedDate());
            values.put("comment", obsDTOS.getComment());
            values.put("modified_date", obsDTOS.getEncounterTime());
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
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "false");
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }


    public boolean updateObs(ObsDTO obsDTO) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {

            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "false");

            updatedCount = db.update("tbl_obs", values, selection, new String[]{obsDTO.getUuid()});

            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }
//        If no value is not found, then update fails so insert instead.
        if (updatedCount == 0) {
            try {
                insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }


        return true;
    }

    public boolean insertObsToDb(List<ObsDTO> obsDTO) throws DAOException {
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
                values.put("conceptuuid", ob.getConceptuuid());
                values.put("value", ob.getValue());
                values.put("modified_date", ob.getObservationTime());
                values.put("voided", "0");
                values.put("sync", "false");    //Earlier was set to FALSE which caused the issue.
                insertedCount = db.insert("tbl_obs", null, values);
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public List<ObsDTO> obsDTOList(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //take All obs except image obs
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND (conceptuuid != ? AND conceptuuid != ?) AND voided='0' AND sync='false'", new String[]{encounteruuid, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE});
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
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
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
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
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "true");
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return isUpdated;

    }

    // select * from tbl_obs where encounteruuid = '795d291f-3f70-4086-8847-1972f797d8e6' and
    // conceptuuid = 'c38c0c50-2fd2-4ae3-b7ba-7dd25adca4ca' and comment is null
    public static List<MedicationAidModel> getObsDispenseAdministerData(String encounterUuid, String conceptUuid) throws DAOException {
        List<MedicationAidModel> list = new ArrayList<>();

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        Cursor obsCursoursor = db.rawQuery("select * from tbl_obs where conceptuuid=? and encounteruuid=? and voided='0' " +
                "and comment is null", new String[]{conceptUuid, encounterUuid});
        try {
            if (obsCursoursor.getCount() > 0) {
                while (obsCursoursor.moveToNext()) {
                    MedicationAidModel model = new MedicationAidModel();
                    model.setUuid(obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("uuid")));
                    model.setValue(obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("value")));
                    list.add(model);
                }
                db.setTransactionSuccessful();
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            obsCursoursor.close();
            db.endTransaction();
        }

        return list;
    }

    public static MedicationAidModel getObsValue(String encounterUuid, String conceptUuid) throws DAOException {
        MedicationAidModel medicationAidModel = new MedicationAidModel();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        Cursor obsCursoursor = db.rawQuery("select * from tbl_obs where conceptuuid=? and encounteruuid=? and voided='0' " +
                "and comment is null", new String[]{conceptUuid, encounterUuid});
        try {
            if (obsCursoursor.getCount() > 0) {
                while (obsCursoursor.moveToNext()) {
                    medicationAidModel.setUuid(obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("uuid")));
                    medicationAidModel.setValue(obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("value")));
                }
                db.setTransactionSuccessful();
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            obsCursoursor.close();
            db.endTransaction();
        }

        return medicationAidModel;
    }


    public static List<PastNotesModel> getObsPastNotes(String visitUUID, String encounterTypeUUID, String conceptUUID) throws DAOException {
        List<PastNotesModel> pastNotesModelList = new ArrayList<>();

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        Cursor obsCursoursor = db.rawQuery("select o.value from tbl_encounter as e join tbl_obs as o " +
                        "on e.uuid = o.encounteruuid " +
                        "where e.visituuid = ? and o.comment is null and o.voided = 0 and " +
                        "(o.sync = 'TRUE' OR o.sync = 'true' OR o.sync = 1) and " +
                        "e.encounter_type_uuid = ? and o.conceptuuid = ?",
                new String[]{visitUUID, encounterTypeUUID, conceptUUID});   // ENC_DISPENSE / OBS_DISPENSE_MEDICATION.
        try {
            if (obsCursoursor.getCount() > 0) {
                while (obsCursoursor.moveToNext()) {
                    PastNotesModel model = new PastNotesModel();
                    model.setValue(obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("value")));
                    pastNotesModelList.add(model);
                }
                db.setTransactionSuccessful();
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            obsCursoursor.close();
            db.endTransaction();
        }

        return pastNotesModelList;
    }





}
