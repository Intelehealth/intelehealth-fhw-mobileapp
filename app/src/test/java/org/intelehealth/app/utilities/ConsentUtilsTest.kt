package org.intelehealth.app.utilities

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ConsentUtilsTest {

    private val date = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse("23-11-2026 10:05:09")!!

    @Test
    fun acceptedValueIsActive() {
        assertEquals(
            "23-11-2026 10:05:09 | hw-1 | mr | active | NAS-mobile",
            ConsentUtils.buildConsentValue("hw-1", "mr", date = date)
        )
    }

    @Test
    fun declinedValueKeepsTheSameFormat() {
        assertEquals(
            "23-11-2026 10:05:09 | hw-1 | en | declined | NAS-mobile",
            ConsentUtils.buildConsentValue("hw-1", "en", ConsentUtils.STATUS_DECLINED, date = date)
        )
    }

    @Test
    fun digitsStayLatinInMarathi() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale("mr", "IN"))
            assertEquals(
                "23-11-2026 10:05:09 | hw-1 | mr | declined | NAS-mobile",
                ConsentUtils.buildConsentValue("hw-1", "mr", ConsentUtils.STATUS_DECLINED, date = date)
            )
        } finally {
            Locale.setDefault(previous)
        }
    }
}
