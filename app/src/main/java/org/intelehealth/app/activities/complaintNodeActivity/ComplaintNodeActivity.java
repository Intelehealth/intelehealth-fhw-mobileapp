package org.intelehealth.app.activities.complaintNodeActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.smartcaredoc.app.R;
import org.intelehealth.app.activities.questionNodeActivity.QuestionNodeActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;

import org.intelehealth.app.utilities.exception.DAOException;

import static org.intelehealth.app.database.dao.PatientsDAO.fetch_gender;

public class ComplaintNodeActivity extends AppCompatActivity {
    final String TAG = "Complaint Node Activity";

    String patientUuid;
    String visitUuid;
    String state;
    String patientName, patientFName, patientLName;
    String patientGender;
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
    private float float_ageYear_Month;
    String mgender;
    Button skipBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(ComplaintNodeActivity.this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientFName = intent.getStringExtra("patientFirstName");
            patientLName = intent.getStringExtra("patientLastName");
            patientGender = intent.getStringExtra("gender");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
        }
        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();

        }

        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterAdultIntials);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL"));
        try {
            encounterDTO.setEncounterTime(OneMinutesLate(AppConstants.dateAndTimeUtils.currentDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
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
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        img_question = findViewById(R.id.img_question);
        tv_selectComplaint = findViewById(R.id.tv_selectComplaint);
        list_recyclerView = findViewById(R.id.list_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list_recyclerView.setLayoutManager(linearLayoutManager);
        list_recyclerView.setItemAnimator(new DefaultItemAnimator());
        skipBtn = findViewById(R.id.btn_skip);

        if (intentTag != null && !intentTag.isEmpty() && intentTag.equalsIgnoreCase("edit"))
            skipBtn.setVisibility(View.GONE);

        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmComplaints();
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmSkipDialog();
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
            //remove items from complaints array here...
            mgender = fetch_gender(patientUuid);

            for (int i = 0; i < complaints.size(); i++) {
                if (mgender.equalsIgnoreCase("M") &&
                        complaints.get(i).getGender().equalsIgnoreCase("0")) {

                    complaints.get(i).remove(complaints, i);
                    i--;
                } else if (mgender.equalsIgnoreCase("F") &&
                        complaints.get(i).getGender().equalsIgnoreCase("1")) {
                    complaints.get(i).remove(complaints, i);
                    i--;
                }
            }

            for (int i = 0; i < complaints.size(); i++) {
                if (!complaints.get(i).getMin_age().equalsIgnoreCase("") &&
                        !complaints.get(i).getMax_age().equalsIgnoreCase("")) {

                    if (float_ageYear_Month < Float.parseFloat(complaints.get(i).getMin_age().trim())) { //age = 1 , min_age = 5
                        complaints.get(i).remove(complaints, i);
                        i--;
                    }

                    //else if(!optionsList.get(i).getMax_age().equalsIgnoreCase(""))
                    else if (float_ageYear_Month > Float.parseFloat(complaints.get(i).getMax_age())) { //age = 15 , max_age = 10
                        complaints.get(i).remove(complaints, i);
                        i--;
                    }

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
                //remove items from complaints array here...
                mgender = fetch_gender(patientUuid);

                for (int i = 0; i < complaints.size(); i++) {
                    if (mgender.equalsIgnoreCase("M") &&
                            complaints.get(i).getGender().equalsIgnoreCase("0")) {

                        complaints.get(i).remove(complaints, i);
                        i--;
                    } else if (mgender.equalsIgnoreCase("F") &&
                            complaints.get(i).getGender().equalsIgnoreCase("1")) {
                        complaints.get(i).remove(complaints, i);
                        i--;
                    }
                }

                for (int i = 0; i < complaints.size(); i++) {
                    if (!complaints.get(i).getMin_age().equalsIgnoreCase("") &&
                            !complaints.get(i).getMax_age().equalsIgnoreCase("")) {

                        if (float_ageYear_Month < Float.parseFloat(complaints.get(i).getMin_age().trim())) { //age = 1 , min_age = 5
                            complaints.get(i).remove(complaints, i);
                            i--;
                        }

                        //else if(!optionsList.get(i).getMax_age().equalsIgnoreCase(""))
                        else if (float_ageYear_Month > Float.parseFloat(complaints.get(i).getMax_age())) { //age = 15 , max_age = 10
                            complaints.get(i).remove(complaints, i);
                            i--;
                        }

                    }

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

        listAdapter = new ComplaintNodeListAdapter(this, complaints);
        list_recyclerView.setAdapter(listAdapter);
        img_question.setVisibility(View.VISIBLE);
        tv_selectComplaint.setVisibility(View.VISIBLE);
        list_recyclerView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
    }

    public String OneMinutesLate(String timeStamp) throws ParseException {

        long FIVE_MINS_IN_MILLIS = 1 * 60 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();

        return df.format(new Date(time + FIVE_MINS_IN_MILLIS));
    }

    private void confirmSkipDialog() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(ComplaintNodeActivity.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.confirm_button));
        alertDialogBuilder.setMessage(getResources().getString(R.string.confirm_skip_complaint));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.skip), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ComplaintNodeActivity.this, VisitSummaryActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("patientFirstName", patientFName);
                intent.putExtra("patientLastName", patientLName);
                intent.putExtra("gender", patientGender);
                intent.putExtra("tag", "skipComplaint");
                intent.putExtra("hasPrescription", "false");
                startActivity(intent);
            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        Button pb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
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
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(ComplaintNodeActivity.this);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                alertDialogBuilder.setTitle(getResources().getString(R.string.complaint_dialog_title));
                alertDialogBuilder.setMessage(getResources().getString(R.string.complaint_required));
                alertDialogBuilder.setNeutralButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
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
                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
            } else {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                alertDialogBuilder.setTitle(getResources().getString(R.string.complaint_dialog_title));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.list_dialog_complaint, null);
                alertDialogBuilder.setView(convertView);
                ListView listView = convertView.findViewById(R.id.complaint_dialog_list_view);
                listView.setDivider(null);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displaySelection);
                listView.setAdapter(arrayAdapter);
                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
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
                        intent.putExtra("patientFirstName", patientFName);
                        intent.putExtra("patientLastName", patientLName);
                        intent.putExtra("gender", patientGender);
                        intent.putExtra("float_ageYear_Month", float_ageYear_Month);
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
                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
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
