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
import org.intelehealth.app.ayu.visit.model.CommonVisitData;

import org.intelehealth.app.databinding.FragmentRecordHeartsoundsBinding;
import org.intelehealth.app.models.VitalsObject;

import java.util.ArrayList;

public class RecordHeartSoundsFragment extends Fragment {

    private FragmentRecordHeartsoundsBinding mBinding;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;

    private String visitUuid, mSummaryString;
    private CommonVisitData commonVisitData;
    private OnRecordingCompleteListener recordingListener;
    private ArrayList<String> heartSounds;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;

        if (context instanceof OnRecordingCompleteListener) {
            recordingListener = (OnRecordingCompleteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRecordingCompleteListener");
        }
    }

    public interface OnRecordingCompleteListener {
        void onRecordingCompleted(String type); // "heart"
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTabList();

        mBinding.ivBackArrowTerms.setOnClickListener(v -> requireActivity().onBackPressed());

        mBinding.btnFinish.setVisibility(View.VISIBLE);
        mBinding.btnFinish.setOnClickListener(v -> {
            new org.intelehealth.app.syncModule.SyncUtils().syncBackground();
            if (recordingListener != null) {
                recordingListener.onRecordingCompleted("heart");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_heartsounds, container, false);
        return mBinding.getRoot();

    }

    public static RecordHeartSoundsFragment newInstance(CommonVisitData mCommonVisitData, boolean isEditMode, VitalsObject vitalsObject, String visitUuid, ArrayList<String> heartSounds) {
        RecordHeartSoundsFragment recordHeartSoundsFragment = new RecordHeartSoundsFragment();
        recordHeartSoundsFragment.mVitalsObject = vitalsObject;
        recordHeartSoundsFragment.commonVisitData = mCommonVisitData;
        recordHeartSoundsFragment.mIsEditMode = isEditMode;
        recordHeartSoundsFragment.visitUuid = visitUuid;
        recordHeartSoundsFragment.heartSounds = heartSounds;
        return recordHeartSoundsFragment;
    }

    private void setTabList() {

        heartSounds = getArguments().getStringArrayList("heartSounds");

        mBinding.tabLayout.removeAllTabs();

        for (String sound : heartSounds) {
            mBinding.tabLayout.addTab(
                    mBinding.tabLayout.newTab().setText(sound)
            );
        }

        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                String selected = tab.getText().toString();

                loadSound(selected); // 👈 important
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (mBinding.tabLayout.getTabCount() > 0) {
            mBinding.tabLayout.getTabAt(0).select();
        }
    }

    private void loadSound(String soundName) {

        // Example:
        //mBinding.tvTitle.setText(soundName);
        // You can handle logic here:
       /* if (soundName.equalsIgnoreCase("Aortic")) {
            // show Aortic UI / recording
            loadFragment(AorticFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
        } else if (soundName.equalsIgnoreCase("Pulmonic")) {
            // show Pulmonic UI
            loadFragment(PulmonicFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
        } else if (soundName.equalsIgnoreCase("Tricuspid")) {
            loadFragment(TricuspidFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
        } else if (soundName.equalsIgnoreCase("Mitral")) {
            loadFragment(TricuspidFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
        }*/
    }

    /* private void setTabList() {
         SessionManager sessionManager = new SessionManager(requireContext());
         Set<String> selectedExams = sessionManager.getVisitSummary(commonVisitData.getPatientUuid());

         boolean hasAortic = false;
         boolean hasPulmonic = false;
         boolean hasTricuspid = false;
         boolean hasMitral = false;

         if (selectedExams != null) {
             for (String exam : selectedExams) {
                 if (exam.contains("Sound Heart: Aortic")) hasAortic = true;
                 if (exam.contains("Sound Heart: Pulmonic")) hasPulmonic = true;
                 if (exam.contains("Sound Heart: Tricuspid")) hasTricuspid = true;
                 if (exam.contains("Sound Heart: Mitral")) hasMitral = true;
             }
         }

         if (hasAortic) {
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Aortic"));
         }
         if (hasPulmonic) {
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Pulmonic"));
         }
         if (hasTricuspid) {
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Tricuspid"));
         }
         if (hasMitral) {
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Mitral"));
         }

         if (mBinding.tabLayout.getTabCount() == 0) {
             // Default to all if none found
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Aortic"));
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Pulmonic"));
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Tricuspid"));
             mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText("Mitral"));
         }

         mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
             @Override
             public void onTabSelected(TabLayout.Tab tab) {
                 String tabText = tab.getText().toString();
                 if (tabText.equalsIgnoreCase("Aortic")) {
                     loadFragment(AorticFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
                 } else if (tabText.equalsIgnoreCase("Pulmonic")) {
                     loadFragment(PulmonicFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
                 } else if (tabText.equalsIgnoreCase("Tricuspid")) {
                     loadFragment(TricuspidFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
                 } else if (tabText.equalsIgnoreCase("Mitral")) {
                     loadFragment(MitralFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
                 }
             }

             @Override public void onTabUnselected(TabLayout.Tab tab) {}
             @Override public void onTabReselected(TabLayout.Tab tab) {}
         });

         if (mBinding.tabLayout.getTabCount() > 0) {
             mBinding.tabLayout.getTabAt(0).select();
             String tabText = mBinding.tabLayout.getTabAt(0).getText().toString();
             if (tabText.equalsIgnoreCase("Aortic")) {
                 loadFragment(AorticFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
             } else if (tabText.equalsIgnoreCase("Pulmonic")) {
                 loadFragment(PulmonicFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
             } else if (tabText.equalsIgnoreCase("Tricuspid")) {
                 loadFragment(TricuspidFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
             } else if (tabText.equalsIgnoreCase("Mitral")) {
                 loadFragment(MitralFragment.newInstance(mIsEditMode, commonVisitData.getPatientName(), commonVisitData.getPatientUuid(), commonVisitData.getVisitUuid(), commonVisitData.getEncounterUuidVitals(), commonVisitData.getIntentTag(), commonVisitData.getPatientAgeYearMonth(), "heart"));
             }
         }
     }*/
    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_steps_summary, fragment)
                .commit();
    }
}
