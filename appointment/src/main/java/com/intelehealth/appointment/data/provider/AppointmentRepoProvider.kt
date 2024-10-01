package com.intelehealth.appointment.data.provider

import com.intelehealth.appointment.data.local.DbConfig
import com.intelehealth.appointment.data.remote.AppointmentWebClient
import com.intelehealth.appointment.data.repository.AppointmentSyncRepo

object AppointmentRepoProvider {
    private var appointmentSyncRepo: AppointmentSyncRepo? = null

    fun getRepo(
        appointmentWebClient: AppointmentWebClient,
        dbConfig: DbConfig
    ): AppointmentSyncRepo = appointmentSyncRepo ?: synchronized(this) {
        appointmentSyncRepo ?: AppointmentSyncRepo(
            appointmentWebClient,dbConfig
        ).also {
            appointmentSyncRepo = it
        }
    }

}