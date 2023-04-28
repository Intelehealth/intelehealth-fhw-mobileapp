package org.intelehealth.app.ayu.visit;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.familyhist.FamilyHistoryFragment;
import org.intelehealth.app.ayu.visit.pastmedicalhist.MedicalHistorySummaryFragment;
import org.intelehealth.app.ayu.visit.pastmedicalhist.PastMedicalHistoryFragment;
import org.intelehealth.app.ayu.visit.physicalexam.PhysicalExamSummaryFragment;
import org.intelehealth.app.ayu.visit.physicalexam.PhysicalExaminationFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonCaptureFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonQuestionsFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonSummaryFragment;
import org.intelehealth.app.ayu.visit.vital.VitalCollectionFragment;
import org.intelehealth.app.ayu.visit.vital.VitalCollectionSummaryFragment;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class VisitCreationActivity extends AppCompatActivity implements VisitCreationActionListener {

    private static final String TAG = VisitCreationActivity.class.getSimpleName();
    private static final String VITAL_FRAGMENT = "VITAL";
    private static final String VITAL_SUMMARY_FRAGMENT = "VITAL_SUMMARY";
    private static final String VISIT_REASON_FRAGMENT = "VISIT_REASON";
    private static final String VISIT_REASON_QUESTION_FRAGMENT = "VISIT_REASON_QUESTION";
    private static final String VISIT_REASON_SUMMARY_FRAGMENT = "VISIT_REASON_SUMMARY";
    private static final String PHYSICAL_EXAM_FRAGMENT = "PHYSICAL_EXAM";
    private static final String PHYSICAL_EXAM_SUMMARY_FRAGMENT = "PHYSICAL_EXAM_SUMMARY";
    private static final String PAST_MEDICAL_HISTORY_FRAGMENT = "PAST_MEDICAL_HISTORY";
    private static final String PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT = "PAST_MEDICAL_HISTORY_SUMMARY";
    private static final String FAMILY_HISTORY_SUMMARY_FRAGMENT = "FAMILY_HISTORY_SUMMARY";
    public static final int STEP_1_VITAL = 1;
    public static final int STEP_1_VITAL_SUMMARY = 1001;
    public static final int STEP_2_VISIT_REASON = 2;
    public static final int STEP_2_VISIT_REASON_QUESTION = 3;
    public static final int STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT = 33;
    public static final int STEP_2_VISIT_REASON_QUESTION_ASSOCIATE_SYMPTOMS = 4;
    public static final int STEP_2_VISIT_REASON_QUESTION_SUMMARY = 44;
    public static final int STEP_3_PHYSICAL_EXAMINATION = 5;
    public static final int STEP_3_PHYSICAL_SUMMARY_EXAMINATION = 55;
    public static final int STEP_4_PAST_MEDICAL_HISTORY = 6;
    public static final int STEP_5_FAMILY_HISTORY = 7;
    public static final int STEP_5_HISTORY_SUMMARY = 8;
    public static final int STEP_6_VISIT_SUMMARY = 9;

    private int mCurrentStep = STEP_1_VITAL;

    SessionManager sessionManager;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";

    private FrameLayout mSummaryFrameLayout;
    private ProgressBar mStep1ProgressBar, mStep2ProgressBar, mStep3ProgressBar, mStep4ProgressBar;

    // Chief complain
    //private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<Node> mChiefComplainRootNodeList = new ArrayList<>();
    private List<Node> mAssociateSymptomsNodeList = new ArrayList<>();
    private int mCurrentComplainNodeIndex = 0;
    private int mCurrentComplainNodeOptionsIndex = 0;
    private List<String> selectedComplains = new ArrayList<>();

    // Physical Examination

    // Past Medical History

    // Family History

    private boolean mIsEditMode = false;
    private int mEditFor = 0; // STEP_1_VITAL , STEP_2_VISIT_REASON, STEP_3_PHYSICAL_EXAMINATION, STEP_4_PAST_MEDICAL_HISTORY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_creation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }

        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        mSummaryFrameLayout = findViewById(R.id.fl_steps_summary);
        mStep1ProgressBar = findViewById(R.id.prog_bar_step1);
        mStep2ProgressBar = findViewById(R.id.prog_bar_step2);
        mStep3ProgressBar = findViewById(R.id.prog_bar_step3);
        mStep4ProgressBar = findViewById(R.id.prog_bar_step4);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            mEditFor = intent.getIntExtra("edit_for", STEP_1_VITAL);
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            Log.v(TAG, "Patient ID: " + patientUuid);
            Log.v(TAG, "Visit ID: " + visitUuid);
            Log.v(TAG, "Patient Name: " + patientName);
            Log.v(TAG, "Intent Tag: " + intentTag);
            Log.v(TAG, "Intent float_ageYear_Month: " + float_ageYear_Month);
            ((TextView) findViewById(R.id.tv_title)).setText(patientName);
            ((TextView) findViewById(R.id.tv_title_desc)).setText(String.format("%s/%s Y", patientGender, String.valueOf((int) float_ageYear_Month)));
            if (intentTag.equalsIgnoreCase("edit")) {
                mIsEditMode = true;
            }
        }

        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();

        }

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterAdultIntials);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL"));
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        Bundle bundle = new Bundle();
        bundle.putString("patientUuid", patientUuid);
        bundle.putString("visitUuid", visitUuid);
        bundle.putString("encounterUuidVitals", encounterVitals);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(getIntent(), null), VITAL_FRAGMENT).
                commit();
    }

    public void backPress(View view) {
        finish();
    }

    private VitalsObject mVitalsObject;

    @Override
    public void onFormSubmitted(int nextAction, Object object) {
        mCurrentStep = nextAction;
        switch (nextAction) {
            case STEP_1_VITAL_SUMMARY:
                if (object != null)
                    mVitalsObject = (VitalsObject) object;
                if (mVitalsObject != null) {
                    //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    mStep1ProgressBar.setProgress(100);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, VitalCollectionSummaryFragment.newInstance(mVitalsObject), VITAL_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_1_VITAL:
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                mSummaryFrameLayout.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(getIntent(), mVitalsObject), VITAL_FRAGMENT).
                        commit();
                break;
            case STEP_2_VISIT_REASON:
                mStep2ProgressBar.setProgress(20);
                ((TextView) findViewById(R.id.tv_sub_title)).setText(getResources().getString(R.string.visit_reason));
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonCaptureFragment.newInstance(getIntent()), VISIT_REASON_FRAGMENT).
                        commit();
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;

            case STEP_2_VISIT_REASON_QUESTION:
                selectedComplains = (List<String>) object;
                loadChiefComplainNodeForSelectedNames(selectedComplains);
                mStep2ProgressBar.setProgress(40);
                setTitle(getResources().getString(R.string.visit_reason) + " : " + selectedComplains.get(0));
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                //mSummaryFrameLayout.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(getIntent(), mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).
                        commit();
                break;
            case STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT:
                mSummaryFrameLayout.setVisibility(View.GONE);
                if (object != null) {
                    int caseNo = (int) object;
                    if (caseNo == STEP_4_PAST_MEDICAL_HISTORY) {
                        showPastMedicalHistoryFragment();
                    }
                }
                break;
            case STEP_2_VISIT_REASON_QUESTION_SUMMARY:
                if (isSavedVisitReason()) {
                    mStep2ProgressBar.setProgress(100);

                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, VisitReasonSummaryFragment.newInstance(getIntent(), insertion), VISIT_REASON_QUESTION_FRAGMENT).
                            commit();
                }
                break;

            case STEP_3_PHYSICAL_EXAMINATION:
                mStep3ProgressBar.setProgress(10);
                setTitle(getResources().getString(R.string._phy_examination));
                mSummaryFrameLayout.setVisibility(View.GONE);
                //mPhysicalExamNode =
                loadPhysicalExam();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(getIntent(), physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                        commit();
                break;
            case STEP_3_PHYSICAL_SUMMARY_EXAMINATION:
                if (isSavedPhysicalExam()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, PhysicalExamSummaryFragment.newInstance(getIntent(), physicalString), PHYSICAL_EXAM_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_4_PAST_MEDICAL_HISTORY:
                showPastMedicalHistoryFragment();
                break;

            case STEP_5_FAMILY_HISTORY:
                mStep4ProgressBar.setProgress(50);
                setTitle(getResources().getString(R.string._medical_family_history));
                mSummaryFrameLayout.setVisibility(View.GONE);
                boolean isEditMode = true;
                if (mFamilyHistoryNode == null) {
                    mFamilyHistoryNode = loadFamilyHistory();
                    isEditMode = false;
                }
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, FamilyHistoryFragment.newInstance(getIntent(), mFamilyHistoryNode, isEditMode), FAMILY_HISTORY_SUMMARY_FRAGMENT).
                        commit();
                break;

            case STEP_5_HISTORY_SUMMARY:
                if (isSavedPastHistory()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, MedicalHistorySummaryFragment.newInstance(getIntent(), patientHistory, familyHistory), PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_6_VISIT_SUMMARY:
                Intent intent1 = new Intent(VisitCreationActivity.this, VisitSummaryActivity_New.class); // earlier visitsummary
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent1.putExtra("state", state);
                intent1.putExtra("name", patientName);
                intent1.putExtra("gender", patientGender);
                intent1.putExtra("tag", intentTag);
                intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent1.putExtra("hasPrescription", "false");
                // intent1.putStringArrayListExtra("exams", selectedExamsList);
                startActivity(intent1);
                finish();
                break;
        }
    }

    private void showPastMedicalHistoryFragment() {
        mStep4ProgressBar.setProgress(10);
        setTitle(getResources().getString(R.string._medical_family_history));
        mSummaryFrameLayout.setVisibility(View.GONE);
        boolean isEditMode = true;
        if (mPastMedicalHistoryNode == null) {
            mPastMedicalHistoryNode = loadPastMedicalHistory();
            isEditMode = false;
        }
        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, PastMedicalHistoryFragment.newInstance(getIntent(), mPastMedicalHistoryNode, isEditMode), PAST_MEDICAL_HISTORY_FRAGMENT).
                commit();
    }

    private boolean isSavedPastHistory() {
        return savePastHistoryData();
    }

    private boolean isSavedPhysicalExam() {
        return savePhysicalExamData();
    }

    private boolean isSavedVisitReason() {

        insertion = "";
        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            Node node = mChiefComplainRootNodeList.get(i);
            String val = formatComplainRecord(node, i == mChiefComplainRootNodeList.size() - 1);
            if (val == null) {
                return false;
            }
        }
        if (insertion.contains("<br/> ►<b>Associated symptoms</b>: <br/>►<b> Associated symptoms</b>:  <br/>")) {
            insertion = insertion.replace("<br/> ►<b>Associated symptoms</b>: <br/>►<b> Associated symptoms</b>:  <br/>", "<br/>►<b> Associated symptoms</b>:  <br/>");
        }
        return insertChiefComplainToDb(insertion);
    }

    private Node mPhysicalExamNode;
    private String mLastChiefComplainPhysicalString = "";

    private List<Node> loadPhysicalExam() {
        mLastChiefComplainPhysicalString = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getPhysicalExams();
        String[] exm = mLastChiefComplainPhysicalString.split(";");
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        for (String s : exm) {
            if (s.contains(":") && s.split(":").length >= 2) {
                String rootNodeName = s.split(":")[0];
                String childNodeName = s.split(":")[1];

                List<String> list = new ArrayList<>();
                if (map.containsKey(rootNodeName)) {
                    list = map.get(rootNodeName);
                }
                list.add(childNodeName);
                map.put(rootNodeName, list);
            }
        }
        String fileLocation = "physExam.json";
        Node filterNode = loadFileToNode(fileLocation);
        physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, fileLocation), null);
        List<Node> optionsList = new ArrayList<>();
        for (int i = 0; i < filterNode.getOptionsList().size(); i++) {
            /*if (i == 0) {
                optionsList.add(filterNode.getOptionsList().get(i).getOptionsList().get(0).getOptionsList().get(0));
            }*/
            if (map.containsKey(filterNode.getOptionsList().get(i).getText())) {
                for (int j = 0; j < filterNode.getOptionsList().get(i).getOptionsList().size(); j++) {
                    optionsList.add(filterNode.getOptionsList().get(i).getOptionsList().get(j).getOptionsList().get(0));
                }
            }
        }
        filterNode.setOptionsList(optionsList);
        return physicalExamMap.getSelectedNodes();
    }

    private Node mPastMedicalHistoryNode;

    private Node loadPastMedicalHistory() {
        String fileLocation = "patHist.json";
        return loadFileToNode(fileLocation);
    }

    private Node mFamilyHistoryNode;

    private Node loadFamilyHistory() {
        String fileLocation = "famHist.json";
        return loadFileToNode(fileLocation);
    }

    private Node loadFileToNode(String fileLocation) {
        JSONObject currentFile = FileUtils.encodeJSON(this, fileLocation);
        Node mainNode = new Node(currentFile);
        mainNode.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
        return mainNode;
    }

    private Node mCommonAssociateSymptoms = null;

    private void loadChiefComplainNodeForSelectedNames(List<String> selectedComplains) {
        for (int i = 0; i < selectedComplains.size(); i++) {
            String fileLocation = "engines/" + selectedComplains.get(i) + ".json";
            JSONObject currentFile = FileUtils.encodeJSON(this, fileLocation);
            Node mainNode = new Node(currentFile);
            List<Node> optionList = new ArrayList<>();
            Node associateSymptoms = null;
            Log.v(TAG, "optionList  mainNode- " + mainNode.getText());
            for (int j = 0; j < mainNode.getOptionsList().size(); j++) {
                if (mainNode.getOptionsList().get(j).getText().equalsIgnoreCase("Associated symptoms")) {
                    if (mCommonAssociateSymptoms == null)
                        mCommonAssociateSymptoms = mainNode.getOptionsList().get(j);
                    else {
                        mCommonAssociateSymptoms.getOptionsList().addAll(mainNode.getOptionsList().get(j).getOptionsList());
                    }

                } else {
                    if (VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, mainNode.getOptionsList().get(j).getGender(), mainNode.getOptionsList().get(j).getMin_age(), mainNode.getOptionsList().get(j).getMax_age())) {
                        mainNode.getOptionsList().get(j).getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
                        optionList.add(mainNode.getOptionsList().get(j));
                    }
                }
            }
            /*if (mCommonAssociateSymptoms != null) {

                mCommonAssociateSymptoms.getOptionsList().removeIf(node -> !checkNodeValidByGenderAndAge(node.getGender(), node.getMin_age(), node.getMax_age()));

                //optionList.add(associateSymptoms);
            }*/
            mainNode.setOptionsList(optionList);
            mChiefComplainRootNodeList.add(mainNode);

        }
        if (mCommonAssociateSymptoms != null) {

            mCommonAssociateSymptoms.setOptionsList(getNodeWithoutDuplicates(mCommonAssociateSymptoms.getOptionsList()));
            mCommonAssociateSymptoms.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));

            //optionList.add(associateSymptoms);
            mChiefComplainRootNodeList.add(mCommonAssociateSymptoms);
        }


    }

    private static List<Node> getNodeWithoutDuplicates(final List<Node> nodes) {
        Set<Node> nodeSet = new TreeSet<Node>(new NodeComparator());
        nodeSet.addAll(nodes);
        return new ArrayList<Node>(nodeSet);
    }

    static class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node n1, Node n2) {
            return n1.getText().compareToIgnoreCase(n2.getText());

        }

    }


    public void setTitle(String text) {
        ((TextView) findViewById(R.id.tv_sub_title)).setText(text);
    }

    @Override
    public void onProgress(int progress) {
        switch (mCurrentStep) {
            case STEP_2_VISIT_REASON_QUESTION:
                mStep2ProgressBar.setProgress(mStep2ProgressBar.getProgress() + progress);
                break;
            case STEP_3_PHYSICAL_EXAMINATION:
                mStep3ProgressBar.setProgress(mStep2ProgressBar.getProgress() + progress);
                break;
        }
    }

    @Override
    public void onTitleChange(String title) {
        switch (mCurrentStep) {
            case STEP_2_VISIT_REASON_QUESTION:
                if (title == null || title.isEmpty()) {
                    setTitle(getResources().getString(R.string.visit_reason) + " : " + selectedComplains.get(0));
                } else {
                    setTitle(title);
                }
                break;
            case STEP_3_PHYSICAL_EXAMINATION:
                setTitle(title);
                break;
        }

    }

    @Override
    public void onManualClose() {
        switch (mCurrentStep) {
            case STEP_1_VITAL_SUMMARY:
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onCameraOpenRequest() {
        openCamera();
    }

    @Override
    public void onImageRemoved(int index, String image) {
        deleteImageFromDatabase(index, image);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    boolean nodeComplete = false;

    public void filterNodeQuestions() {

    }

    String insertion = "";

    //new code for the one by one complain data capture
    public String formatComplainRecord(Node currentNode, boolean isAssociateSymptom) {
        // checking any question missing
        // can check also compulsory question

        AnswerResult answerResult = isAssociateSymptom ? currentNode.checkAllRequiredAnsweredRootNode(this) : currentNode.checkAllRequiredAnswered(this);
        if (!answerResult.result) {
            // show alert dialog
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), answerResult.requiredStrings, true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
                @Override
                public void onDialogActionDone(int action) {

                }
            });
            /*MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(answerResult.requiredStrings);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            Dialog alertDialog = alertDialogBuilder.show();*/
            Log.v(TAG, answerResult.requiredStrings);
            return null;
        }


        // upload images if any

        // generate language from current node

        String complaintString = isAssociateSymptom ? currentNode.generateLanguageSingleNode() : currentNode.generateLanguage();

        Log.v("formatComplainRecord", "Value - " + complaintString);
        if (complaintString != null && !complaintString.isEmpty()) {
            //     String complaintFormatted = complaintString.replace("?,", "?:");

            String complaint = currentNode.getText();
            //    complaintDetails.put(complaint, complaintFormatted);

//                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
            insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
        } else {
            String complaint = currentNode.getText();
            if (!complaint.equalsIgnoreCase(getResources().getString(R.string.associated_symptoms))) {
//                    insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
            }
        }
        Log.v("formatComplainRecord", "Value - " + insertion);
        return insertion;

    }

    /**
     *
     */
    private void showNextComplainQueries() {
        mCurrentComplainNodeIndex++;
        mStep2ProgressBar.setProgress(mStep2ProgressBar.getProgress() + 10);
        setTitle(getResources().getString(R.string.visit_reason) + " : " + selectedComplains.get(mCurrentComplainNodeIndex));
        //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
        //mSummaryFrameLayout.setVisibility(View.GONE);
       /* getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(getIntent(), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex)), VISIT_REASON_QUESTION_FRAGMENT).
                commit();*/
    }

    /**
     * Insert into DB could be made into a Helper Method, but isn't because there are specific concept IDs used each time.
     * Although this could also be made into a function, for now it has now been.
     *
     * @param value String to put into DB
     * @return DB Row number, never used
     */
    private boolean insertChiefComplainToDb(String value) {
        boolean isInserted = false;
        try {
            Log.i(TAG, "insertChiefComplainToDb: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
            Log.i(TAG, "insertChiefComplainToDb: " + value);
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT);
            Log.i(TAG, "insertChiefComplainToDb: uuidOBS - " + uuidOBS);
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue1(value));
            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        return isInserted;
    }


    private void updateDatabase(String string) {
        Log.i(TAG, "updateDatabase: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
//        }
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT));

            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getPhysicalExams();
        if (rawExams != null) {
            String[] splitExams = rawExams.split(";");
            examList.addAll(Arrays.asList(splitExams));
            return examList;
        }
        return null;
    }

    public static void openCamera(Activity activity, String imagePath, String imageName) {
        Log.d(TAG, "open Camera!");
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
            }
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        activity.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                // currentNode.setImagePath(mCurrentPhotoPath);
                // currentNode.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
    }

    /*Physical exam*/
    private boolean insertDbPhysicalExam(String value) {
        Log.i(TAG, "insertDb: ");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PHYSICAL_EXAMINATION);
            Log.i(TAG, "insertDbPhysicalExam: uuidOBS - " + uuidOBS);

            obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue(value));

            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    String physicalString;
    Boolean complaintConfirmed = false;
    PhysicalExam physicalExamMap;

    private boolean savePhysicalExamData() {
        Log.v(TAG, "savePhysicalExamData");
        complaintConfirmed = physicalExamMap.areRequiredAnswered();

        if (complaintConfirmed) {

            physicalString = physicalExamMap.generateFindings();
            while (physicalString.contains("[Describe"))
                physicalString = physicalString.replace("[Describe]", "");

            List<String> imagePathList = physicalExamMap.getImagePathList();
            Log.v(TAG, "savePhysicalExamData, imagePathList " + imagePathList);
            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase(imagePath);
                }
            }


        } else {
            questionsMissing();
        }
        return insertDbPhysicalExam(physicalString);
    }

    private String patientHistory, familyHistory;

    /**
     * @return
     */
    private boolean savePastHistoryData() {

        patientHistory = mPastMedicalHistoryNode.generateLanguage();
        while (patientHistory.contains("[Describe"))
            patientHistory = patientHistory.replace("[Describe]", "");

        //familyHistory = mFamilyHistoryNode.generateLanguage();
        ArrayList<String> familyInsertionList = new ArrayList<>();
        if (mFamilyHistoryNode.anySubSelected()) {
            for (Node node : mFamilyHistoryNode.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = node.generateLanguage();
                    String toInsert = node.getText() + " : " + familyString;
                    toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (org.apache.commons.lang3.StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + ".<br/>";
                    familyInsertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < familyInsertionList.size(); i++) {
            if (i == 0) {
                familyHistory = Node.bullet + familyInsertionList.get(i);
            } else {
                familyHistory = familyHistory + " " + Node.bullet + familyInsertionList.get(i);
            }
        }

        familyHistory = familyHistory.replaceAll("null.", "");
        while (familyHistory.contains("[Describe"))
            familyHistory = familyHistory.replace("[Describe]", "");
        List<String> imagePathList = mFamilyHistoryNode.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }

        return insertDbPastHistory(patientHistory, familyHistory);
    }

    /*Physical exam*/
    private boolean insertDbPastHistory(String patientHistory, String familyHistory) {
        Log.i(TAG, "insertDb: ");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();

            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            Log.i(TAG, "insertDbPastHistory patientHistory : uuidOBS - " + uuidOBS);

            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue(patientHistory));


            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }

            String uuidOBS1 = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            Log.i(TAG, "insertDbPastHistory familyHistory : uuidOBS - " + uuidOBS1);
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(familyHistory));

            if (uuidOBS1 != null) {
                obsDTO.setUuid(uuidOBS1);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    public void questionsMissing() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), getResources().getString(R.string.question_answer_all_phy_exam), true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });

        /*MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(getResources().getString(R.string.question_answer_all_phy_exam));
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);*/
    }

    private void updateImageDatabase(String imageName) {
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    private void deleteImageFromDatabase(int index, String imageName) {
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            String obsUUID = imageName.substring(imageName.lastIndexOf("/") + 1).split("\\.")[0];
            imagesDAO.deleteImageFromDatabase(obsUUID);
            imageUtilsListener.onImageReadyForDelete(index, imageName);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    ActivityResultLauncher<Intent> mStartForCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the Intent
                        String mCurrentPhotoPath = data.getStringExtra("RESULT");

                        Bundle bundle = new Bundle();
                        bundle.putString("image", mCurrentPhotoPath);
                        imageUtilsListener.onImageReady(bundle);

                        //physicalExamMap.setImagePath(mCurrentPhotoPath);
                        Log.i(TAG, mCurrentPhotoPath);
                        //physicalExamMap.displayImage(this, filePath.getAbsolutePath(), imageName);
                        updateImageDatabase(mLastSelectedImageName);
                    }
                }
            });
    ActivityResultLauncher<Intent> mStartForGalleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String currentPhotoPath = "";
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            String[] filePath = {MediaStore.Images.Media.DATA};
                            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                            c.moveToFirst();
                            int columnIndex = c.getColumnIndex(filePath[0]);
                            String picturePath = c.getString(columnIndex);
                            c.close();
                            //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                            Log.v("path", picturePath + "");

                            // copy & rename the file
                            mLastSelectedImageName = UUID.randomUUID().toString();
                            currentPhotoPath = AppConstants.IMAGE_PATH + mLastSelectedImageName + ".jpg";
                            BitmapUtils.copyFile(picturePath, currentPhotoPath);

                            // Handle the Intent


                            Bundle bundle = new Bundle();
                            bundle.putString("image", currentPhotoPath);
                            imageUtilsListener.onImageReady(bundle);

                            //physicalExamMap.setImagePath(mCurrentPhotoPath);
                            Log.i(TAG, currentPhotoPath);
                            //physicalExamMap.displayImage(this, filePath.getAbsolutePath(), imageName);
                            updateImageDatabase(mLastSelectedImageName);
                        } else {
                            Toast.makeText(VisitCreationActivity.this, getResources().getString(R.string.unable_to_pick_data), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });

    private String mLastSelectedImageName = "";

    public void openCamera() {
        validatePermissionAndIntent();
    }

    private void cameraStart() {
        File file = new File(AppConstants.IMAGE_PATH);
        final String imagePath = file.getAbsolutePath();
        final String imageName = UUID.randomUUID().toString();
        mLastSelectedImageName = imageName;
        Intent cameraIntent = new Intent(VisitCreationActivity.this, CameraActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
        }
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        //mContext.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
        mStartForCameraResult.launch(cameraIntent);
    }

    private void galleryStart() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mStartForGalleryResult.launch(intent);
    }

    private static final int MY_CAMERA_REQUEST_CODE = 1001;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;

    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(VisitCreationActivity.this);
        builder.setTitle(getResources().getString(R.string.add_image_by));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    cameraStart();

                } else if (item == 1) {
                    galleryStart();

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void validatePermissionAndIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            //cameraStart();
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                cameraStart();
                selectImage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    ImageUtilsListener imageUtilsListener;

    public void setImageUtilsListener(ImageUtilsListener imageUtilsListener) {
        this.imageUtilsListener = imageUtilsListener;
    }

    private ObjectAnimator syncAnimator;

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            SyncUtils.syncNow(this, view, syncAnimator);
        }
    }

    public void showInfo(View view) {
    }

    public interface ImageUtilsListener {
        void onImageReady(Bundle bundle);

        void onImageReadyForDelete(int index, String image);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
}