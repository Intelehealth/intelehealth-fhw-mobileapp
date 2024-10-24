package org.intelehealth.app.activities.setupActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.parse.Parse;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.forgotPasswordNew.ForgotPasswordActivity_New;
import org.intelehealth.app.activities.forgotPasswordNew.ForgotPasswordOtpVerificationActivity_New;
import org.intelehealth.app.activities.forgotPasswordNew.ResetPasswordActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.loginActivity.LoginActivityNew;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.DownloadMindMapRes;
import org.intelehealth.app.models.Location;
import org.intelehealth.app.models.Results;
import org.intelehealth.app.models.loginModel.LoginModel;
import org.intelehealth.app.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.AdminPassword;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadMindMaps;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringEncryption;
import org.intelehealth.app.utilities.TooltipWindow;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.authJWT_API.AuthJWTBody;
import org.intelehealth.app.utilities.authJWT_API.AuthJWTResponse;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;
import org.intelehealth.klivekit.data.PreferenceHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SetupActivityNew extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "SetupActivityNew";
    private List<Location> mLocations = new ArrayList<>();
    private boolean isLocationFetched;
    AutoCompleteTextView autotvLocations;
    TextInputEditText etUsername, etPassword, etAdminPassword;
    UrlModifiers urlModifiers = new UrlModifiers();
    String encoded = null;
    Base64Utils base64Utils = new Base64Utils();
    SessionManager sessionManager = null;
    String BASE_URL = "";
    private long createdRecordsCount = 0;
    Location location = null;
    private RadioButton r1, r2;
    String key = null;
    String licenseUrl = null;
    CustomProgressDialog customProgressDialog;
    Context context;
    private DownloadMindMaps mTask;
    ProgressDialog mProgressDialog;
    private String mindmapURL = "";
    ImageView questionIV;
    private TextView mLocationErrorTextView, mUserNameErrorTextView, mPasswordErrorTextView, mNoInternetTextView, tvForgotPassword;
    TooltipWindow tipWindow;
    Button btnSetup;
    NetworkUtils networkUtils;

    CustomProgressDialog cpd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_new_ui2);
        sessionManager = new SessionManager(this);
        context = SetupActivityNew.this;
        cpd = new CustomProgressDialog(context);

        networkUtils = new NetworkUtils(context, this);
        questionIV = findViewById(R.id.setup_info_question_mark);
        customProgressDialog = new CustomProgressDialog(context);
        autotvLocations = findViewById(R.id.autotv_select_location);
        btnSetup = findViewById(R.id.btn_setup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password1);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etAdminPassword = findViewById(R.id.admin_password);
        mLocationErrorTextView = findViewById(R.id.tv_location_error);
        mUserNameErrorTextView = findViewById(R.id.tv_username_error);
        mPasswordErrorTextView = findViewById(R.id.tv_password_error);
        mNoInternetTextView = findViewById(R.id.tvNoNetworkSetupScreen);
        mLocationErrorTextView.setVisibility(View.GONE);
        mUserNameErrorTextView.setVisibility(View.GONE);
        mPasswordErrorTextView.setVisibility(View.GONE);
        etUsername.addTextChangedListener(new MyTextWatcher(etUsername));
        etPassword.addTextChangedListener(new MyTextWatcher(etPassword));
        tipWindow = new TooltipWindow(SetupActivityNew.this);

        ImageView ivBackArrow = findViewById(R.id.iv_back_arrow);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        r1 = findViewById(R.id.demoMindmap);
        r2 = findViewById(R.id.downloadMindmap);

        questionIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tipWindow.isTooltipShown())
                    tipWindow.showToolTip(questionIV, getResources().getString(R.string.setup_tooltip_text));
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivityNew.this, ForgotPasswordActivity_New.class);
                intent.putExtra("action",AppConstants.FORGOT_USER_PASSWORD_ACTION);
                startActivity(intent);
            }
        });
        TextView forgotUserNameLabelTextView = findViewById(R.id.tv_forgot_username);

        forgotUserNameLabelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(SetupActivityNew.this, ForgotPasswordActivity_New.class);
                intent.putExtra("action",AppConstants.FORGOT_USER_NAME_ACTION);
                startActivity(intent);*/
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showOkDialog(SetupActivityNew.this, getString(R.string.forgot_your_username), getString(R.string.contact_your_admin), getString(R.string.generic_ok));
            }
        });

        if (isNetworkConnected()) {
            mNoInternetTextView.setVisibility(View.GONE);
            getLocationFromServer();
        }

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(btnSetup.getWindowToken(), 0);
                attemptLogin();
            }
        });


        etAdminPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.admin_password || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //onebackpressed is deprecated
        //that's why added the implementation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkUtils.callBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            mNoInternetTextView.setVisibility(View.GONE);
            getLocationFromServer();
            tvForgotPassword.setEnabled(true);
            btnSetup.setEnabled(true);
        } else {
            mNoInternetTextView.setVisibility(View.VISIBLE);
            tvForgotPassword.setEnabled(false);
            btnSetup.setEnabled(false);
        }
    }

    class MyTextWatcher implements TextWatcher {
        EditText editText;

        MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            if (this.editText.getId() == R.id.et_username) {
                if (val.isEmpty()) {
                    mUserNameErrorTextView.setVisibility(View.VISIBLE);
                    mUserNameErrorTextView.setText(getString(R.string.error_field_required));
                    etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                } else {
                    mUserNameErrorTextView.setVisibility(View.GONE);
                    etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            } else if (this.editText.getId() == R.id.et_password) {
                if (val.isEmpty()) {
                    mPasswordErrorTextView.setVisibility(View.VISIBLE);
                    mPasswordErrorTextView.setText(getString(R.string.error_field_required));
                    etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                } else {
                    mPasswordErrorTextView.setVisibility(View.GONE);
                    etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
        }
    }

    private void attemptLogin() {
        // Store values at the time of the login attempt.
        String userName = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String admin_password = etAdminPassword.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(autotvLocations.getText().toString())) {
            autotvLocations.requestFocus();

            mLocationErrorTextView.setVisibility(View.VISIBLE);
            mLocationErrorTextView.setText(getString(R.string.error_location_not_selected));
            autotvLocations.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return;
        } else {
            mLocationErrorTextView.setVisibility(View.GONE);
            autotvLocations.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(userName)) {
            etUsername.requestFocus();
            mUserNameErrorTextView.setVisibility(View.VISIBLE);
            mUserNameErrorTextView.setText(getString(R.string.error_field_required));
            etUsername.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return;
        } else {
            mUserNameErrorTextView.setVisibility(View.GONE);
            etUsername.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (!isEmailValid(userName)) {
            etUsername.requestFocus();
            mUserNameErrorTextView.setVisibility(View.VISIBLE);
            mUserNameErrorTextView.setText(getString(R.string.error_field_required));
            etUsername.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return;
        } else {
            mUserNameErrorTextView.setVisibility(View.GONE);
            etUsername.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            etPassword.requestFocus();
            mPasswordErrorTextView.setVisibility(View.VISIBLE);
            mPasswordErrorTextView.setText(getString(R.string.error_field_required));
            etPassword.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return;
        } else {
            mPasswordErrorTextView.setVisibility(View.GONE);
            etPassword.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            etPassword.requestFocus();
            mPasswordErrorTextView.setVisibility(View.VISIBLE);
            mPasswordErrorTextView.setText(getString(R.string.error_invalid_password));
            etPassword.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return;
        } else {
            mPasswordErrorTextView.setVisibility(View.GONE);
            etPassword.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (location != null) {
            CustomLog.i(TAG, location.getDisplay());
            //TestSetup(BuildConfig.SERVER_URL, userName, password, admin_password, location);
            //getting jwt token here
            getJWTToken(BuildConfig.SERVER_URL, userName, password, admin_password,location);
            CustomLog.d(TAG, "attempting setup");
        }
    }

    /**
     * some deprecated code updated
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                    return true;
                }
                default -> {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    /**
     * getting jwt token here
     * @param urlString
     * @param username
     * @param password
     * @param admin_password
     * @param location
     */
    private void getJWTToken(String urlString, String username, String password, String admin_password, Location location) {
        cpd.show(getString(R.string.please_wait));

        String finalURL = urlString.concat(":3030/auth/login");
        AuthJWTBody authBody = new AuthJWTBody(username, password, true);
        Observable<AuthJWTResponse> authJWTResponseObservable = AppConstants.apiInterface.AUTH_LOGIN_JWT_API(finalURL, authBody);

        authJWTResponseObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AuthJWTResponse authJWTResponse) {
                        // in case of error password
                        if (!authJWTResponse.getStatus()) {
                            cpd.dismiss();
                            showErrorDialog();
                            return;
                        }

                        sessionManager.setJwtAuthToken(authJWTResponse.getToken());
                        new PreferenceHelper(SetupActivityNew.this).save(PreferenceHelper.AUTH_TOKEN,authJWTResponse.getToken());
                        TestSetup(urlString, username, password, admin_password,location);
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        showErrorDialog();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, String ADMIN_PASSWORD, Location location) {
        CustomLog.d(TAG, "TestSetup: ");
        String urlString = urlModifiers.loginUrl(CLEAN_URL);
        encoded = base64Utils.encoded(USERNAME, PASSWORD);
        sessionManager.setEncoded(encoded);
        CustomLog.d(TAG, "TestSetup: urlString : " + urlString);
        CustomLog.d(TAG, "TestSetup: encoded : " + encoded);
        CustomLog.d(TAG, "TestSetup: encodednew : " + "Basic " + encoded);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
        loginModelObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(LoginModel loginModel) {
                        if (loginModel != null) {
                            Boolean authencated = loginModel.getAuthenticated();
                            if (authencated) {
                                Gson gson = new Gson();
                                sessionManager.setChwname(loginModel.getUser().getDisplay());
                                sessionManager.setCreatorID(loginModel.getUser().getUuid());
                                sessionManager.setSessionID(loginModel.getSessionId());
                                sessionManager.setProviderID(loginModel.getUser().getPerson().getUuid());
                                UrlModifiers urlModifiers = new UrlModifiers();
                                String url = urlModifiers.loginUrlProvider(CLEAN_URL, loginModel.getUser().getUuid());

                                Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                                loginProviderModelObservable
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new DisposableObserver<LoginProviderModel>() {
                                            @Override
                                            public void onNext(LoginProviderModel loginProviderModel) {
                                                if (loginProviderModel.getResults().size() != 0) {
                                                    for (int i = 0; i < loginProviderModel.getResults().size(); i++) {
                                                        CustomLog.i(TAG, "doInBackground: " + loginProviderModel.getResults().get(i).getUuid());
                                                        try {
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
                                                            sessionManager.setFirstTimeLaunch(false);
                                                            sessionManager.setFirstProviderLoginTime(AppConstants.dateAndTimeUtils.currentDateTime());

                                                            IntelehealthApplication.getInstance().initSocketConnection();
                                                            CustomLog.d(TAG, "onNext: 11");
                                                            // OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                                            AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                                                            Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                                                                    .applicationId(AppConstants.IMAGE_APP_ID)
                                                                    .server("https://" + CLEAN_URL + ":1337/parse/")
                                                                    .build()
                                                            );

                                                            SQLiteDatabase sqLiteDatabase = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
                                                            //SQLiteDatabase read_db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();

                                                            sqLiteDatabase.beginTransaction();
                                                            //read_db.beginTransaction();
                                                            ContentValues values = new ContentValues();
                                                            //StringEncryption stringEncryption = new StringEncryption();
                                                            String random_salt = getSalt_DATA();

                                                            //String random_salt = stringEncryption.getRandomSaltString();
                                                            CustomLog.d("salt", "salt: " + random_salt);
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
                                                                CustomLog.d(TAG, "onCreate: selected chw1 : " + loginModel.getUser().getDisplay());

                                                                Logger.logD("values", "values" + values);
                                                                Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                                            } catch (SQLException e) {
                                                                CustomLog.d("SQL", "SQL user credentials: " + e);
                                                            } finally {
                                                                sqLiteDatabase.endTransaction();
                                                            }
                                                            CustomLog.i(TAG, "onPostExecute: Parse init");
                                                            sessionManager.setIsLoggedIn(true);

                                                            Intent intent = new Intent(SetupActivityNew.this, HomeScreenActivity_New.class);
                                                            intent.putExtra("setup", true);
                                                            intent.putExtra("firstLogin", "firstLogin");

                                                            //  if (r2.isChecked()) {
                                               /* if (!sessionManager.getLicenseKey().isEmpty()) {
                                                    sessionManager.setTriggerNoti("no");
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SetupActivityNew.this, R.string.please_enter_valid_license_key, Toast.LENGTH_LONG).show();
                                                }*/
                                                            //   } else {
                                                            sessionManager.setTriggerNoti("no");
                                                            startActivity(intent);
                                                            finish();
                                                            // }
                                                            //  progress.dismiss();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                }

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Logger.logD(TAG, "handle provider error" + e.getMessage());
                                                e.printStackTrace();
                                                cpd.dismiss();
                                                ////   progress.dismiss();
                                                // dismissLoggingInDialog();
                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        });
                                cpd.dismiss();
                            }
                            else {
                                CustomLog.d(TAG, "onNext: loginmodel is null");
                                cpd.dismiss();
                                showErrorDialog();
                            }
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Login Failure" + e.getMessage());
                        e.printStackTrace();
                        // progress.dismiss();
                        ///  dismissLoggingInDialog();
                        cpd.dismiss();
                        showErrorDialog();

                    }

                    @Override
                    public void onComplete() {
                        Logger.logD(TAG, "completed");
                    }
                });


    }

    /**
     * error dialog for incorrect credentials
     */
    private void showErrorDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(SetupActivityNew.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.error_login_title), getString(R.string.error_incorrect_password), true, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }

    private void getLocationFromServer() {
        isLocationFetched = false;
        String BASE_URL = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/";
        if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched) {
            ApiClient.changeApiBaseUrl(BASE_URL);
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
                                    CustomLog.d(TAG, "11onNext: locations list size : " + locationList.getResults().size());
                                    mLocations = locationList.getResults();
                                    List<String> items = getLocationStringList(locationList.getResults());
                                    CustomLog.d(TAG, "11onNext: items size : " + items.size());
                                    LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivityNew.this, items);
                                    autotvLocations.setAdapter(adapter);
                                    isLocationFetched = true;
                                    autotvLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            String selectedValue = (String) adapterView.getItemAtPosition(i);
                                            autotvLocations.setThreshold(100);  // Setting high threshold so that everytime all suggestions are shown.
                                            autotvLocations.setText("");
                                            autotvLocations.setText(selectedValue);
                                            int pos = items.indexOf(selectedValue);
                                            location = locationList.getResults().get(pos);
                                            autotvLocations.setError(null);
                                            autotvLocations.setSelection(autotvLocations.getText().length());
                                            mLocationErrorTextView.setVisibility(View.GONE);
                                            autotvLocations.setBackgroundResource(R.drawable.bg_input_fieldnew);

                                        }
                                    });
                                } else {
                                    isLocationFetched = false;
                                    Toast.makeText(SetupActivityNew.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                isLocationFetched = false;
                                Toast.makeText(SetupActivityNew.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } catch (IllegalArgumentException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

        } else
            Toast.makeText(SetupActivityNew.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();


    }

    private List<String> getLocationStringList(List<Location> locationList) {
        List<String> list = new ArrayList<String>();

        try {
            for (int i = 0; i < locationList.size(); i++) {
                list.add(locationList.get(i).getDisplay());
                CustomLog.d(TAG, "getLocationStringList: value : " + locationList.get(i).getDisplay());
            }


        } catch (Exception e) {
            CustomLog.d(TAG, "getLocationStringList: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return list;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return true;
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
                CustomLog.d("SA", "SA " + salt);
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
                        LayoutInflater li = LayoutInflater.from(this);
                        View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);

                        dialog.setTitle(getString(R.string.enter_license_key))
                                .setView(promptsView)
                                .setPositiveButton(getString(R.string.button_ok), null)
                                .setNegativeButton(getString(R.string.button_cancel), null);

                        AlertDialog alertDialog = dialog.create();
                        alertDialog.setView(promptsView, 20, 0, 20, 0);
                        alertDialog.show();
                        alertDialog.setCanceledOnTouchOutside(false); //dialog wont close when clicked outside...

                        // Get the alert dialog buttons reference
                        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                        // Change the alert dialog buttons text and background color
                        positiveButton.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                        negativeButton.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));

                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditText text = promptsView.findViewById(R.id.licensekey);
                                EditText url = promptsView.findViewById(R.id.licenseurl);

                                url.setError(null);
                                text.setError(null);

                                //If both are not entered...
                                if (url.getText().toString().trim().isEmpty() && text.getText().toString().trim().isEmpty()) {
                                    url.requestFocus();
                                    url.setError(getResources().getString(R.string.enter_server_url));
                                    text.setError(getResources().getString(R.string.enter_license_key));
                                    return;
                                }

                                //If Url is empty...key is not empty...
                                if (url.getText().toString().trim().isEmpty() && !text.getText().toString().trim().isEmpty()) {
                                    url.requestFocus();
                                    url.setError(getResources().getString(R.string.enter_server_url));
                                    return;
                                }

                                //If Url is not empty...key is empty...
                                if (!url.getText().toString().trim().isEmpty() && text.getText().toString().trim().isEmpty()) {
                                    text.requestFocus();
                                    text.setError(getResources().getString(R.string.enter_license_key));
                                    return;
                                }

                                //If Url has : in it...
                                if (url.getText().toString().trim().contains(":")) {
                                    url.requestFocus();
                                    url.setError(getResources().getString(R.string.invalid_url));
                                    return;
                                }

                                //If url entered is Invalid...
                                if (!url.getText().toString().trim().isEmpty()) {
                                    if (Patterns.WEB_URL.matcher(url.getText().toString().trim()).matches()) {
                                        String url_field = "https://" + url.getText().toString() + ":3004/";
                                        if (URLUtil.isValidUrl(url_field)) {
                                            key = text.getText().toString().trim();
                                            licenseUrl = url.getText().toString().trim();

                                            sessionManager.setMindMapServerUrl(licenseUrl);

                                            if (keyVerified(key)) {
                                                getMindmapDownloadURL("https://" + licenseUrl + ":3004/");
                                                alertDialog.dismiss();
                                            }
                                        } else {
                                            Toast.makeText(SetupActivityNew.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        //invalid url || invalid url and key.
                                        Toast.makeText(SetupActivityNew.this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SetupActivityNew.this, R.string.please_enter_url_and_key, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                r2.setChecked(false);
                                r1.setChecked(true);
                            }
                        });

                        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);


                    }
                } else {
                    ((RadioButton) v).setChecked(false);
                    Toast.makeText(SetupActivityNew.this, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }

    private void getMindmapDownloadURL(String url) {
        customProgressDialog.show(getString(R.string.please_wait));
        ApiClient.changeApiBaseUrl(url);
        String encoded1 = sessionManager.getJwtAuthToken();
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key,"Bearer "+encoded1);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                            customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                                CustomLog.e("MindMapURL", "Successfully get MindMap URL");
                                //mTask = new DownloadMindMaps(context, mProgressDialog, "setup");
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                checkExistingMindMaps();

                            } else {
//                                Toast.makeText(SetupActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                                Toast.makeText(SetupActivityNew.this, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            CustomLog.e("MindMapURL", " " + e);
                            Toast.makeText(SetupActivityNew.this, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            CustomLog.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            CustomLog.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

    private void checkExistingMindMaps() {

        //Check is there any existing mindmaps are present, if yes then delete.

        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        CustomLog.e(TAG, "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        CustomLog.e(TAG, "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        CustomLog.e(TAG, "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        CustomLog.e(TAG, "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        CustomLog.e(TAG, "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        CustomLog.e(TAG, "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }

        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        CustomLog.e("DOWNLOAD", "isSTARTED");

    }

    @Override
    protected void onResume() {
        super.onResume();
        //temporary added
        sessionManager.setIsLoggedIn(false);
    }
}