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
import org.intelehealth.app.databinding.FragmentRecordHeartsoundsBinding;
import org.intelehealth.app.models.VitalsObject;

public class RecordHeartSoundsFragment extends Fragment{

    private FragmentRecordHeartsoundsBinding mBinding;
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_heartsounds, container, false);
        return mBinding.getRoot();
    }
    public static RecordHeartSoundsFragment newInstance(boolean isEditMode, VitalsObject vitalsObject) {
        RecordHeartSoundsFragment fragment = new RecordHeartSoundsFragment();

        return fragment;
    }
    private void setTabList(){
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Aortic"));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Pulmonic"));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Tricuspid"));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Mitral"));
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        mBinding.imgdirection.setImageDrawable(getResources().getDrawable(R.drawable.heart_pos1));
                        break;
                    case 1:
                        mBinding.imgdirection.setImageDrawable(getResources().getDrawable(R.drawable.heart_pos2));
                        break;
                    case 2:
                        mBinding.imgdirection.setImageDrawable(getResources().getDrawable(R.drawable.heart_pos3));
                        break;
                    case 3:
                        mBinding.imgdirection.setImageDrawable(getResources().getDrawable(R.drawable.heart_pos4));
                        break;
                }

            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}
