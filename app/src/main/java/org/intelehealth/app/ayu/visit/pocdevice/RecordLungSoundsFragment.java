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
import org.intelehealth.app.ayu.visit.pocdevice.Lung.AnteriorFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lung.LateralFragment;
import org.intelehealth.app.ayu.visit.pocdevice.Lung.PosteriorFragment;
import org.intelehealth.app.databinding.FragmentRecordLungSoundsBinding;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Set;

public class RecordLungSoundsFragment extends Fragment {

    private FragmentRecordLungSoundsBinding mBinding;
    private VisitCreationActionListener mActionListener;
    private OnRecordingCompleteListener recordingListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;

    private String visitUuid;
    private CommonVisitData commonVisitData;

    public interface OnRecordingCompleteListener {
        void onRecordingCompleted(String type);
    }

    public static RecordLungSoundsFragment newInstance(CommonVisitData mCommonVisitData, boolean isEditMode, VitalsObject vitalsObject, String visitUuid) {
        RecordLungSoundsFragment fragment = new RecordLungSoundsFragment();
        fragment.mVitalsObject = vitalsObject;
        fragment.commonVisitData = mCommonVisitData;
        fragment.mIsEditMode = isEditMode;
        fragment.visitUuid = visitUuid;
        return fragment;
    }

    public RecordLungSoundsFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        if (context instanceof OnRecordingCompleteListener) {
            recordingListener = (OnRecordingCompleteListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnRecordingCompleteListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_lung_sounds, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.ivBackArrowTerms.setOnClickListener(v -> requireActivity().onBackPressed());

        mBinding.btnFinish.setOnClickListener(v -> {
            new org.intelehealth.app.syncModule.SyncUtils().syncBackground();
            if (recordingListener != null) {
                recordingListener.onRecordingCompleted("lung");
            }
        });

        setTabList();
    }

    private void setTabList() {
        SessionManager sessionManager = new SessionManager(requireContext());
        Set<String> selectedExams = sessionManager.getVisitSummary(commonVisitData.getPatientUuid());

        boolean hasAnterior = false;
        boolean hasLateral = false;
        boolean hasPosterior = false;

        if (selectedExams != null) {
            for (String exam : selectedExams) {
                if (exam.contains("Sound Lung: Anterior")) hasAnterior = true;
                if (exam.contains("Sound Lung: Lateral")) hasLateral = true;
                if (exam.contains("Sound Lung: Posterior")) hasPosterior = true;
            }
        }

        if (hasAnterior) {
            mBinding.tabLayoutLung.addTab(mBinding.tabLayoutLung.newTab().setText("Anterior"));
        }
        if (hasLateral) {
            mBinding.tabLayoutLung.addTab(mBinding.tabLayoutLung.newTab().setText("lateral"));
        }
        if (hasPosterior) {
            mBinding.tabLayoutLung.addTab(mBinding.tabLayoutLung.newTab().setText("Posterior"));
        }

        if (mBinding.tabLayoutLung.getTabCount() == 0) {
            // Default if none found, maybe show all?
            mBinding.tabLayoutLung.addTab(mBinding.tabLayoutLung.newTab().setText("Anterior"));
            mBinding.tabLayoutLung.addTab(mBinding.tabLayoutLung.newTab().setText("lateral"));
            mBinding.tabLayoutLung.addTab(mBinding.tabLayoutLung.newTab().setText("Posterior"));
        }

        mBinding.tabLayoutLung.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = tab.getText().toString();
                if (tabText.equalsIgnoreCase("Anterior")) {
                    setAnteriorTab();
                } else if (tabText.equalsIgnoreCase("lateral")) {
                    setLateralTab();
                } else if (tabText.equalsIgnoreCase("Posterior")) {
                    setPosteriorTab();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (mBinding.tabLayoutLung.getTabCount() > 0) {
            mBinding.tabLayoutLung.getTabAt(0).select();
            String tabText = mBinding.tabLayoutLung.getTabAt(0).getText().toString();
            if (tabText.equalsIgnoreCase("Anterior")) {
                setAnteriorTab();
            } else if (tabText.equalsIgnoreCase("lateral")) {
                setLateralTab();
            } else if (tabText.equalsIgnoreCase("Posterior")) {
                setPosteriorTab();
            }
        }
    }

    private void setAnteriorTab() {
        loadFragment(AnteriorFragment.newInstance(
                mIsEditMode,
                commonVisitData.getPatientName(),
                commonVisitData.getPatientUuid(),
                commonVisitData.getVisitUuid(),
                commonVisitData.getEncounterUuidVitals(),
                commonVisitData.getIntentTag(),
                commonVisitData.getPatientAgeYearMonth(),
                "lung"));
    }

    private void setLateralTab() {
        loadFragment(LateralFragment.newInstance(
                mIsEditMode,
                commonVisitData.getPatientName(),
                commonVisitData.getPatientUuid(),
                commonVisitData.getVisitUuid(),
                commonVisitData.getEncounterUuidVitals(),
                commonVisitData.getIntentTag(),
                commonVisitData.getPatientAgeYearMonth(),
                "lung"));
    }

    private void setPosteriorTab() {
        loadFragment(PosteriorFragment.newInstance(
                mIsEditMode,
                commonVisitData.getPatientName(),
                commonVisitData.getPatientUuid(),
                commonVisitData.getVisitUuid(),
                commonVisitData.getEncounterUuidVitals(),
                commonVisitData.getIntentTag(),
                commonVisitData.getPatientAgeYearMonth(),
                "lung"));
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_record_lung, fragment)
                .commit();
    }
}