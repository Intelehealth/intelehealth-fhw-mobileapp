package org.intelehealth.app.ui2.visit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;

public class VitalCollectionFragment extends Fragment implements View.OnClickListener {
    private VisitCreationActionListener mActionListener;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;

    TextView mHeight, mWeight, mBMI, mBmiStatusTextView;
    EditText mPulse, mBpSys, mBpDia, mTemperature, mSpo2, mResp;
    private Button mSubmitButton;

    public VitalCollectionFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionFragment newInstance(Bundle data) {
        VitalCollectionFragment fragment = new VitalCollectionFragment();
        fragment.patientUuid = data.getString("patientUuid");
        fragment.visitUuid = data.getString("visitUuid");
        fragment.encounterVitals = data.getString("encounterUuidVitals");
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
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

        mHeight = view.findViewById(R.id.tv_height);
        mWeight = view.findViewById(R.id.tv_weight);

        mBMI = view.findViewById(R.id.tv_bmi_value);
        mBmiStatusTextView = view.findViewById(R.id.tv_bmi_status);

        mBpSys = view.findViewById(R.id.etv_bp_sys);
        mBpDia = view.findViewById(R.id.etv_bp_dia);

        mSpo2 = view.findViewById(R.id.etv_spo2);
        mPulse = view.findViewById(R.id.etv_pulse);
        mResp = view.findViewById(R.id.etv_respiratory_rate);
        mTemperature = view.findViewById(R.id.etv_temperature);

        mSubmitButton = view.findViewById(R.id.btn_submit);
        mSubmitButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                //validate
                mActionListener.onProgress(100);
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY);
                break;
        }
    }
}