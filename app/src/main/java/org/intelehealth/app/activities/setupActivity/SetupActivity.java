package org.intelehealth.app.activities.setupActivity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.parse.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.smartcaredoc.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.NewLocationDao;
import org.intelehealth.app.models.DownloadMindMapRes;
import org.intelehealth.app.models.Location;
import org.intelehealth.app.models.Results;
import org.intelehealth.app.models.loginModel.LoginModel;
import org.intelehealth.app.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.app.models.statewise_location.ChildLocation;
import org.intelehealth.app.models.statewise_location.Setup_LocationModel;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.AdminPassword;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadMindMaps;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringEncryption;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import org.intelehealth.app.activities.homeActivity.HomeActivity;
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
//    private List<Location> mLocations = new ArrayList<>();
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mAdminPasswordView;
    private EditText mUrlField;
    private Button mLoginButton;
//    private Spinner mDropdownLocation;
    private Spinner spinner_state, spinner_district, spinner_village;
    private TextView mAndroidIdTextView;
    private RadioButton r1;
    private RadioButton r2;
    final Handler mHandler = new Handler();
    boolean click_box = false;

    Context context;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;
    CustomProgressDialog customProgressDialog;
    CoordinatorLayout coordinatorLayout;
    HashMap<String, String> hashMap1, hashMap2, hashMap3, hashMap4;
    boolean value = false;
    String base_url;
    Map.Entry<String, String> village_name;
    int state_count = 0, district_count = 0, /*sanch_count = 0,*/ village_count = 0;
    private String selectedState = "", selectedDistrict = "", /*selectedSanch = "",*/
            selectedVillage = "";
    NewLocationDao newLocationDao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar();
        sessionManager = new SessionManager(SetupActivity.this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the unicef should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        // Persistent login information
//        manager = AccountManager.get(SetupActivity.this);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        context = SetupActivity.this;
        customProgressDialog = new CustomProgressDialog(context);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code


        mPasswordView = findViewById(R.id.password);

        mAdminPasswordView = findViewById(R.id.admin_password);
        mUrlField = findViewById(R.id.editText_URL);

        mLoginButton = findViewById(R.id.setup_submit_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mLoginButton.getWindowToken(), 0);
                attemptLogin();

            }
        });

        r1 = findViewById(R.id.demoMindmap);
        r2 = findViewById(R.id.downloadMindmap);

        Button submitButton = findViewById(R.id.setup_submit_button);

//        mDropdownLocation = findViewById(R.id.spinner_location);
        spinner_state = findViewById(R.id.spinner_state);
        spinner_district = findViewById(R.id.spinner_district);
//        spinner_sanch = findViewById(R.id.spinner_sanch);
        spinner_village = findViewById(R.id.spinner_village);

//        spinner_state.setEnabled(false);
        spinner_district.setEnabled(false);
//        spinner_sanch.setEnabled(false);
        spinner_village.setEnabled(false);

        //isOnline();

//        MyReceiver = new NetworkChangeListener() {
//            @Override
//            protected void onNetworkChange(String status) {
//                Snackbar.make(coordinatorLayout, status, Snackbar.LENGTH_SHORT)
//                        .setTextColor(getResources().getColor(R.color.white)).show();
//            }
//        };

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
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(submitButton.getWindowToken(), 0);
                attemptLogin();
                //progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);

            }
        });
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet), getString(R.string.generic_ok));

        /*if(!mUrlField.getText().toString().trim().isEmpty() ||
        !mUrlField.getText().toString().trim().equalsIgnoreCase("")) {

            isLocationFetched = false;
            mEmailView.setError(null);

            LocationArrayAdapter adapter = new LocationArrayAdapter
                    (SetupActivity.this, new ArrayList<String>());
            mDropdownLocation.setAdapter(adapter);*/

            if (!mUrlField.getText().toString().trim().isEmpty() && mUrlField.getText().toString().length() >= 12) {
                if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                    String BASE_URL = "https://" + mUrlField.getText().toString() + ":3004/api/openmrs/";
                    base_url = "https://" + mUrlField.getText().toString() + "/openmrs/ws/rest/v1/";
                    if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
                        value = getLocationFromServer(BASE_URL); //state wise locations...
                    else
                        Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                }
            }

        /*}
        else {

        }*/
        mUrlField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationFetched = false;
                mEmailView.setError(null);
                state_count = 0;
                district_count = 0;
//                sanch_count = 0;
                village_count = 0;
                empty_spinner("url");
                /*LocationArrayAdapter adapter = new LocationArrayAdapter
                        (SetupActivity.this, new ArrayList<String>());
                mDropdownLocation.setAdapter(adapter);*/
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
                            String BASE_URL = "https://" + mUrlField.getText().toString() + ":3004/api/openmrs/";
                            base_url = "https://" + mUrlField.getText().toString() + "/openmrs/ws/rest/v1/";
                            if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
                                value = getLocationFromServer(BASE_URL); //state wise locations...
                            else
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

        });

        spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //district wise locations...
                if (position != 0) {
                    String state_uuid = "";
                    selectedState = spinner_state.getSelectedItem().toString();
                    List<String> district_locations = newLocationDao.getDistrictList(selectedState, context);
                    if (district_locations.size() > 1) {
                        LocationArrayAdapter locationArrayAdapter =
                                new LocationArrayAdapter(SetupActivity.this, district_locations);

                        spinner_district.setEnabled(true);
                        spinner_district.setAlpha(1);
                        spinner_district.setAdapter(locationArrayAdapter);
                        isLocationFetched = true;
                    } else {
                        empty_spinner("state");
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        this will give Sanch...
        spinner_district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //district wise locations...
                if (position != 0) {
                    String sanch_uuid = "";
                    selectedDistrict = spinner_district.getSelectedItem().toString();
                    List<String> village_locations = newLocationDao.getVillageList(selectedState, selectedDistrict/*, selectedSanch*/, context);
                    if (village_locations.size() > 1) {
                        LocationArrayAdapter locationArrayAdapter =
                                new LocationArrayAdapter(SetupActivity.this, village_locations);
                        spinner_village.setEnabled(true);
                        spinner_village.setAlpha(1);
                        spinner_village.setAdapter(locationArrayAdapter);
                        isLocationFetched = true;
                    } else {
                        empty_spinner("district");
                    }
                } else {
                    empty_spinner("district");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        to fetch village and pass as locations to location-api
        spinner_village.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //village wise locations...
                try {
                    if (position != 0) {
                        String village_uuid = "";
                        selectedVillage = spinner_village.getSelectedItem().toString();
                        village_uuid = newLocationDao.getVillageUuid(selectedState, selectedDistrict/*, selectedSanch*/, selectedVillage);
                        hashMap4 = new HashMap<>();
                        hashMap4.put(village_uuid, selectedVillage);
                        for (Map.Entry<String, String> entry : hashMap4.entrySet()) {
                            village_name = entry;
                        }
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showProgressbar();
    }

    private void empty_spinner(String value) {

        if (value.equalsIgnoreCase("state")) {
            List<String> list_district = new ArrayList<>();
            list_district.add(getString(R.string.setup_select_district_str));
            spinner_district.setEnabled(false);
            spinner_district.setAlpha(0.4F);
            LocationArrayAdapter adapter_district = new LocationArrayAdapter(SetupActivity.this, list_district);
            spinner_district.setAdapter(adapter_district);

            /*List<String> list_sanch = new ArrayList<>();
            list_sanch.add("Select Sanch");
            spinner_sanch.setEnabled(false);
            spinner_sanch.setAlpha(0.4F);
            LocationArrayAdapter adapter_sanch = new LocationArrayAdapter(SetupActivity.this, list_sanch);
            spinner_sanch.setAdapter(adapter_sanch);*/

            List<String> list_village = new ArrayList<>();
            list_village.add(getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        } else if (value.equalsIgnoreCase("district")) {
            /*List<String> list_sanch = new ArrayList<>();
            list_sanch.add("Select Sanch");
            spinner_sanch.setEnabled(false);
            spinner_sanch.setAlpha(0.4F);
            LocationArrayAdapter adapter_sanch = new LocationArrayAdapter(SetupActivity.this, list_sanch);
            spinner_sanch.setAdapter(adapter_sanch);*/

            List<String> list_village = new ArrayList<>();
            list_village.add(getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        } /*else if (value.equalsIgnoreCase("sanch")) {
            List<String> list_village = new ArrayList<>();
            list_village.add("Select Village");
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        }*/ else if (value.equalsIgnoreCase("village")) {
            //do nothing
        } else {
            List<String> list_state = new ArrayList<>();
            list_state.add(getString(R.string.setup_select_state_str));
            spinner_state.setEnabled(false);
            spinner_state.setAlpha(0.4F);
            LocationArrayAdapter adapter_state = new LocationArrayAdapter(SetupActivity.this, list_state);
            spinner_state.setAdapter(adapter_state);

            List<String> list_district = new ArrayList<>();
            list_district.add(getString(R.string.setup_select_district_str));
            spinner_district.setEnabled(false);
            spinner_district.setAlpha(0.4F);
            LocationArrayAdapter adapter_district = new LocationArrayAdapter(SetupActivity.this, list_district);
            spinner_district.setAdapter(adapter_district);

            /*List<String> list_sanch = new ArrayList<>();
            list_sanch.add("Select Sanch");
            spinner_sanch.setEnabled(false);
            spinner_sanch.setAlpha(0.4F);
            LocationArrayAdapter adapter_sanch = new LocationArrayAdapter(SetupActivity.this, list_sanch);
            spinner_sanch.setAdapter(adapter_sanch);*/

            List<String> list_village = new ArrayList<>();
            list_village.add(getString(R.string.setup_select_village_str));
            spinner_village.setEnabled(false);
            spinner_village.setAlpha(0.4F);
            LocationArrayAdapter adapter_village = new LocationArrayAdapter(SetupActivity.this, list_village);
            spinner_village.setAdapter(adapter_village);
        }
    }


    /**
     * Check username and password validations.
     * Get user selected location.
     */
    private void attemptLogin() {

        // Reset errors.
        mUrlField.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mAdminPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String url = mUrlField.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String admin_password = mAdminPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(url)) {
            focusView = mUrlField;
            cancel = true;
        }

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

        //spinner...
        if (spinner_state.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_state;
            TextView t = (TextView) spinner_state.getSelectedView();
            t.setError(getString(R.string.setup_select_state_str));
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getString(R.string.setup_select_dropdown_state_msg), Toast.LENGTH_LONG).show();
        }
        else if (spinner_district.getVisibility()== View.VISIBLE && spinner_district.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_district;
            TextView t = (TextView) spinner_district.getSelectedView();
            t.setError(getString(R.string.setup_select_district_str));
            focusView.setEnabled(true);
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getString(R.string.setup_select_dropdown_district_msg), Toast.LENGTH_LONG).show();
        }
        else if (spinner_village.getVisibility()== View.VISIBLE &&  spinner_village.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinner_village;
            TextView t = (TextView) spinner_village.getSelectedView();
            t.setError(getString(R.string.setup_select_village_str));
            t.setTextColor(Color.RED);
            Toast.makeText(SetupActivity.this, getString(R.string.setup_select_dropdown_village_msg), Toast.LENGTH_LONG).show();
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null) {
                if (TextUtils.isEmpty(url)) {
                    mUrlField.requestFocus();
                    mUrlField.setError("Enter Url");
                }

                focusView.requestFocus();
            }
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (village_name != null) {
                String urlString = mUrlField.getText().toString();
                mLoginButton.setText(getString(R.string.please_wait_progress));
                mLoginButton.setEnabled(false);
                TestSetup(urlString, email, password, admin_password, village_name);
                Log.d(TAG, "attempting setup");
            }

//            if (village_name != null) {
//                String urlString = mUrlField.getText().toString();
//                TestSetup(urlString, email, password, admin_password, village_name);
//                Log.d(TAG, "attempting setup");
//            }
        }
    }

    public boolean isOnline () {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showOkDialog(this, getString(R.string.generic_info), getString(R.string.setup_internet_not_available), getString(R.string.generic_ok));
            return false;
        }
        else
        {
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
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean getLocationFromServer(String url) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);

        try {
            Observable<Setup_LocationModel> locationObservable = apiService.SETUP_LOCATIONOBSERVABLE();
            locationObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Setup_LocationModel>() {
                        @Override
                        public void onNext(@NonNull Setup_LocationModel location) {
                            if (location.getStates() != null) {
                                try {
                                    newLocationDao = new NewLocationDao();
                                    newLocationDao.insertSetupLocations(location);
                                    customProgressDialog.dismiss();

                                    List<String> state_locations = newLocationDao.getStateList(context);

                                    if (state_locations.size() != 0) {
                                        LocationArrayAdapter locationArrayAdapter =
                                                new LocationArrayAdapter(SetupActivity.this, state_locations);

                                        spinner_state.setEnabled(true);
                                        spinner_state.setAlpha(1);
                                        spinner_state.setAdapter(locationArrayAdapter);
                                        isLocationFetched = true;
                                    } else {
                                        empty_spinner("state");
                                    }

                                    /*hashMap1 = new HashMap<>();
                                    for (int i = 0; i < state.getResults().size(); i++) {
                                        hashMap1.put(state.getResults().get(i).getUuid(),
                                                state.getResults().get(i).getDisplay());
                                    }*/
                                    value = true;
                                } catch (DAOException e) {
                                    e.printStackTrace();
                                    customProgressDialog.dismiss();
                                    value = false;
                                }
                            } else {
                                customProgressDialog.dismiss();
                                value = false;
                                isLocationFetched = false;
                                Toast.makeText(SetupActivity.this, "Unable to fetch State", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            value = false;
                            if (e.getLocalizedMessage().contains("Unable to resolve host")) {
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SetupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                            customProgressDialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            value = true;
                            customProgressDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            mUrlField.setError(getString(R.string.url_invalid));
            customProgressDialog.dismiss();
        }
        return value;
    }

    /**
     * Parse locations fetched through api and provide the appropriate dropdown.
     *
     * @param url string of url.
     */
   /* private void getLocationFromServer(String url) {
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

    }*/

    /*private boolean getLocationFromServer(String url) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);

        try {
            Observable<State> stateObservable = apiService.STATE_OBSERVABLE();
            stateObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<State>() {
                        @Override
                        public void onNext(@NonNull State state) {
                            if (state.getResults() != null) {
                                customProgressDialog.dismiss();
                                List<String> state_locations = getLocation(state.getResults());
                                LocationArrayAdapter locationArrayAdapter =
                                        new LocationArrayAdapter(SetupActivity.this, state_locations);

                                spinner_state.setEnabled(true);
                                spinner_state.setAlpha(1);
                                spinner_state.setAdapter(locationArrayAdapter);
                                isLocationFetched = true;

                                hashMap1 = new HashMap<>();
                                for (int i = 0; i < state.getResults().size(); i++) {
                                    hashMap1.put(state.getResults().get(i).getUuid(),
                                            state.getResults().get(i).getDisplay());
                                }

                                value = true;
                            } else {
                                customProgressDialog.dismiss();
                                value = false;
                                isLocationFetched = false;
                                Toast.makeText(SetupActivity.this, "Unable to fetch State", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            value = false;
                            Toast.makeText(SetupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            customProgressDialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            value = true;
                            customProgressDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            mUrlField.setError(getString(R.string.url_invalid));
            customProgressDialog.dismiss();
        }

        return value;
    }*/


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

//    private List<String> getLocation(List<Result> resultList) {
//        List<String> list = new ArrayList<>();
//        list.add("Select State");
//        for (int i = 0; i < resultList.size(); i++) {
//            list.add(resultList.get(i).getDisplay());
//        }
//
//        return list;
//    }

    private List<String> getLocation_district(List<ChildLocation> childLocationList, String location_wise) {
        List<String> list = new ArrayList<>();

        if (location_wise.equalsIgnoreCase("state")) {
            list.add("Select District");
        }  else if (location_wise.equalsIgnoreCase("district")) {
            list.add("Select Village");
        }

        for (int i = 0; i < childLocationList.size(); i++) {
            list.add(childLocationList.get(i).getDisplay());
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
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

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
                                            Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        //invalid url || invalid url and key.
                                        Toast.makeText(SetupActivity.this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SetupActivity.this, R.string.please_enter_url_and_key, Toast.LENGTH_SHORT).show();
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
    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, String ADMIN_PASSWORD, Map.Entry<String, String> location) {

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
                                            sessionManager.setLocationName(location.getValue());
                                            sessionManager.setLocationUuid(location.getKey());
                                            sessionManager.setStateName(selectedState);
                                            //  sessionManager.setLocationDescription(location.getDescription());
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
                dialogUtils.showerrorDialog(SetupActivity.this, getResources().getString(R.string.error_login_title), getString(R.string.error_incorrect_password),  getResources().getString(R.string.ok));
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
                                mTask = new DownloadMindMaps(context, mProgressDialog,"setup");
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
//        Locale locale = new Locale(appLanguage);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
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

    public void showMindmapFailedAlert(){
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//      MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(getResources().getString(R.string.protocol_download_failed_alertdialog));
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.generic_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                r1.setChecked(true);
                r2.setChecked(false);
                sessionManager.setLicenseKey("");
            }
        });
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (NetworkConnection.isOnline(SetupActivity.this)) {
                    getMindmapDownloadURL("https://" + licenseUrl + ":3004/");
                }else {
                    dialog.dismiss();
                    r1.setChecked(true);
                    r2.setChecked(false);
                    sessionManager.setLicenseKey("");
                    Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

}
