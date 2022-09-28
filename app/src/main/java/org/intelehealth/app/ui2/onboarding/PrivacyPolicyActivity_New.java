package org.intelehealth.app.ui2.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class PrivacyPolicyActivity_New extends AppCompatActivity {
    private static final String TAG = "PrivacyPolicyActivityNe";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_new_ui2);
        ImageView ivBack = findViewById(R.id.iv_back_arrow_privacy);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivacyPolicyActivity_New.this, SetupPrivacyNoteActivity_New.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

}