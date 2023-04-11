package org.intelehealth.msfarogyabharat.activities.visitSummaryActivity;

import static org.intelehealth.msfarogyabharat.utilities.StringUtils.sortMedications;

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
import android.database.sqlite.SQLiteDatabaseLockedException;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.telephony.SmsManager;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.intelehealth.msfarogyabharat.models.SendCallData;
import org.intelehealth.msfarogyabharat.models.prescribed_medications_model.Datum;
import org.intelehealth.msfarogyabharat.models.prescribed_medications_model.PrescribedMedication;
import org.intelehealth.msfarogyabharat.networkApiCalls.ApiInterface;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitSummaryActivity extends AppCompatActivity {

    private static final String TAG = VisitSummaryActivity.class.getSimpleName();
    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mBloodGlucose, mTemp, mSPO2, mresp;
    String speciality_selected = "";

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
    ObsDTO bloodGlucose = new ObsDTO();
    ObsDTO bpSys = new ObsDTO();
    ObsDTO bpDias = new ObsDTO();
    ObsDTO temperature = new ObsDTO();
    ObsDTO spO2 = new ObsDTO();
    ObsDTO resp = new ObsDTO();

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String foodAllergyReturned = "";
    String doctorName = "";
    String additionalReturned = "";
    String followUpDate = "";

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;
    ImageButton editAddDocs;

    FrameLayout frameLayout_doctor;
    TextView nameView;
    TextView idView;
    TextView visitView;
    TextView heightView;
    TextView weightView;
    TextView bloodGlucoseView;
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

    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";
    private String defaultAdvice;

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
    SharedPreferences mSharedPreference;
    boolean hasLicense = false;
    String mFileName = "config.json";
    public static String prescription1;
    public static String prescription2;
    SessionManager sessionManager, sessionManager1;
    String encounterUuid;
    String encounterVitals;
    //  Boolean isreturningWhatsapp = true;
    String encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;

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

    List<PrescribedMedication> prescribedMedicationArrayList = new ArrayList<>();
    String englishString = "";
    String hindiString = "";
    StringBuilder finalString;

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
        mCHWname.setText(sessionManager.getChwname()); //session manager provider
        //Added Prescription Title from config.Json dynamically through sharedPreferences
        prescriptionHeader1 = sharedPreferences.getString("prescriptionTitle1", "");
        prescriptionHeader2 = sharedPreferences.getString("prescriptionTitle2", "");

        if (isPastVisit) menuItem.setVisible(false);
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
            case R.id.summary_home: {
//                NavUtils.navigateUpFromSameTask(this);
                Intent i = new Intent(this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
//            case R.id.summary_print: {
//                try {
//                    doWebViewPrint_Button();
//                } catch (ParseException e) {
//                    FirebaseCrashlytics.getInstance().recordException(e);
//                }
//                return true;
//            }
            case R.id.summary_sms: {
                //     VisitSummaryActivityPermissionsDispatcher.sendSMSWithCheck(this);
                return true;
            }
            case R.id.summary_endVisit: {
                //meera
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
        sessionManager1 = new SessionManager(this);
//        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
//        if (!language.equalsIgnoreCase("")) {
//            Locale locale = new Locale(language);
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//        }
        appLanguage = sessionManager1.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
//        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        defaultAdvice = "Limit oil intake to 3 teaspoon per day that is 500ml per person per month./तेल का सेवन प्रतिदिनी ३ चम्मच, अर्थात* ५०० मिलि प्रति व्यक्ति प्रति माह तक सीमित करें\n" + "Do not consume simple sugar in the diet. Avoid honey/ jiggery. Instead can use artificial sweeteners like stevia./आहार में साधारण चीनी का सेवन न करें। शहद/गुड़ से परहेज करें। इसके बजाय स्टीविया जैसे कृत्रिम मिठास का उपयोग कर सकते हैं।\n" + "Avoid hydrogenated oil, Dalda, and animal fat. Consume sunflower oil/ Corn oil/ ground nut oil./हाइड्रोजनीकृत तेल, डालडा और पशु वसा से बचें। सूरजमुखी तेल/मकई का तेल/मूंगफली के तेल का सेवन करें।\n" + "Avoid large gaps between the meals. Eat small and frequent meals./छोटे-छोटे अंतरालों में थोडी-थोडी मात्रा में आहार लें\n" + "Limit salt intake to 4gram per day./नमक का सेवन प्रतिदिन 4 ग्राम तक सीमित करें।\n" + "Avoid processed and refined food products like maida and maida products, toast, khaari, biscuits, white breads. Avoid Namkeens, bakery items./मैदा और मैदा उत्पाद, टोस्ट, खारी, बिस्कुट, सफेद ब्रेड जैसे प्रसंस्कृत और परिष्कृत खाद्य उत्पादों से बचें। नमकीन, बेकरी आइटम से बचें।\n" + "Hypoglycaemia most often occurs due to reasons such as inadequate meal intake, too much exercise without proper carbohydrate intake. This can lead to blurred vision, confused thinking, giddiness,sweating, restlnessness, drowsiness, slurred speech./हाइपोग्लाइसीमिया अक्सर अपर्याप्त भोजन सेवन, उचित कार्बोहाइड्रेट सेवन के बिना बहुत अधिक व्यायाम जैसे कारणों से होता है। इससे धुंधली दृष्टि, भ्रमित सोच, चक्कर आना, पसीना आना, बेचैनी, उनींदापन, गंदी बोली हो सकती है |\n" + "Treatment- Have 15 grams of carbohydrate and check it after 15 minutes. If it’s still below 70 mg/dL, have another dose of 15g carbohydrate. Carbohydrate sources to include- Sugar or glucon- 1Tbsp (15g), 150-200ml of fruit juice./उपचार- 15 ग्राम कार्बोहाइड्रेट लें और 15 मिनट बाद चेक करें। यदि यह अभी भी 70 मिलीग्राम / डीएल से नीचे है, तो 15 ग्राम कार्बोहाइड्रेट की एक और खुराक लें। शामिल करने के लिए कार्बोहाइड्रेट स्रोत- चीनी या ग्लूकॉन- 1 बड़ा चम्मच (15 ग्राम), 150-200 मिलीलीटर फलों का रस|";

        final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterUuidAdultIntial = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            mSharedPreference = this.getSharedPreferences("visit_summary", Context.MODE_PRIVATE);
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
        }
        registerBroadcastReceiverDynamically();
        registerDownloadPrescription();
        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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
        frameLayout_doctor = findViewById(R.id.frame_doctor);
        frameLayout_doctor.setVisibility(View.GONE);

        card_print = findViewById(R.id.card_print);
        card_share = findViewById(R.id.card_share);

        card_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (objClsDoctorDetails != null) {
                        doWebViewPrint_Button();
                    } else {
                        Toast.makeText(VisitSummaryActivity.this, getResources().getString(R.string.prescription_not_found), Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        card_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasPrescription.equalsIgnoreCase("true")) {


                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                    EditText editText = new EditText(VisitSummaryActivity.this);
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    InputFilter inputFilter = new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                            return null;
                        }
                    };

//                    String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
//                    String whatsapp_url = partial_whatsapp_presc_url.concat(visitUuid);

                    editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(10)});
                    editText.setText(patient.getPhone_number());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    editText.setLayoutParams(layoutParams);
                    alertDialog.setView(editText);
                    String htmlDoc = sms_prescription();

                    //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
                    alertDialog.setMessage(getResources().getString(R.string.enter_mobile_number_to_share_prescription));
                    alertDialog.setPositiveButton(getResources().getString(R.string.share), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (!editText.getText().toString().equalsIgnoreCase("")) {
                                String phoneNumber = "+91" + editText.getText().toString();

                                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)).putExtra("sms_body", Html.fromHtml(htmlDoc).toString());
//                                        if (intent1.resolveActivity(getPackageManager()) != null)  //Removed because the screen was not getting triggered otherwise: By Nishita
                                startActivity(intent1);

                            } else {
                                Toast.makeText(context, getResources().getString(R.string.please_enter_mobile_number), Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                    AlertDialog dialog = alertDialog.show();
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                    alertDialog.setMessage(getResources().getString(R.string.download_prescription_first_before_sharing));
                    alertDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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

        Executors.newSingleThreadExecutor().execute(() -> {
            //if row is present i.e. if true is returned by the function then the spinner will be disabled.
            Log.d("visitUUID", "onCreate_uuid: " + visitUuid);
            isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
            runOnUiThread(() -> {
                if (isVisitSpecialityExists) speciality_spinner.setEnabled(false);

                //spinner is being populated with the speciality values...
//        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();
                VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();


//        List<String> items = providerAttributeLIstDAO.getAllValues();
//        if (items != null) //According to ticket MHM-124,129 the three specialities are no longer required and need to be removed: By Nishita
//        {
//            if (items.contains("MSF MD")) {
//                items.remove(items.indexOf("MSF MD"));
//            }
//            if (items.contains("Doctor not needed")) {
//                items.remove(items.indexOf("Doctor not needed"));
//            }
//            if (items.contains("All")) {
//                items.remove(items.indexOf("All"));
//            }
//        }
//        Log.d("specc", "spec: " + visitUuid);
                String special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid);
                //Hashmap to List<String> add all value
                ArrayAdapter<String> stringArrayAdapter;

//        if (items != null) {
//            items.add(0, "Select Specialization");
//            stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
//            speciality_spinner.setAdapter(stringArrayAdapter);
//        } else {
                stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.speciality_values));
                speciality_spinner.setAdapter(stringArrayAdapter);
//        }


                if (special_value != null) {
                    int spinner_position = stringArrayAdapter.getPosition(special_value);
                    speciality_spinner.setSelection(spinner_position);
                } else {

                }

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
            });
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

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        additionalImageDownloadText.setPaintFlags(p.getColor());
        additionalImageDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);


        additionalDocumentImagesDownload();

        //image download for physcialExamination documents
        physcialExaminationDownloadText.setPaintFlags(p.getColor());
        physcialExaminationDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        physcialExaminationImagesDownload();


        downloadButton.setEnabled(false);
        downloadButton.setVisibility(View.GONE);
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
                            Executors.newSingleThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        EncounterDAO encounterDAO = new EncounterDAO();
                                        encounterDAO.setEmergency(visitUuid, true);
                                    } catch (DAOException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                }
                            });

                            dialogInterface.dismiss(); //close the dialog
                            //    flag.setChecked(true); //check the dialog here...
                        }
                    });
                    alertdialogBuilder.setNegativeButton(getResources().getString(R.string.generic_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Executors.newSingleThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        EncounterDAO encounterDAO = new EncounterDAO();
                                        encounterDAO.setEmergency(visitUuid, false);
                                    } catch (DAOException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }

                                }
                            });

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
                isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
                if (speciality_spinner.getSelectedItemPosition() != 0) {
                    VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                    boolean isUpdateVisitDone = false;
                    try {

                        if (!isVisitSpecialityExists) {
                            isUpdateVisitDone = speciality_attributes.insertVisitAttributes(visitUuid, speciality_selected);
                        }
                        Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                    } catch (DAOException e) {
                        e.printStackTrace();
                        Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                    }

                    if (isVisitSpecialityExists) speciality_spinner.setEnabled(false);

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
                        String visitIDSelection = "uuid = ?";
                        String[] visitIDArgs = {visitUuid};
                        final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
                        if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                            visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                        }
                        if (visitIDCursor != null) visitIDCursor.close();
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
                                    AppConstants.notificationUtils.DownloadDone(patientName + " " + getResources().getString(R.string.visit_data_failed), getResources().getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity.this);

                                }
                                uploaded = true;

                            }
                        }, 4000);
                    } else {
                        AppConstants.notificationUtils.DownloadDone(patientName + " " + getResources().getString(R.string.visit_data_failed), getResources().getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity.this);
                    }
                    showCallOverDialog();
                } else {
                    TextView t = (TextView) speciality_spinner.getSelectedView();
                    t.setError(getResources().getString(R.string.please_select_specialization));
                    t.setTextColor(Color.RED);

                    AlertDialog.Builder builder = new AlertDialog.Builder(VisitSummaryActivity.this).setMessage(getResources().getString(R.string.please_select_specialization)).setCancelable(false).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
        visitView = findViewById(R.id.textView_visit_value);
        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            idView.setText(patient.getOpenmrs_id());
        } else {
            idView.setText(getResources().getString(R.string.patient_not_registered));
        }

        nameView.setText(patientName);

        heightView = findViewById(R.id.textView_height_value);
        weightView = findViewById(R.id.textView_weight_value);
        bloodGlucoseView = findViewById(R.id.textView_blood_glucose_value);
        bpView = findViewById(R.id.textView_bp_value);
        tempView = findViewById(R.id.textView_temp_value);

        tempfaren = findViewById(R.id.textView_temp_faren);
        tempcel = findViewById(R.id.textView_temp);
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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
                String heightVal = ConvertHeightIntoFeets(height.getValue());
                heightView.setText(heightVal);
            }
        }

        weightView.setText(weight.getValue());
        bloodGlucoseView.setText(bloodGlucose.getValue());

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
            complaintView.setText(Html.fromHtml(complaint.getValue()));
        if (famHistory.getValue() != null)
            famHistView.setText(Html.fromHtml(famHistory.getValue()));
        if (patHistory.getValue() != null)
            patHistView.setText(Html.fromHtml(patHistory.getValue()));
        if (phyExam.getValue() != null) physFindingsView.setText(Html.fromHtml(phyExam.getValue()));

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
                        else dialogEditText.setText("");
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
                    complaintText.setText(Html.fromHtml(complaint.getValue()));
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
                                if (complaint.getValue() != null) {
                                    complaintText.setText(Html.fromHtml(complaint.getValue()));
                                    complaintView.setText(Html.fromHtml(complaint.getValue()));
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
                        else dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(getResources().getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                phyExam.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
                                if (phyExam.getValue() != null) {
                                    physicalText.setText(Html.fromHtml(phyExam.getValue()));
                                    physFindingsView.setText(Html.fromHtml(phyExam.getValue()));
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
                        else dialogEditText.setText("");
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
                addDocs.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                startActivity(addDocs);
            }
        });

        downloadButton.setOnClickListener(view -> {

            if (NetworkConnection.isOnline(getApplication())) {
                Toast.makeText(context1, getResources().getString(R.string.downloading), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context1, getResources().getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
            }

            SyncUtils syncUtils = new SyncUtils();
            syncUtils.syncForeground("downloadPrescription");
            uploaded = true;
            prescribedMedicationArrayList.clear();

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                downloadPrescription();
//                        pd.dismiss();
            }, 5000);
        });

        onExaminationDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_PE);
            }
        });

        doQuery();

        EncounterDAO encounterDAO = new EncounterDAO();
        String emergencyUuid = "";
        try {
            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //visit is uploaded to server checking in this case the checkbox will be disabled...
        if (!isSynedFlag.equalsIgnoreCase("0")) {
            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
                //i.e the visit is a priority visit since getEmergencyEncounters() checks for voided = 0 i.e. priority...
                flag.setChecked(true);
                flag.setEnabled(false);
            } else {
                flag.setChecked(false);
                flag.setEnabled(false);
            }
        } else {
            //to set the checkbox as checked in offline mode so that i can be modified later...
            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
                //i.e the visit is a priority visit since getEmergencyEncounters() checks for voided = 0 i.e. priority...
                flag.setChecked(true);
            } else {
                flag.setChecked(false);
            }
        }
    }

    private void showCallOverDialog() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.call_over);
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SimpleDateFormat startFormat = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm", Locale.ENGLISH);
                Calendar today = Calendar.getInstance();
                today.add(Calendar.MINUTE, -1);
                today.set(Calendar.MILLISECOND, 0);
                Date todayDate1 = today.getTime();
                String callEndTime = startFormat.format(todayDate1);
                sendCallData(callEndTime);
            }
        });
        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void sendCallData(String callEndTime) {
        SendCallData model = new SendCallData();
        Cursor searchCursor = db.rawQuery("SELECT * FROM tbl_ivr_call_details LIMIT 1", null);
        if (searchCursor.moveToFirst()) {
            do {
                model.setState(searchCursor.getString(searchCursor.getColumnIndexOrThrow("state")));
                model.setDistrict(searchCursor.getString(searchCursor.getColumnIndexOrThrow("district")));
                model.setFacility(searchCursor.getString(searchCursor.getColumnIndexOrThrow("facilityName")));
                model.setCallDate(searchCursor.getString(searchCursor.getColumnIndexOrThrow("dateOfCalls")));
                model.setCallStatus(searchCursor.getString(searchCursor.getColumnIndexOrThrow("status")));
                model.setCallAction(searchCursor.getString(searchCursor.getColumnIndexOrThrow("actionIfCompleted")));
                model.setCallNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("callNumber")));
                model.setRemarks(searchCursor.getString(searchCursor.getColumnIndexOrThrow("remarks")));
                model.setCallStartTime(searchCursor.getString(searchCursor.getColumnIndexOrThrow("callStartTime")));
                model.setCallEndTime(callEndTime);
            } while (searchCursor.moveToNext());
        }

        searchCursor.close();
        UrlModifiers urlModifiers = new UrlModifiers();
        ApiInterface apiInterface = AppConstants.apiInterface;
        String sendDataUrl = urlModifiers.sendCallData();
        apiInterface.callPatientData(sendDataUrl, model).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(VisitSummaryActivity.this, "Information stored successfully!", Toast.LENGTH_SHORT).show();
                System.out.println(call);
                System.out.println(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String applyBoldTag(String input) {
        String result = input;
        if (input == null) return null;
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

    private String sms_prescription() {
        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + patient.getLast_name();
        String mPatientOpenMRSID = patient.getOpenmrs_id();
        String mPatientDob = patient.getDate_of_birth();
        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") + ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
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
        mBloodGlucose = bloodGlucose.getValue();
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, VisitSummaryActivity.this), String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity.this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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
        String mComplaint = complaint.getValue();

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

      //  String rx_web = stringToWeb_sms(sortMedications(prescribedMedicationArrayList).trim().replace("\n\n", "\n").replace(Node.bullet, ""));
        String medicationPlan = sortMedications(prescribedMedicationArrayList).replace("\n\n", "\n");
        String rx_web = stringToWeb(medicationPlan.trim()).replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">●", "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">").replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\"></p>", "</p>");

        String tests_web = stringToWeb_sms(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

        String foodAllergy_web = "";
        if (foodAllergyReturned != null && !foodAllergyReturned.isEmpty())
            foodAllergy_web = stringToWeb_sms(foodAllergyReturned.trim().replace("\n\n", "\n")).replace(Node.bullet, "");

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
        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr =/* "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +*/

                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "18003094144" + "</span>" /*+*/

                   /* "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification()
                    + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +

                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +*/

            /*"</div>"*/;

        }

//        if (isRespiratory) {
        String htmlDocument = String.format("<b id=\"heading_1\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b><br>" + "<b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b>" + "<br><br>" +

                        /*"<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +*/
//                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                        /* doctorDetailStr +*/


                        "<b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</b><br>" + "<b id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </b>" + "<br><br>" +

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

                        "<b id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis <br>" + "%s </b><br>" + "<b id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan <br>" + "%s </b><br>" + "<b id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s) <br>" + "%s " + "</b><br>" + "<b id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Advice <br>" + "%s" + "</b><br>" + "<b id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date <br>" + "%s" + "</b><br>" +
                        /* "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +*/

                        /* "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +*/

                        doctorDetailStr
                /*"<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" +*/

                /*  doctrRegistartionNum + "</p>" +*/

                /*"</div>"*/, heading, heading2, /*heading3,*/ mPatientName, age, mGender, /*mSdw*/
//                            address, mPatientOpenMRSID, mDate,

                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/

//                            (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "",
//                            (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",

                /*pat_hist, fam_hist,*/ /*mComplaint,*/
                !diagnosis_web.isEmpty() ? diagnosis_web : stringToWeb_sms("Not Provided"),
                !rx_web.isEmpty() ? rx_web : stringToWeb_sms("Not Provided"),
                !tests_web.isEmpty() ? tests_web : stringToWeb_sms("Not Provided"),
                !advice_web.isEmpty() ? advice_web : stringToWeb_sms("Not Provided"),
                !followUp_web.isEmpty() ? followUp_web : stringToWeb_sms("Not Provided"),
                !doctor_web.isEmpty() ? doctor_web : stringToWeb_sms("Not Provided"));

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
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=?", new String[]{uuid});

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                isExists = true;
            }
        }
        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
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

            String query = "SELECT   a.uuid, a.sync " + "FROM tbl_visit a " + "WHERE a.uuid = '" + visitUUID + "'";

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

    private void physcialExaminationImagesDownload() {
        runOnUiThread(() -> {
            ImagesDAO imagesDAO = new ImagesDAO();
            try {
                List<String> imageList = imagesDAO.isImageListObsExists(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                for (String images : imageList) {
                    if (imagesDAO.isLocalImageUuidExists(images))
                        physcialExaminationDownloadText.setVisibility(View.GONE);
                    else physcialExaminationDownloadText.setVisibility(View.VISIBLE);
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }
            physcialExaminationDownloadText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDownload(UuidDictionary.COMPLEX_IMAGE_PE);
                    physcialExaminationDownloadText.setVisibility(View.GONE);
                }
            });
        });
    }

    private void additionalDocumentImagesDownload() {
        runOnUiThread(() -> {
            ImagesDAO imagesDAO = new ImagesDAO();
            try {
                List<String> imageList = imagesDAO.isImageListObsExists(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD);
                for (String images : imageList) {
                    if (imagesDAO.isLocalImageUuidExists(images))
                        additionalImageDownloadText.setVisibility(View.GONE);
                    else additionalImageDownloadText.setVisibility(View.VISIBLE);
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }

            additionalImageDownloadText.setOnClickListener(v -> {
                startDownload(UuidDictionary.COMPLEX_IMAGE_AD);
                additionalImageDownloadText.setVisibility(View.GONE);
            });
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

    private void startDownload(String imageType) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
        intent.putExtra("ImageType", imageType);
        startService(intent);
    }

    private String stringToWeb(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</p>";
            formatted = para_open + Node.big_bullet + input.replaceAll("\n", para_close + para_open + Node.big_bullet) + para_close;
        }
        return formatted;
    }

    private String stringToWeb_sms(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<b style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</b><br>";
            formatted = para_open + "- " + input.replaceAll("\n", para_close + para_open + "- ") + para_close;
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
        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") + ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
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
        mBloodGlucose = bloodGlucose.getValue();
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = sdf.parse(mPatientDob);

        if (date != null) dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        String medicationPlan = sortMedications(prescribedMedicationArrayList).replace("\n\n", "\n");

        String rx_web = stringToWeb(medicationPlan).replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">●", "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">").replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\"></p>", "</p>");

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

        String foodAllergy_web = stringToWeb(foodAllergyReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

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
        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
                    "</div>";

        }
        if (isRespiratory) {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                            /* doctorDetailStr +*/
                            "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Blood Glucose (ml/dl): %s | %s | Respiratory Rate: %s |  %s </p><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<b><p id=\"food_allergy_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Food Allergy</p></b>" + para_open + "%s" + para_close

                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/ + "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" + doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/ mComplaint,
                    (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                    (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mBloodGlucose)) ? mBloodGlucose : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : ""
                    /*pat_hist, fam_hist,*/, diagnosis_web, foodAllergy_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Blood Glucose: %s | %s | %s </p><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "<b><p id=\"food_allergy_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Food Allergy</p></b>" + para_open + "%s" + para_close + "<br><br>" +

                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/
                    mComplaint,
                    (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                    (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mBloodGlucose)) ? mBloodGlucose : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                    /*pat_hist, fam_hist,*/ diagnosis_web, foodAllergy_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
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
        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") + ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
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
        mBloodGlucose = bloodGlucose.getValue();
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
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
        String mComplaint = complaint.getValue();

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

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

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
        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();


            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
                    "</div>";

        }
        if (isRespiratory) {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                            /* doctorDetailStr +*/
                            "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Blood Glucose: %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" + doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/ (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mBloodGlucose)) ? mBloodGlucose : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                    /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Blood Pressure: %s | Blood Glucose: %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p>" + para_open + "%s" + para_close + "<br><br>" + "<b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b>" + "%s<br>" + "<b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b>" + "%s<br>" + "<b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b>" + "%s<br>" + "<b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b>" + "%s<br>" + "<b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "",*/
                    (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mBloodGlucose)) ? mBloodGlucose : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
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
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

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
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

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
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

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
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

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
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
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
                    pdfPrint.print(printAdapter, dir, fileName, new PdfPrint.CallbackPrint() {
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
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
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
                    pdfPrint.print(printAdapter, dir, fileName, new PdfPrint.CallbackPrint() {
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
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
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
                    pdfPrint.print(printAdapter, dir, fileName, new PdfPrint.CallbackPrint() {
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
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //end...

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
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
                    pdfPrint.print(printAdapter, dir, fileName, new PdfPrint.CallbackPrint() {
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
        String[] columnsToReturn = {"openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "country", "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
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
            case UuidDictionary.BLOOD_GLUCOSE: //Blood Glucose
            {
                bloodGlucose.setValue(value);
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
                Log.v("gson", "gson value: " + value);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                try {
                    PrescribedMedication medication = gson.fromJson(value, PrescribedMedication.class);
                    prescribedMedicationArrayList.add(medication);

                    finalString = new StringBuilder();

                    for (int i = 0; i < prescribedMedicationArrayList.size(); i++) {
                        String engMealTime = prescribedMedicationArrayList.get(i).getEn().getMealType();
                        List<Datum> engDatumList = prescribedMedicationArrayList.get(i).getEn().getData();
                        for (Datum datum : engDatumList) {
                            englishString = datum.getValue();
                        }
                        int serial = i + 1;
                        finalString = finalString.append(serial).append(". ").append(engMealTime).append("\n-").append(englishString).append("\n\n");
                    }

                    finalString = finalString.append("\n");

                    for (int i = 0; i < prescribedMedicationArrayList.size(); i++) {
                        String hindiMealTime = prescribedMedicationArrayList.get(i).getHi().getMealType();
                        List<Datum> hiDatumList = prescribedMedicationArrayList.get(i).getHi().getData();
                        for (Datum datum : hiDatumList) {
                            hindiString = datum.getValue();
                        }

                        int serial = i + 1;
                        finalString = finalString.append(serial).append(". ").append(hindiMealTime).append("\n-").append(hindiString).append("\n\n");
                    }

//                if (!rxReturned.trim().isEmpty()) {
//                    rxReturned = rxReturned + "\n" + value;
//                } else {
//                    rxReturned = value;
//                }
//                Log.i(TAG, "parseData: rxfin" + rxReturned);
                    if (prescriptionCard.getVisibility() != View.VISIBLE) {
                        prescriptionCard.setVisibility(View.VISIBLE);
                    }
                    prescriptionTextView.setText(sortMedications(prescribedMedicationArrayList));
                    //checkForDoctor();
                }
                catch (Exception e) {
                    if (prescriptionCard.getVisibility() != View.VISIBLE) {
                        prescriptionCard.setVisibility(View.VISIBLE);
                    }
                    prescriptionTextView.setText(value);
                }
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
                medicalAdvice_string = medicalAdvice_string.concat("\n").concat(defaultAdvice);
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
            case UuidDictionary.FOOD_ALLERGY: {
                if (!foodAllergyReturned.isEmpty()) {
                    foodAllergyReturned = foodAllergyReturned + "\n" + Node.bullet + " " + value;
                } else {
                    foodAllergyReturned = Node.bullet + " " + value;
                }
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
            Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {

            frameLayout_doctor.setVisibility(View.VISIBLE);

            doctorSign = objClsDoctorDetails.getTextOfSign();

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" + "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getName()) ? objClsDoctorDetails.getName() : "") + "</span><br>" + "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" + "  " + (!TextUtils.isEmpty(objClsDoctorDetails.getQualification()) ? objClsDoctorDetails.getQualification() : "") + ", " + (!TextUtils.isEmpty(objClsDoctorDetails.getSpecialization()) ? objClsDoctorDetails.getSpecialization() : "") + "</span><br>" +
                    // "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " + objClsDoctorDetails.getRegistrationNumber() : "") +
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

    private void updateDatabase(String string, String conceptID) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(String.valueOf(conceptID));
            obsDTO.setEncounteruuid(encounterUuidAdultIntial);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterUuidAdultIntial, String.valueOf(conceptID)));

            obsDAO.updateObs(obsDTO);


        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterUuidAdultIntial);
            encounterDAO.updateEncounterModifiedDate(encounterUuidAdultIntial);
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
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD);
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mAdditionalDocsLayoutManager = new LinearLayoutManager(VisitSummaryActivity.this, LinearLayoutManager.HORIZONTAL, false);
            mAdditionalDocsRecyclerView.setLayoutManager(mAdditionalDocsLayoutManager);
            mAdditionalDocsRecyclerView.setAdapter(horizontalAdapter);
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

                header = getString(R.string.patient_id_) + patient.getOpenmrs_id() + "\n" + getString(R.string.patient_name_title) + patient.getFirst_name() + " " + patient.getLast_name() + "\n" + getString(R.string.patient_DOB) + patient.getDate_of_birth() + "\n";


                if (diagnosisCard.getVisibility() == View.VISIBLE) {
                    if (!diagnosisTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_diagnosis) + ":" + diagnosisTextView.getText().toString() + "\n";
                }
                if (prescriptionCard.getVisibility() == View.VISIBLE) {
                    if (!prescriptionTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_rx) + ":" + prescriptionTextView.getText().toString() + "\n";
                }
                if (medicalAdviceCard.getVisibility() == View.VISIBLE) {
                    if (!medicalAdviceTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_advice) + ":" + medicalAdviceTextView.getText().toString() + "\n";
                }
                if (requestedTestsCard.getVisibility() == View.VISIBLE) {
                    if (!requestedTestsTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_tests_prescribed) + ":" + requestedTestsTextView.getText().toString() + "\n";
                }

                if (followUpDateCard.getVisibility() == View.VISIBLE) {
                    if (!followUpDateTextView.getText().toString().trim().isEmpty())
                        body = body + getResources().getString(R.string.visit_summary_follow_up_date) + ":" + followUpDateTextView.getText().toString() + "\n";
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

                            Toast.makeText(getApplicationContext(), getString(R.string.sms_success), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_sms), Toast.LENGTH_LONG).show();
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
        Executors.newSingleThreadExecutor().execute(() -> {
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
                        runOnUiThread(() -> {
                            diagnosisReturned = "";
                            diagnosisTextView.setText("");
                            diagnosisCard.setVisibility(View.GONE);
                        });
                    }

                    if (!rxReturned.isEmpty()) {
                        runOnUiThread(() -> {
                            rxReturned = "";
                            prescriptionTextView.setText("");
                            prescriptionCard.setVisibility(View.GONE);
                        });
                    }

                    if (!adviceReturned.isEmpty()) {
                        runOnUiThread(() -> {
                            adviceReturned = "";
                            medicalAdviceTextView.setText("");
                            medicalAdviceCard.setVisibility(View.GONE);
                        });
                    }

                    if (!testsReturned.isEmpty()) {
                        runOnUiThread(() -> {
                            testsReturned = "";
                            requestedTestsTextView.setText("");
                            requestedTestsCard.setVisibility(View.GONE);
                        });
                    }

                    if (!followUpDate.isEmpty()) {
                        runOnUiThread(() -> {
                            followUpDate = "";
                            followUpDateTextView.setText("");
                            followUpDateCard.setVisibility(View.GONE);
                        });
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
                            runOnUiThread(() -> {
                                parseData(dbConceptID, dbValue);
                            });
                        } while (visitCursor.moveToNext());
                    }
                    visitCursor.close();

                    //checks if prescription is downloaded and if so then sets the icon color.
                    if (hasPrescription.equalsIgnoreCase("true")) {
                        runOnUiThread(() -> ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green)));
                    }

                    if (uploaded) {
                        try {
                            downloaded = visitsDAO.isUpdatedDownloadColumn(visitUuid, true);
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                    downloadDoctorDetails();
                }

                additionalDocumentImagesDownload();
                physcialExaminationImagesDownload();

            } catch (DAOException e) {
                e.printStackTrace();
            }
        });
    }

    public void downloadPrescriptionDefault() {
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
            downloadDoctorDetails();
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
                runOnUiThread(() -> parseDoctorDetails(dbValue));
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    public String ConvertHeightIntoFeets(String height) {
        int val = Integer.parseInt(height);
        double centemeters = val / 2.54;
        int inche = (int) centemeters % 12;
        int feet = (int) centemeters / 12;
        String heightVal = feet + getString(R.string.table_height_feet) + " " + inche + getString(R.string.table_height_inche);
        System.out.println("value of height=" + val);
        return heightVal;
    }
}