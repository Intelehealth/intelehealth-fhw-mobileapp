package app.intelehealth.client.activities.setupActivity;

import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.parse.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import app.intelehealth.client.R;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.models.DownloadMindMapRes;
import app.intelehealth.client.models.Location;
import app.intelehealth.client.models.Results;
import app.intelehealth.client.models.loginModel.LoginModel;
import app.intelehealth.client.models.loginProviderModel.LoginProviderModel;
import app.intelehealth.client.networkApiCalls.ApiClient;
import app.intelehealth.client.networkApiCalls.ApiInterface;
import app.intelehealth.client.utilities.AdminPassword;
import app.intelehealth.client.utilities.Base64Utils;
import app.intelehealth.client.utilities.DialogUtils;
import app.intelehealth.client.utilities.DownloadMindMaps;
import app.intelehealth.client.utilities.Logger;
import app.intelehealth.client.utilities.NetworkConnection;
import app.intelehealth.client.utilities.SessionManager;
import app.intelehealth.client.utilities.StringEncryption;
import app.intelehealth.client.utilities.UrlModifiers;
import app.intelehealth.client.widget.materialprogressbar.CustomProgressDialog;

import app.intelehealth.client.activities.homeActivity.HomeActivity;
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
    private long createdRecordsCount = 0;
    ProgressDialog mProgressDialog;

    //    protected AccountManager manager;
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
    boolean click_box = false;

    Context context;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;
    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar();
        sessionManager = new SessionManager(this);
        // Persistent login information
//        manager = AccountManager.get(SetupActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        context = SetupActivity.this;
        customProgressDialog = new CustomProgressDialog(context);

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
                if (id == R.id.admin_password || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mAndroidIdTextView = findViewById(R.id.textView_Aid);
        String deviceID = "Device Id: " + IntelehealthApplication.getAndroidId();
        mAndroidIdTextView.setText(deviceID);

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
                    if (!mUrlField.getText().toString().trim().isEmpty() && mUrlField.getText().toString().length() >= 12) {
                        if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                            String BASE_URL = "https://" + mUrlField.getText().toString() + "/openmrs/ws/rest/v1/";
                            if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
                                getLocationFromServer(BASE_URL);
                            else
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

        });

        showProgressbar();
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

    private void showProgressbar() {


// instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(SetupActivity.this);
        mProgressDialog.setMessage(getString(R.string.download_protocols));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
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
            FirebaseCrashlytics.getInstance().recordException(e);
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
                if (NetworkConnection.isOnline(this)) {
                    if (checked) {
                        r1.setChecked(false);
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                        // AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
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
                                        if (text.getText().toString().isEmpty() && text.getText() == null || url.getText().toString().isEmpty() && url.getText() == null) {
                                            text.setFocusable(true);
                                            text.setError(getResources().getString(R.string.enter_license_key));
                                        }

                                        if (sessionManager.getLicenseKey() != null && sessionManager.getLicenseKey().equalsIgnoreCase("https://mindmaps.intelehealth.io:4040")) {
                                            text.setText(sessionManager.getLicenseKey());
                                            url.setText(sessionManager.getMindMapServerUrl());
                                        }


                                        if (!url.getText().toString().trim().isEmpty()) {
                                            if (Patterns.WEB_URL.matcher(url.getText().toString().trim()).matches()) {
                                                String url_field = "https://" + url.getText().toString() + ":3004/";
                                                if (URLUtil.isValidUrl(url_field)) {
                                                    key = text.getText().toString().trim();
                                                    licenseUrl = url.getText().toString().trim();

                                                    if (licenseUrl.isEmpty()) {
                                                        url.setError(getResources().getString(R.string.enter_server_url));
                                                        url.requestFocus();
                                                        return;
                                                    }
                                                    if (licenseUrl.contains(":")) {
                                                        url.setError(getResources().getString(R.string.invalid_url));
                                                        url.requestFocus();
                                                        return;
                                                    }
                                                    if (key.isEmpty()) {
                                                        text.setError(getResources().getString(R.string.enter_license_key));
                                                        text.requestFocus();
                                                        return;
                                                    }

                                                    sessionManager.setMindMapServerUrl(licenseUrl);
                                                    //Toast.makeText(SetupActivity.this, "" + key, Toast.LENGTH_SHORT).show();
                                                    if (keyVerified(key)) {
                                                        // create a shared pref to store the key

                                                        // SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);

                                                        //DOWNLOAD MIND MAP FILE LIST
                                                        //upnew getJSONFile().execute(null, "AllFiles", "TRUE");

                                                        // UpdateProtocolsTask updateProtocolsTask = new UpdateProtocolsTask(SetupActivity.this);
                                                        // updateProtocolsTask.execute(null, "AllFiles", "TRUE");
//                                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(SetupActivity.this);
//                                        downloadProtocolsTask.execute(key);
                                                        getMindmapDownloadURL("https://" + licenseUrl + ":3004/");

                                                    }
                                                } else {
                                                    Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();

                                                }
                                            } else {
                                                //invalid url || invalid url and key.
                                                Toast.makeText(SetupActivity.this, "Enter valid License Url", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(SetupActivity.this, "Please enter URL and Key", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })

                                .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        r2.setChecked(false);
                                        r1.setChecked(true);

                                    }
                                });
                        AlertDialog alertDialog = dialog.create();
                        alertDialog.setView(promptsView, 20, 0, 20, 0);
                        alertDialog.show();
                        // Get the alert dialog buttons reference
                        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                        // Change the alert dialog buttons text and background color
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        // positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);


                    }
                } else {
                    ((RadioButton) v).setChecked(false);
                    Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
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

        ProgressDialog progress;

        String urlString = urlModifiers.loginUrl(CLEAN_URL);
        Logger.logD(TAG, "usernaem and password" + USERNAME + PASSWORD);
        encoded = base64Utils.encoded(USERNAME, PASSWORD);
        sessionManager.setEncoded(encoded);

        progress = new ProgressDialog(SetupActivity.this, R.style.AlertDialogStyle);
        ;//SetupActivity.this);
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
                                          /*  final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, PASSWORD, null);*/

                                            sessionManager.setLocationName(location.getDisplay());
                                            sessionManager.setLocationUuid(location.getUuid());
                                            sessionManager.setLocationDescription(location.getDescription());
                                            sessionManager.setServerUrl(CLEAN_URL);
                                            sessionManager.setServerUrlRest(BASE_URL);
                                            sessionManager.setServerUrlBase("https://" + CLEAN_URL + "/openmrs");
                                            sessionManager.setBaseUrl(BASE_URL);
                                            sessionManager.setSetupComplete(true);

                                            // OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                            AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                                            Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                                                    .applicationId(AppConstants.IMAGE_APP_ID)
                                                    .server("https://" + CLEAN_URL + ":1337/parse/")
                                                    .build()
                                            );

                                            SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                                            //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                                            sqLiteDatabase.beginTransaction();
                                            //read_db.beginTransaction();
                                            ContentValues values = new ContentValues();

                                            //StringEncryption stringEncryption = new StringEncryption();
                                            String random_salt = getSalt_DATA();

                                            //String random_salt = stringEncryption.getRandomSaltString();
                                            Log.d("salt", "salt: " + random_salt);
                                            //Salt_Getter_Setter salt_getter_setter = new Salt_Getter_Setter();
                                            //salt_getter_setter.setSalt(random`_salt);


                                            String hash_password = null;
                                            try {
                                                //hash_email = StringEncryption.convertToSHA256(random_salt + mEmail);
                                                hash_password = StringEncryption.convertToSHA256(random_salt + PASSWORD);
                                            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                                                FirebaseCrashlytics.getInstance().recordException(e);
                                            }

                                            try {
                                                values.put("username", USERNAME);
                                                values.put("password", hash_password);
                                                values.put("creator_uuid_cred", loginModel.getUser().getUuid());
                                                values.put("chwname", loginModel.getUser().getDisplay());
                                                values.put("provider_uuid_cred", sessionManager.getProviderID());
                                                createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                                sqLiteDatabase.setTransactionSuccessful();

                                                Logger.logD("values", "values" + values);
                                                Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                            } catch (SQLException e) {
                                                Log.d("SQL", "SQL user credentials: " + e);
                                            } finally {
                                                sqLiteDatabase.endTransaction();
                                            }

                                            Log.i(TAG, "onPostExecute: Parse init");
                                            Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                                            intent.putExtra("setup", true);
                                            if (r2.isChecked()) {
                                                if (!sessionManager.getLicenseKey().isEmpty()) {
                                                    sessionManager.setTriggerNoti("no");
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SetupActivity.this, R.string.please_enter_valid_license_key, Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                sessionManager.setTriggerNoti("no");
                                                startActivity(intent);
                                                finish();
                                            }
                                            progress.dismiss();
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
                mPasswordView.requestFocus();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }

    public String getSalt_DATA() {
        BufferedReader reader = null;
        String salt = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("salt.env")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                salt = mLine;
                Log.d("SA", "SA " + salt);
            }
        } catch (Exception e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //log the exception
                }
            }
        }
        return salt;

    }

    private void getMindmapDownloadURL(String url) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                            customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                                Log.e("MindMapURL", "Successfully get MindMap URL");
                                mTask = new DownloadMindMaps(context, mProgressDialog);
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                checkExistingMindMaps();

                            } else {
//                                Toast.makeText(SetupActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                                Toast.makeText(SetupActivity.this, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            Log.e("MindMapURL", " " + e);
                            Toast.makeText(SetupActivity.this, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

    private void checkExistingMindMaps() {

        //Check is there any existing mindmaps are present, if yes then delete.

        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        Log.e(TAG, "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        Log.e(TAG, "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        Log.e(TAG, "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        Log.e(TAG, "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        Log.e(TAG, "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        Log.e(TAG, "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }

        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        Log.e("DOWNLOAD", "isSTARTED");

    }
}
