package app.intelehealth.client.activities.complaintNodeActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.questionNodeActivity.QuestionNodeActivity;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.EncounterDAO;
import app.intelehealth.client.knowledgeEngine.Node;
import app.intelehealth.client.models.dto.EncounterDTO;
import app.intelehealth.client.utilities.FileUtils;
import app.intelehealth.client.utilities.SessionManager;

import app.intelehealth.client.utilities.exception.DAOException;

public class ComplaintNodeActivity extends AppCompatActivity {
    final String TAG = "Complaint Node Activity";

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    SearchView searchView;
    List<Node> complaints;
   // CustomArrayAdapter listAdapter;
   ComplaintNodeListAdapter listAdapter;
    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    EncounterDTO encounterDTO;
    SessionManager sessionManager = null;
    ImageView img_question;
    TextView tv_selectComplaint;
    RecyclerView list_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
        }
        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();

        }

        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterAdultIntials);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL"));
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO","DTOcomp: "+ encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        setTitle(patientName + ": " + getTitle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_node);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this,R.style.ToolbarTheme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        img_question = findViewById(R.id.img_question);
        tv_selectComplaint = findViewById(R.id.tv_selectComplaint);
        list_recyclerView = findViewById(R.id.list_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list_recyclerView.setLayoutManager(linearLayoutManager);
        list_recyclerView.setItemAnimator(new DefaultItemAnimator());


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


        boolean hasLicense = false;
//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
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
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (currentFile != null) {
                    Log.i(TAG, currentFile.toString());
                    Node currentNode = new Node(currentFile);
                    complaints.add(currentNode);
                }
            }
        } else {
            String[] fileNames = new String[0];
            try {
                fileNames = getApplicationContext().getAssets().list("engines");
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            if (fileNames != null) {
                for (String name : fileNames) {
                    String fileLocation = "engines/" + name;
                    currentFile = FileUtils.encodeJSON(this, fileLocation);
                    Node currentNode = new Node(currentFile);
                    complaints.add(currentNode);
                }
            }
        }

      /*  listAdapter = new CustomArrayAdapter(ComplaintNodeActivity.this,
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
        });*/

        listAdapter
                 = new ComplaintNodeListAdapter(this,complaints);
        list_recyclerView.setAdapter(listAdapter);

        img_question.setVisibility(View.VISIBLE);
        tv_selectComplaint.setVisibility(View.VISIBLE);
        list_recyclerView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);

//        animateView(img_question);
////        animateView(tv_selectComplaint);
////        final Handler handler = new Handler();
////        handler.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                bottomUpAnimation(list_recyclerView);
////            }
////        }, 1000);
////        handler.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                animateView(fab);
////            }
////        }, 1000);

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
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                alertDialogBuilder.setTitle(getString(R.string.complaint_dialog_title));
                alertDialogBuilder.setMessage(getString(R.string.complaint_required));
                alertDialogBuilder.setNeutralButton(getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.show();
               // alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                //pb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);
            } else {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
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
                        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
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
                AlertDialog alertDialog = alertDialogBuilder.show();
                //alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
               // pb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                //nb.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);
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
        searchView.setFocusableInTouchMode(true);
        //searchView.setFocusable(true);
        //searchView.requestFocus();

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



    // Animate views and handle their visibility
    private void animateView(View v) {

        v.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        fadeIn.setFillAfter(true);
        v.startAnimation(fadeIn);

    }

    private void bottomUpAnimation(View v) {

        v.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        v.startAnimation(bottomUp);

    }
}
