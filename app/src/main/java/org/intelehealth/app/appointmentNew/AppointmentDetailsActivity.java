package org.intelehealth.app.appointmentNew;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;

public class AppointmentDetailsActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentDetailsActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details_ui2);

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
        RelativeLayout stateAppointmentStarted = findViewById(R.id.state_appointment_started);
        RelativeLayout stateAppointmentPrescription = findViewById(R.id.state_prescription_appointment);
        LinearLayout layoutPrescButtons = findViewById(R.id.layout_presc_buttons);
        LinearLayout layoutContactAction = findViewById(R.id.layout_contact_action);
        TextView tvPrescStatus = findViewById(R.id.tv_presc_status_new);
        ImageView ivPrescription = findViewById(R.id.iv_prescription111);
        Button btnEndVisit = findViewById(R.id.btn_end_visit_appointment);
        View layoutSummaryBtns = findViewById(R.id.layout_visit_summary_buttons);
        RelativeLayout layoutPrevScheduledOn = findViewById(R.id.state_prev_scheduled_on);
        RelativeLayout layoutPatientHistory = findViewById(R.id.layout_patient_history);
        TextView tvRescheduleOnTitle = findViewById(R.id.tv_reschedule_on);
        TextView tvAppointmentTime = findViewById(R.id.tv_appointment_time_details);
        ImageView ivDrawerPrescription = findViewById(R.id.iv_drawer_prescription);
        FloatingActionButton fabHelp = findViewById(R.id.fab_help_appointment);
        LinearLayout layoutEndVisit  = findViewById(R.id.layout_end_visit);
        RelativeLayout layoutVisitSummary  = findViewById(R.id.layout_visit_summary);




        //appointment started state - make "stateAppointmentStarted" visible
            stateAppointmentStarted.setVisibility(View.VISIBLE);
        layoutContactAction.setVisibility(View.GONE);
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

}