package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;
import static org.intelehealth.app.database.dao.VisitsDAO.fetchVisitModifiedDateForPrescPending;
import static org.intelehealth.app.database.dao.VisitsDAO.isVisitEnded;
import static org.intelehealth.app.database.dao.VisitsDAO.isVisitNotEnded;
import static org.intelehealth.app.utilities.DateAndTimeUtils.timeAgoFormat;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Html;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.visit.PrescriptionActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointment.model.CancelRequest;
import org.intelehealth.app.appointment.model.CancelResponse;
import org.intelehealth.app.appointmentNew.MyAppointmentNew.MyAppointmentActivityNew;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.PatientRegStage;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailsActivity extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "AppointmentDetailsActiv";
    RelativeLayout stateAppointmentPrescription, layoutPrevScheduledOn, layoutPatientHistory, layoutVisitSummary, stateAppointmentStarted;
    LinearLayout layoutPrescButtons, layoutContactAction, layoutEndVisit;
    TextView tvPrescStatus, tvRescheduleOnTitle, tvAppointmentTime, tvPatientName, tvOpenMrsID, tvGenderAgeText, tvChiefComplaintTxt, tvVisitId, tvVisitStartDate, tvVisitStartTime, tvDrSpeciality, tvPrevAppDate, tvPrevAppTime;
    ImageView ivPrescription, ivDrawerPrescription, ivProfileImage, ivDrawerVisitSummary, ivCallPatient, ivWhatsappPatient, ivWhatsappDoctor, ivCallDoctor;
    Button btnEndVisit, btnRescheduleAppointment, btnCancelAppointment;
    View layoutSummaryBtns;
    FloatingActionButton fabHelp;
    int appointment_id = 0;
    private LinearLayout priorityTag;
    private boolean isEmergency, hasPrescription;
    private String patientName, patientUuid, gender, age, openmrsID, dob, visitID, visit_speciality, followupDate, patient_photo_path, app_start_date, app_start_time, app_start_day, prescription_received_time, appointmentStatus;
    SQLiteDatabase db;
    boolean isVisitStartsIn = false;
    private String vitalsUUID, adultInitialUUID;
    private static final int SCHEDULE_LISTING_INTENT = 2010;
    private String mEngReason = "";
    SessionManager sessionManager;
    AppointmentDAO appointmentDAO;
    private ClsDoctorDetails clsDoctorDetails;
    String dr_MobileNo = "";
    String dr_WhatsappNo = "";
    NetworkUtils networkUtils;
    ImageView ivIsInternet;
    //ImageButton ibEdit;
    private PatientDTO patientDTO;
    String patientPhoneNo = "";
    private TextView mScheduleAppointmentTextView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(AppointmentDetailsActivity.this);
        setContentView(R.layout.activity_appointment_details_ui2);
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        sessionManager = new SessionManager(this);
        context = AppointmentDetailsActivity.this;
        appointmentDAO = new AppointmentDAO();
        networkUtils = new NetworkUtils(AppointmentDetailsActivity.this, this);

        initUI();
    }

    private ObjectAnimator syncAnimator;

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ivIsInternet.setOnClickListener(v -> {
            SyncUtils.syncNow(AppointmentDetailsActivity.this, ivIsInternet, syncAnimator);
        });
        tvTitle.setText(getResources().getString(R.string.appointment_details));
        ImageView ivBack = toolbar.findViewById(R.id.iv_back_arrow_common);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentDetailsActivity.this, MyAppointmentActivityNew.class);
            startActivity(intent);
        });


        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        Intent intent1 = getIntent();
        if (intent1 != null) {
            Bundle args = intent1.getBundleExtra("BUNDLE");
            if (args != null) {
                patientDTO = (PatientDTO) args.getSerializable("patientDTO");

            }
            // privacy_value_selected = intent.getStringExtra("privacy"); //intent value from IdentificationActivity.
        }


        //ibEdit = findViewById(R.id.edit_patient_appointment);

        mScheduleAppointmentTextView = findViewById(R.id.btn_schedule_appointment);

        stateAppointmentStarted = findViewById(R.id.state_appointment_started);
        stateAppointmentPrescription = findViewById(R.id.state_prescription_appointment);
        //this is for remind doctor functionality
        layoutPrescButtons = findViewById(R.id.layout_presc_buttons);
        layoutContactAction = findViewById(R.id.layout_contact_action);
        tvPrescStatus = findViewById(R.id.tv_presc_status_new);
        ivPrescription = findViewById(R.id.iv_prescription111);
        btnEndVisit = findViewById(R.id.btn_end_visit_appointment);
        layoutSummaryBtns = findViewById(R.id.layout_visit_summary_buttons);
        layoutPrevScheduledOn = findViewById(R.id.state_prev_scheduled_on);
        layoutPatientHistory = findViewById(R.id.layout_patient_history);
        tvRescheduleOnTitle = findViewById(R.id.tv_reschedule_on);
        tvAppointmentTime = findViewById(R.id.tv_appointment_time_details);
        ivDrawerPrescription = findViewById(R.id.iv_drawer_prescription);
        fabHelp = findViewById(R.id.fab_help_appointment);
        layoutEndVisit = findViewById(R.id.layout_end_visit);
        layoutVisitSummary = findViewById(R.id.layout_visit_summary);
        ivProfileImage = findViewById(R.id.profile_image_app_details);
        priorityTag = findViewById(R.id.llPriorityTagAppointmentDetails);
        tvPatientName = findViewById(R.id.patname_txt);
        tvOpenMrsID = findViewById(R.id.openmrsID_txt);
        tvGenderAgeText = findViewById(R.id.gender_age_txt);
        tvChiefComplaintTxt = findViewById(R.id.chief_complaint_txt);
        tvVisitId = findViewById(R.id.visitID_appointment);
        tvVisitStartDate = findViewById(R.id.visit_startDate_appointment);
        tvVisitStartTime = findViewById(R.id.tv_starttime_appointment);
        tvDrSpeciality = findViewById(R.id.dr_speciality_appointment);
        ivDrawerVisitSummary = findViewById(R.id.iv_drawer_visit_summary);
        btnRescheduleAppointment = findViewById(R.id.btn_reschedule_appointment);
        btnCancelAppointment = findViewById(R.id.btn_cancel_appointment);
        ivCallPatient = findViewById(R.id.iv_call_patient_app);
        ivWhatsappPatient = findViewById(R.id.iv_whatsapp_patient_app);
        tvPrevAppDate = findViewById(R.id.tv_prev_app_date);
        tvPrevAppTime = findViewById(R.id.tv_prev_app_time);
        ivWhatsappDoctor = findViewById(R.id.iv_whatsapp_doctor_app);
        ivCallDoctor = findViewById(R.id.iv_call_doctor_app);


        Intent intent = this.getIntent(); // The intent was passed to the activity from adapter
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            patientUuid = intent.getStringExtra("patientUuid");
            gender = intent.getStringExtra("gender");
            dob = intent.getStringExtra("dob");
            age = intent.getStringExtra("age");
            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            visit_speciality = intent.getStringExtra("visit_speciality");
            app_start_date = intent.getStringExtra("app_start_date");
            app_start_time = intent.getStringExtra("app_start_time");
            appointment_id = intent.getIntExtra("appointment_id", 0);
            app_start_day = intent.getStringExtra("app_start_day");
            prescription_received_time = intent.getStringExtra("prescription_received_time");
            followupDate = intent.getStringExtra("followup_date");
            if (followupDate == null) followupDate = getFollowupDataForVisitUUID(visitID);
            isEmergency = intent.getBooleanExtra("priority_tag", false);
            hasPrescription = intent.getBooleanExtra("hasPrescription", false);
            appointmentStatus = intent.getStringExtra("status");
            PatientDTO patientDTO = PatientsDAO.getPatientDetailsByUuid(patientUuid);
            patient_photo_path = patientDTO.getPatientPhoto();


        }


        if (patientUuid != null && !patientUuid.isEmpty()) {
            String[] DobAndGender = PatientsDAO.getPatientDobAgeGender(patientUuid);
            if (DobAndGender.length > 0) {
                String age1 = DateAndTimeUtils.getAge_FollowUp(DobAndGender[1], this);
//                tvGenderAgeText.setText(DobAndGender[0] + " " + age1);

                gender = DobAndGender[0];
                age = DobAndGender[1];

                setGenderAgeLocal(context, tvGenderAgeText, dob, gender, sessionManager);
            }

            //get patient phone number from local db
            try {
                patientPhoneNo = PatientsDAO.phoneNumber(patientUuid);
            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
        // Patient Photo
        if (patient_photo_path != null) {
            RequestBuilder<Drawable> requestBuilder = Glide.with(this)
                    .asDrawable().sizeMultiplier(0.3f);
            Glide.with(this).load(patient_photo_path).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileImage);
        } else {
            ivProfileImage.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.avatar1));
        }

        // common data setting

        tvPatientName.setText(patientName);
        tvOpenMrsID.setText(openmrsID);
        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        tvVisitId.setText(getResources().getString(R.string.visitID) + " XXXX" + hideVisitUUID);

        String chief_complaint_value = getChiefComplaint(visitID);
        CustomLog.d(TAG, "initUI: chief_complaint_value : " + chief_complaint_value);
        if (chief_complaint_value != null && !chief_complaint_value.isEmpty()) {


            boolean needToShowCoreValue = false;
            if (chief_complaint_value.startsWith("{") && chief_complaint_value.endsWith("}")) {
                try {
                    // isInOldFormat = false;
                    JSONObject jsonObject = new JSONObject(chief_complaint_value);
                    if (jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                        chief_complaint_value = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                        needToShowCoreValue = false;
                    } else {
                        needToShowCoreValue = true;
                        chief_complaint_value = jsonObject.getString("en");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                needToShowCoreValue = true;
            }

            if (needToShowCoreValue) {
                chief_complaint_value = chief_complaint_value.replace("?<b>", Node.bullet_arrow);

                String[] complaints = org.apache.commons.lang3.StringUtils.split(chief_complaint_value, Node.bullet_arrow);

                chief_complaint_value = "";
                String colon = ":";
                if (complaints != null) {
                    for (String comp : complaints) {
                        if (!comp.trim().isEmpty() && comp.contains(colon)) {
                            chief_complaint_value = chief_complaint_value + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                        }
                    }
                    if (!chief_complaint_value.isEmpty()) {
                        chief_complaint_value = chief_complaint_value.replaceAll(Node.bullet_arrow, "");
                        chief_complaint_value = chief_complaint_value.replaceAll("<br/>", ", ");
                        chief_complaint_value = chief_complaint_value.replaceAll(Node.ASSOCIATE_SYMPTOMS, "");
                        //visitValue = visitValue.substring(0, visitValue.length() - 2);
                        chief_complaint_value = chief_complaint_value.replaceAll("<b>", "");
                        chief_complaint_value = chief_complaint_value.replaceAll("</b>", "");
                        chief_complaint_value = chief_complaint_value.trim();
                        while (chief_complaint_value.endsWith(",")){
                            chief_complaint_value = chief_complaint_value.substring(0, chief_complaint_value.length()-1).trim();
                        }
                    }
                }
                tvChiefComplaintTxt.setText(Html.fromHtml(chief_complaint_value));
            } else {
                chief_complaint_value = chief_complaint_value.replaceAll("<.*?>", "");
                System.out.println(chief_complaint_value);
                CustomLog.v(TAG, chief_complaint_value);
                //►दस्त::● आपको ये लक्षण कब से है• 6 घंटे● दस्त शुरू कैसे हुए?•धीरे धीरे● २४ घंटे में कितनी बार दस्त हुए?•३ से कम बार● दस्त किस प्रकार के है?•पक्का● क्या आपको पिछले महीनो में दस्त शुरू होने से पहले किसी असामान्य भोजन/तरल पदार्थ से अपच महसूस हुआ है•नहीं● क्या आपने आज यहां आने से पहले इस समस्या के लिए कोई उपचार (स्व-दवा या घरेलू उपचार सहित) लिया है या किसी स्वास्थ्य प्रदाता को दिखाया है?•कोई नहीं● अतिरिक्त जानकारी•bsbdbd►क्या आपको निम्न लक्षण है::•उल्टीPatient denies -•दस्त के साथ पेट दर्द•सुजन•मल में खून•बुखार•अन्य [वर्णन करे]

                String[] spt = chief_complaint_value.split("►");
                List<String> list = new ArrayList<>();
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : spt) {
                    String complainName = "";
                    if (s.isEmpty()) continue;
                    //String s1 =  new String(s.getBytes(), "UTF-8");
                    System.out.println(s);
                    String[] spt1 = s.split("::●");
                    complainName = spt1[0];

                    //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
                    if (!complainName.trim().contains(org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedPatientDenies(sessionManager.getAppLanguage()))) {
                        System.out.println(complainName);
                        if (!stringBuilder.toString().isEmpty()) stringBuilder.append(", ");
                        stringBuilder.append(complainName);
                    }

                }


                tvChiefComplaintTxt.setText(stringBuilder.toString());
            }
        }

        tvDrSpeciality.setText(visit_speciality);

        boolean isVisitEnded = isVisitEnded(visitID);

        // checking to see if the visit has ended or not - if the visit has ended, we don't need to show the buttons
        //appointment started state - make "state AppointmentStarted" visible
        String timeText = getAppointmentStartsInTime(app_start_date, app_start_time);
        tvVisitStartDate.setText(DateAndTimeUtils.getDateInDDMMMMYYYYFormat(app_start_date));
        tvVisitStartTime.setText(app_start_time);
        if (isVisitStartsIn && !isVisitEnded) { //that means appointment scheduled
            stateAppointmentStarted.setVisibility(View.VISIBLE);
            layoutContactAction.setVisibility(View.GONE);
            tvRescheduleOnTitle.setVisibility(View.GONE);
            tvAppointmentTime.setText(getResources().getString(R.string.starts) + " " + timeText);
            layoutSummaryBtns.setVisibility(View.VISIBLE);


            btnRescheduleAppointment.setOnClickListener(v -> {
                String subtitle = getResources().getString(R.string.sure_to_reschedule_appointment, patientName);
                rescheduleAppointment(AppointmentDetailsActivity.this, getResources().getString(R.string.reschedule_appointment_new), subtitle, getResources().getString(R.string.yes), getResources().getString(R.string.no));

            });
            btnCancelAppointment.setOnClickListener(v -> {
                String subtitle = getResources().getString(R.string.sure_to_cancel_appointment, patientName);
                cancelAppointment(AppointmentDetailsActivity.this, getResources().getString(R.string.cancel_appointment_new), subtitle, getResources().getString(R.string.yes), getResources().getString(R.string.no));

            });

        } else {
            //appointment scheduled and time has been passed
            //prescription pending  state - make "stateAppointmentStarted" visible,
            // "tvAppointmentTime" gone, "stateAppointmentPrescription" visible
            stateAppointmentStarted.setVisibility(View.VISIBLE);
            tvAppointmentTime.setVisibility(View.GONE);
            stateAppointmentPrescription.setVisibility(View.VISIBLE);
            tvPrescStatus.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary2));
        }


        //appointment rescheduled
        // hide prescription, contact action views, end visit button
        // make visible prev scheduled on and rescheduled on titles, summary buttons
        AppointmentInfo appointmentInfo = appointmentDAO.getDetailsOfRescheduledAppointment(visitID, String.valueOf(appointment_id));
        if (appointmentInfo != null && appointmentInfo.getPrev_slot_date() != null && !appointmentInfo.getPrev_slot_date().isEmpty()) {
            stateAppointmentPrescription.setVisibility(View.GONE);
            layoutPrescButtons.setVisibility(View.GONE);
            btnEndVisit.setVisibility(View.GONE);
            layoutSummaryBtns.setVisibility(View.VISIBLE);
            layoutContactAction.setVisibility(View.GONE);
            layoutPrevScheduledOn.setVisibility(View.VISIBLE);
            tvRescheduleOnTitle.setVisibility(View.VISIBLE);
            String date = appointmentInfo.getPrev_slot_date();
            if (date != null && !date.isEmpty()) {
                tvPrevAppDate.setText(DateAndTimeUtils.getDateInDDMMMMYYYYFormat(date));

            }
            tvPrevAppTime.setText(appointmentInfo.getPrev_slot_time());

        }

        if (hasPrescription) {
            //prescription received  state - make "stateAppointmentStarted" visible,
            // "tvAppointmentTime" gone, "stateAppointmentPrescription" visible, "layoutPrescButtons" gone
            layoutEndVisit.setVisibility(View.VISIBLE);
            layoutSummaryBtns.setVisibility(View.GONE);
            stateAppointmentStarted.setVisibility(View.VISIBLE);
            tvAppointmentTime.setVisibility(View.GONE);
            stateAppointmentPrescription.setVisibility(View.VISIBLE);
            layoutPrescButtons.setVisibility(View.GONE);
            tvPrescStatus.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary1));
//            ivPrescription.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_prescription_green));
            fabHelp.setVisibility(View.GONE);
            tvPrescStatus.setText(getResources().getString(R.string.received) + " " + prescription_received_time);

            //redirection to PrescriptionActivity activity
            ivDrawerPrescription.setOnClickListener(v -> {
                Intent in = new Intent(this, PrescriptionActivity.class);
                in.putExtra("patientname", patientName);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("patient_photo", patient_photo_path);
                in.putExtra("visit_ID", visitID);
                in.putExtra("visit_startDate", "");
                in.putExtra("gender", gender);
                in.putExtra("encounterUuidVitals", "");
                in.putExtra("encounterUuidAdultIntial", "");
                in.putExtra("age", age);
                in.putExtra("tag", "AppointmentDetailsActivity");
                in.putExtra("followupDate", followupDate);
                in.putExtra("openmrsID", openmrsID);
                startActivity(in);
            });

            // end visit - start : if prescription received
            PrescriptionModel pres = isVisitNotEnded(visitID);
            if (pres.getVisitUuid() != null) {
                layoutEndVisit.setVisibility(View.VISIBLE);
                btnEndVisit.setOnClickListener(v -> {
                    VisitUtils.endVisit(AppointmentDetailsActivity.this, visitID, patientUuid, followupDate, vitalsUUID, adultInitialUUID, "state", patientName, "AppointmentDetailsActivity");
                });
            } else {
                layoutEndVisit.setVisibility(View.GONE);
            }
            // end visit - end
        } else {
            // if no presc given than show the dialog of remind and pending based on time passed from visit uploaded.
            ivDrawerPrescription.setVisibility(View.GONE);

            // Check if the Visit is Ended Or Not
            // If the visit has ended, we don't need to show the "Prescription Pending" text

            if (isVisitEnded && !hasPrescription) {
                layoutPrescButtons.setVisibility(View.GONE);
                tvPrescStatus.setText(getResources().getString(R.string.no_prescription_exists));
            } else {
                String modifiedDate = fetchVisitModifiedDateForPrescPending(visitID);
                modifiedDate = timeAgoFormat(modifiedDate);
                if (modifiedDate.contains("minutes") || modifiedDate.contains("hours")) {
                    // here dont show remind block
                    // presc_remind_block.setVisibility(View.GONE);
                    layoutPrescButtons.setVisibility(View.GONE);
                } else {
                    // here show remind block as its pending from more than 1 day.
                    layoutPrescButtons.setVisibility(View.GONE); // show remind btn for presc to be given as its more than days.
                }
                tvPrescStatus.setText(getResources().getString(R.string.pending_since) + " " + modifiedDate.replace("ago", ""));
                tvPrescStatus.setTextColor(ContextCompat.getColor(this,R.color.red));
            }
        }
        // presc block - end

        // visit summary - start
        vitalsUUID = fetchEncounterUuidForEncounterVitals(visitID);
        adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitID);

        layoutVisitSummary.setOnClickListener(v -> {
            Intent in = new Intent(this, VisitSummaryActivity_New.class);
            in.putExtra("patientUuid", patientUuid);
            in.putExtra("visitUuid", visitID);
            in.putExtra("gender", gender);
            in.putExtra("name", patientName);
            in.putExtra("encounterUuidVitals", vitalsUUID);
            in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
            in.putExtra("float_ageYear_Month", age);
            in.putExtra("openMrsId", openmrsID);
            in.putExtra("tag", "AppointmentDetailsActivity");
            startActivity(in);
        });
        // visit summary - end


        handleWhatsappAndCall();

        //edit patient details - Redirect to Identification activity
        /*ibEdit.setOnClickListener(v -> {
            PatientDTO patientDTO = PatientsDAO.getPatientDetailsByUuid(patientUuid);
            patientDTO.setPatientPhoto(patient_photo_path);
            patientDTO.setOpenmrsId(openmrsID);
            patientDTO.setPhonenumber(patientPhoneNo);

            Intent intent2 = new Intent(AppointmentDetailsActivity.this, IdentificationActivity_New.class);
            intent2.putExtra("patientUuid", patientUuid);
            intent2.putExtra("ScreenEdit", "personal_edit");
            intent2.putExtra("patient_detail", true);

            Bundle args = new Bundle();
            args.putSerializable("patientDTO", (Serializable) patientDTO);
            intent2.putExtra("BUNDLE", args);
            startActivity(intent2);
        });*/

        //if appointment is rescheduled and prescription is pending then hide layout summary buttons i.e. cancel and reschedule
        if (appointmentInfo != null && appointmentInfo.getPrev_slot_date() != null && !appointmentInfo.getPrev_slot_date().isEmpty() && !hasPrescription && !isVisitStartsIn) {
            layoutSummaryBtns.setVisibility(View.GONE);
            stateAppointmentStarted.setVisibility(View.VISIBLE);
            tvAppointmentTime.setVisibility(View.GONE);
            stateAppointmentPrescription.setVisibility(View.VISIBLE);
            tvPrescStatus.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary2));
        }

        //if appointment is cancelled
        if (appointmentStatus != null && !appointmentStatus.isEmpty() && appointmentStatus.equalsIgnoreCase("cancelled")) {
            //layoutSummaryBtns.setVisibility(View.GONE);
            btnCancelAppointment.setVisibility(View.GONE);
            btnRescheduleAppointment.setVisibility(View.GONE);

            stateAppointmentStarted.setVisibility(View.VISIBLE);
            tvAppointmentTime.setVisibility(View.GONE);
            stateAppointmentPrescription.setVisibility(View.GONE);

            mScheduleAppointmentTextView.setVisibility(View.GONE);
        } else {
            btnCancelAppointment.setVisibility(View.VISIBLE);
            btnRescheduleAppointment.setVisibility(View.VISIBLE);
            mScheduleAppointmentTextView.setVisibility(View.GONE);
        }

        mScheduleAppointmentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(AppointmentDetailsActivity.this, ScheduleAppointmentActivity_New.class);
                in.putExtra("visitUuid", visitID);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("patientName", patientName);
                in.putExtra("appointmentId", 0);
                in.putExtra("actionTag", "new_schedule");
                in.putExtra("openMrsId", openmrsID);
                in.putExtra("speciality", visit_speciality);
                in.putExtra("requestCode", AppConstants.EVENT_APPOINTMENT_BOOKING_APPOINTMENT_DETAILS);
                mStartForScheduleAppointment.launch(in);
            }
        });

    }

    private void handleWhatsappAndCall() {
        try {
            CustomLog.d(TAG, "handleWhatsappAndCall: patientPhoneNo : " + patientPhoneNo);
            //for patient

            ivWhatsappPatient.setOnClickListener(v -> {
                if (patientPhoneNo != null && !patientPhoneNo.isEmpty()) {
                    String url = "https://api.whatsapp.com/send?phone=" + patientPhoneNo;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else {
                    Toast.makeText(AppointmentDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();
                }
            });
            ivCallPatient.setOnClickListener(v -> {
                if (patientPhoneNo != null && !patientPhoneNo.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + patientPhoneNo));
                    startActivity(intent);
                } else {
                    Toast.makeText(AppointmentDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        //for doctor

        // Fetching dr details from Local db.
        String drDetails = fetchDrDetailsFromLocalDb(visitID);
        Gson gson = new Gson();
        clsDoctorDetails = gson.fromJson(drDetails, ClsDoctorDetails.class);

        if (clsDoctorDetails != null) {
            CustomLog.e("TAG", "TEST VISIT: " + clsDoctorDetails.toString());
            dr_MobileNo = "+91" + clsDoctorDetails.getPhoneNumber();
            dr_WhatsappNo = "+91" + clsDoctorDetails.getWhatsapp();

            try {

                ivWhatsappDoctor.setOnClickListener(v -> {
                    if (dr_WhatsappNo != null && !dr_WhatsappNo.isEmpty()) {
                        String url = "https://api.whatsapp.com/send?phone=" + dr_WhatsappNo;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else {
                        Toast.makeText(AppointmentDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();

                    }
                });


                ivCallDoctor.setOnClickListener(v -> {
                    if (dr_MobileNo != null && !dr_MobileNo.isEmpty()) {
                        Intent i1 = new Intent(Intent.ACTION_DIAL);
                        i1.setData(Uri.parse("tel:" + dr_MobileNo));
                        startActivity(i1);
                    } else {
                        Toast.makeText(AppointmentDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();

                    }

                });


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private String getChiefComplaint(String visitUUID) {
        String chief_complaint_value = "";
        CustomLog.v("Followup", "visitid: " + visitUUID);
        if (visitUUID != null && !visitUUID.isEmpty()) {
            String complaint_query = "select e.uuid, o.value  from tbl_encounter e, tbl_obs o where " + "e.visituuid = ? " + "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
                    "and e.uuid = o.encounteruuid and o.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'"; // chief complaint

            final Cursor cursor = db.rawQuery(complaint_query, new String[]{visitUUID});
            if (cursor.moveToFirst()) {
                do {
                    try {
                        chief_complaint_value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                        CustomLog.v("Followup", "chiefcomplaint: " + chief_complaint_value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();


        }
        return chief_complaint_value;

    }

    private String getAppointmentStartsInTime(String soltDate, String slotTime) {
        //for setting appointment starting in time
        String timeText = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        String slottime = soltDate + " " + slotTime;

        long diff = 0;
        try {
            diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();
            long second = diff / 1000;
            long minutes = second / 60;
            //check for appointment but presc not given and visit not completed
            if (minutes > 0) {
                isVisitStartsIn = true;
                long hours = minutes / 60;
                long mins = minutes % 60;
                if (minutes >= 60) {
                    if (hours >= 24) {
                        timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(soltDate) + ", " + getResources().getString(R.string.at) + " " + slotTime;
                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                            timeText = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(soltDate)) + ", " + slotTime + " " + getResources().getString(R.string.at);
                    } else {
                        timeText = context.getString(R.string.in) + " " + hours + " " + context.getString(R.string.hours) + " " + mins + " " + context.getString(R.string.minutes_txt) + ", " + context.getString(R.string.at) + " " + slotTime;

                    }
                } else {
                    timeText = getResources().getString(R.string.in) + " " + minutes + " " + getResources().getString(R.string.minutes_txt) + ", " + context.getString(R.string.at) + " " + slotTime;
                }

            } else {
                isVisitStartsIn = false;
            }
        } catch (ParseException e) {
            CustomLog.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return timeText;
    }


    public void cancelAppointment(Context context, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_book_app_red));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            askReason(AppointmentDetailsActivity.this);


        });

        alertDialog.show();
    }

    private void cancelAppointmentRequest(String reason) {
        CancelRequest request = new CancelRequest();
        request.setVisitUuid(visitID);
        request.setId(appointment_id);
        request.setReason(reason);
        request.setHwUUID(new SessionManager(AppointmentDetailsActivity.this).getProviderID()); // user id / healthworker id
        String baseurl = BuildConfig.SERVER_URL + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi().cancelAppointment(request).enqueue(new Callback<CancelResponse>() {
            @Override
            public void onResponse(Call<CancelResponse> call, Response<CancelResponse> response) {
                if (response.body() == null) return;
                CancelResponse cancelResponse = response.body();
                if (cancelResponse.isStatus()) {
                    //AppointmentInfo appointmentInfo=appointmentDAO.getAppointmentByVisitId(visitUuid);
                    //if(appointmentInfo!=null && appointmentInfo.getStatus().equalsIgnoreCase("booked")) {
                    appointmentDAO.deleteAppointmentByVisitId(visitID);
                    //}

                    Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.appointment_cancelled_success_txt), Toast.LENGTH_SHORT).show();
                    //   getAppointmentDetails(mAppointmentDetailsResponse.getData().getVisitUuid());
                    //Intent intent = new Intent(AppointmentDetailsActivity.this, HomeScreenActivity_New.class);
                    //startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.failed_to_cancel_appointment), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<CancelResponse> call, Throwable t) {
                CustomLog.v("onFailure", t.getMessage());
            }
        });
    }

    public void rescheduleAppointment(Context context, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_book_app_red));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            askReasonForRescheduleAppointment(AppointmentDetailsActivity.this);
          /*  startActivityForResult(new Intent(context, ScheduleAppointmentActivity_New.class)
                    .putExtra("visitUuid", visitID)
                    .putExtra("patientUuid", patientUuid)
                    .putExtra("patientName", patientName)
                    .putExtra("appointmentId", appointment_id)
                    .putExtra("openMrsId", openmrsID)
                    .putExtra("actionTag", "reschedule")
                    .putExtra("app_start_date", app_start_date)
                    .putExtra("app_start_time", app_start_time)
                    .putExtra("app_start_day", app_start_day)
                    .putExtra("speciality", visit_speciality), SCHEDULE_LISTING_INTENT
            );*/

        });

        alertDialog.show();
    }

    private void askReasonForRescheduleAppointment(Context context) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_ask_reason_new_ui2, null);
        alertdialogBuilder.setView(convertView);

        final TextView titleTextView = convertView.findViewById(R.id.titleTv_new);
        titleTextView.setText(getString(R.string.please_select_your_reschedule_reason));
        final EditText reasonEtv = convertView.findViewById(R.id.reasonEtv_new);
        reasonEtv.setVisibility(View.GONE);
        final RadioButton rb1 = convertView.findViewById(R.id.rb_no_doctor);
        final RadioButton rb2 = convertView.findViewById(R.id.rb_no_patient);
        final RadioButton rb3 = convertView.findViewById(R.id.rb_other_ask);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        final RadioGroup optionsRadioGroup = convertView.findViewById(R.id.rg_ask_reason);
        optionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_no_doctor) {
                    rb1.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_selected_green));
                    rb2.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    rb3.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.doctor_is_not_available));
                    mEngReason = "Doctor is not available";
                } else if (checkedId == R.id.rb_no_patient) {
                    rb2.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_selected_green));
                    rb1.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    rb3.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.patient_is_not_available));
                    mEngReason = "Patient is not available";
                } else if (checkedId == R.id.rb_other_ask) {
                    rb3.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_selected_green));
                    rb2.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    rb1.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    reasonEtv.setText("");
                    reasonEtv.setVisibility(View.VISIBLE);
                }
            }
        });

        final Button textView = convertView.findViewById(R.id.btn_save_ask);
        final Button btnCancel = convertView.findViewById(R.id.btn_cancel_ask);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                String reason = reasonEtv.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.please_enter_reason_txt), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent in = new Intent(context, ScheduleAppointmentActivity_New.class);
                    in.putExtra("actionTag", "rescheduleAppointment");
                    in.putExtra("visitUuid", visitID);
                    in.putExtra("patientUuid", patientUuid);
                    in.putExtra("patientName", patientName);
                    in.putExtra("appointmentId", appointment_id);
                    in.putExtra("openMrsId", openmrsID);
                    in.putExtra("app_start_date", app_start_date);
                    in.putExtra("app_start_time", app_start_time);
                    in.putExtra("app_start_day", app_start_day);
                    in.putExtra("rescheduleReason", mEngReason);
                    in.putExtra("speciality", visit_speciality);
                    in.putExtra("requestCode", AppConstants.EVENT_APPOINTMENT_BOOKING_APPOINTMENT_DETAILS);

                    CustomLog.d(TAG, "onClick: speciality : " + visit_speciality);
                    mStartForScheduleAppointment.launch(in);
                }

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private final ActivityResultLauncher<Intent> mStartForScheduleAppointment = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == AppConstants.EVENT_APPOINTMENT_BOOKING_APPOINTMENT_DETAILS) {
            Toast.makeText(AppointmentDetailsActivity.this, getResources().getString(R.string.appointment_booked_successfully), Toast.LENGTH_LONG).show();
            finish();
        }
    });

    private void askReason(Context context) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_ask_reason_new_ui2, null);
        alertdialogBuilder.setView(convertView);

        final TextView titleTextView = convertView.findViewById(R.id.titleTv_new);
        titleTextView.setText(getString(R.string.please_select_your_cancel_reason));
        final EditText reasonEtv = convertView.findViewById(R.id.reasonEtv_new);
        reasonEtv.setVisibility(View.GONE);
        final RadioButton rb1 = convertView.findViewById(R.id.rb_no_doctor);
        final RadioButton rb2 = convertView.findViewById(R.id.rb_no_patient);
        final RadioButton rb3 = convertView.findViewById(R.id.rb_other_ask);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        final RadioGroup optionsRadioGroup = convertView.findViewById(R.id.rg_ask_reason);
        optionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_no_doctor) {
                    rb1.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_selected_green));
                    rb2.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    rb3.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.doctor_is_not_available));
                    mEngReason = "Doctor is not available";
                } else if (checkedId == R.id.rb_no_patient) {
                    rb2.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_selected_green));
                    rb1.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    rb3.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.patient_is_not_available));
                    mEngReason = "Patient is not available";
                } else if (checkedId == R.id.rb_other_ask) {
                    rb3.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_selected_green));
                    rb2.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    rb1.setButtonDrawable(ContextCompat.getDrawable(context,R.drawable.ui2_ic_circle));
                    reasonEtv.setText("");
                    reasonEtv.setVisibility(View.VISIBLE);
                }
            }
        });

        final Button textView = convertView.findViewById(R.id.btn_save_ask);
        final Button btnCancel = convertView.findViewById(R.id.btn_cancel_ask);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                String reason = reasonEtv.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.please_enter_reason_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                cancelAppointmentRequest(mEngReason.isEmpty() ? reason : mEngReason);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
        alertDialog.show();

    }

    public static String fetchPrescriptionReceivedTime(String visitUUID) {
        CustomLog.d(TAG, "fetchPrescriptionReceivedTime:visitUUID :" + visitUUID);
        String modifiedDate = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        if (visitUUID != null) {
            Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," + " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" + " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" + " e.encounter_type_uuid = ? and" + " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" + " o.conceptuuid = ? and " + " (substr(o.obsservermodifieddate, 1, 4) ||'-'|| substr(o.obsservermodifieddate, 6,2) ||'-'|| substr(o.obsservermodifieddate, 9,2)) = DATE('now') group by p.openmrs_id ", new String[]{ENCOUNTER_VISIT_NOTE, "537bb20d-d09d-4f88-930b-cc45c7d662df"});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.


            if (cursor.moveToFirst()) {
                do {
                    try {
                        String receivedTime = cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate"));
                        CustomLog.v("receivedTime", "receivedTime: " + modifiedDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return modifiedDate;
    }


    //update ui as per internet availability
    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();

    }

    public void editPatientInfo(View view) {
        PatientDTO patientDTO = new PatientDTO();
        String patientSelection = "uuid = ?";
        String[] patientArgs = {patientUuid};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "gender", "date_of_birth", "address1", "address2", "city_village", "state_province", "postal_code", "country", "phone_number", "gender", "sdw", "patient_photo"};
        SQLiteDatabase db = db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patientDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patientDTO.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                patientDTO.setFirstname(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patientDTO.setMiddlename(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patientDTO.setLastname(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patientDTO.setDateofbirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patientDTO.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patientDTO.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patientDTO.setCityvillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patientDTO.setStateprovince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patientDTO.setPostalcode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patientDTO.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patientDTO.setPhonenumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patientDTO.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = new PatientsDAO().getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patientDTO.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patientDTO.setPhonenumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patientDTO.setEducation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patientDTO.setEconomic(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patientDTO.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patientDTO.setSon_dau_wife(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ProfileImageTimestamp")) {

                }
                if (name.equalsIgnoreCase("createdDate")) {
                    patientDTO.setCreatedDate(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("providerUUID")) {
                    patientDTO.setProviderUUID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

        PatientRegistrationActivity.startPatientRegistration(this, patientDTO.getUuid(), PatientRegStage.PERSONAL);
//        Intent intent2 = new Intent(this, IdentificationActivity_New.class);
//        intent2.putExtra("patientUuid", patientDTO.getUuid());
//        intent2.putExtra("ScreenEdit", "personal_edit");
//        intent2.putExtra("patient_detail", true);
//
//        Bundle args = new Bundle();
//        args.putSerializable("patientDTO", (Serializable) patientDTO);
//        intent2.putExtra("BUNDLE", args);
//        startActivity(intent2);
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }
}