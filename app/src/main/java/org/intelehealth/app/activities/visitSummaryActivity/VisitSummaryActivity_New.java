package org.intelehealth.app.activities.visitSummaryActivity;

import static org.intelehealth.app.ui2.utils.CheckInternetAvailability.isNetworkAvailable;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy;
import static org.intelehealth.app.utilities.UuidDictionary.ADDITIONAL_NOTES;
import static org.intelehealth.app.utilities.UuidDictionary.SPECIALITY;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentAdapter;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.app.activities.familyHistoryActivity.FamilyHistoryActivity;
import org.intelehealth.app.activities.notification.AdapterInterface;
import org.intelehealth.app.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.app.activities.vitalActivity.VitalsActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointmentNew.ScheduleAppointmentActivity_New;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.ProviderAttributeLIstDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.services.DownloadService;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitSummaryActivity_New extends AppCompatActivity implements AdapterInterface, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = VisitSummaryActivity_New.class.getSimpleName();
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;

    SQLiteDatabase db;

    Button btn_vs_sendvisit;
    private Context context;
    private ImageButton btn_up_header, btn_up_vitals_header, btn_up_visitreason_header,
            btn_up_phyexam_header, btn_up_medhist_header, openall_btn, btn_up_addnotes_vd_header;
    private RelativeLayout vitals_header_relative, chiefcomplaint_header_relative, physExam_header_relative,
            pathistory_header_relative, addnotes_vd_header_relative, special_vd_header_relative;
    private RelativeLayout vs_header_expandview, vs_vitals_header_expandview, add_additional_doc, vd_special_header_expandview,
            vs_visitreason_header_expandview, vs_phyexam_header_expandview, vs_medhist_header_expandview, vd_addnotes_header_expandview,
            vs_add_notes, parentLayout;
    private LinearLayout btn_bottom_printshare, btn_bottom_vs;
    private EditText additional_notes_edittext;
    SessionManager sessionManager, sessionManager1;
    String appLanguage, patientUuid, visitUuid, state, patientName, patientGender, intentTag, visitUUID,
            medicalAdvice_string = "", medicalAdvice_HyperLink = "", isSynedFlag = "";
    private float float_ageYear_Month;
    String encounterVitals, encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;
    SharedPreferences mSharedPreference;
    Boolean isPastVisit = false, isVisitSpecialityExists = false;
    Boolean isReceiverRegistered = false;
    ArrayList<String> physicalExams;
    VisitSummaryActivity_New.DownloadPrescriptionService downloadPrescriptionService;
    private RecyclerView mAdditionalDocsRecyclerView, mPhysicalExamsRecyclerView, cc_recyclerview;
    private RecyclerView.LayoutManager mAdditionalDocsLayoutManager, mPhysicalExamsLayoutManager;
    private RecyclerView.LayoutManager cc_recyclerview_gridlayout;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    private ComplaintHeaderAdapter cc_adapter;

    boolean hasLicense = false;
    private String hasPrescription = "";
    private boolean isRespiratory = false, uploaded = false, downloaded = false;
    Button uploadButton, btn_vs_print, btn_vs_share;
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
    TextView complaintView, patientReports_txtview, patientDenies_txtview;
    TextView famHistView;
    TextView patHistView;
    TextView physFindingsView;
    TextView mDoctorTitle;
    TextView mDoctorName;
    TextView mCHWname;
    TextView add_docs_title, vd_addnotes_value;
    String addnotes_value = "";

    TextView respiratory;
    TextView respiratoryText;
    TextView tempfaren;
    TextView tempcel;
    String medHistory;
    String baseDir;
    String filePathPhyExam;
    File obsImgdir;
    String gender_tv;
    String mFileName = "config.json";
    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    String speciality_selected = "";
    private TextView physcialExaminationDownloadText, vd_special_value;
    NetworkChangeReceiver receiver;
    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";
    String encounterUuid;
    Spinner speciality_spinner;
    SwitchMaterial flag;
    private Handler mBackgroundHandler;
    private List<DocumentObject> rowListItem;
    String sign_url;

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;
    ImageButton editAddDocs;
    ImageButton btn_up_special_vd_header;

    ImageView profile_image, downloadbtn;
    String profileImage = "";
    String profileImage1 = "";
    ImagesDAO imagesDAO = new ImagesDAO();
    private WebView mWebView;
    public static String prescription1;
    public static String prescription2;
    private CardView doc_speciality_card, special_vd_card, addnotes_vd_card;
    private VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();
    private ImageButton backArrow, priority_hint, refresh;
    private NetworkUtils networkUtils;
    private static final int SCHEDULE_LISTING_INTENT = 2001;
    Button btnAppointment;

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
        networkUtils = new NetworkUtils(this, this);
        fetchingIntent();
        setViewsData();
        expandableCardVisibilityHandling();

    }

    private void fetchingIntent() {
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager1 = new SessionManager(this);
        appLanguage = sessionManager1.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        // todo: uncomment this block later for testing it is commented.
        final Intent intent = this.getIntent(); // The intent was passed to the activity
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

/*
            Log.v(TAG, "inte_value: \n: " + patientUuid + "\n" +
                    visitUuid + "\n" +
                    patientGender + "\n" +
                    encounterVitals + "\n" +
                    encounterUuidAdultIntial + "\n" +
                    EncounterAdultInitial_LatestVisit + "\n" +
                    patientName + "\n" +
                    float_ageYear_Month + "\n" +
                    intentTag + "\n" +
                    isPastVisit + "\n");
*/
            queryData(String.valueOf(patientUuid));
        }

       /* // todo: testing - start
        patientUuid = "5beb27c8-4bae-4d8e-91a1-1fa5dabb51c8";
        visitUuid = "f133d3ca-f448-44c0-b1b4-5889f85e7d5a";
        patientGender = "M";
        encounterVitals = "1c4b19a7-0c1d-48a8-a0a0-a9222018ccef";
        encounterUuidAdultIntial = "e4e5b1f1-72cf-437f-978f-c42a4a0d1183";
        EncounterAdultInitial_LatestVisit = "";
        mSharedPreference = this.getSharedPreferences(
                "visit_summary", Context.MODE_PRIVATE);
        patientName = "Testing Banana";
        float_ageYear_Month = 48.0f;
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
        // todo: testing - end*/


        // receiver
        registerBroadcastReceiverDynamically();
        registerDownloadPrescription();
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        // past visit checking based on intent - start
        if (isPastVisit) {
            editVitals.setVisibility(View.GONE);
            editComplaint.setVisibility(View.GONE);
            editPhysical.setVisibility(View.GONE);
            editFamHist.setVisibility(View.GONE);
            editMedHist.setVisibility(View.GONE);
            editAddDocs.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
//            btnSignSubmit.setVisibility(View.GONE);// todo: uncomment handle later.
            invalidateOptionsMenu();
        } else {
            if (visitUuid != null && !visitUuid.isEmpty()) {


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
        }
        // past visit checking based on intent - end

        showVisitID();  // display visit ID.

        if (intentTag != null && !intentTag.isEmpty()) {
            // Edit btn visibility based on user coming from Visit Details screen - Start
            if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
                editVitals.setVisibility(View.GONE);
                editComplaint.setVisibility(View.GONE);
                editPhysical.setVisibility(View.GONE);
                editFamHist.setVisibility(View.GONE);
                editMedHist.setVisibility(View.GONE);
                editAddDocs.setVisibility(View.GONE);
                add_additional_doc.setVisibility(View.GONE);

                btn_bottom_printshare.setVisibility(View.VISIBLE);
                btn_bottom_vs.setVisibility(View.GONE);

                doc_speciality_card.setVisibility(View.GONE);
                special_vd_card.setVisibility(View.VISIBLE);
                vs_add_notes.setVisibility(View.GONE);
                addnotes_vd_card.setVisibility(View.VISIBLE);

                addnotes_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, ADDITIONAL_NOTES);
                if (!addnotes_value.equalsIgnoreCase("")) {
                    vd_addnotes_value.setText(addnotes_value);
                } else {
                    addnotes_value = "No notes added for Doctor.";
                    vd_addnotes_value.setText(addnotes_value);
                }
            } else {
                editVitals.setVisibility(View.VISIBLE);
                editComplaint.setVisibility(View.VISIBLE);
                editPhysical.setVisibility(View.VISIBLE);
                editFamHist.setVisibility(View.VISIBLE);
                editMedHist.setVisibility(View.VISIBLE);
                editAddDocs.setVisibility(View.VISIBLE);
                add_additional_doc.setVisibility(View.VISIBLE);

                btn_bottom_printshare.setVisibility(View.GONE);
                btn_bottom_vs.setVisibility(View.VISIBLE);

                doc_speciality_card.setVisibility(View.VISIBLE);
                special_vd_card.setVisibility(View.GONE);
                vs_add_notes.setVisibility(View.VISIBLE);
                addnotes_vd_card.setVisibility(View.GONE);
            }
            // Edit btn visibility based on user coming from Visit Details screen - End

        }

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
                vd_special_header_expandview.setVisibility(View.VISIBLE);
                vd_addnotes_header_expandview.setVisibility(View.VISIBLE);
            } else {
                openall_btn.setImageDrawable(getResources().getDrawable(R.drawable.open_all_btn));
                vs_vitals_header_expandview.setVisibility(View.GONE);
                vs_visitreason_header_expandview.setVisibility(View.GONE);
                vs_phyexam_header_expandview.setVisibility(View.GONE);
                vs_medhist_header_expandview.setVisibility(View.GONE);
                vd_special_header_expandview.setVisibility(View.GONE);
                vd_addnotes_header_expandview.setVisibility(View.GONE);
            }

        });

        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

/*
        btn_up_vitals_header.setOnClickListener(v -> {
            if (vs_vitals_header_expandview.getVisibility() == View.VISIBLE)
                vs_vitals_header_expandview.setVisibility(View.GONE);
            else
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
        });
*/
        vitals_header_relative.setOnClickListener(v -> {
            if (vs_vitals_header_expandview.getVisibility() == View.VISIBLE)
                vs_vitals_header_expandview.setVisibility(View.GONE);
            else
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
        });

/*
        btn_up_visitreason_header.setOnClickListener(v -> {
            if (vs_visitreason_header_expandview.getVisibility() == View.VISIBLE)
                vs_visitreason_header_expandview.setVisibility(View.GONE);
            else
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
        });
*/
        chiefcomplaint_header_relative.setOnClickListener(v -> {
            if (vs_visitreason_header_expandview.getVisibility() == View.VISIBLE)
                vs_visitreason_header_expandview.setVisibility(View.GONE);
            else
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
        });

/*
        btn_up_phyexam_header.setOnClickListener(v -> {
            if (vs_phyexam_header_expandview.getVisibility() == View.VISIBLE)
                vs_phyexam_header_expandview.setVisibility(View.GONE);
            else
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
        });
*/
        physExam_header_relative.setOnClickListener(v -> {
            if (vs_phyexam_header_expandview.getVisibility() == View.VISIBLE)
                vs_phyexam_header_expandview.setVisibility(View.GONE);
            else
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
        });

/*
        btn_up_medhist_header.setOnClickListener(v -> {
            if (vs_medhist_header_expandview.getVisibility() == View.VISIBLE)
                vs_medhist_header_expandview.setVisibility(View.GONE);
            else
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
        });
*/
        pathistory_header_relative.setOnClickListener(v -> {
            if (vs_medhist_header_expandview.getVisibility() == View.VISIBLE)
                vs_medhist_header_expandview.setVisibility(View.GONE);
            else
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
        });

/*
        btn_up_special_vd_header.setOnClickListener(v -> {
            if (vd_special_header_expandview.getVisibility() == View.VISIBLE)
                vd_special_header_expandview.setVisibility(View.GONE);
            else
                vd_special_header_expandview.setVisibility(View.VISIBLE);
        });
*/
        special_vd_header_relative.setOnClickListener(v -> {
            if (vd_special_header_expandview.getVisibility() == View.VISIBLE)
                vd_special_header_expandview.setVisibility(View.GONE);
            else
                vd_special_header_expandview.setVisibility(View.VISIBLE);
        });

/*
        btn_up_addnotes_vd_header.setOnClickListener(v -> {
            if (vd_addnotes_header_expandview.getVisibility() == View.VISIBLE)
                vd_addnotes_header_expandview.setVisibility(View.GONE);
            else
                vd_addnotes_header_expandview.setVisibility(View.VISIBLE);
        });
*/
        addnotes_vd_header_relative.setOnClickListener(v -> {
            if (vd_addnotes_header_expandview.getVisibility() == View.VISIBLE)
                vd_addnotes_header_expandview.setVisibility(View.GONE);
            else
                vd_addnotes_header_expandview.setVisibility(View.VISIBLE);
        });
    }

    private void setViewsData() {
        physicalDoumentsUpdates();

        if (patientUuid != null && patientUuid.isEmpty()) {
            queryData(String.valueOf(patientUuid));

            // Patient Photo
            //1.
            try {
                profileImage = imagesDAO.getPatientProfileChangeTime(patientUuid);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        //2.
        if (patient.getPatient_photo() == null || patient.getPatient_photo().equalsIgnoreCase("")) {
            if (NetworkConnection.isOnline(context)) {
                profilePicDownloaded(patient);
            }
        }
        //3.
        if (!profileImage.equalsIgnoreCase(profileImage1)) {
            if (NetworkConnection.isOnline(context)) {
                profilePicDownloaded(patient);
            }
        }

        if (patient.getPatient_photo() != null) {
            Glide.with(context)
                    .load(patient.getPatient_photo())
                    .thumbnail(0.3f)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profile_image);
        } else {
            profile_image.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
        }
        // photo - end

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
        if (height.getValue() != null) {
            if (height.getValue().trim().equals("0")) {
                heightView.setText("-");
            } else {
                heightView.setText(height.getValue());
            }
        }

        weightView.setText(weight.getValue());

        Log.d(TAG, "onCreate: " + weight.getValue());
        if (weight.getValue() != null) {
            String mWeight = weight.getValue().split(" ")[0];
            String mHeight = height.getValue().split(" ")[0];
            if ((mHeight != null && mWeight != null) && !mHeight.isEmpty() && !mWeight.isEmpty()) {
                double numerator = Double.parseDouble(mWeight) * 10000;
                double denominator = Double.parseDouble(mHeight) * Double.parseDouble(mHeight);
                double bmi_value = numerator / denominator;
                mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);
            } else {
                mBMI = "";
            }
            bmiView.setText(mBMI);

        }

        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        if (bpText.equals("/")) {  //when new patient is being registered we get / for BP
            bpView.setText("");
        } else if (bpText.equalsIgnoreCase("null/null")) {
            //when we setup app and get data from other users, we get null/null from server...
            bpView.setText("");
        } else {
            bpView.setText(bpText);
        }

        pulseView.setText(pulse.getValue());
        spO2View.setText(spO2.getValue());

        if (isRespiratory) {
            respiratoryText.setVisibility(View.VISIBLE);
            respiratory.setVisibility(View.VISIBLE);
        } else {
            respiratoryText.setVisibility(View.GONE);
            respiratory.setVisibility(View.GONE);
        }
        respiratory.setText(resp.getValue());
        // vitals values set - end

        // complaints data
        if (complaint.getValue() != null) {
            String value = complaint.getValue();
            Log.v(TAG, "complaint: " + value);
            String valueArray[] = value.replace("<br>►Associated symptoms: <br>", "►<b>Associated symptoms</b>: <br/>")
                    .split("►<b>Associated symptoms</b>: <br/>");

            String[] headerchips = valueArray[0].split("►");
            List<String> cc_tempvalues = new ArrayList<>(Arrays.asList(headerchips));
            List<String> cc_list = new ArrayList<>();

            for (int i = 0; i < cc_tempvalues.size(); i++) {
                if (!cc_tempvalues.get(i).equalsIgnoreCase(""))
                    cc_list.add(cc_tempvalues.get(i).substring(0, headerchips[i].indexOf(":")));
            }

            cc_recyclerview_gridlayout = new GridLayoutManager(this, 2);
            cc_recyclerview.setLayoutManager(cc_recyclerview_gridlayout);
            cc_adapter = new ComplaintHeaderAdapter(this, cc_list);
            cc_recyclerview.setAdapter(cc_adapter);

            String patientReports = "No data added.";
            String patientDenies = "No data added.";

            if (valueArray[0].contains("• Patient reports") && valueArray[0].contains("• Patient denies")) {
                String assoValueBlock[] = valueArray[0].replace("• Patient denies -<br>", "• Patient denies -<br/>")
                        .split("• Patient denies -<br/>");

                // index 0 - Reports
                String reports[] = assoValueBlock[0].replace("• Patient reports -<br>", "• Patient reports -<br/>")
                        .split("• Patient reports -<br/>");
                patientReports = reports[1];
                patientDenies = assoValueBlock[1];
                complaintView.setText(Html.fromHtml(valueArray[0])); // todo: uncomment later
            } else if (valueArray[0].contains("• Patient reports")) {
                // todo: handle later -> comment added on 14 nov 2022
            }

            // todo: testing:
            /*String data = "►Abdominal Pain: <br><span style=\"color:#7F7B92\">• Site</span> &emsp;&emsp; Upper (R) - Right Hypochondrium.<br>" +
                    "• Pain does not radiate.<br>• 4 Hours.<br><span style=\"color:#7F7B92\">• Onset</span> &emsp;&emsp; Gradual.<br><span style=\"color:#7F7B92\">• Timing</span> &emsp;&emsp; Morning.<br>" +
                    "<span style=\"color:#7F7B92\">• Character of the pain*</span> &emsp;&emsp; Constant.<br><span style=\"color:#7F7B92\">• Severity</span> &emsp;&emsp; Mild, 1-3.<br>" +
                    "<span style=\"color:#7F7B92\">• Exacerbating Factors</span> &emsp;&emsp; Hunger.<br><span style=\"color:#7F7B92\">• Relieving Factors</span> &emsp;&emsp; Food.<br><span style=\"color:#7F7B92\">• Prior treatment sought</span> &emsp;&emsp; None.";
            complaintView.setText(Html.fromHtml(data));*/
            // todo: testin end

            // associated symp.
            patientReports_txtview.setText(Html.fromHtml(patientReports));
            patientDenies_txtview.setText(Html.fromHtml(patientDenies));
        }
        // complaints data - end

        // phys exam data
        if (phyExam.getValue() != null) {
            String value = phyExam.getValue();
            String valueArray[] = value.replace("General exams: <br>", "<b>General exams: </b><br/>")
                    .split("<b>General exams: </b><br/>");
            physFindingsView.setText(Html.fromHtml(valueArray[1].replaceFirst("<b>", "<br/><b>")));
        }
        //image download for physcialExamination documents
        Paint p = new Paint();
        physcialExaminationDownloadText.setPaintFlags(p.getColor());
        physcialExaminationDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        physcialExaminationImagesDownload();
        // phys exam data - end

        // medical history data

        // past medical hist
        if (patHistory.getValue() != null)
            patHistView.setText(Html.fromHtml(patHistory.getValue()));
        // past medical hist - end

        // family history
        if (famHistory.getValue() != null)
            famHistView.setText(Html.fromHtml(famHistory.getValue()));
        // family history - end
        // medical history data - end

        // additional doc data
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();

        if (encounterUuidAdultIntial != null) {
            try {
                fileuuidList = imagesDAO.getImageUuid(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD);
                for (String fileuuid : fileuuidList) {
                    String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                    if (new File(filename).exists()) {
                        fileList.add(new File(filename));
                    }
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mAdditionalDocsRecyclerView.setHasFixedSize(true);
            mAdditionalDocsRecyclerView.setLayoutManager(linearLayoutManager);

            if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem,
                        AppConstants.IMAGE_PATH, this, true);
            } else {
                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem,
                        AppConstants.IMAGE_PATH, this, false);
            }

            mAdditionalDocsRecyclerView.setAdapter(recyclerViewAdapter);
            add_docs_title.setText("Add additional document (" + recyclerViewAdapter.getItemCount() + ")");


            editAddDocs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*Intent addDocs = new Intent(VisitSummaryActivity_New.this, AdditionalDocumentsActivity.class);
                addDocs.putExtra("patientUuid", patientUuid);
                addDocs.putExtra("visitUuid", visitUuid);
                addDocs.putExtra("encounterUuidVitals", encounterVitals);
                addDocs.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                startActivity(addDocs);*/
                    selectImage();
                }
            });
            // additional doc data - end
        }


        // speciality data
        //if row is present i.e. if true is returned by the function then the spinner will be disabled.
        Log.d("visitUUID", "onCreate_uuid: " + visitUuid);
        isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
        if (isVisitSpecialityExists)
            speciality_spinner.setEnabled(false);

        //spinner is being populated with the speciality values...
        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();

        List<String> items = providerAttributeLIstDAO.getAllValues();
        Log.d("specc", "spec: " + visitUuid);
        String special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, SPECIALITY);
        //Hashmap to List<String> add all value
        ArrayAdapter<String> stringArrayAdapter;

        //  if(getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("en")) {
        if (items != null) {
            items.add(0, getString(R.string.select_specialization_text));
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

        if (special_value != null) {
//            int spinner_position = stringArrayAdapter.getPosition(special_value);
//            speciality_spinner.setSelection(spinner_position);

            vd_special_value.setText(" " + Node.bullet + "  " + special_value);
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
        // todo: speciality code comes in upload btn as well so add that too....later...
        // speciality data - end

        if (visitUuid != null) {
            // Priority data
            EncounterDAO encounterDAO = new EncounterDAO();
            String emergencyUuid = "";
            try {
                emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
                flag.setChecked(true);
                flag.setEnabled(false);
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
        // Priority data - end

        // edit listeners - start
        editVitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(VisitSummaryActivity_New.this, VitalsActivity.class);
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

        // complaint
        editComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder complaintDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                complaintDialog.setTitle(getString(R.string.visit_summary_complaint));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                complaintDialog.setView(convertView);

                final TextView complaintText = convertView.findViewById(R.id.textView_entry);
                if (complaint.getValue() != null) {
                    complaintText.setText(Html.fromHtml(complaint.getValue()));
                }
                complaintText.setEnabled(false);

                complaintDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        if (complaint.getValue() != null) {
                            dialogEditText.setText(Html.fromHtml(complaint.getValue()));
                        } else {
                            dialogEditText.setText("");
                        }
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                complaint.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
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
                        AlertDialog alertDialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });

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

                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, ComplaintNodeActivity.class);
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
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });

        // physical exam
        editPhysical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder physicalDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
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
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        if (phyExam.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(phyExam.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
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
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = textInput.show();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, dialog);
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
                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, PhysicalExamActivity.class);
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
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });

        // medical history
        editMedHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder historyDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
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
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
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
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, dialog);
                        dialogInterface.dismiss();
                    }
                });

                historyDialog.setNegativeButton(getString(R.string.generic_erase_redo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, PastMedicalHistoryActivity.class);
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
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));
                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });

        // family history
        editFamHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder famHistDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                //final MaterialAlertDialogBuilder famHistDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this,R.style.AlertDialogStyle);
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
                        MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        // final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        if (famHistory.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(famHistory.getValue()));
                        else
                            dialogEditText.setText("");
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
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
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = textInput.show();
                        dialogInterface.dismiss();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
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

                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, FamilyHistoryActivity.class);
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
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));
                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });
        // edit listeners - end

        // upload btn click - start
      /*  uploadButton.setOnClickListener(v -> {
            visitSendDialog(context, getResources().getDrawable(R.drawable.dialog_close_visit_icon), "Send visit?",
                    "Are you sure you want to send the visit to the doctor?",
                    "Yes", "No");
        });*/


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitSendDialog(context, getResources().getDrawable(R.drawable.dialog_close_visit_icon), "Send visit?",
                        "Are you sure you want to send the visit to the doctor?",
                        "Yes", "No");
            }
        });
        // upload btn click - end

        // json based presc header - start
        jsonBasedPrescTitle();
        // json based presc header - end

        downloadbtn.setOnClickListener(v -> {
            try {
                doWebViewPrint_downloadBtn();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
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

    private String showVisitID() {
        if (visitUUID != null && !visitUUID.isEmpty()) {
            String hideVisitUUID = visitUUID;
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            visitView.setText("XXXX" + hideVisitUUID);
        }
        return visitView.getText().toString();
    }

    private void showSelectSpeciliatyErrorDialog() {
        TextView t = (TextView) speciality_spinner.getSelectedView();
        t.setError(getString(R.string.please_select_specialization_msg));
        t.setTextColor(Color.RED);

        AlertDialog.Builder builder = new AlertDialog.Builder(VisitSummaryActivity_New.this)
                .setMessage(getResources().getString(R.string.please_select_specialization_msg))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.generic_ok),
                        new DialogInterface.OnClickListener() {
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


    /**
     * Open dialog to Select douments from Image and Camera as Per the Choices
     */
    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(VisitSummaryActivity_New.this);
        builder.setTitle(R.string.additional_doc_image_picker_title);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent cameraIntent = new Intent(VisitSummaryActivity_New.this, CameraActivity.class);
                    String imageName = UUID.randomUUID().toString();
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);

                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void initUI() {
        // textview - start
        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);
        profile_image = findViewById(R.id.profile_image);
        downloadbtn = findViewById(R.id.downloadbtn);
        nameView = findViewById(R.id.textView_name_value);
        genderView = findViewById(R.id.textView_gender_value);
        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        visitView = findViewById(R.id.textView_visit_value);
        additional_notes_edittext = findViewById(R.id.additional_notes_edittext);
        // textview - end

        // up-down btn - start
        btn_up_header = findViewById(R.id.btn_up_header);
        openall_btn = findViewById(R.id.openall_btn);
        btn_up_vitals_header = findViewById(R.id.btn_up_vitals_header);
        vitals_header_relative = findViewById(R.id.vitals_header_relative);
        parentLayout = findViewById(R.id.parentLayout);
        btn_up_visitreason_header = findViewById(R.id.btn_up_visitreason_header);
        chiefcomplaint_header_relative = findViewById(R.id.chiefcomplaint_header_relative);
        btn_up_phyexam_header = findViewById(R.id.btn_up_phyexam_header);
        physExam_header_relative = findViewById(R.id.physExam_header_relative);
        btn_up_medhist_header = findViewById(R.id.btn_up_medhist_header);
        pathistory_header_relative = findViewById(R.id.pathistory_header_relative);
        btn_up_special_vd_header = findViewById(R.id.btn_up_special_vd_header);
        special_vd_header_relative = findViewById(R.id.special_vd_header_relative);
        btn_up_addnotes_vd_header = findViewById(R.id.btn_up_addnotes_vd_header);
        addnotes_vd_header_relative = findViewById(R.id.addnotes_vd_header_relative);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_vitals_header_expandview = findViewById(R.id.vs_vitals_header_expandview);
        vs_visitreason_header_expandview = findViewById(R.id.vs_visitreason_header_expandview);
        vs_phyexam_header_expandview = findViewById(R.id.vs_phyexam_header_expandview);
        vs_medhist_header_expandview = findViewById(R.id.vs_medhist_header_expandview);
        vd_special_header_expandview = findViewById(R.id.vd_special_header_expandview);
        vd_addnotes_header_expandview = findViewById(R.id.vd_addnotes_header_expandview);
        vs_add_notes = findViewById(R.id.vs_add_notes);
        vd_addnotes_value = findViewById(R.id.vd_addnotes_value);
        // up-down btn - end

        // vitals ids
        heightView = findViewById(R.id.textView_height_value);
        weightView = findViewById(R.id.textView_weight_value);
        pulseView = findViewById(R.id.textView_pulse_value);
        bpView = findViewById(R.id.textView_bp_value);
        tempView = findViewById(R.id.textView_temp_value);

        vd_special_value = findViewById(R.id.vd_special_value);
        doc_speciality_card = findViewById(R.id.doc_speciality_card);
        addnotes_vd_card = findViewById(R.id.addnotes_vd_card);
        special_vd_card = findViewById(R.id.special_vd_card);
        priority_hint = findViewById(R.id.priority_hint);

        priority_hint.setOnClickListener(v -> {
            Toast.makeText(context, R.string.priority_hint, Toast.LENGTH_SHORT).show();
//            Snackbar.make(parentLayout, R.string.priority_hint, Snackbar.LENGTH_SHORT).show();
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                /*Intent intent = new Intent(VisitSummaryActivity_New.this, HomeScreenActivity_New.class);
                startActivity(intent);*/
            }
        });


        tempfaren = findViewById(R.id.textView_temp_faren);
        tempcel = findViewById(R.id.textView_temp);
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                                String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity_New.this, mFileName)));
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
        // vitals ids - end

        // complaint ids
        cc_recyclerview = findViewById(R.id.cc_recyclerview);


        complaintView = findViewById(R.id.textView_content_complaint);
        patientReports_txtview = findViewById(R.id.patientReports_txtview);
        patientDenies_txtview = findViewById(R.id.patientDenies_txtview);
        // complaint ids - end

        // Phys exam ids
        physFindingsView = findViewById(R.id.physFindingsView);
        mPhysicalExamsRecyclerView = findViewById(R.id.recy_physexam);
        physcialExaminationDownloadText = findViewById(R.id.physcial_examination_download);
        // Phys exam ids - end

        // medical history
        famHistView = findViewById(R.id.textView_content_famhist);
        patHistView = findViewById(R.id.textView_content_pathist);
        // medical history - end

        // additonal doc
        add_docs_title = findViewById(R.id.add_docs_title);
        mAdditionalDocsRecyclerView = findViewById(R.id.recy_additional_documents);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        // additonal doc - end

        // speciality ids
        speciality_spinner = findViewById(R.id.speciality_spinner);
        // speciality ids - end

        // priority id
        flag = findViewById(R.id.flaggedcheckbox);
        // priority id - end

        // edit - start
        editVitals = findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = findViewById(R.id.imagebutton_edit_pathist);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        // edit - end

        // Bottom Buttons - start
        btn_bottom_printshare = findViewById(R.id.btn_bottom_printshare);   // linear: print - share
        btn_vs_print = findViewById(R.id.btn_vs_print);   // print
        btn_vs_share = findViewById(R.id.btn_vs_share);   // share

        btn_bottom_vs = findViewById(R.id.btn_bottom_vs);   // appointment - upload
        uploadButton = findViewById(R.id.btn_vs_sendvisit);

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


        // file set
        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        obsImgdir = new File(AppConstants.IMAGE_PATH);

        add_additional_doc = findViewById(R.id.add_additional_doc);

        // navigation for book appointmnet
        btnAppointment = findViewById(R.id.btn_vs_appointment);
        btnAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(VisitSummaryActivity_New.this, ScheduleAppointmentActivity_New.class)
                        .putExtra("visitUuid", visitUuid)
                        .putExtra("patientUuid", patientUuid)
                        .putExtra("patientName", patientName)
                        .putExtra("appointmentId", 0)
                        .putExtra("actionTag", "visitSummary")
                        .putExtra("openMrsId", patient.getOpenmrs_id())
                        .putExtra("speciality", speciality_selected), SCHEDULE_LISTING_INTENT
                );
                finish();

            }
        });
    }

    private void sharePresc() {
        if (hasPrescription.equalsIgnoreCase("true")) {
            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
            final LayoutInflater inflater = LayoutInflater.from(context);
            View convertView = inflater.inflate(R.layout.dialog_sharepresc, null);
            alertdialogBuilder.setView(convertView);

            EditText editText = convertView.findViewById(R.id.editText_mobileno);
            Button sharebtn = convertView.findViewById(R.id.sharebtn);

           /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            EditText editText = new EditText(this);
            editText.setInputType(InputType.TYPE_CLASS_PHONE);

            InputFilter inputFilter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    return null;
                }
            };*/

            String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
            String whatsapp_url = partial_whatsapp_presc_url.concat(visitUuid);
            editText.setText(patient.getPhone_number());

//                    Spanned hyperlink_whatsapp = HtmlCompat.fromHtml("<a href=" + whatsapp_url + ">Click Here</a>", HtmlCompat.FROM_HTML_MODE_COMPACT);

          //  editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(15)});
           /* LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editText.setLayoutParams(layoutParams);
            alertDialog.setView(editText);
*/
         //   alertDialog.setMessage(getResources().getString(R.string.enter_mobile_number_to_share_prescription));
            sharebtn.setOnClickListener(v -> {
                if (!editText.getText().toString().equalsIgnoreCase("")) {
                    String phoneNumber = /*"+91" +*/ editText.getText().toString();
                    String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
                            + whatsapp_url + getString(R.string.and_enter_your_patient_id) + idView.getText().toString();

                    // Toast.makeText(context, R.string.whatsapp_presc_toast, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(
                                    String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                            phoneNumber, whatsappMessage))));

                    // isreturningWhatsapp = true;

                } else {
                    Toast.makeText(context, getResources().getString(R.string.please_enter_mobile_number),
                            Toast.LENGTH_SHORT).show();
                }

            });

            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            alertDialog.show();

//            alertDialog.setPositiveButton(getResources().getString(R.string.share),
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            if (!editText.getText().toString().equalsIgnoreCase("")) {
//                                String phoneNumber = /*"+91" +*/ editText.getText().toString();
//                                String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
//                                        + whatsapp_url + getString(R.string.and_enter_your_patient_id) + idView.getText().toString();
//
//                                // Toast.makeText(context, R.string.whatsapp_presc_toast, Toast.LENGTH_LONG).show();
//                                startActivity(new Intent(Intent.ACTION_VIEW,
//                                        Uri.parse(
//                                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
//                                                        phoneNumber, whatsappMessage))));
//
//                                // isreturningWhatsapp = true;
//
//                            } else {
//                                Toast.makeText(context, getResources().getString(R.string.please_enter_mobile_number),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

           /* AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);*/
        } else {
            /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getResources().getString(R.string.download_prescription_first_before_sharing));
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
            IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);*/

            Toast.makeText(context, getResources().getString(R.string.download_prescription_first_before_sharing), Toast.LENGTH_SHORT).show();
        }
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
            visitUploadBlock();
        });

        alertDialog.show();
    }

    private void visitUploadBlock() {
        Log.d("visitUUID", "upload_click: " + visitUUID);

        isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
        if (speciality_spinner.getSelectedItemPosition() != 0) {
            VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();
            boolean isUpdateVisitDone = false;
            try {
                if (!isVisitSpecialityExists) {
                    isUpdateVisitDone = visitAttributeListDAO.insertVisitAttributes(visitUuid, speciality_selected, SPECIALITY);
                }
                Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
            } catch (DAOException e) {
                e.printStackTrace();
                Log.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
            }

            // Additional Notes - Start
            try {
                String addnotes = additional_notes_edittext.getText().toString();
                Log.v("addnotes", "addnotes: " + addnotes);
                if (!addnotes.equalsIgnoreCase("") && addnotes != null)
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, addnotes, ADDITIONAL_NOTES);
                else
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, "No Data", ADDITIONAL_NOTES);
            } catch (DAOException e) {
                e.printStackTrace();
                Log.v("hospitalType", "hospitalType: " + e.getMessage());
            }
            // Additional Notes - End

            if (isVisitSpecialityExists)
                speciality_spinner.setEnabled(false);

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
                            // ie. visit is uploded successfully.
                            visitSentSuccessDialog(context, getResources().getDrawable(R.drawable.dialog_visit_sent_success_icon),
                                    "Visit successfully sent!",
                                    "Patient's visit has been successfully sent to the doctor.",
                                    "Okay");


                            AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_upload),
                                    getString(R.string.visit_uploaded_successfully), 3, VisitSummaryActivity_New.this);
                            isSynedFlag = "1";
                            //
                            showVisitID();
                            Log.d("visitUUID", "showVisitID: " + visitUUID);
                            isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
                            if (isVisitSpecialityExists)
                                speciality_spinner.setEnabled(false);
                        } else {
                            AppConstants.notificationUtils.DownloadDone(patientName + " " +
                                    getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity_New.this);

                        }
                        uploaded = true;
                    }
                }, 4000);
            } else {
                AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity_New.this);
            }
        } else {
            showSelectSpeciliatyErrorDialog();
        }
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
            //commented to stop navigation bcz navigation from appointment
          /*  Intent intent = new Intent(VisitSummaryActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);*/
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

    @Override
    public void deleteNotifi_Item(List<NotificationModel> list, int position) {

    }

    @Override
    public void deleteAddDoc_Item(List<DocumentObject> documentList, int position) {
        documentList.remove(position);
        add_docs_title.setText("Add additional document (" + recyclerViewAdapter.getItemCount() + ")");
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
            //   ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
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
              /*  if (diagnosisCard.getVisibility() != View.VISIBLE) {
                    diagnosisCard.setVisibility(View.VISIBLE);
                }
                diagnosisTextView.setText(diagnosisReturned);*/
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
               /* if (prescriptionCard.getVisibility() != View.VISIBLE) {
                    prescriptionCard.setVisibility(View.VISIBLE);
                }
                prescriptionTextView.setText(rxReturned);*/
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
              /*  if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }*/
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
               /* medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                Log.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
              /*  if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }
                requestedTestsTextView.setText(testsReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.ADDITIONAL_COMMENTS: {

//                additionalCommentsCard.setVisibility(View.GONE);

                if (!additionalReturned.isEmpty()) {
                    additionalReturned = additionalReturned + "," + value;
                } else {
                    additionalReturned = value;
                }
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
              /*  if (followUpDateCard.getVisibility() != View.VISIBLE) {
                    followUpDateCard.setVisibility(View.VISIBLE);
                }
                followUpDateTextView.setText(followUpDate);*/
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

            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:0px;\">" +
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
                patient.setUuid(patientUuid);
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


    /*temperature convert*/
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

    /*PhysExam images downlaod*/
    private void physcialExaminationImagesDownload() {
        ImagesDAO imagesDAO = new ImagesDAO();
        if (encounterUuidAdultIntial != null) {
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
        }

        physcialExaminationDownloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_PE);
                physcialExaminationDownloadText.setVisibility(View.GONE);
            }
        });
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
    /*PhysExam images downlaod - end*/

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
        super.onStart();
        registerDownloadPrescription();
        callBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter(FILTER));
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
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

        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

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
    protected void onResume() {
        super.onResume();

        //get from encountertbl from the encounter
       /* if (visitnoteencounteruuid.equalsIgnoreCase("")) {
            visitnoteencounteruuid = getStartVisitNoteEncounterByVisitUUID(visitUuid);
        }*/ // todo: uncomment and handle later....

        if (downloadPrescriptionService == null) {
            registerDownloadPrescription();
        }

        callBroadcastReceiver();

        // showing additional images...
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

          /*  HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mAdditionalDocsLayoutManager = new LinearLayoutManager(VisitSummaryActivity_New.this,
                    LinearLayoutManager.HORIZONTAL, false);
            mAdditionalDocsRecyclerView.setLayoutManager(mAdditionalDocsLayoutManager);
            mAdditionalDocsRecyclerView.setAdapter(horizontalAdapter);*/

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mAdditionalDocsRecyclerView.setHasFixedSize(true);
            mAdditionalDocsRecyclerView.setLayoutManager(linearLayoutManager);

            if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem,
                        AppConstants.IMAGE_PATH, this, true);
            } else {
                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem,
                        AppConstants.IMAGE_PATH, this, false);
            }

            mAdditionalDocsRecyclerView.setAdapter(recyclerViewAdapter);
            add_docs_title.setText("Add additional document (" + recyclerViewAdapter.getItemCount() + ")");

            if (recyclerViewAdapter != null) {
                if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
                    recyclerViewAdapter.hideCancelBtnAddDoc(true);
                } else {
                    recyclerViewAdapter.hideCancelBtnAddDoc(false);
                }
            }

        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
    }

    // Netowork reciever
    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }
    }

    // handle message
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
                //   addDownloadButton();
                //if any obs  found then end the visit
                //endVisit();
            } else {
                Log.i(TAG, "found sothing for test");
            }
        }
    }

    // add downlaod button
    private void addDownloadButton() {
      /*  if (!downloadButton.isEnabled()) {
            downloadButton.setEnabled(true);
            downloadButton.setVisibility(View.VISIBLE);
        }*/
    }

    // speciality alrady exists checking

    /**
     * @param uuid the visit uuid of the patient visit records is passed to the function.
     * @return boolean value will be returned depending upon if the row exists in the tbl_visit_attribute tbl
     */
    private boolean speciality_row_exist_check(String uuid) {
        boolean isExists = false;

        if (uuid != null) {
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

        }
        return isExists;

    }

    // start activity for result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                saveImage(mCurrentPhotoPath);
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.v("path", picturePath + "");

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
            }
        }
    }

    // save image
    private void saveImage(String picturePath) {
        Log.v("AdditionalDocuments", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        if (photo.exists()) {
            try {
                long length = photo.length();
                length = length / 1024;
                Log.e("------->>>>", length + "");
            } catch (Exception e) {
                System.out.println("File not found : " + e.getMessage() + e);
            }

            recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    // compress image

    /**
     * @param filePath Final Image path to compress.
     */
    // TODO: crash as there is no permission given in setup app section for firsttime user.
    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean flag = BitmapUtils.fileCompressed(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flag) {
                            saveImage(filePath);
                        } else
                            Toast.makeText(VisitSummaryActivity_New.this, getString(R.string.something_went_wrong),
                                    Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    // update image database
    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageuuid, encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    // udpate database block

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

    // profile pic downalod
    public void profilePicDownloaded(Patient patientModel) {
        sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(patientModel.getUuid());
        Logger.logD("TAG", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, patientModel.getUuid());
                        Logger.logD("TAG", file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("TAG", "complete" + patientModel.getPatient_photo());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(patientModel.getUuid(),
                                    AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg")
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(profile_image);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg", patientModel.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }

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
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3); // todo: uncomment later.
             //   mComplaint = "Test Complaint";
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
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

        String advice_web = stringToWeb(adviceReturned);
        //    String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
/*        String advice_doctor__ = medicalAdviceTextView.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {


//        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
//        Log.d("Hyperlink", "hyper_print: " + advice_web);
//        String advice_split = new StringBuilder(medicalAdviceTextView.getText().toString())
//                .delete(medicalAdviceTextView.getText().toString().indexOf("Start"),
//                        medicalAdviceTextView.getText().toString().lastIndexOf("User")+6).toString();
            //lastIndexOf("User") will give index of U of User
            //so the char this will return is U...here User + 6 will return W eg: User\n\nWatch as +6 will give W

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();
            //lastIndexOf("Doctor_") will give index of D of Doctor_
            //so the char this will return is D...here Doctor_ + 9 will return W eg: Doctor_\n\nWatch as +9 will give W


//        String advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
//        Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        } else {
            advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        }*/


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

        String bp = mBP;
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

            sign_url = "https://uiux.intelehealth.org/ds/" + objClsDoctorDetails.getUuid() + "_sign.png";

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
//            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
//                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
//                    "</div>";


            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;\">" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " +
                    (objClsDoctorDetails.getQualification() == null ||
                            objClsDoctorDetails.getQualification().equalsIgnoreCase("null") ? "" :
                            objClsDoctorDetails.getQualification() + ", ") + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
                    "</div>";
//            mDoctorName.setText(doctrRegistartionNum + "\n" + Html.fromHtml(doctorDetailStr));
        }

        if (isRespiratory) {
            String htmlDocument =
                    String.format(/*font_face +*/ "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                    /* doctorDetailStr +*/
                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" +
                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
                                    "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                                    "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +
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
                                  //  "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                            "<img src=" + sign_url + " alt=\"Dr Signature\">" + // doctor signature...
                                    doctorDetailStr +
                                    "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" +
                                    "</div>"
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "",
                            (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            pat_hist, fam_hist, mComplaint, diagnosis_web, rx_web, tests_web, advice_web/*""*/, followUp_web, doctor_web);
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
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
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
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, /*advice_web*/"", followUp_web, doctor_web);
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
    // Print - end

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
    }

    private void doWebViewPrint_downloadBtn() throws ParseException {
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
                createWebPrintJob_downloadBtn(view, webview_heightContent);
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
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3); // todo: uncomment later.
                //   mComplaint = "Test Complaint";
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
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

        String advice_web = stringToWeb(adviceReturned);
        //    String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
/*        String advice_doctor__ = medicalAdviceTextView.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {


//        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
//        Log.d("Hyperlink", "hyper_print: " + advice_web);
//        String advice_split = new StringBuilder(medicalAdviceTextView.getText().toString())
//                .delete(medicalAdviceTextView.getText().toString().indexOf("Start"),
//                        medicalAdviceTextView.getText().toString().lastIndexOf("User")+6).toString();
            //lastIndexOf("User") will give index of U of User
            //so the char this will return is U...here User + 6 will return W eg: User\n\nWatch as +6 will give W

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();
            //lastIndexOf("Doctor_") will give index of D of Doctor_
            //so the char this will return is D...here Doctor_ + 9 will return W eg: Doctor_\n\nWatch as +9 will give W


//        String advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
//        Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        } else {
            advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        }*/


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

        String bp = mBP;
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

            sign_url = "https://uiux.intelehealth.org/ds/" + objClsDoctorDetails.getUuid() + "_sign.png";

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
//            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
//                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
//                    "</div>";


            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;\">" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " +
                    (objClsDoctorDetails.getQualification() == null ||
                            objClsDoctorDetails.getQualification().equalsIgnoreCase("null") ? "" :
                            objClsDoctorDetails.getQualification() + ", ") + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
                    "</div>";
//            mDoctorName.setText(doctrRegistartionNum + "\n" + Html.fromHtml(doctorDetailStr));
        }

        if (isRespiratory) {
            String htmlDocument =
                    String.format(/*font_face +*/ "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" +
                                    "<hr style=\"font-size:12pt;\">" + "<br/>" +
                                    /* doctorDetailStr +*/
                                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" +
                                    "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" +
                                    "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" +
                                    "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" +
                                    "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" +
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                                    "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +
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
                                    //  "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                                    "<img src=" + sign_url + " alt=\"Dr Signature\">" + // doctor signature...
                                    doctorDetailStr +
                                    "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" +
                                    "</div>"
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                            (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "",
                            (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            pat_hist, fam_hist, mComplaint, diagnosis_web, rx_web, tests_web, advice_web/*""*/, followUp_web, doctor_web);
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
                                    "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
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
                            , heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "",
                            (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                            /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, /*advice_web*/"", followUp_web, doctor_web);
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


    private void createWebPrintJob_downloadBtn(WebView webView, int contentHeight) {
        
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

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {
                                Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {
                                    Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {
                                Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {
                                    Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {
                                Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {
                                    Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //end...

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir,
                        fileName, new PdfPrint.CallbackPrint() {
                            @Override
                            public void success(String path) {
                                Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            }

                        });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //to write to a pdf file...
                    pdfPrint.print(printAdapter, dir,
                            fileName, new PdfPrint.CallbackPrint() {
                                @Override
                                public void success(String path) {
                                    Toast.makeText(VisitSummaryActivity_New.this, "Downloaded To: " + path, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }

                            });
                }
            }
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    new PrintAttributes.Builder().build());

        }


    }


}