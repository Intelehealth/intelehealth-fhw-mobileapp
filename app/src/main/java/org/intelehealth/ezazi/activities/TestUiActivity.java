package org.intelehealth.ezazi.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.intelehealth.ezazi.R;
import org.w3c.dom.Text;

public class TestUiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test_uiactivity);
        super.onCreate(savedInstanceState);

        TextView view1 = findViewById(R.id.view1);
        TextView view2 = findViewById(R.id.view2);
        TextView textView1 = findViewById(R.id.tv_admission_date_error);
        TextView textView2 = findViewById(R.id.tv_admission_time_error);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView1.setVisibility(View.GONE);
            }
        });
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView2.setVisibility(View.GONE);
            }
        });

    }
}
