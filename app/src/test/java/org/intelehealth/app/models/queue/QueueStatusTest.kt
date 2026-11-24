package org.intelehealth.app.models.queue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Parsing of tbl_queue.status / queue API status strings. */
class QueueStatusTest {

    @Test
    fun parsesEveryKnownStatus() {
        QueueStatus.values().forEach {
            assertEquals(it, QueueStatus.fromValue(it.name))
        }
    }

    @Test
    fun isCaseInsensitive() {
        assertEquals(QueueStatus.QUEUED, QueueStatus.fromValue("queued"))
        assertEquals(QueueStatus.CALL_CONNECTED, QueueStatus.fromValue("Call_Connected"))
        assertEquals(QueueStatus.RE_QUEUED, QueueStatus.fromValue("re_queued"))
    }

    @Test
    fun trimsWhitespace() {
        assertEquals(QueueStatus.ASSIGNED, QueueStatus.fromValue("  ASSIGNED\n"))
    }

    @Test
    fun legacyCallStatusNamesStillParse() {
        assertEquals(QueueStatus.CONNECTING, QueueStatus.fromValue("CONNECTING"))
        assertEquals(QueueStatus.CONNECTED, QueueStatus.fromValue("CONNECTED"))
        assertEquals(QueueStatus.COMPLETED, QueueStatus.fromValue("COMPLETED"))
    }

    @Test
    fun nullBlankOrUnknownReturnsNull() {
        assertNull(QueueStatus.fromValue(null))
        assertNull(QueueStatus.fromValue(""))
        assertNull(QueueStatus.fromValue("   "))
        assertNull(QueueStatus.fromValue("REQUEUED"))
        assertNull(QueueStatus.fromValue("call-connected"))
        assertNull(QueueStatus.fromValue("on call"))
    }
}
