package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.ProviderProfileDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class ProviderProfileDao {
    private static final String TAG = "ProviderProfileDao";
    long createdRecordsCount = 0;

    public boolean insertProvidersProfile(List<ProviderProfileDTO> providerDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (ProviderProfileDTO provider : providerDTOS) {
                isInserted =createProvidersProfile(provider, db);
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
    // uuid,username,first_name,middle_name,last_name,gender,date_of_birth,age,phone_number,country_code,email,image_type

    private boolean createProvidersProfile(ProviderProfileDTO provider, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;

        ContentValues values = new ContentValues();
        try {
            values.put("provider_id", provider.getProvider_id());
            values.put("username", provider.getUsername());
            values.put("first_name", provider.getFirstName());
            values.put("middle_name", provider.getMiddleName());
            values.put("last_name", provider.getLastName());
            values.put("gender", provider.getGender());
            values.put("date_of_birth", provider.getDateOfBirth());
            values.put("age", provider.getAge());
            values.put("phone_number", provider.getPhoneNumber());
            values.put("country_code", provider.getCountryCode());
            values.put("email", provider.getEmail());
            values.put("image_path", provider.getImagePath());


            createdRecordsCount = db.insertWithOnConflict("tbl_provider_profile",
                    null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

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

    public ProviderProfileDTO getProvidersDetails() throws DAOException {
        //List<String> providersList = new ArrayList<>();
        ProviderProfileDTO providerProfileDTO = new ProviderProfileDTO();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select * from tbl_provider_profile";
            Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    String provider_id  = cursor.getString(cursor.getColumnIndexOrThrow("provider_id"));
                    String username  = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    String first_name  = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
                    String middle_name  = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"));
                    String last_name  = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
                    String gender  = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
                    String date_of_birth  = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
                    String age  = cursor.getString(cursor.getColumnIndexOrThrow("age"));
                    String phone_number  = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                    String country_code  = cursor.getString(cursor.getColumnIndexOrThrow("country_code"));
                    String email  = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    String image_path  = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                     providerProfileDTO = new ProviderProfileDTO(provider_id,username,
                            first_name,middle_name,
                            last_name,gender,date_of_birth,age,
                            phone_number,country_code,email,image_path);


                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }
        return providerProfileDTO;

    }
}
