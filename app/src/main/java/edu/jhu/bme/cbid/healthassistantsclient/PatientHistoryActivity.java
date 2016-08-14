package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.gson.Gson;

import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

public class PatientHistoryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient History Activity";
    String patient = "patient";


    Long patientID = null;
    String patientStatus;
    String intentTag;

    ArrayList<String> physicalExams;

    int lastExpandedPosition = -1;

    String mFileName = "pathist.json";

    Node patientHistoryMap;
    CustomExpandableListAdapter adapter;
    ExpandableListView historyListView;

    String patientHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
//        patientID = Long.valueOf("1");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getLongExtra("patientID", 1);
            patientStatus = intent.getStringExtra("status");
            intentTag = intent.getStringExtra("tag");
            physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along
            Log.v(LOG_TAG, "Patient ID: " + patientID);
            Log.v(LOG_TAG, "Status: " + patientStatus);
            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }

        setTitle(R.string.title_activity_patient_history);

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
                if(patientHistoryMap.anySubSelected()){
                    patientHistory = patientHistoryMap.generateLanguage();

                    long obsId = insertDb(patientHistory);
                }


                if (intentTag.equals("edit")){
                    Intent intent = new Intent(PatientHistoryActivity.this, VisitSummaryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", intentTag);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(PatientHistoryActivity.this, FamilyHistoryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

                if(!clickedNode.isTerminal()){
                    HelperMethods.subLevelQuestion(clickedNode, PatientHistoryActivity.this, adapter);
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


    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int VISIT_ID = 100;
        final int CREATOR_ID = 42;

        final int CONCEPT_ID = 163187; // RHK MEDICAL HISTORY BLURB


        Gson gson = new Gson();
        String toInsert = gson.toJson(value);

        Log.d(LOG_TAG, toInsert);

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", VISIT_ID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", toInsert);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }


}
