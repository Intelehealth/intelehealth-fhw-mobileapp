package io.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.UUID;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.utilities.exception.DAOException;

public class ImagesDAO {

    public boolean insertImageDatabase(String patientUuid, String visitUuid, String imagePath, String imageprefix) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", UUID.randomUUID().toString());
            contentValues.put("patientuuid", patientUuid);
            contentValues.put("visituuid", visitUuid);
            contentValues.put("image_path", imagePath);
            contentValues.put("image_type", imageprefix);
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

    public void deleteImageFromDatabase(String imagePath) throws DAOException {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] coloumns = {"uuid", "image_path"};
        String[] selectionArgs = {imagePath};
        localdb.beginTransaction();
        try {
            Cursor cursor = localdb.query("tbl_image_records", coloumns, "image_path = ?", selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                localdb.execSQL("DELETE FROM tbl_image_records WHERE image_path=" + "'" + imagePath + "'");
                localdb.setTransactionSuccessful();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
            localdb.close();
        }

    }

}

