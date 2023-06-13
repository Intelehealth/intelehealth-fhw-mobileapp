package org.intelehealth.ezazi.activities.addNewPatient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import org.intelehealth.ezazi.ui.dialog.MultiChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.ThemeTimePickerDialog;
import org.intelehealth.ezazi.ui.dialog.adapter.RiskFactorMultiChoiceAdapter;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringUtils;
import org.intelehealth.ezazi.utilities.UuidGenerator;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    TextInputEditText mAdmissionDateTextView, mAdmissionTimeTextView, mTotalBirthEditText,
            mTotalMiscarriageEditText, mActiveLaborDiagnosedDateTextView, mActiveLaborDiagnosedTimeTextView,
            mMembraneRupturedDateTextView, mMembraneRupturedTimeTextView, etBedNumber, etHospitalOther;
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
    String patientUuidUpdate = "";
    boolean fromSummary = false;
    private PatientAddressInfoFragment secondScreen;
    boolean fromThirdScreen = false, fromSecondScreen = false;
    //    ImageView ivPersonal, ivAddress, ivOther;
    TextView tvSpontaneous, tvInduced;
    int MY_REQUEST_CODE = 5555;
    int dob_indexValue = 15;
    PatientsDAO patientsDAO = new PatientsDAO();
    //    TextView tvPersonalInfo, tvAddressInfo, tvOtherInfo;
//    TextView tvPersonalInfo, tvAddressInfo, tvOtherInfo;
    TextView tvErrorAdmissionDate, tvErrorAdmissionTime, tvErrorTotalBirth, tvErrorTotalMiscarriage, tvErrorLabourOnset,
            tvErrorSacRupturedDate, tvErrorSacRupturedTime, tvErrorPrimaryDoctor, tvErrorSecondaryDoctor,
            tvErrorBedNumber, tvErrorLabourDiagnosedDate, tvErrorLabourDiagnosedTime, tvErrorRiskFactor, tvErrorHospital;
    MaterialCardView cardAdmissionDate, cardAdmissionTime, cardTotalBirth, cardTotalMiscarraige, cardSacRupturedDate,
            cardSacRupturedTime, cardPrimaryDoctor, cardSecondaryDoctor, cardBedNumber, cardDiagnosedDate, cardDiagnosedTime,
            dropdownRiskFactors, cardOptions, cardHospitalOther;
    LinearLayout layoutErrorLabourOnset, layoutSacRuptured;
    boolean isUnknownChecked;

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
        btnBack = view.findViewById(R.id.btn_back_address);
        btnNext = view.findViewById(R.id.btn_next_address);
        mUnknownMembraneRupturedCheckBox = view.findViewById(R.id.mUnknownMembraneRupturedCheckBox);
        mRiskFactorsTextView = view.findViewById(R.id.autotv_risk_factors);
        dropdownRiskFactors = view.findViewById(R.id.dropdown_risk_factors);
        etHospitalOther = view.findViewById(R.id.et_hospital_other);


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
            mAlternateNumberString = getArguments().getString("mAlternateNumberString");
            fromSummary = getArguments().getBoolean("fromSummary");
            patientUuidUpdate = getArguments().getString("patientUuidUpdate");


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

        handleValidations();

    }

    private void handleValidations() {

        //initialize error fields
        tvErrorAdmissionDate = view.findViewById(R.id.tv_admission_date_error);
        tvErrorAdmissionTime = view.findViewById(R.id.tv_admission_time_error);
        tvErrorTotalBirth = view.findViewById(R.id.tv_parity_date_error);
        tvErrorTotalMiscarriage = view.findViewById(R.id.tv_parity_time_error);
        tvErrorLabourOnset = view.findViewById(R.id.tv_error_labour_onset);
        tvErrorSacRupturedDate = view.findViewById(R.id.tv_sac_ruptured_date_error);
        tvErrorSacRupturedTime = view.findViewById(R.id.tv_sac_ruptured_time_error);
        tvErrorPrimaryDoctor = view.findViewById(R.id.tv_error_primary_doctor);
        tvErrorSecondaryDoctor = view.findViewById(R.id.tv_error_secondary_doctor);
        tvErrorBedNumber = view.findViewById(R.id.tv_error_bed_number);
        tvErrorLabourDiagnosedDate = view.findViewById(R.id.tv_labour_diagnosed_date_error);
        tvErrorLabourDiagnosedTime = view.findViewById(R.id.tv_labour_diagnosed_time_error);
        tvErrorRiskFactor = view.findViewById(R.id.tv_error_risk_factor);
        tvErrorHospital = view.findViewById(R.id.tv_error_hospital);


        cardAdmissionDate = view.findViewById(R.id.card_date_admission);
        cardAdmissionTime = view.findViewById(R.id.card_time_admission);
        cardTotalBirth = view.findViewById(R.id.card_total_birth);
        cardTotalMiscarraige = view.findViewById(R.id.card_total_miscarraige);
        cardSacRupturedDate = view.findViewById(R.id.card_sac_ruptured_date);
        cardSacRupturedTime = view.findViewById(R.id.card_sac_ruptured_time);
        cardPrimaryDoctor = view.findViewById(R.id.dropdown_primary_doctor);
        cardSecondaryDoctor = view.findViewById(R.id.dropdown_secondary_doctor);
        cardBedNumber = view.findViewById(R.id.card_bed_no);
        cardDiagnosedDate = view.findViewById(R.id.card_diagnosed_date);
        cardDiagnosedTime = view.findViewById(R.id.card_diagnosed_time);
        layoutErrorLabourOnset = view.findViewById(R.id.card_labour_onset);
        cardOptions = view.findViewById(R.id.card_options);
        cardHospitalOther = view.findViewById(R.id.card_hospital_other);


        mAdmissionDateTextView.addTextChangedListener(new MyTextWatcher(mAdmissionDateTextView));
        mAdmissionTimeTextView.addTextChangedListener(new MyTextWatcher(mAdmissionTimeTextView));
        mTotalBirthEditText.addTextChangedListener(new MyTextWatcher(mTotalBirthEditText));
        mTotalMiscarriageEditText.addTextChangedListener(new MyTextWatcher(mTotalMiscarriageEditText));
        mActiveLaborDiagnosedDateTextView.addTextChangedListener(new MyTextWatcher(mActiveLaborDiagnosedDateTextView));
        mActiveLaborDiagnosedTimeTextView.addTextChangedListener(new MyTextWatcher(mActiveLaborDiagnosedTimeTextView));
        mMembraneRupturedDateTextView.addTextChangedListener(new MyTextWatcher(mMembraneRupturedDateTextView));
        mMembraneRupturedTimeTextView.addTextChangedListener(new MyTextWatcher(mMembraneRupturedTimeTextView));
        mRiskFactorsTextView.addTextChangedListener(new MyTextWatcher(mRiskFactorsTextView));
        mPrimaryDoctorTextView.addTextChangedListener(new MyTextWatcher(mPrimaryDoctorTextView));
        mSecondaryDoctorTextView.addTextChangedListener(new MyTextWatcher(mSecondaryDoctorTextView));
        etBedNumber.addTextChangedListener(new MyTextWatcher(etBedNumber));

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
            optionMaternity.setTextColor(getResources().getColor(R.color.darkGray));
            optionOther.setTextColor(getResources().getColor(R.color.darkGray));
            mHospitalMaternityString = optionHospital.getText().toString();
        });
        optionMaternity.setOnClickListener(v -> {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionHospital.setTextColor(getResources().getColor(R.color.darkGray));
            optionMaternity.setTextColor(getResources().getColor(R.color.white));
            optionOther.setTextColor(getResources().getColor(R.color.darkGray));
            mHospitalMaternityString = optionMaternity.getText().toString();

        });
        optionOther.setOnClickListener(v -> {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionHospital.setTextColor(getResources().getColor(R.color.darkGray));
            optionMaternity.setTextColor(getResources().getColor(R.color.darkGray));
            optionOther.setTextColor(getResources().getColor(R.color.white));
            mHospitalMaternityString = optionOther.getText().toString();

        });

        tvSpontaneous.setOnClickListener(v -> {
            tvSpontaneous.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            tvInduced.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            tvSpontaneous.setTextColor(getResources().getColor(R.color.white));
            tvInduced.setTextColor(getResources().getColor(R.color.darkGray));
            mLaborOnsetString = tvSpontaneous.getText().toString();
        });
        tvInduced.setOnClickListener(v -> {
            tvSpontaneous.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            tvInduced.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            tvSpontaneous.setTextColor(getResources().getColor(R.color.darkGray));
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

        TextInputLayout etLayoutAdmissionDate, etLayoutAdmissionTime, etLabourDiagnosedDate, etLabourDiagnosedTime, etLayoutSacRupturedDate, etLayoutSacRupturedTime, etLayoutRiskFactors, etLayoutPrimaryDoctor, etLayoutSecondaryDoctor;
        etLayoutAdmissionDate = view.findViewById(R.id.etLayout_admission_date);
        etLayoutAdmissionTime = view.findViewById(R.id.etLayout_admission_time);
        etLabourDiagnosedDate = view.findViewById(R.id.etLayout_labor_diagnosed_date);
        etLabourDiagnosedTime = view.findViewById(R.id.etLayout_labor_diagnosed_time);
        etLayoutSacRupturedDate = view.findViewById(R.id.etLayout_sac_ruptured_date);
        etLayoutSacRupturedTime = view.findViewById(R.id.etLayout_sac_ruptured_time);
        etLayoutRiskFactors = view.findViewById(R.id.etLayout_risk_factors);
        etLayoutPrimaryDoctor = view.findViewById(R.id.etLayout_primary_doctor);
        etLayoutSecondaryDoctor = view.findViewById(R.id.etLayout_secondary_doctor);
        layoutSacRuptured = view.findViewById(R.id.card_sac_ruptured);


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
        mAdmissionDateTextView.setOnClickListener(v -> {
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
        mAdmissionTimeTextView.setOnClickListener(v -> {
            selectTimeForAllParameters("admissionTimeString");
        });

        mUnknownMembraneRupturedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: isChecked : " + isChecked);
                if (isChecked) {
                    isUnknownChecked = true;
                    //setenabled false  -- pending as per figma
                    mMembraneRupturedDateTextView.setEnabled(false);
                    mMembraneRupturedTimeTextView.setEnabled(false);
                    layoutSacRuptured.setVisibility(View.GONE);
                    mMembraneRupturedDateTextView.setText("");
                    mMembraneRupturedTimeTextView.setText("");

                } else {
                    isUnknownChecked = false;
                    layoutSacRuptured.setVisibility(View.VISIBLE);
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
        mActiveLaborDiagnosedDateTextView.setOnClickListener(v -> {
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
        mActiveLaborDiagnosedTimeTextView.setOnClickListener(v -> {
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
        mMembraneRupturedDateTextView.setOnClickListener(v -> {
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
        mMembraneRupturedTimeTextView.setOnClickListener(v -> {
            selectTimeForAllParameters("membraneRupturedTime");

        });
        etLayoutRiskFactors.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MultiChoiceDialogFragment<String> dialog1 = new MultiChoiceDialogFragment.Builder<String>(mContext).title(R.string.select_risk_factors).positiveButtonLabel(R.string.save_button).build();

                final String[] itemsArray = {"None", "under age 20", "Women over age 35", "Diabetes", "Obesity", "Underweight", "High blood pressure", "PCOS", "Kidney disease", "Thyroid disease", "Asthma", "Uterine fibroids"};
                List<String> items = Arrays.asList(itemsArray);

                dialog1.setAdapter(new RiskFactorMultiChoiceAdapter(mContext, new ArrayList<>(items)));
                dialog1.setListener(selectedItems -> {
                    if (selectedItems.size() > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < selectedItems.size(); i++) {
                            if (!stringBuilder.toString().isEmpty()) stringBuilder.append(",");
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
        mRiskFactorsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MultiChoiceDialogFragment<String> dialog1 = new MultiChoiceDialogFragment.Builder<String>(mContext).title(R.string.select_risk_factors).positiveButtonLabel(R.string.save_button).build();

                final String[] itemsArray = {"None", "under age 20", "Women over age 35", "Diabetes", "Obesity", "Underweight", "High blood pressure", "PCOS", "Kidney disease", "Thyroid disease", "Asthma", "Uterine fibroids"};
                List<String> items = Arrays.asList(itemsArray);

                dialog1.setAdapter(new RiskFactorMultiChoiceAdapter(mContext, new ArrayList<>(items)));
                dialog1.setListener(selectedItems -> {
                    if (selectedItems.size() > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < selectedItems.size(); i++) {
                            if (!stringBuilder.toString().isEmpty()) stringBuilder.append(",");
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
        mPrimaryDoctorTextView.setOnClickListener(v -> selectPrimaryDoctor());

        etLayoutSecondaryDoctor.setEndIconOnClickListener(v -> {
            selectSecondaryDoctor();
        });
        mSecondaryDoctorTextView.setOnClickListener(v -> {
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
        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, mContext), String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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

    @SuppressLint("UseCompatLoadingForDrawables")
    public void onPatientCreateClicked() {
        if (TextUtils.isEmpty(mAdmissionDateTextView.getText().toString())) {
            mAdmissionDateTextView.requestFocus();

            tvErrorAdmissionDate.setVisibility(View.VISIBLE);
            tvErrorAdmissionDate.setText(getString(R.string.select_admission_date));
            cardAdmissionDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

            return;
        } else {
            tvErrorAdmissionDate.setVisibility(View.GONE);
            cardAdmissionDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(mAdmissionTimeTextView.getText().toString())) {
            mAdmissionTimeTextView.requestFocus();
            tvErrorAdmissionDate.setVisibility(View.INVISIBLE);
            tvErrorAdmissionTime.setVisibility(View.VISIBLE);
            tvErrorAdmissionTime.setText(getString(R.string.select_admission_time));
            cardAdmissionTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorAdmissionDate.setVisibility(View.GONE);
            tvErrorAdmissionTime.setVisibility(View.GONE);
            cardAdmissionTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

        }

        if (TextUtils.isEmpty(mTotalBirthEditText.getText().toString())) {
            mTotalBirthEditText.requestFocus();

            tvErrorTotalBirth.setVisibility(View.VISIBLE);
            tvErrorTotalBirth.setText(getString(R.string.total_birth_count_val_txt));
            cardTotalBirth.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorTotalBirth.setVisibility(View.GONE);
            cardTotalBirth.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(mTotalMiscarriageEditText.getText().toString())) {
            mTotalMiscarriageEditText.requestFocus();
            tvErrorTotalBirth.setVisibility(View.INVISIBLE);

            tvErrorTotalMiscarriage.setVisibility(View.VISIBLE);
            tvErrorTotalMiscarriage.setText(getString(R.string.total_miscarriage_count_val_txt));
            cardTotalMiscarraige.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorTotalBirth.setVisibility(View.GONE);
            tvErrorTotalMiscarriage.setVisibility(View.GONE);
            cardTotalMiscarraige.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }

        if (mLaborOnsetString.isEmpty()) {
            tvSpontaneous.requestFocus();
            tvInduced.requestFocus();

            tvErrorLabourOnset.setVisibility(View.VISIBLE);
            tvErrorLabourOnset.setText(getString(R.string.labor_onset_val_txt));
            tvSpontaneous.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));
            tvInduced.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));

            return;

        } else {
            tvErrorLabourOnset.setVisibility(View.GONE);
            getLabourOnsetValue(mLaborOnsetString);
        }
        if (TextUtils.isEmpty(mActiveLaborDiagnosedDateTextView.getText().toString())) {
            mActiveLaborDiagnosedDateTextView.requestFocus();

            tvErrorLabourDiagnosedDate.setVisibility(View.VISIBLE);
            tvErrorLabourDiagnosedDate.setText(getString(R.string.active_labor_diagnosed_date_val_txt));
            cardDiagnosedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorLabourDiagnosedDate.setVisibility(View.GONE);
            cardDiagnosedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(mActiveLaborDiagnosedTimeTextView.getText().toString())) {
            mActiveLaborDiagnosedTimeTextView.requestFocus();
            tvErrorLabourDiagnosedDate.setVisibility(View.INVISIBLE);
            tvErrorLabourDiagnosedTime.setVisibility(View.VISIBLE);
            tvErrorLabourDiagnosedTime.setText(getString(R.string.active_labor_diagnosed_time_val_txt));
            cardDiagnosedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorLabourDiagnosedDate.setVisibility(View.GONE);
            tvErrorLabourDiagnosedTime.setVisibility(View.GONE);
            cardDiagnosedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (!isUnknownChecked) {
            if (TextUtils.isEmpty(mMembraneRupturedDateTextView.getText().toString())) {
                mMembraneRupturedDateTextView.requestFocus();

                tvErrorSacRupturedDate.setVisibility(View.VISIBLE);
                tvErrorSacRupturedDate.setText(getString(R.string.select_sac_ruptured_date));
                cardSacRupturedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                return;

            } else {
                tvErrorSacRupturedDate.setVisibility(View.GONE);
                cardSacRupturedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
            }
        }
        if (!isUnknownChecked) {
            if (TextUtils.isEmpty(mMembraneRupturedTimeTextView.getText().toString())) {
                mMembraneRupturedTimeTextView.requestFocus();
                tvErrorSacRupturedDate.setVisibility(View.INVISIBLE);
                tvErrorSacRupturedTime.setVisibility(View.VISIBLE);
                tvErrorSacRupturedTime.setText(getString(R.string.select_sac_ruptured_time));
                cardSacRupturedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                return;

            } else {
                tvErrorSacRupturedDate.setVisibility(View.GONE);
                tvErrorSacRupturedTime.setVisibility(View.GONE);
                cardSacRupturedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
            }

        }


        if (TextUtils.isEmpty(mRiskFactorsTextView.getText().toString())) {
            mRiskFactorsTextView.requestFocus();

            tvErrorRiskFactor.setVisibility(View.VISIBLE);
            tvErrorRiskFactor.setText(getString(R.string.please_select_risk_factor));
            dropdownRiskFactors.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorRiskFactor.setVisibility(View.GONE);
            dropdownRiskFactors.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (mHospitalMaternityString.isEmpty()) {
            tvErrorHospital.setVisibility(View.VISIBLE);
            tvErrorHospital.setText(getString(R.string.hospital_matermnity_val_txt));
            //optionHospital.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));
            //optionMaternity.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));
            // optionOther.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));

            return;

        } else if (mHospitalMaternityString.equalsIgnoreCase("other")) {
            etHospitalOther.setVisibility(View.VISIBLE);
            tvErrorHospital.setVisibility(View.VISIBLE);
            tvErrorHospital.setText(getString(R.string.enter_hospital_other_error));
            //cardOptions.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
        } else {
            etHospitalOther.setVisibility(View.GONE);
            tvErrorHospital.setVisibility(View.GONE);
            //cardOptions.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(mPrimaryDoctorTextView.getText().toString())) {
            mPrimaryDoctorTextView.requestFocus();

            tvErrorPrimaryDoctor.setVisibility(View.VISIBLE);
            tvErrorPrimaryDoctor.setText(getString(R.string.select_primary_doctor));
            cardPrimaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorPrimaryDoctor.setVisibility(View.GONE);
            cardPrimaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(mSecondaryDoctorTextView.getText().toString())) {
            mSecondaryDoctorTextView.requestFocus();

            tvErrorSecondaryDoctor.setVisibility(View.VISIBLE);
            tvErrorSecondaryDoctor.setText(getString(R.string.select_secondary_doctor));
            cardSecondaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorSecondaryDoctor.setVisibility(View.GONE);
            cardSecondaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }
        if (TextUtils.isEmpty(etBedNumber.getText().toString())) {
            etBedNumber.requestFocus();

            tvErrorBedNumber.setVisibility(View.VISIBLE);
            tvErrorBedNumber.setText(getString(R.string.enter_bed_no));
            cardBedNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
            return;

        } else {
            tvErrorBedNumber.setVisibility(View.GONE);
            cardBedNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
        }

        //code for adding to the database

        mTotalBirthCount = mTotalBirthEditText.getText().toString().trim();
        mTotalMiscarriageCount = mTotalMiscarriageEditText.getText().toString().trim();

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();


        //wrong uuid

        if (fromSummary && patientUuidUpdate != null && !patientUuidUpdate.isEmpty()) {
            uuid = patientUuidUpdate;
        } else {
            uuid = UUID.randomUUID().toString();

        }

        patientDTO.setUuid(uuid);
        Gson gson = new Gson();

        boolean cancel = false;
        View focusView = null;
        //mLaborOnsetString = "Spontaneous";
        ///mHospitalMaternityString = "Hospital";

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
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.ADMISSION_DATE.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mAdmissionDateString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Admission_Time
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.ADMISSION_TIME.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mAdmissionTimeString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Parity
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.PARITY.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mTotalBirthCount + "," + mTotalMiscarriageCount));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Labor Onset
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.LABOR_ONSET.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mLaborOnsetString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Active Labor Diagnosed
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.ACTIVE_LABOR_DIAGNOSED.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mActiveLaborDiagnosedDate + " " + mActiveLaborDiagnosedTime));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Membrane Ruptured Timestamp
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.MEMBRANE_RUPTURED_TIMESTAMP.value));
            patientAttributesDTO.setValue(mUnknownMembraneRupturedCheckBox.isChecked() ? "U" : StringUtils.getValue(mMembraneRupturedDate + " " + mMembraneRupturedTime));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Risk factors
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.RISK_FACTORS.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mRiskFactorsString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Hospital_Maternity
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.HOSPITAL_MATERNITY.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mHospitalMaternityString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //PrimaryDoctor
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.PRIMARY_DOCTOR.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mPrimaryDoctorUUIDString) + "@#@" + mPrimaryDoctorTextView.getText());
            patientAttributesDTOList.add(patientAttributesDTO);

            //SecondaryDoctor
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.SECONDARY_DOCTOR.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mSecondaryDoctorUUIDString) + "@#@" + mSecondaryDoctorTextView.getText());
            patientAttributesDTOList.add(patientAttributesDTO);

            //Ezazi Registration Number
            int number = (int) (Math.random() * (99999999 - 100 + 1) + 100);
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.REGISTRATION_NUMBER.value));
            patientAttributesDTO.setValue(patientDTO.getCountry().substring(0, 2) + "/" + patientDTO.getStateprovince().substring(0, 2) + "/" + patientDTO.getCityvillage().substring(0, 2) + "/" + String.valueOf(number));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Bed number  -new flow
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.BED_NUMBER.value));
            patientAttributesDTO.setValue(StringUtils.getValue(etBedNumber.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            /*new*/
            //AlternateNo
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.ALTERNATE_NO.value));
            patientAttributesDTO.setValue(StringUtils.getValue(mAlternateNumberString));
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
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute(PatientAttributesDTO.Columns.PROFILE_IMG_TIMESTAMP.value));
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
            Logger.logD(TAG, "buPatientAttrite list size" + patientAttributesDTOList.size());
            patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);
            patientDTO.setSyncd(false);
            Logger.logD("patient json : ", "Json : " + gson.toJson(patientDTO, PatientDTO.class));

        }

        try {

            //updatePatientDetails
            Log.d(TAG, "onPatientCreateClicked: fromSummary : " + fromSummary);
            Log.d(TAG, "onPatientCreateClicked: uuid : " + uuid);

            if (fromSummary) {
                boolean isPatientUpdated = patientsDAO.updatePatientToDBNew(patientDTO, uuid, patientAttributesDTOList);
                boolean isPatientImageUpdated = imagesDAO.updatePatientProfileImages(patientDTO.getPatientPhoto(), uuid);

                if (NetworkConnection.isOnline(getActivity().getApplication())) {
                    SyncDAO syncDAO = new SyncDAO();
                    ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                    boolean ispush = syncDAO.pushDataApi();
                    boolean isPushImage = imagesPushDAO.patientProfileImagesPush();

//                if (ispush)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s data not uploaded.", 2, getApplication());

//                if (isPushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s Image not complete.", 4, getApplication());

                }
                if (isPatientUpdated && isPatientImageUpdated) {
                    Log.d(TAG, "99onPatientCreateClicked:update uuid : " + uuid);
                    Logger.logD(TAG, "updated");
                    Intent i = new Intent(getActivity().getApplication(), PatientDetailActivity.class);
                    i.putExtra("patientUuid", uuid);
                    i.putExtra("patientName", patientDTO.getFirstname() + " " + patientDTO.getLastname());
                    i.putExtra("tag", "newPatient");
                    i.putExtra("hasPrescription", "false");
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getActivity().getApplication().startActivity(i);
                }
            } else {
                boolean isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, uuid);
                boolean isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), uuid);
                if (NetworkConnection.isOnline(mContext)) {
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
                    Log.d(TAG, "99onPatientCreateClicked:add uuid : " + uuid);

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
            }


        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

//    private PatientAttributesDTO createPatientAttribute(String attrTypeUuid, String value) {
//        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(uuid);
//        patientAttributesDTO.setPersonAttributeTypeUuid(attrTypeUuid);
//        patientAttributesDTO.setValue(value);
//
//        return patientAttributesDTO;
//    }

    private void onBackInsertIntopatientDTO() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        bundle.putBoolean("fromThirdScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        bundle.putBoolean("editDetails", true);
        bundle.putString("mAlternateNumberString", mAlternateNumberString);
        bundle.putBoolean("fromSummary", fromSummary);
        bundle.putString("patientUuidUpdate", patientUuidUpdate);

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

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext).title(R.string.select_primary_doctor).positiveButtonLabel(R.string.save_button).content(Arrays.asList(mDoctorNames)).build();

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

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext).title(R.string.select_secondary_doctor).positiveButtonLabel(R.string.save_button).content(Arrays.asList(mDoctorNames)).build();

        dialog.setListener((position, value) -> {
            Log.d(TAG, "selectSecondaryDoctor: position : " + position);
            Log.d(TAG, "selectSecondaryDoctor: value : " + value);
            mSecondaryDoctorUUIDString = mDoctorUUIDs.get(position);
            mSecondaryDoctorTextView.setText(mDoctorNames[position]);
        });

        dialog.show(requireFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void selectTimeForAllParameters(String forWhichParameter) {
        ThemeTimePickerDialog dialog = new ThemeTimePickerDialog.Builder(mContext).title(R.string.current_time).positiveButtonLabel(R.string.ok).build();
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

            getLabourOnsetValue(mLaborOnsetString);
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

            getHospitalMaternityValue(mHospitalMaternityString);
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

        //Bed number
        //new flow
        try {
            etBedNumber.setText(getBedNumber(patient.getUuid()));
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private void getHospitalMaternityValue(String mHospitalMaternityString) {
        if (mHospitalMaternityString.equalsIgnoreCase("Hospital")) {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionHospital.setTextColor(getResources().getColor(R.color.white));
            optionMaternity.setTextColor(getResources().getColor(R.color.gray));
            optionOther.setTextColor(getResources().getColor(R.color.gray));
            mHospitalMaternityString = optionHospital.getText().toString().trim();
        } else if (mHospitalMaternityString.equalsIgnoreCase("Maternity")) {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionHospital.setTextColor(getResources().getColor(R.color.gray));
            optionMaternity.setTextColor(getResources().getColor(R.color.white));
            optionOther.setTextColor(getResources().getColor(R.color.gray));
            mHospitalMaternityString = optionMaternity.getText().toString().trim();
        } else {
            optionHospital.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionMaternity.setBackground(getResources().getDrawable(R.drawable.button_bg_rounded_corners));
            optionOther.setBackground(getResources().getDrawable(R.drawable.button_primary_rounded));
            optionHospital.setTextColor(getResources().getColor(R.color.gray));
            optionMaternity.setTextColor(getResources().getColor(R.color.gray));
            optionOther.setTextColor(getResources().getColor(R.color.white));
            mHospitalMaternityString = etHospitalOther.getText().toString();

        }
    }

    private void getLabourOnsetValue(String mLaborOnsetString) {

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


    class MyTextWatcher implements TextWatcher {
        EditText editText;

        MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            if (this.editText.getId() == R.id.et_admission_date) {
                if (val.isEmpty()) {
                    tvErrorAdmissionDate.setVisibility(View.VISIBLE);
                    tvErrorAdmissionDate.setText(getString(R.string.select_admission_date));
                    cardAdmissionDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                } else {
                    tvErrorAdmissionDate.setVisibility(View.GONE);
                    cardAdmissionDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.et_admission_time) {
                if (val.isEmpty()) {
                    tvErrorAdmissionDate.setVisibility(View.INVISIBLE);
                    tvErrorAdmissionTime.setVisibility(View.VISIBLE);
                    tvErrorAdmissionTime.setText(getString(R.string.select_admission_time));
                    cardAdmissionTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                } else {
                    tvErrorAdmissionTime.setVisibility(View.GONE);
                    tvErrorAdmissionDate.setVisibility(View.GONE);

                    cardAdmissionTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.et_total_birth) {
                if (val.isEmpty()) {
                    tvErrorTotalBirth.setVisibility(View.VISIBLE);
                    tvErrorTotalBirth.setText(getString(R.string.total_birth_count_val_txt));
                    cardTotalBirth.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));


                } else {
                    tvErrorTotalBirth.setVisibility(View.GONE);
                    cardTotalBirth.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.et_total_miscarriage) {
                if (val.isEmpty()) {
                    tvErrorTotalMiscarriage.setVisibility(View.VISIBLE);
                    tvErrorTotalMiscarriage.setText(getString(R.string.total_miscarriage_count_val_txt));
                    cardTotalMiscarraige.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                } else {
                    tvErrorTotalMiscarriage.setVisibility(View.GONE);
                    cardTotalMiscarraige.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
                }
            } else if ((this.editText.getId() == R.id.et_spontaneous) || (this.editText.getId() == R.id.et_induced)) {//labour onset
                if (val.isEmpty()) {

                    tvErrorLabourOnset.setVisibility(View.VISIBLE);
                    tvErrorLabourOnset.setText(getString(R.string.labor_onset_val_txt));
                    tvSpontaneous.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));
                    tvInduced.setBackground(ContextCompat.getDrawable(mContext, R.drawable.error_bg_et));

                } else {
                    tvErrorLabourOnset.setVisibility(View.GONE);
                    tvSpontaneous.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_bg_rounded_corners));
                    tvInduced.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_bg_rounded_corners));

                }
            } else if (this.editText.getId() == R.id.et_labor_diagnosed_date) {
                if (val.isEmpty()) {

                    tvErrorLabourDiagnosedDate.setVisibility(View.VISIBLE);
                    tvErrorLabourDiagnosedDate.setText(getString(R.string.active_labor_diagnosed_date_val_txt));
                    cardDiagnosedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                } else {
                    tvErrorLabourDiagnosedDate.setVisibility(View.GONE);
                    cardDiagnosedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.et_labor_diagnosed_time) {
                if (val.isEmpty()) {
                    tvErrorLabourDiagnosedDate.setVisibility(View.INVISIBLE);
                    tvErrorLabourDiagnosedTime.setVisibility(View.VISIBLE);
                    tvErrorLabourDiagnosedTime.setText(getString(R.string.active_labor_diagnosed_time_val_txt));
                    cardDiagnosedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                } else {
                    tvErrorLabourDiagnosedTime.setVisibility(View.GONE);
                    tvErrorLabourDiagnosedDate.setVisibility(View.GONE);

                    cardDiagnosedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
                }
            } else if (this.editText.getId() == R.id.et_sac_ruptured_date) {
                if (!isUnknownChecked) {
                    if (val.isEmpty()) {
                        tvErrorSacRupturedDate.setVisibility(View.VISIBLE);
                        tvErrorSacRupturedDate.setText(getString(R.string.select_sac_ruptured_date));
                        cardSacRupturedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                    } else {
                        tvErrorSacRupturedDate.setVisibility(View.GONE);
                        cardSacRupturedDate.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
                    }
                }
            } else if (this.editText.getId() == R.id.et_sac_ruptured_time) {
                if (!isUnknownChecked) {
                    if (val.isEmpty()) {
                        tvErrorSacRupturedDate.setVisibility(View.INVISIBLE);
                        tvErrorSacRupturedTime.setVisibility(View.VISIBLE);
                        tvErrorSacRupturedTime.setText(getString(R.string.select_sac_ruptured_time));
                        cardSacRupturedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));

                    } else {
                        tvErrorSacRupturedDate.setVisibility(View.GONE);
                        tvErrorSacRupturedTime.setVisibility(View.GONE);
                        cardSacRupturedTime.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                    }
                }

            } else if (this.editText.getId() == R.id.autotv_risk_factors) {
                if (val.isEmpty()) {

                    tvErrorRiskFactor.setVisibility(View.VISIBLE);
                    tvErrorRiskFactor.setText(getString(R.string.please_select_risk_factor));
                    dropdownRiskFactors.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                } else {
                    tvErrorPrimaryDoctor.setVisibility(View.GONE);
                    dropdownRiskFactors.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.autotv_primary_doctor) {
                if (val.isEmpty()) {

                    tvErrorPrimaryDoctor.setVisibility(View.VISIBLE);
                    tvErrorPrimaryDoctor.setText(getString(R.string.select_primary_doctor));
                    cardPrimaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                } else {
                    tvErrorPrimaryDoctor.setVisibility(View.GONE);
                    cardPrimaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.autotv_secondary_doctor) {
                if (val.isEmpty()) {

                    tvErrorSecondaryDoctor.setVisibility(View.VISIBLE);
                    tvErrorSecondaryDoctor.setText(getString(R.string.secondary_doctor));
                    cardSecondaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                } else {
                    tvErrorSecondaryDoctor.setVisibility(View.GONE);
                    cardSecondaryDoctor.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            } else if (this.editText.getId() == R.id.et_bed_number) {
                if (val.isEmpty()) {

                    tvErrorBedNumber.setVisibility(View.VISIBLE);
                    tvErrorBedNumber.setText(getString(R.string.enter_bed_no));
                    cardBedNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
                } else {
                    tvErrorBedNumber.setVisibility(View.GONE);
                    cardBedNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));

                }
            }
        }
    }

    private String getBedNumber(String patientuuid) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String bedNumber = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='d0786817-68d9-4226-b311-3de68d534b9e' ", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    bedNumber = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();

        return bedNumber;
    }

    private void updatePatientDetails(Patient patientdto, String uuid, List<PatientAttributesDTO> patientAttributesDTOList) {
        try {
            Logger.logD(TAG, "update ");
            boolean isPatientUpdated = patientsDAO.updatePatientToDB(patientdto, uuid, patientAttributesDTOList);
            boolean isPatientImageUpdated = imagesDAO.updatePatientProfileImages(patientdto.getPatient_photo(), uuid);

            if (NetworkConnection.isOnline(getActivity().getApplication())) {
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean ispush = syncDAO.pushDataApi();
                boolean isPushImage = imagesPushDAO.patientProfileImagesPush();

//                if (ispush)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s data not uploaded.", 2, getApplication());

//                if (isPushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s Image not complete.", 4, getApplication());

            }
            if (isPatientUpdated && isPatientImageUpdated) {
                Logger.logD(TAG, "updated");
                Intent i = new Intent(getActivity().getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirst_name() + " " + patientdto.getLast_name());
                i.putExtra("tag", "newPatient");
                i.putExtra("hasPrescription", "false");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().getApplication().startActivity(i);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
}