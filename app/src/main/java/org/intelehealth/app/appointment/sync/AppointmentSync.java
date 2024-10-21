package org.intelehealth.app.appointment.sync;

import android.content.Context;
import android.content.Intent;
import org.intelehealth.app.utilities.CustomLog;

import com.github.ajalt.timberkt.Timber;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.utilities.NavigationUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentSync {
    private static final String TAG = "AppointmentSync";

    public static void getAppointments(Context context) {
        CustomLog.v(TAG, "getAppointments");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String selectedStartDate = simpleDateFormat.format(new Date());
        String selectedEndDate = simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));

        SessionManager sessionManager = new SessionManager(context);
        String baseurl = sessionManager.getServerUrl() + ":3004";
        Timber.tag(TAG).d("URL =>%s", BuildConfig.SERVER_URL);
        Timber.tag(TAG).d("Session URL =>%s", sessionManager.getServerUrl());
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
                                CustomLog.v(TAG, "insert = " + new Gson().toJson(slotInfoResponse.getData().get(i)));
                                appointmentDAO.insert(slotInfoResponse.getData().get(i));
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                        /*if (slotInfoResponse.getCancelledAppointments() != null) {
                            if (slotInfoResponse != null && slotInfoResponse.getCancelledAppointments().size() > 0) {
                                for (int i = 0; i < slotInfoResponse.getCancelledAppointments().size(); i++) {
                                    try {
                                        appointmentDAO.insert(slotInfoResponse.getCancelledAppointments().get(i));

                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                        }*/
                        CustomLog.v(TAG, "getAppointments done!");
                        Intent broadcast = new Intent();
                        broadcast.putExtra("JOB", AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE);
                        broadcast.setPackage(IntelehealthApplication.getAppContext().getPackageName());
                        broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
                        context.sendBroadcast(broadcast);

                        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE)
                                .setPackage(IntelehealthApplication.getAppContext().getPackageName()));
                    }


                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        CustomLog.v(TAG, t.getMessage());
                        //log out operation if response code is 401
                        new NavigationUtils().logoutOperation(context,t);
                    }
                });

    }
}
