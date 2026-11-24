package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.intelehealth.app.reactnative.QueueRowFormatter.STATUS_AWAITING_PRESCRIPTION
import org.intelehealth.app.reactnative.QueueRowFormatter.STATUS_CONNECTING
import org.intelehealth.app.reactnative.QueueRowFormatter.STATUS_DOCTOR_ASSIGNED
import org.intelehealth.app.reactnative.QueueRowFormatter.STATUS_NEXT_IN_QUEUE
import org.intelehealth.app.reactnative.QueueRowFormatter.STATUS_ON_CALL
import org.intelehealth.app.reactnative.QueueRowFormatter.STATUS_WAITING
import org.intelehealth.app.ui.queue.model.QueueRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/**
 * Status / time / avatar shaping applied to each row of the Patient's Queue list.
 * A plain [Application] is used so the real app's onCreate (Firebase, DB) is skipped.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueRowFormatterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun row(
        waitedMinutes: Int = 0,
        etaMinutes: Int = 0,
        etaAt: String? = null,
        connectedAt: String? = null
    ) = QueueRow(
        openmrsId = null, patientName = null, gender = null, dateOfBirth = null,
        status = null, position = 1, visitUuid = null, chiefComplaint = null,
        waitedMinutes = waitedMinutes, etaMinutes = etaMinutes,
        etaAt = etaAt, connectedAt = connectedAt, patientPhoto = null
    )

    private fun inSeconds(seconds: Long) = Instant.now().plusSeconds(seconds).toString()

    // --- mapStatus ---

    @Test
    fun mapStatus_queuedStatusesAtPositionOneAreNextInQueue() {
        listOf("QUEUED", "ESCALATED", "RE_QUEUED").forEach {
            assertEquals(it, STATUS_NEXT_IN_QUEUE, QueueRowFormatter.mapStatus(it, 1))
        }
    }

    @Test
    fun mapStatus_queuedStatusesBehindPositionOneAreWaiting() {
        listOf("QUEUED", "ESCALATED", "RE_QUEUED").forEach {
            assertEquals(it, STATUS_WAITING, QueueRowFormatter.mapStatus(it, 2))
            assertEquals(it, STATUS_WAITING, QueueRowFormatter.mapStatus(it, 10))
        }
    }

    @Test
    fun mapStatus_callAndAssignmentStatuses() {
        assertEquals(STATUS_ON_CALL, QueueRowFormatter.mapStatus("CALL_CONNECTED", 0))
        assertEquals(STATUS_ON_CALL, QueueRowFormatter.mapStatus("CONNECTED", 0))
        assertEquals(STATUS_DOCTOR_ASSIGNED, QueueRowFormatter.mapStatus("ASSIGNED", 0))
        assertEquals(STATUS_CONNECTING, QueueRowFormatter.mapStatus("CALL_CONNECTING", 0))
        assertEquals(STATUS_CONNECTING, QueueRowFormatter.mapStatus("CONNECTING", 0))
        assertEquals(STATUS_AWAITING_PRESCRIPTION, QueueRowFormatter.mapStatus("CALL_COMPLETED", 0))
        assertEquals(STATUS_AWAITING_PRESCRIPTION, QueueRowFormatter.mapStatus("COMPLETED", 0))
    }

    @Test
    fun mapStatus_isCaseAndWhitespaceInsensitive() {
        assertEquals(STATUS_NEXT_IN_QUEUE, QueueRowFormatter.mapStatus("  queued ", 1))
        assertEquals(STATUS_ON_CALL, QueueRowFormatter.mapStatus("call_connected", 0))
    }

    @Test
    fun mapStatus_unmappedStatusesAreEmpty() {
        assertEquals("", QueueRowFormatter.mapStatus("SUBMITTED", 1))
        assertEquals("", QueueRowFormatter.mapStatus("CANCELLED", 1))
        assertEquals("", QueueRowFormatter.mapStatus("SOMETHING_NEW", 1))
        assertEquals("", QueueRowFormatter.mapStatus("", 1))
        assertEquals("", QueueRowFormatter.mapStatus(null, 1))
    }

    // --- labels ---

    @Test
    fun statusLabel_matchesStringResources() {
        assertEquals("On Call", QueueRowFormatter.statusLabel(context, STATUS_ON_CALL))
        assertEquals("Next in Queue", QueueRowFormatter.statusLabel(context, STATUS_NEXT_IN_QUEUE))
        assertEquals("Waiting", QueueRowFormatter.statusLabel(context, STATUS_WAITING))
        assertEquals("Doctor Assigned", QueueRowFormatter.statusLabel(context, STATUS_DOCTOR_ASSIGNED))
        assertEquals("Connecting", QueueRowFormatter.statusLabel(context, STATUS_CONNECTING))
        assertEquals("Awaiting Prescription", QueueRowFormatter.statusLabel(context, STATUS_AWAITING_PRESCRIPTION))
        assertEquals("", QueueRowFormatter.statusLabel(context, ""))
        assertEquals("", QueueRowFormatter.statusLabel(context, null))
    }

    @Test
    fun timeLabel_durationForOnCallAndWaitForQueued() {
        assertEquals("Duration", QueueRowFormatter.timeLabel(context, STATUS_ON_CALL))
        assertEquals("Wait time", QueueRowFormatter.timeLabel(context, STATUS_NEXT_IN_QUEUE))
        assertEquals("Wait time", QueueRowFormatter.timeLabel(context, STATUS_WAITING))
        assertEquals("", QueueRowFormatter.timeLabel(context, STATUS_DOCTOR_ASSIGNED))
        assertEquals("", QueueRowFormatter.timeLabel(context, null))
    }

    @Test
    fun minuteUnit_singularOnlyForOne() {
        assertEquals("Min", QueueRowFormatter.minuteUnit(context, 1))
        assertEquals("Mins", QueueRowFormatter.minuteUnit(context, 0))
        assertEquals("Mins", QueueRowFormatter.minuteUnit(context, 8))
    }

    // --- avatar ---

    @Test
    fun toAvatarUri_prefixesLocalPathsAndKeepsSchemes() {
        assertEquals("", QueueRowFormatter.toAvatarUri(null))
        assertEquals("", QueueRowFormatter.toAvatarUri(""))
        assertEquals("file:///data/p.jpg", QueueRowFormatter.toAvatarUri(" /data/p.jpg "))
        assertEquals("https://x.org/p.jpg", QueueRowFormatter.toAvatarUri("https://x.org/p.jpg"))
        assertEquals("http://x.org/p.jpg", QueueRowFormatter.toAvatarUri("http://x.org/p.jpg"))
        assertEquals("file:///a.jpg", QueueRowFormatter.toAvatarUri("file:///a.jpg"))
        assertEquals("content://media/1", QueueRowFormatter.toAvatarUri("content://media/1"))
    }

    // --- formatTime ---

    @Test
    fun formatTime_emptyForStatusesWithoutTime() {
        val r = row(etaMinutes = 5, waitedMinutes = 5)
        assertEquals("", QueueRowFormatter.formatTime(STATUS_DOCTOR_ASSIGNED, r))
        assertEquals("", QueueRowFormatter.formatTime(STATUS_CONNECTING, r))
        assertEquals("", QueueRowFormatter.formatTime(STATUS_AWAITING_PRESCRIPTION, r))
        assertEquals("", QueueRowFormatter.formatTime("", r))
        assertEquals("", QueueRowFormatter.formatTime(null, r))
    }

    @Test
    fun formatTime_waitingFallsBackToEtaMinutesWithoutEtaAt() {
        assertEquals("07:00", QueueRowFormatter.formatTime(STATUS_WAITING, row(etaMinutes = 7)))
        assertEquals("01:15:00", QueueRowFormatter.formatTime(STATUS_NEXT_IN_QUEUE, row(etaMinutes = 75)))
        assertEquals("00:00", QueueRowFormatter.formatTime(STATUS_WAITING, row(etaMinutes = -3)))
    }

    @Test
    fun formatTime_waitingFallsBackWhenEtaAtUnparseable() {
        assertEquals("04:00", QueueRowFormatter.formatTime(STATUS_WAITING, row(etaMinutes = 4, etaAt = "not-a-date")))
    }

    @Test
    fun formatTime_waitingCountsDownToEtaAt() {
        // ~2h ahead: hours are shown; the seconds part depends on test timing.
        val time = QueueRowFormatter.formatTime(STATUS_WAITING, row(etaAt = inSeconds(2 * 3600 + 30)))
        assertTrue(time, time.matches(Regex("02:00:[0-3]\\d")))
    }

    @Test
    fun formatTime_pastEtaAtClampsToZero() {
        assertEquals("00:00", QueueRowFormatter.formatTime(STATUS_NEXT_IN_QUEUE, row(etaAt = inSeconds(-600))))
    }

    @Test
    fun formatTime_onCallCountsUpFromConnectedAt() {
        val time = QueueRowFormatter.formatTime(STATUS_ON_CALL, row(connectedAt = inSeconds(-(5 * 60 + 2))))
        assertTrue(time, time.matches(Regex("05:0\\d")))
    }

    @Test
    fun formatTime_onCallFallsBackToWaitedMinutes() {
        assertEquals("03:00", QueueRowFormatter.formatTime(STATUS_ON_CALL, row(waitedMinutes = 3)))
        assertEquals("03:00", QueueRowFormatter.formatTime(STATUS_ON_CALL, row(waitedMinutes = 3, connectedAt = "bad")))
    }

    // --- formatTimeInMinutes ---

    @Test
    fun formatTimeInMinutes_usesLocalizedUnit() {
        assertEquals("1 Min", QueueRowFormatter.formatTimeInMinutes(context, STATUS_WAITING, row(etaMinutes = 1)))
        assertEquals("8 Mins", QueueRowFormatter.formatTimeInMinutes(context, STATUS_WAITING, row(etaMinutes = 8)))
        assertEquals("3 Mins", QueueRowFormatter.formatTimeInMinutes(context, STATUS_ON_CALL, row(waitedMinutes = 3)))
    }

    @Test
    fun formatTimeInMinutes_roundsLiveInstants() {
        val eta = QueueRowFormatter.formatTimeInMinutes(context, STATUS_WAITING, row(etaAt = inSeconds(10 * 60)))
        assertEquals("10 Mins", eta)
        val past = QueueRowFormatter.formatTimeInMinutes(context, STATUS_WAITING, row(etaAt = inSeconds(-600)))
        assertEquals("0 Mins", past)
    }

    @Test
    fun formatTimeInMinutes_emptyForStatusesWithoutTime() {
        assertEquals("", QueueRowFormatter.formatTimeInMinutes(context, STATUS_DOCTOR_ASSIGNED, row(etaMinutes = 4)))
    }
}
