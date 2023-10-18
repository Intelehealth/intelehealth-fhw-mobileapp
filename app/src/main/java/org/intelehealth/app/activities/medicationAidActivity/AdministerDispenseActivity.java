package org.intelehealth.app.activities.medicationAidActivity;

import static org.intelehealth.app.utilities.UuidDictionary.OBS_ADMINISTER_MEDICATION;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_DISPENSE_AID;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_DISPENSE_MEDICATION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentsActivity;
import org.intelehealth.app.activities.visitSummaryActivity.HorizontalAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.PatientAttributeLanguageModel;
import org.intelehealth.app.models.dispenseAdministerModel.AidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdministerDispenseActivity extends AppCompatActivity {
    private TextInputEditText tie_medNotes, tie_aidNotes,
            tie_totalCost, tie_vendorDiscount, tie_coveredCost, tie_outOfPocket, tie_others;
    private TextView tv_medData, tv_aidData, tvSave;
    private String tag = "";
    private FrameLayout fl_med, fl_aid;
    private List<MedicationAidModel> medList, aidList;
    private ImageButton imgbtn_uploadDocs;
    private Context context;
    private RecyclerView rv_docs;
    private RecyclerView.LayoutManager docsLayoutManager;
    private String patientUuid, visitUuid, encounterVisitNote, encounterVitals, encounterAdultIntials,
            encounterDispense = "", encounterAdminister = "";
    private ObsDTO obsDTOMedication, obsDTOAid;
//    private List<ObsDTO> obsDTOList_Medication, obsDTOList_Aid;

    private SessionManager sessionManager;
    private MedicationModel medModel = new MedicationModel();
    private AidModel aidModel = new AidModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_dispense);

        context = AdministerDispenseActivity.this;
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

        sessionManager = new SessionManager(context);
        tie_medNotes = findViewById(R.id.tie_medNotes);
        tie_aidNotes = findViewById(R.id.tie_aidNotes);

        tie_totalCost = findViewById(R.id.tie_totalCost);
        tie_vendorDiscount = findViewById(R.id.tie_vendorDiscount);
        tie_coveredCost = findViewById(R.id.tie_coveredCost);
        tie_outOfPocket = findViewById(R.id.tie_outOfPocket);
        tie_others = findViewById(R.id.tie_others);

        tv_medData = findViewById(R.id.tv_medData);
        tv_aidData = findViewById(R.id.tv_aidData);

        fl_aid = findViewById(R.id.fl_aid);
        fl_med = findViewById(R.id.fl_med);

        imgbtn_uploadDocs = findViewById(R.id.imgbtn_uploadDocs);

        rv_docs = findViewById(R.id.rv_docs);
        tvSave = findViewById(R.id.tvSave);

        medList = new ArrayList<>();
        aidList = new ArrayList<>();

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        patientUuid = intent.getStringExtra("patientUuid");
        visitUuid = intent.getStringExtra("visitUuid");
        encounterVisitNote = intent.getStringExtra("encounterVisitNote");
        encounterVitals = intent.getStringExtra("encounterUuidVitals");
        encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

        medList = (List<MedicationAidModel>) intent.getSerializableExtra("med");
        aidList = (List<MedicationAidModel>) intent.getSerializableExtra("aid");    // null on empty.

        // fetched medication values from local db.
       /* try {
            obsDTOList_Medication = ObsDAO.getObsDispenseAdministerData(encounterVisitNote, UuidDictionary.JSV_MEDICATIONS);

            obsDTOList_Aid = new ArrayList<>();
            String value = "";

            obsDTOAid = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_MEDICAL_EQUIP_LOAN);
            if (obsDTOAid != null)
                obsDTOList_Aid.add(ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_MEDICAL_EQUIP_LOAN));

            obsDTOAid = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_FREE_MEDICAL_EQUIP);
            if (obsDTOAid != null)
                obsDTOList_Aid.add(ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_FREE_MEDICAL_EQUIP));

            obsDTOAid = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_COVER_MEDICAL_EXPENSE);
            if (obsDTOAid != null)
                obsDTOList_Aid.add(ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_COVER_MEDICAL_EXPENSE));

            obsDTOAid = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_COVER_SURGICAL_EXPENSE);
            if (obsDTOAid != null)
                obsDTOList_Aid.add(ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_COVER_SURGICAL_EXPENSE));

            obsDTOAid = ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_CASH_ASSISTANCE);
            if (obsDTOAid != null)
                obsDTOList_Aid.add(ObsDAO.getObsValue(encounterVisitNote, UuidDictionary.AID_ORDER_CASH_ASSISTANCE));

        } catch (DAOException e) {
            throw new RuntimeException(e);
        }*/

        setImagesToRV();    // TODO: handle this later with new concept id for UPLOAD_DOCS obs.
        // TODO: here max 4 images will only come.

        if (medList != null && medList.size() > 0) {
            fl_med.setVisibility(View.VISIBLE);
            String medData = "";
            for (MedicationAidModel med : medList) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(med.getValue());
                String value = "";
                if (LocaleHelper.isArabic(context))
                    value = patientAttributeLanguageModel.getAr().replaceAll("\n", "");
                else
                    value = patientAttributeLanguageModel.getEn().replaceAll("\n", "");

                medData = medData + (Node.bullet + " " + value) + "\n\n";
            }

            tv_medData.setText(medData.substring(0, medData.length() - 2));

        }
        else fl_med.setVisibility(View.GONE);

        if (tag.equalsIgnoreCase("administer")) {
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
        }
        else {  // ie. dispense
            getSupportActionBar().setTitle(getString(R.string.dispense_medication_and_aid));

            if (aidList != null && aidList.size() > 0) {
                fl_aid.setVisibility(View.VISIBLE);
                String aidData = "";
                for (MedicationAidModel aid : aidList) {
;
                    PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(aid.getValue());
                    String value = "";
                    if (LocaleHelper.isArabic(context))
                        value = patientAttributeLanguageModel.getAr().replaceAll("\n", "");
                    else
                        value = patientAttributeLanguageModel.getEn().replaceAll("\n", "");

                    aidData = aidData + (Node.bullet + " " + value) + "\n\n";
                }
                tv_aidData.setText(aidData.substring(0, aidData.length() - 2));
            }
            else fl_aid.setVisibility(View.GONE);

        }

        // Edit Text - start
        tie_totalCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_totalCost.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });
        tie_vendorDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_vendorDiscount.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });
        tie_coveredCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_coveredCost.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });

        tie_outOfPocket.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_outOfPocket.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });
        // Edit Text - end

        imgbtn_uploadDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent docIntent = new Intent(context, AdditionalDocumentsActivity.class);
                docIntent.putExtra("patientUuid", patientUuid);
                docIntent.putExtra("visitUuid", visitUuid);
                docIntent.putExtra("encounterUuidVitals", encounterVitals);
                docIntent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);

                if (tag.equalsIgnoreCase("dispense"))
                    docIntent.putExtra("encounterDispenseAdminister", encounterDispense);
                else if (tag.equalsIgnoreCase("administer"))
                    docIntent.putExtra("encounterDispenseAdminister", encounterAdminister);

                startActivity(docIntent);
            }
        });

        tvSave.setOnClickListener(v -> {
            checkValidation();
        });
    }

    private void setImagesToRV() {
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            if (tag.equalsIgnoreCase("dispense"))
                fileuuidList = imagesDAO.getImageUuid(encounterDispense, UuidDictionary.COMPLEX_IMAGE_AD);  // Todo: here uploads docs new concept Id will come.
            else if (tag.equalsIgnoreCase("administer"))
                fileuuidList = imagesDAO.getImageUuid(encounterAdminister, UuidDictionary.COMPLEX_IMAGE_AD);  // Todo: here uploads docs new concept Id will come.

            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }
        }
        catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD("TAG", file.getMessage());
        }
        HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
        docsLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_docs.setLayoutManager(docsLayoutManager);
        rv_docs.setAdapter(horizontalAdapter);
    }

    private void checkValidation() {
        if (medList != null && medList.size() > 0) {
            if (tie_medNotes.getText().toString().isEmpty()) {
                tie_medNotes.requestFocus();
                tie_medNotes.setError(getString(R.string.error_field_required));
                return;
            }
        }

        if (aidList != null && aidList.size() > 0) {
            if (tie_totalCost.getText().toString().isEmpty()) {
                tie_totalCost.requestFocus();
                tie_totalCost.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_vendorDiscount.getText().toString().isEmpty()) {
                tie_vendorDiscount.requestFocus();
                tie_vendorDiscount.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_coveredCost.getText().toString().isEmpty()) {
                tie_coveredCost.requestFocus();
                tie_coveredCost.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_outOfPocket.getText().toString().isEmpty()) {
                tie_outOfPocket.requestFocus();
                tie_outOfPocket.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_aidNotes.getText().toString().isEmpty()) {
                tie_aidNotes.requestFocus();
                tie_aidNotes.setError(getString(R.string.error_field_required));
                return;
            }
        }

        saveDataToDB();
    }

    private void saveDataToDB() {
        String medicineValue = tv_medData.getText().toString().trim();
        String medNotesValue = tie_medNotes.getText().toString().trim();

        String aidValue = tv_aidData.getText().toString().trim();
        String aidNotesValue = tie_aidNotes.getText().toString().trim();
        String totalCostValue = tie_totalCost.getText().toString().trim();
        String vendorDiscountValue = tie_vendorDiscount.getText().toString().trim();
        String coveredCostValue = tie_coveredCost.getText().toString().trim();
        String outOfPocketValue = tie_outOfPocket.getText().toString().trim();
        String otherAids = tie_others.getText().toString().trim();

        boolean isEncounterCreated = false;
        if (tag.equalsIgnoreCase("dispense")) {

            isEncounterCreated = false;
            EncounterDAO encounterDAO = new EncounterDAO();
            EncounterDTO encounterDTO = new EncounterDTO();
            encounterDTO.setUuid(UUID.randomUUID().toString());
            encounterDispense = encounterDTO.getUuid();
            encounterDTO.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_DISPENSE);   // Dispense Encounter
            encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            encounterDTO.setVisituuid(visitUuid);
            encounterDTO.setSyncd(false);
            encounterDTO.setProvideruuid(sessionManager.getProviderID());
            Log.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
            encounterDTO.setVoided(0);
            try {
                isEncounterCreated = encounterDAO.createEncountersToDB(encounterDTO);
                if (isEncounterCreated) {

                    // Dispense - medication push
                    if (medList != null && medList.size() > 0) {
                        insertMedicationObs(medicineValue, medNotesValue, encounterDTO.getUuid(), OBS_DISPENSE_MEDICATION); // Dispense Med Obs.
                    }

                    if (aidList != null && aidList.size() > 0) {
                        insertAidObs(aidValue, aidNotesValue, encounterDTO.getUuid(),
                                totalCostValue, vendorDiscountValue, coveredCostValue, outOfPocketValue, otherAids, OBS_DISPENSE_AID);  // Dispense Aid Obs.
                    }

                    // Create OBS and push - END

                }
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            Toast.makeText(this, getString(R.string.dispense_data_saved), Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase("administer")) {

            isEncounterCreated = false;
            EncounterDAO encounterDAO = new EncounterDAO();
            EncounterDTO encounterDTO = new EncounterDTO();
            encounterDTO.setUuid(UUID.randomUUID().toString());
            encounterAdminister = encounterDTO.getUuid();
            encounterDTO.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_ADMINISTER); // Administer Encounter
            encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            encounterDTO.setVisituuid(visitUuid);
            encounterDTO.setSyncd(false);
            encounterDTO.setProvideruuid(sessionManager.getProviderID());
            Log.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
            encounterDTO.setVoided(0);
            try {
                isEncounterCreated = encounterDAO.createEncountersToDB(encounterDTO);
                if (isEncounterCreated) {

                    // Administer - medication push
                    if (medList != null && medList.size() > 0) {
                        insertMedicationObs(medicineValue, medNotesValue, encounterDTO.getUuid(), OBS_ADMINISTER_MEDICATION);   // Administer Med Obs.
                    }
                }

            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            Toast.makeText(this, getString(R.string.administer_data_saved), Toast.LENGTH_SHORT).show();
        }

        if (NetworkConnection.isOnline(getApplication())) {
            SyncUtils syncUtils = new SyncUtils();
            syncUtils.syncForeground("dispense_administer");
        }

    }

    private void insertAidObs(String aidValue, String aidNotesValue, String encounteruuid,
                              String totalCostValue, String vendorDiscountValue, String coveredCostValue,
                              String outOfPocketValue, String otherAids, String OBS_DISPENSE_ADMINISTER_AID) {
        ObsDAO obsDAO;
        ObsDTO obsDTO;
        List<String> aidUuidList = new ArrayList<>();

        for (MedicationAidModel dto : aidList) {
            aidUuidList.add(dto.getUuid());
            aidModel.setAidUuidList(aidUuidList);   // 1. medicines uuid

           /* String tvAid = aidValue.replaceAll("<br>", "");
            PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(dto.getValue());

            String value = "";
            if (LocaleHelper.isArabic(context))
                value = patientAttributeLanguageModel.getAr().replaceAll("\n", "");
            else
                value = patientAttributeLanguageModel.getEn().replaceAll("\n", "");

            if (tvAid.contains(value)) {
                aidUuidList.add(dto.getUuid());
                aidModel.setAidUuidList(aidUuidList);   // 1. medicines uuid
            }*/
        }

        List<String> notesList = new ArrayList<>();
        notesList.add(aidNotesValue);
        aidModel.setAidNotesList(notesList);    // 2. notes

        aidModel.setHwName(sessionManager.getProviderID());    // 3. hw name
        aidModel.setDateTime(AppConstants.dateAndTimeUtils.currentDateTime()); // 4. datetime.

        // 5. all fields.
        aidModel.setTotalCost(totalCostValue);
        aidModel.setVendorDiscount(vendorDiscountValue);
        aidModel.setCoveredCost(coveredCostValue);
        aidModel.setOutOfPocket(outOfPocketValue);
        aidModel.setOtherAids(otherAids);

        // Create OBS and push - START
        Gson gson = new Gson();
        obsDAO = new ObsDAO();
        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(OBS_DISPENSE_ADMINISTER_AID);  // OBS aid data.
        obsDTO.setEncounteruuid(encounteruuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(gson.toJson(aidModel));

        try {
            boolean isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        // END

    }

    private void insertMedicationObs(String medicineValue, String medNotesValue, String encounteruuid, String OBS_DISPENSE_ADMINISTER_AID) {
        ObsDAO obsDAO;
        ObsDTO obsDTO;
        List<String> medUuidList = new ArrayList<>();

        for (MedicationAidModel dto : medList) {
            medUuidList.add(dto.getUuid());
            medModel.setMedicationUuidList(medUuidList);   // 1. medicines uuid

           /* String tvMed = medicineValue.replaceAll("<br>", "");
            PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(dto.getValue());

            String value = "";
            if (LocaleHelper.isArabic(context))
                value = patientAttributeLanguageModel.getAr().replaceAll("\n", "");
            else
                value = patientAttributeLanguageModel.getEn().replaceAll("\n", "");

            if (tvMed.contains(value)) {
                medUuidList.add(dto.getUuid());
                medModel.setMedicationUuidList(medUuidList);   // 1. medicines uuid
            }*/
        }

        List<String> notesList = new ArrayList<>();
        notesList.add(medNotesValue);
        medModel.setMedicationNotesList(notesList);    // 2. notes
        medModel.setHwName(sessionManager.getProviderID());    // 3. hw name
        medModel.setDateTime(AppConstants.dateAndTimeUtils.currentDateTime()); // 4. datetime.
       // medModel.setDocumentsList(fileList());

        // Create OBS and push - START
        Gson gson = new Gson();
        obsDAO = new ObsDAO();
        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(OBS_DISPENSE_ADMINISTER_AID);  // OBS medicine data.
        obsDTO.setEncounteruuid(encounteruuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(gson.toJson(medModel));

        try {
            boolean isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        // END

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

    private void calculateOtherAids() {
        int totalCost = 0, vendorDiscount = 0, coveredCost = 0, outPocket = 0;
        int otherAid = 0;

        if (!tie_totalCost.getText().toString().isEmpty())
            totalCost = Integer.parseInt(tie_totalCost.getText().toString().trim());

        if (!tie_vendorDiscount.getText().toString().isEmpty())
            vendorDiscount = Integer.parseInt(tie_vendorDiscount.getText().toString().trim());

        if (!tie_coveredCost.getText().toString().isEmpty())
            coveredCost = Integer.parseInt(tie_coveredCost.getText().toString().trim());

        if (!tie_outOfPocket.getText().toString().isEmpty())
            outPocket = Integer.parseInt(tie_outOfPocket.getText().toString().trim());

        otherAid = totalCost - vendorDiscount - coveredCost - outPocket;

        tie_others.setText(String.valueOf(otherAid));
    }

    private PatientAttributeLanguageModel getPatientAttributeFromJSON(String jsonString) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(jsonString, PatientAttributeLanguageModel.class);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));

    }
}