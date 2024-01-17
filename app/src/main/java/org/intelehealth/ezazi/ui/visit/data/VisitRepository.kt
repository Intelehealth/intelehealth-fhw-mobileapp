package org.intelehealth.ezazi.ui.visit.data

import android.database.sqlite.SQLiteDatabase
import org.intelehealth.ezazi.builder.PatientQueryBuilder
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.ui.patient.PatientDataBinder

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 19:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VisitRepository(val database: SQLiteDatabase) {
    fun getAllOutcomePendingVisits() {

    }

    fun getAllUpcomingVisits(): List<PatientDTO> {
        PatientQueryBuilder().upcomingPatientQuery(0, 10).apply {
            val cursor = database.rawQuery(this, null)
            return PatientDataBinder().upcomingPatients(cursor)
        }
    }

    fun getCompletedVisits(offset: Int, limit: Int, providerId: String): List<PatientDTO> {
        PatientQueryBuilder().completedVisitPatientQuery(offset, limit, providerId).apply {
            val cursor = database.rawQuery(this, null)
            return PatientDataBinder().retrieveDataFromCursor(cursor)
        }
    }
}