package app.intelehealth.client.services;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import app.intelehealth.client.utilities.SessionManager;
import app.intelehealth.client.R;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;

/**
 * Created by Dexter Barretto on 8/7/17.
 * Github : @dbarretto
 */

public class DownloadProtocolsTask extends AsyncTask<String, String, String> {
    private static final String TAG = DownloadProtocolsTask.class.getSimpleName();
    SessionManager sessionManager = null;
    WeakReference<Activity> mWeakActivity;
    Activity activity;
    private ProgressDialog mProgress;
    private String parse_app_id = "ih_mm_server";

    public DownloadProtocolsTask(Activity activity) {
        this.mWeakActivity = new WeakReference<>(activity);
        this.activity = activity;
        sessionManager = new SessionManager(activity);
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
            Log.i(TAG, "copyFile: DoesNotExists");
        }

        FileChannel origin = null;
        FileChannel destination = null;
        try {
            origin = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();

            long count = 0;
            long size = origin.size();
            while ((count += destination.transferFrom(origin, count, size - count)) < size) ;
        } finally {
            if (origin != null) {
                origin.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if ((mWeakActivity.get() != null && !mWeakActivity.get().isFinishing())
                && mProgress == null) {
            mProgress = new ProgressDialog(activity);
        }
        if ((mWeakActivity.get() != null && !mWeakActivity.get().isFinishing())
                && mProgress != null && !mProgress.isShowing()) {
            mProgress.setTitle(activity.getString(R.string.please_wait_progress));
            mProgress.setMessage("Connecting to Server");
            mProgress.setCanceledOnTouchOutside(false);
            try {
                mProgress.show();
            } catch (Exception ex) {
                Log.e(TAG, "onPreExecute: ", ex);
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {

        List<Mindmap> mmList = null;
        String licenseKey = params[0];

        mmList = downloadMindMapList(licenseKey);
        if (mmList != null && !mmList.isEmpty()) {
            for (Mindmap mm : mmList) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                String content = downloadMindMap(licenseKey, mm);
                mm.setContent(content);
                if (mm.getName() != null && mm.getContent() != null &&
                        !mm.getName().isEmpty() && !mm.getContent().isEmpty()) {
                    saveMindmap(mm.getName(), mm.getContent());
                }
            }

            publishProgress("progress", "Saving files");

            File engines_dir = new File(activity.getFilesDir().getAbsolutePath(), AppConstants.JSON_FOLDER);
            if (engines_dir.exists()) deleteFolder(engines_dir);
            File physicalExam = new File(activity.getFilesDir().getAbsolutePath() + "/physExam.json");
            Log.i(TAG, "onPostExecute: " + physicalExam.exists());
            File familyHistory = new File(activity.getFilesDir().getAbsolutePath() + "/famHist.json");
            Log.i(TAG, "onPostExecute: " + familyHistory);
            File pastMedicalHistory = new File(activity.getFilesDir().getAbsolutePath() + "/patHist.json");
            Log.i(TAG, "onPostExecute: " + pastMedicalHistory);
            File config = new File(activity.getFilesDir().getAbsolutePath() + "/config.json");
            Log.i(TAG, "onPostExecute: " + config);
            File base_dir = new File(activity.getFilesDir().getAbsolutePath(), AppConstants.JSON_FOLDER_Update);
            base_dir.renameTo(engines_dir);
            File physExam = new File(engines_dir, "physExam.json");
            if (physExam.exists()) {
                Log.i(TAG, "onPostExecute: physExam");
                physicalExam.delete();
                try {
                    copyFile(physExam, physicalExam);
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                physExam.delete();
            }

            File famHist = new File(engines_dir, "famHist.json");
            if (famHist.exists()) {
                Log.i(TAG, "onPostExecute: famHist");
                familyHistory.delete();
                try {
                    copyFile(famHist, familyHistory);
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                famHist.delete();
            }

            File patHist = new File(engines_dir, "patHist.json");
            if (patHist.exists()) {
                Log.i(TAG, "onPostExecute: patHist");
                pastMedicalHistory.delete();
                try {
                    copyFile(patHist, pastMedicalHistory);
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                patHist.delete();
            }

            File configfile = new File(engines_dir, "config.json");
            if (configfile.exists()) {
                Log.i(TAG, "onPostExecute: configfile");
                config.delete();
                try {
                    copyFile(configfile, config);
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                configfile.delete();
            }


            return licenseKey;
        }

        return null;

    }

    @Override
    protected void onProgressUpdate(String... values) {
        String type = values[0];
        if (type.equals("progress")) {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.setMessage(values[1]);
            }
        }
        if (type.equals("toast")) {
            Toast.makeText(IntelehealthApplication.getAppContext(), values[1], Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (response != null && !response.isEmpty()) {
            sessionManager.setLicenseKey(response);
        } else {
            File engines_dir = new File(activity.getFilesDir().getAbsolutePath(), AppConstants.JSON_FOLDER);
            if (engines_dir.exists()) deleteFolder(engines_dir);
            if (sessionManager.getLicenseKey() != null)
                sessionManager.deleteLicensekey();
            Toast.makeText(IntelehealthApplication.getAppContext(), "Error downloading protocols", Toast.LENGTH_LONG).show();

        }
        if (mProgress != null && mProgress.isShowing()) {
            try {
                mProgress.dismiss();
            } catch (Exception ex) {
                Log.e(TAG, "onPreExecute: ", ex);
            }
        }
    }

    private List<Mindmap> downloadMindMapList(String licensekey) {
        List<Mindmap> mmList = new ArrayList<>();
        String mmListRequest =
                String.format("{\"licensekey\":\"%s\"}", licensekey);
        Log.i(TAG, "licensekey: " + mmListRequest);
        HttpURLConnection urlConnection = null;
        try {
            publishProgress("progress", "Downloading Mindmap List");
            //Download List of Protocols Available
            String servStr = "https://" + sessionManager.getMindMapServerUrl() + "/parse/functions/downloadMindMapList";
            URL url = new URL(servStr);
            Log.i("GetMMList", servStr);
            byte[] mmListRequestBytes = mmListRequest.getBytes(StandardCharsets.UTF_8);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("X-Parse-Application-Id", parse_app_id);
            urlConnection.setRequestProperty("X-Parse-REST-API-Key", "undefined");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            DataOutputStream dStream = new DataOutputStream(urlConnection.getOutputStream());
            dStream.write(mmListRequestBytes);
            dStream.flush();
            dStream.close();
            int responseCode = urlConnection.getResponseCode();
            Log.i("RES->", "" + urlConnection.getResponseMessage());
            if (responseCode == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                String mindmap_list_str = stringBuilder.toString();
                if (mindmap_list_str != null && !mindmap_list_str.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(mindmap_list_str);

                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject mm_details = jsonArray.getJSONObject(i);
                        mmList.add(new Mindmap(mm_details.get("name").toString(), mm_details.get("objectId").toString()));
                    }
                    return mmList;
                }
            } else if (responseCode == 400) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                String error = stringBuilder.toString();
                if (error != null && !error.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(error);
                    publishProgress("toast", jsonObject.get("error").toString());
                }
            } else {

                Toast.makeText(IntelehealthApplication.getAppContext(), "Error Downloadind Mindmap List", Toast.LENGTH_LONG).show();
            }


        } catch (JSONException e) {
            Log.e(TAG, "onPostExecute: ", e);
            // deleteFolder(base_dir);
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            // deleteFolder(base_dir);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return null;
    }

    private String downloadMindMap(String licensekey, Mindmap mindmap) {
        String mmListRequest =
                String.format("{\"licensekey\":\"%s\"," +
                                "\"objectid\":\"%s\"}",
                        licensekey,
                        mindmap.getObjectId());
        Log.i(TAG, "licensekey: " + mmListRequest);
        HttpURLConnection urlConnection = null;
        try {
            publishProgress("progress", "Downloading Mindmap " + mindmap.name);
            //Download List of Protocols Available
            String servStr = "https://" + sessionManager.getMindMapServerUrl() + "/parse/functions/downloadMindMap";
            URL url = new URL(servStr);
            Log.i("GetMM", servStr);
            byte[] mmListRequestBytes = mmListRequest.getBytes(StandardCharsets.UTF_8);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("X-Parse-Application-Id", parse_app_id);
            urlConnection.setRequestProperty("X-Parse-REST-API-Key", "undefined");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            DataOutputStream dStream = new DataOutputStream(urlConnection.getOutputStream());
            dStream.write(mmListRequestBytes);
            dStream.flush();
            dStream.close();
            int responseCode = urlConnection.getResponseCode();
            Log.i("RES->", "" + urlConnection.getResponseMessage());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            String mindmap_str = stringBuilder.toString();
            if (mindmap_str != null && !mindmap_str.isEmpty()) {
                JSONObject jsonObject = new JSONObject(mindmap_str);
                if (responseCode == 200) {
                    JSONObject mindmap_json = jsonObject.getJSONObject("result").getJSONObject("content");
                    return mindmap_json.toString();
                } else {
                    if (responseCode == 400) {
                        Toast.makeText(IntelehealthApplication.getAppContext(), jsonObject.get("error").toString(), Toast.LENGTH_LONG).show();
                    }
                }

            } else {
                if ((mWeakActivity.get() != null && !mWeakActivity.get().isFinishing())) {
                    Toast.makeText(IntelehealthApplication.getAppContext(), activity.getString(R.string.error_downloading_protocols),
                            Toast.LENGTH_LONG).show();
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "onPostExecute: ", e);
            // deleteFolder(base_dir);
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            publishProgress("Error downloading" + mindmap.getName());
            return downloadMindMap(licensekey, mindmap);
            // deleteFolder(base_dir);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return null;
    }

    private void saveMindmap(String name, String content) {
        String file_name = name;
        if (!name.isEmpty() && name.length() > 7) {
            switch (name.substring(0, 6)) {
                case "physEx": {
                    file_name = "physExam.json";
                    break;
                }
                case "famHis": {
                    file_name = "famHist.json";
                    break;
                }
                case "patHis": {
                    file_name = "patHist.json";
                    break;
                }
                case "config": {
                    file_name = "config.json";
                    break;
                }
                default:
                    file_name = name;
            }
        }
        File base_dir = new File(activity.getFilesDir().getAbsolutePath(), AppConstants.JSON_FOLDER_Update);
        if (!base_dir.exists()) base_dir.mkdirs();
        try {
            File file = new File(base_dir.getAbsolutePath(), file_name);
            if (file.exists()) file.delete();
            Log.i(TAG, "FNAM : " + file_name);
            FileOutputStream fileout = new FileOutputStream(file);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(content);
            outputWriter.close();
        } catch (Exception e) {
            deleteFolder(base_dir);
            Log.e(TAG, "onPostExecute: ", e);
        }

    }

    private class Mindmap {
        private String name;
        private String objectId;
        private String content;

        public Mindmap(String name, String objectId) {
            this.name = name;
            this.objectId = objectId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}



