package org.intelehealth.app.ui2.onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import org.intelehealth.app.R;

public class SetupPrivacyNoteActivity extends AppCompatActivity {
    private static final String TAG = "SetupPrivacyNoteActivit";
    TextView tvTermsAndPrivacy;

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
                showDialogForInternetConnection();
            }
        });

    }

    public void termsAndPrivacyPolicy() {
        SpannableString SpanString = new SpannableString(getResources().getString(R.string.agree_to_terms));

        ClickableSpan teremsAndCondition = new ClickableSpan() {
            @Override
            public void onClick(View textView) {


                Intent mIntent = new Intent(SetupPrivacyNoteActivity.this, TermsAndConditionsActivityNew.class);
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

                Intent mIntent = new Intent(SetupPrivacyNoteActivity.this, PrivacyPolicyActivityNew.class);
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
    private void showDialogForInternetConnection() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final View customLayout = getLayoutInflater().inflate(R.layout.ui2_layout_dialog_internet_warning, null);
        builder.setView(customLayout);

        //EditText etTask = customLayout.findViewById(R.id.et_task_reminder);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialog.show();
    }

}