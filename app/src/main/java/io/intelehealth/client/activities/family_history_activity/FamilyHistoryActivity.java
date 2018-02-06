package io.intelehealth.client.activities.family_history_activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.custom_expandable_list_adapter.CustomExpandableListAdapter;
import io.intelehealth.client.activities.physical_exam_activity.PhysicalExamActivity;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.node.Node;
import io.intelehealth.client.node.PhysicalExam;
import io.intelehealth.client.utilities.ConceptId;
import io.intelehealth.client.utilities.HelperMethods;

/**
 * Creates the family history mindmap of the patient.
 */
public class FamilyHistoryActivity extends AppCompatActivity {

    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    Integer patientID;
    String visitID;
    String state;
    String patientName;
    String intentTag;

    String image_Prefix = "FH"; //Abbreviation for Family History
    String imageDir = "Family History"; //Abbreviation for Family History

    ArrayList<String> physicalExams;

    String mFileName = "famHist.json";
//    String mFileName = "DemoFamily.json";

    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    CustomExpandableListAdapter adapter;
    ExpandableListView familyListView;

    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "", phistory = "", fhistory = "";
    boolean flag = false;
    boolean hasLicense = false;
    SharedPreferences.Editor e;
    SQLiteDatabase localdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        localdb = mDbHelper.getWritableDatabase();
        //For Testing
//        patientID = Long.valueOf("1");

        // display pop-up to ask for update, if a returning patient
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        e = sharedPreferences.edit();

        boolean past = sharedPreferences.getBoolean("returning", false);
        if (past) {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(FamilyHistoryActivity.this);
            alertdialog.setTitle(getString(R.string.title_activity_family_history));
            alertdialog.setMessage(getString(R.string.question_update_details));
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
                    // skip
                    flag = false;

                    String[] columns = {"value", " concept_id"};
                    String orderBy = "visit_id";

                    try {
                        String famHistSelection = "patient_id = ? AND concept_id = ?";
                        String[] famHistArgs = {String.valueOf(patientID), String.valueOf(ConceptId.RHK_FAMILY_HISTORY_BLURB)};
                        Cursor famHistCursor = localdb.query("obs", columns, famHistSelection, famHistArgs, null, null, orderBy);
                        famHistCursor.moveToLast();
                        fhistory = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                        famHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        fhistory=""; // if family history does not exist
                    }

                    if (fhistory != null && !fhistory.isEmpty() && !fhistory.equals("null")) {
                        insertDb(fhistory);
                    }
                    //  PastMedicalHistoryActivity pmh = new PastMedicalHistoryActivity();
                    // pmh.insertDb(phistory);

                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientID", patientID);
                    intent.putExtra("visitID", visitID);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);
                    //  intent.putStringArrayListExtra("exams", physicalExams);

                    startActivity(intent);

                }
            });
            alertdialog.show();
        }


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getIntExtra("patientID",-1);
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            //       physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });

        if (sharedPreferences.contains("licensekey"))
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(HelperMethods.readFileRoot(mFileName, this));
                familyHistoryMap = new Node(currentFile); //Load the family history mind map
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            familyHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName)); //Load the family history mind map
        }

       // familyHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName)); //Load the family history mind map
        familyListView = (ExpandableListView) findViewById(R.id.family_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);

        familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
                Log.i(TAG, "onChildClick: ");
                clickedNode.toggleSelected();
                //Log.d(TAG, String.valueOf(clickedNode.isSelected()));
                if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
                    familyHistoryMap.getOption(groupPosition).setSelected();
                } else {
                    familyHistoryMap.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                if(clickedNode.getInputType()!= null){
                    if (!clickedNode.getInputType().equals("camera")) {
                        Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, null, null);
                    }
                }

                String imageName = patientID + "_" + visitID + "_" + image_Prefix;
                String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                File filePath = new File(baseDir + File.separator + "Patient Images" + File.separator +
                        patientID + File.separator + visitID + File.separator + imageDir);

                if (!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal() &&
                        familyHistoryMap.getOption(groupPosition).getOption(childPosition).isSelected()) {
                    Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
                }

                return false;
            }
        });

        familyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Node clickedNode = familyHistoryMap.getOption(groupPosition);

                if (clickedNode.getInputType() != null) {
                    Log.i(TAG, "onGroupExpand:  input not null");
                    if (clickedNode.getInputType().equals("camera")) {
                        String imageName = patientID + "_" + visitID + "_" + image_Prefix;
                        String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                        File filePath = new File(baseDir + File.separator + "Patient Images" + File.separator +
                                patientID + File.separator + visitID + File.separator + imageDir);
                        if (!filePath.exists()) {
                            filePath.mkdirs();
                        }
                        Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
                    } else {
                        Log.i(TAG, "onGroupExpand: input not camera");
                        Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, null, null);
                    }
                }

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    familyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

    }

    /*
        Language here works funny.
        The architecture for the language of the family history mind map needs to be modified, as it does not allow for nice sentence building.
        It also has a weird thing with new line characters, and just the way that the language itself should be displayed.
     */
    private void onFabClick() {
        if (familyHistoryMap.anySubSelected()) {
            for (Node node : familyHistoryMap.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = node.generateLanguage();
                    String toInsert = node.getText() + " : " + familyString;
                    toInsert = toInsert.replaceAll(Node.bullet,"");
                    toInsert = toInsert.replaceAll(" - ",", ");
                    toInsert = toInsert.replaceAll("<br/>","");
                    if(StringUtils.right(toInsert,2).equals(", ")){
                     toInsert = toInsert.substring(0,toInsert.length()-2);
                    }
                    toInsert = toInsert+ ".<br/>";

                    insertionList.add(toInsert);

                }
            }
        }

        for (int i = 0; i < insertionList.size(); i++) {
            if (i == 0) {
                insertion = Node.bullet + insertionList.get(i);
            } else {
                insertion = insertion +" "+ Node.bullet + insertionList.get(i);
            }
        }

        List<String> imagePathList = familyHistoryMap.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }


        if (intentTag != null && intentTag.equals("edit")) {
            updateDatabase(insertion);
            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            startActivity(intent);
        } else {

            if (flag == true) {
                // only if OK clicked, collect this new info (old patient)
                if (insertion.length() > 0) {
                    fhistory = fhistory + insertion;
                } else {
                    fhistory = fhistory + "";
                }
                insertDb(fhistory);

                // PastMedicalHistoryActivity pmh = new PastMedicalHistoryActivity();
                // pmh.insertDb(phistory);

                // this will display history data as it is present in database
                // Toast.makeText(FamilyHistoryActivity.this,"new PMH: "+phistory,Toast.LENGTH_SHORT).show();
                // Toast.makeText(FamilyHistoryActivity.this,"new FH: "+fhistory,Toast.LENGTH_SHORT).show();
            } else {
                insertDb(insertion); // new details of family history
            }

            flag = false;
            e.putBoolean("returning", false); // done with old patient, so unset flag and returning
            e.commit();
            Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }


    }

    public long insertDb(String value) {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);// TODO: Connect the proper CREATOR_ID

        final int CONCEPT_ID = ConceptId.RHK_FAMILY_HISTORY_BLURB; // RHK FAMILY HISTORY BLURB

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);
        complaintEntries.put("creator", CREATOR_ID);


        return localdb.insert("obs", null, complaintEntries);
    }

    private void updateImageDatabase(String imagePath) {

        localdb.execSQL("INSERT INTO image_records (patient_id,visit_id,image_path,image_type,delete_status) values("
                + "'" + patientID + "'" + ","
                + visitID + ","
                + "'" + imagePath + "','" + image_Prefix + "'," +
                0 +
                ")");
    }

    private void updateDatabase(String string) {

        int conceptID = ConceptId.RHK_FAMILY_HISTORY_BLURB;
        ContentValues contentValues = new ContentValues();
        contentValues.put("value", string);

        String selection = "patient_id = ? AND visit_id = ? AND concept_id = ?";
        String[] args = {String.valueOf(patientID), visitID, String.valueOf(conceptID)};

        localdb.update(
                "obs",
                contentValues,
                selection,
                args
        );

    }

    @Override
    public void onBackPressed() {
    }

}
