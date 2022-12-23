package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.utilities.DateAndTimeUtils.date_formatter;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.visitSummaryActivity.HorizontalAdapter;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

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
    private LinearLayout presc_profile_header;
    private RelativeLayout dr_details_header_relative, diagnosis_header_relative, medication_header_relative,
            advice_header_relative, test_header_relative, referred_header_relative, followup_header_relative;
    private RelativeLayout vs_header_expandview, vs_drdetails_header_expandview,
            vs_diagnosis_header_expandview, vs_medication_header_expandview,vs_adviceheader_expandview, vs_testheader_expandview,
            vs_speciality_header_expandview, vs_followup_header_expandview, followup_date_block;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time,
            mCHWname, drname, dr_age_gender, qualification, dr_speciality,
            diagnosis_txt, medication_txt, test_txt, advice_txt, referred_speciality_txt, no_followup_txt, followup_date_txt, followup_subtext;
    private ImageView priorityTag, profile_image;
    private SessionManager sessionManager;
    String diagnosisReturned = "", rxReturned = "", testsReturned = "", adviceReturned = "", doctorName = "",
            additionalReturned = "", followUpDate = "";
    String medicalAdvice_string = "", medicalAdvice_HyperLink = "";
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
    Button uploadButton, btn_vs_print, btn_vs_share;
    private WebView mWebView;
    public static String prescription1;
    public static String prescription2;
    boolean hasLicense = false, isRespiratory = false;
    private static String mFileName = "config.json";
    private ImageButton backArrow;
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
        advice_txt = findViewById(R.id.advice_txt);
        test_txt = findViewById(R.id.test_txt);
        referred_speciality_txt = findViewById(R.id.referred_speciality_txt);
        no_followup_txt = findViewById(R.id.no_followup_txt);
        followup_date_txt = findViewById(R.id.followup_date_txt);
        followup_subtext = findViewById(R.id.followup_info);
        followup_date_block = findViewById(R.id.followup_date_block);

        no_btn = findViewById(R.id.no_btn);
        yes_btn = findViewById(R.id.yes_btn);
        downloadBtn = findViewById(R.id.downloadBtn);
        btn_vs_print = findViewById(R.id.btn_vs_print);   // print
        btn_vs_share = findViewById(R.id.btn_vs_share);   // share

        btn_up_header = findViewById(R.id.btn_up_header);
        presc_profile_header = findViewById(R.id.presc_profile_header);
        dr_details_header_relative = findViewById(R.id.dr_details_header_relative);
        diagnosis_header_relative = findViewById(R.id.diagnosis_header_relative);
        medication_header_relative = findViewById(R.id.medication_header_relative);
        advice_header_relative = findViewById(R.id.advice_header_relative);
        test_header_relative = findViewById(R.id.test_header_relative);
        referred_header_relative = findViewById(R.id.referred_header_relative);
        followup_header_relative = findViewById(R.id.followup_header_relative);

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
        vs_adviceheader_expandview = findViewById(R.id.vs_adviceheader_expandview);
        vs_testheader_expandview = findViewById(R.id.vs_testheader_expandview);
        vs_speciality_header_expandview = findViewById(R.id.vs_speciality_header_expandview);
        vs_followup_header_expandview = findViewById(R.id.vs_followup_header_expandview);

        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> {
            finish();
        });
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

        // json based presc header - start
        jsonBasedPrescTitle();
        // json based presc header - end
        
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

        // Follow up - start
        if (followUpDate.equalsIgnoreCase("")) {
            no_followup_txt.setVisibility(View.VISIBLE);
            followup_date_block.setVisibility(View.GONE);
        }
        else {
            no_followup_txt.setVisibility(View.GONE);
            followup_date_block.setVisibility(View.VISIBLE);
        }
        // Follow up - end

        // Bottom Buttons - start
        btn_vs_print.setOnClickListener(v -> {
            try {
                doWebViewPrint_Button();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        btn_vs_share.setOnClickListener(v -> {
            sharePresc();
        });
        // Bottom Buttons - end

        // follow up - yes - start
        yes_btn.setOnClickListener(v -> {
            followupScheduledSuccess(PrescriptionActivity.this, getResources().getDrawable(R.drawable.dialog_visit_sent_success_icon),
                    "Follow up scheduled!",
                    "A follow up for this patient visit has been scheduled successfully.",
                    "Okay");
        });
        // follow up - yes - end
    }

    private void followupScheduledSuccess(Context context, Drawable drawable, String title, String subTitle,
                                        String neutral) {

        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);
        negative_btn.setVisibility(View.GONE);  // as this view requires only one button so other button has hidden.

        icon.setImageDrawable(drawable);
        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        positive_btn.setText(neutral);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        positive_btn.setOnClickListener(v -> {
            /*Intent intent = new Intent(VisitSummaryActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);*/
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void expandableCardVisibilityHandling() {
        presc_profile_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

        dr_details_header_relative.setOnClickListener(v -> {
            if (vs_drdetails_header_expandview.getVisibility() == View.VISIBLE)
                vs_drdetails_header_expandview.setVisibility(View.GONE);
            else
                vs_drdetails_header_expandview.setVisibility(View.VISIBLE);
        });

        diagnosis_header_relative.setOnClickListener(v -> {
            if (vs_diagnosis_header_expandview.getVisibility() == View.VISIBLE)
                vs_diagnosis_header_expandview.setVisibility(View.GONE);
            else
                vs_diagnosis_header_expandview.setVisibility(View.VISIBLE);
        });

        medication_header_relative.setOnClickListener(v -> {
            if (vs_medication_header_expandview.getVisibility() == View.VISIBLE)
                vs_medication_header_expandview.setVisibility(View.GONE);
            else
                vs_medication_header_expandview.setVisibility(View.VISIBLE);
        });

        advice_header_relative.setOnClickListener(v -> { // todo: ddddd
            if (vs_adviceheader_expandview.getVisibility() == View.VISIBLE)
                vs_adviceheader_expandview.setVisibility(View.GONE);
            else
                vs_adviceheader_expandview.setVisibility(View.VISIBLE);
        });

        test_header_relative.setOnClickListener(v -> {
            if (vs_testheader_expandview.getVisibility() == View.VISIBLE)
                vs_testheader_expandview.setVisibility(View.GONE);
            else
                vs_testheader_expandview.setVisibility(View.VISIBLE);
        });

        referred_header_relative.setOnClickListener(v -> {
            if (vs_speciality_header_expandview.getVisibility() == View.VISIBLE)
                vs_speciality_header_expandview.setVisibility(View.GONE);
            else
                vs_speciality_header_expandview.setVisibility(View.VISIBLE);
        });

        followup_header_relative.setOnClickListener(v -> {
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

            case UuidDictionary.MEDICAL_ADVICE: {
                if (!adviceReturned.isEmpty()) {
                    if (adviceReturned.contains("Start Audio"))
                        adviceReturned = Node.bullet + " " +value;
                    else
                        adviceReturned = adviceReturned + "\n\n" + Node.bullet + " " + value;

                    Log.d("GAME", "GAME: " + adviceReturned);
                } else {
                    adviceReturned = value;
                    Log.d("GAME", "GAME_2: " + adviceReturned);
                }

                //medicalAdviceTextView.setText(adviceReturned);

                int j = adviceReturned.indexOf('<');
                int i = adviceReturned.lastIndexOf('>');
                if (i >= 0 && j >= 0) {
                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
                } else {
                    medicalAdvice_HyperLink = "";
                }

                Log.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                if (!medicalAdvice_string.equalsIgnoreCase(""))
                    advice_txt.setText(medicalAdvice_string);
                Log.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

                /*
                 * variable a contains the hyperlink sent from webside.
                 * variable b contains the string data (medical advice) of patient.
                 * */
               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/

              /*  adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
                advice_txt.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                advice_txt.setMovementMethod(LinkMovementMethod.getInstance());
                Log.d("hyper_textview", "hyper_textview: " + advice_txt.getText().toString());*/
                //checkForDoctor();
                break;
            }

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
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = "";
                    advice_txt.setText("");
                  //  medicalAdviceCard.setVisibility(View.GONE);
                }
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
    
    // presc share - start
    private void sharePresc() {
        if (hasPrescription.equalsIgnoreCase("true")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            EditText editText = new EditText(this);
            editText.setInputType(InputType.TYPE_CLASS_PHONE);

            InputFilter inputFilter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    return null;
                }
            };
            String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
            String whatsapp_url = partial_whatsapp_presc_url.concat(visitID);

            editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(10)});
            editText.setText(patient.getPhone_number());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editText.setLayoutParams(layoutParams);
            alertDialog.setView(editText);

            alertDialog.setMessage(getResources().getString(R.string.enter_mobile_number_to_share_prescription));
            alertDialog.setPositiveButton(getResources().getString(R.string.share),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (!editText.getText().toString().equalsIgnoreCase("")) {
                                String phoneNumber = "+91" + editText.getText().toString();
                                String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
                                        + whatsapp_url + getString(R.string.and_enter_your_patient_id) + openmrsID_txt.getText().toString();

                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(
                                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                                        phoneNumber, whatsappMessage))));
                            } else {
                                Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.please_enter_mobile_number),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(PrescriptionActivity.this.getResources().getColor(R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(PrescriptionActivity.this, dialog);
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getResources().getString(R.string.download_prescription_first_before_sharing));
            alertDialog.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(PrescriptionActivity.this.getResources().getColor(R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(PrescriptionActivity.this, dialog);
        }
    }
    // presc share - end

    // Print - start
    private void doWebViewPrint_Button() throws ParseException {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Patient WebView", "page finished loading " + url);
                int webview_heightContent = view.getContentHeight();
                Log.d("variable i", "variable i: " + webview_heightContent);
                createWebPrintJob_Button(view, webview_heightContent);
                mWebView = null;
            }
        });

        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + patient.getLast_name();
        String mPatientOpenMRSID = patient.getOpenmrs_id();
        String mPatientDob = patient.getDate_of_birth();
        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") +
                ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
        String mCityState = patient.getCity_village();
        String mPhone = (!TextUtils.isEmpty(patient.getPhone_number())) ? patient.getPhone_number() : "";
        String mState = patient.getState_province();
        String mCountry = patient.getCountry();

        String mSdw = (!TextUtils.isEmpty(patient.getSdw())) ? patient.getSdw() : "";
        String mOccupation = patient.getOccupation();
        String mGender = patient.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println(getString(R.string.current_time) + c.getTime());

        String[] columnsToReturn = {"startdate"};
        String visitIDorderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {visitID};
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);

      /*  String mPatHist = patHistory.getValue();
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
        mPulse = pulse.getValue();*/
        
       /* try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                                String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }//Load the config file

            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    mTemp = "Temperature(C): " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");

                } else if (obj.getBoolean("mFahrenheit")) {

//                    mTemp = "Temperature(F): " + temperature.getValue();
                    mTemp = "Temperature(F): " + (!TextUtils.isEmpty(temperature.getValue()) ? convertCtoF(temperature.getValue()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        mresp = resp.getValue();
        mSPO2 = "SpO2(%): " + (!TextUtils.isEmpty(spO2.getValue()) ? spO2.getValue() : "");
        String mComplaint = complaint.getValue();

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
        mComplaint = "";
        String colon = ":";
        String mComplaint_new = "";
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

        if (mComplaint.contains("Associated symptoms")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3);
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
            }
        } else {

        }*/


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

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n")
                .replace(Node.bullet, ""));

        //String advice_web = stringToWeb(adviceReturned);
        String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
        String advice_doctor__ = medication_txt.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();

            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        } else {
            advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        }

        String diagnosis_web = stringToWeb(diagnosisReturned);

//        String comments_web = stringToWeb(additionalReturned);
        
        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parse_DateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
            } else {
                followUpDateStr = followUpDate;
            }
        } else {
            followUpDateStr = followUpDate;
        }

        String followUp_web = stringToWeb(followUpDateStr);

        String doctor_web = stringToWeb(doctorName);

        String heading = prescription1;
        String heading2 = prescription2;
        String heading3 = "<br/>";

      /*  String bp = mBP;
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        String address = mAddress + " " + mCityState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");

        String fam_hist = mFamHist;
        String pat_hist = mPatHist;

        if (fam_hist.trim().isEmpty()) {
            fam_hist = getString(R.string.no_history_family_found);
        } else {
            fam_hist = fam_hist.replaceAll(Node.bullet, Node.big_bullet);
        }

        if (pat_hist.trim().isEmpty()) {
            pat_hist = getString(R.string.no_history_patient_illness_found);
        }
*/
        // Generate an HTML document on the fly:
        String fontFamilyFile = "";
        if (details != null && details.getFontOfSign() != null) {
            if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/almondita.ttf');";
            }
        }
        
        String font_face = "<style>" +
                "                @font-face {" +
                "                    font-family: \"MyFont\";" +
                fontFamilyFile +
                "                }" +
                "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (details != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = details.getTextOfSign();
            
            doctrRegistartionNum = !TextUtils.isEmpty(details.getRegistrationNumber()) ?
                    getString(R.string.dr_registration_no) + details.getRegistrationNumber() : "";
            
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + details.getName() + "</span><br>" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " +
                    (details.getQualification() == null || details.getQualification().equalsIgnoreCase("null")
                            ? "" : details.getQualification() + ", ") + details.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(details.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + details.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(details.getEmailId()) ?
                    getString(R.string.dr_email) + details.getEmailId() : "") + "</span><br>" +
                    "</div>";
        }

//        if (isRespiratory) {
//            String htmlDocument =
//                    String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
//                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
//                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
//                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
//                                    /* doctorDetailStr +*/
//                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
//                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" +
//                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
//                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
//                                    "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
//                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
//                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
//                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
//                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
//                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
//                                    "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
//                                    para_open + "%s" + para_close + "<br><br>" +
//                                    "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" +
//                                    "%s<br>" +
//                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
//                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
//                                    doctorDetailStr +
//                                    "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" +
//                                    "</div>"
//                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
//                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
//                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
//            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
//        } else {
//            String htmlDocument =
//                    String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
//                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
//                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
//                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
//                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
//                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" +
//                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
//                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
//                                    "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
//                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
//                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
//                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
//                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
//                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
//                                    "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
//                                    para_open + "%s" + para_close + "<br><br>" +
//                                    "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" +
//                                    "%s<br>" +
//                                    "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" +
//                                    "%s<br>" +
//                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
//                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" +
//                                    doctorDetailStr +
//                                    "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" +
//                                    "</div>"
//                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
//                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
//                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
//            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
//        }


        /**
         * +
         * "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
         * "%s"
         */

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    // print job
    //print button start
    private void createWebPrintJob_Button(WebView webView, int contentHeight) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
        Log.d("webview content height", "webview content height: " + contentHeight);

        if (contentHeight > 2683 && contentHeight <= 3000) {
            //medium size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter,
                    pBuilder.build());


        } else if (contentHeight == 0) {
            //in case of webview bug of 0 contents...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter,
                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..

        } else if (contentHeight > 3000) {
            //large size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter,
                    pBuilder.build());
        } else {
            String jobName = getString(R.string.app_name) + " Visit Summary";

            Log.d("PrintPDF", "PrintPDF");
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter,
                    pBuilder.build());
            //end...
        }
    }

    // string to web
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

    private void jsonBasedPrescTitle() {
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                                String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }
            prescription1 = obj.getString("presciptionHeader1");

            prescription2 = obj.getString("presciptionHeader2");

            //For AFI we are not using Respiratory Value
            if (obj.getBoolean("mResp")) {
                isRespiratory = true;
            } else {
                isRespiratory = false;
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    // Print - end
}