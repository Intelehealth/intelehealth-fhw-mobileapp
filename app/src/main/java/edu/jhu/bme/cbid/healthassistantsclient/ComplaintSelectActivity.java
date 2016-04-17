package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComplaintSelectActivity extends AppCompatActivity {

    Integer patientID = null;

    ExpandableListAdapter listAdapter;
    ExpandableListView expandableListView;
    List<String> listLevelOne;
    HashMap<String, List<String>> listLevelTwo;
    HashMap<String, List<Boolean>> listLevelTwoBool;

    ArrayList<String> categoryList;
    ArrayList<String> complaintList;

    JSONObject knowledge;
    JSONArray arrayCategories;
    JSONArray complaints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Bundle bundle = getIntent().getExtras();
        //patientID = bundle.getInt("patientID");

        setContentView(R.layout.activity_complaint_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        expandableListView = (ExpandableListView) findViewById(R.id.complaint_expandable_list_view);
        try {
            gatherKnowledge();
            //TODO: Add argument to allow for different knowledge "styles"

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //TODO: Add setting to change where knowledge is located

        //Log.d("knowledge", listLevelTwo.toString());


        listAdapter = new ExpandableListAdapter(this, listLevelOne, listLevelTwo);
        expandableListView.setAdapter(listAdapter);
        expandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                List<Boolean> workingList = listLevelTwoBool.get(listLevelOne.get(groupPosition));
                workingList.set(childPosition, !workingList.get(childPosition));
                listLevelTwoBool.put(listLevelOne.get(groupPosition), workingList);

                v.setSelected(true);

                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedComplaint();
            }
        });


        super.onCreate(savedInstanceState);
    }


    private void gatherKnowledge() throws JSONException {

        listLevelOne = new ArrayList<String>();
        listLevelTwo = new HashMap<String, List<String>>();
        listLevelTwoBool = new HashMap<String, List<Boolean>>();
        String raw_json = null;

        try {
            InputStream is = getAssets().open("generic.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            raw_json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            knowledge = new JSONObject(raw_json);
            arrayCategories = knowledge.getJSONArray("options");
            for (int i = 0; i < arrayCategories.length(); i++) {
                JSONObject category = arrayCategories.getJSONObject(i);
                String category_name = category.getString("text");
                listLevelOne.add(category_name);
                //Log.d("Current Category", category_name);
                JSONArray array_complaints = category.getJSONArray("options");
                List<String> workingArray = new ArrayList<String>();
                List<Boolean> workingBoolArray = new ArrayList<Boolean>();
                for (int j = 0; j < array_complaints.length(); j++) {
                    JSONObject complaint = array_complaints.getJSONObject(j);
                    String complaint_name = complaint.getString("text");
                    workingArray.add(complaint_name);
                    //Log.d("Current Complaint", complaint_name);
                    workingBoolArray.add(false);
                }
                //Log.d("working Array", workingArray.toString());
                listLevelTwo.put(listLevelOne.get(i), workingArray);
                //Log.d("list level two", listLevelTwo.toString());
                listLevelTwoBool.put(listLevelOne.get(i), workingBoolArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void selectedComplaint() {

        Integer counter = 0;

        categoryList = new ArrayList<String>();
        complaintList = new ArrayList<String>();

        for (int i = 0; i < listLevelTwoBool.size(); i++) {
            List<Boolean> current = listLevelTwoBool.get(listLevelOne.get(i));

            if (current.contains(true)) {
                counter += 1;
                categoryList.add(listLevelOne.get(i));

                for (int j = 0; j < current.size(); j++) {
                    if (current.get(j)) {
                        complaintList.add(listLevelTwo.get(listLevelOne.get(i)).get(j));
                    }
                }
            }
        }

        if (counter == 0) {
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
            alertDialogBuilder.setMessage(complaintList.toString());
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        gatherComplaints();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(ComplaintSelectActivity.this, ComplaintQuestionsActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("complaints", complaints.toString());
                    //Log.d("complaints", listLevelTwo.toString());
                    //Log.d("selected complaints", listLevelTwoBool.toString());
                    startActivity(intent);
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.complaint_change_selected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

    private void gatherComplaints() throws JSONException {
        complaints = new JSONArray();
        //Log.d("Function", "gather complaints called");
        for (int i = 0; i < listLevelTwoBool.size(); i++) {
            List<Boolean> current = listLevelTwoBool.get(listLevelOne.get(i));
            //Log.d("current", current.toString());
            if (current.contains(true)) {
                JSONObject category = arrayCategories.getJSONObject(i);
                //Log.d("category", category.toString());
                JSONArray categoryOptions = category.getJSONArray("options");
                //Log.d("category options", categoryOptions.toString());
                for (int j = 0; j < current.size(); j++) {
                    if (current.get(j)) {
                        //Log.d("Complaint?", categoryOptions.getJSONObject(j).toString());
                        complaints.put(categoryOptions.getJSONObject(j));
                        //Log.d("complaint", "put successfully");
                    }
                }
            }
        }
    }

}
