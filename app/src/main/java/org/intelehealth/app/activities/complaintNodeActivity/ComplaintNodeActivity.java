package org.intelehealth.app.activities.complaintNodeActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.questionNodeActivity.QuestionNodeActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.common.adapter.NodeAdapterUtils;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.ncd.constants.Constants;
import org.intelehealth.ncd.fhir.CommonQuestionnaireActivity;
import org.intelehealth.ncd.room.dao.PatientDao;
import org.intelehealth.ncd.utils.CategorySegregationUtils;
import org.intelehealth.ncd.utils.DateAndTimeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ComplaintNodeActivity extends AppCompatActivity {
    final String TAG = "Complaint Node Activity";
    String patientUuid, visitUuid, state, patientName, intentTag, encounterVitals, encounterAdultIntials, EncounterAdultInitial_LatestVisit, mgender;
    SearchView searchView;
    List<Node> complaints/*, suggestedComplaints*/;
    ComplaintNodeListAdapter listAdapter;
    //    SuggestedComplaintNodeListAdapter suggestedComplaintListAdapter;
    EncounterDTO encounterDTO;
    SessionManager sessionManager = null;
    ImageView img_question;
    TextView tv_selectComplaint;
    RecyclerView list_recyclerView/*, rv_suggested_complaints*/;
    private float float_ageYear_Month;
    private String intentAdviceFrom;
    private int mAgeInMonth = 0;
    private String mAgeAndMonth = "";
    private List<ReasonData> mSelectedComplains = new ArrayList<>();
    private String mIntentFromNCDCategoryName = Constants.GENERAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            intentAdviceFrom = intent.getStringExtra("advicefrom");
            // to know from which category NCD screening is being started
            mIntentFromNCDCategoryName = intent.getStringExtra(Constants.INTENT_NCD_CATEGORY);
        }
        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();

        }

        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
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

        setTitle(patientName + ": " + getTitle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_node);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
//        toolbar.setTitleTextColor(Color.WHITE);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //img_question = findViewById(R.id.img_question);
        //tv_selectComplaint = findViewById(R.id.tv_selectComplaint);
        list_recyclerView = findViewById(R.id.list_recyclerView);

//        rv_suggested_complaints = findViewById(R.id.rvSuggestedComplaints);
//        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
//        layoutManager.setFlexDirection(FlexDirection.ROW);
//        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
//        rv_suggested_complaints.setLayoutManager(layoutManager);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list_recyclerView.setLayoutManager(linearLayoutManager);
        list_recyclerView.setItemAnimator(new DefaultItemAnimator());


        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmComplaints();
            }
        });


//        ListView complaintList = findViewById(R.id.complaint_list_view);
//        if (complaintList != null) {
//            complaintList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//            complaintList.setClickable(true);
//        }

        complaints = new ArrayList<>();
//        suggestedComplaints = new ArrayList<>();


        boolean hasLicense = !sessionManager.getLicenseKey().isEmpty();
        JSONObject currentFile = null;

        if (hasLicense) {
            File base_dir = new File(this.getFilesDir().getAbsolutePath() + File.separator + AppConstants.JSON_FOLDER);
            File[] files = base_dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    String fileData = FileUtils.readFile(file.getName(), this);
                    if (fileData != null) {
                        try {
                            currentFile = new JSONObject(fileData);
                        } catch (JSONException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                    if (currentFile != null) {
                        Log.i(TAG, currentFile.toString());
                        Node currentNode = new Node(currentFile);

                        complaints.add(currentNode);
                    }
                }
                //remove items from complaints array here...
                mgender = PatientsDAO.fetch_gender(patientUuid);

                for (int i = 0; i < complaints.size(); i++) {
                    if (mgender.equalsIgnoreCase("M") && complaints.get(i).getGender().equalsIgnoreCase("0")) {

                        complaints.get(i).remove(complaints, i);
                        i--;
                    } else if (mgender.equalsIgnoreCase("F") && complaints.get(i).getGender().equalsIgnoreCase("1")) {
                        complaints.get(i).remove(complaints, i);
                        i--;
                    }
                }

                for (int i = 0; i < complaints.size(); i++) {
                    if (!complaints.get(i).getMin_age().equalsIgnoreCase("") && !complaints.get(i).getMax_age().equalsIgnoreCase("")) {

                        if (float_ageYear_Month < Float.parseFloat(complaints.get(i).getMin_age().trim())) { //age = 1 , min_age = 5
                            complaints.get(i).remove(complaints, i);
                            i--;
                        }

                        //else if(!optionsList.get(i).getMax_age().equalsIgnoreCase(""))
                        else if (float_ageYear_Month > Float.parseFloat(complaints.get(i).getMax_age())) { //age = 15 , max_age = 10
                            complaints.get(i).remove(complaints, i);
                            i--;
                        }
                    }
                }
            }
        } else {
            String protocolDirectory = FileUtils.getDirectoryForProtocols(intentAdviceFrom);
            String[] fileNames = {};
            try {
                List<String> tempArrayList = new ArrayList<>();
                String[] tempFileNames = getApplicationContext().getAssets().list(protocolDirectory);

                if (tempFileNames != null) {
                    for (String fileName : tempFileNames) {
                        if (fileName.endsWith(".json")) {
                            tempArrayList.add(fileName);
                        }
                    }

                    fileNames = tempArrayList.toArray(new String[0]);
                }
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            if (fileNames != null) {
                for (String name : fileNames) {
                    String fileLocation = protocolDirectory + "/" + name;
                    currentFile = FileUtils.encodeJSON(this, fileLocation);
                    Node currentNode = new Node(currentFile);
                    complaints.add(currentNode);
                }

                //remove items from complaints array here...
                mgender = PatientsDAO.fetch_gender(patientUuid);

                for (int i = 0; i < complaints.size(); i++) {
                    if (mgender.equalsIgnoreCase("M") && complaints.get(i).getGender() != null && complaints.get(i).getGender().equalsIgnoreCase("0")) {

                        complaints.get(i).remove(complaints, i);
                        i--;
                    } else if (mgender.equalsIgnoreCase("F") && complaints.get(i).getGender() != null && complaints.get(i).getGender().equalsIgnoreCase("1")) {
                        complaints.get(i).remove(complaints, i);
                        i--;
                    }
                }

                for (int i = 0; i < complaints.size(); i++) {
                    if (complaints.get(i).getMin_age() != null && !complaints.get(i).getMin_age().equalsIgnoreCase("") && complaints.get(i).getMax_age() != null && !complaints.get(i).getMax_age().equalsIgnoreCase("")) {
                        if (float_ageYear_Month < Float.parseFloat(complaints.get(i).getMin_age().trim())) { //age = 1 , min_age = 5
                            complaints.get(i).remove(complaints, i);
                            i--;
                        } else if (float_ageYear_Month > Float.parseFloat(complaints.get(i).getMax_age())) { //age = 15 , max_age = 10
                            complaints.get(i).remove(complaints, i);
                            i--;
                        }

                    }

                }
            }
        }
        listAdapter = new ComplaintNodeListAdapter(this, complaints);
//        suggestedComplaintListAdapter = new SuggestedComplaintNodeListAdapter(this, suggestedComplaints);
        list_recyclerView.setAdapter(listAdapter);
//        rv_suggested_complaints.setAdapter(suggestedComplaintListAdapter);

        //img_question.setVisibility(View.VISIBLE);
        //tv_selectComplaint.setVisibility(View.VISIBLE);
        list_recyclerView.setVisibility(View.VISIBLE);
//        rv_suggested_complaints.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);

        String[] temp = String.valueOf(float_ageYear_Month).split("\\.");
        mAgeInMonth = Integer.parseInt(temp[0]) * 12 + Integer.parseInt(temp[1]);
        if (Integer.parseInt(temp[0]) == 0) {
            mAgeAndMonth = temp[1] + " " + getResources().getString(R.string.months);
        } else if (Integer.parseInt(temp[0]) == 0) {
            mAgeAndMonth = temp[0] + " " + getResources().getString(R.string.years);
        } else {
            mAgeAndMonth = temp[0] + " " + getResources().getString(R.string.years) + " " + temp[1] + " " + getResources().getString(R.string.months);
        }
        ((TextView) findViewById(R.id.tv_title)).setText(patientName);
        ((TextView) findViewById(R.id.tv_title_desc)).setText(String.format("%s/%s", mgender, mAgeAndMonth));

        fetchEligibleProtocols();


    }

    private void fetchEligibleProtocols() {
        PatientsDAO patientsDAO = new PatientsDAO();
        List<String> diseaseList;

        try {
            String patientBirthDate = PatientsDAO.getPatientDetailsForRedirection(patientUuid).getDate_of_birth();
            String patientMedicalHistoryJson = patientsDAO.getValueFromPatientAttrbTable(patientUuid, Constants.OTHER_MEDICAL_HISTORY);

            diseaseList = new CategorySegregationUtils(getResources()).populateDiseaseListBasedOnAgeAndHistory(DateAndTimeUtils.INSTANCE.calculateAgeInYears(patientBirthDate), patientMedicalHistoryJson);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        if (diseaseList.size() == 0) {
            displayIneligibleConfirmationDialog();
        } else if (diseaseList.size() == 1 && diseaseList.get(0).equalsIgnoreCase(getString(R.string.tab_general))) {
            displayIneligibleConfirmationDialog();
        } else {
            autoSelectComplaints(diseaseList);
        }
    }

    private void displayIneligibleConfirmationDialog() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setMessage(getString(R.string.not_eligible_for_protocols_message));

        alertDialogBuilder.setPositiveButton(getString(R.string.yes_move_ahead), (dialog, which) -> dialog.dismiss());
        alertDialogBuilder.setNegativeButton(getString(R.string.no_go_back), (dialog, which) -> {
            deleteVisitAndGoBack();
        });

        Dialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void deleteVisitAndGoBack() {
        VisitAttributeListDAO.deleteVisitAttributeUsingVisitUuid(visitUuid);
        EncounterDAO.deleteEncounterUsingVisitUuid(visitUuid);
        VisitsDAO.deleteVisitUsingVisitUuid(visitUuid);
        finish();
    }

    private void autoSelectComplaints(List<String> diseaseList) {
        /*for (String disease : diseaseList) {
            for (Node complaint : complaints) {
                if (disease.equalsIgnoreCase(complaint.getText())) {
                    complaint.toggleSelected();
                }
            }
        }*/
        String selectedLineListCategoryName = mIntentFromNCDCategoryName.toLowerCase().replaceAll("_", "").replaceAll(" ", "").replaceAll("-", "");
        for (Node complaint : complaints) {
            String complainName = complaint.getText().toLowerCase().replaceAll("_", "").replaceAll(" ", "").replaceAll("-", "");
            if (selectedLineListCategoryName.equalsIgnoreCase(complainName)) {
                complaint.toggleSelected();
                break;
            }
        }
    }

    /**
     * Method to confirm all the complaints that were selected, and ensure that the conversation with the patient is thorough.
     */
    public void confirmComplaints() {
        final ArrayList<String> selection = new ArrayList<>();
        final ArrayList<String> displaySelection = new ArrayList<>();
        mSelectedComplains.clear();
        if (listAdapter != null) {
            for (Node node : listAdapter.getmNodes()) {
                if (node.isSelected()) {
                    selection.add(node.getText());
                    displaySelection.add(node.findDisplay());
                    String name = node.getText();
                    ReasonData data = new ReasonData();
                    data.setReasonName(name);
                    data.setReasonNameLocalized(NodeAdapterUtils.getTheChiefComplainNameWRTLocaleNCD(ComplaintNodeActivity.this, name));
                    mSelectedComplains.add(data);
                }
            }

            if (selection.isEmpty()) {
               /* MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                alertDialogBuilder.setTitle(getString(R.string.complaint_dialog_title));
                alertDialogBuilder.setMessage(getString(R.string.complaint_required));
                alertDialogBuilder.setNeutralButton(getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.show();
                // alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                //pb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);*/
                // show custom dialog
                DialogUtils d1 = new DialogUtils();
                d1.showCommonDialog(this, 0, getString(R.string.complaint_dialog_title), getString(R.string.complaint_required), true,
                        getString(R.string.generic_ok), "", null);


            } else {

                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialogWithChipsGrid(ComplaintNodeActivity.this, new ArrayList<ReasonData>(mSelectedComplains), R.drawable.ui2_visit_reason_summary_icon, getResources().getString(R.string.confirm_visit_reason), getResources().getString(R.string.are_you_sure_the_patient_has_the_following_reasons_for_a_visit), false, getResources().getString(R.string.yes), getResources().getString(R.string.no), new DialogUtils.CustomDialogListener() {
                    @Override
                    public void onDialogActionDone(int action) {
                        if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                            Intent intent = new Intent(ComplaintNodeActivity.this, QuestionNodeActivity.class);
                            intent.putExtra("patientUuid", patientUuid);
                            intent.putExtra("visitUuid", visitUuid);
                            intent.putExtra("encounterUuidVitals", encounterVitals);
                            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                            intent.putExtra("state", state);
                            intent.putExtra("name", patientName);
                            intent.putExtra("advicefrom", intentAdviceFrom);
                            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                            intent.putExtra("advicefrom", intentAdviceFrom);
                            if (intentTag != null) {
                                intent.putExtra("tag", intentTag);
                            }
                            intent.putStringArrayListExtra("complaints", selection);
                            String fileLocation = AppConstants.NCD_PROTOCOL_DIRECTORY + "/" + selection.get(0) + ".json";
                            //JSONObject currentFile = FileUtils.encodeJSON(ComplaintNodeActivity.this, fileLocation);
                            String questionnaireTitle = selection.get(0);
                            Intent in = new Intent(ComplaintNodeActivity.this, CommonQuestionnaireActivity.class);
                            //in.setComponent(new ComponentName("com.example.fhir_sdk_poc", "com.example.fhir_sdk_poc.MainActivity"));
                            //in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            in.putExtra("questionnaire_title", questionnaireTitle);  // replace with the actual key
                            in.putExtra("patient_dob", PatientsDAO.fetchDateOfBirth(patientUuid));
                            in.putExtra("patient_age", float_ageYear_Month);
                            in.putExtra("patient_gender", mgender);
                            in.putExtra("appLang", sessionManager.getAppLanguage());

                            questionnaireLauncher.launch(in);






                            /*Intent in = new Intent();
                            in.setComponent(new ComponentName("com.example.fhir_sdk_poc", "com.example.fhir_sdk_poc.MainActivity"));
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(in);*/
                            //finish();
                        }
                    }
                });

               /* MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                alertDialogBuilder.setTitle(R.string.complaint_dialog_title);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.list_dialog_complaint, null);
                alertDialogBuilder.setView(convertView);
                ListView listView = convertView.findViewById(R.id.complaint_dialog_list_view);
                listView.setDivider(null);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displaySelection);
                listView.setAdapter(arrayAdapter);
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(ComplaintNodeActivity.this, QuestionNodeActivity.class);
                        intent.putExtra("patientUuid", patientUuid);
                        intent.putExtra("visitUuid", visitUuid);
                        intent.putExtra("encounterUuidVitals", encounterVitals);
                        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("advicefrom", intentAdviceFrom);
                        intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                        intent.putExtra("advicefrom", intentAdviceFrom);
                        if (intentTag != null) {
                            intent.putExtra("tag", intentTag);
                        }
                        intent.putStringArrayListExtra("complaints", selection);

                        startActivity(intent);
                    }
                });
                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.complaint_change_selected), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.show();
                //alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                // pb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                //nb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);*/
            }
        }
    }

    private ActivityResultLauncher<Intent> questionnaireLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("onActivityResult", "received!");
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String questionnaireResponseJson = data.getStringExtra("questionnaire_response");
                        String questionnaireResponseJsonLocal = data.getStringExtra("questionnaire_response_local");
                        Log.d("onActivityResult", "Response JSON: " + questionnaireResponseJson);

                        // show response rest in a alert dialog
                       /* AlertDialog alertDialog = new AlertDialog.Builder(ComplaintNodeActivity.this)
                                .setTitle("Questionnaire Response")
                                .setMessage(questionnaireResponseJson)
                                .setPositiveButton("OK", null)
                                .create();
                        alertDialog.show();*/
                        updateDatabase(questionnaireResponseJson, UuidDictionary.CURRENT_COMPLAINT);
                        JSONObject object = new JSONObject();
                        try {
                            object.put("text_" + sessionManager.getAppLanguage(), questionnaireResponseJsonLocal);
                            object.put("text_en", questionnaireResponseJson);
                            updateDatabase(object.toString(), UuidDictionary.CC_REG_LANG_VALUE);    // updating regional data.
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // You can parse or handle the response here
                        Intent intent = new Intent(ComplaintNodeActivity.this, VisitSummaryActivity_New.class);
                        intent.putExtra("patientUuid", patientUuid);
                        intent.putExtra("visitUuid", visitUuid);
                        intent.putExtra("encounterUuidVitals", encounterVitals);
                        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("tag", intentTag);
                        intent.putExtra("advicefrom", intentAdviceFrom);
                        startActivity(intent);
                        finish();
                    }
                }
            }
    );

    private void updateDatabase(String string, String conceptID) {
        Log.i(TAG, "updateDatabase: " + patientUuid + " " + visitUuid + " " + conceptID);

        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(conceptID);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, conceptID));
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

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setFocusableInTouchMode(true);
        //searchView.setFocusable(true);
        //searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (listAdapter != null) {
                    listAdapter.filter(newText);
                }
                return true;
            }
        });

        return true;
    }


    // Animate views and handle their visibility
    private void animateView(View v) {

        v.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        fadeIn.setFillAfter(true);
        v.startAnimation(fadeIn);

    }

    private void bottomUpAnimation(View v) {

        v.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        v.startAnimation(bottomUp);

    }

    public void backPress(View view) {
        deleteVisitAndGoBack();
    }
}
