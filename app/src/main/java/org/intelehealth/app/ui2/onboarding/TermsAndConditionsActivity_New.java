package org.intelehealth.app.ui2.onboarding;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class TermsAndConditionsActivity_New extends AppCompatActivity {
    private static final String TAG = "TermsAndConditionsActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions_ui2);

        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermsAndConditionsActivity_New.this, SetupPrivacyNoteActivity_New.class);
                startActivity(intent);
            }
        });
        TextView tvText = findViewById(R.id.tv_terms_conditions);
        JSONObject obj = null;
        try {
            obj = new JSONObject(Objects.requireNonNullElse(
                    FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                    String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            String privacy_string = obj.getString("terms_and_conditions");
            tvText.setText(HtmlCompat.fromHtml(privacy_string, HtmlCompat.FROM_HTML_MODE_LEGACY));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }
}