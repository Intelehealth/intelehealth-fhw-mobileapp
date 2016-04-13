package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComplaintSelectActivity extends AppCompatActivity {


    ExpandableListAdapter listAdapter;
    ExpandableListView expandableListView;
    List<String> listLevelOne;
    HashMap<String, List<String>> listLevelTwo;
    HashMap<String, List<Boolean>> listLevelTwoBool;

    ArrayList<String> categoryList;
    ArrayList<String> complaintList;

    String categoryChosen;
    String complaintChosen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        listAdapter = new ExpandableListAdapter(this, listLevelOne, listLevelTwo);
        expandableListView.setAdapter(listAdapter);
        expandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("Category", Integer.toString(groupPosition));
                Log.d("Complaint", Integer.toString(childPosition));
                String selection = listLevelOne.get(groupPosition);
                List<String> complaintSelection = listLevelTwo.get(selection);
                String message2 = complaintSelection.get(childPosition);
                String message = "Category: " + selection + " - Complaint: " + message2;
                Toast.makeText(ComplaintSelectActivity.this, message, Toast.LENGTH_SHORT).show();

                List<Boolean> workingList = listLevelTwoBool.get(listLevelOne.get(groupPosition));
                workingList.set(childPosition, !workingList.get(childPosition));
                listLevelTwoBool.remove(listLevelOne.get(groupPosition));
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
    }


    private void gatherKnowledge() throws JSONException {

        listLevelOne = new ArrayList<String>();
        listLevelTwo = new HashMap<String, List<String>>();
        listLevelTwoBool = new HashMap<String, List<Boolean>>();
        String raw_json = null;

        try {
            InputStream is = getAssets().open("proto2.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            raw_json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject knowledge = new JSONObject(raw_json);
            JSONArray array_categories = knowledge.getJSONArray("options");
            for (int i = 0; i < array_categories.length(); i++) {
                JSONObject category = array_categories.getJSONObject(i);
                String category_name = category.getString("text");
                listLevelOne.add(category_name);
                JSONArray array_complaints = category.getJSONArray("options");
                List<String> workingArray = new ArrayList<String>();
                List<Boolean> workingBoolArray = new ArrayList<Boolean>();
                for (int j = 0; j < array_complaints.length(); j++) {
                    JSONObject complaint = array_complaints.getJSONObject(j);
                    String complaint_name = complaint.getString("text");
                    workingArray.add(complaint_name);
                    workingBoolArray.add(false);
                }
                listLevelTwo.put(listLevelOne.get(i), workingArray);
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
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(ComplaintSelectActivity.this, ComplaintQuestionsActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }


}
