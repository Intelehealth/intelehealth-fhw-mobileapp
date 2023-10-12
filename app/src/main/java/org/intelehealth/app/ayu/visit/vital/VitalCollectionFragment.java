package org.intelehealth.app.ayu.visit.vital;

import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertFtoC;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.model.BMIStatus;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.DecimalDigitsInputFilter;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class VitalCollectionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = VitalCollectionFragment.class.getSimpleName();
    private VisitCreationActionListener mActionListener;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    private int mAgeInMonth;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    //private Spinner mHeightSpinner, mWeightSpinner;
    private EditText mHeightEditText, mWeightEditText;
    private TextView mBMITextView, mBmiStatusTextView;
    private LinearLayout mBMILinearLayout;
    TextView mHeightErrorTextView, mWeightErrorTextView, mPulseErrorTextView, mSpo2ErrorTextView, mRespErrorTextView, mBpSysErrorTextView, mBpDiaErrorTextView, mTemperatureErrorTextView;
    EditText mPulseEditText, mBpSysEditText, mBpDiaEditText, mTemperatureEditText, mSpo2EditText, mRespEditText;
    private Button mSubmitButton;

    private String heightvalue = "";
    private String weightvalue = "";
    private int flag_height = 0, flag_weight = 0;
    SessionManager sessionManager;
    ConfigUtils configUtils;
    VitalsObject results = new VitalsObject();

    private List<Integer> mHeightMasterList = new ArrayList<>();
    private List<Integer> mWeightMasterList = new ArrayList<>();
    private boolean mIsEditMode = false;
    private EditText mHemoglobinEdittext, mSugarEdittext;
    private TextView mHemoglobinErrorTextView,mBloodSugarErrorTextview;
    public VitalCollectionFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionFragment newInstance(Intent intent, boolean isEditMode, VitalsObject vitalsObject) {
        VitalCollectionFragment fragment = new VitalCollectionFragment();


        fragment.mIsEditMode = isEditMode;
        fragment.results = vitalsObject;

        fragment.patientUuid = intent.getStringExtra("patientUuid");
        fragment.visitUuid = intent.getStringExtra("visitUuid");
        fragment.encounterVitals = intent.getStringExtra("encounterUuidVitals");
        fragment.encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
        fragment.EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
        fragment.state = intent.getStringExtra("state");
        fragment.patientName = intent.getStringExtra("name");
        fragment.patientGender = intent.getStringExtra("gender");
        fragment.intentTag = intent.getStringExtra("tag");
        fragment.float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
        String[] temp = String.valueOf(fragment.float_ageYear_Month).split("\\.");
        fragment.mAgeInMonth = Integer.parseInt(temp[0]) * 12 + Integer.parseInt(temp[1]);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
        configUtils = new ConfigUtils(context);

        //mHeightMasterList = getNumbersInRange(Integer.parseInt(AppConstants.MINIMUM_HEIGHT), Integer.parseInt(AppConstants.MAXIMUM_HEIGHT));
        //mWeightMasterList = getNumbersInRange(Integer.parseInt(AppConstants.MINIMUM_WEIGHT), Integer.parseInt(AppConstants.MAXIMUM_WEIGHT));
    }

    /*public List<Integer> getNumbersUsingIntStreamRange(int start, int end) {
        return IntStream.range(start, end)
                .boxed()
                .collect(Collectors.toList());
    }*/
    public List<Integer> getNumbersInRange(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            result.add(i);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_collection, container, false);
        Log.v("float_ageYear_Month", float_ageYear_Month + "");
        //mHeightSpinner = view.findViewById(R.id.sp_height);
        //mWeightSpinner = view.findViewById(R.id.sp_weight);

        mHeightEditText = view.findViewById(R.id.etv_height);
        mWeightEditText = view.findViewById(R.id.etv_weight);

        mHeightEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});
        mWeightEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});
        /*mHeightTextView.setOnClickListener(this);
        mWeightTextView.setOnClickListener(this);*/

        mBMILinearLayout = view.findViewById(R.id.ll_bmi);
        if (float_ageYear_Month <= 19)
            mBMILinearLayout.setVisibility(View.GONE);
        mBMITextView = view.findViewById(R.id.tv_bmi_value);
        mBmiStatusTextView = view.findViewById(R.id.tv_bmi_status);

        mBpSysEditText = view.findViewById(R.id.etv_bp_sys);
        mBpDiaEditText = view.findViewById(R.id.etv_bp_dia);

        mSpo2EditText = view.findViewById(R.id.etv_spo2);
        mPulseEditText = view.findViewById(R.id.etv_pulse);
        mRespEditText = view.findViewById(R.id.etv_respiratory_rate);
        mTemperatureEditText = view.findViewById(R.id.etv_temperature);
        mTemperatureEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});
        // errors
        mHeightErrorTextView = view.findViewById(R.id.tv_height_error);
        mWeightErrorTextView = view.findViewById(R.id.tv_weight_error);

        mBpSysErrorTextView = view.findViewById(R.id.etv_bp_sys_error);
        mBpDiaErrorTextView = view.findViewById(R.id.etv_bp_dia_error);

        mSpo2ErrorTextView = view.findViewById(R.id.etv_spo2_error);

        mPulseErrorTextView = view.findViewById(R.id.etv_pulse_error);

        mRespErrorTextView = view.findViewById(R.id.etv_respiratory_rate_error);

        mTemperatureErrorTextView = view.findViewById(R.id.etv_temperature_error);

        mHeightErrorTextView.setVisibility(View.GONE);
        mWeightErrorTextView.setVisibility(View.GONE);
        mBpSysErrorTextView.setVisibility(View.GONE);
        mBpDiaErrorTextView.setVisibility(View.GONE);
        mSpo2ErrorTextView.setVisibility(View.GONE);
        mPulseErrorTextView.setVisibility(View.GONE);
        mRespErrorTextView.setVisibility(View.GONE);
        mTemperatureErrorTextView.setVisibility(View.GONE);

        mHeightEditText.addTextChangedListener(new MyTextWatcher(mHeightEditText));
        mWeightEditText.addTextChangedListener(new MyTextWatcher(mWeightEditText));

        mBpSysEditText.addTextChangedListener(new MyTextWatcher(mBpSysEditText));
        mBpDiaEditText.addTextChangedListener(new MyTextWatcher(mBpDiaEditText));
        mSpo2EditText.addTextChangedListener(new MyTextWatcher(mSpo2EditText));
        mPulseEditText.addTextChangedListener(new MyTextWatcher(mPulseEditText));
        mRespEditText.addTextChangedListener(new MyTextWatcher(mRespEditText));
        mTemperatureEditText.addTextChangedListener(new MyTextWatcher(mTemperatureEditText));

        mSubmitButton = view.findViewById(R.id.btn_submit);
        mSubmitButton.setOnClickListener(this);


        //showHeightListing();


        //showWeightListing();



        mHemoglobinEdittext = view.findViewById(R.id.etv_hemoglobin);
        mSugarEdittext = view.findViewById(R.id.etv_sugar);
        mBloodSugarErrorTextview = view.findViewById(R.id.etv_sugar_error);
        mHemoglobinErrorTextView = view.findViewById(R.id.etv_hemoglobin_error);
        mHemoglobinEdittext.addTextChangedListener(new MyTextWatcher(mHemoglobinEdittext));
        mSugarEdittext.addTextChangedListener(new MyTextWatcher(mSugarEdittext));
        mHemoglobinErrorTextView.setVisibility(View.GONE);
        mBloodSugarErrorTextview.setVisibility(View.GONE);


        if (mIsEditMode && results == null) {
            loadSavedDateForEditFromDB();
        }
        return view;
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
            if (val.equals(".")) {
                editText.setText("");
                return;
            }
            if (this.editText.getId() == R.id.etv_pulse) {
                if (val.isEmpty()) {
                    /*mPulseErrorTextView.setVisibility(View.VISIBLE);
                    mPulseErrorTextView.setText(getString(R.string.error_field_required));
                    mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                    mPulseErrorTextView.setVisibility(View.GONE);
                    mPulseEditText.setBackgroundResource(R.drawable.edittext_border);
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {

                        mPulseErrorTextView.setText(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        mPulseErrorTextView.setVisibility(View.VISIBLE);
                        mPulseEditText.requestFocus();
                        mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    } else {
                        mPulseErrorTextView.setVisibility(View.GONE);
                        mPulseEditText.setBackgroundResource(R.drawable.edittext_border);
                    }

                }
            } else if (this.editText.getId() == R.id.etv_temperature) {
                if (val.isEmpty()) {
                    /*mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                    mTemperatureErrorTextView.setText(getString(R.string.error_field_required));
                    mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                    mTemperatureErrorTextView.setVisibility(View.GONE);
                    mTemperatureEditText.setBackgroundResource(R.drawable.edittext_border);
                } else {
                    if (configUtils.celsius()) {
                        if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS)) ||
                                (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS))) {
                            //et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                            mTemperatureEditText.requestFocus();
                            mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                        } else {
                            mTemperatureErrorTextView.setVisibility(View.GONE);
                            mTemperatureEditText.setBackgroundResource(R.drawable.edittext_border);
                        }
                    } else if (configUtils.fahrenheit()) {
                        if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                                (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                            mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                            mTemperatureEditText.requestFocus();
                            mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                        } else {
                            mTemperatureErrorTextView.setVisibility(View.GONE);
                            mTemperatureEditText.setBackgroundResource(R.drawable.edittext_border);
                        }
                    }

                }
            } else if (this.editText.getId() == R.id.etv_spo2) {
                if (val.isEmpty()) {
                    /*mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                    mSpo2ErrorTextView.setText(getString(R.string.error_field_required));
                    mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                    mSpo2ErrorTextView.setVisibility(View.GONE);
                    mSpo2EditText.setBackgroundResource(R.drawable.edittext_border);
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                        mSpo2ErrorTextView.setText(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                        mSpo2EditText.requestFocus();
                        mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    } else {
                        mSpo2ErrorTextView.setVisibility(View.GONE);
                        mSpo2EditText.setBackgroundResource(R.drawable.edittext_border);
                    }

                }
            } else if (this.editText.getId() == R.id.etv_respiratory_rate) {
                if (val.isEmpty()) {
                    /*mRespErrorTextView.setVisibility(View.VISIBLE);
                    mRespErrorTextView.setText(getString(R.string.error_field_required));
                    mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                    mRespErrorTextView.setVisibility(View.GONE);
                    mRespEditText.setBackgroundResource(R.drawable.edittext_border);
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_RESPIRATORY)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_RESPIRATORY))) {
                        mRespErrorTextView.setText(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        mRespErrorTextView.setVisibility(View.VISIBLE);
                        mRespEditText.requestFocus();
                        mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    } else {
                        mRespErrorTextView.setVisibility(View.GONE);
                        mRespEditText.setBackgroundResource(R.drawable.edittext_border);
                    }

                }
            } else if (this.editText.getId() == R.id.etv_bp_sys) {
                String bpDia = mBpDiaEditText.getText().toString().trim();
                if (val.isEmpty()) {
                    if (bpDia.isEmpty()) {
                        mBpSysErrorTextView.setVisibility(View.GONE);
                        mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    } else {
                        mBpSysErrorTextView.setVisibility(View.VISIBLE);
                        mBpSysErrorTextView.setText(getString(R.string.error_field_required));
                        mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    }
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS))) {
                        //et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

                        mBpSysErrorTextView.setText(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                        mBpSysErrorTextView.setVisibility(View.VISIBLE);
                        mBpSysEditText.requestFocus();
                        mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        return;
                    }

                    if (bpDia.isEmpty()) {
                        mBpSysErrorTextView.setVisibility(View.GONE);
                        mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    } else {
                        int bpSysInt = Integer.parseInt(val);
                        int bpDiaInt = Integer.parseInt(bpDia);
                        if (bpDiaInt >= bpSysInt) {
                            mBpSysErrorTextView.setVisibility(View.VISIBLE);
                            mBpSysErrorTextView.setText(getString(R.string.bp_validation_sys));
                            mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        } else {
                            mBpSysErrorTextView.setVisibility(View.GONE);
                            mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                            mBpDiaErrorTextView.setVisibility(View.GONE);
                            mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        }
                    }
                }
            } else if (this.editText.getId() == R.id.etv_bp_dia) {
                String bpSys = mBpSysEditText.getText().toString().trim();

                if (val.isEmpty()) {
                    if (bpSys.isEmpty()) {
                        mBpDiaErrorTextView.setVisibility(View.GONE);
                        mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    } else {
                        mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                        mBpDiaErrorTextView.setText(getString(R.string.error_field_required));
                        mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    }
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS))) {
                        //et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mBpDiaErrorTextView.setText(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                        mBpDiaEditText.requestFocus();
                        mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        return;
                    }
                    if (bpSys.isEmpty()) {
                        mBpDiaErrorTextView.setVisibility(View.GONE);
                        mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    } else {
                        int bpSysInt = Integer.parseInt(bpSys);
                        int bpDiaInt = Integer.parseInt(val);
                        if (bpDiaInt >= bpSysInt) {
                            mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                            mBpDiaErrorTextView.setText(getString(R.string.bp_validation_dia));
                            mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        } else {
                            mBpDiaErrorTextView.setVisibility(View.GONE);
                            mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                            mBpSysErrorTextView.setVisibility(View.GONE);
                            mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                        }
                    }
                }
            } else if (this.editText.getId() == R.id.etv_height) {
                String weight = mWeightEditText.getText().toString().trim();
                if (val.isEmpty()) {
                    if (weight.isEmpty()) {
                        mHeightErrorTextView.setVisibility(View.GONE);
                        mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    } else {
                        mHeightErrorTextView.setVisibility(View.VISIBLE);
                        mHeightErrorTextView.setText(getString(R.string.error_field_required));
                        mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    }
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_HEIGHT)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_HEIGHT))) {
                        //et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

                        mHeightErrorTextView.setText(getString(R.string.height_error, AppConstants.MINIMUM_HEIGHT, AppConstants.MAXIMUM_HEIGHT));
                        mHeightErrorTextView.setVisibility(View.VISIBLE);
                        mHeightEditText.requestFocus();
                        mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    } else {
                        mHeightErrorTextView.setVisibility(View.GONE);
                        mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
                }
                heightvalue = val;
                calculateBMI();
            } else if (this.editText.getId() == R.id.etv_weight) {
                String height = mHeightEditText.getText().toString().trim();

                if (val.isEmpty()) {
                    if (height.isEmpty()) {
                        mWeightErrorTextView.setVisibility(View.GONE);
                        mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    } else {
                        mWeightErrorTextView.setVisibility(View.VISIBLE);
                        mWeightErrorTextView.setText(getString(R.string.error_field_required));
                        mWeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    }
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.getMaxWeightByAge(mAgeInMonth))) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.getMinWeightByAge(mAgeInMonth)))) {
                        //et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mWeightErrorTextView.setText(getString(R.string.weight_error, AppConstants.getMinWeightByAge(mAgeInMonth), AppConstants.getMaxWeightByAge(mAgeInMonth)));
                        mWeightErrorTextView.setVisibility(View.VISIBLE);
                        mWeightEditText.requestFocus();
                        mWeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    } else {
                        mWeightErrorTextView.setVisibility(View.GONE);
                        mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }

                }
                weightvalue = val;
                calculateBMI();
            } else if (this.editText.getId() == R.id.etv_hemoglobin) {
                if (val.isEmpty()) {
                    mHemoglobinErrorTextView.setVisibility(View.GONE);
                    mHemoglobinEdittext.setBackgroundResource(R.drawable.edittext_border);
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_HEMOGLOBIN)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_HEMOGLOBIN))) {

                        mHemoglobinErrorTextView.setText(getString(R.string.hemoglobin_error, AppConstants.MINIMUM_HEMOGLOBIN, AppConstants.MAXIMUM_HEMOGLOBIN));
                        mHemoglobinErrorTextView.setVisibility(View.VISIBLE);
                        mHemoglobinEdittext.requestFocus();
                        mHemoglobinEdittext.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    } else {
                        mHemoglobinErrorTextView.setVisibility(View.GONE);
                        mHemoglobinEdittext.setBackgroundResource(R.drawable.edittext_border);
                    }

                }
            }else if (this.editText.getId() == R.id.etv_sugar) {
                if (val.isEmpty()) {
                    mBloodSugarErrorTextview.setVisibility(View.GONE);
                    mSugarEdittext.setBackgroundResource(R.drawable.edittext_border);
                } else {
                    if ((Double.parseDouble(val) > Double.parseDouble(AppConstants.MAXIMUM_SUGAR)) ||
                            (Double.parseDouble(val) < Double.parseDouble(AppConstants.MINIMUM_SUGAR))) {

                        mBloodSugarErrorTextview.setText(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                        mBloodSugarErrorTextview.setVisibility(View.VISIBLE);
                        mSugarEdittext.requestFocus();
                        mSugarEdittext.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    } else {
                        mBloodSugarErrorTextview.setVisibility(View.GONE);
                        mSugarEdittext.setBackgroundResource(R.drawable.edittext_border);
                    }

                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {//validate
            if (validateTable()) {
                mActionListener.onProgress(100);
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY, mIsEditMode, results);
            }
        }
    }

    private ArrayAdapter<String> mHeightArrayAdapter;

    /*private void showHeightListing() {
        // add a list
        final String[] data = new String[mHeightMasterList.size() + 1];
        data[0] = getResources().getString(R.string.select_height);
        for (int i = 1; i < data.length; i++) {
            data[i] = String.valueOf(mHeightMasterList.get(i - 1)) + " " + getResources().getString(R.string.cm);
        }

        mHeightArrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.simple_spinner_item_1, data);
        mHeightArrayAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mHeightSpinner.setAdapter(mHeightArrayAdapter);
        mHeightSpinner.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));
        mHeightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                if (which != 0) {
                    heightvalue = data[which].split(" ")[0];
                    calculateBMI();
                    mHeightErrorTextView.setVisibility(View.GONE);
                    mHeightSpinner.setBackgroundResource(R.drawable.edittext_border);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }*/

    private ArrayAdapter<String> mWeightArrayAdapter;

    /*private void showWeightListing() {

        // add a list
        final String[] data = new String[mWeightMasterList.size() + 1];
        data[0] = getResources().getString(R.string.select_weight);
        for (int i = 1; i < data.length; i++) {
            data[i] = String.valueOf(mWeightMasterList.get(i - 1)) + " " + getResources().getString(R.string.kg);
        }
        mWeightArrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.simple_spinner_item_1, data);
        mWeightArrayAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mWeightSpinner.setAdapter(mWeightArrayAdapter);
        mWeightSpinner.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));

        mWeightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                if (which != 0) {
                    weightvalue = data[which].split(" ")[0];
                    calculateBMI();
                    mWeightErrorTextView.setVisibility(View.GONE);
                    mWeightSpinner.setBackgroundResource(R.drawable.edittext_border);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }*/

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        // set existing data
        if (results != null) {
            if (results.getHeight() != null && !results.getHeight().isEmpty() && !results.getHeight().equalsIgnoreCase("0")) {
                //mHeightSpinner.setSelection(mHeightArrayAdapter.getPosition(results.getHeight() + " " + getResources().getString(R.string.cm)), true);
                mHeightEditText.setText(results.getHeight());
            }


            if (results.getWeight() != null && !results.getWeight().isEmpty()) {
                //mWeightSpinner.setSelection(mWeightArrayAdapter.getPosition(results.getWeight() + " " + getResources().getString(R.string.kg)), true);
                mWeightEditText.setText(results.getWeight());
            }

            /*if (results.getBmi() != null && !results.getBmi().isEmpty())
              pass*/


            if (results.getBpsys() != null && !results.getBpsys().isEmpty())
                mBpSysEditText.setText(results.getBpsys());

            if (results.getBpdia() != null && !results.getBpdia().isEmpty())
                mBpDiaEditText.setText(results.getBpdia());


            if (results.getPulse() != null && !results.getPulse().isEmpty())
                mPulseEditText.setText(results.getPulse());

            if (results.getTemperature() != null && !results.getTemperature().isEmpty()) {
                if (new ConfigUtils(getActivity()).fahrenheit()) {
                    mTemperatureEditText.setText(convertCtoF(TAG, results.getTemperature()));
                } else {
                    mTemperatureEditText.setText(results.getTemperature());
                }
                if (mTemperatureEditText.getText().toString().endsWith(".")) {
                    mTemperatureEditText.setText(mTemperatureEditText.getText().toString().replace(".", ""));
                }
            }
            if (results.getSpo2() != null && !results.getSpo2().isEmpty())
                mSpo2EditText.setText(results.getSpo2());

            if (results.getResp() != null && !results.getResp().isEmpty())
                mRespEditText.setText(results.getResp());

            if (results.getHemoglobin() != null && !results.getHemoglobin().isEmpty())
                mHemoglobinEdittext.setText(results.getHemoglobin());

            if (results.getBloodSugar() != null && !results.getBloodSugar().isEmpty())
              mSugarEdittext.setText(results.getBloodSugar());

        }
    }


    private BMIStatus getBmiStatus(double bmi) {
        //"< 18.5: Underweight
        //18.5 to 24.9: Normal Weight
        //25.0 to 29.9: Overweight
        //30.0 to 34.9: Moderate Obesity (Class 1)
        //35.0 to 39.9: Severe Obesity (Class 2)
        //40.0 & Above: Very Severe (Morbid) Obesity (Class 3)"
        BMIStatus bmiStatus = new BMIStatus();
        bmiStatus.setStatus("");
        bmiStatus.setColor(R.color.gray);
        if (bmi < 18.5) {
            bmiStatus.setStatus(getResources().getString(R.string.underweight));
            bmiStatus.setColor(R.color.ui2_bmi1);
        } else if (bmi > 18.5 && bmi <= 24.9) {
            bmiStatus.setStatus(getResources().getString(R.string.normal));
            bmiStatus.setColor(R.color.ui2_bmi2);
        } else if (bmi > 24.9 && bmi <= 29.9) {
            bmiStatus.setStatus(getResources().getString(R.string.overweight));
            bmiStatus.setColor(R.color.ui2_bmi3);
        } else if (bmi > 29.9 && bmi <= 34.9) {
            bmiStatus.setStatus(getResources().getString(R.string.morbidly_obese));
            bmiStatus.setColor(R.color.ui2_bmi4);
        } else if (bmi > 34.9 && bmi <= 39.9) {
            bmiStatus.setStatus(getResources().getString(R.string.severely_obese));
            bmiStatus.setColor(R.color.ui2_bmi5);
        } else if (bmi >= 39.9) {
            bmiStatus.setStatus(getResources().getString(R.string.very_severely_obese));
            bmiStatus.setColor(R.color.ui2_bmi6);
        }
        return bmiStatus;
    }


    public void calculateBMI() {

        mBMITextView.setText("");
        mBmiStatusTextView.setText("");
        if (weightvalue == null || weightvalue.isEmpty() || heightvalue == null || heightvalue.isEmpty()) {
            return;
        }
        double numerator = Double.parseDouble(weightvalue) * 10000;
        double denominator = (Double.parseDouble(heightvalue)) * (Double.parseDouble(heightvalue));
        double bmi_value = numerator / denominator;
        DecimalFormat df = new DecimalFormat("0.00");
        mBMITextView.setText(df.format(bmi_value) + " kg/m");
        Log.d("BMI", "BMI: " + mBMITextView.getText().toString());
        //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));

        BMIStatus bmiStatus = getBmiStatus(bmi_value);
        mBmiStatusTextView.setText(String.format("(%s)", bmiStatus.getStatus()));
        mBmiStatusTextView.setTextColor(getResources().getColor(bmiStatus.getColor()));
    }

    public void calculateBMI_onEdit(String height, String weight) {
        if (height != null && weight != null && height.toString().trim().length() > 0 && !height.toString().startsWith(".") &&
                weight.toString().trim().length() > 0 && !weight.toString().startsWith(".")) {

            mBMITextView.setText("");
            mBmiStatusTextView.setText("");
            double numerator = Double.parseDouble(weight) * 10000;
            double denominator = (Double.parseDouble(height)) * (Double.parseDouble(height));
            double bmi_value = numerator / denominator;
            DecimalFormat df = new DecimalFormat("0.00");
            mBMITextView.setText(df.format(bmi_value) + " kg/m");
            Log.d("BMI", "BMI: " + mBMITextView.getText().toString());
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));

            BMIStatus bmiStatus = getBmiStatus(bmi_value);
            mBmiStatusTextView.setText(String.format("(%s)", bmiStatus.getStatus()));
            mBmiStatusTextView.setTextColor(getResources().getColor(bmiStatus.getColor()));
        } else {
            // do nothing
            mBMITextView.setText("");
        }
    }

    public void loadSavedDateForEditFromDB() {

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1'";
        String[] visitArgs = {encounterVitals};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.HEIGHT: //Height
                heightvalue = value;
                //mHeightTextView.setText(value);
                if (heightvalue != null && !heightvalue.isEmpty() && !heightvalue.equalsIgnoreCase("0")) {
                    //Log.v(TAG, "getHeight - " + results.getHeight());
                    //Log.v(TAG, "getPosition - " + mHeightArrayAdapter.getPosition(results.getHeight()));
                    //mHeightSpinner.setSelection(mHeightArrayAdapter.getPosition(heightvalue + " " + getResources().getString(R.string.cm)), true);
                    mHeightEditText.setText(heightvalue);
                }


                break;
            case UuidDictionary.WEIGHT: //Weight
                weightvalue = value;
                if (weightvalue != null && !weightvalue.isEmpty()) {
                    //mWeightSpinner.setSelection(mWeightArrayAdapter.getPosition(weightvalue + " " + getResources().getString(R.string.kg)), true);
                    mWeightEditText.setText(weightvalue);
                }
                //mWeightTextView.setText(value);
                break;
            case UuidDictionary.PULSE: //Pulse
                if (value != null && !value.isEmpty())
                    mPulseEditText.setText(value);
                break;
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
                if (value != null && !value.isEmpty())
                    mBpSysEditText.setText(value);
                break;
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
                if (value != null && !value.isEmpty())
                    mBpDiaEditText.setText(value);
                break;
            case UuidDictionary.TEMPERATURE: //Temperature

                //mTemperatureEditText.setText(value);
                if (new ConfigUtils(getActivity()).fahrenheit()) {
                    mTemperatureEditText.setText(convertCtoF(TAG, value));
                } else {
                    mTemperatureEditText.setText(value);
                }
                if (mTemperatureEditText.getText().toString().endsWith(".")) {
                    mTemperatureEditText.setText(mTemperatureEditText.getText().toString().replace(".", ""));
                }

                break;
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
                if (value != null && !value.isEmpty())
                    mRespEditText.setText(value);
                break;
            case UuidDictionary.SPO2: //SpO2
                if (value != null && !value.isEmpty())
                    mSpo2EditText.setText(value);
                break;

            case UuidDictionary.HEMOGLOBIN_LEVEL: //Hemoglobin
                Log.d(TAG, "parseData: value :: kz :: "+value);
                if (value != null && !value.isEmpty())

                    mHemoglobinEdittext.setText(value);
                break;

            case UuidDictionary.BLOOD_SUGAR_LEVEL: //Blood sugar
                if (value != null && !value.isEmpty())
                    mSugarEdittext.setText(value);
                break;
            default:
                break;

        }
        //on edit on vs screen, the bmi will be set in vitals bmi edit field.
        if (mBMITextView.getText().toString().equalsIgnoreCase("")) {
            calculateBMI_onEdit(heightvalue, weightvalue);
        }
    }

    public boolean validateTable() {
        boolean cancel = false;
        View focusView = null;

        String height = heightvalue;
        String weight = weightvalue;
        if (!weight.isEmpty() && height.isEmpty()) {
            mHeightErrorTextView.setVisibility(View.VISIBLE);
            mHeightErrorTextView.setText(getString(R.string.error_field_required));
            //mHeightSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
            mHeightErrorTextView.setVisibility(View.GONE);
            //mHeightSpinner.setBackgroundResource(R.drawable.edittext_border);
            mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (!height.isEmpty() && weight.isEmpty()) {
            mWeightErrorTextView.setVisibility(View.VISIBLE);
            mWeightErrorTextView.setText(getString(R.string.error_field_required));
            //mWeightSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
            mWeightErrorTextView.setVisibility(View.GONE);
            //mWeightSpinner.setBackgroundResource(R.drawable.edittext_border);
            mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        // Store values at the time of the fab is clicked.
        ArrayList<EditText> values = new ArrayList<EditText>();
        /*values.add(mHeight);
        values.add(mWeight);*/

        values.add(mBpSysEditText); //0
        values.add(mBpDiaEditText); // 1
        values.add(mSpo2EditText);// 2
        values.add(mPulseEditText); // 3
        values.add(mRespEditText); // 4
        values.add(mTemperatureEditText); // 5
      //  values.add(mHemoglobinEdittext); // 6
         //values.add(mSugarEdittext); // 7
       //  Check to see if values were inputted.
        for (int i = 0; i < values.size(); i++) {

            if (i == 3) {
                EditText et = values.get(i);
                String abc2 = et.getText().toString().trim();
                if (!abc2.isEmpty() && !abc2.equals("0.0")) {
                    if ((Double.parseDouble(abc2) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                            (Double.parseDouble(abc2) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {
                        //et.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        focusView = et;
                        mPulseErrorTextView.setText(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        mPulseErrorTextView.setVisibility(View.VISIBLE);
                        mPulseEditText.requestFocus();
                        mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        mPulseErrorTextView.setVisibility(View.GONE);
                        mPulseEditText.setBackgroundResource(R.drawable.edittext_border);
                    }
//       }
                } else {
                    mPulseErrorTextView.setVisibility(View.GONE);
                    mPulseEditText.setBackgroundResource(R.drawable.edittext_border);
                }

            } else if (i == 0) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS))) {
                        //et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

                        mBpSysErrorTextView.setText(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                        mBpSysErrorTextView.setVisibility(View.VISIBLE);
                        mBpSysEditText.requestFocus();
                        mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        //mBpSysErrorTextView.setVisibility(View.GONE);
                        //mBpSysEditText.setBackgroundResource(R.drawable.edittext_border);
                        String bpDia = mBpDiaEditText.getText().toString().trim();
                        if (bpDia.isEmpty()) {
                            mBpSysErrorTextView.setVisibility(View.GONE);
                            mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        } else {
                            int bpSysInt = Integer.parseInt(abc1);
                            int bpDiaInt = Integer.parseInt(bpDia);
                            if (bpDiaInt >= bpSysInt) {
                                mBpSysErrorTextView.setVisibility(View.VISIBLE);
                                mBpSysErrorTextView.setText(getString(R.string.bp_validation_sys));
                                mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                                cancel = true;
                                break;
                            } else {
                                mBpSysErrorTextView.setVisibility(View.GONE);
                                mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                            }
                        }
                    }
//       }
                } else {
                    mBpSysErrorTextView.setVisibility(View.GONE);
                    mBpSysEditText.setBackgroundResource(R.drawable.edittext_border);
                }

            } else if (i == 1) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS))) {
                        //et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mBpDiaErrorTextView.setText(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                        mBpDiaEditText.requestFocus();
                        mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        //mBpDiaErrorTextView.setVisibility(View.GONE);
                        //mBpDiaEditText.setBackgroundResource(R.drawable.edittext_border);
                        String bpSys = mBpSysEditText.getText().toString().trim();
                        if (bpSys.isEmpty()) {
                            mBpDiaErrorTextView.setVisibility(View.GONE);
                            mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        } else {
                            int bpSysInt = Integer.parseInt(bpSys);
                            int bpDiaInt = Integer.parseInt(abc1);
                            if (bpDiaInt >= bpSysInt) {
                                mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                                mBpDiaErrorTextView.setText(getString(R.string.bp_validation_dia));
                                mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                                cancel = true;
                                break;
                            } else {
                                mBpDiaErrorTextView.setVisibility(View.GONE);
                                mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                            }
                        }
                    }
//       }
                } else {
                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.edittext_border);
                }

            } else if (i == 5) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if (configUtils.celsius()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS))) {
                            //et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                            mTemperatureEditText.requestFocus();
                            mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                            cancel = true;
                            break;
                        } else {
                            mTemperatureErrorTextView.setVisibility(View.GONE);
                            mTemperatureEditText.setBackgroundResource(R.drawable.edittext_border);
                        }
                    } else if (configUtils.fahrenheit()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                            //et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                            mTemperatureEditText.requestFocus();
                            mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                            cancel = true;
                            break;
                        } else {
                            mTemperatureErrorTextView.setVisibility(View.GONE);
                            mTemperatureEditText.setBackgroundResource(R.drawable.edittext_border);
                        }
                    }
                } else {
                    mTemperatureErrorTextView.setVisibility(View.GONE);
                    mTemperatureEditText.setBackgroundResource(R.drawable.edittext_border);
                }
            } else if (i == 4) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_RESPIRATORY)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_RESPIRATORY))) {
                        //et.setError(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        mRespErrorTextView.setText(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        mRespErrorTextView.setVisibility(View.VISIBLE);
                        mRespEditText.requestFocus();
                        mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        mRespErrorTextView.setVisibility(View.GONE);
                        mRespEditText.setBackgroundResource(R.drawable.edittext_border);
                    }
//       }
                } else {
                    mRespErrorTextView.setVisibility(View.GONE);
                    mRespEditText.setBackgroundResource(R.drawable.edittext_border);
                }
            } else {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                        //et.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        mSpo2ErrorTextView.setText(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                        mSpo2EditText.requestFocus();
                        mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        mSpo2ErrorTextView.setVisibility(View.GONE);
                        mSpo2EditText.setBackgroundResource(R.drawable.edittext_border);
                    }
//       }
                } else {
                    mSpo2ErrorTextView.setVisibility(View.GONE);
                    mSpo2EditText.setBackgroundResource(R.drawable.edittext_border);
                }
            }
        }
        if (cancel) {
            return false;
        }

        if (mBpSysEditText.getText().toString().trim().isEmpty() && !mBpDiaEditText.getText().toString().trim().isEmpty()) {
            mBpSysErrorTextView.setVisibility(View.VISIBLE);
            mBpSysErrorTextView.setText(getString(R.string.error_field_required));
            mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mBpSysEditText.requestFocus();
            return false;
        }
        if (!mBpSysEditText.getText().toString().trim().isEmpty() && mBpDiaEditText.getText().toString().trim().isEmpty()) {
            mBpDiaErrorTextView.setVisibility(View.VISIBLE);
            mBpDiaErrorTextView.setText(getString(R.string.error_field_required));
            mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mBpDiaEditText.requestFocus();
            return false;
        }

        try {
            if (results == null) {
                results = new VitalsObject();
            }
            if (!height.equals("")) {
                results.setHeight(height);
            } else {
                results.setHeight("0");
            }
            if (!weight.isEmpty()) {
                results.setWeight(weight);
            }
            if (mPulseEditText.getText() != null) {
                results.setPulse((mPulseEditText.getText().toString()));
            }
            if (mBpDiaEditText.getText() != null) {
                results.setBpdia((mBpDiaEditText.getText().toString()));
            }
            if (mBpSysEditText.getText() != null) {
                results.setBpsys((mBpSysEditText.getText().toString()));
            }
            if (mTemperatureEditText.getText() != null && !mTemperatureEditText.getText().toString().isEmpty()) {
                if (configUtils.fahrenheit()) {
                    results.setTemperature(convertFtoC(TAG, mTemperatureEditText.getText().toString()));
                } else {
                    results.setTemperature((mTemperatureEditText.getText().toString()));
                }
            }
            if (mRespEditText.getText() != null) {
                results.setResp((mRespEditText.getText().toString()));
            }
            if (mSpo2EditText.getText() != null) {
                results.setSpo2((mSpo2EditText.getText().toString()));
            }
            if (mSpo2EditText.getText() != null) {
                results.setBmi(mBMITextView.getText().toString().split(" ")[0]);
            }
            if (mHemoglobinEdittext.getText() != null) {
                results.setHemoglobin((mHemoglobinEdittext.getText().toString()));
            }
            if (mSugarEdittext.getText() != null) {
                results.setBloodSugar((mSugarEdittext.getText().toString()));
            }

        } catch (NumberFormatException e) {
            //Snackbar.make(findViewById(R.id.cl_table), R.string.error_non_decimal_no_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

//


        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        if (getActivity().getIntent().equals("edit")) {
            try {
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.HEIGHT);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                if (results.getHeight().equals("")) {
                    obsDTO.setValue("0");
                } else {
                    obsDTO.setValue(results.getHeight());
                }
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.HEIGHT));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.WEIGHT);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getWeight());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.WEIGHT));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.PULSE);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getPulse());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.PULSE));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBpsys());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SYSTOLIC_BP));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBpdia());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.DIASTOLIC_BP));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getTemperature());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.TEMPERATURE));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getResp());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SPO2);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getSpo2());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SPO2));

                obsDAO.updateObs(obsDTO);

                //For Hemoglobin
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN_LEVEL);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getHemoglobin());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.HEMOGLOBIN_LEVEL));

                obsDAO.updateObs(obsDTO);

                //For blood sugar
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.BLOOD_SUGAR_LEVEL);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBloodSugar());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BLOOD_SUGAR_LEVEL));

                obsDAO.updateObs(obsDTO);


                //making flag to false in the encounter table so it will sync again
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    encounterDAO.updateEncounterSync("false", encounterVitals);
                    encounterDAO.updateEncounterModifiedDate(encounterVitals);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                /*Intent intent = new Intent(getActivity(), VisitSummaryActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("gender", patientGender);
                intent.putExtra("tag", intentTag);
                intent.putExtra("hasPrescription", "false");
                startActivity(intent);*/
            } catch (DAOException dao) {
                FirebaseCrashlytics.getInstance().recordException(dao);
            }
        } else {

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.HEIGHT);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            if (results.getHeight().equals("")) {
                obsDTO.setValue("0");
            } else {
                obsDTO.setValue(results.getHeight());
            }

            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.WEIGHT);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getWeight());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.PULSE);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getPulse());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBpsys());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBpdia());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getTemperature());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getResp());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SPO2);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getSpo2());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            //for hemoglobin
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN_LEVEL);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getHemoglobin());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            //for blood sugar
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.BLOOD_SUGAR_LEVEL);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBloodSugar());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            /*Intent intent = new Intent(getActivity(), ComplaintNodeActivity.class);

            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
            intent.putExtra("tag", intentTag);
            startActivity(intent);*/
        }
        return true;
    }

    /*private String convertFtoC(String temperature) {
        Log.i(TAG, "convertFtoC IN: " + temperature);
        if (temperature != null && temperature.length() > 0) {
            String result = "";
            double fTemp = Double.parseDouble(temperature);
            double cTemp = ((fTemp - 32) * 5 / 9);

//            DecimalFormat dtime = new DecimalFormat("#.##");
            DecimalFormat dtime = new DecimalFormat("#.#");
            cTemp = Double.parseDouble(dtime.format(cTemp));
            result = String.format("%.1f", cTemp);
            //result = String.valueOf(cTemp);
            Log.i(TAG, "convertFtoC OUT: " + result);

            return result;
        }
        return "";

    }

    private String convertCtoF(String temperature) {
        Log.i(TAG, "convertCtoF IN: " + temperature);

        if (temperature == null) return "";
        String result = "";
        double a = Double.parseDouble(String.valueOf(temperature));
        Double b = (a * 9 / 5) + 32;

        //DecimalFormat dtime = new DecimalFormat("#.##");
        DecimalFormat dtime = new DecimalFormat("#.#");
        b = Double.parseDouble(dtime.format(b));
        result = String.format("%.1f", b);
        //result = String.valueOf(b);
        Log.i(TAG, "convertCtoF OUT: " + result);
        return result;

    }*/


}