package io.intelehealth.client.views.activites;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

import io.intelehealth.client.R;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.databinding.ActivitySetupBinding;
import io.intelehealth.client.databinding.ContentSetupBinding;
import io.intelehealth.client.models.loginModel.LoginModel;
import io.intelehealth.client.utilities.Base64Methods;
import io.intelehealth.client.utilities.DialogUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.UrlModifiers;
import io.intelehealth.client.viewModels.SetupViewModel;
import io.intelehealth.client.views.adapters.LocationArrayAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.intelehealth.client.app.AppConstants.sessionManager;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    MyClickHandlers handlers = new MyClickHandlers(this);
    SetupViewModel setupViewModel;
    ActivitySetupBinding activitySetupBinding;
    ContentSetupBinding binding;
    private boolean isLocationFetched;
    private TestSetup mAuthTask = null;
    private List<Location> mLocations = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setup);
        activitySetupBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup);
        binding = DataBindingUtil.setContentView(this, R.layout.content_setup);
        setupViewModel = ViewModelProviders.of(this).get(SetupViewModel.class);
        /*set handlers with data binding*/
        activitySetupBinding.setHandlers(handlers);
        activitySetupBinding.setViewmodel(setupViewModel);
        activitySetupBinding.setLifecycleOwner(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        sessionManager=new SessionManager(getApplicationContext());
//        mDropdownLocation = (Spinner) findViewById(R.id.spinner_location);

        // Persistent login information
//        manager = AccountManager.get(SetupActivity.this);

        // Set up the login form.
//        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code

        binding.setupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });


        binding.adminPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.login || actionId == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        binding.textViewAid.setText("Android Id: " + IntelehealthApplication.getAndroidId());

        binding.setupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet), getString(R.string.generic_ok));

        binding.editTextURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationFetched = false;
                LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, new ArrayList<String>());
                binding.spinnerLocation.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!binding.editTextURL.getText().toString().trim().isEmpty() && binding.editTextURL.getText().toString().length() >= 12) {
                    if (Patterns.WEB_URL.matcher(binding.editTextURL.getText().toString()).matches()) {
                        String BASE_URL = "http://" + binding.editTextURL.getText().toString() + ":8080/openmrs/ws/rest/v1/";
                        if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
                            getLocationFromServer(BASE_URL);
                        else
                            Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


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
        binding.email.setError(null);
        binding.password.setError(null);
        binding.adminPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();
        String admin_password = binding.adminPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            binding.password.setError(getString(R.string.error_invalid_password));
            focusView = binding.password;
            cancel = true;
        }

        if (!TextUtils.isEmpty(admin_password) && !isPasswordValid(admin_password)) {
            binding.adminPassword.setError(getString(R.string.error_invalid_password));
            focusView = binding.adminPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            binding.email.setError(getString(R.string.error_field_required));
            focusView = binding.email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            binding.email.setError(getString(R.string.error_invalid_email));
            focusView = binding.email;

        }
        Location location = null;
        if (binding.spinnerLocation.getSelectedItemPosition() <= 0) {
            cancel = true;
            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_selected), Toast.LENGTH_LONG);
        } else {
            location = mLocations.get(binding.spinnerLocation.getSelectedItemPosition() - 1);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (location != null) {
                Log.i(TAG, location.getDisplay());
                String urlString = binding.editTextURL.getText().toString();
                mAuthTask = new TestSetup(urlString, email, password, admin_password, location);
                mAuthTask.execute();
                Log.d(TAG, "attempting setup");
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
        private final String ADMIN_PASSWORD;
        ProgressDialog progress;
        private String BASE_URL;
        private Location LOCATION;


        TestSetup(String url, String username, String password, String adminPassword, Location location) {
            CLEAN_URL = url;
            USERNAME = username;
            PASSWORD = password;
            LOCATION = location;
            ADMIN_PASSWORD = adminPassword;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(SetupActivity.this);
            progress.setTitle(getString(R.string.please_wait_progress));
            progress.setMessage(getString(R.string.logging_in));
            progress.show();
        }


        @Override
        protected Integer doInBackground(Void... params) {
            BufferedReader reader;
            String JSONString;

            WebResponse loginAttempt = new WebResponse();

            try {
//                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Log.d(TAG, "UN: " + USERNAME);
                Log.d(TAG, "PW: " + PASSWORD);


//
//                BASE_URL = "http://" + CLEAN_URL + ":8080/openmrs/ws/rest/v1/";
//                String urlString = BASE_URL + urlModifier;
                UrlModifiers urlModifiers = new UrlModifiers();
                String urlString = urlModifiers.loginUrl(BASE_URL, CLEAN_URL);

                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Base64Methods base64Methods = new Base64Methods();
                String encoded = base64Methods.encoded(USERNAME, PASSWORD);

                sessionManager.setEncoded(encoded);

                Call<LoginModel> call = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(encoded);
                call.enqueue(new Callback<LoginModel>() {
                    @Override
                    public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                        int responsCode = response.code();
                        Logger.logD(TAG, "success" + response.toString());
                        sessionManager.setChwname(response.body().getUser().getDisplay());
                        sessionManager.setCreatorID(response.body().getUser().getUuid());
                        sessionManager.setSessionID(response.body().getSessionId());
                        sessionManager.setProviderID(response.body().getUser().getPerson().getUuid());
                    }

                    @Override
                    public void onFailure(Call<LoginModel> call, Throwable t) {
                        Logger.logD(TAG, "Login Failure" + t.getMessage());

                    }
                });
//                connection.setRequestProperty("Authorization", "Basic " + encoded);
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
//                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();
                loginAttempt.setResponseCode(responseCode);

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

                Log.d(TAG, "JSON Response: " + JSONString);
                loginAttempt.setResponseString(JSONString);
                if (loginAttempt != null && loginAttempt.getResponseCode() != 200) {
                    Log.d(TAG, "Login request was unsuccessful");
                    return loginAttempt.getResponseCode();
                } else if (loginAttempt == null) {
                    return 201;
                } else {
                    JsonObject responseObject = new JsonParser().parse(loginAttempt.getResponseString()).getAsJsonObject();
                    if (responseObject.get("authenticated").getAsBoolean()) {

                        JsonObject userObject = responseObject.get("user").getAsJsonObject();
                        JsonObject personObject = userObject.get("person").getAsJsonObject();


                        String queryString = "?user=" + userObject.get("uuid").getAsString();
                        WebResponse responseProvider;

                        responseProvider = HelperMethods.getCommand(BASE_URL + "provider", queryString, SetupActivity.this, USERNAME, PASSWORD);

                        if (responseProvider != null && responseProvider.getResponseCode() == 200) {
                            String provider_uuid = "";

                            JSONArray resultsArray = null;

                            try {
                                JSONObject JSONResponse = new JSONObject(responseProvider.getResponseString());
                                resultsArray = JSONResponse.getJSONArray("results");

                                Log.i(TAG, "doInBackground: " + JSONResponse.toString());

                                if (resultsArray.length() != 0) {
                                    for (int i = 0; i < resultsArray.length(); i++) {
                                        JSONObject checking = resultsArray.getJSONObject(i);
                                        Log.i(TAG, "doInBackground: " + checking.getString("uuid"));
                                        provider_uuid = checking.getString("uuid");
                                        editor.putString("providerid", provider_uuid);
                                        editor.commit();
                                    }
                                    return 1;
                                }
                                return 201;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return 201;
                            }

                        }

                        return 201;


                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return 201;
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

                editor.putString(SettingsActivity.KEY_PREF_LOCATION_NAME, LOCATION.getDisplay());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_UUID, LOCATION.getUuid());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_DESCRIPTION, LOCATION.getDescription());

                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL_REST, BASE_URL);
                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL_BASE, "http://" + CLEAN_URL + ":8080/openmrs");
                sessionManager.setBaseUrl(BASE_URL);
                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL, CLEAN_URL);
                Log.d(TAG, BASE_URL);
                editor.apply();

                editor.putBoolean(SettingsActivity.KEY_PREF_SETUP_COMPLETE, true);
                editor.apply();

                OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                        .applicationId(HelperMethods.IMAGE_APP_ID)
                        .server("http://" + CLEAN_URL + ":1337/parse/")
                        .build()
                );
                Log.i(TAG, "onPostExecute: Parse init");
                Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                intent.putExtra("setup", true);
                if (r2.isChecked()) {
                    if (sharedPref.contains("licensekey")) {
                        startActivity(intent);
                        startJobDispatcherService(SetupActivity.this);
                        finish();
                    } else {
                        Toast.makeText(SetupActivity.this, "Please enter a valid license key", Toast.LENGTH_LONG).show();
                    }
                } else {
                    startActivity(intent);
                    finish();
                }


            } else if (success == 201) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else if (success == 3) {
                mUrlField.setError(getString(R.string.url_invalid));
                mUrlField.requestFocus();
            }

            progress.dismiss();
        }
    }

    private class MyClickHandlers extends SetupActivity {

        public MyClickHandlers(SetupActivity setupActivity) {
        }

    }
}
