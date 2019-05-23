package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.ProviderDTO;
import io.intelehealth.client.exception.DAOException;

public class ProviderDAO {

    long createdRecordsCount = 0;
    int updatecount = 0;
//    private SQLiteDatabase db = null;

    public boolean insertProviders(List<ProviderDTO> providerDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (ProviderDTO provider : providerDTOS) {
//                Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_provider where uuid = ?", new String[]{provider.getUuid()});
//                if (cursor.getCount() != 0) {
//                    while (cursor.moveToNext()) {
//                        updateProviders(provider);
//                    }
//                } else {
//                    Logger.logD("insert", "insert has to happen");
                createProviders(provider, db);
//                }
//                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

        return isInserted;
    }

    private boolean createProviders(ProviderDTO provider, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;

        ContentValues values = new ContentValues();

//        db.beginTransaction();
        try {
//            for (ProviderDTO provider : providerDTOS) {
//                Logger.logD("insert", "insert has to happen");
                values.put("uuid", provider.getUuid());
                values.put("identifier", provider.getIdentifier());
                values.put("given_name", provider.getGivenName());
                values.put("family_name", provider.getFamilyName());
                values.put("voided", provider.getVoided());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_provider", null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
//            db.setTransactionSuccessful();
//            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
//            db.endTransaction();
        }
        return isCreated;
    }

//    private boolean updateProviders(ProviderDTO provider) throws DAOException {
//        boolean isUpdated = true;
//
//        ContentValues values = new ContentValues();
//        String selection = "uuid = ?";
//        db.beginTransaction();
//        try {
////            for (ProviderDTO provider : providerDTOS) {
////                Logger.logD("insert", "insert has to happen");
//                values.put("identifier", provider.getIdentifier());
//                values.put("given_name", provider.getGivenName());
//                values.put("family_name", provider.getFamilyName());
//                values.put("voided", provider.getVoided());
//                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
//                values.put("synced", "TRUE");
////                Logger.logD("pulldata", "datadumper" + values);
//                updatecount = db.updateWithOnConflict("tbl_provider", values, selection, new String[]{provider.getUuid()}, SQLiteDatabase.CONFLICT_REPLACE);
////            }
//            db.setTransactionSuccessful();
////            Logger.logD("updated", "updatedrecords count" + updatecount);
//        } catch (SQLException e) {
//            isUpdated = false;
//            throw new DAOException(e.getMessage(), e);
//        } finally {
//            db.endTransaction();
//        }
//        return isUpdated;
//    }

}
