package com.intelehealth.appointment.data.local.dao

import com.intelehealth.appointment.data.local.entity.Appointments

class AppointmentDao : CoreDao<Appointments>{
    override suspend fun save(data: List<Appointments>) {
        TODO("Not yet implemented")
    }

    override suspend fun add(data: Appointments) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllRecord(): List<Appointments> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecord(key: String): Appointments {
        TODO("Not yet implemented")
    }
}