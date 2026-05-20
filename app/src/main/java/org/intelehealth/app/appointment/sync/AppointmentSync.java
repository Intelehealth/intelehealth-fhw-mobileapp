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
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentSync {

    private static final String TAG = "AppointmentSync";

    public static void getAppointments(Context context) {

        CustomLog.v(TAG, "getAppointments");

        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        String selectedStartDate = simpleDateFormat.format(new Date());

        String selectedEndDate = simpleDateFormat.format(
                new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        );

        SessionManager sessionManager = new SessionManager(context);

        String baseurl = sessionManager.getServerUrl() + ":3004";

        ApiClientAppointment.getInstance(baseurl)
                .getApi()
                .getSlotsAll(
                        selectedStartDate,
                        selectedEndDate,
                        sessionManager.getLocationUuid()
                )
                .enqueue(new Callback<AppointmentListingResponse>() {

                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call,
                                           retrofit2.Response<AppointmentListingResponse> response) {

                        if (response.body() == null) return;

                        AppointmentListingResponse slotInfoResponse = response.body();

                        Executors.newSingleThreadExecutor().execute(() -> {

                            AppointmentDAO appointmentDAO = new AppointmentDAO();

                            try {

                                // 🔥 Step 1: Delete safely
                                appointmentDAO.deleteAllAppointments();

                                // 🔥 Step 2: Insert loop safely
                                if (slotInfoResponse.getData() != null) {

                                    for (int i = 0; i < slotInfoResponse.getData().size(); i++) {

                                        try {
                                            appointmentDAO.insert(slotInfoResponse.getData().get(i));
                                        } catch (DAOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // 🔥 Step 3: Broadcast AFTER DB completes
                            sendSyncCompleted(context);
                        });
                    }

                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        CustomLog.v(TAG, t.getMessage());
                        new NavigationUtils().logoutOperation(context, t);
                    }
                });
    }

    // =========================
    // BROADCAST SEPARATED
    // =========================
    private static void sendSyncCompleted(Context context) {

        Intent broadcast = new Intent();
        broadcast.putExtra("JOB",
                AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE);

        broadcast.setPackage(
                IntelehealthApplication.getAppContext().getPackageName()
        );

        broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);

        context.sendBroadcast(broadcast);


        IntelehealthApplication.getAppContext().sendBroadcast(
                new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .putExtra(
                                AppConstants.SYNC_INTENT_DATA_KEY,
                                AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE
                        )
                        .setPackage(
                                IntelehealthApplication.getAppContext().getPackageName()
                        )
        );
    }
}
