package org.intelehealth.app.activities.medicationAidActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Medication_Aid_Activity extends AppCompatActivity {
    private RecyclerView rv_medication, rv_aid;
    private MedicationAidAdapter med_adapter, aid_adapter;
    private Context context = Medication_Aid_Activity.this;
    private List<MedicationAidModel> med_list, aid_list;
    private TextView tvDispense, tvAdminister, tvDispenseAdminister;
    private String tag = "", medData = "", aidData = "";
    private FrameLayout fl_med, fl_aid;
    private String patientUuid, visitUuid, encounterVisitNote, encounterVitals, encounterAdultIntials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_aid);

        initUI();

        tvDispenseAdminister.setOnClickListener(v -> {

            if (tag.equalsIgnoreCase("dispense")) {
                if (med_adapter != null && med_adapter.getFinalList().size() < 1 &&
                        aid_adapter != null && aid_adapter.getFinalList().size() < 1) {   // ie. 0 or < 0
                    Toast.makeText(context, getString(R.string.select_at_least_one_item_to_proceed), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (tag.equalsIgnoreCase("administer")) {
                if (med_adapter != null && med_adapter.getFinalList().size() < 1) {   // ie. 0 or < 0
                    Toast.makeText(context, getString(R.string.select_at_least_one_item_to_proceed), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            List<MedicationAidModel> medCheckedList = new ArrayList<>();
            List<MedicationAidModel> aidCheckedList = new ArrayList<>();
            if (med_adapter != null)
                medCheckedList = med_adapter.getFinalList();

            if (aid_adapter != null)
                aidCheckedList = aid_adapter.getFinalList();

            Intent intent = new Intent(context, AdministerDispenseActivity.class);
            intent.putExtra("tag", tag);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterVisitNote", encounterVisitNote);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);

            if (medCheckedList.size() > 0)
                intent.putExtra("med", (Serializable) medCheckedList);
            if (aidCheckedList.size() > 0)
                intent.putExtra("aid", (Serializable) aidCheckedList);

            startActivity(intent);
            Log.d("TAG", " 1st screen: onCreate: " + tag);
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
      //  rv_medication.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        rv_aid = findViewById(R.id.rv_aid);
      //  rv_aid.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        fl_med = findViewById(R.id.fl_med);
        fl_aid = findViewById(R.id.fl_aid);
        tvDispenseAdminister = findViewById(R.id.tvDispenseAdminister);
        tvDispense = findViewById(R.id.tvDispense);
        tvAdminister = findViewById(R.id.tvAdminister);

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        medData = intent.getStringExtra("medicineData");
        aidData = intent.getStringExtra("aidData");
        patientUuid = intent.getStringExtra("patientUuid");
        visitUuid = intent.getStringExtra("visitUuid");
        encounterVisitNote = intent.getStringExtra("encounterVisitNote");
        encounterVitals = intent.getStringExtra("encounterUuidVitals");
        encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

        if (tag.equalsIgnoreCase("administer")) {
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
            tvDispenseAdminister.setText(getString(R.string.administer));
        }
        else {
            getSupportActionBar().setTitle(getString(R.string.dispense_medication_and_aid));
            fl_aid.setVisibility(View.VISIBLE);
            tvDispenseAdminister.setText(getString(R.string.dispense));
        }


        med_list = new ArrayList<>();
        aid_list = new ArrayList<>();

        if (medData != null && !medData.trim().isEmpty()) {
            fl_med.setVisibility(View.VISIBLE);
            ArrayList<String> list = new ArrayList<>(Arrays.asList(medData.split("\n")));
            ArrayList<MedicationAidModel> mm = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                MedicationAidModel a = new MedicationAidModel();
                a.setValue(list.get(i));
                mm.add(a);
            }

            for (MedicationAidModel med: mm) {
                if (!med.getValue().isEmpty())
                    med_list.add(med);
            }
        }
        else fl_med.setVisibility(View.GONE);

        if (aidData != null && !aidData.trim().isEmpty()) {
            fl_aid.setVisibility(View.VISIBLE);
            ArrayList<String> list = new ArrayList<>(Arrays.asList(aidData.split("\n")));
            ArrayList<MedicationAidModel> aa = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                MedicationAidModel a = new MedicationAidModel();
                a.setValue(list.get(i));
                aa.add(a);
            }

                for (MedicationAidModel aid: aa) {
                    if (!aid.getValue().isEmpty())
                        aid_list.add(aid);
            }

        }
        else fl_aid.setVisibility(View.GONE);

       /* med_list.add("Crocin");
        med_list.add("Albendazol");
        med_list.add("Vicks");
        med_list.add("ABC");
        med_list.add("Cloptin");
        med_list.add("Mediocrin");
*/

        /*aid_list.add("Type 1: Give 10,000 rs loan");
        aid_list.add("Type 2: Give medicines for free");
        aid_list.add("Type 3: Cost of amount given.");
        aid_list.add("Type 4: Cover Surgical Expenses.");
        aid_list.add("Type 5: Cash Assitance given.");*/

        RecyclerView.LayoutManager med_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        rv_medication.setLayoutManager(med_LayoutManager);
        rv_medication.setNestedScrollingEnabled(false);
        med_adapter = new MedicationAidAdapter(context, med_list);
        rv_medication.setAdapter(med_adapter);

        if (tag.equalsIgnoreCase("dispense")) {
            RecyclerView.LayoutManager aid_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv_aid.setLayoutManager(aid_LayoutManager);
            rv_aid.setNestedScrollingEnabled(false);
            aid_adapter = new MedicationAidAdapter(context, aid_list);
            rv_aid.setAdapter(aid_adapter);
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