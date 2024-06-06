package org.intelehealth.ekalarogya.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalarogya.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.intelehealth.ekalarogya.app.AppConstants;

public class FileUtils {
    public static String TAG = FileUtils.class.getSimpleName();

    public static String readFile(String FILENAME, Context context) {
        Log.i(TAG, "Reading from file");

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + AppConstants.JSON_FOLDER + File.separator + FILENAME);
            FileInputStream fileIn = new FileInputStream(myDir);
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            final int READ_BLOCK_SIZE = 100;
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            Log.i("FILEREAD>", s);
            return s;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }

    }

    public static String readFileRoot(String FILENAME, Context context) {
        Log.i(TAG, "Reading from file");

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + File.separator + FILENAME);
            myDir.mkdirs(); //  AEAT-552: error: No such file/directory: this will create the dir if its not present.

            FileInputStream fileIn = new FileInputStream(myDir);
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            final int READ_BLOCK_SIZE = 100;
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            Log.i("FILEREAD>", s);
            return s;

        } catch (Exception e) {
            return null;
        }

    }

    public static JSONObject encodeJSON(Context context, String fileName) {
        String raw_json = null;
        JSONObject encoded = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            raw_json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        try {
            encoded = new JSONObject(raw_json);
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return encoded;

    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static String getDirectoryForProtocols(String adviceType) {
        if (adviceType.equalsIgnoreCase("Sevika")) {
            return AppConstants.NCD_PROTOCOL_DIRECTORY;
        } else {
            return AppConstants.PROTOCOL_DIRECTORY;
        }
    }
}
