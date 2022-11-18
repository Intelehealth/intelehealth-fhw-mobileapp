package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterModifiedDateForPrescGiven;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;
import static org.intelehealth.app.database.dao.VisitsDAO.fetchVisitModifiedDateForPrescPending;
import static org.intelehealth.app.database.dao.VisitsDAO.isVisitNotEnded;
import static org.intelehealth.app.utilities.DateAndTimeUtils.timeAgoFormat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.visit.PrescriptionActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.CancelRequest;
import org.intelehealth.app.appointment.model.CancelResponse;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailsActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentDetailsActiv";
    RelativeLayout stateAppointmentPrescription, layoutPrevScheduledOn, layoutPatientHistory, layoutVisitSummary, stateAppointmentStarted;
    LinearLayout layoutPrescButtons, layoutContactAction, layoutEndVisit;
    TextView tvPrescStatus, tvRescheduleOnTitle, tvAppointmentTime, tvPatientName, tvOpenMrsID, tvGenderAgeText, tvChiefComplaintTxt,
            tvVisitId, tvVisitStartDate, tvVisitStartTime, tvDrSpeciality;
    ImageView ivPrescription, ivDrawerPrescription, ivProfileImage, ivDrawerVisitSummary, ivCallPatient, ivWhatsappPatient;
    Button btnEndVisit, btnRescheduleAppointment, btnCancelAppointment;
    View layoutSummaryBtns;
    FloatingActionButton fabHelp;
    int appointment_id = 0;
    private ImageView priorityTag;
    private boolean isEmergency, hasPrescription;
    private String patientName, patientUuid, gender, age, openmrsID,
            visitID, visit_speciality, followupDate, patient_photo_path, app_start_date, app_start_time, app_start_day;
    SQLiteDatabase db;
    boolean isVisitStartsIn = false;
    private String vitalsUUID, adultInitialUUID;
    private static final int SCHEDULE_LISTING_INTENT = 2010;
    private String mEngReason = "";
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details_ui2);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        sessionManager = new SessionManager(this);
        initUI();

    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText(getResources().getString(R.string.appointment_details));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }


        stateAppointmentStarted = findViewById(R.id.state_appointment_started);
        stateAppointmentPrescription = findViewById(R.id.state_prescription_appointment);
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
        priorityTag = findViewById(R.id.priority_tag_app_details);
        tvPatientName = findViewById(R.id.patname_txt);
        tvOpenMrsID = findViewById(R.id.openmrsID_txt);
        tvGenderAgeText = findViewById(R.id.gender_age_txt);
        tvChiefComplaintTxt = findViewById(R.id.chief_complaint_txt);
        tvVisitId = findViewById(R.id.visitID);
        tvVisitStartDate = findViewById(R.id.visit_startDate_appointment);
        tvVisitStartTime = findViewById(R.id.tv_starttime_appointment);
        tvDrSpeciality = findViewById(R.id.dr_speciality_appointment);
        ivDrawerVisitSummary = findViewById(R.id.iv_drawer_visit_summary);
        btnRescheduleAppointment = findViewById(R.id.btn_reschedule_appointment);
        btnCancelAppointment = findViewById(R.id.btn_cancel_appointment);
        ivCallPatient = findViewById(R.id.iv_call_patient_app);
        ivWhatsappPatient = findViewById(R.id.iv_whatsapp_patient_app);


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            patientUuid = intent.getStringExtra("patientUuid");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");

            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            visit_speciality = intent.getStringExtra("visit_speciality");
            app_start_date = intent.getStringExtra("app_start_date");
            app_start_time = intent.getStringExtra("app_start_time");
            appointment_id = intent.getIntExtra("appointment_id", 0);
            app_start_day = intent.getStringExtra("app_start_day");


            // appointment_id = getIntent().getIntExtra("appointment_id");
            Log.d(TAG, "initUI: appointment_id : " + appointment_id);
            followupDate = intent.getStringExtra("followup_date");
            if (followupDate == null)
                followupDate = getFollowupDataForVisitUUID(visitID);
            isEmergency = intent.getBooleanExtra("priority_tag", false);
            hasPrescription = intent.getBooleanExtra("hasPrescription", false);
            patient_photo_path = intent.getStringExtra("patient_photo");


        }

        if (patientUuid != null && !patientUuid.isEmpty()) {
            String[] DobAndGender = PatientsDAO.getPatientDobAgeGender(patientUuid);
            if (DobAndGender.length > 0) {
                String age1 = DateAndTimeUtils.getAge_FollowUp(DobAndGender[1], this);
                tvGenderAgeText.setText(DobAndGender[0] + " " + age1);
                gender = DobAndGender[0];
                age = DobAndGender[1];
            }
        }
        // Patient Photo
        if (patient_photo_path != null) {
            Glide.with(this)
                    .load(patient_photo_path)
                    .thumbnail(0.3f)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivProfileImage);
        } else {
            ivProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.avatar1));
        }

        // common data setting

        tvPatientName.setText(patientName);
        tvOpenMrsID.setText(openmrsID);
        tvVisitId.setText("Visit ID : " + visitID);
        String chief_complaint_value = getChiefComplaint(visitID);
        Log.d(TAG, "initUI: chief_complaint_value : " + chief_complaint_value);
        int first = chief_complaint_value.indexOf("<b>");
        int last = chief_complaint_value.indexOf("</b>");
        Log.d(TAG, "initUI: chief_complaint_value : " + chief_complaint_value);
        chief_complaint_value = chief_complaint_value.substring(first, last + 4);
        tvChiefComplaintTxt.setText(Html.fromHtml(chief_complaint_value));
        tvDrSpeciality.setText(visit_speciality);

        //appointment started state - make "stateAppointmentStarted" visible
        String timeText = getAppointmentStartsInTime(app_start_date, app_start_time);
        tvVisitStartDate.setText(DateAndTimeUtils.getDateInDDMMMMYYYYFormat(app_start_date));
        tvVisitStartTime.setText(app_start_time);
        if (isVisitStartsIn) { //that means appointment scheduled
            stateAppointmentStarted.setVisibility(View.VISIBLE);
            layoutContactAction.setVisibility(View.GONE);
            tvRescheduleOnTitle.setVisibility(View.GONE);
            tvAppointmentTime.setText("Starts " + timeText);
            layoutSummaryBtns.setVisibility(View.VISIBLE);


            btnRescheduleAppointment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String subtitle = "Are you sure you want to Reschedule the appointment for <b>" + patientName + "?</b>";
                    rescheduleAppointment(AppointmentDetailsActivity.this, "Reschedule appointment?", subtitle, "Yes", "No");

                }
            });
            btnCancelAppointment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String subtitle = "Are you sure you want to cancel the appointment for<b>" + patientName + "?</b>";
                    cancelAppointment(AppointmentDetailsActivity.this, "Cancel appointment?", subtitle, "Yes", "No");

                }
            });

        } else {
            //appointment scheduled and time has been passed
            //prescription pending  state - make "stateAppointmentStarted" visible, "tvAppointmentTime" gone, "stateAppointmentPrescription" visible


            stateAppointmentStarted.setVisibility(View.VISIBLE);
            tvAppointmentTime.setVisibility(View.GONE);
            stateAppointmentPrescription.setVisibility(View.VISIBLE);
            tvPrescStatus.setTextColor(getResources().getColor(R.color.colorPrimary2));
        }


        if (hasPrescription) {
            //prescription received  state - make "stateAppointmentStarted" visible,
            // "tvAppointmentTime" gone, "stateAppointmentPrescription" visible, "layoutPrescButtons" gone

            stateAppointmentStarted.setVisibility(View.VISIBLE);
            tvAppointmentTime.setVisibility(View.GONE);
            stateAppointmentPrescription.setVisibility(View.VISIBLE);
            layoutPrescButtons.setVisibility(View.GONE);
            // tvPrescStatus.setText("Received 2 hours ago");
            tvPrescStatus.setTextColor(getResources().getColor(R.color.colorPrimary1));
            ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_prescription_green));
            fabHelp.setVisibility(View.GONE);
            //presc_arrowRight.setVisibility(View.VISIBLE);
            // presc_remind_block.setVisibility(View.GONE);
            String modifiedDate = fetchEncounterModifiedDateForPrescGiven(visitID);
            modifiedDate = timeAgoFormat(modifiedDate);
            tvPrescStatus.setText("Received " + modifiedDate);
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
                    VisitUtils.endVisit(AppointmentDetailsActivity.this, visitID, patientUuid, followupDate,
                            vitalsUUID, adultInitialUUID, "state", patientName, "AppointmentDetailsActivity");
                });
            } else {
                layoutEndVisit.setVisibility(View.GONE);
            }
            // end visit - end
        } else {
            // if no presc given than show the dialog of remind and pending based on time passed from visit uplaoded.
            ivDrawerPrescription.setVisibility(View.GONE);
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
            tvPrescStatus.setText("Pending since " + modifiedDate.replace("ago", ""));
            tvPrescStatus.setTextColor(getResources().getColor(R.color.red));
        }
        // presc block - end

        // visit summary - start
        vitalsUUID = fetchEncounterUuidForEncounterVitals(visitID);
        adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitID);

        ivDrawerVisitSummary.setOnClickListener(v -> {
            Intent in = new Intent(this, VisitSummaryActivity_New.class);
            in.putExtra("patientUuid", patientUuid);
            in.putExtra("visitUuid", visitID);
            in.putExtra("gender", gender);
            in.putExtra("name", patientName);
            in.putExtra("encounterUuidVitals", vitalsUUID);
            in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
            in.putExtra("float_ageYear_Month", age);
            in.putExtra("tag", "AppointmentDetailsActivity");
            startActivity(in);
        });
        // visit summary - end

        //reschedule appointment

        //appointment rescheduled
       /* stateAppointmentPrescription.setVisibility(View.GONE);
        layoutPrescButtons.setVisibility(View.GONE);
        btnEndVisit.setVisibility(View.GONE);
        layoutSummaryBtns.setVisibility(View.GONE);
        layoutContactAction.setVisibility(View.GONE);
        tvAppointmentTime.setVisibility(View.VISIBLE);
        layoutPrevScheduledOn.setVisibility(View.VISIBLE);
        tvRescheduleOnTitle.setVisibility(View.VISIBLE);
        tvAppointmentTime.setText("Starts in 1 day");*/

        //prescription pending  state : click event - make "stateAppointmentStarted" visible,
        // "tvAppointmentTime" gone, "stateAppointmentPrescription" visible, "layoutPrescButtons" visible
       /*  stateAppointmentStarted.setVisibility(View.VISIBLE);
        tvAppointmentTime.setVisibility(View.GONE);
        fabHelp.setVisibility(View.VISIBLE);
        tvPrescStatus.setTextColor(getResources().getColor(R.color.colorPrimary2));
        stateAppointmentPrescription.setVisibility(View.VISIBLE);
        stateAppointmentPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivDrawerPrescription.setVisibility(View.GONE);
                layoutPrescButtons.setVisibility(View.VISIBLE);
            }
        });*/

        //prescription received  state - make "stateAppointmentStarted" visible,
        // "tvAppointmentTime" gone, "stateAppointmentPrescription" visible, "layoutPrescButtons" gone
     /* stateAppointmentStarted.setVisibility(View.VISIBLE);
        tvAppointmentTime.setVisibility(View.GONE);
        stateAppointmentPrescription.setVisibility(View.VISIBLE);
        layoutPrescButtons.setVisibility(View.GONE);
        tvPrescStatus.setText("Received 2 hours ago");
        tvPrescStatus.setTextColor(getResources().getColor(R.color.colorPrimary1));
        ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_prescription_green));
        fabHelp.setVisibility(View.GONE);*/

        //prescription pending  state : click event - make "stateAppointmentStarted" visible,
        // "tvAppointmentTime" gone, "stateAppointmentPrescription" visible, "layoutPrescButtons" visible
        /*stateAppointmentStarted.setVisibility(View.VISIBLE);
        tvAppointmentTime.setVisibility(View.GONE);
        stateAppointmentPrescription.setVisibility(View.VISIBLE);
        fabHelp.setVisibility(View.GONE);
        tvPrescStatus.setText("Received 2 hours ago");
        tvPrescStatus.setTextColor(getResources().getColor(R.color.colorPrimary1));
        ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_prescription_green));
        stateAppointmentPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPrescButtons.setVisibility(View.GONE);
                layoutEndVisit.setVisibility(View.VISIBLE);
            }
        });*/

        //appointment pending
     /*  stateAppointmentStarted.setVisibility(View.VISIBLE);
        tvAppointmentTime.setVisibility(View.VISIBLE);
        stateAppointmentPrescription.setVisibility(View.GONE);
        layoutPrescButtons.setVisibility(View.GONE);
        btnEndVisit.setVisibility(View.GONE);
        layoutContactAction.setVisibility(View.GONE);
        layoutVisitSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutSummaryBtns.setVisibility(View.VISIBLE);

            }
        });*/


        //appointment with patient history
      /*       layoutPatientHistory.setVisibility(View.VISIBLE);
        layoutPrevScheduledOn.setVisibility(View.GONE);
        layoutContactAction.setVisibility(View.GONE);
        layoutPrescButtons.setVisibility(View.GONE);
        btnEndVisit.setVisibility(View.GONE);
        layoutSummaryBtns.setVisibility(View.GONE);
        stateAppointmentPrescription.setVisibility(View.GONE);*/
    }


    private String getChiefComplaint(String visitUUID) {
        String chief_complaint_value = "";
        Log.v("Followup", "visitid: " + visitUUID);
        if (visitUUID != null && !visitUUID.isEmpty()) {
            String complaint_query = "select e.uuid, o.value  from tbl_encounter e, tbl_obs o where " +
                    "e.visituuid = ? " +
                    "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
                    "and e.uuid = o.encounteruuid and o.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'"; // chief complaint

            final Cursor cursor = db.rawQuery(complaint_query, new String[]{visitUUID});
            if (cursor.moveToFirst()) {
                do {
                    try {
                        chief_complaint_value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                        Log.v("Followup", "chiefcomplaint: " + chief_complaint_value);
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
                if (minutes >= 60) {
                    long hours = minutes / 60;
                    if (hours > 24) {

                        timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(soltDate) + ", at " + slotTime;
                    } else {
                        timeText = "In " + hours + " hours, at " + slotTime;

                    }
                } else {
                    timeText = "In " + minutes + " minutes";
                }
            } else {
                isVisitStartsIn = false;
            }


        } catch (ParseException e) {
            Log.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return timeText;
    }

    public void rescheduleAppointment(Context context, String title, String subTitle,
                                      String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_book_app_red));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            startActivityForResult(new Intent(context, ScheduleAppointmentActivity_New.class)
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
            );

        });

        alertDialog.show();
    }

    public void cancelAppointment(Context context, String title, String subTitle,
                                  String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_book_app_red));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            askReason();


        });

        alertDialog.show();
    }

    private void cancelAppointmentRequest(String reason) {
        CancelRequest request = new CancelRequest();
        request.setVisitUuid(visitID);
        request.setId(appointment_id);
        request.setReason(reason);
        request.setHwUUID(new SessionManager(AppointmentDetailsActivity.this).getProviderID()); // user id / healthworker id
        String baseurl = "https://" + sessionManager.getServerUrl() + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .cancelAppointment(request)
                .enqueue(new Callback<CancelResponse>() {
                    @Override
                    public void onResponse(Call<CancelResponse> call, Response<CancelResponse> response) {
                        if (response.body() == null) return;
                        CancelResponse cancelResponse = response.body();
                        if (cancelResponse.isStatus()) {
                            AppointmentDAO appointmentDAO = new AppointmentDAO();
                            //AppointmentInfo appointmentInfo=appointmentDAO.getAppointmentByVisitId(visitUuid);
                            //if(appointmentInfo!=null && appointmentInfo.getStatus().equalsIgnoreCase("booked")) {
                            appointmentDAO.deleteAppointmentByVisitId(visitID);
                            //}

                            Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.appointment_cancelled_success_txt), Toast.LENGTH_SHORT).show();
                            //   getAppointmentDetails(mAppointmentDetailsResponse.getData().getVisitUuid());
                            Intent intent = new Intent(AppointmentDetailsActivity.this, HomeScreenActivity_New.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.failed_to_cancel_appointment), Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<CancelResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });
    }

    private void askReason() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.appointment_cancel_reason_view);

        final TextView titleTextView = (TextView) dialog.findViewById(R.id.titleTv);
        titleTextView.setText(getString(R.string.please_select_your_cancel_reason));
        final EditText reasonEtv = dialog.findViewById(R.id.reasonEtv);
        reasonEtv.setVisibility(View.GONE);
        final RadioGroup optionsRadioGroup = dialog.findViewById(R.id.reasonRG);
        optionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbR1) {
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.doctor_is_not_available));
                    mEngReason = "Doctor is not available";
                } else if (checkedId == R.id.rbR2) {
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.patient_is_not_available));
                    mEngReason = "Patient is not available";
                } else if (checkedId == R.id.rbR3) {
                    reasonEtv.setText("");
                    reasonEtv.setVisibility(View.VISIBLE);
                }
            }
        });

        final TextView textView = dialog.findViewById(R.id.submitTV);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String reason = reasonEtv.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(AppointmentDetailsActivity.this, getString(R.string.please_enter_reason_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                cancelAppointmentRequest(mEngReason.isEmpty() ? reason : mEngReason);
            }
        });

        dialog.show();

    }

}