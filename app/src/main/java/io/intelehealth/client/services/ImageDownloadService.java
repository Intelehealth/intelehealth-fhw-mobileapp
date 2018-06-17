package io.intelehealth.client.services;

/**
 * Created by Dexter Barretto on 17/6/18.
 * Github : @dbarretto
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.intelehealth.client.database.LocalRecordsDatabaseHelper;

/**
 * Created by Dexter Barretto on 16/6/18.
 * Github : @dbarretto
 */
public class ImageDownloadService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ImageDownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        queryAllImagesFromDatabase(this);
        queryAllProfileImages(this);
    }

    private void queryAllImagesFromDatabase(Context context) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(context);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        String[] coloumns = {"_id", "parse_id", "image_path", "image_type", "delete_status"};
        Cursor cursor = localdb.query("image_records", coloumns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String parse_id = cursor.getString(cursor.getColumnIndexOrThrow("parse_id"));
            String image_file_path = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
            Integer delete_status = cursor.getInt(cursor.getColumnIndexOrThrow("delete_status"));
            String image_type = cursor.getString(cursor.getColumnIndexOrThrow("image_type"));
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            File image_file = new File(image_file_path);
            if (!image_file.exists()) {
                image_file.mkdirs();
                if (delete_status.equals(0) || parse_id == null || !parse_id.isEmpty()) {
                    String selection = "_id =?";
                    String[] selectionArgs = {String.valueOf(id)};
                    localdb.delete("image_records", selection, selectionArgs);
                }
                if (parse_id != null && !parse_id.isEmpty()) {
                    downloadImagesFromParse(context, parse_id, image_type, image_file);
                }
            }
        }
        localdb.close();
    }

    private void downloadImagesFromParse(final Context context, String parse_id, String image_type, final File img_file) {
        if (parse_id != null && !parse_id.isEmpty() &&
                image_type != null && !image_type.isEmpty()) {
            switch (image_type) {
                case "AD": {
                    image_type = "AdditionalDocuments";
                    break;
                }
                case "PE": {
                    image_type = "PhysicalExam";
                    break;
                }
                case "MH": {
                    image_type = "MedicalHistory";
                    break;
                }
            }
            ParseQuery<ParseObject> query = ParseQuery.getQuery(image_type);
            query.whereEqualTo("objectId", parse_id);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        Toast.makeText(context, "Image not available", Toast.LENGTH_SHORT).show();
                    } else {
                        final ParseFile file = (ParseFile) object.get("Image");
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {

                                if (img_file.exists()) img_file.delete();
                                try {
                                    img_file.createNewFile();
                                    FileOutputStream fileOutputStream = new FileOutputStream(img_file);
                                    fileOutputStream.write(data);
                                    fileOutputStream.close();
                                } catch (FileNotFoundException exfnf) {
                                } catch (IOException exio) {
                                    Toast.makeText(context, "Error downloading image", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void queryAllProfileImages(Context context) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(context);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        String[] coloumns = {"_id", "openmrs_uuid", "patient_photo"};
        Cursor cursor = localdb.query("patient", coloumns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String openmrs_uuid = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_uuid"));
            String image_file_path = cursor.getString(cursor.getColumnIndexOrThrow("patient_photo"));
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            if (image_file_path != null && !image_file_path.isEmpty()) {
                File image_file = new File(image_file_path);
                if (!image_file.exists()) {
                    image_file.mkdirs();
                    if (openmrs_uuid == null || !openmrs_uuid.isEmpty()) {
                        downloadProfilePhotoFromParse(this, openmrs_uuid, image_file);
                    }
                }
            }
        }
    }

    private void downloadProfilePhotoFromParse(final Context context, String openmrs_uuid, final File img_file) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
        query.whereEqualTo("PatientID", openmrs_uuid);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Toast.makeText(context, "Image not available", Toast.LENGTH_SHORT).show();
                } else {
                    final ParseFile file = (ParseFile) object.get("Image");
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (img_file.exists()) img_file.delete();
                            try {
                                img_file.createNewFile();
                                FileOutputStream fileOutputStream = new FileOutputStream(img_file);
                                fileOutputStream.write(data);
                                fileOutputStream.close();
                            } catch (FileNotFoundException exfnf) {
                            } catch (IOException exio) {
                                Toast.makeText(context, "Error downloading profile picture", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}

