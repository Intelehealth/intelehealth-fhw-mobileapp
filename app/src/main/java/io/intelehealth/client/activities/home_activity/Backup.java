package io.intelehealth.client.activities.home_activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.amitshekhar.utils.DatabaseHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.intelehealth.client.activities.patient_detail_activity.PatientDetailActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by twinkle dhanak on 7/4/2017.
 */



//  consists of all methods that deal with backup
public class Backup
{
    String dbpath = "" , newfilepath = "";
    File dbfile , myfile;
    FileInputStream fis;FileOutputStream fos;
    String value = "";
    SharedPreferences sharedPreferences ;
    SharedPreferences.Editor e ;



    public boolean checkDatabaseForData(Context context) // checks if data exists in db
    {
        SQLiteDatabase checkDB = null;
        boolean exists = false;
        try {

            dbpath = String.valueOf(context.getDatabasePath("localRecords.db").getPath());

            Log.d("dbpath", dbpath);
            // checkDB = SQLiteDatabase.openDatabase(dbpath, null, SQLiteDatabase.OPEN_READONLY);
            // check user_provider table, if empty, db empty
            LocalRecordsDatabaseHelper mDatabaseHelper = new LocalRecordsDatabaseHelper(context);
            SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getReadableDatabase();
            String query = "SELECT * FROM patient"; // patient is the first table to get populated
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                exists = true; // data already exists on db
            } else {
                exists = false; // restore of db is required
            }


        } catch (SQLiteException e) {
            Log.d("DB error:", String.valueOf(e));
            exists = false; // restore of db is required
        }

        return exists;
    }


    // when to call??????????
    public void createFileInMemory(Context context) throws IOException {

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            e = sharedPreferences.edit();

            value = sharedPreferences.getString("value","");


        // create a directory (folder) that will store documents
        // directory created inside: data/data/io.intelehealth/Documents/Hello.db
        // String baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        try {

            File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB");
            if (myDir.exists()) {
            } else {
                myDir.mkdir();
            }
            //file created inside internal memory, outside app package, doesnt delete if app is uninstalled
            newfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" +
                    File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
            Log.d("newfilepath", newfilepath);
            myfile = new File(newfilepath);
            Log.d("myfile path", myfile.getPath().toString());
            if (myfile.exists()) {
                //myfile.createNewFile();
                Toast.makeText(context,"yes my file exists",Toast.LENGTH_SHORT).show();

            } else {
                //myfile.createNewFile();
            }

            //dbfile = new File(dbpath);
            dbfile = new File(context.getDatabasePath("localRecords.db").getPath());
            if (dbfile.exists()) {
                dbfile.createNewFile();

            } else {
                dbfile.createNewFile();
                Toast.makeText(context, "dbfile doesnot exist", Toast.LENGTH_SHORT).show();
            }

            //copyFile(new FileInputStream(dbfile), new FileOutputStream(myfile));



           if(value.matches("yes")) {
                Toast.makeText(context,"Copying into your file",Toast.LENGTH_SHORT).show();
                Log.d("Copying into your file",value);
                fis = new FileInputStream(dbfile);
                fos = new FileOutputStream(myfile);
                readContents(context);
                copyFile(context,fis, fos);
                readContents(context);
            }
          if(value.matches("no"))
            {
                Toast.makeText(context,"Copying into database",Toast.LENGTH_SHORT).show();
                Log.d("Copying into database",value);
                fis = new FileInputStream(myfile);
                fos = new FileOutputStream(dbfile);
                readContents(context);
                copyFile(context,fis,fos);
                readContents(context);
            }


        }
        catch (Exception ie)
        {
            Log.d("Error: ",ie.toString() );
        }


    }



    public void copyFile(Context context,FileInputStream fromFile, FileOutputStream toFile) throws Exception {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;

        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            try {
                fromChannel.transferTo(0, fromChannel.size(), toChannel);

            } catch (IOException e) {
                Log.d("transfer failed:", String.valueOf(e));
            }
        } finally {
            //Long millisecond = myfile.lastModified();
            //Date d = new Date(millisecond);
            //Log.d("Last-Modified");
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }

    }


   public void readContents(Context context) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        try {
            //FileInputStream fis = context.openFileInput(newfilepath);
            FileInputStream fis = new FileInputStream(myfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line = "";

            while ((line = br.readLine()) != null)
                 {
                     line = br.readLine();
                     sb.append(line);
                 }

        } catch (Exception e) {
            Log.d("readerror",e.toString());
            Toast.makeText(context,"Not able to read the file!!",Toast.LENGTH_SHORT).show();
        }
       // String date = java.text.DateFormat.getDateTimeInstance().format(new Date());
        Calendar c = Calendar.getInstance();
        String time = String.valueOf(c.getTime());

        SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");
        String date = df2.format(c.getTime());

        Log.d("Last backup time: ",time);
        Log.d("Last backup date: ",date);
        e.putString("date",date);
        e.putString("time",time);
        e.apply();

        Log.d("file contents: ", String.valueOf(sb));
        Toast.makeText(context,"File contents::     "+String.valueOf(sb),Toast.LENGTH_SHORT).show();
    }




}



