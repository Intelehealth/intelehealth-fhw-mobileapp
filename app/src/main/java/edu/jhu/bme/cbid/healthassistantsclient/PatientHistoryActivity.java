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

    Long patientID;
    ArrayList<String> physicalExams;

    int lastExpandedPosition = -1;

    String mFileName = "pathist.json";

    Node patientHistoryMap;
    NodeAdapter adapter;
    ExpandableListView historyListView;

    String patientHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Bundle bundle = getIntent().getExtras();
        patientID = bundle.getLong("patientID", 1);
        physicalExams = bundle.getStringArrayList("exams");
        Log.d(LOG_TAG, String.valueOf(patientID));


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

                if(patientHistoryMap.anySubSelected()){
                    patientHistory = patientHistoryMap.generateLanguage();

                    long obsId = insertDb(patientHistory);
                }



                Intent intent = new Intent(PatientHistoryActivity.this, FamilyHistoryActivity.class);
                intent.putExtra("patientID", patientID);
                intent.putStringArrayListExtra("exams", physicalExams);
                startActivity(intent);


            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        patientHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName));
        historyListView = (ExpandableListView) findViewById(R.id.patient_history_expandable_list_view);
        adapter = new NodeAdapter(this, patientHistoryMap, this.getClass().getSimpleName());
        historyListView.setAdapter(adapter);

        historyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Node clickedNode = patientHistoryMap.getOption(groupPosition).getOption(childPosition);
                clickedNode.toggleSelected();
                if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
                    patientHistoryMap.getOption(groupPosition).setSelected();
                } else {
                    patientHistoryMap.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                if(!patientHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal()){
                    HelperMethods.subLevelQuestion(clickedNode, PatientHistoryActivity.this, adapter);
                }

                return false;
            }
        });

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

        final int VISIT_ID = 100; // TODO: Connect the proper VISIT_ID
        final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

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
