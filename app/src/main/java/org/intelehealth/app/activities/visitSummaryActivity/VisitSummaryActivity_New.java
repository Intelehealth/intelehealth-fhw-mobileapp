package org.intelehealth.app.activities.visitSummaryActivity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class VisitSummaryActivity_New extends AppCompatActivity {
    private static final String TAG = VisitSummaryActivity_New.class.getSimpleName();
    SQLiteDatabase db;

    Button btn_vs_sendvisit;
    private Context context;
    private ImageButton btn_up_header, btn_up_vitals_header, btn_up_visitreason_header,
            btn_up_phyexam_header, btn_up_medhist_header, openall_btn;
    private RelativeLayout vs_header_expandview, vs_vitals_header_expandview,
            vs_visitreason_header_expandview, vs_phyexam_header_expandview, vs_medhist_header_expandview;
    SessionManager sessionManager, sessionManager1;
    String appLanguage, patientUuid, visitUuid, state, patientName, patientGender, intentTag, visitUUID,
            medicalAdvice_string = "", medicalAdvice_HyperLink = "", isSynedFlag = "";
    private float float_ageYear_Month;
    String encounterVitals, encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;
    SharedPreferences mSharedPreference;
    Boolean isPastVisit = false, isVisitSpecialityExists = false;
    Boolean isReceiverRegistered = false;
    ArrayList<String> physicalExams;
    VisitSummaryActivity.DownloadPrescriptionService downloadPrescriptionService;
    private RecyclerView mAdditionalDocsRecyclerView, mPhysicalExamsRecyclerView;
    private RecyclerView.LayoutManager mAdditionalDocsLayoutManager, mPhysicalExamsLayoutManager;
    boolean hasLicense = false;
    private String hasPrescription = "";
    private boolean isRespiratory = false, uploaded = false, downloaded = false;
    ImageView ivPrescription;   // todo: not needed here

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

    CardView diagnosisCard;
    CardView prescriptionCard;
    CardView medicalAdviceCard;
    CardView requestedTestsCard;
    CardView additionalCommentsCard;
    CardView followUpDateCard;
    CardView card_print, card_share;


    TextView diagnosisTextView;
    TextView prescriptionTextView;
    TextView medicalAdviceTextView;
    TextView requestedTestsTextView;
    TextView additionalCommentsTextView;
    TextView followUpDateTextView;
    //added checkbox flag .m
    CheckBox flag;

    // new
    TextView nameView;
    TextView genderView;
    TextView idView;
    TextView visitView;
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
    TextView respiratoryText;
    TextView tempfaren;
    TextView tempcel;
    String medHistory;
    String baseDir;
    String filePathPhyExam;
    File obsImgdir;
    String gender_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary_new);
        context = VisitSummaryActivity_New.this;

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        initUI();
        fetchingIntent();
        setViewsData();
        expandableCardVisibilityHandling();

        btn_vs_sendvisit.setOnClickListener(v -> {
            visitSendDialog(context, getResources().getDrawable(R.drawable.dialog_close_visit_icon), "Send visit?",
                    "Are you sure you want to send the visit to the doctor?",
                    "Yes", "No");
        });

    }

    private void fetchingIntent() {
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager1 = new SessionManager(this);
        appLanguage = sessionManager1.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        // todo: uncomment this block later for testing it is commented.
     /*   final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientGender = intent.getStringExtra("gender");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterUuidAdultIntial = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            mSharedPreference = this.getSharedPreferences(
                    "visit_summary", Context.MODE_PRIVATE);
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            isPastVisit = intent.getBooleanExtra("pastVisit", false);
//            hasPrescription = intent.getStringExtra("hasPrescription");

            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            if (physicalExams == null) physicalExams = new ArrayList<>();
            physicalExams.clear();
            if (selectedExams != null && !selectedExams.isEmpty()) {
                physicalExams.addAll(selectedExams);
            }
        }*/
        // todo: testing - start
        patientUuid = "6b4b3de2-8f1f-4ad6-9d54-6b8dd0ed724a";
        visitUuid = "559ef1d2-feef-4eae-a30c-950561e0a56c";
        patientGender = "M";
        encounterVitals = "ae1cfff3-b18d-4e52-954e-562c8a7dc11e";
        encounterUuidAdultIntial = "3ab5860d-c9a5-4f53-afdc-6713fc116cc5";
        EncounterAdultInitial_LatestVisit = "";
        mSharedPreference = this.getSharedPreferences(
                "visit_summary", Context.MODE_PRIVATE);
        patientName = "Praj Waing";
        float_ageYear_Month = 0.0f;
        intentTag = "new";
        isPastVisit = false;
//            hasPrescription = intent.getStringExtra("hasPrescription");

      //  Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
        Set<String> selectedExams = new HashSet<>();
        selectedExams.add("");
        selectedExams.add("Abdomen:Distension");
        selectedExams.add("Abdomen:Scars");
        selectedExams.add("Abdomen:Peristaltic sound");
        selectedExams.add("Physical Growth:Sexual Maturation");
        selectedExams.add("Abdomen:Tenderness");
        selectedExams.add("Abdomen:Lumps");
        selectedExams.add("Abdomen:Rebound tenderness");

        if (physicalExams == null) physicalExams = new ArrayList<>();
        physicalExams.clear();
        if (selectedExams != null && !selectedExams.isEmpty()) {
            physicalExams.addAll(selectedExams);
        }
        // todo: testing - end


        // receiver
        registerBroadcastReceiverDynamically();
        registerDownloadPrescription();
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;
    }

    private void expandableCardVisibilityHandling() {
        openall_btn.setOnClickListener(v -> {

            Drawable drawable = openall_btn.getDrawable();
            if (drawable.getConstantState().equals(getResources().getDrawable(R.drawable.open_all_btn).getConstantState())) {
                openall_btn.setImageDrawable(getResources().getDrawable(R.drawable.close_all_btn));
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
            }
            else {
                openall_btn.setImageDrawable(getResources().getDrawable(R.drawable.open_all_btn));
                vs_vitals_header_expandview.setVisibility(View.GONE);
                vs_visitreason_header_expandview.setVisibility(View.GONE);
                vs_phyexam_header_expandview.setVisibility(View.GONE);
                vs_medhist_header_expandview.setVisibility(View.GONE);
            }

        });

        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_vitals_header.setOnClickListener(v -> {
            if (vs_vitals_header_expandview.getVisibility() == View.VISIBLE)
                vs_vitals_header_expandview.setVisibility(View.GONE);
            else
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_visitreason_header.setOnClickListener(v -> {
            if (vs_visitreason_header_expandview.getVisibility() == View.VISIBLE)
                vs_visitreason_header_expandview.setVisibility(View.GONE);
            else
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_phyexam_header.setOnClickListener(v -> {
            if (vs_phyexam_header_expandview.getVisibility() == View.VISIBLE)
                vs_phyexam_header_expandview.setVisibility(View.GONE);
            else
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_medhist_header.setOnClickListener(v -> {
            if (vs_medhist_header_expandview.getVisibility() == View.VISIBLE)
                vs_medhist_header_expandview.setVisibility(View.GONE);
            else
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
        });
    }

    private void setViewsData() {
        queryData(String.valueOf(patientUuid));

        // header title set
        nameView.setText(patientName);

        gender_tv = patientGender;
        setgender(genderView);

        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            idView.setText(patient.getOpenmrs_id());
        } else {
            idView.setText(getString(R.string.patient_not_registered));
        }

        mCHWname = findViewById(R.id.chw_details);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider
        // header title set - end

        // vitals values set.

        // vitals values set - end
    }

    private void initUI() {
        // textview - start
        nameView = findViewById(R.id.textView_name_value);
        genderView = findViewById(R.id.textView_gender_value);
        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        visitView = findViewById(R.id.textView_visit_value);
        // textview - end

        // up-down btn - start
        btn_up_header = findViewById(R.id.btn_up_header);
        openall_btn = findViewById(R.id.openall_btn);
        btn_up_vitals_header = findViewById(R.id.btn_up_vitals_header);
        btn_up_visitreason_header = findViewById(R.id.btn_up_visitreason_header);
        btn_up_phyexam_header = findViewById(R.id.btn_up_phyexam_header);
        btn_up_medhist_header = findViewById(R.id.btn_up_medhist_header);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_vitals_header_expandview = findViewById(R.id.vs_vitals_header_expandview);
        vs_visitreason_header_expandview = findViewById(R.id.vs_visitreason_header_expandview);
        vs_phyexam_header_expandview = findViewById(R.id.vs_phyexam_header_expandview);
        vs_medhist_header_expandview = findViewById(R.id.vs_medhist_header_expandview);
        // up-down btn - end

        btn_vs_sendvisit = findViewById(R.id.btn_vs_sendvisit);
    }

    private void setgender(TextView genderView) {
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            if (gender_tv.equalsIgnoreCase("M")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else if (gender_tv.equalsIgnoreCase("F")) {
                genderView.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else {
                genderView.setText(gender_tv);
            }
        } else {
            genderView.setText(gender_tv);
        }
    }

    private void visitSendDialog(Context context, Drawable drawable, String title, String subTitle,
                    String positiveBtnTxt, String negativeBtnTxt) {

            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
            final LayoutInflater inflater = LayoutInflater.from(context);
            View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
            alertdialogBuilder.setView(convertView);
            ImageView icon = convertView.findViewById(R.id.dialog_icon);
            TextView dialog_title = convertView.findViewById(R.id.dialog_title);
            TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
            Button positive_btn = convertView.findViewById(R.id.positive_btn);
            Button negative_btn = convertView.findViewById(R.id.negative_btn);

            icon.setImageDrawable(drawable);
            dialog_title.setText(title);
            dialog_subtitle.setText(subTitle);
            positive_btn.setText(positiveBtnTxt);
            negative_btn.setText(negativeBtnTxt);

            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            negative_btn.setOnClickListener(v -> {
                alertDialog.dismiss();
            });

            positive_btn.setOnClickListener(v -> {
                alertDialog.dismiss();
                visitSentSuccessDialog(context, getResources().getDrawable(R.drawable.dialog_visit_sent_success_icon),
                        "Visit successfully sent!",
                        "Patient's visit has been successfully sent to the doctor.",
                        "Okay");
            });

            alertDialog.show();
    }

    private void visitSentSuccessDialog(Context context, Drawable drawable, String title, String subTitle,
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
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private BroadcastReceiver broadcastReceiverForIamgeDownlaod = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
            physicalDoumentsUpdates();
        }
    };

    private void physicalDoumentsUpdates() {

        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mPhysicalExamsLayoutManager = new LinearLayoutManager(VisitSummaryActivity_New.this, LinearLayoutManager.HORIZONTAL, false);
            mPhysicalExamsRecyclerView.setLayoutManager(mPhysicalExamsLayoutManager);
            mPhysicalExamsRecyclerView.setAdapter(horizontalAdapter);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
    }

    // setting locale
    public void setLocale(String appLanguage) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
            getApplicationContext().createConfigurationContext(conf);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
    }

    // receiver download
    public void registerBroadcastReceiverDynamically() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        registerReceiver(broadcastReceiverForIamgeDownlaod, filter);
    }

    public void registerDownloadPrescription() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("downloadprescription");
        registerReceiver(downloadPrescriptionService, filter);
    }

    // download pres service class
    public class DownloadPrescriptionService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD(TAG, "Download prescription happen" + new SimpleDateFormat("yyyy MM dd_HH mm ss")
                    .format(Calendar.getInstance().getTime()));
            downloadPrescriptionDefault();
            downloadDoctorDetails();
        }
    }

    // download presc default
    public void downloadPrescriptionDefault() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitUuid, "0"}; // so that the deleted values dont come in the presc.
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

        //checks if prescription is downloaded and if so then sets the icon color.
        if (hasPrescription.equalsIgnoreCase("true")) {
            ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
        }
    }

    // downlaod doctor details
    private void downloadDoctorDetails() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitUuid};
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

    /**
     * This method distinguishes between different concepts using switch case to populate the information into the relevant sections (eg:complaints, physical exam, vitals, etc.).
     *
     * @param concept_id variable of type int.
     * @param value      variable of type String.
     */
    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.CURRENT_COMPLAINT: { //Current Complaint
                complaint.setValue(value.replace("?<b>", Node.bullet_arrow));
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
                    diagnosisReturned = diagnosisReturned + ",\n" + value;
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
                    adviceReturned = adviceReturned + "\n" + value;
                    Log.d("GAME", "GAME: " + adviceReturned);
                } else {
                    adviceReturned = value;
                    Log.d("GAME", "GAME_2: " + adviceReturned);
                }
                if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }
                //medicalAdviceTextView.setText(adviceReturned);
                Log.d("Hyperlink", "hyper_global: " + medicalAdvice_string);

                int j = adviceReturned.indexOf('<');
                int i = adviceReturned.lastIndexOf('>');
                if (i >= 0 && j >= 0) {
                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
                } else {
                    medicalAdvice_HyperLink = "";
                }

                Log.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                Log.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

                /*
                 * variable a contains the hyperlink sent from webside.
                 * variable b contains the string data (medical advice) of patient.
                 * */
               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/

                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
                medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                Log.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
                if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }
                requestedTestsTextView.setText(testsReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.ADDITIONAL_COMMENTS: {

                additionalCommentsCard.setVisibility(View.GONE);

//                if (!additionalReturned.isEmpty()) {
//                    additionalReturned = additionalReturned + "," + value;
//                } else {
//                    additionalReturned = value;
//                }
////                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
////                    additionalCommentsCard.setVisibility(View.VISIBLE);
////                }
//                additionalCommentsTextView.setText(additionalReturned);
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

    // parse doctor details
    ClsDoctorDetails objClsDoctorDetails;
    private void parseDoctorDetails(String dbValue) {
        Gson gson = new Gson();
        objClsDoctorDetails = gson.fromJson(dbValue, ClsDoctorDetails.class);
        Log.e(TAG, "TEST VISIT: " + objClsDoctorDetails);

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
          //  frameLayout_doctor.setVisibility(View.VISIBLE);   // todo: handle later.

            doctorSign = objClsDoctorDetails.getTextOfSign();
            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ?
                    getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";

            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
                    "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" +
                    (!TextUtils.isEmpty(objClsDoctorDetails.getName()) ? objClsDoctorDetails.getName() : "") + "</span><br>" +
                    (!TextUtils.isEmpty(objClsDoctorDetails.getSpecialization()) ?
                            objClsDoctorDetails.getSpecialization() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" +
                    (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") +
                    "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " +
                    objClsDoctorDetails.getRegistrationNumber() : "") +
                    "</div>";

        //    mDoctorName.setText(Html.fromHtml(doctorDetailStr).toString().trim()); // todo: handle later
        }
    }

    // query data
    /**
     * This methods retrieves patient data from database.
     *
     * @param dataString variable of type String
     * @return void
     */
    public void queryData(String dataString) {
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
                    FirebaseCrashlytics.getInstance().recordException(e);
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
        String[] columns = {"value", " conceptuuid"};

        try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ?";
            String[] famHistArgs = {encounterUuidAdultIntial, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
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

            String[] medHistArgs = {encounterUuidAdultIntial, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};

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
//vitals display code
        String visitSelection = "encounteruuid = ? AND voided!='1'";
        String[] visitArgs = {encounterVitals};
        if (encounterVitals != null) {
            try {
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
                if (visitCursor != null && visitCursor.moveToFirst()) {
                    do {
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        parseData(dbConceptID, dbValue);
                    } while (visitCursor.moveToNext());
                }
                if (visitCursor != null) {
                    visitCursor.close();
                }
            } catch (SQLException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
//adult intails display code
        String encounterselection = "encounteruuid = ? AND conceptuuid != ? AND conceptuuid != ? AND voided!='1'";
        String[] encounterargs = {encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE};
        Cursor encountercursor = db.query("tbl_obs", columns, encounterselection, encounterargs, null, null, null);
        try {
            if (encountercursor != null && encountercursor.moveToFirst()) {
                do {
                    String dbConceptID = encountercursor.getString(encountercursor.getColumnIndex("conceptuuid"));
                    String dbValue = encountercursor.getString(encountercursor.getColumnIndex("value"));
                    parseData(dbConceptID, dbValue);
                } while (encountercursor.moveToNext());
            }
            if (encountercursor != null) {
                encountercursor.close();
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
        }

        downloadPrescriptionDefault();
        downloadDoctorDetails();
    }



}