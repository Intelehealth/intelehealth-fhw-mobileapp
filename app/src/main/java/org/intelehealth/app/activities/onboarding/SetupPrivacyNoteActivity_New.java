package org.intelehealth.app.activities.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.chooseLanguageActivity.SplashScreenActivity;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class SetupPrivacyNoteActivity_New extends AppCompatActivity {
    private static final String TAG = "SetupPrivacyNoteActivit";
    TextView tvTermsAndPrivacy;
    CustomDialog customDialog;
    private CardView cardNoteSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_privacy_note_ui2);
        tvTermsAndPrivacy = findViewById(R.id.setupAyuIntroAcceptTCandPPTextView);
        termsAndPrivacyPolicy();
        Button btnSetup = findViewById(R.id.setupAyuIntroSetupBtn);
        cardNoteSnack = findViewById(R.id.setupAyuIntroAcceptTCandPPSnackBar);
        ImageView ivBack = findViewById(R.id.setupAyuIntroBackArrowImageView);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupPrivacyNoteActivity_New.this, SplashScreenActivity.class);
                startActivity(intent);
            }
        });
        btnSetup.getBackground().setAlpha(60);
        CheckBox chkBoxPrivacyPolicy = findViewById(R.id.setupAyuIntroAcceptTCandPPCheckBox);
        btnSetup.setEnabled(false);

        chkBoxPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    chkBoxPrivacyPolicy.setButtonDrawable(getResources().getDrawable(R.drawable.ui2_ic_checkbox_checked));
                    btnSetup.setBackground(getResources().getDrawable(R.drawable.ui2_common_primary_bg));
                    btnSetup.setEnabled(true);
                } else {
                    btnSetup.getBackground().setAlpha(60);
                    chkBoxPrivacyPolicy.setButtonDrawable(getResources().getDrawable(R.drawable.ui2_ic_default_checkbox));
                    btnSetup.setEnabled(false);
                }
            }
        });


        if (chkBoxPrivacyPolicy.isChecked()) {
            btnSetup.getBackground().setAlpha(0);

        } else {
            btnSetup.getBackground().setAlpha(60);

        }

       /* tvTermsAndPrivacy
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        termsAndPrivacyPolicy();

                    }
                });*/

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkBoxPrivacyPolicy.isChecked()) {

                    btnSetup.setBackgroundDrawable(ContextCompat.getDrawable(SetupPrivacyNoteActivity_New.this, R.drawable.ui2_common_primary_bg));
                    customDialog = new CustomDialog(SetupPrivacyNoteActivity_New.this);
                    customDialog.showDialog1();
                } else {
                    showSnackBarAndRemoveLater();
                }

            }
        });

    }

    public void termsAndPrivacyPolicy() {
        SpannableString SpanString = new SpannableString(getResources().getString(R.string.agree_to_terms));
        ClickableSpan termsAndCondition = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                Intent mIntent = new Intent(SetupPrivacyNoteActivity_New.this, TermsAndConditionsActivity_New.class);
                mIntent.putExtra("isTermsAndCondition", true);
                startActivity(mIntent);
                //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);


            }
        };
        ClickableSpan privacy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent mIntent = new Intent(SetupPrivacyNoteActivity_New.this, PrivacyPolicyActivity_New.class);
                mIntent.putExtra("intentType", "doNotNavigateFurther");
                mIntent.putExtra("isPrivacyPolicy", true);
                startActivity(mIntent);
            }
        };

        int startingPositionTC = getResources().getString(R.string.agree_to_terms).indexOf(getResources().getString(R.string.terms_and_conditions));
        int endingPositionTC = startingPositionTC + getResources().getString(R.string.terms_and_conditions).length();
        int startingPositionPP = getResources().getString(R.string.agree_to_terms).indexOf(getResources().getString(R.string.privacy_policy));
        int endingPositionPP = startingPositionPP + getResources().getString(R.string.privacy_policy).length();
        SpanString.setSpan(termsAndCondition, startingPositionTC, endingPositionTC, 0);
        SpanString.setSpan(privacy, startingPositionPP, endingPositionPP, 0);
        /*SpanString.setSpan(new ForegroundColorSpan(Color.BLUE), 9, 27, 0);
        SpanString.setSpan(new ForegroundColorSpan(Color.BLUE), 32, 46, 0);
        SpanString.setSpan(new UnderlineSpan(), 9, 27, 0);
        SpanString.setSpan(new UnderlineSpan(), 32, 46, 0);*/

        tvTermsAndPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        tvTermsAndPrivacy.setText(SpanString, TextView.BufferType.SPANNABLE);
        tvTermsAndPrivacy.setSelected(true);
        tvTermsAndPrivacy.setClickable(true);
    }

    public void openPrivacyPolicy(View view) {
        Intent mIntent = new Intent(SetupPrivacyNoteActivity_New.this, PrivacyPolicyActivity_New.class);
        mIntent.putExtra("isPrivacyPolicy", true);
        mIntent.putExtra("IntentFrom", AppConstants.INTENT_FROM_AYU_FOR_SETUP);
        mStartForResultTCPP.launch(mIntent);
    }

    public void openTermsConditions(View view) {
        Intent mIntent = new Intent(SetupPrivacyNoteActivity_New.this, TermsAndConditionsActivity_New.class);
        mIntent.putExtra("isTermsAndCondition", true);
        mIntent.putExtra("IntentFrom", AppConstants.INTENT_FROM_AYU_FOR_SETUP);
        mStartForResultTCPP.launch(mIntent);
    }

    ActivityResultLauncher<Intent> mStartForResultTCPP = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppConstants.TERMS_CONDITIONS_ACCEPT || result.getResultCode() == AppConstants.PRIVACY_POLICY_ACCEPT) {
                        //Intent intent = result.getData();
                        // Handle the Intent
                        customDialog = new CustomDialog(SetupPrivacyNoteActivity_New.this);
                        customDialog.showDialog1();
                    } else if (result.getResultCode() == AppConstants.TERMS_CONDITIONS_DECLINE || result.getResultCode() == AppConstants.PRIVACY_POLICY_DECLINE) {
                        //Intent intent = result.getData();
                        // Handle the Intent
                        finish();
                    } else {

                    }
                }
            });


    class CustomDialog extends DialogFragment {
        Context context;

        public CustomDialog(Context context) {
            this.context = context;
        }

        public void showDialog1() {
            /*AlertDialog.Builder builder
                    = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            LayoutInflater inflater = LayoutInflater.from(context);
            View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_internet_warning, null);
            builder.setView(customLayout);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
            dialog.show();
            int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);

            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            Button btnOkay = customLayout.findViewById(R.id.btn_okay);
            btnOkay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(SetupPrivacyNoteActivity_New.this, SetupActivityNew.class);
                    context.startActivity(intent);
                    finish();
                }
            });*/
            Intent intent = new Intent(SetupPrivacyNoteActivity_New.this, SetupActivityNew.class);
            context.startActivity(intent);
            finish();
        }
    }

    private void showSnackBarAndRemoveLater() {
        cardNoteSnack.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cardNoteSnack.setVisibility(View.GONE);
            }
        }, 3000);
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
}