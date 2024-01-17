package org.intelehealth.msfarogyabharat.activities.visitSummaryActivity;

import static org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils.SimpleDatetoLongDate;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.apache.commons.lang3.StringUtils;
import org.intelehealth.msfarogyabharat.activities.resolutionActivity.ResolutionActivity;
import org.intelehealth.msfarogyabharat.database.InteleHealthDatabaseHelper;
import org.intelehealth.msfarogyabharat.models.Add_Doc_Adapter_DataModel;
import org.intelehealth.msfarogyabharat.models.dto.EncounterDTO;
import org.intelehealth.msfarogyabharat.models.dto.VisitDTO;
import org.intelehealth.msfarogyabharat.models.pushRequestApiCall.Visit;
import org.intelehealth.msfarogyabharat.utilities.multipleSelectionSpinner.Item;
import org.intelehealth.msfarogyabharat.utilities.multipleSelectionSpinner.MultiSelectionSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.additionalDocumentsActivity.AdditionalDocumentsActivity;
import org.intelehealth.msfarogyabharat.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.msfarogyabharat.activities.familyHistoryActivity.FamilyHistoryActivity;
import org.intelehealth.msfarogyabharat.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.msfarogyabharat.activities.patientSurveyActivity.PatientSurveyActivity;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.dao.EncounterDAO;
import org.intelehealth.msfarogyabharat.database.dao.ImagesDAO;
import org.intelehealth.msfarogyabharat.database.dao.ObsDAO;
import org.intelehealth.msfarogyabharat.database.dao.PatientsDAO;
import org.intelehealth.msfarogyabharat.database.dao.ProviderAttributeLIstDAO;
import org.intelehealth.msfarogyabharat.database.dao.SyncDAO;
import org.intelehealth.msfarogyabharat.database.dao.VisitAttributeListDAO;
import org.intelehealth.msfarogyabharat.database.dao.VisitsDAO;
import org.intelehealth.msfarogyabharat.knowledgeEngine.Node;
import org.intelehealth.msfarogyabharat.models.ClsDoctorDetails;
import org.intelehealth.msfarogyabharat.models.Patient;
import org.intelehealth.msfarogyabharat.models.dto.ObsDTO;
import org.intelehealth.msfarogyabharat.services.DownloadService;
import org.intelehealth.msfarogyabharat.syncModule.SyncUtils;
import org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils;
import org.intelehealth.msfarogyabharat.utilities.FileUtils;
import org.intelehealth.msfarogyabharat.utilities.Logger;

import android.print.PdfPrint;

import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UuidDictionary;

import org.intelehealth.msfarogyabharat.activities.homeActivity.HomeActivity;
import org.intelehealth.msfarogyabharat.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.msfarogyabharat.activities.vitalActivity.VitalsActivity;
import org.intelehealth.msfarogyabharat.utilities.NetworkConnection;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;

public class VisitSummaryActivity extends AppCompatActivity {

    private static final String TAG = VisitSummaryActivity.class.getSimpleName();
    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    String speciality_selected = "Doctor not needed";

    boolean uploaded = false;
    boolean downloaded = false;

    Context context, context1;

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    String visitUUID;
    String medicalAdvice_string = "";
    String medicalAdvice_HyperLink = "";
    String isSynedFlag = "";
    private float float_ageYear_Month;

    Spinner speciality_spinner;

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
    int shareSelectedOption = 2;

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;
    ImageButton editAddDocs;
    ImageButton imagebutton_edit_editFacility;

    FrameLayout frameLayout_doctor;
    TextView nameView;
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

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    RelativeLayout uploadButton;
    RelativeLayout downloadButton;
    ArrayList<String> physicalExams;

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

    Boolean isPastVisit = false, isVisitSpecialityExists = false;
    Boolean isReceiverRegistered = false;
    List<String> encounterAdultInitList = new ArrayList<>();


    //for new visit case allow......

    private String encounterAdultIntialsForNew = "";
    String privacy_value_selected = "";
    EncounterDTO encounterDTO = new EncounterDTO();
    private boolean returning;

    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";

    NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private Menu mymenu;
    MenuItem internetCheck = null;
    MenuItem endVisit_click = null;

    private RecyclerView mAdditionalDocsRecyclerView, recyclerview_visitsummary;
    private RecyclerView.LayoutManager mAdditionalDocsLayoutManager;

    private RecyclerView mPhysicalExamsRecyclerView;
    private RecyclerView.LayoutManager mPhysicalExamsLayoutManager;

    public static String prescriptionHeader1;
    public static String prescriptionHeader2;
    SharedPreferences mSharedPreference;
    boolean hasLicense = false;
    String mFileName = "config.json";
    public static String prescription1;
    public static String prescription2;
    SessionManager sessionManager, sessionManager1, sessionManager2;
    String encounterUuid;
    String encounterVitals;

    //  Boolean isreturningWhatsapp = true;
    String encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;
    String encounterAdultInit = "";

    ProgressBar mProgressBar;
    TextView mProgressText;

    ImageButton additionalDocumentsDownlaod;
    ImageButton onExaminationDownload;

    DownloadPrescriptionService downloadPrescriptionService;
    private TextView additionalImageDownloadText;
    private TextView physcialExaminationDownloadText;

    ImageView ivPrescription;
    private String hasPrescription = "";
    private boolean isRespiratory = false;
    String appLanguage;
    View button_resolution;
    private List<String> visitUuidList, visitStartDatesList;
    private List<String> complaintList_adapter, physexamList_adapter;
    private VisitSummaryAdapter visitsum_adapter;
    private LinearLayoutManager visitsum_layoutmanager;
    private String latestVisitUuid;
    private boolean allVisitsEnded = false;
    private boolean hide_endvisit = false;
    String shareoptionsarray[] = {"Whatsapp", "Email"};
    public static final String prescriptionUrl = "https://training.vikalpindia.org/intelehealth/index.html#/prescription/";
    URLEncoder urlEncoder;

    List<String> districtList;
    ArrayList<Item> mFacilityList;
    TextView txtViewFacility;
    AutoCompleteTextView autocompleteState, autocompleteDistrict;
    EditText editText_landmark;
    MultiSelectionSpinner mFacilitySelection;

    String mState = "", mDistrict = "", mFacilityValue = "";
    JSONArray mFacilityArray = new JSONArray();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_visit_summary, menu);
        MenuItem menuItem = menu.findItem(R.id.summary_endVisit);
        MenuItem menuItemNewVisit = menu.findItem(R.id.summary_addNewCase);

        internetCheck = menu.findItem(R.id.internet_icon);
        MenuItemCompat.getActionView(internetCheck);

        isNetworkAvailable(this);
        sessionManager = new SessionManager(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mCHWname = findViewById(R.id.chw_details);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider
        //Added Prescription Title from config.Json dynamically through sharedPreferences
        prescriptionHeader1 = sharedPreferences.getString("prescriptionTitle1", "");
        prescriptionHeader2 = sharedPreferences.getString("prescriptionTitle2", "");

        //  if (isPastVisit) menuItem.setVisible(false);
        if (hide_endvisit) {
            menuItem.setVisible(false);
            menuItemNewVisit.setVisible(true);
        } else {
            menuItemNewVisit.setVisible(false);

        }
        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver broadcastReceiverForIamgeDownlaod = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            onResume();
            physicalDoumentsUpdates();

        }
    };

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


    @Override
    public void onBackPressed() {
        //do nothing
        //Use the buttons on the screen to navigate
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiverForIamgeDownlaod);
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
        }
        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.summary_addNewCase: {
                mAlertCreateVisitConformation();
                return true;
            }
            case R.id.summary_home: {
//                NavUtils.navigateUpFromSameTask(this);
                Intent i = new Intent(this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
            case R.id.summary_print: {
                try {
                    doWebViewPrint_Button();
                } catch (ParseException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                return true;
            }
            case R.id.summary_sms: {
                //     VisitSummaryActivityPermissionsDispatcher.sendSMSWithCheck(this);
                return true;
            }
            case R.id.summary_endVisit: {
                //meera
                //  downloaded = true;
                if (downloaded) {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);

//                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogStyle);
                    alertDialogBuilder.setMessage(getResources().getString(R.string.end_visit_msg));
                    alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialogBuilder.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            endVisit();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.show();
                    //alertDialog.show();
                    IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

                } else {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogStyle);
                    alertDialogBuilder.setMessage(R.string.error_no_data);
                    alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.show();
                    //alertDialog.show();
                    IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
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
        sessionManager1 = new SessionManager(VisitSummaryActivity.this);

        String language = sessionManager.getAppLanguage();
        // In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            VisitSummaryActivity.this.getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        appLanguage = sessionManager1.getAppLanguage();
//        if (!appLanguage.equalsIgnoreCase("")) {
//            setLocale(appLanguage);
//        }
//        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            latestVisitUuid = intent.getStringExtra("latest_VisitUuid");
            //for new case .......
            privacy_value_selected = intent.getStringExtra("privacy_value_selected");
            Log.v("visituuid", "vuid: " + visitUuid);
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterUuidAdultIntial = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            Log.v("main", "v: " + encounterVitals + " a: " + encounterUuidAdultIntial + " aaa: " + EncounterAdultInitial_LatestVisit);

            mSharedPreference = this.getSharedPreferences(
                    "visit_summary", Context.MODE_PRIVATE);
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            isPastVisit = intent.getBooleanExtra("pastVisit", false);
            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            if (physicalExams == null) physicalExams = new ArrayList<>();
            physicalExams.clear();
            if (selectedExams != null && !selectedExams.isEmpty()) {
                physicalExams.addAll(selectedExams);
            }
        }
        registerBroadcastReceiverDynamically();
        registerDownloadPrescription();
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

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

        setTitle(getString(R.string.title_activity_patient_summary));
        setTitle(patientName + ": " + getTitle());

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mLayout = findViewById(R.id.summary_layout);
        context = getApplicationContext();
        context1 = VisitSummaryActivity.this;
//we can remove by data binding
        button_resolution = findViewById(R.id.button_resolution);

        mAdditionalDocsRecyclerView = findViewById(R.id.recy_additional_documents);
        mPhysicalExamsRecyclerView = findViewById(R.id.recy_physexam);
        recyclerview_visitsummary = findViewById(R.id.recyclerview_visitsummary);

        editVitals = findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = findViewById(R.id.imagebutton_edit_pathist);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        uploadButton = findViewById(R.id.button_upload);
        downloadButton = findViewById(R.id.button_download);
        //additionalDocumentsDownlaod = findViewById(R.id.imagebutton_download_additional_document);
        onExaminationDownload = findViewById(R.id.imagebutton_download_physexam);
        //additionalDocumentsDownlaod.setVisibility(View.GONE);
        physcialExaminationDownloadText = findViewById(R.id.physcial_examination_download);
        onExaminationDownload.setVisibility(View.GONE);
        //image download for additional documents
        additionalImageDownloadText = findViewById(R.id.additional_documents_download);

        diagnosisCard = findViewById(R.id.cardView_diagnosis);
        prescriptionCard = findViewById(R.id.cardView_rx);
        medicalAdviceCard = findViewById(R.id.cardView_medical_advice);
        requestedTestsCard = findViewById(R.id.cardView_tests);
        additionalCommentsCard = findViewById(R.id.cardView_additional_comments);
        followUpDateCard = findViewById(R.id.cardView_follow_up_date);
        mDoctorTitle = findViewById(R.id.title_doctor);
        mDoctorName = findViewById(R.id.doctor_details);
        frameLayout_doctor = findViewById(R.id.frame_doctor);
        frameLayout_doctor.setVisibility(View.GONE);

        card_print = findViewById(R.id.card_print);
        card_share = findViewById(R.id.card_share);

        complaintList_adapter = new ArrayList<>();
        physexamList_adapter = new ArrayList<>();

        String a = sessionManager1.getFacilityResolution();
        if (sessionManager1.getFacilityResolution().equalsIgnoreCase(latestVisitUuid)) {
            button_resolution.setVisibility(View.GONE);
        } else {
            // button_resolution.setVisibility(View.VISIBLE);
        }

        card_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*try {
                    doWebViewPrint_Button();*/

                // redirect to web browser for prescription
                Intent intent1 = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(prescriptionUrl + patientUuid + "/" + sessionManager.getAppLanguage()));
                Log.v("main", "prescurl: " + prescriptionUrl + patientUuid);
                startActivity(intent1);

/*                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
            }
        });

        card_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button_resolution.getVisibility() == View.GONE) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                    LayoutInflater inflater = VisitSummaryActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.sharelayout, null);

//                    EditText editText = new EditText(VisitSummaryActivity.this);
//                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
//                    InputFilter inputFilter = new InputFilter() {
//                        @Override
//                        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//                            return null;
//                        }
//                    };

//                    String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
//                    String whatsapp_url = partial_whatsapp_presc_url.concat(visitUuid);

//                    editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(10)});
//                    editText.setText(patient.getPhone_number());
//                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
//                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    // editText.setLayoutParams(layoutParams);
                    //  alertDialog.setView(editText);

                    alertDialog.setView(dialogView);
                    EditText editText = dialogView.findViewById(R.id.editTextmobileno);
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    InputFilter inputFilter = new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                            return null;
                        }
                    };

                    editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(10)});
                    editText.setText(patient.getPhone_number());

                    RadioButton whatsappradio = dialogView.findViewById(R.id.whatsappradio);
                    RadioButton emailradio = dialogView.findViewById(R.id.emailradio);

                    whatsappradio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onRadioButtonClicked(view);
                        }
                    });

                    emailradio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onRadioButtonClicked(view);
                        }
                    });

                    String htmlDoc = sms_prescription();
                    alertDialog.setTitle(R.string.share_prescription);
                    alertDialog.setMessage(getResources().getString(R.string.enter_mobile_number_to_share_prescription));

                    alertDialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            shareSelectedOption = 2; //to remvoe the selected value from the cache memory of the variable.
                        }
                    });

                    alertDialog.setPositiveButton(getResources().getString(R.string.share),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                                        Log.v("main", "sharedoption: " + shareSelectedOption);
                                        if (shareSelectedOption != 2) {
                                            String phoneNumber = "+91" + editText.getText().toString();
                                            /*startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.fromParts("sms", phoneNumber, null))
                                                    .putExtra("sms_body", Html.fromHtml(htmlDoc).toString()));*/

                                            //Add code for Whatsapp and Email here
                                            if (shareSelectedOption == 0) {
                                                //Whatsapp...
                                                String phoneNumberWithCountryCode = "+91" + editText.getText().toString();
                                                shareWhatsapp(phoneNumberWithCountryCode);
                                                shareSelectedOption = 2;
                                            } else if (shareSelectedOption == 1) {
                                                //Email...
                                                shareEmail();
                                                shareSelectedOption = 2;
                                            }
                                            //end
                                        } else {
                                            Toast.makeText(context, getResources().getString(R.string.please_select_an_option),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        Toast.makeText(context, getResources().getString(R.string.please_enter_mobile_number),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                    AlertDialog dialog = alertDialog.show();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                    alertDialog.setMessage(getResources().getString(R.string.give_resolution_first));
                    alertDialog.setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = alertDialog.show();
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

                }


            }
        });


//        mDoctorTitle.setVisibility(View.GONE);
//        mDoctorName.setVisibility(View.GONE);
        speciality_spinner = findViewById(R.id.speciality_spinner);
        diagnosisTextView = findViewById(R.id.textView_content_diagnosis);
        prescriptionTextView = findViewById(R.id.textView_content_rx);
        medicalAdviceTextView = findViewById(R.id.textView_content_medical_advice);
        requestedTestsTextView = findViewById(R.id.textView_content_tests);
        additionalCommentsTextView = findViewById(R.id.textView_content_additional_comments);
        followUpDateTextView = findViewById(R.id.textView_content_follow_up_date);
        ivPrescription = findViewById(R.id.iv_prescription);
        txtViewFacility = findViewById(R.id.txtViewFacility);
        imagebutton_edit_editFacility = findViewById(R.id.imagebutton_edit_editFacility);
        autocompleteState = findViewById(R.id.autocomplete_state);
        autocompleteDistrict = findViewById(R.id.autocomplete_district);
        editText_landmark = findViewById(R.id.editText_landmark);
        mFacilitySelection = (MultiSelectionSpinner) findViewById(R.id.mFacilitySelection);
        autocompleteDistrict.setEnabled(false);
        districtList = new ArrayList<>();
        mFacilityList = new ArrayList<>();
        mFacilityList.add(new Item(getString(R.string.select), false));

        mFacilitySelection.setVisibility(View.GONE);
        txtViewFacility.setVisibility(View.VISIBLE);


        //get all visits from patientuuid
        visitUuidList = new ArrayList<>();
        visitStartDatesList = new ArrayList<>();
        String visitStartDate = "";
        String visitIDSelection = "patientuuid = ?";
        String[] visitIDArgs = {patientUuid};
        Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
        if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
            do {
                visitUuid = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                visitStartDate = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
                visitUuidList.add(visitUuid); // All visits will be stored in this arraylist.

                visitStartDate = SimpleDatetoLongDate(visitStartDate);
                Log.v("main", "startdate: " + visitStartDate);

                visitStartDatesList.add(visitStartDate);
            } while (visitIDCursor.moveToNext());
        }
        if (visitIDCursor != null) {
            visitIDCursor.close();
        }

        if (isPastVisit) { //For past visit checking we hide the edit section.
         /*   editVitals.setVisibility(View.GONE);
            editComplaint.setVisibility(View.GONE);
            editPhysical.setVisibility(View.GONE);
            editFamHist.setVisibility(View.GONE);
            editMedHist.setVisibility(View.GONE);
            editAddDocs.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            button_resolution.setVisibility(View.GONE);
            invalidateOptionsMenu();*/

            //check if any active visit then show the edit fields.
            Cursor cursor = db.rawQuery("Select uuid from tbl_visit where enddate is NULL AND patientuuid = ?",
                    new String[]{patientUuid});
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        //do nothing
                    }
                    while (cursor.moveToNext());
                } else {
                    //as movetoFirst() will return False since there is no data this means no visit is Active (all are Ended).
                    editVitals.setVisibility(View.GONE);
                    editComplaint.setVisibility(View.GONE);
                    editPhysical.setVisibility(View.GONE);
                    editFamHist.setVisibility(View.GONE);
                    editMedHist.setVisibility(View.GONE);
                    editAddDocs.setVisibility(View.GONE);
                    uploadButton.setVisibility(View.GONE);
                    button_resolution.setVisibility(View.GONE);
                    allVisitsEnded = true;
                    invalidateOptionsMenu();
                    hide_endvisit = true;
                    imagebutton_edit_editFacility.setVisibility(View.GONE);
                }
            } catch (SQLException e) {

            }
            if (cursor != null) {
                cursor.close();
            }
            //end
        } else {
            String visitIDorderBy = "startdate";
            String visitIDSelection__ = "uuid = ?";
            String[] visitIDArgs__ = {visitUuid};

            final Cursor visitIDCursor__ = db.query("tbl_visit", null, visitIDSelection__, visitIDArgs__,
                    null, null, visitIDorderBy);

            if (visitIDCursor__ != null && visitIDCursor__.moveToFirst() && visitIDCursor__.getCount() > 0) {
                visitIDCursor__.moveToFirst();
                visitUUID = visitIDCursor__.getString(visitIDCursor__.getColumnIndexOrThrow("uuid"));
                Log.v("visituuid", "visituuid: " + visitUUID + "\n");
            }
            if (visitIDCursor__ != null) visitIDCursor__.close();
            if (visitUUID != null && !visitUUID.isEmpty()) {
                //  addDownloadButton();

            }
        }

        //To show the most recent data on top - reverse the arraylist
        // Collections.reverse(visitUuidList);
        //end

        //getEncounters - start
        EncounterDAO encounterDAO = new EncounterDAO();
        for (String v_uuid : visitUuidList) {
            String encounterIDSelection = "visituuid = ?";

            String[] encounterIDArgs = {v_uuid};

            Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
            if (encounterCursor != null && encounterCursor.moveToFirst()) {
                do {
                    if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase
                            (encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                        encounterAdultInit = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        encounterAdultInitList.add(encounterAdultInit);
                    }

                } while (encounterCursor.moveToNext());
            }
            assert encounterCursor != null;
            encounterCursor.close();

            //complaint obs - start
            String previsitSelection = "encounteruuid = ? AND conceptuuid = ? and voided !='1'";
            String[] previsitArgs = {encounterAdultInit, UuidDictionary.CURRENT_COMPLAINT};
            String[] previsitColumms = {"value", " conceptuuid", "encounteruuid"};
            Cursor previsitCursor = db.query("tbl_obs", previsitColumms, previsitSelection, previsitArgs, null, null, null);
            if (previsitCursor.moveToFirst() && previsitCursor != null) {
                do {
                    //here we will get multiple values so add each in Arraylist and then send to RecyclerAdapter
                    String value = previsitCursor.getString(previsitCursor.getColumnIndexOrThrow("value"));
                    ObsDTO obsDTO = new ObsDTO();
                    obsDTO.setValue(value);
                    value = obsDTO.getValue(sessionManager.getAppLanguage());
                    complaintList_adapter.add(Html.fromHtml(value).toString());
                }
                while (previsitCursor.moveToNext());

            }
            previsitCursor.close();
            //complaint obs - end

            //physexam obs - start
            String previsitSelection_phy = "encounteruuid = ? AND conceptuuid = ? and voided !='1'";
            String[] previsitArgs_phy = {encounterAdultInit, UuidDictionary.PHYSICAL_EXAMINATION};
            String[] previsitColumms_phy = {"value", " conceptuuid", "encounteruuid"};
            Cursor previsitCursor_phy = db.query("tbl_obs", previsitColumms_phy, previsitSelection_phy, previsitArgs_phy,
                    null, null, null);
            if (previsitCursor_phy.moveToFirst() && previsitCursor_phy != null) {
                do {
                    //here we will get multiple values so add each in Arraylist and then send to RecyclerAdapter
                    String value = previsitCursor_phy.getString
                            (previsitCursor_phy.getColumnIndexOrThrow("value"));
                    ObsDTO obsDTO = new ObsDTO();
                    obsDTO.setValue(value);
                    value = obsDTO.getValue(sessionManager.getAppLanguage());
                    physexamList_adapter.add(Html.fromHtml(value).toString());
                }
                while (previsitCursor_phy.moveToNext());

            }
            previsitCursor_phy.close();
            //physexam obs - end

        }
        //getEncounters - end

        //get all visits - end
        Add_Doc_Adapter_DataModel model = new Add_Doc_Adapter_DataModel(encounterAdultInit, patientUuid, visitUuid,
                patientName, float_ageYear_Month);
        visitsum_adapter = new VisitSummaryAdapter(context, context1, visitUuidList, visitStartDatesList,
                complaintList_adapter, physexamList_adapter, allVisitsEnded, visitUuid, model, new VisitSummaryAdapter.OnVisitItemClickListner() {
            @Override
            public void mItemClicking(int pos) {
                String[] columns = {"value", " conceptuuid"};
                try {

                    String FacilityHistSelection = "encounteruuid = ? AND conceptuuid = ?";
                    String facilityText = null;
                    if (pos < encounterAdultInitList.size()) {
                        String[] FacilityHistArgs = {encounterAdultInitList.get(pos), UuidDictionary.Facility};
                        Cursor medHistCursor = db.query("tbl_obs", columns, FacilityHistSelection, FacilityHistArgs, null, null, null);
                        medHistCursor.moveToLast();
                        facilityText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                        medHistCursor.close();
                    }
//todo single textutill
                    if (!TextUtils.isEmpty(facilityText)) {
                        if (facilityText == null) {

                        } else if (facilityText.equalsIgnoreCase("-")) {
                            autocompleteState.setText("");
                            autocompleteDistrict.setText("");
                            txtViewFacility.setText("");
                            mFacilityValue = "";
                        } else {
                            String[] arrayString = facilityText.split(",", 3);


                            autocompleteState.setText("" + arrayString[0]);
                            mState = arrayString[0];
                            autocompleteDistrict.setText("" + arrayString[1]);
                            mDistrict = arrayString[1];
//                          txtViewFacility.setText("" + arrayString[arrayString.length - 1]);
                            mFacilitySelection.setVisibility(View.GONE);
                            mFacilityValue = arrayString[arrayString.length - 1];
                            txtViewFacility.setText(arrayString[arrayString.length - 1].replaceAll("\\|", "\n\n"));

                        }
                    } else {
                        autocompleteState.setText("");
                        autocompleteDistrict.setText("");
                        txtViewFacility.setText("");
                        mFacilityValue = "";
                    }
//                    medHistCursor.close();
                } catch (CursorIndexOutOfBoundsException e) {
                    autocompleteState.setText("");
                    autocompleteDistrict.setText("");
                    txtViewFacility.setText("");

                    // if facility  does not exist
                }
            }
        });
        visitsum_layoutmanager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerview_visitsummary.setLayoutManager(visitsum_layoutmanager);
        recyclerview_visitsummary.setAdapter(visitsum_adapter);


        //if row is present i.e. if true is returned by the function then the spinner will be disabled.
        Log.d("visitUUID", "onCreate_uuid: " + visitUuid);
        /*isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
        if (isVisitSpecialityExists)
            speciality_spinner.setEnabled(false);*/

        //spinner is being populated with the speciality values...
        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();
        VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();


        List<String> items = providerAttributeLIstDAO.getAllValues();
        Log.d("specc", "spec: " + visitUuid);
        // String special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid);
        //Hashmap to List<String> add all value
        ArrayAdapter<String> stringArrayAdapter;

        if (items != null) {
//            items.add(0, "Select Specialization");
            items.add(0, getResources().getString(R.string.select_specialization));
            stringArrayAdapter =
                    new ArrayAdapter<String>
                            (this, android.R.layout.simple_spinner_dropdown_item, items);
            speciality_spinner.setAdapter(stringArrayAdapter);
        } else {
            stringArrayAdapter =
                    new ArrayAdapter<String>
                            (this, android.R.layout.simple_spinner_dropdown_item,
                                    getResources().getStringArray(R.array.speciality_values));
            speciality_spinner.setAdapter(stringArrayAdapter);
        }


      /*  if (special_value != null) {
            int spinner_position = stringArrayAdapter.getPosition(special_value);
            speciality_spinner.setSelection(spinner_position);
        } else {

        }*/
        JSONObject json = loadJsonObjectFromAsset("state_district_facility.json");
//        JsonArray Country=new JsonArray();
        String[] countries = getResources().getStringArray(R.array.states_india);
//        try{
//
//            JSONArray stateArray = json.getJSONArray("states");
//            for (int i= 0; i < stateArray.length(); i++) {
//                String StateList = stateArray.getJSONObject(i).getString("state");
////                              mFacilityArray=districtArray.getJSONObject(j).getJSONArray("facility");
//                Country.add(StateList);
//            }}catch (Exception e){}

        // Create the adapter and set it to the AutoCompleteTextView

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        autocompleteState.setAdapter(adapter);

        if (autocompleteState.getText().toString().equals("")) {
            autocompleteDistrict.setText("");
            autocompleteDistrict.setEnabled(false);
        }

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
                if (selectedState.equalsIgnoreCase("") || autocompleteState.getText().equals("") || selectedState.equalsIgnoreCase("Select State")) {
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
                            ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(VisitSummaryActivity.this, android.R.layout.simple_list_item_1, districtList);
                            autocompleteDistrict.setAdapter(districtAdapter);
                            break;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
//                    editText_landmark.setEnabled(false);
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
//                            ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(VisitSummaryActivity.this, android.R.layout.simple_list_item_1, districtList);
//                            autocompleteDistrict.setAdapter(districtAdapter);
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

//                      mFacilitySelection.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        speciality_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("SPINNER", "SPINNER_Selected: " + adapterView.getItemAtPosition(i).toString());

                speciality_selected = adapterView.getItemAtPosition(i).toString();
                Log.d("SPINNER", "SPINNER_Selected_final: " + speciality_selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//        if (hasPrescription.equalsIgnoreCase("true")) {
//            ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
//        }

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        obsImgdir = new File(AppConstants.IMAGE_PATH);

        flag = findViewById(R.id.flaggedcheckbox);


//        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
//            //i.e the visit is a priority visit since getEmergencyEncounters() checks for voided = 0 i.e. priority...
//            flag.setChecked(true);
//            flag.setEnabled(false);
//        }
//        else if() {
//            flag.setChecked(false);
//            flag.setEnabled(false);
//        }
//        else {
//            flag.setChecked(false); //new visit from phy exam or else if old visit but still it will come here
        //as without prio upload -> no row added in db of emergency enc Also, for new visit no row will already be there for enc emergency...
//            flag.setEnabled(false);
//        }

        physicalDoumentsUpdates();

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Paint p = new Paint();
        p.setColor(Color.BLUE);
        additionalImageDownloadText.setPaintFlags(p.getColor());
        additionalImageDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        additionalDocumentImagesDownload();

        //image download for physcialExamination documents
        physcialExaminationDownloadText.setPaintFlags(p.getColor());
        physcialExaminationDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        //  physcialExaminationImagesDownload();


        downloadButton.setEnabled(false);
        downloadButton.setVisibility(View.GONE);

        flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag.isChecked()) {
                    MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                    alertdialogBuilder.setMessage(getResources().getString(R.string.emergency_confirmation));
                    alertdialogBuilder.setPositiveButton(getResources().getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //here set emergency as True for this visit...
                            try {
                                EncounterDAO encounterDAO = new EncounterDAO();
                                encounterDAO.setEmergency(visitUuid, true);
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                            dialogInterface.dismiss(); //close the dialog
                            //    flag.setChecked(true); //check the dialog here...
                        }
                    });
                    alertdialogBuilder.setNegativeButton(getResources().getString(R.string.generic_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                EncounterDAO encounterDAO = new EncounterDAO();
                                encounterDAO.setEmergency(visitUuid, false);
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }

                            flag.setChecked(false); //uncheck the checkbox here...
                            dialog.dismiss(); //dialog is closed...
                        }
                    });
                    AlertDialog alertDialog = alertdialogBuilder.create();
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(false); //dialog will not close when clicked outside...

                    Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                    Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
                } else {
                    try {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        encounterDAO.setEmergency(visitUuid, false);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }
        });
/*
        flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("check", "checked: "+ isChecked);

                //If user ticks the checkbox then show a dialog box...
                if(isChecked) {
                    MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                    alertdialogBuilder.setMessage("Do you want to set this visit as an Emergency?");
                    alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //here set emergency as True for this visit...
                            try {
                                EncounterDAO encounterDAO = new EncounterDAO();
                                encounterDAO.setEmergency(visitUuid, isChecked);
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                            dialogInterface.dismiss(); //close the dialog
                        //    flag.setChecked(true); //check the dialog here...
                        }
                    });
                    alertdialogBuilder.setNegativeButton(R.string.generic_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                EncounterDAO encounterDAO = new EncounterDAO();
                                encounterDAO.setEmergency(visitUuid, false);
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }

                            flag.setChecked(false); //uncheck the checkbox here...
                            dialog.dismiss(); //dialog is closed...
                        }
                    });
                    AlertDialog alertDialog = alertdialogBuilder.create();
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(false); //dialog will not close when clicked outside...

                    Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                    Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
                }
                else {
                    try {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        encounterDAO.setEmergency(visitUuid, isChecked);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }

            }
        });
*/
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFacility();
                isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
                //   if (speciality_spinner.getSelectedItemPosition() != 0) {
                VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                boolean isUpdateVisitDone = false;
                try {

                    if (!isVisitSpecialityExists) {
                        isUpdateVisitDone = speciality_attributes
                                .insertVisitAttributes(visitUuid, speciality_selected);
                    }
                    Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                } catch (DAOException e) {
                    e.printStackTrace();
                    Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                }

                if (isVisitSpecialityExists)
                    speciality_spinner.setEnabled(false);

                VisitAttributeListDAO visit_state_attributes = new VisitAttributeListDAO();
                boolean isUpdateVisitState = false;
                try {
                    if (!isVisitSpecialityExists) {
                        isUpdateVisitState = visit_state_attributes.insertVisitAttributesState(visitUuid, "" + sessionManager.getStateName());
                    }
                    Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                } catch (DAOException e) {
                    e.printStackTrace();
                    Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                }

                if (flag.isChecked()) {
                    try {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        encounterDAO.setEmergency(visitUuid, true);

                        //here disable the checkbox...
                        flag.setEnabled(false);

                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                } else {
                    //here if checkbox is not selected still disable the checkbox...
                    flag.setEnabled(false);

                   /* try {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        encounterDAO.setEmergency(visitUuid, false);

                        //here disable the checkbox...
                        flag.setEnabled(false);

                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }*/
                }


                if (patient.getOpenmrs_id() == null || patient.getOpenmrs_id().isEmpty()) {
                    String patientSelection = "uuid = ?";
                    String[] patientArgs = {String.valueOf(patient.getUuid())};
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
                    Log.v("visituuid", "visituuid: " + visitUUID + "\n");
                    String visitIDSelection = "uuid = ?";
                    String[] visitIDArgs = {visitUuid};
                    final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
                    if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                        visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                        Log.v("visituuid", "visituuid: " + visitUUID + "\n");
                    }
                    if (visitIDCursor != null)
                        visitIDCursor.close();
                }

                if (!flag.isChecked()) {
                    //
                }

                if (NetworkConnection.isOnline(getApplication())) {
                    Toast.makeText(context, getResources().getString(R.string.upload_started), Toast.LENGTH_LONG).show();

                    SyncDAO syncDAO = new SyncDAO();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Added the 4 sec delay and then push data.For some reason doing immediately does not work
                            //Do something after 100ms
                            SyncUtils syncUtils = new SyncUtils();
                            boolean isSynced = syncUtils.syncForeground("visitSummary");
                            if (isSynced) {
                                AppConstants.notificationUtils.DownloadDone(patientName + " " + getResources().getString(R.string.visit_data_upload), getResources().getString(R.string.visit_uploaded_successfully), 3, VisitSummaryActivity.this);
                                //
                                showVisitID();
                                Log.d("visitUUID", "showVisitID: " + visitUUID);
                                isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
                                if (isVisitSpecialityExists)
                                    speciality_spinner.setEnabled(false);

                            } else {
                                AppConstants.notificationUtils.DownloadDone(patientName + " "
                                        + getResources().getString(R.string.visit_data_failed), getResources().getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity.this);

                            }
                            uploaded = true;
                            if (/*isFollowUpOrClosed()*/ isCaseClosed() && isSynced) { //only when the sync is successful then only end the visit
                                //this fixes the bug of visit not ending even when ended if the flow was done quickly my the user
                                endVisit();
                            }
                        }
                    }, 4000);
                } else {
                    AppConstants.notificationUtils.DownloadDone(patientName + " " + getResources().getString(R.string.visit_data_failed), getResources().getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity.this);
                }
                //  }
                /*else {
                    TextView t = (TextView) speciality_spinner.getSelectedView();
                    if (t != null) {
                        t.setError(getResources().getString(R.string.please_select_specialization));
                        t.setTextColor(Color.RED);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(VisitSummaryActivity.this)
                            .setMessage(getResources().getString(R.string.please_select_specialization))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                }*/

            }
        });

        if (intentTag != null && intentTag.equals("prior")) {
            uploadButton.setEnabled(false);
        }

        queryData(String.valueOf(patientUuid));
        nameView = findViewById(R.id.textView_name_value);

        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        visitView = findViewById(R.id.textView_visit_value);
        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            idView.setText(patient.getOpenmrs_id());
        } else {
            idView.setText(getResources().getString(R.string.patient_not_registered));
        }

        nameView.setText(patientName);

        heightView = findViewById(R.id.textView_height_value);
        weightView = findViewById(R.id.textView_weight_value);
        pulseView = findViewById(R.id.textView_pulse_value);
        bpView = findViewById(R.id.textView_bp_value);
        tempView = findViewById(R.id.textView_temp_value);

        tempfaren = findViewById(R.id.textView_temp_faren);
        tempcel = findViewById(R.id.textView_temp);
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                                String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity.this, mFileName)));
            }
            if (obj.getBoolean("mCelsius")) {
                tempcel.setVisibility(View.VISIBLE);
                tempfaren.setVisibility(View.GONE);
                tempView.setText(temperature.getValue());
                Log.d("temp", "temp_C: " + temperature.getValue());
            } else if (obj.getBoolean("mFahrenheit")) {
                tempfaren.setVisibility(View.VISIBLE);
                tempcel.setVisibility(View.GONE);
                if (temperature.getValue() != null && !temperature.getValue().isEmpty()) {
                    tempView.setText(convertCtoF(temperature.getValue()));
                    Log.d("temp", "temp_F: " + tempView.getText().toString());
                }
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        spO2View = findViewById(R.id.textView_pulseox_value);
        respiratory = findViewById(R.id.textView_respiratory_value);
        respiratoryText = findViewById(R.id.textView_respiratory);
        bmiView = findViewById(R.id.textView_bmi_value);
        complaintView = findViewById(R.id.textView_content_complaint);
        famHistView = findViewById(R.id.textView_content_famhist);
        patHistView = findViewById(R.id.textView_content_pathist);
        physFindingsView = findViewById(R.id.textView_content_physexam);

        if (isRespiratory) {
            respiratoryText.setVisibility(View.VISIBLE);
            respiratory.setVisibility(View.VISIBLE);
        } else {
            respiratoryText.setVisibility(View.GONE);
            respiratory.setVisibility(View.GONE);
        }

        if (height.getValue() != null) {
            if (height.getValue().trim().equals("0")) {
                heightView.setText("-");
            } else {
                heightView.setText(height.getValue());
            }
        }

        weightView.setText(weight.getValue());
        pulseView.setText(pulse.getValue());

        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        if (bpText.equals("/")) {  //when new patient is being registered we get / for BP
            bpView.setText("");
        } else if (bpText.equalsIgnoreCase("null/null")) {
            //when we setup app and get data from other users, we get null/null from server...
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
//        tempView.setText(temperature.getValue());
        //    Respiratory added by mahiti dev team
        respiratory.setText(resp.getValue());
        spO2View.setText(spO2.getValue());
        if (complaint.getValue() != null)
            complaintView.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
        if (famHistory.getValue() != null)
            famHistView.setText(Html.fromHtml(famHistory.getValue()));
        if (patHistory.getValue() != null)
            patHistView.setText(Html.fromHtml(patHistory.getValue()));
        if (phyExam.getValue() != null)
            physFindingsView.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));


        editVitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(VisitSummaryActivity.this, VitalsActivity.class);
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                intent1.putExtra("name", patientName);
                intent1.putExtra("tag", "edit");
                startActivity(intent1);
            }
        });

        editFamHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder famHistDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                //final MaterialAlertDialogBuilder famHistDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity.this,R.style.AlertDialogStyle);
                famHistDialog.setTitle(getResources().getString(R.string.visit_summary_family_history));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                famHistDialog.setView(convertView);

                final TextView famHistText = convertView.findViewById(R.id.textView_entry);
                if (famHistory.getValue() != null)
                    famHistText.setText(Html.fromHtml(famHistory.getValue()));
                famHistText.setEnabled(false);

                famHistDialog.setPositiveButton(getResources().getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        // final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(getResources().getString(R.string.question_text_input));
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (famHistory.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(famHistory.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //famHistory.setValue(dialogEditText.getText().toString());
                                famHistory.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));

                                if (famHistory.getValue() != null) {
                                    famHistText.setText(Html.fromHtml(famHistory.getValue()));
                                    famHistView.setText(Html.fromHtml(famHistory.getValue()));
                                }
                                updateDatabase(famHistory.getValue(), UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(getResources().getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = textInput.show();
                        dialogInterface.dismiss();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
                    }
                });

                famHistDialog.setNeutralButton(getResources().getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                famHistDialog.setNegativeButton(getResources().getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent1 = new Intent(VisitSummaryActivity.this, FamilyHistoryActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("edit_FamHist", "edit_FamHist");

                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                        intent1.putExtra("tag", "edit");
                        startActivity(intent1);
                        dialogInterface.dismiss();
                    }
                });

//                famHistDialog.show();
                AlertDialog alertDialog = famHistDialog.create();
                alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                // pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                //nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity.this, R.font.lato_bold));
                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
            }
        });

        editComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder complaintDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                complaintDialog.setTitle(getResources().getString(R.string.visit_summary_complaint));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                complaintDialog.setView(convertView);

                final TextView complaintText = convertView.findViewById(R.id.textView_entry);
                if (complaint.getValue() != null) {
                    complaintText.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
                }
                complaintText.setEnabled(false);

                complaintDialog.setPositiveButton(getResources().getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(getResources().getString(R.string.question_text_input));
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (complaint.getValue() != null) {
                            dialogEditText.setText(Html.fromHtml(complaint.getValue()));
                        } else {
                            dialogEditText.setText("");
                        }
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String input = dialogEditText.getText().toString();
                                input = applyBoldTag(input);
                                complaint.setValue(input.replace("\n", "<br>"));
//                                complaint.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
                                if (complaint.getValue() != null) {
//                                    complaintText.setText(Html.fromHtml(complaint.getValue()));
                                    complaintView.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
                                }
                                updateDatabase(complaint.getValue(), UuidDictionary.CURRENT_COMPLAINT);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNeutralButton(getResources().getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });

                complaintDialog.setNegativeButton(getResources().getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Deleting the old image in physcial examination
                        if (obsImgdir.exists()) {
                            ImagesDAO imagesDAO = new ImagesDAO();

                            try {
                                List<String> imageList = imagesDAO.getImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                                for (String obsImageUuid : imageList) {
                                    String imageName = obsImageUuid + ".jpg";
                                    new File(obsImgdir, imageName).deleteOnExit();
                                }
                                imagesDAO.deleteConceptImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                            } catch (DAOException e1) {
                                FirebaseCrashlytics.getInstance().recordException(e1);
                            }
                        }

                        Intent intent1 = new Intent(VisitSummaryActivity.this, ComplaintNodeActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                        intent1.putExtra("tag", "edit");
                        startActivity(intent1);
                        dialogInterface.dismiss();
                    }
                });

                complaintDialog.setNeutralButton(getResources().getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                //complaintDialog.show();
                AlertDialog alertDialog = complaintDialog.create();
                alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
            }
        });

        editPhysical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder physicalDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                physicalDialog.setTitle(getResources().getString(R.string.visit_summary_on_examination));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                physicalDialog.setView(convertView);

                final TextView physicalText = convertView.findViewById(R.id.textView_entry);
                if (phyExam.getValue() != null)
                    physicalText.setText(Html.fromHtml(phyExam.getValue()));
                physicalText.setEnabled(false);

                physicalDialog.setPositiveButton(getResources().getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(getResources().getString(R.string.question_text_input));
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (phyExam.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(phyExam.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                phyExam.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
                                if (phyExam.getValue() != null) {
                                    physicalText.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));
                                    physFindingsView.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));
                                }
                                updateDatabase(phyExam.getValue(), UuidDictionary.PHYSICAL_EXAMINATION);
                                dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(getResources().getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, dialog);
                        dialogInterface.dismiss();
                    }
                });

                physicalDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (obsImgdir.exists()) {
                            ImagesDAO imagesDAO = new ImagesDAO();

                            try {
                                List<String> imageList = imagesDAO.getImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                                for (String obsImageUuid : imageList) {
                                    String imageName = obsImageUuid + ".jpg";
                                    new File(obsImgdir, imageName).deleteOnExit();
                                }
                                imagesDAO.deleteConceptImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                            } catch (DAOException e1) {
                                FirebaseCrashlytics.getInstance().recordException(e1);
                            }
                        }
                        Intent intent1 = new Intent(VisitSummaryActivity.this, PhysicalExamActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
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

                AlertDialog alertDialog = physicalDialog.create();
                alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
            }
        });

        editMedHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder historyDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
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
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
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
                                //patHistory.setValue(dialogEditText.getText().toString());
                                patHistory.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));

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
                        AlertDialog dialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, dialog);
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
                        intent1.putExtra("edit_PatHist", "edit_PatHist");

                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
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

//                historyDialog.show();
                AlertDialog alertDialog = historyDialog.create();
                alertDialog.show();
                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity.this, R.font.lato_bold));
                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
            }
        });

        editAddDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addDocs = new Intent(VisitSummaryActivity.this, AdditionalDocumentsActivity.class);
                addDocs.putExtra("patientUuid", patientUuid);
                addDocs.putExtra("visitUuid", visitUuid);
                addDocs.putExtra("encounterUuidVitals", encounterVitals);
                addDocs.putExtra("encounterUuidAdultIntial", encounterAdultInit);
                addDocs.putStringArrayListExtra("encounterAdultInitList", (ArrayList<String>) encounterAdultInitList);
                // addDocs.putExtra("encounterUuidAdultIntial", encounterAdultInitList);
                startActivity(addDocs);
            }
        });
//        if(imagebutton_edit_editFacility.getVisibility()==View.VISIBLE){
//
//            autocompleteState.setEnabled(true);
//            autocompleteDistrict.setEnabled(true);
//            mFacilitySelection.setEnabled(true);
//        }
//        else{
//            autocompleteState.setEnabled(false);
//            autocompleteDistrict.setEnabled(false);
//            mFacilitySelection.setEnabled(false);
//        }


        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkConnection.isOnline(getApplication())) {
                    Toast.makeText(context1, getResources().getString(R.string.downloading), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context1, getResources().getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
                }

                SyncUtils syncUtils = new SyncUtils();
                syncUtils.syncForeground("downloadPrescription");
                uploaded = true;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadPrescription();
//                        pd.dismiss();
                    }
                }, 5000);
            }
        });

        onExaminationDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startDownload(UuidDictionary.COMPLEX_IMAGE_PE, patientUuid);
            }
        });

        doQuery();

//        EncounterDAO encounterDAO = new EncounterDAO();
//        String emergencyUuid = "";
//        try {
//            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
//        } catch (DAOException e) {
//            FirebaseCrashlytics.getInstance().recordException(e);
//        }
//
//        //visit is uploaded to server checking in this case the checkbox will be disabled...
//        if (!isSynedFlag.equalsIgnoreCase("0")) {
//            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
//                //i.e the visit is a priority visit since getEmergencyEncounters() checks for voided = 0 i.e. priority...
//                flag.setChecked(true);
//                flag.setEnabled(false);
//            } else {
//                flag.setChecked(false);
//                flag.setEnabled(false);
//            }
//        } else {
//            //to set the checkbox as checked in offline mode so that i can be modified later...
//            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
//                //i.e the visit is a priority visit since getEmergencyEncounters() checks for voided = 0 i.e. priority...
//                flag.setChecked(true);
//            } else {
//                flag.setChecked(false);
//            }
//        }

        button_resolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConnection.isOnline(context)) {
                    Toast.makeText(VisitSummaryActivity.this, R.string.please_connect_to_internet, Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadFacility();
                if (latestVisitUuid == null || latestVisitUuid.isEmpty())
                    latestVisitUuid = visitUuid;

                if (visitUUID == null || visitUUID.isEmpty()) {
                    String visitIDorderBy = "startdate";
                    String visitIDSelection = "uuid = ?";
                    String[] visitIDArgs = {latestVisitUuid}; //so that it fetches only the latest last visit everytime.
                    final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
                    if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
                        visitIDCursor.moveToFirst();
                        visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                    }
                    if (visitIDCursor != null) visitIDCursor.close();
                }
                boolean synced = false;
                Cursor idCursor = db.rawQuery("select v.sync from tbl_visit as v where v.uuid = ?",
                        new String[]{latestVisitUuid});

                if (idCursor.getCount() != 0) {
                    while (idCursor.moveToNext()) {
                        String value = idCursor.getString(idCursor.getColumnIndexOrThrow("sync"));
                        if ("1".equalsIgnoreCase(value) || Boolean.parseBoolean(value)) {
                            synced = true;
                        }
                    }
                }
                idCursor.close();

                if (visitUUID != null && !visitUUID.isEmpty() && synced && !complaintList_adapter.isEmpty()) {
                    Intent intent1 = new Intent(VisitSummaryActivity.this, ResolutionActivity.class);
                    intent1.putExtra("patientUuid", patientUuid);
                    intent1.putExtra("visitUuid", latestVisitUuid);
                    intent1.putExtra("encounterUuidVitals", encounterVitals);
                    intent1.putExtra("edit_PatHist", "edit_PatHist");
                    intent1.putExtra("encounterUuidAdultIntial", encounterAdultInit); //latest visit uuid
                    intent1.putExtra("name", patientName);
                    intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent1.putExtra("tag", "edit");

                    if (complaintList_adapter.get(complaintList_adapter.size() - 1).contains(":")) {
                        String substring = complaintList_adapter.get(complaintList_adapter.size() - 1)
                                .substring(0, complaintList_adapter.get(complaintList_adapter.size() - 1).indexOf(":"));
                        boolean value = substring.toLowerCase().contains("violence") ||
                                complaintList_adapter.get(complaintList_adapter.size() - 1).toLowerCase().contains("हिंसा");
                        intent1.putExtra("resolutionViolence", value);
                    } else {
                        boolean value = complaintList_adapter.get(complaintList_adapter.size() - 1).toLowerCase().contains("violence")
                                || complaintList_adapter.get(complaintList_adapter.size() - 1).toLowerCase().contains("हिंसा");
                        intent1.putExtra("resolutionViolence", value);
                    }

                    launcher.launch(intent1);
//                    startActivityForResult(intent1, 100);
                } else {
                    Toast.makeText(VisitSummaryActivity.this, R.string.resolution_upload_reminder,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isFollowUpOrClosed()) {
            button_resolution.setVisibility(View.GONE);
        }
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), o -> button_resolution.setVisibility(View.GONE)
    );
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 100 && resultCode == RESULT_OK) {
//            /*button_resolution.setEnabled(false);
//            button_resolution.setBackgroundColor(getResources().getColor(R.color.font_black_4));*/
//
//        }
//    }

    private void shareEmail() {
        String to = "";
        String subject = "E-Prescription";
        String body = prescriptionUrl + patientUuid + "/" + sessionManager.getAppLanguage();
        //https://training.vikalpindia.org/intelehealth/index.html#/prescription/patientId/en
        Log.v("main", "prescurl: " + body);
        String mailTo = "mailto:" + to +
                "?&subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body);
        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setData(Uri.parse(mailTo));
        startActivity(emailIntent);
    }

    private void shareWhatsapp(String phoneNumberWithCountryCode) {
        //Whatsapp is not accepting special characters # so need to encode it.
        String url = "";
        url = URLEncoder.encode(prescriptionUrl);
        String message = url + patientUuid + "/" + sessionManager.getAppLanguage();
        Log.v("main", "prescurl: " + message);
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(
                        String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                phoneNumberWithCountryCode, message))));
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

    private String sms_prescription() {
        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name()))
                ? patient.getMiddle_name() : "") + " " + patient.getLast_name();
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
        String[] visitIDArgs = {visitUuid};
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = SimpleDatetoLongDate(startDateTime);

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
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, VisitSummaryActivity.this),
                                String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity.this,
                                        AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity.this, mFileName)));
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
        // String mComplaint = complaint.getValue();
        String mComplaint = complaintList_adapter.get(complaintList_adapter.size() - 1);

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = StringUtils.split(mComplaint, Node.bullet_arrow);
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
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3);
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
            }
        } else {

        }

        if (mPatientOpenMRSID == null) {
            mPatientOpenMRSID = getString(R.string.patient_not_registered);
        }

        String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
        String para_close = "</p>";

        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(mPatientDob);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        String rx_web = stringToWeb_sms(rxReturned);

        String tests_web = stringToWeb_sms(testsReturned.trim().replace("\n\n", "\n")
                .replace(Node.bullet, ""));

        String advice_web = stringToWeb_sms(medicalAdvice_string.trim().replace("\n\n", "\n"));
        Log.d("Hyperlink", "hyper_print: " + advice_web);

        String diagnosis_web = stringToWeb_sms(diagnosisReturned);

        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parseDateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
            } else {
                followUpDateStr = followUpDate;
            }
        } else {
            followUpDateStr = followUpDate;
        }

        String followUp_web = stringToWeb_sms(followUpDateStr);

        String doctor_web = stringToWeb_sms(doctorName);

        String heading = prescription1;
        String heading2 = prescription2;
        String heading3 = "<br/>";

        String bp = mBP;
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        String address = mState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");

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

        // Generate an HTML document on the fly:
        String fontFamilyFile = "";
        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
            Log.d("font", "font: " + objClsDoctorDetails.getFontOfSign());
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
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
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) +
                    objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr =/* "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +*/

                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
                            "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "18003094144" + "</span>" /*+*/

                   /* "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification()
                    + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +

                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +*/

            /*"</div>"*/;

        }

//        if (isRespiratory) {
        String resolution = getResolution(visitUuid);
        String htmlDocument =
                String.format("<b id=\"heading_1\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b><br>" +
                                "<b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b>" +
                                "<br><br>" +

                                /*"<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +*/
//                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                /* doctorDetailStr +*/


                                "<b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</b><br>" +
                                "<b id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </b>" +
                                "<br><br>" +

                                  /*  "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +*/

                                   /* "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +*/

                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/

                                  /*  "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
                                    para_open + "%s" + para_close + "<br><br>" +*/

                                "<b hidden id=\"resolution_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Resolution <br>" +
                                "%s </b><br>" +
                                /*"<b hidden id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis <br>" +
                                "%s </b><br>" +
                                "<b hidden id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan <br>" +
                                "%s </b><br>" +
                                "<b hidden id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s) <br>" +
                                "%s " + "</b><br>" +
                                "<b hidden id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Advice <br>" +
                                "%s" + "</b><br>" +
                                "<b hidden id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date <br>" +
                                "%s" + "</b><br>" +*/
                                /* "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +*/

                                /* "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +*/

                                doctorDetailStr
                        /*"<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" +*/

                        /*  doctrRegistartionNum + "</p>" +*/

                        /*"</div>"*/
                        , heading, heading2, /*heading3,*/ mPatientName, age, mGender, /*mSdw*/
//                            address, mPatientOpenMRSID, mDate,

                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/

//                            (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "",
//                            (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",

                        /*pat_hist, fam_hist,*/ /*mComplaint,*/
                        resolution
//                        !diagnosis_web.isEmpty() ? diagnosis_web : stringToWeb_sms("Not Provided"),
//                        !rx_web.isEmpty() ? rx_web : stringToWeb_sms("Not Provided"),
//                        !tests_web.isEmpty() ? tests_web : stringToWeb_sms("Not Provided"),
//                        !advice_web.isEmpty() ? advice_web : stringToWeb_sms("Not Provided"),
//                        !followUp_web.isEmpty() ? followUp_web : stringToWeb_sms("Not Provided"),
//                        !doctor_web.isEmpty() ? doctor_web : stringToWeb_sms("Not Provided")
                );

        Log.d("html", "html:ppp " + Html.fromHtml(htmlDocument));
        //   webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
/*        } else {
            htmlDocument =
                    String.format( "<b id=\"heading_1\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b><br>" +
                                    "<b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b>" +
                                    "<br><br>" +

                                  *//*  "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +*//*

                                    "<b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</b><br>" +
                                    "<b id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </b>" +
                                    "<br><br>" +

                                   *//* "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +*//*

         *//* "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +*//*

         *//*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*//*

         *//* "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
                                    para_open + "%s" + para_close + "<br><br>" +*//*

                                    "<b id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis <br>" +
                                    "%s </b><br>" +
                                    "<b id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan <br>" +
                                    "%s </b><br>" +
                                    "<b id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s) <br>" +
                                    "%s " + "</b><br>" +
                                    "<b id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Advice <br>" +
                                    "%s" + "</b><br>" +
                                    "<b id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date <br>" +
                                    "%s" + "</b><br>" +
                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +

                                   *//* "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" +*//*
                                    doctorDetailStr +
                                    "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" +

                                   *//* + doctrRegistartionNum + "</span>" +*//*
                                    "</div>"

                            , heading, heading2,*//* heading3,*//* mPatientName, age, mGender, *//*mSdw*//* *//*address, mPatientOpenMRSID, mDate,*//*
         *//*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*//*

         *//* (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "",
                            (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",*//*

         *//*pat_hist, fam_hist,*//* *//*mComplaint,*//*

                            diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            Log.d("html","html:ppp "+ Html.fromHtml(htmlDocument));
            // webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        }*/
        //   webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        return htmlDocument;
    }

    /**
     * //     * @param uuid the visit uuid of the patient visit records is passed to the function.
     *
     * @return boolean value will be returned depending upon if the row exists in the tbl_visit_attribute tbl
     */

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

    private boolean speciality_row_exist_check(String uuid) {
        boolean isExists = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=?",
                new String[]{uuid});

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                isExists = true;
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return isExists;
    }


    private String convertCtoF(String temperature) {

        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
//        DecimalFormat dtime = new DecimalFormat("0.00");
//        b = Double.parseDouble(dtime.format(b));
//        int IntValue = (int) Math.round(b);
        double roundOff = Math.round(b * 100.0) / 100.0;
        resultVal = nf.format(roundOff);
        return resultVal;

    }

    private String showVisitID() {

        if (visitUUID != null && !visitUUID.isEmpty()) {
            String hideVisitUUID = visitUUID;
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            visitView.setText("XXXX" + hideVisitUUID);
        }

        return visitView.getText().toString();
    }


    private void doQuery() {

        if (visitUUID != null && !visitUUID.isEmpty()) {

            String query = "SELECT   a.uuid, a.sync " +
                    "FROM tbl_visit a " +
                    "WHERE a.uuid = '" + visitUUID + "'";

            final Cursor cursor = db.rawQuery(query, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        isSynedFlag = cursor.getString(cursor.getColumnIndexOrThrow("sync"));
                    } while (cursor.moveToNext());
                }
            }
            if (cursor != null) {
                cursor.close();
            }

            Log.e("ISSYNCED==", isSynedFlag);

            if (!isSynedFlag.equalsIgnoreCase("0")) {
                String hideVisitUUID = visitUUID;
                hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
                visitView.setText("XXXX" + hideVisitUUID);
            } else {
                visitView.setText(getResources().getString(R.string.visit_not_uploaded));
            }
        } else {
            if (visitUuid != null && !visitUuid.isEmpty()) {
                String hideVisitUUID = visitUuid;
                hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
                visitView.setText("XXXX" + hideVisitUUID);
            }
        }
    }

/*
    private void physcialExaminationImagesDownload() {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            List<String> imageList = imagesDAO.isImageListObsExists(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
            for (String images : imageList) {
                if (imagesDAO.isLocalImageUuidExists(images))
                    physcialExaminationDownloadText.setVisibility(View.GONE);
                else
                    physcialExaminationDownloadText.setVisibility(View.VISIBLE);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        physcialExaminationDownloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_PE, patientUuid);
                physcialExaminationDownloadText.setVisibility(View.GONE);
            }
        });
    }
*/

    private void additionalDocumentImagesDownload() {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            List<String> obsUuidList = imagesDAO.isImageListObsExists(encounterAdultInitList,
                    UuidDictionary.COMPLEX_IMAGE_AD);
            if (obsUuidList.size() == 0) {
                additionalImageDownloadText.setVisibility(View.GONE); //This means the visit is a new one...
            } else {
                additionalImageDownloadText.setVisibility(View.VISIBLE); //This means the app is fresh installed...
            }

            List<String> imageList = imagesDAO.get_tbl_additional_doc(patientUuid, encounterAdultInitList);

            for (String images : imageList) {
                if (imagesDAO.isLocalImageUuidExists(images))
                    additionalImageDownloadText.setVisibility(View.GONE);
                else
                    additionalImageDownloadText.setVisibility(View.VISIBLE);
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }

        additionalImageDownloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_AD, patientUuid);
                additionalImageDownloadText.setVisibility(View.GONE);
            }
        });

    }

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
            mPhysicalExamsLayoutManager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false);
            mPhysicalExamsRecyclerView.setLayoutManager(mPhysicalExamsLayoutManager);
            mPhysicalExamsRecyclerView.setAdapter(horizontalAdapter);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
    }

    private void startDownload(String imageType, String patientUuid) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putStringArrayListExtra("encounterUuidAdultIntialList", (ArrayList<String>) encounterAdultInitList);
        intent.putExtra("ImageType", imageType);
        intent.putExtra("patientUuid", patientUuid);
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

    private String stringToWeb_sms(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<b style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</b><br>";
            formatted = para_open + "- " +
                    input.replaceAll("\n", para_close + para_open + "- ")
                    + para_close;
        }
        return formatted;
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd MMM yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    //print button start
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
        String[] visitIDArgs = {visitUuid};
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = SimpleDatetoLongDate(startDateTime);

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
        // String mComplaint = complaint.getValue();
        String mComplaint = complaintList_adapter.get(complaintList_adapter.size() - 1);

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = StringUtils.split(mComplaint, Node.bullet_arrow);
        mComplaint = "";
        String colon = ":";
        String mComplaint_new = "";
        if (complaints != null) {
            for (String comp : complaints) {
                if (!comp.trim().isEmpty()) {
                    if (comp.contains(colon)) {
                        mComplaint = mComplaint + Node.big_bullet + comp.substring(0, comp.indexOf(colon)) + "<br/>";
                    } else {
                        mComplaint = mComplaint + Node.big_bullet + comp + "<br/>";
                    }

                }
            }
            if (!mComplaint.isEmpty()) {
                mComplaint = mComplaint.substring(0, mComplaint.length() - 2);
                mComplaint = mComplaint.replaceAll("<b>", "");
                mComplaint = mComplaint.replaceAll("</b>", "");
            }
        }

        if (mComplaint.contains("Associated symptoms")) {
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3);
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
            }
        } else {

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

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n")
                .replace(Node.bullet, ""));

        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
        Log.d("Hyperlink", "hyper_print: " + advice_web);

        String diagnosis_web = stringToWeb(diagnosisReturned);

        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parseDateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
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

        String bp = mBP;
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        String address = mState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");

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

        // Generate an HTML document on the fly:
        String fontFamilyFile = "";
        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
            Log.d("font", "font: " + objClsDoctorDetails.getFontOfSign());
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
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
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
                    "</div>";

        }
        if (isRespiratory || true) {
            String resolution = getResolution(visitUuid);
            String htmlDocument =
                    String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                    /* doctorDetailStr +*/
                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" +
                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
                                    "<b><p hidden id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p hidden id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                                    "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
                                    para_open + "%s" + para_close + "<br><br>" +
                                    "<b><p id=\"resolution_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Resolution & Feedback</p></b>" +
                                    "%s<br>" +
                                    "<u><b><p hidden id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p hidden id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p hidden id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p hidden id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p hidden id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" +
                                    "%s<br>" +
                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                                    doctorDetailStr +
                                    "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" +
                                    "</div>"
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/ (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "",
                            (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            /*pat_hist, fam_hist,*/ mComplaint, resolution, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument =
                    String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" +
                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
                                    "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                                    "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
                                    para_open + "%s" + para_close + "<br><br>" +
                                    "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" +
                                    "%s<br>" +
                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" +
                                    doctorDetailStr +
                                    "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" +
                                    "</div>"
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/ (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        }

        /**
         * +
         * "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
         * "%s"
         */

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private String getResolution(String visitUuid) {
        String resolution = "N/A";
        Cursor idCursor = db.rawQuery("select o.value from tbl_obs as o where o.conceptuuid = ? and o.encounteruuid in (select uuid from tbl_encounter as e where e.visituuid = ?)",
                new String[]{UuidDictionary.CONCEPT_RESOLUTION, visitUuid});

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                resolution = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
            }
        }
        idCursor.close();
        return resolution;
    }

    //print button end

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
                int webview_heightContent = view.getContentHeight();
                Log.d("variable i", "variable i: " + webview_heightContent);
                createWebPrintJob(view, webview_heightContent);
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
        String mDate = SimpleDatetoLongDate(startDateTime);

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
                    mTemp = "Temperature(F): " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        mresp = resp.getValue();
        mSPO2 = "SpO2(%): " + (!TextUtils.isEmpty(spO2.getValue()) ? spO2.getValue() : "");
        // String mComplaint = complaint.getValue();
        String mComplaint = complaintList_adapter.get(complaintList_adapter.size() - 1);

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = StringUtils.split(mComplaint, Node.bullet_arrow);
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
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3);
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
            }
        } else {

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

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n")
                .replace(Node.bullet, ""));

        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
        Log.d("Hyperlink", "hyper_print: " + advice_web);

        String diagnosis_web = stringToWeb(diagnosisReturned);

        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parseDateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
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

        String bp = mBP;
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        String address = mState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");

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
        String fontFamilyFile = "";
        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
            Log.d("font", "font: " + objClsDoctorDetails.getFontOfSign());
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
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
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();


            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
                    "</div>";

        }
        if (isRespiratory) {
            String htmlDocument =
                    String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                    /* doctorDetailStr +*/
                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" +
                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
                                    "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                                    "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" +
                                    para_open + "%s" + para_close + "<br><br>" +
                                    "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" +
                                    "%s<br>" +
                                    "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" +
                                    "%s<br>" +
                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                                    doctorDetailStr +
                                    "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" +
                                    "</div>"
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/ (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument =
                    String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" +
                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
                                    "<p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                                    "<p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p>" +
                                    para_open + "%s" + para_close + "<br><br>" +
                                    "<b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b>" +
                                    "%s<br>" +
                                    "<b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b>" +
                                    "%s<br>" +
                                    "<b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b>" +
                                    "%s<br>" +
                                    "<b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b>" +
                                    "%s<br>" +
                                    "<b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b>" +
                                    "%s<br>" +
                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" +
                                    doctorDetailStr +
                                    "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" +
                                    "</div>"
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/
                            (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        }

        /**
         * +
         * "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
         * "%s"
         */

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    /**
     * This method creates a print job using PrintManager instance and PrintAdapter Instance
     *
     * @param webView       object of type WebView.
     * @param contentHeight
     */

    //print button start
    private void createWebPrintJob_Button(WebView webView, int contentHeight) {

//       int i =  webView.getContentHeight();
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

//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());
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

//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());
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

//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());
        } else {
            //small size prescription...
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            Log.d("PrintPDF", "PrintPDF");
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter,
                    pBuilder.build());

        }
    }

    private void createWebPrintJob(WebView webView, int contentHeight) {

//       int i =  webView.getContentHeight();
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

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {
                            }

                            @Override
                            public void onFailure() {
                            }
                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {
                                }

                                @Override
                                public void onFailure() {
                                }
                            });
                }
            }

        } else if (contentHeight == 0) {
            //in case of webview bug of 0 contents...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {

                            }

                            @Override
                            public void onFailure() {

                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {

                                }

                                @Override
                                public void onFailure() {

                                }

                            });
                }
            }

        } else if (contentHeight > 3000) {
            //large size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {

                            }

                            @Override
                            public void onFailure() {

                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {

                                }

                                @Override
                                public void onFailure() {

                                }

                            });
                }
            }

        } else {
            //small size prescription...
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            Log.d("PrintPDF", "PrintPDF");
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //end...

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {

                            }

                            @Override
                            public void onFailure() {

                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {

                                }

                                @Override
                                public void onFailure() {

                                }

                            });
                }
            }
        }
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
                MaterialAlertDialogBuilder followUpAlert = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                followUpAlert.setMessage(getString(R.string.visit_summary_follow_up_reminder) + followUpDate);
                followUpAlert.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(VisitSummaryActivity.this, PatientSurveyActivity.class);
                        intent.putExtra("patientUuid", patientUuid);
                        intent.putExtra("visitUuid", visitUuid);
                        intent.putExtra("encounterUuidVitals", encounterVitals);
                        intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("tag", intentTag);
                        startActivity(intent);
                    }
                });
                followUpAlert.show();
            } else {
                Intent intent = new Intent(VisitSummaryActivity.this, PatientSurveyActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("tag", intentTag);
                startActivity(intent);
            }
        } else {

            Log.d(TAG, "endVisit: null");
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
            alertDialogBuilder.setMessage(getResources().getString(R.string.visit_summary_upload_reminder));
            alertDialogBuilder.setNeutralButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);

        }
    }


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


        try {

            String FacilityHistSelection = "encounteruuid = ? AND conceptuuid = ?";

            String[] FacilityHistArgs = {encounterAdultInitList.get(encounterAdultInitList.size() - 1), UuidDictionary.Facility};
            Cursor medHistCursor = db.query("tbl_obs", columns, FacilityHistSelection, FacilityHistArgs, null, null, null);
            medHistCursor.moveToLast();
            String facilityText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
//            patHistory.setValue(facilityText);
//todo single textutill
            if (!TextUtils.isEmpty(facilityText)) {

//                medHistory = patHistory.getValue();
//Toast.makeText(this,"fa=== "+facilityText,Toast.LENGTH_LONG).show();
                if (facilityText == null) {

                } else if (facilityText.equalsIgnoreCase("-")) {
                    autocompleteState.setText("");
                    autocompleteDistrict.setText("");
                    txtViewFacility.setText("");
                    mFacilityValue = "";
                } else {
                    String[] arrayString = facilityText.split(",", 3);


                    autocompleteState.setText("" + arrayString[0]);
                    mState = arrayString[0];
                    autocompleteDistrict.setText("" + arrayString[1]);
                    mDistrict = arrayString[1];
                    mFacilitySelection.setVisibility(View.GONE);
                    mFacilityValue = arrayString[arrayString.length - 1];
                    txtViewFacility.setText(arrayString[arrayString.length - 1].replaceAll("\\|", "\n\n"));
                }

//                medHistory = medHistory.replace("\"", "");
//                medHistory = medHistory.replace("\n", "");
//                do {
//                    medHistory = medHistory.replace("  ", "");
//                } while (medHistory.contains("  "));
            } else {
                autocompleteState.setText("");
                autocompleteDistrict.setText("");
                txtViewFacility.setText("");
                mFacilityValue = "";
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            autocompleteState.setText("");
            autocompleteDistrict.setText("");
            txtViewFacility.setText("");
            // if facility  does not exist
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
//        downloadDoctorDetails();
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
                medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink + medicalAdvice_string.replaceAll("\n", "<br><br>")));
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

    ClsDoctorDetails objClsDoctorDetails;

    private void parseDoctorDetails(String dbValue) {
        Gson gson = new Gson();
        //  dbValue = dbValue.replace("{", "");
        try {
            objClsDoctorDetails = gson.fromJson(dbValue, ClsDoctorDetails.class);
            Log.e(TAG, "TEST DB: " + dbValue);
            Log.e(TAG, "TEST VISIT: " + objClsDoctorDetails);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(context, getResources().getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT).show();
        }

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {

            frameLayout_doctor.setVisibility(View.VISIBLE);

            doctorSign = objClsDoctorDetails.getTextOfSign();

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
                    "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getName()) ? objClsDoctorDetails.getName() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" + "  " +
                    (!TextUtils.isEmpty(objClsDoctorDetails.getQualification()) ? objClsDoctorDetails.getQualification() : "") + ", "
                    + (!TextUtils.isEmpty(objClsDoctorDetails.getSpecialization()) ? objClsDoctorDetails.getSpecialization() : "") + "</span><br>" +
                    // "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " + objClsDoctorDetails.getRegistrationNumber() : "") +
                    "</div>";

            mDoctorName.setText(Html.fromHtml(doctorDetailStr).toString().trim());
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

    public void updateDatabase(String string, String conceptID) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(String.valueOf(conceptID));
//          obsDTO.setEncounteruuid(encounterUuidAdultIntial);
            obsDTO.setEncounteruuid(encounterAdultInit); //latest visit encounter.
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultInit, String.valueOf(conceptID)));

            obsDAO.updateObs(obsDTO);


        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultInit);
            encounterDAO.updateEncounterModifiedDate(encounterAdultInit);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
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
        if (downloadPrescriptionService == null) {
            registerDownloadPrescription();
        }

        super.onResume();
        callBroadcastReceiver();

        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileNameList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileNameList = imagesDAO.getFilename(patientUuid, encounterAdultInitList);
            for (String file_imagename : fileNameList) {
                String filename = AppConstants.IMAGE_PATH + file_imagename + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mAdditionalDocsLayoutManager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false);
            mAdditionalDocsRecyclerView.setLayoutManager(mAdditionalDocsLayoutManager);
            mAdditionalDocsRecyclerView.setAdapter(horizontalAdapter); //TODO: here on VS screen we show the images based on image names.
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
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

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }

    }

    public void sendSMS() {
        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(this);
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

                header = getString(R.string.patient_id_) + patient.getOpenmrs_id() + "\n"
                        + getString(R.string.patient_name_title) + patient.getFirst_name() + " " + patient.getLast_name() + "\n"
                        + getString(R.string.patient_DOB) + patient.getDate_of_birth() + "\n";


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

                if (followUpDateCard.getVisibility() == View.VISIBLE) {
                    if (!followUpDateTextView.getText().toString().trim().isEmpty())
                        body = body + getResources().getString(R.string.visit_summary_follow_up_date) + ":" +
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

        textInput.setNegativeButton(getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = textInput.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    public void downloadPrescription() {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            if (visitsDAO.getDownloadedValue(visitUuid).equalsIgnoreCase("false") && uploaded) {
                String visitnote = "";
                EncounterDAO encounterDAO = new EncounterDAO();
                String encounterIDSelection = "visituuid = ? ";
                String[] encounterIDArgs = {visitUuid};
                Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                    } while (encounterCursor.moveToNext());

                }
                if (encounterCursor != null) {
                    encounterCursor.close();
                }
                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = "";
                    diagnosisTextView.setText("");
                    diagnosisCard.setVisibility(View.GONE);
                }
                if (!rxReturned.isEmpty()) {
                    rxReturned = "";
                    prescriptionTextView.setText("");
                    prescriptionCard.setVisibility(View.GONE);

                }
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = "";
                    medicalAdviceTextView.setText("");
                    medicalAdviceCard.setVisibility(View.GONE);
                }
                if (!testsReturned.isEmpty()) {
                    testsReturned = "";
                    requestedTestsTextView.setText("");
                    requestedTestsCard.setVisibility(View.GONE);
                }

                if (!followUpDate.isEmpty()) {
                    followUpDate = "";
                    followUpDateTextView.setText("");
                    followUpDateCard.setVisibility(View.GONE);
                }
                String[] columns = {"value", " conceptuuid"};
                String visitSelection = "encounteruuid = ? and voided!='1'";
                String[] visitArgs = {visitnote};
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

                //checks if prescription is downloaded and if so then sets the icon color.
                if (hasPrescription.equalsIgnoreCase("true")) {
                    ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
                }

                if (uploaded) {
                    try {
                        downloaded = visitsDAO.isUpdatedDownloadColumn(visitUuid, true);
                        Log.v("visituuid", "download_vuuid: " + visitUuid);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                downloadDoctorDetails();
            }

            additionalDocumentImagesDownload();
            //  physcialExaminationImagesDownload();

        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    public void downloadPrescriptionDefault() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ?";
        String[] encounterIDArgs = {visitUuid};
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs,
                null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
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

        //In onStop() it will check and unregister the receiver...
        if (receiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            receiver = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        isReceiverRegistered = false;
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
        int check = 0;
        if (data != null) {
            check = data.getInt("Restart");
        }
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

            } else {
                Log.i(TAG, "found sothing for test");

            }
        }
    }

    private void addDownloadButton() {
        if (!downloadButton.isEnabled()) {
            downloadButton.setEnabled(true);
            downloadButton.setVisibility(View.VISIBLE);
        }
        button_resolution.setVisibility(View.VISIBLE);

        if (isFollowUpOrClosed()) {
            button_resolution.setVisibility(View.GONE);
        }
    }

    private boolean isFollowUpOrClosed() {
        boolean flag = false;

        if (complaintList_adapter.size() > 0) {
            String complaintData = complaintList_adapter.get(complaintList_adapter.size() - 1);
            if (complaintView != null) {
                String i = complaintData.toLowerCase().replaceAll("\\s+", "");
                Log.v("main", "vi: " + i);
                if (complaintData.toLowerCase().replaceAll("\\s+", "").contains("b.domesticviolence-follow-up:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("घरेलूहिंसा-फ़ॉलो-अप:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("c.domesticviolence-caseclosed:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("घरेलूहिंसा-केसबंद:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("e.safeabortion-follow-up:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("सुरक्षितगर्भपात-फ़ॉलो-अप:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("f.safeabortion-querybyrelativesorothers:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("सुरक्षितगर्भपात-रिश्तेदारोंयाअन्यलोगोंद्वारापूछताछ:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("g.safeabortion-caseclosed:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("सुरक्षितगर्भपात-केसबंद:")) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
        }
        return flag;
    }

    private boolean isCaseClosed() {
        boolean flag = false;
        if (!complaintList_adapter.isEmpty()) {
            String complaintData = complaintList_adapter.get(complaintList_adapter.size() - 1);
            if (complaintView != null) {
                String i = complaintData.toLowerCase().replaceAll("\\s+", "");
                Log.v("main", "vi: " + i);

                flag = complaintData.toLowerCase().replaceAll("\\s+", "").contains("c.domesticviolence-caseclosed:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("घरेलूहिंसा-केसबंद:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("g.safeabortion-caseclosed:") ||
                        complaintData.toLowerCase().replaceAll("\\s+", "").contains("सुरक्षितगर्भपात-केसबंद:");
            }
        }
        return flag;
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

    public class DownloadPrescriptionService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD(TAG, "Download prescription happen" + new SimpleDateFormat("yyyy MM dd_HH mm ss").format(Calendar.getInstance().getTime()));
            downloadPrescriptionDefault();
//            downloadDoctorDetails();
        }
    }

    private void downloadDoctorDetails() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitUuid};
        String encounter_type_uuid_comp = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e";// make the encounter_type_uuid as constant later on.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounter_type_uuid_comp.equalsIgnoreCase(encounterCursor.getString
                        (encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
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
                if (!UuidDictionary.CONCEPT_RESOLUTION.equalsIgnoreCase(dbConceptID))
                    parseDoctorDetails(dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    public String applyBoldTag(String input) {
        String result = input;
        if (input == null)
            return null;
        try {
            if (result.contains("►") && result.contains(":")) {
                result = result.replaceAll("►", "►<b>");
                result = result.replaceAll(":", "</b>:");
            } else {
                result = String.format("<b>%s</b>", input);
            }
        } catch (Exception e) {
            result = String.format("<b>%s</b>", input);
        }
        return result;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.whatsappradio:
                if (checked)
                    shareSelectedOption = 0;
                break;
            case R.id.emailradio:
                if (checked)
                    shareSelectedOption = 1;
                break;
        }
    }

    private void uploadFacility() {
        if (checkFacilityPresentOrNot(encounterAdultInitList.get(encounterAdultInitList.size() - 1), UuidDictionary.Facility)) {
            ObsDTO obsDTO = new ObsDTO();
            ObsDAO obsDAO = new ObsDAO();
            try {
                obsDTO.setConceptuuid(UuidDictionary.Facility);
                obsDTO.setEncounteruuid(encounterAdultInitList.get(encounterAdultInitList.size() - 1)); //latest visit encounter.
                obsDTO.setCreator(sessionManager.getCreatorID());

                if (mFacilitySelection.getVisibility() == View.VISIBLE) {
                    mFacilityValue = mFacilitySelection.getSelectedItemsAsString();
                } else {
                    mFacilityValue = mFacilityValue;
                }

                String finalFacility = "";
                if (!TextUtils.isEmpty(mFacilityValue) && !mFacilityValue.equalsIgnoreCase("")) {
                    finalFacility = mState + "," + mDistrict + "," + mFacilityValue;

                } else {
                    finalFacility = "-";
                }

                obsDTO.setValue(finalFacility);

                obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultInitList.get(encounterAdultInitList.size() - 1), UuidDictionary.Facility));

                obsDAO.updateObs(obsDTO);


            } catch (DAOException dao) {
                FirebaseCrashlytics.getInstance().recordException(dao);
            }

            EncounterDAO encounterDAO = new EncounterDAO();
            try {
                encounterDAO.updateEncounterSync("false", encounterAdultInitList.get(encounterAdultInitList.size() - 1));
                encounterDAO.updateEncounterModifiedDate(encounterAdultInitList.get(encounterAdultInitList.size() - 1));
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
            obsDTO.setEncounteruuid(encounterAdultInitList.get(encounterAdultInitList.size() - 1));

            if (mFacilitySelection.getVisibility() == View.VISIBLE) {
                mFacilityValue = mFacilitySelection.getSelectedItemsAsString();
            } else {
                mFacilityValue = mFacilityValue;
            }

            String finalFacility = "";
            if (!TextUtils.isEmpty(mFacilityValue) && !mFacilityValue.equalsIgnoreCase("")) {
                finalFacility = mState + "," + mDistrict + "," + mFacilityValue;

            } else {
                finalFacility = "";
            }
            obsDTO.setValue(finalFacility);
            obsDTO.setConceptuuid(UuidDictionary.Facility);
            obsDTOList.add(obsDTO);
            try {
                obsDAO.insertObsToDb(obsDTOList);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    /**
     * check need to update or insert.
     */
    boolean checkFacilityPresentOrNot(String encounterId, String conceptId) {
        boolean valueCount = false;
        Cursor cursorFacility = db.rawQuery("select uuid from tbl_obs where encounteruuid= ? and conceptuuid= ?", new String[]{encounterId, conceptId});
        try {
            if (cursorFacility.getCount() != 0) {
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

    private void mAlertCreateVisitConformation() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);

//                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(getResources().getString(R.string.create_visit_msg));
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                visitProcessStart();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);

    }

    private void visitProcessStart() {
        SimpleDateFormat followUpFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date todayDate = new Date();
        String today = followUpFormat.format(todayDate);
        Date followUpDate = new Date();
        Date currentDateFU = new Date();

        InteleHealthDatabaseHelper mDatabaseHelper = new InteleHealthDatabaseHelper(VisitSummaryActivity.this);
        SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getReadableDatabase();

        String[] cols = {"value"};
//        String visitFollowUpDate = warnFollowUp(sqLiteDatabase,cols);
//        if(!visitFollowUpDate.equalsIgnoreCase("") || !visitFollowUpDate.isEmpty())
//        {
//            try {
//                followUpDate = followUpFormat.parse(visitFollowUpDate);
//                currentDateFU = followUpFormat.parse(today);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if(followUpDate.compareTo(currentDateFU)>0 || followUpDate.compareTo(currentDateFU)==0)
//            {
//                MaterialAlertDialogBuilder followUpAlert = new MaterialAlertDialogBuilder(PatientDetailActivity.this);
//                followUpAlert.setMessage(getString(R.string.pending_follow_up) +  "\n" + getString(R.string.still_continue));
//                followUpAlert.setPositiveButton(getResources().getString(R.string.continue_button), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        newVisitStart(sqLiteDatabase,cols);
//                    }
//                });
//                followUpAlert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//                AlertDialog alertDialog = followUpAlert.create();
//                alertDialog.show();
//                IntelehealthApplication.setAlertDialogCustomTheme(PatientDetailActivity.this, alertDialog);
////                    Toast.makeText(PatientDetailActivity.this,"Follow Up Date greater.",Toast.LENGTH_LONG).show();
//            }
//        }
//        else
//        {
        newVisitStart(sqLiteDatabase, cols);

//        }

    }

    private void newVisitStart(SQLiteDatabase sqLiteDatabase, String[] cols) {
        String CREATOR_ID = sessionManager.getCreatorID();
        returning = false;
        sessionManager.setReturning(returning);

        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);

        String uuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(uuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value(privacy_value_selected);//privacy value added.

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
// for getuuid`````````````````````

        if (visitUuid != null && !visitUuid.isEmpty()) {
//            CardView histCardView = findViewById(R.id.cardView_history);
//            histCardView.setVisibility(View.GONE);
        } else {
            // no use here looping...
            visitUuidList = new ArrayList<>();
            String visitIDSelection = "patientuuid = ?";
            String[] visitIDArgs = {patientUuid};
            Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
            if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                do {
                    visitUuid = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                    visitUuidList.add(visitUuid); // All visits will be stored in this arraylist.
                } while (visitIDCursor.moveToNext());
            }
            if (visitIDCursor != null) {
                visitIDCursor.close();
            }
            //no use...for loop...
            for (String visituuid : visitUuidList) {
                Logger.logD(TAG, visituuid);
                EncounterDAO encounterDAO1 = new EncounterDAO();
                String encounterIDSelection = "visituuid = ?";
                String[] encounterIDArgs = {visituuid};

                Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO1.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            encounterVitals = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                        if (encounterDAO1.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            encounterAdultIntialsForNew = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                        if (encounterDAO1.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
//                            encounterVisitNote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                    } while (encounterCursor.moveToNext());
                }
                encounterCursor.close();
            }
            familyHistory(famHistView, patientUuid, encounterAdultIntialsForNew);
            pastMedicalHistory(famHistView, patientUuid, encounterAdultIntialsForNew);
            // pastVisits(patientUuid);
        }


        Cursor cursor = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for PMH (Past Medical History)
                new String[]{encounterAdultIntialsForNew, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB},
                null, null, null);

        if (cursor.moveToFirst()) {
            // rows present
            do {
                // so that null data is not appended
//                phistory = phistory + cursor.getString(0);

            }
            while (cursor.moveToNext());
            returning = true;
            sessionManager.setReturning(returning);
        }
        cursor.close();

        Cursor cursor1 = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for FH (Family History)
                new String[]{encounterAdultIntialsForNew, UuidDictionary.RHK_FAMILY_HISTORY_BLURB},
                null, null, null);
        if (cursor1.moveToFirst()) {
            // rows present
            do {
//                fhistory = fhistory + cursor1.getString(0);
            }
            while (cursor1.moveToNext());
            returning = true;
            sessionManager.setReturning(returning);
        }
        cursor1.close();

        // Will display data for patient as it is present in database
        // Toast.makeText(PatientDetailActivity.this,"PMH: "+phistory,Toast.LENGTH_SHORT).sƒhow();
        // Toast.makeText(PatientDetailActivity.this,"FH: "+fhistory,Toast.LENGTH_SHORT).show();

        Intent intent2 = new Intent(VisitSummaryActivity.this, ComplaintNodeActivity.class);
//        String fullName = patient_new.getFirst_name() + " " + patient_new.getLast_name();
        intent2.putExtra("patientUuid", patientUuid);

        VisitDTO visitDTO = new VisitDTO();

        visitDTO.setUuid(uuid);
        visitDTO.setPatientuuid(patientUuid);
        visitDTO.setStartdate(thisDate);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setLocationuuid(sessionManager.getLocationUuid());
        visitDTO.setSyncd(false);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());//static
        VisitsDAO visitsDAO = new VisitsDAO();

        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        // visitUuid = String.valueOf(visitLong);
//                localdb.close();
        intent2.putExtra("patientUuid", patientUuid);
        intent2.putExtra("visitUuid", uuid);
//        intent2.putExtra("encounterUuidVitals", encounterDTO.getUuid());
        intent2.putExtra("encounterUuidAdultIntial", "");
        intent2.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntialsForNew);
        intent2.putExtra("name", patientName);
        intent2.putExtra("tag", "new");
        intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
        startActivity(intent2);

    }

    public void familyHistory(TextView famHistView, String patientuuid,
                              String EncounterAdultInitials_LatestVisit) {
        //String visitSelection = "patientuuid = ? AND enddate IS NULL OR enddate = ''";
        String visitSelection = "patientuuid = ?";
        String[] visitArgs = {patientuuid};
        String[] visitColumns = {"uuid, startdate", "enddate"};
        String visitOrderBy = "startdate";
        Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

//        previousVisitsList = findViewById(R.id.linearLayout_previous_visits);
        if (visitCursor.getCount() < 1) {
//            neverSeen();
        } else {

            if (visitCursor.moveToLast() && visitCursor != null) {
                do {
                    EncounterDAO encounterDAO = new EncounterDAO();
                    String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("startdate"));
                    String end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                    String visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));

                    String encounterlocalAdultintial = "";
                    String encountervitalsLocal = null;
                    String encounterIDSelection = "visituuid = ?";

                    String[] encounterIDArgs = {visit_id};

                    Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                    if (encounterCursor != null && encounterCursor.moveToFirst()) {
                        do {
                            if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                            }
                            if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                            }

                        } while (encounterCursor.moveToNext());
                    }
                    if (encounterCursor != null) {
                        encounterCursor.close();
                    }
                    String famHistSelection = "encounteruuid = ? AND conceptuuid = ? And voided!='1'";
                    String[] famHistArgs = {EncounterAdultInitials_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
                    String[] famHistColumns = {"value", " conceptuuid"};
                    Cursor famHistCursor = db.query("tbl_obs", famHistColumns, famHistSelection, famHistArgs, null, null, null);
                    famHistCursor.moveToLast();
                    String famHistValue;

                    try {
                        famHistValue = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                    } catch (Exception e) {
                        famHistValue = "";
                    } finally {
                        famHistCursor.close();
                    }

                    if (famHistValue != null && !famHistValue.equals("")) {
                        famHistView.setText(Html.fromHtml(famHistValue));
                    } else {
                        famHistView.setText(getString(R.string.string_no_hist));
                    }

                } while (visitCursor.moveToPrevious());
            }
            visitCursor.close();
        }

    }

    public void pastMedicalHistory(TextView medHistView, String patientuuid,
                                   String EncounterAdultInitials_LatestVisit) {
        //String visitSelection = "patientuuid = ? AND enddate IS NULL OR enddate = ''";
        String visitSelection = "patientuuid = ?";
        String[] visitArgs = {patientuuid};
        String[] visitColumns = {"uuid, startdate", "enddate"};
        String visitOrderBy = "startdate";
        Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

//        previousVisitsList = findViewById(R.id.linearLayout_previous_visits);
        if (visitCursor.getCount() < 1) {
//            neverSeen();
        } else {

            if (visitCursor.moveToLast() && visitCursor != null) {
                do {
                    EncounterDAO encounterDAO = new EncounterDAO();
                    String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("startdate"));
                    String end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                    String visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));

                    String encounterlocalAdultintial = "";
                    String encountervitalsLocal = null;
                    String encounterIDSelection = "visituuid = ?";

                    String[] encounterIDArgs = {visit_id};

                    Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                    if (encounterCursor != null && encounterCursor.moveToFirst()) {
                        do {
                            if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                            }
                            if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                            }

                        } while (encounterCursor.moveToNext());
                    }
                    if (encounterCursor != null) {
                        encounterCursor.close();
                    }
                    String medHistSelection = "encounteruuid = ? AND conceptuuid = ? And voided!='1'";
                    String[] medHistArgs = {EncounterAdultInitials_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                    String[] medHistColumms = {"value", " conceptuuid"};
                    Cursor medHistCursor = db.query("tbl_obs", medHistColumms, medHistSelection, medHistArgs, null, null, null);
                    medHistCursor.moveToLast();

                    String medHistValue;

                    try {
                        medHistValue = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                    } catch (Exception e) {
                        medHistValue = "";
                    } finally {
                        medHistCursor.close();
                    }

                    ObsDTO obsDTO = new ObsDTO();
                    obsDTO.setValue(medHistValue);
                    medHistValue = obsDTO.getValue(sessionManager.getAppLanguage());
                    Log.v(TAG, medHistValue);
//                    if (medHistValue != null && !medHistValue.equals("")) {
////                        medHistView.setText(Html.fromHtml(medHistValue));
//                    } else {
//                        medHistView.setText(getString(R.string.string_no_hist));
//                    }
                } while (visitCursor.moveToPrevious());
            }
            visitCursor.close();
        }
    }

}
