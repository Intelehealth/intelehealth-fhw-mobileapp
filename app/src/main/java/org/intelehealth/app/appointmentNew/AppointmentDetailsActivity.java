package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.database.dao.EncounterDAO.getChiefComplaint;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentDetailsActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentDetailsActiv";
    RelativeLayout stateAppointmentPrescription, layoutPrevScheduledOn, layoutPatientHistory, layoutVisitSummary, stateAppointmentStarted;
    LinearLayout layoutPrescButtons, layoutContactAction, layoutEndVisit;
    TextView tvPrescStatus, tvRescheduleOnTitle, tvAppointmentTime, tvPatientName, tvOpenMrsID, tvGenderAgeText, tvChiefComplaintTxt,
            tvVisitId, tvVisitStartDate, tvVisitStartTime;
    ImageView ivPrescription, ivDrawerPrescription, ivProfileImage;
    Button btnEndVisit;
    View layoutSummaryBtns;
    FloatingActionButton fabHelp;
    private ImageView priorityTag;
    private boolean isEmergency, hasPrescription;
    private String patientName, patientUuid, gender, age, openmrsID,
            visitID, visit_speciality, followupDate, patient_photo_path, app_start_date, app_start_time;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details_ui2);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

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
            Log.d(TAG, "initUI: app_start_time : " + app_start_time);
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
                String age = DateAndTimeUtils.getAge_FollowUp(DobAndGender[1], this);
                tvGenderAgeText.setText(DobAndGender[0] + " " + age);
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
        int first = chief_complaint_value.indexOf("<b>");
        int last = chief_complaint_value.indexOf("</b>");
        chief_complaint_value = chief_complaint_value.substring(first, last + 4);

        tvChiefComplaintTxt.setText(Html.fromHtml(chief_complaint_value));
        tvVisitStartDate.setText(DateAndTimeUtils.getDateInDDMMMMYYYYFormat(app_start_date));
        tvVisitStartTime.setText(app_start_time);
        //tvAppointmentTime.setText(app_start_time);
       String timeText  = getAppointmentStartsInTime(app_start_date, app_start_time);
        Log.d(TAG, "initUI:timeText :  "+timeText);

        //appointment started state - make "stateAppointmentStarted" visible
        stateAppointmentStarted.setVisibility(View.VISIBLE);
        layoutContactAction.setVisibility(View.GONE);
        tvRescheduleOnTitle.setVisibility(View.GONE);
        tvRescheduleOnTitle.setVisibility(View.GONE);

        //prescription pending  state - make "stateAppointmentStarted" visible, "tvAppointmentTime" gone, "stateAppointmentPrescription" visible
         /*  stateAppointmentStarted.setVisibility(View.VISIBLE);
        tvAppointmentTime.setVisibility(View.GONE);
        stateAppointmentPrescription.setVisibility(View.VISIBLE);
        tvPrescStatus.setTextColor(getResources().getColor(R.color.colorPrimary2));
        */

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
            Log.v("AppointmentInfo", "Diff minutes - " + minutes);

            //check for appointmet but presc not given and visit not completed
            if (minutes > 0) {
                if (minutes >= 60) {
                    long hours = minutes / 60;
                    if (hours > 24) {

                        timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(soltDate) + ", at " + slotTime;
                        // holder.tvDate.setText(timeText);
                      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            holder.tvDate.setTextColor(context.getColor(R.color.iconTintGray));
                        }*/
                    } else {
                        timeText = "In " + hours + " hours, at " + slotTime;

                        //holder.tvDate.setText(timeText);
                      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                        }*/
                    }
                } else {
                    timeText = "In " + minutes + " minutes";

                    //   holder.tvDate.setText(timeText);
                   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                    }*/
                }
            }


        } catch (ParseException e) {
            Log.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return timeText;
    }

}