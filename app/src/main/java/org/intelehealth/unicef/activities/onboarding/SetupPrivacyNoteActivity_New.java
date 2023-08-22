package org.intelehealth.unicef.activities.onboarding;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.BaseActivity;
import org.intelehealth.unicef.activities.chooseLanguageActivity.SplashScreenActivity;
import org.intelehealth.unicef.activities.setupActivity.SetupActivityNew;
import org.intelehealth.unicef.app.AppConstants;

public class SetupPrivacyNoteActivity_New extends BaseActivity {
    private static final String TAG = "SetupPrivacyNoteActivit";
    TextView tvTermsAndPrivacy;
    CustomDialog customDialog;
    private CardView cardNoteSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_privacy_note_ui2);
        //tvTermsAndPrivacy = findViewById(R.id.tv_privacy_notice_link);
        Button btnSetup = findViewById(R.id.btn_setup);
        cardNoteSnack = findViewById(R.id.card_note_snack_policy);

        TextView tcTextView = (TextView) findViewById(R.id.tv_privacy_notice_link_2);
        tcTextView.setPaintFlags(tcTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView ppTextView = (TextView) findViewById(R.id.tv_privacy_notice_link_4);
        ppTextView.setPaintFlags(ppTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        ImageView ivBack = findViewById(R.id.iv_setup_privacy_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupPrivacyNoteActivity_New.this, SplashScreenActivity.class);
                startActivity(intent);
            }
        });


        btnSetup.getBackground().setAlpha(60);
        CheckBox chkBoxPrivacyPolicy = findViewById(R.id.checkbox_privacy_policy);
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

        ClickableSpan teremsAndCondition = new ClickableSpan() {
            @Override
            public void onClick(View textView) {


                Intent mIntent = new Intent(SetupPrivacyNoteActivity_New.this, TermsAndConditionsActivity_New.class);
                mIntent.putExtra("isTermsAndCondition", true);
                startActivity(mIntent);
                //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);


            }
        };

        // Character starting from 9 - 27 is Terms and condition.
        // Character starting from 32 - 46 is privacy policy.

        ClickableSpan privacy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                Intent mIntent = new Intent(SetupPrivacyNoteActivity_New.this, PrivacyPolicyActivity_New.class);
                mIntent.putExtra("isPrivacyPolicy", true);
                startActivity(mIntent);
                //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);

            }
        };

        SpanString.setSpan(teremsAndCondition, 9, 27, 0);
        SpanString.setSpan(privacy, 32, 46, 0);
        SpanString.setSpan(new ForegroundColorSpan(Color.BLUE), 9, 27, 0);
        SpanString.setSpan(new ForegroundColorSpan(Color.BLUE), 32, 46, 0);
        SpanString.setSpan(new UnderlineSpan(), 9, 27, 0);
        SpanString.setSpan(new UnderlineSpan(), 32, 46, 0);

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
}