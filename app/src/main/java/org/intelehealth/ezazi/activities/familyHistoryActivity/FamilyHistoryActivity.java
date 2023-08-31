package org.intelehealth.ezazi.activities.familyHistoryActivity;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ImagesDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.knowledgeEngine.Node;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;

import org.intelehealth.ezazi.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.ezazi.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.ezazi.utilities.pageindicator.ScrollingPagerIndicator;

public class FamilyHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
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
    String stage1Hr1_1_EncounterUuid;

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
          //  encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_FamHist = intent.getStringExtra("edit_FamHist");
//            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
//            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");

            stage1Hr1_1_EncounterUuid = intent.getStringExtra("Stage1_Hr1_1_En");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);

            //TODO: temporary..
//            if(edit_FamHist == null)
//                new_result = getFamilyHistoryVisitData();
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
//            Log.v(TAG, new_result); //TODO: temporary..
//            textView.setText(Html.fromHtml(new_result));


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
        recyclerViewIndicator=findViewById(R.id.recyclerViewIndicator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        family_history_recyclerView = findViewById(R.id.family_history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        family_history_recyclerView.setLayoutManager(linearLayoutManager);
        family_history_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(family_history_recyclerView);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
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
           // Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
            // TODO: For now commented Physical Exam screen....17-May-22
            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
//            intent.putExtra("encounterUuidVitals", encounterVitals);
//            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
//            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
            intent.putExtra("tag", intentTag);
            intent.putExtra("Stage1_Hr1_1_En", stage1Hr1_1_EncounterUuid);

            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }


    }

    public boolean insertDb(String value) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
//        obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
//        obsDTO.setEncounteruuid(encounterAdultIntials);

        obsDTO.setConceptuuid(UuidDictionary.PAIN_RELIEF);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
       // obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(value));
        obsDTO.setValue("PAIN RELIEF");
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //1
        obsDTO.setConceptuuid(UuidDictionary.ORAL_FLUID);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("ORAL FLUID"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //2
        obsDTO.setConceptuuid(UuidDictionary.POSTURE);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("POSTURE"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //3
        obsDTO.setConceptuuid(UuidDictionary.BASELINE_FHR);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("123"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //4
        obsDTO.setConceptuuid(UuidDictionary.FHR_DECELERATION);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("FHR"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //5
        obsDTO.setConceptuuid(UuidDictionary.AMNIOTIC_FLUID);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("AMNIOTIC"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //6
        obsDTO.setConceptuuid(UuidDictionary.FETAL_POSITION);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("FETAL POS"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //7
        obsDTO.setConceptuuid(UuidDictionary.CAPUT);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("CAPUT"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //8
        obsDTO.setConceptuuid(UuidDictionary.MOULDING);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("MOULDING"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //9
        obsDTO.setConceptuuid(UuidDictionary.PULSE);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("999"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //10
        obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("888"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //11
        obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("777"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //12
        obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("40"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //13
        obsDTO.setConceptuuid(UuidDictionary.URINE_PROTEIN);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("URINE"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //14
        obsDTO.setConceptuuid(UuidDictionary.CONTRACTIONS_PER_10MIN);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("111"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //15
        obsDTO.setConceptuuid(UuidDictionary.DURATION_OF_CONTRACTION);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("222"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //16
        obsDTO.setConceptuuid(UuidDictionary.CERVIX_PLOT_X);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("CERVIX"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //17
        obsDTO.setConceptuuid(UuidDictionary.DESCENT_PLOT_0);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("333"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //18
        obsDTO.setConceptuuid(UuidDictionary.OXYTOCIN_UL_DROPS_MIN);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("OXYTOCIN"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //19
        obsDTO.setConceptuuid(UuidDictionary.MEDICINE);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("medicine"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //20
        obsDTO.setConceptuuid(UuidDictionary.IV_FLUIDS);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("iv fluids"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //21
        obsDTO.setConceptuuid(UuidDictionary.ASSESSMENT);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("assessment"));
        isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //22
        obsDTO.setConceptuuid(UuidDictionary.PLAN);
        obsDTO.setEncounteruuid(stage1Hr1_1_EncounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(org.intelehealth.ezazi.utilities.StringUtils.getValue("plan"));
        isInserted = false;
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
        onFabClick();

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


