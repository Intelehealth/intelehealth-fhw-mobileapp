package com.intelehealth.appointment.data.repository

import android.content.Context
import android.content.Intent
import android.webkit.WebViewClient
import com.intelehealth.appointment.data.local.DbConfig
import com.intelehealth.appointment.data.local.entity.Appointments
import com.intelehealth.appointment.data.remote.AppointmentWebClient
import com.intelehealth.appointment.data.remote.provider.WebClientProvider
import com.intelehealth.appointment.data.remote.response.AppointmentListingResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

class AppointmentSyncRepo(
    private var appointmentWebClient: AppointmentWebClient,
    private var dbConfig: DbConfig
) {


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