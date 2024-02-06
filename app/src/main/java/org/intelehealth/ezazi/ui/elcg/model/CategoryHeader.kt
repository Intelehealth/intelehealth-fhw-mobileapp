package org.intelehealth.ezazi.ui.elcg.model

import org.intelehealth.klivekit.chat.model.ItemHeader

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 18:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class CategoryHeader(val stage: Int) : ItemHeader {
    override fun isHeader(): Boolean = true

    override fun createdDate(): String = ""
}
