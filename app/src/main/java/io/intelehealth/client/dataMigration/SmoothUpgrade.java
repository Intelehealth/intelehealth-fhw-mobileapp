package io.intelehealth.client.dataMigration;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.IOException;

import io.intelehealth.client.BuildConfig;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.backup.Backup;
import io.intelehealth.client.utilities.SessionManager;

import static io.intelehealth.client.app.AppConstants.APP_VERSION_CODE;

public class SmoothUpgrade {
    public SQLiteDatabase myDataBase;
    SessionManager sessionManager = null;
    Context context;
    Backup backup = new Backup();
    boolean dbexist = checkdatabase();

    public SmoothUpgrade(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    public void copyDatabase() {
        int versionCode = BuildConfig.VERSION_CODE;

        if (versionCode <= APP_VERSION_CODE) {
            try {
                backup.createFileInMemory(context, true);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

    }

    public Boolean checkingDatabase() {

        copyDatabase();

        if (dbexist) {
            System.out.println("Database exists");
            opendatabase();
            insertOfflineOldData();
        } else {
            System.out.println("Database doesn't exist");
//            createdatabase();
        }

        return true;
    }

    public void insertOfflineOldData() {
        myDataBase.beginTransaction();
//        try {
//            Cursor cursor = myDataBase.rawQuery("Select * from patient where openmrsid IS NULL OR openmrsid =''", null);
//            if (cursor != null) {
//                cursor.moveToFirst();
//                while (!cursor.isAfterLast()) {
//                    String containerData = cursor.getString(cursor.getColumnIndexOrThrow("firstname"));
//                    cursor.moveToNext();
//                    Logger.logD("ShowData->", containerData);
//                }
//            }
//            if (cursor != null) {
//                cursor.close();
//            }
//            myDataBase.setTransactionSuccessful();
//        }catch (SQLiteException e){
//            e.printStackTrace();
//        }
    }


    private boolean checkdatabase() {

        boolean checkdb = false;
        try {
            File dbfile = new File(AppConstants.dbfilepath);
            checkdb = dbfile.exists();
        } catch (SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }


    public void opendatabase() throws SQLException {
        //Open the database
        String mypath = AppConstants.dbfilepath;
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }
}
