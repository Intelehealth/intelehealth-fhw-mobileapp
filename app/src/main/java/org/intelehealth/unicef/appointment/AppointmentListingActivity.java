package org.intelehealth.unicef.appointment;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;
import org.intelehealth.unicef.appointment.adapter.AppointmentListingAdapter;
import org.intelehealth.unicef.appointment.api.ApiClientAppointment;
import org.intelehealth.unicef.appointment.dao.AppointmentDAO;
import org.intelehealth.unicef.appointment.model.AppointmentInfo;
import org.intelehealth.unicef.appointment.model.AppointmentListingResponse;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentListingActivity extends LocalConfigActivity {
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
//        mSelectedStartDate = simpleDateFormat.format(new Date());
        mSelectedStartDate = "01/01/1970";
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
                        if (response.body() == null) return;
                        AppointmentListingResponse slotInfoResponse = response.body();
                        AppointmentDAO appointmentDAO = new AppointmentDAO();
                        appointmentDAO.deleteAllAppointments();
                        for (int i = 0; i < slotInfoResponse.getData().size(); i++) {

                            try {
                                appointmentDAO.insert(slotInfoResponse.getData().get(i));
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                        getAppointments();
                        /*AppointmentListingAdapter slotListingAdapter = new AppointmentListingAdapter(rvAppointments,
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
                        }*/
                    }

                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
//        Locale locale = new Locale(appLanguage);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
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