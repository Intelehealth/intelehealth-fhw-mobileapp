package org.intelehealth.app.ui2.onboarding;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;

public class SetupPrivacyNoteActivity_New extends AppCompatActivity {
    private static final String TAG = "SetupPrivacyNoteActivit";
    TextView tvTermsAndPrivacy;
    CustomDialog customDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_privacy_note_ui2);
        tvTermsAndPrivacy = findViewById(R.id.tv_privacy_notice_link);
        Button btnSetup = findViewById(R.id.btn_setup);
        btnSetup.getBackground().setAlpha(60);
        CheckBox chkBoxPrivacyPolicy = findViewById(R.id.checkbox_privacy_policy);


        chkBoxPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    chkBoxPrivacyPolicy.setButtonDrawable(getResources().getDrawable(R.drawable.ui2_ic_checkbox_checked));
                    btnSetup.setBackground(getResources().getDrawable(R.drawable.ui2_common_primary_bg));

                } else {
                    btnSetup.getBackground().setAlpha(60);
                    chkBoxPrivacyPolicy.setButtonDrawable(getResources().getDrawable(R.drawable.ui2_ic_default_checkbox));

                }
            }
        });


        if (chkBoxPrivacyPolicy.isChecked()) {
            btnSetup.getBackground().setAlpha(0);

        } else {
            btnSetup.getBackground().setAlpha(60);

        }

        tvTermsAndPrivacy
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        termsAndPrivacyPolicy();

                    }
                });

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog = new CustomDialog(SetupPrivacyNoteActivity_New.this);
                customDialog.showDialog1();
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
                overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);


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
                overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);

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
    }


    class CustomDialog extends DialogFragment {
        Context context;

        public CustomDialog(Context context) {
            this.context = context;
        }

        public void showDialog1() {
            AlertDialog.Builder builder
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

                }
            });
        }
    }
}