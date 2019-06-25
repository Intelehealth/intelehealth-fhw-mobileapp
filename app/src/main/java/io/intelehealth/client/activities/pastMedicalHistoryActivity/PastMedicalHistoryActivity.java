package io.intelehealth.client.activities.pastMedicalHistoryActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.familyHistoryActivity.FamilyHistoryActivity;
import io.intelehealth.client.activities.physcialExamActivity.CustomExpandableListAdapter;
import io.intelehealth.client.activities.visitSummaryActivity.VisitSummaryActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.knowledgeEngine.Node;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.StringUtils;
import io.intelehealth.client.utilities.UuidDictionary;

public class PastMedicalHistoryActivity extends AppCompatActivity {

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

//    String mFileName = "DemoHistory.json";

    private static final String TAG = PastMedicalHistoryActivity.class.getSimpleName();

    Node patientHistoryMap;
    CustomExpandableListAdapter adapter;
    ExpandableListView historyListView;

    String patientHistory;
    String phistory = "";

    boolean flag = false;
    SharedPreferences.Editor e;

    SessionManager sessionManager = null;
    private String encounterVitals;
    private String encounterAdultIntials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        e = sharedPreferences.edit();

        boolean past = sessionManager.isReturning();
        if (past) {

            AlertDialog.Builder alertdialog = new AlertDialog.Builder(PastMedicalHistoryActivity.this);
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
                        String medHistSelection = "encounteruuid = ? AND conceptuuid = ?";
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
            alertdialog.show();

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
            //      physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }


        setTitle(R.string.title_activity_patient_history);
        setTitle(getTitle() + ": " + patientName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_medical_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    startActivity(intent);
                } else {

                    //  if(patientHistoryMap.anySubSelected()){
                    patientHistory = patientHistoryMap.generateLanguage();

                    if (flag == true) { // only if OK clicked, collect this new info (old patient)
                        phistory = phistory + patientHistory; // only PMH updated
                        e.putBoolean("returning", true);
                        e.commit();
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

        });


        if (sharedPreferences.contains("licensekey")) hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                patientHistoryMap = new Node(currentFile); //Load the patient history mind map
            } catch (JSONException e) {
                Crashlytics.logException(e);
            }
        } else {
            patientHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the patient history mind map
        }

        historyListView = findViewById(R.id.patient_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, patientHistoryMap, this.getClass().getSimpleName()); //The adapter might change depending on the activity.
        historyListView.setAdapter(adapter);

        historyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
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
                        Node.handleQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, null, null);
                    }
                }

                Log.i(TAG, String.valueOf(clickedNode.isTerminal()));
                if (!clickedNode.isTerminal() && clickedNode.isSelected()) {
                    imageName = patientUuid + "_" + visitUuid + "_" + image_Prefix;
                    String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                    filePath = new File(baseDir + File.separator + "Patient Images" + File.separator +
                            patientUuid + File.separator + visitUuid + File.separator + imageDir);
                    Node.subLevelQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, filePath.toString(), imageName);
                }

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
        });
    }


    /**
     * This method inserts medical history of patient in database.
     *
     * @param value variable of type String
     * @return long
     */
    public long insertDb(String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);
        //TODO: Get the right creator_ID


        String CONCEPT_ID = UuidDictionary.RHK_MEDICAL_HISTORY_BLURB; // RHK MEDICAL HISTORY BLURB
        //Eventually will be stored in a separate table

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("uuid", UUID.randomUUID().toString());
        complaintEntries.put("encounteruuid", encounterAdultIntials);
        complaintEntries.put("value", StringUtils.getValue(value));
        complaintEntries.put("conceptuuid", CONCEPT_ID);
        complaintEntries.put("creator", CREATOR_ID);


        return localdb.insert("tbl_obs", null, complaintEntries);
    }


    private void updateImageDatabase(String imagePath) {

        localdb.execSQL("INSERT INTO image_records (patient_id,visit_id,image_path,image_type,delete_status) values("
                + "'" + patientUuid + "'" + ","
                + visitUuid + ","
                + "'" + imagePath + "','" + image_Prefix + "'," +
                0 +
                ")");
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string) {
        String conceptID = UuidDictionary.RHK_MEDICAL_HISTORY_BLURB;
        ContentValues contentValues = new ContentValues();
        contentValues.put("value", string);

        String selection = "encounteruuid = ? AND conceptuuid = ?";
        String[] args = {encounterAdultIntials, conceptID};

        localdb.update(
                "tbl_obs",
                contentValues,
                selection,
                args
        );

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
}

