package org.intelehealth.app.utilities;

import android.content.Context;
import org.intelehealth.app.utilities.CustomLog;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String TAG = FileUtils.class.getSimpleName();

    public static String readFile(String FILENAME, Context context) {
        CustomLog.i(TAG, "Reading from file");

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
            CustomLog.i("FILEREAD>", s);
            return s;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }

    }

    public static String readFileRoot(String FILENAME, Context context) {
        CustomLog.i(TAG, "Reading from file - "+FILENAME);

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + File.separator + FILENAME);
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
            CustomLog.i("FILEREAD>", s);
            return s;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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
            Toast.makeText(context, context.getString(R.string.config_file_missing), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return encoded;

    }

    /**
     * this method is required during license mm load from Engines folder
     * @param context
     * @param fileName
     * @return
     */
    public static JSONObject encodeJSONFromFile(Context context, String fileName) {
        JSONObject encoded = null;
        try {
            encoded = new JSONObject(FileUtils.readFile(fileName, context));

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return encoded;

    }

}
