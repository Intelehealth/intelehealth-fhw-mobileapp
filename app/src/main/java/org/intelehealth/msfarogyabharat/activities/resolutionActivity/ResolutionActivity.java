package org.intelehealth.msfarogyabharat.activities.resolutionActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.familyHistoryActivity.FamilyHistoryActivity;
import org.intelehealth.msfarogyabharat.activities.homeActivity.HomeActivity;
import org.intelehealth.msfarogyabharat.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.msfarogyabharat.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.dao.EncounterDAO;
import org.intelehealth.msfarogyabharat.database.dao.ImagesDAO;
import org.intelehealth.msfarogyabharat.database.dao.ObsDAO;
import org.intelehealth.msfarogyabharat.database.dao.PatientsDAO;
import org.intelehealth.msfarogyabharat.database.dao.VisitsDAO;
import org.intelehealth.msfarogyabharat.knowledgeEngine.Node;
import org.intelehealth.msfarogyabharat.models.dto.ObsDTO;
import org.intelehealth.msfarogyabharat.syncModule.SyncUtils;
import org.intelehealth.msfarogyabharat.utilities.Base64Utils;
import org.intelehealth.msfarogyabharat.utilities.FileUtils;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.utilities.UuidDictionary;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;
import org.intelehealth.msfarogyabharat.utilities.multipleSelectionSpinner.Item;
import org.intelehealth.msfarogyabharat.utilities.multipleSelectionSpinner.MultiSelectionSpinner;
import org.intelehealth.msfarogyabharat.utilities.pageindicator.ScrollingPagerIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResolutionActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {

    String patient = "patient";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;

    ArrayList<String> physicalExams;
    int lastExpandedPosition = -1;

    static final String RESOLUTION_DOMESTIC_VIOLANCE = "resolution/Domestic Violence-Resolution and Feedback.json";
    static final String RESOLUTION_DOMESTIC_VIOLANCE_PATH = "Domestic Violence-Resolution and Feedback.json";
    static final String RESOLUTION_SAFE_ABORTION = "resolution/Safe abortion- Resolution and feedback.json";
    static final String RESOLUTION_SAFE_ABORTION_PATH = "Safe abortion- Resolution and feedback.json";
    String image_Prefix = "MH";
    String imageDir = "Medical History";
    String imageName;
    File filePath;

    SQLiteDatabase localdb, db;
    String mgender;

    boolean hasLicense = false;
    String edit_PatHist = "";

    private static final String TAG = ResolutionActivity.class.getSimpleName();
    Node patientHistoryMap;
//    String patientHistory;
    String phistory = "";

    boolean flag = false;

    SessionManager sessionManager = null;
    private String encounterVitals;
    private String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    RecyclerView pastMedical_recyclerView;
    QuestionsAdapter adapter;
    ScrollingPagerIndicator recyclerViewIndicator;
    String new_result;
    AutoCompleteTextView autocompleteState, autocompleteDistrict;
    MultiSelectionSpinner mFacilitySelection;
    JSONArray mFacilityArray = new JSONArray();
    List<String> districtList;
    ArrayList<Item> mFacilityList;
    TextView txtViewFacility;
    String mState = "", mDistrict = "", mFacilityValue = "";
    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_PatHist = intent.getStringExtra("edit_PatHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
//            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);

            if (edit_PatHist == null)
                new_result = getPastMedicalVisitData();
        }

        boolean past = sessionManager.isReturning();
        if (past && edit_PatHist == null) {
            MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
            alertdialog.setTitle(getString(R.string.question_update_details));
            View layoutInflater = LayoutInflater.from(ResolutionActivity.this)
                    .inflate(R.layout.past_fam_hist_previous_details, null);
            alertdialog.setView(layoutInflater);
            TextView textView = layoutInflater.findViewById(R.id.textview_details);
            Log.v(TAG, new_result);
            textView.setText(Html.fromHtml(new_result));


//            alertdialog.setMessage(getString(R.string.question_update_details));
            alertdialog.setPositiveButton(getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // allow to edit
                    flag = true;
                }
            });
            alertdialog.setNegativeButton(getString(R.string.generic_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String[] columns = {"value", " conceptuuid"};
                    try {
                        String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                        String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                        Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
                        medHistCursor.moveToLast();
                        phistory = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                        medHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        phistory = ""; // if medical history does not exist
                    }

                    // skip
                    flag = false;
                    if (phistory != null && !phistory.isEmpty() && !phistory.equals("null")) {
                        insertDb(phistory);
                    }

                    Intent intent = new Intent(ResolutionActivity.this, FamilyHistoryActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    //    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);

                }
            });
            AlertDialog alertDialog = alertdialog.create();
            alertDialog.show();

            Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
            pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
            nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

        }

        setTitle(getString(R.string.give_resolution));
        setTitle(getTitle() + ": " + patientName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_medical_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        pastMedical_recyclerView = findViewById(R.id.pastMedical_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        pastMedical_recyclerView.setLayoutManager(linearLayoutManager);
        pastMedical_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(pastMedical_recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }

        });


//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        String resolution = null;
        if (intent.getBooleanExtra("resolutionViolence", false))
            resolution = RESOLUTION_DOMESTIC_VIOLANCE;
        else
            resolution = RESOLUTION_SAFE_ABORTION;

        if (hasLicense) {
            try {
                String resolutionData = null;
                if (intent.getBooleanExtra("resolutionViolence", false))
                    resolutionData = RESOLUTION_DOMESTIC_VIOLANCE_PATH;
                else
                    resolutionData = RESOLUTION_SAFE_ABORTION_PATH;

                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFile(resolutionData, this));
                patientHistoryMap = new Node(currentFile); //Load the patient history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            patientHistoryMap = new Node(FileUtils.encodeJSON(this, resolution)); //Load the patient history mind map
        }

       /* historyListView = findViewById(R.id.patient_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, patientHistoryMap, this.getClass().getSimpleName()); //The adapter might change depending on the activity.
        historyListView.setAdapter(adapter);*/


        mgender = PatientsDAO.fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            patientHistoryMap.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            patientHistoryMap.fetchItem("1");
        }

        // flaoting value of age is passed to Node for comparison...
        patientHistoryMap.fetchAge(float_ageYear_Month);

        adapter = new QuestionsAdapter(this, patientHistoryMap, pastMedical_recyclerView, this.getClass().getSimpleName(),
                this, false);
        pastMedical_recyclerView.setAdapter(adapter);

        recyclerViewIndicator.attachToRecyclerView(pastMedical_recyclerView);



       /* historyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onListClick(v, groupPosition, childPosition);
                return false;
            }
        });

        //Same fix as before, close all other groups when something is clicked.
        historyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    historyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });*/
    }


    private void onListClick(View v, int groupPosition, int childPosition) {
        Node clickedNode = patientHistoryMap.getOption(groupPosition).getOption(childPosition);

        //  clickedNode.toggleSelected();
        // clickedNode.setSelected(true);
        if (clickedNode.isSelected()) {
            clickedNode.setSelected(false);
            adapter.notifyDataSetChanged();
        } else {
            clickedNode.setSelected(true);
            adapter.notifyDataSetChanged();
        }

        // single/multiple choice questions - start
        Node options = patientHistoryMap.getOption(groupPosition);
        if(options.isMultiChoice() && !clickedNode.isExcludedFromMultiChoice()) {
            for (int i = 0; i < options.getOptionsList().size(); i++) {
                Node childnode = options.getOptionsList().get(i);
                if(childnode.isSelected() && childnode.isExcludedFromMultiChoice()) {
                    patientHistoryMap.getOption(groupPosition).getOption(i).setUnselected();
                }
            }
        }

        if(!options.isMultiChoice() || (options.isMultiChoice() &&
                clickedNode.isExcludedFromMultiChoice() && clickedNode.isSelected())) {

            for (int i = 0; i < options.getOptionsList().size(); i++) {
                Node childnode = options.getOptionsList().get(i);
                if(!childnode.getId().equals(clickedNode.getId())) {
                    patientHistoryMap.getOption(groupPosition).getOption(i).setUnselected();
                }
            }
        }
        adapter.notifyDataSetChanged();
        //end

        //Nodes and the expandable list act funny, so if anything is clicked, a lot of stuff needs to be updated.
        if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
            patientHistoryMap.getOption(groupPosition).setSelected(true);
        } else {
            patientHistoryMap.getOption(groupPosition).setUnselected();
        }
        adapter.notifyDataSetChanged();
        //end

        if (clickedNode.isSelected()) {
            if (clickedNode.getInputType() != null) {
                if (!clickedNode.getInputType().equals("camera")) {
                    imageName = UUID.randomUUID().toString();
                    Node.handleQuestion(clickedNode, ResolutionActivity.this, adapter, null, null);
                }
            }

        Log.i(TAG, String.valueOf(clickedNode.isTerminal()));
        if (!clickedNode.isTerminal() && clickedNode.isSelected()) {
            imageName = UUID.randomUUID().toString();

            Node.subLevelQuestion(clickedNode, ResolutionActivity.this, adapter, filePath.toString(), imageName);
        }
    }

    }


    private void fabClick() {

        //facility - start
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(ResolutionActivity.this);
        View convertView = LayoutInflater.from(ResolutionActivity.this).inflate(R.layout.facility_resolution, null);
        dialogBuilder.setView(convertView);

        //views defining...
        txtViewFacility = convertView.findViewById(R.id.txtViewFacility);
        autocompleteState = convertView.findViewById(R.id.autocomplete_state);
        autocompleteDistrict = convertView.findViewById(R.id.autocomplete_district);
        mFacilitySelection = convertView.findViewById(R.id.mFacilitySelection);
        autocompleteDistrict.setEnabled(false);
        districtList = new ArrayList<>();
        mFacilityList = new ArrayList<>();
        mFacilityList.add(new Item(getString(R.string.select), false));

        String[] countries = getResources().getStringArray(R.array.states_india);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(ResolutionActivity.this, android.R.layout.simple_list_item_1, countries);
        autocompleteState.setAdapter(adapter);

        if (autocompleteState.getText().toString().equals("")) {
            autocompleteDistrict.setText("");
            autocompleteDistrict.setEnabled(false);
        }

        //state
        autocompleteState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                autocompleteDistrict.setEnabled(false);
                autocompleteDistrict.setText("");
                txtViewFacility.setVisibility(View.VISIBLE);
                txtViewFacility.setText("");
                txtViewFacility.setHint(getString(R.string.textViewHintFacility));
                mFacilitySelection.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autocompleteDistrict.setEnabled(false);
                autocompleteDistrict.setText("");
                txtViewFacility.setVisibility(View.VISIBLE);
                txtViewFacility.setText("");
                txtViewFacility.setHint(getString(R.string.textViewHintFacility));
                mFacilitySelection.setVisibility(View.GONE);
                mState = "";
                mDistrict = "";
                mFacilityValue = "";
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mFacilitySelection.setItems(mFacilityList);


        autocompleteState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedState = parent.getItemAtPosition(position).toString();
                mState = parent.getItemAtPosition(position).toString();
                if (selectedState.equalsIgnoreCase("") || autocompleteState.getText().equals("") ||
                        selectedState.equalsIgnoreCase("Select State")) {
                    autocompleteDistrict.setText("");
                    autocompleteDistrict.setEnabled(false);
                    mFacilitySelection.setEnabled(false);
                    mFacilitySelection.setClickable(false);
                    mFacilityList.clear();
                    txtViewFacility.setVisibility(View.VISIBLE);
                    mFacilitySelection.setVisibility(View.GONE);
                } else
                    autocompleteDistrict.setEnabled(true);

                txtViewFacility.setVisibility(View.VISIBLE);
                mFacilitySelection.setVisibility(View.GONE);
                mFacilitySelection.setEnabled(false);
                mFacilityList.clear();
                districtList.clear();
                try {
                    json = loadJsonObjectFromAsset("state_district_facility.json");
                    JSONArray stateArray = json.getJSONArray("states");
                    for (int i = 0; i < stateArray.length(); i++) {
                        String state = stateArray.getJSONObject(i).getString("state");
                        if (state.equalsIgnoreCase(selectedState)) {
                            JSONObject districtObj = stateArray.getJSONObject(i);
                            JSONArray districtArray = districtObj.getJSONArray("districts");
                            for (int j = 0; j < districtArray.length(); j++) {
                                String district = districtArray.getJSONObject(j).getString("name");
//                              mFacilityArray=districtArray.getJSONObject(j).getJSONArray("facility");
                                districtList.add(district);
                            }
                            ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(ResolutionActivity.this,
                                    android.R.layout.simple_list_item_1, districtList);
                            autocompleteDistrict.setAdapter(districtAdapter);
                            break;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //state

        //district
        autocompleteDistrict.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtViewFacility.setVisibility(View.VISIBLE);
                txtViewFacility.setText("");
                txtViewFacility.setHint(getString(R.string.textViewHintFacility));
                mFacilitySelection.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtViewFacility.setVisibility(View.VISIBLE);
                txtViewFacility.setText("");
                txtViewFacility.setHint(getString(R.string.textViewHintFacility));
                mFacilitySelection.setVisibility(View.GONE);
                mDistrict = "";
                mFacilityValue = "";
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        autocompleteDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = parent.getItemAtPosition(position).toString();
                mDistrict = parent.getItemAtPosition(position).toString();

                if (selectedDistrict.equalsIgnoreCase("") || autocompleteState.getText().equals("")) {
                    mFacilityList.clear();
                    mFacilitySelection.setClickable(false);
                    mFacilitySelection.setEnabled(false);
                    mFacilitySelection.setVisibility(View.GONE);
                    txtViewFacility.setVisibility(View.VISIBLE);
                } else
                    txtViewFacility.setVisibility(View.GONE);
                mFacilitySelection.setVisibility(View.VISIBLE);
                mFacilitySelection.setEnabled(true);
                mFacilityList.clear();
                try {
                    mFacilityArray = new JSONArray();
                    JSONArray stateArray = json.getJSONArray("states");
                    for (int i = 0; i < stateArray.length(); i++) {
                        String state = stateArray.getJSONObject(i).getString("state");
                        if (state.equalsIgnoreCase(autocompleteState.getText().toString())) {
                            JSONObject districtObj = stateArray.getJSONObject(i);

                            JSONArray districtArray = districtObj.getJSONArray("districts");
                            for (int j = 0; j < districtArray.length(); j++) {
                                String district = districtArray.getJSONObject(j).getString("name");

                                if (district.equalsIgnoreCase(selectedDistrict)) {
                                    Log.d("jgkfdjg", "selectedDistrict" + mFacilityArray);
                                    mFacilityArray = districtArray.getJSONObject(j).getJSONArray("facility");

                                    for (int k = 0; k < mFacilityArray.length(); k++) {
                                        mFacilityList.add(new Item(mFacilityArray.getString(k), false));
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (mFacilityArray.length() != 0 && mFacilityList != null && mFacilityList.size() != 0) {
                        mFacilitySelection.setItems(mFacilityList);

                    } else {
                        mFacilityList.clear();
                        txtViewFacility.setVisibility(View.VISIBLE);
                        txtViewFacility.setText("-");
                        mFacilitySelection.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //district end...


        dialogBuilder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //facility data insert - start
                uploadFacility();
                //facility data insert - end

                //resolution data insert - start...
                //If nothing is selected, there is nothing to put into the database.
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(ResolutionActivity.this);
                //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                String s = patientHistoryMap.generateLanguageResolution();
                alertDialogBuilder.setMessage(Html.fromHtml(phistory + s));
                alertDialogBuilder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        List<String> imagePathList = patientHistoryMap.getImagePathList();

                        if (imagePathList != null) {
                            for (String imagePath : imagePathList) {
                                updateImageDatabase(imagePath);
                            }
                        }

                        if (intentTag != null && intentTag.equals("edit")) {
                            if (patientHistoryMap.anySubSelected()) {
                                String patientHistory = patientHistoryMap.generateLanguage();
                                updateDatabase(patientHistory); // update details of patient's visit, when edit button on VisitSummary is pressed
                            }
                        } else {

                            //  if(patientHistoryMap.anySubSelected()){
                            String patientHistory = patientHistoryMap.generateLanguage();
                            String patientHistoryHindi = patientHistoryMap.generateLanguage("hi");
                            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                                patientHistoryHindi = s;
                            Map<String, String> patientHistoryMap = new HashMap<>();
                            patientHistoryMap.put("en", patientHistory);
                            patientHistoryMap.put("hi", patientHistoryHindi);
                            String patientHistoryJson = new Gson().toJson(patientHistoryMap);

                            if (flag == true) { // only if OK clicked, collect this new info (old patient)
                                phistory = phistory + patientHistory; // only PMH updated
                                sessionManager.setReturning(true);

                               // uploadFacility();
                                insertDb(phistory);

                                // however, we concat it here to patientHistory and pass it along to FH, not inserting into db
                            } else  // new patient, directly insert into database
                            {

                                insertDb(patientHistoryJson); //new patient...
                            }
                        }
                    }
                });
                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.show();
                //alertDialog.show();
                IntelehealthApplication.setAlertDialogCustomTheme(ResolutionActivity.this, alertDialog);
                //resolution data insert - end
            }
        });


        //dialog creation...
        AlertDialog alertDialogfacility = dialogBuilder.create();
        alertDialogfacility.show();
        alertDialogfacility.setCancelable(false);
        alertDialogfacility.setCanceledOnTouchOutside(false);
        IntelehealthApplication.setAlertDialogCustomTheme(ResolutionActivity.this, alertDialogfacility);

        //facility - end
    }


    /**
     * This method inserts medical history of patient in database.
     *
     * @param value variable of type String
     * @return long
     */
    public boolean insertDb(String value) {

        //facility start

        //facility end

        //create encounter
        SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Calendar today = Calendar.getInstance();
        String startDate = startFormat.format(today.getTime());

        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.getGiveResolutionUrl();
        String encoded = new Base64Utils().encoded("admin", "IHUser#1");
        JSONObject body = new JSONObject();
        try {
            body.put("patient", patientUuid);
            body.put("encounterType", UuidDictionary.ENCOUNTER_VISIT_COMPLETE);
            JSONArray providers = new JSONArray();
            JSONObject provider = new JSONObject();
            provider.put("provider", sessionManager.getProviderID());
            provider.put("encounterRole", "73bbb069-9781-4afc-a9d1-54b6b2270e03");
            providers.put(provider);
            body.put("encounterProviders", providers);
            body.put("visit", visitUuid);
            body.put("encounterDatetime", startDate);


            JSONArray obsArr = new JSONArray();
            JSONObject obs = new JSONObject();
//            obs.put("concept", "76aaef14-b022-4cf2-8409-e13424e9dd38");
            obs.put("concept", UuidDictionary.CONCEPT_RESOLUTION);
            obs.put("value", value);
            obsArr.put(obs);
            body.put("obs", obsArr);

            Resolution resolution = new Gson().fromJson(body.toString(), Resolution.class);
            AppConstants.apiInterface.GIVE_RESOLUTION_API_CALL_OBSERVABLE(url, "Basic " + encoded, resolution)
                    .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(ResolutionActivity.this, R.string.give_resolution_success, Toast.LENGTH_LONG).show();
                    endVisit();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }


    private void endVisit() {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //SyncDAO syncDAO = new SyncDAO();
        //syncDAO.pushDataApi();
        new SyncUtils().syncForeground("survey"); //Sync function will work in foreground of app and
        // the Time will be changed for last sync.

//        AppConstants.notificationUtils.DownloadDone(getString(R.string.end_visit_notif), getString(R.string.visit_ended_notif), 3, PatientSurveyActivity.this);

        sessionManager.removeVisitSummary(patientUuid, visitUuid);

        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void updateImageDatabase(String imagePath) {

        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.ENCOUNTER_VISIT_COMPLETE);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.ENCOUNTER_VISIT_COMPLETE));
            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                patientHistoryMap.setImagePath(mCurrentPhotoPath);
                Log.i(TAG, mCurrentPhotoPath);
                patientHistoryMap.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void fabClickedAtEnd() {
        // patientHistoryMap = node;
        fabClick();
    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos, int physExamPos) {
        onListClick(null, groupPos, childPos);
    }


    public void AnimateView(View v) {

        int fadeInDuration = 500; // Configure time values here
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        if (v != null) {
            v.setAnimation(animation);
        }


    }

    public void bottomUpAnimation(View v) {

        if (v != null) {
            v.setVisibility(View.VISIBLE);
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            v.startAnimation(bottomUp);
        }

    }

    private String getPastMedicalVisitData() {
        String result = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // String[] columns = {"value"};
        String[] columns = {"value", " conceptuuid"};
        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
            Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            result = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if medical history does not exist
        }
        db.close();
        return result;
    }

    private void uploadFacility() {
        if (checkFacilityPresentOrNot(encounterAdultIntials, UuidDictionary.Facility)) {
            ObsDTO obsDTO = new ObsDTO();
            ObsDAO obsDAO = new ObsDAO();
            try {
                obsDTO.setConceptuuid(UuidDictionary.Facility);
                obsDTO.setEncounteruuid(encounterAdultIntials); //latest visit encounter.
                obsDTO.setCreator(sessionManager.getCreatorID());

                if (mFacilitySelection.getVisibility() == View.VISIBLE) {
                    mFacilityValue = mFacilitySelection.getSelectedItemsAsString();
                } else {
                    mFacilityValue = mFacilityValue;
                }

                obsDTO.setValue("" + mState + "," + mDistrict + "," + mFacilityValue);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.Facility));
                obsDAO.updateObs(obsDTO);


            } catch (DAOException dao) {
                FirebaseCrashlytics.getInstance().recordException(dao);
            }

            EncounterDAO encounterDAO = new EncounterDAO();
            try {
                encounterDAO.updateEncounterSync("false", encounterAdultIntials);
                encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            List<ObsDTO> obsDTOList = new ArrayList<>();
            obsDTO = new ObsDTO();
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounterAdultIntials);

            if (mFacilitySelection.getVisibility() == View.VISIBLE) {
                mFacilityValue = mFacilitySelection.getSelectedItemsAsString();
            } else {
                mFacilityValue = mFacilityValue;
            }
            obsDTO.setValue("" + mState + "," + mDistrict + "," + mFacilityValue);
            obsDTO.setConceptuuid(UuidDictionary.Facility);
            obsDTOList.add(obsDTO);
            try {
                obsDAO.insertObsToDb(obsDTOList);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    public JSONObject loadJsonObjectFromAsset(String assetName) {
        try {
            String json = loadStringFromAsset(assetName);
            if (json != null)
                return new JSONObject(json);
        } catch (Exception e) {
            Log.e("JsonUtils", e.toString());
        }

        return null;
    }

    private String loadStringFromAsset(String assetName) throws Exception {
        InputStream is = getApplicationContext().getAssets().open(assetName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }

    public boolean checkFacilityPresentOrNot(String encounterId, String conceptId) {
        Log.v("main", "encounter: "+ encounterId);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        boolean valueCount = false;
        Cursor cursorFacility = db.rawQuery("select uuid from tbl_obs where encounteruuid= ? and conceptuuid= ?",
                new String[]{encounterId, conceptId});
        try {
            if (cursorFacility.getCount() > 0) {
                valueCount = true;
            } else {
                valueCount = false;
            }
        } catch (SQLException e) {
        }
        if (cursorFacility != null) {
            cursorFacility.close();
        }
        return valueCount;
    }

}

