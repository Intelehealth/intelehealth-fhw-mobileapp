package org.intelehealth.app.ayu.visit.vital;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_collection_summary, container, false);
        ((TextView) view.findViewById(R.id.tv_height)).setText(mVitalsObject.getHeight());
        ((TextView) view.findViewById(R.id.tv_weight)).setText(mVitalsObject.getWeight());
        ((TextView) view.findViewById(R.id.tv_bmi)).setText(mVitalsObject.getBmi() + " kg/m");
        ((TextView) view.findViewById(R.id.tv_bp)).setText(mVitalsObject.getBpsys() + "/" + mVitalsObject.getBpdia());
        ((TextView) view.findViewById(R.id.tv_pulse)).setText(mVitalsObject.getPulse() + " bpm");
        ((TextView) view.findViewById(R.id.tv_temperature)).setText(mVitalsObject.getTemperature());
        ((TextView) view.findViewById(R.id.tv_spo2)).setText(mVitalsObject.getSpo2() + " %");
        ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(mVitalsObject.getResp() + " breaths/min");
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON, mVitalsObject);
            }
        });
        view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mVitalsObject);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mVitalsObject);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mVitalsObject);
            }
        });
        view.findViewById(R.id.imb_btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getActivity())) {
                    new SyncUtils().syncBackground();
                    Toast.makeText(getActivity(), getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}