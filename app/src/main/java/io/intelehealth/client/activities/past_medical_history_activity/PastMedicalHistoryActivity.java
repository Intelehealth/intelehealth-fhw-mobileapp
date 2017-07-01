
package io.intelehealth.client.activities.past_medical_history_activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.R;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.activities.custom_expandable_list_adapter.CustomExpandableListAdapter;
import io.intelehealth.client.activities.family_history_activity.FamilyHistoryActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.node.Node;

/**
 * This class updates the medical records of patient based on his past medical history.
 */
public class PastMedicalHistoryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient History Activity";
    String patient = "patient";
    String patientID = "1";
    String visitID;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> physicalExams;
    int lastExpandedPosition = -1;

    String mFileName = "patHist.json";
    String image_Prefix = "MH";
    String imageDir = "Medical History";

//    String mFileName = "DemoHistory.json";

    private static final String TAG = PastMedicalHistoryActivity.class.getSimpleName();

    Node patientHistoryMap;
    CustomExpandableListAdapter adapter;
    ExpandableListView historyListView;

    String patientHistory;String phistory="";

    boolean flag=false;SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
//        patientID = Long.valueOf("1");

        // display pop-up to ask for update, if a returning patient
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         e = sharedPreferences.edit();
         phistory = sharedPreferences.getString("phistory","  ");

        boolean past = sharedPreferences.getBoolean("returning",false);
        if(past)
        {

            AlertDialog.Builder alertdialog = new AlertDialog.Builder(PastMedicalHistoryActivity.this);
            alertdialog.setTitle("Past-Medical History");
            alertdialog.setMessage("Do you want to update details?");
            alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // allow to edit
                    flag = true;
                }
            });
            alertdialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                      // skip
                    flag = false;
                        if (phistory != null && !phistory.isEmpty() && !phistory.equals("null"))
                        {
                            phistory = "    ";
                            insertDb(phistory);}
                    Intent intent =new Intent(PastMedicalHistoryActivity.this,FamilyHistoryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);

                }
            });
            alertdialog.show();

        }


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }


        setTitle(R.string.title_activity_patient_history);
        setTitle(getTitle() + ": " + patientName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    startActivity(intent);
                } else {

                  //  if(patientHistoryMap.anySubSelected()){
                        patientHistory = patientHistoryMap.generateLanguage();

                        if(flag == true) { // only if OK clicked, collect this new info (old patient)
                            phistory = phistory + patientHistory; // only PMH updated
                            e.putString("phistory",phistory);
                            e.putBoolean("returning",true);
                            e.commit();
                            insertDb(phistory);

                            // however, we concat it here to patientHistory and pass it along to FH, not inserting into db
                        }
                        else  // new patient, directly insert into database
                        {
                            insertDb(patientHistory);
                        }

                    Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);

                    }
                }

            });



        patientHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName)); //Load the patient history mind map
        historyListView = (ExpandableListView) findViewById(R.id.patient_history_expandable_list_view);
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

                Log.i(TAG, String.valueOf(clickedNode.isTerminal()));
                if (!clickedNode.isTerminal() && clickedNode.isSelected()) {
                    String imageName = patientID + "_" + visitID + "_" + image_Prefix;
                    String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                    File filePath = new File(baseDir + File.separator + "Patient Images" + File.separator +
                            patientID + File.separator + visitID + File.separator + imageDir);
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
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);
        //TODO: Get the right creator_ID


        final int CONCEPT_ID = 163187; // RHK MEDICAL HISTORY BLURB
        //Eventually will be stored in a separate table

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);
        complaintEntries.put("creator", CREATOR_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }


    private void updateImageDatabase(String imagePath) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        localdb.execSQL("INSERT INTO image_records (patient_id,visit_id,image_path) values("
                + "'" + patientID + "'" + ","
                + visitID + ","
                + "'" + imagePath + "'" +
                ")");
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();

        int conceptID = 163187;
        ContentValues contentValues = new ContentValues();
        contentValues.put("value", string);

        String selection = "patient_id = ? AND visit_id = ? AND concept_id = ?";
        String[] args = {patientID, visitID, String.valueOf(conceptID)};

        localdb.update(
                "obs",
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
                patientHistoryMap.displayImage(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
    }
}

