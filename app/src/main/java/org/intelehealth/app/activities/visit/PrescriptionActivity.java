package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.utilities.DateAndTimeUtils.date_formatter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.HorizontalAdapter;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Prajwal Waingankar on 4/11/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class PrescriptionActivity extends AppCompatActivity {
    private String patientName, patientUuid, gender, age, openmrsID, vitalsUUID, adultInitialUUID, intentTag,
            visitID, visit_startDate, visit_speciality, patient_photo_path, chief_complaint_value;
    private ImageButton btn_up_header, btnup_drdetails_header, btnup_diagnosis_header, btnup_medication_header,
            btnup_test_header, btnup_speciality_header, btnup_followup_header, no_btn, yes_btn, downloadBtn;
    private RelativeLayout vs_header_expandview, vs_drdetails_header_expandview,
            vs_diagnosis_header_expandview, vs_medication_header_expandview, vs_testheader_expandview,
            vs_speciality_header_expandview, vs_followup_header_expandview;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time,
            mCHWname, drname, dr_age_gender, qualification, dr_speciality,
            diagnosis_txt, medication_txt, test_txt, referred_speciality_txt, no_followup_txt, followup_date_txt, followup_subtext;
    private ImageView priorityTag, profile_image;
    private SessionManager sessionManager;
    String diagnosisReturned = "", rxReturned = "", testsReturned = "", adviceReturned = "", doctorName = "",
            additionalReturned = "", followUpDate = "";
    private SQLiteDatabase db;
    private Patient patient = new Patient();
    private String hasPrescription = "";
    boolean downloaded = false;
    String encounterUuid;
    DownloadPrescriptionService downloadPrescriptionService;
    Boolean isReceiverRegistered = false;
    Context context;
    NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    boolean uploaded = false;
    MenuItem internetCheck = null;
    String visitnoteencounteruuid = "";
    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription2);

        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        fetchIntent();
        setDataToView();
        expandableCardVisibilityHandling();
    }

    private void initUI() {
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

        patName_txt = findViewById(R.id.textView_name_value);
        profile_image = findViewById(R.id.profile_image);
        gender_age_txt = findViewById(R.id.textView_gender_value);
        openmrsID_txt = findViewById(R.id.textView_id_value);
        mCHWname = findViewById(R.id.chw_details);
        visitID_txt = findViewById(R.id.textView_visit_value);

        drname = findViewById(R.id.drname);
        dr_age_gender = findViewById(R.id.dr_age_gender);
        qualification = findViewById(R.id.qualification);
        dr_speciality = findViewById(R.id.dr_speciality);

        diagnosis_txt = findViewById(R.id.diagnosis_txt);
        medication_txt = findViewById(R.id.medication_txt);
        test_txt = findViewById(R.id.test_txt);
        referred_speciality_txt = findViewById(R.id.referred_speciality_txt);
        no_followup_txt = findViewById(R.id.no_followup_txt);
        followup_date_txt = findViewById(R.id.followup_date_txt);
        followup_subtext = findViewById(R.id.followup_info);

        no_btn = findViewById(R.id.no_btn);
        yes_btn = findViewById(R.id.yes_btn);
        downloadBtn = findViewById(R.id.downloadBtn);

        btn_up_header = findViewById(R.id.btn_up_header);
        btnup_drdetails_header = findViewById(R.id.btnup_drdetails_header);
        btnup_diagnosis_header = findViewById(R.id.btnup_diagnosis_header);
        btnup_medication_header = findViewById(R.id.btnup_medication_header);
        btnup_test_header = findViewById(R.id.btnup_test_header);
        btnup_speciality_header = findViewById(R.id.btnup_speciality_header);
        btnup_followup_header = findViewById(R.id.btnup_followup_header);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_drdetails_header_expandview = findViewById(R.id.vs_drdetails_header_expandview);
        vs_diagnosis_header_expandview = findViewById(R.id.vs_diagnosis_header_expandview);
        vs_medication_header_expandview = findViewById(R.id.vs_medication_header_expandview);
        vs_testheader_expandview = findViewById(R.id.vs_testheader_expandview);
        vs_speciality_header_expandview = findViewById(R.id.vs_speciality_header_expandview);
        vs_followup_header_expandview = findViewById(R.id.vs_followup_header_expandview);
    }

    private void fetchIntent() {
        Intent intent = this.getIntent(); // The intent was passed to the activity
        sessionManager = new SessionManager(this);
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            patientUuid = intent.getStringExtra("patientUuid");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");
            Log.d("TAG", "getAge_FollowUp: s : "+age);
            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            vitalsUUID = intent.getStringExtra("encounterUuidVitals");
            adultInitialUUID = intent.getStringExtra("encounterUuidAdultIntial");
            visit_startDate = intent.getStringExtra("visit_startDate");
            patient_photo_path = intent.getStringExtra("patient_photo");
            intentTag = intent.getStringExtra("tag");
        }
    }

    private void setDataToView() {
        // settind data - start
        downloadPrescriptionDefault();
        registerDownloadPrescription();

        //get from encountertbl from the encounter
        visitnoteencounteruuid = getStartVisitNoteEncounterByVisitUUID(visitID);
        // settind data - end

        // Patient Photo
        profile_image = findViewById(R.id.profile_image);
        if (patient_photo_path != null) {
            Glide.with(this)
                    .load(patient_photo_path)
                    .thumbnail(0.3f)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profile_image);
        }
        else {
            profile_image.setImageDrawable(getResources().getDrawable(R.drawable.avatar1));
        }
        // end

        patName_txt.setText(patientName);
        gender_age_txt.setText(gender + " " + age);
        openmrsID_txt.setText(openmrsID);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider

        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        visitID_txt.setText("XXXX" + hideVisitUUID);

        // dr details - start
        String drDetails = fetchDrDetailsFromLocalDb(visitID);
        parseDoctorDetails(drDetails);
        // dr details - end

        // download btn - start
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getApplication())) {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloading), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
                }

                SyncUtils syncUtils = new SyncUtils();
                syncUtils.syncForeground("downloadPrescription");
                uploaded = true;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadPrescription();
                    }
                }, 5000);
            }
        });

        // download btn - end
    }

    private void expandableCardVisibilityHandling() {
        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_drdetails_header.setOnClickListener(v -> {
            if (vs_drdetails_header_expandview.getVisibility() == View.VISIBLE)
                vs_drdetails_header_expandview.setVisibility(View.GONE);
            else
                vs_drdetails_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_diagnosis_header.setOnClickListener(v -> {
            if (vs_diagnosis_header_expandview.getVisibility() == View.VISIBLE)
                vs_diagnosis_header_expandview.setVisibility(View.GONE);
            else
                vs_diagnosis_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_medication_header.setOnClickListener(v -> {
            if (vs_medication_header_expandview.getVisibility() == View.VISIBLE)
                vs_medication_header_expandview.setVisibility(View.GONE);
            else
                vs_medication_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_test_header.setOnClickListener(v -> {
            if (vs_testheader_expandview.getVisibility() == View.VISIBLE)
                vs_testheader_expandview.setVisibility(View.GONE);
            else
                vs_testheader_expandview.setVisibility(View.VISIBLE);
        });

        btnup_speciality_header.setOnClickListener(v -> {
            if (vs_speciality_header_expandview.getVisibility() == View.VISIBLE)
                vs_speciality_header_expandview.setVisibility(View.GONE);
            else
                vs_speciality_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_followup_header.setOnClickListener(v -> {
            if (vs_followup_header_expandview.getVisibility() == View.VISIBLE)
                vs_followup_header_expandview.setVisibility(View.GONE);
            else
                vs_followup_header_expandview.setVisibility(View.VISIBLE);
        });
    }

    // parse dr details - start
    ClsDoctorDetails details;
    private void parseDoctorDetails(String dbValue) {
        Gson gson = new Gson();
        details = gson.fromJson(dbValue, ClsDoctorDetails.class);
        Log.e("TAG", "TEST VISIT: " + details.toString());

        drname.setText(details.getName());
        dr_age_gender.setText(details.getEmailId());
        qualification.setText(details.getQualification());
        dr_speciality.setText(details.getSpecialization());
    }
    // parse dr details - end

    // parse presc value - start
    /**
     * This method distinguishes between different concepts using switch case to populate the information into the relevant sections (eg:complaints, physical exam, vitals, etc.).
     *
     * @param concept_id variable of type int.
     * @param value      variable of type String.
     */
    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {
                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = diagnosisReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    diagnosisReturned = Node.bullet + " " + value;
                }
                diagnosis_txt.setText(diagnosisReturned);
                break;
            }

            case UuidDictionary.JSV_MEDICATIONS: {
                Log.i("TAG", "parse_va: " + value);
                Log.i("TAG", "parseData: rx" + rxReturned);
                if (!rxReturned.trim().isEmpty()) {
                    rxReturned = rxReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    rxReturned = Node.bullet + " " + value;
                }
                Log.i("TAG", "parseData: rxfin" + rxReturned);
                medication_txt.setText(rxReturned);
                //checkForDoctor();
                break;
            }

//            case UuidDictionary.MEDICAL_ADVICE: {
//                if (!adviceReturned.isEmpty()) {
//                    adviceReturned = adviceReturned + "\n" + value;
//                    Log.d("GAME", "GAME: " + adviceReturned);
//                } else {
//                    adviceReturned = value;
//                    Log.d("GAME", "GAME_2: " + adviceReturned);
//                }
//                if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
//                    medicalAdviceCard.setVisibility(View.VISIBLE);
//                }
//                //medicalAdviceTextView.setText(adviceReturned);
//                Log.d("Hyperlink", "hyper_global: " + medicalAdvice_string);
//
//                int j = adviceReturned.indexOf('<');
//                int i = adviceReturned.lastIndexOf('>');
//                if (i >= 0 && j >= 0) {
//                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
//                } else {
//                    medicalAdvice_HyperLink = "";
//                }
//
//                Log.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);
//
//                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
//                Log.d("Hyperlink", "hyper_string: " + medicalAdvice_string);
//
//                /*
//                 * variable a contains the hyperlink sent from webside.
//                 * variable b contains the string data (medical advice) of patient.
//                 * */
//               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
//                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
//                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/
//
//                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
//                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
//                medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
//                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
//                Log.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());
//                //checkForDoctor();
//                break;
//            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
                test_txt.setText(testsReturned);
                break;
            }

//            case UuidDictionary.ADDITIONAL_COMMENTS: {
//                additionalCommentsCard.setVisibility(View.GONE);
////                if (!additionalReturned.isEmpty()) {
////                    additionalReturned = additionalReturned + "," + value;
////                } else {
////                    additionalReturned = value;
////                }
//////                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
//////                    additionalCommentsCard.setVisibility(View.VISIBLE);
//////                }
////                additionalCommentsTextView.setText(additionalReturned);
//                //checkForDoctor();
//                break;
//            }

            case UuidDictionary.FOLLOW_UP_VISIT: {
                if (!followUpDate.isEmpty()) {
                    followUpDate = followUpDate + "," + value;
                } else {
                    followUpDate = date_formatter(value.substring(0,10), "dd-MM-yyyy", "dd MMM, yyyy");
                }
                followup_date_txt.setText(followUpDate);
                followup_subtext.setText("The doctor suggested a follow-up visit on " + followUpDate +
                        ". Does the patient want to take a follow-up visit?");
                break;
            }

            default:
                Log.i("TAG", "parseData: " + value);
                break;
        }
    }
    // parse presc value - end

    // downlaod - start
    public void downloadPrescriptionDefault() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitID, "0"}; // so that the deleted values dont come in the presc.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                hasPrescription = "true"; //if any kind of prescription data is present...
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

        downloaded = true;
    }
    // query data - end
    
    // handle - start
    private void handleMessage(Intent msg) {
        Log.i("TAG", "handleMessage: Entered");
        Bundle data = msg.getExtras();
        int check = 0;
        if (data != null) {
            check = data.getInt("Restart");
        }
        if (check == 100) {
            Log.i("TAG", "handleMessage: 100");
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
            Log.i("TAG", "handleMessage: 200");
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
                        Log.i("TAG", "found diagnosis");
                        break;
                    case UuidDictionary.JSV_MEDICATIONS:
                        Log.i("TAG", "found medications");
                        break;
                    case UuidDictionary.MEDICAL_ADVICE:
                        Log.i("TAG", "found medical advice");
                        break;
                    case UuidDictionary.ADDITIONAL_COMMENTS:
                        Log.i("TAG", "found additional comments");
                        break;
                    case UuidDictionary.REQUESTED_TESTS:
                        Log.i("TAG", "found tests");
                        break;
                    default:
                }

                //if any obs  found then end the visit
                //endVisit();

            }
            obsCursor.close();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

    public void registerDownloadPrescription() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("downloadprescription");
        registerReceiver(downloadPrescriptionService, filter);
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
    protected void onStart() {
        registerDownloadPrescription();
        callBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter(FILTER));
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (receiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            receiver = null;
        }
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
            downloadPrescriptionService = null;
        }
        isReceiverRegistered = false;
    }

    @Override
    public void onResume() // register the receiver here
    {
        //get from encountertbl from the encounter
        if (visitnoteencounteruuid.equalsIgnoreCase("")) {
            visitnoteencounteruuid = getStartVisitNoteEncounterByVisitUUID(visitID);
        }

        if (downloadPrescriptionService == null) {
            registerDownloadPrescription();
        }
        super.onResume();

        callBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
        }
        super.onDestroy();

    }
    // handle - end

    public class DownloadPrescriptionService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD("TAG", "Download prescription happen" + new SimpleDateFormat("yyyy MM dd_HH mm ss").format(Calendar.getInstance().getTime()));
            downloadPrescriptionDefault();
            downloadDoctorDetails();
        }
    }

    // downlaod dr - start
    private void downloadDoctorDetails() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitID};
        String encounter_type_uuid_comp = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e";// make the encounter_type_uuid as constant later on.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounter_type_uuid_comp.equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1' ";
        String[] visitArgs = {visitnote};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseDoctorDetails(dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }
    // downlaod dr - end

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }

    }

    // network check - start
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
    // network check - end

    // downlaod presc - start
    public void downloadPrescription() {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            if (visitsDAO.getDownloadedValue(visitID).equalsIgnoreCase("false") && uploaded) {
                String visitnote = "";

                EncounterDAO encounterDAO = new EncounterDAO();
                String encounterIDSelection = "visituuid = ? AND voided = ?";
                String[] encounterIDArgs = {visitID, "0"}; // voided = 0 so that the Deleted values dont come in the presc.
                Cursor encounterCursor = db.query("tbl_encounter", null,
                        encounterIDSelection, encounterIDArgs, null, null, null);
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE")
                                .equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                    } while (encounterCursor.moveToNext());

                }
             //   if (encounterCursor != null) {
                    encounterCursor.close();
             //   }

                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = "";
                    diagnosis_txt.setText("");
                 //   diagnosisCard.setVisibility(View.GONE);
                }
                if (!rxReturned.isEmpty()) {
                    rxReturned = "";
                    medication_txt.setText("");
                  //  prescriptionCard.setVisibility(View.GONE);

                }
              /*  if (!adviceReturned.isEmpty()) {
                    adviceReturned = "";
                    medicalAdviceTextView.setText("");
                    medicalAdviceCard.setVisibility(View.GONE);
                }*/
                if (!testsReturned.isEmpty()) {
                    testsReturned = "";
                    test_txt.setText("");
                  //  requestedTestsCard.setVisibility(View.GONE);
                }
//                if (!additionalReturned.isEmpty()) {
//                    additionalReturned = "";
//                    additionalCommentsTextView.setText("");
//                    additionalCommentsCard.setVisibility(View.GONE);
//                }

                if (!followUpDate.isEmpty()) {
                    followUpDate = "";
                    followup_date_txt.setText("");
                  //  followUpDateCard.setVisibility(View.GONE);
                }

                String[] columns = {"value", " conceptuuid"};
                String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
                String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
                if (visitCursor.moveToFirst()) {
                    do {
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        hasPrescription = "true"; //if any kind of prescription data is present...
                        parseData(dbConceptID, dbValue);
                    } while (visitCursor.moveToNext());
                }
                visitCursor.close();

                if (uploaded) {
                    try {
                        downloaded = visitsDAO.isUpdatedDownloadColumn(visitID, true);
                        Toast.makeText(PrescriptionActivity.this, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                downloadDoctorDetails();
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
    // downlaod presc - end

}