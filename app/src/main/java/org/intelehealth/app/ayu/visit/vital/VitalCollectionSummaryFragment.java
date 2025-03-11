package org.intelehealth.app.ayu.visit.vital;

import static org.intelehealth.app.app.AppConstants.RISK_LIMIT_SPO2;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.visit.staticEnabledFields.VitalsEnabledFieldsHelper;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.model.BMIStatus;
import org.intelehealth.app.ayu.visit.model.VitalsWrapper;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository;
import org.intelehealth.config.presenter.fields.factory.PatientVitalViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.PatientVital;
import org.intelehealth.config.utility.PatientVitalConfigKeys;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VitalCollectionSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VitalCollectionSummaryFragment extends Fragment {
    private static final String TAG = VitalCollectionSummaryFragment.class.getSimpleName();

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;
    private List<PatientVital> mPatientVitalList;

    private long mLastClickTime = 0;
    private String visitUuid;


    private LinearLayout mHeightLinearLayout, mWeightLinearLayout, mBMILinearLayout, mBPLinearLayout, mPulseLinearLayout, mTemperatureLinearLayout, mSpo2LinearLayout, mRespiratoryRateLinearLayout, mHaemoglobinLinearLayout, mSugarRandomLinearLayout, mBloodGroupLinearLayout;

    public VitalCollectionSummaryFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionSummaryFragment newInstance(VitalsWrapper result, boolean isEditMode) {
        VitalCollectionSummaryFragment fragment = new VitalCollectionSummaryFragment();
        fragment.mVitalsObject = result.getVitalsObject();
        fragment.visitUuid = result.getVisitUUID();
        fragment.mIsEditMode = isEditMode;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //config viewmodel initialization
        PatientVitalRepository repository = new PatientVitalRepository(ConfigDatabase.getInstance(requireActivity()).patientVitalDao());
        PatientVitalViewModelFactory factory = new PatientVitalViewModelFactory(repository);
        PatientVitalViewModel patientVitalViewModel = new ViewModelProvider(this, factory).get(PatientVitalViewModel.class);
        //requireActivity();
//        patientVitalViewModel.getAllEnabledLiveFields().observe(requireActivity(), it -> {
        mPatientVitalList = getStaticVitalsEnabledFields();
        //Timber.tag(TAG).v(new Gson().toJson(mPatientVitalList));
        updateUI();
//                }
//        );
    }

    private List<PatientVital> getStaticVitalsEnabledFields() {
        return VitalsEnabledFieldsHelper.INSTANCE.getStaticVitalsEnabledFields();
    }

    private void updateUI() {
        mHeightLinearLayout.setVisibility(View.GONE);
        mWeightLinearLayout.setVisibility(View.GONE);
        mBMILinearLayout.setVisibility(View.GONE);
        mBPLinearLayout.setVisibility(View.GONE);
        mPulseLinearLayout.setVisibility(View.GONE);
        mTemperatureLinearLayout.setVisibility(View.GONE);
        mSpo2LinearLayout.setVisibility(View.GONE);
        mRespiratoryRateLinearLayout.setVisibility(View.GONE);
        mHaemoglobinLinearLayout.setVisibility(View.GONE);
        mSugarRandomLinearLayout.setVisibility(View.GONE);

        mBloodGroupLinearLayout.setVisibility(View.GONE);
        for (PatientVital patientVital : mPatientVitalList) {
            CustomLog.v(TAG, patientVital.getName() + "\t" + patientVital.getVitalKey());

            if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.HEIGHT)) {
                mHeightLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.WEIGHT)) {
                mWeightLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.BMI)) {
                mBMILinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SBP) || patientVital.getVitalKey().equals(PatientVitalConfigKeys.DBP)) {
                mBPLinearLayout.setVisibility(View.VISIBLE);
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.PULSE)) {
                mPulseLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.TEMPERATURE)) {
                mTemperatureLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SPO2)) {
                mSpo2LinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.RESPIRATORY_RATE)) {
                mRespiratoryRateLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.BLOOD_TYPE)) {
                mBloodGroupLinearLayout.setVisibility(View.VISIBLE);
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.HAEMOGLOBIN)) {
                mHaemoglobinLinearLayout.setVisibility(View.VISIBLE);
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SUGAR_RANDOM)) {
                mSugarRandomLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_collection_summary, container, false);

        mHeightLinearLayout = view.findViewById(R.id.ll_height_container);
        mWeightLinearLayout = view.findViewById(R.id.ll_weight_container);
        mBMILinearLayout = view.findViewById(R.id.ll_bmi);
        mBPLinearLayout = view.findViewById(R.id.ll_bp_container);
        mPulseLinearLayout = view.findViewById(R.id.ll_pulse_container);
        mTemperatureLinearLayout = view.findViewById(R.id.ll_temperature_container);
        mSpo2LinearLayout = view.findViewById(R.id.ll_spo2_container);
        mRespiratoryRateLinearLayout = view.findViewById(R.id.ll_respiratory_rate_container);
        mBloodGroupLinearLayout = view.findViewById(R.id.ll_blood_group_container);
        mHaemoglobinLinearLayout = view.findViewById(R.id.ll_haemoglobin_container);
        mSugarRandomLinearLayout = view.findViewById(R.id.ll_sugar_random_container);

        if (mVitalsObject != null) {
            if (mVitalsObject.getHeight() != null && !mVitalsObject.getHeight().isEmpty() && !mVitalsObject.getHeight().equalsIgnoreCase("0"))
                ((TextView) view.findViewById(R.id.tv_height)).setText(VisitUtils.convertHeightIntoFeets(mVitalsObject.getHeight(), requireContext()));
//                ((TextView) view.findViewById(R.id.tv_height)).setText(mVitalsObject.getHeight() + " " + getResources().getString(R.string.cm));
            else
                ((TextView) view.findViewById(R.id.tv_height)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getWeight() != null && !mVitalsObject.getWeight().isEmpty())
                ((TextView) view.findViewById(R.id.tv_weight)).setText(mVitalsObject.getWeight() + " " + getResources().getString(R.string.kg));
            else
                ((TextView) view.findViewById(R.id.tv_weight)).setText(getString(R.string.ui2_no_information));

            TextView bmiTextView = view.findViewById(R.id.tv_bmi);
            if (mVitalsObject.getBmi() != null && !mVitalsObject.getBmi().isEmpty()){
                setBmiStatus(bmiTextView);
            } else {
                ((TextView) view.findViewById(R.id.tv_bmi)).setText(getString(R.string.ui2_no_information));
            }

            TextView bpTextView = view.findViewById(R.id.tv_bp);
            if (mVitalsObject.getBpsys() != null && !mVitalsObject.getBpsys().isEmpty() &&
                    mVitalsObject.getBpdia() != null && !mVitalsObject.getBpdia().isEmpty()){
                setBpStatus(bpTextView);
            } else {
                ((TextView) view.findViewById(R.id.tv_bp)).setText(getString(R.string.ui2_no_information));
            }

            if (mVitalsObject.getPulse() != null && !mVitalsObject.getPulse().isEmpty())
                ((TextView) view.findViewById(R.id.tv_pulse)).setText(mVitalsObject.getPulse() + " " + getResources().getString(R.string.bpm));
            else
                ((TextView) view.findViewById(R.id.tv_pulse)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getTemperature() != null && !mVitalsObject.getTemperature().isEmpty()) {
                if (new ConfigUtils(getActivity()).fahrenheit()) {
                    ((TextView) view.findViewById(R.id.tv_temperature)).setText(convertCtoF(TAG, mVitalsObject.getTemperature()));
                } else {
                    ((TextView) view.findViewById(R.id.tv_temperature)).setText(mVitalsObject.getTemperature());
                }
            } else {

                ((TextView) view.findViewById(R.id.tv_temperature)).setText(getString(R.string.ui2_no_information));
            }

            if (mVitalsObject.getSpo2() != null && !mVitalsObject.getSpo2().isEmpty())
                ((TextView) view.findViewById(R.id.tv_spo2)).setText(mVitalsObject.getSpo2() + " %");
            else
                ((TextView) view.findViewById(R.id.tv_spo2)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getBloodGroup() != null && !mVitalsObject.getBloodGroup().isEmpty())
                ((TextView) view.findViewById(R.id.tv_blood_group)).setText(mVitalsObject.getBloodGroup());
            else
                ((TextView) view.findViewById(R.id.tv_blood_group)).setText(getString(R.string.ui2_no_information));


            if (mVitalsObject.getResp() != null && !mVitalsObject.getResp().isEmpty())
                ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(mVitalsObject.getResp() + " " + getResources().getString(R.string.breaths_min));
            else
                ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getHaemoglobin() != null && !mVitalsObject.getHaemoglobin().isEmpty()) {
                ((TextView) view.findViewById(R.id.tv_haemoglobin)).setText(mVitalsObject.getHaemoglobin());
            } else {
                ((TextView) view.findViewById(R.id.tv_haemoglobin)).setText(getString(R.string.ui2_no_information));
            }

            if (mVitalsObject.getSugarRandom() != null && !mVitalsObject.getSugarRandom().isEmpty()) {
                ((TextView) view.findViewById(R.id.tv_sugar_random)).setText(mVitalsObject.getSugarRandom());
            } else {
                ((TextView) view.findViewById(R.id.tv_sugar_random)).setText(getString(R.string.ui2_no_information));
            }
        }
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsEditMode && ((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {
                    String visitType = IntelehealthApplication.getInstance().getVisitType();
                    if (visitType.equals(AppConstants.VISIT_TYPE_SEVIKA)) {
                        saveSevikaVisitAndProceed();
                    } else {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON, mIsEditMode, mVitalsObject);
                    }

                    /*MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
                    alertDialogBuilder.setMessage(getResources().getString(R.string.doctor_advice_alert_msg));

                    alertDialogBuilder.setNegativeButton(getResources().getString(R.string.vital_alert_save_button), (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                        try {
                            // avoiding multi-click by checking if click is within 1000ms than avoid it.
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();

                            speciality_attributes.insertVisitAttributes(visitUuid,"", AppConstants.DOCTOR_NOT_NEEDED);
                            // speciality_attributes.insertVisitAttributes(visitUuid, " Specialist doctor not needed");
                        } catch (DAOException e) {
                            e.printStackTrace();
                        }

                        //-------End Visit----------
                        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                        Date todayDate = new Date();
                        String endDate = currentDate.format(todayDate);
                        endVisit(visitUuid, mVitalsObject.getPatientUuid(), endDate);
                    });
                    alertDialogBuilder.setPositiveButton(getResources().getString(R.string.vital_alert_continue_button), (dialog, which) -> {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON, mIsEditMode, mVitalsObject);
                        dialog.dismiss();
                    });
                    AlertDialog alertDialog = alertDialogBuilder.show();
                    IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);*/
                }
            }
        });
        view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, true, mVitalsObject);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }
        });
        ImageButton refresh = view.findViewById(R.id.imb_btn_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getActivity())) {
                    syncNow(getActivity(), refresh, syncAnimator);
                }
            }
        });
        return view;
    }

    private void setBmiStatus(TextView bmiTextView) {
        double bmi = Double.parseDouble(mVitalsObject.getBmi());
        bmiTextView.setText(bmi + " " + getResources().getString(R.string.kg_m));
        if (bmi < 18.50) {
            bmiTextView.setTextColor(getResources().getColor(R.color.ui2_bmi1_ekal));
        } else if (bmi >= 18.50 && bmi <= 22.99) {
            bmiTextView.setTextColor(getResources().getColor(R.color.ui2_bmi2_ekal));
        } else if (bmi >= 23 && bmi <= 24.99) {
            bmiTextView.setTextColor(getResources().getColor(R.color.ui2_bmi3_ekal));
        } else if (bmi >= 25 && bmi <= 29.99) {
            bmiTextView.setTextColor(getResources().getColor(R.color.ui2_bmi4_ekal));
        } else if (bmi > 30) {
            bmiTextView.setTextColor(getResources().getColor(R.color.ui2_bmi5_ekal));
        }
    }

    private void setBpStatus(TextView bpTextView) {
        int sys = Integer.parseInt(mVitalsObject.getBpsys());
        int dia = Integer.parseInt(mVitalsObject.getBpdia());

        int sysColor = ContextCompat.getColor(requireActivity(), R.color.ui2_bp_default_ekal);
        int diaColor = ContextCompat.getColor(requireActivity(), R.color.ui2_bp_default_ekal);

        if (sys >= 90 && sys < 120) {
            sysColor = ContextCompat.getColor(requireActivity(), R.color.ui2_sys1_ekal);
        } else if (sys >= 120 && sys <= 139) {
            sysColor = ContextCompat.getColor(requireActivity(), R.color.ui2_sys2_ekal);
        }

        if (dia < 80) {
            diaColor = ContextCompat.getColor(requireActivity(), R.color.ui2_dia1_ekal);
        } else if (dia >= 80 && dia <= 99) {
            diaColor = ContextCompat.getColor(requireActivity(), R.color.ui2_dia2_ekal);
        }

        bpTextView.setText(mVitalsObject.getBpsys() + "/" + mVitalsObject.getBpdia());
        bpTextView.setTextColor(sysColor);

        SpannableString spannableString = new SpannableString(mVitalsObject.getBpsys() + "/" + mVitalsObject.getBpdia());
        spannableString.setSpan(new ForegroundColorSpan(sysColor), 0, mVitalsObject.getBpsys().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(diaColor), mVitalsObject.getBpsys().length() + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        bpTextView.setText(spannableString);
    }

    private void saveSevikaVisitAndProceed() {
        String title = getString(R.string.doctor_advice_alert_title);
        String message = getString(R.string.doctor_advice_alert_msg);
        String startButtonString = getString(R.string.alert_start_button);
        String saveAndExitButtonString = getResources().getString(R.string.alert_save_and_exit_button);

        String riskAlertMessage = getRiskProneVitals();
        if (!riskAlertMessage.isEmpty()) {
            message = riskAlertMessage.concat("\n").concat(message);
        }

        new DialogUtils().showCommonDialog(requireContext(), R.drawable.ic_doctor_service_start, title, message, false, startButtonString, saveAndExitButtonString, action -> {
                    if (action == DialogUtils.CustomDialogListener.NEGATIVE_CLICK) {
                        VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                        try {
                            // avoiding multi-click by checking if click is within 1000ms than avoid it.
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            speciality_attributes.insertVisitAttributes(visitUuid, AppConstants.DOCTOR_NOT_NEEDED, UuidDictionary.SPECIALITY);
                        } catch (DAOException e) {
                            e.printStackTrace();
                        }

                        //-------End Visit----------
                        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                        Date todayDate = new Date();
                        String endDate = currentDate.format(todayDate);
                        endVisit(visitUuid, mVitalsObject.getPatientUuid(), endDate);
                    } else if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON, mIsEditMode, mVitalsObject);
                    }
                }
        );
    }

    private String getRiskProneVitals() {
        String riskProneMessage = "";

        String bmi = mVitalsObject.getBmi();
        if (bmi != null && !bmi.isEmpty()) {
            if (Double.parseDouble(bmi) < 18.5) {
                riskProneMessage = riskProneMessage.concat(getResources().getString(R.string.weight_loss_alert_msg)).concat("\n");
            } else if (Double.parseDouble(bmi) > 25.0) {
                riskProneMessage = riskProneMessage.concat(getResources().getString(R.string.weight_gain_alert_msg)).concat("\n");
            }
        }

        String spo2 = mVitalsObject.getSpo2();
        if (spo2 != null && !spo2.isEmpty() && Integer.parseInt(spo2) < RISK_LIMIT_SPO2) {
            riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_spo2_button)).concat("\n");
        }

        String pulse = mVitalsObject.getPulse();
        String patientDateOfBirth = ((VisitCreationActivity) requireActivity()).patientDTO.getDateofbirth();
        float ageYearMonth = DateAndTimeUtils.getFloat_Age_Year_Month(patientDateOfBirth);
        if (ageYearMonth < 35) {
            if (pulse != null && !pulse.isEmpty() && (Integer.parseInt(pulse) < AppConstants.RISK_LIMIT_PULSE_LOWER_60 || Integer.parseInt(pulse) > AppConstants.RISK_LIMIT_PULSE_UPPER_200)) {
                riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_pulse_button)).concat("\n");
            }
        } else if (ageYearMonth >= 35 && ageYearMonth < 50) {
            if (pulse != null && !pulse.isEmpty() && (Integer.parseInt(pulse) < AppConstants.RISK_LIMIT_PULSE_LOWER_58 || Integer.parseInt(pulse) > AppConstants.RISK_LIMIT_PULSE_UPPER_150)) {
                riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_pulse_button)).concat("\n");
            }
        } else {
            if (pulse != null && !pulse.isEmpty() && (Integer.parseInt(pulse) < AppConstants.RISK_LIMIT_PULSE_LOWER_40 || Integer.parseInt(pulse) > AppConstants.RISK_LIMIT_PULSE_UPPER_140)) {
                riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_pulse_button)).concat("\n");
            }
        }

        String respiratory = mVitalsObject.getResp();
        if (respiratory != null && !respiratory.isEmpty() && (Integer.parseInt(respiratory) < AppConstants.RISK_LIMIT_RESPIRATORY_LOWER || Integer.parseInt(respiratory) > AppConstants.RISK_LIMIT_RESPIRATORY_UPPER)) {
            riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_resp_button)).concat("\n");
        }

        String temperature = mVitalsObject.getTemperature();
        if (ageYearMonth < 1) {
            if (temperature != null && !temperature.isEmpty() && (Double.parseDouble(temperature) < AppConstants.RISK_LIMIT_TEMPERATURE_LOWER_95 || Double.parseDouble(temperature) < AppConstants.RISK_LIMIT_TEMPERATURE_UPPER_100)) {
                riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_temperature_button)).concat("\n");
            }
        } else {
            if (temperature != null && !temperature.isEmpty() && (Double.parseDouble(temperature) < AppConstants.RISK_LIMIT_TEMPERATURE_LOWER_95 || Double.parseDouble(temperature) < AppConstants.RISK_LIMIT_TEMPERATURE_UPPER_103)) {
                riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_temperature_button)).concat("\n");
            }
        }

        String haemoglobin = mVitalsObject.getHaemoglobin();
        if (haemoglobin != null && !haemoglobin.isEmpty() && (Double.parseDouble(haemoglobin) < AppConstants.RISK_LIMIT_HAEMOGLOBIN_LOWER || Double.parseDouble(haemoglobin) > AppConstants.RISK_LIMIT_HAEMOGLOBIN_UPPER)) {
            riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_hgb_button)).concat("\n");
        }

        String sugarRandom = mVitalsObject.getSugarRandom();
        if (sugarRandom != null && !sugarRandom.isEmpty() && (Integer.parseInt(sugarRandom) < AppConstants.RISK_LIMIT_SUGAR_RANDOM_LOWER || Integer.parseInt(sugarRandom) > AppConstants.RISK_LIMIT_SUGAR_RANDOM_UPPER)) {
            riskProneMessage = riskProneMessage.concat(getString(R.string.vital_alert_sugar_random_button)).concat("\n");

        }

        return riskProneMessage;
    }

    private void endVisit(String visitUuid, String patientUuid, String endTime) {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, endTime);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        new SyncUtils().syncForeground("");
        sessionManager.removeVisitSummary(patientUuid, visitUuid);
        Intent intent = new Intent(getContext(), HomeScreenActivity_New.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private ObjectAnimator syncAnimator;
}