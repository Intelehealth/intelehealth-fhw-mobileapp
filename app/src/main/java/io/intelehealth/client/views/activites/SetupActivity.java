package io.intelehealth.client.views.activites;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parse.Parse;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.databinding.ActivitySetupBinding;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.Results;
import io.intelehealth.client.models.loginModel.LoginModel;
import io.intelehealth.client.models.loginProviderModel.LoginProviderModel;
import io.intelehealth.client.network.ApiClient;
import io.intelehealth.client.network.ApiInterface;
import io.intelehealth.client.services.DownloadProtocolsTask;
import io.intelehealth.client.utilities.AdminPassword;
import io.intelehealth.client.utilities.Base64Methods;
import io.intelehealth.client.utilities.DialogUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.OfflineLogin;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UrlModifiers;
import io.intelehealth.client.viewModels.SetupViewModel;
import io.intelehealth.client.views.adapters.LocationArrayAdapter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    SetupViewModel setupViewModel;
    ActivitySetupBinding activitySetupBinding;
    private boolean isLocationFetched;
    private TestSetup mAuthTask = null;
    private List<Location> mLocations = new ArrayList<>();


    protected AccountManager manager;
    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Methods base64Methods = new Base64Methods();
    String encoded = null;
    AlertDialog.Builder dialog;
    String key = null;
    SessionManager sessionManager = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setup);
        activitySetupBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup);
        getSupportActionBar();
        setupViewModel = ViewModelProviders.of(this).get(SetupViewModel.class);
        /*set handlers with data binding*/
        activitySetupBinding.setViewmodel(setupViewModel);
        activitySetupBinding.setLifecycleOwner(this);
        sessionManager = new SessionManager(this);
        // Persistent login information
        manager = AccountManager.get(SetupActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activitySetupBinding.setupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });


        activitySetupBinding.adminPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        activitySetupBinding.textViewAid.setText("Android Id: " + IntelehealthApplication.getAndroidId());

        activitySetupBinding.setupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet), getString(R.string.generic_ok));

        activitySetupBinding.editTextURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationFetched = false;
                LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, new ArrayList<String>());
                activitySetupBinding.spinnerLocation.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Logger.logD(TAG, "ontextchanged" + s);
                Logger.logD(TAG, "on ui" + activitySetupBinding.editTextURL.getText().toString());
                if (!activitySetupBinding.editTextURL.getText().toString().trim().isEmpty() && activitySetupBinding.editTextURL.getText().toString().length() >= 13) {
                    if (Patterns.WEB_URL.matcher(activitySetupBinding.editTextURL.getText().toString()).matches()) {
                        String BASE_URL = "http://" + activitySetupBinding.editTextURL.getText().toString() + ":8080/openmrs/ws/rest/v1/";
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
        activitySetupBinding.email.setError(null);
        activitySetupBinding.password.setError(null);
        activitySetupBinding.adminPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = activitySetupBinding.email.getText().toString();
        String password = activitySetupBinding.password.getText().toString();
        String admin_password = activitySetupBinding.adminPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            activitySetupBinding.password.setError(getString(R.string.error_invalid_password));
            focusView = activitySetupBinding.password;
            cancel = true;
        }

        if (!TextUtils.isEmpty(admin_password) && !isPasswordValid(admin_password)) {
            activitySetupBinding.adminPassword.setError(getString(R.string.error_invalid_password));
            focusView = activitySetupBinding.adminPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            activitySetupBinding.email.setError(getString(R.string.error_field_required));
            focusView = activitySetupBinding.email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            activitySetupBinding.email.setError(getString(R.string.error_invalid_email));
            focusView = activitySetupBinding.email;

        }
        Location location = null;

        if (activitySetupBinding.spinnerLocation.getSelectedItemPosition() <= 0) {
            cancel = true;
            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_selected), Toast.LENGTH_LONG).show();
        } else {
            location = mLocations.get(activitySetupBinding.spinnerLocation.getSelectedItemPosition() - 1);
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
                String urlString = activitySetupBinding.editTextURL.getText().toString();
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
     * Parse locations fetched through api and provide the appropriate dropdown.
     *
     * @param url string of url.
     */
    private void getLocationFromServer(String url) {
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService =
                ApiClient.createService(ApiInterface.class);
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
                            activitySetupBinding.spinnerLocation.setAdapter(adapter);
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
                    activitySetupBinding.demoMindmap.setChecked(false);
                }
                break;

            case R.id.downloadMindmap:
                if (checked) {
                    activitySetupBinding.downloadMindmap.setChecked(false);

                    dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle(getString(R.string.enter_license_key))
                            .setView(promptsView)

                            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Dialog d = (Dialog) dialog;

                                    EditText text = d.findViewById(R.id.licensekey);
                                    key = text.getText().toString();
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

            String urlString = urlModifiers.loginUrl(CLEAN_URL);
            Logger.logD(TAG, "usernaem and password" + USERNAME + PASSWORD);
            encoded = base64Methods.encoded(USERNAME, PASSWORD);
            sessionManager.setEncoded(encoded);
            Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
            loginModelObservable.subscribe(new Observer<LoginModel>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(LoginModel loginModel) {
                    int responsCode = loginModel.hashCode();
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
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Logger.logD(TAG, "handle provider error" + e.getMessage());
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
                }

                @Override
                public void onComplete() {
                    Logger.logD(TAG, "completed");
                }
            });


            return 200;
        }

        @Override
        protected void onPostExecute(Integer success) {
            mAuthTask = null;
//            showProgress(false);

            if (success == 200) {
                final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                manager.addAccountExplicitly(account, PASSWORD, null);

                sessionManager.setLocationName(LOCATION.getDisplay());
                sessionManager.setLocationUuid(LOCATION.getUuid());
                sessionManager.setLocationDescription(LOCATION.getDescription());
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
                if (activitySetupBinding.downloadMindmap.isChecked()) {
                    if (sessionManager.getLicenseKey().contains("licensekey")) {
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


            } else if (success == 201) {
                activitySetupBinding.password.setError(getString(R.string.error_incorrect_password));
                activitySetupBinding.password.requestFocus();
            } else if (success == 3) {
                activitySetupBinding.editTextURL.setError(getString(R.string.url_invalid));
                activitySetupBinding.editTextURL.requestFocus();
            }

            progress.dismiss();
        }
    }

}
