package org.intelehealth.ezazi.ui.elcg.data

import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.ui.elcg.model.ELCGGraph

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 23:02.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ELCGRepository(private val elcgDataSource: ELCGDataSource) {
    fun fetchELCGData(visitId: String) = elcgDataSource.loadAllVisitEncounter(visitId)

    fun buildELCGGraph(
        encounters: ArrayList<EncounterDTO>,
        elcgGraph: ELCGGraph
    ) = elcgDataSource.buildELCGGraph(encounters, elcgGraph)

    fun isVisitCompleted(visitId: String) = elcgDataSource.isVisitCompleted(visitId)
}