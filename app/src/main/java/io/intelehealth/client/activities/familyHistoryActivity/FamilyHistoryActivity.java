package io.intelehealth.client.activities.familyHistoryActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.physcialExamActivity.CustomExpandableListAdapter;
import io.intelehealth.client.activities.physcialExamActivity.PhysicalExamActivity;
import io.intelehealth.client.activities.visitSummaryActivity.VisitSummaryActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.database.dao.EncounterDAO;
import io.intelehealth.client.database.dao.ImagesDAO;
import io.intelehealth.client.database.dao.ObsDAO;
import io.intelehealth.client.knowledgeEngine.Node;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;

public class FamilyHistoryActivity extends AppCompatActivity {
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
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
    SessionManager sessionManager;
    String encounterVitals;
    String encounterAdultIntials;
    private String imageName = null;
    private File filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
        boolean past = sessionManager.isReturning();
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

                    String[] columns = {"value", " conceptuuid"};

                    try {
                        String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                        String[] famHistArgs = {encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
                        Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
                        famHistCursor.moveToLast();
                        fhistory = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                        famHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        fhistory = ""; // if family history does not exist
                    }

                    if (fhistory != null && !fhistory.isEmpty() && !fhistory.equals("null")) {
                        insertDb(fhistory);
                    }
                    //  PastMedicalHistoryActivity pmh = new PastMedicalHistoryActivity();
                    // pmh.insertDb(phistory);

                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
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
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
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
        setTitle(R.string.title_activity_family_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });

        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                familyHistoryMap = new Node(currentFile); //Load the family history mind map
            } catch (JSONException e) {
                Crashlytics.getInstance().core.logException(e);
            }
        } else {
            familyHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the family history mind map
        }

        // familyHistoryMap = new Node(HelperMethods.encodeJSON(this, mFileName)); //Load the family history mind map
        familyListView = findViewById(R.id.family_history_expandable_list_view);
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

                if (clickedNode.getInputType() != null) {
                    if (!clickedNode.getInputType().equals("camera")) {
                        Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, null, null);
                    }
                }

//                String imageName = patientUuid + "_" + visitUuid + "_" + image_Prefix;
//                String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
//                File filePath = new File(baseDir + File.separator + "Patient Images" + File.separator +
//                        patientUuid + File.separator + visitUuid + File.separator + imageDir);

                if (!filePath.exists()) {
                    boolean res = filePath.mkdirs();
                    Log.i("RES>", "" + filePath + " -> " + res);
                }

                imageName = UUID.randomUUID().toString();

                if (!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal() &&
                        familyHistoryMap.getOption(groupPosition).getOption(childPosition).isSelected()) {
                    Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
                }

                return false;
            }
        });
    }

    private void onFabClick() {
        if (familyHistoryMap.anySubSelected()) {
            for (Node node : familyHistoryMap.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = node.generateLanguage();
                    String toInsert = node.getText() + " : " + familyString;
                    toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + ".<br/>";
                    insertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < insertionList.size(); i++) {
            if (i == 0) {
                insertion = Node.bullet + insertionList.get(i);
            } else {
                insertion = insertion + " " + Node.bullet + insertionList.get(i);
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

            //making flag to false in the encounter table so it will sync again
//            EncounterDAO encounterDAO = new EncounterDAO();
//            try {
//                encounterDAO.updateEncounterSync("false", encounterAdultIntials);
//            } catch (DAOException e) {
//                Crashlytics.getInstance().core.logException(e);
//            }
            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
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
            sessionManager.setReturning(false);
            Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }


    }

    public boolean insertDb(String value) {


//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//        final String CREATOR_ID = prefs.getString("creatorid", null);// TODO: Connect the proper CREATOR_ID
//
//        final String CONCEPT_ID = UuidDictionary.RHK_FAMILY_HISTORY_BLURB; // RHK FAMILY HISTORY BLURB
//
//        ContentValues complaintEntries = new ContentValues();

//        complaintEntries.put("patient_id", patientID);
//        complaintEntries.put("visit_id", visitID);
//        complaintEntries.put("uuid", UUID.randomUUID().toString());
//        complaintEntries.put("encounteruuid", encounterAdultIntials);
//        complaintEntries.put("value", io.intelehealth.client.utilities.StringUtils.getValue(value));
//        complaintEntries.put("conceptuuid", CONCEPT_ID);
//        complaintEntries.put("creator", CREATOR_ID);
//        complaintEntries.put("sync", "false");

        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(io.intelehealth.client.utilities.StringUtils.getValue(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }

        return isInserted;
    }

    private void updateImageDatabase(String imagePath) {

//        localdb.execSQL("INSERT INTO image_records (patient_id,visit_id,image_path,image_type,delete_status) values("
//                + "'" + patientUuid + "'" + ","
//                + visitUuid + ","
//                + "'" + imagePath + "','" + image_Prefix + "'," +
//                0 +
//                ")");
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
    }

    private void updateDatabase(String string) {

//        String conceptID = UuidDictionary.RHK_FAMILY_HISTORY_BLURB;
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("value", string);
//        contentValues.put("sync", "false");
//        String selection = "encounteruuid = ? AND conceptuuid = ?";
//        String[] args = {encounterAdultIntials, conceptID};
//
//        localdb.update(
//                "tbl_obs",
//                contentValues,
//                selection,
//                args
//        );
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB));

            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            Crashlytics.getInstance().core.logException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
    }

    @Override
    public void onBackPressed() {
    }

}



