package org.intelehealth.app.activities.patientDetailActivity;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import org.intelehealth.app.activities.visitSummaryActivity.HorizontalAdapter;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.models.FamilyMemberRes;
import org.intelehealth.app.models.patientImageModelRequest.PatientADPImageDownloadResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import com.smartcaredoc.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.activities.vitalActivity.VitalsActivity;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.exception.DAOException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static org.intelehealth.app.utilities.StringUtils.en__gu_dob;
import static org.intelehealth.app.utilities.StringUtils.en__as_dob;
import static org.intelehealth.app.utilities.StringUtils.en__bn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.app.utilities.StringUtils.en__kn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ml_dob;
import static org.intelehealth.app.utilities.StringUtils.en__mr_dob;
import static org.intelehealth.app.utilities.StringUtils.en__or_dob;

import static org.intelehealth.app.utilities.StringUtils.switch_gu_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_education_edit;
import static org.intelehealth.app.utilities.StringUtils.en__ru_dob;
import static org.intelehealth.app.utilities.StringUtils.en__te_dob;
import static org.intelehealth.app.utilities.StringUtils.switch_as_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.en__ta_dob;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_state_india_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_education_edit;

public class PatientDetailActivity extends AppCompatActivity {
    private static final String TAG = PatientDetailActivity.class.getSimpleName();
    String patientName, patientFName, patientLName;
    String mGender;
    String visitUuid = null;
    List<String> visitUuidList;
    String patientUuid;
    String intentTag = "";
    String profileImage = "";
    String profileImage1 = "";
    SessionManager sessionManager = null;
    Patient patient_new = new Patient();
    TextView phoneView;

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
    Button newVisit;
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
    float float_ageYear_Month;
   // RecyclerView additionalDocRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(PatientDetailActivity.this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        //  sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        setContentView(R.layout.activity_patient_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        sessionManager = new SessionManager(this);
        reMyreceive = new Myreceiver();
        filter = new IntentFilter("OpenmrsID");
        newVisit = findViewById(R.id.button_new_visit);
        rvFamilyMember = findViewById(R.id.rv_familymember);
        tvNoFamilyMember = findViewById(R.id.tv_nofamilymember);
        context = PatientDetailActivity.this;
      //  additionalDocRV = findViewById(R.id.recy_additional_documents);
        ivPrescription = findViewById(R.id.iv_prescription);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            patientName = intent.getStringExtra("patientName");
            patientFName = intent.getStringExtra("patientFirstName");
            patientLName = intent.getStringExtra("patientLastName");
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
                    houseHoldValue = patientsDAO.getAttributeValue(patientUuid, "3d2de264-9c8f-4fcc-bd97-660b74f8ffb0");
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                Log.e("houseHOLDID", houseHoldValue);

                sessionManager.setHouseholdUuid(houseHoldValue);
                Intent i = new Intent(PatientDetailActivity.this, IdentificationActivity.class);
                i.putExtra("privacy", "Accept");
                i.putExtra("newMember", "newMemberYes");
                i.putExtra("address1", patient_new.getAddress1());
                i.putExtra("postalCode", patient_new.getPostal_code());
                startActivity(i);
            }
        });

        setDisplay(patientUuid);

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
                try {
                    encounterDTO.setEncounterTime(ThirtySecondsLate(thisDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }                encounterDTO.setVisituuid(uuid);
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

                VisitAttributeListDAO visitAttribute = new VisitAttributeListDAO();

                try {
                    visitsDAO.insertPatientToDB(visitDTO);
                    visitAttribute.insertVisitAttributes(uuid, sessionManager.getStateName(), "7d3c899e-7913-4aac-ad0c-b097bfa7e96d");

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
                intent2.putExtra("patientFirstName",patientFName);
                intent2.putExtra("patientLastName", patientLName);
                intent2.putExtra("gender", mGender);
                intent2.putExtra("tag", "new");
                intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
                startActivity(intent2);
            }
        });

        LoadFamilyMembers();

    }

    public String ThirtySecondsLate (String timeStamp) throws ParseException {

        long FIVE_MINS_IN_MILLIS = (1 * 60 * 1000)/2;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();

        return df.format(new Date(time + FIVE_MINS_IN_MILLIS));
    }

    private void LoadFamilyMembers() {

        String houseHoldValue = "";
        try {
            houseHoldValue = patientsDAO.getAttributeValue(patientUuid, "3d2de264-9c8f-4fcc-bd97-660b74f8ffb0");
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
        ContextCompat.registerReceiver(this,reMyreceive, filter,ContextCompat.RECEIVER_NOT_EXPORTED);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(reMyreceive);
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

        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "gender",
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
                patient_new.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
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
                if (name.equalsIgnoreCase("Aadhar details")) {
                    patient_new.setAadhar_details(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Patient Additional Documents")) {
                    patient_new.setAdditionalDocPath(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
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
        TextView genderView = findViewById(R.id.textView_Gender);
        TextView ageView = findViewById(R.id.textView_age);
        TextView addr1View = findViewById(R.id.textView_address_1);
        TableRow addr2Row = findViewById(R.id.tableRow_addr2);
        TextView addr2View = findViewById(R.id.textView_address2);
        TextView addrFinalView = findViewById(R.id.textView_address_final);
        TextView casteView = findViewById(R.id.textView_caste);
        TextView economic_statusView = findViewById(R.id.textView_economic_status);
        TextView education_statusView = findViewById(R.id.textView_education_status);
        phoneView = findViewById(R.id.textView_phone);
        TextView sdwView = findViewById(R.id.textView_SDW);
        TextView aadharView = findViewById(R.id.textView_Aadhar);
        TableRow aadharRow = findViewById(R.id.tableRow_Aadhar);
        TableRow sdwRow = findViewById(R.id.tableRow_SDW);
        TextView occuView = findViewById(R.id.textView_occupation);
        TableRow occuRow = findViewById(R.id.tableRow_Occupation);
        TableRow economicRow = findViewById(R.id.tableRow_Economic_Status);
        TableRow educationRow = findViewById(R.id.tableRow_Education_Status);
        TableRow casteRow = findViewById(R.id.tableRow_Caste);
        ImageView whatsapp_no = findViewById(R.id.whatsapp_no);
        ImageView calling = findViewById(R.id.calling);

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
            if (obj.getBoolean("casteLayout")) {
                casteRow.setVisibility(View.VISIBLE);
            } else {
                casteRow.setVisibility(View.GONE);
            }
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
        if (patient_new.getMiddle_name() == null || patient_new.getMiddle_name().equalsIgnoreCase("-")) {
            patientName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
            patientLName = patient_new.getLast_name();
            patientFName = patient_new.getFirst_name();
        } else {
            patientName = patient_new.getFirst_name() + " " + patient_new.getMiddle_name() + " " + patient_new.getLast_name();
            patientLName = patient_new.getLast_name();
            patientFName = patient_new.getFirst_name();
        }

      /*  if (patient_new.getAdditionalDocPath() != null && !patient_new.getAdditionalDocPath().trim().isEmpty()) {
            String additionalDocPathVal = patient_new.getAdditionalDocPath();
            ArrayList<String> additionalDocPaths = new ArrayList<>(Arrays.asList(additionalDocPathVal.split(",")));
            ArrayList<File> files = new ArrayList<>();
            if (additionalDocPaths.size()>0) {
                for(int i = 0; i<additionalDocPaths.size();i++)
                    files.add(new File(additionalDocPaths.get(i).trim()));
            }
            additionalDocRV.setHasFixedSize(true);
            additionalDocRV.setLayoutManager(new LinearLayoutManager(PatientDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(files, this);
            additionalDocRV.setAdapter(horizontalAdapter);
        }*/

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
        Log.v("ADP", "ADP: " + "pd_image: " + patient_new.getPatient_photo());
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

        String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patient_new.getDate_of_birth());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            String dob_text = en__or_dob(dob); //to show text of English into Odiya...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            String dob_text = en__bn_dob(dob); //to show text of English into Odiya...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            String dob_text = en__te_dob(dob); //to show text of English into telugu...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            String dob_text = en__mr_dob(dob); //to show text of English into telugu...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            String dob_text = en__as_dob(dob); //to show text of English into telugu...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            String dob_text = en__ml_dob(dob); //to show text of English into telugu...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            String dob_text = en__kn_dob(dob); //to show text of English into telugu...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            String dob_text = en__ru_dob(dob); //to show text of English into Russian...
            dobView.setText(dob_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            String dob_text = en__ta_dob(dob); //to show text of English into Tamil...
            dobView.setText(dob_text);
        } else {
            dobView.setText(dob);
        }
        //dobView.setText(dob);
        mGender = patient_new.getGender();
        if (patient_new.getGender() == null || patient_new.getGender().equals("")) {
            genderView.setVisibility(View.GONE);
        } else {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                }else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                }else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }  else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                }else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                }else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    genderView.setText(patient_new.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                if (patient_new.getGender().equalsIgnoreCase("M")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patient_new.getGender().equalsIgnoreCase("F")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patient_new.getGender().equalsIgnoreCase("Other")) {
                    genderView.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                }else {
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

        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            String country = "भारत";
            String state_india = switch_mr_state_india_edit(patient_new.getState_province());

            if (patient_new.getPostal_code() != null) {
                String addrFinalLine;

                if (!patient_new.getPostal_code().equalsIgnoreCase("")) {
                    addrFinalLine = String.format("%s, %s, %s, %s",
                            city_village, state_india,
                            patient_new.getPostal_code(), country);
                } else {
                    addrFinalLine = String.format("%s, %s, %s",
                            city_village, state_india,
                            country);
                }
                addrFinalView.setText(addrFinalLine);
            } else {
                String addrFinalLine = String.format("%s, %s, %s",
                        city_village, state_india,
                        country);
                addrFinalView.setText(addrFinalLine);
            }
        }
        else {
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
        }

        phoneView.setText(patient_new.getPhone_number());

        if(patient_new.getEducation_level() != null) {
            if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                education_statusView.setText("नहीं दिया गया");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                education_statusView.setText("ଦିଅ ଯାଇ ନାହିଁ");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                education_statusView.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                education_statusView.setText("సమకూర్చబడలేదు");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                education_statusView.setText("झाले नाही");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                education_statusView.setText("প্ৰদান কৰা হোৱা নাই");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                education_statusView.setText("നൽകിയിട്ടില്ല");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                education_statusView.setText("ಒದಗಿಸಲಾಗಿಲ್ಲ");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                education_statusView.setText("Не предоставлен");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                education_statusView.setText("সরবরাহ করা হয়নি");
            } else if (patient_new.getEducation_level().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                education_statusView.setText("வழங்கப்படவில்லை");
            } else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String education = switch_hi_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String education = switch_or_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String education = switch_ta_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String education = switch_te_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String education = switch_mr_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String education = switch_as_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String education = switch_ml_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String education = switch_kn_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String education = switch_ru_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String education = switch_gu_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String education = switch_bn_education_edit(patient_new.getEducation_level());
                    education_statusView.setText(education);
                } else {
                    education_statusView.setText(patient_new.getEducation_level());
                }
            }
        }

        if(patient_new.getEconomic_status() != null) {
            if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                economic_statusView.setText("नहीं दिया गया");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                economic_statusView.setText("ଦିଅ ଯାଇ ନାହିଁ");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                economic_statusView.setText("வழங்கப்படவில்லை");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                economic_statusView.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                economic_statusView.setText("సమకూర్చబడలేదు");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                economic_statusView.setText("झाले नाही");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                economic_statusView.setText("প্ৰদান কৰা হোৱা নাই");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                economic_statusView.setText("നൽകിയിട്ടില്ല");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                economic_statusView.setText("ಒದಗಿಸಲಾಗಿಲ್ಲ");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                economic_statusView.setText("Не предоставлен");
            } else if (patient_new.getEconomic_status().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                economic_statusView.setText("সরবরাহ করা হয়নি");
            } else {
                economic_statusView.setText(patient_new.getEconomic_status());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String economic = switch_hi_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String economic = switch_or_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String economic = switch_ta_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String economic = switch_bn_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String economic = switch_gu_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String economic = switch_te_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String economic = switch_mr_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String economic = switch_as_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String economic = switch_ml_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String economic = switch_kn_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String economic = switch_ru_economic_edit(patient_new.getEconomic_status());
                    economic_statusView.setText(economic);
                } else {
                    economic_statusView.setText(patient_new.getEconomic_status());
                }
            }
        }

        if(patient_new.getCaste() != null) {
            if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                casteView.setText("नहीं दिया गया");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                casteView.setText("ଦିଅ ଯାଇ ନାହିଁ");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                casteView.setText("సమకూర్చబడలేదు");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                casteView.setText("झाले नाही");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                casteView.setText("প্ৰদান কৰা হোৱা নাই");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                casteView.setText("നൽകിയിട്ടില്ല");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                casteView.setText("ಒದಗಿಸಲಾಗಿಲ್ಲ");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                casteView.setText("Не предоставлен");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                casteView.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                casteView.setText("সরবরাহ করা হয়নি");
            } else if (patient_new.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                casteView.setText("வழங்கப்படவில்லை");
            } else {
                casteView.setText(patient_new.getCaste());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String caste = switch_gu_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String caste = switch_te_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String caste = switch_mr_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String caste = switch_as_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String caste = switch_ml_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String caste = switch_kn_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String caste = switch_ru_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String caste = switch_bn_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String caste = switch_ta_caste_edit(patient_new.getCaste());
                    casteView.setText(caste);
                } else {
                    casteView.setText(patient_new.getCaste());
                }
            }
        }

        if (patient_new.getSdw() != null && !patient_new.getSdw().equals("")) {
            sdwView.setText(patient_new.getSdw());
        } else {
            sdwRow.setVisibility(View.GONE);
        }

        if (patient_new.getAadhar_details() != null && !patient_new.getAadhar_details().equals("")) {
            aadharView.setText(patient_new.getAadhar_details());
        } else {
            aadharRow.setVisibility(View.GONE);
        }

        if (patient_new.getOccupation() != null && !patient_new.getOccupation().equals("")) {
            occuView.setText(patient_new.getOccupation());
        } else {
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

        whatsapp_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumberWithCountryCode = "+91" + phoneView.getText().toString();
//                String message =
//                        getString(R.string.hello_my_name_is) + " " + sessionManager.getChwname() + " " +
//                                /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);
                String message = getString(R.string.hello_my_name_is) + sessionManager.getChwname();
                //                        + getString(R.string.and_i_be_assisting_you);

                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(
                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                        phoneNumberWithCountryCode, message))));
            }
        });

        //mobile calling is supported...
        calling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL); //ACTION_DIAL: doesnt requires permission...
                intent.setData(Uri.parse("tel:" + phoneView.getText().toString()));
                startActivity(intent);
            }
        });
    }

    public void profilePicDownloaded() {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(patientUuid);
        Logger.logD(TAG, "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
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
                            isImageDownloaded = imagesDAO.insertPatientProfileImages
                                    (AppConstants.IMAGE_PATH + patientUuid + ".jpg", "PP", patientUuid);
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
                        complaintxt1.setText(getString(R.string.screening));
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
                        complaintxt1.setText(getString(R.string.screening));
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
                visitSummary.putExtra("patientFirstName",patientFName);
                visitSummary.putExtra("patientLastName", patientLName);
                visitSummary.putExtra("gender", mGender);
                visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month);
                visitSummary.putExtra("tag", getVisitType(visit_id, intentTag));
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

    private String getVisitType(String visit_id, String intentTag) {
        String encounterlocalAdultintial = intentTag;
        String encounterIDSelection = "visituuid = ? AND sync!=?";
        String[] encounterIDArgs = {visit_id, "false"};

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        EncounterDAO encounterDAO = new EncounterDAO();
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());
        }
        encounterCursor.close();
        if(!encounterlocalAdultintial.equalsIgnoreCase(intentTag))
            return intentTag;
        else
            return "skipComplaint";
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

                    if (famHistValue != null && !famHistValue.equals("")) {
                        famHistView.setText(Html.fromHtml(famHistValue));
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

                    Log.v(TAG, medHistValue);
                    if (medHistValue != null && !medHistValue.equals("")) {
                        medHistView.setText(Html.fromHtml(medHistValue));
                    } else {
                        medHistView.setText(getString(R.string.string_no_hist));
                    }
                } while (visitCursor.moveToPrevious());
            }
            visitCursor.close();
        }
    }

    public void pastVisits(String patientuuid) {
        String visitSelection = "patientuuid = ?";
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
                                        visitValue = visitValue + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";
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
                    // Called when we close org on vitals screen and Didn't select any complaints
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

}
