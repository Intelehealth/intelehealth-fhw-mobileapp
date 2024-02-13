package org.intelehealth.ezazi.ui.elcg.model

import org.intelehealth.klivekit.chat.model.ItemHeader

class StageHeader (val stage: Int) : ItemHeader {
    override fun isHeader(): Boolean = true

    override fun createdDate(): String = ""
}
