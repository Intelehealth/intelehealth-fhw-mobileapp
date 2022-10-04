package org.intelehealth.app.activities.identificationActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeFragment_New;
import org.intelehealth.app.utilities.CustomEditText;

public class IdentificationActivity_New extends AppCompatActivity {
    ImageButton btn_nxt_firstscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification_new);

      /*  getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_firstscreen, new HomeFragment_New())
                .commit();*/
        // TODO: update later correctly. create fragment_regs_firstscren class extends fragment

        btn_nxt_firstscreen = findViewById(R.id.btn_nxt_firstscreen);
        btn_nxt_firstscreen.setOnClickListener(v -> {

        });
    }
}