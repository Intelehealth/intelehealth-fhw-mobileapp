package app.intelehealth.client.activities.familyHistoryActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ExpandableListView;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.questionNodeActivity.QuestionsAdapter;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.database.dao.EncounterDAO;
import app.intelehealth.client.database.dao.ImagesDAO;
import app.intelehealth.client.database.dao.ObsDAO;
import app.intelehealth.client.knowledgeEngine.Node;
import app.intelehealth.client.models.dto.ObsDTO;
import app.intelehealth.client.utilities.FileUtils;
import app.intelehealth.client.utilities.SessionManager;
import app.intelehealth.client.utilities.UuidDictionary;

import app.intelehealth.client.activities.physcialExamActivity.CustomExpandableListAdapter;
import app.intelehealth.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.intelehealth.client.activities.visitSummaryActivity.VisitSummaryActivity;
import app.intelehealth.client.utilities.exception.DAOException;

public class FamilyHistoryActivity extends AppCompatActivity  implements  QuestionsAdapter.FabClickListener{
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> physicalExams;
    String mFileName = "famHist.json";
    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    //CustomExpandableListAdapter adapter;
   // ExpandableListView familyListView;

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

    RecyclerView family_history_recyclerView;
    QuestionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
        boolean past = sessionManager.isReturning();
        if (past) {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(FamilyHistoryActivity.this,R.style.AlertDialogStyle);
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

                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);

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
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        setTitle(R.string.title_activity_family_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this,R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        family_history_recyclerView = findViewById(R.id.family_history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        family_history_recyclerView.setLayoutManager(linearLayoutManager);
        family_history_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(family_history_recyclerView);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });

//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
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

      //  familyListView = findViewById(R.id.family_history_expandable_list_view);

        adapter = new QuestionsAdapter(this,familyHistoryMap,family_history_recyclerView,this.getClass().getSimpleName(),this,false);
        family_history_recyclerView.setAdapter(adapter);
        /*adapter = new CustomExpandableListAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);*/

        /*familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                return false;
            }
        });*/
    }

    private void onListClick(View v, int groupPosition,int childPosition){
        Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
        Log.i(TAG, "onChildClick: ");
        clickedNode.toggleSelected();
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
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
            Log.i("RES>", "" + filePath + " -> " + res);
        }

        imageName = UUID.randomUUID().toString();

        if (!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal() &&
                familyHistoryMap.getOption(groupPosition).getOption(childPosition).isSelected()) {
            Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
        }

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

            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("hasPrescription", "false");
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
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(app.intelehealth.client.utilities.StringUtils.getValue(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }

        return isInserted;
    }

    private void updateImageDatabase(String imagePath) {

        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
    }

    private void updateDatabase(String string) {

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

    @Override
    public void fabClickedAtEnd() {
        onFabClick();

    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos,int physExamPos) {
        onListClick(null,groupPos,childPos);
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
        if(v != null){
            v.setAnimation(animation);
        }


    }
    public void bottomUpAnimation(View v) {

        if( v != null){
            v.setVisibility(View.VISIBLE);
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            v.startAnimation(bottomUp);
        }

    }
}



