package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.intelehealth.app.ui.queue.model.QueueRow
import org.intelehealth.klivekit.data.PreferenceHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** React Native props for the Patient's Queue list screen and each of its rows. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueListPropsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        clearPrefs(context)
    }

    private fun dobForAge(years: Long) = LocalDate.now().minusYears(years).minusDays(10).toString()

    private fun row(
        status: String? = "QUEUED",
        position: Int = 1,
        openmrsId: String? = "OMRS-1",
        name: String? = "Asha Patil",
        gender: String? = "F",
        dob: String? = dobForAge(34),
        complaint: String? = null,
        etaMinutes: Int = 0,
        waitedMinutes: Int = 0,
        etaAt: String? = null,
        connectedAt: String? = null,
        photo: String? = null
    ) = QueueRow(
        openmrsId = openmrsId, patientName = name, gender = gender, dateOfBirth = dob,
        status = status, position = position, visitUuid = "visit-1", chiefComplaint = complaint,
        waitedMinutes = waitedMinutes, etaMinutes = etaMinutes, etaAt = etaAt,
        connectedAt = connectedAt, patientPhoto = photo
    )

    private fun rowProps(row: QueueRow): Bundle = QueueListProps.rowBundle(context, row)

    // --- row props ---

    @Test
    fun row_patientFields() {
        val b = rowProps(row())

        assertEquals("OMRS-1", b.getString("queueNumber"))
        assertEquals("OMRS-1", b.getString("patientId"))
        assertEquals("Asha Patil", b.getString("patientName"))
        assertEquals("F", b.getString("gender"))
        assertEquals(34, b.getInt("age"))
    }

    @Test
    fun row_missingPatientFieldsBecomeEmptyStrings() {
        val b = rowProps(row(openmrsId = null, name = null, gender = null, dob = null))

        assertEquals("", b.getString("queueNumber"))
        assertEquals("", b.getString("patientName"))
        assertEquals("", b.getString("gender"))
        assertEquals(0, b.getInt("age"))
    }

    @Test
    fun row_invalidDateOfBirthGivesAgeZeroInsteadOfCrashing() {
        assertEquals(0, rowProps(row(dob = "not-a-date")).getInt("age"))
    }

    @Test
    fun row_nextInQueueStatusAndLabels() {
        val b = rowProps(row(status = "QUEUED", position = 1, etaMinutes = 7))

        assertEquals(1, b.getInt("position"))
        assertEquals("nextInQueue", b.getString("status"))
        assertEquals("Next in Queue", b.getString("statusLabel"))
        assertEquals("Wait time", b.getString("timeLabel"))
        assertEquals("07:00", b.getString("time"))
    }

    @Test
    fun row_waitingBehindFirstPosition() {
        val b = rowProps(row(status = "ESCALATED", position = 4, etaMinutes = 75))

        assertEquals("waiting", b.getString("status"))
        assertEquals("Waiting", b.getString("statusLabel"))
        assertEquals("01:15:00", b.getString("time"))
    }

    @Test
    fun row_onCallShowsDuration() {
        val b = rowProps(row(status = "CALL_CONNECTED", position = 0, waitedMinutes = 3))

        assertEquals("onCall", b.getString("status"))
        assertEquals("On Call", b.getString("statusLabel"))
        assertEquals("Duration", b.getString("timeLabel"))
        assertEquals("03:00", b.getString("time"))
    }

    @Test
    fun row_statusesWithoutTimeHaveEmptyTimeAndTimeLabel() {
        val b = rowProps(row(status = "ASSIGNED", position = 0))

        assertEquals("doctorAssigned", b.getString("status"))
        assertEquals("Doctor Assigned", b.getString("statusLabel"))
        assertEquals("", b.getString("timeLabel"))
        assertEquals("", b.getString("time"))
    }

    @Test
    fun row_unknownStatusIsBlank() {
        val b = rowProps(row(status = "CANCELLED"))
        assertEquals("", b.getString("status"))
        assertEquals("", b.getString("statusLabel"))
    }

    @Test
    fun row_symptomsParsedFromChiefComplaint() {
        val b = rowProps(row(complaint = "►<b>Fever</b>: 2 days►<b>Cough</b>: dry►<b>Associated symptoms</b>: x"))
        assertEquals(arrayListOf("Fever", "Cough"), b.getStringArrayList("symptoms"))
    }

    @Test
    fun row_noComplaintGivesEmptySymptoms() {
        assertEquals(arrayListOf<String>(), rowProps(row(complaint = null)).getStringArrayList("symptoms"))
    }

    @Test
    fun row_localizedFixedLabels() {
        val b = rowProps(row())
        assertEquals("Overdue", b.getString("overdueLabel"))
        assertEquals("+%1\$d More", b.getString("moreLabel"))
    }

    @Test
    fun row_rawInstantsForLiveTick() {
        val b = rowProps(row(etaAt = "2026-11-23T10:00:00Z", connectedAt = "2026-11-23T09:50:00Z"))
        assertEquals("2026-11-23T10:00:00Z", b.getString("etaAt"))
        assertEquals("2026-11-23T09:50:00Z", b.getString("connectedAt"))

        val empty = rowProps(row())
        assertEquals("", empty.getString("etaAt"))
        assertEquals("", empty.getString("connectedAt"))
    }

    @Test
    fun row_avatarUri() {
        assertEquals("file:///data/p.jpg", rowProps(row(photo = "/data/p.jpg")).getString("avatarUrl"))
        assertEquals("", rowProps(row(photo = null)).getString("avatarUrl"))
    }

    // --- screen props ---

    @Test
    fun screen_wrapsRowsUnderQueue() {
        val rows = arrayListOf(rowProps(row()), rowProps(row(position = 2)))

        @Suppress("DEPRECATION")
        val queue = QueueListProps.screenProps(context, rows).getParcelableArrayList<Bundle>("queue")!!

        assertEquals(2, queue.size)
        assertEquals(2, queue[1].getInt("position"))
    }

    @Test
    fun screen_emptyQueueStillHasLabels() {
        val props = QueueListProps.screenProps(context, arrayListOf())

        @Suppress("DEPRECATION")
        assertTrue(props.getParcelableArrayList<Bundle>("queue")!!.isEmpty())
        val labels = props.getBundle("labels")!!
        assertEquals("All", labels.getString("tabAll"))
        assertEquals("Next", labels.getString("tabNext"))
        assertEquals("Waiting", labels.getString("tabWaiting"))
        assertEquals("On Call", labels.getString("tabOnCall"))
        assertEquals("Patients in Queue :", labels.getString("countLabel"))
        assertEquals("Search patient", labels.getString("searchPlaceholder"))
    }

    @Test
    fun screen_noBannerWhenNoDoctorAway() {
        assertFalse(QueueListProps.screenProps(context, arrayListOf()).containsKey("banner"))
    }

    @Test
    fun screen_seedsPersistedBanner() {
        PreferenceHelper(context).save(
            PreferenceHelper.STATUS_BANNER_DATA,
            """{"variant":"alert","title":"A doctor is away","subtitle":"Queue Paused","actionLabel":"View Queue"}"""
        )

        val banner = QueueListProps.screenProps(context, arrayListOf()).getBundle("banner")!!

        assertEquals("alert", banner.getString("variant"))
        assertEquals("A doctor is away", banner.getString("title"))
        assertEquals("Queue Paused", banner.getString("subtitle"))
        assertEquals("View Queue", banner.getString("actionLabel"))
    }

    @Test
    fun screen_corruptBannerIsSkipped() {
        PreferenceHelper(context).save(PreferenceHelper.STATUS_BANNER_DATA, "{broken")
        assertNull(QueueListProps.screenProps(context, arrayListOf()).getBundle("banner"))
    }
}
