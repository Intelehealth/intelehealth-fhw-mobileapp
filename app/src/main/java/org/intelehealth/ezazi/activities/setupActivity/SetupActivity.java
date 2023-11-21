package org.intelehealth.ezazi.activities.setupActivity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.StrictMode;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.parse.Parse;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.models.DownloadMindMapRes;
import org.intelehealth.ezazi.models.Location;
import org.intelehealth.ezazi.models.OxytocinResponseModel;
import org.intelehealth.ezazi.models.Results;
import org.intelehealth.ezazi.models.loginModel.LoginModel;
import org.intelehealth.ezazi.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.services.firebase_services.TokenRefreshUtils;
import org.intelehealth.ezazi.ui.shared.InputChangeValidationListener;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.password.activity.ForgotPasswordActivity;
import org.intelehealth.ezazi.utilities.Base64Utils;
import org.intelehealth.ezazi.utilities.DialogUtils;
import org.intelehealth.ezazi.utilities.DownloadMindMaps;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringEncryption;
import org.intelehealth.ezazi.utilities.TextThemeUtils;
import org.intelehealth.ezazi.utilities.UrlModifiers;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;
import org.intelehealth.klivekit.utils.FirebaseUtils;
import org.intelehealth.klivekit.utils.Manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    private TextInputEditText mEmailView;
    private TextInputEditText mPasswordView;
    private TextInputLayout mLocationInputView;
    private TextInputLayout mEmailInputView;
    private TextInputLayout mPasswordInputView;

    //    private EditText mAdminPasswordView;
//    private EditText mUrlField;
    private Button mLoginButton;
    private AutoCompleteTextView mDropdownLocation;
    //    private Spinner spinner_state, spinner_district,
//            spinner_sanch, spinner_village;
//    private TextView mAndroidIdTextView;
//    private RadioButton r1;
//    private RadioButton r2;
    final Handler mHandler = new Handler();
    boolean click_box = false;

    Context context;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;

    private String setupUrl = "";

    CustomProgressDialog customProgressDialog;

    //    private BroadcastReceiver MyReceiver = null;
    CoordinatorLayout coordinatorLayout;
    //    HashMap<String, String> hashMap1, hashMap2, hashMap3, hashMap4;
//    boolean value = false;
    String base_url;
//    Map.Entry<String, String> village_name;
//    int state_count = 0, district_count = 0, sanch_count = 0, village_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_ezazi);
        sessionManager = new SessionManager(SetupActivity.this);

        setupUrl = BuildConfig.SERVER_URL;
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        TokenRefreshUtils.refreshToken(this);
        context = SetupActivity.this;
        customProgressDialog = new CustomProgressDialog(context);

        // Set up the login form.
        mLocationInputView = findViewById(R.id.etLocationLayout);
        mEmailInputView = findViewById(R.id.etUsernameLayout);
        mPasswordInputView = findViewById(R.id.etPasswordLayout);
        mEmailView = findViewById(R.id.et_email);

        mPasswordView = findViewById(R.id.et_password);

        mLoginButton = findViewById(R.id.btnSetup);
        changeButtonStatus(false);
        findViewById(R.id.includeForgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(SetupActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            //cantLogin());
        });
        mLoginButton.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mLoginButton.getWindowToken(), 0);
            attemptLogin();
        });

        addValidationListener();
        MaterialButton forgotPassword = findViewById(R.id.includeForgotPassword);
        TextThemeUtils.applyUnderline(forgotPassword);

        mDropdownLocation = findViewById(R.id.spinner_location);

        if (!setupUrl.trim().isEmpty() || !setupUrl.trim().equalsIgnoreCase("")) {

            isLocationFetched = false;
            mEmailView.setError(null);

            LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, new ArrayList<String>());
            mDropdownLocation.setOnItemClickListener((parent, view, position, id) -> {
                Log.e(TAG, "onCreate: id=>" + view.getContentDescription());
                mLocationInputView.setError(null);
                changeButtonStatus(true);
            });
            mDropdownLocation.setAdapter(adapter);

            if (!setupUrl.trim().isEmpty() && setupUrl.length() >= 12) {
                if (Patterns.WEB_URL.matcher(setupUrl).matches()) {
                    String BASE_URL = setupUrl + "/openmrs/ws/rest/v1/";
                    base_url = setupUrl + "/openmrs/ws/rest/v1/";
                    if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
//                                value = getLocationFromServer(BASE_URL); //state wise locations...
                        getLocationFromServer(BASE_URL);
                    else
                        Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                }
            }

        } else {

        }

        showProgressbar();
    }

    // Replaced by Mithun Vaghela
    private void showConfirmationDialog(String title, int content) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this).title(title).positiveButtonLabel(R.string.generic_ok).hideNegativeButton(true).content(getString(content)).build();

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void addValidationListener() {
        new InputChangeValidationListener(mEmailInputView, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isEmailValid(text);
            }

            @Override
            public void onValidatted(boolean isValid) {
                changeButtonStatus(isValid);
            }
        }).validate(getString(R.string.error_invalid_email));

        new InputChangeValidationListener(mPasswordInputView, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isPasswordValid(text);
            }

            @Override
            public void onValidatted(boolean isValid) {
                changeButtonStatus(isValid);
            }
        }).validate(getString(R.string.error_invalid_password));
    }

    private void changeButtonStatus(boolean isValid) {
        mLoginButton.setEnabled(isValid && checkInputNotEmpty());
    }

    private boolean checkInputNotEmpty() {
        return mDropdownLocation.getText().length() > 0 && Objects.requireNonNull(mEmailView.getText()).length() > 0 && Objects.requireNonNull(mPasswordView.getText()).length() > 0;
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
//        mUrlField.setError(null);
        mEmailInputView.setError(null);
        mPasswordInputView.setError(null);
        mLocationInputView.setError(null);
//        mAdminPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String url = setupUrl;
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
//        String admin_password = mAdminPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(url)) {
//            focusView = mUrlField;
            cancel = true;
        }


//        if (!TextUtils.isEmpty(admin_password) && !isPasswordValid(admin_password)) {
//            mAdminPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mAdminPasswordView;
//            cancel = true;
//        }

        String selectedLocation = mDropdownLocation.getText().toString();
        // Check for a valid email address.
        if (TextUtils.isEmpty(selectedLocation)) {
            mLocationInputView.setError(getString(R.string.error_location_not_selected));
//            focusView = mLocationInputView;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailInputView.setError(getString(R.string.error_require_email));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailInputView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordInputView.setError(getString(R.string.error_require_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordInputView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        //spinner...
//        if (spinner_state.getSelectedItemPosition() <= 0) {
//            cancel = true;
//            focusView = spinner_state;
//            TextView t = (TextView) spinner_state.getSelectedView();
//            t.setError("Select State");
//            t.setTextColor(Color.RED);
//            Toast.makeText(SetupActivity.this, "Select State from dropdown", Toast.LENGTH_LONG).show();
//        } else if (spinner_district.getSelectedItemPosition() <= 0) {
//            cancel = true;
//            focusView = spinner_district;
//            TextView t = (TextView) spinner_district.getSelectedView();
//            t.setError("Select District");
//            focusView.setEnabled(true);
//            t.setTextColor(Color.RED);
//            Toast.makeText(SetupActivity.this, "Select District from dropdown", Toast.LENGTH_LONG).show();
//        } else if (spinner_sanch.getSelectedItemPosition() <= 0) {
//            cancel = true;
//            focusView = spinner_sanch;
//            TextView t = (TextView) spinner_sanch.getSelectedView();
//            t.setError("Select Sanch");
//            t.setTextColor(Color.RED);
//            Toast.makeText(SetupActivity.this, "Select Sanch from dropdown", Toast.LENGTH_LONG).show();
//        } else if (spinner_village.getSelectedItemPosition() <= 0) {
//            cancel = true;
//            focusView = spinner_village;
//            TextView t = (TextView) spinner_village.getSelectedView();
//            t.setError("Select Village");
//            t.setTextColor(Color.RED);
//            Toast.makeText(SetupActivity.this, "Select Village from dropdown", Toast.LENGTH_LONG).show();
//        }


        //spinner-end...

        Location location = new Location();

        //add state wise here...

      /*  if (mDropdownLocation.getSelectedItemPosition() <= 0) {
            cancel = true;
            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_selected), Toast.LENGTH_LONG);
        } else {
            location = mLocations.get(mDropdownLocation.getSelectedItemPosition() - 1);
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null) {
                if (TextUtils.isEmpty(url)) {
//                    mUrlField.requestFocus();
//                    mUrlField.setError("Enter Url");
                }

                focusView.requestFocus();
            }
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //  if (location != null) {
            //   Log.i(TAG, "location:: " + location.getDisplay());
            String urlString = setupUrl;
            // as in ezazi we dont want to show locations dropdown so adding as static value.
            location.setDisplay(selectedLocation);
            location.setUuid("eb374eaf-430e-465e-81df-fe94c2c515be");
            TestSetup(urlString, email, password, location);
            Log.d(TAG, "attempting setup");
            //    }

//            if (village_name != null) {
//                String urlString = setupUrl;
//                TestSetup(urlString, email, password, admin_password, village_name);
//                Log.d(TAG, "attempting setup");
//            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showOkDialog(this, getString(R.string.generic_info), getString(R.string.setup_internet_not_available), getString(R.string.generic_ok));
            return false;
        } else {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet_available), getString(R.string.generic_ok));
            return true;
        }
//        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        broadcastIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(MyReceiver);
    }

//    public void broadcastIntent() {
//        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//    }

    private void showProgressbar() {
// instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(SetupActivity.this);
        mProgressDialog.setMessage(getString(R.string.download_protocols));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
    }

    private boolean isEmailValid(String email) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 7;
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
            resultsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<Results<Location>>() {
                @Override
                public void onNext(Results<Location> locationResults) {
                    if (locationResults.getResults() != null) {
                        Results<Location> locationList = locationResults;
                        mLocations = locationList.getResults();
                        List<String> items = getLocationStringList(locationList.getResults());
                        LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, items);
                        mDropdownLocation.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
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
//            mUrlField.setError(getString(R.string.url_invalid));
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
//        list.add(getString(R.string.login_location_select));
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }


    public void onRadioClick(View v) {

    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }
    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, Location location) {
        Log.i(TAG, "location:: " + location.getDisplay());
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
                Log.e(TAG, "onNext: setChwname" + sessionManager.getChwname());
                Log.e(TAG, "onNext: setCreatorID" + sessionManager.getCreatorID());
                Log.e(TAG, "onNext: setSessionID" + sessionManager.getSessionID());
                Log.e(TAG, "onNext: setProviderID" + sessionManager.getProviderID());
                UrlModifiers urlModifiers = new UrlModifiers();
                String url = urlModifiers.loginUrlProvider(CLEAN_URL, loginModel.getUser().getUuid());
                if (authencated) {
                    Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                    loginProviderModelObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<LoginProviderModel>() {
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
                                    sessionManager.setServerUrlBase(CLEAN_URL + "/openmrs");
                                    sessionManager.setBaseUrl(BASE_URL);
                                    sessionManager.setSetupComplete(true);
                                    saveToken();

                                    IntelehealthApplication.getInstance().startRealTimeObserverAndSocket();

                                    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext()).applicationId(AppConstants.IMAGE_APP_ID).server("https://" + CLEAN_URL + ":1337/parse/").build());

                                    SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

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
                                    } catch (NoSuchAlgorithmException |
                                             UnsupportedEncodingException e) {
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
                                    sessionManager.setTriggerNoti("no");
                                    startActivity(intent);
                                    finish();
                                    progress.dismiss();
                                }

                                // here call Oxytocin api and save the result in sessionmanager.
                                if (sessionManager.getOxytocinValue() == null) {
                                    fetchOxytocinValueFromAPI();
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
                showConfirmationDialog("Error Login", R.string.error_incorrect_password);
                mEmailView.requestFocus();
                mPasswordView.requestFocus();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }

    public void cantLogin() {
        final SpannableString span_string = new SpannableString(getApplicationContext().getText(R.string.email_link));
        Linkify.addLinks(span_string, Linkify.EMAIL_ADDRESSES);

        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this).content(getString(R.string.contact_whatsapp)).positiveButtonLabel(R.string.contact).negativeButtonLabel(R.string.close_button).build();

        dialog.setListener(() -> {
            String phoneNumberWithCountryCode = AppConstants.HELP_NUMBER;//"+917005308163";
            String message = getString(R.string.hello_my_name_is) + sessionManager.getChwname() +
                    /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumberWithCountryCode, message))));
        });

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void fetchOxytocinValueFromAPI() {
        String url = urlModifiers.getOxytocinUrl();
        Call<OxytocinResponseModel> call = AppConstants.apiInterface.GET_OXYTOCIN_UNIT(url);
        call.enqueue(new Callback<OxytocinResponseModel>() {
            @Override
            public void onResponse(Call<OxytocinResponseModel> call, Response<OxytocinResponseModel> response) {
                if (response.body() != null && response.body().getData() != null && response.isSuccessful()) {
                    // ie. response is received successfully...
                    for (int i = 0; i < response.body().getData().size(); i++) {
                        sessionManager.setOxytocinValue(response.body().getData().get(i).getValue());
                    }
                    Log.v(TAG, "oxytocin value: " + sessionManager.getOxytocinValue().toString());
                }
            }

            @Override
            public void onFailure(Call<OxytocinResponseModel> call, Throwable t) {
                Log.e(TAG, "oxytocin error: " + t.getMessage());
            }
        });
    }

    public String getSalt_DATA() {
        BufferedReader reader = null;
        String salt = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("salt.env")));

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
            resultsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<DownloadMindMapRes>() {
                @Override
                public void onNext(DownloadMindMapRes res) {
                    customProgressDialog.dismiss();
                    if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                        Log.e("MindMapURL", "Successfully get MindMap URL");
                        mTask = new DownloadMindMaps(context, mProgressDialog, "setup");
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
            context.createConfigurationContext(conf);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    public void showMindmapFailedAlert() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//      MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(getResources().getString(R.string.protocol_download_failed_alertdialog));
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.generic_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
//                r1.setChecked(true);
//                r2.setChecked(false);
                sessionManager.setLicenseKey("");
            }
        });
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (NetworkConnection.isOnline(SetupActivity.this)) {
                    getMindmapDownloadURL("https://" + licenseUrl + ":3004/");
                } else {
                    dialog.dismiss();
//                    r1.setChecked(true);
//                    r2.setChecked(false);
                    sessionManager.setLicenseKey("");
                    Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void saveToken() {
        ProviderDAO providerDAO = new ProviderDAO();
        Manager.getInstance().setBaseUrl("https://" + sessionManager.getServerUrl());
        // save fcm reg. token for chat (Video)
        try {
            FirebaseUtils.saveToken(this, providerDAO.getUserUuid(sessionManager.getProviderID()), IntelehealthApplication.getInstance().refreshedFCMTokenID, sessionManager.getAppLanguage());
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

}
