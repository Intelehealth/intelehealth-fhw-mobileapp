package org.intelehealth.ekalarogya.activities.patientDetailActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.intelehealth.ekalarogya.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.shared.BaseActivity;
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

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.database.InteleHealthDatabaseHelper;
import org.intelehealth.ekalarogya.database.dao.EncounterDAO;
import org.intelehealth.ekalarogya.database.dao.ImagesDAO;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.database.dao.VisitsDAO;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.models.FamilyMemberRes;
import org.intelehealth.ekalarogya.models.Patient;
import org.intelehealth.ekalarogya.models.dto.EncounterDTO;
import org.intelehealth.ekalarogya.models.dto.VisitDTO;
import org.intelehealth.ekalarogya.utilities.DateAndTimeUtils;
import org.intelehealth.ekalarogya.utilities.DownloadFilesUtils;
import org.intelehealth.ekalarogya.utilities.FileUtils;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UrlModifiers;
import org.intelehealth.ekalarogya.utilities.UuidDictionary;

import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ekalarogya.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ekalarogya.activities.vitalActivity.VitalsActivity;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static org.intelehealth.ekalarogya.utilities.StringUtils.*;

public class PatientDetailActivity extends BaseActivity {
    private static final String TAG = PatientDetailActivity.class.getSimpleName();
    String patientName;
    String visitUuid = null;
    List<String> visitUuidList;
    String patientUuid;
    String intentTag = "";
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
    Button newVisit, buttonSevikaAdvice, householdSurvey;
    IntentFilter filter;
    Myreceiver reMyreceive;
    ImageView photoView;
    ImagesDAO imagesDAO = new ImagesDAO();
    TextView idView;
    RecyclerView rvFamilyMember;
    TextView tvNoFamilyMember;

    String privacy_value_selected;

    ImageView ivPrescription;
    private String hasPrescription = "";
    Context context;
    private Context updatedContext;
    float float_ageYear_Month;
    public static final String HOUSEHOLD_ATTR_UUID = "10720d1a-1471-431b-be28-285d64767093";
    private Resources updatedResources, originalResources;

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
        buttonSevikaAdvice = findViewById(R.id.button_sevika_advice);
        householdSurvey = findViewById(R.id.button_household_survey);
        rvFamilyMember = findViewById(R.id.rv_familymember);
        tvNoFamilyMember = findViewById(R.id.tv_nofamilymember);
        context = PatientDetailActivity.this;
        setupTranslationTools();

        ivPrescription = findViewById(R.id.iv_prescription);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            patientName = intent.getStringExtra("patientName");
            hasPrescription = intent.getStringExtra("hasPrescription");
            privacy_value_selected = intent.getStringExtra("privacy"); //intent value from IdentificationActivity.

            intentTag = intent.getStringExtra("tag");
            Logger.logD(TAG, "Patient ID: " + patientUuid);
            Logger.logD(TAG, "Patient Name: " + patientName);
            Logger.logD(TAG, "Intent Tag: " + intentTag);
            Logger.logD(TAG, "Privacy Value on (PatientDetail): " + privacy_value_selected);
        }

        if (hasPrescription.equalsIgnoreCase("true")) {
            ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
        }

        editbtn = findViewById(R.id.edit_button);
        ib_addFamilyMember = findViewById(R.id.ic_addFamilyMember);
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(PatientDetailActivity.this, IdentificationActivity.class);
                intent2.putExtra("patientUuid", patientUuid);
                startActivity(intent2);

            }
        });
        ib_addFamilyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String houseHoldValue = "";
                try {
                    houseHoldValue = patientsDAO.getValueFromPatientAttrbTable(patientUuid, HOUSEHOLD_ATTR_UUID);
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

        setDisplay(patientUuid);

        buttonSevikaAdvice.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        buttonSevikaAdvice.setTextColor(getResources().getColor(R.color.white));

        householdSurvey.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        householdSurvey.setTextColor(getResources().getColor(R.color.white));

        if (newVisit.isEnabled()) {
            newVisit.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            newVisit.setTextColor(getResources().getColor(R.color.white));
        } else {
            //newVisit.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            //newVisit.setTextColor(getResources().getColor(R.color.white));
        }

        newVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startVisitConfirmation("Doctor");
            }
        });

        buttonSevikaAdvice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVisitConfirmation("Sevika");
            }
        });

        householdSurvey.setOnClickListener(v -> {
            Intent surveyIntent = new Intent(PatientDetailActivity.this, SurveyActivity.class);
            surveyIntent.putExtra("patientUuid", patientUuid);
            startActivity(surveyIntent);
        });

        LoadFamilyMembers();

    }

    private void setupTranslationTools() {
        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        updatedContext = context.createConfigurationContext(configuration);
        updatedResources = updatedContext.getResources();
        originalResources = this.context.getResources();
    }

    private void startVisitConfirmation(String startNewAdviceBy) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(PatientDetailActivity.this);
        if (startNewAdviceBy.equalsIgnoreCase("Sevika")) {
            alertDialogBuilder.setMessage(getResources().getString(R.string.start_newadvice_confirmation_msg));
        } else {
            alertDialogBuilder.setMessage(getResources().getString(R.string.start_newvisit_confirmation_msg));
        }
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.generic_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startNewVisit(startNewAdviceBy);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(PatientDetailActivity.this, alertDialog);
    }

    private void startNewVisit(String startNewAdviceBy) {
        // before starting, we determine if it is new visit for a returning patient
        // extract both FH and PMH
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        todayDate = DateUtils.addMinutes(todayDate, -5);
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
                new String[]{encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB},
                null, null, null);

        if (cursor.moveToFirst()) {
            // rows present
            do {
                // so that null data is not appended
                phistory = phistory + cursor.getString(0);

            }
            while (cursor.moveToNext());
            returning = true;
            sessionManager.setReturning(returning);
        }
        cursor.close();

        Cursor cursor1 = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for FH (Family History)
                new String[]{encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB},
                null, null, null);
        if (cursor1.moveToFirst()) {
            // rows present
            do {
                fhistory = fhistory + cursor1.getString(0);
            }
            while (cursor1.moveToNext());
            returning = true;
            sessionManager.setReturning(returning);
        }
        cursor1.close();

        // Will display data for patient as it is present in database
        // Toast.makeText(PatientDetailActivity.this,"PMH: "+phistory,Toast.LENGTH_SHORT).sƒhow();
        // Toast.makeText(PatientDetailActivity.this,"FH: "+fhistory,Toast.LENGTH_SHORT).show();

        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setUuid(uuid);
        visitDTO.setPatientuuid(patient_new.getUuid());
        visitDTO.setStartdate(thisDate);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setLocationuuid(sessionManager.getCurrentLocationUuid());
        visitDTO.setSyncd(false);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());//static
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (startNewAdviceBy.equalsIgnoreCase("Sevika")) {
            navigateToComplaintScreen(visitUuid);
        } else {
            navigateToVitalsScreen(uuid);
        }
    }

    private void navigateToComplaintScreen(String visitUuid) {
        String fullName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
        int age = DateAndTimeUtils.getAgeInYear(patient_new.getDate_of_birth(), context);

        Intent intent2 = new Intent(PatientDetailActivity.this, ComplaintNodeActivity.class);
        intent2.putExtra("patientUuid", patientUuid);
        intent2.putExtra("visitUuid", visitUuid);
        intent2.putExtra("encounterUuidVitals", encounterDTO.getUuid());
        intent2.putExtra("encounterUuidAdultIntial", "");
        intent2.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
        intent2.putExtra("name", fullName);
        intent2.putExtra("age", age);
        intent2.putExtra("tag", "new");
        intent2.putExtra("advicefrom", "Sevika");
        intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
        startActivity(intent2);
    }

    private void navigateToVitalsScreen(String visitUuid) {
        String fullName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
        int age = DateAndTimeUtils.getAgeInYear(patient_new.getDate_of_birth(), context);

        Intent intent2 = new Intent(PatientDetailActivity.this, VitalsActivity.class);
        intent2.putExtra("patientUuid", patientUuid);
        intent2.putExtra("visitUuid", visitUuid);
        intent2.putExtra("encounterUuidVitals", encounterDTO.getUuid());
        intent2.putExtra("encounterUuidAdultIntial", "");
        intent2.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
        intent2.putExtra("name", fullName);
        intent2.putExtra("age", age);
        intent2.putExtra("tag", "new");
        intent2.putExtra("advicefrom", "Doctor");
        intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
        startActivity(intent2);
    }

    private void LoadFamilyMembers() {

        String houseHoldValue = "";
        try {
            houseHoldValue = patientsDAO.getValueFromPatientAttrbTable(patientUuid, HOUSEHOLD_ATTR_UUID);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //here the householduuid was not added to sessionmanager() so the issue of error...
        sessionManager.setHouseholdUuid(houseHoldValue);
        //end...


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

                if (listPatientNames.size() > 0) {
                    tvNoFamilyMember.setVisibility(View.GONE);
                    rvFamilyMember.setVisibility(View.VISIBLE);
                    FamilyMemberAdapter familyMemberAdapter = new FamilyMemberAdapter(listPatientNames, this);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                    rvFamilyMember.setLayoutManager(linearLayoutManager);
                    rvFamilyMember.setAdapter(familyMemberAdapter);
                } else {
                    tvNoFamilyMember.setVisibility(View.VISIBLE);
                    rvFamilyMember.setVisibility(View.GONE);
                }

            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    @Override
    protected void onStart() {
        ContextCompat.registerReceiver(this, reMyreceive, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(reMyreceive);
        super.onDestroy();
    }

    public void setDisplay(String dataString) {

        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw",
                "patient_photo"};
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

                if (name.equalsIgnoreCase("caste")) {
                    patient_new.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient_new.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient_new.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient_new.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient_new.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient_new.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ProfileImageTimestamp")) {
                    profileImage1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

        photoView = findViewById(R.id.imageView_patient);

        idView = findViewById(R.id.textView_ID);
        TextView patinetName = findViewById(R.id.textView_name);
        TextView dobView = findViewById(R.id.textView_DOB);
        TextView ageView = findViewById(R.id.textView_age);
        TextView genderView = findViewById(R.id.textView_gender);
        TextView addr1View = findViewById(R.id.textView_address_1);
        TableRow addr2Row = findViewById(R.id.tableRow_addr2);
        TextView addr2View = findViewById(R.id.textView_address2);
        TextView addrFinalView = findViewById(R.id.textView_address_final);
        TextView casteView = findViewById(R.id.textView_caste);
        TextView economic_statusView = findViewById(R.id.textView_economic_status);
        TextView education_statusView = findViewById(R.id.textView_education_status);
        TextView phoneView = findViewById(R.id.textView_phone);
        TextView sdwView = findViewById(R.id.textView_SDW);
        TableRow sdwRow = findViewById(R.id.tableRow_SDW);
        TextView occuView = findViewById(R.id.textView_occupation);
        TableRow occuRow = findViewById(R.id.tableRow_Occupation);
        TableRow economicRow = findViewById(R.id.tableRow_Economic_Status);
        TableRow educationRow = findViewById(R.id.tableRow_Education_Status);
        TableRow casteRow = findViewById(R.id.tableRow_Caste);

        TextView medHistView = findViewById(R.id.textView_patHist);
        TextView famHistView = findViewById(R.id.textView_famHist);


        if (!sessionManager.getLicenseKey().isEmpty()) {
            hasLicense = true;
        }

        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context),
                                String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file
            /*if (obj.getBoolean("casteLayout")) {
                casteRow.setVisibility(View.VISIBLE);
            } else {
                casteRow.setVisibility(View.GONE);
            }*/
            if (obj.getBoolean("educationLayout")) {
                educationRow.setVisibility(View.VISIBLE);
            } else {
                educationRow.setVisibility(View.GONE);
            }
            if (obj.getBoolean("economicLayout")) {
                economicRow.setVisibility(View.VISIBLE);
            } else {
                economicRow.setVisibility(View.GONE);
            }

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
            patientName = patient_new.getFirst_name() + " " + patient_new.getMiddle_name() + " " + patient_new.getLast_name();
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
        Glide.with(PatientDetailActivity.this)
                .load(patient_new.getPatient_photo())
                .thumbnail(0.3f)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(photoView);

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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            String dob_text = en__bn_dob(dob); //to show text of English into bengali...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            String dob_text = en__kn_dob(dob); //to show text of English into k...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            String dob_text = en__mr_dob(dob); //to show text of English into k...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            String dob_text = en__as_dob(dob); //to show text of English into Assamese...
            dobView.setText(dob_text);
        } else {
            dobView.setText(dob);
        }

        if (patient_new.getGender() != null) {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("O")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("O")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("O")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("O")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("O")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getString(R.string.identification_screen_checkbox_female));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
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
                addrFinalLine = String.format("%s, %s, %s, %s",
                        city_village, patient_new.getState_province(),
                        patient_new.getPostal_code(), patient_new.getCountry());
            } else {
                addrFinalLine = String.format("%s, %s, %s",
                        city_village, patient_new.getState_province(),
                        patient_new.getCountry());
            }
            addrFinalView.setText(addrFinalLine);
        } else {
            String addrFinalLine = String.format("%s, %s, %s",
                    city_village, patient_new.getState_province(),
                    patient_new.getCountry());
            addrFinalView.setText(addrFinalLine);
        }

        phoneView.setText(patient_new.getPhone_number());

        //english = en
        //hindi = hi
        //education
        if (patient_new.getEducation_level() != null) {
            if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                education_statusView.setText("नहीं दिया गया");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                education_statusView.setText("ଉପଲବ୍ଧ ନାହିଁ");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                education_statusView.setText("প্রদান করা হয়নি");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                education_statusView.setText("ಒದಗಿಸಿಲ್ಲ");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                education_statusView.setText("दिले नाही");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                education_statusView.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                education_statusView.setText("প্ৰদান কৰা হোৱা নাই");
            } else {
//                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                    String education = switch_hi_education_edit(patient_new.getEducation_level());
//                    education_statusView.setText(education);
//                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
//                    String education = switch_or_education_edit(patient_new.getEducation_level());
//                    education_statusView.setText(education);
//                } else {
//                    education_statusView.setText(patient_new.getEducation_level());
//                }

                if (patient_new.getEducation_level() != null && !patient_new.getEducation_level().equalsIgnoreCase("")) {
                    String education = getEducationStrings(patient_new.getEducation_level(), updatedResources, originalResources, sessionManager.getAppLanguage());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String education = switch_or_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String education = switch_gu_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String education = switch_as_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else {
                    education_statusView.setText(patient_new.getEducation_level());
                }

                // education_statusView.setText(patient_new.getEducation_level());
            }
        }

        //economic
        if (patient_new.getEconomic_status() != null) {
            if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                economic_statusView.setText("नहीं दिया गया");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                economic_statusView.setText("ଉପଲବ୍ଧ ନାହିଁ");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                economic_statusView.setText("প্রদান করা হয়নি");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                economic_statusView.setText("ಒದಗಿಸಿಲ್ಲ");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                economic_statusView.setText("दिले नाही");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                economic_statusView.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                economic_statusView.setText("প্ৰদান কৰা হোৱা নাই");
            } else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String economic = switch_hi_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String economic = switch_or_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String economic = switch_bn_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String economic = switch_kn_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String economic = switch_mr_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String economic = switch_gu_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String economic = switch_as_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else {
                    economic_statusView.setText(patient_new.getEconomic_status());
                }
                // economic_statusView.setText(patient_new.getEconomic_status());
            }
        }

        //caste
        if (patient_new.getCaste() != null) {
            if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                casteView.setText("नहीं दिया गया");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                casteView.setText("ଉପଲବ୍ଧ ନାହିଁ");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                casteView.setText("প্রদান করা হয়নি");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                casteView.setText("ಒದಗಿಸಿಲ್ಲ");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                casteView.setText("दिले नाही");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                casteView.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided"/*getResources().getString(R.string.not_provided)*/) &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                casteView.setText("પপ্ৰদান কৰা হোৱা নাই");
            } else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String caste = switch_bn_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String caste = switch_kn_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String caste = switch_mr_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String caste = switch_gu_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String caste = switch_as_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else {
                    casteView.setText(patient_new.getCaste());
                }
                // casteView.setText(patient_new.getCaste());
            }
        }

//
        if (patient_new.getSdw() != null && !patient_new.getSdw().equals("")) {
            sdwView.setText(patient_new.getSdw());
        } else {
            sdwRow.setVisibility(View.GONE);
        }
//
        if (patient_new.getOccupation() != null && !patient_new.getOccupation().equals("")) {
           /* if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String occupation = switch_hi_occupation_edit(patient_new.getOccupation());
                occuView.setText(occupation);
            }else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                String occupation = switch_or_occupation_edit(patient_new.getOccupation());
                occuView.setText(occupation);
            }else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                String occupation = switch_gu_occupation_edit(patient_new.getOccupation());
                occuView.setText(occupation);
            }else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                String occupation = switch_as_occupation_edit(patient_new.getOccupation(),context);
                occuView.setText(occupation);
            } else {
                occuView.setText(patient_new.getOccupation());
            }*/

            String education = getOccupationString(patient_new.getOccupation(), updatedResources, originalResources, sessionManager.getAppLanguage());
            occuView.setText(education);

        } else {
//            occuRow.setVisibility(View.GONE);
            occuView.setText("");
        }

        if (visitUuid != null && !visitUuid.isEmpty()) {
            CardView histCardView = findViewById(R.id.cardView_history);
            histCardView.setVisibility(View.GONE);
        } else {
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
            familyHistory(famHistView, patientUuid, encounterAdultIntials);
            pastMedicalHistory(medHistView, patientUuid, encounterAdultIntials);
            pastVisits(patientUuid);
        }
    }

    public void profilePicDownloaded() {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(patientUuid);
        Logger.logD(TAG, "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
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
                            Glide.with(PatientDetailActivity.this)
                                    .load(AppConstants.IMAGE_PATH + patientUuid + ".jpg")
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(photoView);
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
                    SpannableString spannableString = new SpannableString(visitString + " " + getString(R.string.active_tag_patientDetail));
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

            if (buttonSevikaAdvice.isEnabled()) {
                buttonSevikaAdvice.setEnabled(false);
            }
            if (buttonSevikaAdvice.isClickable()) {
                buttonSevikaAdvice.setClickable(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    buttonSevikaAdvice.setBackgroundColor
                            (getColor(R.color.divider));
                    buttonSevikaAdvice.setTextColor(getColor(R.color.white));
                } else {
                    buttonSevikaAdvice.setBackgroundColor(getResources().getColor(R.color.divider));
                    buttonSevikaAdvice.setTextColor(getResources().getColor(R.color.white));
                }
            }

            if (newVisit.isEnabled()) {
                newVisit.setEnabled(false);
            }
            if (newVisit.isClickable()) {
                newVisit.setClickable(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    newVisit.setBackgroundColor
                            (getColor(R.color.divider));
                    newVisit.setTextColor(getColor(R.color.white));
                } else {
                    newVisit.setBackgroundColor(getResources().getColor(R.color.divider));
                    newVisit.setTextColor(getResources().getColor(R.color.white));
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
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
            try {
                idView.setText(patientsDAO.getOpenmrsId(patientUuid));

            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            setTitle(idView.getText());
        }
    }

    public void familyHistory(TextView famHistView, String patientuuid,
                              String EncounterAdultInitials_LatestVisit) {
        //String visitSelection = "patientuuid = ? AND enddate IS NULL OR enddate = ''";
        String visitSelection = "patientuuid = ?";
        String[] visitArgs = {patientuuid};
        String[] visitColumns = {"uuid, startdate", "enddate"};
        String visitOrderBy = "startdate";
        Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

        previousVisitsList = findViewById(R.id.linearLayout_previous_visits);
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
                    String famHistSelection = "encounteruuid = ? AND (conceptuuid = ? OR conceptuuid = ?) And voided!='1'";
                    String[] famHistArgs = {EncounterAdultInitials_LatestVisit,
                            UuidDictionary.RHK_FAMILY_HISTORY_BLURB, UuidDictionary.FAMHIST_REG_LANG_VALUE};
                    String[] famHistColumns = {"value", " conceptuuid"};
                    Cursor famHistCursor = db.query("tbl_obs", famHistColumns, famHistSelection, famHistArgs, null, null, null);
                    //  famHistCursor.moveToLast();
                    String famHistValue = "", famHistValue_REG = "";

                    if (famHistCursor != null && famHistCursor.moveToFirst()) {
                        do {
                            String famConceptID = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("conceptuuid"));
                            if (famConceptID.equalsIgnoreCase(UuidDictionary.RHK_FAMILY_HISTORY_BLURB)) {
                                famHistValue = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                            } else if (famConceptID.equalsIgnoreCase(UuidDictionary.FAMHIST_REG_LANG_VALUE)) {
                                famHistValue_REG = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                            }
                        }
                        while (famHistCursor.moveToNext());
                    }

                    if (famHistCursor != null)
                        famHistCursor.close();

/*
                    try {
                        famHistValue = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                    } catch (Exception e) {
                        famHistValue = "";
                    } finally {
                        famHistCursor.close();
                    }
*/
                    String value = fetchValue_REG(famHistValue_REG, famHistValue, sessionManager);
                    if (value != null && !value.equals("")) {
                        famHistView.setText(Html.fromHtml(value));
                    } else {
                        famHistView.setText(getString(R.string.string_no_hist));
                    }

                } while (visitCursor.moveToPrevious());
            }
            visitCursor.close();
        }

    }

    public void pastMedicalHistory(TextView medHistView, String patientuuid,
                                   String EncounterAdultInitials_LatestVisit) {
        //String visitSelection = "patientuuid = ? AND enddate IS NULL OR enddate = ''";
        String visitSelection = "patientuuid = ?";
        String[] visitArgs = {patientuuid};
        String[] visitColumns = {"uuid, startdate", "enddate"};
        String visitOrderBy = "startdate";
        Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

        previousVisitsList = findViewById(R.id.linearLayout_previous_visits);
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
                    String medHistSelection = "encounteruuid = ? AND (conceptuuid = ? OR conceptuuid = ?) And voided!='1'";
                    String[] medHistArgs = {EncounterAdultInitials_LatestVisit,
                            UuidDictionary.RHK_MEDICAL_HISTORY_BLURB, UuidDictionary.PASTHIST_REG_LANG_VALUE};
                    String[] medHistColumms = {"value", " conceptuuid"};
                    Cursor medHistCursor = db.query("tbl_obs", medHistColumms, medHistSelection, medHistArgs, null, null, null);
                    //  medHistCursor.moveToLast();

                    String medHistValue = "", medHistValue_REG = "";
                    if (medHistCursor != null && medHistCursor.moveToFirst()) {
                        do {
                            String medHistConceptID = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("conceptuuid"));
                            if (medHistConceptID.equalsIgnoreCase(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB)) {
                                medHistValue = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                            } else if (medHistConceptID.equalsIgnoreCase(UuidDictionary.PASTHIST_REG_LANG_VALUE)) {
                                medHistValue_REG = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                            }
                        }
                        while (medHistCursor.moveToNext());
                    }

                    if (medHistCursor != null)
                        medHistCursor.close();
/*
                    try {
                        medHistValue = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                    } catch (Exception e) {
                        medHistValue = "";
                    } finally {
                        medHistCursor.close();
                    }
*/

                    Log.v(TAG, medHistValue);
                    String value = fetchValue_REG(medHistValue_REG, medHistValue, sessionManager);
                    if (value != null && !value.equals("")) {
                        medHistView.setText(Html.fromHtml(value));
                    } else {
                        medHistView.setText(getString(R.string.string_no_hist));
                    }

                  /*  if (medHistValue != null && !medHistValue.equals("")) {
                        medHistView.setText(Html.fromHtml(medHistValue));
                    } else {
                        medHistView.setText(getString(R.string.string_no_hist));
                    }*/

                } while (visitCursor.moveToPrevious());
            }
            visitCursor.close();
        }
    }

    public void pastVisits(String patientuuid) {
        String visitSelection = "patientuuid = ? AND voided = 0";
        String[] visitArgs = {patientuuid};
        String[] visitColumns = {"uuid, startdate", "enddate"};
        String visitOrderBy = "startdate";
        Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

        previousVisitsList = findViewById(R.id.linearLayout_previous_visits);
        if (visitCursor.getCount() < 1) {
            neverSeen();
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
                    encounterCursor.close();

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
                                    if (!comp.trim().isEmpty() && comp.contains(colon)) {
                                        Log.d("colon", "colon: " + comp);
                                        visitValue = visitValue + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";
                                        Log.d("colon", "colon_visitvalue: " + visitValue);
                                    }
                                }
                                if (!visitValue.isEmpty()) {
                                    visitValue = visitValue.substring(0, visitValue.length() - 2);
                                    visitValue = visitValue.replaceAll("<b>", "");
                                    visitValue = visitValue.replaceAll("</b>", "");
                                }
                                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                try {

                                    Date formatted = currentDate.parse(date);
                                    String visitDate = currentDate.format(formatted);
                                    createOldVisit(visitDate, visit_id, end_date, visitValue, encountervitalsLocal, encounterlocalAdultintial);
                                } catch (ParseException e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }
                        }
                        // Called when we select complaints but not select any sub knowledgeEngine inside that complaint
                        else {
                            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            try {

                                Date formatted = currentDate.parse(date);
                                String visitDate = currentDate.format(formatted);
                                createOldVisit(visitDate, visit_id, end_date, visitValue, encountervitalsLocal, encounterlocalAdultintial);
                            } catch (ParseException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                    }
                    // Called when we close app on vitals screen and Didn't select any complaints
                    else {
                        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        try {

                            Date formatted = currentDate.parse(date);
                            String visitDate = currentDate.format(formatted);
                            createOldVisit(visitDate, visit_id, end_date, visitValue, encountervitalsLocal, encounterlocalAdultintial);
                        } catch (ParseException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                } while (visitCursor.moveToPrevious());
            }
        }
        visitCursor.close();
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
        if (item.getItemId() == R.id.detail_home) {
            Intent intent = new Intent(PatientDetailActivity.this, HomeActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
