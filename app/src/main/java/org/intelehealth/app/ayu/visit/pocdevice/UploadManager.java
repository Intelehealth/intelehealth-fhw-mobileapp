package org.intelehealth.app.ayu.visit.pocdevice;


import android.util.Base64;
import android.util.Log;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.report.HeartSoundData;
import com.ayudevice.ayusynksdk.report.SoundFile;
import com.ayudevice.ayusynksdk.report.constants.LocationType;
import com.ayudevice.ayusynksdk.report.listener.DiagnosisReportUpdateListener;
import com.ayudevice.ayusynksdk.utils.logs.AyuLogsListener;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.PresignedResponse;
import org.intelehealth.app.models.UploadResponse;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadManager {
    private static final String TAG = "UPLOAD_MANAGER";
    static SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    static String currentWavPath;
    static String trackerId;
    static String postion;

    public interface UploadListener {
        void onUploadSuccess(String trackerId);
        void onAIResultSuccess(String trackerId, String result);
    }

    private static UploadListener uploadListener;

    public static void setUploadListener(UploadListener listener) {
        uploadListener = listener;
    }

    public static void uploadRecording(String wavPath, String visitUuid, String type, String position_from) {
        Log.d(TAG, "Starting upload...");
        String creatorId = sessionManager.getCreatorID();
        postion = position_from;
        File file = new File(wavPath);
        currentWavPath = wavPath;
        if (!file.exists()) {
            Log.e(TAG, "File not found: " + wavPath);
            return;
        }
        Log.d(TAG, "File size: " + file.length());
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("audio/wav"),
                        file);
        MultipartBody.Part audioFile =
                MultipartBody.Part.createFormData(
                        "audio_file",
                        file.getName(),
                        requestFile);
        RequestBody visit_uuid_body =
                RequestBody.create(
                        MediaType.parse("text/plain"),
                        visitUuid);
        RequestBody creator_uuid =
                RequestBody.create(
                        MediaType.parse("text/plain"),
                        creatorId);
        RequestBody sound_type =
                RequestBody.create(
                        MediaType.parse("text/plain"),
                        type);
        RequestBody position =
                RequestBody.create(
                        MediaType.parse("text/plain"),
                        position_from);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UploadResponse> call = apiService.uploadSound(
                audioFile,
                visit_uuid_body,
                creator_uuid,
                sound_type,
                position);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call,
                                   Response<UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UploadResponse uploadResponse = response.body();
                    Log.d(TAG, "Upload Success");
                    trackerId = uploadResponse.getTracker();
                    
                    if (uploadListener != null) {
                        uploadListener.onUploadSuccess(trackerId);
                    }
                    
                    getPresignedUrl(trackerId, postion);
                } else {
                    Log.e(TAG, "Upload failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Log.e(TAG, "Upload error: " + t.getMessage());
            }
        });
    }

    private static void getPresignedUrl(String tracker, String position) {

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<PresignedResponse> call = apiService.getPresignedUrl(tracker);

        call.enqueue(new Callback<PresignedResponse>() {
            @Override
            public void onResponse(Call<PresignedResponse> call, Response<PresignedResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String url = response.body().presigned_url;


                    Log.d(TAG, "Presigned URL: " + url);

                    if (url != null) {
                        sendFileToAI(currentWavPath, position);
                    }

                } else {
                    Log.e(TAG, "Failed to get presigned URL");
                }
            }

            @Override
            public void onFailure(Call<PresignedResponse> call, Throwable t) {
                Log.e(TAG, "Presigned API error: " + t.getMessage());
            }
        });
    }

    // =========================
    // STEP 3: Send to AI SDK
    // =========================
    private static void sendFileToAI(String filePath, String position) {

        Log.d("AI_FLOW", "Using FILE method");
        Log.d("AI_DEBUG", "File path: " + filePath);

        File wavFile = new File(filePath);

        if (!wavFile.exists()) {
            Log.e("AI_FLOW", "❌ File not found");
            return;
        }

      /*  LocationType.Heart location = mapPosition(position);
      //  HeartSoundData heartSoundData = new HeartSoundData(getFileFromLastRecordedAudio(), LocationType.Heart.aortic);
        SoundFile<HeartSoundData> soundFile = new SoundFile<>(heartSoundData);
        AyuSynk.getBleInstance().generateDiagnosisReport(soundFile);*/


        // ✅ FILE-BASED INPUT
        /*HeartSoundData hs = new HeartSoundData(wavFile, location);
        //SoundFile<HeartSoundData> soundFile = new SoundFile<>(hs);
        AyuSynk.getBleInstance().generateDiagnosisReport(new SoundFile<>(hs));
        AyuSynk.getBleInstance().showLogs(true);*/

        AyuSynk.getBleInstance().setDiagnosisReportUpdateListener(new DiagnosisReportUpdateListener() {
            @Override
            public void reportRequestAdded(SoundFile soundFile) {
                Log.d("AI_FLOW", "✅ QUEUED");
            }
            @Override
            public void reportGenerated(SoundFile soundFile) {
                Log.d("AI_FLOW", "🔥 SUCCESS");
                // Process AI output
                parseAIResponse(soundFile);
            }
            @Override
            public void onReportGenerationError(String error) {
                Log.e("AI_FLOW", "❌ ERROR: " + error);
            }
        });
        AyuSynk.getBleInstance().setLogsListener(new AyuLogsListener() {
            @Override
            public void logs(String s) {
                System.out.println("AyusynkLogs" +  s);

            }
        });

    }
    private static String cleanUrl(String url) {

        if (url == null) return null;

        // remove duplicated URL
        int index = url.indexOf("https://", 10);
        if (index != -1) {
            url = url.substring(0, index);
            Log.d("AI_DEBUG", "URL LENGTH: " + url.length());
            Log.d("AI_DEBUG", "URL VALID: " + url.startsWith("https://"));
        }

        return url.trim();
    }
    // =========================
    // STEP 4: Parse AI Result
    // =========================
    private static void parseAIResponse(SoundFile<HeartSoundData> soundFile) {

        if (soundFile == null) {
            Log.e("AI", "SoundFile NULL");
            return;
        }

        List<HeartSoundData> list = soundFile.getSoundData();

        if (list == null || list.isEmpty()) {
            Log.e("AI", "Empty list");
            return;
        }

        for (HeartSoundData d : list) {

            String url = d.getFileLink();

            Log.d("AI", "Location: " + d.getLocationName());
            Log.d("AI", "URL: " + url);

            if (url != null) {
                uploadAIResult(trackerId, url);
            }
        }
    }

    // =========================
    // STEP 5: Upload AI Output
    // =========================
    private static void uploadAIResult(String trackerId, String fileUrl) {

        if (trackerId == null || fileUrl == null) {
            Log.e("UPLOAD_AI", "Invalid data");
            return;
        }

        String base64 = convertUrlToBase64(fileUrl);

        if (base64 == null) {
            Log.e("UPLOAD_AI", "Base64 failed");
            return;
        }

        RequestBody tracker =
                RequestBody.create(MediaType.parse("text/plain"), trackerId);

        RequestBody audio =
                RequestBody.create(MediaType.parse("text/plain"), base64);

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);

        api.uploadOutput(tracker, audio).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("UPLOAD_AI", "✅ SUCCESS");
                    if (uploadListener != null) {
                        uploadListener.onAIResultSuccess(trackerId, "Diagnosis ready");
                    }
                } else {
                    Log.e("UPLOAD_AI", "Upload failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UPLOAD_AI", "Fail: " + t.getMessage());
            }
        });
    }

    // =========================
    // UTILS
    // =========================
    private static String convertUrlToBase64(String urlStr) {

        try {
            URL url = new URL(urlStr);
            InputStream is = url.openStream();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];

            int n;
            while ((n = is.read(data)) != -1) {
                buffer.write(data, 0, n);
            }

            return Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static LocationType.Heart mapPosition(String pos) {

        if (pos == null) return LocationType.Heart.mitral;

        switch (pos.toLowerCase()) {
            case "aortic":
                return LocationType.Heart.aortic;
            case "pulmonic":
                return LocationType.Heart.pulmonic;
            case "tricuspid":
                return LocationType.Heart.tricuspid;
            default:
                return LocationType.Heart.mitral;
        }
    }
}

