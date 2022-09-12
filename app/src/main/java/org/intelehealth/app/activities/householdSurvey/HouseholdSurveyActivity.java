package org.intelehealth.app.activities.householdSurvey;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.householdSurvey.Fragments.FirstScreenFragment;
import org.intelehealth.app.activities.householdSurvey.model.Questions;
import org.intelehealth.app.activities.householdSurvey.model.SurveyData;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.databinding.ActivityHouseholdSurveyBinding;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.RTLUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HouseholdSurveyActivity extends AppCompatActivity implements View.OnClickListener {
    public static List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

    private List<Questions> mItemList = new ArrayList<Questions>();
    private SurveyData mSurveyData = null;
    private int mTotalStages = 1;
    private int mStageNumber = 1;
    private boolean mIsEditMode = false;
    private Context context;

    private ActivityHouseholdSurveyBinding mScreenBinding;
    private String mPatientUUid = "";
    private String mPatientAIDType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenBinding = ActivityHouseholdSurveyBinding.inflate(getLayoutInflater());
        View view = mScreenBinding.getRoot();
        setContentView(view);
        setTitle(getString(R.string.household_survey));
       /* getSupportFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new FirstScreenFragment())
                .commit();*/

       /* Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
        }*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPatientUUid = getIntent().getStringExtra("patientUuid");
        mPatientAIDType = "";// get aid typed from patient attributes;
        mIsEditMode = getIntent().getBooleanExtra("isEditMode", false);
        context = HouseholdSurveyActivity.this;
        if (mIsEditMode) {
            //getSupportActionBar().setTitle("Edit : History Collection");
            /*mSaveTextView.setText("Update");*/
        }
        mScreenBinding.rvQuery.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mSurveyData = new Gson().fromJson(FileUtils.encodeJSON(this, "survery_data.json").toString(), SurveyData.class);

        mTotalStages = mSurveyData.getSurveyQuestions().size();
        showPage();
        mScreenBinding.btnNext.setOnClickListener(this);
        mScreenBinding.btnPrev.setOnClickListener(this);
    }

    private void showPage() {
        showUI();
        mScreenBinding.tvSurveySectionName.setText(LocaleHelper.isArabic(this) ? mSurveyData.getSurveyQuestions().get(mStageNumber - 1).getTitleAr() : mSurveyData.getSurveyQuestions().get(mStageNumber - 1).getTitle());
        SurveyCommonAdapter surveyCommonAdapter = new SurveyCommonAdapter(this, mScreenBinding.rvQuery,
                mSurveyData.getSurveyQuestions().get(mStageNumber - 1).getQuestions(), new SurveyCommonAdapter.OnItemSelection() {
            @Override
            public void onSelect(Questions questions) {

            }
        });
        mScreenBinding.rvQuery.setAdapter(surveyCommonAdapter);
    }

    private void showUI() {
        mScreenBinding.headerLayout.removeAllViews();
        for (int i = 1; i <= mTotalStages; i++) {
            View v = View.inflate(this, R.layout.page_indicator_view, null);
            TextView tv = v.findViewById(R.id.tvCount);
            tv.setText(String.format(Locale.getDefault(), "%d", i));
            if (mStageNumber == i) {
                tv.setBackgroundResource(R.drawable.ic_baseline_lens_24);
                tv.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                tv.setBackgroundResource(R.drawable.ic_baseline_lens_24_1);
                tv.setTextColor(ContextCompat.getColor(context, R.color.gray_6));
            }
            mScreenBinding.headerLayout.addView(v);
        }

        mScreenBinding.btnPrev.setVisibility(View.VISIBLE);
        mScreenBinding.btnNext.setVisibility(View.VISIBLE);
        mScreenBinding.btnPrev.setText(getString(R.string.previous));
        mScreenBinding.btnNext.setText(getString(R.string.next));

        if (mStageNumber == 1) {
            mScreenBinding.btnPrev.setVisibility(View.INVISIBLE);
        } else if (mStageNumber == mTotalStages) {
            mScreenBinding.btnNext.setText(getString(R.string.submit));
        }
    }


    @Override
    public void onClick(View view) {
        Logger.logV("Survey", String.valueOf(mStageNumber));
        if (view.getId() == R.id.btnNext) {
            if (mStageNumber == mTotalStages) {
                saveSurvey();
            } else {
                mStageNumber++;
                showPage();
            }
        } else if (view.getId() == R.id.btnPrev) {
            mStageNumber--;
            showPage();
        }
    }

    private void saveSurvey() {
        PatientsDAO patientsDAO = new PatientsDAO();
        List<PatientAttributesDTO> attributesDTOList = new ArrayList<>();
        for (int i = 0; i < mSurveyData.getSurveyQuestions().size(); i++) {
            for (int j = 0; j < mSurveyData.getSurveyQuestions().get(i).getQuestions().size(); j++) {
                String question = mSurveyData.getSurveyQuestions().get(i).getQuestions().get(j).getQuestion().trim();
                String attributeTypeUuid = patientsDAO.getUuidForAttributeByDesc(question);
                if (!attributeTypeUuid.isEmpty() && mSurveyData.getSurveyQuestions().get(i).getQuestions().get(j).getAnswerValue() != null) {
                    PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(mPatientUUid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(attributeTypeUuid);
                    patientAttributesDTO.setValue(new Gson().toJson(mSurveyData.getSurveyQuestions().get(i).getQuestions().get(j).getAnswerValue()));
                    attributesDTOList.add(patientAttributesDTO);
                }
            }

        }
        try {
            patientsDAO.insertPatientAttributes(attributesDTOList, null);
            Toast.makeText(context, "Household Survey Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (DAOException e) {
            e.printStackTrace();
        }


    }
}