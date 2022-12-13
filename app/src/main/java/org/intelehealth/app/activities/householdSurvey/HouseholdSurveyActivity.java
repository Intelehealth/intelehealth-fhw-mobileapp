package org.intelehealth.app.activities.householdSurvey;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.householdSurvey.model.AidTypeAnswerValue;
import org.intelehealth.app.activities.householdSurvey.model.AnswerValue;
import org.intelehealth.app.activities.householdSurvey.model.Questions;
import org.intelehealth.app.activities.householdSurvey.model.Survey;
import org.intelehealth.app.activities.householdSurvey.model.SurveyData;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.ActivityHouseholdSurveyBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<String> mPatientAidTypes = new ArrayList<>();
    private boolean mIsTriageMode = false;
    private String aidType;
    private SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenBinding = ActivityHouseholdSurveyBinding.inflate(getLayoutInflater());
        View view = mScreenBinding.getRoot();
        setContentView(view);
        sessionManager = new SessionManager(this);
//        setTitle(getString(R.string.livelihood_needs_assessment));

       /* getSupportFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new FirstScreenFragment())
                .commit();*/

       /* Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
        }*/

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPatientUUid = getIntent().getStringExtra("patientUuid");
        aidType = getIntent().getStringExtra("aidType");
        mIsTriageMode = getIntent().getBooleanExtra("IsTriageMode", false);
        Log.v("aidTYpe", "aidTypeIntent" + aidType);

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) { // As requested by Programs Team.
            if (aidType.equalsIgnoreCase("Community Health Need Assessment"))
                setTitle("تقييم الحاجات الصحية المجتمعية");
            else if (aidType.equalsIgnoreCase("General Aid"))
                setTitle("المساعدة العامة");
            else if (aidType.equalsIgnoreCase("Student Aid"))
                setTitle("مساعدة الطلاب");
        }

        String attributeTypeUuidForAidType = new PatientsDAO().getUuidForAttribute("patient aid type");// get aid typed from patient attributes;
      /*  try {
            String value = new PatientsDAO().getPatientAttributeValueByTypeUUID(mPatientUUid, attributeTypeUuidForAidType);
            AidTypeAnswerValue answerValue = new Gson().fromJson(value, AidTypeAnswerValue.class);
            if (answerValue == null || answerValue.getEnValues().size() == 0 || answerValue.getArValues().size() == 0) {
                Toast.makeText(context, getString(R.string.aid_types_missing), Toast.LENGTH_SHORT).show();
                finish();
            }
            Log.v("answerValue", answerValue.getEnValues().get(0));

            mPatientAidTypes = answerValue.getEnValues();
            Log.v("answerValue", "aidArray: " + mPatientAidTypes + "");
        } catch (DAOException e) {
            e.printStackTrace();
        }*/

        mPatientAidTypes.add(aidType);
        Log.v("answerValue", "aidArray: " + mPatientAidTypes + "");

        mIsEditMode = getIntent().getBooleanExtra("isEditMode", false);
        context = HouseholdSurveyActivity.this;
        if (mIsEditMode) {
            // TODO: pre-fill Values from db in this case.
        }

        mScreenBinding.rvQuery.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        if (mIsTriageMode) {
            mSurveyData = new Gson().fromJson(FileUtils.encodeJSON(this, "triage_survery_data.json").toString(), SurveyData.class);

            mScreenBinding.headerLayout.setVisibility(View.GONE);
            mScreenBinding.tvSurveySectionName.setVisibility(View.GONE);
            setTitle(getString(R.string.triage_survey));
        } else {
            SurveyData surveyData = new Gson().fromJson(FileUtils.encodeJSON(this, "survery_data.json").toString(), SurveyData.class);
            mScreenBinding.headerLayout.setVisibility(View.VISIBLE);

            mSurveyData = new SurveyData();
            List<Survey> surveyQuestions = new ArrayList<>();
            mSurveyData.setSurveyQuestions(surveyQuestions);
            for (int i = 0; i < surveyData.getSurveyQuestions().size(); i++) {
                Survey survey = surveyData.getSurveyQuestions().get(i);
                Log.v("answerValue", survey.getTitle());
                List<Questions> questions = new ArrayList<Questions>();
                for (int j = 0; j < survey.getQuestions().size(); j++) {
                    for (int k = 0; k < survey.getQuestions().get(j).getAids().size(); k++) {
                        if (mPatientAidTypes.contains(survey.getQuestions().get(j).getAids().get(k))) {
                            questions.add(survey.getQuestions().get(j));
                            Log.v("answerValue", survey.getQuestions().get(j).getQuestion());
                            break;
                        }
                    }
                }
                survey.setQuestions(questions);
                if (!questions.isEmpty())
                    mSurveyData.getSurveyQuestions().add(survey);
            }
        }
        preFillForEdit();
        mTotalStages = mSurveyData.getSurveyQuestions().size();
        showPage();
        mScreenBinding.btnNext.setOnClickListener(this);
        mScreenBinding.btnPrev.setOnClickListener(this);
    }

    private void preFillForEdit() {
        for (int i = 0; i < mSurveyData.getSurveyQuestions().size(); i++) {
            Survey survey = mSurveyData.getSurveyQuestions().get(i);
            for (int j = 0; j < survey.getQuestions().size(); j++) {
                try {
                    String attributeTypeUuidForAidType = new PatientsDAO().getUuidForAttributeByDesc(survey.getQuestions().get(j).getQuestion().trim());
                    String value = new PatientsDAO().getPatientAttributeValueByTypeUUID(mPatientUUid, attributeTypeUuidForAidType);
                    AnswerValue answerValue = new Gson().fromJson(value, AnswerValue.class);
                    if (answerValue != null) {
                        mSurveyData.getSurveyQuestions().get(i).getQuestions().get(j).setAnswerValue(answerValue);
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            answerChecking(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        answerChecking(true);
    }

    private void answerChecking(boolean isDraftMode) {
        List<PatientAttributesDTO> attributesDTOList = filterAnsweredQuestions(isDraftMode);
        if (attributesDTOList.isEmpty()) {
            if (!isDraftMode) {
                Toast.makeText(context, getString(R.string.empty_survey), Toast.LENGTH_SHORT).show();
                return;
            } else {
                finish();
            }
        }

        if (isDraftMode) {
            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);

            // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            alertdialogBuilder.setMessage(R.string.alert_for_draft);
            alertdialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    saveSurvey(attributesDTOList, isDraftMode);
                }
            });
            alertdialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(getResources().getColor(org.intelehealth.apprtc.R.color.colorPrimary));
            //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            negativeButton.setTextColor(getResources().getColor(org.intelehealth.apprtc.R.color.colorPrimary));
            //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            //IntelehealthApplication.setAlertDialogCustomTheme(this, aler
        } else {
            saveSurvey(attributesDTOList, isDraftMode);
        }
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
        }
        if (mStageNumber == mTotalStages) {
            mScreenBinding.btnNext.setText(getString(R.string.submit));
        }

    }


    @Override
    public void onClick(View view) {
        Logger.logV("Survey", String.valueOf(mStageNumber));
        if (view.getId() == R.id.btnNext) {
            if (mStageNumber == mTotalStages) {
                answerChecking(false);
            } else {
                mStageNumber++;
                showPage();
            }
        } else if (view.getId() == R.id.btnPrev) {
            mStageNumber--;
            showPage();
        }
    }

    private List<PatientAttributesDTO> filterAnsweredQuestions(boolean isDraftMode) {
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

                    patientAttributesDTO.setDraftStatus(String.valueOf(isDraftMode));
                    attributesDTOList.add(patientAttributesDTO);
                }
            }

        }
        return attributesDTOList;
    }

    private void saveSurvey(List<PatientAttributesDTO> attributesDTOList, boolean isDraftMode) {

        try {
            Log.v("attributesDTOList", new Gson().toJson(attributesDTOList));
            PatientsDAO patientsDAO = new PatientsDAO();
            patientsDAO.SurveyupdatePatientToDB(mPatientUUid, attributesDTOList);
            if (!isDraftMode && NetworkConnection.isOnline(this)) {
                SyncDAO syncDAO = new SyncDAO();
                syncDAO.pushDataApi();
                if (mIsTriageMode)
                    Toast.makeText(context, getString(R.string.triage_survey_saved), Toast.LENGTH_SHORT).show();
                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) { // As requested by Programs Team.
                        if (aidType.equalsIgnoreCase("Community Health Need Assessment"))
                            Toast.makeText(context, "تم إرسال تقييم الحاجة إلى صحة المجتمع!", Toast.LENGTH_SHORT).show();
                        else if (aidType.equalsIgnoreCase("General Aid"))
                            Toast.makeText(context, "تقديم المساعدة العامة!", Toast.LENGTH_SHORT).show();
                        else if (aidType.equalsIgnoreCase("Student Aid"))
                            Toast.makeText(context, "تم إرسال المساعدة الطلابية!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, aidType + " submitted!", Toast.LENGTH_SHORT).show();
                    }
                }
//                    Toast.makeText(context, getString(R.string.household_survey_saved), Toast.LENGTH_SHORT).show();
            }
            if (isDraftMode) {
                Toast.makeText(context, getString(R.string.household_survey_draft), Toast.LENGTH_SHORT).show();
            }


            finish();
        } catch (DAOException e) {
            e.printStackTrace();
        }


    }
}