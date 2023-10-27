package org.intelehealth.app.activities.medicationAidActivity;

import static org.intelehealth.app.database.dao.EncounterDAO.getEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.EncounterDAO.getEncounterListByVisitUUID;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.householdSurvey.model.AnswerValue;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.PatientAttributeLanguageModel;
import org.intelehealth.app.models.dispenseAdministerModel.AidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationModel;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Medication_Aid_Activity extends AppCompatActivity {
    private RecyclerView rv_medication, rv_aid;
    public final String TAG = Medication_Aid_Activity.this.getClass().getName();
    private MedicationAidAdapter med_adapter, aid_adapter;
    private Context context = Medication_Aid_Activity.this;
    private List<MedicationAidModel> med_list, aid_list;
    private TextView tvDispense, tvAdminister, tvDispenseAdminister;
    private String tag = "", medData = "", aidData = "";
    private FrameLayout fl_med, fl_aid;
    private String patientUuid, visitUuid, encounterVisitNote, encounterVitals, encounterAdultIntials,
            encounterDispense, encounterAdminister;
    private SessionManager sessionManager;
    private String appLanguage;
    private List<MedicationAidModel> update_medUuidList = new ArrayList<>();
    private List<MedicationAidModel> update_aidUuidList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_aid);

        initUI();
//        appLanguage = sessionManager.getAppLanguage();
        /*if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }*/

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
                medCheckedList.addAll(med_adapter.getFinalList());

            if (aid_adapter != null)
                aidCheckedList.addAll(aid_adapter.getFinalList());

            String encounterDisenseAdminister = "";
            if (tag.equalsIgnoreCase("dispense"))
                encounterDisenseAdminister = encounterDispense;
            else if (tag.equalsIgnoreCase("administer"))
                encounterDisenseAdminister = encounterAdminister;


            Intent intent = new Intent(context, AdministerDispenseActivity.class);
            intent.putExtra("tag", tag);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterVisitNote", encounterVisitNote);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            /*intent.putExtra("encounterDispense", encounterDispense);
            intent.putExtra("encounterAdminister", encounterAdminister);*/
            intent.putExtra("encounterDisenseAdminister", encounterDisenseAdminister);

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

        if (tag.equalsIgnoreCase("administer")) {   // Administer
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
            tvDispenseAdminister.setText(getString(R.string.administer));
            findViewById(R.id.tv_aid).setVisibility(View.GONE);
        } else {  // Dispense
            getSupportActionBar().setTitle(getString(R.string.dispense_medication_and_aid));
            fl_aid.setVisibility(View.VISIBLE);
            tvDispenseAdminister.setText(getString(R.string.dispense));
        }

        Log.d("TAG", "d/a initUI: enc dispense: " + encounterDispense + ", enc admin: " + encounterAdminister);


        med_list = new ArrayList<>();
        aid_list = new ArrayList<>();

        // medication
        try {
            med_list = ObsDAO.getObsDispenseAdministerData(encounterVisitNote, UuidDictionary.JSV_MEDICATIONS);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        if (med_list != null && med_list.size() > 0)
            fl_med.setVisibility(View.VISIBLE);
        else fl_med.setVisibility(View.GONE);


        // aid
        try {
            MedicationAidModel model = new MedicationAidModel();

            model = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_MEDICAL_EQUIP_LOAN);
            if (model.getUuid() != null || model.getValue() != null) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(model.getValue());
                patientAttributeLanguageModel.setAr(getResources().getString(R.string.aid_order_type1) + " " +
                        patientAttributeLanguageModel.getAr());
                patientAttributeLanguageModel.setEn(getResources().getString(R.string.aid_order_type1) + " " +
                        patientAttributeLanguageModel.getEn());

                Gson gson = new Gson();
                model.setValue(gson.toJson(patientAttributeLanguageModel));
                aid_list.add(model);
            }

            model = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_FREE_MEDICAL_EQUIP);
            if (model.getUuid() != null || model.getValue() != null) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(model.getValue());
                patientAttributeLanguageModel.setAr(getResources().getString(R.string.aid_order_type2) + " " +
                        patientAttributeLanguageModel.getAr());
                patientAttributeLanguageModel.setEn(getResources().getString(R.string.aid_order_type2) + " " +
                        patientAttributeLanguageModel.getEn());

                Gson gson = new Gson();
                model.setValue(gson.toJson(patientAttributeLanguageModel));
                aid_list.add(model);
            }

            model = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_COVER_MEDICAL_EXPENSE);
            if (model.getUuid() != null || model.getValue() != null) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(model.getValue());
                patientAttributeLanguageModel.setAr(getResources().getString(R.string.aid_order_type3) + " " +
                        patientAttributeLanguageModel.getAr());
                patientAttributeLanguageModel.setEn(getResources().getString(R.string.aid_order_type3) + " " +
                        patientAttributeLanguageModel.getEn());

                Gson gson = new Gson();
                model.setValue(gson.toJson(patientAttributeLanguageModel));
                aid_list.add(model);
            }

            model = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_COVER_SURGICAL_EXPENSE);
            if (model.getUuid() != null || model.getValue() != null) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(model.getValue());
                patientAttributeLanguageModel.setAr(getResources().getString(R.string.aid_order_type4) + " " +
                        patientAttributeLanguageModel.getAr());
                patientAttributeLanguageModel.setEn(getResources().getString(R.string.aid_order_type4) + " " +
                        patientAttributeLanguageModel.getEn());

                Gson gson = new Gson();
                model.setValue(gson.toJson(patientAttributeLanguageModel));
                aid_list.add(model);
            }

            model = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_CASH_ASSISTANCE);
            if (model.getUuid() != null || model.getValue() != null) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(model.getValue());
                patientAttributeLanguageModel.setAr(getResources().getString(R.string.aid_order_type5) + " " +
                        patientAttributeLanguageModel.getAr());
                patientAttributeLanguageModel.setEn(getResources().getString(R.string.aid_order_type5) + " " +
                        patientAttributeLanguageModel.getEn());

                Gson gson = new Gson();
                model.setValue(gson.toJson(patientAttributeLanguageModel));
                aid_list.add(model);
            }
        } catch (Exception e) {
            Toast.makeText(context, "something wrong.", Toast.LENGTH_SHORT).show();

        }

        if (aid_list != null && aid_list.size() > 0)
            fl_aid.setVisibility(View.VISIBLE);
        else
            fl_aid.setVisibility(View.GONE);


        // Administer
        if (tag.equalsIgnoreCase("administer")) {
            // fetch encounter administer uuid
          //  encounterAdminister = getEncounterByVisitUUID(visitUuid, UuidDictionary.ENCOUNTER_ADMINISTER);
            List<String> encounterListByVisitUUID = getEncounterListByVisitUUID(visitUuid, UuidDictionary.ENCOUNTER_ADMINISTER);
            if (encounterListByVisitUUID != null && encounterListByVisitUUID.size() > 0) {
                for (int i = 0; i < encounterListByVisitUUID.size(); i++) {
                    encounterAdminister = encounterListByVisitUUID.get(i);
                    Log.d(TAG, "encounterAdminister: " + encounterAdminister);  //
                    if (!encounterAdminister.isEmpty()) {
                        // ie. value is already present so set those values to checked for hte checkbox.
                        try {
                            //   MedicationAidModel model = ObsDAO.getObsValue(encounterAdminister, UuidDictionary.OBS_ADMINISTER_MEDICATION);
                            update_medUuidList.addAll(ObsDAO.getObsDispenseAdministerData(encounterAdminister, UuidDictionary.OBS_ADMINISTER_MEDICATION));
                            //  update_medUuidList.add(model);

                        } catch (DAOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        }
        // Dispense
        else {
            // fetch encounter dispense uuid
//            encounterDispense = getEncounterByVisitUUID(visitUuid, UuidDictionary.ENCOUNTER_DISPENSE);
            List<String> encounterListByVisitUUID = getEncounterListByVisitUUID(visitUuid, UuidDictionary.ENCOUNTER_DISPENSE);
            if (encounterListByVisitUUID != null && encounterListByVisitUUID.size() > 0) {
                for (int i = 0; i < encounterListByVisitUUID.size(); i++) {
                    encounterDispense = encounterListByVisitUUID.get(i);
                    Log.d(TAG, "encounterDispense: " + encounterDispense);  //
                    if (!encounterDispense.isEmpty()) {
                        try {

                            //  MedicationAidModel medModel = ObsDAO.getObsValue(encounterDispense, UuidDictionary.OBS_DISPENSE_MEDICATION);    // 27f6b6df-d3a5-47b6-8a36-5843ed204794
                            update_medUuidList.addAll(ObsDAO.getObsDispenseAdministerData(encounterDispense, UuidDictionary.OBS_DISPENSE_MEDICATION));    // 27f6b6df-d3a5-47b6-8a36-5843ed204794
                            // update_medUuidList.add(medModel);

                            //  MedicationAidModel aidModel = ObsDAO.getObsValue(encounterDispense, UuidDictionary.OBS_DISPENSE_AID);
                            update_aidUuidList.addAll(ObsDAO.getObsDispenseAdministerData(encounterDispense, UuidDictionary.OBS_DISPENSE_AID));
                            //   update_aidUuidList.add(aidModel);


                        } catch (DAOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }

            }
        }

        if (tag.equalsIgnoreCase("dispense")) {
            RecyclerView.LayoutManager med_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv_medication.setLayoutManager(med_LayoutManager);
            rv_medication.setNestedScrollingEnabled(false);
            med_adapter = new MedicationAidAdapter(context, med_list, LocaleHelper.isArabic(context), update_medUuidList, "dispense");
            rv_medication.setAdapter(med_adapter);

            RecyclerView.LayoutManager aid_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv_aid.setLayoutManager(aid_LayoutManager);
            rv_aid.setNestedScrollingEnabled(false);
            aid_adapter = new MedicationAidAdapter(context, aid_list, LocaleHelper.isArabic(context), update_aidUuidList, "dispense");
            rv_aid.setAdapter(aid_adapter);
        }
        else if (tag.equalsIgnoreCase("administer")) {
            RecyclerView.LayoutManager med_LayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv_medication.setLayoutManager(med_LayoutManager);
            rv_medication.setNestedScrollingEnabled(false);
            med_adapter = new MedicationAidAdapter(context, med_list, LocaleHelper.isArabic(context), update_medUuidList, "administer");
            rv_medication.setAdapter(med_adapter);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    private PatientAttributeLanguageModel getPatientAttributeFromJSON(String jsonString) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(jsonString, PatientAttributeLanguageModel.class);
    }

}