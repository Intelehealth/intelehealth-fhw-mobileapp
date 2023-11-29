package org.intelehealth.ezazi.ui.elcg.model

import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.klivekit.chat.model.ItemHeader

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 00:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class ELCGData(
    val hour: Int,
    val encounters: List<EncounterDTO>
) : ItemHeader {
    override fun isHeader(): Boolean = false

    override fun createdDate(): String = ""
}
