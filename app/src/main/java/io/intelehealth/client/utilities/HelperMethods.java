package io.intelehealth.client.utilities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.objects.WebResponse;

/**
 * Created by tusharjois on 3/22/16.
 */
public class HelperMethods {

    public static final String LOG_TAG = "Helper Methods";

    //public static final String MIND_MAP_SERVER_URL = "http://139.59.73.230:1337/parse/";
    public static final String MIND_MAP_SERVER_URL = "http://165.227.97.214:1337/parse/";
    public static final String MIND_MAP_APP_ID = "app";
    public static final String IMAGE_APP_ID = "app2";
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static File base_dir;

    public static int getAge(String s) {
        if (s == null) return 0;

        String[] components = s.split("\\-");
        int year = Integer.parseInt(components[0]);
        String month = (components[1]);
        int day = Integer.parseInt(components[2]);

        Log.d("Hi_ap_month",month);

        int monthInt = 0;
        switch (month){
            case  "January" :  monthInt =1;break;
            case  "February" :  monthInt =2;break;
            case  "March" :  monthInt =3;break;
            case  "April" :  monthInt =4;break;
            case  "May" :  monthInt =5;break;
            case  "June" :  monthInt =6;break;
            case  "July" :  monthInt =7;break;
            case  "August" :  monthInt =8;break;
            case  "September" :  monthInt =9;break;
            case  "October" :  monthInt =10;break;
            case  "November" :  monthInt =11;break;
            case  "December" :  monthInt =12;break;
        }
        Log.d("To check Month",monthInt+"");
        LocalDate birthdate = new LocalDate(year, monthInt, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        return period.getYears();
    }

    public static String SimpleDatetoLongFollowupDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy");
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return formattedDate;
    }

    public static String SimpleDatetoLongDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy");
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return formattedDate;
    }

/*
    public static int getMonth(String s1) {
        if (s1 == null) return 0;

        String[] components = s1.split("\\-");

        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

        LocalDate birthdate1 = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate1, now, PeriodType.yearMonthDay());
        return period.getMonths();
    }
*/
    /**
     * Turns the mind map into a JSON Object that can be manipulated.
     *
     * @param context  The current context.
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

    public static String[] dispatchTakePictureIntent(int requestType, Activity activity) {
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
            Toast.makeText(activity, activity.getString(R.string.error_no_camera), Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "No camera activity to handle image capture");
        }

        return results;
    }

    /**
     * Perform GET request to the server.
     *
     * @param urlModifier modification in the url
     * @param dataString  data content
     * @param context     context of the activity
     * @return {@link WebResponse}
     */
    public static WebResponse getCommand(String urlModifier, String dataString, Context context) {


        AccountManager manager;
        BufferedReader reader;
        String JSONString;

        WebResponse webResponse = new WebResponse();

        try {

            //TODO: grab the URL and the UN and PW from the sharedprefs, and the account


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            final String BASE_URL = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL_REST, "");
            final String session_id = sharedPref.getString("sessionid", null);

            String USERNAME = null;
            String PASSWORD = null;

            manager = AccountManager.get(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
            if (accountList.length == 1) {
                Account authAccount = accountList[0];
                USERNAME = authAccount.name;
                PASSWORD = manager.getPassword(authAccount);
            } else {
                return null;
            }


            String urlString = BASE_URL + urlModifier + dataString;

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            if (session_id != null) {
                connection.setRequestProperty("Cookie", "jsessionid=" + session_id);
            } else {
                connection.setRequestProperty("Authorization", "Basic " + encoded);
            }
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

    public static WebResponse getCommand(String urlModifier, String dataString, Context context,String Username, String Password) {


        BufferedReader reader;
        String JSONString;

        WebResponse webResponse = new WebResponse();

        try {

            //TODO: grab the URL and the UN and PW from the sharedprefs, and the account


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            final String session_id = sharedPref.getString("sessionid", null);

            String USERNAME = Username;
            String PASSWORD = Password;



            String urlString = urlModifier + dataString;

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            if (session_id != null) {
                connection.setRequestProperty("Cookie", "jsessionid=" + session_id);
            } else {
                connection.setRequestProperty("Authorization", "Basic " + encoded);
            }
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

    /**
     * Perform POST request to the server.
     *
     * @param urlModifier modification in url
     * @param dataString  data content
     * @param context     context of the activity
     * @return {@link WebResponse}
     */
    public static WebResponse postCommand(String urlModifier, String dataString, Context context) {
        BufferedReader reader;
        String JSONString;
        AccountManager manager;
        WebResponse webResponse = new WebResponse();

        //TODO: grab the URL and the UN and PW from the sharedprefs, and the account

        try {

            String USERNAME = null;
            String PASSWORD = null;

            manager = AccountManager.get(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
            if (accountList.length == 1) {
                Account authAccount = accountList[0];
                USERNAME = authAccount.name;
                PASSWORD = manager.getPassword(authAccount);
            } else {
                return null;
            }


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String BASE_URL = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL_REST, "");
            final String session_id = sharedPref.getString("sessionid", null);


            String urlString = BASE_URL + urlModifier;
            Log.d(LOG_TAG, "URL POSTED TO: " + urlString);
            URL url = new URL(urlString);

            byte[] outputInBytes = dataString.getBytes("UTF-8");
            int content = dataString.getBytes("UTF-8").length;



            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);

            connection.setRequestProperty("Authorization", "Basic " + encoded);
            Log.d(LOG_TAG, USERNAME + "-" + PASSWORD);
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
                webResponse.setResponseObject(JSONString);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return webResponse;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String readFile(String FILENAME, Context context) {
        Log.i(LOG_TAG, "Reading from file");

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + JSON_FOLDER + File.separator + FILENAME);
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
            e.printStackTrace();
            return null;
        }

    }

    public static String readFileRoot(String FILENAME, Context context) {
        Log.i(LOG_TAG, "Reading from file");

        try {
            File myDir = new File(context.getFilesDir().getAbsolutePath() + File.separator +  File.separator + FILENAME);
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
            e.printStackTrace();
            return null;
        }

    }

}
