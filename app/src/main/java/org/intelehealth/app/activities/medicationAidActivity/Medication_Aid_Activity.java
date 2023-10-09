package org.intelehealth.app.activities.medicationAidActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.prescription.PrescriptionActivity;

import java.util.ArrayList;
import java.util.List;

public class Medication_Aid_Activity extends AppCompatActivity {
    private RecyclerView rv_medication, rv_aid;
    private MedicationAidAdapter med_adapter, aid_adapter;
    private Context context = Medication_Aid_Activity.this;
    private List<String> med_list, aid_list;
    private TextView tvDispense, tvAdminister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_aid);

        initUI();

        tvDispense.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdministerDispenseActivity.class);
            startActivity(intent);
        });
        tvAdminister.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdministerDispenseActivity.class);
            startActivity(intent);
        });

    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        rv_medication = findViewById(R.id.rv_medication);
        rv_aid = findViewById(R.id.rv_aid);
        tvDispense = findViewById(R.id.tvDispense);
        tvAdminister = findViewById(R.id.tvAdminister);

        med_list = new ArrayList<>();
        med_list.add("Crocin");
        med_list.add("Albendazol");
        med_list.add("Vicks");
        med_list.add("ABC");
        med_list.add("Cloptin");
        med_list.add("Mediocrin");

        aid_list = new ArrayList<>();
        aid_list.add("Type 1: Give 10,000 rs loan");
        aid_list.add("Type 2: Give medicines for free");
        aid_list.add("Type 3: Cost of amount given.");
        aid_list.add("Type 4: Cover Surgical Expenses.");
        aid_list.add("Type 5: Cash Assitance given.");

        RecyclerView.LayoutManager med_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        rv_medication.setLayoutManager(med_LayoutManager);
        rv_medication.setNestedScrollingEnabled(false);
        med_adapter = new MedicationAidAdapter(context, med_list);
        rv_medication.setAdapter(med_adapter);

        RecyclerView.LayoutManager aid_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        rv_aid.setLayoutManager(aid_LayoutManager);
        rv_aid.setNestedScrollingEnabled(false);
        aid_adapter = new MedicationAidAdapter(context, aid_list);
        rv_aid.setAdapter(aid_adapter);
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