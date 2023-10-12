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
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.MedicationAidModel;

import java.util.ArrayList;
import java.util.List;

public class AdministerDispenseActivity extends AppCompatActivity {
    private TextInputEditText tie_medNotes, tie_aidNotes;
    private TextView tv_medData, tv_aidData;
    private String tag = "";
    private FrameLayout fl_aid;
    private List<MedicationAidModel> medList, aidList;


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
        tv_medData = findViewById(R.id.tv_medData);
        tv_aidData = findViewById(R.id.tv_aidData);
        fl_aid = findViewById(R.id.fl_aid);
        medList = new ArrayList<>();
        aidList = new ArrayList<>();

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        medList = (List<MedicationAidModel>) intent.getSerializableExtra("med");
        aidList = (List<MedicationAidModel>) intent.getSerializableExtra("aid");    // null on empty.

        if (medList != null && medList.size() > 0) {
            String medData = "";
            for (MedicationAidModel med : medList) {
                medData = medData + (Node.bullet + " " + med.getValue()) + "\n\n";
            }
            tv_medData.setText(medData.substring(0, medData.length() - 2));
        }

        if (tag.equalsIgnoreCase("administer")) {
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
        }
        else {  // ie. dispense
            getSupportActionBar().setTitle(getString(R.string.dispense_medication_and_aid));
            fl_aid.setVisibility(View.VISIBLE);

            if (aidList != null && aidList.size() > 0) {
                String aidData = "";
                for (MedicationAidModel aid : aidList) {
                    aidData = aidData + (Node.bullet + " " + aid.getValue()) + "\n\n";
                }
                tv_aidData.setText(aidData.substring(0, aidData.length() - 2));
            }
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