package org.intelehealth.app.ui2.onboarding;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    private boolean isPanelShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screenactivity_ui2);
        TextView tvTitle = findViewById(R.id.tv_title);
        isPanelShown = false;

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  slideUpDown();

                Intent intent = new Intent(SplashScreenActivity.this, IntroScreensActivityNew.class);
                startActivity(intent);
            }
        });

    }

    public void slideUpDown() {
        if (!isPanelShown) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(SplashScreenActivity.this
                    ,
                    R.anim.ui2_bottom_up);
            ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.layout_panel);
            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);
            isPanelShown = true;
        } else {
          /*  // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_down);

            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.INVISIBLE);
            isPanelShown = false;*/
        }
    }
}