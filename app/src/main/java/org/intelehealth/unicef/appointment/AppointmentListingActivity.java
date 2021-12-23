package org.intelehealth.unicef.appointment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.appointment.adapter.AppointmentListingAdapter;
import org.intelehealth.unicef.appointment.adapter.SlotListingAdapter;
import org.intelehealth.unicef.appointment.api.ApiClientAppointment;
import org.intelehealth.unicef.appointment.dao.AppointmentDAO;
import org.intelehealth.unicef.appointment.model.AppointmentInfo;
import org.intelehealth.unicef.appointment.model.AppointmentListingResponse;
import org.intelehealth.unicef.appointment.model.SlotInfo;
import org.intelehealth.unicef.appointment.model.SlotInfoResponse;
import org.intelehealth.unicef.utilities.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentListingActivity extends AppCompatActivity {
    RecyclerView rvAppointments;
    private String mSelectedStartDate = "";
    private String mSelectedEndDate = "";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_listing);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.appointment_listing_title);
        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mSelectedStartDate = simpleDateFormat.format(new Date());
        mSelectedEndDate = simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
        getAppointments();
        getSlots();
    }

    private void getAppointments() {
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointments();
        AppointmentListingAdapter appointmentListingAdapter = new AppointmentListingAdapter(rvAppointments, this, appointmentInfoList, new AppointmentListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(AppointmentInfo appointmentInfo) {

            }
        });
        rvAppointments.setAdapter(appointmentListingAdapter);
        if (appointmentInfoList.isEmpty()) {
            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getSlots() {

        String baseurl = "https://" + new SessionManager(this).getServerUrl() + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlotsAll(mSelectedStartDate, mSelectedEndDate, new SessionManager(this).getLocationUuid())

                .enqueue(new Callback<AppointmentListingResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call, retrofit2.Response<AppointmentListingResponse> response) {
                        AppointmentListingResponse slotInfoResponse = response.body();

                        AppointmentListingAdapter slotListingAdapter = new AppointmentListingAdapter(rvAppointments,
                                AppointmentListingActivity.this,
                                slotInfoResponse.getData(), new AppointmentListingAdapter.OnItemSelection() {
                            @Override
                            public void onSelect(AppointmentInfo appointmentInfo) {

                            }


                        });
                        rvAppointments.setAdapter(slotListingAdapter);
                        if (slotListingAdapter.getItemCount() == 0) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }
}