package io.intelehealth.client.activities.setup_activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.activities.home_activity.HomeActivity;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.activities.login_activity.OfflineLogin;
import io.intelehealth.client.api.retrofit.RestApi;
import io.intelehealth.client.api.retrofit.ServiceGenerator;
import io.intelehealth.client.models.Results;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.utilities.HelperMethods;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * One time setup which requires OpenMRS server URL and user permissions
 */
public class SetupActivity extends AppCompatActivity {

    private final String LOG_TAG = "SetupActivity";

    private TestSetup mAuthTask = null;

    ProgressBar progressBar;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    protected AccountManager manager;
    private EditText mUrlField;
    private EditText mPrefixField;

    private Button mLoginButton;

    private Spinner mDropdownLocation;

    private List<Location> mLocations = new ArrayList<>();


    private static final int PERMISSION_ALL = 1;
    public File base_dir;
    public String[] FILES;

    AlertDialog.Builder dialog;
    String key = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDropdownLocation = (Spinner) findViewById(R.id.spinner_location);

        // Persistent login information
        manager = AccountManager.get(SetupActivity.this);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code

        mLoginButton = (Button) findViewById(R.id.setup_submit_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.setup_submit_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "button pressed");
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mUrlField = (EditText) findViewById(R.id.editText_URL);
        mPrefixField = (EditText) findViewById(R.id.editText_prefix);

        progressBar = (ProgressBar) findViewById(R.id.progressBar_setup);
        Button submitButton = (Button) findViewById(R.id.setup_submit_button);

        progressBar.setVisibility(View.GONE);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                //progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);

            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.generic_warning);
        alertDialogBuilder.setMessage(R.string.setup_internet);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        String[] PERMISSIONS = {Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCOUNT_MANAGER
        };

        if (!hasPermissions(this, PERMISSIONS))

        {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        mUrlField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Toast.makeText(SetupActivity.this, "Working", Toast.LENGTH_LONG).show();
                    // code to execute when EditText loses focus
                    if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                        String BASE_URL = "http://" + mUrlField.getText().toString() + "/openmrs/ws/rest/v1/";
                        getLocationFromServer(BASE_URL);

                    }
                }
            }
        });


       /* mDropdownLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_TAG,"position :"+position);
                if(mLocations!=null)
                Log.i(LOG_TAG,mLocations.get(position).getName());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/

        //INITIALIZE PARSE CONFIGS
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(HelperMethods.MIND_MAP_APP_ID)
                .server(HelperMethods.MIND_MAP_SERVER_URL)
                .build()
        );

    }

    //DOWNLOAD ALL MIND MAPS
    private void downloadMindMaps() {
        base_dir = new File(getFilesDir().getAbsolutePath(), HelperMethods.JSON_FOLDER);
        if (!base_dir.exists())
            base_dir.mkdirs();
        for (String file : FILES) {
            String[] parts = file.split(".json");
            //Log.i("DOWNLOADING-->",parts[0].replaceAll("\\s+",""));
            new getJSONFile().execute(file, parts[0].replaceAll("\\s+", ""));
        }
    }

    private void downloadFilesInfo() {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("AllFiles");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject obj : objects) {
                        ParseFile fileObject = (ParseFile) obj.get("FILES");
                        fileObject.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    String tmp = new String(data);
                                    String files[] = tmp.split("\n");
                                    Log.i("FLEN", "" + files.length);
                                    FILES = new String[files.length];
                                    FILES = files;
                                    downloadMindMaps();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(SetupActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check username and password validations.
     * Get user selected location.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;

        }
        Location location = null;
        if (mDropdownLocation.getSelectedItemPosition() <= 0) {
            cancel = true;
            Toast.makeText(SetupActivity.this, "Please select a value form the dropdown", Toast.LENGTH_LONG);
        } else {
            location = mLocations.get(mDropdownLocation.getSelectedItemPosition() - 1);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (location != null) {
                Log.i(LOG_TAG, location.getDisplay());
                String urlString = mUrlField.getText().toString();
                String prefixString = mPrefixField.getText().toString();
                mAuthTask = new TestSetup(urlString, prefixString, email, password, location);
                mAuthTask.execute();
                Log.d(LOG_TAG, "attempting setup");
            }
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Attempts login to the OpenMRS server.
     * If successful cretes a new {@link Account}
     * If unsuccessful details are saved in SharedPreferences.
     */
    private class TestSetup extends AsyncTask<Void, Void, Integer> {

        private final String USERNAME;
        private final String PASSWORD;
        private final String CLEAN_URL;
        private final String PREFIX;
        private String BASE_URL;
        private Location LOCATION;


        TestSetup(String url, String prefix, String username, String password, Location location) {
            CLEAN_URL = url;
            PREFIX = prefix;
            USERNAME = username;
            PASSWORD = password;
            LOCATION = location;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Integer doInBackground(Void... params) {
            BufferedReader reader;
            String JSONString;

            WebResponse loginAttempt = new WebResponse();

            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Log.d(LOG_TAG, "UN: " + USERNAME);
                Log.d(LOG_TAG, "PW: " + PASSWORD);

                String urlModifier = "session";


                BASE_URL = "http://" + CLEAN_URL + "/openmrs/ws/rest/v1/";
                String urlString = BASE_URL + urlModifier;

                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);

                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                Log.d(LOG_TAG, "GET URL: " + url);
                Log.i(LOG_TAG, connection.getRequestProperties().toString());

                int responseCode = connection.getResponseCode();
                loginAttempt.setResponseCode(responseCode);

                Log.d(LOG_TAG, "GET URL: " + url);
                Log.d(LOG_TAG, "Response Code from Server: " + connection.getResponseCode());

                // Read the input stream into a String
                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Do Nothing.
                    return 201;
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
                    return 201;
                }

                JSONString = buffer.toString();

                Log.d(LOG_TAG, "JSON Response: " + JSONString);
                loginAttempt.setResponseString(JSONString);
                if (loginAttempt != null && loginAttempt.getResponseCode() != 200) {
                    Log.d(LOG_TAG, "Login request was unsuccessful");
                    return loginAttempt.getResponseCode();
                } else if (loginAttempt == null) {
                    return 201;
                } else {
                    JsonObject responseObject = new JsonParser().parse(loginAttempt.getResponseString()).getAsJsonObject();
                    if (responseObject.get("authenticated").getAsBoolean()) {

                        JsonObject userObject = responseObject.get("user").getAsJsonObject();
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("sessionid", responseObject.get("sessionId").getAsString());
                        editor.putString("creatorid", userObject.get("uuid").getAsString());
                        editor.commit();
                        return 1;
                    } else {
                        return 3;
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return 201;
            } catch (IOException e) {
                e.printStackTrace();
                return 201;
            }
        }

        @Override
        protected void onPostExecute(Integer success) {
            mAuthTask = null;
//            showProgress(false);

            if (success == 1) {
                final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                manager.addAccountExplicitly(account, PASSWORD, null);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString(SettingsActivity.KEY_PREF_LOCATION_NAME, LOCATION.getDisplay());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_UUID, LOCATION.getUuid());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_DESCRIPTION, LOCATION.getDescription());

                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL, BASE_URL);
                Log.d(LOG_TAG, BASE_URL);
                editor.apply();

                editor.putString(SettingsActivity.KEY_PREF_ID_PREFIX, PREFIX);
                Log.d(LOG_TAG, PREFIX);
                editor.apply();

                editor.putBoolean(SettingsActivity.KEY_PREF_SETUP_COMPLETE, true);
                editor.apply();

                OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);

                Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            } else if (success == 201) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else if (success == 3) {
                mUrlField.setError("Check your URL.");
                mUrlField.requestFocus();
            } else {
                mPrefixField.setError("Select a different prefix!");
                mPrefixField.requestFocus();
            }
        }
    }

    /**
     * Parse locations fetched through api and provide the appropriate dropdown.
     *
     * @param url string of url.
     */
    private void getLocationFromServer(String url) {
        ServiceGenerator.changeApiBaseUrl(url);
        RestApi apiService =
                ServiceGenerator.createService(RestApi.class);
        Call<Results<Location>> call = apiService.getLocations(null);
        call.enqueue(new Callback<Results<Location>>() {
            @Override
            public void onResponse(Call<Results<Location>> call, Response<Results<Location>> response) {
                if (response.code() == 200) {
                    Results<Location> locationList = response.body();
                    mLocations = locationList.getResults();
                    List<String> items = getLocationStringList(locationList.getResults());
                    LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, items);
                    mDropdownLocation.setAdapter(adapter);
                }

            }

            @Override
            public void onFailure(Call<Results<Location>> call, Throwable t) {
                Toast.makeText(SetupActivity.this, "Unable to fetch locations", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Returns list of locations.
     *
     * @param locationList a list of type {@link Location}.
     * @return list of type string.
     * @see Location
     */
    private List<String> getLocationStringList(List<Location> locationList) {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.login_location_select));
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }

    public void onRadioClick(View v) {
        RadioButton r1 = (RadioButton) findViewById(R.id.demoMindmap);
        RadioButton r2 = (RadioButton) findViewById(R.id.downloadMindmap);

        boolean checked = ((RadioButton) v).isChecked();
        switch (v.getId()) {
            case R.id.demoMindmap:
                if (checked) {
                    r2.setChecked(false);
                }
                break;

            case R.id.downloadMindmap:
                if (checked) {
                    r1.setChecked(false);

                    dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle("Enter License Key")
                            .setView(promptsView)

                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Dialog d = (Dialog) dialog;

                                    EditText text = (EditText) d.findViewById(R.id.licensekey);
                                    key = text.getText().toString();
                                    //Toast.makeText(SetupActivity.this, "" + key, Toast.LENGTH_SHORT).show();
                                    if (keyVerified(key)) {
                                        // create a shared pref to store the key
                                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        // SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);

                                        //DOWNLOAD MIND MAP FILE LIST
                                        downloadFilesInfo();

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("licensekey", key);
                                        editor.commit();
                                    }
                                }
                            })

                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.create().show();


                }
                break;
        }
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }


    /**
     * Gets json Files from Parse Server
     */
    private class getJSONFile extends AsyncTask<String, Void, String> {

        String FILENAME, COLLECTION_NAME;

        @Override
        protected String doInBackground(String... params) {

            //SPACE SEPARATED NAMES ARE MADE UNDERSCORE SEPARATED
            FILENAME = params[0];
            COLLECTION_NAME = params[1];

            try {
                String servStr = HelperMethods.MIND_MAP_SERVER_URL + "classes/" + COLLECTION_NAME;
                URL url = new URL(servStr);
                Log.i("Connect", HelperMethods.MIND_MAP_SERVER_URL + "classes/" + COLLECTION_NAME);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("X-Parse-Application-Id", "app");
                urlConnection.setRequestProperty("X-Parse-REST-API-Key", "undefined");
                Log.i("RES->", "" + urlConnection.getResponseMessage());
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Toast.makeText(SetupActivity.this, "Error Downloading Mind Maps", Toast.LENGTH_SHORT).show();
                return;
            }
            String writable = "";
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                JSONObject finalresponse = jsonArray.getJSONObject(0);
                writable = finalresponse.getJSONObject("Main").toString();
                Log.i("INFO", writable);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //WRITE FILE in base_dir
            try {
                File mydir = new File(base_dir.getAbsolutePath(), FILENAME);
                if (!mydir.exists())
                    mydir.getParentFile().mkdirs();
                Log.i("FNAM", FILENAME);
                FileOutputStream fileout = new FileOutputStream(mydir);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                outputWriter.write(writable);
                outputWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //HelperMethods.readFile(FILENAME,SetupActivity.this);
        }
    }
}