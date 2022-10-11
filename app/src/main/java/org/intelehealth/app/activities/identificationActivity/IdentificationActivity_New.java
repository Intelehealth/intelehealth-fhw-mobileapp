package org.intelehealth.app.activities.identificationActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeFragment_New;
import org.intelehealth.app.utilities.CustomEditText;

public class IdentificationActivity_New extends AppCompatActivity {
    Button btn_nxt_firstscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        btn_nxt_firstscreen = findViewById(R.id.btn_nxt_firstscreen);
        btn_nxt_firstscreen.setOnClickListener(v -> {

        });
    }
}