package io.intelehealth.client;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.offline_login.OfflineLogin;
import io.intelehealth.client.retrofit.RestApi;
import io.intelehealth.client.retrofit.ServiceGenerator;
import io.intelehealth.client.retrofit.models.Results;
import io.intelehealth.client.retrofit.models.resource.Location;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
//                progressBar.setVisibility(View.VISIBLE);
//                progressBar.setProgress(0);

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
        if(mDropdownLocation.getSelectedItemPosition()< 0){
            cancel = true;
            Toast.makeText(SetupActivity.this,"Please select a value form the dropdown",Toast.LENGTH_LONG);
        }
        else{
            location =  mLocations.get(mDropdownLocation.getSelectedItemPosition()-1);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if(location!=null) {
                Log.i(LOG_TAG,location.getDisplay());
                String urlString = mUrlField.getText().toString();
                String prefixString = mPrefixField.getText().toString();
                mAuthTask = new TestSetup(urlString, prefixString, email, password,location);
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


    private class TestSetup extends AsyncTask<Void, Void, Integer> {

        private final String USERNAME;
        private final String PASSWORD;
        private final String CLEAN_URL;
        private final String PREFIX;
        private String BASE_URL;
        private Location LOCATION;


        TestSetup(String url, String prefix, String username, String password,Location location) {
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

                Log.d(LOG_TAG, "UN: " + USERNAME);
                Log.d(LOG_TAG, "PW: " + PASSWORD);

             //TODO: Hack Code... Change This...
                String urlModifier = "patient";
                String dataString = "?q=" + PREFIX;

                BASE_URL = "http://" + CLEAN_URL + "/openmrs/ws/rest/v1/";
                String urlString = BASE_URL + urlModifier + dataString;

                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();
                loginAttempt.setResponseCode(responseCode);

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
                loginAttempt.setResponseString(JSONString);
                if (loginAttempt != null && loginAttempt.getResponseCode() != 200) {
                    Log.d(LOG_TAG, "Login request was unsuccessful");
                    return loginAttempt.getResponseCode();
                }

                if (!loginAttempt.getResponseString().isEmpty()) {
                    try {
                        JSONObject responseObject = new JSONObject(loginAttempt.getResponseString());
                        JSONArray results = responseObject.getJSONArray("results");
                        if (results.length() == 0) {
                            return 1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return 201;
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return 3;
            } catch (IOException e) {
                e.printStackTrace();
                return 201;
            }

            return 201;
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

                editor.putString(SettingsActivity.KEY_PREF_LOCATION_NAME,LOCATION.getDisplay());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_UUID,LOCATION.getUuid());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_DESCRIPTION,LOCATION.getDescription());

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
                    mLocations =locationList.getResults();
                    List<String> items = getLocationStringList(locationList.getResults());
                    LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, items);
                    mDropdownLocation.setAdapter(adapter);
                }

            }

            @Override
            public void onFailure(Call<Results<Location>> call, Throwable t) {
                    Toast.makeText(SetupActivity.this,"Unable to fetch locations",Toast.LENGTH_LONG).show();
            }
        });
    }


    private List<String> getLocationStringList(List<Location> locationList) {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.login_location_select));
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }
}