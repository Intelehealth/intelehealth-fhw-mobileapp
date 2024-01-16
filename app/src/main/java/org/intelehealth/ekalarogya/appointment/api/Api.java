package org.intelehealth.ekalarogya.appointment.api;


import org.intelehealth.ekalarogya.appointment.model.AppointmentDetailsResponse;
import org.intelehealth.ekalarogya.appointment.model.AppointmentListingResponse;
import org.intelehealth.ekalarogya.appointment.model.BookAppointmentRequest;
import org.intelehealth.ekalarogya.appointment.model.CancelRequest;
import org.intelehealth.ekalarogya.appointment.model.CancelResponse;
import org.intelehealth.ekalarogya.appointment.model.SlotInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Api {

    /*Appointment Module*/
    //https://uniceftraining.intelehealth.org:3004/api/appointment/getAppointment/5d1ce60a-763f-4754-b410-7733e77d59ea
    @Headers({"Accept: application/json"})
    @GET("api/appointment/getAppointment/{visitUuid}")
    Call<AppointmentDetailsResponse> getAppointmentDetails(
            @Path("visitUuid") String visitUuid,
            @Header("Authorization") String authHeader
    );

    @Headers({"Accept: application/json"})
    @POST("api/appointment/cancelAppointment")
    Call<CancelResponse> cancelAppointment(
            @Body CancelRequest request,
            @Header("Authorization") String authHeader
    );

    // https://uniceftraining.intelehealth.org:3004/api/appointment/getAppointmentSlots?fromDate=16/12/2021&toDate=31/12/2021&speciality=Neurologist
    @Headers({"Accept: application/json"})
    @GET("api/appointment/getAppointmentSlots?")
    Call<SlotInfoResponse> getSlots(
            @Query("fromDate") String mSelectedStartDate,
            @Query("toDate") String mSelectedEndDate,
            @Query("speciality") String speciality,
            @Header("Authorization") String authHeader
    );

    @Headers({"Accept: application/json"})
    @GET("api/appointment/getSlots?")
    Call<AppointmentListingResponse> getSlotsAll(
            @Query("fromDate") String mSelectedStartDate,
            @Query("toDate") String mSelectedEndDate,
            @Query("locationUuid") String locationUuid,
            @Header("Authorization") String authHeader
    );

    @Headers({"Accept: application/json"})
    @POST
    Call<AppointmentDetailsResponse> bookAppointment(
            @Url String url,
            @Body BookAppointmentRequest request,
            @Header("Authorization") String authHeader
    );

}
