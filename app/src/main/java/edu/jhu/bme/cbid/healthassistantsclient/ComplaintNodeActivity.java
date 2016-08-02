package edu.jhu.bme.cbid.healthassistantsclient;

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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Knowledge;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

public class ComplaintNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Complaint Node Activity";

    Long patientID = null;

    ExpandableListView complaintListView;

    Knowledge mKnowledge;
    /**
     * Eventually, the knowledge base will be selected from the Settings Menu.
     * For now, the filename is listed here to be used later.
     */
    String mFileName = "knowledge.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle bundle = getIntent().getExtras();
        patientID = bundle.getLong("patientID", 0);
        Log.d(LOG_TAG, String.valueOf(patientID));

        //For Testing
//        patientID = Long.valueOf("1");


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getLongExtra("patientID", 0);
            Log.v(LOG_TAG, patientID + "");
        }

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

        complaintListView = (ExpandableListView) findViewById(R.id.complaint_expandable_list_view);
        if (complaintListView != null) {
            complaintListView.setVisibility(View.GONE);
        }
        ListView complaintList = (ListView) findViewById(R.id.complaint_list_view);

        mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        final NodeAdapter adapter = new NodeAdapter(this, mKnowledge, this.getClass().getSimpleName());
        complaintListView.setAdapter(adapter);
        complaintListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

        complaintListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView textView = (TextView) v.findViewById(R.id.expandable_list_item);

                mKnowledge.storeSelectedComplaint(textView.getText().toString());

                adapter.notifyDataSetChanged();

                return false;
            }
        });


        final List<Node> complaints = mKnowledge.getmComplaints();
        List<String> complaintTitles = new ArrayList<>();
        for (int i = 0; i < complaints.size(); i++) {
            complaintTitles.add(i,complaints.get(i).text() );
        }
        Log.d(LOG_TAG, complaintTitles.toString());

        if (complaintList != null) {
            complaintList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        }
        complaintList.setClickable(true);

        final subNodeAdapter listAdapter = new subNodeAdapter(ComplaintNodeActivity.this,
                R.layout.list_item_subquestion,
                complaints);

        complaintList.setAdapter(listAdapter);

        complaintList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                complaints.get(position).toggleSelected();
                listAdapter.notifyDataSetChanged();
            }
        });


    }

    public void confirmComplaints() {

        final ArrayList<String> selection = mKnowledge.getSelectedComplaints();

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
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selection);
            listView.setAdapter(arrayAdapter);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(ComplaintNodeActivity.this, QuestionNodeActivity.class);
                    intent.putExtra("patientID", patientID);
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
