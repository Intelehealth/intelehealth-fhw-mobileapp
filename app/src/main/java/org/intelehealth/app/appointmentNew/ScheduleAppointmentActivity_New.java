package org.intelehealth.app.appointmentNew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;

public class ScheduleAppointmentActivity_New extends AppCompatActivity {
    private static final String TAG = "ScheduleAppointmentActi";
    ImageView ivPrevMonth, ivNextMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment_new);


        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        View toolbar = findViewById(R.id.toolbar_schedule_appointments);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText("Schedule appointment");
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }


        initUI();
    }

    private void initUI() {
        RecyclerView rvMorningSlots = findViewById(R.id.rv_morning_time_slots);
        RecyclerView rvAfternoonSlots = findViewById(R.id.rv_afternoon_time_slots);
        RecyclerView rvEveningSlots = findViewById(R.id.rv_evening_time_slots);

        ivPrevMonth = findViewById(R.id.iv_prev_month);
        ivNextMonth = findViewById(R.id.iv_next_month);

        rvMorningSlots.setHasFixedSize(true);
        rvMorningSlots.setLayoutManager(new GridLayoutManager(this, 3));

        rvAfternoonSlots.setHasFixedSize(true);
        rvAfternoonSlots.setLayoutManager(new GridLayoutManager(this, 3));

        rvEveningSlots.setHasFixedSize(true);
        rvEveningSlots.setLayoutManager(new GridLayoutManager(this, 3));

    /*    PickUpTimeSlotsAdapter pickUpTimeSlotsAdapter = new PickUpTimeSlotsAdapter(this);
        rvMorningSlots.setAdapter(pickUpTimeSlotsAdapter);
        rvAfternoonSlots.setAdapter(pickUpTimeSlotsAdapter);
        rvEveningSlots.setAdapter(pickUpTimeSlotsAdapter);*/

        generateMonthsDynamically();
    }

    private void generateMonthsDynamically() {

        LinearLayout layoutDates = findViewById(R.id.layout_dates);
        for (int i = 1; i <= 12; i++) {
            Button button = new Button(this);
            button.setText("Button " + i);
            button.setLayoutParams(new
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ScheduleAppointmentActivity_New.this, "This button is created dynamically",
                            Toast.LENGTH_SHORT).show();
                }
            });
            if (layoutDates != null) {
                layoutDates.addView(button);
            }

        }
        ivPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ivNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}