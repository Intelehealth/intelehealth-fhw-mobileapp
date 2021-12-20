package org.intelehealth.unicef.activities.presription;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.database.dao.ObsDAO;
import org.intelehealth.unicef.models.dto.ObsDTO;
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

    SQLiteDatabase db;
    private SessionManager sessionManager;
    private String appLanguage;
    private String followUpDate;
    private String encounterVitals;
    private String encounterUuidAdultIntial;
    ObsDAO obsDAO = new ObsDAO();

    private Context mContext;

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

        final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientGender = intent.getStringExtra("gender");
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
        btnCallSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton checked = findViewById(rgCall.getCheckedRadioButtonId());
                String result = checked.getText().toString();
                addResult(llCallResult, result);
                btnCallSubmit.setEnabled(false);
                btnCallSubmit.setAlpha(0.4f);
                rgCall.clearCheck();
                setObsData(encounterUuidAdultIntial, UuidDictionary.MEDICAL_ADVICE, result);
                uploadCallWithPatientData();
            }
        });


        //diagnosis
        LinearLayout llDiagnosisResult = findViewById(R.id.llDiagnosisResult);
        RadioGroup rgDiagnosis1 = findViewById(R.id.rgDiagnosis1);
        RadioGroup rgDiagnosis2 = findViewById(R.id.rgDiagnosis2);
        Button btnAddDiagnosis = findViewById(R.id.btnAddDiagnosis);
        EditText etDiagnosis = findViewById(R.id.etDiagnosis);
        btnAddDiagnosis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etDiagnosis.getText().toString())) {
                    RadioButton checked1 = findViewById(rgDiagnosis1.getCheckedRadioButtonId());
                    RadioButton checked2 = findViewById(rgDiagnosis2.getCheckedRadioButtonId());
                    if (checked1 != null && checked2 != null) {
                        String result = String.format("%s:%s & %s", etDiagnosis.getText(), checked1.getText(), checked2.getText());
                        addResult(llDiagnosisResult, result);
                        setObsData(encounterUuidAdultIntial, UuidDictionary.TELEMEDICINE_DIAGNOSIS, result);
                        etDiagnosis.setText("");
                        rgDiagnosis1.clearCheck();
                        rgDiagnosis2.clearCheck();
                    }
                }
            }
        });



        //meds
        LinearLayout llMedsResult = findViewById(R.id.llMedsResult);
        AutoCompleteTextView etMedication = findViewById(R.id.etMedication);
        etMedication.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.medications)));
        EditText etMedicationStrength = findViewById(R.id.etMedicationStrength);
        EditText etMedicationUnit = findViewById(R.id.etMedicationUnit);
        EditText etMedicationAmount = findViewById(R.id.etMedicationAmount);
        EditText etMedicationUnitTabSyrup = findViewById(R.id.etMedicationUnitTabSyrup);
        EditText etMedicationFreq = findViewById(R.id.etMedicationFreq);
        EditText etMedicationRoute = findViewById(R.id.etMedicationRoute);
        EditText etMedicationDuration = findViewById(R.id.etMedicationDuration);
        EditText etMedicationUnitsDays = findViewById(R.id.etMedicationUnitsDays);
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
                    setObsData(encounterUuidAdultIntial, UuidDictionary.JSV_MEDICATIONS, result);
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
                }
            }
        });


        //test
        LinearLayout llTestResult = findViewById(R.id.llTestResult);
        AutoCompleteTextView etTest = findViewById(R.id.etTest);
        Button btnAddTest = findViewById(R.id.btnAddTest);
        btnAddTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etTest.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    addResult(llTestResult, result);
                    setObsData(encounterUuidAdultIntial, UuidDictionary.REQUESTED_TESTS, result);
                    etTest.setText("");
                }
            }
        });


        //advice
        LinearLayout llAdviceResult = findViewById(R.id.llAdviceResult);
        AutoCompleteTextView etAdviceRefer = findViewById(R.id.etAdviceRefer);
        Button btnAddAdvice = findViewById(R.id.btnAddAdvice);
        btnAddAdvice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etAdviceRefer.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    addResult(llAdviceResult, result);
                    setObsData(encounterUuidAdultIntial, UuidDictionary.MEDICAL_ADVICE, result);
                    etAdviceRefer.setText("");
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
                    setObsData(encounterUuidAdultIntial, UuidDictionary.ADDITIONAL_COMMENTS, result);
                    etNotes.setText("");
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
                    setObsData(encounterUuidAdultIntial, UuidDictionary.FOLLOW_UP_VISIT, result);
                    etFollowUpDate.setText("");
                    etFollowUpRemark.setText("");
                }
            }
        });
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
