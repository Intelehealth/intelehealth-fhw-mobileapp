package org.intelehealth.app.ayu.visit.pocdevice;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class S3Uploader {

    private static final String TAG = "S3Uploader";

    public interface UploadCallback {
        void onSuccess();
        void onError(String error);
    }

    private final OkHttpClient client;

    public S3Uploader() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Upload WAV file to S3 using presigned URL
     *
     * @param presignedUrl S3 presigned URL (PUT)
     * @param filePath local WAV file path
     * @param callback result callback
     */
    public void uploadFile(String presignedUrl, String filePath, UploadCallback callback) {

        File file = new File(filePath);

        if (!file.exists()) {
            callback.onError("File not found");
            return;
        }

        Log.d(TAG, "Uploading file: " + file.getAbsolutePath());
        Log.d(TAG, "File size: " + file.length());

        RequestBody requestBody = RequestBody.create(
                file,
                MediaType.parse("audio/wav")
        );

        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .addHeader("Content-Type", "audio/wav")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Upload failed: " + e.getMessage());
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Upload success. Code: " + response.code());
                    callback.onSuccess();
                } else {
                    String error = "Upload failed. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
                response.close();
            }
        });
    }
}
