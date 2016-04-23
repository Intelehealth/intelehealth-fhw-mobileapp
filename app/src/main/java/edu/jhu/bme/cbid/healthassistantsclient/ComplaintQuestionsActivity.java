package edu.jhu.bme.cbid.healthassistantsclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
gather the knowledge from the JSON file --> check the settings to get the proper string
look through the list of selected responses
when i hit a true, grab the questions from that and put it into the questions hashmap
build the same map, but with booleans all set to false
display the questions, and make the title of the toolbar the complaint
onclicklistener so that the answer selected is highlighted, and then record it into the bool map
expand the first question
then collapse that group, expand the next question/group
when collapsing, check to see if its the last question or not
if its the last one, dialog box telling them that the next complaint will now be presented
when all questions complete, dialog box that says processing language
gather all the language and present to the HA to confirm
once confirmed, write the language bits into the obs table?
 */

public class ComplaintQuestionsActivity extends AppCompatActivity {

    public static final String LOG_TAG = "Complaint Question";

    Integer patientID;

    TextView complaintDisplay;
    ExpandableListAdapter listAdapter;
    ExpandableListView expandableListView;

    List<String> listTitles;
    List<String> listQuestions;
    HashMap<String, List<String>> listOptions;
    HashMap<String, List<Boolean>> listOptionsBool;

    JSONArray complaints;
    Integer complaintNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_complaint_questions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        complaintDisplay = (TextView) findViewById(R.id.complaint_question_display);

        Bundle bundle = getIntent().getExtras();
        patientID = bundle.getInt("patientID");
        try {
            complaints = new JSONArray(bundle.getString("complaints"));
            //Log.d("Number of complaints", ((Integer) complaints.length()).toString());
            expandableListView = (ExpandableListView) findViewById(R.id.complaint_questions_expandable);
            setupQuestions(complaintNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                List<Boolean> workingList = listOptionsBool.get(listQuestions.get(groupPosition));
                workingList.set(childPosition, !workingList.get(childPosition));
                listOptionsBool.put(listQuestions.get(groupPosition), workingList);
                v.setSelected(true);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (groupPosition < listQuestions.size() - 1) {
                    expandableListView.collapseGroup(groupPosition);
                    expandableListView.expandGroup(groupPosition + 1);
                }
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (complaintNumber < complaints.length() - 1) {
                    complaintNumber++;
                    try {
                        setupQuestions(complaintNumber);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
    }


    private void setupQuestions(int complaintCount) throws JSONException {
        listTitles = new ArrayList<String>();
        listQuestions = new ArrayList<String>();
        listOptions = new HashMap<String, List<String>>();
        listOptionsBool = new HashMap<String, List<Boolean>>();

        JSONObject current = complaints.getJSONObject(complaintCount);
        listTitles.add(current.getString("text"));
        //Log.d("Title", current.getString("text"));
        JSONArray complaintQuestions = current.getJSONArray("options");
        for (int j = 0; j < complaintQuestions.length(); j++) {
            JSONObject question = complaintQuestions.getJSONObject(j);
            listQuestions.add(question.getString("text"));
            //Log.d("Question", question.getString("text"));

            JSONArray questionOptions = question.optJSONArray("options");
            String daysFieldConfirmation = question.optString("days-field");
            String textFieldConfirmation = question.optString("text-field");

            if (questionOptions != null) {

                List<String> workingArray = new ArrayList<>();
                List<Boolean> workingBoolArray = new ArrayList<>();

                for (int k = 0; k < questionOptions.length(); k++) {
                    JSONObject answerChoice = questionOptions.getJSONObject(k);
                    workingArray.add(answerChoice.getString("text"));
                    //Log.d("Answer Choices", answerChoice.getString("text"));
                    workingBoolArray.add(false);
                    //Log.d("Answer Choice Array", workingArray.toString());
                }
                listOptions.put(listQuestions.get(j), workingArray);
                listOptionsBool.put(listQuestions.get(j), workingBoolArray);
            } else if (!daysFieldConfirmation.isEmpty()) {

                List<String> workingArray = new ArrayList<>();
                List<Boolean> workingBoolArray = new ArrayList<>();

                workingArray.add(getResources().getString(R.string.complaint_days_question));
                workingBoolArray.add(false);
                //Log.d("Days Question", workingArray.toString());
                listOptions.put(listQuestions.get(j), workingArray);
                listOptionsBool.put(listQuestions.get(j), workingBoolArray);
            }

        }

        listAdapter = new ExpandableListAdapter(this, listQuestions, listOptions);
        expandableListView.setAdapter(listAdapter);
        expandableListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        expandableListView.expandGroup(0);
        complaintDisplay.setText(current.getString("text"));

    }
}

