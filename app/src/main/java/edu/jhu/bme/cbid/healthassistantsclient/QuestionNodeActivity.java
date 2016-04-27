package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
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

    Integer patientID = null;
    Knowledge mKnowledge;
    ExpandableListView questionListView;
    String mFileName = "generic.json";
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
                physicalExams.addAll(parseExams(currentNode));

                if (complaintNumber < complaints.size() - 1) {
                    complaintNumber++;
                    setupQuestions(complaintNumber);
                } else {
                    Intent intent = new Intent(QuestionNodeActivity.this, PatientMedicalHistory.class);
                    intent.putExtra("patientID", patientID);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }
            }
        });

        setupQuestions(complaintNumber);

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
                    //TODO: nth level parsing of nodes
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