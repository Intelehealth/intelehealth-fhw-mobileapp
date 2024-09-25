package org.intelehealth.config.network

import com.intelehealth.appointment.network.request.CancelRequest
import com.intelehealth.appointment.network.response.AppointmentDetailsResponse
import com.intelehealth.appointment.network.response.AppointmentListingResponse
import com.intelehealth.appointment.network.response.CancelResponse
import com.intelehealth.appointment.network.response.SlotInfoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Tanvir Hasan. on 25-09-2024 - 15:46.
 * Email : mhasan@intelehealth.org
 **/

interface AppointmentWebClient {
    @GET("api/appointment/getAppointment/{visitUuid}")
    fun getAppointmentDetails(@Path("visitUuid") visitUuid: String?): Call<AppointmentDetailsResponse?>?

    @POST("api/appointment/cancelAppointment")
    fun cancelAppointment(@Body request: CancelRequest?): Call<CancelResponse?>?

    // https://uniceftraining.intelehealth.org:3004/api/appointment/getAppointmentSlots?fromDate=16/12/2021&toDate=31/12/2021&speciality=Neurologist
    @GET("api/appointment/getAppointmentSlots?")
    fun getSlots(
        @Query("fromDate") mSelectedStartDate: String?, @Query("toDate") mSelectedEndDate: String?,
        @Query("speciality") speciality: String?
    ): Call<SlotInfoResponse?>?

    @GET("api/appointment/getSlots?")
    fun getSlotsAll(
        @Query("fromDate") mSelectedStartDate: String?,
        @Query("toDate") mSelectedEndDate: String?,
        @Query("locationUuid") locationUuid: String?
    ): Call<AppointmentListingResponse?>?
}