package app.intelehealth.client.utilities;

import android.util.Log;
import android.util.Pair;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

import static app.intelehealth.client.app.AppConstants.IMAGE_PATH;

public class DownloadFilesUtils {

    String TAG = DownloadFilesUtils.class.getSimpleName();

    public void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(IMAGE_PATH, filename + ".jpg");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte[] data = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                return;
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }

}

