package org.intelehealth.app.activities.familyHistoryActivity;

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
import org.intelehealth.app.shared.BaseActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.utilities.pageindicator.ScrollingPagerIndicator;

public class FamilyHistoryActivity extends BaseActivity implements QuestionsAdapter.FabClickListener {
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
    String state;
    String patientName, patientFName, patientLName;
    String patientGender;
    String intentTag;
    private float float_ageYear_Month;

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
        //In case of crash still the org should hold the current lang fix.
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
            patientFName = intent.getStringExtra("patientFirstName");
            patientLName = intent.getStringExtra("patientLastName");
            patientGender = intent.getStringExtra("gender");
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
            textView.setSingleLine(false);
            Log.v(TAG, new_result);

            if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                textView.setText(Html.fromHtml(getUpdateTranslations(new_result)));
            } else {
                textView.setText(Html.fromHtml(new_result));
            }

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
                        insertDb(fhistory);
                    }

                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("patientFirstName", patientFName);
                    intent.putExtra("patientLastName", patientLName);
                    intent.putExtra("gender", patientGender);
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
                triggerConfirmation();
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

    private void triggerConfirmation() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);

        // Depending on the app language, our alert dialog text will be translated
        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            alertDialogBuilder.setMessage(Html.fromHtml(familyHistoryMap.formQuestionAnswer(0)
                    .replace("Question not answered", "प्रश्नाचे उत्तर दिले नाही")
                    .replace("Patient reports -", "रुग्ण अहवाल-")
                    .replace("Patient denies -", "रुग्ण नकार देतो-")
                    .replace("Hours", "तास")
                    .replace("Days", "दिवस")
                    .replace("Weeks", "आठवडे")
                    .replace("Months", "महिने")
                    .replace("Years", "वर्षे")
                    .replace("times per hour", "प्रति तास")
                    .replace("time per day", "दररोज वेळा")
                    .replace("times per week", "आठवड्यातून काही वेळा")
                    .replace("times per month", "दरमहा वेळा")
                    .replace("times per year", "दरवर्षी वेळा")
                    .replace("Jan", "जानेवारी")
                    .replace("Feb", "फेब्रुवारी")
                    .replace("Mar", "मार्च")
                    .replace("Apr", "एप्रिल")
                    .replace("May", "मे")
                    .replace("Jun", "जून")
                    .replace("Jul", "जुलै")
                    .replace("Aug", "ऑगस्ट")
                    .replace("Sept", "सप्टेंबर")
                    .replace("Oct", "ऑक्टोबर")
                    .replace("Nov", "नोव्हेंबर")
                    .replace("Dec", "डिसेंबर")));
        } else {
            // Else case handles the English language
            alertDialogBuilder.setMessage(Html.fromHtml(familyHistoryMap.formQuestionAnswer(0)));
        }

        // Handle positive button click
        alertDialogBuilder.setPositiveButton(R.string.generic_yes, (dialog, which) -> {
            dialog.dismiss();
            onFabClick();
        });

        // Handle negative button click
        alertDialogBuilder.setNegativeButton(R.string.generic_back, ((dialog, which) -> dialog.dismiss()));
        Dialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void onFabClick() {
        if (familyHistoryMap.anySubSelected()) {
            for (Node node : familyHistoryMap.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = node.generateLanguage();
                    String toInsert = node.getText() + " : " + familyString;
                    toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + ".<br/>";
                    insertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < insertionList.size(); i++) {
            if (i == 0) {
                insertion = Node.bullet + insertionList.get(i);
            } else {
                insertion = insertion + " " + Node.bullet + insertionList.get(i);
            }
        }

        insertion = insertion.replaceAll("null.", "");

        List<String> imagePathList = familyHistoryMap.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }


        if (intentTag != null && intentTag.equals("edit")) {
            updateDatabase(insertion);

            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("patientFirstName", patientFName);
            intent.putExtra("patientLastName", patientLName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("tag", intentTag);
            intent.putExtra("hasPrescription", "false");
            startActivity(intent);
        } else {

            if (flag == true) {
                // only if OK clicked, collect this new info (old patient)
                if (insertion.length() > 0) {
                    fhistory = fhistory + insertion;
                } else {
                    fhistory = fhistory + "";
                }
                insertDb(fhistory);
            } else {
                insertDb(insertion); // new details of family history
            }

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
            intent.putExtra("patientFirstName", patientFName);
            intent.putExtra("patientLastName", patientLName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
            intent.putExtra("tag", intentTag);
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }


    }

    public boolean insertDb(String value) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(value));
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

    private void updateDatabase(String string) {

        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB));

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
        triggerConfirmation();
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

    private String getUpdateTranslations(String text) {
        text = text
                .replace("High BP", "उच्च रक्तदाब")
                .replace("Heart Disease", "हृदयरोग")
                .replace("Stroke", "अर्धांगवायू")
                .replace("Diabetes", "मधुमेह")
                .replace("Asthma", "दमा")
                .replace("Tuberculosis", "क्षयरोग")
                .replace("Jaundice", "काविळ")
                .replace("Cancer", "कर्करोग")
                .replace("Other", "इतर")
                .replace("Mother", "आई")
                .replace("Father", "वडील")
                .replace("Sister", "बहीण")
                .replace("Brother", "भाऊ")
                .replace("Do you have a family history of any of the following?", "तुमच्या कुटुंबात खालीलपैकी कोणत्याही आजाराचा इतिहास आहे का?");

        return text;
    }

    private void onListClick(View v, int groupPosition, int childPosition) {
        Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
        Node rootNode = familyHistoryMap.getOption(groupPosition);

        if (rootNode.getChoiceType().equals("single") && !rootNode.anySubSelected()) {
            Log.i(TAG, "onChildClick: ");
            clickedNode.toggleSelected();
            if (rootNode.anySubSelected()) {
                rootNode.setSelected(true);
            } else {
                rootNode.setUnselected();
            }
            adapter.notifyDataSetChanged();

            handleSpecialInputType(clickedNode);
            handleSubLevelQuestion(clickedNode, groupPosition, childPosition);
        } else if (rootNode.getChoiceType().equals("single") && rootNode.anySubSelected()) {
            showSingleChoiceAlert();
        } else {
            if (clickedNode.isExcludedFromMultiChoice()) {
                deselectAllOptions(groupPosition);
                clickedNode.setSelected(true);
            } else {
                clickedNode.toggleSelected();
                updateRootSelectionStatus(rootNode);

                handleSpecialInputType(clickedNode);
                handleSubLevelQuestion(clickedNode, groupPosition, childPosition);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void handleSpecialInputType(Node node) {
        if (node.getInputType() != null && !node.getInputType().equals("camera")) {
            Node.handleQuestion(node, FamilyHistoryActivity.this, adapter, null, null);
        }
        if (node.getInputType() != null && node.getInputType().equals("camera")) {
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            Node.handleQuestion(node, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
        }
    }

    private void handleSubLevelQuestion(Node node, int groupPosition, int childPosition) {
        if (!node.isTerminal() && node.isSelected()) {
            Node.subLevelQuestion(node, FamilyHistoryActivity.this, adapter, filePath.toString(), UUID.randomUUID().toString());
        }
    }

    private void showSingleChoiceAlert() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
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
    }

    /*private void deselectAllOptions(int groupPosition) {
        Node rootNode = familyHistoryMap.getOption(groupPosition);
        Log.d(TAG, "kkdeselectAllOptions: rootnode optionslist : "+rootNode.getOptionsList().size());
        for (Node option : rootNode.getOptionsList()) {
            option.setUnselected();
            for (Node subOption : option.getOptionsList()) {
                Log.d(TAG, "kkdeselectAllOptions: option optionslist : "+option.getOptionsList().size());
                subOption.setUnselected();
            }
        }
    }
*/
/*
    private void deselectAllOptions(int groupPosition) {
        Node rootNode = familyHistoryMap.getOption(groupPosition);
        if (rootNode == null) {
            Log.e(TAG, "deselectAllOptions: rootNode is null for groupPosition: " + groupPosition);
            return;
        }

        List<Node> rootOptionsList = rootNode.getOptionsList();
        if (rootOptionsList == null) {
            Log.e(TAG, "deselectAllOptions: rootOptionsList is null for rootNode: " + rootNode);
            return;
        }

        Log.d(TAG, "deselectAllOptions: rootNode optionsList size: " + rootOptionsList.size());
        for (Node option : rootOptionsList) {
            option.setUnselected();
            List<Node> subOptionsList = option.getOptionsList();
            if (subOptionsList != null) {
                Log.d(TAG, "deselectAllOptions: option subOptionsList size: " + subOptionsList.size());
                for (Node subOption : subOptionsList) {
                    subOption.setUnselected();
                }
            } else {
                Log.d(TAG, "deselectAllOptions: subOptionsList is null for option: " + option);
            }
        }

        adapter.notifyDataSetChanged();
    }
*/

    private void deselectAllOptions(int groupPosition) {
        Node rootNode = familyHistoryMap.getOption(groupPosition);
        Log.d(TAG, "kkdeselectAllOptions: rootnode optionslist : " + rootNode.getOptionsList().size());
        for (Node option : rootNode.getOptionsList()) {
            option.setUnselected();
            if (option.getOptionsList() != null) {
                for (Node subOption : option.getOptionsList()) {
                    Log.d(TAG, "kkdeselectAllOptions: option optionslist : " + option.getOptionsList().size());
                    subOption.setUnselected();
                }
            }
        }
    }

    private void updateRootSelectionStatus(Node rootNode) {
        if (rootNode.anySubSelected()) {
            rootNode.setSelected(true);
        } else {
            rootNode.setUnselected();
        }
    }

}



