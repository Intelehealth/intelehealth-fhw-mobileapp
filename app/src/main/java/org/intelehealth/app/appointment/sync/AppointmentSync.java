package org.intelehealth.app.appointment.sync;

import android.content.Context;
import android.util.Log;


import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.models.auth.ResponseChecker;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentSync {

    public static void getAppointments(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String selectedStartDate = simpleDateFormat.format(new Date());
        String selectedEndDate = simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
        String baseurl = BuildConfig.SERVER_URL + ":3004";
        ApiClientAppointment.getInstance(baseurl)
                .getApi()
                .getSlotsAll(selectedStartDate, selectedEndDate, sessionManager.getLocationUuid(), authHeader)

                .enqueue(new Callback<AppointmentListingResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call, retrofit2.Response<AppointmentListingResponse> response) {
                        ResponseChecker<AppointmentListingResponse> responseChecker = new ResponseChecker<>(response);
                        if (responseChecker.isNotAuthorized()) {
                            //TODO: redirect to login screen
                            return;
                        }

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
