package org.intelehealth.app.activities.medicationAidActivity;

import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.ADMINISTER;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.COLLECTED;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.DISPENSE;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.RECEIVED;
import static org.intelehealth.app.utilities.EditTextUtils.emojiFilter;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_ADMINISTER_AID;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_ADMINISTER_MEDICATION;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_DISPENSE_AID;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_DISPENSE_MEDICATION;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_TEST_COLLECT;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_TEST_RECEIVE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
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
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.PatientAttributeLanguageModel;
import org.intelehealth.app.models.dispenseAdministerModel.AidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.services.DownloadService;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.BaseActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class AdministerDispenseActivity extends BaseActivity {
    private TextInputEditText tie_medNotes, tie_aidNotes,
            tie_totalCost, tie_vendorDiscount, tie_coveredCost, tie_outOfPocket, tie_others;

    public static final String TAG = "AdministerActivity";
    public static final int IMAGE_LIST_INTENT = 700;

    public static final int IMAGE_LIMIT = 4;
    private TextView tv_medData, tv_aidData, tvSave, tv_medication;
    private String tag = "";
    private FrameLayout fl_med, fl_aid;
    private List<MedicationAidModel> medList, aidList, testList;
    private ImageButton imgbtn_uploadDocs;
    private Context context;
    private RecyclerView rv_docs;
    private RecyclerView.LayoutManager docsLayoutManager;
    private String patientUuid, visitUuid, encounterVisitNote, encounterVitals, encounterAdultIntials, encounterDisenseAdminister = "",
            EncounterAdultInitial_LatestVisit, patientName, patientAge,
            patientGender, intentTag;
    private float float_ageYear_Month;
    private Boolean isPastVisit = false;

    /*encounterDispense = "", encounterAdminister = ""*/;
    private ObsDTO obsDTOMedication, obsDTOAid;
    //    private List<ObsDTO> obsDTOList_Medication, obsDTOList_Aid;
    private ArrayList<String> fileuuidList = new ArrayList<>();
    ArrayList<File> fileList;

    private SessionManager sessionManager;
    private MedicationModel medModel = new MedicationModel();
    private AidModel aidModel = new AidModel();
    private TextView additionalImageDownloadText;
    private boolean isEncounterCreated = false;
    private HorizontalAdapter horizontalAdapter = new HorizontalAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_dispense);

        context = AdministerDispenseActivity.this;
        try {
            initUI();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        tie_medNotes.setHint(getString(R.string.enter_details_here));
        tie_aidNotes.setHint(getString(R.string.enter_details_here));
        //   registerBroadcastReceiverDynamically();

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

    private void initUI() throws DAOException {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        sessionManager = new SessionManager(context);
        tie_medNotes = findViewById(R.id.tie_medNotes);
        tie_medNotes.setFilters(new InputFilter[]{emojiFilter});
        tie_aidNotes = findViewById(R.id.tie_aidNotes);
        tie_aidNotes.setFilters(new InputFilter[]{emojiFilter});

        tie_totalCost = findViewById(R.id.tie_totalCost);
        tie_vendorDiscount = findViewById(R.id.tie_vendorDiscount);
        tie_coveredCost = findViewById(R.id.tie_coveredCost);
        tie_outOfPocket = findViewById(R.id.tie_outOfPocket);
        tie_others = findViewById(R.id.tie_others);

        tv_medData = findViewById(R.id.tv_medData);
        tv_aidData = findViewById(R.id.tv_aidData);
        tv_medication = findViewById(R.id.tv_medication);

        fl_aid = findViewById(R.id.fl_aid);
        fl_med = findViewById(R.id.fl_med);

        imgbtn_uploadDocs = findViewById(R.id.imgbtn_uploadDocs);
        //image download for additional documents
        additionalImageDownloadText = findViewById(R.id.additional_documents_download);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        additionalImageDownloadText.setPaintFlags(p.getColor());
        additionalImageDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        rv_docs = findViewById(R.id.rv_docs);
        tvSave = findViewById(R.id.tvSave);

        medList = new ArrayList<>();
        aidList = new ArrayList<>();
        testList = new ArrayList<>();

        Intent intent = getIntent();
        tag = intent.getStringExtra("mtag");
        patientUuid = intent.getStringExtra("patientUuid");
        visitUuid = intent.getStringExtra("visitUuid");
        encounterVisitNote = intent.getStringExtra("encounterVisitNote");
        encounterVitals = intent.getStringExtra("encounterUuidVitals");
        encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

        EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
        patientName = intent.getStringExtra("name");
        patientAge = intent.getStringExtra("age");
        patientGender = intent.getStringExtra("gender");
        float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
        intentTag = intent.getStringExtra("tag");
        isPastVisit = intent.getBooleanExtra("pastVisit", false);

        //  encounterDisenseAdminister = intent.getStringExtra("encounterDisenseAdminister");

       /* encounterDispense = intent.getStringExtra("encounterDispense");
        encounterAdminister = intent.getStringExtra("encounterAdminister");*/

        medList = (List<MedicationAidModel>) intent.getSerializableExtra("med");
        aidList = (List<MedicationAidModel>) intent.getSerializableExtra("aid");    // null on empty.
        testList = (List<MedicationAidModel>) intent.getSerializableExtra("test");    // null on empty.

/*
        if (!encounterDisenseAdminister.isEmpty() && encounterDisenseAdminister != null) {
            if (tag.equalsIgnoreCase(DISPENSE)) {
                setMedicationValues();
                setAidValues();
            } else if (tag.equalsIgnoreCase(ADMINISTER)) {
                setMedicationValues();
            }
        }
*/


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

        //   additionalDocumentImagesDownload();
        //   setImagesToRV();
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
        else if (testList != null && testList.size() > 0) {
            fl_med.setVisibility(View.VISIBLE);
            tv_medication.setText(getString(R.string.test));

            StringBuilder testData = new StringBuilder();
            for (MedicationAidModel test : testList) {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(test.getValue());
                String value = "";
                if (LocaleHelper.isArabic(context))
                    value = patientAttributeLanguageModel.getAr().replaceAll("\n", "");
                else
                    value = patientAttributeLanguageModel.getEn().replaceAll("\n", "");

                testData.append(Node.bullet).append(" ").append(value).append("\n\n");
            }

            tv_medData.setText(testData.substring(0, testData.length() - 2));

        } else
            fl_med.setVisibility(View.GONE);

        if (tag.equalsIgnoreCase(ADMINISTER)) {
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
        }
        else if (tag.equalsIgnoreCase(COLLECTED)) {
            getSupportActionBar().setTitle(getString(R.string.collected_test));
            fl_aid.setVisibility(View.GONE);
        }
        else if (tag.equalsIgnoreCase(RECEIVED)) {
            getSupportActionBar().setTitle(getString(R.string.results_received));
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
            } else
                fl_aid.setVisibility(View.GONE);

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

                if (tag.equalsIgnoreCase(DISPENSE)) {
                    createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_DISPENSE);
                }
                else if (tag.equalsIgnoreCase(ADMINISTER)) {
                    createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_ADMINISTER);
                }
                else if (tag.equalsIgnoreCase(COLLECTED)) {
                    createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_TEST_COLLECT);
                }
                else if (tag.equalsIgnoreCase(RECEIVED)) {
                    createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_TEST_RECEIVE);
                }

                docIntent.putExtra("patientUuid", patientUuid);
                docIntent.putExtra("visitUuid", visitUuid);
                docIntent.putExtra("encounterUuidVitals", encounterVitals);
                docIntent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                docIntent.putExtra("fileuuidList", fileuuidList);
                docIntent.putExtra("isDispenseAdminister", (tag.equalsIgnoreCase(DISPENSE) || tag.equalsIgnoreCase(ADMINISTER)
                        || tag.equalsIgnoreCase(COLLECTED) || tag.equalsIgnoreCase(RECEIVED)));

                Log.d(TAG, "img btn onClick: " + encounterDisenseAdminister);
                docIntent.putExtra("encounterDispenseAdminister", encounterDisenseAdminister);
                documentResult.launch(docIntent);
            }
        });

        tvSave.setOnClickListener(v -> {
            checkValidation();
        });
    }

    private final ActivityResultLauncher<Intent> documentResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), o -> {
                Log.e(TAG, ": documentResult =>" + o.getResultCode());
                if (o.getData() != null) {
                    Log.e(TAG, ": documentResult =>RESULT_OK::" + new Gson().toJson(o.getData()));
                    List<DocumentObject> rowListItem = new ArrayList<>();
                    rowListItem = (List<DocumentObject>) o.getData().getSerializableExtra("rowListItem");
                    encounterDisenseAdminister = o.getData().getStringExtra("encounterDispenseAdminister");

                    fileuuidList = new ArrayList<>();
                    if (rowListItem.size() > 0) {
                        HashSet<String> hashSet = new HashSet<>();
                        for (int i = 0; i < rowListItem.size(); i++) {
                            hashSet.add(StringUtils.getFileNameWithoutExtensionString(rowListItem.get(i).getDocumentName()));
                            // Log.d("TAG", "onActivityResult fileuuidList: " + fileuuidList.get(i));
                        }


                        fileuuidList.addAll(hashSet);
                        setImagesToRV();
                    }
                }
            });

    private void setAidValues() throws DAOException {
        String encounterId = encounterDisenseAdminister;
        String conceptId = OBS_DISPENSE_AID;
        if (tag.equalsIgnoreCase(ADMINISTER))
            conceptId = OBS_ADMINISTER_AID;

        // Notes
        List<String> aidNotes = new ArrayList<>();
        AidModel model = new Gson().fromJson(ObsDAO.getObsValue(encounterId, conceptId).getValue(), AidModel.class);
        if (model != null) {
/*
            if (model.getAidNotesList() != null)
                aidNotes.addAll(model.getAidNotesList());
*/

          /*  if (model.getTotalCost() != null)
                tie_totalCost.setText(model.getTotalCost());    // total cost

            if (model.getVendorDiscount() != null)
                tie_vendorDiscount.setText(model.getVendorDiscount());    // vendor discount

            if (model.getCoveredCost() != null)
                tie_coveredCost.setText(model.getCoveredCost());    // covered cost

            if (model.getOutOfPocket() != null)
                tie_outOfPocket.setText(model.getOutOfPocket());    // out of pocket

            if (model.getOtherAids() != null)
                tie_others.setText(model.getOtherAids());    // other aids
*/
/*
            if (fileuuidList.size() == 0 && model.getDocumentsList() != null && model.getDocumentsList().size() > 0)   // docs images
                fileuuidList.addAll(model.getDocumentsList());
*/
        }

/*
        if (aidNotes != null) {
            aidModel.setAidNotesList(aidNotes);
            if (aidModel.getAidNotesList() != null) {
                for (int i = 0; i < aidModel.getAidNotesList().size(); i++) {
                    tie_aidNotes.setText(aidModel.getAidNotesList().get(i));
                }
            }
        }
*/

    }

    private void setMedicationValues() throws DAOException {
        String encounterId = encounterDisenseAdminister;
        String conceptId = OBS_DISPENSE_MEDICATION;
        if (tag.equalsIgnoreCase(ADMINISTER))
            conceptId = OBS_ADMINISTER_MEDICATION;

        List<String> medNotes = new ArrayList<>();
        MedicationModel medicationModel = new Gson().fromJson(ObsDAO.getObsValue(encounterId, conceptId).getValue(),
                MedicationModel.class);

/*
        if (medicationModel != null && medicationModel.getMedicationNotesList() != null)
            medNotes.addAll(medicationModel.getMedicationNotesList());
*/

/*
            if (fileuuidList.size() == 0 && medicationModel.getDocumentsList() != null && medicationModel.getDocumentsList().size() > 0)
                fileuuidList.addAll(medicationModel.getDocumentsList());
*/

/*
        if (medNotes != null) {
            medModel.setMedicationNotesList(medNotes);
            if (medModel.getMedicationNotesList() != null) {
                for (int i = 0; i < medModel.getMedicationNotesList().size(); i++) {
                    tie_medNotes.setText(medModel.getMedicationNotesList().get(i));
                }
            }
        }
*/

    }

    private void setImagesToRV() {
        fileList = new ArrayList<File>();
        Log.d("TAG", "setImagesToRV: count: " + String.valueOf(fileuuidList.size()));

        for (String fileuuid : fileuuidList) {
            String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
            if (new File(filename).exists()) {
                fileList.add(new File(filename));
            }
        }
        horizontalAdapter = new HorizontalAdapter(fileList, this);
        docsLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_docs.setLayoutManager(docsLayoutManager);
        rv_docs.setAdapter(horizontalAdapter);
    }

    private void checkValidation() {
/*
        if ((medList != null && medList.size() > 0) || (testList != null && testList.size() > 0)) {
            if (tie_medNotes.getText().toString().isEmpty()) {
                tie_medNotes.requestFocus();
                tie_medNotes.setError(getString(R.string.error_field_required));
                return;
            }
        }
*/  // SYR-577

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
/*
            if (tie_aidNotes.getText().toString().isEmpty()) {
                tie_aidNotes.requestFocus();
                tie_aidNotes.setError(getString(R.string.error_field_required));
                return;
            }
*/  // SYR-577
        }

        if (horizontalAdapter != null && horizontalAdapter.getItemCount() == 0) {
            Toast.makeText(context, R.string.please_add_at_least_one_image, Toast.LENGTH_SHORT).show();
            return;
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

        if (tag.equalsIgnoreCase(DISPENSE)) {
            createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_DISPENSE);
            if (isEncounterCreated) {
                // Dispense - medication push
                if (medList != null && medList.size() > 0) {
                    insertMedicationObs(medList, medicineValue, medNotesValue, encounterDisenseAdminister, OBS_DISPENSE_MEDICATION); // Dispense Med Obs.
                }

                if (aidList != null && aidList.size() > 0) {
                    insertAidObs(aidValue, aidNotesValue, encounterDisenseAdminister,
                            totalCostValue, vendorDiscountValue, coveredCostValue, outOfPocketValue, otherAids, OBS_DISPENSE_AID);  // Dispense Aid Obs.
                }
                // Create OBS and push - END
            }
            Toast.makeText(this, getString(R.string.dispense_data_saved), Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase(ADMINISTER)) {

            createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_ADMINISTER);

            if (isEncounterCreated) {
                // Administer - medication push
                if (medList != null && medList.size() > 0) {
                    insertMedicationObs(medList, medicineValue, medNotesValue, encounterDisenseAdminister, OBS_ADMINISTER_MEDICATION);   // Administer Med Obs.
                }
            }

            Toast.makeText(this, getString(R.string.administer_data_saved), Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase(COLLECTED)) {
            createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_TEST_COLLECT);
            if (isEncounterCreated) {
                // Test - collect push
                if (testList != null && testList.size() > 0) {
                    insertMedicationObs(testList, medicineValue, medNotesValue, encounterDisenseAdminister, OBS_TEST_COLLECT);   // Administer Med Obs.
                }
            }

            Toast.makeText(this, getString(R.string.test_collection_data_saved), Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase(RECEIVED)) {
            createEncounterDispense_Administer(UuidDictionary.ENCOUNTER_TEST_RECEIVE);
            if (isEncounterCreated) {
                // Test - receive push
                if (testList != null && testList.size() > 0) {
                    insertMedicationObs(testList, medicineValue, medNotesValue, encounterDisenseAdminister, OBS_TEST_RECEIVE);   // Administer Med Obs.
                }
            }

            Toast.makeText(this, getString(R.string.test_result_data_saved), Toast.LENGTH_SHORT).show();
        }

        if (NetworkConnection.isOnline(getApplication())) {
            SyncUtils syncUtils = new SyncUtils();
            syncUtils.syncForeground("dispense_administer");
        }

        Intent intent = new Intent(context, VisitSummaryActivity.class);
        intent.putExtra("mtag", tag);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterVisitNote", encounterVisitNote);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
        intent.putExtra("encounterDisenseAdminister", encounterDisenseAdminister);

        intent.putExtra("gender", patientGender);
        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
        intent.putExtra("name", patientName);
        intent.putExtra("age", patientAge);
        intent.putExtra("float_ageYear_Month", float_ageYear_Month);
        intent.putExtra("tag", intentTag);
        intent.putExtra("pastVisit", isPastVisit);

        startActivity(intent);
    }

    private void createEncounterDispense_Administer(String encounterTypeUUID) {
        if (encounterDisenseAdminister.equalsIgnoreCase("") || encounterDisenseAdminister == null)
            encounterDisenseAdminister = UUID.randomUUID().toString();

        //  encounterDisenseAdminister = UUID.randomUUID().toString();
        isEncounterCreated = false;
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterDisenseAdminister);
        encounterDTO.setEncounterTypeUuid(encounterTypeUUID);   // Dispense Encounter
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);

        try {
            isEncounterCreated = encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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

        if (!aidNotesValue.isEmpty()) {
            List<String> notesList = new ArrayList<>();
            notesList.add(aidNotesValue);
            aidModel.setAidNotesList(notesList);    // 2. notes
        }

        aidModel.setHwUuid(sessionManager.getProviderID()); // 3. hw uuid
        aidModel.setHwName(sessionManager.getChwname());    // 3b. hw name
        aidModel.setDateTime(AppConstants.dateAndTimeUtils.currentDateTime()); // 4. datetime.

        // 5. all fields.
        aidModel.setTotalCost(totalCostValue);
        aidModel.setVendorDiscount(vendorDiscountValue);
        aidModel.setCoveredCost(coveredCostValue);
        aidModel.setOutOfPocket(outOfPocketValue);
        aidModel.setOtherAids(otherAids);
        aidModel.setDocumentsList(fileuuidList);

        // Create OBS and push - START
        Gson gson = new Gson();
        obsDAO = new ObsDAO();
        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(OBS_DISPENSE_ADMINISTER_AID);  // OBS aid data.
        obsDTO.setEncounteruuid(encounteruuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(gson.toJson(aidModel));

        Log.d(TAG, "insertAidObs: " + gson.toJson(aidModel));

        try {
            boolean isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        // END

    }

    private void insertMedicationObs(List<MedicationAidModel> dataList, String medicineValue,
                                     String medNotesValue, String encounteruuid, String OBS_CONCEPT_UUID) {
        ObsDAO obsDAO;
        ObsDTO obsDTO;
        List<String> medUuidList = new ArrayList<>();

        for (MedicationAidModel dto : dataList) {
            medUuidList.add(dto.getUuid());
            medModel.setMedicationUuidList(medUuidList);   // 1. medicines uuid
        }

        if (!medNotesValue.isEmpty()) {
            List<String> notesList = new ArrayList<>();
            notesList.add(medNotesValue);
            medModel.setMedicationNotesList(notesList);    // 2. notes
        }
        medModel.setHwUuid(sessionManager.getProviderID());    // 3. hw id
        medModel.setHwName(sessionManager.getChwname());    // 3b. hw name
        medModel.setDateTime(AppConstants.dateAndTimeUtils.currentDateTime()); // 4. datetime.
        medModel.setDocumentsList(fileuuidList);

        // Create OBS and push - START
        Gson gson = new Gson();
        obsDAO = new ObsDAO();
        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(OBS_CONCEPT_UUID);  // OBS medicine data.
        obsDTO.setEncounteruuid(encounteruuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(gson.toJson(medModel));

        Log.d(TAG, "insertMedicationObs: " + gson.toJson(medModel));

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

    @Override
    protected void onResume() {
        super.onResume();
//        setImagesToRV();
    }

    private void additionalDocumentImagesDownload() {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {

           /* if (tag.equalsIgnoreCase(DISPENSE))
                fileuuidList = (ArrayList<String>) imagesDAO.isImageListObsExists(encounterDispense, UuidDictionary.COMPLEX_IMAGE_AD);
            else if (tag.equalsIgnoreCase(ADMINISTER))
                fileuuidList = (ArrayList<String>) imagesDAO.isImageListObsExists(encounterAdminister, UuidDictionary.COMPLEX_IMAGE_AD);*/

            List<String> imageList = imagesDAO.isImageListObsExists(encounterDisenseAdminister, UuidDictionary.COMPLEX_IMAGE_AD);

            for (String images : imageList) {
                if (imagesDAO.isLocalImageUuidExists(images))
                    additionalImageDownloadText.setVisibility(View.GONE);
                else additionalImageDownloadText.setVisibility(View.VISIBLE);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        additionalImageDownloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_AD);
                additionalImageDownloadText.setVisibility(View.GONE);
            }
        });

    }

    private void startDownload(String imageType) {  // todo: handle this.
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        // intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
        intent.putExtra("encounterUuidAdultIntial", encounterDisenseAdminister);
        intent.putExtra("ImageType", imageType);
        startService(intent);
    }

    public void registerBroadcastReceiverDynamically() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        ContextCompat.registerReceiver(this, broadcastReceiverForIamgeDownlaod, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    private BroadcastReceiver broadcastReceiverForIamgeDownlaod = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    };

    @Override
    protected void onDestroy() {
        //  unregisterReceiver(broadcastReceiverForIamgeDownlaod);
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_LIST_INTENT) {
            List<DocumentObject> rowListItem = new ArrayList<>();
            if (data != null) {
                rowListItem = (List<DocumentObject>) data.getSerializableExtra("rowListItem");
                encounterDisenseAdminister = data.getStringExtra("encounterDispenseAdminister");
            }

            fileuuidList = new ArrayList<>();
            if (rowListItem.size() > 0) {
                HashSet<String> hashSet = new HashSet<>();
                for (int i = 0; i < rowListItem.size(); i++) {
                    hashSet.add(StringUtils.getFileNameWithoutExtensionString(rowListItem.get(i).getDocumentName()));
                }


                fileuuidList.addAll(hashSet);
            }

        }
    }
}