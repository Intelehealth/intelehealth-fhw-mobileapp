package org.intelehealth.app.activities.onboarding;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.activity.AbhaCardVerificationActivity;
import org.intelehealth.app.abdm.activity.CreateAbhaAccountActivity;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.widget.TextViewDialogFragment;

import java.util.Locale;

public class PrivacyPolicyActivity_New extends BaseActivity {
    private static final String TAG = "PrivacyPolicyActivityNe";
    private Button btn_accept_privacy;
    private int mIntentFrom;
    String appLanguage, intentType;
    SessionManager sessionManager = null;
    private Context context = PrivacyPolicyActivity_New.this;
    public static final String hasABHA = "hasABHA";
    public static final String ABHA_CONSENT = "ABHA_CONSENT";
    public static final String intentPatientNameTag = "patientName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_new_ui2);
        sessionManager = new SessionManager(PrivacyPolicyActivity_New.this);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        intentType = getIntent().getStringExtra("intentType");
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        btn_accept_privacy = findViewById(R.id.btn_accept_privacy);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(PrivacyPolicyActivity_New.this, SetupPrivacyNoteActivity_New.class);
//                startActivity(intent); // TODO: add finish here...
                finish();
            }
        });

        //show button if it's from add patient
        if (!intentType.equalsIgnoreCase("doNotNavigateFurther")) {
            findViewById(R.id.layout_button_privacy).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_button_privacy).setVisibility(View.GONE);
        }

        btn_accept_privacy.setOnClickListener(v -> {
            if (intentType.equalsIgnoreCase("doNotNavigateFurther")) {
                setResult(AppConstants.PRIVACY_POLICY_ACCEPT);
                finish();
            } else {
                showABHADialog();
            }
        });

    }

    private void showABHADialog() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_abha_creation, null);
        alertdialogBuilder.setView(convertView);
        Button positive_btn = convertView.findViewById(R.id.yes_abha_btn);
        Button create_abha_btn = convertView.findViewById(R.id.create_abha_btn);
        Button continue_without_abha_btn = convertView.findViewById(R.id.continue_without_abha_btn);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        create_abha_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            triggerTextViewDialogFragment(CreateAbhaAccountActivity.class);
        });

        continue_without_abha_btn.setOnClickListener(v -> {
            Intent intent = new Intent(context, IdentificationActivity_New.class);
            sessionManager.setCreateAbha(false);
            startActivity(intent);
            finish();
        });

        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            triggerTextViewDialogFragment(AbhaCardVerificationActivity.class);
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

    public void declinePP(View view) {
        setResult(AppConstants.PRIVACY_POLICY_DECLINE);
        finish();
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

    private void triggerTextViewDialogFragment(Class<?> activityToLaunch) {
        TextViewDialogFragment dialogFragment = new TextViewDialogFragment(
                getString(R.string.please_enter_the_patient_name),
                getString(R.string.patient_name_cannot_be_empty),
                new DialogUtils.TextViewDialogListener() {
                    @Override
                    public void onDialogActionDone(int action) {
                    }

                    @Override
                    public void onDialogActionDone(int action, String text) {
                        Intent intent = new Intent(context, activityToLaunch);
                        intent.putExtra(intentPatientNameTag, text);
                        sessionManager.setCreateAbha(true);
                        startActivity(intent);
                        finish();
                    }
                }
        );

        dialogFragment.show(getSupportFragmentManager(), TextViewDialogFragment.TAG);
    }
}