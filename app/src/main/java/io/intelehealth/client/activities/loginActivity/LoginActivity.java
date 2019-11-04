package io.intelehealth.client.activities.loginActivity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.homeActivity.HomeActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.models.loginModel.LoginModel;
import io.intelehealth.client.models.loginProviderModel.LoginProviderModel;
import io.intelehealth.client.utilities.Base64Utils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.OfflineLogin;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UrlModifiers;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    TextView txt_cant_login;
    /**
     * A dummy authentication store containing known user names and passwords.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "username:password", "admin:nimda"
    };
    private final String TAG = LoginActivity.class.getSimpleName();
    protected AccountManager manager;
    ProgressDialog progress;
    SessionManager sessionManager = null;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;
    private OfflineLogin offlineLogin = null;

    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private long createdRecordsCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionManager = new SessionManager(this);
        setTitle(R.string.title_activity_login);

        offlineLogin = OfflineLogin.getOfflineLogin();
        txt_cant_login = findViewById(R.id.cant_login_id);
        txt_cant_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cant_log();
            }
        });
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
//            startJobDispatcherService(LoginActivity.this);
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
        mUsernameView = findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.logD(TAG, "button pressed");
                attemptLogin();
            }
        });

    }

    /**
     * Returns void.
     * This method checks if valid username and password are given as input.
     *
     * @return void
     */
    private void attemptLogin() {

//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }


        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
//
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
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
            UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
            Log.d(TAG, "attempting login");
        } else {
            //offlineLogin.login(email, password);
            offlineLogin.offline_login(email, password);
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
        if (show) progress.show();
        else progress.dismiss();
    }

    public void cant_log() {
        final SpannableString span_string = new SpannableString(getApplicationContext().getText(R.string.email_link));
        Linkify.addLinks(span_string, Linkify.EMAIL_ADDRESSES);

        new AlertDialog.Builder(this)
                .setMessage(span_string)
                .setNegativeButton("Send Email", new DialogInterface.OnClickListener() {
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

    /**
     * class UserLoginTask will authenticate user using email and password.
     * Depending on server's response, user may or may not have successful login.
     * This class also uses SharedPreferences to store session ID
     */
    public void UserLoginTask(String mEmail, String mPassword) {

//        private final String mEmail;
//        private final String mPassword;
//        boolean success = false;

//        UserLoginTask(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            showProgress(true);
//        }

//        @Override
//        protected Boolean doInBackground(Void... params) {


//                Log.d(TAG, "UN: " + USERNAME);
//                Log.d(TAG, "PW: " + PASSWORD);
        String urlString = urlModifiers.loginUrl(sessionManager.getServerUrl());
        Logger.logD(TAG, "usernaem and password" + mEmail + mPassword);
        encoded = base64Utils.encoded(mEmail, mPassword);
        sessionManager.setEncoded(encoded);
        showProgress(true);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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
                String url = urlModifiers.loginUrlProvider(sessionManager.getServerUrl(), loginModel.getUser().getUuid());
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
//                                                success = true;
                                            final Account account = new Account(mEmail, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, mPassword, null);
                                            Log.d("MANAGER", "MANAGER "+account);
                                            //offlineLogin.invalidateLoginCredentials();


                                            SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                                            //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                                            sqLiteDatabase.beginTransaction();
                                            //read_db.beginTransaction();
                                            ContentValues values = new ContentValues();

                                            try
                                            {
                                                values.put("username",mEmail);
                                                values.put("password", mPassword);
                                                createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values,SQLiteDatabase.CONFLICT_REPLACE);
                                                sqLiteDatabase.setTransactionSuccessful();

                                                Logger.logD("values", "values" + values);
                                                Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                            }
                                            catch (SQLException e)
                                            {
                                                Log.d("SQL","SQL user credentials: "+e);
                                            }

                                            finally {
                                                sqLiteDatabase.endTransaction();

                                            }


                                            offlineLogin.setUpOfflineLogin(mEmail, mPassword);

                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            intent.putExtra("login", true);
//                startJobDispatcherService(LoginActivity.this);
                                            startActivity(intent);
                                            finish();
                                            showProgress(false);

                                            sessionManager.setReturningUser(true);

                                        }
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
//                                        success = false;
                                    showProgress(false);
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
//                    success = false;
                showProgress(false);
//                    DialogUtils dialogUtils=new DialogUtils();
//                    dialogUtils.showerrorDialog(LoginActivity.this,"Error Login",getString(R.string.error_incorrect_password),"ok");
                Toast.makeText(LoginActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                mPasswordView.setError("");
                mUsernameView.setError("");
                mPasswordView.setText("");
                mUsernameView.setText("");
                mPasswordView.requestFocus();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


//            return true;
//
//        }

//        @Override
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;


//            if (success) {

//            } else {
//
//            }
//        }

//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            showProgress(false);
//        }
    }

}
