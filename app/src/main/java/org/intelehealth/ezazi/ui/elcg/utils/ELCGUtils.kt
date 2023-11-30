package org.intelehealth.ezazi.ui.elcg.utils

import com.github.ajalt.timberkt.Timber
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.app.IntelehealthApplication
import org.intelehealth.ezazi.database.dao.EncounterDAO
import org.intelehealth.ezazi.database.dao.ObsDAO
import org.intelehealth.ezazi.database.dao.ProviderDAO
import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.ui.elcg.model.ELCGData
import org.intelehealth.ezazi.ui.elcg.model.ELCGGraph
import org.intelehealth.ezazi.ui.elcg.model.StageHeader
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 10:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object ELCGUtils {
    private const val MAX_ENCOUNTER_STAGE_1 = 15
    private const val MAX_ENCOUNTER_STAGE_2 = 5

    fun fetchELCGEncounters(visitId: String): ArrayList<EncounterDTO> {
        val sessionManager = SessionManager(IntelehealthApplication.getAppContext())
        val encounterDAO = EncounterDAO()
        return encounterDAO.getEncountersByVisitUUID(visitId).apply {
            for (i in this.indices) {
                this[i].apply {
                    encounterTypeName = encounterDAO.getEncounterTypeNameByUUID(encounterTypeUuid)
                    encounterType = ObsDAO().getEncounterType(uuid, sessionManager.getCreatorID())
                    providerName = ProviderDAO().getProviderNameById(provideruuid)
                }
            }
        }
    }

    fun generateELCGDataList(
        encounters: ArrayList<EncounterDTO>, elcgGraph: ELCGGraph
    ): LinkedList<ItemHeader> {
        val elcgDataList = LinkedList<ItemHeader>()
        val stage1Encounters = encounters.filter { it.encounterTypeName.contains("Stage1") }
        val stage2Encounters = encounters.filter { it.encounterTypeName.contains("Stage2") }

        if (stage2Encounters.isNotEmpty()) {
            elcgDataList.add(StageHeader(R.string.stage_2))
            elcgDataList.addAll(
                getHourWiseElcgDataList(
                    stage2Encounters, MAX_ENCOUNTER_STAGE_2, elcgGraph, 2
                )
            )
        }

        if (stage1Encounters.isNotEmpty()) {
            elcgDataList.add(StageHeader(R.string.stage_1))
            elcgDataList.addAll(
                getHourWiseElcgDataList(
                    stage1Encounters, MAX_ENCOUNTER_STAGE_1, elcgGraph, 1
                )
            )
        }
        return elcgDataList
    }

    private fun getHourWiseElcgDataList(
        encounters: List<EncounterDTO>, limit: Int, elcgGraph: ELCGGraph, stage: Int
    ): LinkedList<ELCGData> {
//        Stage1_Hour1_1
        val elcgDataList = LinkedList<ELCGData>()
        val obsDao = ObsDAO();
        for (i in limit downTo 1) {
            Timber.d { "current hour $i" }
            encounters.filter { it.encounterTypeName.contains("Stage${stage}_Hour$i") }.apply {
                this.forEach {
                    it.alertCount = obsDao.countEncounterAlert(it.uuid)
                    it.encounterStatus = obsDao.checkObsAddedOrNt(it.uuid, "")
                    it.obsDTOList = obsDao.getELCGObsByEncounterUuid(it.uuid, elcgGraph.attributes)

                }
                if (this.isNotEmpty()) elcgDataList.add(ELCGData(i, this))
            }
        }

        return elcgDataList
    }

    fun isVisitCompleted(visitId: String): Boolean {
        return EncounterDAO().getVisitCompleteEncounterByVisitUUID(visitId).let {
            return@let it.isEmpty().not()
        }
    }
//
//    fun getSupportiveCareObservations(encounterId: String): MutableList<ObsDTO>? {
//        return ObsDAO().getELCGObsByEncounterUuid(encounterId, ELCGGraph.SupportiveCare.attributes)
//    }
//
//    fun getBabyCareObservations(encounterId: String): MutableList<ObsDTO>? {
//        return ObsDAO().getELCGObsByEncounterUuid(encounterId, ELCGGraph.Baby.attributes)
//    }
//
//    fun getWomenCareObservations(encounterId: String): MutableList<ObsDTO>? {
//        return ObsDAO().getELCGObsByEncounterUuid(encounterId, ELCGGraph.Women.attributes)
//    }
//
//    fun getLabourProgressObservations(encounterId: String): MutableList<ObsDTO>? {
//        return ObsDAO().getELCGObsByEncounterUuid(encounterId, ELCGGraph.LabourProgress.attributes)
//    }
//
//    fun getMedicationObservations(encounterId: String): MutableList<ObsDTO>? {
//        return ObsDAO().getELCGObsByEncounterUuid(
//            encounterId, ELCGGraph.MedicationAdministration.attributes
//        )
//    }
//
//    fun getSharedDecisionMakingObservations(encounterId: String): MutableList<ObsDTO>? {
//        return ObsDAO().getELCGObsByEncounterUuid(
//            encounterId, ELCGGraph.SharedDecisionMaking.attributes
//        )
//    }
}