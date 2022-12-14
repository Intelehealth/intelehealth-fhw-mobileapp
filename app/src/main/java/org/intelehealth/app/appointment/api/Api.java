package org.intelehealth.app.appointment.api;



import com.google.gson.JsonObject;

import org.intelehealth.app.appointment.model.AppointmentDetailsResponse;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.appointment.model.CancelRequest;
import org.intelehealth.app.appointment.model.CancelResponse;
import org.intelehealth.app.appointment.model.SlotInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Api {

    /*Appointment Module*/
    //https://uniceftraining.intelehealth.org:3004/api/appointment/getAppointment/5d1ce60a-763f-4754-b410-7733e77d59ea
    @GET("api/appointment/getAppointment/{visitUuid}")
    Call<AppointmentDetailsResponse> getAppointmentDetails(@Path("visitUuid") String visitUuid);

    @POST("api/appointment/cancelAppointment")
    Call<CancelResponse> cancelAppointment(@Body CancelRequest request);

    // https://uniceftraining.intelehealth.org:3004/api/appointment/getAppointmentSlots?fromDate=16/12/2021&toDate=31/12/2021&speciality=Neurologist
    @GET("api/appointment/getAppointmentSlots?")
    Call<SlotInfoResponse> getSlots(@Query("fromDate") String mSelectedStartDate, @Query("toDate")String mSelectedEndDate,
                                    @Query("speciality")String speciality);

    @GET("api/appointment/getSlots?")
    Call<AppointmentListingResponse> getSlotsAll(@Query("fromDate") String mSelectedStartDate,
                                                 @Query("toDate")String mSelectedEndDate,
                                                 @Query("locationUuid")String locationUuid
    );

    @POST
    Call<AppointmentDetailsResponse> bookAppointment(@Url String url,  @Body BookAppointmentRequest request);

    /*@GET("/api/openmrs/forgetPassword/requestOtp?")
    Call<AppointmentListingResponse> forgotPassword(@Query("fromDate") String mSelectedStartDate,
                                                 @Query("toDate")String mSelectedEndDate,
                                                 @Query("locationUuid")String locationUuid
    );*/

}
