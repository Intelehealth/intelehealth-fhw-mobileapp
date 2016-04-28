package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Complaint;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Knowledge;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;


public class QuestionNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Question Node Activity";

    Long patientID = null;
    Knowledge mKnowledge;
    ExpandableListView questionListView;
    String mFileName = "knowledge.json";
    int complaintNumber = 0;
    HashMap<String, String> complaintDetails;
    ArrayList<String> complaints;
    List<Node> complaintsNodes;
    ArrayList<String> physicalExams;
    Node currentNode;
    NodeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getInt("patientID");
//        complaints = bundle.getStringArrayList("complaints");

        complaints = new ArrayList<>();
        complaintDetails = new HashMap<>();
        physicalExams = new ArrayList<>();
        complaints.add("Difficulty in Breathing");

        mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        complaintsNodes = new ArrayList<>();
        for (int i = 0; i < complaints.size(); i++) {
            complaintsNodes.add(mKnowledge.getComplaint(complaints.get(i)));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questionListView = (ExpandableListView) findViewById(R.id.complaint_question_expandable_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (!currentNode.getOption(i).isSelected()) {
                        questionsMissing();
                        questionListView.expandGroup(i);
                        break;
                    }
                }

                ArrayList<String> selectedAssociations = currentNode.getSelectedAssociations();
                for (int i = 0; i < selectedAssociations.size(); i++) {
                    if (!complaints.contains(selectedAssociations.get(i))) {
                        complaints.add(selectedAssociations.get(i));
                        complaintsNodes.add(mKnowledge.getComplaint(selectedAssociations.get(i)));
                    }
                }

                String complaintString = currentNode.generateLanguage();
                String complaint = currentNode.text();
                complaintDetails.put(complaint, complaintString);

                Complaint complaintObj = new Complaint(complaint, complaintString);

                long obsId = insertDb(complaintObj);

                physicalExams.addAll(parseExams(currentNode));

                if (complaintNumber < complaints.size() - 1) {
                    complaintNumber++;
                    setupQuestions(complaintNumber);
                } else {
                    Intent intent = new Intent(QuestionNodeActivity.this, PatientHistoryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }
            }
        });

        setupQuestions(complaintNumber);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getLongExtra("patientID", 0);
            Log.v(LOG_TAG, patientID + "");
        }


        questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                currentNode.getOption(groupPosition).getOption(childPosition).toggleSelected();
                if (currentNode.getOption(groupPosition).anySubSelected()) {
                    currentNode.getOption(groupPosition).setSelected();
                } else {
                    currentNode.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                Node question = currentNode.getOption(groupPosition).getOption(childPosition);
                if (question.type() != null) {
                    handleQuestion(question);
                    adapter.notifyDataSetChanged();
                }

                if (!question.isTerminal()) {

                    adapter.notifyDataSetChanged();
                }

                adapter.notifyDataSetChanged();
                return false;

            }
        });

        questionListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != 0) {
                    questionListView.collapseGroup(groupPosition - 1);
                }
            }
        });

        questionListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });

    }

    private long insertDb(Complaint complaintObj) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int VISIT_ID = 100; // TODO: Connect the proper VISIT_ID
        final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

        final int CONCEPT_ID = 163186; // RHK COMPLAINT


        Gson gson = new Gson();
        String toInsert = gson.toJson(complaintObj);

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

    private void setupQuestions(int complaintIndex) {
        currentNode = mKnowledge.getComplaint(complaints.get(complaintIndex));
        adapter = new NodeAdapter(this, currentNode, this.getClass().getSimpleName());
        questionListView.setAdapter(adapter);
        questionListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        questionListView.expandGroup(0);
        setTitle(currentNode.text());
    }

    public void questionsMissing() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.question_answer_all);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void handleQuestion(Node questionNode) {
        String type = questionNode.type();
        switch (type) {
            case "text":
                HelperMethods.askText(questionNode, this, adapter);
                break;
            case "date":
                HelperMethods.askDate(questionNode, this, adapter);
                break;
            case "location":
                HelperMethods.askLocation(questionNode, this, adapter);
                break;
            case "number":
                HelperMethods.askNumber(questionNode, this, adapter);
                break;
            case "area":
                HelperMethods.askArea(questionNode, this, adapter);
                break;
            case "duration":
                HelperMethods.askDuration(questionNode, this, adapter);
                break;
            case "range":
                HelperMethods.askRange(questionNode, this, adapter);
                break;
            case "frequency":
                HelperMethods.askFrequency(questionNode, this, adapter);
                break;
        }
    }

    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getExams();
        String[] splitExams = rawExams.split(";");
        examList.addAll(Arrays.asList(splitExams));
        return examList;
    }
}