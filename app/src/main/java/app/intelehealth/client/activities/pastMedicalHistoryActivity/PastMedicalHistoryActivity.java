package app.intelehealth.client.activities.pastMedicalHistoryActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ExpandableListView;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.questionNodeActivity.QuestionsAdapter;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.EncounterDAO;
import app.intelehealth.client.database.dao.ImagesDAO;
import app.intelehealth.client.database.dao.ObsDAO;
import app.intelehealth.client.knowledgeEngine.Node;
import app.intelehealth.client.models.dto.ObsDTO;
import app.intelehealth.client.utilities.FileUtils;
import app.intelehealth.client.utilities.RecyclerViewIndicator;
import app.intelehealth.client.utilities.SessionManager;
import app.intelehealth.client.utilities.StringUtils;
import app.intelehealth.client.utilities.UuidDictionary;

import app.intelehealth.client.activities.familyHistoryActivity.FamilyHistoryActivity;
import app.intelehealth.client.activities.physcialExamActivity.CustomExpandableListAdapter;
import app.intelehealth.client.activities.visitSummaryActivity.VisitSummaryActivity;
import app.intelehealth.client.utilities.exception.DAOException;

public class PastMedicalHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {

    String patient = "patient";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> physicalExams;
    int lastExpandedPosition = -1;

    String mFileName = "patHist.json";
    String image_Prefix = "MH";
    String imageDir = "Medical History";
    String imageName;
    File filePath;

    SQLiteDatabase localdb;

    boolean hasLicense = false;

//  String mFileName = "DemoHistory.json";

    private static final String TAG = PastMedicalHistoryActivity.class.getSimpleName();

    Node patientHistoryMap;
    // CustomExpandableListAdapter adapter;
    //ExpandableListView historyListView;

    String patientHistory;
    String phistory = "";

    boolean flag = false;

    SessionManager sessionManager = null;
    private String encounterVitals;
    private String encounterAdultIntials;
    RecyclerView pastMedical_recyclerView;
    QuestionsAdapter adapter;
    RecyclerViewIndicator recyclerViewIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        e = sharedPreferences.edit();

        boolean past = sessionManager.isReturning();
        if (past) {
            MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);

            //AlertDialog.Builder alertdialog = new AlertDialog.Builder(PastMedicalHistoryActivity.this,R.style.AlertDialogStyle);
            alertdialog.setTitle(getString(R.string.title_activity_patient_history));
            alertdialog.setMessage(getString(R.string.question_update_details));
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
                        String[] medHistArgs = {encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                        Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
                        medHistCursor.moveToLast();
                        phistory = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                        medHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        phistory = ""; // if medical history does not exist
                    }

                    // skip
                    flag = false;
                    if (phistory != null && !phistory.isEmpty() && !phistory.equals("null")) {
                        insertDb(phistory);
                    }

                    Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    //    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);

                }
            });
            Dialog builderDialog = alertdialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, builderDialog);

        }

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
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

        recyclerViewIndicator=findViewById(R.id.recyclerViewIndicator);
        pastMedical_recyclerView = findViewById(R.id.pastMedical_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
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
                Crashlytics.getInstance().core.logException(e);
            }
        } else {
            patientHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the patient history mind map
        }

       /* historyListView = findViewById(R.id.patient_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, patientHistoryMap, this.getClass().getSimpleName()); //The adapter might change depending on the activity.
        historyListView.setAdapter(adapter);*/

        adapter = new QuestionsAdapter(this, patientHistoryMap, pastMedical_recyclerView, this.getClass().getSimpleName(), this, false);
        pastMedical_recyclerView.setAdapter(adapter);

        recyclerViewIndicator.setRecyclerView(pastMedical_recyclerView);



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
        Node clickedNode = patientHistoryMap.getOption(groupPosition).getOption(childPosition);
        clickedNode.toggleSelected();

        //Nodes and the expandable list act funny, so if anything is clicked, a lot of stuff needs to be updated.
        if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
            patientHistoryMap.getOption(groupPosition).setSelected();
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

    }


    private void fabClick() {
        //If nothing is selected, there is nothing to put into the database.

        List<String> imagePathList = patientHistoryMap.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }


        if (intentTag != null && intentTag.equals("edit")) {
            if (patientHistoryMap.anySubSelected()) {
                patientHistory = patientHistoryMap.generateLanguage();
                updateDatabase(patientHistory); // update details of patient's visit, when edit button on VisitSummary is pressed
            }

            // displaying all values in another activity
            Intent intent = new Intent(PastMedicalHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("hasPrescription", "false");
            startActivity(intent);
        } else {

            //  if(patientHistoryMap.anySubSelected()){
            patientHistory = patientHistoryMap.generateLanguage();

            if (flag == true) { // only if OK clicked, collect this new info (old patient)
                phistory = phistory + patientHistory; // only PMH updated
                sessionManager.setReturning(true);


                insertDb(phistory);

                // however, we concat it here to patientHistory and pass it along to FH, not inserting into db
            } else  // new patient, directly insert into database
            {
                insertDb(patientHistory);
            }

            Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            //       intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);

        }
    }


    /**
     * This method inserts medical history of patient in database.
     *
     * @param value variable of type String
     * @return long
     */
    public boolean insertDb(String value) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }

        return isInserted;
    }


    private void updateImageDatabase(String imagePath) {

        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string) {

        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB));

            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            Crashlytics.getInstance().core.logException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
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
}

