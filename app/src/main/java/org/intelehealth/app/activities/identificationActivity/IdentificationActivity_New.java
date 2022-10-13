package org.intelehealth.app.activities.identificationActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeFragment_New;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.utilities.CustomEditText;

/**
 * Created by Prajwal Waingankar on 29/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class IdentificationActivity_New extends AppCompatActivity {
   // ActivityIdentificationNewBinding binding;
    Button nxt_btn_main, btn_back_firstscreen, btn_nxt_firstscreen;
    RelativeLayout relativeLayout;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = ActivityIdentificationNewBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        setContentView(R.layout.activity_identification_new);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

      /*  getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_firstscreen, new HomeFragment_New())
                .commit();*/
        // TODO: update later correctly. create fragment_regs_firstscren class extends fragment

        initUI();

        nxt_btn_main.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_SecondScreen())
                    .commit();

            relativeLayout.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        });

        btn_back_firstscreen.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                    .commit();

            relativeLayout.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
        });

        btn_nxt_firstscreen.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_ThirdScreen())
                    .commit();

//            Fragment f = new Fragment_ThirdScreen();
//            if (f.isResumed()) {
//                Intent intent = new Intent(IdentificationActivity_New.this, PatientDetailActivity2.class);
//                startActivity(intent);
//            }

//            Fragment_ThirdScreen test = (Fragment_ThirdScreen) getSupportFragmentManager().findFragmentByTag("thirdsc");
//            if (test != null && test.isVisible()) {
//                //DO STUFF
//                Intent intent = new Intent(IdentificationActivity_New.this, PatientDetailActivity2.class);
//                startActivity(intent);
//            }
//            else {
//                Log.v("", "d");
//                //Whatever
//            }


//            relativeLayout.setVisibility(View.VISIBLE);
//            linearLayout.setVisibility(View.GONE);
        });


    }

    private void initUI() {
        nxt_btn_main = findViewById(R.id.nxt_btn_main);
        btn_back_firstscreen = findViewById(R.id.btn_back_firstscreen);
        btn_nxt_firstscreen = findViewById(R.id.btn_nxt_firstscreen);
        relativeLayout = findViewById(R.id.relative_firstscreen_nxt_btn);
        linearLayout = findViewById(R.id.linear_secondscreen_nxt_btn);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                .commit();
    }
}