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
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Knowledge;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;


public class QuestionNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Question Node Activity";

    int lastExpandedPosition = -1;


    Long patientID = null;
    String patientStatus;
    String intentTag;

    Knowledge mKnowledge; //Knowledge engine
    ExpandableListView questionListView;
    String mFileName = "knowledge.json"; //knowledge engine file
    int complaintNumber = 0; //assuming there is at least one complaint, starting complaint number
    HashMap<String, String> complaintDetails; //temporary storage of complaint findings
    ArrayList<String> complaints; //list of complaints going to be used
    List<Node> complaintsNodes; //actual nodes to be used
    ArrayList<String> physicalExams;
    Node currentNode;
    CustomExpandableListAdapter adapter;
    boolean nodeComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
//        patientID = Long.valueOf("1");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getLongExtra("patientID", 1);
            patientStatus = intent.getStringExtra("status");
            intentTag = intent.getStringExtra("tag");
            complaints = intent.getStringArrayListExtra("complaints");
            Log.v(LOG_TAG, "Patient ID: " + patientID);
            Log.v(LOG_TAG, "Status: " + patientStatus);
            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }

        complaintDetails = new HashMap<>();
        physicalExams = new ArrayList<>();

        mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        complaintsNodes = new ArrayList<>();
        for (int i = 0; i < complaints.size(); i++) {
            complaintsNodes.add(mKnowledge.getComplaint(complaints.get(i)));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        questionListView = (ExpandableListView) findViewById(R.id.complaint_question_expandable_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }
        });

        setupQuestions(complaintNumber);
        //In the event there is more than one complaint, they will be prompted one at a time.

        questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Node question = currentNode.getOption(groupPosition).getOption(childPosition);
                question.toggleSelected();
                if (currentNode.getOption(groupPosition).anySubSelected()) {
                    currentNode.getOption(groupPosition).setSelected();
                } else {
                    currentNode.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                if (!question.getInputType().isEmpty()) {
                    HelperMethods.handleQuestion(question, QuestionNodeActivity.this, adapter);
                    //If there is an input type, then the question has a special method of data entry.
                }

                if (!question.isTerminal()) {
                    HelperMethods.subLevelQuestion(question, QuestionNodeActivity.this, adapter);
                    //If the node is not terminal, that means there are more questions to be asked for this branch.
                }
                return false;

            }
        });

        //Not a perfect method, but closes all other questions when a new one is clicked.
        //Expandable Lists in Android are broken, so this is a band-aid fix.
        questionListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    questionListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

    }

    /**
     * Summarizes the information of the current complaint node.
     * Then has that put into the database, and then checks to see if there are more complaint nodes.
     * If there are more, presents the user with the next set of questions.
     * All exams are also stored into a string, which will be passed through the activities to the Physical Exam Activity.
     */
    private void fabClick(){
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            if (!currentNode.getOption(i).isSelected()) {
                nodeComplete = false;
                questionListView.expandGroup(i);
                break;
            } else {
                nodeComplete = true;
            }
        }

        if (!nodeComplete) {
            questionsMissing();
        } else if (nodeComplete) {

            ArrayList<String> selectedAssociations = currentNode.getSelectedAssociations();
            for (int i = 0; i < selectedAssociations.size(); i++) {
                if (!complaints.contains(selectedAssociations.get(i))) {
                    complaints.add(selectedAssociations.get(i));
                    complaintsNodes.add(mKnowledge.getComplaint(selectedAssociations.get(i)));
                }
            }
            String complaintString = currentNode.generateLanguage();
            String complaint = currentNode.getText();
            complaintDetails.put(complaint, complaintString);

            String insertion = complaint + ": " + complaintString;

            insertDb(insertion);

            physicalExams.addAll(parseExams(currentNode));

            if (complaintNumber < complaints.size() - 1) {
                complaintNumber++;
                setupQuestions(complaintNumber);
            } else {
                if (intentTag.equals("edit")) {
                    Intent intent = new Intent(QuestionNodeActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(QuestionNodeActivity.this, PatientHistoryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }
            }

        }

    }

    /**
     * Insert into DB could be made into a Helper Method, but isn't because there are specific concept IDs used each time.
     * Although this could also be made into a function, for now it has now been.
     * @param value String to put into DB
     * @return DB Row number, never used
     */
    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int VISIT_ID = 100;
        final int CREATOR_ID = 42;

        final int CONCEPT_ID = 163186; //OpenMRS complaint concept ID

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", VISIT_ID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

    /**
     * Sets up the complaint node's questions.
     * @param complaintIndex Index of complaint being displayed to user.
     */
    private void setupQuestions(int complaintIndex) {
        nodeComplete = false;
        currentNode = mKnowledge.getComplaint(complaints.get(complaintIndex));
        adapter = new CustomExpandableListAdapter(this, currentNode, this.getClass().getSimpleName());
        questionListView.setAdapter(adapter);
        questionListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        questionListView.expandGroup(0);
        setTitle(currentNode.getText());
    }

    //Dialog Alert forcing user to answer all questions.
    //Can be removed if necessary
    //TODO: Add setting to allow for all questions unrequired.
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


    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getExams();
        String[] splitExams = rawExams.split(";");
        examList.addAll(Arrays.asList(splitExams));
        return examList;
    }


}