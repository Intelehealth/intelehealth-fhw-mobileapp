package io.intelehealth.client.activities.complaintNodeActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.questionNodeActivity.QuestionNodeActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.database.dao.EncounterDAO;
import io.intelehealth.client.knowledgeEngine.Node;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.exception.DAOException;

public class ComplaintNodeActivity extends AppCompatActivity {
    final String TAG = "Complaint Node Activity";

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;

    SearchView searchView;


    List<Node> complaints;
    String mFileName = "knowledge.json";
    //String mFileName = "DemoBrain.json";

    CustomArrayAdapter listAdapter;
    String encounterVitals;
    String encounterAdultIntials;
    EncounterDTO encounterDTO;
    SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();
        }
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterAdultIntials);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }

        setTitle(patientName + ": " + getTitle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_node);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmComplaints();
            }
        });


        ListView complaintList = findViewById(R.id.complaint_list_view);
        if (complaintList != null) {
            complaintList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            complaintList.setClickable(true);
        }

        complaints = new ArrayList<>();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean hasLicense = false;
//        if (sharedPreferences.contains("licensekey"))
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;
        JSONObject currentFile = null;
        if (hasLicense) {
            File base_dir = new File(getFilesDir().getAbsolutePath() + File.separator + AppConstants.JSON_FOLDER);
            File[] files = base_dir.listFiles();
            for (File file : files) {
                try {
                    currentFile = new JSONObject(FileUtils.readFile(file.getName(), this));
                } catch (JSONException e) {
                    Crashlytics.getInstance().core.logException(e);
                }
                Log.i(TAG, currentFile.toString());
                Node currentNode = new Node(currentFile);
                complaints.add(currentNode);
            }
        } else {
            String[] fileNames = new String[0];
            try {
                fileNames = getApplicationContext().getAssets().list("engines");
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
            }
            for (String name : fileNames) {
                String fileLocation = "engines/" + name;
                currentFile = FileUtils.encodeJSON(this, fileLocation);
                Node currentNode = new Node(currentFile);
                complaints.add(currentNode);
            }
        }

        listAdapter = new CustomArrayAdapter(ComplaintNodeActivity.this,
                R.layout.list_item_subquestion,
                complaints);

        assert complaintList != null;
        complaintList.setAdapter(listAdapter);


        complaintList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                complaints.get(position).toggleSelected();
                listAdapter.notifyDataSetChanged();
                //The adapter needs to be notified every time a knowledgeEngine is clicked to ensure proper display of selected nodes.
            }
        });

    }

    /**
     * Method to confirm all the complaints that were selected, and ensure that the conversation with the patient is thorough.
     */
    public void confirmComplaints() {

        final ArrayList<String> selection = new ArrayList<>();
        final ArrayList<String> displaySelection = new ArrayList<>();
        if (listAdapter != null) {
            for (Node node : listAdapter.getmNodes()) {
                if (node.isSelected()) {
                    selection.add(node.getText());
                    displaySelection.add(node.findDisplay());
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
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);

            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.complaint_dialog_title);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.list_dialog_complaint, null);
                alertDialogBuilder.setView(convertView);
                ListView listView = convertView.findViewById(R.id.complaint_dialog_list_view);
                listView.setDivider(null);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displaySelection);
                listView.setAdapter(arrayAdapter);
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(ComplaintNodeActivity.this, QuestionNodeActivity.class);
                        intent.putExtra("patientUuid", patientUuid);
                        intent.putExtra("visitUuid", visitUuid);
                        intent.putExtra("encounterUuidVitals", encounterVitals);
                        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        if (intentTag != null) {
                            intent.putExtra("tag", intentTag);
                        }
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
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setFocusable(true);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (listAdapter != null) {
                    listAdapter.filter(newText);
                }
                return true;
            }
        });

        return true;
    }


}
