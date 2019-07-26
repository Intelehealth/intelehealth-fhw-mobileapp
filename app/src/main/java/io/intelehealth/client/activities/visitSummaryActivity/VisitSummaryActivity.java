package io.intelehealth.client.activities.visitSummaryActivity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.additionalDocumentsActivity.AdditionalDocumentsActivity;
import io.intelehealth.client.activities.complaintNodeActivity.ComplaintNodeActivity;
import io.intelehealth.client.activities.familyHistoryActivity.FamilyHistoryActivity;
import io.intelehealth.client.activities.homeActivity.HomeActivity;
import io.intelehealth.client.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import io.intelehealth.client.activities.patientSurveyActivity.PatientSurveyActivity;
import io.intelehealth.client.activities.physcialExamActivity.PhysicalExamActivity;
import io.intelehealth.client.activities.vitalActivity.VitalsActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.database.dao.EncounterDAO;
import io.intelehealth.client.database.dao.ImagesDAO;
import io.intelehealth.client.database.dao.ImagesPushDAO;
import io.intelehealth.client.database.dao.PatientsDAO;
import io.intelehealth.client.database.dao.PullDataDAO;
import io.intelehealth.client.knowledgeEngine.Node;
import io.intelehealth.client.models.Patient;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.services.DownloadService;
import io.intelehealth.client.syncModule.SyncUtils;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;

public class VisitSummaryActivity extends AppCompatActivity {

    private static final String TAG = VisitSummaryActivity.class.getSimpleName();
    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;

    boolean uploaded = false;
    boolean downloaded = false;
    boolean dataChanged = false;
    String failedMessage;

    Context context;

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    String visitUUID;


    SQLiteDatabase db;

    Patient patient = new Patient();
    ObsDTO complaint = new ObsDTO();
    ObsDTO famHistory = new ObsDTO();
    ObsDTO patHistory = new ObsDTO();
    ObsDTO phyExam = new ObsDTO();
    ObsDTO height = new ObsDTO();
    ObsDTO weight = new ObsDTO();
    ObsDTO pulse = new ObsDTO();
    ObsDTO bpSys = new ObsDTO();
    ObsDTO bpDias = new ObsDTO();
    ObsDTO temperature = new ObsDTO();
    ObsDTO spO2 = new ObsDTO();
    ObsDTO resp = new ObsDTO();

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";
    String followUpDate = "";

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;
    ImageButton editAddDocs;

    TextView nameView;
    TextView idView;
    TextView heightView;
    TextView weightView;
    TextView pulseView;
    TextView bpView;
    TextView tempView;
    TextView spO2View;
    TextView bmiView;
    TextView complaintView;
    TextView famHistView;
    TextView patHistView;
    TextView physFindingsView;
    TextView mDoctorTitle;
    TextView mDoctorName;
    TextView mCHWname;
    //    //    Respiratory added by mahiti dev team
    TextView respiratory;
    TextView tempfaren;
    TextView tempcel;
    String medHistory;
    String baseDir;
    String filePathPhyExam;
    File phyExamDir;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    Button uploadButton;
    Button downloadButton;
    ArrayList<String> physicalExams;

    CardView diagnosisCard;
    CardView prescriptionCard;
    CardView medicalAdviceCard;
    CardView requestedTestsCard;
    CardView additionalCommentsCard;
    CardView followUpDateCard;

    TextView diagnosisTextView;
    TextView prescriptionTextView;
    TextView medicalAdviceTextView;
    TextView requestedTestsTextView;
    TextView additionalCommentsTextView;
    TextView followUpDateTextView;
    //added checkbox flag .m
    CheckBox flag;

    Boolean isPastVisit = false;
    Boolean isReceiverRegistered = false;

    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";

    NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private Menu mymenu;
    MenuItem internetCheck = null;
    MenuItem endVisit_click = null;

    private RecyclerView mAdditionalDocsRecyclerView;
    private RecyclerView.LayoutManager mAdditionalDocsLayoutManager;

    private RecyclerView mPhysicalExamsRecyclerView;
    private RecyclerView.LayoutManager mPhysicalExamsLayoutManager;

    public static String prescriptionHeader1;
    public static String prescriptionHeader2;
    String additionalDocumentDir = "Additional Documents";
    String physicalExamDocumentDir = "Physical Exam";
    SharedPreferences mSharedPreference;
    boolean hasLicense = false;
    String mFileName = "config.json";
    public static String prescription1;
    public static String prescription2;
    SessionManager sessionManager;
    String encounterUuid;
    String encounterVitals;
    String encounterAdultIntials;

    ProgressBar mProgressBar;
    TextView mProgressText;

    ImageButton additionalDocumentsDownlaod;
    ImageButton onExaminationDownload;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_visit_summary, menu);
        MenuItem menuItem = menu.findItem(R.id.summary_endVisit);

        internetCheck = menu.findItem(R.id.internet_icon);
        MenuItemCompat.getActionView(internetCheck);

        isNetworkAvailable(this);
        sessionManager = new SessionManager(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mCHWname = findViewById(R.id.chw_details);
        mCHWname.setText(sessionManager.getChwname());
        //Added Prescription Title from config.Json dynamically through sharedPreferences
        prescriptionHeader1 = sharedPreferences.getString("prescriptionTitle1", "");
        prescriptionHeader2 = sharedPreferences.getString("prescriptionTitle2", "");

        if (isPastVisit) menuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver broadcastReceiverForIamgeDownlaod = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//            if (intent.getAction().equals(AppConstants.MESSAGE_PROGRESS)) {
//                Download download = intent.getParcelableExtra("download");
//                if (download.getProgress() == 100) {
//                } else {
//                }
//            }
            onResume();
            physicalDoumentsUpdates();

        }
    };

    public void registerBroadcastReceiverDynamically() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        registerReceiver(broadcastReceiverForIamgeDownlaod, filter);
    }


    @Override
    public void onBackPressed() {
        //do nothing
        //Use the buttons on the screen to navigate
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverForIamgeDownlaod);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.summary_home: {
//                NavUtils.navigateUpFromSameTask(this);
                Intent i = new Intent(this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
            case R.id.summary_print: {
                try {
                    doWebViewPrint();
                } catch (ParseException e) {
                    Crashlytics.getInstance().core.logException(e);
                }
                return true;
            }
            case R.id.summary_sms: {
                //     VisitSummaryActivityPermissionsDispatcher.sendSMSWithCheck(this);
                return true;
            }
            case R.id.summary_endVisit: {
                //meera
                if (downloaded) {
                    endVisit();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage(R.string.error_no_data);
                    alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(getApplicationContext());
        final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            mSharedPreference = this.getSharedPreferences(
                    "visit_summary", Context.MODE_PRIVATE);
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            isPastVisit = intent.getBooleanExtra("pastVisit", false);

            Set<String> selectedExams = mSharedPreference.getStringSet("exam_" + patientUuid, null);
            if (physicalExams == null) physicalExams = new ArrayList<>();
            physicalExams.clear();
            if (selectedExams != null && !selectedExams.isEmpty()) {
                physicalExams.addAll(selectedExams);
            }
        }


        registerBroadcastReceiverDynamically();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.contains("licensekey"))
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, this)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }
            prescription1 = obj.getString("presciptionHeader1");

            prescription2 = obj.getString("presciptionHeader2");


        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }

        setTitle(R.string.title_activity_patient_summary);
        setTitle(patientName + ": " + getTitle());

        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mLayout = findViewById(R.id.summary_layout);
        context = getApplicationContext();
//we can remove by data binding
        mAdditionalDocsRecyclerView = findViewById(R.id.recy_additional_documents);
        mPhysicalExamsRecyclerView = findViewById(R.id.recy_physexam);

        diagnosisCard = findViewById(R.id.cardView_diagnosis);
        prescriptionCard = findViewById(R.id.cardView_rx);
        medicalAdviceCard = findViewById(R.id.cardView_medical_advice);
        requestedTestsCard = findViewById(R.id.cardView_tests);
        additionalCommentsCard = findViewById(R.id.cardView_additional_comments);
        followUpDateCard = findViewById(R.id.cardView_follow_up_date);
        mDoctorTitle = findViewById(R.id.title_doctor);
        mDoctorName = findViewById(R.id.doctor_details);
        mDoctorTitle.setVisibility(View.GONE);
        mDoctorName.setVisibility(View.GONE);

        diagnosisTextView = findViewById(R.id.textView_content_diagnosis);
        prescriptionTextView = findViewById(R.id.textView_content_rx);
        medicalAdviceTextView = findViewById(R.id.textView_content_medical_advice);
        requestedTestsTextView = findViewById(R.id.textView_content_tests);
        additionalCommentsTextView = findViewById(R.id.textView_content_additional_comments);
        followUpDateTextView = findViewById(R.id.textView_content_follow_up_date);

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
//
//        filePathPhyExam = baseDir + File.separator + "Patient Images" + File.separator + patientUuid + File.separator +
//                visitUuid + File.separator + physicalExamDocumentDir;

        phyExamDir = new File(AppConstants.IMAGE_PATH);

        flag = findViewById(R.id.flaggedcheckbox);
//        EncounterDAO encounterDAO = new EncounterDAO();
//        VisitsDAO visitsDAO = new VisitsDAO();
//        try {
//            visitsDAO.isUpdatedEmergencyColumn(visitUuid, encounterDAO.isEmergency(visitUuid));
//        } catch (DAOException e) {
//            e.printStackTrace();
//        }
//        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//        String query = "Select ifnull(emergency,'') as emergency FROM tbl_visit WHERE uuid = '" + visitUuid + "'";
//        Cursor cursor = db.rawQuery(query, null);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String emergency = cursor.getString(cursor.getColumnIndex("emergency"));
//                if (emergency.equalsIgnoreCase("true") || emergency.equalsIgnoreCase("1")) {
//                    flag.setChecked(true);
//                }
//            }
//        }
//        if (cursor != null) {
//            cursor.close();
//        }
        EncounterDAO encounterDAO = new EncounterDAO();
        String emergencyUuid = "";
        try {
            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }

        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
            flag.setChecked(true);
        }

        physicalDoumentsUpdates();

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        editVitals = findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = findViewById(R.id.imagebutton_edit_pathist);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        uploadButton = findViewById(R.id.button_upload);
        downloadButton = findViewById(R.id.button_download);

        additionalDocumentsDownlaod = findViewById(R.id.imagebutton_download_additional_document);
        onExaminationDownload = findViewById(R.id.imagebutton_download_physexam);


        downloadButton.setEnabled(false);
        downloadButton.setVisibility(View.GONE);
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        if (isPastVisit) {
            editVitals.setVisibility(View.GONE);
            editComplaint.setVisibility(View.GONE);
            editPhysical.setVisibility(View.GONE);
            editFamHist.setVisibility(View.GONE);
            editMedHist.setVisibility(View.GONE);
            editAddDocs.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            String visitIDorderBy = "startdate";
            String visitIDSelection = "uuid = ?";
            String[] visitIDArgs = {visitUuid};
            final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
            if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
                visitIDCursor.moveToFirst();
                visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
            }
            if (visitIDCursor != null) visitIDCursor.close();
            if (visitUUID != null && !visitUUID.isEmpty()) {
                addDownloadButton();

            }

        }
        flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    EncounterDAO encounterDAO = new EncounterDAO();
                    encounterDAO.setEmergency(visitUuid, isChecked);
                } catch (DAOException e) {
                    Crashlytics.getInstance().core.logException(e);
                }
            }
        });
        db.close();
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//                EmergencyEncounterDAO emergencyEncounterDAO = new EmergencyEncounterDAO();

//                if (flag.isChecked()) {
////                    Log.d(TAG, "Emergency flag val: " + flag.isChecked());
////                    String emergency_checked = String.valueOf(flag.isChecked());
////                    String updateQuery = "UPDATE tbl_visit SET emergency ='" + emergency_checked + "' WHERE uuid = '" + visitUuid + "'";
////                    db.execSQL(updateQuery);
////                    db.close();
//                    emergencyEncounterDAO.uploadEncounterEmergency(visitUuid, 0);
//                } else {
////                    db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
////                    Log.d(TAG, "Emergency flag val: " + flag.isChecked());
////                    String emergency_checked = String.valueOf(flag.isChecked());
////                    String updateQuery = "UPDATE tbl_visit SET emergency ='" + emergency_checked + "' WHERE uuid = '" + visitUuid + "'";
////                    db.execSQL(updateQuery);
////                    db.close();
//                    emergencyEncounterDAO.uploadEncounterEmergency(visitUuid, 1);
////                    if (NetworkConnection.isOnline(getApplication())) {
////                        emergencyEncounterDAO.removeEncounterEmergency(visitUuid, db);
////                    }
//                }
                if (patient.getOpenmrs_id() == null || patient.getOpenmrs_id().isEmpty()) {
                    String patientSelection = "uuid = ?";
                    String[] patientArgs = {String.valueOf(patient.getUuid())};
                    db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
                    String table = "tbl_patient";
                    String[] columnsToReturn = {"openmrs_id"};
                    final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);


                    if (idCursor.moveToFirst()) {
                        do {
                            patient.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndex("openmrs_id")));
                        } while (idCursor.moveToNext());
                    }
                    idCursor.close();
                }

                if (patient.getOpenmrs_id() == null || patient.getOpenmrs_id().isEmpty()) {
                }

                if (visitUUID == null || visitUUID.isEmpty()) {
                    String visitIDSelection = "uuid = ?";
                    String[] visitIDArgs = {visitUuid};
                    final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
                    if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                        visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                    }
                    if (visitIDCursor != null)
                        visitIDCursor.close();
                }
                db.close();
                db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
                String[] columnsToReturn = {"startdate"};
                String visitIDorderBy = "startdate";
                String visitIDSelection = "uuid = ?";
                String[] visitIDArgs = {visitUuid};
                Cursor visitIDCursor1 = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
                visitIDCursor1.moveToLast();
                String startDateTime = visitIDCursor1.getString(visitIDCursor1.getColumnIndexOrThrow("startdate"));
                visitIDCursor1.close();

                if (!flag.isChecked()) {
                    //
                }
                Snackbar.make(view, "Uploading to doctor.", Snackbar.LENGTH_LONG).show();
                if (NetworkConnection.isOnline(getApplication())) {
                    AppConstants.notificationUtils.showNotifications("Visit Data Upload", "Uploading visit data", VisitSummaryActivity.this);
                    PullDataDAO pullDataDAO = new PullDataDAO();
                    ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Added the 4 sec delay and then push data.For some reason doing immediately does not work
                            //Do something after 100ms
//                            pullDataDAO.pushDataApi();
//                            imagesPushDAO.patientProfileImagesPush();
//                            imagesPushDAO.obsImagesPush();
//                            pullDataDAO.pullData(VisitSummaryActivity.this);
                            SyncUtils syncUtils = new SyncUtils();
                            boolean isSynced = syncUtils.syncForeground();
                            if (isSynced)
                                AppConstants.notificationUtils.DownloadDone("Visit Data Upload", "Uploaded visit data", VisitSummaryActivity.this);
                            else
                                AppConstants.notificationUtils.DownloadDone("Visit Data Upload", "failed to Uploaded", VisitSummaryActivity.this);
                        }
                    }, 4000);
                } else {
                    AppConstants.notificationUtils.showNotifications("Visit Data Upload", "Check your connectivity", VisitSummaryActivity.this);
                }
            }

        });

        if (intentTag != null && intentTag.equals("prior")) {
            uploadButton.setEnabled(false);
        }


        queryData(String.valueOf(patientUuid));
        nameView = findViewById(R.id.textView_name_value);

        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            idView.setText(patient.getOpenmrs_id());
        } else {
            idView.setText(getString(R.string.patient_not_registered));
        }

        nameView.setText(patientName);

        heightView = findViewById(R.id.textView_height_value);
        weightView = findViewById(R.id.textView_weight_value);
        pulseView = findViewById(R.id.textView_pulse_value);
        bpView = findViewById(R.id.textView_bp_value);
        tempView = findViewById(R.id.textView_temp_value);

//        textView_temp
        tempfaren = findViewById(R.id.textView_temp_faren);
        tempcel = findViewById(R.id.textView_temp);
        try {
            JSONObject obj = null;
//            #630
            if (hasLicense) {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, VisitSummaryActivity.this)); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity.this, mFileName)));
            }
            if (obj.getBoolean("mCelsius")) {
                tempcel.setVisibility(View.VISIBLE);
                tempfaren.setVisibility(View.GONE);

            } else if (obj.getBoolean("mFahrenheit")) {
                tempfaren.setVisibility(View.VISIBLE);
                tempcel.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        spO2View = findViewById(R.id.textView_pulseox_value);
        respiratory = findViewById(R.id.textView_respiratory_value);
        bmiView = findViewById(R.id.textView_bmi_value);
        complaintView = findViewById(R.id.textView_content_complaint);
        famHistView = findViewById(R.id.textView_content_famhist);
        patHistView = findViewById(R.id.textView_content_pathist);
        physFindingsView = findViewById(R.id.textView_content_physexam);

        heightView.setText(height.getValue());
        weightView.setText(weight.getValue());
        pulseView.setText(pulse.getValue());

        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        if (bpText.equals("/")) {
            bpView.setText("");
        } else {
            bpView.setText(bpText);
        }

        Log.d(TAG, "onCreate: " + weight.getValue());
        String mWeight = weight.getValue();
        String mHeight = height.getValue();
        if ((mHeight != null && mWeight != null) && !mHeight.isEmpty() && !mWeight.isEmpty()) {
            double numerator = Double.parseDouble(mWeight) * 10000;
            double denominator = Double.parseDouble(mHeight) * Double.parseDouble(mHeight);
            double bmi_value = numerator / denominator;
            mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);
        } else {
            mBMI = "";
        }
        patHistory.setValue(medHistory);

        bmiView.setText(mBMI);
        tempView.setText(temperature.getValue());
        //    Respiratory added by mahiti dev team
        respiratory.setText(resp.getValue());
        spO2View.setText(spO2.getValue());
        if (complaint.getValue() != null)
            complaintView.setText(Html.fromHtml(complaint.getValue()));
        if (famHistory.getValue() != null)
            famHistView.setText(Html.fromHtml(famHistory.getValue()));
        if (patHistory.getValue() != null)
            patHistView.setText(Html.fromHtml(patHistory.getValue()));
        if (phyExam.getValue() != null)
            physFindingsView.setText(Html.fromHtml(phyExam.getValue()));

        SharedPreferences.Editor e = sharedPreferences.edit();


        editVitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(VisitSummaryActivity.this, VitalsActivity.class);
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent1.putExtra("name", patientName);
                //   intent.putStringArrayListExtra("exams", physicalExams);
                intent1.putExtra("tag", "edit");
                startActivity(intent1);
            }
        });

        editFamHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder famHistDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                famHistDialog.setTitle(getString(R.string.visit_summary_family_history));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                famHistDialog.setView(convertView);

                final TextView famHistText = convertView.findViewById(R.id.textView_entry);
                if (famHistory.getValue() != null)
                    famHistText.setText(Html.fromHtml(famHistory.getValue()));
                famHistText.setEnabled(false);

                famHistDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog.Builder textInput = new AlertDialog.Builder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (famHistory.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(famHistory.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                famHistory.setValue(dialogEditText.getText().toString());
                                if (famHistory.getValue() != null) {
                                    famHistText.setText(Html.fromHtml(famHistory.getValue()));
                                    famHistView.setText(Html.fromHtml(famHistory.getValue()));
                                }
                                updateDatabase(famHistory.getValue(), UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        textInput.show();
                        dialogInterface.dismiss();
                    }
                });

                famHistDialog.setNeutralButton(getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                famHistDialog.setNegativeButton(R.string.generic_erase_redo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent1 = new Intent(VisitSummaryActivity.this, FamilyHistoryActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", "edit");
                        startActivity(intent1);
                        dialogInterface.dismiss();
                    }
                });

                famHistDialog.show();
            }
        });

        editComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder complaintDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                complaintDialog.setTitle(getString(R.string.visit_summary_complaint));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                complaintDialog.setView(convertView);

                final TextView complaintText = convertView.findViewById(R.id.textView_entry);
                if (complaint.getValue() != null)
                    complaintText.setText(Html.fromHtml(complaint.getValue()));
                complaintText.setEnabled(false);

                complaintDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog.Builder textInput = new AlertDialog.Builder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (complaint.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(complaint.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                complaint.setValue(dialogEditText.getText().toString());
                                if (complaint.getValue() != null) {
                                    complaintText.setText(Html.fromHtml(complaint.getValue()));
                                    complaintView.setText(Html.fromHtml(complaint.getValue()));
                                }
                                updateDatabase(complaint.getValue(), UuidDictionary.CURRENT_COMPLAINT);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        textInput.show();
                        dialogInterface.dismiss();
                    }
                });

                complaintDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(VisitSummaryActivity.this, ComplaintNodeActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", "edit");
                        startActivity(intent1);
                        dialogInterface.dismiss();
                    }
                });

                complaintDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                complaintDialog.show();
            }
        });

        editPhysical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder physicalDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                physicalDialog.setTitle(getString(R.string.visit_summary_on_examination));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                physicalDialog.setView(convertView);

                final TextView physicalText = convertView.findViewById(R.id.textView_entry);
                if (phyExam.getValue() != null)
                    physicalText.setText(Html.fromHtml(phyExam.getValue()));
                physicalText.setEnabled(false);

                physicalDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog.Builder textInput = new AlertDialog.Builder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (phyExam.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(phyExam.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                phyExam.setValue(dialogEditText.getText().toString());
                                if (phyExam.getValue() != null) {
                                    physicalText.setText(Html.fromHtml(phyExam.getValue()));
                                    physFindingsView.setText(Html.fromHtml(phyExam.getValue()));
                                }
                                updateDatabase(phyExam.getValue(), UuidDictionary.PHYSICAL_EXAMINATION);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        textInput.show();
                        dialogInterface.dismiss();
                    }
                });

                physicalDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (phyExamDir.exists()) {
                            String[] children = phyExamDir.list();
                            String[] childList = children;
                            SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
                            for (String child : childList) {
                                new File(phyExamDir, child).delete();

//                                String[] coloumns = {"uuid","image_path"};
//                                String[] selectionArgs = {phyExamDir.getAbsolutePath() + File.separator + child};
                                ImagesDAO imagesDAO = new ImagesDAO();
                                try {
                                    imagesDAO.deleteImageFromDatabase(io.intelehealth.client.utilities.StringUtils.getFileNameWithoutExtensionString(child));
                                } catch (DAOException e1) {
                                    e1.printStackTrace();
                                }
//                                Cursor cursor = localdb.query("tbl_image_records", coloumns, "image_path = ?", selectionArgs, null, null, null);
//                                if (cursor != null && cursor.moveToFirst()) {
//                                    String parse_id = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
//                                    if (parse_id != null && !parse_id.isEmpty()) {
//                                        ContentValues contentValues = new ContentValues();
//                                        contentValues.put("delete_status", 1);
//                                        String[] whereArgs = {parse_id};
//                                        localdb.update("image_records", contentValues, "parse_id = ?", whereArgs);
//                                    } else {
//                                        localdb.execSQL("DELETE FROM image_records WHERE image_path=" +
//                                                "'" + phyExamDir.getAbsolutePath() + File.separator + child + "'");
//                                    }
//                                }
                            }
                            phyExamDir.delete();
                            localdb.close();
                        }
                        Intent intent1 = new Intent(VisitSummaryActivity.this, PhysicalExamActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", "edit");
                        //    intent1.putStringArrayListExtra("exams", physicalExams);
                        for (String string : physicalExams)
                            Log.i(TAG, "onClick: " + string);
                        startActivity(intent1);
                        dialogInterface.dismiss();
                    }
                });

                physicalDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                physicalDialog.show();
            }
        });

        editMedHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder historyDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                historyDialog.setTitle(getString(R.string.visit_summary_medical_history));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                historyDialog.setView(convertView);

                final TextView historyText = convertView.findViewById(R.id.textView_entry);
                if (patHistory.getValue() != null)
                    historyText.setText(Html.fromHtml(patHistory.getValue()));
                historyText.setEnabled(false);

                historyDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog.Builder textInput = new AlertDialog.Builder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (patHistory.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(patHistory.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                patHistory.setValue(dialogEditText.getText().toString());
                                if (patHistory.getValue() != null) {
                                    historyText.setText(Html.fromHtml(patHistory.getValue()));
                                    patHistView.setText(Html.fromHtml(patHistory.getValue()));
                                }
                                updateDatabase(patHistory.getValue(), UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        textInput.show();
                        dialogInterface.dismiss();
                    }
                });

                historyDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(VisitSummaryActivity.this, PastMedicalHistoryActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", "edit");
                        startActivity(intent1);
                        dialogInterface.dismiss();
                    }
                });

                historyDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                historyDialog.show();
            }
        });

        editAddDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addDocs = new Intent(VisitSummaryActivity.this, AdditionalDocumentsActivity.class);
                addDocs.putExtra("patientUuid", patientUuid);
                addDocs.putExtra("visitUuid", visitUuid);
                addDocs.putExtra("encounterUuidVitals", encounterVitals);
                addDocs.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                startActivity(addDocs);
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PullDataDAO pullDataDAO = new PullDataDAO();
                boolean pull = pullDataDAO.pullData(VisitSummaryActivity.this);
                if (pull)
                    AppConstants.notificationUtils.DownloadDone("download from doctor", "prescription Downloaded", VisitSummaryActivity.this);
                else
                    AppConstants.notificationUtils.DownloadDone("download from doctor", "no prescription Downloaded", VisitSummaryActivity.this);
                if (!downloaded)
                    downloadPrescription();
                else
                    return;

                //mLayout.addView(downloadButton, mLayout.getChildCount());
            }
        });
        additionalDocumentsDownlaod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_AD);
            }
        });
        onExaminationDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_PE);
            }
        });
    }

    private void physicalDoumentsUpdates() {

        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE);
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + "/" + fileuuid + ".jpg";
                fileList.add(new File(filename));
            }
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mPhysicalExamsLayoutManager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false);
            mPhysicalExamsRecyclerView.setLayoutManager(mPhysicalExamsLayoutManager);
            mPhysicalExamsRecyclerView.setAdapter(horizontalAdapter);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
//        if (!phyExamDir.exists()) {
//            phyExamDir.mkdirs();
//            Log.v(TAG, "directory ceated " + phyExamDir.getAbsolutePath());
//        } else {
//            File[] files = phyExamDir.listFiles();
//            List<File> fileList = Arrays.asList(files);
//            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
//            mPhysicalExamsLayoutManager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false);
//            mPhysicalExamsRecyclerView.setLayoutManager(mPhysicalExamsLayoutManager);
//            mPhysicalExamsRecyclerView.setAdapter(horizontalAdapter);
//        }
    }

    private void startDownload(String imageType) {

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
        intent.putExtra("ImageType", imageType);
        startService(intent);

    }


    private String stringToWeb(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</p>";
            formatted = para_open + Node.big_bullet +
                    input.replaceAll("\n", para_close + para_open + Node.big_bullet)
                    + para_close;
        }

        return formatted;
    }

    private void doWebViewPrint() throws ParseException {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Patient WebView", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        String mPatientName = patient.getFirst_name() + " " + patient.getMiddle_name() + " " + patient.getLast_name();
        String mPatientOpenMRSID = patient.getOpenmrs_id();
        String mPatientDob = patient.getDate_of_birth();
        String mAddress = patient.getAddress1() + "\n" + patient.getAddress2();
        String mCityState = patient.getCity_village();
        String mPhone = patient.getPhone_number();
        String mState = patient.getState_province();
        String mCountry = patient.getCountry();

        String mSdw = patient.getSdw();
        String mOccupation = patient.getOccupation();
        String mGender = patient.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        String[] columnsToReturn = {"startdate"};
        String visitIDorderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {visitUuid};
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);

        String mPatHist = patHistory.getValue();
        if (mPatHist == null) {
            mPatHist = "";
        }
        String mFamHist = famHistory.getValue();
        if (mFamHist == null) {
            mFamHist = "";
        }
        mHeight = height.getValue();
        mWeight = weight.getValue();
        mBP = bpSys.getValue() + "/" + bpDias.getValue();
        mPulse = pulse.getValue();
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, this)); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }//Load the config file

            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    mTemp = "Temperature(C): " + temperature.getValue();

                } else if (obj.getBoolean("mFahrenheit")) {

                    mTemp = "Temperature(F): " + temperature.getValue();
                }
            }
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
        }
//        if (temperature.getValue() != null) {
//            if (Integer.parseInt(temperature.getValue()) > 80) {
//                mTemp = "Temperature(F): " + temperature.getValue();
//            } else {
//                mTemp = "Temperature(C): " + temperature.getValue();
//            }
//        }else{
//            mTemp="";
//        }

//        mTemp = temperature.getValue();

        mresp = resp.getValue();
        mSPO2 = "SpO2(%): " + spO2.getValue();
        String mComplaint = complaint.getValue();

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = StringUtils.split(mComplaint, Node.bullet_arrow);
        mComplaint = "";
        String colon = ":";
        if (complaints != null) {
            for (String comp : complaints) {
                if (!comp.trim().isEmpty()) {
                    mComplaint = mComplaint + Node.big_bullet + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                }
            }
            if (!mComplaint.isEmpty()) {
                mComplaint = mComplaint.substring(0, mComplaint.length() - 2);
                mComplaint = mComplaint.replaceAll("<b>", "");
                mComplaint = mComplaint.replaceAll("</b>", "");
            }
        }

        if (mPatientOpenMRSID == null) {
            mPatientOpenMRSID = getString(R.string.patient_not_registered);
        }

        String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
        String para_close = "</p>";


        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(mPatientDob);
        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        String rx_web = stringToWeb(rxReturned);

        String tests_web = stringToWeb(testsReturned);

        String advice_web = stringToWeb(adviceReturned);

        String diagnosis_web = stringToWeb(diagnosisReturned);

        String comments_web = stringToWeb(additionalReturned);

        String followUp_web = stringToWeb(followUpDate);

        String doctor_web = stringToWeb(doctorName);

        String heading = prescription1;
        String heading2 = prescription2;
        String heading3 = "<br/>";

        String bp = mBP;
        if (bp.equals("/")) bp = "";

        String address = mAddress + " " + mCityState + " " + mPhone;

        String fam_hist = mFamHist;
        String pat_hist = mPatHist;

        if (fam_hist.trim().isEmpty()) {
            fam_hist = "No history of illness in family provided.";
        } else {
            fam_hist = fam_hist.replaceAll(Node.bullet, Node.big_bullet);
        }

        if (pat_hist.trim().isEmpty()) {
            pat_hist = "No history of patient's illness provided.";
        }

        // Generate an HTML document on the fly:
        String htmlDocument =
                String.format("<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s | Son/Daughter/Wife of: %s </p>" +
                                "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\"><b>Address and Contact:</b> %s</p>" +
                                "<b><p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p></b><br><br>" +
                                "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p>" +
                                "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p>" +
                                "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p>" +
                                "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Chief Complaint(s)</p></b>" +
                                para_open + "%s" + para_close + "<br><br>" +
                                "<b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b>" +
                                "%s" +
                                "<b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication</p></b>" +
                                "%s" +
                                "<b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Tests To Be Performed</p></b>" +
                                "%s" +
                                "<b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b>" +
                                "%s" +
                                "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
                                "%s" +
                                "<b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b>" +
                                "%s"
                        , heading, heading2, heading3, mPatientName, age, mGender, mSdw, address, mPatientOpenMRSID, mDate, mHeight, mWeight,
                        mBMI, bp, mPulse, mTemp, mresp, mSPO2, pat_hist, fam_hist, mComplaint, diagnosis_web, rx_web, tests_web, advice_web, comments_web, followUp_web, doctor_web);
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
        db.close();
    }

    /**
     * This method creates a print job using PrintManager instance and PrintAdapter Instance
     *
     * @param webView object of type WebView.
     */
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Visit Summary";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    private void endVisit() {
        Log.d(TAG, "endVisit: ");
        if (visitUUID == null || visitUUID.isEmpty()) {
            String visitIDorderBy = "startdate";
            String visitIDSelection = "uuid = ?";
            String[] visitIDArgs = {visitUuid};
            final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
            if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
                visitIDCursor.moveToFirst();
                visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
            }
            if (visitIDCursor != null) visitIDCursor.close();
        }
        if (visitUUID != null && !visitUUID.isEmpty()) {
            if (followUpDate != null && !followUpDate.isEmpty()) {
                AlertDialog.Builder followUpAlert = new AlertDialog.Builder(VisitSummaryActivity.this);
                followUpAlert.setMessage(getString(R.string.visit_summary_follow_up_reminder) + followUpDate);
                followUpAlert.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(VisitSummaryActivity.this, PatientSurveyActivity.class);
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
                });
                followUpAlert.show();
            } else {
                Intent intent = new Intent(VisitSummaryActivity.this, PatientSurveyActivity.class);
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
        } else {

            Log.d(TAG, "endVisit: null");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VisitSummaryActivity.this);
            alertDialogBuilder.setMessage(getString(R.string.visit_summary_upload_reminder));
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }


    /**
     * This methods retrieves patient data from database.
     *
     * @param dataString variable of type String
     * @return void
     */

    public void queryData(String dataString) {
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};

        String table = "tbl_patient";
        String[] columnsToReturn = {"openmrs_id", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province", "country",
                "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndex("openmrs_id")));
                patient.setFirst_name(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddle_name(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLast_name(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDate_of_birth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndex("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndex("address2")));
                patient.setCity_village(idCursor.getString(idCursor.getColumnIndex("city_village")));
                patient.setState_province(idCursor.getString(idCursor.getColumnIndex("state_province")));
                patient.setCountry(idCursor.getString(idCursor.getColumnIndex("country")));
                patient.setPostal_code(idCursor.getString(idCursor.getColumnIndex("postal_code")));
                patient.setPhone_number(idCursor.getString(idCursor.getColumnIndex("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient.setPatient_photo(idCursor.getString(idCursor.getColumnIndex("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();
        db.close();
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        PatientsDAO patientsDAO = new PatientsDAO();
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    Crashlytics.getInstance().core.logException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
        db.close();
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] columns = {"value", " conceptuuid"};

        try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ?";
            String[] famHistArgs = {encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
            Cursor famHistCursor = db.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            String famHistText = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistory.setValue(famHistText);
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            famHistory.setValue(""); // if family history does not exist
        }

        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ?";

            String[] medHistArgs = {encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};

            Cursor medHistCursor = db.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            patHistory.setValue(medHistText);

            if (medHistText != null && !medHistText.isEmpty()) {

                medHistory = patHistory.getValue();


                medHistory = medHistory.replace("\"", "");
                medHistory = medHistory.replace("\n", "");
                do {
                    medHistory = medHistory.replace("  ", "");
                } while (medHistory.contains("  "));
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            patHistory.setValue(""); // if medical history does not exist
        }

//        String visitSelection = "encounteruuid = ?";
//        String[] visitArgs = {encounterVitals};
//        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
//        if (visitCursor.moveToFirst()) {
//            do {
//                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
//                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
//                parseData(dbConceptID, dbValue);
//            } while (visitCursor.moveToNext());
//        }
//        visitCursor.close();

        String encounterselection = "encounteruuid = ? AND conceptuuid != ? AND conceptuuid != ?";
        String[] encounterargs = {encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE};
        Cursor encountercursor = db.query("tbl_obs", columns, encounterselection, encounterargs, null, null, null);
        if (encountercursor.moveToFirst()) {
            do {
                String dbConceptID = encountercursor.getString(encountercursor.getColumnIndex("conceptuuid"));
                String dbValue = encountercursor.getString(encountercursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (encountercursor.moveToNext());
        }
        encountercursor.close();
//    setup the downloaded prescription
        downloadPrescription();
    }

    /**
     * This method distinguishes between different concepts using switch case to populate the information into the relevant sections (eg:complaints, physical exam, vitals, etc.).
     *
     * @param concept_id variable of type int.
     * @param value      variable of type String.
     */
    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.CURRENT_COMPLAINT: { //Current Complaint
                complaint.setValue(value);
                break;
            }
            case UuidDictionary.PHYSICAL_EXAMINATION: { //Physical Examination
                phyExam.setValue(value);
                break;
            }
            case UuidDictionary.HEIGHT: //Height
            {
                height.setValue(value);
                break;
            }
            case UuidDictionary.WEIGHT: //Weight
            {
                weight.setValue(value);
                break;
            }
            case UuidDictionary.PULSE: //Pulse
            {
                pulse.setValue(value);
                break;
            }
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
            {
                bpSys.setValue(value);
                break;
            }
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
            {
                bpDias.setValue(value);
                break;
            }
            case UuidDictionary.TEMPERATURE: //Temperature
            {
                temperature.setValue(value);
                break;
            }
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
            {
                resp.setValue(value);
                break;
            }
            case UuidDictionary.SPO2: //SpO2
            {
                spO2.setValue(value);
                break;
            }
            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {
                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = diagnosisReturned + "," + value;
                } else {
                    diagnosisReturned = value;
                }
                if (diagnosisCard.getVisibility() != View.VISIBLE) {
                    diagnosisCard.setVisibility(View.VISIBLE);
                }
                diagnosisTextView.setText(diagnosisReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.JSV_MEDICATIONS: {
                Log.i(TAG, "parseData: val:" + value);
                Log.i(TAG, "parseData: rx" + rxReturned);
                if (!rxReturned.trim().isEmpty()) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
                Log.i(TAG, "parseData: rxfin" + rxReturned);
                if (prescriptionCard.getVisibility() != View.VISIBLE) {
                    prescriptionCard.setVisibility(View.VISIBLE);
                }
                prescriptionTextView.setText(rxReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.MEDICAL_ADVICE: {
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = adviceReturned + "," + value;
                } else {
                    adviceReturned = value;
                }
                if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }
                medicalAdviceTextView.setText(adviceReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "," + value;
                } else {
                    testsReturned = value;
                }
                if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }
                requestedTestsTextView.setText(testsReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.ADDITIONAL_COMMENTS: {
                if (!additionalReturned.isEmpty()) {
                    additionalReturned = additionalReturned + "," + value;
                } else {
                    additionalReturned = value;
                }
                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
                    additionalCommentsCard.setVisibility(View.VISIBLE);
                }
                additionalCommentsTextView.setText(additionalReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.FOLLOW_UP_VISIT: {
                if (!followUpDate.isEmpty()) {
                    followUpDate = followUpDate + "," + value;
                } else {
                    followUpDate = value;
                }
                if (followUpDateCard.getVisibility() != View.VISIBLE) {
                    followUpDateCard.setVisibility(View.VISIBLE);
                }
                followUpDateTextView.setText(followUpDate);
                //checkForDoctor();
                break;
            }


            default:
                Log.i(TAG, "parseData: " + value);
                break;

        }
    }


    /**
     * @param title   variable of type String
     * @param content variable of type String
     * @param index   variable of type int
     */
    private void createNewCardView(String title, String content, int index) {
        final LayoutInflater inflater = VisitSummaryActivity.this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.card_doctor_content, null);
        TextView titleView = convertView.findViewById(R.id.textview_heading);
        TextView contentView = convertView.findViewById(R.id.textview_content);
        titleView.setText(title);
        contentView.setText(content);
        mLayout.addView(convertView, index);
    }

    /**
     * This method updates patient details to database.
     *
     * @param string    variable of type String
     * @param conceptID variable of type int
     */

    private void updateDatabase(String string, String conceptID) {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("value", string);

        String selection = "encounteruuid = ? AND conceptuuid = ?";
        String[] args = {encounterUuid, String.valueOf(conceptID)};

        localdb.update(
                "tbl_obs",
                contentValues,
                selection,
                args
        );

    }


    public void callBroadcastReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            receiver = new NetworkChangeReceiver();
            registerReceiver(receiver, filter);
            isReceiverRegistered = true;
        }
    }

    @Override
    public void onResume() // register the receiver here
    {
        super.onResume();

        callBroadcastReceiver();

        String filePathAddDoc = baseDir + File.separator + "Patient Images" + File.separator + patientUuid + File.separator +
                visitUuid + File.separator + additionalDocumentDir;


        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                fileList.add(new File(filename));
                Logger.logD(TAG, "Image name:" + filename);
            }
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mAdditionalDocsLayoutManager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false);
            mAdditionalDocsRecyclerView.setLayoutManager(mAdditionalDocsLayoutManager);
            mAdditionalDocsRecyclerView.setAdapter(horizontalAdapter);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
//        if (!addDocDir.exists()) {
//            addDocDir.mkdirs();
//            Log.v(TAG, "directory created " + addDocDir.getAbsolutePath());
//        } else {
//            File[] files = addDocDir.listFiles();
//            addDocDir.listFiles(new FilenameFilter() {
//                @Override
//                public boolean accept(File dir, String name) {
//                    return false;
//                }
//            })
        //List<File> fileList = Arrays.asList(files);


//    }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        isReceiverRegistered = false;
    }


    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }

    }


    public void sendSMS() {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        textInput.setTitle(R.string.identification_screen_prompt_phone_number);
        final EditText phoneNumberEditText = new EditText(context);
        phoneNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            phoneNumberEditText.setTextColor(getColor(R.color.gray));
        } else {
            phoneNumberEditText.setTextColor(getResources().getColor(R.color.gray));
        }

        textInput.setView(phoneNumberEditText);

        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String body = "";
                String header = "";
                String message = "";

                String openMRSID = patient.getOpenmrs_id();
                if (openMRSID == null) {
                    openMRSID = getString(R.string.patient_not_registered);
                }

                header = "Patient Id: " + patient.getOpenmrs_id() + "\n"
                        + "Patient Name: " + patient.getFirst_name() + " " + patient.getLast_name() + "\n"
                        + "Patient DOB: " + patient.getDate_of_birth() + "\n";


                if (diagnosisCard.getVisibility() == View.VISIBLE) {
                    if (!diagnosisTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_diagnosis) + ":" +
                                diagnosisTextView.getText().toString() + "\n";
                }
                if (prescriptionCard.getVisibility() == View.VISIBLE) {
                    if (!prescriptionTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_rx) + ":" +
                                prescriptionTextView.getText().toString() + "\n";
                }
                if (medicalAdviceCard.getVisibility() == View.VISIBLE) {
                    if (!medicalAdviceTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_advice) + ":" +
                                medicalAdviceTextView.getText().toString() + "\n";
                }
                if (requestedTestsCard.getVisibility() == View.VISIBLE) {
                    if (!requestedTestsTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_tests_prescribed) + ":" +
                                requestedTestsTextView.getText().toString() + "\n";
                }
                if (additionalCommentsCard.getVisibility() == View.VISIBLE) {
                    if (!additionalCommentsTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_additional_comments) + ":" +
                                additionalCommentsTextView.getText().toString() + "\n";
                }
                if (followUpDateCard.getVisibility() == View.VISIBLE) {
                    if (!followUpDateTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_follow_up_date) + ":" +
                                followUpDateTextView.getText().toString() + "\n";
                }


                if (!phoneNumberEditText.getText().toString().trim().isEmpty()) {
                    if (!body.isEmpty()) {
                        if (body != null && body.length() > 0) {
                            body = body.substring(0, body.length() - 2);
                            message = header + body;
                        }
                        try {
                            SmsManager sm = SmsManager.getDefault();
                            String number = phoneNumberEditText.getText().toString();
                            ArrayList<String> parts = sm.divideMessage(message);

                            sm.sendMultipartTextMessage(number, null, parts, null, null);

                            Toast.makeText(getApplicationContext(), getString(R.string.sms_success),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_sms),
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "onClick: " + e.getMessage());
                        }

                    } else {
                        Toast.makeText(context, getString(R.string.error_no_data), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.error_phone_number), Toast.LENGTH_SHORT).show();
                }
            }
        });


        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        textInput.show();
    }

    public void downloadPrescription() {
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ?";
        String[] encounterIDArgs = {visitUuid};
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();
        db.close();
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? ";
        String[] visitArgs = {visitnote};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
        db.close();
        downloaded = true;

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter(FILTER));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

    private void handleMessage(Intent msg) {
        Log.i(TAG, "handleMessage: Entered");
        Bundle data = msg.getExtras();
        int check = data.getInt("Restart");
        if (check == 100) {
            Log.i(TAG, "handleMessage: 100");
            diagnosisReturned = "";
            rxReturned = "";
            testsReturned = "";
            adviceReturned = "";
            additionalReturned = "";
            followUpDate = "";
            String[] columns = {"value", " conceptuuid"};
            String visitSelection = "encounteruuid = ? ";
            String[] visitArgs = {encounterUuid};
            Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
            if (visitCursor.moveToFirst()) {
                do {
                    String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                    String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                    parseData(dbConceptID, dbValue);
                } while (visitCursor.moveToNext());
            }
            visitCursor.close();
        } else if (check == 200) {
            Log.i(TAG, "handleMessage: 200");
            //meera
            // addDownloadButton();

//          <-----  code to end the visit only after doctor sends anything ----->


            //if(downloaded){
            String[] columns = {"concept_id"};
            String orderBy = "visit_id";

            //obscursor checks in obs table
            Cursor obsCursor = db.query("tbl_obs", columns, null, null, null, null, orderBy);

            //dbconceptid will store data found in concept_id

            if (obsCursor.moveToFirst() && obsCursor.getCount() > 1) {
                String dbConceptID = obsCursor.getString(obsCursor.getColumnIndex("conceptuuid"));

//                    if obsCursor founds something move to next
                while (obsCursor.moveToNext()) ;

                switch (dbConceptID) {
                    //case values for each prescription
                    case UuidDictionary.TELEMEDICINE_DIAGNOSIS:
                        Log.i(TAG, "found diagnosis");
                        break;
                    case UuidDictionary.JSV_MEDICATIONS:
                        Log.i(TAG, "found medications");
                        break;
                    case UuidDictionary.MEDICAL_ADVICE:
                        Log.i(TAG, "found medical advice");
                        break;
                    case UuidDictionary.ADDITIONAL_COMMENTS:
                        Log.i(TAG, "found additional comments");
                        break;
                    case UuidDictionary.REQUESTED_TESTS:
                        Log.i(TAG, "found tests");
                        break;
                    default:
                }
                obsCursor.close();
                addDownloadButton();
                //if any obs  found then end the visit
                //endVisit();

            }
//                    <-----if obs not found restrict user to end the visit ----->
            else {
                Log.i(TAG, "found sothing for test");
           /* downloaded=false;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage(R.string.error_no_data);
                alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();*/

            }

        }

    }


    private void addDownloadButton() {

        if (!downloadButton.isEnabled()) {
            downloadButton.setEnabled(true);
            downloadButton.setVisibility(View.VISIBLE);
            /*
            final float scale = getResources().getDisplayMetrics().density;
            int pixels = (int) (11 * scale + 0.5f);

            downloadButton = new Button((new ContextThemeWrapper(context, R.style.RobotoButtonStyle)));
            LinearLayoutCompat.LayoutParams layoutParams= new LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10,10,10,10);
            downloadButton.setPadding(0,0,0,0);
            downloadButton.setLayoutParams(layoutParams);
            downloadButton.setAllCaps(false);
            downloadButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            downloadButton.setBackgroundColor(getResources().getColor(R.color.divider));
            downloadButton.setText(R.string.visit_summary_button_download);
            */

            // Toast.makeText(this, getString(R.string.visit_summary_button_download), Toast.LENGTH_SHORT).show();


        }

    }

    private void isNetworkAvailable(Context context) {
        int flag = 0;

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            if (internetCheck != null) {
                                internetCheck.setIcon(R.mipmap.ic_data_on);
                                flag = 1;
//                                SyncUtils syncUtils = new SyncUtils();
//                                syncUtils.syncBackground();
                            }
                        }
                    }
                }
            }
        }

        if (flag == 0) {
            if (internetCheck != null) {
                internetCheck.setIcon(R.mipmap.ic_data_off);
            }

        }

    }

}
