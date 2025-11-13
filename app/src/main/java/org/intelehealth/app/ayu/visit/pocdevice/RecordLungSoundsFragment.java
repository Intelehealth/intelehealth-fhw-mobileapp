package org.intelehealth.app.ayu.visit.pocdevice;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.databinding.FragmentRecordLungSoundsBinding;
import org.intelehealth.app.models.VitalsObject;

public class RecordLungSoundsFragment extends Fragment{

    private FragmentRecordLungSoundsBinding mBinding;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.ivBackArrowTerms.setOnClickListener(view1 -> {
            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
        });
        mBinding.btnCancel.setOnClickListener(view1 -> {
            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
        });
        setTabList();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_lung_sounds, container, false);
        return mBinding.getRoot();
    }
    public static RecordLungSoundsFragment newInstance(boolean isEditMode, VitalsObject vitalsObject) {
        RecordLungSoundsFragment fragment = new RecordLungSoundsFragment();

        return fragment;
    }
    private void setTabList(){
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Anterior"));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("lateral"));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Posterior"));

        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        setAnteriorTab();
                        break;
                    case 1:
                        setlateralTab();
                        break;
                    case 2:
                        setPosteriorTab();
                        break;

                }

            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void setAnteriorTab(){
        mBinding.imgdirection.setVisibility(View.VISIBLE);
        mBinding.imgdirection.setImageDrawable(getResources().getDrawable(R.drawable.lung_front));
        mBinding.llpos1.setVisibility(View.VISIBLE);
        mBinding.llpos2.setVisibility(View.VISIBLE);
        mBinding.llpos3.setVisibility(View.VISIBLE);
        mBinding.llpos4.setVisibility(View.GONE);
        mBinding.llpos5.setVisibility(View.GONE);
        mBinding.llpos6.setVisibility(View.GONE);
        mBinding.llsidepos.setVisibility(View.GONE);
    }

    private void setPosteriorTab(){
        mBinding.imgdirection.setVisibility(View.VISIBLE);
        mBinding.imgdirection.setImageDrawable(getResources().getDrawable(R.drawable.lung_back));
        mBinding.llpos4.setVisibility(View.VISIBLE);
        mBinding.llpos5.setVisibility(View.VISIBLE);
        mBinding.llpos6.setVisibility(View.VISIBLE);
        mBinding.llpos1.setVisibility(View.GONE);
        mBinding.llpos2.setVisibility(View.GONE);
        mBinding.llpos3.setVisibility(View.GONE);
        mBinding.llsidepos.setVisibility(View.GONE);
    }
    private void setlateralTab(){
        mBinding.imgdirection.setVisibility(View.GONE);
        mBinding.llpos4.setVisibility(View.GONE);
        mBinding.llpos5.setVisibility(View.GONE);
        mBinding.llpos6.setVisibility(View.GONE);
        mBinding.llpos1.setVisibility(View.GONE);
        mBinding.llpos2.setVisibility(View.GONE);
        mBinding.llpos3.setVisibility(View.GONE);
        mBinding.llsidepos.setVisibility(View.VISIBLE);
    }
}
