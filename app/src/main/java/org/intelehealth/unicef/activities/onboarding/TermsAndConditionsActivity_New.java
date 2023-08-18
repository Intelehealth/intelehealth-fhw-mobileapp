package org.intelehealth.unicef.activities.onboarding;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.utilities.FileUtils;
import org.intelehealth.unicef.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TermsAndConditionsActivity_New extends AppCompatActivity {
    private static final String TAG = "TermsAndConditionsActiv";
    private int mIntentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions_ui2);
        mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }


        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvText1 = findViewById(R.id.tv_term_condition_1);
        TextView tvText2 = findViewById(R.id.tv_term_condition_2);
        JSONObject obj = null;
        try {
            obj = new JSONObject(Objects.requireNonNullElse(
                    FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                    String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file

            String privacy_string_1 = "";
            String privacy_string_2 = "";

            if (new SessionManager(this).getAppLanguage().equalsIgnoreCase("ru")) {
                privacy_string_1 = obj.getString("terms_and_conditions_1_ru");
                privacy_string_2 = obj.getString("terms_and_conditions_2_ru");
            } else {
                privacy_string_1 = obj.getString("terms_and_conditions_1");
                privacy_string_2 = obj.getString("terms_and_conditions_2");
            }


            tvText1.setText(HtmlCompat.fromHtml(privacy_string_1, HtmlCompat.FROM_HTML_MODE_LEGACY));
            tvText2.setText(HtmlCompat.fromHtml(privacy_string_2, HtmlCompat.FROM_HTML_MODE_LEGACY));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

    public void declineTC(View view) {
        setResult(AppConstants.TERMS_CONDITIONS_DECLINE);
        finish();
    }

    public void acceptPP(View view) {
        setResult(AppConstants.TERMS_CONDITIONS_ACCEPT);
        finish();
    }
}