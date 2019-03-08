
package io.intelehealth.client.activities.login_activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
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

import io.fabric.sdk.android.Fabric;
import io.intelehealth.client.R;
import io.intelehealth.client.activities.home_activity.HomeActivity;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.activities.setup_activity.SetupActivity;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.services.sync.JobDispatchService;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.SessionManager;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

   TextView txt_cant_login;

    private final String LOG_TAG = "LoginActivity";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    ProgressDialog progress;

    /**
     * A dummy authentication store containing known user names and passwords.
     */
    // TODO: remove after connecting to a real authentication system.
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "username:password", "admin:nimda"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    protected AccountManager manager;

    private OfflineLogin offlineLogin = null;

SessionManager sessionManager;

    public void cant_log()
    {
        final SpannableString span_string = new SpannableString(getApplicationContext().getText(R.string.email_link));
        Linkify.addLinks(span_string,Linkify.EMAIL_ADDRESSES);

        new AlertDialog.Builder(this)
                .setMessage(span_string)
                .setNegativeButton("Send Email", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                        Intent intent = new Intent(Intent.ACTION_SENDTO); //to get only the list of e-mail clients
                        intent.setType("text/plain");
                        intent.setData(Uri.parse("mailto:support@intelehealth.io"));
                        // intent.putExtra(Intent.EXTRA_EMAIL, "support@intelehealth.io");
                        // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        //  intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");

                        startActivity(Intent.createChooser(intent, "Send Email"));
                        //add email function here !
                    }

                })
                .setPositiveButton("Close", null)
                .show();

        //prajwal_changes
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.title_activity_login);

        offlineLogin = OfflineLogin.getOfflineLogin();


sessionManager=new SessionManager(getApplicationContext());
        txt_cant_login = (TextView) findViewById(R.id.cant_login_id);
        // pop_window_login = new PopupWindow(getApplicationContext());
        txt_cant_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                cant_log();
            }

        });

        // Persistent login information
        manager = AccountManager.get(LoginActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("login", true);
            startJobDispatcherService(LoginActivity.this);
            startActivity(intent);
            finish();
        }

        //Enforces Offline Login Check only if network not present
        if (!NetworkConnection.isOnline(this)) {
            if (OfflineLogin.getOfflineLogin().getOfflineLoginStatus()) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("login", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }


        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code

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

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "button pressed");
                attemptLogin();
            }
        });


      /*  // attempt to get device-Id for the phone
        TextView deviceIdView = (TextView) findViewById(R.id.textview_device_id);
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceId = getString(R.string.device_id) + deviceId;
        deviceIdView.setText(deviceId);
        */  //prajwal_changes
    }


    /**
     * Returns void.
     * This method checks if valid username and password are given as input.
     *
     * @return void
     */
    private void attemptLogin() {

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString();
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
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else if (NetworkConnection.isOnline(this)) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
            Log.d(LOG_TAG, "attempting login");
        } else {
            offlineLogin.login(email, password);
        }

    }


    /**
     * @param password Password
     * @return boolean
     */
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void showProgress(final boolean show) {
        if (progress == null) {
            progress = new ProgressDialog(LoginActivity.this);
            progress.setTitle(getString(R.string.please_wait_progress));
            progress.setMessage(getString(R.string.logging_in));
        }
        if(show) progress.show();
        else progress.dismiss();
    }

    /**
     * class UserLoginTask will authenticate user using email and password.
     * Depending on server's response, user may or may not have successful login.
     * This class also uses SharedPreferences to store session ID
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            BufferedReader reader;
            String JSONString;

            WebResponse loginAttempt = new WebResponse();

            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                final String BASE_URL = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL_REST, "");

                final String USERNAME = mEmail;
                final String PASSWORD = mPassword;
                Log.d(LOG_TAG, "UN: " + USERNAME);
                Log.d(LOG_TAG, "PW: " + PASSWORD);

                String urlModifier = "session";
                String urlString = BASE_URL + urlModifier;

                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
                sessionManager.setEncoded(encoded);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();
                loginAttempt.setResponseCode(responseCode);

                // Read the input stream into a String
                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Do Nothing.
                    return false;
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
                    Log.d(LOG_TAG, "Login get request was unsuccessful");
                    return false;
                } else if (loginAttempt == null) {
                    return false;
                } else {
                    JsonObject jsonObject = new JsonParser().parse(loginAttempt.getResponseString()).getAsJsonObject();
                    if (jsonObject.get("authenticated").getAsBoolean()) {

                        JsonObject userObject = jsonObject.get("user").getAsJsonObject();
                        JsonObject personObject = userObject.get("person").getAsJsonObject();

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("sessionid", jsonObject.get("sessionId").getAsString());
                        editor.putString("creatorid", userObject.get("uuid").getAsString());
                        editor.putString("providerid", personObject.get("uuid").getAsString());
                        editor.putString("chwname", personObject.get("display").getAsString());
                        editor.commit();

                        String queryString = "?user=" + userObject.get("uuid").getAsString();
                        WebResponse responseProvider;

                        responseProvider = HelperMethods.getCommand(BASE_URL + "provider", queryString, LoginActivity.this, USERNAME, PASSWORD);

                        if (responseProvider != null && responseProvider.getResponseCode() == 200) {
                            String provider_uuid = "";

                            JSONArray resultsArray = null;

                            try {
                                JSONObject JSONResponse = new JSONObject(responseProvider.getResponseString());
                                resultsArray = JSONResponse.getJSONArray("results");

                                if (resultsArray.length() != 0) {
                                    for (int i = 0; i < resultsArray.length(); i++) {
                                        JSONObject checking = resultsArray.getJSONObject(i);
                                        provider_uuid = checking.getString("uuid");
                                        editor.putString("providerid", provider_uuid);
                                        editor.commit();
                                    }

                                    OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                    return true;

                                }
                                return false;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return false;
                            }

                        }

                        return false;

                    } else {
                        return false;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);


            if (success) {
                final Account account = new Account(mEmail, "io.intelehealth.openmrs");
                manager.addAccountExplicitly(account, mPassword, null);
                offlineLogin.invalidateLoginCredentials();
                offlineLogin.setUpOfflineLogin(mEmail, mPassword);
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra("login", true);
                startJobDispatcherService(LoginActivity.this);
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * A method of FirebaseJobDispatcher Library.
     * It schedules background jobs for android app.
     *
     * @param context Current context
     * @return returns void
     */
    private void startJobDispatcherService(Context context) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job uploadCronJob = firebaseJobDispatcher.newJobBuilder()
                .setService(JobDispatchService.class)
                .setTag("Delayed Job Queue")
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        1770, 1830
                ))

                .setReplaceCurrent(true)
                .setConstraints(
                        // only run on any network
                        Constraint.ON_ANY_NETWORK)
                .build();

        firebaseJobDispatcher.mustSchedule(uploadCronJob);
    }
}

