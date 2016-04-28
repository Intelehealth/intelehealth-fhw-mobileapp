package edu.jhu.bme.cbid.healthassistantsclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

public class FamilyHistoryActivity extends AppCompatActivity {

    Long patientID;
    ArrayList<String> physicalExams;

    String mFileName = "famhist.json";

    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    NodeAdapter adapter;
    ExpandableListView familyListView;

    HashMap<String, String> familyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getInt("patientID");
//        physicalExams = bundle.getStringArrayList("exams");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(familyHistoryMap.anySubSelected()){
                    for (Node node : familyHistoryMap.getOptionsList()) {
                        familyHistory.put(node.text(), node.generateLanguage());
                    }
                }

                //TODO: insert into DB here

//                Intent intent = new Intent(FamilyHistoryActivity.this, FamilyHistoryActivity.class);
//                intent.putExtra("patientID", patientID);
//                intent.putStringArrayListExtra("exams", physicalExams);
//                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        familyHistory = new HashMap<>();
        familyHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName));
        familyListView = (ExpandableListView) findViewById(R.id.family_history_expandable_list_view);
        adapter = new NodeAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);

        familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
                clickedNode.toggleSelected();
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



}
