package org.intelehealth.ezazi.activities.addNewPatient;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.customCalendar.CustomCalendarViewUI2;
import org.intelehealth.ezazi.database.dao.ImagesDAO;
import org.intelehealth.ezazi.database.dao.ImagesPushDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.models.Patient;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.models.dto.ProviderDTO;
import org.intelehealth.ezazi.models.pushRequestApiCall.Address;
import org.intelehealth.ezazi.ui.dialog.MultiChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.ThemeTimePickerDialog;
import org.intelehealth.ezazi.ui.dialog.adapter.RiskFactorMultiChoiceAdapter;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringUtils;
import org.intelehealth.ezazi.utilities.UuidGenerator;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class PatientOtherInfoFragment extends Fragment {
    private static final String TAG = "PatientPersonalInfoFrag";

    public static PatientOtherInfoFragment getInstance() {
        return new PatientOtherInfoFragment();
    }

    View view;
    AutoCompleteTextView mRiskFactorsTextView, mPrimaryDoctorTextView, mSecondaryDoctorTextView;
    Context mContext;
    TextInputEditText mAdmissionDateTextView, mAdmissionTimeTextView, mTotalBirthEditText, mTotalMiscarriageEditText, mActiveLaborDiagnosedDateTextView,
            mActiveLaborDiagnosedTimeTextView, mMembraneRupturedDateTextView, mMembraneRupturedTimeTextView, etBedNumber;
    MaterialButton btnBack, btnNext;
    TextView optionHospital, optionMaternity, optionOther;
    Intent i_privacy;
    private String mAdmissionDateString = "", mAdmissionTimeString = "";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private String patientUuid = "";
    // strings
    private String mTotalBirthCount = "0", mTotalMiscarriageCount = "0";
    private String mLaborOnsetString = "";
    private String mHospitalMaternityString = "";
    private String mActiveLaborDiagnosedDate = "", mActiveLaborDiagnosedTime = "";
    private String mMembraneRupturedDate = "", mMembraneRupturedTime = "";
    private String mRiskFactorsString = "", mPrimaryDoctorUUIDString = "", mSecondaryDoctorUUIDString = "";
    private List<String> mSelectedRiskFactorList = new ArrayList<String>();

    private String mAlternateNumberString = "", mWifeDaughterOfString = "";
    private String mOthersString = "";
    private String[] mDoctorNames;
    String privacy_value;
    private boolean mIsEditMode = false;
    private List<ProviderDTO> mProviderDoctorList = new ArrayList<ProviderDTO>();
    private List<String> mDoctorUUIDs = new ArrayList<>();
    String patientID_edit;
    Patient patient1 = new Patient();
    private boolean hasLicense = false;
    SessionManager sessionManager = null;
    UuidGenerator uuidGenerator = new UuidGenerator();
    String uuid = "";
    PatientDTO patientDTO = new PatientDTO();
    CheckBox mUnknownMembraneRupturedCheckBox;
    ImagesDAO imagesDAO = new ImagesDAO();
    boolean patient_detail = false;
    private PatientAddressInfoFragment secondScreen;
    boolean fromThirdScreen = false, fromSecondScreen = false;
    //    ImageView ivPersonal, ivAddress, ivOther;
    TextView tvSpontaneous, tvInduced;

    int MY_REQUEST_CODE = 5555;
    int dob_indexValue = 15;
    PatientsDAO patientsDAO = new PatientsDAO();
//    TextView tvPersonalInfo, tvAddressInfo, tvOtherInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_patient_other_info, container, false);
        mContext = getActivity();
        sessionManager = new SessionManager(mContext);

        initUI();
        return view;
    }

    private void initUI() {
//        ivPersonal = getActivity().findViewById(R.id.iv_personal_info);
//        ivAddress = getActivity().findViewById(R.id.iv_address_info);
//        ivOther = getActivity().findViewById(R.id.iv_other_info);
//        tvPersonalInfo = getActivity().findViewById(R.id.tv_personal_info);
//        tvAddressInfo = getActivity().findViewById(R.id.tv_address_info);
//        tvOtherInfo = getActivity().findViewById(R.id.tv_other_info);

        mAdmissionDateTextView = view.findViewById(R.id.et_admission_date);
        mAdmissionTimeTextView = view.findViewById(R.id.et_admission_time);
        mTotalBirthEditText = view.findViewById(R.id.et_total_birth);
        mTotalMiscarriageEditText = view.findViewById(R.id.et_total_miscarriage);
        tvSpontaneous = view.findViewById(R.id.et_spontaneous);
        tvInduced = view.findViewById(R.id.et_induced);
        mActiveLaborDiagnosedDateTextView = view.findViewById(R.id.et_labor_diagnosed_date);
        mActiveLaborDiagnosedTimeTextView = view.findViewById(R.id.et_labor_diagnosed_time);
        mMembraneRupturedDateTextView = view.findViewById(R.id.et_sac_ruptured_date);
        mMembraneRupturedTimeTextView = view.findViewById(R.id.et_sac_ruptured_time);
        optionHospital = view.findViewById(R.id.option_hospital);
        optionMaternity = view.findViewById(R.id.option_maternity);
        optionOther = view.findViewById(R.id.option_other);
        mPrimaryDoctorTextView = view.findViewById(R.id.autotv_primary_doctor);
        mSecondaryDoctorTextView = view.findViewById(R.id.autotv_secondary_doctor);
        etBedNumber = view.findViewById(R.id.et_bed_number);
        btnBack = view.findViewById(R.id.btn_back_other);
        btnNext = view.findViewById(R.id.btn_next_other);
        mUnknownMembraneRupturedCheckBox = view.findViewById(R.id.mUnknownMembraneRupturedCheckBox);
        mRiskFactorsTextView = view.findViewById(R.id.autotv_risk_factors);


        handleOptionsForMaternity();

        mRiskFactorsTextView.setOnClickListener(v -> {
            // showRiskFactorsDialog();
        });
        /*new*/
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            mProviderDoctorList = providerDAO.getDoctorList();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        handleAllClickListeners();

        secondScreen = new PatientAddressInfoFragment();
        if (getArguments() != null) {
            Log.d(TAG, "initUI: other");
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromSecondScreen = getArguments().getBoolean("fromSecondScreen");
            patient_detail = getArguments().getBoolean("patient_detail");
            //   patientID_edit = getArguments().getString("patientUuid");
            //check new flow
            Log.d(TAG, "33initUI: firstname personal:  " + patientDTO.getFirstname());
            Log.d(TAG, "33initUI: lastname personal: " + patientDTO.getLastname());
            Log.d(TAG, "33initUI: middlename personal: " + patientDTO.getMiddlename());
            Log.d(TAG, "33initUI: dob personal: " + patientDTO.getDateofbirth());
            Log.d(TAG, "33initUI: phoneno: personal " + patientDTO.getPhonenumber());
            Log.d(TAG, "33initUI: patient_detail personal: " + patient_detail);



          /*  if (patientID_edit != null) {
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
            } else {
                patient1.setUuid(patientDTO.getUuid());
                setscreen(patientDTO.getUuid());
            }*/

          /*  if (patientID_edit != null) {
                patientDTO.setUuid(patientID_edit);
            } else {
                // do nothing...
            }
*/

            if (patient_detail) {
                //    patientDTO.setUuid(patientID_edit);
            } else {
                // do nothing...
            }

         /*   if (patientID_edit != null) {
                patientDTO.setUuid(patientID_edit);
            } else if (patientDTO.getUuid() != null){
                //  patientDTO.setUuid(uuid);
                patientID_edit = patientDTO.getUuid();
            }
            else {
              //  do nothing
            }*/
        }

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                mIsEditMode = true;
                // this.setTitle(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
                updateUI(patient1);
            }
        }
    }

    private void handleOptionsForMaternity() {
        //mLaborOnsetString = "Spontaneous";
        ///mHospitalMaternityString = "Hospital";
        //maternity/hospital selectors
        optionHospital.setOnClickListener(v -> {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionHospital.setTextColor(getResources().getColor(R.color.white));
            optionMaternity.setTextColor(getResources().getColor(R.color.gray));
            optionOther.setTextColor(getResources().getColor(R.color.gray));
            mHospitalMaternityString = optionHospital.getText().toString();
        });
        optionMaternity.setOnClickListener(v -> {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionHospital.setTextColor(getResources().getColor(R.color.gray));
            optionMaternity.setTextColor(getResources().getColor(R.color.white));
            optionOther.setTextColor(getResources().getColor(R.color.gray));
            mHospitalMaternityString = optionMaternity.getText().toString();

        });
        optionOther.setOnClickListener(v -> {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionHospital.setTextColor(getResources().getColor(R.color.gray));
            optionMaternity.setTextColor(getResources().getColor(R.color.gray));
            optionOther.setTextColor(getResources().getColor(R.color.white));
            mHospitalMaternityString = optionOther.getText().toString();

        });

        tvSpontaneous.setOnClickListener(v -> {
            tvSpontaneous.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            tvInduced.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            tvSpontaneous.setTextColor(getResources().getColor(R.color.white));
            tvInduced.setTextColor(getResources().getColor(R.color.gray));
            mLaborOnsetString = tvSpontaneous.getText().toString();
        });
        tvInduced.setOnClickListener(v -> {
            tvSpontaneous.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            tvInduced.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            tvSpontaneous.setTextColor(getResources().getColor(R.color.gray));
            tvInduced.setTextColor(getResources().getColor(R.color.white));
            mLaborOnsetString = tvInduced.getText().toString();

        });

     /*   mUnknownMembraneRupturedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    mUnknownMembraneRupturedCheckBox.setButtonDrawable(getResources().getDrawable(R.drawable.cb_selected));

                } else {
                    mUnknownMembraneRupturedCheckBox.setButtonDrawable(getResources().getDrawable(R.drawable.ic_checkbox));
                }
            }
        });*/
    }

    private void handleAllClickListeners() {

        TextInputLayout etLayoutAdmissionDate, etLayoutAdmissionTime,
                etLabourDiagnosedDate, etLabourDiagnosedTime, etLayoutSacRupturedDate, etLayoutSacRupturedTime,
                etLayoutRiskFactors, etLayoutPrimaryDoctor, etLayoutSecondaryDoctor;
        etLayoutAdmissionDate = view.findViewById(R.id.etLayout_admission_date);
        etLayoutAdmissionTime = view.findViewById(R.id.etLayout_admission_time);
        etLabourDiagnosedDate = view.findViewById(R.id.etLayout_labor_diagnosed_date);
        etLabourDiagnosedTime = view.findViewById(R.id.etLayout_labor_diagnosed_time);
        etLayoutSacRupturedDate = view.findViewById(R.id.etLayout_sac_ruptured_date);
        etLayoutSacRupturedTime = view.findViewById(R.id.etLayout_sac_ruptured_time);
        etLayoutRiskFactors = view.findViewById(R.id.etLayout_risk_factors);
        etLayoutPrimaryDoctor = view.findViewById(R.id.etLayout_primary_doctor);
        etLayoutSecondaryDoctor = view.findViewById(R.id.etLayout_secondary_doctor);


        etLayoutAdmissionDate.setEndIconOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("whichDate", "admissionDate");
            CustomCalendarViewUI2 dialog = new CustomCalendarViewUI2(getActivity());
            dialog.setArguments(args);
            dialog.setTargetFragment(PatientOtherInfoFragment.this, MY_REQUEST_CODE);
            if (getFragmentManager() != null) {
                dialog.show(getFragmentManager(), "PatientOtherInfoFragment");
            }

        });

        etLayoutAdmissionTime.setEndIconOnClickListener(v -> {
            selectTimeForAllParameters("admissionTimeString");
        });

        mUnknownMembraneRupturedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: isChecked : " + isChecked);
                if (isChecked) {
                    etLayoutSacRupturedDate.setVisibility(View.GONE);
                    etLayoutSacRupturedTime.setVisibility(View.GONE);
                    mMembraneRupturedDateTextView.setEnabled(false);
                    mMembraneRupturedTimeTextView.setEnabled(false);

                } else {
                    etLayoutSacRupturedDate.setVisibility(View.VISIBLE);
                    etLayoutSacRupturedTime.setVisibility(View.VISIBLE);
                    mMembraneRupturedDateTextView.setEnabled(true);
                    mMembraneRupturedTimeTextView.setEnabled(true);


                }
            }
        });
        etLabourDiagnosedDate.setEndIconOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("whichDate", "labourDiagnosedDate");
            CustomCalendarViewUI2 dialog = new CustomCalendarViewUI2(getActivity());
            dialog.setArguments(args);
            dialog.setTargetFragment(PatientOtherInfoFragment.this, MY_REQUEST_CODE);
            if (getFragmentManager() != null) {
                dialog.show(getFragmentManager(), "PatientOtherInfoFragment");
            }
        });
        etLabourDiagnosedTime.setEndIconOnClickListener(v -> {
            selectTimeForAllParameters("laborOnsetString");
        });

        etLayoutSacRupturedDate.setEndIconOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putString("whichDate", "sacRupturedDate");
            CustomCalendarViewUI2 dialog1 = new CustomCalendarViewUI2(getActivity());
            dialog1.setArguments(args1);
            dialog1.setTargetFragment(PatientOtherInfoFragment.this, MY_REQUEST_CODE);
            if (getFragmentManager() != null) {
                dialog1.show(getFragmentManager(), "PatientOtherInfoFragment");
            }
        });
        etLayoutSacRupturedTime.setEndIconOnClickListener(v -> {
            selectTimeForAllParameters("membraneRupturedTime");

        });
        etLayoutRiskFactors.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MultiChoiceDialogFragment<String> dialog1 = new MultiChoiceDialogFragment.Builder<String>(mContext)
                        .title(R.string.select_risk_factors)
                        .positiveButtonLabel(R.string.save_button)
                        .build();

                final String[] itemsArray = {"None", "under age 20", "Women over age 35", "Diabetes", "Obesity", "Underweight",
                        "High blood pressure", "PCOS", "Kidney disease", "Thyroid disease", "Asthma", "Uterine fibroids"};
                List<String> items = Arrays.asList(itemsArray);

                dialog1.setAdapter(new RiskFactorMultiChoiceAdapter(mContext, new ArrayList<>(items)));
                dialog1.setListener(selectedItems -> {
                    if (selectedItems.size() > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < selectedItems.size(); i++) {
                            if (!stringBuilder.toString().isEmpty())
                                stringBuilder.append(",");
                            stringBuilder.append(selectedItems.get(i));

                        }
                        mRiskFactorsString = stringBuilder.toString();
                        mRiskFactorsTextView.setText(mRiskFactorsString);
                    }

                });

                assert getFragmentManager() != null;
                dialog1.show(getFragmentManager(), MultiChoiceDialogFragment.class.getCanonicalName());
            }
        });

        etLayoutPrimaryDoctor.setEndIconOnClickListener(v -> selectPrimaryDoctor());

        etLayoutSecondaryDoctor.setEndIconOnClickListener(v -> {
            selectSecondaryDoctor();
        });

        i_privacy = getActivity().getIntent();
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.


        //Initialize the local database to store patient information

      /*  Intent intent = getActivity().getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                mIsEditMode = true;
                // this.setTitle(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient1.setUuid(patientID_edit);
                //temp commit
                // setscreen(patientID_edit);
                //  updateUI(patient1);
            }
        }*/
//        if (sessionManager.valueContains("licensekey"))
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, mContext),
                                String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(mContext, "JsonException" + e, Toast.LENGTH_LONG).show();
            // temp commit -  showAlertDialogButtonClicked(e.toString());
        }

        /*temp commit
        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();

        }*/
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void generateUuid() {

        patientUuid = uuidGenerator.UuidGenerator();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        ivPersonal.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_info_done));
//        ivAddress.setImageDrawable(getResources().getDrawable(R.drawable.ic_address_done));
//        ivOther.setImageDrawable(getResources().getDrawable(R.drawable.ic_other_info_active));
//        tvPersonalInfo.setTextColor(getResources().getColor(R.color.colorPrimary));
//        tvAddressInfo.setTextColor(getResources().getColor(R.color.colorPrimary));
//        tvOtherInfo.setTextColor(getResources().getColor(R.color.colorPrimary));


        btnBack.setOnClickListener(v -> {
            onBackInsertIntopatientDTO();
        });

        btnNext.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), PatientDetailActivity2.class);
//                startActivity(intent);
            onPatientCreateClicked();
        });


    }

    public void onPatientCreateClicked() {

        mTotalBirthCount = mTotalBirthEditText.getText().toString().trim();
        mTotalMiscarriageCount = mTotalMiscarriageEditText.getText().toString().trim();

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = UUID.randomUUID().toString();

        patientDTO.setUuid(uuid);
        Gson gson = new Gson();

        boolean cancel = false;
        View focusView = null;
        //mLaborOnsetString = "Spontaneous";
        ///mHospitalMaternityString = "Hospital";
        if (mAdmissionDateString.isEmpty()) {
            Toast.makeText(mContext, "Please select admission date", Toast.LENGTH_SHORT).show();

            return;
        }
        if (mAdmissionTimeString.isEmpty()) {
            Toast.makeText(mContext, "Please select admission time", Toast.LENGTH_SHORT).show();

            return;
        }

        if (mTotalBirthCount.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.total_birth_count_val_txt), Toast.LENGTH_SHORT).show();
            mTotalBirthEditText.requestFocus();
            return;
        }
        if (mTotalMiscarriageCount.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.total_miscarriage_count_val_txt), Toast.LENGTH_SHORT).show();
            mTotalMiscarriageEditText.requestFocus();
            return;
        }
        if (mLaborOnsetString.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.labor_onset_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mActiveLaborDiagnosedDate.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.active_labor_diagnosed_date_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mActiveLaborDiagnosedTime.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.active_labor_diagnosed_time_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }

     /*   if (!mUnknownMembraneRupturedCheckBox.isChecked() && mMembraneRupturedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.membrane_ruptured_date_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mUnknownMembraneRupturedCheckBox.isChecked() && mMembraneRupturedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.membrane_ruptured_time_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }*/
        if (mRiskFactorsString.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.risk_factors_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mHospitalMaternityString.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.hospital_matermnity_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPrimaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.primary_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSecondaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.seconday_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        /*end*/
        if (cancel) {
            focusView.requestFocus();
        } else {


            ///  1 patientDTO.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
//            patientDTO.setCountry(StringUtils.getValue(mSwitch_hi_en_te_Country(mCountry.getSelectedItem().toString(),sessionManager.getAppLanguage())));
//
//            patientDTO.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            ///  2  patientDTO.setPatientPhoto(mCurrentPhotoPath);
//          patientDTO.setEconomic(StringUtils.getValue(m));
            //// 3 patientDTO.setStateprovince(StringUtils.getValue(mState.getSelectedItem().toString()));
//            patientDTO.setStateprovince(StringUtils.getValue(mSwitch_hi_en_te_State(mState.getSelectedItem().toString(),sessionManager.getAppLanguage())));

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mCaste));
            patientAttributesDTOList.add(patientAttributesDTO);*/


            //Admission_Date
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Admission_Date"));
            patientAttributesDTO.setValue(StringUtils.getValue(mAdmissionDateString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Admission_Time
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Admission_Time"));
            patientAttributesDTO.setValue(StringUtils.getValue(mAdmissionTimeString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Parity
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Parity"));
            patientAttributesDTO.setValue(StringUtils.getValue(mTotalBirthCount + "," + mTotalMiscarriageCount));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Labor Onset
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Labor Onset"));
            patientAttributesDTO.setValue(StringUtils.getValue(mLaborOnsetString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Active Labor Diagnosed
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Active Labor Diagnosed"));
            patientAttributesDTO.setValue(StringUtils.getValue(mActiveLaborDiagnosedDate + " " + mActiveLaborDiagnosedTime));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Membrane Ruptured Timestamp
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Membrane Ruptured Timestamp"));
            patientAttributesDTO.setValue(mUnknownMembraneRupturedCheckBox.isChecked() ? "U" : StringUtils.getValue(mMembraneRupturedDate + " " + mMembraneRupturedTime));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Risk factors
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Risk factors"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRiskFactorsString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Hospital_Maternity
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Hospital_Maternity"));
            patientAttributesDTO.setValue(StringUtils.getValue(mHospitalMaternityString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //PrimaryDoctor
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PrimaryDoctor"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPrimaryDoctorUUIDString) + "@#@" + mPrimaryDoctorTextView.getText());
            patientAttributesDTOList.add(patientAttributesDTO);

            //SecondaryDoctor
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("SecondaryDoctor"));
            patientAttributesDTO.setValue(StringUtils.getValue(mSecondaryDoctorUUIDString) + "@#@" + mSecondaryDoctorTextView.getText());
            patientAttributesDTOList.add(patientAttributesDTO);

            //Ezazi Registration Number
            int number = (int) (Math.random() * (99999999 - 100 + 1) + 100);
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Ezazi Registration Number"));
            patientAttributesDTO.setValue(patientDTO.getCountry().substring(0, 2) + "/" + patientDTO.getStateprovince().substring(0, 2) + "/" + patientDTO.getCityvillage().substring(0, 2) + "/" + String.valueOf(number));
            patientAttributesDTOList.add(patientAttributesDTO);
            /*end*/

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Mother's Name"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRelationship.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
            patientAttributesDTO.setValue(StringUtils.getValue(mOccupation.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatus));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mEducation));
            patientAttributesDTOList.add(patientAttributesDTO);
*/
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
            patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());

            //House Hold Registration
//            if (sessionManager.getHouseholdUuid().equals("")){
//
//                String HouseHold_UUID = UUID.randomUUID().toString();
//                sessionManager.setHouseholdUuid(HouseHold_UUID);
//
//                patientAttributesDTO = new PatientAttributesDTO();
//                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//                patientAttributesDTO.setPatientuuid(uuid);
//                patientAttributesDTO.setPersonAttributeTypeUuid
//                        (patientsDAO.getUuidForAttribute("householdID"));
//                patientAttributesDTO.setValue(HouseHold_UUID);
//
//            } else {
//
//                String HouseHold_UUID = sessionManager.getHouseholdUuid();
//                patientAttributesDTO = new PatientAttributesDTO();
//                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//                patientAttributesDTO.setPatientuuid(uuid);
//                patientAttributesDTO.setPersonAttributeTypeUuid
//                        (patientsDAO.getUuidForAttribute("householdID"));
//                patientAttributesDTO.setValue(HouseHold_UUID);
//
//            }

            patientAttributesDTOList.add(patientAttributesDTO);
            Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
            patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);
            patientDTO.setSyncd(false);
            Logger.logD("patient json : ", "Json : " + gson.toJson(patientDTO, PatientDTO.class));

        }

        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, uuid);
            Log.d(TAG, "onPatientCreateClicked: isPatientInserted : " + isPatientInserted);

            boolean isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), uuid);

            if (NetworkConnection.isOnline(mContext)) {
//                patientApiCall();
//                frameJson();

//                AppConstants.notificationUtils.showNotifications(getString(R.string.patient_data_upload),
//                        getString(R.string.uploading) + patientDTO.getFirstname() + "" + patientDTO.getLastname() +
//                                "'s data", 2, getApplication());


                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi();
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();
//                if (push)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s data not uploaded.", 2, getApplication());

//                if (pushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirstname() + "" + patientDTO.getLastname() + "'s Image not complete.", 4, getApplication());


//
            }
//            else {
//                AppConstants.notificationUtils.showNotifications(getString(R.string.patient_data_failed), getString(R.string.check_your_connectivity), 2, IdentificationActivity.this);
//            }
            // if (isPatientInserted && isPatientImageInserted) {

            if (isPatientInserted) {
                Logger.logD(TAG, "inserted");
                Intent i = new Intent(mContext, PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientDTO.getFirstname() + " " + patientDTO.getLastname());
                i.putExtra("tag", "newPatient");
                i.putExtra("privacy", privacy_value);
                i.putExtra("hasPrescription", "false");
                Log.d(TAG, "Privacy Value on (Identification): " + privacy_value); //privacy value transferred to PatientDetail activity.
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(i);
            } else {
                Toast.makeText(mContext, "Error of adding the data", Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    private void onBackInsertIntopatientDTO() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromThirdScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        secondScreen.setArguments(bundle); // passing data to Fragment

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, secondScreen)
                .commit();
        ((AddNewPatientActivity) requireActivity()).changeCurrentPage(AddNewPatientActivity.PAGE_ADDRESS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //selectedDate  -  30/5/2023
        if (data != null) {
            Bundle bundle = data.getExtras();
            String selectedDate = bundle.getString("selectedDate");
            String whichDate = bundle.getString("whichDate");
            Log.d(TAG, "onActivityResult: selectedDate : " + selectedDate);

            if (!whichDate.isEmpty()) {
                if (whichDate.equals("admissionDate")) {
                    mAdmissionDateString = selectedDate;
                    mAdmissionDateTextView.setText(selectedDate);
                /*String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(mAdmissionDateString);
                if (!mAdmissionDateString.isEmpty()) {
                    String[] splitedDate = mAdmissionDateString.split("/");
                   mAdmissionDateTextView.setText(dateToshow1 + ", " + splitedDate[2]);
                    */
                } else if (whichDate.equals("labourDiagnosedDate")) {
                    mActiveLaborDiagnosedDate = selectedDate;
                    mActiveLaborDiagnosedDateTextView.setText(selectedDate);
                } else if (whichDate.equals("sacRupturedDate")) {
                    mMembraneRupturedDate = selectedDate;
                    mMembraneRupturedDateTextView.setText(selectedDate);
                }
            }
        }
    }

    private void selectPrimaryDoctor() {
        List<ProviderDTO> providerDoctorList = new ArrayList<>();
        for (int i = 0; i < mProviderDoctorList.size(); i++) {
            if (!mSecondaryDoctorUUIDString.equals(mProviderDoctorList.get(i).getUserUuid())) {
                providerDoctorList.add(mProviderDoctorList.get(i));
            }
        }
        Log.d(TAG, "onClick:providerDoctorList : " + providerDoctorList.size());
        mDoctorNames = new String[providerDoctorList.size()];
        mDoctorUUIDs.clear();
        int selectedId = 0;

        for (int i = 0; i < providerDoctorList.size(); i++) {
            mDoctorNames[i] = providerDoctorList.get(i).getGivenName() + " " + providerDoctorList.get(i).getFamilyName();
            mDoctorUUIDs.add(providerDoctorList.get(i).getUserUuid());
            if (mPrimaryDoctorUUIDString.equals(providerDoctorList.get(i).getUserUuid()))
                selectedId = i;
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                .title(R.string.select_primary_doctor)
                .positiveButtonLabel(R.string.save_button)
                .content(Arrays.asList(mDoctorNames))
                .build();

        dialog.setListener((position, value) -> {
            Log.d(TAG, "selectPrimaryDoctor: position : " + position);
            Log.d(TAG, "selectPrimaryDoctor: value : " + value);
            mPrimaryDoctorUUIDString = mDoctorUUIDs.get(position);
            mPrimaryDoctorTextView.setText(mDoctorNames[position]);
        });

        dialog.show(requireFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void selectSecondaryDoctor() {
        if (mPrimaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(mContext, "Please select the primary doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ProviderDTO> providerDoctorList = new ArrayList<>();
        for (int i = 0; i < mProviderDoctorList.size(); i++) {
            if (!mPrimaryDoctorUUIDString.equals(mProviderDoctorList.get(i).getUserUuid())) {
                providerDoctorList.add(mProviderDoctorList.get(i));
            }
        }
        mDoctorNames = new String[providerDoctorList.size()];
        mDoctorUUIDs.clear();
        int selectedId = 0;
        for (int i = 0; i < providerDoctorList.size(); i++) {
            mDoctorNames[i] = providerDoctorList.get(i).getGivenName() + " " + providerDoctorList.get(i).getFamilyName();
            mDoctorUUIDs.add(providerDoctorList.get(i).getUserUuid());
            if (mSecondaryDoctorUUIDString.equals(providerDoctorList.get(i).getUserUuid()))
                selectedId = i;
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                .title(R.string.select_secondary_doctor)
                .positiveButtonLabel(R.string.save_button)
                .content(Arrays.asList(mDoctorNames))
                .build();

        dialog.setListener((position, value) -> {
            Log.d(TAG, "selectSecondaryDoctor: position : " + position);
            Log.d(TAG, "selectSecondaryDoctor: value : " + value);
            mSecondaryDoctorUUIDString = mDoctorUUIDs.get(position);
            mSecondaryDoctorTextView.setText(mDoctorNames[position]);
        });

        dialog.show(requireFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void selectTimeForAllParameters(String forWhichParameter) {
        ThemeTimePickerDialog dialog = new ThemeTimePickerDialog.Builder(mContext)
                .title(R.string.current_time)
                .positiveButtonLabel(R.string.ok)
                .build();
        dialog.setListener((hours, minutes, amPm, value) -> {
            Log.d("ThemeTimePickerDialog", "value : " + value);
            boolean isPM = (hours >= 12);
            String timeString = String.format("%02d:%02d %s", (hours == 12 || hours == 0) ? 12 : hours % 12, minutes, isPM ? "PM" : "AM");
            Log.d(TAG, "selectTime: timeString : " + timeString);

            if (forWhichParameter.equals("admissionTimeString")) {
                mAdmissionTimeString = timeString;
                mAdmissionTimeTextView.setText(timeString);
            } else if (forWhichParameter.equals("laborOnsetString")) {
                mActiveLaborDiagnosedTime = timeString;
                mActiveLaborDiagnosedTimeTextView.setText(timeString);
            } else if (forWhichParameter.equals("membraneRupturedTime")) {
                mMembraneRupturedTime = timeString;
                mMembraneRupturedTimeTextView.setText(timeString);
            }
        });
        dialog.show(getFragmentManager(), "ThemeTimePickerDialog");
    }

    private void setscreen(String patientUID) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection = "uuid=?";
        String[] patientArgs = {patientUID};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo",
                "economic_status", "education_status", "caste"};
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
        String[] patientArgs1 = {patientUID};
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
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient1.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient1.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient1.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient1.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient1.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                /*new*/
                if (name.equalsIgnoreCase("AlternateNo")) {
                    patient1.setAlternateNo(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Wife_Daughter_Of")) {
                    patient1.setWifeDaughterOf(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Admission_Date")) {
                    patient1.setAdmissionDate(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Admission_Time")) {
                    patient1.setAdmissionTime(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }


                if (name.equalsIgnoreCase("Parity")) {
                    patient1.setParity(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Labor Onset")) {
                    patient1.setLaborOnset(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Active Labor Diagnosed")) {
                    patient1.setActiveLaborDiagnosed(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Membrane Ruptured Timestamp")) {
                    patient1.setMembraneRupturedTimestamp(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Risk factors")) {
                    patient1.setRiskFactors(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Hospital_Maternity")) {
                    patient1.setHospitalMaternity(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PrimaryDoctor")) {
                    patient1.setPrimaryDoctor(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase("SecondaryDoctor")) {
                    patient1.setSecondaryDoctor(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase("Ezazi Registration Number")) {
                    patient1.seteZaziRegNumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                /*end*/

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

    private void updateUI(Patient patient) {

        //Admission_Date
        if (patient.getAdmissionDate() != null) {
            mAdmissionDateString = patient.getAdmissionDate();
            mAdmissionDateTextView.setText(mAdmissionDateString);
        }
        //Admission_Time
        if (patient.getAdmissionTime() != null) {
            mAdmissionTimeString = patient.getAdmissionTime();
            mAdmissionTimeTextView.setText(mAdmissionTimeString);
        }

        // parity
        if (patient.getParity() != null) {
            mTotalBirthCount = patient.getParity().split(",")[0];
            mTotalMiscarriageCount = patient.getParity().split(",")[1];
            mTotalBirthEditText.setText(mTotalBirthCount);
            mTotalMiscarriageEditText.setText(mTotalMiscarriageCount);
        }

        //Labor Onset
        if (patient.getLaborOnset() != null) {
            mLaborOnsetString = patient.getLaborOnset();
            if (mLaborOnsetString.equalsIgnoreCase("Spontaneous")) {
                tvSpontaneous.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
                tvInduced.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                tvSpontaneous.setTextColor(getResources().getColor(R.color.white));
                tvInduced.setTextColor(getResources().getColor(R.color.gray));
                mLaborOnsetString = tvSpontaneous.getText().toString();
            } else if (mLaborOnsetString.equalsIgnoreCase("Induced")) {
                tvSpontaneous.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                tvInduced.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
                tvSpontaneous.setTextColor(getResources().getColor(R.color.gray));
                tvInduced.setTextColor(getResources().getColor(R.color.white));
                mLaborOnsetString = tvInduced.getText().toString();
            }
        }
        //When was active labor diagnosed?
        if (patient.getActiveLaborDiagnosed() != null) {
            mActiveLaborDiagnosedDate = patient.getActiveLaborDiagnosed().split(" ")[0];
            mActiveLaborDiagnosedTime = patient.getActiveLaborDiagnosed().split(" ")[1];
            mActiveLaborDiagnosedDateTextView.setText(mActiveLaborDiagnosedDate);
            mActiveLaborDiagnosedTimeTextView.setText(mActiveLaborDiagnosedTime);
        }

        //When was the membrane ruptured?
        if (patient.getMembraneRupturedTimestamp() != null) {
            if (patient.getMembraneRupturedTimestamp().equalsIgnoreCase("U")) {
                mUnknownMembraneRupturedCheckBox.setChecked(true);
            } else {
                mUnknownMembraneRupturedCheckBox.setChecked(false);
                mMembraneRupturedDate = patient.getMembraneRupturedTimestamp().split(" ")[0];
                mMembraneRupturedTime = patient.getMembraneRupturedTimestamp().split(" ")[1];
                mMembraneRupturedDateTextView.setText(mMembraneRupturedDate);
                mMembraneRupturedTimeTextView.setText(mMembraneRupturedTime);
            }
        }
        //Risk factors
        if (patient.getRiskFactors() != null) {
            mRiskFactorsString = patient.getRiskFactors();
            mRiskFactorsTextView.setText(mRiskFactorsString);
        }

        //Hospital/Maternity?
        if (patient.getHospitalMaternity() != null) {
            //mOthersEditText.setVisibility(View.GONE);
            mHospitalMaternityString = patient.getHospitalMaternity();
            if (mHospitalMaternityString.equalsIgnoreCase("Hospital")) {
                optionHospital.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
                optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                optionHospital.setTextColor(getResources().getColor(R.color.white));
                optionMaternity.setTextColor(getResources().getColor(R.color.gray));
                optionOther.setTextColor(getResources().getColor(R.color.gray));
                mHospitalMaternityString = optionHospital.getText().toString();
            } else if (mHospitalMaternityString.equalsIgnoreCase("Maternity")) {
                optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
                optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                optionHospital.setTextColor(getResources().getColor(R.color.gray));
                optionMaternity.setTextColor(getResources().getColor(R.color.white));
                optionOther.setTextColor(getResources().getColor(R.color.gray));
                mHospitalMaternityString = optionMaternity.getText().toString();
            } else {
                optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
                optionOther.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
                optionHospital.setTextColor(getResources().getColor(R.color.gray));
                optionMaternity.setTextColor(getResources().getColor(R.color.gray));
                optionOther.setTextColor(getResources().getColor(R.color.white));
                mHospitalMaternityString = optionOther.getText().toString();
            }
        }

        //primaryDoctor
        Log.v(TAG, "getPrimaryDoctor" + patient.getPrimaryDoctor());
        Log.v(TAG, "getPrimaryDoctor" + patient.getPrimaryDoctor());
        if (patient.getPrimaryDoctor() != null) {
            mPrimaryDoctorUUIDString = patient.getPrimaryDoctor().split("@#@")[0];
            mPrimaryDoctorTextView.setText(patient.getPrimaryDoctor().split("@#@")[1]);
        }

        //secondaryDoctor
        if (patient.getPrimaryDoctor() != null) {
            mSecondaryDoctorUUIDString = patient.getSecondaryDoctor().split("@#@")[0];
            mSecondaryDoctorTextView.setText(patient.getSecondaryDoctor().split("@#@")[1]);
        }
    }

}