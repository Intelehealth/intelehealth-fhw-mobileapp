package io.intelehealth.client;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.objects.Knowledge;
import io.intelehealth.client.objects.Node;

public class ComplaintNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Complaint Node Activity";

    String patientID = "1";
    String visitID;
    String state;
    String patientName;
    String intentTag;

    Knowledge mKnowledge;
    List<Node> complaints;
    //    String mFileName = "knowledge.json";
    String mFileName = "DemoBrain.json";

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
//            Log.v(LOG_TAG, "Patient ID: " + patientID);
//            Log.v(LOG_TAG, "Visit ID: " + visitID);
//            Log.v(LOG_TAG, "Patient Name: " + patientName);
//            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }

        setTitle(patientName + ": " + getTitle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmComplaints();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ListView complaintList = (ListView) findViewById(R.id.complaint_list_view);

        /*
        first get a list of all the files in that folder
        then put the strings into an array list
        each time something is selected, you just add the id to the list, and if it deselected you remove it
        then take the final list of what was selected, and attach as an extra into question node
         */

/*
        ArrayList<String> availableComplaints = new ArrayList<>();
        String[] fileNames = new String[0];
        try {
            fileNames = getApplicationContext().getAssets().list("engines");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String name:fileNames){
            String currentName = name;
            String [] separated = currentName.split("\\.");
            availableComplaints.add(separated[0]);
        }
        if (complaintList != null) {
            complaintList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            complaintList.setClickable(true);
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.list_item_subquestion, availableComplaints);
        complaintList.setAdapter(adapter);

        */



        //mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        complaints = new ArrayList<>();
        String[] fileNames = new String[0];
        try {
            fileNames = getApplicationContext().getAssets().list("engines");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String name:fileNames){
            String fileLocation = "engines/" + name;
            JSONObject currentFile = HelperMethods.encodeJSON(this, fileLocation);
            Node currentNode = new Node(currentFile);
            complaints.add(currentNode);
        }


        if (complaintList != null) {
            complaintList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            complaintList.setClickable(true);
        }

        final CustomArrayAdapter listAdapter = new CustomArrayAdapter(ComplaintNodeActivity.this,
                R.layout.list_item_subquestion,
                complaints);

        assert complaintList != null;
        complaintList.setAdapter(listAdapter);


        complaintList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                complaints.get(position).toggleSelected();
                listAdapter.notifyDataSetChanged();
                //The adapter needs to be notified every time a node is clicked to ensure proper display of selected nodes.
            }
        });


    }

    /**
     * Method to confirm all the complaints that were selected, and ensure that the conversation with the patient is thorough.
     */
    public void confirmComplaints() {

        final ArrayList<String> selection = new ArrayList<>();
        for(Node node:complaints){
            if(node.isSelected()){
                selection.add(node.getText());
            }
        }

        if (selection.isEmpty()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.complaint_dialog_title);
            alertDialogBuilder.setMessage(R.string.complaint_required);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.complaint_dialog_title);
            final LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.list_dialog_complaint, null);
            alertDialogBuilder.setView(convertView);
            ListView listView = (ListView) convertView.findViewById(R.id.complaint_dialog_list_view);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selection);
            listView.setAdapter(arrayAdapter);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(ComplaintNodeActivity.this, QuestionNodeActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);

                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("complaints", selection);
                    startActivity(intent);
                }
            });
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.complaint_change_selected), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

}
