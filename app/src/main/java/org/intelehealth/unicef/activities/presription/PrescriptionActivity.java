package org.intelehealth.unicef.activities.presription;

import static org.intelehealth.unicef.utilities.UuidDictionary.ADDITIONAL_COMMENTS;
import static org.intelehealth.unicef.utilities.UuidDictionary.ENCOUNTER_ROLE;
import static org.intelehealth.unicef.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.unicef.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;
import static org.intelehealth.unicef.utilities.UuidDictionary.FOLLOW_UP_VISIT;
import static org.intelehealth.unicef.utilities.UuidDictionary.JSV_MEDICATIONS;
import static org.intelehealth.unicef.utilities.UuidDictionary.MEDICAL_ADVICE;
import static org.intelehealth.unicef.utilities.UuidDictionary.OBS_DOCTORDETAILS;
import static org.intelehealth.unicef.utilities.UuidDictionary.REQUESTED_TESTS;
import static org.intelehealth.unicef.utilities.UuidDictionary.TELEMEDICINE_DIAGNOSIS;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.homeActivity.HomeActivity;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.database.dao.ObsDAO;
import org.intelehealth.unicef.models.ClsDoctorDetails;
import org.intelehealth.unicef.models.dto.ObsDTO;
import org.intelehealth.unicef.models.prescriptionUpload.EncounterProvider;
import org.intelehealth.unicef.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.unicef.models.prescriptionUpload.EndVisitResponseBody;
import org.intelehealth.unicef.models.prescriptionUpload.Ob;
import org.intelehealth.unicef.models.prescriptionUpload.ObsPrescription;
import org.intelehealth.unicef.networkApiCalls.ApiClient;
import org.intelehealth.unicef.networkApiCalls.ApiInterface;
import org.intelehealth.unicef.utilities.LocaleHelper;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.UuidDictionary;
import org.intelehealth.unicef.utilities.VisitUtils;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class PrescriptionActivity extends AppCompatActivity {

    private static final String TAG = PrescriptionActivity.class.getSimpleName();

    Context context;

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
        context = getApplicationContext();


        initUI();
    }

    private void initUI() {
        //call
        LinearLayout llCallResult = findViewById(R.id.llCallResult);
        RadioGroup rgCall = findViewById(R.id.rgCall);
        Button btnCallSubmit = findViewById(R.id.btnCallSubmit);
        Button btnSignSubmit = findViewById(R.id.btnSignSubmit);

        btnCallSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton checked = findViewById(rgCall.getCheckedRadioButtonId());
                String result = checked.getText().toString();
                addResult(llCallResult, result);
                btnCallSubmit.setEnabled(false);
                btnCallSubmit.setAlpha(0.4f);
                rgCall.clearCheck();
                setObsData(encounterUuidAdultIntial, MEDICAL_ADVICE, result);
                uploadCallWithPatientData();
            }
        });


        //diagnosis
        LinearLayout llDiagnosisResult = findViewById(R.id.llDiagnosisResult);
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
                    RadioButton checked1 = findViewById(rgDiagnosis1.getCheckedRadioButtonId());
                    RadioButton checked2 = findViewById(rgDiagnosis2.getCheckedRadioButtonId());
                    if (checked1 != null && checked2 != null) {
                        String result = String.format("%s:%s & %s", etDiagnosis.getText(), checked1.getText(), checked2.getText());
                        addResult(llDiagnosisResult, result);
                        setObsData(encounterUuidAdultIntial, TELEMEDICINE_DIAGNOSIS, result);
                        etDiagnosis.setText("");
                        rgDiagnosis1.clearCheck();
                        rgDiagnosis2.clearCheck();

                        // Api call will upload this piece of data...
                        uploadPrescriptionData(result, TELEMEDICINE_DIAGNOSIS);
                    }
                }
            }
        });


        //meds
        LinearLayout llMedsResult = findViewById(R.id.llMedsResult);
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

        EditText etMedicationFreq = findViewById(R.id.etMedicationFreq);
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
                        &&!TextUtils.isEmpty(etMedicationStrength.getText().toString())
                        &&!TextUtils.isEmpty(etMedicationUnit.getText().toString())
                        &&!TextUtils.isEmpty(etMedicationAmount.getText().toString())
                        &&!TextUtils.isEmpty(etMedicationUnitTabSyrup.getText().toString())
                        &&!TextUtils.isEmpty(etMedicationFreq.getText().toString())
                        &&!TextUtils.isEmpty(etMedicationDuration.getText().toString())
                        &&!TextUtils.isEmpty(etMedicationUnitsDays.getText().toString())
                ) {
                    String result = String.format("%s: %s %s %s %s %s %s %s for %s %s %s"
                            ,etMedication.getText()
                            ,etMedicationStrength.getText()
                            ,etMedicationUnit.getText()
                            ,etMedicationAmount.getText()
                            ,etMedicationUnitTabSyrup.getText()
                            ,etMedicationFreq.getText()
                            ,etMedicationRoute.getText()
                            ,etMedicationReason.getText()
                            ,etMedicationDuration.getText()
                            ,etMedicationUnitsDays.getText()
                            ,etMedicationInstructions.getText()
                            );
                    addResult(llMedsResult, result);
                    setObsData(encounterUuidAdultIntial, JSV_MEDICATIONS, result);
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
                }
            }
        });

        //test
        LinearLayout llTestResult = findViewById(R.id.llTestResult);
        AutoCompleteTextView etTest = findViewById(R.id.etTest);
        etTest.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.tests)));
        etTest.setThreshold(1);

        Button btnAddTest = findViewById(R.id.btnAddTest);
        btnAddTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etTest.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    addResult(llTestResult, result);
                    setObsData(encounterUuidAdultIntial, REQUESTED_TESTS, result);
                    etTest.setText("");
                    uploadPrescriptionData(result, REQUESTED_TESTS);
                }
            }
        });

        //advice
        LinearLayout llAdviceResult = findViewById(R.id.llAdviceResult);
        AutoCompleteTextView etAdviceRefer = findViewById(R.id.etAdviceRefer);
        etAdviceRefer.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.advice)));
        etAdviceRefer.setThreshold(1);

        Button btnAddAdvice = findViewById(R.id.btnAddAdvice);
        btnAddAdvice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etAdviceRefer.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    addResult(llAdviceResult, result);
                    setObsData(encounterUuidAdultIntial, MEDICAL_ADVICE, result);
                    etAdviceRefer.setText("");
                    uploadPrescriptionData(result, MEDICAL_ADVICE);
                }
            }
        });

        //notes
        LinearLayout llNotesResult = findViewById(R.id.llNotesResult);
        AutoCompleteTextView etNotes = findViewById(R.id.etNotes);
        Button btnAddNote = findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etNotes.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    addResult(llNotesResult, result);
                    setObsData(encounterUuidAdultIntial, ADDITIONAL_COMMENTS, result);
                    etNotes.setText("");
                    uploadPrescriptionData(result, ADDITIONAL_COMMENTS);
                }
            }
        });

        //follow up
        LinearLayout llFollowUpResult = findViewById(R.id.llFollowUpResult);
        EditText etFollowUpDate = findViewById(R.id.etFollowUpDate);
        assignDatePicker(etFollowUpDate);
        EditText etFollowUpRemark = findViewById(R.id.etFollowUpRemark);
        Button btnFollowUp = findViewById(R.id.btnFollowUp);
        btnFollowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etFollowUpDate.getText().toString())) {
                    llFollowUpResult.removeAllViews();
                    String result = etFollowUpDate.getText().toString();
                    if (!TextUtils.isEmpty(etFollowUpRemark.getText()))
                        result += ", Remarks: " + etFollowUpRemark.getText();
                    addResult(llFollowUpResult, result);
                    setObsData(encounterUuidAdultIntial, FOLLOW_UP_VISIT, result);
                    etFollowUpDate.setText("");
                    etFollowUpRemark.setText("");
                    uploadPrescriptionData(result, FOLLOW_UP_VISIT);
                }
            }
        });

        btnSignSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here, prescription is given just need to pass the Visit Complete encounter to update the status of the visit on webapp...
                String url = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/encounter";
                visitCompleteStatus = getVisitCompleteDataModel();
                String encoded = sessionManager.getEncoded();

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
                                Intent intent = new Intent(PrescriptionActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Log.e("pres", "signandsubmit: "+ e);
                            }

                            @Override
                            public void onComplete() {
                                Log.e("pres", "signandsubmitcomplete: ");
                            }
                        });
            }
        });
    }

    private EndVisitEncounterPrescription getVisitCompleteDataModel() {
        ClsDoctorDetails doctorDetails = new ClsDoctorDetails();
        doctorDetails.setName("Demo doctor1");
        doctorDetails.setPhoneNumber("7005308163");
        doctorDetails.setWhatsapp("7005308163");
        doctorDetails.setSpecialization("Neurologist");
        doctorDetails.setFontOfSign("Pacifico");
        doctorDetails.setTextOfSign("Dr. Demo 1");

        List<Ob> obList = new ArrayList<>();
        Ob ob = new Ob();
        ob.setConcept(OBS_DOCTORDETAILS);
        ob.setValue(doctorDetails);
        obList.add(ob);

        List<EncounterProvider> encounterProviderList = new ArrayList<>();
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setEncounterRole(ENCOUNTER_ROLE); // Constant
        encounterProvider.setProvider(sessionManager.getProviderID()); // user setup app provider
        encounterProviderList.add(encounterProvider);

        EndVisitEncounterPrescription datamodel = new EndVisitEncounterPrescription();
        datamodel.setPatient(patientUuid);
        datamodel.setEncounterProviders(encounterProviderList);
        datamodel.setVisit(visitUUID);
        datamodel.setEncounterDatetime(AppConstants.dateAndTimeUtils.currentDateTime());
        datamodel.setEncounterType(ENCOUNTER_VISIT_COMPLETE);
        datamodel.setObs(obList);

        Log.v("presbody", "newsubmit: "+ new Gson().toJson(datamodel));
        return datamodel;
    }

    private boolean uploadPrescriptionData(String data, String CONCEPTUUID) {
        boolean isupload = false;
        String encoded = sessionManager.getEncoded();
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        ObsPrescription prescription = getObsPrescription(AppConstants.dateAndTimeUtils.currentDateTime(), data, CONCEPTUUID);
        Observable<ResponseBody> resultsObservable = apiService.OBS_PRESCRIPTION_UPLOAD(OBSURL, prescription, "Basic " + encoded);
        resultsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        // Response of successful uploaded data.
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        return isupload;
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

        DatePickerDialog mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                String format = simpleDateFormat.format(calendar.getTime());
                etFollowUpDate.setText(format);
            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        etFollowUpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    void setObsData(String encounterUid, String conceptUid, String value) {
        ObsDTO obsDTO = new ObsDTO();
        try {
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounterUid);
            obsDTO.setValue(value);
            obsDTO.setConceptuuid(conceptUid);
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }
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
}
