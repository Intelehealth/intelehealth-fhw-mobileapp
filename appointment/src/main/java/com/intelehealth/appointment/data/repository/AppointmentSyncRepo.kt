package com.intelehealth.appointment.data.repository

import android.content.Context
import android.content.Intent
import com.intelehealth.appointment.data.local.DbConfig
import com.intelehealth.appointment.data.local.entity.Appointments
import com.intelehealth.appointment.data.remote.AppointmentWebClient
import com.intelehealth.appointment.data.remote.response.AppointmentListingResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentSyncRepo @Inject constructor() {
    @Inject
    lateinit var appointmentWebClient: AppointmentWebClient
    @Inject
    lateinit var dbConfig: DbConfig


    fun fetchAppointmentAndUpdate(startDate:String,endDate:String, locationId:String){
            CoroutineScope(Dispatchers.Default).launch {
                appointmentWebClient.getSlotsAll(startDate,endDate,locationId)?.let {
                    if (it.isSuccessful){
                        it.body()?.let { it1 -> saveData(it1) }
                    }else{

                    }
                }
            }
        }

    private suspend fun saveData(it: AppointmentListingResponse) {
        dbConfig.appointmentsDao().save(Appointments.toAppointments(it.data))
    }
}