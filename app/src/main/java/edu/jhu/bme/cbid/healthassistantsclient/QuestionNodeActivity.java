package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
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
    Node currentNode;
    NodeAdapter adapter;

    String specialString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getInt("patientID");
//        complaints = bundle.getStringArrayList("complaints");

        complaints = new ArrayList<>();
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
                if (complaintNumber < complaints.size() - 1) {

                    //TODO: Build the string for this specific complaint before moving onto the next one
                    complaintNumber++;
                    setupQuestions(complaintNumber);
                }
            }
        });

        setupQuestions(complaintNumber);

        questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d(LOG_TAG, currentNode.getOption(groupPosition).getOption(childPosition).language());
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
                }

                //nextQuestion(groupPosition);
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

    private void nextQuestion(int groupPosition) {
        if (groupPosition < adapter.getGroupCount() - 1) {
            questionListView.collapseGroup(groupPosition);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            questionListView.expandGroup(groupPosition + 1);
        }
    }

    private void handleQuestion(Node questionNode) {
        String type = questionNode.type();
        switch (type) {
            case "text":
                HelperMethods.askText(questionNode, this);
                break;
            case "date":
                HelperMethods.askDate(questionNode, this);
                break;
            case "location":
                HelperMethods.askLocation(questionNode, this);
                break;
            case "number":
                HelperMethods.askNumber(questionNode, this);
                break;
            case "area":
                HelperMethods.askArea(questionNode, this);
                break;
            case "duration":
                HelperMethods.askDuration(questionNode, this);
                break;
            case "range":
                HelperMethods.askRange(questionNode, this);
                break;
            case "frequency":
                HelperMethods.askFrequency(questionNode, this);
                break;
        }
    }

}
