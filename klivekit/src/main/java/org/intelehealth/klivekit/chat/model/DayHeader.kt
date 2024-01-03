package org.intelehealth.klivekit.chat.model

import org.intelehealth.klivekit.utils.DateTimeUtils
import org.intelehealth.klivekit.utils.extensions.toLocalDateFormat

/**
 * Created by Vaghela Mithun R. on 03-08-2023 - 19:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class DayHeader(val date: String) : ItemHeader {
    override fun isHeader(): Boolean = true
    override fun createdDate(): String {
        return date
    }

    fun displayFormat() = date.toLocalDateFormat(DateTimeUtils.MESSAGE_DAY_FORMAT)

    companion object {
        @JvmStatic
        fun buildHeader(header: String) = DayHeader(header);
    }
}