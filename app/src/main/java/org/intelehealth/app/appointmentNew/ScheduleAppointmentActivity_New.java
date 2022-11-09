package org.intelehealth.app.appointmentNew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.appointment.ScheduleListingActivity;
import org.intelehealth.app.appointment.adapter.SlotListingAdapter;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.appointment.model.SlotInfoResponse;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import retrofit2.Call;
import retrofit2.Callback;

public class ScheduleAppointmentActivity_New extends AppCompatActivity {
    private static final String TAG = "ScheduleAppointmentActi";
    ImageView ivPrevMonth, ivNextMonth;
    RecyclerView rvMorningSlots, rvAfternoonSlots, rvEveningSlots;

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
        getSlots();
    }

    private void initUI() {
        rvMorningSlots = findViewById(R.id.rv_morning_time_slots);
        rvAfternoonSlots = findViewById(R.id.rv_afternoon_time_slots);
        rvEveningSlots = findViewById(R.id.rv_evening_time_slots);

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

    private void getSlots() {
        Log.d(TAG, "getSlots: ");
        String baseurl = "https://" + new SessionManager(this).getServerUrl() + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlots("2022-11-02", "2022-11-02", "General Physician")
                .enqueue(new Callback<SlotInfoResponse>() {
                    @Override
                    public void onResponse(Call<SlotInfoResponse> call, retrofit2.Response<SlotInfoResponse> response) {
                        SlotInfoResponse slotInfoResponse = response.body();
                        Log.d(TAG, "onResponse: "+response.body().toString());
                        SlotListingAdapter slotListingAdapter = new SlotListingAdapter(rvMorningSlots,
                                ScheduleAppointmentActivity_New.this,
                                slotInfoResponse.getDates(), slotInfo -> {
                            Log.d(TAG, "onResponse: dates list : "+slotInfoResponse.getDates().size());
                                    //------before reschedule need to cancel appointment----
                                    AppointmentDAO appointmentDAO = new AppointmentDAO();
                                    // appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                                   /* if (appointmentId != 0) {
                                        askReason(slotInfo);
                                    } else {
                                        bookAppointment(slotInfo, null);
                                    }*/

                                });
                        rvMorningSlots.setAdapter(slotListingAdapter);
                        if (slotListingAdapter.getItemCount() == 0) {
                          //  findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                       //     findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<SlotInfoResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }

}