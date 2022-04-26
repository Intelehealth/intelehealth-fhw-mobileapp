package org.intelehealth.unicef.appointment.sync;

import android.content.Context;
import android.util.Log;

import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.appointment.api.ApiClientAppointment;
import org.intelehealth.unicef.appointment.dao.AppointmentDAO;
import org.intelehealth.unicef.appointment.model.AppointmentListingResponse;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentSync {
    public static void getAppointments(Context context) {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String selectedStartDate = simpleDateFormat.format(new Date());
        String selectedEndDate = simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
//        String baseurl = "https://" + new SessionManager(context).getServerUrl() + ":3004";

        /* below changes are done to handle the following crash in firebase crashlytics
            Fatal Exception: java.lang.RuntimeException
            Unable to start activity ComponentInfo{org.intelehealth.unicef/org.intelehealth.unicef.activities.homeActivity.HomeActivity}:
            java.lang.IllegalArgumentException: Invalid URL host: ""
             Version No. 1.8.13(23)

            - By Nishita Goyal on 26th April 2022
         */

        String baseurl = "";
        if (sessionManager.getServerUrl() != null && !sessionManager.getServerUrl().equalsIgnoreCase(""))
            baseurl = "https://" + sessionManager.getServerUrl() + ":3004";
        else return;
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlotsAll(selectedStartDate, selectedEndDate, new SessionManager(context).getLocationUuid())

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
                    }


                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }
}
