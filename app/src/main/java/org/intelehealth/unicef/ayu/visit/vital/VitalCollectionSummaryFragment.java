package org.intelehealth.unicef.ayu.visit.vital;

import static org.intelehealth.unicef.syncModule.SyncUtils.syncNow;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.ayu.visit.VisitCreationActionListener;
import org.intelehealth.unicef.ayu.visit.VisitCreationActivity;
import org.intelehealth.unicef.models.VitalsObject;
import org.intelehealth.unicef.utilities.ConfigUtils;
import org.intelehealth.unicef.utilities.NetworkConnection;
import org.intelehealth.unicef.utilities.SessionManager;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VitalCollectionSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VitalCollectionSummaryFragment extends Fragment {

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private VitalsObject mVitalsObject;

    public VitalCollectionSummaryFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionSummaryFragment newInstance(VitalsObject result) {
        VitalCollectionSummaryFragment fragment = new VitalCollectionSummaryFragment();
        fragment.mVitalsObject = result;
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

    private String convertFtoC(String temperature) {

        if (temperature != null && temperature.length() > 0) {
            String result = "";
            double fTemp = Double.parseDouble(temperature);
            double cTemp = ((fTemp - 32) * 5 / 9);
            Log.i("TAG", "uploadTemperatureInC: " + cTemp);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_collection_summary, container, false);
        if (mVitalsObject.getHeight() != null && !mVitalsObject.getHeight().isEmpty() && !mVitalsObject.getHeight().equalsIgnoreCase("0"))
            ((TextView) view.findViewById(R.id.tv_height)).setText(mVitalsObject.getHeight() + " " + getResources().getString(R.string.cm));
        else
            ((TextView) view.findViewById(R.id.tv_height)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getWeight() != null && !mVitalsObject.getWeight().isEmpty())
            ((TextView) view.findViewById(R.id.tv_weight)).setText(mVitalsObject.getWeight() + " " + getResources().getString(R.string.kg));
        else
            ((TextView) view.findViewById(R.id.tv_weight)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getBmi() != null && !mVitalsObject.getBmi().isEmpty())
            ((TextView) view.findViewById(R.id.tv_bmi)).setText(mVitalsObject.getBmi() + " " + getResources().getString(R.string.kg_m));
        else
            ((TextView) view.findViewById(R.id.tv_bmi)).setText(getString(R.string.ui2_no_information));


        if (mVitalsObject.getBpsys() != null && !mVitalsObject.getBpsys().isEmpty())
            ((TextView) view.findViewById(R.id.tv_bp)).setText(mVitalsObject.getBpsys() + "/" + mVitalsObject.getBpdia());
        else
            ((TextView) view.findViewById(R.id.tv_bp)).setText(getString(R.string.ui2_no_information));
        if (mVitalsObject.getPulse() != null && !mVitalsObject.getPulse().isEmpty())
            ((TextView) view.findViewById(R.id.tv_pulse)).setText(mVitalsObject.getPulse() + " " + getResources().getString(R.string.bpm));
        else
            ((TextView) view.findViewById(R.id.tv_pulse)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getTemperature() != null && !mVitalsObject.getTemperature().isEmpty()) {
            if (new ConfigUtils(getActivity()).fahrenheit()) {
                ((TextView) view.findViewById(R.id.tv_temperature)).setText(convertCtoF(mVitalsObject.getTemperature()));
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

        if (mVitalsObject.getResp() != null && !mVitalsObject.getResp().isEmpty())
            ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(mVitalsObject.getResp() + " " + getResources().getString(R.string.breaths_min));
        else
            ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(getString(R.string.ui2_no_information));

        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON,false, mVitalsObject);
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
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, true, mVitalsObject);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, true, mVitalsObject);
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

    private ObjectAnimator syncAnimator;
}