package org.intelehealth.app.utilities;

import org.intelehealth.app.utilities.CustomLog;

import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.CancelRequest;
import org.intelehealth.app.appointment.model.CancelResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentUtils {
    public void cancelAppointmentRequestOnVisitEnd(String visitID, int appointment_id, String reason, String providerID, String baseurl) {
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        CancelRequest request = new CancelRequest();

        request.setVisitUuid(visitID);
        request.setId(appointment_id);
        request.setReason(reason);
        request.setHwUUID(providerID);

        ApiClientAppointment.getInstance(baseurl).getApi().cancelAppointment(request).enqueue(new Callback<CancelResponse>() {
            @Override
            public void onResponse(Call<CancelResponse> call, Response<CancelResponse> response) {
                if (response.body() == null) return;
                CancelResponse cancelResponse = response.body();
                if (cancelResponse.isStatus()) {
                    appointmentDAO.deleteAppointmentByVisitId(visitID);
                }
            }

            @Override
            public void onFailure(Call<CancelResponse> call, Throwable t) {
                CustomLog.v("onFailure", t.getMessage());
            }
        });
    }
}