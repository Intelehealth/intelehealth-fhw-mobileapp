package com.intelehealth.appointment.data.remote

import com.intelehealth.appointment.data.remote.request.CancelRequest
import com.intelehealth.appointment.data.remote.response.AppointmentDetailsResponse
import com.intelehealth.appointment.data.remote.response.AppointmentListingResponse
import com.intelehealth.appointment.data.remote.response.CancelResponse
import com.intelehealth.appointment.data.remote.response.SlotInfoResponse
import retrofit2.Call
import retrofit2.Response
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
    suspend fun getSlots(
        @Query("fromDate") mSelectedStartDate: String?, @Query("toDate") mSelectedEndDate: String?,
        @Query("speciality") speciality: String?
    ): Response<SlotInfoResponse?>?

    @GET("api/appointment/getSlots?")
    suspend fun getSlotsAll(
        @Query("fromDate") mSelectedStartDate: String?,
        @Query("toDate") mSelectedEndDate: String?,
        @Query("locationUuid") locationUuid: String?
    ): Response<AppointmentListingResponse>?
}