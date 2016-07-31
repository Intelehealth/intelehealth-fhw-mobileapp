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

import java.util.ArrayList;
import java.util.HashMap;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

public class FamilyHistoryActivity extends AppCompatActivity {

    String LOG_TAG = "Family History Activity";

    String family = "family";

    Long patientID;
    ArrayList<String> physicalExams;

    String mFileName = "famhist.json";

    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    NodeAdapter adapter;
    ExpandableListView familyListView;

    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getLong("patientID", 0);
//        physicalExams = bundle.getStringArrayList("exams");

        //For Testing
        patientID = Long.valueOf("1");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(familyHistoryMap.anySubSelected()){
                    for (Node node : familyHistoryMap.getOptionsList()) {
                        if(node.isSelected()){
                            String familyString = node.generateLanguage();
                            String toInsert = node.text() + " has " + familyString;
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

                long obsId = insertDb(insertion);
                Intent intent = new Intent(FamilyHistoryActivity.this, TableExamActivity.class);
                intent.putExtra("patientID", patientID);
                intent.putStringArrayListExtra("exams", physicalExams);
                startActivity(intent);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        familyHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName));
        familyListView = (ExpandableListView) findViewById(R.id.family_history_expandable_list_view);
        adapter = new NodeAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);

        familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
                clickedNode.toggleSelected();
                Log.d(LOG_TAG, String.valueOf(clickedNode.isSelected()));
                if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
                    familyHistoryMap.getOption(groupPosition).setSelected();
                } else {
                    familyHistoryMap.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                if(!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal()){
                    HelperMethods.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter);
                }

                return false;
            }
        });

        familyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition);

                if(clickedNode.type() != null){
                    HelperMethods.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter);
                }

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    familyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

    }

    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int VISIT_ID = 100; // TODO: Connect the proper VISIT_ID
        final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

        final int CONCEPT_ID = 163188; // RHK FAMILY HISTORY BLURB

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", VISIT_ID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

    private String generateString(HashMap<String, String> stringMap){
        String generated = "";
        for (String s : stringMap.keySet()) {
            generated = generated.concat(stringMap.get(s) + ", ");
        }
        generated = generated.substring(0, generated.length() - 2);
        return generated;
    }


}
