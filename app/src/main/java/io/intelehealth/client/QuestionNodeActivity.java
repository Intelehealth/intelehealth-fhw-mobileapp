package io.intelehealth.client;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import org.json.JSONObject;

import io.intelehealth.client.db.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.Knowledge;
import io.intelehealth.client.objects.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Gets more specifics of the ailment suffered by patient.
 */
public class QuestionNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Question Node Activity";

    int lastExpandedPosition = -1;


    String patientID = "1";
    String visitID;
    String state;
    String patientName;
    String intentTag;

    Boolean complaintConfirmed = false;


    Knowledge mKnowledge; //Knowledge engine
    ExpandableListView questionListView;
        String mFileName = "knowledge.json"; //knowledge engine file
//    String mFileName = "DemoBrain.json";
    int complaintNumber = 0; //assuming there is at least one complaint, starting complaint number
    HashMap<String, String> complaintDetails; //temporary storage of complaint findings
    ArrayList<String> complaints; //list of complaints going to be used
    List<Node> complaintsNodes; //actual nodes to be used
    ArrayList<String> physicalExams;
    Node currentNode;
    CustomExpandableListAdapter adapter;
    boolean nodeComplete = false;

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
            complaints = intent.getStringArrayListExtra("complaints");
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }


        complaintDetails = new HashMap<>();
        physicalExams = new ArrayList<>();

        //mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        complaintsNodes = new ArrayList<>();
        for (int i = 0; i < complaints.size(); i++) {
            String fileLocation = "engines/" + complaints.get(i) + ".json";
            JSONObject currentFile = HelperMethods.encodeJSON(this, fileLocation);
            Node currentNode = new Node(currentFile);
            complaintsNodes.add(currentNode);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        questionListView = (ExpandableListView) findViewById(R.id.complaint_question_expandable_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }
        });

        setupQuestions(complaintNumber);
        //In the event there is more than one complaint, they will be prompted one at a time.

        questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if ((currentNode.getOption(groupPosition).getChoiceType().equals("single")) && !currentNode.getOption(groupPosition).anySubSelected()) {
                    Node question = currentNode.getOption(groupPosition).getOption(childPosition);
                    question.toggleSelected();
                    if (currentNode.getOption(groupPosition).anySubSelected()) {
                        currentNode.getOption(groupPosition).setSelected();
                    } else {
                        currentNode.getOption(groupPosition).setUnselected();
                    }

                    if (!question.getInputType().isEmpty() && question.isSelected()) {
                        Node.handleQuestion(question, QuestionNodeActivity.this, adapter);
                        //If there is an input type, then the question has a special method of data entry.
                    }

                    if (!question.isTerminal() && question.isSelected()) {
                        Node.subLevelQuestion(question, QuestionNodeActivity.this, adapter);
                        //If the node is not terminal, that means there are more questions to be asked for this branch.
                    }
                } else if ((currentNode.getOption(groupPosition).getChoiceType().equals("single")) && currentNode.getOption(groupPosition).anySubSelected()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QuestionNodeActivity.this);
                    alertDialogBuilder.setMessage("This question can have only one answer.");
                    alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {

                    Node question = currentNode.getOption(groupPosition).getOption(childPosition);
                    question.toggleSelected();
                    if (currentNode.getOption(groupPosition).anySubSelected()) {
                        currentNode.getOption(groupPosition).setSelected();
                    } else {
                        currentNode.getOption(groupPosition).setUnselected();
                    }

                    if (!question.getInputType().isEmpty() && question.isSelected()) {
                        Node.handleQuestion(question, QuestionNodeActivity.this, adapter);
                        //If there is an input type, then the question has a special method of data entry.
                    }

                    if (!question.isTerminal() && question.isSelected()) {
                        Node.subLevelQuestion(question, QuestionNodeActivity.this, adapter);
                        //If the node is not terminal, that means there are more questions to be asked for this branch.
                    }
                }
                adapter.notifyDataSetChanged();
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
        });

    }

    /**
     * Summarizes the information of the current complaint node.
     * Then has that put into the database, and then checks to see if there are more complaint nodes.
     * If there are more, presents the user with the next set of questions.
     * All exams are also stored into a string, which will be passed through the activities to the Physical Exam Activity.
     */
    private void fabClick() {
//        for (int i = 0; i < adapter.getGroupCount(); i++) {
//            if (!currentNode.getOption(i).isSelected()) {
//                nodeComplete = false;
//                questionListView.expandGroup(i);
//                break;
//            } else {
//                nodeComplete = true;
//            }
//        }
        nodeComplete = true;

        if (!complaintConfirmed) {
            questionsMissing();
        } else {

            //TODO: Under this new scheme where there is just a list of existing JSONS, need to parse out associated symptoms
//            ArrayList<String> selectedAssociations = currentNode.getSelectedAssociations();
//            for (int i = 0; i < selectedAssociations.size(); i++) {
//                if (!complaints.contains(selectedAssociations.get(i))) {
//                    complaints.add(selectedAssociations.get(i));
//                    complaintsNodes.add(mKnowledge.getComplaint(selectedAssociations.get(i)));
//                }
//            }
            String complaintString = currentNode.generateLanguage();
            String complaintFormatted = complaintString.replace("?,", "?:");

            String complaint = currentNode.getText();
            complaintDetails.put(complaint, complaintFormatted);

            String insertion = complaint + ": " + complaintFormatted;


            physicalExams.addAll(parseExams(currentNode));

            if (complaintNumber < complaints.size() - 1) {
                complaintNumber++;
                setupQuestions(complaintNumber);
                complaintConfirmed = false;
            } else {
                if (intentTag != null && intentTag.equals("edit")) {
                    updateDatabase(insertion);
                    Intent intent = new Intent(QuestionNodeActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                } else {
                    insertDb(insertion);
                    Intent intent = new Intent(QuestionNodeActivity.this, PastMedicalHistoryActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);
                }
            }

        }

    }

    /**
     * Insert into DB could be made into a Helper Method, but isn't because there are specific concept IDs used each time.
     * Although this could also be made into a function, for now it has now been.
     *
     * @param value String to put into DB
     * @return DB Row number, never used
     */
    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);

        final int CONCEPT_ID = 163186; //OpenMRS complaint concept ID

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

    private void updateDatabase(String string) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();

        int conceptID = 163186;
        ContentValues contentValues = new ContentValues();
        contentValues.put("value", string);

        String selection = "patient_id = ? AND visit_id = ? AND concept_id = ?";
        String[] args = {patientID, visitID, String.valueOf(conceptID)};

        localdb.update(
                "visit",
                contentValues,
                selection,
                args
        );

    }
    /**
     * Sets up the complaint node's questions.
     *
     * @param complaintIndex Index of complaint being displayed to user.
     */
    private void setupQuestions(int complaintIndex) {
        nodeComplete = false;
        currentNode = complaintsNodes.get(complaintIndex);
        adapter = new CustomExpandableListAdapter(this, currentNode, this.getClass().getSimpleName());
        questionListView.setAdapter(adapter);
        questionListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        questionListView.expandGroup(0);
        setTitle(patientName + ": " + currentNode.getText());
    }

    //Dialog Alert forcing user to answer all questions.
    //Can be removed if necessary
    //TODO: Add setting to allow for all questions unrequired.
    public void questionsMissing() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.question_answer_all);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                complaintConfirmed = true;
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getExams();
        String[] splitExams = rawExams.split(";");
        examList.addAll(Arrays.asList(splitExams));
        return examList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                currentNode.setImagePath(mCurrentPhotoPath);
                currentNode.displayImage(this);
            }
        }
    }

}