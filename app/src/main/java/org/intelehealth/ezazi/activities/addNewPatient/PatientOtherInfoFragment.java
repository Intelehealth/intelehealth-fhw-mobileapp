package org.intelehealth.ezazi.activities.addNewPatient;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import org.intelehealth.ezazi.ui.dialog.ThemeTimePickerDialog;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class PatientOtherInfoFragment extends Fragment {
    private static final String TAG = "PatientPersonalInfoFrag";
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
    ImageView ivPersonal, ivAddress, ivOther;
    TextView tvSpontaneous, tvInduced, tvSacRuptured;
    int MY_REQUEST_CODE = 5555;
    int dob_indexValue = 15;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_patient_other_info, container, false);
        mContext = getActivity();
        sessionManager = new SessionManager(mContext);

        initUI();
        return view;
    }

    private void initUI() {
        ivPersonal = getActivity().findViewById(R.id.iv_personal_info);
        ivAddress = getActivity().findViewById(R.id.iv_address_info);
        ivOther = getActivity().findViewById(R.id.iv_other_info);

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
        tvSacRuptured = view.findViewById(R.id.textView8);


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
                etLabourDiagnosedDate, etLabourDiagnosedTime, etLayoutSacRupturedDate, etLayoutSacRupturedTime;
        etLayoutAdmissionDate = view.findViewById(R.id.etLayout_admission_date);
        etLayoutAdmissionTime = view.findViewById(R.id.etLayout_admission_time);
        etLabourDiagnosedDate = view.findViewById(R.id.etLayout_labor_diagnosed_date);
        etLabourDiagnosedTime = view.findViewById(R.id.etLayout_labor_diagnosed_time);
        etLayoutSacRupturedDate = view.findViewById(R.id.etLayout_sac_ruptured_date);
        etLayoutSacRupturedTime = view.findViewById(R.id.etLayout_sac_ruptured_time);

        etLayoutAdmissionDate.setEndIconOnClickListener(v -> {
          /*  Calendar mCalendar = Calendar.getInstance();
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, R.style.DatePicker_Theme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(year, month, dayOfMonth);
                    Date date = cal.getTime();

                    mAdmissionDateString = simpleDateFormat.format(date);
                    mAdmissionDateTextView.setText(mAdmissionDateString);
                }
            }, year, month, dayOfMonth);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();*/

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
           /* ThemeTimePickerDialog dialog = new ThemeTimePickerDialog.Builder(mContext)
                    .title(R.string.current_time)
                    .positiveButtonLabel(R.string.ok)
                    .build();
            dialog.setListener((hours, minutes, amPm, value) -> {
                Log.d("ThemeTimePickerDialog", "value : " + value);
            });
            dialog.show(Objects.requireNonNull(getFragmentManager()), "ThemeTimePickerDialog");
*/
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, R.style.DatePicker_Theme,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            boolean isPM = (hourOfDay >= 12);
                            mAdmissionTimeString = String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                            mAdmissionTimeTextView.setText(mAdmissionTimeString);
                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        mUnknownMembraneRupturedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: isChecked : " + isChecked);
                if (isChecked) {
                    etLayoutSacRupturedDate.setVisibility(View.GONE);
                    etLayoutSacRupturedTime.setVisibility(View.GONE);
                    tvSacRuptured.setVisibility(View.GONE);
                } else {
                    etLayoutSacRupturedDate.setVisibility(View.VISIBLE);
                    etLayoutSacRupturedTime.setVisibility(View.VISIBLE);
                    tvSacRuptured.setVisibility(View.VISIBLE);

                }
            }
        });
        etLabourDiagnosedDate.setEndIconOnClickListener(v -> {
           /* Calendar mCalendar = Calendar.getInstance();
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, R.style.DatePicker_Theme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(year, month, dayOfMonth);
                    Date date = cal.getTime();

                    mActiveLaborDiagnosedDate = simpleDateFormat.format(date);
                    mActiveLaborDiagnosedDateTextView.setText(mActiveLaborDiagnosedDate);
                }
            }, year, month, dayOfMonth);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
      */

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
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, R.style.DatePicker_Theme,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            boolean isPM = (hourOfDay >= 12);
                            mActiveLaborDiagnosedTime = String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                            mActiveLaborDiagnosedTimeTextView.setText(mActiveLaborDiagnosedTime);
                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        etLayoutSacRupturedDate.setEndIconOnClickListener(v -> {
          /*  Calendar mCalendar = Calendar.getInstance();
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, R.style.DatePicker_Theme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(year, month, dayOfMonth);
                    Date date = cal.getTime();

                    mMembraneRupturedDate = simpleDateFormat.format(date);
                    mMembraneRupturedDateTextView.setText(mMembraneRupturedDate);
                }
            }, year, month, dayOfMonth);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });*/

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
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, R.style.DatePicker_Theme,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            boolean isPM = (hourOfDay >= 12);
                            mMembraneRupturedTime = String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                            mMembraneRupturedTimeTextView.setText(mMembraneRupturedTime);

                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        });
        mRiskFactorsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = {"None", "under age 20", "Women over age 35", "Diabetes", "Obesity", "Underweight",
                        "High blood pressure", "PCOS", "Kidney disease", "Thyroid disease", "Asthma", "Uterine fibroids"};
                boolean[] selectedItems = new boolean[items.length];
                for (int i = 0; i < items.length; i++) {
                    selectedItems[i] = mSelectedRiskFactorList.contains(items[i]);
                }
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);

                builder.setTitle("Select Risk Factors")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setMultiChoiceItems(items, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int itemIndex, boolean isChecked) {
                                Log.i("Dialogos", "Opción elegida: " + items[itemIndex]);
                                if (isChecked) {
                                    if (itemIndex == 0) {
                                        mSelectedRiskFactorList.clear();
                                    } else {
                                        mSelectedRiskFactorList.remove(items[0]);
                                    }
                                    if (!mSelectedRiskFactorList.contains(items[itemIndex]))
                                        mSelectedRiskFactorList.add(items[itemIndex]);
                                } else {
                                    mSelectedRiskFactorList.remove(items[itemIndex]);
                                }
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < mSelectedRiskFactorList.size(); i++) {
                                    if (!stringBuilder.toString().isEmpty())
                                        stringBuilder.append(",");
                                    stringBuilder.append(mSelectedRiskFactorList.get(i));

                                }
                                mRiskFactorsString = stringBuilder.toString();
                                mRiskFactorsTextView.setText(mRiskFactorsString);
                            }
                        });


                builder.create().show();
            }
        });

        mPrimaryDoctorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);

                builder.setTitle("Select Primary Doctor")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setSingleChoiceItems(mDoctorNames, selectedId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPrimaryDoctorUUIDString = mDoctorUUIDs.get(which);
                                mPrimaryDoctorTextView.setText(mDoctorNames[which]);
                            }
                        });
                builder.create().show();
            }
        });

        mSecondaryDoctorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);

                builder.setTitle("Select Secondary Doctor")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setSingleChoiceItems(mDoctorNames, selectedId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSecondaryDoctorUUIDString = mDoctorUUIDs.get(which);
                                mSecondaryDoctorTextView.setText(mDoctorNames[which]);
                            }
                        });
                builder.create().show();
            }
        });

     /*   mLaborOnsetRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbLOSpontaneous) {
                    mLaborOnsetString = "Spontaneous";
                } else if (checkedId == R.id.rbLOInduced) {
                    mLaborOnsetString = "Induced";
                }
            }
        });*/

/*
        mHospitalMaternityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mOthersEditText.setVisibility(View.GONE);
                if (checkedId == R.id.rbHospital) {
                    mHospitalMaternityString = "Hospital";
                } else if (checkedId == R.id.rbMaternity) {
                    mHospitalMaternityString = "Maternity";
                } else {
                    mHospitalMaternityString = " ";
                    mOthersEditText.setVisibility(View.VISIBLE);
//                    mOthersEditText.setText(mHospitalMaternityString);
                }
            }
        });
*/
        /*end*/

        i_privacy = getActivity().getIntent();
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.


//Initialize the local database to store patient information

        Intent intent = getActivity().getIntent(); // The intent was passed to the activity
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
        }
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

        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    /*
        private void showRiskFactorsDialog() {
            final String[] itemsArray = {"None", "under age 20", "Women over age 35", "Diabetes", "Obesity", "Underweight",
                    "High blood pressure", "PCOS", "Kidney disease", "Thyroid disease", "Asthma", "Uterine fibroids"};

            ArrayList<MultiChoiceItem> riskFactorItems = new ArrayList<>();
            for (int i = 0; i < itemsArray.length; i++) {
                RiskFactorsModel riskFactorsModel = new RiskFactorsModel(itemsArray[i], i);
                riskFactorItems.add(riskFactorsModel);
            }

            //SelectAllMultiChoice selectAll = new SelectAllMultiChoice();
            //selectAll.setHeader("Select All");
            // items.add(0, selectAll);
            MultiChoiceDialogFragment<MultiChoiceItem> dialog = new MultiChoiceDialogFragment.Builder<MultiChoiceItem>
                    (mContext)
                    .title(R.string.select_risk_factors)
                    .positiveButtonLabel(R.string.save_button)
                    .build();

            dialog.setAdapter(new PatientMultiChoiceAdapter(mContext, riskFactorItems));
            dialog.setListener(selectedItems -> {
                //your code after multi selection
                //  if (selectedItems.size() > 0) showNextShiftNursesDialog(selectedItems);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < riskFactorItems.size(); i++) {
                    if (!stringBuilder.toString().isEmpty())
                        stringBuilder.append(",");
                    stringBuilder.append(riskFactorItems.get(i));

                }
                String mRiskFactorsString = stringBuilder.toString();
                Log.d(TAG, "showRiskFactorsDialog: mRiskFactorsString :" + mRiskFactorsString);

            });

            dialog.show(Objects.requireNonNull(getFragmentManager()), MultiChoiceDialogFragment.class.getCanonicalName());
        }
    */
    public void generateUuid() {

        patientUuid = uuidGenerator.UuidGenerator();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ivPersonal.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_info_done));
        ivAddress.setImageDrawable(getResources().getDrawable(R.drawable.ic_address_done));
        ivOther.setImageDrawable(getResources().getDrawable(R.drawable.ic_other_info_active));
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
            //
            //   //temp comment  boolean isPatientImageInserted = imagesDAO.insertPatientProfileImages(mCurrentPhotoPath, uuid);

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

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, secondScreen)
                .commit();
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
}