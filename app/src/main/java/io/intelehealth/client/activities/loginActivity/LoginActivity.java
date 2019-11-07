package io.intelehealth.client.activities.loginActivity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;

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
import io.intelehealth.client.widget.materialprogressbar.CustomProgressDialog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    TextView txt_cant_login;

    private final String TAG = LoginActivity.class.getSimpleName();
    protected AccountManager manager;
    //    ProgressDialog progress;
    Context context;
    CustomProgressDialog cpd;
    SessionManager sessionManager = null;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private OfflineLogin offlineLogin = null;

    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private ImageView icLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = LoginActivity.this;
        sessionManager = new SessionManager(context);
        cpd = new CustomProgressDialog(context);

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
//          startJobDispatcherService(LoginActivity.this);
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
        icLogo = findViewById(R.id.iv_logo);
        setLogo();

        // Set up the login form.
        mUsernameView = findViewById(R.id.et_email);
        // populateAutoComplete(); TODO: create our own autocomplete code
        mPasswordView = findViewById(R.id.et_password);
//      mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void setLogo() {

        File f = new File("/data/data/" + context.getPackageName() + "/files/logo/ic_sams.png");
        if (f.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile("/data/data/" + context.getPackageName() + "/files/logo/ic_sams.png");
            icLogo.setImageBitmap(bitmap);
        } else {
            Log.e("SetLogo","No Logo Found in Mindmap Folder");
        }

    }

    /**
     * Returns void.
     * This method checks if valid username and password are given as input.
     *
     * @return void
     */
    private void attemptLogin() {

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.enter_username));
            mUsernameView.requestFocus();
            return;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.enter_password));
            mPasswordView.requestFocus();
            return;
        }

        if (password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            return;
        }

        if (NetworkConnection.isOnline(this)) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            UserLoginTask(email, password);
        } else {
            offlineLogin.login(email, password);
        }
    }

//    private void showProgress(final boolean show) {
//        if (progress == null) {
//            progress = new ProgressDialog(LoginActivity.this);
//            progress.setTitle(getString(R.string.please_wait_progress));
//            progress.setMessage(getString(R.string.logging_in));
//        }
//        if (show) progress.show();
//        else progress.dismiss();
//    }

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
                        // intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");

                        startActivity(Intent.createChooser(intent, "Send Email"));
                        //add email function here !
                    }

                })
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * class UserLoginTask will authenticate user using email and password.
     * Depending on server's response, user may or may not have successful login.
     * This class also uses SharedPreferences to store session ID
     */
    public void UserLoginTask(String mEmail, String mPassword) {

        String urlString = urlModifiers.loginUrl(sessionManager.getServerUrl());
        Logger.logD(TAG, "username and password" + mEmail + mPassword);
        encoded = base64Utils.encoded(mEmail, mPassword);
        sessionManager.setEncoded(encoded);
//      showProgress(true);
        cpd.show();
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
                                            final Account account = new Account(mEmail, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, mPassword, null);
                                            offlineLogin.invalidateLoginCredentials();
                                            offlineLogin.setUpOfflineLogin(mEmail, mPassword);
                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            intent.putExtra("login", true);
//                                          startJobDispatcherService(LoginActivity.this);
                                            startActivity(intent);
                                            finish();
//                                          showProgress(false);
                                            cpd.dismiss();
                                            sessionManager.setReturningUser(true);
                                        }
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
//                                  showProgress(false);
                                    cpd.dismiss();
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
//              showProgress(false);
                cpd.dismiss();
                Toast.makeText(LoginActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
//                mPasswordView.setError("");
//                mUsernameView.setError("");
//                mPasswordView.setText("");
//                mUsernameView.setText("");
//                mPasswordView.requestFocus();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });
    }
}
