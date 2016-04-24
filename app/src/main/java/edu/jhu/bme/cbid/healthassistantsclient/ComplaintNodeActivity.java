package edu.jhu.bme.cbid.healthassistantsclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Knowledge;

public class ComplaintNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Complaint Node Activity";

    Integer patientID = null;

    ExpandableListView complaintListView;

    Knowledge mKnowledge;
    /**
     * Eventually, the knowledge base will be selected from the Settings Menu.
     * For now, the filename is listed here to be used later.
     */
    String mFileName = "generic.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Bundle bundle = getIntent().getExtras();
        //patientID = bundle.getInt("patientID");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        complaintListView = (ExpandableListView) findViewById(R.id.complaint_expandable_list_view);

        mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        NodeAdapter adapter = new NodeAdapter(this, mKnowledge);
        complaintListView.setAdapter(adapter);
        complaintListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

        complaintListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView textView = (TextView) v.findViewById(R.id.complaint_item);
                textView.setSelected(true);
                ImageView checkMark = (ImageView) v.findViewById(R.id.complaint_item_image);
                checkMark.setImageResource(R.drawable.green_check);
                return false;
            }
        });

    }

}
