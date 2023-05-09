package org.intelehealth.ekalarogya.activities.pastMedicalHistoryActivity;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;


import org.intelehealth.ekalarogya.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.ekalarogya.activities.questionNodeActivity.QuestionNodeActivity;
import org.intelehealth.ekalarogya.models.AnswerResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.EncounterDAO;
import org.intelehealth.ekalarogya.database.dao.ImagesDAO;
import org.intelehealth.ekalarogya.database.dao.ObsDAO;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.models.dto.ObsDTO;
import org.intelehealth.ekalarogya.utilities.FileUtils;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.intelehealth.ekalarogya.utilities.UuidDictionary;

import org.intelehealth.ekalarogya.activities.familyHistoryActivity.FamilyHistoryActivity;
import org.intelehealth.ekalarogya.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.intelehealth.ekalarogya.utilities.pageindicator.ScrollingPagerIndicator;

import org.intelehealth.ekalarogya.database.dao.PatientsDAO;

public class PastMedicalHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {

    String patient = "patient";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;
    AlertDialog confirmationAlertDialog = null;

    ArrayList<String> physicalExams;
    int lastExpandedPosition = -1;

    String mFileName = "patHist.json";
    String image_Prefix = "MH";
    String imageDir = "Medical History";
    String imageName;
    File filePath;

    SQLiteDatabase localdb, db;
    String mgender;

    boolean hasLicense = false;
    String edit_PatHist = "";

//  String mFileName = "DemoHistory.json";

    private static final String TAG = PastMedicalHistoryActivity.class.getSimpleName();

    Node patientHistoryMap;
    // CustomExpandableListAdapter adapter;
    //ExpandableListView historyListView;

    String patientHistory, patientHistoryHindi, patientHistoryOdiya,
            patientHistoryGujrati,patientHistoryAssamese,
    patientHistoryBengali, patientHistoryKannada;
    String phistory = "", pHistoryOnly_REG = "";

    boolean flag = false;

    SessionManager sessionManager = null;
    private String encounterVitals;
    private String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    RecyclerView pastMedical_recyclerView;
    QuestionsAdapter adapter;
    ScrollingPagerIndicator recyclerViewIndicator;
    String new_result;

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

        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        e = sharedPreferences.edit();

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_PatHist = intent.getStringExtra("edit_PatHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);

            if (edit_PatHist == null)
                new_result = getPastMedicalVisitData();
        }

        boolean past = sessionManager.isReturning();
        if (past && edit_PatHist == null) {
            MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
            alertdialog.setTitle(getString(R.string.question_update_details));
//            TextView textViewTitle = new TextView(this);
//            textViewTitle.setText(getString(R.string.question_update_details));
//            textViewTitle.setTextColor(getResources().getColor((R.color.colorPrimary)));
//            textViewTitle.setPadding(30,50,30,0);
//            textViewTitle.setTextSize(16F);
//            textViewTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            alertdialog.setCustomTitle(textViewTitle);
            //AlertDialog.Builder alertdialog = new AlertDialog.Builder(PastMedicalHistoryActivity.this,R.style.AlertDialogStyle);

            View layoutInflater = LayoutInflater.from(PastMedicalHistoryActivity.this)
                    .inflate(R.layout.past_fam_hist_previous_details, null);
            alertdialog.setView(layoutInflater);
            TextView textView = layoutInflater.findViewById(R.id.textview_details);
            Log.v(TAG, new_result);
            textView.setText(Html.fromHtml(new_result));


//            alertdialog.setMessage(getString(R.string.question_update_details));
            alertdialog.setPositiveButton(getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // allow to edit
                    flag = true;
                }
            });
            alertdialog.setNegativeButton(getString(R.string.generic_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String[] columns = {"value", " conceptuuid"};
                    try {
                        String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                        String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                        Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
                        medHistCursor.moveToLast();
                        phistory = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                        medHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        phistory = ""; // if medical history does not exist
                        pHistoryOnly_REG = "";
                    }

                    // skip
                    flag = false;
                    if (phistory != null && !phistory.isEmpty() && !phistory.equals("null")) {
                        insertDb(phistory, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                    }

                    Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    //    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);

                }
            });
            AlertDialog alertDialog = alertdialog.create();
            alertDialog.show();

            Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
            pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
            nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);


        }

        setTitle(getString(R.string.title_activity_patient_history));
        setTitle(getTitle() + ": " + patientName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_medical_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        pastMedical_recyclerView = findViewById(R.id.pastMedical_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        pastMedical_recyclerView.setLayoutManager(linearLayoutManager);
        pastMedical_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(pastMedical_recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }

        });


//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                patientHistoryMap = new Node(currentFile); //Load the patient history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            patientHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the patient history mind map
        }

       /* historyListView = findViewById(R.id.patient_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, patientHistoryMap, this.getClass().getSimpleName()); //The adapter might change depending on the activity.
        historyListView.setAdapter(adapter);*/


        mgender = PatientsDAO.fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            patientHistoryMap.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            patientHistoryMap.fetchItem("1");
        }

        // flaoting value of age is passed to Node for comparison...
        patientHistoryMap.fetchAge(float_ageYear_Month);

        adapter = new QuestionsAdapter(this, patientHistoryMap, pastMedical_recyclerView, this.getClass().getSimpleName(), this, false);
        pastMedical_recyclerView.setAdapter(adapter);

        recyclerViewIndicator.attachToRecyclerView(pastMedical_recyclerView);



       /* historyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onListClick(v, groupPosition, childPosition);
                return false;
            }
        });

        //Same fix as before, close all other groups when something is clicked.
        historyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    historyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });*/
    }


    private void onListClick(View v, int groupPosition, int childPosition) {
        if ((patientHistoryMap.getOption(groupPosition).getChoiceType().equals("single")) && !patientHistoryMap.getOption(groupPosition).anySubSelected()) {
            Node clickedNode = patientHistoryMap.getOption(groupPosition).getOption(childPosition);
            clickedNode.toggleSelected();

            //Nodes and the expandable list act funny, so if anything is clicked, a lot of stuff needs to be updated.
            if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
                patientHistoryMap.getOption(groupPosition).setSelected(true);
            } else {
                patientHistoryMap.getOption(groupPosition).setUnselected();
            }
            adapter.notifyDataSetChanged();

            if (clickedNode.getInputType() != null) {
                if (!clickedNode.getInputType().equals("camera")) {
                    imageName = UUID.randomUUID().toString();
                    Node.handleQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, null, null);
                }
            }

            Log.i(TAG, String.valueOf(clickedNode.isTerminal()));
            if (!clickedNode.isTerminal() && clickedNode.isSelected()) {
                imageName = UUID.randomUUID().toString();

                Node.subLevelQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, filePath.toString(), imageName);
            }
        }else if ((patientHistoryMap.getOption(groupPosition).getChoiceType().equals("single")) && patientHistoryMap.getOption(groupPosition).anySubSelected()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QuestionNodeActivity.this,R.style.AlertDialogStyle);
            alertDialogBuilder.setMessage(R.string.this_question_only_one_answer);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        } else {

            Node question = patientHistoryMap.getOption(groupPosition).getOption(childPosition);
            question.toggleSelected();
            if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
                patientHistoryMap.getOption(groupPosition).setSelected(true);
            } else {
                patientHistoryMap.getOption(groupPosition).setUnselected();
            }

            if (!patientHistoryMap.findDisplay().equalsIgnoreCase("Associated Symptoms")
                    && !patientHistoryMap.findDisplay().equalsIgnoreCase("जुड़े लक्षण")
                    && !patientHistoryMap.findDisplay().equalsIgnoreCase("ಸಂಬಂಧಿತ ರೋಗಲಕ್ಷಣಗಳು")
                    && !patientHistoryMap.findDisplay().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")
                    && !patientHistoryMap.findDisplay().equalsIgnoreCase("સંકળાયેલ લક્ષણો")
                    && !patientHistoryMap.findDisplay().equalsIgnoreCase("সংশ্লিষ্ট উপসর্গ")
                    && !patientHistoryMap.findDisplay().equalsIgnoreCase("সংশ্লিষ্ট লক্ষণ")) {
                //code added to handle multiple and single option selection.
                Node rootNode = patientHistoryMap.getOption(groupPosition);
                if (rootNode.isMultiChoice() && !question.isExcludedFromMultiChoice()) {
                    for (int i = 0; i < rootNode.getOptionsList().size(); i++) {
                        Node childNode = rootNode.getOptionsList().get(i);
                        if (childNode.isSelected() && childNode.isExcludedFromMultiChoice()) {
                            patientHistoryMap.getOption(groupPosition).getOptionsList().get(i).setUnselected();
                        }
                    }
                }
                Log.v(TAG, "rootNode - " + new Gson().toJson(rootNode));
                if (!rootNode.isMultiChoice() || (rootNode.isMultiChoice() &&
                        question.isExcludedFromMultiChoice() && question.isSelected())) {
                    for (int i = 0; i < rootNode.getOptionsList().size(); i++) {
                        Node childNode = rootNode.getOptionsList().get(i);
                        if (!childNode.getId().equals(question.getId())) {
                            patientHistoryMap.getOption(groupPosition).getOptionsList().get(i).setUnselected();
                        }
                    }
                }
            }
            if (!question.getInputType().isEmpty() && question.isSelected()) {
                if (question.getInputType().equals("camera")) {
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    Node.handleQuestion(question, PastMedicalHistoryActivity.this, adapter, filePath.toString(), imageName);
                } else {
                    Node.handleQuestion(question, PastMedicalHistoryActivity.this, adapter, null, null);
                }
                //If there is an input type, then the question has a special method of data entry.
            }

            if (!question.isTerminal() && question.isSelected()) {
                Node.subLevelQuestion(question, PastMedicalHistoryActivity.this, adapter, filePath.toString(), imageName);
                //If the knowledgeEngine is not terminal, that means there are more questions to be asked for this branch.
            }
        }
        //adapter.updateNode(currentNode);
        adapter.notifyDataSetChanged();

    }

    private void fabClick() {
        //If nothing is selected, there is nothing to put into the database.

        AnswerResult answerResult = patientHistoryMap.checkAllRequiredAnswered(PastMedicalHistoryActivity.this);
        if (!answerResult.result) {
            // show alert dialog
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(answerResult.requiredStrings);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            Dialog alertDialog = alertDialogBuilder.show();
            Log.v(TAG, answerResult.requiredStrings);
            return;
        }else {
            patientHistory = "";
            patientHistoryHindi = "";
            patientHistoryOdiya = "";
            patientHistoryGujrati = "";
            patientHistoryAssamese = "";
            patientHistoryBengali = "";
            patientHistoryKannada = "";
            List<String> imagePathList = patientHistoryMap.getImagePathList();

            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase(imagePath);
                }
            }

            if (intentTag != null && intentTag.equals("edit")) {
                if (patientHistoryMap.anySubSelected()) {
                    patientHistory = patientHistoryMap.generateLanguage();
                    //String []arr=patientHistory.split(" - <br/>");
                    if (!patientHistory.isEmpty() && !patientHistory.endsWith(" - <br/>")) {
                        if (sessionManager.getCurrentLang().equalsIgnoreCase("hi")) {
                            patientHistoryHindi = patientHistoryMap.generateLanguage("hi");
                            ConfirmationDialog(patientHistory, patientHistoryHindi);
                        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("or")) {
                            patientHistoryOdiya = patientHistoryMap.generateLanguage("or");
                            ConfirmationDialog(patientHistory, patientHistoryOdiya);
                        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("gu")) {
                            patientHistoryGujrati = patientHistoryMap.generateLanguage("gu");
                            ConfirmationDialog(patientHistory, patientHistoryGujrati);
                        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("as")) {
                            patientHistoryAssamese = patientHistoryMap.generateLanguage("as");
                            ConfirmationDialog(patientHistory, patientHistoryAssamese);
                        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("bn")) {
                            patientHistoryBengali = patientHistoryMap.generateLanguage("bn");
                            ConfirmationDialog(patientHistory, patientHistoryBengali);
                        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("kn")) {
                            patientHistoryKannada = patientHistoryMap.generateLanguage("kn");
                            ConfirmationDialog(patientHistory, patientHistoryKannada);
                        } else {
                            ConfirmationDialog(patientHistory, patientHistory);
                        }
                    }
                    //updateDatabase(patientHistory); // update details of patient's visit, when edit button on VisitSummary is pressed
                }

                /**
                 * Note: On edit, pastmedhist will never be empty as all the questions are mandatory to be answered
                 * so no need to add regional here in the below block of if().
                 */
                if (patientHistory.isEmpty() || patientHistory.endsWith(" - <br/>")) {
                    patientHistory = "";
                    updateDatabase(patientHistory, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                    Intent intent = new Intent(PastMedicalHistoryActivity.this, VisitSummaryActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    intent.putExtra("hasPrescription", "false");
                    startActivity(intent);
                }
            } else {
                //if(patientHistoryMap.anySubSelected()){
                patientHistory = patientHistoryMap.generateLanguage();
                if (patientHistory != null && !patientHistory.isEmpty() && !patientHistory.endsWith(" - <br/>")) {
                    if (sessionManager.getCurrentLang().equalsIgnoreCase("hi")) {
                        patientHistoryHindi = patientHistoryMap.generateLanguage("hi");
                        ConfirmationDialog(patientHistory, patientHistoryHindi);
                    } else if (sessionManager.getCurrentLang().equalsIgnoreCase("or")) {
                        patientHistoryOdiya = patientHistoryMap.generateLanguage("or");
                        ConfirmationDialog(patientHistory, patientHistoryOdiya);
                    } else if (sessionManager.getCurrentLang().equalsIgnoreCase("gu")) {
                        patientHistoryGujrati = patientHistoryMap.generateLanguage("gu");
                        ConfirmationDialog(patientHistory, patientHistoryGujrati);
                    } else if (sessionManager.getCurrentLang().equalsIgnoreCase("as")) {
                        patientHistoryAssamese = patientHistoryMap.generateLanguage("as");
                        ConfirmationDialog(patientHistory, patientHistoryAssamese);
                    } else if (sessionManager.getCurrentLang().equalsIgnoreCase("bn")) {
                        patientHistoryBengali = patientHistoryMap.generateLanguage("bn");
                        ConfirmationDialog(patientHistory, patientHistoryBengali);
                    } else if (sessionManager.getCurrentLang().equalsIgnoreCase("kn")) {
                        patientHistoryKannada = patientHistoryMap.generateLanguage("kn");
                        ConfirmationDialog(patientHistory, patientHistoryKannada);
                    } else {
                        ConfirmationDialog(patientHistory, patientHistory);
                    }
                } else {
                    /**
                     * Note: On edit, pastmedhist will never be empty as all the questions are mandatory to be answered
                     * so no need to add regional here in the below block of if().
                     */
                    if (patientHistory == null || patientHistory.isEmpty() || patientHistory.endsWith(" - <br/>")) {
                        patientHistory = "";
                        if (flag == true) { // only if OK clicked, collect this new info (old patient)
                            phistory = phistory + patientHistory; // only PMH updated
                            sessionManager.setReturning(true);
                            phistory = Node.dateformate_hi_or_gu_as_en(phistory, sessionManager);
                            insertDb(phistory, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                            // however, we concat it here to patientHistory and pass it along to FH, not inserting into db
                        } else  // new patient, directly insert into database
                        {
                            patientHistory = Node.dateformate_hi_or_gu_as_en(patientHistory, sessionManager);
                            insertDb(patientHistory, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                        }
                        Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
                        intent.putExtra("patientUuid", patientUuid);
                        intent.putExtra("visitUuid", visitUuid);
                        intent.putExtra("encounterUuidVitals", encounterVitals);
                        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                        intent.putExtra("tag", intentTag);
                        //intent.putStringArrayListExtra("exams", physicalExams);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    public void ConfirmationDialog(String patHist, String displayStr) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);

        if (displayStr.contains("Yes [Describe]") || displayStr.contains("[Describe]") || displayStr.contains("[Describe]"))
        {
            displayStr.replaceAll("Yes [Describe]","");
            displayStr.replaceAll("Other [Describe]","");
            displayStr.replaceAll("[Describe]","");
        }

        displayStr=Node.dateformat_en_hi_or_gu_as(displayStr,sessionManager);
        patHist=Node.dateformate_hi_or_gu_as_en(patHist,sessionManager);
        phistory=Node.dateformate_hi_or_gu_as_en(phistory,sessionManager);
        String finalPatHist = patHist;
        String finalPatHist_REG = displayStr;

        alertDialogBuilder.setMessage(Html.fromHtml(displayStr));

        alertDialogBuilder.setPositiveButton(getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                if (intentTag != null && intentTag.equals("edit")) {
                    updateDatabase(finalPatHist, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);

                    // regional language store in db to show on VS screen.
                    JSONObject object = new JSONObject();
                    try {
                        object.put("text_" + sessionManager.getAppLanguage(), finalPatHist_REG);
                        updateDatabase(object.toString(), UuidDictionary.PASTHIST_REG_LANG_VALUE);    // updating regional data.
                        Log.v("insertion_tag", "insertion_update_regional_pathist: " + object.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // end

                    intent = new Intent(PastMedicalHistoryActivity.this, VisitSummaryActivity.class);
                } else {
                    if (flag == true) { // only if OK clicked, collect this new info (old patient)
                        phistory = phistory + finalPatHist; // only PMH updated
                        sessionManager.setReturning(true);

                        //regional - start
                        pHistoryOnly_REG = pHistoryOnly_REG + finalPatHist_REG;
                        JSONObject object = new JSONObject();
                        try {
                            object.put("text_" + sessionManager.getAppLanguage(), pHistoryOnly_REG);
                            insertDb(object.toString(), UuidDictionary.PASTHIST_REG_LANG_VALUE);    // updating regional data.
                            Log.v("insertion_tag", "insertion_insert_regional_pathist_only: " + object.toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // regional - end

                        insertDb(phistory, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                        // however, we concat it here to patientHistory and pass it along to FH, not inserting into db
                    } else  // new patient, directly insert into database
                    {
                        //regional - start
                        JSONObject object = new JSONObject();
                        try {
                            object.put("text_" + sessionManager.getAppLanguage(), finalPatHist_REG);
                            insertDb(object.toString(), UuidDictionary.PASTHIST_REG_LANG_VALUE);    // updating regional data.
                            Log.v("insertion_tag", "insertion_insert_regional_pathist: " + object.toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // regional - end

                        insertDb(finalPatHist, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                    }
                    intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class); // earlier it was vitals
                }

                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent.putExtra("tag", intentTag);
                startActivity(intent);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.generic_back), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        confirmationAlertDialog = alertDialogBuilder.create();
        // alertDialog.show();
        if (!confirmationAlertDialog.isShowing()) {
            confirmationAlertDialog.show();
            confirmationAlertDialog.setCancelable(false);
            IntelehealthApplication.setAlertDialogCustomTheme(this, confirmationAlertDialog);
        }
    }

    /**
     * This method inserts medical history of patient in database.
     *
     * @param value variable of type String
     * @return long
     */
    public boolean insertDb(String value, String conceptID) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(conceptID);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    private void updateImageDatabase(String imagePath) {

        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string, String conceptID) {

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                patientHistoryMap.setImagePath(mCurrentPhotoPath);
                Log.i(TAG, mCurrentPhotoPath);
                patientHistoryMap.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void fabClickedAtEnd() {
        // patientHistoryMap = node;
        fabClick();
    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos, int physExamPos) {
        onListClick(null, groupPos, childPos);
    }


    public void AnimateView(View v) {

        int fadeInDuration = 500; // Configure time values here
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        if (v != null) {
            v.setAnimation(animation);
        }


    }

    public void bottomUpAnimation(View v) {

        if (v != null) {
            v.setVisibility(View.VISIBLE);
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            v.startAnimation(bottomUp);
        }

    }

    private String getPastMedicalVisitData() {
        String result = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // String[] columns = {"value"};
        String[] columns = {"value", " conceptuuid"};
        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
            Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            result = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if medical history does not exist
        }
        db.close();
        return result;
    }
}