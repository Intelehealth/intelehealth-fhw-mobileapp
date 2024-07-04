package org.intelehealth.app.activities.visitSummaryActivity;

import static org.intelehealth.app.activities.identificationActivity.IdentificationActivity.checkAndRemoveEndDash;
import static org.intelehealth.app.database.dao.EncounterDAO.getEncounterListByVisitUUID;
import static org.intelehealth.app.utilities.DateAndTimeUtils.formatDateFromOnetoAnother;
import static org.intelehealth.app.utilities.EditTextUtils.emojiFilter;
import static org.intelehealth.app.utilities.StringUtils.en_ar_dob;
import static org.intelehealth.app.utilities.StringUtils.getLocaleGender;
import static org.intelehealth.app.utilities.StringUtils.switch_en_to_ar_village_edit;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_ROLE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_TEST_COLLECT;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_TEST_RECEIVE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.CompoundButton;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentsActivity;
import org.intelehealth.app.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.app.activities.familyHistoryActivity.FamilyHistoryActivity;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.householdSurvey.model.AnswerValue;
import org.intelehealth.app.activities.medicationAidActivity.Medication_Aid_Activity;
import org.intelehealth.app.activities.medicationAidActivity.PastNotesDispenseAdministerActivity;
import org.intelehealth.app.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.app.activities.prescription.PrescriptionActivity;
import org.intelehealth.app.activities.textprintactivity.TextPrintESCActivity;
import org.intelehealth.app.activities.vitalActivity.VitalsActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.ScheduleListingActivity;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentDetailsResponse;
import org.intelehealth.app.appointment.model.CancelRequest;
import org.intelehealth.app.appointment.model.CancelResponse;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.ProviderAttributeLIstDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.auth.ResponseChecker;
import org.intelehealth.app.models.dispenseAdministerModel.AidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationModel;
import org.intelehealth.app.models.dispenseAdministerModel.PastNotesModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.models.prescriptionUpload.EncounterProvider;
import org.intelehealth.app.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.app.models.prescriptionUpload.EndVisitResponseBody;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.services.DownloadService;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.BaseActivity;
import org.intelehealth.app.webrtc.activity.SilaChatActivity;
import org.intelehealth.klivekit.model.RtcArgs;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitSummaryActivity extends BaseActivity implements View.OnClickListener /*implements PrinterObserver*/ {

    private static final String TAG = VisitSummaryActivity.class.getSimpleName();
    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    String speciality_selected = "";
    String second_speciality_selected = "";

    boolean uploaded = false;
    boolean downloaded = false;

    Context context;

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String patientAge;
    String patientGender;
    String intentTag;
    String visitUUID;
    String medicalAdvice_string = "";
    String medicalAdvice_HyperLink = "";
    String isSynedFlag = "";
    private float float_ageYear_Month;

    Spinner speciality_spinner;
    Spinner second_speciality_spinner;

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

    String newDiagnosisReturned = "";
    String diagnosisReturned = "";
    String newRxReturned = "";
    String rxReturned = "";
    String newTestsReturned = "";
    String testsReturned = "";
    String newAdviceReturned = "";
    String adviceReturned = "";
    String newMedicalEquipLoanAidOrder = "";
    String newFreeMedicalEquipAidOrder = "";
    String newCoverMedicalExpenseAidOrder = "";
    String newCoverSurgicalExpenseAidOrder = "";
    String newCashAssistanceExpenseAidOrder = "";
    String newMedicalEquipLoanAidOrderPresc = "";
    String newFreeMedicalEquipAidOrderPresc = "";
    String newCoverMedicalExpenseAidOrderPresc = "";
    String newCoverSurgicalExpenseAidOrderPresc = "";
    String newCashAssistanceExpenseAidOrderPresc = "";
    String aidOrderReturned = "";
    String doctorName = "";
    String newAdditionalReturned = "";
    String additionalReturned = "";
    String newDischargeOrderReturned = "";
    String dischargeOrderReturned = "";
    String newFollowUpDate = "";
    String followUpDate = "";

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;
    ImageButton editAddDocs;

    FrameLayout frameLayout_doctor, fl_DispenseAdminister;
    View layout_dispense_1, layout_dispense_2, layout_test;
    TextView nameView;
    TextView ageView;
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
    TextView respiratory;
    TextView respiratoryText;
    TextView tempfaren;
    TextView tempcel;
    String medHistory;
    String baseDir;
    String filePathPhyExam;
    File obsImgdir;
    TextView presc_status;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    NestedScrollView nscrollview;

    RelativeLayout uploadButton, rl_med_aid;
    private TextView tvDispense_1, tvDispense_2, tvAdminister_1, tvAdminister_2, tvCollectedBy, tvReceivedBy;
    public static final String MEDICATION = "medication", AID = "aid", COLLECTED = "collected", RECEIVED = "received",
            DISPENSE = "dispense", ADMINISTER = "administer", ADDITIONAL_REMARKS = "additional_remarks";

    RelativeLayout downloadButton;
    ArrayList<String> physicalExams;

    CardView diagnosisCard;
    CardView prescriptionCard;
    CardView medicalAdviceCard;
    CardView requestedTestsCard;
    CardView additionalCommentsCard;
    CardView dischargeOrderCard;
    CardView aidOrderCard;
    CardView followUpDateCard;
    CardView card_print, card_share;
    CardView saveButton;


    TextView diagnosisTextView;
    TextView prescriptionTextView;
    TextView medicalAdviceTextView;
    TextView requestedTestsTextView;
    TextView additionalCommentsTextView;
    TextView dischargeOrderTextView;
    TextView aidOrderType1TextView, aidOrderType2TextView, aidOrderType3TextView, aidOrderType4TextView, aidOrderType5TextView;
    String aid1, aid2, aid3, aid4, aid5;
    View aidhl_1, aidhl_2, aidhl_3, aidhl_4;
    private List<MedicationAidModel> update_aidUuidList = new ArrayList<>();
    private List<MedicationAidModel> update_medUuidDispenseList = new ArrayList<>();
    private List<MedicationAidModel> update_medUuidAdministeredList = new ArrayList<>();
    private List<MedicationAidModel> update_collectedTest_UUIDList = new ArrayList<>();
    private List<MedicationAidModel> update_receivedTest_UUIDList = new ArrayList<>();
    private String encounterDispense, encounterAdminister, encounterTestCollect, encounterTestReceive;

    LinearLayout aidOrderType1TableRow, aidOrderType2TableRow, aidOrderType3TableRow,
            aidOrderType4TableRow, aidOrderType5TableRow, tl_prescribed_medications, ll_test;
    TextView followUpDateTextView;
    //added checkbox flag .m
    CheckBox flag;
    EndVisitEncounterPrescription endVisitEncounterPrescription;
    String visitnoteencounteruuid = "";
    Button btnSignSubmit, saveBtn;
    Base64Utils base64Utils = new Base64Utils();

    Boolean isPastVisit = false, isVisitSpecialityExists = false, isVisitSecondSpecialityExists = false;
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
    private String hasPrescription = "", hasPartialPrescription = "";
    private boolean isRespiratory = false;
    String appLanguage;
    private TextInputEditText tie_add_remarks;

   /* TextView tv_device_selected;
    Button btn_connect;
    private Object configObj;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private ProgressBar pb_connect;
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    private PrinterInterface curPrinterInterface = null;
    IntelehealthApplication application;*/

    private void reset() {
        newDiagnosisReturned = "";
        diagnosisReturned = "";
        newRxReturned = "";
        rxReturned = "";
        newTestsReturned = "";
        testsReturned = "";
        newAdviceReturned = "";
        adviceReturned = "";
        newMedicalEquipLoanAidOrder = "";
        newFreeMedicalEquipAidOrder = "";
        newCoverMedicalExpenseAidOrder = "";
        newCoverSurgicalExpenseAidOrder = "";
        newCashAssistanceExpenseAidOrder = "";
        newMedicalEquipLoanAidOrderPresc = "";
        newFreeMedicalEquipAidOrderPresc = "";
        newCoverMedicalExpenseAidOrderPresc = "";
        newCoverSurgicalExpenseAidOrderPresc = "";
        newCashAssistanceExpenseAidOrderPresc = "";
        aidOrderReturned = "";
        doctorName = "";
        newAdditionalReturned = "";
        additionalReturned = "";
        newDischargeOrderReturned = "";
        dischargeOrderReturned = "";
        newFollowUpDate = "";
        followUpDate = "";
    }

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
        ContextCompat.registerReceiver(this, broadcastReceiverForIamgeDownlaod, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    public void registerDownloadPrescription() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("downloadprescription");
        ContextCompat.registerReceiver(this, downloadPrescriptionService, filter, ContextCompat.RECEIVER_EXPORTED);
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
                Intent i = new Intent(this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
            case R.id.summary_sms: {
                //     VisitSummaryActivityPermissionsDispatcher.sendSMSWithCheck(this);
                return true;
            }
            case R.id.summary_endVisit: {
                onEndVisit();
                return true;
            }
            case R.id.summary_patient_detail: {
                onPatientDetailClicked();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onPatientDetailClicked() {
        String patientStatus = "returning";
        Intent patientDetailIntent = new Intent(context, PatientDetailActivity.class);
        patientDetailIntent.putExtra("patientUuid", patientUuid);
        patientDetailIntent.putExtra("patientName", patient.getFirst_name() + "" + patient.getLast_name());
        patientDetailIntent.putExtra("status", patientStatus);
        patientDetailIntent.putExtra("tag", "search");
        patientDetailIntent.putExtra("hasPrescription", "false");     // At the time of writing this statement, this hasPrescription has no significance as the prescription imageview is set as gone in patient detail activity
        startActivity(patientDetailIntent);
        finish();
    }

    private void onEndVisit() {
        // As discussed with Programs Team, from now on, we will simply show a dialog with the new message in all cases - visit not uploaded, prescription not given, prescription given, etc.
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);

        // the new message provided by Programs Team
        alertDialogBuilder.setMessage(getString(R.string.visit_data_loss_message));

        // Positive button option
        alertDialogBuilder.setPositiveButton(getString(R.string.generic_yes), (dialog, which) -> {
            dialog.dismiss();
            endVisit();
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            appointmentDAO.deleteAppointmentByVisitId(visitUuid);
        });

        // Negative button option
        alertDialogBuilder.setNegativeButton(getString(R.string.generic_no), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

    }

    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";

//    private void collectChatConnectionInfoFromFirebase() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance(AppConstants.getFirebaseRTDBUrl());
//        DatabaseReference chatDatabaseReference = database.getReference(AppConstants.getFirebaseRTDBRootRefForTextChatConnInfo() + "/" + visitUuid);
//        chatDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                HashMap value = (HashMap) snapshot.getValue();
//                if (value != null) {
//                    try {
//                        String fromUUId = String.valueOf(value.get("toUser"));
//                        String toUUId = String.valueOf(value.get("fromUser"));
//                        String patientUUid = String.valueOf(value.get("patientId"));
//                        String visitUUID = String.valueOf(value.get("visitId"));
//                        String patientName = String.valueOf(value.get("patientName"));
//                        JSONObject connectionInfoObject = new JSONObject();
//                        connectionInfoObject.put("fromUUID", fromUUId);
//                        connectionInfoObject.put("toUUID", toUUId);
//                        connectionInfoObject.put("patientUUID", patientUUid);
//
//                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//                        String packageName = pInfo.packageName;
//
//                        Intent intent = new Intent(ACTION_NAME);
//                        intent.putExtra("visit_uuid", visitUUID);
//                        intent.putExtra("connection_info", connectionInfoObject.toString());
//                        intent.setComponent(new ComponentName(packageName, "org.intelehealth.app.services.firebase_services.RTCMessageReceiver"));
//                        getApplicationContext().sendBroadcast(intent);
//
//                        Log.v(TAG, "collectChatConnectionInfoFromFirebase, onDataChange : " + connectionInfoObject.toString());
//                    } catch (JSONException | PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w(TAG, "collectChatConnectionInfoFromFirebase - Failed to read value.", error.toException());
//            }
//        });
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager1 = new SessionManager(VisitSummaryActivity.this);
        appLanguage = sessionManager1.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
        final Intent intent = this.getIntent(); // The intent was passed to the activity

        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientGender = intent.getStringExtra("gender");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterUuidAdultIntial = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            mSharedPreference = this.getSharedPreferences("visit_summary", Context.MODE_PRIVATE);
            patientName = intent.getStringExtra("name");
            patientAge = intent.getStringExtra("age");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            isPastVisit = intent.getBooleanExtra("pastVisit", false);
            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            if (physicalExams == null) physicalExams = new ArrayList<>();
            physicalExams.clear();
            if (selectedExams != null && !selectedExams.isEmpty()) {
                physicalExams.addAll(selectedExams);
            }
//            collectChatConnectionInfoFromFirebase();
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

            prescription1 = obj.getString("presciptionHeader0") + "<br/> " + obj.getString("presciptionHeader1");
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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startChat());
        mLayout = findViewById(R.id.summary_layout);
        context = getApplicationContext();
        mDoctorAppointmentBookingTextView = findViewById(R.id.tvDoctorAppointmentBooking);
        mCancelAppointmentBookingTextView = findViewById(R.id.tvDoctorAppointmentBookingCancel);
        mInfoAppointmentBookingTextView = findViewById(R.id.tvDoctorAppointmentBookingInfo);
        mCancelAppointmentBookingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAppointment();
            }
        });
        mDoctorAppointmentBookingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doQuery();
                if ((speciality_selected == null || speciality_selected.isEmpty() || "Select Specialization".equalsIgnoreCase(speciality_selected))) {
                    Toast.makeText(VisitSummaryActivity.this, getString(R.string.please_select_speciality), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isSynedFlag.equalsIgnoreCase("0")) {
                    Toast.makeText(VisitSummaryActivity.this, getString(R.string.please_upload_visit), Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivityForResult(new Intent(VisitSummaryActivity.this, ScheduleListingActivity.class).putExtra("visitUuid", visitUuid).putExtra("patientUuid", patientUuid).putExtra("patientName", patientName).putExtra("appointmentId", mAppointmentId).putExtra("openMrsId", patient.getOpenmrs_id()).putExtra("speciality", speciality_selected), SCHEDULE_LISTING_INTENT);
            }
        });
        mAdditionalDocsRecyclerView = findViewById(R.id.recy_additional_documents);
        mPhysicalExamsRecyclerView = findViewById(R.id.recy_physexam);
        diagnosisCard = findViewById(R.id.cardView_diagnosis);
        prescriptionCard = findViewById(R.id.cardView_rx);
        medicalAdviceCard = findViewById(R.id.cardView_medical_advice);
        requestedTestsCard = findViewById(R.id.cardView_tests);
        additionalCommentsCard = findViewById(R.id.cardView_additional_comments);
        dischargeOrderCard = findViewById(R.id.cardView_discharge_order);
        aidOrderCard = findViewById(R.id.cardView_aid_order);
        followUpDateCard = findViewById(R.id.cardView_follow_up_date);
        mDoctorTitle = findViewById(R.id.title_doctor);
        mDoctorName = findViewById(R.id.doctor_details);
        //  fl_DispenseAdminister = findViewById(R.id.fl_DispenseAdminister);
        frameLayout_doctor = findViewById(R.id.frame_doctor);
        frameLayout_doctor.setVisibility(View.GONE);
        saveButton = findViewById(R.id.card_save);
        presc_status = findViewById(R.id.prescription_status);
        card_print = findViewById(R.id.card_print);
        card_share = findViewById(R.id.card_share);
        btnSignSubmit = findViewById(R.id.btnSignSubmit);
        saveBtn = findViewById(R.id.saveBtn);
        tie_add_remarks = findViewById(R.id.tie_add_remarks);
        tie_add_remarks.setFilters(new InputFilter[]{emojiFilter});
        nscrollview = findViewById(R.id.nscrollview);
        tie_add_remarks.setHint(getString(R.string.enter_details_here));
        tie_add_remarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tie_add_remarks.setHint("");
                tie_add_remarks.setBackground(getResources().getDrawable(R.drawable.edittext_border));
                tie_add_remarks.setHintTextColor(getResources().getColor(R.color.edittext_hint_color));

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equalsIgnoreCase(""))
                    tie_add_remarks.setHint(getString(R.string.enter_details_here));
                else
                    tie_add_remarks.setHint("");
            }
        });


        // thermal printer
       /* tv_device_selected = findViewById(R.id.tv_device_selected);
        btn_connect = findViewById(R.id.btn_connect);
        pb_connect = findViewById(R.id.pb_connect);

        application = new IntelehealthApplication();
        application.setCurrentCmdType(BaseEnum.CMD_ESC);
        // printerFactory = new UniversalPrinterFactory();
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        rtPrinter.setPrinterInterface(curPrinterInterface);
        PrinterObserverManager.getInstance().add(this);
        application.setRtPrinter(rtPrinter);*/

/*
        tv_device_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here on click, will open the Dialog that will show all the nearby Bluetooth devices...
                showBluetoothDeviceChooseDialog();
            }
        });
*/

/*
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here on clicking will connect with the selected Bluetooth device...
                doConnect();
            }
        });
*/

        //get from encountertbl from the encounter
        EncounterDAO encounterStartVisitNoteDAO = new EncounterDAO();
        visitnoteencounteruuid = encounterStartVisitNoteDAO.getStartVisitNoteEncounterByVisitUUID(visitUuid);

        card_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    textPrint();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        saveButton.setOnClickListener(v -> {
            try {
                if (objClsDoctorDetails != null) {
                    doWebViewPrint_Button();
                } else {
                    Toast.makeText(VisitSummaryActivity.this, getResources().getString(R.string.no_presc_available), Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException exception) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
        });

        card_share.setOnClickListener(v -> {

            if (hasPrescription.equalsIgnoreCase("true")) {
                String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
                String whatsappMessage = "Hello, Thank you for using Intelehealth. To Download your prescription please click here " + partial_whatsapp_presc_url + visitUuid +
                        " and enter your Patient ID - " + idView.getText().toString();
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                    partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrlArabic();
                    whatsappMessage = "مرحبًا ، شكرًا لك على استخدام صلة. لتحميل الوصفة الطبية الخاصة بك ، يرجى النقر هنا " + partial_whatsapp_presc_url + visitUuid +
                            " وأدخل معرف الحالة - " + idView.getText().toString();
                }
                //String whatsapp_url = partial_whatsapp_presc_url.concat(visitUuid);
                //String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here) + "\t" + whatsapp_url + "\t" + getString(R.string.and_enter_your_patient_id) + "\t" + idView.getText().toString();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://api.whatsapp.com/send?text=%s", whatsappMessage))));
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                alertDialog.setMessage(getResources().getString(R.string.download_prescription_first_before_sharing));
                alertDialog.setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = alertDialog.show();
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

            }


        });


//        mDoctorTitle.setVisibility(View.GONE);
//        mDoctorName.setVisibility(View.GONE);
        speciality_spinner = findViewById(R.id.speciality_spinner);
        second_speciality_spinner = findViewById(R.id.second_speciality_spinner);
        diagnosisTextView = findViewById(R.id.textView_content_diagnosis);
        //   prescriptionTextView = findViewById(R.id.textView_content_rx);
        medicalAdviceTextView = findViewById(R.id.textView_content_medical_advice);
     //   requestedTestsTextView = findViewById(R.id.textView_content_tests);
        additionalCommentsTextView = findViewById(R.id.textView_content_additional_comments);
        dischargeOrderTextView = findViewById(R.id.textView_content_discharge_order);
        aidOrderType1TextView = findViewById(R.id.textView_content_aid_order_type1);
        aidOrderType2TextView = findViewById(R.id.textView_content_aid_order_type2);
        aidOrderType3TextView = findViewById(R.id.textView_content_aid_order_type3);
        aidOrderType4TextView = findViewById(R.id.textView_content_aid_order_type4);
        aidOrderType5TextView = findViewById(R.id.textView_content_aid_order_type5);
        aidOrderType1TableRow = findViewById(R.id.tableRow_content_aid_order_type1);
        aidOrderType2TableRow = findViewById(R.id.tableRow_content_aid_order_type2);
        aidOrderType3TableRow = findViewById(R.id.tableRow_content_aid_order_type3);
        aidOrderType4TableRow = findViewById(R.id.tableRow_content_aid_order_type4);
        aidOrderType5TableRow = findViewById(R.id.tableRow_content_aid_order_type5);
        aidhl_1 = findViewById(R.id.aidhl_1);
        aidhl_2 = findViewById(R.id.aidhl_2);
        aidhl_3 = findViewById(R.id.aidhl_3);
        aidhl_4 = findViewById(R.id.aidhl_4);
        tl_prescribed_medications = findViewById(R.id.tl_prescribed_medications);
        ll_test = findViewById(R.id.ll_test);
        followUpDateTextView = findViewById(R.id.textView_content_follow_up_date);
        ivPrescription = findViewById(R.id.iv_prescription);


        //if row is present i.e. if true is returned by the function then the spinner will be disabled.
        Log.d("visitUUID", "onCreate_uuid: " + visitUuid);
        isVisitSpecialityExists = speciality_row_exist_check(visitUuid, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
        isVisitSecondSpecialityExists = speciality_row_exist_check(visitUuid, "8100ec1a-063b-47d5-9781-224d835fc688");
        if (isVisitSpecialityExists) speciality_spinner.setEnabled(false);
        if (isVisitSecondSpecialityExists) second_speciality_spinner.setEnabled(false);


        //spinner is being populated with the speciality values...
        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();
        VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();

        List<String> items = providerAttributeLIstDAO.getAllValues(sessionManager1.getAppLanguage());
        List<String> second_speciality_items = providerAttributeLIstDAO.getAllValuesForSecond(sessionManager1.getAppLanguage());
        Log.d("specc", "spec: " + visitUuid);
        String special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
        String second_special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, "8100ec1a-063b-47d5-9781-224d835fc688");
        //Hashmap to List<String> add all value
        ArrayAdapter<String> stringArrayAdapter, stringArrayAdapter1;

        //  if(getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("en")) {
        if (items != null) {
            items.add(0, getString(R.string.select_specialization_text));
            stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
            speciality_spinner.setAdapter(stringArrayAdapter);
        } else {
            stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.speciality_values));
            speciality_spinner.setAdapter(stringArrayAdapter);
        }

        if (second_speciality_items != null) {
            second_speciality_items.add(0, getString(R.string.select_specialization_text));
            stringArrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, second_speciality_items);
            second_speciality_spinner.setAdapter(stringArrayAdapter1);
        } else {
            stringArrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.second_speciality_values));
            second_speciality_spinner.setAdapter(stringArrayAdapter1);
        }

        if (special_value != null) {
            int spinner_position = 0;
            if (sessionManager1.getAppLanguage().equalsIgnoreCase("ar")) {
                spinner_position = stringArrayAdapter.getPosition(org.intelehealth.app.utilities.StringUtils.getProviderNameInArabic(special_value));
            } else {
                spinner_position = stringArrayAdapter.getPosition(special_value);
            }
            speciality_spinner.setSelection(spinner_position);
        } else {

        }

        if (second_special_value != null) {
            int spinner_position = 0;
            if (sessionManager1.getAppLanguage().equalsIgnoreCase("ar")) {
                spinner_position = stringArrayAdapter1.getPosition(org.intelehealth.app.utilities.StringUtils.getProviderNameInArabic(second_special_value));
            } else {
                spinner_position = stringArrayAdapter1.getPosition(second_special_value);
            }
            second_speciality_spinner.setSelection(spinner_position);
        } else {

        }

        speciality_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("SPINNER", "SPINNER_Selected: " + adapterView.getItemAtPosition(i).toString());

                speciality_selected = org.intelehealth.app.utilities.StringUtils.getProviderNameInEnglish(adapterView.getItemAtPosition(i).toString());
                Log.d("SPINNER", "SPINNER_Selected_final: " + speciality_selected);


            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        second_speciality_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                second_speciality_selected = org.intelehealth.app.utilities.StringUtils.getProviderNameInEnglish(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        obsImgdir = new File(AppConstants.IMAGE_PATH);

        flag = findViewById(R.id.flaggedcheckbox);
        EncounterDAO encounterDAO = new EncounterDAO();
        String emergencyUuid = "";
        try {
            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
            flag.setChecked(true);
        }

        physicalDoumentsUpdates();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        editVitals = findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = findViewById(R.id.imagebutton_edit_pathist);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        //  rl_med_aid = findViewById(R.id.rl_med_aid);

        layout_dispense_1 = findViewById(R.id.layout_dispense_1);
        layout_dispense_2 = findViewById(R.id.layout_dispense_2);
        layout_test = findViewById(R.id.layout_test);

        tvDispense_1 = layout_dispense_1.findViewById(R.id.tvDispense);
        tvDispense_2 = layout_dispense_2.findViewById(R.id.tvDispense);

        tvAdminister_1 = layout_dispense_1.findViewById(R.id.tvAdminister);
        tvAdminister_2 = layout_dispense_2.findViewById(R.id.tvAdminister);
        tvAdminister_2.setVisibility(View.GONE);

        tvCollectedBy = layout_test.findViewById(R.id.tvDispense);
        tvCollectedBy.setText(R.string.collected_by);
        tvReceivedBy = layout_test.findViewById(R.id.tvAdminister);
        tvReceivedBy.setText(R.string.resulted_by);

        uploadButton = findViewById(R.id.button_upload);
        downloadButton = findViewById(R.id.button_download);
        onExaminationDownload = findViewById(R.id.imagebutton_download_physexam);
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
            btnSignSubmit.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
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
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });

        tvDispense_1.setOnClickListener(this);
        tvDispense_1.setTag(encounterStartVisitNoteDAO);
        tvDispense_2.setOnClickListener(this);
        tvDispense_2.setTag(encounterStartVisitNoteDAO);

        tvAdminister_1.setOnClickListener(this);
        tvAdminister_1.setTag(encounterStartVisitNoteDAO);
        tvAdminister_2.setOnClickListener(this);
        tvAdminister_2.setTag(encounterStartVisitNoteDAO);

        tvCollectedBy.setTag(encounterStartVisitNoteDAO);
        tvCollectedBy.setOnClickListener(v -> {
            EncounterDAO tag = (EncounterDAO) v.getTag();
            collected_received_Intent(tag, COLLECTED);
        });

        tvReceivedBy.setTag(encounterStartVisitNoteDAO);
        tvReceivedBy.setOnClickListener(v -> {
            EncounterDAO tag = (EncounterDAO) v.getTag();
            collected_received_Intent(tag, RECEIVED);
        });

/*
        rl_med_aid.setOnClickListener(v -> {
            Intent i = new Intent(context, Medication_Aid_Activity.class);
            startActivity(i);
        });
*/

        saveBtn.setOnClickListener(v -> {
            // Additional remarks - start
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                saveBtn.setForeground(getResources().getDrawable(R.drawable.button_bg_rounded_stroke));

            String addRemarks = tie_add_remarks.getText().toString().trim();
            if (addRemarks != null) {
                if (!addRemarks.isEmpty()) {    // ie. not empty.
                    createAdditionalRemarksOBSandPush(addRemarks);
                }
                else {  // ie. empty
                    tie_add_remarks.requestFocus();
                    tie_add_remarks.setBackground(getResources().getDrawable(R.drawable.edittext_error_border));
                    tie_add_remarks.setHintTextColor(getResources().getColor(R.color.design_default_color_error));
                  //  additionalRemarkValidaiton(tie_add_remarks);
                }
            }
            // Additional remarks - end
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("visitUUID", "upload_click: " + visitUUID);

                // Additional Remarks - start
                if(!tie_add_remarks.getText().toString().trim().isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        tie_add_remarks.requestFocus();
                        saveBtn.setForeground(getResources().getDrawable(R.drawable.edittext_error_border));
                        Toast.makeText(context, getString(R.string.click_on_save_button_to_save_the_changes), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                // Additional Remarks - end

                // The below condition has been added keeping in mind that no HW will be able to upload empty complaints: (Ticket SYR-171): Nishita Goyal
                if (complaint.getValue() == null || complaintView.getText().equals("") || complaintView.getText().equals(" ")) {
                    Toast.makeText(VisitSummaryActivity.this, getResources().getString(R.string.visit_failed_for_no_complaint), Toast.LENGTH_LONG).show();
                    return;
                }

                if (speciality_spinner.getSelectedItemPosition() == 0 && second_speciality_spinner.getSelectedItemPosition() == 0) {
                    showSelectSpecialtyErrorDialog();
                    return;
                }
                isVisitSpecialityExists = speciality_row_exist_check(visitUUID, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
                isVisitSecondSpecialityExists = speciality_row_exist_check(visitUUID, "8100ec1a-063b-47d5-9781-224d835fc688");

                if (second_speciality_spinner.getSelectedItemPosition() != 0) {
                    VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                    boolean isUpdateVisitDone = false;
                    try {
                        if (!isVisitSecondSpecialityExists) {
                            isUpdateVisitDone = speciality_attributes.insertVisitAttributes(visitUuid, second_speciality_selected, "8100ec1a-063b-47d5-9781-224d835fc688");
                        }
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }

                    if (isVisitSecondSpecialityExists) second_speciality_spinner.setEnabled(false);

                }

                if (speciality_spinner.getSelectedItemPosition() != 0) {
                    VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                    boolean isUpdateVisitDone = false;
                    try {
                        if (!isVisitSpecialityExists) {
                            isUpdateVisitDone = speciality_attributes.insertVisitAttributes(visitUuid, speciality_selected, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
                        }
                        Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                    } catch (DAOException e) {
                        e.printStackTrace();
                        Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
                    }


                    if (isVisitSpecialityExists) speciality_spinner.setEnabled(false);
                }

                if (flag.isChecked()) {
                    try {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        encounterDAO.setEmergency(visitUuid, true);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
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

                if (visitUUID == null || visitUUID.isEmpty()) {
                    String visitIDSelection = "uuid = ?";
                    String[] visitIDArgs = {visitUuid};
                    final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
                    if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                        visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                    }
                    if (visitIDCursor != null) visitIDCursor.close();
                }

                if (NetworkConnection.isOnline(getApplication())) {
                    Toast.makeText(context, getResources().getString(R.string.upload_started), Toast.LENGTH_LONG).show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Added the 4 sec delay and then push data.For some reason doing immediately does not work
                            //Do something after 100ms
                            SyncUtils syncUtils = new SyncUtils();
                            boolean isSynced = syncUtils.syncForeground("visitSummary");
                            if (isSynced) {
                                /*presc_status.setText(getResources().getString(R.string.prescription_pending));
                                presc_status.setBackground(getResources().getDrawable(R.drawable.presc_status_red));*/

                                AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_upload), getString(R.string.visit_uploaded_successfully), 3, VisitSummaryActivity.this);
                                isSynedFlag = "1";
                                //
                                showVisitID();
                                isVisitUploaded();

                                Log.d("visitUUID", "showVisitID: " + visitUUID);
                                isVisitSpecialityExists = speciality_row_exist_check(visitUUID, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
                                isVisitSecondSpecialityExists = speciality_row_exist_check(visitUUID, "8100ec1a-063b-47d5-9781-224d835fc688");
                                if (isVisitSpecialityExists)
                                    speciality_spinner.setEnabled(false);
                                if (isVisitSecondSpecialityExists)
                                    second_speciality_spinner.setEnabled(false);
                            } else {
                                AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity.this);

                            }
                            uploaded = true;
                        }
                    }, 4000);
                } else {
                    AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity.this);
                }
            }
        });

        if (intentTag != null && intentTag.equals("prior")) {
            uploadButton.setEnabled(false);
        }

        nameView = findViewById(R.id.textView_name_value);
        ageView = findViewById(R.id.textView_age_value);
        genderView = findViewById(R.id.textView_gender_value);

        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        visitView = findViewById(R.id.textView_visit_value);

        queryData(String.valueOf(patientUuid));


        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            idView.setText(patient.getOpenmrs_id());
        } else {
            idView.setText(getString(R.string.patient_not_registered));
        }
        String gender_tv = patientGender;
        nameView.setText(patientName);
        ageView.setText(DateAndTimeUtils.getAgeInYearMonth(patient.getDate_of_birth(), this));

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
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
                if (temperature.getValue() != null && !temperature.getValue().isEmpty() && !temperature.getValue().equalsIgnoreCase("0")) {
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
                heightView.setText("");
            } else {
                heightView.setText(height.getValue());
            }
        }

        if (weight.getValue() != null) {
            if (weight.getValue().trim().equals("0")) {
                weightView.setText("");
            } else {
                weightView.setText(weight.getValue());
            }
        }

        if (pulse.getValue() != null) {
            if (pulse.getValue().trim().equals("0")) {
                pulseView.setText("");
            } else {
                pulseView.setText(pulse.getValue());
            }
        }

        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        if (bpText.equals("/")) {  //when new patient is being registered we get / for BP
            bpView.setText("");
        } else if (bpText.equalsIgnoreCase("null/null")) {
            //when we setup app and get data from other users, we get null/null from server...
            bpView.setText("");
        } else if (bpSys.getValue().equalsIgnoreCase("0") || bpDias.getValue().equalsIgnoreCase("0")) {
            bpView.setText("");
        } else {
            bpView.setText(bpText);
        }

        Log.d(TAG, "onCreate: " + weight.getValue());
        String mWeight = weight.getValue();
        String mHeight = height.getValue();
        if ((mHeight != null && mWeight != null) && !mHeight.isEmpty() && !mWeight.isEmpty() && (!mHeight.equalsIgnoreCase("0") && !mWeight.equalsIgnoreCase("0"))) {
            double numerator = Double.parseDouble(mWeight) * 10000;
            double denominator = Double.parseDouble(mHeight) * Double.parseDouble(mHeight);
            double bmi_value = numerator / denominator;
            mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);
        } else {
            mBMI = "";
        }
        patHistory.setValue(medHistory);

        bmiView.setText(mBMI);

        //    Respiratory added by mahiti dev team
        if (resp.getValue() != null) {
            if (resp.getValue().trim().equals("0")) {
                respiratory.setText("");
            } else {
                respiratory.setText(resp.getValue());
            }
        }

        if (spO2.getValue() != null) {
            if (spO2.getValue().trim().equals("0")) {
                spO2View.setText("");
            } else {
                spO2View.setText(spO2.getValue());
            }
        }

        if (complaint.getValue() != null)
            complaintView.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
        if (famHistory.getValue() != null && !famHistory.getValue().isEmpty())
            famHistView.setText(Node.bullet + Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
        if (patHistory.getValue() != null)
            patHistView.setText(Html.fromHtml(patHistory.getValue(sessionManager.getAppLanguage())));
        if (phyExam.getValue() != null)
            physFindingsView.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));

        editVitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(VisitSummaryActivity.this, VitalsActivity.class);
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("gender", patientGender);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("gender", patientGender);
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
                famHistDialog.setTitle(getString(R.string.visit_summary_family_history));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                famHistDialog.setView(convertView);

                final TextView famHistText = convertView.findViewById(R.id.textView_entry);
                if (famHistory.getValue() != null)
                    famHistText.setText(Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
                famHistText.setEnabled(false);

                /*famHistDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        // final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (famHistory.getValue(sessionManager.getAppLanguage()) != null && !famHistory.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase(""))
                            dialogEditText.setText(Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
                        else dialogEditText.setText("");
//                        if (famHistory.getValue(sessionManager.getAppLanguage()) != null && !famHistory.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase(""))
//                            dialogEditText.setText(Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
//                        else
//                        {
//                            if(sessionManager.getAppLanguage().equalsIgnoreCase("en"))
//                            {
//                                if (famHistory.getValue("ar") != null && !famHistory.getValue("ar").equalsIgnoreCase(""))
//                                    dialogEditText.setText(Html.fromHtml(famHistory.getValue("ar")));
//                                else
//                                    dialogEditText.setText("");
//                            }
//                            else if(sessionManager.getAppLanguage().equalsIgnoreCase("ar"))
//                            {
//                                if (famHistory.getValue("en") != null && !famHistory.getValue("en").equalsIgnoreCase(""))
//                                    dialogEditText.setText(Html.fromHtml(famHistory.getValue("en")));
//                                else
//                                    dialogEditText.setText("");
//                            }
//                            else
//                                dialogEditText.setText("");
//                        }
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //famHistory.setValue(dialogEditText.getText().toString());
                                String dataStringValue = mapDataIntoJson(dialogEditText.getText().toString().replace("\n", "<br>"));
                                famHistory.setValue(dataStringValue);
                                if (famHistory.getValue() != null) {
                                    famHistText.setText(Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
                                    famHistView.setText(Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
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
                        AlertDialog alertDialog = textInput.show();
                        dialogInterface.dismiss();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
                    }
                });*/

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
                        intent1.putExtra("gender", patientGender);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("edit_FamHist", "edit_FamHist");
                        intent1.putExtra("gender", patientGender);
                     /*   if(EncounterAdultInitial_LatestVisit != null &&
                                !EncounterAdultInitial_LatestVisit.isEmpty()) {
                            intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                        }
                        else {
                            intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        }*/
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
                complaintDialog.setTitle(getString(R.string.visit_summary_complaint));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                complaintDialog.setView(convertView);

                final TextView complaintText = convertView.findViewById(R.id.textView_entry);
                if (complaint.getValue() != null) {
                    complaintText.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
                }
                complaintText.setEnabled(false);

               /* complaintDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (complaint.getValue(sessionManager.getAppLanguage()) != null && !complaint.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase("")) {
                            dialogEditText.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
                        } else {
                            dialogEditText.setText("");
                        }
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String dataStringValue = mapDataIntoJson(dialogEditText.getText().toString().replace("\n", "<br>"));
                                complaint.setValue(dataStringValue);
                                if (complaint.getValue() != null) {
//                                    famHistText.setText(Html.fromHtml(famHistory.getValue(sessionManager.getAppLanguage())));
                                    complaintView.setText(Html.fromHtml(complaint.getValue(sessionManager.getAppLanguage())));
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
                        AlertDialog alertDialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });*/

                complaintDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
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
                        intent1.putExtra("gender", patientGender);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
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
                physicalDialog.setTitle(getString(R.string.visit_summary_on_examination));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                physicalDialog.setView(convertView);

                final TextView physicalText = convertView.findViewById(R.id.textView_entry);
                if (phyExam.getValue(sessionManager.getAppLanguage()) != null && !phyExam.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase(""))
                    physicalText.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));
                physicalText.setEnabled(false);

               /* physicalDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (phyExam.getValue(sessionManager.getAppLanguage()) != null && !phyExam.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase(""))
                            dialogEditText.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));
                        else dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String dataStringValue = mapDataIntoJson(dialogEditText.getText().toString().replace("\n", "<br>"));
                                phyExam.setValue(dataStringValue);
                                if (phyExam.getValue() != null) {
                                    physicalText.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));
                                    physFindingsView.setText(Html.fromHtml(phyExam.getValue(sessionManager.getAppLanguage())));
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
                        AlertDialog dialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity.this, dialog);
                        dialogInterface.dismiss();
                    }
                });*/

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
                        intent1.putExtra("gender", patientGender);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("gender", patientGender);
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
                    historyText.setText(Html.fromHtml(patHistory.getValue(sessionManager.getAppLanguage())));
                historyText.setEnabled(false);

                /*historyDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity.this);
                        if (patHistory.getValue(sessionManager.getAppLanguage()) != null && !patHistory.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase(""))
                            dialogEditText.setText(Html.fromHtml(patHistory.getValue(sessionManager.getAppLanguage())));
                        else dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //patHistory.setValue(dialogEditText.getText().toString());
                                String dataStringValue = mapDataIntoJson(dialogEditText.getText().toString().replace("\n", "<br>"));
                                patHistory.setValue(dataStringValue);
                                if (patHistory.getValue(sessionManager.getAppLanguage()) != null && !patHistory.getValue(sessionManager.getAppLanguage()).equalsIgnoreCase("")) {
                                    historyText.setText(Html.fromHtml(patHistory.getValue(sessionManager.getAppLanguage())));
                                    patHistView.setText(Html.fromHtml(patHistory.getValue(sessionManager.getAppLanguage())));
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
                });*/

                historyDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(VisitSummaryActivity.this, PastMedicalHistoryActivity.class);
                        intent1.putExtra("patientUuid", patientUuid);
                        intent1.putExtra("visitUuid", visitUuid);
                        intent1.putExtra("encounterUuidVitals", encounterVitals);
                        intent1.putExtra("edit_PatHist", "edit_PatHist");
                        intent1.putExtra("gender", patientGender);
//                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                      /*  if(EncounterAdultInitial_LatestVisit != null &&
                                !EncounterAdultInitial_LatestVisit.isEmpty()) {
                            intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                        }
                        else {
                            intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                        }*/
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

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkConnection.isOnline(getApplication())) {
                    Toast.makeText(context, getResources().getString(R.string.downloading), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, getResources().getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
                }

                SyncUtils syncUtils = new SyncUtils();
                syncUtils.syncForeground("downloadPrescription");
//                AppConstants.notificationUtils.DownloadDone(getString(R.string.download_from_doctor), getString(R.string.prescription_downloaded), 3, VisitSummaryActivity.this);
                uploaded = true;
//                ProgressDialog pd = new ProgressDialog(VisitSummaryActivity.this);
//                pd.setTitle(getString(R.string.downloading_prescription));
//                pd.show();
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

     /*   additionalDocumentsDownlaod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_AD);
            }
        }); */
        onExaminationDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_PE);
            }
        });

        doQuery();
        isVisitUploaded();
        //   queryData(String.valueOf(patientUuid));
        //  downloadPrescriptionDefault();
        getAppointmentDetails(visitUuid);
    }

    /**
     * This function is used to save the entered Additional Remarks into the Obs table and set sync = false to be pushed to the backend.
     * @param addRemarks - the information that is entered by the HW.
     */
    private void createAdditionalRemarksOBSandPush(String addRemarks) {
        EncounterDAO encounterDAO = new EncounterDAO(); // 1. update sync of encounter.
        try {
            encounterDAO.updateEncounterSync("false", encounterUuidAdultIntial);
            encounterDAO.updateEncounterModifiedDate(encounterUuidAdultIntial);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        // Create OBS and push - START
        PastNotesModel model = new PastNotesModel();
        model.setAdditional_remark(addRemarks);
        model.setHwUuid(sessionManager.getProviderID()); // 3. hw uuid
        model.setHwName(sessionManager.getChwname());    // 3b. hw name
        model.setDateTime(AppConstants.dateAndTimeUtils.currentDateTime()); // 4. datetime.

        Gson gson = new Gson();
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.ADDITIONAL_REMARKS);  // OBS aid data.
        obsDTO.setEncounteruuid(encounterUuidAdultIntial);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(gson.toJson(model));

        Log.d(TAG, "insertAidObs: " + gson.toJson(model));

        try {
            boolean isInserted = obsDAO.insertObs(obsDTO);  // 2. create new obs.
            if (isInserted) {
                tie_add_remarks.setText("");
                Toast.makeText(context, getString(R.string.additional_remark_is_saved_successfully), Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        // END
    }

    private void startChat() {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUID(visitUuid);
        RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
        RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitUuid);
        RtcArgs args = new RtcArgs();
        if (rtcConnectionDTO != null)
            args.setDoctorUuid(rtcConnectionDTO.getConnectionInfo());
        else args.setDoctorUuid("");
        args.setPatientId(patientUuid);
        args.setPatientName(patientName);
        args.setVisitId(visitUuid);
        args.setNurseId(encounterDTO.getProvideruuid());
        SilaChatActivity.startChatActivity(VisitSummaryActivity.this, args);
    }

    private void admininisterIntent(EncounterDAO encounterStartVisitNoteDAO) {
        String med = getMedicationData();
        //get from encountertbl from the encounter
        if (visitnoteencounteruuid.isEmpty()) {
            visitnoteencounteruuid = encounterStartVisitNoteDAO.getStartVisitNoteEncounterByVisitUUID(visitUuid);
        }

        if (med.trim().isEmpty()) {
            Toast.makeText(context, getString(R.string.no_medication_data_present_to_administer), Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(context, Medication_Aid_Activity.class);
        i.putExtra("mtag", "administer");
        i.putExtra("medicineData", med);
        i = sendCommonIntentToMedicationActivity(i);

        startActivity(i);
    }

    private void collected_received_Intent(EncounterDAO encounterStartVisitNoteDAO, String screenTag) {
        String test = getTestsData();

        //get from encountertbl from the encounter
        if (visitnoteencounteruuid.isEmpty()) {
            visitnoteencounteruuid = encounterStartVisitNoteDAO.getStartVisitNoteEncounterByVisitUUID(visitUuid);
        }

        if (test.trim().isEmpty()) {
            Toast.makeText(context, "No test data found.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(context, Medication_Aid_Activity.class);
        i.putExtra("mtag", screenTag);  // ie. collected or received
        i.putExtra("testData", test);
        i = sendCommonIntentToMedicationActivity(i);

        startActivity(i);
    }


    public void dispenseIntent(EncounterDAO encounterStartVisitNoteDAO) {

        String med = getMedicationData();
        String aid = getAidData();

        //get from encountertbl from the encounter
        if (visitnoteencounteruuid.isEmpty()) {
            visitnoteencounteruuid = encounterStartVisitNoteDAO.getStartVisitNoteEncounterByVisitUUID(visitUuid);
        }

        if (med.trim().isEmpty() && aid.trim().isEmpty()) {
            Toast.makeText(context, getString(R.string.no_medication_and_aid_data_present_to_dispense), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "dispense intent: " + med + ", " + aid + ", " + patientUuid + ", " + visitUuid + ", " +
                visitnoteencounteruuid + ", " + encounterVitals + ", " + encounterUuidAdultIntial); // visitnoteenc comes empty here.

        Intent i = new Intent(context, Medication_Aid_Activity.class);
        i.putExtra("mtag", "dispense");
        i.putExtra("medicineData", med);
        i.putExtra("aidData", aid);
        i = sendCommonIntentToMedicationActivity(i);

        //   mSharedPreference = this.getSharedPreferences("visit_summary", Context.MODE_PRIVATE);

        startActivity(i);
    }

    private Intent sendCommonIntentToMedicationActivity(Intent i) {
        i.putExtra("patientUuid", patientUuid);
        i.putExtra("visitUuid", visitUuid);
        i.putExtra("encounterVisitNote", visitnoteencounteruuid);
        i.putExtra("encounterUuidVitals", encounterVitals);
        i.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
        i.putExtra("gender", patientGender);
        i.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
        i.putExtra("name", patientName);
        i.putExtra("age", patientAge);
        i.putExtra("float_ageYear_Month", float_ageYear_Month);
        i.putExtra("tag", intentTag);
        i.putExtra("pastVisit", isPastVisit);

        return i;
    }

    private String mapDataIntoJson(String dataString) {
        Map<String, String> dataMapString = new HashMap<>();
        //In manual entry case, the same value has to be stored in the language in json object. This was suggested by Sagar under ticket SYR-166: Nishita Goyal
        dataMapString.put("ar", dataString);
        dataMapString.put("en", dataString);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        dataString = gson.toJson(dataMapString);
        return dataString;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

//    private void doConnect() {
//
//        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) { // No device is selected.
//            showAlertDialog(getString(R.string.main_pls_choose_device));
//            return;
//        }
//
//        pb_connect.setVisibility(View.VISIBLE);
//        TimeRecordUtils.record("Start：", System.currentTimeMillis());
//        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
//        connectBluetooth(bluetoothEdrConfigBean);
//    }

/*
    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);

        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //do nothing...
        }
    }
*/


/*
    public void showAlertDialog(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog.Builder dialog =
                        new android.app.AlertDialog.Builder(VisitSummaryActivity.this);
                dialog.setTitle("Please connect device");
                dialog.setMessage(msg);
                dialog.setNegativeButton(R.string.cancel, null);
                dialog.show();
            }
        });
    }
*/

    //This will open a Dialog that will show all the Bluetooth devices...
/*
    private void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(
                new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
                    @Override
                    public void onDeviceItemClick(BluetoothDevice device) {
                        if (TextUtils.isEmpty(device.getName())) {
                            tv_device_selected.setText(device.getAddress());
                        } else {
                            tv_device_selected.setText(device.getName() + " [" + device.getAddress() + "]");
                        }
                        configObj = new BluetoothEdrConfigBean(device);
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                        isConfigPrintEnable(configObj);
                    }
                });
        bluetoothDeviceChooseDialog.show(VisitSummaryActivity.this.getFragmentManager(), null);
    }
*/

/*
    private void isConfigPrintEnable(Object configObj) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true);
        } else {
            setPrintEnable(false);
        }
    }
*/

/*
    private void setPrintEnable(boolean isEnable) {
        card_print.setEnabled(isEnable);
        btn_connect.setEnabled(!isEnable);
    }
*/

/*
    private boolean isInConnectList(Object configObj) {
        boolean isInList = false;
        for (int i = 0; i < printerInterfaceArrayList.size(); i++) {
            PrinterInterface printerInterface = printerInterfaceArrayList.get(i);
            if (configObj.toString().equals(printerInterface.getConfigObject().toString())) {
                if (printerInterface.getConnectState() == ConnectStateEnum.Connected) {
                    isInList = true;
                    break;
                }
            }
        }
        return isInList;
    }
*/

    private boolean additionalRemarkValidaiton(View view) {

        return true;
    }

    /**
     * @param uuid the visit uuid of the patient visit records is passed to the function.
     * @return boolean value will be returned depending upon if the row exists in the tbl_visit_attribute tbl
     */
    private boolean speciality_row_exist_check(String uuid, String specialityUuid) {
        boolean isExists = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=? AND visit_attribute_type_uuid = ?", new String[]{uuid, specialityUuid});

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


    //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
    public void setLocale(String appLanguage) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
            VisitSummaryActivity.this.createConfigurationContext(conf);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
    }

    private String convertCtoF(String temperature) {

        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
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

            String query = "SELECT a.uuid, a.sync " + "FROM tbl_visit a " + "WHERE a.uuid = '" + visitUUID + "'";

            db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
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

                presc_status.setText(getResources().getString(R.string.case_not_uploaded));
                presc_status.setBackground(getResources().getDrawable(R.drawable.presc_status_red));
            }
        } else {
            if (visitUuid != null && !visitUuid.isEmpty()) {
                String hideVisitUUID = visitUuid;
                hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
                visitView.setText("XXXX" + hideVisitUUID);
//              visitView.setText("----");
            }
        }
    }

    private void physcialExaminationImagesDownload() {
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
    }

    private void additionalDocumentImagesDownload() {
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
        additionalImageDownloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_AD);
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

    private void startDownload(String imageType) {

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
        intent.putExtra("ImageType", imageType);
        startService(intent);

    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd-MMMM-yyyy";
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
                //Log.i("Patient WebView", "page finished loading " + url);
                int webview_heightContent = view.getContentHeight();
                //Log.d("variable i", "variable i: " + webview_heightContent);
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
        String mGender = (sessionManager.getAppLanguage().equalsIgnoreCase("ar") ? getLocaleGender(this, patient.getGender()) : patient.getGender());

        Calendar c = Calendar.getInstance();
        //System.out.println(getString(R.string.current_time) + c.getTime());

        String[] columnsToReturn = {"startdate"};
        String visitIDorderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {visitUuid};
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = (sessionManager1.getAppLanguage().equalsIgnoreCase("ar") ? en_ar_dob(DateAndTimeUtils.SimpleDatetoLongDate(startDateTime)) : DateAndTimeUtils.SimpleDatetoLongDate(startDateTime));

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
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }//Load the config file

            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    mTemp = "Temperature(C): " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");

                } else if (obj.getBoolean("mFahrenheit")) {

//                    mTemp = "Temperature(F): " + temperature.getValue();
                    mTemp = getString(R.string.prescription_temp_f) + (!TextUtils.isEmpty(temperature.getValue()) ? convertCtoF(temperature.getValue()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        mresp = resp.getValue();
        mSPO2 = "SpO2(%): " + (!TextUtils.isEmpty(spO2.getValue()) ? spO2.getValue() : "");
        //AnswerValue answerValue = new Gson().fromJson(complaint.getValue(), AnswerValue.class);
        String mComplaint = complaint.getValue();//LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue();
        //Log.v("complaints", mComplaint);
        //Show only the headers of the complaints in the printed prescription
        String[] complaints = StringUtils.split(mComplaint, Node.bullet_arrow);
        mComplaint = "";
        String colon = ":";
        String mComplaint_new = "";
        if (complaints != null) {
            for (String comp : complaints) {
                Log.v("complaints", comp);
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
                //The below change is done to fix the string out of bound exception reported in ticket SYR-172,173. The comparison length is kept to 20 and the text length is counted as 20 in crshlytics: Nishita Goyal
                mComplaint = mComplaint.length() > 20 ? mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3) : "";
            }
        } else {

        }

        if (mComplaint.contains("الأعراض المرافقة")) {
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                //The below change is done to fix the string out of bound exception reported in ticket SYR-172,173. The comparison length is kept to 20 and the text length is counted as 20 in crshlytics: Nishita Goyal
                mComplaint = mComplaint.length() > 20 ? mComplaint.substring(0, compla.indexOf("الأعراض المرافقة") - 3) : "";
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

        String formattedAidOrder = "";
        if (aidOrderType1TextView.getVisibility() == View.VISIBLE && aidOrderType1TextView.getText().toString() != null && !aidOrderType1TextView.getText().toString().trim().equalsIgnoreCase("") && !newMedicalEquipLoanAidOrderPresc.trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType1TextView.getText().toString().trim()*/  newMedicalEquipLoanAidOrderPresc + "\n";
        if (aidOrderType2TextView.getVisibility() == View.VISIBLE && aidOrderType2TextView.getText().toString() != null && !aidOrderType2TextView.getText().toString().trim().equalsIgnoreCase("") && !newFreeMedicalEquipAidOrderPresc.trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType2TextView.getText().toString().trim()*/ newFreeMedicalEquipAidOrderPresc + "\n";
        if (aidOrderType3TextView.getVisibility() == View.VISIBLE && aidOrderType3TextView.getText().toString() != null && !aidOrderType3TextView.getText().toString().trim().equalsIgnoreCase("") && !newCoverMedicalExpenseAidOrderPresc.trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType3TextView.getText().toString().trim()*/  newCoverMedicalExpenseAidOrderPresc + "\n";
        if (aidOrderType4TextView.getVisibility() == View.VISIBLE && aidOrderType4TextView.getText().toString() != null && !aidOrderType4TextView.getText().toString().trim().equalsIgnoreCase("") && !newCoverSurgicalExpenseAidOrderPresc.trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType4TextView.getText().toString().trim()*/ newCoverSurgicalExpenseAidOrderPresc + "\n";
        if (aidOrderType5TextView.getVisibility() == View.VISIBLE && aidOrderType5TextView.getText().toString() != null && !aidOrderType5TextView.getText().toString().trim().equalsIgnoreCase("") && !newCashAssistanceExpenseAidOrderPresc.trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType5TextView.getText().toString().trim()*/ newCashAssistanceExpenseAidOrderPresc;

        formattedAidOrder = formattedAidOrder.replace("Others||", "Others - ");

        String aidOrder_web = "";
        if (!formattedAidOrder.isEmpty() && !formattedAidOrder.trim().equalsIgnoreCase(""))
            aidOrder_web = stringToWebAidOrder(mapDataIntoJson(formattedAidOrder.replace("\n", "<br>")));

        String rx_web = stringToWeb(rxReturned).replace("<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">●</p>", "");

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

        String advice_web = "";

        Log.d("advice", adviceReturned);
        String advice_doctor__ = adviceReturned;
        advice_web = stringToWeb(advice_doctor__);


        String diagnosis_web = stringToWeb(diagnosisReturned);
        String discharge_order_web = stringToWeb(dischargeOrderReturned);
        String comments_web = stringToWeb(additionalReturned);


        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = (sessionManager1.getAppLanguage().equalsIgnoreCase("ar") ? en_ar_dob(spiltFollowDate[0]) + ", " + remainingStr : spiltFollowDate[0] + ", " + remainingStr);
            } else {
                followUpDateStr = followUpDate;
            }
        } else {
            followUpDateStr = followUpDate;
        }

        String followUp_web = stringToWeb(followUpDateStr);
        if (sessionManager1.getAppLanguage().equalsIgnoreCase("ar"))
            followUp_web = en_ar_dob(followUp_web);

        String doctor_web = stringToWeb(doctorName);

        String heading = getPrescriptionHeading();
        String heading2 = prescription2;
        String heading3 = "<br/>";

        String bp = mBP;
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        if (sessionManager1.getAppLanguage().equalsIgnoreCase("ar"))
            mCityState = switch_en_to_ar_village_edit(mCityState);

        String address = mAddress + " " + mCityState + ((!TextUtils.isEmpty(mPhone) && !mPhone.equalsIgnoreCase("-")) ? ", " + mPhone : ", " + checkAndConvertPrescriptionHeadings(getString(R.string.not_provided)));

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
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Almondita.ttf');";
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

            String doctSp = !LocaleHelper.isArabic(this) ? objClsDoctorDetails.getSpecialization() : "طبيب عام"; //General Physician
            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? checkAndConvertPrescriptionHeadings(getString(R.string.dr_registration_no)) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + /*"<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + doctSp + "</span><br>" +*/
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? checkAndConvertPrescriptionHeadings(getString(R.string.dr_email)) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + "</div>";

//            mDoctorName.setText(doctrRegistartionNum + "\n" + Html.fromHtml(doctorDetailStr));
        }
        if (isRespiratory) {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                            /* doctorDetailStr +*/
                            "<u><p id=\"patient_information_title\" style=\"font-size:15pt; margin: 0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.patient_information)) + "</p></b></u>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_name)) + ": %s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_age)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_gender)) + ": %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_address_contact)) + ": %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:0px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_patient_id)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_date_of_visit)) + ": %s </p><br>" +
//                            "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_vitals)) + "</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">" +
//                            checkAndConvertPrescriptionHeadings(getString(R.string.prescription_ht)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_wt)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_bmi)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_bp)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_pulse)) + ": %s | %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_rr)) + ": %s |  %s </p><br>" +
                                   /* "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<u><b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:0px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_reason_for_visit)) + "</p></b></u>" + para_open + "%s" + para_close + "<br><br>"

                    , heading, heading3, mPatientName, age, mGender, address, mPatientOpenMRSID, mDate, /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",*/
                    /*pat_hist, fam_hist,*/ mComplaint);

            if (!diagnosis_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:0px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_diagnosis)) + "</p></b></u>" + "%s<br>", diagnosis_web));
            }

            Log.e(TAG, "doWebViewPrint_Button: rx_web=>" + rx_web);
            if (!rx_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_med_plan_ordered_items)) + "</p></b></u>" + "%s<br>", rx_web));
            }

            if (!tests_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_rec_investigation)) + "</p></b></u>" + "%s<br>", tests_web));
            }

            if (!advice_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_general_instructions)) + "</p></b></u>" + "%s<br>", advice_web));
            }

            //changes done for ticket SYR-358
            /*if (!comments_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"additional_comments_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_additional_comments)) + "</p></b></u>" + "%s<br>", comments_web));
            }*/

            if (!aidOrder_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"aid_order_heading\" style=\"font-size:15pt;margin-top:0px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_aid_order)) + "</p></b></u>" + "%s<br>", aidOrder_web));
            }

            if (!discharge_order_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"discharge_order_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_discharge_order)) + "</p></b></u>" + "%s<br>", discharge_order_web));
            }

            if (!followUp_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_follow_up_date)) + "</p></b></u>" + "%s<br>", followUp_web));
            }


            htmlDocument = htmlDocument.concat(String.format("<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" + doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", doctor_web));

            Log.e(TAG, "doWebViewPrint_Button: html=>" + htmlDocument);
            if (LocaleHelper.isArabic(this))
                htmlDocument = "<html dir=\"rtl\" lang=\"ar\"><body>" + htmlDocument + "</body></html>";
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_age)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_gender)) + ": %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_address_contact)) + ": %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_patient_id)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_date_of_visit)) + ": %s </p><br>" +
//                            "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_vitals)) + "</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_ht)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_wt)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_bmi)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_bp)) + ": %s | " + checkAndConvertPrescriptionHeadings(getString(R.string.prescription_pulse)) + ": %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_reason_for_visit)) + "</p></b>" +

                            para_open + "%s" + para_close + "<br><br>"

                    , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, /*(!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",*/
                    /*pat_hist, fam_hist,*/ mComplaint);


            if (!diagnosis_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_diagnosis)) + "</p></b></u>" + "%s<br>", diagnosis_web));
            }

            if (!rx_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_med_plan)) + "</p></b></u>" + "%s<br>", rx_web));
            }

            if (!tests_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_rec_investigation)) + "</p></b></u>" + "%s<br>", tests_web));
            }

            if (!advice_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_general_advice)) + "</p></b></u>" + "%s<br>", advice_web));
            }

            //changes done for ticket SYR-358
            /*if (!comments_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"additional_comments_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_additional_comments)) + "</p></b></u>" + "%s<br>", comments_web));
            }*/

            if (!aidOrder_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"aid_order_heading\" style=\"font-size:15pt;margin-top:0px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_aid_order)) + "</p></b></u>" + "%s<br>", aidOrder_web));
            }

            if (!discharge_order_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"discharge_order_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_discharge_order)) + "</p></b></u>" + "%s<br>", discharge_order_web));
            }

            if (!followUp_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.prescription_follow_up_date)) + "</p></b></u>" + "%s<br>", followUp_web));
            }

            htmlDocument = htmlDocument.concat(String.format("<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", doctor_web));
            if (LocaleHelper.isArabic(this))
                htmlDocument = "<html dir=\"rtl\" lang=\"\"><body>" + htmlDocument + "</body></html>";
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


//    private void doWebViewPrint() throws ParseException {
//        // Create a WebView object specifically for printing
//        WebView webView = new WebView(this);
//        webView.setWebViewClient(new WebViewClient() {
//
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                Log.i("Patient WebView", "page finished loading " + url);
//                int webview_heightContent = view.getContentHeight();
//                Log.d("variable i", "variable i: " + webview_heightContent);
//                createWebPrintJob(view, webview_heightContent);
//                mWebView = null;
//            }
//        });
//
//        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + patient.getLast_name();
//        String mPatientOpenMRSID = patient.getOpenmrs_id();
//        String mPatientDob = patient.getDate_of_birth();
//        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") +
//                ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
//        String mCityState = patient.getCity_village();
//        String mPhone = (!TextUtils.isEmpty(patient.getPhone_number())) ? patient.getPhone_number() : "";
//        String mState = patient.getState_province();
//        String mCountry = patient.getCountry();
//
//        String mSdw = (!TextUtils.isEmpty(patient.getSdw())) ? patient.getSdw() : "";
//        String mOccupation = patient.getOccupation();
//        String mGender = patient.getGender();
//
//        Calendar c = Calendar.getInstance();
//        System.out.println("Current time => " + c.getTime());
//
//        String[] columnsToReturn = {"startdate"};
//        String visitIDorderBy = "startdate";
//        String visitIDSelection = "uuid = ?";
//        String[] visitIDArgs = {visitUuid};
//        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
//        visitIDCursor.moveToLast();
//        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
//        visitIDCursor.close();
//        String mDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);
//
//        String mPatHist = patHistory.getValue();
//        if (mPatHist == null) {
//            mPatHist = "";
//        }
//        String mFamHist = famHistory.getValue();
//        if (mFamHist == null) {
//            mFamHist = "";
//        }
//        mHeight = height.getValue();
//        mWeight = weight.getValue();
//        mBP = bpSys.getValue() + "/" + bpDias.getValue();
//        mPulse = pulse.getValue();
//        try {
//            JSONObject obj = null;
//            if (hasLicense) {
//                obj = new JSONObject(Objects.requireNonNullElse
//                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
//                                String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
//            } else {
//                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
//            }//Load the config file
//
//            if (obj.getBoolean("mTemperature")) {
//                if (obj.getBoolean("mCelsius")) {
//
//                    mTemp = "Temperature(C): " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");
//
//                } else if (obj.getBoolean("mFahrenheit")) {
//
////                    mTemp = "Temperature(F): " + temperature.getValue();
//                    mTemp = "Temperature(F): " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");
//                }
//            }
//        } catch (Exception e) {
//            FirebaseCrashlytics.getInstance().recordException(e);
//        }
//        mresp = resp.getValue();
//        mSPO2 = "SpO2(%): " + (!TextUtils.isEmpty(spO2.getValue()) ? spO2.getValue() : "");
//        String mComplaint = complaint.getValue();
//
//        //Show only the headers of the complaints in the printed prescription
//        String[] complaints = StringUtils.split(mComplaint, Node.bullet_arrow);
//        mComplaint = "";
//        String colon = ":";
//        String mComplaint_new = "";
//        if (complaints != null) {
//            for (String comp : complaints) {
//                if (!comp.trim().isEmpty()) {
//                    mComplaint = mComplaint + Node.big_bullet + comp.substring(0, comp.indexOf(colon)) + "<br/>";
//
//                }
//            }
//            if (!mComplaint.isEmpty()) {
//                mComplaint = mComplaint.substring(0, mComplaint.length() - 2);
//                mComplaint = mComplaint.replaceAll("<b>", "");
//                mComplaint = mComplaint.replaceAll("</b>", "");
//            }
//        }
//
//        if (mComplaint.contains("Associated symptoms")) {
//            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
//            for (String compla : cc) {
//                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3);
//            }
//        } else {
//
//        }
//
//        if (mComplaint.contains("जुड़े लक्षण")) {
//            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
//            for (String compla : cc) {
//                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
//            }
//        } else {
//
//        }
//
//
//        if (mPatientOpenMRSID == null) {
//            mPatientOpenMRSID = getString(R.string.patient_not_registered);
//        }
//
//        String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
//        String para_close = "</p>";
//
//
//        Calendar today = Calendar.getInstance();
//        Calendar dob = Calendar.getInstance();
//
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = sdf.parse(mPatientDob);
//        dob.setTime(date);
//
//        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
//
//        String rx_web = stringToWeb(rxReturned);
//
//        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n")
//                .replace(Node.bullet, ""));
//
//        //String advice_web = stringToWeb(adviceReturned);
//        String advice_web = "";
//        if (medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
//
//
////        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
////        Log.d("Hyperlink", "hyper_print: " + advice_web);
//            String advice_split = new StringBuilder(medicalAdviceTextView.getText().toString())
//                    .delete(medicalAdviceTextView.getText().toString().indexOf("Start"),
//                            medicalAdviceTextView.getText().toString().lastIndexOf("User") + 6).toString();
//            //lastIndexOf("User") will give index of U of User
//            //so the char this will return is U...here User + 6 will return W eg: User\n\nWatch as +6 will give W
//
////        String advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
////        Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
//            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
//            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
//        } else {
//            advice_web = stringToWeb(medicalAdviceTextView.getText().toString().replace("\n\n", "\n")); //showing advice here...
//            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
//        }
//
//
//        String diagnosis_web = stringToWeb(diagnosisReturned);
//
////        String comments_web = stringToWeb(additionalReturned);
//
//
//        String followUpDateStr = "";
//        if (followUpDate != null && followUpDate.contains(",")) {
//            String[] spiltFollowDate = followUpDate.split(",");
//            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
//                String remainingStr = "";
//                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
//                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
//                }
//                followUpDateStr = parseDateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
//            } else {
//                followUpDateStr = followUpDate;
//            }
//        } else {
//            followUpDateStr = followUpDate;
//        }
//
//        String followUp_web = stringToWeb(followUpDateStr);
//
//        String doctor_web = stringToWeb(doctorName);
//
//        String heading = prescription1;
//        String heading2 = prescription2;
//        String heading3 = "<br/>";
//
//        String bp = mBP;
//        if (bp.equals("/") || bp.equals("null/null")) bp = "";
//
//        String address = mAddress + " " + mCityState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");
//
//        String fam_hist = mFamHist;
//        String pat_hist = mPatHist;
//
//        if (fam_hist.trim().isEmpty()) {
//            fam_hist = "No history of illness in family provided.";
//        } else {
//            fam_hist = fam_hist.replaceAll(Node.bullet, Node.big_bullet);
//        }
//
//        if (pat_hist.trim().isEmpty()) {
//            pat_hist = "No history of patient's illness provided.";
//        }
//
//        // Generate an HTML document on the fly:
//        String fontFamilyFile = "";
//        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
//            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
//            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
//            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
//            }
//        }
//        String font_face = "<style>" +
//                "                @font-face {" +
//                "                    font-family: \"MyFont\";" +
//                fontFamilyFile +
//                "                }" +
//                "            </style>";
//
//        String doctorSign = "";
//        String doctrRegistartionNum = "";
//        // String docDigitallySign = "";
//        String doctorDetailStr = "";
//        if (objClsDoctorDetails != null) {
//            //  docDigitallySign = "Digitally Signed By";
//            doctorSign = objClsDoctorDetails.getTextOfSign();
//
//
//            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " + objClsDoctorDetails.getRegistrationNumber() : "";
//            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
//                    "</div>";
//
////            mDoctorName.setText(doctrRegistartionNum + "\n" + Html.fromHtml(doctorDetailStr));
//        }
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
//                                    "<p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p>" +
//                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
//                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
//                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
//                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
//                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
//                                    "<p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p>" +
//                                    para_open + "%s" + para_close + "<br><br>" +
//                                    "<b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b>" +
//                                    "%s<br>" +
//                                    "<b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b>" +
//                                    "%s<br>" +
//                                    "<b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b>" +
//                                    "%s<br>" +
//                                    "<b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b>" +
//                                    "%s<br>" +
//                                    "<b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b>" +
//                                    "%s<br>" +
//                                    "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
//                                    "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" +
//                                    doctorDetailStr +
//                                    "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" +
//                                    "</div>"
//                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
//                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
//                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
//            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
//        }
//
//
//        /**
//         * +
//         * "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
//         * "%s"
//         */
//
//        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
//        // to the PrintManager
//        mWebView = webView;
//    }

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

//            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());
//
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
//            String fileName = visitUUID +".pdf";
//            File dir = new File(path);
//            if (!dir.exists())
//                dir.mkdirs();

//            File directory = new File(dir, fileName);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
/*
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
            }
*/
      /*      else
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
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
            }*/

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

          /*  PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = visitUUID +".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);*/

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
/*
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
            }
*/
/*            else
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
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
            }*/

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

            /*PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = visitUUID +".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);*/

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
/*
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
            }
*/
           /* else
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
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
            }*/

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

           /* PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = visitUUID +".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);*/

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

            //end...

            //TODO: write different functions for <= Lollipop versions..
/*
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
            }
*/
           /* else
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
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
            }*/
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    new PrintAttributes.Builder().build());

        }


    }

    //print button end


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

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

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

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

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

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

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
            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

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
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    new PrintAttributes.Builder().build());

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
        VisitUtils.endVisit(VisitSummaryActivity.this, visitUuid, patientUuid, followUpDate, encounterVitals, encounterUuidAdultIntial, state, patientName, intentTag, sessionManager1.getAppLanguage());
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
        String[] columns = {"uuid", "value", " conceptuuid", "comment", "creator", "obsservermodifieddate"};

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
//                medHistory = medHistory.replace("\"", "");
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
        String visitSelection = "encounteruuid = ? AND voided != '1'";
        String[] visitArgs = {encounterVitals};
        if (encounterVitals != null) {
            try {
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, "obsservermodifieddate DESC");
                if (visitCursor != null && visitCursor.moveToFirst()) {
                    do {
                        String uuid = visitCursor.getString(visitCursor.getColumnIndex("uuid"));
                        String comment = visitCursor.getString(visitCursor.getColumnIndex("comment"));
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        String creator = visitCursor.getString(visitCursor.getColumnIndex("creator"));
                        String created_date = visitCursor.getString(visitCursor.getColumnIndex("obsservermodifieddate"));
                        if (dbValue.startsWith("{")) {
                            AnswerValue answerValue = new Gson().fromJson(dbValue, AnswerValue.class);
                            parseData(uuid, dbConceptID, LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue(), comment, creator, created_date);
                        } else {
                            parseData(uuid, dbConceptID, dbValue, comment, creator, created_date);
                        }
                        //}
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
        Cursor encountercursor = db.query("tbl_obs", columns, encounterselection, encounterargs, null, null, "obsservermodifieddate DESC");
        try {
            if (encountercursor != null && encountercursor.moveToFirst()) {
                do {
                    String uuid = encountercursor.getString(encountercursor.getColumnIndex("uuid"));
                    String comment = encountercursor.getString(encountercursor.getColumnIndex("comment"));
                    String dbConceptID = encountercursor.getString(encountercursor.getColumnIndex("conceptuuid"));
                    String dbValue = encountercursor.getString(encountercursor.getColumnIndex("value"));
                    String creator = encountercursor.getString(encountercursor.getColumnIndex("creator"));
                    String created_date = encountercursor.getString(encountercursor.getColumnIndex("obsservermodifieddate"));
                    if (dbValue.startsWith("{")) {
                        AnswerValue answerValue = new Gson().fromJson(dbValue, AnswerValue.class);
                        parseData(uuid, dbConceptID, LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue(), comment, creator, created_date);
                    } else {
                        parseData(uuid, dbConceptID, dbValue, comment, creator, created_date);
                    }
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
    private void parseData(String uuid, String concept_id, String value, String comment, String creator, String created_date) {
        switch (concept_id) {
            case UuidDictionary.CURRENT_COMPLAINT: { //Current Complaint
                complaint.setValue(value.replace("?<b>", Node.bullet_arrow));
                break;
            }
            case UuidDictionary.PHYSICAL_EXAMINATION: { //Physical Examination
                phyExam.setValue(value);
                break;
            }
            case UuidDictionary.HEIGHT: {
                height.setValue(value);
                break;
            }
            case UuidDictionary.WEIGHT: {
                weight.setValue(value);
                break;
            }
            case UuidDictionary.PULSE: {
                pulse.setValue(value);
                break;
            }
            case UuidDictionary.SYSTOLIC_BP: {
                bpSys.setValue(value);
                break;
            }
            case UuidDictionary.DIASTOLIC_BP: {
                bpDias.setValue(value);
                break;
            }
            case UuidDictionary.TEMPERATURE: {
                temperature.setValue(value);
                break;
            }
            case UuidDictionary.RESPIRATORY: {
                resp.setValue(value);
                break;
            }
            case UuidDictionary.SPO2: {
                spO2.setValue(value);
                break;
            }
            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {

                if (!newDiagnosisReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newDiagnosisReturned = newDiagnosisReturned + "<br><br>" + "<strike><font color=\\'#000000\\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newDiagnosisReturned = newDiagnosisReturned + "<br><br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (newDiagnosisReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newDiagnosisReturned = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newDiagnosisReturned = value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (!diagnosisReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    diagnosisReturned = diagnosisReturned + "\n" + value;
                } else if (diagnosisReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    diagnosisReturned = value;
                }

                if (diagnosisCard.getVisibility() != View.VISIBLE) {
                    diagnosisCard.setVisibility(View.VISIBLE);
                }
                /*if(sessionManager.getAppLanguage().equalsIgnoreCase("ar"))
                {
                    newDiagnosisReturned = newDiagnosisReturned
                            .replace("Added By", "إضافة من")
                            .replace("Deleted By", "حذف بواسطة");
                }*/
                diagnosisTextView.setText(Html.fromHtml(newDiagnosisReturned));
                Log.e(TAG, diagnosisTextView.getText().toString());
                //checkForDoctor();
                if (LocaleHelper.isArabic(this)) {
                    diagnosisTextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.AID_ORDER_MEDICAL_EQUIP_LOAN: {

               /* if (aidhl_1.getVisibility() != View.VISIBLE)
                    aidhl_1.setVisibility(View.VISIBLE);*/

                TextView textView = createShowTextView();
                fetchDispensed_MedicationAndAid();
                String disaidformattedvalue = (!formatDispensedByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatDispensedByDetails(uuid, true) + "</font>" : "";

                if (!newMedicalEquipLoanAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newMedicalEquipLoanAidOrder = newMedicalEquipLoanAidOrder + "<br><br>" + "<strike><font color=\\'#000000\\'>" + getResources().getString(R.string.aid_order_type1) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newMedicalEquipLoanAidOrder = newMedicalEquipLoanAidOrder + "<br><br>" + getResources().getString(R.string.aid_order_type1) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>" +
                                disaidformattedvalue;
                }

                if (newMedicalEquipLoanAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newMedicalEquipLoanAidOrder = "<strike><font color=\'#000000\'>" + getResources().getString(R.string.aid_order_type1) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" +
                                disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newMedicalEquipLoanAidOrder = getResources().getString(R.string.aid_order_type1) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>" +
                                disaidformattedvalue;

                    if (newMedicalEquipLoanAidOrder != null && !newMedicalEquipLoanAidOrder.isEmpty()) {
                        // ie. there is atleast one item. so add a textview -> show more.
                        //  textView.setText(getString(R.string.show_details));
                        textView.setTag(0);
                        if (aidOrderType1TableRow.getChildCount() == 1)
                            aidOrderType1TableRow.addView(textView);

                        // ie. value is present for this Aid_1 field.
                        // if (newMedicalEquipLoanAidOrder.contains("Added By")) {
                        String a[] = newMedicalEquipLoanAidOrder.split("<br>");
                        aid1 = a[0];
                        Log.d(TAG, "parseData: " + aid1);
                        // aid1 = newMedicalEquipLoanAidOrder;
                        //   }
                        fetchDispensed_MedicationAndAid();    // so that it runs only the first time and fetches all the values at once.
                    }

                }

                if (!newMedicalEquipLoanAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newMedicalEquipLoanAidOrderPresc = newMedicalEquipLoanAidOrderPresc + "\n" + getResources().getString(R.string.aid_order_type1) + " " + value;
                } else if (newMedicalEquipLoanAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newMedicalEquipLoanAidOrderPresc = getResources().getString(R.string.aid_order_type1) + " " + value;
                }

                if (!aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = aidOrderReturned + "\n" + getResources().getString(R.string.aid_order_type1) + " " + value;
                } else if (aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = getResources().getString(R.string.aid_order_type1) + " " + value;
                }

                Log.d("aidOrder", aidOrderReturned);
                if (aidOrderCard.getVisibility() != View.VISIBLE) {
                    aidOrderCard.setVisibility(View.VISIBLE);
                }
                if (!value.isEmpty() && !value.trim().equalsIgnoreCase("")) {
                    aidOrderType1TextView.setVisibility(View.VISIBLE);
                    aidOrderType1TableRow.setVisibility(View.VISIBLE);
                    if (value.contains("Others||"))
                        value = value.replace("Others||", "Others - ");
                }

                if (!newMedicalEquipLoanAidOrder.isEmpty() && !newMedicalEquipLoanAidOrder.trim().equalsIgnoreCase("")) {
                    if (newMedicalEquipLoanAidOrder.contains("Others||"))
                        newMedicalEquipLoanAidOrder = newMedicalEquipLoanAidOrder.replace("Others||", "Others - ");
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("ar") && newMedicalEquipLoanAidOrder.contains("||"))
                        newMedicalEquipLoanAidOrder = newMedicalEquipLoanAidOrder.replace("||", " ");
                }

                aidOrderType1TextView.setText(Html.fromHtml(aid1));

                textView.setOnClickListener(v -> {
                    if (textView.getTag() != null) {
                        if (textView.getTag().equals(0)) {
                            textView.setText(getString(R.string.hide_details));
                            textView.setTag(1);
                            aidOrderType1TextView.setText(Html.fromHtml(newMedicalEquipLoanAidOrder));
                        } else {
                            textView.setText(getString(R.string.show_details));
                            textView.setTag(0);
                            aidOrderType1TextView.setText(Html.fromHtml(aid1));
                        }
                    }
                });

                if (LocaleHelper.isArabic(this)) {
                    aidOrderType1TextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.AID_ORDER_FREE_MEDICAL_EQUIP: {

                if (aidhl_1.getVisibility() != View.VISIBLE)
                    aidhl_1.setVisibility(View.VISIBLE);

                TextView textView = createShowTextView();
                fetchDispensed_MedicationAndAid();
                String disaidformattedvalue = (!formatDispensedByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatDispensedByDetails(uuid, true) + "</font>" : "";

                if (!newFreeMedicalEquipAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newFreeMedicalEquipAidOrder = newFreeMedicalEquipAidOrder + "<br><br>" + "<strike><font color=\\'#000000\\'>" + getResources().getString(R.string.aid_order_type2) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newFreeMedicalEquipAidOrder = newFreeMedicalEquipAidOrder + "<br><br>" + getResources().getString(R.string.aid_order_type2) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;
                }

                if (newFreeMedicalEquipAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newFreeMedicalEquipAidOrder = "<strike><font color=\'#000000\'>" + getResources().getString(R.string.aid_order_type2) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newFreeMedicalEquipAidOrder = getResources().getString(R.string.aid_order_type2) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;

                    if (newFreeMedicalEquipAidOrder != null && !newFreeMedicalEquipAidOrder.isEmpty()) {
                        // ie. there is atleast one item. so add a textview -> show more.
                        //  textView.setText(getString(R.string.show_details));
                        textView.setTag(0);
                        if (aidOrderType2TableRow.getChildCount() == 1)
                            aidOrderType2TableRow.addView(textView);

                        // ie. value is present for this Aid_1 field.
                        //  if (newFreeMedicalEquipAidOrder.contains("Added By")) {
                        String a[] = newFreeMedicalEquipAidOrder.split("<br>");
                        aid2 = a[0];
                        Log.d(TAG, "parseData: " + aid2);
                        // aid2 = newMedicalEquipLoanAidOrder;
                        // }
                        fetchDispensed_MedicationAndAid();    // so that it runs only the first time and fetches all the values at once.
                    }

                }

                if (!newFreeMedicalEquipAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newFreeMedicalEquipAidOrderPresc = newFreeMedicalEquipAidOrderPresc + ",\n" + getResources().getString(R.string.aid_order_type2) + " " + value;
                } else if (newFreeMedicalEquipAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newFreeMedicalEquipAidOrderPresc = getResources().getString(R.string.aid_order_type2) + " " + value;
                }

                if (!aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = aidOrderReturned + "\n" + getResources().getString(R.string.aid_order_type2) + " " + value;
                } else if (aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = getResources().getString(R.string.aid_order_type2) + " " + value;
                }
                Log.d("aidOrder", aidOrderReturned);
                if (aidOrderCard.getVisibility() != View.VISIBLE) {
                    aidOrderCard.setVisibility(View.VISIBLE);
                }
                if (!value.isEmpty() && !value.trim().equalsIgnoreCase("")) {
                    aidOrderType2TextView.setVisibility(View.VISIBLE);
                    aidOrderType2TableRow.setVisibility(View.VISIBLE);
                    if (value.contains("Others||"))
                        value = value.replace("Others||", "Others - ");
                }

                if (!newFreeMedicalEquipAidOrder.isEmpty() && !newFreeMedicalEquipAidOrder.trim().equalsIgnoreCase("")) {
                    if (newFreeMedicalEquipAidOrder.contains("Others||"))
                        newFreeMedicalEquipAidOrder = newFreeMedicalEquipAidOrder.replace("Others||", "Others - ");
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("ar") && newFreeMedicalEquipAidOrder.contains("||"))
                        newFreeMedicalEquipAidOrder = newFreeMedicalEquipAidOrder.replace("||", " ");
                }

                //  aidOrderType2TextView.setText(Html.fromHtml(newFreeMedicalEquipAidOrder));
                aidOrderType2TextView.setText(Html.fromHtml(aid2));
                textView.setOnClickListener(v -> {
                    if (textView.getTag() != null) {
                        if (textView.getTag().equals(0)) {
                            textView.setText(getString(R.string.hide_details));
                            textView.setTag(1);
                            aidOrderType2TextView.setText(Html.fromHtml(newFreeMedicalEquipAidOrder));
                        } else {
                            textView.setText(getString(R.string.show_details));
                            textView.setTag(0);
                            aidOrderType2TextView.setText(Html.fromHtml(aid2));
                        }
                    }
                });

                if (LocaleHelper.isArabic(this)) {
                    aidOrderType2TextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.AID_ORDER_COVER_MEDICAL_EXPENSE: {

                if (aidhl_2.getVisibility() != View.VISIBLE)
                    aidhl_2.setVisibility(View.VISIBLE);

                TextView textView = createShowTextView();
                fetchDispensed_MedicationAndAid();
                String disaidformattedvalue = (!formatDispensedByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatDispensedByDetails(uuid, true) + "</font>" : "";

                if (!newCoverMedicalExpenseAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newCoverMedicalExpenseAidOrder = newCoverMedicalExpenseAidOrder + "<br><br>" + "<strike><font color=\\'#000000\\'>" + getResources().getString(R.string.aid_order_type3) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newCoverMedicalExpenseAidOrder = newCoverMedicalExpenseAidOrder + "<br><br>" + getResources().getString(R.string.aid_order_type3) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;
                }

                if (newCoverMedicalExpenseAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newCoverMedicalExpenseAidOrder = "<strike><font color=\'#000000\'>" + getResources().getString(R.string.aid_order_type3) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newCoverMedicalExpenseAidOrder = getResources().getString(R.string.aid_order_type3) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;

                    if (newCoverMedicalExpenseAidOrder != null && !newCoverMedicalExpenseAidOrder.isEmpty()) {
                        // ie. there is atleast one item. so add a textview -> show more.
                        //  textView.setText(getString(R.string.show_details));
                        textView.setTag(0);
                        if (aidOrderType3TableRow.getChildCount() == 1)
                            aidOrderType3TableRow.addView(textView);

                        // ie. value is present for this Aid_1 field.
                        //  if (newFreeMedicalEquipAidOrder.contains("Added By")) {
                        String a[] = newCoverMedicalExpenseAidOrder.split("<br>");
                        aid3 = a[0];
                        Log.d(TAG, "parseData: " + aid3);
                        // aid2 = newMedicalEquipLoanAidOrder;
                        // }
                        fetchDispensed_MedicationAndAid();    // so that it runs only the first time and fetches all the values at once.
                    }

                }

                if (!newCoverMedicalExpenseAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newCoverMedicalExpenseAidOrderPresc = newCoverMedicalExpenseAidOrderPresc + "\n" + getResources().getString(R.string.aid_order_type3) + " " + value;
                } else if (newCoverMedicalExpenseAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newCoverMedicalExpenseAidOrderPresc = getResources().getString(R.string.aid_order_type3) + " " + value;
                }

                if (!aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = aidOrderReturned + ",\n" + getResources().getString(R.string.aid_order_type3) + " " + value;
                } else if (aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = getResources().getString(R.string.aid_order_type3) + " " + value;
                }
                Log.d("aidOrder", aidOrderReturned);
                if (aidOrderCard.getVisibility() != View.VISIBLE) {
                    aidOrderCard.setVisibility(View.VISIBLE);
                }
                if (!value.isEmpty() && !value.trim().equalsIgnoreCase("")) {
                    aidOrderType3TextView.setVisibility(View.VISIBLE);
                    aidOrderType3TableRow.setVisibility(View.VISIBLE);
                }

                if (!newCoverMedicalExpenseAidOrder.isEmpty() && !newCoverMedicalExpenseAidOrder.trim().equalsIgnoreCase("")) {
                    if (newCoverMedicalExpenseAidOrder.contains("Others||"))
                        newCoverMedicalExpenseAidOrder = newCoverMedicalExpenseAidOrder.replace("Others||", "Others - ");
                }

                //  aidOrderType3TextView.setText(Html.fromHtml(newCoverMedicalExpenseAidOrder));
                aidOrderType3TextView.setText(Html.fromHtml(aid3));
                textView.setOnClickListener(v -> {
                    if (textView.getTag() != null) {
                        if (textView.getTag().equals(0)) {
                            textView.setText(getString(R.string.hide_details));
                            textView.setTag(1);
                            aidOrderType3TextView.setText(Html.fromHtml(newCoverMedicalExpenseAidOrder));
                        } else {
                            textView.setText(getString(R.string.show_details));
                            textView.setTag(0);
                            aidOrderType3TextView.setText(Html.fromHtml(aid3));
                        }
                    }
                });

                if (LocaleHelper.isArabic(this)) {
                    aidOrderType3TextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.AID_ORDER_COVER_SURGICAL_EXPENSE: {

                if (aidhl_3.getVisibility() != View.VISIBLE)
                    aidhl_3.setVisibility(View.VISIBLE);

                TextView textView = createShowTextView();
                fetchDispensed_MedicationAndAid();
                String disaidformattedvalue = (!formatDispensedByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatDispensedByDetails(uuid, true) + "</font>" : "";

                if (!newCoverSurgicalExpenseAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newCoverSurgicalExpenseAidOrder = newCoverSurgicalExpenseAidOrder + "<br><br>" + "<strike><font color=\\'#000000\\'>" + getResources().getString(R.string.aid_order_type4) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newCoverSurgicalExpenseAidOrder = newCoverSurgicalExpenseAidOrder + "<br><br>" + getResources().getString(R.string.aid_order_type4) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;
                }

                if (newCoverSurgicalExpenseAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newCoverSurgicalExpenseAidOrder = "<strike><font color=\'#000000\'>" + getResources().getString(R.string.aid_order_type4) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newCoverSurgicalExpenseAidOrder = getResources().getString(R.string.aid_order_type4) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;

                    if (newCoverSurgicalExpenseAidOrder != null && !newCoverSurgicalExpenseAidOrder.isEmpty()) {
                        // ie. there is atleast one item. so add a textview -> show more.
                        //  textView.setText(getString(R.string.show_details));
                        textView.setTag(0);
                        if (aidOrderType4TableRow.getChildCount() == 1)
                            aidOrderType4TableRow.addView(textView);

                        // ie. value is present for this Aid_1 field.
                        //  if (newFreeMedicalEquipAidOrder.contains("Added By")) {
                        String a[] = newCoverSurgicalExpenseAidOrder.split("<br>");
                        aid4 = a[0];
                        Log.d(TAG, "parseData: " + aid4);
                        // aid2 = newMedicalEquipLoanAidOrder;
                        // }
                        fetchDispensed_MedicationAndAid();    // so that it runs only the first time and fetches all the values at once.
                    }
                }

                if (!newCoverSurgicalExpenseAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newCoverSurgicalExpenseAidOrderPresc = newCoverSurgicalExpenseAidOrderPresc + "\n" + getResources().getString(R.string.aid_order_type4) + " " + value;
                } else if (newCoverSurgicalExpenseAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newCoverSurgicalExpenseAidOrderPresc = getResources().getString(R.string.aid_order_type4) + " " + value;
                }

                if (!aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = aidOrderReturned + "\n" + getResources().getString(R.string.aid_order_type4) + " " + value;
                } else if (aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = getResources().getString(R.string.aid_order_type4) + " " + value;
                }
                Log.d("aidOrder", aidOrderReturned);

                if (aidOrderCard.getVisibility() != View.VISIBLE) {
                    aidOrderCard.setVisibility(View.VISIBLE);
                }
                if (!value.isEmpty() && !value.trim().equalsIgnoreCase("")) {
                    aidOrderType4TextView.setVisibility(View.VISIBLE);
                    aidOrderType4TableRow.setVisibility(View.VISIBLE);
                }

                if (!newCoverSurgicalExpenseAidOrder.isEmpty() && !newCoverSurgicalExpenseAidOrder.trim().equalsIgnoreCase("")) {
                    if (newCoverSurgicalExpenseAidOrder.contains("Others||"))
                        newCoverSurgicalExpenseAidOrder = newCoverSurgicalExpenseAidOrder.replace("Others||", "Others - ");
                }

                //  aidOrderType4TextView.setText(Html.fromHtml(newCoverSurgicalExpenseAidOrder));
                aidOrderType4TextView.setText(Html.fromHtml(aid4));
                textView.setOnClickListener(v -> {
                    if (textView.getTag() != null) {
                        if (textView.getTag().equals(0)) {
                            textView.setText(getString(R.string.hide_details));
                            textView.setTag(1);
                            aidOrderType4TextView.setText(Html.fromHtml(newCoverSurgicalExpenseAidOrder));
                        } else {
                            textView.setText(getString(R.string.show_details));
                            textView.setTag(0);
                            aidOrderType4TextView.setText(Html.fromHtml(aid4));
                        }
                    }
                });

                if (LocaleHelper.isArabic(this)) {
                    aidOrderType4TextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.AID_ORDER_CASH_ASSISTANCE: {

                if (aidhl_4.getVisibility() != View.VISIBLE)
                    aidhl_4.setVisibility(View.VISIBLE);

                TextView textView = createShowTextView();
                fetchDispensed_MedicationAndAid();
                String disaidformattedvalue = (!formatDispensedByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatDispensedByDetails(uuid, true) + "</font>" : "";

                if (!newCashAssistanceExpenseAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newCashAssistanceExpenseAidOrder = newCashAssistanceExpenseAidOrder + "<br><br>" + "<strike><font color=\\'#000000\\'>" + getResources().getString(R.string.aid_order_type5) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newCashAssistanceExpenseAidOrder = newCashAssistanceExpenseAidOrder + "<br><br>" + getResources().getString(R.string.aid_order_type5) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;
                }

                if (newCashAssistanceExpenseAidOrder.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newCashAssistanceExpenseAidOrder = "<strike><font color=\'#000000\'>" + getResources().getString(R.string.aid_order_type5) + " " + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + disaidformattedvalue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newCashAssistanceExpenseAidOrder = getResources().getString(R.string.aid_order_type5) + " " + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + disaidformattedvalue;

                    if (newCashAssistanceExpenseAidOrder != null && !newCashAssistanceExpenseAidOrder.isEmpty()) {
                        // ie. there is atleast one item. so add a textview -> show more.
                        //  textView.setText(getString(R.string.show_details));
                        textView.setTag(0);
                        if (aidOrderType5TableRow.getChildCount() == 1)
                            aidOrderType5TableRow.addView(textView);

                        // ie. value is present for this Aid_1 field.
                        //  if (newFreeMedicalEquipAidOrder.contains("Added By")) {
                        String a[] = newCashAssistanceExpenseAidOrder.split("<br>");
                        aid5 = a[0];
                        Log.d(TAG, "parseData: " + aid5);
                        // aid2 = newMedicalEquipLoanAidOrder;
                        // }
                        fetchDispensed_MedicationAndAid();    // so that it runs only the first time and fetches all the values at once.
                    }
                }

                if (!newCashAssistanceExpenseAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newCashAssistanceExpenseAidOrderPresc = newCashAssistanceExpenseAidOrderPresc + "\n" + getResources().getString(R.string.aid_order_type5) + " " + value;
                } else if (newCashAssistanceExpenseAidOrderPresc.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    newCashAssistanceExpenseAidOrderPresc = getResources().getString(R.string.aid_order_type5) + " " + value;
                }

                if (!aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = aidOrderReturned + ",\n" + getResources().getString(R.string.aid_order_type5) + " " + value;
                } else if (aidOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    aidOrderReturned = getResources().getString(R.string.aid_order_type5) + " " + value;
                }

                Log.d("aidOrder", aidOrderReturned);
                if (aidOrderCard.getVisibility() != View.VISIBLE) {
                    aidOrderCard.setVisibility(View.VISIBLE);
                }
                if (!value.isEmpty() && !value.trim().equalsIgnoreCase("")) {
                    aidOrderType5TextView.setVisibility(View.VISIBLE);
                    aidOrderType5TableRow.setVisibility(View.VISIBLE);
                }
                if (!newCashAssistanceExpenseAidOrder.isEmpty() && !newCashAssistanceExpenseAidOrder.trim().equalsIgnoreCase("")) {
                    if (newCashAssistanceExpenseAidOrder.contains("Others||"))
                        newCashAssistanceExpenseAidOrder = newCashAssistanceExpenseAidOrder.replace("Others||", "Others - ");
                }

                //  aidOrderType5TextView.setText(Html.fromHtml(newCashAssistanceExpenseAidOrder));
                aidOrderType5TextView.setText(Html.fromHtml(aid5));
                textView.setOnClickListener(v -> {
                    if (textView.getTag() != null) {
                        if (textView.getTag().equals(0)) {
                            textView.setText(getString(R.string.hide_details));
                            textView.setTag(1);
                            aidOrderType5TextView.setText(Html.fromHtml(newCashAssistanceExpenseAidOrder));
                        } else {
                            textView.setText(getString(R.string.show_details));
                            textView.setTag(0);
                            aidOrderType5TextView.setText(Html.fromHtml(aid5));
                        }
                    }
                });


                if (LocaleHelper.isArabic(this)) {
                    aidOrderType5TextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.JSV_MEDICATIONS: {

                Log.e(TAG, "parseData: JSV_MEDICATIONS=>" + value);
                if (value.contains("\n"))
                    value = value.replace("\n", "<br>");

                fetchDispensed_MedicationAndAid();
                fetchAdministered_Medication();

                String dispenseMedicationValue = (!formatDispensedByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatDispensedByDetails(uuid, true) + "</font>" : "";

                String administerMedicationValue = (!formatAdministeredByDetails(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + formatAdministeredByDetails(uuid, true) + "</font>" : "";


                if (!newRxReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newRxReturned = /*newRxReturned +*/ "<br>" + "<strike><font color=\\'#000000\\'>" + value +
                                "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + dispenseMedicationValue + administerMedicationValue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newRxReturned = /*newRxReturned + */ "<br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + dispenseMedicationValue + administerMedicationValue;
                }

                if (newRxReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newRxReturned = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + dispenseMedicationValue + administerMedicationValue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newRxReturned = value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>"
                                + dispenseMedicationValue + administerMedicationValue;
                }

                if (!rxReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    rxReturned = rxReturned + "\n" + value;
                } else if (rxReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    rxReturned = value;
                }
                Log.e(TAG, "parseData: rxReturn=>" + rxReturned);
                if (prescriptionCard.getVisibility() != View.VISIBLE) {
                    prescriptionCard.setVisibility(View.VISIBLE);
                }

                // show details option - start
                TextView textView = createTextView();
                textView.setTag(R.id.tl_prescribed_medications, newRxReturned);
                textView.setTag(newRxReturned);

                String a[] = textView.getTag().toString().split(getResources().getString(R.string.added_by));
                textView.setText(Html.fromHtml(a[0].substring(0, a[0].lastIndexOf("<br>"))));
                Log.d(TAG, "parseData: med_txt: \n" + newRxReturned + "\n -------- \n" + textView.getText().toString());

                TextView show_textView = createShowTextView();
                show_textView.setTag(textView);
                show_textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int c = showMoreAndHideContent((TextView) view.getTag(), R.id.tl_prescribed_medications);
                        if (c == 1)
                            show_textView.setText(getString(R.string.hide_details));
                        else
                            show_textView.setText(getString(R.string.show_details));
                    }
                });

                if (LocaleHelper.isArabic(this)) {
                    textView.setGravity(Gravity.END);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);  // Had to add this as the text direction was breaking for some text.
                }

                if (tl_prescribed_medications.getChildCount() > 0)
                    tl_prescribed_medications.addView(showDividerLine());

                tl_prescribed_medications.addView(textView);
                tl_prescribed_medications.addView(show_textView);

                Log.d(TAG, "parseData: med: " + tl_prescribed_medications.getChildCount() + "\n" + textView.getText().toString());
                // show details option - end

                break;
            }
            case UuidDictionary.MEDICAL_ADVICE: {

                if (!newAdviceReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newAdviceReturned = newAdviceReturned + "<br><br>" + "<strike><font color=\\'#000000\\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newAdviceReturned = newAdviceReturned + "<br><br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (newAdviceReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newAdviceReturned = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty()) {
                        newAdviceReturned = newAdviceReturned + "<br><br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                    }
                }

                if (!adviceReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    adviceReturned = adviceReturned + "\n" + value;
                } else if (adviceReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    adviceReturned = value;
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

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                adviceReturned = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
//                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                newAdviceReturned = newAdviceReturned.replaceAll("\n", "<br><br>");

                medicalAdviceTextView.setText(Html.fromHtml(newAdviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                if (LocaleHelper.isArabic(this)) {
                    medicalAdviceTextView.setGravity(Gravity.END);
                }
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (value.contains("\n"))
                    value = value.replace("\n", "<br>");

                fetchTest_Collected_Received_Data(ENCOUNTER_TEST_COLLECT);
                fetchTest_Collected_Received_Data(ENCOUNTER_TEST_RECEIVE);

                String collectedByValue = (!format_TestCollectReceive_Details(uuid, true).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + format_TestCollectReceive_Details(uuid, true) + "</font>" : "";

                String receiveByValue = (!format_TestCollectReceive_Details(uuid, false).isEmpty())
                        ? "<br><font color=\'#2F1E91\'>" + format_TestCollectReceive_Details(uuid, false) + "</font>" : "";

                if (!newTestsReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newTestsReturned = /*newTestsReturned + "<br>" +*/ "<br>" + "<strike><font color=\\'#000000\\'>" + value +
                                "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>"
                                + collectedByValue + receiveByValue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";

                    else if (comment == null || comment.trim().isEmpty())
                        newTestsReturned = /*newTestsReturned + "<br>" +*/ "<br>" + value + "<br><font color=\'#2F1E91\'>" +
                                formatCreatorDetails(creator, created_date, "") + "</font>" +
                                collectedByValue + receiveByValue;
                }

                if (newTestsReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newTestsReturned = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" +
                                formatCreatorDetails(creator, created_date, comment) + "</font>" +
                                collectedByValue + receiveByValue + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";

                    else if (comment == null || comment.trim().isEmpty())
                        newTestsReturned = value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>" +
                                collectedByValue + receiveByValue;
                }

                if (!testsReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    testsReturned = testsReturned + "\n" + Node.bullet + value;
                } else if (testsReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    testsReturned = Node.bullet + " " + value;
                }

                if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }

                // show details option - start
                TextView textView = createTextView();
                textView.setTag(R.id.ll_test, newTestsReturned);
                textView.setTag(newTestsReturned);

                String a[] = textView.getTag().toString().split(getResources().getString(R.string.added_by));
                textView.setText(Html.fromHtml(a[0].substring(0, a[0].lastIndexOf("<br>"))));
                Log.d(TAG, "parseData: test_txt: \n" + newTestsReturned + "\n -------- \n" + textView.getText().toString());

                TextView show_textView = createShowTextView();
                show_textView.setTag(textView);
                show_textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int c = showMoreAndHideContent((TextView) view.getTag(), R.id.ll_test);
                        if (c == 1)
                            show_textView.setText(getString(R.string.hide_details));
                        else
                            show_textView.setText(getString(R.string.show_details));
                    }
                });

                if (LocaleHelper.isArabic(this)) {
                    textView.setGravity(Gravity.END);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);  // Had to add this as the text direction was breaking for some text.
                }

                if (ll_test.getChildCount() > 0)
                    ll_test.addView(showDividerLine());

                ll_test.addView(textView);
                ll_test.addView(show_textView);

                Log.d(TAG, "parseData: test: " + ll_test.getChildCount() + "\n" + textView.getText().toString());

               /* if (LocaleHelper.isArabic(this)) {
                    requestedTestsTextView.setGravity(Gravity.END);
                }

                requestedTestsTextView.setText(Html.fromHtml(newTestsReturned));*/
                // show details option - end

                //checkForDoctor();
                break;
            }
            case UuidDictionary.ADDITIONAL_COMMENTS: {
                if (!newAdditionalReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newAdditionalReturned = newAdditionalReturned + "<br><br>" + "<strike><font color=\\'#000000\\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newAdditionalReturned = newAdditionalReturned + "<br><br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (newAdditionalReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newAdditionalReturned = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newAdditionalReturned = value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (!additionalReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    additionalReturned = additionalReturned + "\n" + value;
                } else if (additionalReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    additionalReturned = value;
                }

                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
                    additionalCommentsCard.setVisibility(View.VISIBLE);
                }
                additionalCommentsTextView.setText(Html.fromHtml(newAdditionalReturned));
                //checkForDoctor();
                break;
            }
            case UuidDictionary.DISCHARGE_ORDER: {

                if (!newDischargeOrderReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newDischargeOrderReturned = newDischargeOrderReturned + "<br><br>" + "<strike><font color=\\'#000000\\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newDischargeOrderReturned = newDischargeOrderReturned + "<br><br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (newDischargeOrderReturned.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newDischargeOrderReturned = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newDischargeOrderReturned = value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (!dischargeOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    dischargeOrderReturned = dischargeOrderReturned + "\n" + value;
                } else if (dischargeOrderReturned.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    dischargeOrderReturned = value;
                }

                if (dischargeOrderCard.getVisibility() != View.VISIBLE) {
                    dischargeOrderCard.setVisibility(View.VISIBLE);
                }
                dischargeOrderTextView.setText(Html.fromHtml(newDischargeOrderReturned));
                //checkForDoctor();
                break;
            }
            case UuidDictionary.FOLLOW_UP_VISIT: {

                if (!newFollowUpDate.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newFollowUpDate = newFollowUpDate + "<br><br>" + "<strike><font color=\\'#000000\\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newFollowUpDate = newFollowUpDate + "<br><br>" + value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (newFollowUpDate.isEmpty()) {
                    if (comment != null && !comment.trim().isEmpty())
                        newFollowUpDate = "<strike><font color=\'#000000\'>" + value + "</font></strike>" + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, comment) + "</font>" + "<br><font color=\'#ff0000\'>" + formatComment(comment) + "</font>";
                    else if (comment == null || comment.trim().isEmpty())
                        newFollowUpDate = value + "<br><font color=\'#2F1E91\'>" + formatCreatorDetails(creator, created_date, "") + "</font>";
                }

                if (!followUpDate.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    followUpDate = followUpDate + "\n" + value;
                } else if (followUpDate.isEmpty() && (comment == null || comment.trim().isEmpty())) {
                    followUpDate = value;
                }

                if (followUpDateCard.getVisibility() != View.VISIBLE) {
                    followUpDateCard.setVisibility(View.VISIBLE);
                }
                followUpDateTextView.setText(Html.fromHtml(sessionManager1.getAppLanguage().equalsIgnoreCase("ar") ? en_ar_dob(newFollowUpDate) : newFollowUpDate));
                //checkForDoctor();
                break;
            }
            default:
                Log.i(TAG, "parseData: " + value);
                break;

        }
    }

    private View showDividerLine() {
        View view = new View(VisitSummaryActivity.this);
        view.setBackgroundColor(getResources().getColor(R.color.divider));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        params.topMargin = 50;
        view.setLayoutParams(params);
        return view;
    }

    private int showMoreAndHideContent(TextView contentTextView, int id) {
        int tag = 0;
        String value = contentTextView.getTag(id).toString();
        String[] a = contentTextView.getTag().toString().split(getResources().getString(R.string.added_by));
        Log.d(TAG, "showMoreAndHideContent: " + value + "\n" + a[0] + "\n" + "--------------");

        if (contentTextView.getTag().toString().contains(getResources().getString(R.string.added_by))) {
            contentTextView.setText(Html.fromHtml((String) contentTextView.getTag()));
            contentTextView.setTag(a[0]);
            tag = 1;
        } else {
            contentTextView.setText(Html.fromHtml(a[0].substring(0, a[0].lastIndexOf("<br>"))));
            contentTextView.setTag(value);
            tag = 0;
        }

        return tag;
    }

    private TextView createShowTextView() {
        TextView textView = new MaterialTextView(VisitSummaryActivity.this);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textView.setTextColor(getResources().getColor(R.color.intro_next));
        textView.setPadding(0, 10, 0, 10);
        textView.setText(getString(R.string.show_details));
        return textView;
    }

    private TextView createTextView() {
        TextView textView = new MaterialTextView(VisitSummaryActivity.this);
        textView.setTextSize(16.0f);
        textView.setId(View.generateViewId());
        return textView;
    }

    private void fetchAdministered_Medication() {
        Log.d(TAG, "fetchDispensedAid: " + visitUuid);
        List<String> encounterListByVisitUUID = getEncounterListByVisitUUID(visitUuid, UuidDictionary.ENCOUNTER_ADMINISTER);
        if (encounterListByVisitUUID != null && encounterListByVisitUUID.size() > 0) {
            if (update_medUuidAdministeredList != null && update_medUuidAdministeredList.size() > 0)
                update_medUuidAdministeredList.clear();

            for (int i = 0; i < encounterListByVisitUUID.size(); i++) {
                encounterAdminister = encounterListByVisitUUID.get(i);
                Log.d(TAG, "encounterAdminister: " + encounterAdminister);  //
                if (!encounterAdminister.isEmpty()) {
                    try {

                        //  MedicationAidModel medModel = ObsDAO.getObsValue(encounterDispense, UuidDictionary.OBS_DISPENSE_MEDICATION);    // 27f6b6df-d3a5-47b6-8a36-5843ed204794
                        update_medUuidAdministeredList.addAll(ObsDAO.getObsDispenseAdministerData(encounterAdminister, UuidDictionary.OBS_ADMINISTER_MEDICATION));    // 27f6b6df-d3a5-47b6-8a36-5843ed204794

                       /* //  MedicationAidModel aidModel = ObsDAO.getObsValue(encounterDispense, UuidDictionary.OBS_DISPENSE_AID);
                        update_aidUuidList.addAll(ObsDAO.getObsDispenseAdministerData(encounterDispense, UuidDictionary.OBS_DISPENSE_AID));
*/
                    } catch (DAOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            Log.d(TAG, "fetchAdministered_Medication: " + String.valueOf(update_medUuidAdministeredList.size()));
        }
    }


    private void fetchDispensed_MedicationAndAid() {
        Log.d(TAG, "fetchDispensedAid: " + visitUuid);
        List<String> encounterListByVisitUUID = getEncounterListByVisitUUID(visitUuid, UuidDictionary.ENCOUNTER_DISPENSE);
        if (encounterListByVisitUUID != null && encounterListByVisitUUID.size() > 0) {

            if (update_medUuidDispenseList != null && update_medUuidDispenseList.size() > 0)
                update_medUuidDispenseList.clear();

            if (update_aidUuidList != null && update_aidUuidList.size() > 0)
                update_aidUuidList.clear();

            for (int i = 0; i < encounterListByVisitUUID.size(); i++) {
                encounterDispense = encounterListByVisitUUID.get(i);
                Log.d(TAG, "encounterDispense: " + encounterDispense);  //
                if (!encounterDispense.isEmpty()) {
                    try {

                        //  MedicationAidModel medModel = ObsDAO.getObsValue(encounterDispense, UuidDictionary.OBS_DISPENSE_MEDICATION);    // 27f6b6df-d3a5-47b6-8a36-5843ed204794
                        update_medUuidDispenseList.addAll(ObsDAO.getObsDispenseAdministerData(encounterDispense, UuidDictionary.OBS_DISPENSE_MEDICATION));    // 27f6b6df-d3a5-47b6-8a36-5843ed204794

                        //  MedicationAidModel aidModel = ObsDAO.getObsValue(encounterDispense, UuidDictionary.OBS_DISPENSE_AID);
                        update_aidUuidList.addAll(ObsDAO.getObsDispenseAdministerData(encounterDispense, UuidDictionary.OBS_DISPENSE_AID));

                    } catch (DAOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    private void fetchTest_Collected_Received_Data(String ENCOUNTER_TYPE_UUID) {
        Log.d(TAG, "fetchTest_Collected_Received_Data: " + visitUuid);
        List<String> encounterListByVisitUUID = getEncounterListByVisitUUID(visitUuid, ENCOUNTER_TYPE_UUID);

        if (encounterListByVisitUUID.size() > 0) {
            // collected test - start
            if (ENCOUNTER_TYPE_UUID.equalsIgnoreCase(ENCOUNTER_TEST_COLLECT)) {
                if (update_collectedTest_UUIDList != null && update_collectedTest_UUIDList.size() > 0)
                    update_collectedTest_UUIDList.clear();

                for (int i = 0; i < encounterListByVisitUUID.size(); i++) {
                    encounterTestCollect = encounterListByVisitUUID.get(i);
                    Log.d(TAG, "encounterTestCollect: " + encounterTestCollect);

                    if (!encounterTestCollect.isEmpty()) {
                        try {
                            update_collectedTest_UUIDList.addAll(ObsDAO.getObsDispenseAdministerData
                                    (encounterTestCollect, UuidDictionary.OBS_TEST_COLLECT));    // COLLECT = 4476c831-0dd7-4677-94e2-509ddda03f01
                        } catch (DAOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            // collected test - end

            // received test - start
            if (ENCOUNTER_TYPE_UUID.equalsIgnoreCase(ENCOUNTER_TEST_RECEIVE)) {
                if (update_receivedTest_UUIDList != null && update_receivedTest_UUIDList.size() > 0)
                    update_receivedTest_UUIDList.clear();

                for (int i = 0; i < encounterListByVisitUUID.size(); i++) {
                    encounterTestReceive = encounterListByVisitUUID.get(i);
                    Log.d(TAG, "encounterTestReceive: " + encounterTestReceive);

                    if (!encounterTestReceive.isEmpty()) {
                        try {
                            update_receivedTest_UUIDList.addAll(ObsDAO.getObsDispenseAdministerData
                                    (encounterTestReceive, UuidDictionary.OBS_TEST_RECEIVE));    // RECEIVE = 769ef1e2-9e3d-439a-a3d7-d0190636110e
                        } catch (DAOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            // receive test - end

        }
    }

    private String format_TestCollectReceive_Details(String uuid, boolean isTestCollectBy) {
        String obsformat = "";

        if (isTestCollectBy) {  // TEST COLLECTED BY
            if (update_collectedTest_UUIDList != null && update_collectedTest_UUIDList.size() > 0) {
                for (int i = 0; i < update_collectedTest_UUIDList.size(); i++) {
                    MedicationModel medicationModel = new Gson().fromJson(update_collectedTest_UUIDList.get(i).getValue(), MedicationModel.class);
                    if (medicationModel.getMedicationUuidList() != null && medicationModel.getMedicationUuidList().contains(uuid)) {
                        String valueTimeStamp = "";
                        if (update_collectedTest_UUIDList.get(i).getCreatedDate() != null)
                            valueTimeStamp = getValueTimeStamp(update_collectedTest_UUIDList.get(i).getCreatedDate());
                        else {
                            AidModel aidModel = new Gson().fromJson(update_collectedTest_UUIDList.get(i).getValue(), AidModel.class);
                            valueTimeStamp = getValueTimeStamp(aidModel.getDateTime());
                        }
                        Log.d(TAG, "format_TestCollectReceive_Details: collect: " + valueTimeStamp);
                        if (!obsformat.isEmpty())
                            obsformat = obsformat + "<br>" + getString(R.string.collected_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                        else
                            obsformat = getString(R.string.collected_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                    }
                }
            }
        }
        else {  // TEST_RECEIVE_BY
            if (update_receivedTest_UUIDList != null && update_receivedTest_UUIDList.size() > 0) {
                for (int i = 0; i < update_receivedTest_UUIDList.size(); i++) {
                    MedicationModel medicationModel = new Gson().fromJson(update_receivedTest_UUIDList.get(i).getValue(), MedicationModel.class);
                    if (medicationModel.getMedicationUuidList() != null && medicationModel.getMedicationUuidList().contains(uuid)) {
                        String valueTimeStamp = "";
                        if (update_receivedTest_UUIDList.get(i).getCreatedDate() != null)
                            valueTimeStamp = getValueTimeStamp(update_receivedTest_UUIDList.get(i).getCreatedDate());
                        else {
                            AidModel aidModel = new Gson().fromJson(update_receivedTest_UUIDList.get(i).getValue(), AidModel.class);
                            valueTimeStamp = getValueTimeStamp(aidModel.getDateTime());
                        }
                        Log.d(TAG, "format_TestCollectReceive_Details: receive: " + valueTimeStamp);
                        if (!obsformat.isEmpty())
                            obsformat = obsformat + "<br>" + getString(R.string.resulted_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                        else
                            obsformat = getString(R.string.resulted_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                    }
                }
            }
        }

        return obsformat;
    }
    private String formatAdministeredByDetails(String uuid, boolean isDispense) {
        String obsformat = "";

        if (update_medUuidAdministeredList != null && update_medUuidAdministeredList.size() > 0) {
            for (int i = 0; i < update_medUuidAdministeredList.size(); i++) {
                MedicationModel medicationModel = new Gson().fromJson(update_medUuidAdministeredList.get(i).getValue(), MedicationModel.class);
                if (medicationModel.getMedicationUuidList() != null && medicationModel.getMedicationUuidList().contains(uuid)) {
                    // String creator_name = getCreatorName(creator);
                    String valueTimeStamp = "";
                    if (update_medUuidAdministeredList.get(i).getCreatedDate() != null)
                        valueTimeStamp = getValueTimeStamp(update_medUuidAdministeredList.get(i).getCreatedDate());
                    else {
                        AidModel aidModel = new Gson().fromJson(update_medUuidAdministeredList.get(i).getValue(), AidModel.class);
                        valueTimeStamp = getValueTimeStamp(aidModel.getDateTime());
                    }
                    Log.d(TAG, "formatAdministeredByDetails: medi: " + valueTimeStamp);
                    if (!obsformat.isEmpty())
                        obsformat = obsformat + "<br>" + getResources().getString(R.string.administered_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                    else
                        obsformat = getResources().getString(R.string.administered_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                }
            }
        }

        return obsformat;
    }

    private String formatDispensedByDetails(String uuid, boolean isDispense) {
        String obsAddedByString = "";

        if (update_aidUuidList != null && update_aidUuidList.size() > 0) {
            for (int i = 0; i < update_aidUuidList.size(); i++) {
                AidModel aidModel = new Gson().fromJson(update_aidUuidList.get(i).getValue(), AidModel.class);
                if (aidModel.getAidUuidList() != null && aidModel.getAidUuidList().contains(uuid)) {
                    // String creator_name = getCreatorName(creator);
                    String valueTimeStamp = "";
                    if (update_aidUuidList.get(i).getCreatedDate() != null)
                        valueTimeStamp = getValueTimeStamp(update_aidUuidList.get(i).getCreatedDate());
                    else {
                        valueTimeStamp = getValueTimeStamp(aidModel.getDateTime());
                    }

                    Log.d(TAG, "formatDispensedByDetails: aid: " + valueTimeStamp);
                    obsAddedByString = getResources().getString(R.string.dispensed_by) + " " + aidModel.getHwName() + "<br>" + valueTimeStamp;
                }
            }
        }

        if (update_medUuidDispenseList != null && update_medUuidDispenseList.size() > 0) {
            for (int i = 0; i < update_medUuidDispenseList.size(); i++) {
                MedicationModel medicationModel = new Gson().fromJson(update_medUuidDispenseList.get(i).getValue(), MedicationModel.class);
                if (medicationModel.getMedicationUuidList() != null && medicationModel.getMedicationUuidList().contains(uuid)) {
                    // String creator_name = getCreatorName(creator);
                    String valueTimeStamp = "";
                    if (update_medUuidDispenseList.get(i).getCreatedDate() != null)
                        valueTimeStamp = getValueTimeStamp(update_medUuidDispenseList.get(i).getCreatedDate());
                    else {
                        AidModel aidModel = new Gson().fromJson(update_medUuidDispenseList.get(i).getValue(), AidModel.class);
                        valueTimeStamp = getValueTimeStamp(aidModel.getDateTime());
                    }

                    Log.d(TAG, "formatDispensedByDetails: medi: " + valueTimeStamp);
                    obsAddedByString = getResources().getString(R.string.dispensed_by) + " " + medicationModel.getHwName() + "<br>" + valueTimeStamp;
                }
            }
        }

        return obsAddedByString;
    }

    private String formatCreatorDetails(String creator, String created_date, String comment) {
        String obsAddedByString = "Added by:";
        String valueTimeStamp = getValueTimeStamp(created_date);
        if (comment != null && !comment.trim().isEmpty()) {
            String[] stringarray = comment.split("\\|"); //DELETED|<DELETED_TIMESTAMP>|<DELETOR_DOCTOR_NAME>|<DELETOR_DOCTOR_REGISTRATION_NO>|<CREATOR_DOCTOR_NAME>|<CREATOR_DOCTOR_REGISTRATION_NO>|<CREATED_TIMESTAMP>
            String doctorName = stringarray[2];
            valueTimeStamp = formatTimeForComment(stringarray[6]);
            if (stringarray[4].contains(" ")) {
                String[] names = stringarray[4].split(" ");
                String fname = String.valueOf(names[0].toCharArray()[0]);
                doctorName = fname + " " + names[names.length - 1];
            }
            if (stringarray[5].equalsIgnoreCase("NA") || stringarray[5].equalsIgnoreCase("null"))
                obsAddedByString = getResources().getString(R.string.added_by) + " " + doctorName + "<br>" + valueTimeStamp;
            else
                obsAddedByString = getResources().getString(R.string.added_by) + " " + doctorName + " (" + stringarray[5] + ") " + "<br>" + valueTimeStamp;
        } else if (comment.trim().equalsIgnoreCase("") || comment.trim().isEmpty()) {
            String creator_name = getCreatorName(creator);
            obsAddedByString = getResources().getString(R.string.added_by) + " " + creator_name + "<br>" + valueTimeStamp;
        }
        return obsAddedByString;
    }

    private String getValueTimeStamp(String created_date) {
        if (created_date == null || created_date.isEmpty())
            return "";

        Log.d(TAG, "getValueTimeStamp: " + created_date);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = df.parse(created_date);
            df.setTimeZone(TimeZone.getDefault());
        } catch (ParseException e) {
            e.printStackTrace();
            created_date = formatDateFromOnetoAnother(created_date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss");
            try {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                date = df.parse(created_date);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }


        String formattedDate = df.format(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatTime = new SimpleDateFormat("dd-MM-yyyy, hh:mm aa", Locale.ENGLISH);
        Date date1 = null;
        try {
            date1 = sdf.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String valueTimeStamp = formatTime.format(date1);
        return valueTimeStamp;
    }

    private String getCreatorName(String creator_id) {
        String creator_name = "";
        String creator_reg_num = "";
        String[] columns = {"uuid", "userid", " given_name", "family_name"};
        String providerSelection = "userid = ?";
        String[] providerArgs = {creator_id};
        db.beginTransaction();
        Cursor providerCursor = db.query("tbl_provider", columns, providerSelection, providerArgs, null, null, null);
        if (providerCursor.moveToFirst()) {
            do {
                String providerUuid = providerCursor.getString(providerCursor.getColumnIndex("uuid"));
                String firstName = providerCursor.getString(providerCursor.getColumnIndex("given_name"));
                String lastName = providerCursor.getString(providerCursor.getColumnIndex("family_name"));
                if (firstName != null && !firstName.isEmpty() && !firstName.trim().equalsIgnoreCase(""))
                    creator_name = firstName.substring(0, 1);
                if (lastName != null && !lastName.isEmpty() && !lastName.trim().equalsIgnoreCase(""))
                    creator_name = creator_name + " " + lastName;
                creator_reg_num = getCreatorRegNumber(providerUuid);
            } while (providerCursor.moveToNext());
        }

        if (providerCursor != null && !providerCursor.isClosed())
            providerCursor.close();

        db.setTransactionSuccessful();
        db.endTransaction();
        String creatorDetails = creator_name;
        if (!creator_reg_num.equalsIgnoreCase("NA"))
            creatorDetails = creator_name + " (" + creator_reg_num + ")";
        return creatorDetails;
    }

    private String getCreatorRegNumber(String providerUuid) {
        String registrationNum = "NA";
        String[] columns = {"provideruuid", "value", " attributetypeuuid"};
        String providerSelection = "provideruuid = ? AND attributetypeuuid = ?";
        String[] providerArgs = {providerUuid, "992ccbdd-201a-44ef-8abb-c2eee079886d"};
        db.beginTransaction();
        Cursor providerCursor = db.query("tbl_provider_attribute", columns, providerSelection, providerArgs, null, null, null);
        if (providerCursor.moveToFirst()) {
            do {
                String value = providerCursor.getString(providerCursor.getColumnIndex("value"));
                if (value != null && !value.isEmpty() && !value.trim().equalsIgnoreCase(""))
                    registrationNum = value;
            } while (providerCursor.moveToNext());
        }
        if (providerCursor != null && !providerCursor.isClosed())
            providerCursor.close();

        db.setTransactionSuccessful();
        db.endTransaction();
        return registrationNum;
    }

    private String formatComment(String comment) {
        String formattedComment = "Deleted by:";
        String seperator = "\u200e";
        String[] stringarray = comment.split("\\|"); //DELETED|2023-07-27T05:05:58.894Z|Cardiologist doctor doctor7|6789|General doctor: Doctor
        String valueTimeStamp = formatTimeForComment(stringarray[1]);

        String doctorName = stringarray[2];
        if (stringarray[2].contains(" ")) {
            String[] names = stringarray[2].split(" ");
            String fname = String.valueOf(names[0].toCharArray()[0]);
            doctorName = fname + " " + names[names.length - 1];
        }

        if (stringarray[3].equalsIgnoreCase("NA") || stringarray[5].equalsIgnoreCase("null"))
            formattedComment = getResources().getString(R.string.deleted_by) + " " + doctorName + "<br>" + valueTimeStamp;
        else
            formattedComment = getResources().getString(R.string.deleted_by) + " " + doctorName + " (" + stringarray[3] + ") " + "<br> " + valueTimeStamp;

        return formattedComment;
    }

    private String formatTimeForComment(String s) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = df.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df.setTimeZone(TimeZone.getDefault());
        String formattedDate = df.format(date);
        formattedDate = formattedDate.substring(0, formattedDate.lastIndexOf("Z"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat formatTime = new SimpleDateFormat("dd-MM-yyyy, hh:mm aa", Locale.ENGLISH);
        Date date1 = null;
        try {
            date1 = sdf.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDateAndTime = formatTime.format(date1);
        return formattedDateAndTime;
    }

    ClsDoctorDetails objClsDoctorDetails;

    private void parseDoctorDetails(String dbValue) {
        hasPrescription = "true";
        presc_status.setText(getResources().getString(R.string.prescription_received));
        presc_status.setBackground(getResources().getDrawable(R.drawable.presc_status_green));
        hasPartialPrescription = "false";
        Gson gson = new Gson();
        objClsDoctorDetails = gson.fromJson(dbValue, ClsDoctorDetails.class);
        Log.e(TAG, "TEST VISIT: " + objClsDoctorDetails);

        // Dispense & Administer - START
        if (!isPastVisit) {
            //  fl_DispenseAdminister.setVisibility(View.VISIBLE);
            layout_dispense_1.setVisibility(View.VISIBLE);
            layout_dispense_2.setVisibility(View.VISIBLE);
            layout_test.setVisibility(View.VISIBLE);
        }
        // Dispense & Administer - END

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
//            mDoctorName.setVisibility(View.VISIBLE);
//            mDoctorTitle.setVisibility(View.VISIBLE);
            frameLayout_doctor.setVisibility(View.VISIBLE);

            doctorSign = objClsDoctorDetails.getTextOfSign();
            String doctSp = !LocaleHelper.isArabic(this) ? objClsDoctorDetails.getSpecialization() : "طبيب عام"; //General Physician
            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? checkAndConvertPrescriptionHeadings(getString(R.string.dr_registration_no)) + objClsDoctorDetails.getRegistrationNumber() : "";
            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">"
                    + "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">"
                    + (!TextUtils.isEmpty(objClsDoctorDetails.getName()) ? objClsDoctorDetails.getName() : "")
                    + "</span><br>" + "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">"
                    + doctorDetails(objClsDoctorDetails, doctSp)
                    + "</span><br>" +
                    // "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ? "Phone Number: " + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? checkAndConvertPrescriptionHeadings(getString(R.string.dr_registration_no)) + objClsDoctorDetails.getRegistrationNumber() : "") + "</div>";
            if (LocaleHelper.isArabic(this)) {
                doctorDetailStr = "<html dir=\"rtl\" lang=\"\"><body>" + doctorDetailStr + "</body></html>";
                mDoctorName.setGravity(Gravity.END);
            }
            mDoctorName.setText(Html.fromHtml(doctorDetailStr).toString().trim());
        }
    }

    private String doctorDetails(ClsDoctorDetails objClsDoctorDetails, String doctSp) {
        return (!TextUtils.isEmpty(objClsDoctorDetails.getQualification())
                ? objClsDoctorDetails.getQualification() + ", " + getDoctorSpecialization(doctSp)
                : getDoctorSpecialization(doctSp));
    }

    private String getDoctorSpecialization(String specialization) {
        return (!TextUtils.isEmpty(objClsDoctorDetails.getSpecialization()) ? specialization : "");
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
            ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_EXPORTED);
            isReceiverRegistered = true;
        }
    }

    @Override
    public void onResume() // register the receiver here
    {
        //get from encountertbl from the encounter
        if (visitnoteencounteruuid.equalsIgnoreCase("")) {
            EncounterDAO encounterStartVisitNoteDAO = new EncounterDAO();
            visitnoteencounteruuid = encounterStartVisitNoteDAO.getStartVisitNoteEncounterByVisitUUID(visitUuid);
        }

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

        //logic code for handling the whatsapp prescription part...
//        if(isreturningWhatsapp)
//        {
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EkalArogya_PDF/";
//            File dir = new File(path);
//            deleteRecursive(dir);
//        }
    }

//    public static void deleteRecursive(File fileOrDirectory) {
//        if (fileOrDirectory.isDirectory()) {
//            for (File child : fileOrDirectory.listFiles()) {
//                deleteRecursive(child);
//            }
//        }
//        fileOrDirectory.delete();
//    }

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

    public void signAndSubmit(View view) {
        endVisitApiCall();
    }

    private void endVisitApiCall() {
        // If the value is present in the db, then pick only that value and not hit the api. This way, everytime an api call wont be hit
        // and multiple Start Visit Note encounters wont br created.

        //check if data is uploaded to backend...
        // If Visit is not uplaoded...
        if (isSynedFlag.equalsIgnoreCase("0")) {
            Toast.makeText(VisitSummaryActivity.this, getResources().getString(R.string.visit_summary_upload_reminder_prescription), Toast.LENGTH_SHORT).show();
            return;
        }

        // Visit is uploaded but Prescription is already given...
        if (!isSynedFlag.equalsIgnoreCase("0") && hasPrescription.equalsIgnoreCase("true")) {
            Toast.makeText(VisitSummaryActivity.this, getResources().getString(R.string.visit_summary_prescription_already_given), Toast.LENGTH_SHORT).show();
            return;
        }

        if (visitnoteencounteruuid.equalsIgnoreCase("")) {
            startvisitnoteApiCall();
        } else {
            Intent visitSummary = new Intent(VisitSummaryActivity.this, PrescriptionActivity.class);
            visitSummary.putExtra("visitUuid", visitUUID);
            visitSummary.putExtra("patientUuid", patientUuid);
            visitSummary.putExtra("startVisitNoteApiEncounterResponse", visitnoteencounteruuid);
            visitSummary.putExtra("encounterUuidVitals", encounterVitals);
            visitSummary.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
            visitSummary.putExtra("EncounterAdultInitial_LatestVisit", encounterUuidAdultIntial);
            visitSummary.putExtra("name", patientName);
            visitSummary.putExtra("gender", genderView.getText());
            visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month);
            visitSummary.putExtra("tag", intentTag);
            visitSummary.putExtra("pastVisit", isPastVisit);
            if (hasPrescription.equalsIgnoreCase("true")) {
                visitSummary.putExtra("hasPrescription", "true");
            } else {
                visitSummary.putExtra("hasPrescription", "false");
            }
            startActivity(visitSummary);
        }
    }

    public void startvisitnoteApiCall() {
        String url = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/encounter";
        endVisitEncounterPrescription = getEndVisitDataModel();
        //  String encoded = sessionManager.getEncoded();
        String encoded = base64Utils.encoded("sysnurse", "Nurse123");

        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<EndVisitResponseBody> resultsObservable = apiService.END_VISIT_RESPONSE_BODY_OBSERVABLE(url, endVisitEncounterPrescription, "Basic " + encoded);
        resultsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<EndVisitResponseBody>() {
            @Override
            public void onNext(@NonNull EndVisitResponseBody endVisitResponseBody) {
                String encounter = endVisitResponseBody.getUuid(); // Use this uuid for pres obs api body.

                try {
                    EncounterDAO encounterDAO_ = new EncounterDAO();
                    encounterDAO_.insertStartVisitNoteEncounterToDb(encounter, visitUuid);

                } catch (DAOException e) {
                    e.printStackTrace();
                }

                Intent visitSummary = new Intent(VisitSummaryActivity.this, PrescriptionActivity.class);
                visitSummary.putExtra("visitUuid", visitUUID);
                visitSummary.putExtra("patientUuid", patientUuid);
                visitSummary.putExtra("startVisitNoteApiEncounterResponse", encounter);
                visitSummary.putExtra("encounterUuidVitals", encounterVitals);
                visitSummary.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                visitSummary.putExtra("EncounterAdultInitial_LatestVisit", encounterUuidAdultIntial);
                visitSummary.putExtra("name", patientName);
                visitSummary.putExtra("gender", genderView.getText());
                visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month);
                visitSummary.putExtra("tag", intentTag);
                visitSummary.putExtra("pastVisit", isPastVisit);
                if (hasPrescription.equalsIgnoreCase("true")) {
                    visitSummary.putExtra("hasPrescription", "true");
                } else {
                    visitSummary.putExtra("hasPrescription", "false");
                }
                startActivity(visitSummary);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e("err", "sd: " + e);
            }

            @Override
            public void onComplete() {
                Log.e("err", "sd");
            }
        });
    }

    private EndVisitEncounterPrescription getEndVisitDataModel() {
        List<EncounterProvider> encounterProviderList = new ArrayList<>();
        EncounterProvider encounterProvider = new EncounterProvider();

        encounterProvider.setEncounterRole(ENCOUNTER_ROLE); // Constant
        encounterProvider.setProvider(sessionManager1.getProviderID()); // user setup app provider
        encounterProviderList.add(encounterProvider);

        EndVisitEncounterPrescription datamodel = new EndVisitEncounterPrescription();
        datamodel.setPatient(patientUuid);
        datamodel.setEncounterProviders(encounterProviderList);
        datamodel.setVisit(visitUUID);
        datamodel.setEncounterDatetime(AppConstants.dateAndTimeUtils.currentDateTime());
        datamodel.setEncounterType(ENCOUNTER_VISIT_NOTE);

        Log.v("presbody", "new: " + new Gson().toJson(datamodel));
        return datamodel;
    }

    @Override
    public void onClick(View view) {
        EncounterDAO tag = (EncounterDAO) view.getTag();
        if (view.getId() == R.id.tvDispense)
            dispenseIntent(tag);
        else if (view.getId() == R.id.tvAdminister)
            admininisterIntent(tag);
    }

    /**
     * This is redirect and show all the past added Additional Remarks list...
     * @param view
     */
    public void viewPastnotes(View view) {
        Intent intent = new Intent(this, PastNotesDispenseAdministerActivity.class);
        intent.putExtra("viewtag", ADDITIONAL_REMARKS);
        intent.putExtra("mtag", ADDITIONAL_REMARKS);
        intent.putExtra("visitUUID", visitUUID);
        startActivity(intent);
    }

/*    @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_connect.setVisibility(View.GONE);
                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
                        Toast.makeText(VisitSummaryActivity.this, printerInterface.getConfigObject().toString()
                                + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                        tv_device_selected.setText(printerInterface.getConfigObject().toString());
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                        curPrinterInterface = printerInterface; // set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
                            Toast.makeText(VisitSummaryActivity.this, printerInterface.getConfigObject().toString()
                                            + getString(R.string._main_disconnect),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VisitSummaryActivity.this, getString(R.string._main_disconnect),
                                    Toast.LENGTH_SHORT).show();
                        }
                        TimeRecordUtils.record("Time：", System.currentTimeMillis());
                        tv_device_selected.setText(R.string.please_connect);
                        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
                        curPrinterInterface = null;
                        printerInterfaceArrayList.remove(printerInterface);
                        setPrintEnable(false);

                        break;
                    default:
                        break;
                }
            }
        });
    }*/

    /*  @Override
      public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

      }

      @Override
      public void onPointerCaptureChanged(boolean hasCapture) {

      }
  */
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
//                if (additionalCommentsCard.getVisibility() == View.VISIBLE) {
//                    if (!additionalCommentsTextView.getText().toString().trim().isEmpty())
//                        body = body + getString(R.string.visit_summary_additional_comments) + ":" +
//                                additionalCommentsTextView.getText().toString() + "\n";
//                }
                if (followUpDateCard.getVisibility() == View.VISIBLE) {
                    if (!followUpDateTextView.getText().toString().trim().isEmpty())
                        body = body + getString(R.string.visit_summary_follow_up_date) + ":" + followUpDateTextView.getText().toString() + "\n";
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
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            if (visitsDAO.getDownloadedValue(visitUuid).equalsIgnoreCase("false") && uploaded) {
                String visitnote = "";
                EncounterDAO encounterDAO = new EncounterDAO();
                String encounterIDSelection = "visituuid = ? AND voided = ?";
                String[] encounterIDArgs = {visitUuid, "0"}; // voided = 0 so that the Deleted values dont come in the presc.
                db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
                Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                    } while (encounterCursor.moveToNext());

                }

                if (visitnote.equalsIgnoreCase("")) {
                    Toast.makeText(context, getResources().getString(R.string.no_presc_available), Toast.LENGTH_LONG).show();
                    return;
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
                    tl_prescribed_medications.removeAllViews();
                    //   prescriptionTextView.setText("");
                    prescriptionCard.setVisibility(View.GONE);

                }
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = "";
                    medicalAdviceTextView.setText("");
                    medicalAdviceCard.setVisibility(View.GONE);
                }
                if (!testsReturned.isEmpty()) {
                    testsReturned = "";
                    ll_test.removeAllViews();
                  //  requestedTestsTextView.setText("");
                    requestedTestsCard.setVisibility(View.GONE);
                }

                if (!aidOrderReturned.isEmpty()) {
                    aidOrderReturned = "";
                    aidOrderType1TextView.setText("");
                    aidOrderType2TextView.setText("");
                    aidOrderType3TextView.setText("");
                    aidOrderType4TextView.setText("");
                    aidOrderType5TextView.setText("");
                    aidOrderCard.setVisibility(View.GONE);
                }
                if (!followUpDate.isEmpty()) {
                    followUpDate = "";
                    followUpDateTextView.setText("");
                    followUpDateCard.setVisibility(View.GONE);
                }
                String[] columns = {"uuid", "value", " conceptuuid", "comment", "creator", "obsservermodifieddate"};
                String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
                String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, "obsservermodifieddate DESC");
                if (visitCursor.moveToFirst()) {
                    reset();
                    do {
                        String uuid = visitCursor.getString(visitCursor.getColumnIndex("uuid"));
                        String comment = visitCursor.getString(visitCursor.getColumnIndex("comment"));
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        String creator = visitCursor.getString(visitCursor.getColumnIndex("creator"));
                        String created_date = visitCursor.getString(visitCursor.getColumnIndex("obsservermodifieddate"));
                        hasPartialPrescription = "true"; //if any kind of prescription data is present...
                        presc_status.setText(getResources().getString(R.string.prescription_in_progress));
                        presc_status.setBackground(getResources().getDrawable(R.drawable.presc_status_orange));

                        // Dispense & Administer - START
                        if (!isPastVisit) {
                            //  fl_DispenseAdminister.setVisibility(View.VISIBLE);
                            layout_dispense_1.setVisibility(View.VISIBLE);
                            layout_dispense_2.setVisibility(View.VISIBLE);
                            layout_test.setVisibility(View.VISIBLE);
                        }
                        // Dispense & Administer - END

                        if (dbValue.startsWith("{")) {
                            AnswerValue answerValue = new Gson().fromJson(dbValue, AnswerValue.class);
                            parseData(uuid, dbConceptID, LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue(), comment, creator, created_date);
                        } else {
                            parseData(uuid, dbConceptID, dbValue, comment, creator, created_date);
                        }
                    } while (visitCursor.moveToNext());
                } else {
                    // here presc is not present now we will check for visitUUID present or not if present than show presc pending...
                    isVisitUploaded();
                }
                visitCursor.close();

                //checks if prescription is downloaded and if so then sets the icon color.
                if (hasPrescription.equalsIgnoreCase("true")) {
                    ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
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

    }

    private void isVisitUploaded() {
        String v = "";
        if (visitUUID == null)
            v = visitUuid;
        else if (visitUuid == null)
            v = visitUUID;

        isVisitSpecialityExists = speciality_row_exist_check(v, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
        Log.d(TAG, "isVisitUploaded: " + "visitView: " + visitView.getText().toString() + ", visitUUID: " + v + ", " + isVisitSpecialityExists);

        if (visitView != null && visitView.getText().toString().contains("XXXX") &&
                presc_status.getText().toString().equalsIgnoreCase(getString(R.string.case_not_uploaded))) {
            presc_status.setText(getResources().getString(R.string.prescription_pending));
            presc_status.setBackground(getResources().getDrawable(R.drawable.presc_status_red));
        }

    }

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
        String[] columns = {"uuid", "value", " conceptuuid", "comment", "creator", "obsservermodifieddate"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, "obsservermodifieddate DESC");
        if (visitCursor.moveToFirst()) {
            do {
                String uuid = visitCursor.getString(visitCursor.getColumnIndex("uuid"));
                String comment = visitCursor.getString(visitCursor.getColumnIndex("comment"));
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                String creator = visitCursor.getString(visitCursor.getColumnIndex("creator"));
                String created_date = visitCursor.getString(visitCursor.getColumnIndex("obsservermodifieddate"));
                hasPartialPrescription = "true"; //if any kind of prescription data is present...
                presc_status.setText(getResources().getString(R.string.prescription_in_progress));
                presc_status.setBackground(getResources().getDrawable(R.drawable.presc_status_orange));

                // Dispense & Administer - START
                if (!isPastVisit) {
                    //   fl_DispenseAdminister.setVisibility(View.VISIBLE);
                    layout_dispense_1.setVisibility(View.VISIBLE);
                    layout_dispense_2.setVisibility(View.VISIBLE);
                    layout_test.setVisibility(View.VISIBLE);
                }
                // Dispense & Administer - END

                if (dbValue.startsWith("{")) {
                    AnswerValue answerValue = new Gson().fromJson(dbValue, AnswerValue.class);
                    parseData(uuid, dbConceptID, LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue(), comment, creator, created_date);
                } else {
                    parseData(uuid, dbConceptID, dbValue, comment, creator, created_date);

                }
            } while (visitCursor.moveToNext());
        } else {
            // here presc is not present now we will check for visitUUID present or not if present than show presc pending...
            isVisitUploaded();
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
        ContextCompat.registerReceiver(this, mMessageReceiver, new IntentFilter(FILTER), ContextCompat.RECEIVER_EXPORTED);
//        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
//                );
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
        }

        //In onStop() it will check and unregister the receiver...
        //This is done in onStop as we are registering them in onStart()
        if (receiver != null) {
            unregisterReceiver(receiver);
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
            dischargeOrderReturned = "";
            aidOrderReturned = "";
            followUpDate = "";
            String[] columns = {"uuid", "value", " conceptuuid", "comment", "creator", "obsservermodifieddate"};
            String visitSelection = "encounteruuid = ? ";
            String[] visitArgs = {encounterUuid};
            Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, "obsservermodifieddate DESC");
            if (visitCursor.moveToFirst()) {
                do {
                    String uuid = visitCursor.getString(visitCursor.getColumnIndex("uuid"));
                    String comment = visitCursor.getString(visitCursor.getColumnIndex("comment"));
                    String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                    String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                    String creator = visitCursor.getString(visitCursor.getColumnIndex("creator"));
                    String created_date = visitCursor.getString(visitCursor.getColumnIndex("obsservermodifieddate"));
                    parseData(uuid, dbConceptID, dbValue, comment, creator, created_date);
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

    private void showSelectSpecialtyErrorDialog() {
        /*TextView t = (TextView) speciality_spinner.getSelectedView();
        t.setError(getString(R.string.please_select_specialization_msg));
        t.setTextColor(Color.RED);*/

        AlertDialog.Builder builder = new AlertDialog.Builder(VisitSummaryActivity.this).setMessage(getResources().getString(R.string.please_select_specialization_msg)).setCancelable(false).setPositiveButton(getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
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
                parseDoctorDetails(dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private AppointmentDetailsResponse mAppointmentDetailsResponse;
    private int mAppointmentId = 0;
    private TextView mDoctorAppointmentBookingTextView;
    private TextView mCancelAppointmentBookingTextView;
    private TextView mInfoAppointmentBookingTextView;
    private static final int SCHEDULE_LISTING_INTENT = 2001;

    private void getAppointmentDetails(String visitUUID) {
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        mInfoAppointmentBookingTextView.setVisibility(View.VISIBLE);
        mInfoAppointmentBookingTextView.setText(getString(R.string.please_wait));
        Log.v("VisitSummary", "getAppointmentDetails");
        String baseurl = BuildConfig.SERVER_URL + ":3004";

        ApiClientAppointment.getInstance(baseurl).getApi()
                .getAppointmentDetails(visitUUID, authHeader)
                .enqueue(new Callback<AppointmentDetailsResponse>() {
            @Override
            public void onResponse(Call<AppointmentDetailsResponse> call, retrofit2.Response<AppointmentDetailsResponse> response) {
                ResponseChecker<AppointmentDetailsResponse> responseChecker = new ResponseChecker<>(response);
                if (responseChecker.isNotAuthorized()) {
                    //TODO: redirect to login screen
                    return;
                }

                if (response == null || response.body() == null) return;
                mAppointmentDetailsResponse = response.body();
                if (!mAppointmentDetailsResponse.isStatus()) {
                    Toast.makeText(VisitSummaryActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                if (mAppointmentDetailsResponse.getData() == null) {
                    mCancelAppointmentBookingTextView.setVisibility(View.GONE);
                    mInfoAppointmentBookingTextView.setVisibility(View.GONE);
                    mDoctorAppointmentBookingTextView.setVisibility(View.VISIBLE);
                    mDoctorAppointmentBookingTextView.setText(getString(R.string.book_appointment));
                    mAppointmentId = 0;
                } else {
                    //-------------------insert into local db--------------------
                    try {
                        AppointmentDAO appointmentDAO = new AppointmentDAO();
                        appointmentDAO.insert(mAppointmentDetailsResponse.getData());
                        mAppointmentId = mAppointmentDetailsResponse.getData().getId();

                        mCancelAppointmentBookingTextView.setVisibility(View.VISIBLE);
                        mInfoAppointmentBookingTextView.setVisibility(View.VISIBLE);
                        mDoctorAppointmentBookingTextView.setVisibility(View.VISIBLE);
                        mDoctorAppointmentBookingTextView.setText(getString(R.string.reschedule_appointment));
                        mInfoAppointmentBookingTextView.setText(getString(R.string.appointment_booked) + ":\n\n" + org.intelehealth.app.utilities.StringUtils.getTranslatedDays(mAppointmentDetailsResponse.getData().getSlotDay(), new SessionManager(VisitSummaryActivity.this).getAppLanguage()) + "\n" + mAppointmentDetailsResponse.getData().getSlotDate() + "\n" + org.intelehealth.app.utilities.StringUtils.getTranslatedSlot(mAppointmentDetailsResponse.getData().getSlotTime(), new SessionManager(VisitSummaryActivity.this).getAppLanguage()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                checkAndDisplayAppointment();
            }

            @Override
            public void onFailure(Call<AppointmentDetailsResponse> call, Throwable t) {
                Log.v("onFailure", t.getMessage());
                checkAndDisplayAppointment();
            }
        });

    }

    private void checkAndDisplayAppointment() {
        EncounterDAO encounterDAO = new EncounterDAO();
        boolean isCompletedOrExited = false;
        try {
            isCompletedOrExited = encounterDAO.isCompletedOrExited(visitUuid);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if (isCompletedOrExited) {
            mCancelAppointmentBookingTextView.setVisibility(View.GONE);
            mInfoAppointmentBookingTextView.setVisibility(View.GONE);
            mDoctorAppointmentBookingTextView.setVisibility(View.GONE);
        }
    }

    private void cancelAppointment() {
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage(getString(R.string.appointment_booking_cancel_confirmation_txt))
                //set positive button
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CancelRequest request = new CancelRequest();
                        request.setVisitUuid(mAppointmentDetailsResponse.getData().getVisitUuid());
                        request.setId(mAppointmentDetailsResponse.getData().getId());
                        request.setHwUuid((new SessionManager(VisitSummaryActivity.this).getProviderID()));
                        request.setReason("Patient not available");
                        String baseurl = BuildConfig.SERVER_URL + ":3004";
                        ApiClientAppointment.getInstance(baseurl).getApi()
                                .cancelAppointment(request, authHeader)
                                .enqueue(new Callback<CancelResponse>() {
                            @Override
                            public void onResponse(Call<CancelResponse> call, Response<CancelResponse> response) {
                                ResponseChecker<CancelResponse> responseChecker = new ResponseChecker<>(response);
                                if (responseChecker.isNotAuthorized()) {
                                    //TODO: redirect to login screen
                                    return;
                                }

                                if (response.body() == null) return;
                                CancelResponse cancelResponse = response.body();
                                if (cancelResponse.isStatus()) {
                                    AppointmentDAO appointmentDAO = new AppointmentDAO();
                                    //AppointmentInfo appointmentInfo=appointmentDAO.getAppointmentByVisitId(visitUuid);
                                    //if(appointmentInfo!=null && appointmentInfo.getStatus().equalsIgnoreCase("booked")) {
                                    appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                                    //}

                                    Toast.makeText(VisitSummaryActivity.this, getString(R.string.appointment_cancelled_success_txt), Toast.LENGTH_SHORT).show();
                                    getAppointmentDetails(mAppointmentDetailsResponse.getData().getVisitUuid());
                                } else {
                                    Toast.makeText(VisitSummaryActivity.this, getString(R.string.failed_to_cancel_appointment), Toast.LENGTH_SHORT).show();

                                }
                            }

                            @Override
                            public void onFailure(Call<CancelResponse> call, Throwable t) {
                                Log.v("onFailure", t.getMessage());
                            }
                        });
                    }
                })
                //set negative button
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCHEDULE_LISTING_INTENT) {
            getAppointmentDetails(visitUuid);
        }
    }

    // This function will call the TextPrintActivity screen for printing the text data.
    private void textPrint() throws UnsupportedEncodingException {
        if (objClsDoctorDetails != null) {
            String htmlDocPrescription = sms_prescription();
            // Bitmap doctorSignature = getdoctorsignature();
            String htmlDoctorDetails = getDoctorDetailsHTML();
            Intent intent_esc = new Intent(VisitSummaryActivity.this, TextPrintESCActivity.class);
            intent_esc.putExtra("sms_prescripton", htmlDocPrescription);
            intent_esc.putExtra("doctorDetails", htmlDoctorDetails);
            intent_esc.putExtra("font-family", objClsDoctorDetails.getFontOfSign());
            intent_esc.putExtra("drSign-text", objClsDoctorDetails.getTextOfSign());
            startActivity(intent_esc);
        } else {
            Toast.makeText(VisitSummaryActivity.this, getResources().getString(R.string.no_presc_available), Toast.LENGTH_SHORT).show();
            //  Toast.makeText(VisitSummaryActivity.this, "No Prescription Available", Toast.LENGTH_SHORT).show();
        }

/*
        switch (IntelehealthApplication.getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                //turn2Activity(TextPrintESCActivity.class);
                Intent intent_esc = new Intent(VisitSummaryActivity.this, TextPrintESCActivity.class);
                intent_esc.putExtra("sms_prescripton", htmlDocPrescription);
                intent_esc.putExtra("doctorDetails", htmlDoctorDetails);
                break;
            default:
               // turn2Activity(TextPrintActivity.class);
                break;
        }
*/
    }

    private String getDoctorDetailsHTML() {
        // Generate an HTML document on the fly:
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            String doctSp = !LocaleHelper.isArabic(this) ? objClsDoctorDetails.getSpecialization() : "طبيب عام"; //General Physician
            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? checkAndConvertPrescriptionHeadings(getString(R.string.dr_registration_no)) + objClsDoctorDetails.getRegistrationNumber() : "";

            doctorDetailStr =/* "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +*/

                    "<br><span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + // Dr.Name
                            "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() //Dr. Qualifi
                            + " " + doctSp + "</span><br>" + doctrRegistartionNum;

            Log.e("precs", "htmlpresc_doctor: " + Html.fromHtml(doctorDetailStr).toString());

//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>";

//                            + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "+918068533343" + "</span>"
            /*+*/

                   /* "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification()
                    + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +

                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +*/

            /*"</div>"*/
            ;

        }
        return doctorDetailStr;
    }


    /**
     * @return htmlDocument: HTML formated string for displaying prescription...
     */
    private String sms_prescription() {
        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + ((!TextUtils.isEmpty(patient.getLast_name())) ? patient.getLast_name() : "");
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
        mPulse = pulse.getValue();
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

        if (mComplaint.contains("الأعراض المرافقة")) {
            String[] cc = StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("الأعراض المرافقة") - 3);
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

        String rx_web = stringToWeb_sms(rxReturned).replace("<b style=\"font-size:11pt; margin: 0px; padding: 0px;\">- </b>", "");

        String tests_web = stringToWeb_sms(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

        String advice_web = stringToWeb_sms(medicalAdvice_string.trim().replace("\n\n", "\n"));
        Log.d("Hyperlink", "hyper_print: " + advice_web);

        String diagnosis_web = stringToWeb_sms(diagnosisReturned);
        String discharge_order_web = stringToWeb_sms(dischargeOrderReturned);
        String comments_web = stringToWeb(additionalReturned);


        String formattedAidOrder = "";
        if (aidOrderType1TextView.getVisibility() == View.VISIBLE && aidOrderType1TextView.getText().toString() != null && !aidOrderType1TextView.getText().toString().trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType1TextView.getText().toString().trim()*/  newMedicalEquipLoanAidOrderPresc + "\n";
        if (aidOrderType2TextView.getVisibility() == View.VISIBLE && aidOrderType2TextView.getText().toString() != null && !aidOrderType2TextView.getText().toString().trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType2TextView.getText().toString().trim()*/ newFreeMedicalEquipAidOrderPresc + "\n";
        if (aidOrderType3TextView.getVisibility() == View.VISIBLE && aidOrderType3TextView.getText().toString() != null && !aidOrderType3TextView.getText().toString().trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType3TextView.getText().toString().trim()*/  newCoverMedicalExpenseAidOrderPresc + "\n";
        if (aidOrderType4TextView.getVisibility() == View.VISIBLE && aidOrderType4TextView.getText().toString() != null && !aidOrderType4TextView.getText().toString().trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType4TextView.getText().toString().trim()*/ newCoverSurgicalExpenseAidOrderPresc + "\n";
        if (aidOrderType5TextView.getVisibility() == View.VISIBLE && aidOrderType5TextView.getText().toString() != null && !aidOrderType5TextView.getText().toString().trim().equalsIgnoreCase(""))
            formattedAidOrder = formattedAidOrder + /*aidOrderType5TextView.getText().toString().trim()*/ newCashAssistanceExpenseAidOrderPresc;

        formattedAidOrder = formattedAidOrder.replace("Others||", "Others - ");
        String aidOrder_web = stringToWebAidOrder(mapDataIntoJson(formattedAidOrder.replace("\n", "<br>")));

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

        String heading = getPrescriptionHeading();
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

//        // Generate an HTML document on the fly:
//        String fontFamilyFile = "";
//        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
//            Log.d("font", "font: " + objClsDoctorDetails.getFontOfSign());
//            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
//            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
//            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
//            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
//                fontFamilyFile = "src: url('file:///android_asset/fonts/Almondita-mLZJP.ttf');";
//            }
//        }
//        String font_face = "<style>" +
//                "                @font-face {" +
//                "                    font-family: \"MyFont\";" +
//                fontFamilyFile +
//                "                }" +
//                "            </style>";
//
//        String doctorSign = "";
//        String doctrRegistartionNum = "";
//        // String docDigitallySign = "";
//        String doctorDetailStr = "";
//        if (objClsDoctorDetails != null) {
//            //  docDigitallySign = "Digitally Signed By";
//            doctorSign = objClsDoctorDetails.getTextOfSign();
//
//            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) +
//                    objClsDoctorDetails.getRegistrationNumber() : "";
//
//            doctorDetailStr =/* "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +*/
//
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + // Dr.Name
//                            "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() //Dr. Qualifi
//                    + " " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//                            doctrRegistartionNum;
//
//                    Log.e("precs", "htmlpresc: "+ Html.fromHtml(doctorDetailStr).toString());
//
////                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
////                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>";
//
////                            + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "+918068533343" + "</span>"
//            /*+*/
//
//                   /* "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification()
//                    + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +*/
//
//            /*"</div>"*/;
//
//        }

//        if (isRespiratory) {

        String htmlDocument = String.format("<b id=\"heading_1\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b><br>" + "<b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b>" + "<br><br>" + "<b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</b><br>" + "<b id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">" + getString(R.string.prescription_age) + ": %s | " + getString(R.string.prescription_gender) + ": %s  </b>" + "<br><br>", heading, heading2, mPatientName, age, mGender);

        // If the Diagnosis is not empty, only then the details will be displayed in the Prescription
        if (!diagnosis_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<b id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + getString(R.string.prescription_diagnosis) + " <br>" + "%s </b><br>", diagnosis_web));
        }

        // If the Medication Plan provided is not empty, only then will the details be displayed in the Prescription
        if (!rx_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<b id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + getString(R.string.prescription_med_plan) + " <br>" + "%s </b><br>", rx_web));
        }

        // If the Tests provided is not empty, only then will the details be displayed in the Prescription
        if (!tests_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<b id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + getString(R.string.prescription_rec_investigation) + " <br>" + "%s " + "</b><br>", tests_web));
        }

        // If the Advice provided is not empty, only then will the details be displayed in the Prescription
        if (!advice_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<b id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + getString(R.string.prescription_general_advice) + " <br>" + "%s" + "</b><br>", advice_web));
        }

        //changes done for ticket SYR-358
            /*if (!comments_web.isEmpty()) {
                htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"additional_comments_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_additional_comments)) + "</p></b></u>" + "%s<br>", comments_web));
            }*/

        if (!aidOrder_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"aid_order_heading\" style=\"font-size:15pt;margin-top:0px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_aid_order)) + "</p></b></u>" + "%s<br>", aidOrder_web));
        }

        if (!discharge_order_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<u><b><p id=\"discharge_order_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + checkAndConvertPrescriptionHeadings(getResources().getString(R.string.visit_summary_discharge_order)) + "</p></b></u>" + "%s<br>", discharge_order_web));
        }

        // If the Follow Up Date provided is not empty, only then will the details be displayed in the Prescription
        if (!followUp_web.isEmpty()) {
            htmlDocument = htmlDocument.concat(String.format("<b id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">" + getString(R.string.prescription_follow_up_date) + " <br>" + "%s" + "</b><br>", followUp_web));
        }

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
     * @param input string based text data
     * @return formatted: adding html tags
     */
    private String stringToWeb_sms(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            AnswerValue answerValue = new Gson().fromJson(input, AnswerValue.class);
            String _input = LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue();

            String para_open = "<b style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</b><br>";
            formatted = para_open + "- " + _input.replaceAll("\n", para_close + para_open + "- ") + para_close;
        }
        if (formatted.trim().endsWith("-"))
            formatted = formatted.substring(0, formatted.lastIndexOf("-"));
        return formatted;
    }

    private String stringToWeb(String input) {
        Log.v("VS", input);
        String formatted = "";
        if (input != null && !input.isEmpty()) {
            if (input.endsWith("\n"))
                input = input.substring(0, input.lastIndexOf("\n"));
            String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</p>";
            formatted = para_open + Node.big_bullet + input.replaceAll("\n", para_close + para_open + Node.big_bullet) + para_close;
        }
        return formatted;
    }

    private String stringToWebAdvice(String input) {
        Log.v("VS", input);
        String formatted = "";
        if (input != null && !input.isEmpty()) {
            String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</p>";
            formatted = para_open + input.replaceAll("<br><br>", para_close + para_open + Node.big_bullet + " ") + para_close;
        }
        return formatted;
    }


    private String stringToWebAidOrder(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            AnswerValue answerValue = new Gson().fromJson(input, AnswerValue.class);
            String _input = LocaleHelper.isArabic(this) ? answerValue.getArValue() : answerValue.getEnValue();

            String para_open = "<style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "<br>";
            formatted = para_open + Node.big_bullet + " " + _input.replaceAll("\n", para_close + para_open);
            formatted = formatted.replaceAll("<br>", "<br>" + Node.big_bullet + " ");
            if (formatted.trim().endsWith(Node.big_bullet))
                formatted = formatted.substring(0, formatted.lastIndexOf(Node.big_bullet));
        }
        return formatted;
    }


/*
    private Bitmap getdoctorsignature() {
        // Generate an HTML document on the fly:
        int fontFamilyFile = 0;
        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
            Log.d("font", "font: " + objClsDoctorDetails.getFontOfSign());
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = R.font.youthness;
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = R.font.asem;
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = R.font.arty;
            }*/
/* else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = R.font.almondita;
            }*//*

        }

        return testB;

    }
*/

    // Prescription headings are not getting converted for some devices. We are unsure why this is happening.
    // For now, we're are forcefully converting the strings if the language selected is Arabic - Added by Arpan Sircar
    private String checkAndConvertPrescriptionHeadings(String string) {
        String newString = string;
        if (sessionManager1.getAppLanguage().equalsIgnoreCase("ar")) {
            if (string.equalsIgnoreCase("Patient Information")) newString = "معلومات المريض";
            if (string.equalsIgnoreCase("Name")) newString = "الاسم";
            if (string.equalsIgnoreCase("Age")) newString = "العمر";
            if (string.equalsIgnoreCase("Gender")) newString = "الجنس";
            if (string.equalsIgnoreCase("Address and Contact")) newString = "العنوان";
            if (string.equalsIgnoreCase("Patient Id")) newString = "رقم الاضبارة";
            if (string.equalsIgnoreCase("Date of Visit")) newString = "موعد الزيارة";
            if (string.equalsIgnoreCase("Reason of Visit")) newString = "سبب الزيارة";
            if (string.equalsIgnoreCase("Presenting complaint(s)")) newString = "تقديم الشكاوى (s)";
            if (string.equalsIgnoreCase("Diagnosis")) newString = "التشخيص";
            if (string.equalsIgnoreCase("Medication Plan")) newString = "الخطة العلاجية";
            if (string.equalsIgnoreCase("Recommended Investigation(s)"))
                newString = "التحاليل و الفحوصات المطلوبة";
            if (string.equalsIgnoreCase("General Instructions")) newString = "توجيهات عامة";
            if (string.equalsIgnoreCase("Followup Date")) newString = "تاريخ زيارة المتابعة";
            if (string.equalsIgnoreCase("Not provided")) newString = "غير مزود";
            if (string.equalsIgnoreCase("Email:")) newString = "البريد الإلكتروني:";
            if (string.equalsIgnoreCase("Registration No:")) newString = "رقم التسجيل:";
            if (string.equalsIgnoreCase("Aid Order")) newString = "امر صرف مساعدة";
            if (string.equalsIgnoreCase("Discharge Order")) newString = "تخريج الحالة";
        }
        return newString;
    }

    private String getPrescriptionHeading() {
        String villageName = checkAndRemoveEndDash(patient.getCity_village());
        if (sessionManager1.getAppLanguage().equalsIgnoreCase("ar")) {
            villageName = switch_en_to_ar_village_edit(villageName);
            villageName = "فريق ".concat(villageName).concat(" الصحي");
        } else {
            villageName = villageName.concat(" Health Unit");
        }
        return villageName;
    }

    public String getMedicationData() {
        return rxReturned;
    }

    public String getTestsData() {
        return testsReturned;
    }

    public String getAidData() {
        String aid = "";

        if (!newMedicalEquipLoanAidOrderPresc.isEmpty())
            aid = aid + "\n" + newMedicalEquipLoanAidOrderPresc;

        if (!newFreeMedicalEquipAidOrderPresc.isEmpty())
            aid = aid + "\n" + newFreeMedicalEquipAidOrderPresc;

        if (!newCoverMedicalExpenseAidOrderPresc.isEmpty())
            aid = aid + "\n" + newCoverMedicalExpenseAidOrderPresc;

        if (!newCoverSurgicalExpenseAidOrderPresc.isEmpty())
            aid = aid + "\n" + newCoverSurgicalExpenseAidOrderPresc;

        if (!newCashAssistanceExpenseAidOrderPresc.isEmpty())
            aid = aid + "\n" + newCashAssistanceExpenseAidOrderPresc;

        return aid;
    }
}