package io.intelehealth.client;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import io.intelehealth.client.objects.WebResponse;

/**
 * Created by tusharjois on 3/22/16.
 */
public class HelperMethods {

    public static final String LOG_TAG = "Helper Methods";

    public static int getAge(String s) {
        if (s == null) return 0;

        String[] components = s.split("\\-");

        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        return period.getYears();
    }

    /**
     * Turns the mind map into a JSON Object that can be manipulated.
     * @param context The current context.
     * @param fileName The name of the JSON file to use.
     * @return fileName converted into the proper JSON Object to use
     */
    public static JSONObject encodeJSON(Context context, String fileName) {
        String raw_json = null;
        JSONObject encoded = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            raw_json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            encoded = new JSONObject(raw_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return encoded;

    }

    // Camera as an Input Type
    // All Activities that use this code need to implement onActivityResult and onRequestPermissionsResult
    // See IdentificationActivity for implementation details
    public static final int REQUEST_CAMERA = 0; // To identify a camera permissions request
    public static final int REQUEST_READ_EXTERNAL = 1;


    static File createImageFile(String uuidString) throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                uuidString,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        //TODO: upload this to google drive using a service, and then store the public share link into android


        return image;
    }

    static String[] startImageCapture(Context context, Activity activity) {

        String[] results = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL);
                } else if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.CAMERA}, REQUEST_CAMERA);
                } else {
                    results = dispatchTakePictureIntent(REQUEST_CAMERA, activity);
                }
            } else {
                results = dispatchTakePictureIntent(REQUEST_CAMERA, activity);
            }
        } else {
            results = dispatchTakePictureIntent(REQUEST_CAMERA, activity);
        }

        return results;
    }

    static String[] dispatchTakePictureIntent(int requestType, Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String[] results = null;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;

            try {
                String uuidString = UUID.randomUUID().toString();
                photoFile = HelperMethods.createImageFile(uuidString);

                // Save a file: path for use with ACTION_VIEW intents
                String imagePath = photoFile.getAbsolutePath();

                results = new String[2];
                results[0] = uuidString;
                results[1] = imagePath;

            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(LOG_TAG, ex.getMessage());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                activity.startActivityForResult(takePictureIntent, requestType);
            }

        } else {
            Toast.makeText(activity, "No camera installed.", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "No camera activity to handle image capture");
        }

        return results;
    }

    static WebResponse getCommand(String urlModifier, String dataString) {
        BufferedReader reader;
        String JSONString;

        WebResponse webResponse = new WebResponse();

        try {

            //TODO: grab the URL and the UN and PW from the sharedprefs, and the account

            final String USERNAME = "Admin";
            final String PASSWORD = "CBIDtiger123";
            final String BASE_URL = "http://openmrs.amal.io:8080/openmrs/ws/rest/v1/";

            String urlString = BASE_URL + urlModifier + dataString;

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

            int responseCode = connection.getResponseCode();
            webResponse.setResponseCode(responseCode);

            Log.d(LOG_TAG, "GET URL: " + url);
            Log.d(LOG_TAG, "Response Code from Server: " + String.valueOf(responseCode));

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Do Nothing.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            JSONString = buffer.toString();

            Log.d(LOG_TAG, "JSON Response: " + JSONString);
            webResponse.setResponseString(JSONString);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return webResponse;
    }

    static WebResponse postCommand(String urlModifier, String dataString) {
        BufferedReader reader;
        String JSONString;

        WebResponse webResponse = new WebResponse();

        //TODO: grab the URL and the UN and PW from the sharedprefs, and the account

        try {
            final String USERNAME = "Admin";
            final String PASSWORD = "CBIDtiger123";
            final String BASE_URL = "http://openmrs.amal.io:8080/openmrs/ws/rest/v1/";

            String urlString = BASE_URL + urlModifier;

            URL url = new URL(urlString);

            byte[] outputInBytes = dataString.getBytes("UTF-8");


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.write(outputInBytes);
            dStream.flush();
            dStream.close();
            int responseCode = connection.getResponseCode();
            webResponse.setResponseCode(responseCode);


            Log.d(LOG_TAG, "POST URL: " + url);
            Log.d(LOG_TAG, "Response Code from Server: " + String.valueOf(responseCode));

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            JSONString = buffer.toString();

            Log.d(LOG_TAG, "JSON Response: " + JSONString);

            try {
                JSONObject JSONResponse = new JSONObject(JSONString);
                webResponse.setResponseString(JSONResponse.getString("uuid"));

            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return webResponse;
    }



}
