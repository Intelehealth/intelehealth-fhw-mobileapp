package io.intelehealth.client.activities.setupActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parse.Parse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.homeActivity.HomeActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.Results;
import io.intelehealth.client.models.loginModel.LoginModel;
import io.intelehealth.client.models.loginProviderModel.LoginProviderModel;
import io.intelehealth.client.networkApiCalls.ApiClient;
import io.intelehealth.client.networkApiCalls.ApiInterface;
import io.intelehealth.client.services.DownloadProtocolsTask;
import io.intelehealth.client.utilities.AdminPassword;
import io.intelehealth.client.utilities.Base64Utils;
import io.intelehealth.client.utilities.DialogUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.OfflineLogin;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UrlModifiers;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    private boolean isLocationFetched;
    String BASE_URL = "";
    private static final int PERMISSION_ALL = 1;

    protected AccountManager manager;
    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    AlertDialog.Builder dialog;
    String key = null;
    String licenseUrl = null;
    SessionManager sessionManager = null;
    public File base_dir;
    public String[] FILES;
    //        private TestSetup mAuthTask = null;
    private List<Location> mLocations = new ArrayList<>();
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mAdminPasswordView;
    private EditText mUrlField;
    private Button mLoginButton;
    private Spinner mDropdownLocation;
    private TextView mAndroidIdTextView;
    private RadioButton r1;
    private RadioButton r2;
    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar();
        sessionManager = new SessionManager(this);
        // Persistent login information
        manager = AccountManager.get(SetupActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code

        mLoginButton = findViewById(R.id.setup_submit_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        r1 = findViewById(R.id.demoMindmap);
        r2 = findViewById(R.id.downloadMindmap);

        mPasswordView = findViewById(R.id.password);

        mAdminPasswordView = findViewById(R.id.admin_password);

        Button submitButton = findViewById(R.id.setup_submit_button);

        mUrlField = findViewById(R.id.editText_URL);
        mDropdownLocation = findViewById(R.id.spinner_location);
        mAdminPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mAndroidIdTextView = findViewById(R.id.textView_Aid);
        mAndroidIdTextView.setText("Android Id: " + IntelehealthApplication.getAndroidId());

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                //progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);

            }
        });
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet), getString(R.string.generic_ok));

        mUrlField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationFetched = false;
                LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, new ArrayList<String>());
                mDropdownLocation.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(userStoppedTyping, 1500); // 1.5 second


            }

            Runnable userStoppedTyping = new Runnable() {

                @Override
                public void run() {
                    // user didn't typed for 1.5 seconds, do whatever you want
                    if (!mUrlField.getText().toString().trim().isEmpty() && mUrlField.getText().toString().length() >= 12 ) {
                        if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                            String BASE_URL = "http://" + mUrlField.getText().toString() + ":8080/openmrs/ws/rest/v1/";
                            if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
                                getLocationFromServer(BASE_URL);
                            else
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

        });



    }

    /**
     * Check username and password validations.
     * Get user selected location.
     */
    private void attemptLogin() {

//        if (mAuthTask != null) {
//            return;
//        }


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mAdminPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String admin_password = mAdminPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(admin_password) && !isPasswordValid(admin_password)) {
            mAdminPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mAdminPasswordView;
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
            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_selected), Toast.LENGTH_LONG);
        } else {
            location = mLocations.get(mDropdownLocation.getSelectedItemPosition() - 1);
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
                String urlString = mUrlField.getText().toString();
                TestSetup(urlString, email, password, admin_password, location);
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
     * Parse locations fetched through api and provide the appropriate dropdown.
     *
     * @param url string of url.
     */
    private void getLocationFromServer(String url) {
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<Results<Location>> resultsObservable = apiService.LOCATION_OBSERVABLE(null);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Results<Location>>() {
                        @Override
                        public void onNext(Results<Location> locationResults) {
                            if (locationResults.getResults() != null) {
                                Results<Location> locationList = locationResults;
                                mLocations = locationList.getResults();
                                List<String> items = getLocationStringList(locationList.getResults());
                                LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, items);
                                mDropdownLocation.setAdapter(adapter);
                                isLocationFetched = true;
                            } else {
                                isLocationFetched = false;
                                Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            isLocationFetched = false;
                            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
            mUrlField.setError(getString(R.string.url_invalid));
        }

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
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle(getString(R.string.enter_license_key))
                            .setView(promptsView)

                            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Dialog d = (Dialog) dialog;

                                    EditText text = d.findViewById(R.id.licensekey);
                                    EditText url = d.findViewById(R.id.licenseurl);
                                    key = text.getText().toString();
                                    licenseUrl = url.getText().toString();
                                    sessionManager.setMindMapServerUrl(licenseUrl);
                                    //Toast.makeText(SetupActivity.this, "" + key, Toast.LENGTH_SHORT).show();
                                    if (keyVerified(key)) {
                                        // create a shared pref to store the key

                                        // SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);

                                        //DOWNLOAD MIND MAP FILE LIST
                                        //upnew getJSONFile().execute(null, "AllFiles", "TRUE");

                                        // UpdateProtocolsTask updateProtocolsTask = new UpdateProtocolsTask(SetupActivity.this);
                                        // updateProtocolsTask.execute(null, "AllFiles", "TRUE");
                                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(SetupActivity.this);
                                        downloadProtocolsTask.execute(key);

                                    }
                                }
                            })

                            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                    // Get the alert dialog buttons reference
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    // Change the alert dialog buttons text and background color
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

                    negativeButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));


                }
                break;
        }
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }

    /**
     * Attempts login to the OpenMRS server.
     * If successful cretes a new {@link Account}
     * If unsuccessful details are saved in SharedPreferences.
     */
    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, String ADMIN_PASSWORD, Location location) {

//         String USERNAME;
//         String PASSWORD;
//         String CLEAN_URL;
//         String ADMIN_PASSWORD;
        ProgressDialog progress;

//         Location LOCATION;
//        int responsecode;

//        TestSetup(String url, String username, String password, String adminPassword, Location location) {
//            CLEAN_URL = url;
//            USERNAME = username;
//            PASSWORD = password;
//            LOCATION = location;
//            ADMIN_PASSWORD = adminPassword;
//        }

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();

//        }


//        @Override
//        protected Integer doInBackground(Void... params) {
        String urlString = urlModifiers.loginUrl(CLEAN_URL);
        Logger.logD(TAG, "usernaem and password" + USERNAME + PASSWORD);
        encoded = base64Utils.encoded(USERNAME, PASSWORD);
        sessionManager.setEncoded(encoded);

        progress = new ProgressDialog(SetupActivity.this);
        progress.setTitle(getString(R.string.please_wait_progress));
        progress.setMessage(getString(R.string.logging_in));
        progress.show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
        loginModelObservable.subscribe(new Observer<LoginModel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(LoginModel loginModel) {
                Boolean authencated = loginModel.getAuthenticated();
                Gson gson = new Gson();
                Logger.logD(TAG, "success" + gson.toJson(loginModel));
                sessionManager.setChwname(loginModel.getUser().getDisplay());
                sessionManager.setCreatorID(loginModel.getUser().getUuid());
                sessionManager.setSessionID(loginModel.getSessionId());
                sessionManager.setProviderID(loginModel.getUser().getPerson().getUuid());
                UrlModifiers urlModifiers = new UrlModifiers();
                String url = urlModifiers.loginUrlProvider(CLEAN_URL, loginModel.getUser().getUuid());
                if (authencated) {
                    Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                    loginProviderModelObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<LoginProviderModel>() {
                                @Override
                                public void onNext(LoginProviderModel loginProviderModel) {
                                    if (loginProviderModel.getResults().size() != 0) {
                                        for (int i = 0; i < loginProviderModel.getResults().size(); i++) {
                                            Log.i(TAG, "doInBackground: " + loginProviderModel.getResults().get(i).getUuid());
                                            sessionManager.setProviderID(loginProviderModel.getResults().get(i).getUuid());
//                                                responsecode = 200;
                                            final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, PASSWORD, null);

                                            sessionManager.setLocationName(location.getDisplay());
                                            sessionManager.setLocationUuid(location.getUuid());
                                            sessionManager.setLocationDescription(location.getDescription());
                                            sessionManager.setServerUrl(CLEAN_URL);
                                            sessionManager.setServerUrlRest(BASE_URL);
                                            sessionManager.setServerUrlBase("http://" + CLEAN_URL + ":8080/openmrs");
                                            sessionManager.setBaseUrl(BASE_URL);
                                            sessionManager.setSetupComplete(true);

                                            OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                            AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                                            Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                                                    .applicationId(AppConstants.IMAGE_APP_ID)
                                                    .server("http://" + CLEAN_URL + ":4040/parse/")
                                                    .build()
                                            );
                                            Log.i(TAG, "onPostExecute: Parse init");
                                            Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                                            intent.putExtra("setup", true);
                                            if (r2.isChecked()) {
                                                if (sessionManager.valueContains("licensekey")) {
                                                    startActivity(intent);
//                        startJobDispatcherService(SetupActivity.this);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SetupActivity.this, "Please enter a valid license key", Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                startActivity(intent);
                                                finish();
                                            }
                                            progress.dismiss();

//                                        } else if (success == 201) {
//                                            activitySetupBinding.password.setError(getString(R.string.error_incorrect_password));
//                                            activitySetupBinding.password.requestFocus();
//                                        } else if (success == 3) {
//                                            activitySetupBinding.editTextURL.setError(getString(R.string.url_invalid));
//                                            activitySetupBinding.editTextURL.requestFocus();
//                                        }
                                        }
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
                                    progress.dismiss();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                progress.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showerrorDialog(SetupActivity.this, "Error Login", getString(R.string.error_incorrect_password), "ok");
                mEmailView.requestFocus();
                // mEmailView.setError(getString(R.string.error_incorrect_password));
                // mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }


}
