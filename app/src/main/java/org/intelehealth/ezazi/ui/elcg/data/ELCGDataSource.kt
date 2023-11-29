package org.intelehealth.ezazi.ui.elcg.data

import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.ui.elcg.model.ELCGGraph
import org.intelehealth.ezazi.ui.elcg.utils.ELCGUtils
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 23:03.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ELCGDataSource {
    fun loadAllVisitEncounter(visitId: String): ArrayList<EncounterDTO> {
        return ELCGUtils.fetchELCGEncounters(visitId)
    }

    fun buildELCGGraph(
        encounters: ArrayList<EncounterDTO>,
        elcgGraph: ELCGGraph
    ): LinkedList<ItemHeader> {
        return ELCGUtils.generateELCGDataList(encounters, elcgGraph)
    }

    fun isVisitCompleted(visitId: String) = ELCGUtils.isVisitCompleted(visitId)
}