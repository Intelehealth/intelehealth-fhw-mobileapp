package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class ProviderProfileDao {
    private static final String TAG = "ProviderProfileDao";
    long createdRecordsCount = 0;


/*
    public boolean updateProfilePicture(String provider_id, String imagePath) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "provider_id = ?";
        try {
            values.put("provider_id", provider_id);
            values.put("image_path", imagePath);

            updatedCount = db.update("tbl_provider_profile", values, selection, new String[]{provider_id});
            //If no value is not found, then update fails so insert instead.
            if (updatedCount == 0) {
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }

        return true;
    }
   public List<ProviderProfileDTO> unsyncedProviderDetails() throws DAOException {
        List<ProviderProfileDTO> providerDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_provider_profile where (sync = ? OR sync=?) COLLATE NOCASE", new String[]{"0", "false"});
            ProviderProfileDTO providerProfileDTO = new ProviderProfileDTO();
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    providerProfileDTO = new ProviderProfileDTO();
                    providerProfileDTO.setProvider_id(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    providerProfileDTO.setFirstName(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                    providerProfileDTO.setMiddleName(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                    providerProfileDTO.setLastName(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                    providerProfileDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    providerProfileDTO.setDateOfBirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                    providerProfileDTO.setAge(idCursor.getString(idCursor.getColumnIndexOrThrow("age")));
                    providerProfileDTO.setPhoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                    providerProfileDTO.setEmail(idCursor.getString(idCursor.getColumnIndexOrThrow("email")));
                    providerProfileDTO.setImagePath(idCursor.getString(idCursor.getColumnIndexOrThrow("image_path")));
                    providerDTOList.add(providerProfileDTO);
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return providerDTOList;
    }
    public boolean updateProviderProfileSync(String uuid, String synced) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("updateProviderProfileSync", "updateProviderProfileSync profile " + uuid + synced);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "provider_id=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);
            values.put("provider_id", uuid);
            int i = db.update("tbl_provider_profile", values, whereclause, whereargs);
            Logger.logD("profile", "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD("profile", "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }

        return isUpdated;
    }*/

}
