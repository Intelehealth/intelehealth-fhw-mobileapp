package org.intelehealth.app.ayu.visit.vital;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.model.BMIStatus;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.ConfigUtils;
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
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    private Spinner mHeightTextView, mWeightTextView;
    TextView mBMITextView, mBmiStatusTextView;
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

    public VitalCollectionFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionFragment newInstance(Intent intent) {
        VitalCollectionFragment fragment = new VitalCollectionFragment();


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
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
        configUtils = new ConfigUtils(context);

        mHeightMasterList = getNumbersInRange(60, 250);
        mWeightMasterList = getNumbersInRange(1, 150);
    }

    /*public List<Integer> getNumbersUsingIntStreamRange(int start, int end) {
        return IntStream.range(start, end)
                .boxed()
                .collect(Collectors.toList());
    }*/
    public List<Integer> getNumbersInRange(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
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

        mHeightTextView = view.findViewById(R.id.sp_height);
        mWeightTextView = view.findViewById(R.id.sp_weight);
        /*mHeightTextView.setOnClickListener(this);
        mWeightTextView.setOnClickListener(this);*/

        mBMITextView = view.findViewById(R.id.tv_bmi_value);
        mBmiStatusTextView = view.findViewById(R.id.tv_bmi_status);

        mBpSysEditText = view.findViewById(R.id.etv_bp_sys);
        mBpDiaEditText = view.findViewById(R.id.etv_bp_dia);

        mSpo2EditText = view.findViewById(R.id.etv_spo2);
        mPulseEditText = view.findViewById(R.id.etv_pulse);
        mRespEditText = view.findViewById(R.id.etv_respiratory_rate);
        mTemperatureEditText = view.findViewById(R.id.etv_temperature);

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

        mBpSysEditText.addTextChangedListener(new MyTextWatcher(mBpSysEditText));
        mBpDiaEditText.addTextChangedListener(new MyTextWatcher(mBpDiaEditText));
        mSpo2EditText.addTextChangedListener(new MyTextWatcher(mSpo2EditText));
        mPulseEditText.addTextChangedListener(new MyTextWatcher(mPulseEditText));
        mRespEditText.addTextChangedListener(new MyTextWatcher(mRespEditText));
        mTemperatureEditText.addTextChangedListener(new MyTextWatcher(mTemperatureEditText));

        mSubmitButton = view.findViewById(R.id.btn_submit);
        mSubmitButton.setOnClickListener(this);


        showHeightListing();


        showWeightListing();


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
            if (this.editText.getId() == R.id.etv_bp_sys) {
                if (val.isEmpty()) {
                    mBpSysErrorTextView.setVisibility(View.VISIBLE);
                    mBpSysErrorTextView.setText(getString(R.string.error_field_required));
                    mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mBpSysErrorTextView.setVisibility(View.GONE);
                    mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.etv_bp_dia) {
                if (val.isEmpty()) {
                    mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                    mBpDiaErrorTextView.setText(getString(R.string.error_field_required));
                    mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                //validate
                if (validateTable()) {
                    mActionListener.onProgress(100);
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY, results);
                }
                break;

        }
    }


    private void showHeightListing() {
        // add a list
        final String[] data = new String[mHeightMasterList.size() + 1];
        data[0] = "Select Height";
        for (int i = 1; i < data.length; i++) {
            data[i] = String.valueOf(mHeightMasterList.get(i - 1)) + " cm";
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, data);
        adaptador.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mHeightTextView.setAdapter(adaptador);
        mHeightTextView.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));
        mHeightTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                if (which != 0) {
                    heightvalue = data[which].split(" ")[0];
                    calculateBMI();
                    mHeightErrorTextView.setVisibility(View.GONE);
                    mHeightTextView.setBackgroundResource(R.drawable.edittext_border);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void showWeightListing() {

        // add a list
        final String[] data = new String[mWeightMasterList.size() + 1];
        data[0] = "Select Wight";
        for (int i = 1; i < data.length; i++) {
            data[i] = String.valueOf(mWeightMasterList.get(i - 1)) + " kg";
        }
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, data);
        adaptador.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        mWeightTextView.setAdapter(adaptador);
        mWeightTextView.setPopupBackgroundDrawable(getActivity().getDrawable(R.drawable.popup_menu_background));

        mWeightTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                if (which != 0) {
                    weightvalue = data[which].split(" ")[0];
                    calculateBMI();
                    mWeightErrorTextView.setVisibility(View.GONE);
                    mWeightTextView.setBackgroundResource(R.drawable.edittext_border);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }


    private BMIStatus getBmiStatus(double bmi) {
        BMIStatus bmiStatus = new BMIStatus();
        if (bmi < 18.5) {
            bmiStatus.setStatus("Underweight");
            bmiStatus.setColor(R.color.ui2_bmi1);
        } else if (bmi > 18.5 && bmi <= 24.9) {
            bmiStatus.setStatus("Normal");
            bmiStatus.setColor(R.color.ui2_bmi2);
        } else if (bmi > 24.9 && bmi <= 29.9) {
            bmiStatus.setStatus("Overweight");
            bmiStatus.setColor(R.color.ui2_bmi3);
        } else if (bmi > 29.9 && bmi <= 34.9) {
            bmiStatus.setStatus("Obese");
            bmiStatus.setColor(R.color.ui2_bmi4);
        } else if (bmi > 34.9 && bmi <= 39.9) {
            bmiStatus.setStatus("Severely Obese");
            bmiStatus.setColor(R.color.ui2_bmi5);
        } else {
            bmiStatus.setStatus("Morbidly Obese");
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
        if (height.toString().trim().length() > 0 && !height.toString().startsWith(".") &&
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

    public void loadPrevious() {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
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
                break;
            case UuidDictionary.WEIGHT: //Weight
                weightvalue = value;
                //mWeightTextView.setText(value);
                break;
            case UuidDictionary.PULSE: //Pulse
                mPulseEditText.setText(value);
                break;
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
                mBpSysEditText.setText(value);
                break;
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
                mBpDiaEditText.setText(value);
                break;
            case UuidDictionary.TEMPERATURE: //Temperature

                mTemperatureEditText.setText(value);


                break;
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
                mRespEditText.setText(value);
                break;
            case UuidDictionary.SPO2: //SpO2
                mSpo2EditText.setText(value);
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
            mHeightTextView.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
            mHeightErrorTextView.setVisibility(View.GONE);
            mHeightTextView.setBackgroundResource(R.drawable.edittext_border);
        }

        if (!height.isEmpty() && weight.isEmpty()) {
            mWeightErrorTextView.setVisibility(View.VISIBLE);
            mWeightErrorTextView.setText(getString(R.string.error_field_required));
            mWeightTextView.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
            mWeightErrorTextView.setVisibility(View.GONE);
            mWeightTextView.setBackgroundResource(R.drawable.edittext_border);
        }

        /*//BP vaidations added by Prajwal.
        if (mBpSysEditText.getText().toString().isEmpty() && !mBpDiaEditText.getText().toString().isEmpty() ||
                !mBpSysEditText.getText().toString().isEmpty() && mBpDiaEditText.getText().toString().isEmpty()) {
            if (mBpSysEditText.getText().toString().isEmpty()) {
                mBpSysEditText.requestFocus();
//                mBpSys.setError("Enter field");
                mBpSysEditText.setError(getResources().getString(R.string.error_field_required));
                return false;
            }

            if (mBpDiaEditText.getText().toString().isEmpty()) {
                mBpDiaEditText.requestFocus();
//                mBpDia.setError("Enter field");
                mBpDiaEditText.setError(getResources().getString(R.string.error_field_required));
                return false;
            }
        }else{
            if (val.isEmpty()) {
                mBpSysErrorTextView.setVisibility(View.VISIBLE);
                mBpSysErrorTextView.setText(getString(R.string.error_field_required));
                mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            } else {
                mBpSysErrorTextView.setVisibility(View.GONE);
                mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

            mBpDiaErrorTextView.setVisibility(View.GONE);
            mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

            mBpSysErrorTextView.setVisibility(View.GONE);
            mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }*/

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

        // Check to see if values were inputted.
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
                        mBpSysErrorTextView.setVisibility(View.GONE);
                        mBpSysEditText.setBackgroundResource(R.drawable.edittext_border);
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
                        mBpDiaErrorTextView.setVisibility(View.GONE);
                        mBpDiaEditText.setBackgroundResource(R.drawable.edittext_border);
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

        try {
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
                    results.setTemperature(convertFtoC(mTemperatureEditText.getText().toString()));
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

    private String convertFtoC(String temperature) {

        if (temperature != null && temperature.length() > 0) {
            String result = "";
            double fTemp = Double.parseDouble(temperature);
            double cTemp = ((fTemp - 32) * 5 / 9);
            Log.i(TAG, "uploadTemperatureInC: " + cTemp);
            DecimalFormat dtime = new DecimalFormat("#.##");
            cTemp = Double.parseDouble(dtime.format(cTemp));
            result = String.valueOf(cTemp);
            return result;
        }
        return "";

    }

    private String convertCtoF(String temperature) {

        String result = "";
        double a = Double.parseDouble(String.valueOf(temperature));
        Double b = (a * 9 / 5) + 32;

        DecimalFormat dtime = new DecimalFormat("#.##");
        b = Double.parseDouble(dtime.format(b));

        result = String.valueOf(b);
        return result;

    }


}