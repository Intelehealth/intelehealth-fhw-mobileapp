package io.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.models.ObsImageModel.ObsPushDTO;
import io.intelehealth.client.models.patientImageModelRequest.PatientProfile;
import io.intelehealth.client.utilities.Base64Utils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;

public class ImagesDAO {
    public String TAG = ImagesDAO.class.getSimpleName();

    public boolean insertObsImageDatabase(String uuid, String encounteruuid, String conceptUuid) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", uuid);
            contentValues.put("encounteruuid", encounteruuid);
            contentValues.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("conceptuuid", conceptUuid);
            contentValues.put("voided", "0");
            contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_obs", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return isInserted;
    }

    public boolean updateObs(String uuid) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
            values.put("sync", "TRUE");
            updatedCount = db.update("tbl_obs", values, selection, new String[]{uuid});
            //If no value is not found, then update fails so insert instead.
            if (updatedCount == 0) {
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();
            db.close();
        }

        return true;
    }

    public void deleteImageFromDatabase(String uuid) throws DAOException {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] coloumns = {"uuid"};
        String[] selectionArgs = {uuid};
        localdb.beginTransaction();
        try {
            Cursor cursor = localdb.query("tbl_obs", coloumns, "uuid = ?", selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                localdb.execSQL("UPDATE tbl_obs SET voided = '1' WHERE uuid=" + "'" + uuid + "' AND sync='false'");
                localdb.setTransactionSuccessful();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }

    }

    public List<String> getVoidedImageObs() throws DAOException {
        Logger.logD(TAG, "uuid for images");
        ArrayList<String> uuidList = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where (conceptuuid=? OR conceptuuid = ?) AND voided=? AND sync = ? COLLATE NOCASE", new String[]{UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE, "1", "false"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuidList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return uuidList;

    }

    public boolean insertPatientProfileImages(String imagepath, String patientUuid) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", UUID.randomUUID().toString());
            contentValues.put("patientuuid", patientUuid);
            contentValues.put("visituuid", "");
            contentValues.put("image_path", imagepath);
            contentValues.put("image_type", "PP");
            contentValues.put("obs_time_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_image_records", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return isInserted;
    }

    public boolean updatePatientProfileImages(String imagepath, String patientuuid) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "patientuuid = ? AND image_type = ?";
        try {
            contentValues.put("patientuuid", patientuuid);
            contentValues.put("image_path", imagepath);
            contentValues.put("obs_time_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "false");
            isupdate = localdb.update("tbl_image_records", contentValues, whereclause, new String[]{patientuuid, "PP"});
            if (isupdate != 0)
                isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return isUpdated;
    }

    public List<PatientProfile> getPatientProfileUnsyncedImages() throws DAOException {
        List<PatientProfile> patientProfiles = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        Base64Utils base64Utils = new Base64Utils();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where sync = ? OR sync=? AND image_type = ? COLLATE NOCASE", new String[]{"0", "false", "PP"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    PatientProfile patientProfile = new PatientProfile();
                    patientProfile.setPerson(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    patientProfile.setBase64EncodedImage(base64Utils.getBase64FromFileWithConversion(idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"))));
                    patientProfiles.add(patientProfile);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }

        return patientProfiles;
    }

    public List<ObsPushDTO> getObsUnsyncedImages() throws DAOException {
        List<ObsPushDTO> obsImages = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("select c.uuid as patientuuid,d.conceptuuid,a.uuid as encounteruuid,d.uuid as obsuuid,d.modified_date  from tbl_encounter a , tbl_visit b , tbl_patient c,tbl_obs d \n" +
                    "where a.visituuid=b.uuid and b.patientuuid=c.uuid and d.encounteruuid=a.uuid and (d.sync=0 or d.sync='false') and (d.conceptuuid=? or d.conceptuuid=?) and d.voided='0'", new String[]{UuidDictionary.COMPLEX_IMAGE_PE, UuidDictionary.COMPLEX_IMAGE_AD});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    ObsPushDTO obsPushDTO = new ObsPushDTO();
                    obsPushDTO.setConcept(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                    obsPushDTO.setEncounter(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                    obsPushDTO.setObsDatetime(idCursor.getString(idCursor.getColumnIndexOrThrow("modified_date")));
                    obsPushDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("obsuuid")));
                    obsPushDTO.setPerson(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
//                    obsPushDTO.setPerson(getPatientUuidFromEncounter(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")), localdb));
                    obsImages.add(obsPushDTO);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }

        return obsImages;

    }

    public String getobsImagePath(String uuid) throws DAOException {
        String imagePath = "";
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where uuid=? COLLATE NOCASE", new String[]{uuid});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    imagePath = idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }

        return imagePath;
    }

    public String getPatientProfileChangeTime(String patientUuid) throws DAOException {
        String datetime = "";
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where patientuuid=? AND image_type = ? COLLATE NOCASE", new String[]{patientUuid, "PP"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    datetime = idCursor.getString(idCursor.getColumnIndexOrThrow("obs_time_date"));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }

        return datetime;
    }


    public boolean updateUnsyncedPatientProfile(String patientuuid, String type) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "patientuuid = ? AND image_type = ?";
        try {
            contentValues.put("patientuuid", patientuuid);
            contentValues.put("sync", "true");
            isupdate = localdb.update("tbl_image_records", contentValues, whereclause, new String[]{patientuuid, type});
            if (isupdate != 0)
                isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return isUpdated;
    }

    public boolean updateUnsyncedObsImages(String uuid) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "uuid = ?";
        try {
            contentValues.put("uuid", uuid);
            contentValues.put("sync", "true");
            isupdate = localdb.update("tbl_obs", contentValues, whereclause, new String[]{uuid});
            if (isupdate != 0)
                isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return isUpdated;
    }

    private String getImageTypeUUid(String imageType) {
        String imagetype = "";
        if (imageType.equalsIgnoreCase("AD"))
            imagetype = UuidDictionary.COMPLEX_IMAGE_AD;
        else
            imagetype = UuidDictionary.COMPLEX_IMAGE_PE;

        return imagetype;
    }

    public String getPatientUuidFromEncounter(String encounterUuid, SQLiteDatabase localdb) throws DAOException {
        Logger.logD(TAG, "encounter uuid " + encounterUuid);
        String visituuid = "";
//        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_encounter where uuid=? AND voided=? COLLATE NOCASE", new String[]{encounterUuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    visituuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
//            localdb.endTransaction();
//            localdb.close();
        }
        VisitsDAO visitsDAO = new VisitsDAO();
        return visitsDAO.patientUuidByViistUuid(visituuid);

    }

    public ArrayList getImageUuid(String encounterUuid, String conceptuuid) throws DAOException {
        Logger.logD(TAG, "encounter uuid for image " + encounterUuid);
        ArrayList<String> uuidList = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE", new String[]{encounterUuid, conceptuuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuidList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }
        return uuidList;
    }


}

