package org.intelehealth.ekalarogya.activities.familyHistoryActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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


import org.apache.commons.lang3.StringUtils;
import org.intelehealth.ekalarogya.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.ekalarogya.models.AnswerResult;
import org.intelehealth.ekalarogya.shared.BaseActivity;
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
import org.intelehealth.ekalarogya.utilities.UuidDictionary;

import org.intelehealth.ekalarogya.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.ekalarogya.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.intelehealth.ekalarogya.utilities.pageindicator.ScrollingPagerIndicator;

public class FamilyHistoryActivity extends BaseActivity implements QuestionsAdapter.FabClickListener {
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;
    AlertDialog confirmationAlertDialog = null;
    boolean fabFlag = false;

    ArrayList<String> physicalExams;
    String mFileName = "famHist.json";
    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    //CustomExpandableListAdapter adapter;
    // ExpandableListView familyListView;

    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "", phistory = "", fhistory = "";
    boolean flag = false;
    boolean hasLicense = false;
    SharedPreferences.Editor e;
    SQLiteDatabase localdb, db;
    SessionManager sessionManager;
    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    private String imageName = null;
    private File filePath;
    ScrollingPagerIndicator recyclerViewIndicator;

    RecyclerView family_history_recyclerView;
    QuestionsAdapter adapter;
    String edit_FamHist = "";
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

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_FamHist = intent.getStringExtra("edit_FamHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);

            if (edit_FamHist == null)
                new_result = getFamilyHistoryVisitData();
        }

        boolean past = sessionManager.isReturning();
        if (past && edit_FamHist == null) {
            MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
            alertdialog.setTitle(getString(R.string.question_update_details));
            //AlertDialog.Builder alertdialog = new AlertDialog.Builder(FamilyHistoryActivity.this,R.style.AlertDialogStyle);
//            TextView textViewTitle = new TextView(this);
//            textViewTitle.setText(getString(R.string.question_update_details));
//            textViewTitle.setTextColor(getResources().getColor((R.color.colorPrimary)));
//            textViewTitle.setPadding(30,50,30,0);
//            textViewTitle.setTextSize(16F);
//            textViewTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            alertdialog.setCustomTitle(textViewTitle);

            View layoutInflater = LayoutInflater.from(FamilyHistoryActivity.this)
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
                    // skip
                    flag = false;

                    String[] columns = {"value", " conceptuuid"};

                    try {
                        String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                        String[] famHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
                        Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
                        famHistCursor.moveToLast();
                        fhistory = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                        famHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        fhistory = ""; // if family history does not exist
                    }

                    if (fhistory != null && !fhistory.isEmpty() && !fhistory.equals("null")) {
                        insertDb(fhistory, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                    }

                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        setTitle(R.string.title_activity_family_history);
        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        family_history_recyclerView = findViewById(R.id.family_history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        family_history_recyclerView.setLayoutManager(linearLayoutManager);
        family_history_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(family_history_recyclerView);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fabFlag) {
                    fabFlag = true;
                    onFabClick();
                }
            }
        });

//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                familyHistoryMap = new Node(currentFile); //Load the family history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            familyHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the family history mind map
        }

        //  familyListView = findViewById(R.id.family_history_expandable_list_view);

        adapter = new QuestionsAdapter(this, familyHistoryMap, family_history_recyclerView, this.getClass().getSimpleName(), this, false);
        family_history_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(family_history_recyclerView);
        /*adapter = new CustomExpandableListAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);*/

        /*familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                return false;
            }
        });*/
    }

    private String getFamilyHistoryVisitData() {
        String result = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

        String[] columns = {"value", " conceptuuid"};

        try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            String[] famHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
            Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            result = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if family history does not exist
        }

        db.close();
        return result;
    }

    private void onListClick(View v, int groupPosition, int childPosition) {
        if ((familyHistoryMap.getOption(groupPosition).getChoiceType().equals("single")) && !familyHistoryMap.getOption(groupPosition).anySubSelected()) {
            Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
            Log.i(TAG, "onChildClick: ");
            clickedNode.toggleSelected();
            if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
                familyHistoryMap.getOption(groupPosition).setSelected(true);
            } else {
                familyHistoryMap.getOption(groupPosition).setUnselected();
            }
            adapter.notifyDataSetChanged();

            if (clickedNode.getInputType() != null) {
                if (!clickedNode.getInputType().equals("camera")) {
                    Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, null, null);
                }
            }
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
                Log.i("RES>", "" + filePath + " -> " + res);
            }

            imageName = UUID.randomUUID().toString();

            if (!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal() &&
                    familyHistoryMap.getOption(groupPosition).getOption(childPosition).isSelected()) {
                Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
            }
        }else if ((familyHistoryMap.getOption(groupPosition).getChoiceType().equals("single")) && familyHistoryMap.getOption(groupPosition).anySubSelected()) {
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
        }else {
            Node question = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
            question.toggleSelected();
            if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
                familyHistoryMap.getOption(groupPosition).setSelected(true);
            } else {
                familyHistoryMap.getOption(groupPosition).setUnselected();
            }

            if (!familyHistoryMap.findDisplay().equalsIgnoreCase("Associated Symptoms")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("जुड़े लक्षण")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("ಸಂಬಂಧಿತ ರೋಗಲಕ್ಷಣಗಳು")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("संबद्ध लक्षणे")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("સંકળાયેલ લક્ષણો")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("সংশ্লিষ্ট উপসর্গ")
                    && !familyHistoryMap.findDisplay().equalsIgnoreCase("সংশ্লিষ্ট লক্ষণ")) {
                //code added to handle multiple and single option selection.
                Node rootNode = familyHistoryMap.getOption(groupPosition);
                if (rootNode.isMultiChoice() && !question.isExcludedFromMultiChoice()) {
                    for (int i = 0; i < rootNode.getOptionsList().size(); i++) {
                        Node childNode = rootNode.getOptionsList().get(i);
                        if (childNode.isSelected() && childNode.isExcludedFromMultiChoice()) {
                            familyHistoryMap.getOption(groupPosition).getOptionsList().get(i).setUnselected();
                        }
                    }
                }
                Log.v(TAG, "rootNode - " + new Gson().toJson(rootNode));
                if (!rootNode.isMultiChoice() || (rootNode.isMultiChoice() &&
                        question.isExcludedFromMultiChoice() && question.isSelected())) {
                    for (int i = 0; i < rootNode.getOptionsList().size(); i++) {
                        Node childNode = rootNode.getOptionsList().get(i);
                        if (!childNode.getId().equals(question.getId())) {
                            familyHistoryMap.getOption(groupPosition).getOptionsList().get(i).setUnselected();
                        }
                    }
                }
            }
            if (!question.getInputType().isEmpty() && question.isSelected()) {
                if (question.getInputType().equals("camera")) {
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    Node.handleQuestion(question, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
                } else {
                    Node.handleQuestion(question, FamilyHistoryActivity.this, adapter, null, null);
                }
                //If there is an input type, then the question has a special method of data entry.
            }

            if (!question.isTerminal() && question.isSelected()) {
                Node.subLevelQuestion(question, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
                //If the knowledgeEngine is not terminal, that means there are more questions to be asked for this branch.
            }
        }
        //adapter.updateNode(currentNode);
        adapter.notifyDataSetChanged();

    }

    private void onFabClick() {
        AnswerResult answerResult = familyHistoryMap.checkAllRequiredAnswered(FamilyHistoryActivity.this);
        if (!answerResult.result) {
            // show alert dialog
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(answerResult.requiredStrings);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fabFlag=false;
                    dialog.dismiss();
                }
            });
            Dialog alertDialog = alertDialogBuilder.show();
            Log.v(TAG, answerResult.requiredStrings);
            return;
        }else {
            ArrayList<String> displayList = new ArrayList<>();
            String displayStr = "", fhistoryDisplayStr = "";

            if (insertionList != null && insertionList.size() > 0) {
                insertionList.clear();
                insertion = "";
                fhistory = "";
            }

            String val = "", val_insert = "";
            if (familyHistoryMap.anySubSelected()) {
                for (Node node : familyHistoryMap.getOptionsList()) {
                    if (node.isSelected()) {
                        if (node.getOptionsList() != null) {
                            for (Node node1 : node.getOptionsList()) {
                                if (node1.isSelected()) {
                                    //val = val + Node.bullet + node1.findDisplay() + " - ";
                                    val = val + Node.bullet + node1.findDisplay() + " : ";
                                    val_insert = val_insert + Node.bullet + node1.getLanguage() + " : ";
                                    if (node1.getOptionsList() != null) {
                                        for (Node node2 : node1.getOptionsList()) {
                                            if (node2.isSelected()) {
                                                if (!node2.findDisplay().contains("[")) {
                                                    val = val + node2.findDisplay() + " - ";
                                                    val_insert = val_insert + node2.getLanguage() + " - ";
                                                } else {
                                                    val = val + node2.getLanguage() + " - ";
                                                    val_insert = val_insert + node2.getLanguage() + " - ";
                                                }
                                            }
                                        }
                                    }
                                    val = val + ".<br/>";
                                    val_insert = val_insert + ".<br/>";
                                }
                            }
                        } else {
                            val = val + Node.bullet + node.findDisplay();
                            val_insert = val_insert + Node.bullet + node.getLanguage();
                        }

                        String toInsertDisplay = node.findDisplay() + " <br/> " + val;
                        toInsertDisplay = toInsertDisplay.replaceAll(Node.bullet, "");
                        toInsertDisplay = toInsertDisplay.replaceAll(" - ", ", ");
                        //toInsertDisplay = toInsertDisplay.replaceAll(" - ", " : ");
                        toInsertDisplay = toInsertDisplay.replaceAll(", ." + "<br/>", ". ");
                        if (StringUtils.right(toInsertDisplay, 2).equals(", ")) {
                            toInsertDisplay = toInsertDisplay.substring(0, toInsertDisplay.length() - 2);
                        }
                        //toInsertDisplay = toInsertDisplay.trim() + ".<br/>";
                        toInsertDisplay = toInsertDisplay.replaceAll(": \\.", ":<br/>");
                        toInsertDisplay = toInsertDisplay.replaceAll("\\. ", "<br/>");
                        toInsertDisplay = toInsertDisplay.replaceAll("<br/><br/>", "<br/>");
                        displayList.add(toInsertDisplay);

                        String toInsert = node.getText() + " <br/> " + val_insert;
                        toInsert = toInsert.replaceAll(Node.bullet, "");
                        toInsert = toInsert.replaceAll(" - ", ", ");
                        //toInsertDisplay = toInsertDisplay.replaceAll(" - ", " : ");
                        toInsert = toInsert.replaceAll(", ." + "<br/>", ". ");
                        if (StringUtils.right(toInsert, 2).equals(", ")) {
                            toInsert = toInsert.substring(0, toInsert.length() - 2);
                        }
                        //toInsertDisplay = toInsertDisplay.trim() + ".<br/>";
                        toInsert = toInsert.replaceAll(": \\.", ":<br/>");
                        toInsert = toInsert.replaceAll("\\. ", "<br/>");
                        toInsert = toInsert.replaceAll("<br/><br/>", "<br/>");
                        insertionList.add(toInsert);
                    }
                }
            }

            for (int i = 0; i < insertionList.size(); i++) {
                if (i == 0) {
                    insertion = Node.bullet + insertionList.get(i);
                    displayStr = Node.bullet + displayList.get(i);
                } else {
                    insertion = insertion + " " + Node.bullet + insertionList.get(i);
                    displayStr = displayStr + " " + Node.bullet + displayList.get(i);
                }
            }

            insertion = insertion.replaceAll("null.", "");
            displayStr = displayStr.replaceAll("null.", "");

            List<String> imagePathList = familyHistoryMap.getImagePathList();

            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase(imagePath);
                }
            }

            if (intentTag != null && intentTag.equals("edit")) {
                if (!insertion.isEmpty() && !insertion.contains(":.") && !insertion.contains(": <br/>") && !insertion.endsWith("? <br/> ")) {
                    ConfirmationDialog(insertion, displayStr);
                } else {
                    if (insertion.isEmpty() || insertion.contains(":.") || insertion.contains(": <br/>") || insertion.endsWith("? <br/> ")) {
                        insertion = "";
                        updateDatabase(insertion, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                    } else {
                        updateDatabase(insertion, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                    }

                    Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
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
                boolean checkFlag = false;
                if (flag == true) {
                    // only if OK clicked, collect this new info (old patient)
                    if (insertion.length() > 0) {
                        fhistory = fhistory + insertion;
                        fhistoryDisplayStr = fhistoryDisplayStr + displayStr;
                    } else {
                        fhistory = fhistory + "";
                        fhistoryDisplayStr = fhistoryDisplayStr + "";
                    }
                    if (!fhistory.isEmpty() && !insertion.contains(":.") && !insertion.contains(": <br/>") && !insertion.endsWith("? <br/> ")) {
                        ConfirmationDialog(fhistory, fhistoryDisplayStr);
                    } else {
                        if (fhistory.isEmpty() || insertion.contains(":.") || insertion.contains(": <br/>") || insertion.endsWith("? <br/> ")) {
                            fhistory = "";
                        }

                        insertDb(fhistory, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                        checkFlag = true;
                    }
                    // insertDb(fhistory);
                } else {
                    if (!insertion.isEmpty() && !insertion.contains(":.") && !insertion.contains(": <br/>") && !insertion.endsWith("? <br/> ")) {
                        ConfirmationDialog(insertion, displayStr);
                    } else {
                        if (fhistory.isEmpty() || insertion.contains(":.") || insertion.contains(": <br/>") || insertion.endsWith("? <br/> ")) {
                            insertion = "";
                        }
                        insertDb(insertion, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                        checkFlag = true;
                    }
                    //insertDb(insertion); // new details of family history
                }

                if (checkFlag) {
                    flag = false;
                    sessionManager.setReturning(false);
                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    //   intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }
            }
        }
    }

    public void ConfirmationDialog(String confirmationStr, String displayStr) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setMessage(Html.fromHtml(displayStr));
        alertDialogBuilder.setPositiveButton(getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (intentTag != null && intentTag.equals("edit")) {
                    updateDatabase(confirmationStr, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                    // regional language store in db to show on VS screen.
                    JSONObject object = new JSONObject();
                    try {
                        object.put("text_" + sessionManager.getAppLanguage(), displayStr);
                        updateDatabase(object.toString(), UuidDictionary.FAMHIST_REG_LANG_VALUE);    // updating regional data.
                        Log.v("insertion_tag", "insertion_update_regional_famhist: " + object.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // end

                    fabFlag = false;
                    Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
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
                } else {
                    insertDb(confirmationStr, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                    //regional - start
                    JSONObject object = new JSONObject();
                    try {
                        object.put("text_" + sessionManager.getAppLanguage(), displayStr);
                        insertDb(object.toString(), UuidDictionary.FAMHIST_REG_LANG_VALUE);    // updating regional data.
                        Log.v("insertion_tag", "insertion_insert_regional_famhist_only: " + object.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // regional - end
                    flag = false;
                    fabFlag = false;
                    sessionManager.setReturning(false);
                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    //   intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.generic_back), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fabFlag = false;
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

    public boolean insertDb(String value, String conceptID) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(conceptID);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ekalarogya.utilities.StringUtils.getValue(value));
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
    public void onBackPressed() {
    }

    @Override
    public void fabClickedAtEnd() {
        if (!fabFlag) {
            fabFlag = true;
            onFabClick();
        }
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
}



