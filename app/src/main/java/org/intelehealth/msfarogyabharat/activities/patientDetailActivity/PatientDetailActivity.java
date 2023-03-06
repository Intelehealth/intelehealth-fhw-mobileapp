package org.intelehealth.msfarogyabharat.activities.patientDetailActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


import org.apache.commons.lang3.StringUtils;
import org.intelehealth.msfarogyabharat.database.dao.SendCallDataDAO;
import org.intelehealth.msfarogyabharat.database.dao.SyncDAO;
import org.intelehealth.msfarogyabharat.models.SendCallData;
import org.intelehealth.msfarogyabharat.models.dto.PatientAttributesDTO;
import org.intelehealth.msfarogyabharat.models.dto.PatientDTO;
import org.intelehealth.msfarogyabharat.networkApiCalls.ApiInterface;
import org.intelehealth.msfarogyabharat.widget.materialprogressbar.CustomProgressDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.homeActivity.HomeActivity;
import org.intelehealth.msfarogyabharat.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.msfarogyabharat.activities.medicaladvice.MedicalAdviceExistingPatientsActivity;
import org.intelehealth.msfarogyabharat.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.msfarogyabharat.activities.vitalActivity.VitalsActivity;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.InteleHealthDatabaseHelper;
import org.intelehealth.msfarogyabharat.database.dao.EncounterDAO;
import org.intelehealth.msfarogyabharat.database.dao.ImagesDAO;
import org.intelehealth.msfarogyabharat.database.dao.PatientsDAO;
import org.intelehealth.msfarogyabharat.database.dao.VisitsDAO;
import org.intelehealth.msfarogyabharat.knowledgeEngine.Node;
import org.intelehealth.msfarogyabharat.models.FamilyMemberRes;
import org.intelehealth.msfarogyabharat.models.Patient;
import org.intelehealth.msfarogyabharat.models.WelcomeSms;
import org.intelehealth.msfarogyabharat.models.dto.EncounterDTO;
import org.intelehealth.msfarogyabharat.models.dto.VisitDTO;
import org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils;
import org.intelehealth.msfarogyabharat.utilities.DownloadFilesUtils;
import org.intelehealth.msfarogyabharat.utilities.FileUtils;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.NetworkConnection;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.utilities.UuidDictionary;

import org.intelehealth.msfarogyabharat.activities.homeActivity.HomeActivity;
import org.intelehealth.msfarogyabharat.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.msfarogyabharat.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.msfarogyabharat.activities.vitalActivity.VitalsActivity;
import org.intelehealth.msfarogyabharat.utilities.NetworkConnection;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import static org.intelehealth.msfarogyabharat.utilities.StringUtils.en__as_dob;
import static org.intelehealth.msfarogyabharat.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.msfarogyabharat.utilities.StringUtils.en__or_dob;

//import static org.intelehealth.ekalhelpline.utilities.StringUtils.en__as_dob;

public class PatientDetailActivity extends AppCompatActivity {
    private static final String TAG = PatientDetailActivity.class.getSimpleName();
    public static final String EXTRA_SHOW_MEDICAL_ADVICE = "EXTRA_SHOW_MEDICAL_ADVICE";

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ExecutorService familyHistoryExecutorService = Executors.newSingleThreadExecutor();
    ExecutorService patientHistoryExecutorService = Executors.newSingleThreadExecutor();
    ExecutorService pasVisitsExecutorService = Executors.newSingleThreadExecutor();
    TextView patinetName, dobView, ageView, genderView, addr1View, addr2View, addrFinalView, callerRelation, helplineInfo, phoneType, casteView, economic_statusView, education_statusView, phoneView, sdwView, occuView, medHistView, famHistView;

    TableRow addr2Row, sdwRow, occuRow, economicRow, educationRow, casteRow;
    ImageView whatsapp_no, calling;

    String patientName;
    String visitUuid = null;
    List<String> visitUuidList;
    String patientUuid;
    String intentTag = "";
    String intentTag1 = "";
    String intentTag2 = "";
    String profileImage = "";
    String profileImage1 = "";
    SessionManager sessionManager = null;
    Patient patient_new = new Patient();

    EncounterDTO encounterDTO = new EncounterDTO();
    PatientsDAO patientsDAO = new PatientsDAO();
    private boolean hasLicense = false;
    private boolean returning;

    String phistory = "";
    String fhistory = "";
    LinearLayout previousVisitsList;
    String visitValue;
    private String encounterVitals = "";
    private String encounterAdultIntials = "";
    SQLiteDatabase db = null;
    ImageButton editbtn;
    ImageButton ib_addFamilyMember;
    Button newVisit, newAdvice;
    IntentFilter filter;
    Myreceiver reMyreceive;
    ImageView photoView;
    ImagesDAO imagesDAO = new ImagesDAO();
    TextView idView;
    RecyclerView rvFamilyMember;
    TextView tvNoFamilyMember;
    String privacy_value_selected;
    String remark = "Field not required for MSF";
    RadioButton mIncoming;
    RadioButton mOutgoing;
    String mCallType = "";
    RadioGroup callRadioGrp;
    Button saveButton;
    PatientDTO patientdto = new PatientDTO();
    ImageView ivPrescription;
    private String hasPrescription = "";
    Context context;
    float float_ageYear_Month;
    private boolean isMedicalAdvice;
    private boolean MedicalAdvice = false;
    String callStatus = "Incoming";
    String callAction = "Incoming";
    String callStartTime = "Incoming";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        reMyreceive = new Myreceiver();
        filter = new IntentFilter("OpenmrsID");
        newVisit = findViewById(R.id.button_new_visit);
        newAdvice = findViewById(R.id.btn_new_advice);
//        rvFamilyMember = findViewById(R.id.rv_familymember);
//        tvNoFamilyMember = findViewById(R.id.tv_nofamilymember);
        context = PatientDetailActivity.this;

        ivPrescription = findViewById(R.id.iv_prescription);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            patientName = intent.getStringExtra("patientName");
            hasPrescription = intent.getStringExtra("hasPrescription");
            MedicalAdvice = intent.getBooleanExtra("MedicalAdvice", false);
            privacy_value_selected = intent.getStringExtra("privacy"); //intent value from IdentificationActivity.
            String phoneNumber = intent.getStringExtra("phoneNumber");
            if (!TextUtils.isEmpty(phoneNumber)) {
                sendWelcomeSms(phoneNumber);
            }

            intentTag = intent.getStringExtra("tag");
            intentTag1 = intent.getStringExtra("intentTag1");
            intentTag2 = intent.getStringExtra("intentTag2");
            Logger.logD(TAG, "Patient ID: " + patientUuid);
            Logger.logD(TAG, "Patient Name: " + patientName);
            Logger.logD(TAG, "Intent Tag: " + intentTag);
            Logger.logD(TAG, "Privacy Value on (PatientDetail): " + privacy_value_selected);

            if (intentTag2 != null && intentTag2.equalsIgnoreCase("findPatient") && intentTag1 != null && intentTag1.equalsIgnoreCase("editDetails")) {
                newAdvice.setVisibility(View.VISIBLE);
                newAdvice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MedicalAdviceExistingPatientsActivity.start(PatientDetailActivity.this, patientUuid);
                    }
                });

            }

        }

        if (hasPrescription.equalsIgnoreCase("true")) {
            ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
        }

        editbtn = findViewById(R.id.edit_button);
        //   ib_addFamilyMember = findViewById(R.id.ic_addFamilyMember);
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(PatientDetailActivity.this, IdentificationActivity.class);
                intent2.putExtra("patientUuid", patientUuid);
                intent2.putExtra("intentTag1", "editDetails");
                intent2.putExtra("intentTag2", intentTag2);
                startActivity(intent2);

            }
        });
/*
        ib_addFamilyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String houseHoldValue = "";
                try {
                    houseHoldValue = patientsDAO.getHouseHoldValue(patientUuid);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                Log.e("houseHOLDID", houseHoldValue);

                sessionManager.setHouseholdUuid(houseHoldValue);
                Intent i = new Intent(PatientDetailActivity.this, IdentificationActivity.class);
                i.putExtra("privacy", "Accept");
                startActivity(i);
            }
        });
*/

        setData();
        setDisplay(patientUuid);

        if (newVisit.isEnabled()) {
            newVisit.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            newVisit.setTextColor(getResources().getColor(R.color.white));
        } else {
            //newVisit.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            //newVisit.setTextColor(getResources().getColor(R.color.white));
        }

        if (MedicalAdvice == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PatientDetailActivity.this).setMessage(R.string.text_patient_and_advice_created).setCancelable(false).setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(PatientDetailActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

            Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setTextColor(getResources().getColor(R.color.colorPrimary));
            //  positive.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        callRadioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                saveButton.setText(getResources().getString(R.string.button_save));
                if (i == R.id.identification_incoming) {
                    mCallType = "Incoming";
                } else if (i == R.id.identification_outgoing) {
                    mCallType = "Outgoing";
                }
                saveButton.setEnabled(true);
                saveButton.setClickable(true);
                saveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeData();
            }
        });

        newVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (callAction.equalsIgnoreCase("Incoming") && callStatus.equalsIgnoreCase("Incoming") && callStartTime.equalsIgnoreCase("Incoming"))
                    storeCallData(callStatus, callAction, remark);
                // before starting, we determine if it is new visit for a returning patient
                // extract both FH and PMH
                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
                Date todayDate = new Date();
                String thisDate = currentDate.format(todayDate);


                String uuid = UUID.randomUUID().toString();
                EncounterDAO encounterDAO = new EncounterDAO();
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(UUID.randomUUID().toString());
                encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS"));
                encounterDTO.setEncounterTime(thisDate);
                encounterDTO.setVisituuid(uuid);
                encounterDTO.setSyncd(false);
                encounterDTO.setProvideruuid(sessionManager.getProviderID());
                Log.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
                encounterDTO.setVoided(0);
                encounterDTO.setPrivacynotice_value(privacy_value_selected);//privacy value added.

                try {
                    encounterDAO.createEncountersToDB(encounterDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                InteleHealthDatabaseHelper mDatabaseHelper = new InteleHealthDatabaseHelper(PatientDetailActivity.this);
                SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getReadableDatabase();

                String CREATOR_ID = sessionManager.getCreatorID();
                returning = false;
                sessionManager.setReturning(returning);

                String[] cols = {"value"};
                Cursor cursor = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for PMH (Past Medical History)
                        new String[]{encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB}, null, null, null);

                if (cursor.moveToFirst()) {
                    // rows present
                    do {
                        // so that null data is not appended
                        phistory = phistory + cursor.getString(0);

                    } while (cursor.moveToNext());
                    returning = true;
                    sessionManager.setReturning(returning);
                }
                cursor.close();

                Cursor cursor1 = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for FH (Family History)
                        new String[]{encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB}, null, null, null);
                if (cursor1.moveToFirst()) {
                    // rows present
                    do {
                        fhistory = fhistory + cursor1.getString(0);
                    } while (cursor1.moveToNext());
                    returning = true;
                    sessionManager.setReturning(returning);
                }
                cursor1.close();

                // Will display data for patient as it is present in database
                // Toast.makeText(PatientDetailActivity.this,"PMH: "+phistory,Toast.LENGTH_SHORT).sƒhow();
                // Toast.makeText(PatientDetailActivity.this,"FH: "+fhistory,Toast.LENGTH_SHORT).show();

                Intent intent2 = new Intent(PatientDetailActivity.this, VitalsActivity.class);
                String fullName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
                intent2.putExtra("patientUuid", patientUuid);

                VisitDTO visitDTO = new VisitDTO();

                visitDTO.setUuid(uuid);
                visitDTO.setPatientuuid(patient_new.getUuid());
                visitDTO.setStartdate(thisDate);
                visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
                visitDTO.setLocationuuid(sessionManager.getLocationUuid());
                visitDTO.setSyncd(false);
                visitDTO.setCreatoruuid(sessionManager.getCreatorID());//static
                VisitsDAO visitsDAO = new VisitsDAO();

                try {
                    visitsDAO.insertPatientToDB(visitDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                // visitUuid = String.valueOf(visitLong);
//                localdb.close();
                intent2.putExtra("patientUuid", patientUuid);
                intent2.putExtra("visitUuid", uuid);
                intent2.putExtra("encounterUuidVitals", encounterDTO.getUuid());
                intent2.putExtra("encounterUuidAdultIntial", "");
                intent2.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
                intent2.putExtra("name", fullName);
                intent2.putExtra("tag", "new");
                intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
                startActivity(intent2);
            }
        });

        //  LoadFamilyMembers();
        if (intent != null && intent.getBooleanExtra(EXTRA_SHOW_MEDICAL_ADVICE, false)) {
            newAdvice.setVisibility(View.VISIBLE);
            newAdvice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callAction.equalsIgnoreCase("Incoming") && callStatus.equalsIgnoreCase("Incoming") && callStartTime.equalsIgnoreCase("Incoming"))
                        storeCallData(callStatus, callAction, remark);
                    MedicalAdviceExistingPatientsActivity.start(PatientDetailActivity.this, patientUuid);
                }
            });
        }

    }

    private void setData() {
        previousVisitsList = findViewById(R.id.linearLayout_previous_visits);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setEnabled(false);
        saveButton.setClickable(false);
        saveButton.setTextColor(getResources().getColor(R.color.divider));
        mIncoming = findViewById(R.id.identification_incoming);
        mOutgoing = findViewById(R.id.identification_outgoing);
        callRadioGrp = findViewById(R.id.radioGrp_callType);

        photoView = findViewById(R.id.imageView_patient);

        idView = findViewById(R.id.textView_ID);
        patinetName = findViewById(R.id.textView_name);
        dobView = findViewById(R.id.textView_DOB);
        ageView = findViewById(R.id.textView_age);
        genderView = findViewById(R.id.textView_gender);
        addr1View = findViewById(R.id.textView_address_1);
        addr2Row = findViewById(R.id.tableRow_addr2);
        addr2View = findViewById(R.id.textView_address2);
        addrFinalView = findViewById(R.id.textView_address_final);
        callerRelation = findViewById(R.id.textView_callerRelation);
        helplineInfo = findViewById(R.id.textView_helplineInfo);
        phoneType = findViewById(R.id.textView_phoneType);
        casteView = findViewById(R.id.textView_caste);
        economic_statusView = findViewById(R.id.textView_economic_status);
        education_statusView = findViewById(R.id.textView_education_status);
        phoneView = findViewById(R.id.textView_phone);
        sdwView = findViewById(R.id.textView_SDW);
        sdwRow = findViewById(R.id.tableRow_SDW);
        occuView = findViewById(R.id.textView_occupation);
        occuRow = findViewById(R.id.tableRow_Occupation);
        economicRow = findViewById(R.id.tableRow_Economic_Status);
        educationRow = findViewById(R.id.tableRow_Education_Status);
        casteRow = findViewById(R.id.tableRow_Caste);
        whatsapp_no = findViewById(R.id.whatsapp_no);
        calling = findViewById(R.id.calling);
        medHistView = findViewById(R.id.textView_patHist);
        famHistView = findViewById(R.id.textView_famHist);
    }

    private void storeData() {

        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid("8e48443f-c7aa-47b9-95b2-35e6d3e663d1");
        patientAttributesDTO.setValue(mCallType);
        patientAttributesDTOList.add(patientAttributesDTO);


        try {
            boolean isPatientUpdated = patientsDAO.updatePatientToDB(patient_new, patientUuid, patientAttributesDTOList);
            if (NetworkConnection.isOnline(getApplication())) {
                SyncDAO syncDAO = new SyncDAO();
                boolean ispush = syncDAO.pushDataApi();
                if (ispush) {
                    saveButton.setText(getResources().getString(R.string.saved));
                    saveButton.setTextColor(getResources().getColor(R.color.scale_5));
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeSms(String phoneNumber) {
        if (!NetworkConnection.isOnline(this)) {
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        if (((Activity) context).isFinishing()) return;

        if (TextUtils.isEmpty(phoneNumber)) return;
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.getSendSmsUrl();
        Call<ResponseBody> patientIvrCall = AppConstants.apiInterface.SEND_WELCOME_SMS(url, AppConstants.SMS_API_KEY, String.format("91%s", phoneNumber), "TIFDOC", "API", "TXN", AppConstants.SMS_TEMPLATE_ID, getString(R.string.welcome_sms));
        patientIvrCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void LoadFamilyMembers() {

        String houseHoldValue = "";
        try {
            houseHoldValue = patientsDAO.getHouseHoldValue(patientUuid);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (!houseHoldValue.equalsIgnoreCase("")) {
            //Fetch all patient UUID from houseHoldValue
            try {
                List<FamilyMemberRes> listPatientNames = new ArrayList<>();
                List<String> patientUUIDs = new ArrayList<>(patientsDAO.getPatientUUIDs(houseHoldValue));
                Log.e("patientUUIDs", "" + patientUUIDs);

                for (int i = 0; i < patientUUIDs.size(); i++) {
                    if (!patientUUIDs.get(i).equals(patientUuid)) {
                        listPatientNames.addAll(patientsDAO.getPatientName(patientUUIDs.get(i)));
                    }
                }

            /*    if (listPatientNames.size() > 0) {
                    tvNoFamilyMember.setVisibility(View.GONE);
                    rvFamilyMember.setVisibility(View.VISIBLE);
                    FamilyMemberAdapter familyMemberAdapter = new FamilyMemberAdapter(listPatientNames, this);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                    rvFamilyMember.setLayoutManager(linearLayoutManager);
                    rvFamilyMember.setAdapter(familyMemberAdapter);
                } else {
                    tvNoFamilyMember.setVisibility(View.VISIBLE);
                    rvFamilyMember.setVisibility(View.GONE);
                }*/

            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    @Override
    protected void onStart() {
        registerReceiver(reMyreceive, filter);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(reMyreceive);
        executorService.shutdownNow();
        patientHistoryExecutorService.shutdownNow();
        familyHistoryExecutorService.shutdownNow();
        pasVisitsExecutorService.shutdownNow();
        super.onDestroy();
    }

//    public float age_in_Decimal(String age) {
//        float age_float = 0;
//        //2 years 4 months 4 days
//        //int age_int = Integer.parseInt(age.replaceAll("[\\D]", "")); //244
//        String ageTrim = age.trim();
//        String year = String.valueOf(ageTrim.charAt(ageTrim.indexOf("years") - 1));
//        String month = String.valueOf(ageTrim.charAt(ageTrim.indexOf("months") - 1));
//        String result = year + " " + month;
//        //int month = age_.indexOf("months") - 1;
//
//        return age_float;
//    }

    public void setDisplay(String dataString) {
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(PatientDetailActivity.this);
        executorService.execute(() -> {
            runOnUiThread(customProgressDialog::show);

            String patientSelection = "uuid = ?";
            String[] patientArgs = {dataString};
            String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "postal_code", "country", "phone_number", "gender", "sdw", "patient_photo"};
            Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
            if (idCursor.moveToFirst()) {
                do {
                    patient_new.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    patient_new.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                    patient_new.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                    patient_new.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                    patient_new.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                    patient_new.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                    patient_new.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                    patient_new.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                    patient_new.setCity_village(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                    patient_new.setState_province(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                    patient_new.setPostal_code(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                    patient_new.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                    patient_new.setPhone_number(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                    patient_new.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    patient_new.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));

                } while (idCursor.moveToNext());
            }
            idCursor.close();

            String patientSelection1 = "patientuuid = ?";
            String[] patientArgs1 = {dataString};
            String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
            Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
            String name = "";
            if (idCursor1.moveToFirst()) {
                do {
                    try {
                        name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }

                    if (name.equalsIgnoreCase("CALL_TYPE")) {
                        patient_new.setCallType(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }

                    if (name.equalsIgnoreCase("caste")) {
                        patient_new.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("Telephone Number")) {
                        patient_new.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
              /*  if (name.equalsIgnoreCase("Education Level")) {
                    patient_new.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }*/
                    if (name.equalsIgnoreCase("Economic Status")) {
                        patient_new.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("occupation")) {
                        patient_new.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("Son/wife/daughter")) {
                        patient_new.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                /*if (name.equalsIgnoreCase("ProfileImageTimestamp")) {
                    profileImage1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                }*/

                } while (idCursor1.moveToNext());
            }
            idCursor1.close();

            runOnUiThread(() -> {
                if (!sessionManager.getLicenseKey().isEmpty()) {
                    hasLicense = true;
                }

                try {
                    JSONObject obj = null;
                    if (hasLicense) {
                        obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context), String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
                    } else {
                        obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
                    }

                    //Display the fields on the Add Patient screen as per the config file
//            if (obj.getBoolean("casteLayout")) {
//                casteRow.setVisibility(View.VISIBLE);
//            } else {
//                casteRow.setVisibility(View.GONE);
//            }
//            if (obj.getBoolean("educationLayout")) {
//                educationRow.setVisibility(View.VISIBLE);
//            } else {
//                educationRow.setVisibility(View.GONE);
//            }
//            if (obj.getBoolean("economicLayout")) {
//                economicRow.setVisibility(View.VISIBLE);
//            } else {
//                economicRow.setVisibility(View.GONE);
//            }

                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
                    Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
//            showAlertDialogButtonClicked(e.toString());
                }

//changing patient to patient_new object
                if (patient_new.getMiddle_name() == null) {
                    patientName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
                } else {
                    patientName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
                }

//        setTitle(patientName);
                patinetName.setText(patientName);
                try {
                    profileImage = imagesDAO.getPatientProfileChangeTime(patientUuid);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (patient_new.getPatient_photo() == null || patient_new.getPatient_photo().equalsIgnoreCase("")) {
                    if (NetworkConnection.isOnline(getApplication())) {
                        profilePicDownloaded();
                    }
                }
                if (!profileImage.equalsIgnoreCase(profileImage1)) {
                    if (NetworkConnection.isOnline(getApplication())) {
                        profilePicDownloaded();
                    }
                }
                Glide.with(getApplicationContext()).load(patient_new.getPatient_photo()).thumbnail(0.3f).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(photoView);

                if (patient_new.getOpenmrs_id() != null && !patient_new.getOpenmrs_id().isEmpty()) {
                    idView.setText(patient_new.getOpenmrs_id());
//            sessionManager.setOfllineOpenMRSID(patient_new.getOpenmrs_id());
                } else {
                    idView.setText(getString(R.string.patient_not_registered));
                }

//        if (!NetworkConnection.isOnline(getApplication())) {
//            if (!sessionManager.getOfllineOpenMRSID().equals("")) {
//                idView.setText(sessionManager.getOfllineOpenMRSID());
//            } else {
//                idView.setText(getString(R.string.patient_not_registered));
//            }
//        }

                setTitle(patient_new.getOpenmrs_id());
                //String id = idView.toString();
                //Log.d("IDEA","IDEA"+id);

                String age = DateAndTimeUtils.getAgeInYearMonth(patient_new.getDate_of_birth(), context);
                ageView.setText(age);
                float_ageYear_Month = DateAndTimeUtils.getFloat_Age_Year_Month(patient_new.getDate_of_birth());

                //dob to be displayed based on translation...
                String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patient_new.getDate_of_birth());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                    dobView.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                    dobView.setText(dob_text);
                }
/*
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            String dob_text = en__as_dob(dob); //to show text of English into Odiya...
            dobView.setText(dob_text);
        }
*/
                else {
                    dobView.setText(dob);
                }

                if (patient_new.getGender() != null) {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        if (patient_new.getGender().equalsIgnoreCase("M")) {
                            genderView.setText(getString(R.string.identification_screen_checkbox_male));
                        } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                            genderView.setText(getString(R.string.identification_screen_checkbox_female));
                        } else {
                            genderView.setText(patient_new.getGender());
                        }
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        if (patient_new.getGender().equalsIgnoreCase("M")) {
                            genderView.setText(getString(R.string.identification_screen_checkbox_male));
                        } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                            genderView.setText(getString(R.string.identification_screen_checkbox_female));
                        } else {
                            genderView.setText(patient_new.getGender());
                        }
                    } else {
                        genderView.setText(patient_new.getGender());
                    }
                }
                if (patient_new.getAddress1() == null || patient_new.getAddress1().equals("")) {
                    addr1View.setVisibility(View.GONE);
                } else {
                    addr1View.setText(patient_new.getAddress1());
                }
                if (patient_new.getAddress2() == null || patient_new.getAddress2().equals("")) {
                    addr2Row.setVisibility(View.GONE);
                } else {
                    addr2View.setText(patient_new.getAddress2());
                }
                String city_village;
                if (patient_new.getCity_village() != null) {
                    city_village = patient_new.getCity_village().trim();
                } else {
                    city_village = "";
                }

                if (patient_new.getPostal_code() != null) {
                    String addrFinalLine;
                    if (!patient_new.getPostal_code().equalsIgnoreCase("")) {
                        addrFinalLine = String.format("%s, %s, %s, %s", city_village, patient_new.getState_province(), patient_new.getPostal_code(), patient_new.getCountry());
                    } else {
                        addrFinalLine = String.format("%s, %s, %s", city_village, patient_new.getState_province(), patient_new.getCountry());
                    }
                    addrFinalView.setText(addrFinalLine);
                } else {
                    String addrFinalLine = String.format("%s, %s, %s", city_village, patient_new.getState_province(), patient_new.getCountry());
                    addrFinalView.setText(addrFinalLine);
                }

                phoneView.setText(patient_new.getPhone_number());

                if (patient_new.getCallType() != null) {
                    mCallType = patient_new.getCallType();
                    if (patient_new.getCallType().equals("Outgoing")) {
                        mOutgoing.setChecked(true);
                        if (mIncoming.isChecked()) mIncoming.setChecked(false);
                    } else {
                        mIncoming.setChecked(true);
                        if (mOutgoing.isChecked()) mOutgoing.setChecked(false);
                    }
                } else {
                    mIncoming.setChecked(false);
                    mOutgoing.setChecked(false);
                }

                callerRelation.setText(patient_new.getSdw());
                helplineInfo.setText(patient_new.getCaste());
                phoneType.setText(patient_new.getEconomic_status());

                //english = en
                //hindi = hi
                //education
      /*  if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            education_statusView.setText("नहीं दिया गया");
        } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            education_statusView.setText(patient_new.getEducation_level());
        } else {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String education = switch_hi_education_edit(patient_new.getEducation_level());
                education_statusView.setText(education);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                education_statusView.setText(patient_new.getEducation_level());
            } else {
                education_statusView.setText(patient_new.getEducation_level());
            }
            // education_statusView.setText(patient_new.getEducation_level());
        }*/

                //economic
     /*   if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            economic_statusView.setText("नहीं दिया गया");
        } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            economic_statusView.setText(patient_new.getEconomic_status());
        } else {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String economic = switch_hi_economic_edit(patient_new.getEconomic_status());
                economic_statusView.setText(economic);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                economic_statusView.setText(patient_new.getEconomic_status());
            } else {
                economic_statusView.setText(patient_new.getEconomic_status());
            }
            // economic_statusView.setText(patient_new.getEconomic_status());
        }*/

                //caste
      /*  if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            casteView.setText("नहीं दिया गया");
        } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            casteView.setText(patient_new.getCaste());
        } else {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String caste = switch_hi_caste_edit(patient_new.getCaste());
                casteView.setText(caste);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                casteView.setText(patient_new.getCaste());
            } else {
                casteView.setText(patient_new.getCaste());
            }
            // casteView.setText(patient_new.getCaste());
        }*/

//
      /*  if (patient_new.getSdw() != null && !patient_new.getSdw().equals("")) {
            sdwView.setText(patient_new.getSdw());
        } else {
            sdwRow.setVisibility(View.GONE);
        }*/
//
                if (patient_new.getOccupation() != null && !patient_new.getOccupation().equals("")) {
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                String occupation = switch_hi_occupation_edit(patient_new.getOccupation());
//                occuView.setText(occupation);
//            } else {
                    occuView.setText(patient_new.getOccupation());
//            }
                } else {
//            occuRow.setVisibility(View.GONE);
                    occuView.setText(getResources().getString(R.string.not_provided));
                }

                if (visitUuid != null && !visitUuid.isEmpty()) {
                    CardView histCardView = findViewById(R.id.cardView_history);
                    histCardView.setVisibility(View.GONE);
                } else {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        visitUuidList = new ArrayList<>();
                        String visitIDSelection = "patientuuid = ?";
                        String[] visitIDArgs = {patientUuid};
                        Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
                        if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                            do {
                                visitUuid = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                                visitUuidList.add(visitUuid);
                            } while (visitIDCursor.moveToNext());
                        }
                        if (visitIDCursor != null) {
                            visitIDCursor.close();
                        }
                        for (String visituuid : visitUuidList) {
                            Logger.logD(TAG, visituuid);
                            EncounterDAO encounterDAO = new EncounterDAO();
                            String encounterIDSelection = "visituuid = ?";
                            String[] encounterIDArgs = {visituuid};

                            Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                            if (encounterCursor != null && encounterCursor.moveToFirst()) {
                                do {
                                    if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                        encounterVitals = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                    }
                                    if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                        encounterAdultIntials = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                    }
                                } while (encounterCursor.moveToNext());
                            }
                            encounterCursor.close();
                        }
                        runOnUiThread(() -> {
                            familyHistory(famHistView, patientUuid, encounterAdultIntials);
                            pastMedicalHistory(medHistView, patientUuid, encounterAdultIntials);
                            pastVisits(patientUuid);
                        });
                    });
                }
                whatsapp_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String phoneNumberWithCountryCode = "+91" + phoneView.getText().toString();
//                String message =
//                        getString(R.string.hello_my_name_is) + " " + sessionManager.getChwname() + " " +
//                                /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);
                        String message = getString(R.string.hello_my_name_is) + sessionManager.getChwname() + getString(R.string.and_i_be_assisting_you);

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumberWithCountryCode, message))));
                    }
                });

                //mobile calling is supported...
                calling.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SimpleDateFormat startFormat = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm", Locale.ENGLISH);
                        Calendar today = Calendar.getInstance();
                        today.add(Calendar.MINUTE, -1);
                        today.set(Calendar.MILLISECOND, 0);
                        Date todayDate1 = today.getTime();
                        callStartTime = startFormat.format(todayDate1);

                /*Intent intent = new Intent(Intent.ACTION_DIAL); //ACTION_DIAL: doesnt requires permission...
                intent.setData(Uri.parse("tel:" + phoneView.getText().toString()));
                startActivity(intent);*/
                        callPatientViaIVR();
                    }
                });
                customProgressDialog.dismiss();
            });
        });
    }

    public void profilePicDownloaded() {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(patientUuid);
        Logger.logD(TAG, "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody file) {
                DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                downloadFilesUtils.saveToDisk(file, patientUuid);
                Logger.logD(TAG, file.toString());
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "complete" + patient_new.getPatient_photo());
                PatientsDAO patientsDAO = new PatientsDAO();
                boolean updated = false;
                try {
                    updated = patientsDAO.updatePatientPhoto(patientUuid, AppConstants.IMAGE_PATH + patientUuid + ".jpg");
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (updated) {
                    Glide.with(PatientDetailActivity.this).load(AppConstants.IMAGE_PATH + patientUuid + ".jpg").thumbnail(0.3f).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(photoView);
                }
                ImagesDAO imagesDAO = new ImagesDAO();
                boolean isImageDownloaded = false;
                try {
                    isImageDownloaded = imagesDAO.insertPatientProfileImages(AppConstants.IMAGE_PATH + patientUuid + ".jpg", patientUuid);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
//                        if (isImageDownloaded)
//                            AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_image_download_notifi), "" + patient_new.getFirst_name() + "" + patient_new.getLast_name() + "'s Image Download Incomplete.", 4, getApplication());
//                        else
//                            AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_image_download_notifi), "" + patient_new.getFirst_name() + "" + patient_new.getLast_name() + "'s Image Download Incomplete.", 4, getApplication());
            }
        });
    }

    /**
     * This method retrieves details about patient's old visits.
     *
     * @param datetime variable of type String.
     * @return void
     */
    private void createOldVisit(final String datetime, String visit_id, String end_datetime, String visitValue, String encounterVitalslocal, String encounterAdultIntialslocal) throws ParseException {

        final Boolean past_visit;
        final TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final String visitString = String.format("Seen on (%s)", DateAndTimeUtils.SimpleDatetoLongDate(datetime));
        if (end_datetime == null || end_datetime.isEmpty()) {
            // visit has not yet ended

            for (int i = 1; i <= 2; i++) {
                if (i == 1) {
                    SpannableString spannableString = new SpannableString(visitString + getString(R.string.active_tag_patientDetail));
                    Object greenSpan = new BackgroundColorSpan(Color.GREEN);
                    Object underlineSpan = new UnderlineSpan();
                    spannableString.setSpan(greenSpan, spannableString.length() - 6, spannableString.length(), 0);
                    spannableString.setSpan(underlineSpan, 0, spannableString.length() - 7, 0);
                    textView.setText(spannableString);
                    layoutParams.setMargins(5, 10, 5, 0);
                    //  textView.setLayoutParams(layoutParams);
                    textView.setTextSize(16);
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_regular);
                    textView.setTypeface(typeface);
                    previousVisitsList.addView(textView);
                }
                //If patient come up with any complaints
                if (i == 2) {
                    TextView complaintxt1 = new TextView(this);
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_regular);
                    complaintxt1.setTypeface(typeface);
                    complaintxt1.setLayoutParams(layoutParams);
                    if (visitValue != null && !visitValue.equals("")) {
                        String visitComplaint = Html.fromHtml(visitValue).toString();
                        complaintxt1.setText(visitComplaint.replace("\n" + Node.bullet_arrow + getString(R.string.associated_symptoms_patientDetail), ""));
                    } else {
                        Log.e("Check", "No complaint");
                    }
                    layoutParams.setMargins(5, 10, 5, 0);
                    //complaintxt1.setLayoutParams(layoutParams);
                    complaintxt1.setTextSize(16);
                    previousVisitsList.addView(complaintxt1);
                }
            }
            past_visit = false;

            if (newVisit.isEnabled()) {
                newVisit.setEnabled(false);
                newAdvice.setEnabled(false);

            }
            if (newVisit.isClickable()) {
                newVisit.setClickable(false);
                newAdvice.setClickable(false);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    newVisit.setBackgroundColor(getColor(R.color.divider));
                    newVisit.setTextColor(getColor(R.color.white));
                    newAdvice.setBackgroundColor(getColor(R.color.divider));
                    newAdvice.setTextColor(getColor(R.color.white));
                } else {
                    newVisit.setBackgroundColor(getResources().getColor(R.color.divider));
                    newVisit.setTextColor(getResources().getColor(R.color.white));
                    newAdvice.setBackgroundColor(getResources().getColor(R.color.divider));
                    newAdvice.setTextColor(getResources().getColor(R.color.white));
                }
            }

        } else {
            // when visit has ended
            past_visit = true;
            for (int i = 1; i <= 2; i++) {
                if (i == 1) {
                    textView.setText(visitString);
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_regular);
                    textView.setTypeface(typeface);
                    textView.setTextSize(16);
                    layoutParams.setMargins(5, 10, 5, 0);
                    // textView.setLayoutParams(layoutParams);
                    previousVisitsList.addView(textView);
                }
                //If patient has any past complaints
                if (i == 2) {
                    TextView complaintxt1 = new TextView(this);
                    if (visitValue != null && !visitValue.equals("")) {
                        String visitComplaint = Html.fromHtml(visitValue).toString();
                        complaintxt1.setText(visitComplaint.replace("\n" + Node.bullet_arrow + getString(R.string.associated_symptoms_patientDetail), ""));
                    } else {
                        Log.e("Check", "No complaint");
                        //if medical advice change heading accordingly
                        if (isMedicalAdvice)
                            complaintxt1.setText(Node.bullet_arrow + getString(R.string.text_medical_advice));
                        else
                            complaintxt1.setText(Node.bullet_arrow + getString(R.string.self_assessment));

                    }
                    layoutParams.setMargins(5, 10, 5, 0);
                    // complaintxt1.setLayoutParams(layoutParams);
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_regular);
                    complaintxt1.setTypeface(typeface);
                    complaintxt1.setTextSize(16);
                    previousVisitsList.addView(complaintxt1);
                }
            }
        }

        textView.setTextSize(16);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 10, 0, 0);
        // textView.setLayoutParams(llp);
        textView.setTag(visit_id);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_regular);
        textView.setTypeface(typeface);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 int position = (Integer) v.getTag();
                Intent visitSummary = new Intent(PatientDetailActivity.this, VisitSummaryActivity.class);

                visitSummary.putExtra("visitUuid", visit_id);
                visitSummary.putExtra("patientUuid", patientUuid);
                visitSummary.putExtra("encounterUuidVitals", encounterVitalslocal);
                visitSummary.putExtra("encounterUuidAdultIntial", encounterAdultIntialslocal);
                visitSummary.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
                visitSummary.putExtra("name", patientName);
                visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month);
                visitSummary.putExtra("tag", intentTag);
                visitSummary.putExtra("pastVisit", past_visit);
                if (hasPrescription.equalsIgnoreCase("true")) {
                    visitSummary.putExtra("hasPrescription", "true");
                } else {
                    visitSummary.putExtra("hasPrescription", "false");
                }
                startActivity(visitSummary);
            }
        });
        //previousVisitsList.addView(textView);
        //TODO: add on click listener to open the previous visit
    }

    //function to check if visit is of medical advise type
    //end date must be exact 5 minutes greater than start date
    private boolean isMedicalAdvice(String datetime, String end_datetime) {
        if (TextUtils.isEmpty(datetime) || TextUtils.isEmpty(end_datetime)) return false;

        SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        SimpleDateFormat endFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
        try {
            Date startTime = startFormat.parse(datetime);
            Date endTime = endFormat.parse(end_datetime);
            long diff = endTime.getTime() - startTime.getTime();
            if (diff == TimeUnit.MINUTES.toMillis(5)) return true;
        } catch (Exception e) {
            try {
                Date startTime = startFormat.parse(datetime);
                Date endTime = startFormat.parse(end_datetime);
                long diff = endTime.getTime() - startTime.getTime();
                if (diff == TimeUnit.MINUTES.toMillis(5)) return true;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return false;
    }


    /**
     * This method is called when patient has no prior visits.
     *
     * @return void
     */
    private void neverSeen() {
        final LayoutInflater inflater = PatientDetailActivity.this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.list_item_previous_visit, null);
        TextView textView = convertView.findViewById(R.id.textView_visit_info);
        String visitString = getString(R.string.no_prior_visits);
        textView.setText(visitString);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_regular);
        textView.setTypeface(typeface);
        textView.setTextSize(16);
        previousVisitsList.addView(convertView);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public class Myreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(() -> {
                try {
                    idView.setText(patientsDAO.getOpenmrsId(patientUuid));

                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                setTitle(idView.getText());
            });
        }
    }

    public void familyHistory(TextView famHistView, String patientuuid, String EncounterAdultInitials_LatestVisit) {
        familyHistoryExecutorService.execute(() -> {

            //String visitSelection = "patientuuid = ? AND enddate IS NULL OR enddate = ''";
            String visitSelection = "patientuuid = ?";
            String[] visitArgs = {patientuuid};
            String[] visitColumns = {"uuid, startdate", "enddate"};
            String visitOrderBy = "startdate";
            Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

            if (visitCursor.getCount() < 1) {
//            neverSeen();
            } else {

                if (visitCursor.moveToLast() && visitCursor != null) {
                    do {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("startdate"));
                        String end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                        String visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));

                        String encounterlocalAdultintial = "";
                        String encountervitalsLocal = null;
                        String encounterIDSelection = "visituuid = ?";

                        String[] encounterIDArgs = {visit_id};

                        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                        if (encounterCursor != null && encounterCursor.moveToFirst()) {
                            do {
                                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                    encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                }
                                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                    encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                }

                            } while (encounterCursor.moveToNext());
                        }
                        if (encounterCursor != null) {
                            encounterCursor.close();
                        }
                        String famHistSelection = "encounteruuid = ? AND conceptuuid = ? And voided!='1'";
                        String[] famHistArgs = {EncounterAdultInitials_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
                        String[] famHistColumns = {"value", " conceptuuid"};
                        Cursor famHistCursor = db.query("tbl_obs", famHistColumns, famHistSelection, famHistArgs, null, null, null);
                        famHistCursor.moveToLast();
                        String famHistValue;

                        try {
                            famHistValue = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                        } catch (Exception e) {
                            famHistValue = "";
                        } finally {
                            famHistCursor.close();
                        }

                        String finalFamHistValue = famHistValue;
                        runOnUiThread(() -> {
                            if (finalFamHistValue != null && !finalFamHistValue.equals("")) {
                                famHistView.setText(Html.fromHtml(finalFamHistValue));
                            } else {
                                famHistView.setText(getString(R.string.string_no_hist));
                            }
                        });
                    } while (visitCursor.moveToPrevious());
                }
                visitCursor.close();
            }
        });
    }

    public void pastMedicalHistory(TextView medHistView, String patientuuid, String EncounterAdultInitials_LatestVisit) {
        pasVisitsExecutorService.execute(() -> {

            //String visitSelection = "patientuuid = ? AND enddate IS NULL OR enddate = ''";
            String visitSelection = "patientuuid = ?";
            String[] visitArgs = {patientuuid};
            String[] visitColumns = {"uuid, startdate", "enddate"};
            String visitOrderBy = "startdate";
            Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

            if (visitCursor.getCount() < 1) {
//            neverSeen();
            } else {

                if (visitCursor.moveToLast() && visitCursor != null) {
                    do {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("startdate"));
                        String end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                        String visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));

                        String encounterlocalAdultintial = "";
                        String encountervitalsLocal = null;
                        String encounterIDSelection = "visituuid = ?";

                        String[] encounterIDArgs = {visit_id};

                        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                        if (encounterCursor != null && encounterCursor.moveToFirst()) {
                            do {
                                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                    encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                }
                                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                    encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                }

                            } while (encounterCursor.moveToNext());
                        }
                        if (encounterCursor != null) {
                            encounterCursor.close();
                        }
                        String medHistSelection = "encounteruuid = ? AND conceptuuid = ? And voided!='1'";
                        String[] medHistArgs = {EncounterAdultInitials_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                        String[] medHistColumms = {"value", " conceptuuid"};
                        Cursor medHistCursor = db.query("tbl_obs", medHistColumms, medHistSelection, medHistArgs, null, null, null);
                        medHistCursor.moveToLast();

                        String medHistValue;

                        try {
                            medHistValue = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                        } catch (Exception e) {
                            medHistValue = "";
                        } finally {
                            medHistCursor.close();
                        }

                        String finalMedHistValue = medHistValue;
                        runOnUiThread(() -> {
                            Log.v(TAG, finalMedHistValue);
                            if (finalMedHistValue != null && !finalMedHistValue.equals("")) {
                                medHistView.setText(Html.fromHtml(finalMedHistValue));
                            } else {
                                medHistView.setText(getString(R.string.string_no_hist));
                            }
                        });
                    } while (visitCursor.moveToPrevious());
                }
                visitCursor.close();
            }
        });
    }

    public void pastVisits(String patientuuid) {
        pasVisitsExecutorService.execute(() -> {

            String visitSelection = "patientuuid = ?";
            String[] visitArgs = {patientuuid};
            String[] visitColumns = {"uuid, startdate", "enddate"};
            String visitOrderBy = "startdate";
            Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

            if (visitCursor.getCount() < 1) {
                runOnUiThread(this::neverSeen);
            } else {

                if (visitCursor.moveToLast() && visitCursor != null) {
                    do {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("startdate"));
                        String end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                        String visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));
                        isMedicalAdvice = isMedicalAdvice(date, end_date);

                        String encounterlocalAdultintial = "";
                        String encountervitalsLocal = null;
                        String encounterIDSelection = "visituuid = ?";

                        String[] encounterIDArgs = {visit_id};

                        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                        if (encounterCursor != null && encounterCursor.moveToFirst()) {
                            do {
                                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                    encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                }
                                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                    encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                }

                            } while (encounterCursor.moveToNext());
                        }

                        if (encounterCursor != null) encounterCursor.close();

                        String previsitSelection = "encounteruuid = ? AND conceptuuid = ? and voided !='1'";
                        String[] previsitArgs = {encounterlocalAdultintial, UuidDictionary.CURRENT_COMPLAINT};
                        String[] previsitColumms = {"value", " conceptuuid", "encounteruuid"};
                        Cursor previsitCursor = db.query("tbl_obs", previsitColumms, previsitSelection, previsitArgs, null, null, null);
                        if (previsitCursor.moveToLast() && previsitCursor != null) {

                            String visitValue = previsitCursor.getString(previsitCursor.getColumnIndexOrThrow("value"));
                            if (visitValue != null && !visitValue.isEmpty()) {

                                visitValue = visitValue.replace("?<b>", Node.bullet_arrow);

                                String[] complaints = StringUtils.split(visitValue, Node.bullet_arrow);

                                visitValue = "";
                                String colon = ":";
                                if (complaints != null) {
                                    for (String comp : complaints) {
                                        if (!comp.trim().isEmpty()) {
                                            Log.d("colon", "colon: " + comp);
                                            if (comp != null && comp.indexOf(colon) < comp.length()) {
                                                visitValue = visitValue + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";
                                            } else {
                                                visitValue = visitValue + Node.bullet_arrow + comp;
                                            }
                                            Log.d("colon", "colon_visitvalue: " + visitValue);
                                        }
                                    }
                                    if (!visitValue.isEmpty()) {
                                        visitValue = visitValue.substring(0, visitValue.length() - 2);
                                        visitValue = visitValue.replaceAll("<b>", "");
                                        visitValue = visitValue.replaceAll("</b>", "");
                                    }
                                    SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                                    String finalVisitValue = visitValue;
                                    String finalEncountervitalsLocal = encountervitalsLocal;
                                    String finalEncounterlocalAdultintial = encounterlocalAdultintial;
                                    runOnUiThread(() -> {
                                        try {
                                            Date formatted = currentDate.parse(date);
                                            String visitDate = currentDate.format(formatted);
                                            createOldVisit(visitDate, visit_id, end_date, finalVisitValue, finalEncountervitalsLocal, finalEncounterlocalAdultintial);
                                        } catch (ParseException e) {
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    });
                                }
                            }
                            // Called when we select complaints but not select any sub knowledgeEngine inside that complaint
                            else {
                                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String finalVisitValue1 = visitValue;
                                String finalEncountervitalsLocal1 = encountervitalsLocal;
                                String finalEncounterlocalAdultintial1 = encounterlocalAdultintial;
                                runOnUiThread(() -> {
                                    try {

                                        Date formatted = currentDate.parse(date);
                                        String visitDate = currentDate.format(formatted);
                                        createOldVisit(visitDate, visit_id, end_date, finalVisitValue1, finalEncountervitalsLocal1, finalEncounterlocalAdultintial1);
                                    } catch (ParseException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                });
                            }
                        }
                        // Called when we close app on vitals screen and Didn't select any complaints
                        else {
                            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String finalEncountervitalsLocal2 = encountervitalsLocal;
                            String finalEncounterlocalAdultintial2 = encounterlocalAdultintial;
                            runOnUiThread(() -> {
                                try {
                                    Date formatted = currentDate.parse(date);
                                    String visitDate = currentDate.format(formatted);
                                    createOldVisit(visitDate, visit_id, end_date, visitValue, finalEncountervitalsLocal2, finalEncounterlocalAdultintial2);
                                } catch (ParseException e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            });
                        }
                        previsitCursor.close();
                    } while (visitCursor.moveToPrevious());
                }
            }
            visitCursor.close();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.detail_home:
                Intent intent = new Intent(PatientDetailActivity.this, HomeActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void callPatientViaIVR() {
        if (!NetworkConnection.isOnline(this)) {
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        String receiver = phoneView.getText().toString();
        if (TextUtils.isEmpty(receiver)) return;
        UrlModifiers urlModifiers = new UrlModifiers();
        String caller = sessionManager.getProviderPhoneno(); //fetches the provider mobile no who has logged in the app...
        String url = urlModifiers.getIvrCallUrl(caller, receiver);
        Logger.logD(TAG, "ivr call url" + url);
        Single<String> patientIvrCall = AppConstants.ivrApiInterface.CALL_PATIENT_IVR(url);
        patientIvrCall.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableSingleObserver<String>() {
            @Override
            public void onSuccess(@NonNull String s) {
                showAlert(R.string.calling_patient);
            }

            @Override
            public void onError(Throwable e) {
                showAlert(R.string.error_calling_patient);
            }
        });
    }

    void showAlert(int messageRes) {
        if (isFinishing()) return;
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setMessage(messageRes);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                storeCallResponse();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    //This function will ask the user that whether the call was success or not, based on the answer, the other dialog will show up.
    private void storeCallResponse() {
        final int[] checkedItems = {0};
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(PatientDetailActivity.this);
        alertDialogBuilder.setMessage(getString(R.string.able_to_connect));
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String[] items = {getString(R.string.patient_counselled), getString(R.string.patient_denied_counselling), getString(R.string.medical_advice_provided), getString(R.string.reschedule_call)};
                showOptionDialog(items, checkedItems, true);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String[] items = {getString(R.string.not_valid_number), getString(R.string.not_reachable), getString(R.string.not_picked_up)};
                showOptionDialog(items, checkedItems, false);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void showOptionDialog(String[] items, int[] checkedItems, boolean success) {
        final String[] selectedItem = {items[0]};
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(PatientDetailActivity.this);
        alertDialog.setTitle(getString(R.string.select_call_output));
        alertDialog.setSingleChoiceItems(items, checkedItems[0], new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedItem[0] = items[which];
            }
        });
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Not required for MSF as the app is available only in english language.
//                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
//                    selectedItem[0] = org.intelehealth.swasthyasamparktelemedicine.utilities.StringUtils.switch_hi_en_call_reason(selectedItem[0]);

                dialogInterface.dismiss();
                if (!success) {
                    callStatus = "Unable to reach patient";
                    callAction = selectedItem[0];
                    storeCallData(callStatus, callAction, remark); //these strings has to be sent in same format and in english only
                } else {
                    callStatus = "Able to reach patient";
                    callAction = selectedItem[0];
                    storeCallData(callStatus, callAction, remark); //these strings has to be sent in same format and in english only

                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void storeCallData(String callStatus, String callAction, String remark) {

        //get system date; Format need to be same as per Satyadeep's request.
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date todayDate = new Date();
        String callDate = currentDate.format(todayDate) + " 00:00";

        //populate the body for
        SendCallDataDAO sendCallDataDAO = new SendCallDataDAO();
        SendCallData sendCallData = new SendCallData();
        sendCallData.state = patient_new.getState_province();
        sendCallData.district = patient_new.getCity_village();
        sendCallData.callStatus = callStatus;
        sendCallData.callAction = callAction;
        sendCallData.callDate = callDate;
        sendCallData.remarks = remark;
        sendCallData.callNumber = sessionManager.getProviderPhoneno();
        sendCallData.callStartTime = callStartTime;
        sendCallData.facility = "Unknown"; //facility column needs to be send to maintain dashboard attributes but this value is of no use and also not getting it anywhere in our data thus sending "Unknown"

        if (callAction.equalsIgnoreCase("Patient Counselled") || callAction.equalsIgnoreCase("Medical advice provided")) {
            //if the patient asks for counselling then it means that the data needs to be pushed to the imocalls api after uploading visit so thus we are storing info temporarily in a separate table.
            try {
                sendCallDataDAO.insertCallData(sendCallData);
            } catch (DAOException e) {
                e.printStackTrace();
            }
        } else {
            SimpleDateFormat startFormat = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm", Locale.ENGLISH);
            Calendar today = Calendar.getInstance();
            today.add(Calendar.MINUTE, -1);
            today.set(Calendar.MILLISECOND, 0);
            Date todayDate1 = today.getTime();
            String callEndTime = startFormat.format(todayDate1);
            sendCallData.callEndTime = callEndTime;
            UrlModifiers urlModifiers = new UrlModifiers();
            ApiInterface apiInterface = AppConstants.apiInterface;
            String sendDataUrl = urlModifiers.sendCallData();
            apiInterface.callPatientData(sendDataUrl, sendCallData).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(PatientDetailActivity.this, getString(R.string.data_stored_successfully), Toast.LENGTH_LONG).show();
                    System.out.println(call);
                    System.out.println(response);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PatientDetailActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
//                    if (!PatientDetailActivity.this.isDestroyed())
//                        new AlertDialog.Builder(PatientDetailActivity.this).setMessage(t.getMessage()).setPositiveButton(R.string.generic_ok, null).show();
                }
            });
            onBackPressed();
        }
    }


}

