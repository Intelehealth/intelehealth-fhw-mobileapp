package com.intelehealth.appointment.data.repository

import com.intelehealth.appointment.data.remote.AppointmentWebClient
import com.intelehealth.appointment.data.remote.response.SlotInfoResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ScheduleAppointmentRepo @Inject constructor(
    private var appointmentWebClient: AppointmentWebClient
) {
    suspend fun getSlots(startDate: String, endDate: String, speciality: String): Flow<Result<SlotInfoResponse?>> = flow {
        try {
            val response = appointmentWebClient.getSlots(startDate,endDate,speciality)
            if(response?.isSuccessful == true){
                emit(Result.success(response.body()))
            }else{
                emit(Result.failure(Exception(response?.message())))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}