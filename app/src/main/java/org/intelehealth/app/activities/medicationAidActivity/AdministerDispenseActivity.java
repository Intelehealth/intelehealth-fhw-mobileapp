package org.intelehealth.app.activities.medicationAidActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.R;

public class AdministerDispenseActivity extends AppCompatActivity {
    private TextInputEditText tie_medNotes, tie_aidNotes;
    private String tag = "";
    private FrameLayout fl_aid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_dispense);

        initUI();

        tie_medNotes.setHint(getString(R.string.enter_details_here));
        tie_aidNotes.setHint(getString(R.string.enter_details_here));

        tie_medNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tie_medNotes.setHint("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equalsIgnoreCase(""))
                    tie_medNotes.setHint(getString(R.string.enter_details_here));
                else
                    tie_medNotes.setHint("");
            }
        });

        tie_aidNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tie_aidNotes.setHint("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equalsIgnoreCase(""))
                    tie_aidNotes.setHint(getString(R.string.enter_details_here));
                else
                    tie_aidNotes.setHint("");
            }
        });
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        tie_medNotes = findViewById(R.id.tie_medNotes);
        tie_aidNotes = findViewById(R.id.tie_aidNotes);
        fl_aid = findViewById(R.id.fl_aid);

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        if (tag.equalsIgnoreCase("administer")) {
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
        }
        else {
            getSupportActionBar().setTitle(getString(R.string.dispense_medication_and_aid));
            fl_aid.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}