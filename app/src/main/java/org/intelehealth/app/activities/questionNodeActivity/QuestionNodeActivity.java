package org.intelehealth.app.activities.questionNodeActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import org.intelehealth.app.utilities.CustomLog;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;


import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.shared.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.utilities.pageindicator.ScrollingPagerIndicator;

import static org.intelehealth.app.database.dao.PatientsDAO.fetch_gender;


public class QuestionNodeActivity extends BaseActivity implements QuestionsAdapter.FabClickListener {
    final String TAG = "Question Node Activity";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String patientGender;
    String intentTag;
    String mgender;

    String imageName;
    File filePath;
    Boolean complaintConfirmed = false;
    SessionManager sessionManager = null;
    private float float_ageYear_Month;
    Context context;


    //    Knowledge mKnowledge; //Knowledge engine
    // ExpandableListView questionListView;
    String mFileName = "knowledge.json"; //knowledge engine file
    //    String mFileName = "DemoBrain.json";
    int complaintNumber = 0; //assuming there is at least one complaint, starting complaint number
    HashMap<String, String> complaintDetails; //temporary storage of complaint findings
    ArrayList<String> complaints; //list of complaints going to be used
    List<Node> complaintsNodes; //actual nodes to be used
    ArrayList<String> physicalExams;
    Node currentNode;
    // CustomExpandableListAdapter adapter;
    QuestionsAdapter adapter;
    boolean nodeComplete = false;

    int lastExpandedPosition = -1;
    String insertion = "";
    private SharedPreferences prefs;
    private String encounterVitals;
    private String encounterAdultIntials, EncounterAdultInitial_LatestVisit;

    private List<Node> optionsList = new ArrayList<>();
    Node assoSympNode;
    private JSONObject assoSympObj = new JSONObject();
    private JSONArray assoSympArr = new JSONArray();
    private JSONObject finalAssoSympObj = new JSONObject();
    ScrollingPagerIndicator recyclerViewIndicator;


    FloatingActionButton fab;
    RecyclerView question_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        context = QuestionNodeActivity.this;
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        filePath = new File(AppConstants.IMAGE_PATH);
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            complaints = intent.getStringArrayListExtra("complaints");
        }
        complaintDetails = new HashMap<>();
        physicalExams = new ArrayList<>();
        complaintsNodes = new ArrayList<>();

        boolean hasLicense = false;
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        JSONObject currentFile = null;
        for (int i = 0; i < complaints.size(); i++) {
            if (hasLicense) {
                try {
                    currentFile = new JSONObject(FileUtils.readFile(complaints.get(i) + ".json", this));
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                String fileLocation = "engines/" + complaints.get(i) + ".json";
                currentFile = FileUtils.encodeJSON(this, fileLocation);
            }
            Node currentNode = new Node(currentFile);
            complaintsNodes.add(currentNode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_node);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // questionListView = findViewById(R.id.complaint_question_expandable_list_view);

        fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }
        });
        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        question_recyclerView = findViewById(R.id.question_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        question_recyclerView.setLayoutManager(linearLayoutManager);

        question_recyclerView.setNestedScrollingEnabled(true);
        question_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(question_recyclerView);

        setupQuestions(complaintNumber);
        //In the event there is more than one complaint, they will be prompted one at a time.

 /*       questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                onListClicked(v, groupPosition, childPosition);

                return false;

            }
        });

        //Not a perfect method, but closes all other questions when a new one is clicked.
        //Expandable Lists in Android are broken, so this is a band-aid fix.
        questionListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    questionListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });*/

    }


    public void onListClicked(View v, int groupPosition, int childPosition) {
        CustomLog.e(TAG, "CLICKED: " + currentNode.getOption(groupPosition).toString());
        if ((currentNode.getOption(groupPosition).getChoiceType().equals("single")) && !currentNode.getOption(groupPosition).anySubSelected()) {
            Node question = currentNode.getOption(groupPosition).getOption(childPosition);
            question.toggleSelected();
            if (currentNode.getOption(groupPosition).anySubSelected()) {
                currentNode.getOption(groupPosition).setSelected(true);
            } else {
                currentNode.getOption(groupPosition).setUnselected();
            }


            if (!question.getInputType().isEmpty() && question.isSelected()) {
                if (question.getInputType().equals("camera")) {
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    imageName = UUID.randomUUID().toString();
                    Node.handleQuestion(question, QuestionNodeActivity.this, adapter, filePath.toString(), imageName);
                } else {
                    Node.handleQuestion(question, QuestionNodeActivity.this, adapter, null, null);
                }
            }


            if (!question.isTerminal() && question.isSelected()) {
                Node.subLevelQuestion(question, QuestionNodeActivity.this, adapter, filePath.toString(), imageName);
                //If the knowledgeEngine is not terminal, that means there are more questions to be asked for this branch.
            }
        } else if ((currentNode.getOption(groupPosition).getChoiceType().equals("single")) && currentNode.getOption(groupPosition).anySubSelected()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QuestionNodeActivity.this,R.style.AlertDialogStyle);
            alertDialogBuilder.setMessage(R.string.this_question_only_one_answer);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        } else {

            Node question = currentNode.getOption(groupPosition).getOption(childPosition);
            question.toggleSelected();
            if (currentNode.getOption(groupPosition).anySubSelected()) {
                currentNode.getOption(groupPosition).setSelected(true);
            } else {
                currentNode.getOption(groupPosition).setUnselected();
            }

            if (!currentNode.findDisplay().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS)
                    && !currentNode.findDisplay().equalsIgnoreCase("जुड़े लक्षण")
                    && !currentNode.findDisplay().equalsIgnoreCase("ପେଟଯନ୍ତ୍ରଣା")
                    && !currentNode.findDisplay().equalsIgnoreCase("સંકળાયેલ લક્ષણો")
                    && !currentNode.findDisplay().equalsIgnoreCase("সংশ্লিষ্ট লক্ষণ")

            ) {
                //code added to handle multiple and single option selection.
                Node rootNode = currentNode.getOption(groupPosition);
                if (rootNode.isMultiChoice() && !question.isExcludedFromMultiChoice()) {
                    for (int i = 0; i < rootNode.getOptionsList().size(); i++) {
                        Node childNode = rootNode.getOptionsList().get(i);
                        if (childNode.isSelected() && childNode.isExcludedFromMultiChoice()) {
                            currentNode.getOption(groupPosition).getOptionsList().get(i).setUnselected();
                        }
                    }
                }
                CustomLog.v(TAG, "rootNode - " + new Gson().toJson(rootNode));
                if (!rootNode.isMultiChoice() || (rootNode.isMultiChoice() &&
                        question.isExcludedFromMultiChoice() && question.isSelected())) {
                    for (int i = 0; i < rootNode.getOptionsList().size(); i++) {
                        Node childNode = rootNode.getOptionsList().get(i);
                        if (!childNode.getId().equals(question.getId())) {
                            currentNode.getOption(groupPosition).getOptionsList().get(i).setUnselected();
                        }
                    }
                }
            }

            if (!question.getInputType().isEmpty() && question.isSelected()) {
                if (question.getInputType().equals("camera")) {
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    Node.handleQuestion(question, QuestionNodeActivity.this, adapter, filePath.toString(), imageName);
                } else {
                    Node.handleQuestion(question, QuestionNodeActivity.this, adapter, null, null);
                }
                //If there is an input type, then the question has a special method of data entry.
            }

            if (!question.isTerminal() && question.isSelected()) {
                Node.subLevelQuestion(question, QuestionNodeActivity.this, adapter, filePath.toString(), imageName);
                //If the knowledgeEngine is not terminal, that means there are more questions to be asked for this branch.
            }
        }
        //adapter.updateNode(currentNode);
        adapter.notifyDataSetChanged();

    }

    /**
     * Summarizes the information of the current complaint knowledgeEngine.
     * Then has that put into the database, and then checks to see if there are more complaint nodes.
     * If there are more, presents the user with the next set of questions.
     * All exams are also stored into a string, which will be passed through the activities to the Physical Exam Activity.
     */
    private void fabClick() {
        nodeComplete = true;

        AnswerResult answerResult = currentNode.checkAllRequiredAnswered(context);
        if (!answerResult.result) {
            // show alert dialog
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(answerResult.requiredStrings);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            Dialog alertDialog = alertDialogBuilder.show();
            CustomLog.v(TAG, answerResult.requiredStrings);
            return;
        }


        if (!complaintConfirmed) {
            questionsMissing();
        } else {
            List<String> imagePathList = currentNode.getImagePathList();

            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase(imagePath);
                }
            }

            String complaintString = currentNode.generateLanguage();

            if (complaintString != null && !complaintString.isEmpty()) {
                //     String complaintFormatted = complaintString.replace("?,", "?:");

                String complaint = currentNode.getText();
                //    complaintDetails.put(complaint, complaintFormatted);

//                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
            } else {
                String complaint = currentNode.getText();
                if (!complaint.equalsIgnoreCase(getResources().getString(R.string.associated_symptoms))) {
//                    insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
                    insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
                }
            }
            ArrayList<String> selectedAssociatedComplaintsList = currentNode.getSelectedAssociations();
            if (selectedAssociatedComplaintsList != null && !selectedAssociatedComplaintsList.isEmpty()) {
                for (String associatedComplaint : selectedAssociatedComplaintsList) {
                    if (!complaints.contains(associatedComplaint)) {
                        complaints.add(associatedComplaint);
                        String fileLocation = "engines/" + associatedComplaint + ".json";
                        JSONObject currentFile = FileUtils.encodeJSON(this, fileLocation);
                        Node currentNode = new Node(currentFile);
                        complaintsNodes.add(currentNode);
                    }
                }
            }

            ArrayList<String> childNodeSelectedPhysicalExams = currentNode.getPhysicalExamList();
            if (!childNodeSelectedPhysicalExams.isEmpty())
                physicalExams.addAll(childNodeSelectedPhysicalExams); //For Selected child nodes

            ArrayList<String> rootNodePhysicalExams = parseExams(currentNode);
            if (rootNodePhysicalExams != null && !rootNodePhysicalExams.isEmpty())
                physicalExams.addAll(rootNodePhysicalExams); //For Root Node

            if (complaintNumber < complaints.size() - 1) {
                complaintNumber++;
                setupQuestions(complaintNumber);
                complaintConfirmed = false;
            } else if (complaints.size() >= 1 && complaintNumber == complaints.size() - 1 && !optionsList.isEmpty()) {
                complaintNumber++;
                removeDuplicateSymptoms();
                complaintConfirmed = false;
            } else {
                if (intentTag != null && intentTag.equals("edit")) {
                    CustomLog.i(TAG, "fabClick: update" + insertion);
                    updateDatabase(insertion);
                    Intent intent = new Intent(QuestionNodeActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("gender", patientGender);
                    intent.putExtra("tag", intentTag);

                    Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
                    sessionManager.setVisitSummary(patientUuid, selectedExams);

                    startActivity(intent);
                } else {
                    CustomLog.i(TAG, "fabClick: " + insertion);
                    insertDb(insertion);
                    Intent intent = new Intent
                            (QuestionNodeActivity.this, PastMedicalHistoryActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("gender", patientGender);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
                    sessionManager.setVisitSummary(patientUuid, selectedExams);

                    startActivity(intent);
                }
            }
        }

        // question_recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        //question_recyclerView.notifyAll();
        recyclerViewIndicator.attachToRecyclerView(question_recyclerView);

    }

    /**
     * Insert into DB could be made into a Helper Method, but isn't because there are specific concept IDs used each time.
     * Although this could also be made into a function, for now it has now been.
     *
     * @param value String to put into DB
     * @return DB Row number, never used
     */
    private boolean insertDb(String value) {

        CustomLog.i(TAG, "insertDb: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue1(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        return isInserted;
    }

    private void updateImageDatabase(String imagePath) {


        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "","");
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void updateDatabase(String string) {
        CustomLog.i(TAG, "updateDatabase: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
//        }
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT));

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

    /**
     * Sets up the complaint knowledgeEngine's questions.
     *
     * @param complaintIndex Index of complaint being displayed to user.
     */
    private void setupQuestions(int complaintIndex) {
        nodeComplete = false;

        if (complaints.size() >= 1) {
            getAssociatedSymptoms(complaintIndex);
        } else {
            currentNode = complaintsNodes.get(complaintIndex);
        }

        mgender = fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            currentNode.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            currentNode.fetchItem("1");
        }

        // flaoting value of age is passed to Node for comparison...
        currentNode.fetchAge(float_ageYear_Month);


        adapter = new QuestionsAdapter(this, currentNode, question_recyclerView, this.getClass().getSimpleName(), this, false);
        question_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(question_recyclerView);
      /*  adapter = new CustomExpandableListAdapter(this, currentNode, this.getClass().getSimpleName());
        questionListView.setAdapter(adapter);
        questionListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        questionListView.expandGroup(0);*/
        setTitle(patientName + ": " + currentNode.findDisplay());

    }

    private void getAssociatedSymptoms(int complaintIndex) {

        List<Node> assoComplaintsNodes = new ArrayList<>();
        assoComplaintsNodes.addAll(complaintsNodes);

        for (int i = 0; i < complaintsNodes.get(complaintIndex).size(); i++) {

            if ((complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS))
                    || (complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("जुड़े लक्षण")) || (complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))
                    || (complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("સંકળાયેલ લક્ષણો"))
                    || (complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("সংশ্লিষ্ট লক্ষণ"))) {
                optionsList.addAll(complaintsNodes.get(complaintIndex).getOptionsList().get(i).getOptionsList());

                assoComplaintsNodes.get(complaintIndex).getOptionsList().remove(i);
                currentNode = assoComplaintsNodes.get(complaintIndex);
                CustomLog.e("CurrentNode", "" + currentNode);

            } else {
                currentNode = complaintsNodes.get(complaintIndex);
            }
        }
    }

    public void setRecyclerViewIndicator() {
        question_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(question_recyclerView);
    }

    private void removeDuplicateSymptoms() {

        nodeComplete = false;

        HashSet<String> hashSet = new HashSet<>();

        List<Node> finalOptionsList = new ArrayList<>(optionsList);

        if (optionsList.size() != 0) {

            for (int i = 0; i < optionsList.size(); i++) {

                if (hashSet.contains(optionsList.get(i).getText())) {

                    finalOptionsList.remove(optionsList.get(i));

                } else {
                    hashSet.add(optionsList.get(i).getText());
                }
            }

            try {
                assoSympObj.put("id", "ID_294177528");
                assoSympObj.put("text", Node.ASSOCIATE_SYMPTOMS);
                assoSympObj.put("display", "Do you have the following symptom(s)?");
                assoSympObj.put("display-hi", "क्या आपको निम्नलिखित लक्षण हैं?");
                assoSympObj.put("display-or", "ତମର ଏହି ଲକ୍ଷଣ ସବୁ ଅଛି କି?");
                assoSympObj.put("display-gj", "શું તમારી પાસે નીચેના લક્ષણ (ઓ) છે?");
                assoSympObj.put("display-kn", "ನೀವು ಈ ಕೆಳಗಿನ ರೋಗಲಕ್ಷಣವನ್ನು ಹೊಂದಿದ್ದೀರಾ?");
                assoSympObj.put("display-te", "మీకు ఈ క్రింది లక్షణం (లు) ఉన్నాయా?");
                assoSympObj.put("display-mr", "तुम्हाला खालील लक्षणे आहेत का?");
                assoSympObj.put("display-as", "আপোনাৰ তলত দিয়া লক্ষণ(সমূহ) আছেনে?");
                assoSympObj.put("display-ml", "നിങ്ങൾക്ക് ഇനിപ്പറയുന്ന രോഗലക്ഷണം ഉണ്ടോ?");
                assoSympObj.put("display-bn", "আপনার কি নিম্নলিখিত লক্ষণগুলি রয়েছে?");
                assoSympObj.put("display-ta", "பின்வரும் அறிகுறி (கள்) உங்களிடம் உள்ளதா?");
                assoSympObj.put("pos-condition", "c.");
                assoSympObj.put("neg-condition", "s.");
                assoSympArr.put(0, assoSympObj);
                finalAssoSympObj.put("id", "ID_844006222");
                finalAssoSympObj.put("text", Node.ASSOCIATE_SYMPTOMS);
                finalAssoSympObj.put("display-kn", "ಸಂಯೋಜಿತ ಲಕ್ಷಣಗಳು");
                finalAssoSympObj.put("display-ml", "ബന്ധപ്പെട്ട രോഗലക്ഷണങ്ങൾ");
                finalAssoSympObj.put("display-as", "সংশ্লিষ্ট লক্ষণ");
                finalAssoSympObj.put("display-mr", "संबंधित लक्षणे");
                finalAssoSympObj.put("display-te", "అనుబంధ లక్షణాలు");
                finalAssoSympObj.put("display-or", "ପେଟଯନ୍ତ୍ରଣା");
                finalAssoSympObj.put("display-hi", "जुड़े लक्षण");
                finalAssoSympObj.put("display-ta", "தொடர்புடைய அறிகுறிகள்");
                finalAssoSympObj.put("display-bn", "জড়িত লক্ষণগুলি");
                finalAssoSympObj.put("display-gj", "સંકળાયેલ લક્ષણો");
                finalAssoSympObj.put("perform-physical-exam", "");
                finalAssoSympObj.put("options", assoSympArr);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            assoSympNode = new Node(finalAssoSympObj);
            assoSympNode.getOptionsList().get(0).setOptionsList(finalOptionsList);
            assoSympNode.getOptionsList().get(0).setTerminal(false);

            currentNode = assoSympNode;


            mgender = fetch_gender(patientUuid);

            if (mgender.equalsIgnoreCase("M")) {
                currentNode.fetchItem("0");
            } else if (mgender.equalsIgnoreCase("F")) {
                currentNode.fetchItem("1");
            }

            // flaoting value of age is passed to Node for comparison...
            currentNode.fetchAge(float_ageYear_Month);

            adapter = new QuestionsAdapter(this, currentNode, question_recyclerView, this.getClass().getSimpleName(), this, true);
            question_recyclerView.setAdapter(adapter);
            setTitle(patientName + ": " + currentNode.findDisplay());
        }
    }

    //Dialog Alert forcing user to answer all questions.
    //Can be removed if necessary
    //TODO: Add setting to allow for all questions unrequired..addAll(Arrays.asList(splitExams))
    public void questionsMissing() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        // AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        //language ui
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if(sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            String a = currentNode.formQuestionAnswer(0, false);
            CustomLog.d("tag", a);
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "सवाल का जवाब नहीं दिया")
                    .replace("Patient reports -", "पेशेंट ने सूचित किया -")
                    .replace("Patient denies -", "पेशेंट ने मना कर दिया -")
                    .replace("Hours", "घंटे").replace("Days","दिन")
                    .replace("Weeks", "हफ्तों").replace("Months", "महीने")
                    .replace("Years", "वर्ष")
                    .replace("times per hour", "प्रति घंटे बार")
                    .replace("time per day", "प्रति दिन का समय")
                    .replace("times per week", "प्रति सप्ताह बार")
                    .replace("times per month", "प्रति माह बार")
                    .replace("times per year", "प्रति वर्ष बार")));
        } else if(sessionManager.getAppLanguage().equalsIgnoreCase("or")){
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "ପ୍ରଶ୍ନର ଉତ୍ତର ନାହିଁ |")
                    .replace("Patient reports -", "ରୋଗୀ ରିପୋର୍ଟ -")
                    .replace("Patient denies -", "ରୋଗୀ ଅସ୍ୱୀକାର କରନ୍ତି -")
                    .replace("Hours", "ଘଣ୍ଟା").replace("Days", "ଦିନ")
                    .replace("Weeks", "ସପ୍ତାହ").replace("Months", "ମାସ")
                    .replace("Years", "ବର୍ଷ")
                    .replace("times per hour", "ସମୟ ପ୍ରତି ଘଣ୍ଟା")
                    .replace("time per day", "ସମୟ ପ୍ରତିଦିନ")
                    .replace("times per week", "ସମୟ ପ୍ରତି ସପ୍ତାହ")
                    .replace("times per month", "ସମୟ ପ୍ରତି ମାସରେ |")
                    .replace("times per year", "ସମୟ ପ୍ରତିବର୍ଷ")));
        } else if(sessionManager.getAppLanguage().equalsIgnoreCase("gu")){
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "પ્રશ્નનો જવાબ મળ્યો નથી")
                    .replace("Patient reports -", "દરદી રિપોર્ટ કરે છે -")
                    .replace("Patient denies -", "દરદી મના કરે છે -")
                    .replace("Hours", "કલાક").replace("Days","દિવસ")
                    .replace("Weeks", "અઠવાડિયું").replace("Months", "માસ")
                    .replace("Years", "વર્ષ")
                    .replace("times per hour", "કલાક દીઠ વખત")
                    .replace("time per day", "દિવસ દીઠ વખત")
                    .replace("times per week", "દર અઠવાડિયે વખત")
                    .replace("times per month", "દર મહિને વખત")
                    .replace("times per year", "વર્ષ દીઠ વખત")));
        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "ప్రశ్నకు సమాధానం ఇవ్వలేదు")
                    .replace("Patient reports -", "రోగి నివేదికలు -")
                    .replace("Patient denies -", "రోగి నిరాకరించాడు -")
                    .replace("Hours", "గంటలు").replace("Days", "రోజులు")
                    .replace("Weeks", "వారాలు").replace("Months", "నెలల")
                    .replace("Years", "సంవత్సరాలు")
                    .replace("times per hour", "గంటకు సార్లు")
                    .replace("time per day", "రోజుకు సార్లు")
                    .replace("times per week", "వారానికి సార్లు")
                    .replace("times per month", "నెలకు సార్లు")
                    .replace("times per year", "సంవత్సరానికి సార్లు")));
        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "प्रश्नाचे उत्तर दिले नाही")
                    .replace("Patient reports -", "रुग्ण अहवाल-")
                    .replace("Patient denies -", "रुग्ण नकार देतो-")
                    .replace("Hours", "तास")
                    .replace("Days", "दिवस")
                    .replace("Weeks", "आठवडे")
                    .replace("Months", "महिने")
                    .replace("Years", "वर्षे")
                    .replace("times per hour", "प्रति तास")
                    .replace("time per day", "दररोज वेळा")
                    .replace("times per week", "आठवड्यातून काही वेळा")
                    .replace("times per month", "दरमहा वेळा")
                    .replace("times per year", "दरवर्षी वेळा")));

        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "ಪ್ರಶ್ನೆಗೆ ಉತ್ತರಿಸಲಾಗಿಲ್ಲ")
                    .replace("Patient reports -", "ರೋಗಿಯ ವರದಿಗಳು-")
                    .replace("Patient denies -", "ರೋಗಿಯು ನಿರಾಕರಿಸುತ್ತಾನೆ-")
                    .replace("Hours", "ಗಂಟೆಗಳು").replace("Days", "ದಿನಗಳು")
                    .replace("Weeks", "ವಾರಗಳು").replace("Months", "ತಿಂಗಳುಗಳು")
                    .replace("Years", "ವರ್ಷಗಳು")
                    .replace("times per hour", "ಗಂಟೆಗೆ ಬಾರಿ").replace("time per day", "ದಿನಕ್ಕೆ ಬಾರಿ")
                    .replace("times per week", "ವಾರಕ್ಕೆ ಬಾರಿ").replace("times per month", "ತಿಂಗಳಿಗೆ ಬಾರಿ")
                    .replace("times per year", "ವರ್ಷಕ್ಕೆ ಬಾರಿ")));
        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "প্ৰশ্নৰ উত্তৰ দিয়া হোৱা নাই")
                    .replace("Patient reports -", "ৰোগীৰ প্ৰতিবেদন -")
                    .replace("Patient denies -", "ৰোগীয়ে অস্বীকাৰ কৰে -")
                    .replace("Hours", "ঘণ্টা").replace("Days", "দিনসমূহ")
                    .replace("Weeks", "সপ্তাহ").replace("Months", "মাহ")
                    .replace("Years", "বছৰ")
                    .replace("times per hour", "প্ৰতি ঘণ্টাত সময়")
                    .replace("time per day", "প্ৰতিদিনে সময়")
                    .replace("times per week", "প্ৰতি সপ্তাহত সময়")
                    .replace("times per month", "প্ৰতি মাহে সময়")
                    .replace("times per year", "প্ৰতি বছৰে সময়")));
        }
        //Malyalam Language Support...
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "ചോദ്യത്തിന് ഉത്തരം ലഭിച്ചില്ല")
                    .replace("Patient reports -", "രോഗിയുടെ റിപ്പോർട്ടുകൾ -")
                    .replace("Patient denies -", "രോഗി നിരസിക്കുന്നു -")
                    .replace("Hours", "മണിക്കൂറുകൾ").replace("Days", "ദിവസങ്ങളിൽ")
                    .replace("Weeks", "ആഴ്ചകൾ").replace("Months", "മാസങ്ങൾ")
                    .replace("Years", "വർഷങ്ങൾ")
                    .replace("times per hour", "മണിക്കൂറിൽ തവണ")
                    .replace("time per day", "പ്രതിദിനം തവണ")
                    .replace("times per week", "ആഴ്ചയിൽ തവണ")
                    .replace("times per month", "മാസത്തിൽ തവണ")
                    .replace("times per year", "വർഷത്തിൽ തവണ")));
        }
        else if(sessionManager.getAppLanguage().equalsIgnoreCase("bn")){
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "প্রশ্নের উত্তর দেওয়া হয়নি")
                    .replace("Patient reports -", "রোগীর রিপোর্ট-")
                    .replace("Patient denies -", "রোগী অস্বীকার করে-")
                    .replace("Hours", "ঘন্টার").replace("Days", "দিনগুলি")
                    .replace("Weeks", "সপ্তাহ").replace("Months", "মাস")
                    .replace("Years", "বছর")
                    .replace("times per hour", "প্রতি ঘন্টা")
                    .replace("time per day", "দিনে বার")
                    .replace("times per week", "প্রতি সপ্তাহে বার")
                    .replace("times per month", "প্রতি মাসে বার")
                    .replace("times per year", "প্রতি বছর বার")));
        } else if(sessionManager.getAppLanguage().equalsIgnoreCase("ta")){
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)
                    .replace("Question not answered", "கேள்விக்கு பதில் அளிக்கப்படவில்லை")
                    .replace("Patient reports -", "நோயாளி கூறுகிறார்-")
                    .replace("Patient denies -", "நோயாளி மறுக்கிறார்-")
                    .replace("Hours", "மணி").replace("Days","நாட்கள்")
                    .replace("Weeks", "வாரங்கள்").replace("Months", "மாதங்கள்")
                    .replace("Years", "ஆண்டுகள்")
                    .replace("times per hour", "ஒரு மணி நேரத்திற்கு முறை")
                    .replace("time per day", "ஒரு நாளைக்கு முறை")
                    .replace("times per week", "வாரத்திற்கு முறை")
                    .replace("times per month", "மாதம் முறை")
                    .replace("times per year", "வருடத்திற்கு முறை")));
        }
        else {
            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0, false)));
        }

        //  alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0)));
        alertDialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                complaintConfirmed = true;
                dialog.dismiss();
                fabClick();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.generic_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        //alertDialog.show();
    }


    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getPhysicalExams();
        if (rawExams != null) {
            String[] splitExams = rawExams.split(";");
            examList.addAll(Arrays.asList(splitExams));
            return examList;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                currentNode.setImagePath(mCurrentPhotoPath);
                currentNode.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
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

    @Override
    public void fabClickedAtEnd() {
        //currentNode = node;
        fabClick();
    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos, int physExamPos) {
        onListClicked(null, groupPos, childPos);
    }


}
