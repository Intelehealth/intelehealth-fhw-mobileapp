package org.intelehealth.unicef.activities.prescription;

import static org.intelehealth.unicef.utilities.UuidDictionary.ENCOUNTER_DR_ROLE;
import static org.intelehealth.unicef.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.unicef.utilities.UuidDictionary.FOLLOW_UP_VISIT;
import static org.intelehealth.unicef.utilities.UuidDictionary.JSV_MEDICATIONS;
import static org.intelehealth.unicef.utilities.UuidDictionary.MEDICAL_ADVICE;
import static org.intelehealth.unicef.utilities.UuidDictionary.OBS_DOCTORDETAILS;
import static org.intelehealth.unicef.utilities.UuidDictionary.REQUESTED_TESTS;
import static org.intelehealth.unicef.utilities.UuidDictionary.TELEMEDICINE_DIAGNOSIS;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;
import org.intelehealth.unicef.activities.homeActivity.HomeActivity;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.database.dao.ObsDAO;
import org.intelehealth.unicef.database.dao.ProviderDAO;
import org.intelehealth.unicef.models.ClsDoctorDetails;
import org.intelehealth.unicef.models.dto.ObsDTO;
import org.intelehealth.unicef.models.prescriptionUpload.EncounterProvider;
import org.intelehealth.unicef.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.unicef.models.prescriptionUpload.Ob;
import org.intelehealth.unicef.models.prescriptionUpload.ObsPrescResponse;
import org.intelehealth.unicef.models.prescriptionUpload.ObsPrescription;
import org.intelehealth.unicef.networkApiCalls.ApiClient;
import org.intelehealth.unicef.networkApiCalls.ApiInterface;
import org.intelehealth.unicef.syncModule.SyncUtils;
import org.intelehealth.unicef.utilities.Base64Utils;
import org.intelehealth.unicef.utilities.LocaleHelper;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.VisitUtils;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class PrescriptionActivity extends LocalConfigActivity {

    private static final String TAG = PrescriptionActivity.class.getSimpleName();

    Context context;
    Context presContext;

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String patientGender;
    String intentTag;
    String visitUUID;
    String encounterVisitNote;

    SQLiteDatabase db;
    private SessionManager sessionManager;
    private String appLanguage;
    private String followUpDate;
    private String encounterVitals;
    private String encounterUuidAdultIntial;
    ObsDAO obsDAO = new ObsDAO();
    private Context mContext;
    String OBSURL;
    EndVisitEncounterPrescription visitCompleteStatus;

    RecyclerView diagnosisRecyclerView;
    DiagnosisPrescAdapter diagnosisPrescAdapter;
    List<PrescDataModel> diagnosisList;

    RecyclerView medicationRecyclerView;
    MedicationPrescAdapter medicationPrescAdapter;
    List<PrescDataModel> medicationList;

    RecyclerView testRecyclerView;
    TestPrescAdapter testPrescAdapter;
    List<PrescDataModel> testList;

    RecyclerView adviceRecyclerView;
    AdvicePrescAdapter advicePrescAdapter;
    List<PrescDataModel> adviceList;

    RecyclerView followupRecyclerView;
    FollowupPresAdapter followupPrescAdapter;
    List<PrescDataModel> followupList;
    Base64Utils base64Utils = new Base64Utils();
    boolean isuploadPrescription = false;
    Button btnFollowUp;


    TextView txtDignosisHeading;

    @Override
    protected void attachBaseContext(Context newBase) {
        mContext = newBase;
        super.attachBaseContext(LocaleHelper.updateLocale(newBase, new SessionManager(newBase).getAppLanguage()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager = new SessionManager(this);
        appLanguage = sessionManager.getAppLanguage();

        OBSURL = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/obs";

        final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientGender = intent.getStringExtra("gender");
            encounterVisitNote = intent.getStringExtra("startVisitNoteApiEncounterResponse"); // this is needed for the obs api for sending each data.
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterUuidAdultIntial = intent.getStringExtra("encounterUuidAdultIntial");
//            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
//            mSharedPreference = this.getSharedPreferences("visit_summary", Context.MODE_PRIVATE);
            patientName = intent.getStringExtra("name");

            intentTag = intent.getStringExtra("tag");
        }

//        setTitle(patientName + ": " + getTitle());
        setTitle(R.string.prescription);

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        context = getApplicationContext();
        presContext = PrescriptionActivity.this;

        txtDignosisHeading = findViewById(R.id.txtDignosisHeading);
        txtDignosisHeading.setText(getString(R.string.diagnosis));
        initUI();
    }

    private void initUI() {
        //call
        Button btnSignSubmit = findViewById(R.id.btnSignSubmit);

        //diagnosis
        // LinearLayout llDiagnosisResult = findViewById(R.id.llDiagnosisResult);
        diagnosisRecyclerView = findViewById(R.id.diagnosisRecyclerView);
        diagnosisList = new ArrayList<>();
        ObsDAO obsDAODiagnosis = new ObsDAO();
        diagnosisList = obsDAODiagnosis.fetchAllObsPrescData(encounterVisitNote, TELEMEDICINE_DIAGNOSIS, "true");
        diagnosisPrescAdapter = new DiagnosisPrescAdapter(presContext, diagnosisList);
        RecyclerView.LayoutManager diagnosismanager = new LinearLayoutManager(PrescriptionActivity.this,
                LinearLayoutManager.VERTICAL, false);
        diagnosisRecyclerView.setLayoutManager(diagnosismanager);
        diagnosisRecyclerView.setAdapter(diagnosisPrescAdapter);

        RadioGroup rgDiagnosis1 = findViewById(R.id.rgDiagnosis1);
        RadioGroup rgDiagnosis2 = findViewById(R.id.rgDiagnosis2);
        Button btnAddDiagnosis = findViewById(R.id.btnAddDiagnosis);
        AutoCompleteTextView etDiagnosis = findViewById(R.id.etDiagnosis);
        etDiagnosis.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.diagnosis)));
        etDiagnosis.setThreshold(1);

        btnAddDiagnosis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etDiagnosis.getText().toString())) {
                    etDiagnosis.setError(null);
                    RadioButton checked1 = findViewById(rgDiagnosis1.getCheckedRadioButtonId());
                    RadioButton checked2 = findViewById(rgDiagnosis2.getCheckedRadioButtonId());
                    if (checked1 != null && checked2 != null) {
                        String result = String.format("%s:%s & %s", etDiagnosis.getText(), checked1.getText(), checked2.getText());
                        Log.v("diag", "diag: " + result);
                        //  addResult(llDiagnosisResult, result);
                        // setObsData(encounterUuidAdultIntial, TELEMEDICINE_DIAGNOSIS, result);
                        etDiagnosis.setText("");
                        rgDiagnosis1.clearCheck();
                        rgDiagnosis2.clearCheck();

                        // Api call will upload this piece of data...
                        uploadPrescriptionData(result, TELEMEDICINE_DIAGNOSIS);
                    }
                } else {
                    etDiagnosis.setError(getResources().getString(R.string.error_field_required));
                    return;
                }
            }
        });


        //meds
        //  LinearLayout llMedsResult = findViewById(R.id.llMedsResult);
        medicationRecyclerView = findViewById(R.id.medicationRecyclerView);
        medicationList = new ArrayList<>();
        ObsDAO obsDAOMedication = new ObsDAO();
        medicationList = obsDAOMedication.fetchAllObsPrescData(encounterVisitNote, JSV_MEDICATIONS, "true");
        medicationPrescAdapter = new MedicationPrescAdapter(presContext, medicationList);
        RecyclerView.LayoutManager medicationmanager = new LinearLayoutManager(PrescriptionActivity.this,
                LinearLayoutManager.VERTICAL, false);
        medicationRecyclerView.setLayoutManager(medicationmanager);
        medicationRecyclerView.setAdapter(medicationPrescAdapter);

        AutoCompleteTextView etMedication = findViewById(R.id.etMedication);
        etMedication.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.medications)));
        etMedication.setThreshold(1);

        EditText etMedicationStrength = findViewById(R.id.etMedicationStrength);
        AutoCompleteTextView etMedicationUnit = findViewById(R.id.etMedicationUnit);
        etMedicationUnit.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.weight_units)));
        etMedicationUnit.setThreshold(1);

        EditText etMedicationAmount = findViewById(R.id.etMedicationAmount);
        AutoCompleteTextView etMedicationUnitTabSyrup = findViewById(R.id.etMedicationUnitTabSyrup);
        etMedicationUnitTabSyrup.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.weight_units)));
        etMedicationUnitTabSyrup.setThreshold(1);

        // EditText etMedicationFreq = findViewById(R.id.etMedicationFreq);
        AutoCompleteTextView etMedicationFreq = findViewById(R.id.etMedicationFreq);
        etMedicationFreq.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.frequency)));
        etMedicationFreq.setThreshold(1);

        AutoCompleteTextView etMedicationRoute = findViewById(R.id.etMedicationRoute);
        etMedicationRoute.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.route)));
        etMedicationRoute.setThreshold(1);

        EditText etMedicationDuration = findViewById(R.id.etMedicationDuration);
        AutoCompleteTextView etMedicationUnitsDays = findViewById(R.id.etMedicationUnitsDays);
        etMedicationUnitsDays.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.time_units)));
        etMedicationUnitsDays.setThreshold(1);

        EditText etMedicationReason = findViewById(R.id.etMedicationReason);
        EditText etMedicationInstructions = findViewById(R.id.etMedicationInstructions);
        Button btnAddMeds = findViewById(R.id.btnAddMeds);

        btnAddMeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etMedication.getText().toString())
                        && !TextUtils.isEmpty(etMedicationStrength.getText().toString())
                        && !TextUtils.isEmpty(etMedicationUnit.getText().toString())
                        && !TextUtils.isEmpty(etMedicationAmount.getText().toString())
                        && !TextUtils.isEmpty(etMedicationUnitTabSyrup.getText().toString())
                        && !TextUtils.isEmpty(etMedicationFreq.getText().toString())
                        && !TextUtils.isEmpty(etMedicationDuration.getText().toString())
                        && !TextUtils.isEmpty(etMedicationUnitsDays.getText().toString())
                ) {
                    etMedication.setError(null);
                    etMedicationStrength.setError(null);
                    etMedicationUnit.setError(null);
                    etMedicationAmount.setError(null);
                    etMedicationUnitTabSyrup.setError(null);
                    etMedicationFreq.setError(null);
                    etMedicationDuration.setError(null);
                    etMedicationUnitsDays.setError(null);

                    String result = String.format("%s: %s %s %s %s %s %s %s for %s %s %s"
                            , etMedication.getText()
                            , etMedicationStrength.getText()
                            , etMedicationUnit.getText()
                            , etMedicationAmount.getText()
                            , etMedicationUnitTabSyrup.getText()
                            , etMedicationFreq.getText()
                            , etMedicationRoute.getText()
                            , etMedicationReason.getText()
                            , etMedicationDuration.getText()
                            , etMedicationUnitsDays.getText()
                            , etMedicationInstructions.getText()
                    );
                    //   addResult(llMedsResult, result);
                    // setObsData(encounterUuidAdultIntial, JSV_MEDICATIONS, result);
                    etMedication.setText("");
                    etMedicationStrength.setText("");
                    etMedicationUnit.setText("");
                    etMedicationAmount.setText("");
                    etMedicationUnitTabSyrup.setText("");
                    etMedicationFreq.setText("");
                    etMedicationRoute.setText("");
                    etMedicationReason.setText("");
                    etMedicationDuration.setText("");
                    etMedicationUnitsDays.setText("");
                    etMedicationInstructions.setText("");

                    uploadPrescriptionData(result, JSV_MEDICATIONS);
                } else {
                    if (TextUtils.isEmpty(etMedication.getText().toString())) {
                        etMedication.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationStrength.getText().toString())) {
                        etMedicationStrength.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationUnit.getText().toString())) {
                        etMedicationUnit.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationAmount.getText().toString())) {
                        etMedicationAmount.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationUnitTabSyrup.getText().toString())) {
                        etMedicationUnitTabSyrup.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationFreq.getText().toString())) {
                        etMedicationFreq.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationDuration.getText().toString())) {
                        etMedicationDuration.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(etMedicationUnitsDays.getText().toString())) {
                        etMedicationUnitsDays.setError(getResources().getString(R.string.error_field_required));
                        return;
                    }
                }
            }
        });

        //test
        // LinearLayout llTestResult = findViewById(R.id.llTestResult);
        testRecyclerView = findViewById(R.id.testRecyclerView);
        testList = new ArrayList<>();
        ObsDAO obsDAOTest = new ObsDAO();
        testList = obsDAOTest.fetchAllObsPrescData(encounterVisitNote, REQUESTED_TESTS, "true");
        testPrescAdapter = new TestPrescAdapter(presContext, testList);
        RecyclerView.LayoutManager testmanager = new LinearLayoutManager(PrescriptionActivity.this,
                LinearLayoutManager.VERTICAL, false);
        testRecyclerView.setLayoutManager(testmanager);
        testRecyclerView.setAdapter(testPrescAdapter);

        AutoCompleteTextView etTest = findViewById(R.id.etTest);
        etTest.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.tests)));
        etTest.setThreshold(1);

        Button btnAddTest = findViewById(R.id.btnAddTest);
        btnAddTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etTest.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    etTest.setError(null);
                    //  addResult(llTestResult, result);
                    //  setObsData(encounterUuidAdultIntial, REQUESTED_TESTS, result);
                    etTest.setText("");
                    uploadPrescriptionData(result, REQUESTED_TESTS);
                } else {
                    etTest.setError(getResources().getString(R.string.error_field_required));
                    return;
                }
            }
        });

        //advice
        // LinearLayout llAdviceResult = findViewById(R.id.llAdviceResult);
        adviceRecyclerView = findViewById(R.id.adviceRecyclerView);
        adviceList = new ArrayList<>();
        ObsDAO obsDAOAdvice = new ObsDAO();
        adviceList = obsDAOAdvice.fetchAllObsPrescData(encounterVisitNote, MEDICAL_ADVICE, "true");
        advicePrescAdapter = new AdvicePrescAdapter(presContext, adviceList);
        RecyclerView.LayoutManager advicemanager = new LinearLayoutManager(PrescriptionActivity.this,
                LinearLayoutManager.VERTICAL, false);
        adviceRecyclerView.setLayoutManager(advicemanager);
        adviceRecyclerView.setAdapter(advicePrescAdapter);

        AutoCompleteTextView etAdviceRefer = findViewById(R.id.etAdviceRefer);
        etAdviceRefer.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.advice)));
        etAdviceRefer.setThreshold(1);

        Button btnAddAdvice = findViewById(R.id.btnAddAdvice);
        btnAddAdvice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etAdviceRefer.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    etAdviceRefer.setError(null);
                    //  addResult(llAdviceResult, result);
                    // setObsData(encounterUuidAdultIntial, MEDICAL_ADVICE, result);
                    etAdviceRefer.setText("");
                    uploadPrescriptionData(result, MEDICAL_ADVICE);
                } else {
                    etAdviceRefer.setError(getResources().getString(R.string.error_field_required));
                    return;
                }
            }
        });

        //notes
       /* LinearLayout llNotesResult = findViewById(R.id.llNotesResult);
        AutoCompleteTextView etNotes = findViewById(R.id.etNotes);
        Button btnAddNote = findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etNotes.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    addResult(llNotesResult, result);
                  //  setObsData(encounterUuidAdultIntial, ADDITIONAL_COMMENTS, result);
                    etNotes.setText("");
                    uploadPrescriptionData(result, ADDITIONAL_COMMENTS);
                }
            }
        });*/

        //follow up
        //  LinearLayout llFollowUpResult = findViewById(R.id.llFollowUpResult);
        btnFollowUp = findViewById(R.id.btnFollowUp);
        followupRecyclerView = findViewById(R.id.followupRecyclerView);
        followupList = new ArrayList<>();
        ObsDAO obsDAOFollowup = new ObsDAO();
        followupList = obsDAOFollowup.fetchAllObsPrescData(encounterVisitNote, FOLLOW_UP_VISIT, "true");
        followupPrescAdapter = new FollowupPresAdapter(presContext, followupList);
        RecyclerView.LayoutManager folllowupmanager = new LinearLayoutManager(
                PrescriptionActivity.this, LinearLayoutManager.VERTICAL, false);
        followupRecyclerView.setLayoutManager(folllowupmanager);
        followupRecyclerView.setAdapter(followupPrescAdapter);
        EditText etFollowUpDate = findViewById(R.id.etFollowUpDate);
        assignDatePicker(etFollowUpDate);
        EditText etFollowUpRemark = findViewById(R.id.etFollowUpRemark);


        btnFollowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etFollowUpDate.getText().toString())) {
                    etFollowUpDate.setError(null);
                    // llFollowUpResult.removeAllViews();
                    String result = etFollowUpDate.getText().toString();
                    if (!TextUtils.isEmpty(etFollowUpRemark.getText()))
                        result += ", " + getResources().getString(R.string.remarks) + etFollowUpRemark.getText();

                    if (followupPrescAdapter.getItemCount() > 0) {
                        Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.morethanonefollowupnotallowed),
                                Toast.LENGTH_SHORT).show();
                        etFollowUpDate.setText("");
                        etFollowUpRemark.setText("");
                        return;
                    } else {
                        etFollowUpDate.setText("");
                        etFollowUpRemark.setText("");
                        uploadPrescriptionData(result, FOLLOW_UP_VISIT);
                    }

                } else {
                    etFollowUpDate.setError(getResources().getString(R.string.error_field_required));
                    return;
                }
            }
        });


        btnSignSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here, prescription is given just need to pass the Visit Complete encounter to update the status of the visit on webapp...
                String url = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/encounter";
//                visitCompleteStatus = getVisitCompleteDataModel();
                // String encoded = sessionManager.getEncoded();

                try {
                    visitCompleteStatus = getVisitCompleteDataModel();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                String encoded = base64Utils.encoded("sysnurse", "Nurse123");

                ApiInterface apiService = ApiClient.createService(ApiInterface.class);
                Observable<ResponseBody> responseBodyObservable = apiService.OBS_SIGNANDSUBMIT_STATUS(
                        url, visitCompleteStatus, "Basic " + encoded);
                responseBodyObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<ResponseBody>() {
                            @Override
                            public void onNext(@NonNull ResponseBody responseBody) {
                                // status is received...
                                SyncUtils syncUtils = new SyncUtils();
                                syncUtils.syncForeground("downloadPrescription");
                                showDialogForHomeScreen();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Log.e("pres", "signandsubmit: " + e);
                            }

                            @Override
                            public void onComplete() {
                                Log.e("pres", "signandsubmitcomplete: ");
                            }
                        });
            }
        });
    }

    private void showDialogForHomeScreen() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.prescGivenSuccessfully);
        alertdialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(PrescriptionActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private EndVisitEncounterPrescription getVisitCompleteDataModel() throws DAOException {
        ClsDoctorDetails doctorDetails = new ClsDoctorDetails();
        ProviderDAO providerDAO = new ProviderDAO();
        Log.v("chwname", "chwnam: " + sessionManager.getChwname() + ", " + sessionManager.getProviderID());
        doctorDetails.setFontOfSign("almondita"); // common signature for all the family doctor fonts.
        doctorDetails.setName(providerDAO.getProviderGiven_Lastname(sessionManager.getProviderID()));
        doctorDetails.setSpecialization("Family Doctor");
        doctorDetails.setTextOfSign(providerDAO.getProviderGiven_Lastname(sessionManager.getProviderID()));
        Log.v("chwdetails", "chwdetails: " + new Gson().toJson(doctorDetails));
        // doctorDetails.setWhatsapp("7005308163");
        // doctorDetails.setPhoneNumber("7005308163");

        String drDetails = new Gson().toJson(doctorDetails);
        List<Ob> obList = new ArrayList<>();
        Ob ob = new Ob();
        ob.setConcept(OBS_DOCTORDETAILS);
        ob.setValue(drDetails);
        Log.v("drdetail", "drdetail: " + drDetails);
        obList.add(ob);

        List<EncounterProvider> encounterProviderList = new ArrayList<>();
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setEncounterRole(ENCOUNTER_DR_ROLE); // Constant
        encounterProvider.setProvider(sessionManager.getProviderID()); // user setup app provider
        encounterProviderList.add(encounterProvider);

        EndVisitEncounterPrescription datamodel = new EndVisitEncounterPrescription();
        datamodel.setPatient(patientUuid);
        datamodel.setEncounterProviders(encounterProviderList);
        datamodel.setVisit(visitUuid);
        datamodel.setEncounterDatetime(AppConstants.dateAndTimeUtils.currentDateTime());
        datamodel.setEncounterType(ENCOUNTER_VISIT_COMPLETE);
        datamodel.setObs(obList);

        Log.v("presbody", "newsubmit: " + new Gson().toJson(datamodel));
        return datamodel;
    }


//    private EndVisitEncounterPrescription getVisitCompleteDataModel() throws DAOException {
//        // For now it was added static values...need to discuss with team how we are going to send this data.
//        ClsDoctorDetails doctorDetails = new ClsDoctorDetails();
//
////        doctorDetails.setWhatsapp("7005308163");
////        doctorDetails.setPhoneNumber("7005308163");
////        doctorDetails.setFontOfSign("Pacifico");
////        doctorDetails.setName("Demo doctor1");
////        doctorDetails.setSpecialization("Neurologist");
////        doctorDetails.setTextOfSign("Dr. Demo 1");
//
//        ProviderDAO providerDAO = new ProviderDAO();
//        Log.v("chwname", "chwnam: "+ sessionManager.getChwname() + ", "+ sessionManager.getProviderID());
//        doctorDetails.setFontOfSign("almondita"); // common signature for all the family doctor fonts.
//        doctorDetails.setName("Dr. " + providerDAO.getProviderGiven_Lastname(sessionManager.getProviderID()));
//        doctorDetails.setSpecialization("Family Doctor");
//        doctorDetails.setTextOfSign(providerDAO.getProviderGiven_Lastname(sessionManager.getProviderID()));
//        Log.v("chwdetails", "chwdetails: " + new Gson().toJson(doctorDetails));
//        // doctorDetails.setWhatsapp("7005308163");
//        // doctorDetails.setPhoneNumber("7005308163");
//
//        String drDetails = new Gson().toJson(doctorDetails);
//        List<Ob> obList = new ArrayList<>();
//        Ob ob = new Ob();
//        ob.setConcept(OBS_DOCTORDETAILS);
//        ob.setValue(drDetails);
//        Log.v("drdetail", "drdetail: " + drDetails);
//        obList.add(ob);
//
//        List<EncounterProvider> encounterProviderList = new ArrayList<>();
//        EncounterProvider encounterProvider = new EncounterProvider();
//        encounterProvider.setEncounterRole(ENCOUNTER_DR_ROLE); // Constant
////        encounterProvider.setProvider(ENCOUNTER_DR_PROVIDER); // user setup app provider
//        encounterProvider.setProvider(sessionManager.getProviderID()); // user setup app provider
//        encounterProviderList.add(encounterProvider);
//
//        EndVisitEncounterPrescription datamodel = new EndVisitEncounterPrescription();
//        datamodel.setPatient(patientUuid);
//        datamodel.setEncounterProviders(encounterProviderList);
//        datamodel.setVisit(visitUuid);
//        datamodel.setEncounterDatetime(AppConstants.dateAndTimeUtils.currentDateTime());
//        datamodel.setEncounterType(ENCOUNTER_VISIT_COMPLETE);
//        datamodel.setObs(obList);
//
//        Log.v("presbody", "newsubmit: " + new Gson().toJson(datamodel));
//        return datamodel;
//    }

    private boolean uploadPrescriptionData(String data, String CONCEPTUUID) {
        isuploadPrescription = false;
        // String encoded = sessionManager.getEncoded();
        String encoded = base64Utils.encoded("sysnurse", "Nurse123");
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        ObsPrescription prescription = getObsPrescription(AppConstants.dateAndTimeUtils.currentDateTime(), data, CONCEPTUUID);
        Observable<ObsPrescResponse> resultsObservable = apiService.OBS_PRESCRIPTION_UPLOAD(OBSURL, prescription, "Basic " + encoded);
        resultsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ObsPrescResponse>() {
                    @Override
                    public void onNext(@NonNull ObsPrescResponse obsPrescResponse) {
                        // Response of successful uploaded data.
                        String uuid = obsPrescResponse.getUuid();
                        setObsData(uuid, CONCEPTUUID, data);
                        updateRecyclerView(CONCEPTUUID, uuid, data, encounterVisitNote);
                        isuploadPrescription = true;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        isuploadPrescription = false;
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        return isuploadPrescription;
    }

    private void updateRecyclerView(String conceptuuid, String uuid, String data, String encounterVisitNote) {
        switch (conceptuuid) {
            case TELEMEDICINE_DIAGNOSIS:
                diagnosisList.add(new PrescDataModel(uuid, data, encounterVisitNote, conceptuuid));
                diagnosisPrescAdapter.notifyDataSetChanged();
                break;
            case JSV_MEDICATIONS:
                medicationList.add(new PrescDataModel(uuid, data, encounterVisitNote, conceptuuid));
                medicationPrescAdapter.notifyDataSetChanged();
                break;
            case REQUESTED_TESTS:
                testList.add(new PrescDataModel(uuid, data, encounterVisitNote, conceptuuid));
                testPrescAdapter.notifyDataSetChanged();
                break;
            case MEDICAL_ADVICE:
                adviceList.add(new PrescDataModel(uuid, data, encounterVisitNote, conceptuuid));
                advicePrescAdapter.notifyDataSetChanged();
                break;
            case FOLLOW_UP_VISIT:
                followupList.add(new PrescDataModel(uuid, data, encounterVisitNote, conceptuuid));
                followupPrescAdapter.notifyDataSetChanged();
                break;
            default:
                // do nothing...
        }
    }

    private ObsPrescription getObsPrescription(String currentDateTime, String diagnosisData, String CONCEPT_UUID) {
        ObsPrescription obsPrescription = new ObsPrescription();
        obsPrescription.setConcept(CONCEPT_UUID);
        obsPrescription.setPerson(patientUuid);
        obsPrescription.setObsDatetime(currentDateTime);
        obsPrescription.setValue(diagnosisData);
        obsPrescription.setEncounter(encounterVisitNote);

        return obsPrescription;
    }

    private boolean uploadCallWithPatientData() {
        boolean isUpload = false;

        return isUpload;
    }

    private void assignDatePicker(EditText etFollowUpDate) {
        Calendar today = Calendar.getInstance();
        int mDOBYear = today.get(Calendar.YEAR);
        int mDOBMonth = today.get(Calendar.MONTH);
        int mDOBDay = today.get(Calendar.DAY_OF_MONTH);
        //DOB is set using an AlertDialog
        // Locale.setDefault(Locale.ENGLISH);

        DatePickerDialog mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat simpleDateFormat = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag(appLanguage));
                        } else {
                            simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        String format = simpleDateFormat.format(calendar.getTime());
                        etFollowUpDate.setText(format);
                    }
                }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        // mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDOBPicker.getDatePicker().setMinDate(System.currentTimeMillis() - 10000); // So that in Followup all the dates from today and future will be enabled to be selected.
        etFollowUpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFollowUpDate.setError(null);
                mDOBPicker.show();
            }
        });
    }

    private void addResult(LinearLayout llResult, String result) {
        TextView tvResult = new TextView(context);
        tvResult.setTextColor(ContextCompat.getColor(context, R.color.font_black_0));
        tvResult.setTypeface(ResourcesCompat.getFont(context, R.font.lato_regular));
        int dimension = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );
        tvResult.setPadding(dimension, dimension, dimension, dimension);
        tvResult.setBackgroundResource(R.drawable.rounded_rectangle_orange);
        tvResult.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvResult.setText(result);
        llResult.addView(tvResult);
        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dimension / 2));
        llResult.addView(space);
    }

    /**
     * @param uuid
     * @param conceptUid
     * @param value
     * @return This function will add in the local db of obs table the value that user entered so that we can then use it for Delete operation...
     */
    boolean setObsData(String uuid, String conceptUid, String value) {
        boolean isInserted = false;
        ObsDTO obsDTO = new ObsDTO();
        try {
            obsDTO.setUuid(uuid);
            obsDTO.setEncounteruuid(encounterVisitNote); //encounter of Start Visit Note api response.
            obsDTO.setValue(value);
            obsDTO.setConceptuuid(conceptUid);
            isInserted = obsDAO.insertPrescObs(obsDTO);
        } catch (DAOException e) {
            e.printStackTrace();
            isInserted = false;
        }
        return isInserted;
    }

    private List<String> getResult(LinearLayout llResult) {
        List<String> result = new ArrayList<>();
        if (llResult != null) {
            for (int i = 0; i < llResult.getChildCount(); i++) {
                View childAt = llResult.getChildAt(i);
                if (childAt instanceof TextView) {
                    result.add(((TextView) childAt).getText().toString());
                }
            }
        }
        return result;
    }

    private void endVisit() {
        Log.d(TAG, "endVisit: ");
        if (visitUUID == null || visitUUID.isEmpty()) {
            String visitIDorderBy = "startdate";
            String visitIDSelection = "uuid = ?";
            String[] visitIDArgs = {visitUuid};
            final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
            if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
                visitIDCursor.moveToFirst();
                visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
            }
            if (visitIDCursor != null) visitIDCursor.close();
        }
        VisitUtils.endVisit(PrescriptionActivity.this, visitUuid, patientUuid, followUpDate, encounterVitals, encounterUuidAdultIntial, state, patientName, intentTag);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}