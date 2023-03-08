package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.utilities.StringUtils.arrayValueInJson;
import static org.intelehealth.app.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.app.utilities.StringUtils.switch_ar_to_en_state;
import static org.intelehealth.app.utilities.StringUtils.switch_ar_to_en_village;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.householdSurvey.model.AidTypeAnswerValue;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.activities.setupActivity.SetupActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.ActivityIdentificationBinding;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.PatientAttributeLanguageModel;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.EditTextUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.IReturnValues;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidGenerator;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class IdentificationActivity extends AppCompatActivity /*implements SurveyCallback, ViewPagerCallback, PregnancyOutcomeCallback*/ {
    //Following variables have been commented out as they belongs originally to NAS project and are no longer required for Syriana: By Nishita
    /*private LinearLayout pregnancyQuestionsLinearLayout;
    //    private ArrayAdapter<CharSequence> economicStatusAdapter;
    private ArrayAdapter<CharSequence> casteAdapter;
    MultipleDiseasesDialog dialog;
    //    EditText mRelationship;
    EditText economicText;
    EditText educationText;
    //    TextInputLayout casteLayout;
//    TextInputLayout economicLayout;
//    TextInputLayout educationLayout;
    EditText casteText;
    //    Spinner mCaste;
    //    Spinner mEconomicStatus;
    private int retainPickerYear;
    private int retainPickerMonth;
    private int retainPickerDate;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the mDOB editText value and add in the db.
    // Roster Questions
    Spinner spinner_phoneownership, spinner_bpchecked, spinner_sugarchecked, spinner_hbchecked,
            spinner_bmi, spinner_healthissuereported, spinner_primaryhealthprovider, spinner_firstlocation, spinner_referredto, spinner_modeoftransport,
            spinner_experiencerscore, spinner_block, spinner_village, spinner_focalPointBlock;

    Spinner spinner_focalPointVillage, spinner_pregnantpasttwoyrs, spinner_outcomepregnancy, spinner_childalive, spinner_placeofdeliverypregnant,
            spinner_sexofbaby, spinner_pregnancyplanned, spinner_pregnancyhighriskcase, spinner_pregnancycomplications, spinner_singlemultiplebirths;

    ArrayAdapter<CharSequence> adapter_whatisyourrelation, adapter_maritualstatus , adapter_phoneownership, adapter_bpchecked, adapter_sugarchecked, adapter_hbchecked, adapter_bmi, adapter_focalPointBlock, adapter_FocalVillage_Peth, adapter_FocalVillage_Surgana, adapter_block, adapter_healthissuereported, adapter_primaryhealthprovider, adapter_firstlocation, adapter_referredto, adapter_modeoftransport, adapter_experiencerscore;


    ArrayAdapter<CharSequence> adapter_pregnantpasttwoyrs, adapter_outcomepregnancy, adapter_childalive, adapter_placeofdeliverypregnant, adapter_sexofbaby, adapter_pregnancyplanned, adapter_pregnancyhighriskcase, adapter_pregnancycomplications, adapter_singlemultiplebirths;

    EditText edittext_noofepisodes, edittext_avgcosttravel, edittext_avgcostconsult, edittext_avgcostmedicines;

    EditText edittext_howmanytimmespregnant, edittext_yearofpregnancy, edittext_monthspregnancylast, edittext_monthsbeingpregnant, edittext_babyagedied;

    // TextInputLayout til_whatisyourrelation_other, til_occupation_other;
//    LinearLayout textinputlayout_blockVillageOther;
    // TextInputEditText et_whatisyourrelation_other, et_occupation_other, et_block_other, et_village_other
//    private TextView blockTextView, villageTextView;
    private LinearLayout llPORoaster, ll18;
    public ViewPager2 viewPager2;
    private HouseholdSurveyAdapter adapter;
    private PregnancyOutcomeAdapter pregnancyOutcomeAdapter;
    private List<HealthIssues> healthIssuesList = new ArrayList<>();
    private List<PregnancyRosterData> pregnancyOutcomesList = new ArrayList<>();
    int noOfClicks = 0; */

    private static final String TAG = IdentificationActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    private ArrayAdapter<CharSequence> educationAdapter, occupationAdapter, countryAdapter, stateAdapter, villageAdapter, relationAdapter, maritalStatusAdapter, residenceNatureAdapter, linkNatureAdapter, husbandStatusAdapter, independentResidenceAdapter, whyHOHAdapter, changeConditionReasonAdapter, percentageIncomeAdapter, specialNeedsAdapter, lossDueToEarthquakeAdapter, lossOfAnalgesicAdapter, lossOfBreadwinnerAdapter, strongSocialTiesAdapter, placesOfPreferenceAdapter;
    UuidGenerator uuidGenerator = new UuidGenerator();
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    Patient patient1 = new Patient();
    private String patientUuid = "", mGender, patientID_edit, country1, state, uuid = "", mCurrentPhotoPath, privacy_value, headOfHousehold = "No";
    private int mDOBYear, mDOBMonth, mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0, mAgeMonths = 0, mAgeDays = 0;
    PatientsDAO patientsDAO = new PatientsDAO();
    TextInputLayout postal_layout;
    EditText mFirstName, mMiddleName, mLastName, mDOB, mPhoneNum, mAge, mAddress1, mAddress2, mPostal, countryText, stateText, sinceChangeHappenedET, sinceSupportingFamilyET;
    MaterialAlertDialogBuilder mAgePicker;
    RadioButton mGenderM, mGenderF, mGenderO, yesHOH, noHOH;
    RadioGroup radioGrp;
    Spinner mOccupation, mCountry, mState, mEducation, mVillage;
    LinearLayout countryStateLayout;
    ImageView mImageView;
    PatientDTO patientdto = new PatientDTO();
    ImagesDAO imagesDAO = new ImagesDAO();
    Context context;
    private String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private String BlockCharacterSet_Name = "\\@$!=><&^*+\"\'€¥£`~";
    Intent i_privacy;
    int dob_indexValue = 15;
    String mAddress1Value = "", mAddress2Value = "", mPostalValue = "", stateValue = "", countryValue = "", villageValue = "", mRelationshipValue = "";
    Spinner HOH_relation_spinner, marital_status_spinner, husband_status_spinner, residence_nature_spinner, link_nature_spinner, independent_resid_spinner, whyHOH_spinner, mainTendency_spinner, changeConditionReason_spinner, percentage_income_spinner;
    private ActivityIdentificationBinding binding;
    MaterialCheckBox fhhSurveyCB, generalCB, studentCB, emergencyCB;
    ArrayList<String> selectedAid_en, selectedAid_ar;
    ScrollView scrollView;
    private MaterialCheckBox phone_checkbox, earthquakeVictimCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        binding = ActivityIdentificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(R.string.title_activity_identification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        i_privacy = getIntent();
        context = IdentificationActivity.this;
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initUI();

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                this.setTitle(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
            }
            if (intent.hasExtra("newMember")) {
                mPostalValue = getIntent().getStringExtra("postalCode");
                mAddress1Value = getIntent().getStringExtra("address1");
                mAddress2Value = getIntent().getStringExtra("address2");
                countryValue = getIntent().getStringExtra("country");
                stateValue = getIntent().getStringExtra("state");
                villageValue = getIntent().getStringExtra("villageSurvey");
                mRelationshipValue = getIntent().getStringExtra("relationshipStatus");
            }
        }

        configureViewsFromConfig();

        //setting the fields when user clicks edit details
        /* We enter a '-' into the db if the field is empty when saving.
         * The following statements are used to handle those situations
         * And ensure that we don't display a '-' to user in the EditTexts when they're editing the patient*/

        if (patient1.getFirst_name() != null && !patient1.getFirst_name().equalsIgnoreCase("-"))
            mFirstName.setText(patient1.getFirst_name());

        if (patient1.getMiddle_name() != null && !patient1.getMiddle_name().equalsIgnoreCase("-"))
            mMiddleName.setText(patient1.getMiddle_name());

        if (patient1.getLast_name() != null && !patient1.getLast_name().equalsIgnoreCase("-"))
            mLastName.setText(patient1.getLast_name());

        if (patient1.getDate_of_birth() != null && !patient1.getDate_of_birth().equalsIgnoreCase("-"))
            mDOB.setText(patient1.getDate_of_birth());

        if (patient1.getPhone_number() != null && !patient1.getPhone_number().equalsIgnoreCase("-"))
            mPhoneNum.setText(patient1.getPhone_number());


        if (patientID_edit == null) {
            mAddress1.setText(mAddress1Value);
            mAddress2.setText(mAddress2Value);

            //TODO: Support for state and country

            if (!mPostalValue.equalsIgnoreCase("-")) mPostal.setText(mPostalValue);

            // block
          /*  try {
                String blockLanguage = "block_" + sessionManager.getAppLanguage();
                int block_id = getResources().getIdentifier(blockLanguage, "array", getApplicationContext().getPackageName());
                if (block_id != 0) {
                    adapter_block = ArrayAdapter.createFromResource(this, block_id, android.R.layout.simple_spinner_dropdown_item);
                }
                spinner_block.setAdapter(adapter_block);

                String block_Transl = "";
                block_Transl = getPethBlock_edit(blockValue, sessionManager.getAppLanguage());
                int spinner_position = adapter_block.getPosition(block_Transl);
                spinner_block.setSelection(spinner_position);

            } catch (Exception e) {
                e.printStackTrace();
            }
            // block

            // village
            spinner_block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 1:
                            spinner_village.setVisibility(View.VISIBLE);
                            et_block_other.setVisibility(View.GONE);
                            et_village_other.setVisibility(View.GONE);
                            et_block_other.setText("");
                            et_village_other.setText("");

                            String focalVillagePeth_Language = "peth_block_village_" + sessionManager.getAppLanguage();
                            int focalVillage_Peth_id = getResources().getIdentifier(focalVillagePeth_Language, "array", getApplicationContext().getPackageName());
                            if (focalVillage_Peth_id != 0) {
                                adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        focalVillage_Peth_id, android.R.layout.simple_spinner_dropdown_item);
                            }
                            adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                    focalVillage_Peth_id, R.layout.custom_spinner);
                            spinner_village.setAdapter(adapter_FocalVillage_Peth);

                            try {
                                String village_Peth_Transl = "";
                                village_Peth_Transl = getPethBlockVillage_edit(villageValue, sessionManager.getAppLanguage());
                                int spinner_peth_position = adapter_FocalVillage_Peth.getPosition(village_Peth_Transl);
                                spinner_village.setSelection(spinner_peth_position);
                            } catch (NullPointerException exception) {
                                exception.printStackTrace();
                            }
                            break;

                        case 2:
                            spinner_village.setVisibility(View.VISIBLE);
                            et_block_other.setVisibility(View.GONE);
                            et_village_other.setVisibility(View.GONE);
                            et_block_other.setText("");
                            et_village_other.setText("");

                            String focalVillageSurgane_Language = "suragana_block_villages_" + sessionManager.getAppLanguage();
                            int focalVillage_Surgane_id = getResources().getIdentifier(focalVillageSurgane_Language, "array", getApplicationContext().getPackageName());
                            if (focalVillage_Surgane_id != 0) {
                                adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        focalVillage_Surgane_id, android.R.layout.simple_spinner_dropdown_item);
                            }
                            adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                    focalVillage_Surgane_id, R.layout.custom_spinner);
                            spinner_village.setAdapter(adapter_FocalVillage_Surgana);

                            try {
                                String village_Surgane_Transl = "";
                                village_Surgane_Transl = getPethBlockVillage_edit(villageValue, sessionManager.getAppLanguage());
                                int spinner_surgana_position = adapter_FocalVillage_Surgana.getPosition(village_Surgane_Transl);
                                spinner_village.setSelection(spinner_surgana_position);
                            } catch (NullPointerException exception) {
                                exception.printStackTrace();
                            }
                            break;

                        case 3:
                            spinner_village.setVisibility(View.GONE);
                            spinner_village.setSelection(0);
                            textinputlayout_blockVillageOther.setVisibility(View.VISIBLE);
                            et_block_other.setVisibility(View.VISIBLE);
                            et_village_other.setVisibility(View.VISIBLE);
                            break;

                        default:
//                        spinner_village.setAdapter(null);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            }); */

        } else {
            mAddress1.setText(patient1.getAddress1());
            mAddress2.setText(patient1.getAddress2());
            if (!patient1.getPostal_code().equalsIgnoreCase("-"))
                mPostal.setText(patient1.getPostal_code());
        }

        if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
            mImageView.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

        setAdapterToSpinners();

        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();
        }

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID_edit != null) {

            if (patient1.getPhone_number() != null && !patient1.getPhone_number().equalsIgnoreCase("-")) {
                mPhoneNum.setText(patient1.getPhone_number());
                mPhoneNum.setEnabled(true);
                phone_checkbox.setChecked(false);
            } else {
                mPhoneNum.setEnabled(false);
                phone_checkbox.setChecked(true);
                mPhoneNum.setText("");
            }

            if (patient1.getGender().equals("M")) {
                mGenderM.setChecked(true);
                if (mGenderF.isChecked()) mGenderF.setChecked(false);
                if (mGenderO.isChecked()) mGenderO.setChecked(false);
                Log.v(TAG, "yes");
            } else if (patient1.getGender().equals("F")) {
                mGenderF.setChecked(true);
                if (mGenderM.isChecked()) mGenderM.setChecked(false);
                if (mGenderO.isChecked()) mGenderO.setChecked(false);
                Log.v(TAG, "yes");
            } else if (patient1.getGender().equals("O")) {
                mGenderO.setChecked(true);
                if (mGenderM.isChecked()) mGenderM.setChecked(false);
                if (mGenderF.isChecked()) mGenderF.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                // do nothing...
            }

        }
        if (mGenderM.isChecked()) {
            mGender = "M";
        } else {
            mGender = "F";
        }

        // Phone checkbox
        if (phone_checkbox.isChecked()) mPhoneNum.setEnabled(false);
        else mPhoneNum.setEnabled(true);

        phone_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mPhoneNum.setError(null);

                if (checked) {
                    mPhoneNum.setEnabled(false);
                    mPhoneNum.setText("");
                } else mPhoneNum.setEnabled(true);
            }
        });
        // Phone checkbox - End


        if (patientID_edit != null) {
            // setting country according database
            mCountry.setSelection(countryAdapter.getPosition(String.valueOf(patient1.getCountry())));

            // setting state - start
            String state = String.valueOf(patient1.getState_province());
            if (sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                mState.setSelection(stateAdapter.getPosition(state));
            else if (sessionManager.getAppLanguage().equalsIgnoreCase("ar"))
                mState.setSelection(stateAdapter.getPosition(StringUtils.switch_en_to_ar_state(state)));
            // setting state - end

            if (patient1 != null && patient1.getPatientAidType() != null) {
                Log.d("Patient Aid Type", patient1.getPatientAidType());
                if (patient1.getPatientAidType().toString().contains(getString(R.string.student_aid_survey))) {
                    studentCB.setChecked(true);
//                    hideSpinnersBasedOnAid();
                }
                if (patient1.getPatientAidType().toString().contains(getString(R.string.emergency_survey))) {
                    emergencyCB.setChecked(true);
//                    hideSpinnersBasedOnAid();
                }
                if (patient1.getPatientAidType().toString().contains(getString(R.string.fhh_survey))) {
                    fhhSurveyCB.setChecked(true);
//                    showSpinnersBasedOnAid();
                }
                if (patient1.getPatientAidType().toString().contains(getString(R.string.general_aid_survey))) {
                    generalCB.setChecked(true);
//                    showSpinnersBasedOnAid();
                }

            }

            if (patient1 != null & patient1.getHeadOfHousehold() != null) {
                String headOfHousehold = patient1.getHeadOfHousehold();
                if (headOfHousehold.equalsIgnoreCase(getString(R.string.yes)))
                    yesHOH.setChecked(true);
                else noHOH.setChecked(true);

            }
            if (patient1 != null && patient1.getEducation_level() != null && patient1.getEducation_level().equalsIgnoreCase(getString(R.string.not_provided)))
                mEducation.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String education = patient1.getEducation_level();
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else {
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
                }
            }

            if (patient1 != null && patient1.getOccupation() != null && patient1.getOccupation().equalsIgnoreCase(getString(R.string.not_provided)))
                mOccupation.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String occupation = patient1.getOccupation();
                    mOccupation.setSelection(occupationAdapter != null ? occupationAdapter.getPosition(occupation) : 0);
                } else {
                    mOccupation.setSelection(occupationAdapter != null ? occupationAdapter.getPosition(patient1.getOccupation()) : 0);
                }
            }

            if (patient1 != null && patient1.getMaritalstatus() != null && patient1.getMaritalstatus().equalsIgnoreCase(getString(R.string.not_provided)))
                marital_status_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String maritalStatus = patient1.getMaritalstatus();
                    marital_status_spinner.setSelection(maritalStatusAdapter != null ? maritalStatusAdapter.getPosition(maritalStatus) : 0);
                } else {
                    marital_status_spinner.setSelection(maritalStatusAdapter != null ? maritalStatusAdapter.getPosition(patient1.getMaritalstatus()) : 0);
                }
            }

            if (patient1 != null && patient1.getIndependentResidence() != null && patient1.getIndependentResidence().equalsIgnoreCase(getString(R.string.not_provided)))
                independent_resid_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String independentResid = patient1.getIndependentResidence();
                    independent_resid_spinner.setSelection(independentResidenceAdapter != null ? independentResidenceAdapter.getPosition(independentResid) : 0);
                } else {
                    independent_resid_spinner.setSelection(independentResidenceAdapter != null ? independentResidenceAdapter.getPosition(patient1.getIndependentResidence()) : 0);
                }
            }

            if (patient1 != null && patient1.getResidentialComplexNature() != null && patient1.getResidentialComplexNature().equalsIgnoreCase(getString(R.string.not_provided)))
                residence_nature_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String residenceNature = patient1.getResidentialComplexNature();
                    residence_nature_spinner.setSelection(residenceNatureAdapter != null ? residenceNatureAdapter.getPosition(residenceNature) : 0);
                } else {
                    residence_nature_spinner.setSelection(residenceNatureAdapter != null ? residenceNatureAdapter.getPosition(patient1.getResidentialComplexNature()) : 0);
                }
            }

            if (patient1 != null && patient1.getLinkNature() != null && patient1.getLinkNature().equalsIgnoreCase(getString(R.string.not_provided)))
                link_nature_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String linkNature = patient1.getLinkNature();
                    link_nature_spinner.setSelection(linkNatureAdapter != null ? linkNatureAdapter.getPosition(linkNature) : 0);
                } else {
                    link_nature_spinner.setSelection(linkNatureAdapter != null ? linkNatureAdapter.getPosition(patient1.getLinkNature()) : 0);
                }
            }

            if (patient1 != null && patient1.getRelationshiphoh() != null && patient1.getRelationshiphoh().equalsIgnoreCase(getString(R.string.not_provided)))
                HOH_relation_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String relationHOH = patient1.getRelationshiphoh();
                    HOH_relation_spinner.setSelection(relationAdapter != null ? relationAdapter.getPosition(relationHOH) : 0);
                } else {
                    HOH_relation_spinner.setSelection(relationAdapter != null ? relationAdapter.getPosition(patient1.getRelationshiphoh()) : 0);
                }
            }

            if (patient1 != null && patient1.getHusbandStatus() != null && patient1.getHusbandStatus().equalsIgnoreCase(getString(R.string.not_provided)))
                husband_status_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String husbandStatus = patient1.getHusbandStatus();
                    husband_status_spinner.setSelection(husbandStatusAdapter != null ? husbandStatusAdapter.getPosition(husbandStatus) : 0);
                } else {
                    husband_status_spinner.setSelection(husbandStatusAdapter != null ? husbandStatusAdapter.getPosition(patient1.getHusbandStatus()) : 0);
                }
            }

            if (patient1 != null && patient1.getPercentageOfIncome() != null && patient1.getPercentageOfIncome().equalsIgnoreCase(getString(R.string.not_provided)))
                percentage_income_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String percentageOfIncome = patient1.getPercentageOfIncome();
                    percentage_income_spinner.setSelection(percentageIncomeAdapter != null ? percentageIncomeAdapter.getPosition(percentageOfIncome) : 0);
                } else {
                    percentage_income_spinner.setSelection(percentageIncomeAdapter != null ? percentageIncomeAdapter.getPosition(patient1.getPercentageOfIncome()) : 0);
                }
            }

            if (patient1 != null && patient1.getMainTendency() != null && patient1.getMainTendency().equalsIgnoreCase(getString(R.string.not_provided)))
                mainTendency_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String mainTendency = patient1.getMainTendency();
                    mainTendency_spinner.setSelection(independentResidenceAdapter != null ? independentResidenceAdapter.getPosition(mainTendency) : 0);
                } else {
                    mainTendency_spinner.setSelection(independentResidenceAdapter != null ? independentResidenceAdapter.getPosition(patient1.getMainTendency()) : 0);
                }
            }

            if (patient1 != null && patient1.getConditionOfSupport() != null && patient1.getConditionOfSupport().equalsIgnoreCase(getString(R.string.not_provided)))
                changeConditionReason_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String changeCondition = patient1.getConditionOfSupport();
                    changeConditionReason_spinner.setSelection(changeConditionReasonAdapter != null ? changeConditionReasonAdapter.getPosition(changeCondition) : 0);
                } else {
                    changeConditionReason_spinner.setSelection(changeConditionReasonAdapter != null ? changeConditionReasonAdapter.getPosition(patient1.getConditionOfSupport()) : 0);
                }
            }

            if (patient1 != null && patient1.getMainOfficial() != null && patient1.getMainOfficial().equalsIgnoreCase(getString(R.string.not_provided)))
                whyHOH_spinner.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    //TODO: Arabic language changes
                    String whyHOH = patient1.getMainOfficial();
                    whyHOH_spinner.setSelection(whyHOHAdapter != null ? whyHOHAdapter.getPosition(whyHOH) : 0);
                } else {
                    whyHOH_spinner.setSelection(whyHOHAdapter != null ? whyHOHAdapter.getPosition(patient1.getMainOfficial()) : 0);
                }
            }

            if (patient1 != null && patient1.getSinceChangeHappened() != null)
                sinceChangeHappenedET.setText(patient1.getSinceChangeHappened());
            else sinceChangeHappenedET.setText("");

            if (patient1 != null && patient1.getSinceWhenMainResponsible() != null)
                sinceSupportingFamilyET.setText(patient1.getSinceWhenMainResponsible());
            else sinceSupportingFamilyET.setText("");

            setSpinnerSelectionForEmergencyQuestions();

        } else {
            mCountry.setSelection(countryAdapter.getPosition(country1));
            //   mState.setSelection(stateAdapter.getPosition(state));
            // setting state - start
            String state = String.valueOf(sessionManager.getStateName());
            if (sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                mState.setSelection(stateAdapter.getPosition(state));
            else if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                int position = stateAdapter.getPosition(StringUtils.switch_en_to_ar_state(state));
                mState.setSelection(position);
            }
            // setting state - end
        }

        mGenderF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        yesHOH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        noHOH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientTemp = "";
                if (patientUuid.equalsIgnoreCase("")) {
                    patientTemp = patientID_edit;
                } else {
                    patientTemp = patientUuid;
                }
                File filePath = new File(AppConstants.IMAGE_PATH + patientTemp);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(IdentificationActivity.this, CameraActivity.class);

                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientTemp);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            }
        });

        mDOBYear = today.get(Calendar.YEAR);
        mDOBMonth = today.get(Calendar.MONTH);
        mDOBDay = today.get(Calendar.DAY_OF_MONTH);

        //DOB is set using an AlertDialog
        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                mDOB.setError(null);
                mAge.setError(null);
                //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());
                dob_indexValue = monthOfYear; //fetching the inex value of month selected...

                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    //TODO: Change hindi language to arabic
                    String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                    mDOB.setText(dob_text);
                } else {
                    mDOB.setText(dobString);
                }

                mDOBYear = year;
                mDOBMonth = monthOfYear;
                mDOBDay = dayOfMonth;

                String age = getYear(dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DATE), today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
                //get years months days
                String[] frtData = age.split("-");

                String[] yearData = frtData[0].split(" ");
                String[] monthData = frtData[1].split(" ");
                String[] daysData = frtData[2].split(" ");

                mAgeYears = Integer.valueOf(yearData[0]);
                mAgeMonths = Integer.valueOf(monthData[1]);
                mAgeDays = Integer.valueOf(daysData[1]);
                String ageS = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " + mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " + mAgeDays + getResources().getString(R.string.days);
                mAge.setText(ageS);
            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });
        //if patient update then age will be set
        if (patientID_edit != null) {
            //dob to be displayed based on translation...
            String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth());
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                //TODO: Change hindi language to arabic
                mDOB.setText(dob_text);
            } else {
                mDOB.setText(dob);
            }
            //get year month days
            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth()).split(" ");
            mAgeYears = Integer.valueOf(ymdData[0]);
            mAgeMonths = Integer.valueOf(ymdData[1]);
            mAgeDays = Integer.valueOf(ymdData[2]);
            String age = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " + mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " + mAgeDays + getResources().getString(R.string.days);
            mAge.setText(age);
        }

        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAgePicker = new MaterialAlertDialogBuilder(IdentificationActivity.this, R.style.AlertDialogStyle);
                mAgePicker.setTitle(R.string.identification_screen_prompt_age);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
                mAgePicker.setView(convertView);
                NumberPicker yearPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
                NumberPicker monthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
                NumberPicker dayPicker = convertView.findViewById(R.id.dialog_3_numbers_unit);
                dayPicker.setVisibility(View.VISIBLE);

                final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
                final TextView dayTv = convertView.findViewById(R.id.dialog_2_numbers_text_3);
                dayPicker.setVisibility(View.VISIBLE);

                int totalDays = today.getActualMaximum(Calendar.DAY_OF_MONTH);
                dayTv.setText(getString(R.string.days));
                middleText.setText(getString(R.string.identification_screen_picker_years));
                endText.setText(getString(R.string.identification_screen_picker_months));


                yearPicker.setMinValue(0);
                yearPicker.setMaxValue(100);
                monthPicker.setMinValue(0);
                monthPicker.setMaxValue(12);

                dayPicker.setMinValue(0);
                dayPicker.setMaxValue(31);

                EditText yearText = yearPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
                EditText monthText = monthPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
                EditText dayText = dayPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));


                yearPicker.setValue(mAgeYears);
                monthPicker.setValue(mAgeMonths);
                dayPicker.setValue(mAgeDays);

                //year
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mAgeYears = Integer.valueOf(value);
                    }
                }, yearText);

                //month
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mAgeMonths = Integer.valueOf(value);
                    }
                }, monthText);

                //day
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mAgeDays = Integer.valueOf(value);
                    }
                }, dayText);
                mAgePicker.setPositiveButton(R.string.generic_ok, (dialog, which) -> {
                    String ageString = mAgeYears + getString(R.string.identification_screen_text_years) + " - " + mAgeMonths + getString(R.string.identification_screen_text_months) + " - " + mAgeDays + getString(R.string.days);
                    mAge.setText(ageString);

                    Calendar calendar = Calendar.getInstance();
                    int curYear = calendar.get(Calendar.YEAR);
                    int birthYear = curYear - mAgeYears;
                    int curMonth = calendar.get(Calendar.MONTH);
                    int birthMonth = curMonth - mAgeMonths;
                    int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - mAgeDays;
                    mDOBYear = birthYear;
                    mDOBMonth = birthMonth;

                    if (birthDay < 0) {
                        mDOBDay = birthDay + totalDays - 1;
                        mDOBMonth--;

                    } else {
                        mDOBDay = birthDay;
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
                    String dobString = simpleDateFormat.format(dob.getTime());
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                        //TODO: Change hindi language to arabic
                        mDOB.setText(dob_text);
                    } else {
                        mDOB.setText(dobString);
                    }
                    mDOBPicker.updateDate(mDOBYear, mDOBMonth, mDOBDay);
                    dialog.dismiss();
                });

                mAgePicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = mAgePicker.show();
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (patientID_edit != null) {
                onPatientUpdateClicked(patient1);
            } else {
                onPatientCreateClicked();
            }
        });

    }

    private void setSpinnerSelectionForEmergencyQuestions() {

        // We will set the data only if the emergency victim checkbox is selected

        if (earthquakeVictimCheckbox.isChecked()) {

            // Set data for special needs spinner
            // Check if the field has no value
            if (patient1 == null || patient1.getNatureOfSpecialNeeds() == null || getPatientAttributeFromJSON(patient1.getNatureOfSpecialNeeds()).getEn().equalsIgnoreCase("Not provided")) {
                binding.spinnerNatureSpecialNeeds.setSelection(0);
            } else {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(patient1.getNatureOfSpecialNeeds());
                String natureOfSpecialNeeds = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    natureOfSpecialNeeds = patientAttributeLanguageModel.getAr();
                } else {
                    natureOfSpecialNeeds = patientAttributeLanguageModel.getEn();
                }
                binding.spinnerNatureSpecialNeeds.setSelection(specialNeedsAdapter.getPosition(natureOfSpecialNeeds));
            }

            // Set data for loss as a result of earthquake spinner
            // Check if the field has no value
            if (patient1 == null || patient1.getLossAsAResultOfTheEarthquake() == null || getPatientAttributeFromJSON(patient1.getLossAsAResultOfTheEarthquake()).getEn().equalsIgnoreCase("Not provided")) {
                binding.spinnerLossAsAResultOfTheEarthquake.setSelection(0);
            } else {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(patient1.getLossAsAResultOfTheEarthquake());
                String lossAsAResultOfTheEarthquake = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    lossAsAResultOfTheEarthquake = patientAttributeLanguageModel.getAr();
                } else {
                    lossAsAResultOfTheEarthquake = patientAttributeLanguageModel.getEn();
                }
                binding.spinnerLossAsAResultOfTheEarthquake.setSelection(lossDueToEarthquakeAdapter.getPosition(lossAsAResultOfTheEarthquake));
            }

            // Set data for loss of breadwinner as a result of earthquake spinner
            // Check if the field has no value
            if (patient1 == null || patient1.getLossOfBreadwinnerAsAResultOfTheEarthquake() == null || getPatientAttributeFromJSON(patient1.getLossOfBreadwinnerAsAResultOfTheEarthquake()).getEn().equalsIgnoreCase("Not provided")) {
                binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.setSelection(0);
            } else {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(patient1.getLossOfBreadwinnerAsAResultOfTheEarthquake());
                String lossOfBreadwinner = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    lossOfBreadwinner = patientAttributeLanguageModel.getAr();
                } else {
                    lossOfBreadwinner = patientAttributeLanguageModel.getEn();
                }
                binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.setSelection(lossOfBreadwinnerAdapter.getPosition(lossOfBreadwinner));
            }

            // Set data for loss of analgesic as a result of earthquake spinner
            // Check if the field has no value
            if (patient1 == null || patient1.getLossOfAnalgesicAsAResultOfTheEarthquake() == null || getPatientAttributeFromJSON(patient1.getLossOfAnalgesicAsAResultOfTheEarthquake()).getEn().equalsIgnoreCase("Not provided")) {
                binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.setSelection(0);
            } else {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(patient1.getLossOfAnalgesicAsAResultOfTheEarthquake());
                String lossOfAnalgesic = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    lossOfAnalgesic = patientAttributeLanguageModel.getAr();
                } else {
                    lossOfAnalgesic = patientAttributeLanguageModel.getEn();
                }
                binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.setSelection(lossOfAnalgesicAdapter.getPosition(lossOfAnalgesic));
            }

            // Set data for strong social ties as a result of earthquake spinner
            // Check if the field has no value
            if (patient1 == null || patient1.getStrongSocialTiesInYourResidenceArea() == null || getPatientAttributeFromJSON(patient1.getStrongSocialTiesInYourResidenceArea()).getEn().equalsIgnoreCase("Not provided")) {
                binding.spinnerStrongSocialTies.setSelection(0);
            } else {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(patient1.getStrongSocialTiesInYourResidenceArea());
                String strongSocialTies = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    strongSocialTies = patientAttributeLanguageModel.getAr();
                } else {
                    strongSocialTies = patientAttributeLanguageModel.getEn();
                }
                binding.spinnerStrongSocialTies.setSelection(strongSocialTiesAdapter.getPosition(strongSocialTies));
            }

            // Set data for places of relatives in other parts of Syria
            // Check if the field has no value
            if (patient1 == null || patient1.getPlacesOfRelativesInOtherPartsOfSyria() == null || patient1.getPlacesOfRelativesInOtherPartsOfSyria().isEmpty()) {
                binding.etPlacesOfRelatives.setText("");
            } else {
                binding.etPlacesOfRelatives.setText(patient1.getPlacesOfRelativesInOtherPartsOfSyria());
            }

            // Set data for places of preference for hosting as a result of earthquake spinner
            // Check if the field has no value
            if (patient1 == null || patient1.getPlacesOfPreferenceForHosting() == null || getPatientAttributeFromJSON(patient1.getPlacesOfPreferenceForHosting()).getEn().equalsIgnoreCase("Not provided")) {
                binding.spinnerPlacesOfPreference.setSelection(0);
            } else {
                PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(patient1.getPlacesOfPreferenceForHosting());
                String placesOfPreference = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    placesOfPreference = patientAttributeLanguageModel.getAr();
                } else {
                    placesOfPreference = patientAttributeLanguageModel.getEn();
                }
                binding.spinnerPlacesOfPreference.setSelection(placesOfPreferenceAdapter.getPosition(placesOfPreference));
            }
        }
    }

    private void setAdapterToSpinners() {
        Resources res = getResources();
        try {
            String countriesLanguage = "countries";
            int countries = res.getIdentifier(countriesLanguage, "array", getApplicationContext().getPackageName());
            if (countries != 0) {
                countryAdapter = ArrayAdapter.createFromResource(this, countries, R.layout.custom_spinner);

            }
            mCountry.setAdapter(countryAdapter);
            mCountry.setSelection(countryAdapter.getPosition(sessionManager.getCountryName()));
            mCountry.setEnabled(false);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String relationLanguage = "relationshipHoH_" + sessionManager.getAppLanguage();
            int relations = res.getIdentifier(relationLanguage, "array", getApplicationContext().getPackageName());
            if (relations != 0) {
                relationAdapter = ArrayAdapter.createFromResource(this, relations, R.layout.custom_spinner);
            }
            HOH_relation_spinner.setAdapter(relationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String maritalLanguage = "marital_" + sessionManager.getAppLanguage();
            int maritalStatus = res.getIdentifier(maritalLanguage, "array", getApplicationContext().getPackageName());
            if (maritalStatus != 0) {
                maritalStatusAdapter = ArrayAdapter.createFromResource(this, maritalStatus, R.layout.custom_spinner);
            }
            marital_status_spinner.setAdapter(maritalStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try {
            String residNatureLanguage = "nature_residential_complex_" + sessionManager.getAppLanguage();
            int residNature = res.getIdentifier(residNatureLanguage, "array", getApplicationContext().getPackageName());
            if (residNature != 0) {
                residenceNatureAdapter = ArrayAdapter.createFromResource(this, residNature, R.layout.custom_spinner);
            }
            residence_nature_spinner.setAdapter(residenceNatureAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String indepResidLanguage = "independent_residence_" + sessionManager.getAppLanguage();
            int residIndep = res.getIdentifier(indepResidLanguage, "array", getApplicationContext().getPackageName());
            if (residIndep != 0) {
                independentResidenceAdapter = ArrayAdapter.createFromResource(this, residIndep, R.layout.custom_spinner);
            }
            independent_resid_spinner.setAdapter(independentResidenceAdapter);
            mainTendency_spinner.setAdapter(independentResidenceAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String linkNatureLanguage = "nature_of_link_to_residence_" + sessionManager.getAppLanguage();
            int linkNatureStatus = res.getIdentifier(linkNatureLanguage, "array", getApplicationContext().getPackageName());
            if (linkNatureStatus != 0) {
                linkNatureAdapter = ArrayAdapter.createFromResource(this, linkNatureStatus, R.layout.custom_spinner);
            }
            link_nature_spinner.setAdapter(linkNatureAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String husbandStatusLanguage = "husband_status_" + sessionManager.getAppLanguage();
            int husbandStatus = res.getIdentifier(husbandStatusLanguage, "array", getApplicationContext().getPackageName());
            if (husbandStatus != 0) {
                husbandStatusAdapter = ArrayAdapter.createFromResource(this, husbandStatus, R.layout.custom_spinner);
            }
            husband_status_spinner.setAdapter(husbandStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String educationLanguage = "education_" + sessionManager.getAppLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(this, educations, R.layout.custom_spinner);
            }
            mEducation.setAdapter(educationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        try {
            String occupationLanguage = "occupation_identification_" + sessionManager.getAppLanguage();
            int occupations = res.getIdentifier(occupationLanguage, "array", getApplicationContext().getPackageName());
            if (occupations != 0) {
                occupationAdapter = ArrayAdapter.createFromResource(this, occupations, R.layout.custom_spinner);
            }
            mOccupation.setAdapter(occupationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }

        try {
            String whyHOHLanguage = "why_hoh_" + sessionManager.getAppLanguage();
            int whyHOH = res.getIdentifier(whyHOHLanguage, "array", getApplicationContext().getPackageName());
            if (whyHOH != 0) {
                whyHOHAdapter = ArrayAdapter.createFromResource(this, whyHOH, R.layout.custom_spinner);
            }
            whyHOH_spinner.setAdapter(whyHOHAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }

        try {
            String reasonLanguage = "reason_for_change_" + sessionManager.getAppLanguage();
            int reason = res.getIdentifier(reasonLanguage, "array", getApplicationContext().getPackageName());
            if (reason != 0) {
                changeConditionReasonAdapter = ArrayAdapter.createFromResource(this, reason, R.layout.custom_spinner);
            }
            changeConditionReason_spinner.setAdapter(changeConditionReasonAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }

        try {
            String perc_income_Language = "percentage_income_" + sessionManager.getAppLanguage();
            int incomePerc = res.getIdentifier(perc_income_Language, "array", getApplicationContext().getPackageName());
            if (incomePerc != 0) {
                percentageIncomeAdapter = ArrayAdapter.createFromResource(this, incomePerc, R.layout.custom_spinner);
            }
            percentage_income_spinner.setAdapter(percentageIncomeAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }

        // Victim Special Needs Adapter - Start
        try {
            String special_needs_Language = "victim_special_needs_" + sessionManager.getAppLanguage();
            int specialNeeds = res.getIdentifier(special_needs_Language, "array", getApplicationContext().getPackageName());
            if (specialNeeds != 0) {
                specialNeedsAdapter = ArrayAdapter.createFromResource(this, specialNeeds, R.layout.custom_spinner);
            }
            binding.spinnerNatureSpecialNeeds.setAdapter(specialNeedsAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
        // Victim Special Needs Adapter - End

        // Victim Loss Due To Earthquake Adapter - Start
        try {
            String loss_due_to_earthquake_Language = "victim_loss_due_to_earthquake_" + sessionManager.getAppLanguage();
            int earthquakeLoss = res.getIdentifier(loss_due_to_earthquake_Language, "array", getApplicationContext().getPackageName());
            if (earthquakeLoss != 0) {
                lossDueToEarthquakeAdapter = ArrayAdapter.createFromResource(this, earthquakeLoss, R.layout.custom_spinner);
            }
            binding.spinnerLossAsAResultOfTheEarthquake.setAdapter(lossDueToEarthquakeAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
        // Victim Loss Due To Earthquake Adapter - End

        // Victim Loss Of Analgesic Due To Earthquake Adapter - Start
        try {
            String loss_of_analgesic_due_to_earthquake_Language = "victim_loss_of_analgesic_" + sessionManager.getAppLanguage();
            int analgesicLoss = res.getIdentifier(loss_of_analgesic_due_to_earthquake_Language, "array", getApplicationContext().getPackageName());
            if (analgesicLoss != 0) {
                lossOfAnalgesicAdapter = ArrayAdapter.createFromResource(this, analgesicLoss, R.layout.custom_spinner);
            }
            binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.setAdapter(lossOfAnalgesicAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
        // Victim Loss Of Analgesic Due To Earthquake Adapter - Start

        // Victim Loss Of Breadwinner Due To Earthquake Adapter - Start
        try {
            String loss_of_breadwinner_due_to_earthquake_Language = "victim_loss_of_breadwinner_" + sessionManager.getAppLanguage();
            int breadwinnerLoss = res.getIdentifier(loss_of_breadwinner_due_to_earthquake_Language, "array", getApplicationContext().getPackageName());
            if (breadwinnerLoss != 0) {
                lossOfBreadwinnerAdapter = ArrayAdapter.createFromResource(this, breadwinnerLoss, R.layout.custom_spinner);
            }
            binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.setAdapter(lossOfBreadwinnerAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
        // Victim Loss Of Breadwinner Due To Earthquake Adapter - Start

        // Victim Strong Social Ties Adapter - Start
        try {
            String strong_social_ties_Language = "victim_strong_social_ties_" + sessionManager.getAppLanguage();
            int strongSocialTies = res.getIdentifier(strong_social_ties_Language, "array", getApplicationContext().getPackageName());
            if (strongSocialTies != 0) {
                strongSocialTiesAdapter = ArrayAdapter.createFromResource(this, strongSocialTies, R.layout.custom_spinner);
            }
            binding.spinnerStrongSocialTies.setAdapter(strongSocialTiesAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
        // Victim Strong Social Ties Adapter - Start

        // Victim Strong Social Ties Adapter - Start
        try {
            String places_of_preference_Language = "victim_place_of_preference_" + sessionManager.getAppLanguage();
            int placesOfPreference = res.getIdentifier(places_of_preference_Language, "array", getApplicationContext().getPackageName());
            if (placesOfPreference != 0) {
                placesOfPreferenceAdapter = ArrayAdapter.createFromResource(this, placesOfPreference, R.layout.custom_spinner);
            }
            binding.spinnerPlacesOfPreference.setAdapter(placesOfPreferenceAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
        // Victim Strong Social Ties Adapter - Start

      /*  NewLocationDao newLocationDao = new NewLocationDao();
        List<String> villageList = newLocationDao.getVillageList(sessionManager.getStateName(), sessionManager.getDistrictName(), context);
        if (villageList.size() > 1) {
            LocationArrayAdapter locationArrayAdapter =
                    new LocationArrayAdapter(IdentificationActivity.this, villageList);
            mVillage.setAdapter(locationArrayAdapter);
            mVillage.setEnabled(false);
            if (patientID_edit != null) {
                mVillage.setSelection(locationArrayAdapter.getPosition(patient1.getCity_village()));
            } else {
                mVillage.setSelection(locationArrayAdapter.getPosition(sessionManager.getVillageName()));
            }
        }*/

    }

    private void configureViewsFromConfig() {
        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context), String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file
            if (obj.getBoolean("mFirstName")) {
                mFirstName.setVisibility(View.VISIBLE);
            } else {
                mFirstName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mMiddleName")) {
                mMiddleName.setVisibility(View.VISIBLE);
            } else {
                mMiddleName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mLastName")) {
                mLastName.setVisibility(View.VISIBLE);
            } else {
                mLastName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mDOB")) {
                mDOB.setVisibility(View.VISIBLE);
            } else {
                mDOB.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPhoneNum")) {
                mPhoneNum.setVisibility(View.VISIBLE);
            } else {
                mPhoneNum.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAge")) {
                mAge.setVisibility(View.VISIBLE);
            } else {
                mAge.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress1")) {
                mAddress1.setVisibility(View.VISIBLE);
            } else {
                mAddress1.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress2")) {
                mAddress2.setVisibility(View.VISIBLE);
            } else {
                mAddress2.setVisibility(View.GONE);
            }

            if (obj.getBoolean("countryStateLayout")) {
                countryStateLayout.setVisibility(View.VISIBLE);
            } else {
                countryStateLayout.setVisibility(View.GONE);
            }
            /*if (obj.getBoolean("mPostal")) {
                postal_layout.setVisibility(View.VISIBLE);
                mPostal.setVisibility(View.VISIBLE);
            } else {
                postal_layout.setVisibility(View.GONE);
                mPostal.setVisibility(View.GONE);
            }*/

            if (obj.getBoolean("mGenderM")) {
                mGenderM.setVisibility(View.VISIBLE);
            } else {
                mGenderM.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mGenderF")) {
                mGenderF.setVisibility(View.VISIBLE);
            } else {
                mGenderF.setVisibility(View.GONE);
            }

            country1 = obj.getString("mCountry");
            state = sessionManager.getStateName();

            if (country1.equalsIgnoreCase("India")) {
                EditTextUtils.setEditTextMaxLength(10, mPhoneNum);
            } else if (country1.equalsIgnoreCase("Syria")) {
                EditTextUtils.setEditTextMaxLength(9, mPhoneNum);
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }

    }


    public String getYear(int syear, int smonth, int sday, int eyear, int emonth, int eday) {
        String calculatedAge = null;
        int resmonth;
        int resyear;
        int resday;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            LocalDate today = LocalDate.now();
            LocalDate birthday = LocalDate.of(syear, smonth + 1, sday);

            Period p = Period.between(birthday, today);
            System.out.println(p.getDays());
            System.out.println(p.getMonths());
            System.out.println(p.getYears());
            calculatedAge = p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";


        } else {

            //calculating year
            resyear = eyear - syear;

            //calculating month
            if (emonth >= smonth) {
                resmonth = emonth - smonth;
            } else {
                resmonth = emonth - smonth;
                resmonth = 12 + resmonth;
                resyear--;
            }

            //calculating date
            if (eday >= sday) {
                resday = eday - sday;
            } else {
                resday = eday - sday;
                resday = 30 + resday;
                if (resmonth == 0) {
                    resmonth = 11;
                    resyear--;
                } else {
                    resmonth--;
                }
            }

            //displaying error if calculated age is negative
            if (resday < 0 || resmonth < 0 || resyear < 0) {
                Toast.makeText(this, "Current Date must be greater than Date of Birth", Toast.LENGTH_LONG).show();
                mDOB.setError(getString(R.string.identification_screen_error_dob));
                mAge.setError(getString(R.string.identification_screen_error_age));
            } else {
                // t1.setText("Age: " + resyear + " years /" + resmonth + " months/" + resday + " days");

                calculatedAge = resyear + " years - " + resmonth + " months - " + resday + " days";
            }
        }

        return calculatedAge != null ? calculatedAge : " ";
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.identification_gender_male:
                if (checked) mGender = "M";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.identification_gender_female:
                if (checked) mGender = "F";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.identification_gender_others:
                if (checked) mGender = "O";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.hoh_yes:
                if (checked) headOfHousehold = "Yes";
                break;
            case R.id.hoh_no:
                if (checked) headOfHousehold = "No";
                break;
        }

//        updateRoaster();
    }

    private InputFilter inputFilter_Name = new InputFilter() { //filter input for name fields
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null && BlockCharacterSet_Name.contains(("" + charSequence))) {
                return "";
            }
            return null;
        }
    };

    private InputFilter inputFilter_Others = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null && BlockCharacterSet_Others.contains(("" + charSequence))) {
                return "";
            }
            return null;
        }
    };

    public void generateUuid() {
        patientUuid = uuidGenerator.UuidGenerator();
    }

    // This method is for setting the screen with existing values in database whenn user clicks edit details
    private void setscreen(String str) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection = "uuid=?";
        String[] patientArgs = {str};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo", "economic_status", "education_status", "caste"};
        Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient1.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patient1.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient1.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient1.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient1.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient1.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient1.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient1.setCity_village(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient1.setState_province(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient1.setPostal_code(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient1.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient1.setPhone_number(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient1.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient1.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient1.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));

            } while (idCursor.moveToNext());
            idCursor.close();
        }
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {str};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient1.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone number")) {
                    patient1.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient1.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient1.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Employment status")) {
                    patient1.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("nature_of_the_residential_complex")) {
                    patient1.setResidentialComplexNature(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("link to the place of residence")) {
                    patient1.setLinkNature(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("family situation")) {
                    patient1.setMaritalstatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("The family relationship")) {
                    patient1.setRelationshiphoh(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("status of husband")) {
                    patient1.setHusbandStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("responsible for family")) {
                    patient1.setHeadOfHousehold(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("patient aid type")) {
                    patient1.setPatientAidType(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("percentage of income")) {
                    patient1.setPercentageOfIncome(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("number of years for support")) {
                    patient1.setSinceWhenMainResponsible(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Do you have an independent residence")) {
                    patient1.setIndependentResidence(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("main official for family's support")) {
                    patient1.setMainOfficial(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("main tendency for breadwinner")) {
                    patient1.setMainTendency(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("condition of the support")) {
                    patient1.setConditionOfSupport(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("change happened years")) {
                    patient1.setSinceChangeHappened(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("CREATED_BY")) {
                    patient1.setHealthWorkerID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("IS_DISASTER_VICTIM")) {
                    String isDisasterVictim = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    earthquakeVictimCheckbox.setChecked(isDisasterVictim.equalsIgnoreCase("yes"));
                }
                if (name.equalsIgnoreCase("NATURE_OF_SPECIAL_NEEDS")) {
                    patient1.setNatureOfSpecialNeeds(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("LOSS_AS_A_RESULT_OF_THE_EARTHQUAKE")) {
                    patient1.setLossAsAResultOfTheEarthquake(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("LOSS_OF_ANALGESIC_AS_A_RESULT_OF_THE_EARTHQUAKE")) {
                    patient1.setLossOfAnalgesicAsAResultOfTheEarthquake(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("LOSS_OF_BREADWINNER_AS_RESULT_OF_EARTHQUAKE")) {
                    patient1.setLossOfBreadwinnerAsAResultOfTheEarthquake(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("STRONG_SOCIAL_TIES_IN_YOUR_RESIDENCE_AREA")) {
                    patient1.setStrongSocialTiesInYourResidenceArea(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PLACES_OF_RELATIVES_IN_OTHER_PARTS_OF_SYRIA")) {
                    patient1.setPlacesOfRelativesInOtherPartsOfSyria(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PLACES_OF_PREFERENCE_FOR_HOSTING")) {
                    patient1.setPlacesOfPreferenceForHosting(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                /*if (name.equalsIgnoreCase("FirstLocation")) {
                    patient1.setFirstlocation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ReferredTo")) {
                    patient1.setReferredto(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ModeofTransport")) {
                    patient1.setModetransport(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("CostofTravel")) {
                    patient1.setCosttravel(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("CostofConsult")) {
                    patient1.setCostconsult(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("CostofMedicines")) {
                    patient1.setCostmedicines(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ScoreofExperience")) {
                    patient1.setScoreexperience(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("NoOfTimesPregnant")) {
                    patient1.setTimespregnant(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("No_Pregnancy_Outcome_2years")) {
                    patient1.setNoOfPregnancyOutcomeTwoYrs(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PregnanyPastTwoYears")) {
                    patient1.setPasttwoyrs(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ChildAlive")) {
                    patient1.setChildalive(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("YearOfPregnant")) {
                    patient1.setYearsofpregnancy(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("MonthPregnantLast")) {
                    patient1.setLastmonthspregnancy(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("NoOfMonthsPregnant")) {
                    patient1.setMonthsofpregnancy(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("FocalFacility")) {
                    patient1.setFocalfacility(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("SingleMultipleBirth")) {
                    patient1.setSinglemultiplebirth(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("SexOfBaby")) {
                    patient1.setSexofbaby(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("BabyAgeDied")) {
                    patient1.setAgediedbaby(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PlaceOfDelivery")) {
                    patient1.setPlacedelivery(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PregnancyPlanned")) {
                    patient1.setPlannedpregnancy(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("HighRiskPregnancy")) {
                    patient1.setHighriskpregnancy(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Complications")) {
                    patient1.setComplications(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("HealthIssueReported")) {
                    String value = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    healthIssuesList = new Gson().fromJson(value, new TypeToken<List<HealthIssues>>() {
                    }.getType());
                    adapter = new HouseholdSurveyAdapter(healthIssuesList, this, sessionManager.getAppLanguage(), this);
                    binding.mainViewPager.setAdapter(adapter);
                    binding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    setViewPagerOffset(binding.mainViewPager);
                }
                if (name.equalsIgnoreCase("PregnancyOutcomesReported")) {
                    String value = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    pregnancyOutcomesList = new Gson().fromJson(value, new TypeToken<List<PregnancyRosterData>>() {
                    }.getType());
                    pregnancyOutcomeAdapter = new PregnancyOutcomeAdapter(pregnancyOutcomesList, this, sessionManager.getAppLanguage());
                    binding.poViewPager.setAdapter(pregnancyOutcomeAdapter);
                    binding.poViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    setViewPagerOffset(binding.poViewPager);
                } */
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }


    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.are_you_want_go_back);
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent i_back = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i_back);
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    public void showAlertDialogButtonClicked(String errorMessage) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Config Error");
        alertDialogBuilder.setMessage(errorMessage);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent i = new Intent(IdentificationActivity.this, SetupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// This flag ensures all activities on top of the CloseAllViewsDemo are cleared.
                startActivity(i);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result Received");
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            Log.v(TAG, "Request Code " + CameraActivity.TAKE_IMAGE);
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                Log.v("IdentificationActivity", mCurrentPhotoPath);

                Glide.with(this).load(new File(mCurrentPhotoPath)).thumbnail(0.25f).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mImageView);
            }
        }
    }

    public void onPatientCreateClicked() {
        uuid = UUID.randomUUID().toString();
        patientdto.setUuid(uuid);

        final boolean[] cancel = {false};
        View focusView = null;


        if (dob.equals("") || dob.toString().equals("")) {
            if (dob.after(today)) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
                //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                mDOBPicker.show();
                alertDialog.show();

                Button postiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                postiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                // postiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
                return;
            }
        }

        if (mPhoneNum.getText().toString().trim().length() > 0) {
            if (mPhoneNum.getText().toString().trim().length() < 9) {
                mPhoneNum.requestFocus();
                mPhoneNum.setError(getString(R.string.enter_9_digits));
                return;
            }
        }

   /*     ArrayList<EditText> values = new ArrayList<>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
        values.add(mDOB);
        values.add(mPhoneNum);
        values.add(mAddress1);
        values.add(mAddress2);
        values.add(mCity);
        values.add(mPostal);
        values.add(mRelationship);
        values.add(mOccupation);*/

        // This additional validation is causing gender to be checked first. Commented as advised by Programs Team - by Arpan Sircar

//        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
//            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//            alertDialogBuilder.setTitle(R.string.error);
//            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
//            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();
//
//            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//            positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//
//            return;
//        }


        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("") && !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mPhoneNum.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked() || mGenderO.isChecked()) && (yesHOH.isChecked() || noHOH.isChecked()) && (studentCB.isChecked() || emergencyCB.isChecked() || generalCB.isChecked() || fhhSurveyCB.isChecked())) {

            Log.v(TAG, "Result");

        } else {
            if (mFirstName.getText().toString().equals("")) {
                focusView = mFirstName;
                mFirstName.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (mLastName.getText().toString().equals("")) {
                if (focusView == null) focusView = mLastName;
                mLastName.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (mDOB.getText().toString().equals("")) {
                if (focusView == null) focusView = mDOB;
                mDOB.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (mAge.getText().toString().equals("")) {
                if (focusView == null) focusView = mAge;
                mAge.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (!phone_checkbox.isChecked() && mPhoneNum.getText().toString().equalsIgnoreCase("")) {
                if (focusView == null) focusView = mPhoneNum;
                mPhoneNum.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            } else {
                mPhoneNum.setError(null);
            }

            if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
                View finalFocusView = focusView;
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finalFocusView == null) {
                            radioGrp.getParent().requestChildFocus(radioGrp, radioGrp);
                        }
                        cancel[0] = true;
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            }

/*
            if (!yesHOH.isChecked() && !noHOH.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_required_fields);
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

            }
*/

/*
            if (!generalCB.isChecked() && !studentCB.isChecked() && !emergencyCB.isChecked() && !fhhSurveyCB.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_required_fields);
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

            }
*/
        }


        if (mAddress1.getText().toString().isEmpty() || mAddress1.getText().toString().equalsIgnoreCase("")) {
            mAddress1.setError(getString(R.string.error_field_required));
            if (focusView == null) focusView = mAddress1;
            cancel[0] = true;
        } else {
            mAddress1.setError(null);
        }

        // Check for validations on Earthquake Victim questions only if the Earthquake Victim Checkbox is checked - Added by Arpan Sircar
        if (earthquakeVictimCheckbox.isChecked()) {

            // Validation for Special Needs Spinner
            if (binding.spinnerNatureSpecialNeeds.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerNatureSpecialNeeds.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerNatureSpecialNeeds;
                cancel[0] = true;
            }

            // Validation for Loss As a Result Of Earthquake Spinner
            if (binding.spinnerLossAsAResultOfTheEarthquake.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerLossAsAResultOfTheEarthquake.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerLossAsAResultOfTheEarthquake;
                cancel[0] = true;
            }

            // Validation for Loss of Analgesic Spinner
            if (binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null)
                    focusView = binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake;
                cancel[0] = true;
            }

            // Validation for Loss of Breadwinner
            if (binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null)
                    focusView = binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake;
                cancel[0] = true;
            }

            // Validation for Strong Social Ties Spinner
            if (binding.spinnerStrongSocialTies.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerStrongSocialTies.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerStrongSocialTies;
                cancel[0] = true;
            }

            // Validation for Places Of Relatives EditText
            if ((binding.etPlacesOfRelatives.getText().toString().isEmpty() || binding.etPlacesOfRelatives.getText().toString().equalsIgnoreCase(""))) {
                binding.etPlacesOfRelatives.setError(getString(R.string.places_of_relatives_cannot_be_empty));
                if (focusView == null) focusView = binding.etPlacesOfRelatives;
                cancel[0] = true;
            }

            // Validation for Place Of Preference Spinner
            if (binding.spinnerPlacesOfPreference.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerPlacesOfPreference.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerPlacesOfPreference;
                cancel[0] = true;
            }
        }

        /*if (sinceSupportingFamilyET.getVisibility() == View.VISIBLE && (sinceSupportingFamilyET.getText().toString().isEmpty() || sinceSupportingFamilyET.getText().toString().equalsIgnoreCase(""))) {
            sinceSupportingFamilyET.setError(getString(R.string.error_field_required));
            focusView = sinceSupportingFamilyET;
            cancel = true;
            return;
        } else {
            sinceSupportingFamilyET.setError(null);
        }

        if (sinceChangeHappenedET.getVisibility() == View.VISIBLE && (sinceChangeHappenedET.getText().toString().isEmpty() || sinceChangeHappenedET.getText().toString().equalsIgnoreCase(""))) {
            sinceChangeHappenedET.setError(getString(R.string.error_field_required));
            focusView = sinceChangeHappenedET;
            cancel = true;
            return;
        } else {
            sinceChangeHappenedET.setError(null);
        }

        if (mCountry.getSelectedItemPosition() == 0) {
            countryText.setError(getString(R.string.error_field_required));
            focusView = countryText;
            cancel = true;
            return;
        } else {
            countryText.setError(null);
        }

        if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
            focusView = stateText;
            cancel = true;
            return;
        } else {
            stateText.setError(null);
        }

        //Roster Insert Validations - Start
        if (HOH_relation_spinner.getVisibility() == View.VISIBLE && (HOH_relation_spinner.getSelectedItemPosition() == 0)) {
            TextView t = (TextView) HOH_relation_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = HOH_relation_spinner;
            cancel = true;
            return;
        }


        if (marital_status_spinner.getVisibility() == View.VISIBLE && (marital_status_spinner.getSelectedItemPosition() == 0)) {
            TextView t = (TextView) marital_status_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = marital_status_spinner;
            cancel = true;
            return;
        }

        if (mEducation.getVisibility() == View.VISIBLE && (mEducation.getSelectedItemPosition() == 0)) {
            TextView t = (TextView) mEducation.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = mEducation;
            cancel = true;
            return;
        }

        if (mOccupation.getVisibility() == View.VISIBLE && (mOccupation.getSelectedItemPosition() == 0)) {
            TextView t = (TextView) mOccupation.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = mOccupation;
            cancel = true;
            return;
        }*/

           /*Other
        if (til_whatisyourrelation_other.getVisibility() == View.VISIBLE) {
            if (et_whatisyourrelation_other.getText().toString().equals("")) {
                et_whatisyourrelation_other.setError(getString(R.string.error_field_required));
                focusView = et_whatisyourrelation_other;
                cancel = true;
                return;
            }
        }*/


        /*Other
        if (til_occupation_other.getVisibility() == View.VISIBLE) {
            if (et_occupation_other.getText().toString().equals("")) {
                et_occupation_other.setError(getString(R.string.error_field_required));
                focusView = et_occupation_other;
                cancel = true;
                return;
            }
        }

        if (((String) mOccupation.getSelectedItem()).equals(getString(R.string.other_please_specify))) {
            String occupation = Objects.requireNonNull(til_occupation_other.getEditText()).getText().toString();
            if (occupation.equals("")) {
                til_occupation_other.getEditText().setError(getString(R.string.error_field_required));
                focusView = til_occupation_other.getEditText();
                cancel = true;
                return;
            }
        } */

        /*if (spinner_phoneownership.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_phoneownership.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_phoneownership;
            cancel = true;
            return;
        }

        if (ll18.getVisibility() == View.VISIBLE) {
            if (spinner_bpchecked.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_bpchecked.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_bpchecked;
                cancel = true;
                return;
            }

            if (spinner_sugarchecked.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_sugarchecked.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_sugarchecked;
                cancel = true;
                return;
            }
        }

        if (spinner_hbchecked.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_hbchecked.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_hbchecked;
            cancel = true;
            return;
        }

        if (spinner_bmi.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_bmi.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_bmi;
            cancel = true;
            return;
        }

        if (llPORoaster.getVisibility() == View.VISIBLE) {
            if (edittext_howmanytimmespregnant.getText().toString().equalsIgnoreCase("") &&
                    edittext_howmanytimmespregnant.getText().toString().isEmpty()) {
                edittext_howmanytimmespregnant.setError(getString(R.string.select));
                focusView = edittext_howmanytimmespregnant;
                cancel = true;
                return;
            }

            if (spinner_pregnantpasttwoyrs.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_pregnantpasttwoyrs.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_pregnantpasttwoyrs;
                cancel = true;
                return;
            }

            if (spinner_pregnantpasttwoyrs.getSelectedItemPosition() == 1) {
                if (binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equalsIgnoreCase("") &&
                        binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty()) {
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.setError(getString(R.string.select));
                    focusView = binding.edittextNoOfPregnancyOutcomePastTwoYrs;
                    cancel = true;
                    return;
                }

                if (!binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equalsIgnoreCase("") &&
                        !binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty() &&
                        Integer.parseInt(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString()) > 0 &&
                        pregnancyOutcomesList.size() == 0) {
                    Toast.makeText(this, R.string.please_add_pregnancy_outcome, Toast.LENGTH_SHORT).show();
                    focusView = binding.edittextNoOfPregnancyOutcomePastTwoYrs;
                    cancel = true;
                    return;
                }

                if (pregnancyOutcomesList.size() != Integer.parseInt(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString())) {
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.setError(getString(R.string.select));
                    focusView = binding.edittextNoOfPregnancyOutcomePastTwoYrs;
                    cancel = true;
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.setFocusable(true);
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.requestFocus();
                    return;
                }
            }
        } */

//                if (spinner_outcomepregnancy.getSelectedItemPosition() == 1) {
//                    if (spinner_childalive.getSelectedItemPosition() == 0) {
//                        TextView t = (TextView) spinner_childalive.getSelectedView();
//                        t.setError(getString(R.string.select));
//                        t.setTextColor(Color.RED);
//                        focusView = spinner_childalive;
//                        cancel = true;
//                        return;
//                    }
//                }
//
//                if (edittext_yearofpregnancy.getText().toString().equalsIgnoreCase("") &&
//                        edittext_yearofpregnancy.getText().toString().isEmpty()) {
//                    edittext_yearofpregnancy.setError(getString(R.string.select));
//                    focusView = edittext_yearofpregnancy;
//                    cancel = true;
//                    return;
//                }
//
//                if (spinner_outcomepregnancy.getSelectedItemPosition() == 5) {
//                    if (edittext_monthsbeingpregnant.getText().toString().equalsIgnoreCase("") ||
//                            edittext_monthsbeingpregnant.getText().toString().isEmpty()) {
//                        edittext_monthsbeingpregnant.setError(getString(R.string.error_field_required));
//                    } else {
//                        edittext_monthsbeingpregnant.setError(null);
//                    }
//                }
//
//                if (spinner_outcomepregnancy.getSelectedItemPosition() != 0 && spinner_outcomepregnancy.getSelectedItemPosition() != 5) {
//                    if (edittext_monthspregnancylast.getVisibility() == View.VISIBLE &&
//                            edittext_monthspregnancylast.getText().toString().equalsIgnoreCase("") &&
//                            edittext_monthspregnancylast.getText().toString().isEmpty()) {
//                        edittext_monthspregnancylast.setError(getString(R.string.select));
//                        focusView = edittext_monthspregnancylast;
//                        cancel = true;
//                        return;
//                    }
//                }
//
//                if (spinner_outcomepregnancy.getSelectedItemPosition() != 4 && spinner_outcomepregnancy.getSelectedItemPosition() != 5) {
//                    if (spinner_placeofdeliverypregnant.getSelectedItemPosition() == 0) {
//                        TextView t = (TextView) spinner_placeofdeliverypregnant.getSelectedView();
//                        t.setError(getString(R.string.select));
//                        t.setTextColor(Color.RED);
//                        focusView = spinner_placeofdeliverypregnant;
//                        cancel = true;
//                        return;
//                    }
//                }
//
//                if (spinner_outcomepregnancy.getSelectedItemPosition() != 3 && spinner_outcomepregnancy.getSelectedItemPosition() != 4 &&
//                        spinner_outcomepregnancy.getSelectedItemPosition() != 5) {
//                    if (spinner_singlemultiplebirths.getSelectedItemPosition() == 0) {
//                        TextView t = (TextView) spinner_singlemultiplebirths.getSelectedView();
//                        t.setError(getString(R.string.select));
//                        t.setTextColor(Color.RED);
//                        focusView = spinner_singlemultiplebirths;
//                        cancel = true;
//                        return;
//                    }
//                }
//
//                if (spinner_outcomepregnancy.getSelectedItemPosition() == 1 && spinner_outcomepregnancy.getSelectedItemPosition() == 2) {
//                    if (spinner_sexofbaby.getSelectedItemPosition() == 0) {
//                        TextView t = (TextView) spinner_sexofbaby.getSelectedView();
//                        t.setError(getString(R.string.select));
//                        t.setTextColor(Color.RED);
//                        focusView = spinner_sexofbaby;
//                        cancel = true;
//                        return;
//                    }
//
//                    if (spinner_pregnancycomplications.getSelectedItemPosition() == 0) {
//                        TextView t = (TextView) spinner_pregnancycomplications.getSelectedView();
//                        t.setError(getString(R.string.select));
//                        t.setTextColor(Color.RED);
//                        focusView = spinner_pregnancycomplications;
//                        cancel = true;
//                        return;
//                    }
//                }
//
//                if (spinner_pregnancyplanned.getSelectedItemPosition() == 0) {
//                    TextView t = (TextView) spinner_pregnancyplanned.getSelectedView();
//                    t.setError(getString(R.string.select));
//                    t.setTextColor(Color.RED);
//                    focusView = spinner_pregnancyplanned;
//                    cancel = true;
//                    return;
//                }
//
//                if (spinner_pregnancyhighriskcase.getSelectedItemPosition() == 0) {
//                    TextView t = (TextView) spinner_pregnancyhighriskcase.getSelectedView();
//                    t.setError(getString(R.string.select));
//                    t.setTextColor(Color.RED);
//                    focusView = spinner_pregnancyhighriskcase;
//                    cancel = true;
//                    return;
//                }
//            }
//        }

        // Roster Insert Validations - End

        if (cancel[0]) {
            focusView.getParent().requestChildFocus(focusView, focusView);
            Toast.makeText(this, getString(R.string.identification_fill_required_fields), Toast.LENGTH_SHORT).show();
        } else {
            displayPinDialog();
        }
    }

    private void insertPatient(String patientPin) {
        Gson gson = new Gson();
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        patientdto.setFirstname(StringUtils.getValue(mFirstName.getText().toString()));
        patientdto.setMiddlename(StringUtils.getValue(mMiddleName.getText().toString()));
        patientdto.setLastname(StringUtils.getValue(mLastName.getText().toString()));
        patientdto.setPhonenumber(StringUtils.getValue(mPhoneNum.getText().toString()));
        patientdto.setGender(StringUtils.getValue(mGender));

        String[] dob_array = mDOB.getText().toString().split(" ");
        Log.d("dob_array", "0: " + dob_array[0]);
        Log.d("dob_array", "0: " + dob_array[1]);
        Log.d("dob_array", "0: " + dob_array[2]);

        //get month index and return English value for month.
        if (dob_indexValue == 15) {
            String dob = StringUtils.hi_or_bn_en_noEdit(mDOB.getText().toString(), sessionManager.getAppLanguage());
            patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob)));
        } else {
            String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
            dob_array[1] = dob_array[1].replace(dob_array[1], dob);
            String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];
            patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob_value)));
        }

        patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
        patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));

        String value = switch_ar_to_en_village(mVillage.getSelectedItem().toString());
        patientdto.setCityvillage(value);

        patientdto.setPostalcode(StringUtils.getValue(mPostal.getText().toString()));
        patientdto.setCountry("Syria"); //hardcoding this as this field is important to send in the db but partner asked to remove this field from patient registration.
        patientdto.setPatientPhoto(mCurrentPhotoPath);

        String stateName = mState.getSelectedItem().toString();
        patientdto.setStateprovince(switch_ar_to_en_state(stateName));

        //  patientdto.setStateprovince(mState.getSelectedItem().toString());

          /*  patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("patient aid type"));
            String aidTypeInJson = getAidTypeInJson();
            patientAttributesDTO.setValue(aidTypeInJson);
            patientAttributesDTOList.add(patientAttributesDTO);*/

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("responsible for family"));
        String hohInfoValue = headOfHousehold;
        String hohInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), hohInfoValue, R.array.independent_residence_en, R.array.independent_residence_ar);
        patientAttributesDTO.setValue(hohInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("main official for family's support"));
        String whyHOHInfoValue = StringUtils.getProvided(whyHOH_spinner);
        String whyHOHInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), whyHOHInfoValue, R.array.why_hoh_en, R.array.why_hoh_ar);
        patientAttributesDTO.setValue(whyHOHInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        // Commented by Arpan Sircar - these two fields are not visible in the activity - keeping them here is preventing sync from happening as empty values are being passed.

//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("change happened years"));
//            patientAttributesDTO.setValue(sinceChangeHappenedET.getText().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);
//
//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("number of years for support"));
//            patientAttributesDTO.setValue(sinceSupportingFamilyET.getText().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("main tendency for breadwinner"));
        String mainTendInfoValue = StringUtils.getProvided(mainTendency_spinner);
        String mainTendInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), mainTendInfoValue, R.array.independent_residence_en, R.array.independent_residence_ar);
        patientAttributesDTO.setValue(mainTendInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("condition of the support"));
        String conditionSupportInfoValue = StringUtils.getProvided(changeConditionReason_spinner);
        String conditionSupportInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), conditionSupportInfoValue, R.array.reason_for_change_en, R.array.reason_for_change_ar);
        patientAttributesDTO.setValue(conditionSupportInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("percentage of income"));
        String percIncomeInfoValue = StringUtils.getProvided(percentage_income_spinner);
        String percIncomeInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), percIncomeInfoValue, R.array.percentage_income_en, R.array.percentage_income_ar);
        patientAttributesDTO.setValue(percIncomeInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("status of husband"));
        String husbandStatusInfoValue = StringUtils.getProvided(husband_status_spinner);
        String husbandStatusInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), husbandStatusInfoValue, R.array.husband_status_en, R.array.husband_status_ar);
        patientAttributesDTO.setValue(husbandStatusInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Do you have an independent residence"));
        String indepResidInfoValue = StringUtils.getProvided(independent_resid_spinner);
        String indepResidInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), indepResidInfoValue, R.array.independent_residence_en, R.array.independent_residence_ar);
        patientAttributesDTO.setValue(indepResidInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone number"));
        patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nature_of_the_residential_complex"));
        String residenceNatureInfoValue = StringUtils.getProvided(residence_nature_spinner);
        String residenceNatureInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), residenceNatureInfoValue, R.array.nature_residential_complex_en, R.array.nature_residential_complex_ar);
        patientAttributesDTO.setValue(residenceNatureInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Employment status"));
        String occupationInfoValue = StringUtils.getProvided(mOccupation);
        String occupationInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), occupationInfoValue, R.array.occupation_identification_en, R.array.occupation_identification_ar);
        patientAttributesDTO.setValue(occupationInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("link to the place of residence"));
        String linkNatureInfoValue = StringUtils.getProvided(link_nature_spinner);
        String linkNatureInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), linkNatureInfoValue, R.array.nature_of_link_to_residence_en, R.array.nature_of_link_to_residence_ar);
        patientAttributesDTO.setValue(linkNatureInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        // relationsip hoh
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("The family relationship"));
        String relationHOHInfoValue = StringUtils.getProvided(HOH_relation_spinner);
        String relationHOHInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), relationHOHInfoValue, R.array.relationshipHoH_en, R.array.relationshipHoH_ar);
        patientAttributesDTO.setValue(relationHOHInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        // marital
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("family situation"));
        String maritalStatusInfoValue = StringUtils.getProvided(marital_status_spinner);
        String maritalStatusInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), maritalStatusInfoValue, R.array.marital_en, R.array.marital_ar);
        patientAttributesDTO.setValue(maritalStatusInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
        String educationInfoValue = StringUtils.getProvided(mEducation);
        String educationInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), educationInfoValue, R.array.education_en, R.array.education_ar);
        patientAttributesDTO.setValue(educationInfoJson);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
        patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PATIENT_PIN"));
        patientAttributesDTO.setValue(patientPin);
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("CREATED_BY"));
        patientAttributesDTO.setValue(sessionManager.getProviderID());
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("IS_DISASTER_VICTIM"));

        if (earthquakeVictimCheckbox.isChecked()) {
            patientAttributesDTO.setValue("Yes");
        } else {
            patientAttributesDTO.setValue("No");
        }

        patientAttributesDTOList.add(patientAttributesDTO);

        //House Hold Registration
        if (sessionManager.getHouseholdUuid().equals("")) {
            String HouseHold_UUID = UUID.randomUUID().toString();
            sessionManager.setHouseholdUuid(HouseHold_UUID);
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Household ID Number"));
            patientAttributesDTO.setValue(HouseHold_UUID);

        } else {
            String HouseHold_UUID = sessionManager.getHouseholdUuid();
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Household ID Number"));
            patientAttributesDTO.setValue(HouseHold_UUID);
        }
        //House Hold Registration - End

        // If the earthquake checkbox is checked, we will show the earthquake questions. And only then we will extract the data.
        if (earthquakeVictimCheckbox.isChecked()) {

            // Nature of special needs spinner
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NATURE_OF_SPECIAL_NEEDS"));
            String specialNeedsInfoValue = StringUtils.getProvided(binding.spinnerNatureSpecialNeeds);
            String specialNeedsInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), specialNeedsInfoValue, R.array.victim_special_needs_en, R.array.victim_special_needs_ar);
            patientAttributesDTO.setValue(specialNeedsInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Loss as a result of the earthquake
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("LOSS_AS_A_RESULT_OF_THE_EARTHQUAKE"));
            String lossInfoValue = StringUtils.getProvided(binding.spinnerLossAsAResultOfTheEarthquake);
            String lossInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), lossInfoValue, R.array.victim_loss_due_to_earthquake_en, R.array.victim_loss_due_to_earthquake_ar);
            patientAttributesDTO.setValue(lossInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Loss of analgesic as a result of the earthquake
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("LOSS_OF_ANALGESIC_AS_A_RESULT_OF_THE_EARTHQUAKE"));
            String lossOfAnalgesicInfoValue = StringUtils.getProvided(binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake);
            String lossOfAnalgesicInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), lossOfAnalgesicInfoValue, R.array.victim_loss_of_analgesic_en, R.array.victim_loss_of_analgesic_ar);
            patientAttributesDTO.setValue(lossOfAnalgesicInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Loss of breadwinner as a result of the earthquake
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("LOSS_OF_BREADWINNER_AS_RESULT_OF_EARTHQUAKE"));
            String lossOfBreadwinnerInfoValue = StringUtils.getProvided(binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake);
            String lossOfBreadwinnerInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), lossOfBreadwinnerInfoValue, R.array.victim_loss_of_breadwinner_en, R.array.victim_loss_of_breadwinner_ar);
            patientAttributesDTO.setValue(lossOfBreadwinnerInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Strong social ties spinner
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("STRONG_SOCIAL_TIES_IN_YOUR_RESIDENCE_AREA"));
            String strongSocialTiesInfoValue = StringUtils.getProvided(binding.spinnerStrongSocialTies);
            String strongSocialTiesInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), strongSocialTiesInfoValue, R.array.victim_strong_social_ties_en, R.array.victim_strong_social_ties_ar);
            patientAttributesDTO.setValue(strongSocialTiesInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Place of relatives
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PLACES_OF_RELATIVES_IN_OTHER_PARTS_OF_SYRIA"));
            String placesOfRelativesInfoValue = binding.etPlacesOfRelatives.getText().toString();
            patientAttributesDTO.setValue(placesOfRelativesInfoValue);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Place of hosting spinner
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PLACES_OF_PREFERENCE_FOR_HOSTING"));
            String placeOfHostingInfoValue = StringUtils.getProvided(binding.spinnerPlacesOfPreference);
            String placeOfHostingInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), placeOfHostingInfoValue, R.array.victim_place_of_preference_en, R.array.victim_place_of_preference_ar);
            patientAttributesDTO.setValue(placeOfHostingInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        patientAttributesDTOList.add(patientAttributesDTO);

        Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
        patientdto.setPatientAttributesDTOList(patientAttributesDTOList);
        patientdto.setSyncd(false);
        Logger.logD("patient json : ", "Json : " + gson.toJson(patientdto, PatientDTO.class));

        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean isPatientInserted = patientsDAO.insertPatientToDB(patientdto, uuid);
            boolean isPatientImageInserted = imagesDAO.insertPatientProfileImages(mCurrentPhotoPath, uuid);

            if (NetworkConnection.isOnline(getApplication())) {
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi();
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();
            }
            if (isPatientInserted && isPatientImageInserted) {
                Logger.logD(TAG, "inserted");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirstname() + " " + patientdto.getLastname());
                i.putExtra("tag", "newPatient");
                i.putExtra("privacy", privacy_value);
                i.putExtra("hasPrescription", "false");
                Log.d(TAG, "Privacy Value on (Identification): " + privacy_value); //privacy value transferred to PatientDetail activity.
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            } else {
                Toast.makeText(IdentificationActivity.this, "Error of adding the data", Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /*private String getAidTypeInJson() {
        if (selectedAid_en != null && selectedAid_ar != null) {
            Gson gson = new Gson();
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("ar", selectedAid_ar.toString());
            resultMap.put("en", selectedAid_en.toString());
            return gson.toJson(resultMap);
        }
        return "Not Provided";
    }*/

    private String getAidTypeInJson() {
        if (selectedAid_en != null && selectedAid_ar != null) {
            Gson gson = new Gson();
            AidTypeAnswerValue answerValue = new AidTypeAnswerValue();
            answerValue.setEnValues(selectedAid_en);
            answerValue.setArValues(selectedAid_ar);
            return gson.toJson(answerValue);
        }
        return "Not Provided";
    }

    /*private void insertedit_RosterValuesIntoLocalDB(PatientAttributesDTO patientAttributesDTO,
                                                    @NonNull List<PatientAttributesDTO> patientAttributesDTOList) {

        // block
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("blockSurvey"));
        if (spinner_block.getSelectedItemPosition() == 3) {
            patientAttributesDTO.setValue(StringUtils.getValue(et_block_other.getText().toString()));
        } else {
            patientAttributesDTO.setValue(StringUtils.getPethBlock(spinner_block.getSelectedItem().toString(), sessionManager.getAppLanguage()));
        }
        patientAttributesDTOList.add(patientAttributesDTO);

        // village
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("villageNameSurvey"));
        if (spinner_block.getSelectedItemPosition() == 3) {
            patientAttributesDTO.setValue(StringUtils.getValue(et_village_other.getText().toString()));
        } else {
            patientAttributesDTO.setValue(StringUtils.getPethBlockVillage(spinner_village.getSelectedItem().toString(), sessionManager.getAppLanguage()));
        }
        patientAttributesDTOList.add(patientAttributesDTO);*/

        /*  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

         phone owner
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PhoneOwnership"));
        patientAttributesDTO.setValue(StringUtils.getPhoneOwnerShip(spinner_phoneownership.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//        patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(spinner_phoneownership));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        if (ll18.getVisibility() == View.VISIBLE) {
            // bp checked
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("BPchecked"));
            patientAttributesDTO.setValue(StringUtils.getBP(spinner_bpchecked.getSelectedItem().toString(), sessionManager.getAppLanguage()));
            Log.d("HOH", "2602: " + spinner_bpchecked.getSelectedItem());
            patientAttributesDTOList.add(patientAttributesDTO);

            // sugar checked
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Sugarchecked"));
            patientAttributesDTO.setValue(StringUtils.getSuger(spinner_sugarchecked.getSelectedItem().toString(), sessionManager.getAppLanguage()));
            //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        // hb test
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("HBtest"));
        patientAttributesDTO.setValue(StringUtils.getHB(spinner_hbchecked.getSelectedItem().toString(), sessionManager.getAppLanguage()));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        // bmi
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("BMI"));
        patientAttributesDTO.setValue(StringUtils.getBMI(spinner_bmi.getSelectedItem().toString(), sessionManager.getAppLanguage()));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        // health issue reported
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("HealthIssueReported"));
        String value = new Gson().toJson(healthIssuesList);
        patientAttributesDTO.setValue(value);
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        // pregnancy issue reported
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PregnancyOutcomesReported"));
        String pregnancyValue = new Gson().toJson(pregnancyOutcomesList);
        patientAttributesDTO.setValue(pregnancyValue);
//        Log.d(TAG, "insertedit_RosterValuesIntoLocalDB: " + pregnancyValue);
        patientAttributesDTOList.add(patientAttributesDTO);

//        //no episodes
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NoofEpisodes"));
//        patientAttributesDTO.setValue(StringUtils.getValue(edittext_noofepisodes.getText().toString()));
//        Log.d("HOH", "total family meme: " + edittext_noofepisodes.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//        //no episodes
//
//        // primary health provider
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PrimaryHealthProvider"));
//        patientAttributesDTO.setValue(StringUtils.getPrimeryHealthProvider(spinner_primaryhealthprovider.getSelectedItem().toString(),sessionManager.getAppLanguage()));
//        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//
//        // first location
     /*   patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("FirstLocation"));
        patientAttributesDTO.setValue(StringUtils.getFirstLocation(spinner_firstlocation.getSelectedItem().toString(),sessionManager.getAppLanguage()));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);
//*/
//        // referred to
      /*  patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ReferredTo"));
        patientAttributesDTO.setValue(StringUtils.getReferedDTO(spinner_referredto.getSelectedItem().toString(),sessionManager.getAppLanguage()));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);*/
//
//        // mode of transport
    /*    patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ModeofTransport"));
        patientAttributesDTO.setValue(StringUtils.getModerateSport(spinner_modeoftransport.getSelectedItem().toString(),sessionManager.getAppLanguage()));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);*/
//
//        //cost travel
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("CostofTravel"));
//        patientAttributesDTO.setValue(StringUtils.getValue(edittext_avgcosttravel.getText().toString()));
//        Log.d("HOH", "total family meme: " + edittext_avgcosttravel.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//        //cost travel
//
//        //cost consult
     /*   patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("CostofConsult"));
        patientAttributesDTO.setValue(StringUtils.getValue(edittext_avgcostconsult.getText().toString()));
        Log.d("HOH", "total family meme: " + edittext_avgcostconsult.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);*/
//        //cost consult
//
    //cost medicines
       /* patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("CostofMedicines"));
        patientAttributesDTO.setValue(StringUtils.getValue(edittext_avgcostmedicines.getText().toString()));
        Log.d("HOH", "total family meme: " + edittext_avgcostmedicines.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);*/
//        //cost medicines
//
//        // score of experience
        /*patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ScoreofExperience"));
        patientAttributesDTO.setValue(StringUtils.getScoreExperience(spinner_experiencerscore.getSelectedItem().toString(),sessionManager.getAppLanguage()));
        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);


        // if (llPORoaster.getVisibility() == View.VISIBLE) {

        //how many times pregnant
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NoOfTimesPregnant"));
        if (llPORoaster.getVisibility() == View.VISIBLE) {
            patientAttributesDTO.setValue(StringUtils.getValue(edittext_howmanytimmespregnant.getText().toString()));
        } else {
            patientAttributesDTO.setValue("-");
        }
//        Log.d("HOH", "total family meme: " + edittext_howmanytimmespregnant.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);
        //how many times

//             past two years
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PregnanyPastTwoYears"));
        if (llPORoaster.getVisibility() == View.VISIBLE) {
            patientAttributesDTO.setValue(StringUtils.getPasttwoyrs(spinner_pregnantpasttwoyrs.getSelectedItem().toString(),
                    sessionManager.getAppLanguage()));
        } else {
            patientAttributesDTO.setValue("-");
        }
//        Log.d("HOH", "pregtwoyrs: " + spinner_pregnantpasttwoyrs.getSelectedItem().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //no of times pregnant past 2yrs
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("No_Pregnancy_Outcome_2years"));

        if (llPORoaster.getVisibility() == View.VISIBLE) {
            patientAttributesDTO.setValue(StringUtils.getValue(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString()));
        } else {
            patientAttributesDTO.setValue("-");
        }

//        Log.d("HOH", "total family meme: " + binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO); */
    //no of times pregnant past 2yrs
    //  }
    // past two yrs - end

    // outcome pregnancy
//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("OutcomeOfPregnancy"));
//            patientAttributesDTO.setValue(StringUtils.getOvercomePragnency(spinner_outcomepregnancy.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//            //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);
//
//            // child alive
//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ChildAlive"));
////        patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(spinner_childalive));
//            patientAttributesDTO.setValue(StringUtils.getChildAlive(spinner_childalive.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//            //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);
    //       }

    //year of pregnancy
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("YearOfPregnant"));
//        patientAttributesDTO.setValue(StringUtils.getValue(edittext_yearofpregnancy.getText().toString()));
//        Log.d("HOH", "total family meme: " + edittext_yearofpregnancy.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//        //year of pregnancy
//
//        //months pregnant last
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("MonthPregnantLast"));
//        patientAttributesDTO.setValue(StringUtils.getValue(edittext_monthspregnancylast.getText().toString()));
//        Log.d("HOH", "total family meme: " + edittext_monthspregnancylast.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//        //months pregnant last
//
//        //months pregnant
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NoOfMonthsPregnant"));
//        patientAttributesDTO.setValue(StringUtils.getValue(edittext_monthsbeingpregnant.getText().toString()));
//        Log.d("HOH", "total family meme: " + edittext_monthsbeingpregnant.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//        //months pregnant
//
//        // place of delivery
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PlaceOfDelivery"));
//        patientAttributesDTO.setValue(StringUtils.getPlaceDelivery(spinner_placeofdeliverypregnant.getSelectedItem().toString(),
//                sessionManager.getAppLanguage()));
//        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//
//        patientAttributesDTOList.add(patientAttributesDTO);
//        if (binding.llFocalPoint.getVisibility() == View.VISIBLE && llPORoaster.getVisibility() == View.VISIBLE) {
//            //focal facility
//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("FocalFacility"));
//            String blockData = StringUtils.getFocalFacility_Block(spinner_focalPointBlock.getSelectedItem().toString(), sessionManager.getAppLanguage());
////        String villageData = StringUtils.getFocalFacility_Village(spinner_focalFacilityVillage.getSelectedItem().toString(), sessionManager.getAppLanguage());
//            patientAttributesDTO.setValue(blockData);
////        Log.d("HOH", "FocalFaclity: " + blockData + " - " + villageData);
//            patientAttributesDTOList.add(patientAttributesDTO);
//            //focal facility
//
//        }
//        // single/multiple
//        if (binding.llSingleMultipleBirth.getVisibility() == View.VISIBLE) {
//
//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("SingleMultipleBirth"));
//            patientAttributesDTO.setValue(StringUtils.getSinglemultiplebirths(spinner_singlemultiplebirths.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//            Log.d("HOH", "3005 : " + spinner_singlemultiplebirths.getSelectedItem().toString());
//            Log.d("HOH", "3006 : " + patientAttributesDTO.getValue());
//            patientAttributesDTOList.add(patientAttributesDTO);
//            //singlemultiple
//        }
//        // sex of baby
//        if (binding.llBabyGender.getVisibility() == View.VISIBLE) {
//
//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("SexOfBaby"));
//            patientAttributesDTO.setValue(StringUtils.getSexOfBaby(spinner_sexofbaby.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//            //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);
//        }
//        //baby age died
//
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("BabyAgeDied"));
//        patientAttributesDTO.setValue(StringUtils.getValue(edittext_babyagedied.getText().toString()));
//        Log.d("HOH", "total family meme: " + edittext_babyagedied.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//        //baby age died
//
//        // pregnancy planned
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PregnancyPlanned"));
//        patientAttributesDTO.setValue(StringUtils.getPregnancyPlanned(spinner_pregnancyplanned.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//
//        // pregnancy high risk
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("HighRiskPregnancy"));
//        patientAttributesDTO.setValue(StringUtils.getHighRiskPregnancy(spinner_pregnancyhighriskcase.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//
//        // complications
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Complications"));
//        patientAttributesDTO.setValue(StringUtils.getComplications(spinner_pregnancycomplications.getSelectedItem().toString(), sessionManager.getAppLanguage()));
//        //  Log.d("HOH", "Bankacc: " + spinner_whatisyourrelation.getSelectedItem().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);
//    }

     /* private void rosterValidations(View focusView, boolean cancel)
        {
        if (spinner_whatisyourrelation.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_whatisyourrelation.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_whatisyourrelation;
            cancel = true;
            return;
        }
        if (spinner_maritualstatus.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_maritualstatus.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_maritualstatus;
            cancel = true;
            return;
        }

        if (spinner_phoneownership.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_phoneownership.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_phoneownership;
            cancel = true;
            return;
        }

        if (spinner_bpchecked.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_bpchecked.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_bpchecked;
            cancel = true;
            return;
        }

        if (spinner_sugarchecked.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_sugarchecked.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_sugarchecked;
            cancel = true;
            return;
        }

        if (spinner_hbchecked.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_hbchecked.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_hbchecked;
            cancel = true;
            return;
        }

        if (spinner_bmi.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_bmi.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_bmi;
            cancel = true;
            return;
        } */

//        if (spinner_healthissuereported.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_healthissuereported.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_healthissuereported;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_noofepisodes.getText().toString().equalsIgnoreCase("") &&
//                edittext_noofepisodes.getText().toString().isEmpty()) {
//            edittext_noofepisodes.setError(getString(R.string.select));
//            focusView = edittext_noofepisodes;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_primaryhealthprovider.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_primaryhealthprovider.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_primaryhealthprovider;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_firstlocation.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_firstlocation.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_firstlocation;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_referredto.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_referredto.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_referredto;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_modeoftransport.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_modeoftransport.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_modeoftransport;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_avgcosttravel.getText().toString().equalsIgnoreCase("") &&
//                edittext_avgcosttravel.getText().toString().isEmpty()) {
//            edittext_avgcosttravel.setError(getString(R.string.select));
//            focusView = edittext_avgcosttravel;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_avgcostconsult.getText().toString().equalsIgnoreCase("") &&
//                edittext_avgcostconsult.getText().toString().isEmpty()) {
//            edittext_avgcostconsult.setError(getString(R.string.select));
//            focusView = edittext_avgcostconsult;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_avgcostmedicines.getText().toString().equalsIgnoreCase("") &&
//                edittext_avgcostmedicines.getText().toString().isEmpty()) {
//            edittext_avgcostmedicines.setError(getString(R.string.select));
//            focusView = edittext_avgcostmedicines;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_experiencerscore.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_experiencerscore.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_experiencerscore;
//            cancel = true;
//            return;
//        }

//        if (edittext_howmanytimmespregnant.getText().toString().equalsIgnoreCase("") &&
//                edittext_howmanytimmespregnant.getText().toString().isEmpty()) {
//            edittext_howmanytimmespregnant.setError(getString(R.string.select));
//            focusView = edittext_howmanytimmespregnant;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_pregnantpasttwoyrs.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_pregnantpasttwoyrs.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_pregnantpasttwoyrs;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_outcomepregnancy.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_outcomepregnancy.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_outcomepregnancy;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_childalive.getVisibility() == View.VISIBLE && spinner_childalive.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_childalive.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_childalive;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_yearofpregnancy.getText().toString().equalsIgnoreCase("") &&
//                edittext_yearofpregnancy.getText().toString().isEmpty()) {
//            edittext_yearofpregnancy.setError(getString(R.string.select));
//            focusView = edittext_yearofpregnancy;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_monthspregnancylast.getText().toString().equalsIgnoreCase("") &&
//                edittext_monthspregnancylast.getText().toString().isEmpty()) {
//            edittext_monthspregnancylast.setError(getString(R.string.select));
//            focusView = edittext_monthspregnancylast;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_monthsbeingpregnant.getVisibility() == View.VISIBLE &&
//                edittext_monthsbeingpregnant.getText().toString().equalsIgnoreCase("") &&
//                edittext_monthsbeingpregnant.getText().toString().isEmpty()) {
//            edittext_monthsbeingpregnant.setError(getString(R.string.select));
//            focusView = edittext_monthsbeingpregnant;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_placeofdeliverypregnant.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_placeofdeliverypregnant.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_placeofdeliverypregnant;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_singlemultiplebirths.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_singlemultiplebirths.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_singlemultiplebirths;
//            cancel = true;
//            return;
//        }
//
//
//        if (spinner_sexofbaby.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_sexofbaby.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_sexofbaby;
//            cancel = true;
//            return;
//        }
//
//        if (edittext_babyagedied.getText().toString().equalsIgnoreCase("") &&
//                edittext_babyagedied.getText().toString().isEmpty()) {
//            edittext_babyagedied.setError(getString(R.string.select));
//            focusView = edittext_babyagedied;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_pregnancyplanned.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_pregnancyplanned.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_pregnancyplanned;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_pregnancyhighriskcase.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_pregnancyhighriskcase.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_pregnancyhighriskcase;
//            cancel = true;
//            return;
//        }
//
//        if (spinner_pregnancycomplications.getSelectedItemPosition() == 0) {
//            TextView t = (TextView) spinner_pregnancycomplications.getSelectedView();
//            t.setError(getString(R.string.select));
//            t.setTextColor(Color.RED);
//            focusView = spinner_pregnancycomplications;
//            cancel = true;
//            return;
//        }
//}

    public void onPatientUpdateClicked(Patient patientdto) {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = patientdto.getUuid();

        patientdto.setUuid(uuid);
        Gson gson = new Gson();

        final boolean[] cancel = {false};
        View focusView = null;


        if (dob.equals("") || dob.toString().equals("")) {
            if (dob.after(today)) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
                //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                mDOBPicker.show();
                alertDialog.show();

                Button postiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                postiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                // postiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
                return;
            }
        }

        if (mPhoneNum.getText().toString().trim().length() > 0) {
            if (mPhoneNum.getText().toString().trim().length() < 9) {
                mPhoneNum.requestFocus();
                mPhoneNum.setError(getResources().getString(R.string.enter_9_digits));
                return;
            }
        }

       /* ArrayList<EditText> values = new ArrayList<>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
        values.add(mDOB);
        values.add(mPhoneNum);
        values.add(mAddress1);
        values.add(mAddress2);
        values.add(mCity);
        values.add(mPostal);
        values.add(mRelationship);
        values.add(mOccupation);*/


        /*if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            return;
        }*/


        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("") && !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mPhoneNum.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked() || mGenderO.isChecked()) && (yesHOH.isChecked() || noHOH.isChecked()) && (studentCB.isChecked() || emergencyCB.isChecked() || generalCB.isChecked() || fhhSurveyCB.isChecked())) {
            aidSelectionImplementation();
            Log.v(TAG, "Result");
        } else {
            if (mFirstName.getText().toString().equals("")) {
                if (focusView == null) focusView = mFirstName;
                mFirstName.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (mLastName.getText().toString().equals("")) {
                if (focusView == null) focusView = mLastName;
                mLastName.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (mDOB.getText().toString().equals("")) {
                if (focusView == null) focusView = mDOB;
                mDOB.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (mAge.getText().toString().equals("")) {
                if (focusView == null) focusView = mAge;
                mAge.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            }

            if (!phone_checkbox.isChecked() && mPhoneNum.getText().toString().equalsIgnoreCase("")) {
                if (focusView == null) focusView = mPhoneNum;
                mPhoneNum.setError(getString(R.string.error_field_required));
                cancel[0] = true;
            } else {
                mPhoneNum.setError(null);
            }

            if (!mGenderF.isChecked() && !mGenderM.isChecked() && !mGenderO.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
                View finalFocusView = focusView;
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finalFocusView == null)
                            radioGrp.getParent().requestChildFocus(radioGrp, radioGrp);
                        cancel[0] = true;
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
                return;
            }

/*
                if (!yesHOH.isChecked() && !noHOH.isChecked()) {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                    alertDialogBuilder.setTitle(R.string.error);
                    alertDialogBuilder.setMessage(R.string.identification_screen_required_fields);
                    alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

                }
*/

/*
                if (!generalCB.isChecked() && !studentCB.isChecked() && !emergencyCB.isChecked() && !fhhSurveyCB.isChecked()) {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                    alertDialogBuilder.setTitle(R.string.error);
                    alertDialogBuilder.setMessage(R.string.identification_screen_required_fields);
                    alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

                }
*/
        }

//        if (mCountry.getSelectedItemPosition() == 0) {
//            countryText.setError(getString(R.string.error_field_required));
//            focusView = countryText;
//            cancel = true;
//            return;
//        } else {
//            countryText.setError(null);
//        }

//            if (mState.getSelectedItemPosition() == 0) {
//                stateText.setError(getString(R.string.error_field_required));
//                focusView = stateText;
//                cancel = true;
//                return;
//            } else {
//                stateText.setError(null);
//            }

        if (mAddress1.getText().toString().isEmpty() || mAddress1.getText().toString().equalsIgnoreCase("")) {
            mAddress1.setError(getString(R.string.error_field_required));
            if (focusView == null) focusView = mAddress1;
            cancel[0] = true;
        } else {
            mAddress1.setError(null);
        }

        // Check for validations on Earthquake Victim questions only if the Earthquake Victim Checkbox is checked - Added by Arpan Sircar
        if (earthquakeVictimCheckbox.isChecked()) {

            // Validation for Special Needs Spinner
            if (binding.spinnerNatureSpecialNeeds.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerNatureSpecialNeeds.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerNatureSpecialNeeds;
                cancel[0] = true;
            }

            // Validation for Loss As a Result Of Earthquake Spinner
            if (binding.spinnerLossAsAResultOfTheEarthquake.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerLossAsAResultOfTheEarthquake.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerLossAsAResultOfTheEarthquake;
                cancel[0] = true;
            }

            // Validation for Loss of Analgesic Spinner
            if (binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null)
                    focusView = binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake;
                cancel[0] = true;
            }

            // Validation for Loss of Breadwinner
            if (binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null)
                    focusView = binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake;
                cancel[0] = true;
            }

            // Validation for Strong Social Ties Spinner
            if (binding.spinnerStrongSocialTies.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerStrongSocialTies.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerStrongSocialTies;
                cancel[0] = true;
            }

            // Validation for Places Of Relatives EditText
            if ((binding.etPlacesOfRelatives.getText().toString().isEmpty() || binding.etPlacesOfRelatives.getText().toString().equalsIgnoreCase(""))) {
                binding.etPlacesOfRelatives.setError(getString(R.string.places_of_relatives_cannot_be_empty));
                if (focusView == null) focusView = binding.etPlacesOfRelatives;
                cancel[0] = true;
            }

            // Validation for Place Of Preference Spinner
            if (binding.spinnerPlacesOfPreference.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.spinnerPlacesOfPreference.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                if (focusView == null) focusView = binding.spinnerPlacesOfPreference;
                cancel[0] = true;
            }
        }

            /*if (HOH_relation_spinner.getVisibility() == View.VISIBLE && (HOH_relation_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) HOH_relation_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = HOH_relation_spinner;
                cancel = true;
                return;
            }

            if (marital_status_spinner.getVisibility() == View.VISIBLE && (marital_status_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) marital_status_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = marital_status_spinner;
                cancel = true;
                return;
            }

            if (mEducation.getVisibility() == View.VISIBLE && (mEducation.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) mEducation.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = mEducation;
                cancel = true;
                return;
            }

            if (mOccupation.getVisibility() == View.VISIBLE && (mOccupation.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) mOccupation.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = mOccupation;
                cancel = true;
                return;
            }

            if (link_nature_spinner.getVisibility() == View.VISIBLE && (link_nature_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) link_nature_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = link_nature_spinner;
                cancel = true;
                return;
            }

            if (independent_resid_spinner.getVisibility() == View.VISIBLE && (independent_resid_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) independent_resid_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = independent_resid_spinner;
                cancel = true;
                return;
            }

            if (residence_nature_spinner.getVisibility() == View.VISIBLE && (residence_nature_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) residence_nature_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = residence_nature_spinner;
                cancel = true;
                return;
            }

            if (whyHOH_spinner.getVisibility() == View.VISIBLE && (whyHOH_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) whyHOH_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = whyHOH_spinner;
                cancel = true;
                return;
            }

            if (mainTendency_spinner.getVisibility() == View.VISIBLE && (mainTendency_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) mainTendency_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = mainTendency_spinner;
                cancel = true;
                return;
            }

            if (changeConditionReason_spinner.getVisibility() == View.VISIBLE && (changeConditionReason_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) changeConditionReason_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = changeConditionReason_spinner;
                cancel = true;
                return;
            }

            if (percentage_income_spinner.getVisibility() == View.VISIBLE && (percentage_income_spinner.getSelectedItemPosition() == 0)) {
                TextView t = (TextView) percentage_income_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = percentage_income_spinner;
                cancel = true;
                return;
            }

            if (sinceSupportingFamilyET.getVisibility() == View.VISIBLE && (sinceSupportingFamilyET.getText().toString().isEmpty() || sinceSupportingFamilyET.getText().toString().equalsIgnoreCase(""))) {
                sinceSupportingFamilyET.setError(getString(R.string.error_field_required));
                focusView = sinceSupportingFamilyET;
                cancel = true;
                return;
            } else {
                sinceSupportingFamilyET.setError(null);
            }

            if (sinceChangeHappenedET.getVisibility() == View.VISIBLE && (sinceChangeHappenedET.getText().toString().isEmpty() || sinceChangeHappenedET.getText().toString().equalsIgnoreCase(""))) {
                sinceChangeHappenedET.setError(getString(R.string.error_field_required));
                focusView = sinceChangeHappenedET;
                cancel = true;
                return;
            } else {
                sinceChangeHappenedET.setError(null);
            } */


        if (cancel[0]) {
            focusView.getParent().requestChildFocus(focusView, focusView);
            // the below changes are done for the requirement raised in ticket SYR-160
//                View finalFocusView = focusView;
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        int vLeft = finalFocusView.getLeft();
//                        int vRight = finalFocusView.getRight();
//                        int sWidth = scrollView.getWidth();
//                        scrollView.smoothScrollTo(((vLeft + vRight - sWidth) / 4), 1);
//                    }
//                });
            Toast.makeText(this, getString(R.string.identification_fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (mCurrentPhotoPath == null) mCurrentPhotoPath = patientdto.getPatient_photo();

            patientdto.setFirst_name(StringUtils.getValue(mFirstName.getText().toString()));
            patientdto.setMiddle_name(StringUtils.getValue(mMiddleName.getText().toString()));
            patientdto.setLast_name(StringUtils.getValue(mLastName.getText().toString()));
            patientdto.setPhone_number(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));

            String[] dob_array = mDOB.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or_bn_en_noEdit(mDOB.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
                String dob_month_split = dob_array[1];
                dob_array[1] = dob_month_split.replace(dob_month_split, dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];

                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob_value)));
            }

            patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));

            String value = switch_ar_to_en_village(mVillage.getSelectedItem().toString());
            patientdto.setCity_village(value);

            patientdto.setPostal_code(StringUtils.getValue(mPostal.getText().toString()));
            patientdto.setCountry("Syria"); //hardcoding this as this field is important to send in the db but partner asked to remove this field from patient registration.
            patientdto.setPatient_photo(mCurrentPhotoPath);

            String stateName = mState.getSelectedItem().toString();
            patientdto.setState_province(switch_ar_to_en_state(stateName));
            Log.v("state", "state name: " + stateName);
            //  patientdto.setState_province(mState.getSelectedItem().toString());

              /*  patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("patient aid type"));
                String aidTypeInJson = getAidTypeInJson();
                patientAttributesDTO.setValue(aidTypeInJson);
                patientAttributesDTOList.add(patientAttributesDTO);*/

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("responsible for family"));
            String hohInfoValue = headOfHousehold;
            String hohInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), hohInfoValue, R.array.independent_residence_en, R.array.independent_residence_ar);
            patientAttributesDTO.setValue(hohInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("main official for family's support"));
            String whyHOHInfoValue = StringUtils.getProvided(whyHOH_spinner);
            String whyHOHInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), whyHOHInfoValue, R.array.why_hoh_en, R.array.why_hoh_ar);
            patientAttributesDTO.setValue(whyHOHInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // Commented by Arpan Sircar - these two fields are not visible in the activity - keeping them here is preventing sync from happening as empty values are being passed.

//                patientAttributesDTO = new PatientAttributesDTO();
//                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//                patientAttributesDTO.setPatientuuid(uuid);
//                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("change happened years"));
//                patientAttributesDTO.setValue(sinceChangeHappenedET.getText().toString());
//                patientAttributesDTOList.add(patientAttributesDTO);
//
//                patientAttributesDTO = new PatientAttributesDTO();
//                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//                patientAttributesDTO.setPatientuuid(uuid);
//                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("number of years for support"));
//                patientAttributesDTO.setValue(sinceSupportingFamilyET.getText().toString());
//                patientAttributesDTOList.add(patientAttributesDTO);


            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("main tendency for breadwinner"));
            String mainTendInfoValue = StringUtils.getProvided(mainTendency_spinner);
            String mainTendInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), mainTendInfoValue, R.array.independent_residence_en, R.array.independent_residence_ar);
            patientAttributesDTO.setValue(mainTendInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("condition of the support"));
            String conditionSupportInfoValue = StringUtils.getProvided(changeConditionReason_spinner);
            String conditionSupportInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), conditionSupportInfoValue, R.array.reason_for_change_en, R.array.reason_for_change_ar);
            patientAttributesDTO.setValue(conditionSupportInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("percentage of income"));
            String percIncomeInfoValue = StringUtils.getProvided(percentage_income_spinner);
            String percIncomeInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), percIncomeInfoValue, R.array.percentage_income_en, R.array.percentage_income_ar);
            patientAttributesDTO.setValue(percIncomeInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("status of husband"));
            String husbandStatusInfoValue = StringUtils.getProvided(husband_status_spinner);
            String husbandStatusInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), husbandStatusInfoValue, R.array.husband_status_en, R.array.husband_status_ar);
            patientAttributesDTO.setValue(husbandStatusInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Do you have an independent residence"));
            String indepResidInfoValue = StringUtils.getProvided(independent_resid_spinner);
            String indepResidInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), indepResidInfoValue, R.array.independent_residence_en, R.array.independent_residence_ar);
            patientAttributesDTO.setValue(indepResidInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone number"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nature_of_the_residential_complex"));
            String residenceNatureInfoValue = StringUtils.getProvided(residence_nature_spinner);
            String residenceNatureInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), residenceNatureInfoValue, R.array.nature_residential_complex_en, R.array.nature_residential_complex_ar);
            patientAttributesDTO.setValue(residenceNatureInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Employment status"));
            String occupationInfoValue = StringUtils.getProvided(mOccupation);
            String occupationInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), occupationInfoValue, R.array.occupation_identification_en, R.array.occupation_identification_ar);
            patientAttributesDTO.setValue(occupationInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("link to the place of residence"));
            String linkNatureInfoValue = StringUtils.getProvided(link_nature_spinner);
            String linkNatureInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), linkNatureInfoValue, R.array.nature_of_link_to_residence_en, R.array.nature_of_link_to_residence_ar);
            patientAttributesDTO.setValue(linkNatureInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // relationsip hoh
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("The family relationship"));
            String relationHOHInfoValue = StringUtils.getProvided(HOH_relation_spinner);
            String relationHOHInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), relationHOHInfoValue, R.array.relationshipHoH_en, R.array.relationshipHoH_ar);
            patientAttributesDTO.setValue(relationHOHInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            // marital
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("family situation"));
            String maritalStatusInfoValue = StringUtils.getProvided(marital_status_spinner);
            String maritalStatusInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), maritalStatusInfoValue, R.array.marital_en, R.array.marital_ar);
            patientAttributesDTO.setValue(maritalStatusInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            String educationInfoValue = StringUtils.getProvided(mEducation);
            String educationInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), educationInfoValue, R.array.education_en, R.array.education_ar);
            patientAttributesDTO.setValue(educationInfoJson);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
            patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("CREATED_BY"));
            patientAttributesDTO.setValue(patient1.getHealthWorkerID());
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("IS_DISASTER_VICTIM"));

            if (earthquakeVictimCheckbox.isChecked()) {
                patientAttributesDTO.setValue("Yes");
            } else {
                patientAttributesDTO.setValue("No");
            }

            patientAttributesDTOList.add(patientAttributesDTO);

            //House Hold Registration
            if (sessionManager.getHouseholdUuid().equals("")) {
                String HouseHold_UUID = UUID.randomUUID().toString();
                sessionManager.setHouseholdUuid(HouseHold_UUID);
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Household ID Number"));
                patientAttributesDTO.setValue(HouseHold_UUID);
            } else {
                String HouseHold_UUID = sessionManager.getHouseholdUuid();
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Household ID Number"));
                patientAttributesDTO.setValue(HouseHold_UUID);
            }

            // If the earthquake checkbox is checked, we will show the earthquake questions. And only then we will extract the data.
            if (earthquakeVictimCheckbox.isChecked()) {

                // Nature of special needs spinner
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NATURE_OF_SPECIAL_NEEDS"));
                String specialNeedsInfoValue = StringUtils.getProvided(binding.spinnerNatureSpecialNeeds);
                String specialNeedsInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), specialNeedsInfoValue, R.array.victim_special_needs_en, R.array.victim_special_needs_ar);
                patientAttributesDTO.setValue(specialNeedsInfoJson);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Loss as a result of the earthquake
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("LOSS_AS_A_RESULT_OF_THE_EARTHQUAKE"));
                String lossInfoValue = StringUtils.getProvided(binding.spinnerLossAsAResultOfTheEarthquake);
                String lossInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), lossInfoValue, R.array.victim_loss_due_to_earthquake_en, R.array.victim_loss_due_to_earthquake_ar);
                patientAttributesDTO.setValue(lossInfoJson);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Loss of analgesic as a result of the earthquake
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("LOSS_OF_ANALGESIC_AS_A_RESULT_OF_THE_EARTHQUAKE"));
                String lossOfAnalgesicInfoValue = StringUtils.getProvided(binding.spinnerLossOfAnalgesicAsAResultOfTheEarthquake);
                String lossOfAnalgesicInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), lossOfAnalgesicInfoValue, R.array.victim_loss_of_analgesic_en, R.array.victim_loss_of_analgesic_ar);
                patientAttributesDTO.setValue(lossOfAnalgesicInfoJson);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Loss of breadwinner as a result of the earthquake
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("LOSS_OF_BREADWINNER_AS_RESULT_OF_EARTHQUAKE"));
                String lossOfBreadwinnerInfoValue = StringUtils.getProvided(binding.spinnerLossOfBreadwinnerAsAResultOfTheEarthquake);
                String lossOfBreadwinnerInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), lossOfBreadwinnerInfoValue, R.array.victim_loss_of_breadwinner_en, R.array.victim_loss_of_breadwinner_ar);
                patientAttributesDTO.setValue(lossOfBreadwinnerInfoJson);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Strong social ties spinner
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("STRONG_SOCIAL_TIES_IN_YOUR_RESIDENCE_AREA"));
                String strongSocialTiesInfoValue = StringUtils.getProvided(binding.spinnerStrongSocialTies);
                String strongSocialTiesInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), strongSocialTiesInfoValue, R.array.victim_strong_social_ties_en, R.array.victim_strong_social_ties_ar);
                patientAttributesDTO.setValue(strongSocialTiesInfoJson);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Place of relatives
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PLACES_OF_RELATIVES_IN_OTHER_PARTS_OF_SYRIA"));
                String placesOfRelativesInfoValue = binding.etPlacesOfRelatives.getText().toString();
                patientAttributesDTO.setValue(placesOfRelativesInfoValue);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Place of hosting spinner
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PLACES_OF_PREFERENCE_FOR_HOSTING"));
                String placeOfHostingInfoValue = StringUtils.getProvided(binding.spinnerPlacesOfPreference);
                String placeOfHostingInfoJson = arrayValueInJson(this, sessionManager.getAppLanguage(), placeOfHostingInfoValue, R.array.victim_place_of_preference_en, R.array.victim_place_of_preference_ar);
                patientAttributesDTO.setValue(placeOfHostingInfoJson);
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            //House Hold Registration - End
            patientAttributesDTOList.add(patientAttributesDTO);

            Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
            Logger.logD(TAG, "PatientAttribute list" + patientAttributesDTOList.toString());
            Logger.logD("patient json onPatientUpdateClicked : ", "Json : " + gson.toJson(patientdto, Patient.class));

        }

        try {
            Logger.logD(TAG, "update ");
            boolean isPatientUpdated = patientsDAO.updatePatientToDB(patientdto, uuid, patientAttributesDTOList);
            boolean isPatientImageUpdated = imagesDAO.updatePatientProfileImages(mCurrentPhotoPath, uuid);

            if (NetworkConnection.isOnline(getApplication())) {
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean ispush = syncDAO.pushDataApi();
                boolean isPushImage = imagesPushDAO.patientProfileImagesPush();
            }

            if (isPatientUpdated && isPatientImageUpdated) {
                Logger.logD(TAG, "updated");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirst_name() + " " + patientdto.getLast_name());
                i.putExtra("tag", "newPatient");
                i.putExtra("hasPrescription", "false");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        commentedRoasterValidations();
    }

    private void initUI() {
        scrollView = findViewById(R.id.identification_scrollView);
        mFirstName = findViewById(R.id.identification_first_name);
        mFirstName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mMiddleName = findViewById(R.id.identification_middle_name);
        mMiddleName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mLastName = findViewById(R.id.identification_last_name);
        mLastName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mDOB = findViewById(R.id.identification_birth_date_text_view);
        mPhoneNum = findViewById(R.id.identification_phone_number);
        phone_checkbox = findViewById(R.id.phone_checkbox);
        earthquakeVictimCheckbox = findViewById(R.id.earthquake_victim_checkbox);
        mAge = findViewById(R.id.identification_age);
        mAddress1 = findViewById(R.id.identification_address1);
        mAddress1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50
        mAddress2 = findViewById(R.id.identification_address2);
        mAddress2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50
        stateText = findViewById(R.id.identification_state);
        mState = findViewById(R.id.spinner_state);
        mVillage = findViewById(R.id.spinner_village);
        postal_layout = findViewById(R.id.postal_layout);
        mPostal = findViewById(R.id.identification_postal_code);
        countryText = findViewById(R.id.identification_country);
        mCountry = findViewById(R.id.spinner_country);
        mGenderM = findViewById(R.id.identification_gender_male);
        mGenderF = findViewById(R.id.identification_gender_female);
        radioGrp = findViewById(R.id.radioGrp);
        mGenderO = findViewById(R.id.identification_gender_others);
        mOccupation = findViewById(R.id.spinner_occupation);
        mEducation = findViewById(R.id.spinner_education);
        countryStateLayout = findViewById(R.id.identification_llcountry_state);
        mImageView = findViewById(R.id.imageview_id_picture);
        HOH_relation_spinner = findViewById(R.id.spinner_whatisyourrelation);
        marital_status_spinner = findViewById(R.id.spinner_maritualstatus);
        link_nature_spinner = findViewById(R.id.spinner_residence_link_nature);
        residence_nature_spinner = findViewById(R.id.spinner_residence_nature);
        husband_status_spinner = findViewById(R.id.spinner_husband_status);
        yesHOH = findViewById(R.id.hoh_yes);
        noHOH = findViewById(R.id.hoh_no);
        independent_resid_spinner = findViewById(R.id.spinner_independent_residence);
        whyHOH_spinner = findViewById(R.id.spinner_main_official);
        mainTendency_spinner = findViewById(R.id.spinner_main_tendency_for_breadwinner);
        changeConditionReason_spinner = findViewById(R.id.spinner_reason_changing_condition);
        percentage_income_spinner = findViewById(R.id.spinner_percentage_income);
        sinceChangeHappenedET = findViewById(R.id.ET_since_change_happened);
        sinceSupportingFamilyET = findViewById(R.id.et_since_when_supporting_family);
        emergencyCB = findViewById(R.id.cbEmergency);
        generalCB = findViewById(R.id.cbGeneralAid);
        studentCB = findViewById(R.id.cbStudentAid);
        fhhSurveyCB = findViewById(R.id.cbFHHSurvey);
        selectedAid_en = new ArrayList<>();
        selectedAid_ar = new ArrayList<>();
//        hideSpinnersBasedOnAid();
        aidSelectionImplementation();
        roasterCommentedCode();
        initializeEarthquakeVictimCheckbox();
        initializeStateAndVillageSpinner();
    }

    private void initializeStateAndVillageSpinner() {
        try {
            String stateLanguage = "states_syriana_en";
            if (sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                stateLanguage = "states_syriana_" + sessionManager.getAppLanguage();
            if (sessionManager.getAppLanguage().equalsIgnoreCase("ar"))
                stateLanguage = "states_syriana_" + sessionManager.getAppLanguage();

            int states = getResources().getIdentifier(stateLanguage, "array", getApplicationContext().getPackageName());
            if (states != 0) {
                stateAdapter = ArrayAdapter.createFromResource(this, states, R.layout.custom_spinner);

            }
            mState.setAdapter(stateAdapter);
            mState.setSelection(stateAdapter.getPosition(sessionManager.getStateName()));
            mState.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
        }

        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setVillageSpinnerAdapter("as_sweida_villages_");
                        break;

                    case 1:
                        setVillageSpinnerAdapter("homms_villages_");
                        break;

                    case 2:
                        setVillageSpinnerAdapter("tartous_villages_");
                        break;

                    case 3:
                        setVillageSpinnerAdapter("rural_damascus_villages_");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeEarthquakeVictimCheckbox() {
        // We will show the earthquake victim questions only if the Earthquake Victim checkbox is checked - Added by Arpan Sircar for Syriana Emergency Release
        earthquakeVictimCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.llEarthquakeVictimQuestions.setVisibility(View.VISIBLE);
            else binding.llEarthquakeVictimQuestions.setVisibility(View.GONE);

        });
    }

    private void hideSpinnersBasedOnAid() {
        link_nature_spinner.setSelection(0);
        link_nature_spinner.setVisibility(View.GONE);
        residence_nature_spinner.setSelection(0);
        residence_nature_spinner.setVisibility(View.GONE);
        husband_status_spinner.setSelection(0);
        husband_status_spinner.setVisibility(View.GONE);
        independent_resid_spinner.setSelection(0);
        independent_resid_spinner.setVisibility(View.GONE);
        percentage_income_spinner.setSelection(0);
        percentage_income_spinner.setVisibility(View.GONE);
        whyHOH_spinner.setSelection(0);
        whyHOH_spinner.setVisibility(View.GONE);
        sinceSupportingFamilyET.setText("");
        sinceSupportingFamilyET.setVisibility(View.GONE);
        mainTendency_spinner.setSelection(0);
        mainTendency_spinner.setVisibility(View.GONE);
        changeConditionReason_spinner.setSelection(0);
        changeConditionReason_spinner.setVisibility(View.GONE);
        sinceChangeHappenedET.setText("");
        sinceChangeHappenedET.setVisibility(View.GONE);
    }

    private void showSpinnersBasedOnAid() {
        link_nature_spinner.setVisibility(View.VISIBLE);
        residence_nature_spinner.setVisibility(View.VISIBLE);
        husband_status_spinner.setVisibility(View.VISIBLE);
        independent_resid_spinner.setVisibility(View.VISIBLE);
        percentage_income_spinner.setVisibility(View.VISIBLE);
        whyHOH_spinner.setVisibility(View.VISIBLE);
        sinceSupportingFamilyET.setVisibility(View.VISIBLE);
        mainTendency_spinner.setVisibility(View.VISIBLE);
        changeConditionReason_spinner.setVisibility(View.VISIBLE);
        sinceChangeHappenedET.setVisibility(View.VISIBLE);
    }

    private void aidSelectionImplementation() {

        if (emergencyCB.isChecked()) {
            if (!selectedAid_en.contains("Emergency Need Evaluation"))
                selectedAid_en.add("Emergency Need Evaluation");
            if (!selectedAid_ar.contains("تقييم الاحتياجات الطارئة"))
                selectedAid_ar.add("تقييم الاحتياجات الطارئة");
        }
        if (generalCB.isChecked()) {
            if (!selectedAid_en.contains("General Aid")) selectedAid_en.add("General Aid");
            if (!selectedAid_ar.contains("المساعدة العامة")) selectedAid_ar.add("المساعدة العامة");
        }
        if (studentCB.isChecked()) {
            if (!selectedAid_en.contains("Student Aid")) selectedAid_en.add("Student Aid");
            if (!selectedAid_ar.contains("مساعدة الطلاب")) selectedAid_ar.add("مساعدة الطلاب");
        }
        if (fhhSurveyCB.isChecked()) {
            if (!selectedAid_en.contains("FHH Survey")) selectedAid_en.add("FHH Survey");
            if (!selectedAid_ar.contains("مسح FHH")) selectedAid_ar.add("مسح FHH");
        }

        emergencyCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emergencyCB.isChecked()) {
                    if (!selectedAid_en.contains("Emergency Need Evaluation"))
                        selectedAid_en.add("Emergency Need Evaluation");
                    if (!selectedAid_ar.contains("تقييم الاحتياجات الطارئة"))
                        selectedAid_ar.add("تقييم الاحتياجات الطارئة");
                } else if (!emergencyCB.isChecked()) {
                    if (selectedAid_en.contains("Emergency Need Evaluation"))
                        selectedAid_en.remove("Emergency Need Evaluation");
                    if (selectedAid_ar.contains("تقييم الاحتياجات الطارئة"))
                        selectedAid_ar.remove("تقييم الاحتياجات الطارئة");

                }
            }
        });

        generalCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (generalCB.isChecked()) {
                    if (!selectedAid_en.contains("General Aid")) selectedAid_en.add("General Aid");
                    if (!selectedAid_ar.contains("المساعدة العامة"))
                        selectedAid_ar.add("المساعدة العامة");
//                    showSpinnersBasedOnAid();
                } else if (!generalCB.isChecked()) {
                    if (selectedAid_en.contains("General Aid"))
                        selectedAid_en.remove("General Aid");
                    if (selectedAid_ar.contains("المساعدة العامة"))
                        selectedAid_ar.remove("المساعدة العامة");

//                    if (!selectedAid_en.contains("FHH Survey") && !selectedAid_ar.contains("مسح FHH"))
//                        hideSpinnersBasedOnAid();
                }
            }
        });

        studentCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (studentCB.isChecked()) {
                    if (!selectedAid_en.contains("Student Aid")) selectedAid_en.add("Student Aid");
                    if (!selectedAid_ar.contains("مساعدة الطلاب"))
                        selectedAid_ar.add("مساعدة الطلاب");
                } else if (!studentCB.isChecked()) {
                    if (selectedAid_en.contains("Student Aid"))
                        selectedAid_en.remove("Student Aid");
                    if (selectedAid_ar.contains("مساعدة الطلاب"))
                        selectedAid_ar.remove("مساعدة الطلاب");
                }
            }
        });

        fhhSurveyCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fhhSurveyCB.isChecked()) {
                    if (!selectedAid_en.contains("FHH Survey")) selectedAid_en.add("FHH Survey");
                    if (!selectedAid_ar.contains("مسح FHH")) selectedAid_ar.add("مسح FHH");
//                    showSpinnersBasedOnAid();
                } else if (!fhhSurveyCB.isChecked()) {
                    if (selectedAid_en.contains("FHH Survey")) selectedAid_en.remove("FHH Survey");

                    if (selectedAid_ar.contains("مسح FHH")) selectedAid_ar.remove("مسح FHH");

//                    if (!selectedAid_en.contains("General Aid") && !selectedAid_ar.contains("المساعدة العامة"))
//                        hideSpinnersBasedOnAid();

                }
            }
        });
    }

    private void commentedRoasterValidations() {
            /*Other
            if (til_whatisyourrelation_other.getVisibility() == View.VISIBLE) {
                if (et_whatisyourrelation_other.getText().toString().equals("")) {
                    et_whatisyourrelation_other.setError(getString(R.string.error_field_required));
                    focusView = et_whatisyourrelation_other;
                    cancel = true;
                    return;
                }
            }

            if (til_occupation_other.getVisibility() == View.VISIBLE) {
                if (et_occupation_other.getText().toString().equals("")) {
                    et_occupation_other.setError(getString(R.string.error_field_required));
                    focusView = et_occupation_other;
                    cancel = true;
                    return;
                }
            }

            if (((String) mOccupation.getSelectedItem()).equals(getString(R.string.other_please_specify))) {
                String occupation = Objects.requireNonNull(til_occupation_other.getEditText()).getText().toString();
                if (occupation.equals("")) {
                    til_occupation_other.getEditText().setError(getString(R.string.error_field_required));
                    focusView = til_occupation_other.getEditText();
                    cancel = true;
                    return;
                }
            }*/
        /*if (ll18.getVisibility() == View.VISIBLE) {
            if (spinner_bpchecked.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_bpchecked.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_bpchecked;
                cancel = true;
                return;
            }

            if (spinner_sugarchecked.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_sugarchecked.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_sugarchecked;
                cancel = true;
                return;
            }
        }

        if (spinner_hbchecked.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_hbchecked.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_hbchecked;
            cancel = true;
            return;
        }

        if (spinner_bmi.getSelectedItemPosition() == 0) {
            TextView t = (TextView) spinner_bmi.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = spinner_bmi;
            cancel = true;
            return;
        }

        if (llPORoaster.getVisibility() == View.VISIBLE) {
            if (edittext_howmanytimmespregnant.getText().toString().equalsIgnoreCase("") &&
                    edittext_howmanytimmespregnant.getText().toString().isEmpty()) {
                edittext_howmanytimmespregnant.setError(getString(R.string.select));
                focusView = edittext_howmanytimmespregnant;
                cancel = true;
                return;
            }

            if (spinner_pregnantpasttwoyrs.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_pregnantpasttwoyrs.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_pregnantpasttwoyrs;
                cancel = true;
                return;
            }

            if (binding.spinnerPregnantpasttwoyrs.getSelectedItemPosition() == 1) {
                Logger.logD("Spinner", binding.spinnerPregnantpasttwoyrs.getSelectedItem().toString());
                if (binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equalsIgnoreCase("") &&
                        binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty()) {
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.setError(getString(R.string.select));
                    focusView = binding.edittextNoOfPregnancyOutcomePastTwoYrs;
                    cancel = true;
                    return;
                }

                if (binding.poViewPager.getAdapter() != null) {
                    if (!binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equalsIgnoreCase("") &&
                            !binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty() &&
                            Integer.parseInt(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString()) > 0 &&
                            binding.poViewPager.getAdapter().getItemCount() == 0) {
                        Toast.makeText(this, R.string.please_add_pregnancy_outcome, Toast.LENGTH_SHORT).show();
                        focusView = binding.edittextNoOfPregnancyOutcomePastTwoYrs;
                        cancel = true;
                        return;
                    }
                }

                Log.v(TAG, "adapter count: " + pregnancyOutcomeAdapter.getItemCount());
                if (pregnancyOutcomesList.size() != Integer.parseInt(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString())) {
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.setError(getString(R.string.select));
                    focusView = binding.edittextNoOfPregnancyOutcomePastTwoYrs;
                    cancel = true;
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.setFocusable(true);
                    binding.edittextNoOfPregnancyOutcomePastTwoYrs.requestFocus();
                    return;
                }

        } */

    }

    private void roasterCommentedCode() {
        //        mRelationship = findViewById(R.id.identification_relationship);
//        mRelationship.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25
            /*til_occupation_other = findViewById(R.id.til_occupation_other);
            et_occupation_other = findViewById(R.id.et_occupation_other);
            mOccupation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 13) {
                        til_occupation_other.setVisibility(View.VISIBLE);
                    } else {
                        til_occupation_other.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/

/*        mOccupation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

//        mCaste = findViewById(R.id.spinner_caste);
//        mEconomicStatus = findViewById(R.id.spinner_economic_status);
//        casteText = findViewById(R.id.identification_caste);
//        educationText = findViewById(R.id.identification_education);
//        economicText = findViewById(R.id.identification_econiomic_status);

//        casteLayout = findViewById(R.id.identification_txtlcaste);
//        economicLayout = findViewById(R.id.identification_txtleconomic);
//        educationLayout = findViewById(R.id.identification_txtleducation); */

//        pregnancyQuestionsLinearLayout = findViewById(R.id.pregnancy_questions_linear_layout);

        //Roaster Spinner
            /*til_whatisyourrelation_other = findViewById(R.id.til_whatisyourrelation_other);
            et_whatisyourrelation_other = findViewById(R.id.et_whatisyourrelation_other);
            spinner_whatisyourrelation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 16) {
                        til_whatisyourrelation_other.setVisibility(View.VISIBLE);
                    } else {
                        til_whatisyourrelation_other.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/

//        spinner_phoneownership = findViewById(R.id.spinner_phoneownership);
//        spinner_bpchecked = findViewById(R.id.spinner_bpchecked);
//        spinner_sugarchecked = findViewById(R.id.spinner_sugarchecked);
//        spinner_hbchecked = findViewById(R.id.spinner_hbchecked);
//        spinner_bmi = findViewById(R.id.spinner_bmi);
//        spinner_healthissuereported = findViewById(R.id.spinner_healthissuereported);
//        spinner_primaryhealthprovider = findViewById(R.id.spinner_primaryhealthprovider);
//        spinner_firstlocation = findViewById(R.id.spinner_firstlocation);
//        spinner_referredto = findViewById(R.id.spinner_referredto);
//        spinner_singlemultiplebirths = findViewById(R.id.spinner_singlemultiplebirths);
//        spinner_modeoftransport = findViewById(R.id.spinner_modeoftransport);
//        spinner_experiencerscore = findViewById(R.id.spinner_experiencerscore);
//        spinner_pregnantpasttwoyrs = findViewById(R.id.spinner_pregnantpasttwoyrs);
//        spinner_outcomepregnancy = findViewById(R.id.spinner_outcomepregnancy);
//        spinner_placeofdeliverypregnant = findViewById(R.id.spinner_placeofdeliverypregnant);
//        spinner_focalPointBlock = findViewById(R.id.spinner_focal_block);
//        spinner_focalPointVillage = findViewById(R.id.spinner_focal_village);

       /* spinner_pregnantpasttwoyrs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    binding.linearNoOfPregrnancyOutcome.setVisibility(View.VISIBLE);
                   /* if(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equals("") ||
                            binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty()) {
                        binding.linearButtonCardBlock.setVisibility(View.GONE);
                    }
                    else {
                        binding.linearButtonCardBlock.setVisibility(View.VISIBLE);
                    }*/
      /*          } else {
                    binding.linearNoOfPregrnancyOutcome.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        spinner_outcomepregnancy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position != 0) {
//                    if (position == 1) {
//                        binding.llChildAlive.setVisibility(View.VISIBLE);
//                    } else {
//                        binding.llChildAlive.setVisibility(View.GONE);
//                        binding.edittextBabyagedied.setVisibility(View.GONE);
//                        spinner_childalive.setSelection(0);
//                    }
//
//                    if (position == 5) {
//                        binding.edittextMonthspregnancylast.setVisibility(View.GONE);
//                    } else {
//                        binding.edittextMonthspregnancylast.setVisibility(View.VISIBLE);
//                    }
//
//                    if (position == 5) {
//                        binding.edittextMonthsbeingpregnant.setVisibility(View.VISIBLE);
//                    } else {
//                        binding.edittextMonthsbeingpregnant.setVisibility(View.GONE);
//                    }
//
//                    if (position == 4 || position == 5) {
//                        binding.llDeliveryPlace.setVisibility(View.GONE);
//                    } else {
//                        binding.llDeliveryPlace.setVisibility(View.VISIBLE);
//                    }
//
//
//                    if (position == 3 || position == 4 || position == 5) {
//                        binding.llFocalPoint.setVisibility(View.GONE);
//                        binding.llSingleMultipleBirth.setVisibility(View.GONE);
//                        binding.llBabyGender.setVisibility(View.GONE);
//                        binding.llChildComplications.setVisibility(View.GONE);
//                        //  binding.edittextBabyagedied.setVisibility(View.GONE);
//                    } else {
//                        binding.llSingleMultipleBirth.setVisibility(View.VISIBLE);
//                        binding.llBabyGender.setVisibility(View.VISIBLE);
//                        binding.llChildComplications.setVisibility(View.VISIBLE);
//                        binding.llFocalPoint.setVisibility(View.VISIBLE);
//
//                        //todo for place of deleivery is home so fockl is not shown at that time
//                        if (spinner_placeofdeliverypregnant.getSelectedItemPosition() == 1) {
//                            spinner_placeofdeliverypregnant.setSelection(0);
//
//                        }
//                        // binding.edittextBabyagedied.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        spinner_childalive = findViewById(R.id.spinner_childalive);
//        spinner_childalive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                if (position == 2)
//                    edittext_babyagedied.setVisibility(View.VISIBLE);
//                else
//                    edittext_babyagedied.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        spinner_block = findViewById(R.id.spinner_block);
        spinner_village = findViewById(R.id.spinner_village);

        spinner_block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        spinner_village.setVisibility(View.VISIBLE);
                        et_block_other.setVisibility(View.GONE);
                        et_village_other.setVisibility(View.GONE);
                        et_block_other.setText("");
                        et_village_other.setText("");

                        String focalVillagePeth_Language = "peth_block_village_" + sessionManager.getAppLanguage();
                        int focalVillage_Peth_id = getResources().getIdentifier(focalVillagePeth_Language, "array", getApplicationContext().getPackageName());
                        if (focalVillage_Peth_id != 0) {
                            adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                    focalVillage_Peth_id, android.R.layout.simple_spinner_dropdown_item);
                        }
                        adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                focalVillage_Peth_id, R.layout.custom_spinner);
                        spinner_village.setAdapter(adapter_FocalVillage_Peth);

                        try {
                            String village_Peth_Transl = "";
                            village_Peth_Transl = getPethBlockVillage_edit(patient1.getVillageNameSurvey(), sessionManager.getAppLanguage());
                            int spinner_peth_position = adapter_FocalVillage_Peth.getPosition(village_Peth_Transl);
                            spinner_village.setSelection(spinner_peth_position);
                        } catch (NullPointerException exception) {
                            exception.printStackTrace();
                        }
                        break;

                    case 2:
                        spinner_village.setVisibility(View.VISIBLE);
                        et_block_other.setVisibility(View.GONE);
                        et_village_other.setVisibility(View.GONE);
                        et_block_other.setText("");
                        et_village_other.setText("");

                        String focalVillageSurgane_Language = "suragana_block_villages_" + sessionManager.getAppLanguage();
                        int focalVillage_Surgane_id = getResources().getIdentifier(focalVillageSurgane_Language, "array", getApplicationContext().getPackageName());
                        if (focalVillage_Surgane_id != 0) {
                            adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                    focalVillage_Surgane_id, android.R.layout.simple_spinner_dropdown_item);
                        }
                        adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                focalVillage_Surgane_id, R.layout.custom_spinner);
                        spinner_village.setAdapter(adapter_FocalVillage_Surgana);

                        try {
                            String village_Surgane_Transl = "";
                            village_Surgane_Transl = getPethBlockVillage_edit(patient1.getVillageNameSurvey(), sessionManager.getAppLanguage());
                            int spinner_surgana_position = adapter_FocalVillage_Surgana.getPosition(village_Surgane_Transl);
                            spinner_village.setSelection(spinner_surgana_position);
                        } catch (NullPointerException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case 3:
                        spinner_village.setVisibility(View.GONE);
                        spinner_village.setSelection(0);
                        textinputlayout_blockVillageOther.setVisibility(View.VISIBLE);
                        et_block_other.setVisibility(View.VISIBLE);
                        et_village_other.setVisibility(View.VISIBLE);
                        break;

                    default:
//                        spinner_village.setAdapter(null);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }); */

//        spinner_placeofdeliverypregnant = findViewById(R.id.spinner_placeofdeliverypregnant);
//        spinner_placeofdeliverypregnant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                if (pos == 1) {
//                    binding.llFocalPoint.setVisibility(View.GONE);
//                } else {
//                    binding.llFocalPoint.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

//        spinner_focalPointBlock = findViewById(R.id.spinner_focal_block);
//        spinner_focalPointVillage = findViewById(R.id.spinner_focal_village);

//        spinner_focalPointBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (patientID_edit == null) {
//                    switch (position) {
//                        case 1:
//                            String focalVillagePeth_Language = "peth_block_village_" + sessionManager.getAppLanguage();
//                            int focalVillage_Peth_id = getResources().getIdentifier(focalVillagePeth_Language, "array", getApplicationContext().getPackageName());
//                            if (focalVillage_Peth_id != 0) {
//                                adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                        focalVillage_Peth_id, android.R.layout.simple_spinner_dropdown_item);
//                            }
//                            adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                    focalVillage_Peth_id, R.layout.custom_spinner);
//                            spinner_focalPointVillage.setAdapter(adapter_FocalVillage_Peth);
//                            spinner_focalPointVillage.setVisibility(View.VISIBLE);
//                            break;
//
//                        case 2:
//                            String focalVillageSurgane_Language = "suragana_block_villages_" + sessionManager.getAppLanguage();
//                            int focalVillage_Surgane_id = getResources().getIdentifier(focalVillageSurgane_Language, "array", getApplicationContext().getPackageName());
//                            if (focalVillage_Surgane_id != 0) {
//                                adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                        focalVillage_Surgane_id, android.R.layout.simple_spinner_dropdown_item);
//                            }
//                            adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                    focalVillage_Surgane_id, R.layout.custom_spinner);
//                            spinner_focalPointVillage.setAdapter(adapter_FocalVillage_Surgana);
//                            spinner_focalPointVillage.setVisibility(View.VISIBLE);
//                            break;
//
//                        default:
//                            spinner_focalPointVillage.setVisibility(View.GONE);
//                    }
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


//        spinner_sexofbaby = findViewById(R.id.spinner_sexofbaby);
//        spinner_pregnancyplanned = findViewById(R.id.spinner_pregnancyplanned);
//        spinner_pregnancyhighriskcase = findViewById(R.id.spinner_pregnancyhighriskcase);
//        spinner_pregnancycomplications = findViewById(R.id.spinner_pregnancycomplications);
        //Roaster Spinner End

        // Roster EditText
        // TODO: Add filters
//        edittext_noofepisodes = findViewById(R.id.edittext_noofepisodes);
//        edittext_avgcosttravel = findViewById(R.id.edittext_avgcosttravel);
//        edittext_avgcostconsult = findViewById(R.id.edittext_avgcostconsult);
//        edittext_avgcostmedicines = findViewById(R.id.edittext_avgcostmedicines);
//        edittext_howmanytimmespregnant = findViewById(R.id.edittext_howmanytimmespregnant);
//        edittext_yearofpregnancy = findViewById(R.id.edittext_yearofpregnancy);
//        edittext_monthspregnancylast = findViewById(R.id.edittext_monthspregnancylast);
//        edittext_monthsbeingpregnant = findViewById(R.id.edittext_monthsbeingpregnant);
//        edittext_babyagedied = findViewById(R.id.edittext_babyagedied);
        //Roster EditText
//        llPORoaster = findViewById(R.id.llPORoaster);
//        ll18 = findViewById(R.id.ll18);
    }

   /* private void setupHealthCard() {
//        if (fragmentList.isEmpty()) {
//            binding.editHealthIssueButton.setVisibility(View.GONE);
//        } else {
//            binding.editHealthIssueButton.setVisibility(View.VISIBLE);
//        }

        binding.addHealthIssueButton.setOnClickListener(v -> {
            dialog = new MultipleDiseasesDialog();
            dialog.show(getSupportFragmentManager(), MultipleDiseasesDialog.TAG);
        });

//        binding.editHealthIssueButton.setOnClickListener(v -> {
//            editSurveyData();
//        });
    }

    private void setupPOCard() {
        //TODO: Validations
        if (patientID_edit == null)
            sessionManager.setNoOfclicks(0);


        binding.addPregnancyOutcomeButton.setOnClickListener(v -> {
            if (!binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty() &&
                    !binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equals("") &&
                    pregnancyOutcomesList.size() < Integer.parseInt(binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString())) {

                PregnancyRosterDialog dialog = new PregnancyRosterDialog(
                        sessionManager.getNoOfclicks(),
                        edittext_howmanytimmespregnant.getText().toString(),
                        StringUtils.getPasttwoyrs(spinner_pregnantpasttwoyrs.getSelectedItem().toString(), sessionManager.getAppLanguage()),
                        binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString()); //TODO: support transaltions...

                dialog.show(getSupportFragmentManager(), PregnancyRosterDialog.TAG);
            } else {
                if (!binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().isEmpty() &&
                        !binding.edittextNoOfPregnancyOutcomePastTwoYrs.getText().toString().equals("")) {
                    Toast.makeText(context, R.string.no_of_times_pasttwoyrs_limit_toast, Toast.LENGTH_SHORT).show();
                } else {
                    //
                }

            }
        });
    } */

   /* private void editSurveyData() {
        int position = binding.mainViewPager.getCurrentItem();
        Logger.logD("Position", String.valueOf(position));
        HealthIssues healthIssues = healthIssuesList.get(position);

        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
//        bundle.putString("householdMemberName", healthIssues.getHouseholdMemberName());
        bundle.putString("healthIssueReported", healthIssues.getHealthIssueReported());
        bundle.putString("numberOfEpisodesInTheLastYear", healthIssues.getNumberOfEpisodesInTheLastYear());
        bundle.putString("primaryHealthcareProviderValue", healthIssues.getPrimaryHealthcareProviderValue());
        bundle.putString("firstLocationOfVisit", healthIssues.getFirstLocationOfVisit());
        bundle.putString("referredTo", healthIssues.getReferredTo());
        bundle.putString("modeOfTransportation", healthIssues.getModeOfTransportation());
        bundle.putString("averageCostOfTravelAndStayPerEpisode", healthIssues.getAverageCostOfTravelAndStayPerEpisode());
        bundle.putString("averageCostOfConsultation", healthIssues.getAverageCostOfConsultation());
        bundle.putString("averageCostOfMedicine", healthIssues.getAverageCostOfMedicine());
        bundle.putString("scoreForExperienceOfTreatment", healthIssues.getScoreForExperienceOfTreatment());

        MultipleDiseasesDialog dialog = new MultipleDiseasesDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), MultipleDiseasesDialog.TAG);
    } */

   /* private void setViewPagerOffset(ViewPager2 viewPager2) {
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);

        int pageMarginPx = getResources().getDimensionPixelOffset(R.dimen.pageMargin);
        float offsetPx = getResources().getDimensionPixelOffset(R.dimen.offset);
        viewPager2.setPageTransformer((page, position) -> {
            ViewPager2 viewPager = (ViewPager2) page.getParent().getParent();
            float offset = position * -(2 * offsetPx + pageMarginPx);
            if (viewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.setTranslationX(-offset);
                } else {
                    page.setTranslationX(offset);
                }
            } else {
                page.setTranslationY(offset);
            }
        });
    }

    private void updateRoaster() {
        if (mGenderF.isChecked() && mAgeYears >= 15 && mAgeYears <= 50) {
            llPORoaster.setVisibility(View.VISIBLE);
        } else {
            llPORoaster.setVisibility(View.GONE);
        }

        if (mAgeYears >= 18) {
            ll18.setVisibility(View.VISIBLE);
        } else {
            ll18.setVisibility(View.GONE);
        }
    } */

   /* private void editRosterQuestionsUIHandling() {

        if (patient1.getBlockSurvey() != null && !patient1.getBlockSurvey().equalsIgnoreCase("")) {
            String block_Transl = "";
            block_Transl = getPethBlock_edit(patient1.getBlockSurvey(), sessionManager.getAppLanguage());
            int spinner_position = adapter_block.getPosition(block_Transl);
            Logger.logD("Position", String.valueOf(spinner_position));
            if (spinner_position == -1) {
                spinner_block.setSelection(3);
                spinner_village.setVisibility(View.GONE);
                et_block_other.setVisibility(View.VISIBLE);
                et_village_other.setVisibility(View.VISIBLE);
                et_block_other.setText(patient1.getBlockSurvey());
                et_village_other.setText(patient1.getVillageNameSurvey());
            } else {
                spinner_block.setSelection(spinner_position);
                spinner_village.setVisibility(View.VISIBLE);
                et_block_other.setVisibility(View.GONE);
                et_village_other.setVisibility(View.GONE);
            }

        }

        if (patient1.getRelationshiphoh() != null && !patient1.getRelationshiphoh().equalsIgnoreCase("")) {
            String relationhoh_Transl = "";
            relationhoh_Transl = getRelationShipHoH_edit(patient1.getRelationshiphoh(), sessionManager.getAppLanguage());
            Log.d("ritika", "1164" + relationhoh_Transl);
            int spinner_position = adapter_whatisyourrelation.getPosition(relationhoh_Transl);

            if (spinner_position == -1) {
                til_whatisyourrelation_other.setVisibility(View.VISIBLE);
//                et_whatisyourrelation_other.setVisibility(View.VISIBLE);
                spinner_whatisyourrelation.setSelection(16);
                et_whatisyourrelation_other.setText(relationhoh_Transl);
            } else {
                til_whatisyourrelation_other.setVisibility(View.GONE);
//                et_whatisyourrelation_other.setVisibility(View.GONE);
                spinner_whatisyourrelation.setSelection(spinner_position);
            }
        }
        //Relations ship HOH

        //maritualstatus
        if (patient1.getMaritualstatus() != null && !patient1.getMaritualstatus().equalsIgnoreCase("")) {
            String maritualstatus_Transl = "";
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                maritualstatus_Transl = StringUtils.switch_as_caste_edit(patient1.getMaritualstatus());
//                // TODO: Add switch case in StringUtils
//            } else {
//                maritualstatus_Transl = patient1.getMaritualstatus();
//            }
            maritualstatus_Transl = StringUtils.getMaritual_edit(patient1.getMaritualstatus(), sessionManager.getAppLanguage());
            int spinner_position = adapter_maritualstatus.getPosition(maritualstatus_Transl);
            spinner_maritualstatus.setSelection(spinner_position);
        }
        //maritualstatus

        //phoneowner
        if (patient1.getPhoneownership() != null && !patient1.getPhoneownership().equalsIgnoreCase("")) {
            String phoneowner_Transl = "";
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                phoneowner_Transl = StringUtils.switch_as_caste_edit(patient1.getPhoneownership());
//                // TODO: Add switch case in StringUtils
//            } else {
//                phoneowner_Transl = patient1.getPhoneownership();
//            }
            phoneowner_Transl = getPhoneOwnerShip_edit(patient1.getPhoneownership(), sessionManager.getAppLanguage());
            int spinner_position = adapter_phoneownership.getPosition(phoneowner_Transl);
            spinner_phoneownership.setSelection(spinner_position);
        }
        //phoneowner

        //bp
        if (patient1.getBpchecked() != null && !patient1.getBpchecked().equalsIgnoreCase("")) {
            String bp_Transl = "";
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                bp_Transl = StringUtils.switch_as_caste_edit(patient1.getBpchecked());
//                // TODO: Add switch case in StringUtils
//            } else {
//                bp_Transl = patient1.getBpchecked();
//            }

            bp_Transl = getBP_edit(patient1.getBpchecked(), sessionManager.getAppLanguage());
            Log.d("1113,", "bp" + bp_Transl);
            int spinner_position = adapter_bpchecked.getPosition(bp_Transl);
            Log.d("115,", "bpmm" + spinner_position);
            spinner_bpchecked.setSelection(spinner_position);
        }

        if (patient1.getOccupation() != null && !patient1.getOccupation().equalsIgnoreCase("")) {
            String occupation_Transl = "";
            occupation_Transl = getOccupationsIdentification_Edit(patient1.getOccupation(), sessionManager.getAppLanguage());
            int spinner_position = occupationAdapter.getPosition(occupation_Transl);
            if (spinner_position == -1) {
                mOccupation.setSelection(13);
                et_occupation_other.setVisibility(View.VISIBLE);
                et_occupation_other.setText(patient1.getOccupation());
            } else {
                mOccupation.setSelection(spinner_position);
            }
        }

        //sugar
        if (patient1.getSugarchecked() != null && !patient1.getSugarchecked().equalsIgnoreCase("")) {
            String sugar_Transl = "";
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                sugar_Transl = StringUtils.switch_as_caste_edit(patient1.getSugarchecked());
//                // TODO: Add switch case in StringUtils
//            } else {
//                sugar_Transl = patient1.getSugarchecked();
//            }
            sugar_Transl = getSuger_edit(patient1.getSugarchecked(), sessionManager.getAppLanguage());
            int spinner_position = adapter_sugarchecked.getPosition(sugar_Transl);
            spinner_sugarchecked.setSelection(spinner_position);
        }
        //sugar

        //hb
        if (patient1.getHbtest() != null && !patient1.getHbtest().equalsIgnoreCase("")) {
            String hb_Transl = "";
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                hb_Transl = StringUtils.switch_as_caste_edit(patient1.getHbtest());
//                // TODO: Add switch case in StringUtils
//            } else {
//                hb_Transl = patient1.getHbtest();
//            }
            hb_Transl = getHB_edit(patient1.getHbtest(), sessionManager.getAppLanguage());
            int spinner_position = adapter_hbchecked.getPosition(hb_Transl);
            spinner_hbchecked.setSelection(spinner_position);
        }
        //hb

        //bmi
        if (patient1.getBmi() != null && !patient1.getBmi().equalsIgnoreCase("")) {
            String bmi_Transl = "";
//            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                bmi_Transl = StringUtils.switch_as_caste_edit(patient1.getBmi());
//                // TODO: Add switch case in StringUtils
//            } else {
//                bmi_Transl = patient1.getBmi();
//            }
            bmi_Transl = getBMI_edit(patient1.getBmi(), sessionManager.getAppLanguage());
            int spinner_position = adapter_bmi.getPosition(bmi_Transl);
            spinner_bmi.setSelection(spinner_position);
        }
        //bmi

        //healthissuereported
//        if (patient1.getHealthissuereported() != null && !patient1.getHealthissuereported().equalsIgnoreCase("")) {
//            String healthissuereported_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                healthissuereported_Transl = StringUtils.switch_as_caste_edit(patient1.getHealthissuereported());
////                // TODO: Add switch case in StringUtils
////            } else {
////                healthissuereported_Transl = patient1.getHealthissuereported();
////            }
//            healthissuereported_Transl = getHealthIsReported_edit(patient1.getHealthissuereported(),sessionManager.getAppLanguage());
//
//            int spinner_position = adapter_healthissuereported.getPosition(healthissuereported_Transl);
//            spinner_healthissuereported.setSelection(spinner_position);
//        }
        //healthissuereported

        //no episodes
//        edittext_noofepisodes.setText(patient1.getNoepisodes());
        //no episodes

        //primaryhealthprovider
//        if (patient1.getPrimaryhealthprovider() != null && !patient1.getPrimaryhealthprovider().equalsIgnoreCase("")) {
//            String primaryhealthprovider_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                primaryhealthprovider_Transl = StringUtils.switch_as_caste_edit(patient1.getPrimaryhealthprovider());
////                // TODO: Add switch case in StringUtils
////            } else {
////                primaryhealthprovider_Transl = patient1.getPrimaryhealthprovider();
////            }
//
//
//            primaryhealthprovider_Transl = getPrimeryHealthProvider_edit(patient1.getPrimaryhealthprovider(),sessionManager.getAppLanguage());
//
//            int spinner_position = adapter_primaryhealthprovider.getPosition(primaryhealthprovider_Transl);
//            spinner_primaryhealthprovider.setSelection(spinner_position);
//        }
        //primaryhealthprovider

        //firstlocation
//        if (patient1.getFirstlocation() != null && !patient1.getFirstlocation().equalsIgnoreCase("")) {
//            String firstlocation_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                firstlocation_Transl = StringUtils.switch_as_caste_edit(patient1.getFirstlocation());
////                // TODO: Add switch case in StringUtils
////            } else {
////                firstlocation_Transl = patient1.getFirstlocation();
////            }
//            firstlocation_Transl=getFirstLocation_edit(patient1.getFirstlocation(),sessionManager.getAppLanguage());
//
//            int spinner_position = adapter_firstlocation.getPosition(firstlocation_Transl);
//            spinner_firstlocation.setSelection(spinner_position);
//        }
        //firstlocation

        //referredto
//        if (patient1.getReferredto() != null && !patient1.getReferredto().equalsIgnoreCase("")) {
//            String referredto_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                referredto_Transl = StringUtils.switch_as_caste_edit(patient1.getReferredto());
////                // TODO: Add switch case in StringUtils
////            } else {
////                referredto_Transl = patient1.getReferredto();
////            }
//            referredto_Transl = getReferedDTO_edit(patient1.getReferredto(),sessionManager.getAppLanguage());
//            int spinner_position = adapter_referredto.getPosition(referredto_Transl);
//            spinner_referredto.setSelection(spinner_position);
//        }
        //referredto

        //modetransport
////        if (patient1.getModetransport() != null && !patient1.getModetransport().equalsIgnoreCase("")) {
////            String modetransport_Transl = "";
//////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//////                modetransport_Transl = StringUtils.switch_as_caste_edit(patient1.getModetransport());
//////                // TODO: Add switch case in StringUtils
//////            } else {
//////                modetransport_Transl = patient1.getModetransport();
//////            }
////
////            modetransport_Transl = getModerateSport_edit(patient1.getModetransport(),sessionManager.getAppLanguage());
////            int spinner_position = adapter_modeoftransport.getPosition(modetransport_Transl);
////            spinner_modeoftransport.setSelection(spinner_position);
////        }
//        //modetransport
//
//        //EditText
//        edittext_avgcosttravel.setText(patient1.getCosttravel());
//        edittext_avgcostconsult.setText(patient1.getCostconsult());
//        edittext_avgcostmedicines.setText(patient1.getCostmedicines());
        //EditText

        //scoreofexperience
//        if (patient1.getScoreexperience() != null && !patient1.getScoreexperience().equalsIgnoreCase("")) {
//            String scoreofexperience_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                scoreofexperience_Transl = StringUtils.switch_as_caste_edit(patient1.getScoreexperience());
////                // TODO: Add switch case in StringUtils
////            } else {
////                scoreofexperience_Transl = patient1.getScoreexperience();
////            }
//            scoreofexperience_Transl = getScoreExperience_edit(patient1.getScoreexperience(),sessionManager.getAppLanguage());
//            int spinner_position = adapter_experiencerscore.getPosition(scoreofexperience_Transl);
//            spinner_experiencerscore.setSelection(spinner_position);
//        }
        //scoreofexperience

        // how many times & no of pregnancy outcome in 2yrs
        if (patient1.getTimespregnant() != null && !patient1.getTimespregnant().equalsIgnoreCase("-"))
            edittext_howmanytimmespregnant.setText(patient1.getTimespregnant());

        if (patient1.getNoOfPregnancyOutcomeTwoYrs() != null && !patient1.getNoOfPregnancyOutcomeTwoYrs().equalsIgnoreCase("-"))
            binding.edittextNoOfPregnancyOutcomePastTwoYrs.setText(patient1.getNoOfPregnancyOutcomeTwoYrs());
        // how many times & no of pregnancy outcome in 2yrs

        //pasttwoyrs
        if (patient1.getPasttwoyrs() != null && !patient1.getPasttwoyrs().equalsIgnoreCase("")) {
            String pasttwoyrs_Transl = "";
            pasttwoyrs_Transl = getPasttwoyrs_edit(patient1.getPasttwoyrs(), sessionManager.getAppLanguage());
            int spinner_position = adapter_pregnantpasttwoyrs.getPosition(pasttwoyrs_Transl);
            spinner_pregnantpasttwoyrs.setSelection(spinner_position);
        }
//        //pasttwoyrs

        //outcomeofpreg
//        if (patient1.getOutcomepregnancy() != null && !patient1.getOutcomepregnancy().equalsIgnoreCase("")) {
//            String outcomeofpreg_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                outcomeofpreg_Transl = StringUtils.switch_as_caste_edit(patient1.getOutcomepregnancy());
////                // TODO: Add switch case in StringUtils
////            } else {
////                outcomeofpreg_Transl = patient1.getOutcomepregnancy();
////            }
//            outcomeofpreg_Transl = getOvercomePragnency_edit(patient1.getOutcomepregnancy(), sessionManager.getAppLanguage());
//            int spinner_position = adapter_outcomepregnancy.getPosition(outcomeofpreg_Transl);
//            spinner_outcomepregnancy.setSelection(spinner_position);
//        }
        //outcomeofpreg

        //childalive
//        if (patient1.getChildalive() != null && !patient1.getChildalive().equalsIgnoreCase("")) {
//            String childalive_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                childalive_Transl = StringUtils.switch_as_caste_edit(patient1.getChildalive());
////                // TODO: Add switch case in StringUtils
////            } else {
////                childalive_Transl = patient1.getChildalive();
////            }
//            childalive_Transl = getChildAlive_edit(patient1.getChildalive(), sessionManager.getAppLanguage());
//            int spinner_position = adapter_childalive.getPosition(childalive_Transl);
//            spinner_childalive.setSelection(spinner_position);
//        }
        //childalive

        //EditText
//        edittext_yearofpregnancy.setText(patient1.getYearsofpregnancy());
//        edittext_monthspregnancylast.setText(patient1.getLastmonthspregnancy());
//        edittext_monthsbeingpregnant.setText(patient1.getMonthsofpregnancy());
        //EditText

        //placedelivery
//        if (patient1.getPlacedelivery() != null && !patient1.getPlacedelivery().equalsIgnoreCase("")) {
//            String placedelivery_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                placedelivery_Transl = StringUtils.switch_as_caste_edit(patient1.getPlacedelivery());
////                // TODO: Add switch case in StringUtils
////            } else {
////                placedelivery_Transl = patient1.getPlacedelivery();
////            }
//            placedelivery_Transl = getPlaceDelivery_edit(patient1.getPlacedelivery(), sessionManager.getAppLanguage());
//            int spinner_position = adapter_placeofdeliverypregnant.getPosition(placedelivery_Transl);
//            spinner_placeofdeliverypregnant.setSelection(spinner_position);
//        }
        //placedelivery

        //focal
        if (patient1.getFocalfacility() != null && !patient1.getFocalfacility().equalsIgnoreCase("")) {
            String focal_Transl = "";
            String focalBlockTransl = "", focalVillageTransl = "", StringBlock = "", StingVillage = "";

            String[] block_village_split = patient1.getFocalfacility().split(":");
            StringBlock = block_village_split[0]; // This contains Block selected in Spinner
//            StingVillage = block_village_split[1]; // This contains Village selected in Spinner

            focalBlockTransl = getFocalFacility_Block_edit(StringBlock, sessionManager.getAppLanguage());
            focalVillageTransl = getFocalFacility_Village_edit(StingVillage, sessionManager.getAppLanguage());

            int spinner_positionBlock = adapter_focalPointBlock.getPosition(focalBlockTransl);
            spinner_focalPointBlock.setSelection(spinner_positionBlock);

           /* int spinner_positionVillage = adapter_focalPointVillage.getPosition(focalVillageTransl);
            spinner_focalPointVillage.setSelection(spinner_positionVillage);*/
//
//            switch (spinner_positionBlock) {
//                case 1:
//                    String focalVillagePeth_Language = "peth_block_village_" + sessionManager.getAppLanguage();
//                    int focalVillage_Peth_id = getResources().getIdentifier(focalVillagePeth_Language, "array", getApplicationContext().getPackageName());
//                    if (focalVillage_Peth_id != 0) {
//                        adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                focalVillage_Peth_id, android.R.layout.simple_spinner_dropdown_item);
//                    }
//                    adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            focalVillage_Peth_id, R.layout.custom_spinner);
//                    spinner_focalPointVillage.setAdapter(adapter_FocalVillage_Peth);
//                    spinner_focalPointVillage.setVisibility(View.VISIBLE);
//                    int spinner_positionVillage = adapter_FocalVillage_Peth.getPosition(focalVillageTransl);
//                    spinner_focalPointVillage.setSelection(spinner_positionVillage);
//                    break;
//
//                case 2:
//                    String focalVillageSurgane_Language = "suragana_block_villages_" + sessionManager.getAppLanguage();
//                    int focalVillage_Surgane_id = getResources().getIdentifier(focalVillageSurgane_Language, "array", getApplicationContext().getPackageName());
//                    if (focalVillage_Surgane_id != 0) {
//                        adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                focalVillage_Surgane_id, android.R.layout.simple_spinner_dropdown_item);
//                    }
//                    adapter_FocalVillage_Surgana = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            focalVillage_Surgane_id, R.layout.custom_spinner);
//                    spinner_focalPointVillage.setAdapter(adapter_FocalVillage_Surgana);
//                    spinner_focalPointVillage.setVisibility(View.VISIBLE);
//
//                    int spinner_village = adapter_FocalVillage_Surgana.getPosition(focalVillageTransl);
//                    spinner_focalPointVillage.setSelection(spinner_village);
//
//                    break;
//
//                default:
//                    spinner_focalPointVillage.setVisibility(View.GONE);
//            }
//        }
//        //focal

    //Single/Multiple
//        if (patient1.getSinglemultiplebirth() != null && !patient1.getSinglemultiplebirth().equalsIgnoreCase("")) {
//            String singlemultiple_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                singlemultiple_Transl = StringUtils.switch_as_caste_edit(patient1.getSinglemultiplebirth());
////                // TODO: Add switch case in StringUtils
////            } else {
////                singlemultiple_Transl = patient1.getSinglemultiplebirth();
////            }
//            singlemultiple_Transl = getSinglemultiplebirths_edit(patient1.getSinglemultiplebirth(), sessionManager.getAppLanguage());
//
//            Log.d("1437", "singlemultiplebirths " + patient1.getSinglemultiplebirth());
//            Log.d("1438", "singlemultiplebirths " + singlemultiple_Transl);
//
//            int spinner_position = adapter_singlemultiplebirths.getPosition(singlemultiple_Transl);
//            spinner_singlemultiplebirths.setSelection(spinner_position);
//        }
    //Single/Multiple

    //focal

    //focal

    //sexofbaby
//        if (patient1.getSexofbaby() != null && !patient1.getSexofbaby().equalsIgnoreCase("")) {
//            String sexofbaby_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                sexofbaby_Transl = StringUtils.switch_as_caste_edit(patient1.getSexofbaby());
////                // TODO: Add switch case in StringUtils
////            } else {
////                sexofbaby_Transl = patient1.getSexofbaby();
////            }
//
//            sexofbaby_Transl = getSexOfBaby_edit(patient1.getSexofbaby(), sessionManager.getAppLanguage());
//            int spinner_position = adapter_sexofbaby.getPosition(sexofbaby_Transl);
//            spinner_sexofbaby.setSelection(spinner_position);
//        }
    //sexofbaby

    //baby age died
//        edittext_babyagedied.setText(patient1.getAgediedbaby());
    //baby age died

    //pregplanned
//        if (patient1.getPlannedpregnancy() != null && !patient1.getPlannedpregnancy().equalsIgnoreCase("")) {
//            String pregplanned_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                pregplanned_Transl = StringUtils.switch_as_caste_edit(patient1.getPlannedpregnancy());
////                // TODO: Add switch case in StringUtils
////            } else {
////                pregplanned_Transl = patient1.getPlannedpregnancy();
////            }
//            pregplanned_Transl = getPregnancyPlanned_edit(patient1.getPlannedpregnancy(), sessionManager.getAppLanguage());
//            int spinner_position = adapter_pregnancyplanned.getPosition(pregplanned_Transl);
//            spinner_pregnancyplanned.setSelection(spinner_position);
//        }
    //pregplanned

    //highriskpreg
//        if (patient1.getHighriskpregnancy() != null && !patient1.getHighriskpregnancy().equalsIgnoreCase("")) {
//            String highriskpreg_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                highriskpreg_Transl = StringUtils.switch_as_caste_edit(patient1.getHighriskpregnancy());
////                // TODO: Add switch case in StringUtils
////            } else {
////                highriskpreg_Transl = patient1.getHighriskpregnancy();
////            }
//            highriskpreg_Transl = getHeighPregnancyPlanned_edit(patient1.getHighriskpregnancy(), sessionManager.getAppLanguage());
//            int spinner_position = adapter_pregnancyhighriskcase.getPosition(highriskpreg_Transl);
//            spinner_pregnancyhighriskcase.setSelection(spinner_position);
//        }
    //highriskpreg

    //complications
//        if (patient1.getComplications() != null && !patient1.getComplications().equalsIgnoreCase("")) {
//            String complications_Transl = "";
////            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
////                complications_Transl = StringUtils.switch_as_caste_edit(patient1.getComplications());
////                // TODO: Add switch case in StringUtils
////            } else {
////                complications_Transl = patient1.getComplications();
////            }
//            complications_Transl = getComplications_edit(patient1.getComplications(), sessionManager.getAppLanguage());
//
//            int spinner_position = adapter_pregnancycomplications.getPosition(complications_Transl);
//            spinner_pregnancycomplications.setSelection(spinner_position);
//        }
    //complications
//    }
//
//
//    private void roaster_spinnerAdapter() {
//        //Spinner - Start
//        //Relationsship Spinner adapter
//        Resources res = getResources();
//        try {
//            if (!sessionManager.getHouseholdUuid().equals("") && mRelationshipValue.equalsIgnoreCase("Self")) {
//                String relationshiphohLanguage = "relationshipHoH_Self_" + sessionManager.getAppLanguage();
//                int relationshiphoh_id = res.getIdentifier(relationshiphohLanguage, "array", getApplicationContext().getPackageName());
//                if (relationshiphoh_id != 0) {
//                    adapter_whatisyourrelation = ArrayAdapter.createFromResource(this,
//                            relationshiphoh_id, android.R.layout.simple_spinner_dropdown_item);
//                }
//                spinner_whatisyourrelation.setAdapter(adapter_whatisyourrelation);
//            } else { // Here removing Self from the spinner... since HOH is already selected...
//                String relationshiphohLanguage = "relationshipHoH_" + sessionManager.getAppLanguage();
//                int relationshiphoh_id = res.getIdentifier(relationshiphohLanguage, "array", getApplicationContext().getPackageName());
//                if (relationshiphoh_id != 0) {
//                    adapter_whatisyourrelation = ArrayAdapter.createFromResource(this,
//                            relationshiphoh_id, android.R.layout.simple_spinner_dropdown_item);
//                }
//                spinner_whatisyourrelation.setAdapter(adapter_whatisyourrelation);
//            }
//
//        } catch (Exception e) {
//            Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //relationship spinner adapter
//
//        //maritual Spinner adapter
//        try {
//            String maritualLanguage = "maritual_" + sessionManager.getAppLanguage();
//            int maritual_id = res.getIdentifier(maritualLanguage, "array", getApplicationContext().getPackageName());
//            if (maritual_id != 0) {
//                adapter_maritualstatus = ArrayAdapter.createFromResource(this,
//                        maritual_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_maritualstatus.setAdapter(adapter_maritualstatus);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //maritual spinner adapter
//
//        //phone Spinner adapter
//        try {
//            String phoneownerLanguage = "phoneownership_" + sessionManager.getAppLanguage();
//            int phoneowner_id = res.getIdentifier(phoneownerLanguage, "array", getApplicationContext().getPackageName());
//            if (phoneowner_id != 0) {
//                adapter_phoneownership = ArrayAdapter.createFromResource(this,
//                        phoneowner_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_phoneownership.setAdapter(adapter_phoneownership);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //phone spinner adapter
//
//        //bp Spinner adapter
//        try {
//            String bpLanguage = "bp_" + sessionManager.getAppLanguage();
//            int bp_id = res.getIdentifier(bpLanguage, "array", getApplicationContext().getPackageName());
//            if (bp_id != 0) {
//                adapter_bpchecked = ArrayAdapter.createFromResource(this,
//                        bp_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_bpchecked.setAdapter(adapter_bpchecked);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //bp spinner adapter
//
//        //sugar Spinner adapter
//        try {
//            String sugarLanguage = "sugar_" + sessionManager.getAppLanguage();
//            int sugar_id = res.getIdentifier(sugarLanguage, "array", getApplicationContext().getPackageName());
//            if (sugar_id != 0) {
//                adapter_sugarchecked = ArrayAdapter.createFromResource(this,
//                        sugar_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_sugarchecked.setAdapter(adapter_sugarchecked);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //sugar spinner adapter
//
//        //HB Spinner adapter
//        try {
//            String hbLanguage = "hb_" + sessionManager.getAppLanguage();
//            int hb_id = res.getIdentifier(hbLanguage, "array", getApplicationContext().getPackageName());
//            if (hb_id != 0) {
//                adapter_hbchecked = ArrayAdapter.createFromResource(this,
//                        hb_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_hbchecked.setAdapter(adapter_hbchecked);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //HB spinner adapter
//
//        //BMI Spinner adapter
//        try {
//            String bmiLanguage = "bmi_" + sessionManager.getAppLanguage();
//            int bmi_id = res.getIdentifier(bmiLanguage, "array", getApplicationContext().getPackageName());
//            if (bmi_id != 0) {
//                adapter_bmi = ArrayAdapter.createFromResource(this,
//                        bmi_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_bmi.setAdapter(adapter_bmi);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //BMI spinner adapter

    //health issue Spinner adapter
//        try {
//            String healthissueLanguage = "healthissuereported_" + sessionManager.getAppLanguage();
//            int healthissue_id = res.getIdentifier(healthissueLanguage, "array", getApplicationContext().getPackageName());
//            if (healthissue_id != 0) {
//                adapter_healthissuereported = ArrayAdapter.createFromResource(this,
//                        healthissue_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_healthissuereported.setAdapter(adapter_healthissuereported);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //health issue spinner adapter

    // primary health provider Spinner adapter
//        try {
//            String primaryhealthproviderLanguage = "primaryhealthprovider_" + sessionManager.getAppLanguage();
//            int primaryhealthproviderLanguage_id = res.getIdentifier(primaryhealthproviderLanguage, "array", getApplicationContext().getPackageName());
//            if (primaryhealthproviderLanguage_id != 0) {
//                adapter_primaryhealthprovider = ArrayAdapter.createFromResource(this,
//                        primaryhealthproviderLanguage_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_primaryhealthprovider.setAdapter(adapter_primaryhealthprovider);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //primary health provider spinner adapter

    // first location Spinner adapter
//        try {
//            String firstlocationLanguage = "firstlocation_" + sessionManager.getAppLanguage();
//            int firstlocation_id = res.getIdentifier(firstlocationLanguage, "array", getApplicationContext().getPackageName());
//            if (firstlocation_id != 0) {
//                adapter_firstlocation = ArrayAdapter.createFromResource(this,
//                        firstlocation_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_firstlocation.setAdapter(adapter_firstlocation);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //first location spinner adapter

    // referred Spinner adapter
//        try {
//            String referredLanguage = "referredto_" + sessionManager.getAppLanguage();
//            int referred_id = res.getIdentifier(referredLanguage, "array", getApplicationContext().getPackageName());
//            if (referred_id != 0) {
//                adapter_referredto = ArrayAdapter.createFromResource(this,
//                        referred_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_referredto.setAdapter(adapter_referredto);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //referred spinner adapter

    // mode transport Spinner adapter
//        try {
//            String modetransportLanguage = "modetransport_" + sessionManager.getAppLanguage();
//            int modetransport_id = res.getIdentifier(modetransportLanguage, "array", getApplicationContext().getPackageName());
//            if (modetransport_id != 0) {
//                adapter_modeoftransport = ArrayAdapter.createFromResource(this,
//                        modetransport_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_modeoftransport.setAdapter(adapter_modeoftransport);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
//        //mode transport spinner adapter

    // score experience Spinner adapter
//        try {
//            String scoreexperienceLanguage = "scoreexperience_" + sessionManager.getAppLanguage();
//            int scoreexperience_id = res.getIdentifier(scoreexperienceLanguage, "array", getApplicationContext().getPackageName());
//            if (scoreexperience_id != 0) {
//                adapter_experiencerscore = ArrayAdapter.createFromResource(this,
//                        scoreexperience_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_experiencerscore.setAdapter(adapter_experiencerscore);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //score experience spinner adapter

    // past 2 yrs Spinner adapter
//        try {
//            String pasttwoyrsLanguage = "pasttwoyrs_" + sessionManager.getAppLanguage();
//            int pasttwoyrs_id = res.getIdentifier(pasttwoyrsLanguage, "array", getApplicationContext().getPackageName());
//            if (pasttwoyrs_id != 0) {
//                adapter_pregnantpasttwoyrs = ArrayAdapter.createFromResource(this,
//                        pasttwoyrs_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_pregnantpasttwoyrs.setAdapter(adapter_pregnantpasttwoyrs);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //past 2 yrs spinner adapter

    // outcome pregnancy Spinner adapter
//        try {
//            String outcomepregnancyLanguage = "outcomepregnancy_" + sessionManager.getAppLanguage();
//            int outcomepregnancy_id = res.getIdentifier(outcomepregnancyLanguage, "array", getApplicationContext().getPackageName());
//            if (outcomepregnancy_id != 0) {
//                adapter_outcomepregnancy = ArrayAdapter.createFromResource(this,
//                        outcomepregnancy_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_outcomepregnancy.setAdapter(adapter_outcomepregnancy);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //outcome pregnancy spinner adapter

    // child alive Spinner adapter
//        try {
//            String childaliveLanguage = "childalive_" + sessionManager.getAppLanguage();
//            int childalive_id = res.getIdentifier(childaliveLanguage, "array", getApplicationContext().getPackageName());
//            if (childalive_id != 0) {
//                adapter_childalive = ArrayAdapter.createFromResource(this,
//                        childalive_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_childalive.setAdapter(adapter_childalive);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //child alive spinner adapter

    // place delivery Spinner adapter
//        try {
//            String placedeliveryLanguage = "placedelivery_" + sessionManager.getAppLanguage();
//            int placedelivery_id = res.getIdentifier(placedeliveryLanguage, "array", getApplicationContext().getPackageName());
//            if (placedelivery_id != 0) {
//                adapter_placeofdeliverypregnant = ArrayAdapter.createFromResource(this,
//                        placedelivery_id, android.R.layout.simple_spinner_dropdown_item);
//            }
//            spinner_placeofdeliverypregnant.setAdapter(adapter_placeofdeliverypregnant);
//
//        } catch (Exception e) {
//            // Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }
    //place delivery spinner adapter

       /* if (!getIntent().hasExtra("newMember") && patientID_edit != null) {
            // block
            try {
                String blockLanguage = "block_" + sessionManager.getAppLanguage();
                int block_id = res.getIdentifier(blockLanguage, "array", getApplicationContext().getPackageName());
                if (block_id != 0) {
                    adapter_block = ArrayAdapter.createFromResource(this,
                            block_id, android.R.layout.simple_spinner_dropdown_item);
                }
                spinner_block.setAdapter(adapter_block);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // block

            // village
            //        if (spinner_block.getSelectedItemPosition() == 1) {
            try {
                String villageLanguage = "peth_block_village_" + sessionManager.getAppLanguage();
                int village_id = res.getIdentifier(villageLanguage, "array", getApplicationContext().getPackageName());
                if (village_id != 0) {
                    adapter_FocalVillage_Peth = ArrayAdapter.createFromResource(this,
                            village_id, android.R.layout.simple_spinner_dropdown_item);
                }
                spinner_village.setAdapter(adapter_FocalVillage_Peth);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } */

 /*   @Override
    public void saveSurveyData(HealthIssues survey) {
//        if (binding.editHealthIssueButton.getVisibility() == View.GONE) {
//            binding.editHealthIssueButton.setVisibility(View.VISIBLE);
//        }
        healthIssuesList.add(survey);
        adapter = new HouseholdSurveyAdapter(healthIssuesList, this, sessionManager.getAppLanguage(), this);
        binding.mainViewPager.setAdapter(adapter);
        binding.mainViewPager.setCurrentItem(healthIssuesList.size() - 1);
        binding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(binding.mainViewPager);
    }

    @Override
    public void saveSurveyDataAtPosition(HealthIssues survey, int position) {
//        if (binding.editHealthIssueButton.getVisibility() == View.GONE) {
//            binding.editHealthIssueButton.setVisibility(View.VISIBLE);
//        }
        healthIssuesList.set(position, survey);
        adapter = new HouseholdSurveyAdapter(healthIssuesList, this, sessionManager.getAppLanguage(), this);
        binding.mainViewPager.setAdapter(adapter);
        binding.mainViewPager.setCurrentItem(position);
        binding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(binding.mainViewPager);
    }

    public void deleteSurveyData(int position, Object object) {
        if (object instanceof HealthIssues) {
            healthIssuesList.remove(position);
            adapter = new HouseholdSurveyAdapter(healthIssuesList, this, sessionManager.getAppLanguage(), this);
            binding.mainViewPager.setAdapter(adapter);
            if (!healthIssuesList.isEmpty()) {
                binding.mainViewPager.setCurrentItem(healthIssuesList.size() - 1);
            }
            binding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            setViewPagerOffset(binding.mainViewPager);
        }

        if (object instanceof PregnancyRosterData) {
            pregnancyOutcomesList.remove(position);
            pregnancyOutcomeAdapter = new PregnancyOutcomeAdapter(pregnancyOutcomesList, this, sessionManager.getAppLanguage());
            binding.poViewPager.setAdapter(pregnancyOutcomeAdapter);
            if (!pregnancyOutcomesList.isEmpty()) {
                binding.poViewPager.setCurrentItem(pregnancyOutcomesList.size() - 1);
            }
            binding.poViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            setViewPagerOffset(binding.poViewPager);
        }
    }

    @Override
    public void getIssueClicked(HealthIssues survey, int position) {
        MaterialAlertDialogBuilder listDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle);
        listDialog.setItems(new String[]{getString(R.string.edit_dialog_button), getString(R.string.delete_dialog_button)}, (dialog, which) -> {
            if (which == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("healthIssueReported", survey.getHealthIssueReported());
                bundle.putString("numberOfEpisodesInTheLastYear", survey.getNumberOfEpisodesInTheLastYear());
                bundle.putString("primaryHealthcareProviderValue", survey.getPrimaryHealthcareProviderValue());
                bundle.putString("firstLocationOfVisit", survey.getFirstLocationOfVisit());
                bundle.putString("referredTo", survey.getReferredTo());
                bundle.putString("modeOfTransportation", survey.getModeOfTransportation());
                bundle.putString("averageCostOfTravelAndStayPerEpisode", survey.getAverageCostOfTravelAndStayPerEpisode());
                bundle.putString("averageCostOfConsultation", survey.getAverageCostOfConsultation());
                bundle.putString("averageCostOfMedicine", survey.getAverageCostOfMedicine());
                bundle.putString("scoreForExperienceOfTreatment", survey.getScoreForExperienceOfTreatment());

                MultipleDiseasesDialog diseasesDialog = new MultipleDiseasesDialog();
                diseasesDialog.setArguments(bundle);
                diseasesDialog.show(getSupportFragmentManager(), MultipleDiseasesDialog.TAG);
            }

            if (which == 1) {
                deleteSurveyData(position, survey);
            }
        });
        listDialog.show();
    }

    @Override
    public void getPregnancyIssueClicked(PregnancyRosterData data, int position) {
        MaterialAlertDialogBuilder listDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle);
        listDialog.setItems(new String[]{getString(R.string.edit_dialog_button), getString(R.string.delete_dialog_button)}, (dialog, which) -> {
            if (which == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("numberOfTimesPregnant", data.getNumberOfTimesPregnant());
                bundle.putString("anyPregnancyOutcomesInThePastTwoYears", data.getAnyPregnancyOutcomesInThePastTwoYears());
                bundle.putString("pregnancyOutcome", data.getPregnancyOutcome());
                bundle.putString("isChildAlive", data.getIsChildAlive());
                bundle.putString("isPreTerm", data.getIsPreTerm());
                bundle.putString("yearOfPregnancyOutcome", data.getYearOfPregnancyOutcome());
                bundle.putString("monthsOfPregnancy", data.getMonthsOfPregnancy());
                bundle.putString("monthsBeenPregnant", data.getMonthsBeenPregnant());
                bundle.putString("placeOfDelivery", data.getPlaceOfDelivery());
                bundle.putString("typeOfDelivery", data.getTypeOfDelivery());
                bundle.putString("focalFacilityForPregnancy", data.getFocalFacilityForPregnancy());
                bundle.putString("facilityName", data.getFacilityName());
                bundle.putString("singleMultipleBirths", data.getSingleMultipleBirths());
                bundle.putString("babyAgeDied", data.getBabyAgeDied());
                bundle.putString("sexOfBaby", data.getSexOfBaby());
                bundle.putString("pregnancyPlanned", data.getPregnancyPlanned());
                bundle.putString("highRiskPregnancy", data.getHighRiskPregnancy());
                bundle.putString("pregnancyComplications", data.getPregnancyComplications());

                PregnancyRosterDialog pregnancyDialog = new PregnancyRosterDialog();
                pregnancyDialog.setArguments(bundle);
                pregnancyDialog.show(getSupportFragmentManager(), PregnancyRosterDialog.TAG);
            }

            if (which == 1) {
                deleteSurveyData(position, data);
            }
        });
        listDialog.show();
    }

    @Override
    public void savePregnancyData(PregnancyRosterData data) {
        binding.edittextNoOfPregnancyOutcomePastTwoYrs.setError(null);
        pregnancyOutcomesList.add(data);
        pregnancyOutcomeAdapter = new PregnancyOutcomeAdapter(pregnancyOutcomesList, this, sessionManager.getAppLanguage());
        binding.poViewPager.setAdapter(pregnancyOutcomeAdapter);
        binding.poViewPager.setCurrentItem(pregnancyOutcomesList.size() - 1);
        binding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(binding.poViewPager);
    }

    @Override
    public void savePregnancyDataAtPosition(PregnancyRosterData data, int position) {
        binding.edittextNoOfPregnancyOutcomePastTwoYrs.setError(null);
        pregnancyOutcomesList.set(position, data);
        pregnancyOutcomeAdapter = new PregnancyOutcomeAdapter(pregnancyOutcomesList, this, sessionManager.getAppLanguage());
        binding.poViewPager.setAdapter(pregnancyOutcomeAdapter);
        binding.poViewPager.setCurrentItem(position);
        binding.mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(binding.poViewPager);
    } */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    private void displayPinDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.layout_dialog_patient_pin, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        AppCompatTextView disclaimerTextView = view.findViewById(R.id.tv_title);
        Button saveButton = view.findViewById(R.id.button_save);
        TextInputEditText pinEditText = view.findViewById(R.id.et_pin);

        disclaimerTextView.setText(getString(R.string.request_patient_pin_string));
        saveButton.setOnClickListener(v -> {
            if (isPinFieldValid(pinEditText)) {
                String pin = Objects.requireNonNull(pinEditText.getText()).toString();
                insertPatient(pin);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private boolean isPinFieldValid(TextInputEditText pinEditText) {
        String enteredPin = pinEditText.getText().toString();

        if (enteredPin.isEmpty()) {
            pinEditText.setError(getString(R.string.empty_pin_error));
            return false;
        }

        if (enteredPin.length() < 4) {
            pinEditText.setError(getString(R.string.please_enter_four_digit_pin));
            return false;
        }

        return true;
    }

    private PatientAttributeLanguageModel getPatientAttributeFromJSON(String jsonString) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(jsonString, PatientAttributeLanguageModel.class);
    }

    private void setVillageSpinnerAdapter(String arrayName) {
        try {
            String villages_Language = arrayName + sessionManager.getAppLanguage();
            int village = getResources().getIdentifier(villages_Language, "array", getApplicationContext().getPackageName());
            if (village != 0) {
                villageAdapter = ArrayAdapter.createFromResource(this, village, R.layout.custom_spinner);
            }
            mVillage.setAdapter(villageAdapter);

            if (patient1 != null && patient1.getCity_village() != null) {
                String villageName = patient1.getCity_village();
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    villageName = StringUtils.switch_en_to_ar_village_edit(villageName);
                }
                int position = villageAdapter.getPosition(villageName);
                if (position != -1) mVillage.setSelection(position);
            } else {
                String villageName = checkAndRemoveEndDash(sessionManager.getVillageName());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    villageName = StringUtils.switch_en_to_ar_village_edit(villageName);
                }
                int position = villageAdapter.getPosition(villageName);
                if (position != -1) mVillage.setSelection(position);
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.occupation_values_missing, Toast.LENGTH_SHORT).show();
        }
    }

    public static String checkAndRemoveEndDash(String villageString) {
        String lastCharacter = String.valueOf(villageString.charAt(villageString.length() - 1));
        if (lastCharacter.equalsIgnoreCase("-")) {
            villageString = villageString.substring(0, villageString.length() - 1);
        }
        return villageString;
    }
}