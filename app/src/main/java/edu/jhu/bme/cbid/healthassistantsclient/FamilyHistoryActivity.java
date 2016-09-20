package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

import java.util.ArrayList;

public class FamilyHistoryActivity extends AppCompatActivity {

    String LOG_TAG = "Family History Activity";

    String patientID = "1";
    String visitID;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> physicalExams;

    String mFileName = "famHist.json";
//    String mFileName = "famHistDemo.json";

    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    CustomExpandableListAdapter adapter;
    ExpandableListView familyListView;

    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
//        patientID = Long.valueOf("1");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along
//            Log.v(LOG_TAG, "Patient ID: " + patientID);
//            Log.v(LOG_TAG, "Visit ID: " + visitID);
//            Log.v(LOG_TAG, "Patient Name: " + patientName);
//            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        familyHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName)); //Load the family history mind map
        familyListView = (ExpandableListView) findViewById(R.id.family_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);

        familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
                clickedNode.toggleSelected();
                //Log.d(LOG_TAG, String.valueOf(clickedNode.isSelected()));
                if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
                    familyHistoryMap.getOption(groupPosition).setSelected();
                } else {
                    familyHistoryMap.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                if(!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal()){
                    Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter);
                }

                return false;
            }
        });

        familyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition);

                if(clickedNode.getInputType() != null){
                    Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter);
                }

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    familyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

    }

    /*
        Language here works funny.
        The architecture for the language of the family history mind map needs to be modified, as it does not allow for nice sentence building.
        It also has a weird thing with new line characters, and just the way that the language itself should be displayed.
     */
    private void onFabClick(){
        if(familyHistoryMap.anySubSelected()){
            for (Node node : familyHistoryMap.getOptionsList()) {
                if(node.isSelected()){
                    String familyString = node.generateLanguage();
                    String toInsert = node.getText() + " has " + familyString;
                    insertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < insertionList.size(); i++) {
            if (i == 0){
                insertion = insertionList.get(i);
            } else {
                insertion = insertion + "; " + insertionList.get(i);
            }
        }

        insertDb(insertion);

        if (intentTag.equals("edit")){
            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            startActivity(intent);
        } else {

            Intent intent = new Intent(FamilyHistoryActivity.this, TableExamActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }


    }

    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

        final int CONCEPT_ID = 163188; // RHK FAMILY HISTORY BLURB

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);
        complaintEntries.put("creator", CREATOR_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

}
