package org.intelehealth.app.ayu.visit.pocdevice.Lung;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorOneFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Heart.AorticFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lateral.LateralFourFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lateral.LateralOneFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lateral.LateralThreeFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lateral.LateralTwoFragment;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentLateralBinding;
import org.intelehealth.app.models.VitalsObject;

import java.io.Serializable;


public class LateralFragment extends Fragment {
    private FragmentLateralBinding mBinding;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;

    InteleHealthDatabaseHelper db;
    private static final String ARG_PATIENT_UUID = "patientUuid";
    private static final String ARG_VISIT_UUID = "visitUuid";
    private static final String ARG_INTENT_TAG = "intentTag";
    private static final String ARG_AGE = "float_ageYear_Month";
    private static final String ARG_TYPE = "type";
    private static  final String ARG_PATINT_NAME = "patientName";
    private static final String ENCOUNTER_UUID = "encounterUuid";


    String patientUuid, visitUuid, patientName,   encounterUuid,intentTag, type;
    float float_ageYear_Month;

    private CommonVisitData commonVisitData;
    public static LateralFragment newInstance(boolean isEditMode,
                                             String patientName,
                                             String patientUuid,
                                             String visitUuid,
                                             String encounterUuid,
                                             String intentTag,
                                             float float_ageYear_Month,
                                             String type) {

        LateralFragment lateralFragment = new LateralFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEditMode", isEditMode);

        args.putString(ARG_PATIENT_UUID, patientUuid);
        args.putString(ARG_PATINT_NAME,patientName);
        args.putString(ARG_VISIT_UUID, visitUuid);
        args.putString(ENCOUNTER_UUID, encounterUuid);
        args.putString(ARG_INTENT_TAG, intentTag);
        args.putFloat(ARG_AGE, float_ageYear_Month);
        args.putString(ARG_TYPE, type); // Heart / Lung

        lateralFragment.setArguments(args);
        return lateralFragment;

    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_lateral, container, false);
        setTabList();
        return mBinding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            patientUuid = getArguments().getString(ARG_PATIENT_UUID);
            patientName = getArguments().getString(ARG_PATINT_NAME);
            visitUuid = getArguments().getString(ARG_VISIT_UUID);
            encounterUuid = getArguments().getString(ENCOUNTER_UUID);
            intentTag = getArguments().getString(ARG_INTENT_TAG);
            float_ageYear_Month = getArguments().getFloat(ARG_AGE);
            type = getArguments().getString(ARG_TYPE);
        }

    }
    private void setTabList() {
        mBinding.tabLayoutLateral.addTab(mBinding.tabLayoutLateral.newTab().setText("L1"));
        mBinding.tabLayoutLateral.addTab(mBinding.tabLayoutLateral.newTab().setText("L2"));
        mBinding.tabLayoutLateral.addTab(mBinding.tabLayoutLateral.newTab().setText("L3"));
        mBinding.tabLayoutLateral.addTab(mBinding.tabLayoutLateral.newTab().setText("L4"));
        mBinding.tabLayoutLateral.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        loadFragment(LateralOneFragment.newInstance(
                                mIsEditMode,
                                patientName,
                                patientUuid,
                                visitUuid,
                                encounterUuid,
                                intentTag,
                                float_ageYear_Month,
                                "lung"));
                        break;
                    case 1:
                        loadFragment(LateralTwoFragment.newInstance(
                                mIsEditMode,
                                patientName,
                                patientUuid,
                                visitUuid,
                                encounterUuid,
                                intentTag,
                                float_ageYear_Month,
                                "lung"));
                        break;
                    case 2:
                        loadFragment(LateralThreeFragment.newInstance(
                                mIsEditMode,
                                patientName,
                                patientUuid,
                                visitUuid,
                                encounterUuid,
                                intentTag,
                                float_ageYear_Month,
                                "lung"));
                        break;
                    case 3:
                        loadFragment(LateralFourFragment.newInstance(
                                mIsEditMode,
                                patientName,
                                patientUuid,
                                visitUuid,
                                encounterUuid,
                                intentTag,
                                float_ageYear_Month,
                                "lung"));
                        break;
                }

            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        mBinding.tabLayoutLateral.getTabAt(0).select();
        loadFragment(LateralOneFragment.newInstance(
                mIsEditMode,
                patientName,
                patientUuid,
                visitUuid,
                encounterUuid,
                intentTag,
                float_ageYear_Month,
                "lung"));
    }
    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_lateral_layout, fragment)
                .commit();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}