package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.intelehealth.app.ui.queue.model.QueueRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/** Queue status banner shown at the top of the Visit Summary screen. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class VisitSummaryQueueBannerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun row(
        status: String?,
        position: Int,
        etaMinutes: Int = 0,
        waitedMinutes: Int = 0,
        etaAt: String? = null,
        connectedAt: String? = null
    ) = QueueRow(
        openmrsId = "OMRS-1", patientName = "Asha Patil", gender = "F",
        dateOfBirth = "1990-01-15", status = status, position = position,
        visitUuid = "visit-1", chiefComplaint = null,
        waitedMinutes = waitedMinutes, etaMinutes = etaMinutes,
        etaAt = etaAt, connectedAt = connectedAt, patientPhoto = null
    )

    private fun props(row: QueueRow): Bundle = VisitSummaryQueueBanner.buildProps(context, row)

    // --- when the banner shows ---

    @Test
    fun shouldShow_onlyForSubmittedOpenVisitWithQms() {
        assertTrue(VisitSummaryQueueBanner.shouldShow(true, true, false))
    }

    @Test
    fun shouldShow_hiddenWhenQmsNotConfigured() {
        assertFalse(VisitSummaryQueueBanner.shouldShow(false, true, false))
    }

    @Test
    fun shouldShow_hiddenBeforeVisitIsSubmitted() {
        assertFalse(VisitSummaryQueueBanner.shouldShow(true, false, false))
    }

    @Test
    fun shouldShow_hiddenOnceVisitEnded() {
        assertFalse(VisitSummaryQueueBanner.shouldShow(true, true, true))
    }

    @Test
    fun shouldShow_hiddenForEveryOtherCombination() {
        for (qms in listOf(true, false)) for (submitted in listOf(true, false)) for (ended in listOf(true, false)) {
            val expected = qms && submitted && !ended
            assertEquals("qms=$qms submitted=$submitted ended=$ended",
                expected, VisitSummaryQueueBanner.shouldShow(qms, submitted, ended))
        }
    }

    // --- banner props ---

    @Test
    fun nextInQueue_showsPositionStatusAndWaitTime() {
        val p = props(row("QUEUED", position = 1, etaMinutes = 8))

        assertEquals("warning", p.getString("variant"))
        assertEquals("Position #1", p.getString("title"))
        assertEquals("Next in Queue", p.getString("subtitle"))
        assertEquals(QueueRowFormatter.STATUS_NEXT_IN_QUEUE, p.getString("status"))
        assertEquals("8 Mins", p.getString("time"))
    }

    @Test
    fun waiting_behindFirstPosition() {
        val p = props(row("RE_QUEUED", position = 3, etaMinutes = 1))

        assertEquals("Position #3", p.getString("title"))
        assertEquals("Waiting", p.getString("subtitle"))
        assertEquals(QueueRowFormatter.STATUS_WAITING, p.getString("status"))
        assertEquals("1 Min", p.getString("time"))
    }

    @Test
    fun onCall_withoutPositionShowsInQueueAndCallDuration() {
        val p = props(row("CALL_CONNECTED", position = 0, waitedMinutes = 4))

        assertEquals("In Queue", p.getString("title"))
        assertEquals("On Call", p.getString("subtitle"))
        assertEquals(QueueRowFormatter.STATUS_ON_CALL, p.getString("status"))
        assertEquals("4 Mins", p.getString("time"))
    }

    @Test
    fun statusesWithoutTime_haveEmptyTime() {
        val assigned = props(row("ASSIGNED", position = 0))
        assertEquals("Doctor Assigned", assigned.getString("subtitle"))
        assertEquals("", assigned.getString("time"))

        val completed = props(row("CALL_COMPLETED", position = 0))
        assertEquals("Awaiting Prescription", completed.getString("subtitle"))
        assertEquals("", completed.getString("time"))
    }

    @Test
    fun unknownStatus_hasEmptyStatusAndSubtitle() {
        val p = props(row("SUBMITTED", position = 2))

        assertEquals("Position #2", p.getString("title"))
        assertEquals("", p.getString("status"))
        assertEquals("", p.getString("subtitle"))
        assertEquals("", p.getString("time"))
    }

    @Test
    fun liveInstantsArePassedThroughForTheRnTick() {
        val eta = Instant.now().plusSeconds(10 * 60).toString()
        val connected = "2026-11-23T09:50:00Z"
        val p = props(row("QUEUED", position = 2, etaAt = eta, connectedAt = connected))

        assertEquals(eta, p.getString("etaAt"))
        assertEquals(connected, p.getString("connectedAt"))
        assertEquals("time is computed from etaAt", "10 Mins", p.getString("time"))
    }

    @Test
    fun missingInstantsBecomeEmptyStrings() {
        val p = props(row("QUEUED", position = 1))

        assertEquals("", p.getString("etaAt"))
        assertEquals("", p.getString("connectedAt"))
    }

    @Test
    fun minuteLabelsAreLocalizedSingularAndPlural() {
        val p = props(row("QUEUED", position = 1))

        assertEquals("Min", p.getString("minuteLabel"))
        assertEquals("Mins", p.getString("minutesLabel"))
    }

    @Test
    fun propsCarryExactlyTheBannerKeys() {
        assertEquals(
            setOf(
                "variant", "title", "subtitle", "status", "time",
                "minuteLabel", "minutesLabel", "etaAt", "connectedAt"
            ),
            props(row("QUEUED", position = 1)).keySet()
        )
    }
}
