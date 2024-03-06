package org.intelehealth.ezazi.ui.visit.data

import android.database.sqlite.SQLiteDatabase
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.builder.PatientQueryBuilder
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.ui.elcg.model.CategoryHeader
import org.intelehealth.ezazi.ui.patient.PatientDataBinder
import org.intelehealth.klivekit.chat.model.ItemHeader

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 19:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VisitRepository(val database: SQLiteDatabase) {
    fun getOutcomePendingVisits(offset: Int, limit: Int, providerId: String): List<ItemHeader> {
        PatientQueryBuilder().outcomePendingPatientQuery(offset, limit, providerId).apply {
            val cursor = database.rawQuery(this, null)
            PatientDataBinder().outcomePendingVisits(cursor).apply {
                return filterOutcomePendingVisitByStage(this)
            }
        }
    }

    fun getOutcomePendingVisitsCount(providerId: String): Int {
        PatientQueryBuilder().outcomePendingPatientCountQuery(providerId).apply {
            val cursor = database.rawQuery(this, null)
            return PatientDataBinder().getOutcomePendingCount(cursor)
        }
    }

    fun getUpcomingVisits(): List<ItemHeader> {
        PatientQueryBuilder().upcomingPatientQuery(0, 10).apply {
            val cursor = database.rawQuery(this, null)
            PatientDataBinder().upcomingPatients(cursor).apply {
                val upcomingVisits = arrayListOf<ItemHeader>()
                upcomingVisits.addAll(this)
                return upcomingVisits;
            }
        }
    }

    fun getCompletedVisits(offset: Int, limit: Int, providerId: String): List<PatientDTO> {
        PatientQueryBuilder().completedVisitPatientQuery(offset, limit, providerId).apply {
            val cursor = database.rawQuery(this, null)
            return PatientDataBinder().completedVisits(cursor)
        }
    }

    private fun filterOutcomePendingVisitByStage(visits: List<PatientDTO>): List<ItemHeader> {
        val outcomePendingVisits = arrayListOf<ItemHeader>()
        val stage1Visits = visits.filter { it.stage.lowercase().contains("Stage-1".lowercase()) }
        val stage2Visits = visits.filter { it.stage.lowercase().contains("Stage-2".lowercase()) }

        if (stage2Visits.isNotEmpty()) {
            outcomePendingVisits.add(CategoryHeader(R.string.stage_2))
            outcomePendingVisits.addAll(stage2Visits)
        }

        if (stage1Visits.isNotEmpty()) {
            outcomePendingVisits.add(CategoryHeader(R.string.stage_1))
            outcomePendingVisits.addAll(stage1Visits)
        }

        return outcomePendingVisits
    }

    fun getOutcomePendingStatus(visitId: String): Boolean {
        PatientQueryBuilder().outcomePendingStatusQuery(visitId).apply {
            val cursor = database.rawQuery(this, null)
            return PatientDataBinder().getOutcomePendingStatus(cursor)
        }
    }
}