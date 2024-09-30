package com.intelehealth.appointment.data.repository

import com.intelehealth.appointment.callback.DataStatus
import com.intelehealth.appointment.data.local.DbConfig
import com.intelehealth.appointment.data.local.entity.Appointments
import com.intelehealth.appointment.data.remote.AppointmentWebClient
import com.intelehealth.appointment.data.remote.response.AppointmentInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AppointmentSyncRepo(
    private var appointmentWebClient: AppointmentWebClient,
    private var dbConfig: DbConfig
) {


    fun fetchAppointmentAndUpdate(
        startDate: String,
        endDate: String,
        locationId: String,
        dataStatus: DataStatus
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            appointmentWebClient.getSlotsAll(startDate, endDate, locationId)?.let {
                if (it.isSuccessful) {
                    it.body()?.let { it1 ->
                        saveData(it1.data)
                        saveData(it1.cancelledAppointments)
                        dataStatus.success("Success")
                    }
                } else {
                    dataStatus.failed(it.code().toString())
                }
            }
        }
    }

    private suspend fun saveData(it: MutableList<AppointmentInfo>) {
        for (appointment in it) {
            var rescheduledAppointmentsModel: com.intelehealth.appointment.data.remote.response.RescheduledAppointmentsModel? =
                null
            if (appointment.rescheduledAppointments != null && appointment.rescheduledAppointments.size > 0) {
                val rescheduledSize: Int = appointment.rescheduledAppointments.size
                rescheduledAppointmentsModel =
                    appointment.rescheduledAppointments[rescheduledSize - 1]
            }
            val checkAppointmentInfo =
                dbConfig.appointmentsDao().getRecordByVisitId(appointment.visitUuid)
            appointment.apply {
                uuid = checkAppointmentInfo?.uuid ?: UUID.randomUUID().toString()
                slotDay = rescheduledAppointmentsModel?.slotDay
                slotDate = rescheduledAppointmentsModel?.slotDate
                slotTime = rescheduledAppointmentsModel?.slotTime
            }

            dbConfig.appointmentsDao().add(Appointments.toAppointment(appointment))
        }
    }
}