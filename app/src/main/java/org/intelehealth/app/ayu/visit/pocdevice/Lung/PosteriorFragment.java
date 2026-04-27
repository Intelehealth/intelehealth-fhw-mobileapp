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
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorFiveFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorFourFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorOneFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorSixFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorThreeFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Anterior.AnteriorTwoFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lateral.LateralOneFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Posterior.PosteriorFiveFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Posterior.PosteriorFourFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Posterior.PosteriorOneFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Posterior.PosteriorSixFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Posterior.PosteriorThreeFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Posterior.PosteriorTwoFragment;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentAnteriorBinding;
import org.intelehealth.app.databinding.FragmentPosteriorBinding;
import org.intelehealth.app.models.VitalsObject;

import java.io.Serializable;

public class PosteriorFragment extends Fragment {
    private FragmentPosteriorBinding mBinding;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;
    private static final String ARG_PATIENT_UUID = "patientUuid";
    private static final String ARG_VISIT_UUID = "visitUuid";
    private static final String ARG_PATIENT_NAME = "patientName";
    private static final String ARG_INTENT_TAG = "intentTag";
    private static final String ARG_AGE = "float_ageYear_Month";
    private static final String ARG_TYPE = "type";
    private static  final String ARG_PATINT_NAME = "patientName";
    private static final String ENCOUNTER_UUID = "encounterUuid";
    String patientName, patientUuid, visitUuid, encounterUuid,intentTag, type;
    float float_ageYear_Month;

    private CommonVisitData commonVisitData;

    public static PosteriorFragment newInstance(boolean isEditMode,
                                              String patientName,
                                              String patientUuid,
                                              String visitUuid,
                                              String encounterUuid,
                                              String intentTag,
                                              float float_ageYear_Month,
                                              String type) {

        PosteriorFragment posteriorFragment = new PosteriorFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEditMode", isEditMode);

        args.putString(ARG_PATIENT_UUID, patientUuid);
        args.putString(ARG_PATINT_NAME,patientName);
        args.putString(ARG_VISIT_UUID, visitUuid);
        args.putString(ENCOUNTER_UUID, encounterUuid);
        args.putString(ARG_INTENT_TAG, intentTag);
        args.putFloat(ARG_AGE, float_ageYear_Month);
        args.putString(ARG_TYPE, type); // Heart / Lung

        posteriorFragment.setArguments(args);
        return posteriorFragment;

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_posterior, container, false);
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
        mBinding.tabPosteriorFrg.addTab(mBinding.tabPosteriorFrg.newTab().setText("P1"));
        mBinding.tabPosteriorFrg.addTab(mBinding.tabPosteriorFrg.newTab().setText("P2"));
        mBinding.tabPosteriorFrg.addTab(mBinding.tabPosteriorFrg.newTab().setText("P3"));
        mBinding.tabPosteriorFrg.addTab(mBinding.tabPosteriorFrg.newTab().setText("P4"));
        mBinding.tabPosteriorFrg.addTab(mBinding.tabPosteriorFrg.newTab().setText("P5"));
        mBinding.tabPosteriorFrg.addTab(mBinding.tabPosteriorFrg.newTab().setText("P6"));
        mBinding.tabPosteriorFrg.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        loadFragment(PosteriorOneFragment.newInstance(
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
                        loadFragment(PosteriorTwoFragment.newInstance(
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
                        loadFragment(PosteriorThreeFragment.newInstance(
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
                        loadFragment(PosteriorFourFragment.newInstance(
                                mIsEditMode,
                                patientName,
                                patientUuid,
                                visitUuid,
                                encounterUuid,
                                intentTag,
                                float_ageYear_Month,
                                "lung"));
                        break;
                    case 4:
                        loadFragment(PosteriorFiveFragment.newInstance(
                                mIsEditMode,
                                patientName,
                                patientUuid,
                                visitUuid,
                                encounterUuid,
                                intentTag,
                                float_ageYear_Month,
                                "lung"));
                        break;
                    case 5:
                        loadFragment(PosteriorSixFragment.newInstance(
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
        mBinding.tabPosteriorFrg.getTabAt(0).select();
        loadFragment(PosteriorOneFragment.newInstance(
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
                .replace(R.id.fragment_posterior_layout, fragment)
                .commit();
    }


}