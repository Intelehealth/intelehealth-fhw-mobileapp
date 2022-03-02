package org.intelehealth.swasthyasamparktelemedicine.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

import org.intelehealth.swasthyasamparktelemedicine.app.AppConstants;
import org.intelehealth.swasthyasamparktelemedicine.models.dto.ProviderDTO;
import org.intelehealth.swasthyasamparktelemedicine.utilities.exception.DAOException;

public class ProviderDAO {

    long createdRecordsCount = 0;

    public boolean insertProviders(List<ProviderDTO> providerDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (ProviderDTO provider : providerDTOS) {
                createProviders(provider, db);
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

    private boolean createProviders(ProviderDTO provider, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;

        ContentValues values = new ContentValues();
        try {
            values.put("uuid", provider.getUuid());
            values.put("identifier", provider.getIdentifier());
            values.put("given_name", provider.getGivenName());
            values.put("family_name", provider.getFamilyName());
            values.put("voided", provider.getVoided());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "TRUE");

            createdRecordsCount = db.insertWithOnConflict("tbl_provider", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

    public List<String> getProvidersList() throws DAOException {
        List<String> providersList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select distinct a.uuid,a.given_name,a.family_name from tbl_provider a, tbl_encounter b , tbl_visit c where a.uuid=b.provider_uuid and b.visituuid=c.uuid";
            Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providersList.add(cursor.getString(cursor.getColumnIndexOrThrow("given_name")) + cursor.getString(cursor.getColumnIndexOrThrow("family_name")));

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
        return providersList;

    }

    //this function is created to query all the provider which have cases assigned to them in patient table attribute.
    public List<String> getProvidersListUpdated() throws DAOException {
        List<String> providersList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select distinct a.uuid,a.given_name,a.family_name, b.value from tbl_provider a, tbl_patient_attribute b where b.person_attribute_type_uuid = '29456b35-23bb-46f9-b2d1-e6c241c653ba' AND b.value = a.uuid";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providersList.add(cursor.getString(cursor.getColumnIndexOrThrow("given_name")) + cursor.getString(cursor.getColumnIndexOrThrow("family_name")));

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
        return providersList;

    }


    public List<String> getProvidersUuidList() throws DAOException {
        List<String> providersList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select distinct a.uuid,a.given_name,a.family_name from tbl_provider a, tbl_encounter b , tbl_visit c where a.uuid=b.provider_uuid and b.visituuid=c.uuid";
            Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providersList.add(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));

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
        return providersList;

    }

    public List<String> getProvidersUuidListUpdated() throws DAOException {
        List<String> providersList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select distinct a.uuid,a.given_name,a.family_name, b.value from tbl_provider a, tbl_patient_attribute b where b.person_attribute_type_uuid = '29456b35-23bb-46f9-b2d1-e6c241c653ba' AND b.value = a.uuid";
            Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providersList.add(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));

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
        return providersList;

    }

}
