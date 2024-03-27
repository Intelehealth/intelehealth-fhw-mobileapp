package org.intelehealth.app.activities.forgotPasswordNew;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.loginActivity.LoginActivityNew;
import org.intelehealth.app.models.ChangePasswordModel_New;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.OfflineLogin;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.TooltipWindow;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.security.SecureRandom;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ChangePasswordActivity_New extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "ChangePasswordActivity_";
    TextInputEditText etCurrentPassword, etNewPassword, etNewPasswordConfirm;
    CustomProgressDialog cpd;
    Context context;
    SessionManager sessionManager = null;
    TextView tvErrorCurrentPassword, tvErrorNewPassword, tvErrorConfirmPassword, tvGeneratePassword;
    RelativeLayout layoutParent;
    NetworkUtils networkUtils;
    ImageView ivIsInternet;
    ObjectAnimator syncAnimator;
    private CardView customSnackBar;
    private TextView customSnackBarText;
    private Button btnSave;
    private final SyncUtils syncUtils = new SyncUtils();
    TooltipWindow tipWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_new_ui2);
        networkUtils = new NetworkUtils(ChangePasswordActivity_New.this, this);
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ImageView ivBack = toolbar.findViewById(R.id.iv_back_arrow_common);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswordActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });
        context = ChangePasswordActivity_New.this;
        cpd = new CustomProgressDialog(context);
        sessionManager = new SessionManager(context);
        customSnackBar = findViewById(R.id.snackbar_cv);
        customSnackBarText = findViewById(R.id.snackbar_text);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password_change);
        etNewPasswordConfirm = findViewById(R.id.et_new_password_confirm);
        layoutParent = findViewById(R.id.layout_parent);
        tvErrorCurrentPassword = findViewById(R.id.tv_error_current_password);
        tvErrorNewPassword = findViewById(R.id.tv_error_new_password);
        tvErrorConfirmPassword = findViewById(R.id.tv_error_confirm_password);
        tvGeneratePassword = findViewById(R.id.textview_mobile_no_note);
        tipWindow = new TooltipWindow(ChangePasswordActivity_New.this);
        tvGeneratePassword.setOnClickListener(v -> {
            randomString(8);
        });

        btnSave = findViewById(R.id.btn_save_change);

        ivIsInternet.setOnClickListener(v -> {
            SyncUtils.syncNow(ChangePasswordActivity_New.this, ivIsInternet, syncAnimator);
        });

        ImageView ivQuestionChangePassword = findViewById(R.id.ivQuestionChangePasswordScreen);
        ivQuestionChangePassword.setOnClickListener(v -> {
            if (!tipWindow.isTooltipShown())
                tipWindow.showToolTip(ivQuestionChangePassword, getResources().getString(R.string.generate_password_tooltip_text));
        });

        btnSave.setOnClickListener(v -> {
            SnackbarUtils snackbarUtils = new SnackbarUtils();
            snackbarUtils.hideKeyboard(ChangePasswordActivity_New.this);
            if (areInputFieldsValid())
                if (NetworkConnection.isOnline(this)) {
                    apiCallForChangePassword(etCurrentPassword.getText().toString(), etNewPassword.getText().toString());
                }
        });

        tvTitle.setText(getResources().getString(R.string.change_password));


        manageErrorFields();
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

    public void apiCallForChangePassword(String currentPassword, String newPassword) {
        cpd.show(getString(R.string.please_wait));
        String serverUrl = BuildConfig.SERVER_URL;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String encoded = "Bearer " + sessionManager.getEncoded(); //Bearer bnVyc2UyMzpEYW5pZWxDcmFpZzE=
        ChangePasswordModel_New inputModel = new ChangePasswordModel_New(currentPassword, newPassword);
        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> loginModelObservable = apiService.CHANGE_PASSWORD_OBSERVABLE(inputModel, encoded);
        loginModelObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseBody test) {
                        showSnackBarAndRemoveLater(getString(R.string.the_password_has_been_successfully_changed), R.drawable.survey_snackbar_icon);
                        final Handler handler = new Handler();
                        handler.postDelayed(() -> performLogout(), 2000);
                        cpd.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Login Failure" + e.getMessage());
                        e.printStackTrace();
//                        if (e.getMessage().contains("HTTP 400")) {
                            tvErrorCurrentPassword.setVisibility(View.VISIBLE);
                            tvErrorCurrentPassword.setText(getResources().getString(R.string.error_password_not_exist));
                            etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
//                        } else {
//                            showSnackBarAndRemoveLater(getString(R.string.error_password_not_exist), R.drawable.fingerprint_dialog_error);
//                        }
                        cpd.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD(TAG, "completed");
                    }
                });

    }

    private boolean areInputFieldsValid() {
        boolean result = false;
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etNewPasswordConfirm.getText().toString();
        if (TextUtils.isEmpty(currentPassword)) {
            tvErrorCurrentPassword.setVisibility(View.VISIBLE);
            tvErrorCurrentPassword.setText(getResources().getString(R.string.enter_current_password));
            etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        } else if (TextUtils.isEmpty(newPassword)) {
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        } else if (TextUtils.isEmpty(confirmPassword)) {
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        } else if (!StringUtils.isValidPassword(newPassword)) {

            tvErrorNewPassword.setText(getString(R.string.password_validation));
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } /*else if (newPassword.length() >= 8 && !isValid(etNewPassword.getText().toString())) {
            result = false;
            tvErrorNewPassword.setText(getString(R.string.password_validation));
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        }*/ else if (!newPassword.equals(confirmPassword)) {
            etNewPasswordConfirm.setText("");
            tvErrorConfirmPassword.setText(getString(R.string.password_match));
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        } else if (currentPassword.equals(newPassword)) {
            etNewPassword.setText("");
            etNewPasswordConfirm.setText("");
            tvErrorNewPassword.setText(getString(R.string.old_password_and_new_password_cannot_be_same));
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            tvErrorConfirmPassword.setText(getString(R.string.old_password_and_new_password_cannot_be_same));
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        } else {
            etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            result = true;
        }
        return result;
    }

    private void manageErrorFields() {

        etCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (TextUtils.isEmpty(etCurrentPassword.getText().toString())) {
                        tvErrorCurrentPassword.setVisibility(View.VISIBLE);
                        tvErrorCurrentPassword.setText(getResources().getString(R.string.enter_current_password));
                        etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                        return;
                    } else {
                        tvErrorCurrentPassword.setVisibility(View.GONE);
                        tvErrorCurrentPassword.setText(getResources().getString(R.string.enter_current_password));
                        etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etNewPassword.getText().toString())) {
                    tvErrorNewPassword.setVisibility(View.VISIBLE);
                    etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvErrorNewPassword.setVisibility(View.GONE);
                    etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });

        etNewPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etNewPasswordConfirm.getText().toString())) {
                    tvErrorConfirmPassword.setVisibility(View.VISIBLE);
                    etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvErrorConfirmPassword.setVisibility(View.GONE);
                    etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });
    }

    /*public static boolean isValid(String passwordhere) {
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        if (passwordhere.length() < 8) {
            return false;
        } else {
            for (int p = 0; p < passwordhere.length(); p++) {
                if (Character.isUpperCase(passwordhere.charAt(p))) {
                    hasUppercase = true;
                }
            }
            for (int q = 0; q < passwordhere.length(); q++) {
                if (Character.isLowerCase(passwordhere.charAt(q))) {
                    hasLowercase = true;
                }
            }
            for (int r = 0; r < passwordhere.length(); r++) {
                if (Character.isDigit(passwordhere.charAt(r))) {
                    hasDigit = true;
                }
            }

            if (hasUppercase && hasLowercase && hasDigit)
                return true;
            else
                return false;
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_no_internet));

        }
    }

    private void performLogout() {
        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);
        Intent intent = new Intent(ChangePasswordActivity_New.this, LoginActivityNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        syncUtils.syncBackground();
        sessionManager.setReturningUser(false);
        sessionManager.setLogout(true);
    }

    private void showSnackBarAndRemoveLater(String text, int iconResId) {
        ImageView ivAlertIcon = findViewById(R.id.snackbar_icon);
        ivAlertIcon.setImageDrawable(ContextCompat.getDrawable(context, iconResId));
        btnSave.setVisibility(View.GONE);           // While displaying this snackbar over the button, the snackbar elevation is not visible if the button is visible
        customSnackBar.setVisibility(View.VISIBLE); // due to this, while displaying this snackbar, we're hiding the button so that the elevation is properly visible - added by Arpan Sircar
        customSnackBarText.setText(text);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            customSnackBar.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        }, 4000);
    }


    void randomString(int len) {
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        etNewPassword.setText(sb.toString());
        etNewPasswordConfirm.setText(sb.toString());

    }

}