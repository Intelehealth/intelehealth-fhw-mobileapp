package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.Arguments
import com.google.gson.Gson
import org.intelehealth.app.database.dao.QueueDAO
import org.intelehealth.app.models.dto.QueueDTO
import org.intelehealth.app.testutil.TestAppDatabase
import org.intelehealth.app.testutil.TestAppDatabase.insertChiefComplaint
import org.intelehealth.app.testutil.TestAppDatabase.insertPatient
import org.intelehealth.app.testutil.TestAppDatabase.insertVisitFor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate

/**
 * Home "Next In Queue" card paths that read the local DB: the first cards seeded
 * from tbl_queue (before any queue notification), and filling in a notification
 * that only identifies the patient/visit by UUID.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueCardUpdaterDbTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: SQLiteDatabase
    private lateinit var arguments: MockedStatic<Arguments>

    private val dob34 = LocalDate.now().minusYears(34).minusDays(10).toString()
    private val complaint = """{"en":"►<b>Fever</b>: 2 days►<b>Cough</b>: dry"}"""

    @Before
    fun setUp() {
        clearPrefs(context)
        db = TestAppDatabase.install(context)
        arguments = mockReactArguments()
    }

    @After
    fun tearDown() {
        arguments.close()
        TestAppDatabase.uninstall()
    }

    /** A synced patient with an open visit and chief complaint, like after a pull. */
    private fun seedPatient(visit: String, patient: String, first: String = "Asha") {
        insertPatient(db, patient, "OMRS-$patient", first, "Patil", "F", dob34, "/data/$patient.jpg")
        insertVisitFor(db, visit, patient)
        insertChiefComplaint(db, visit, complaint)
    }

    private fun queue(vararg json: String) = TestAppDatabase.onWorkerThread {
        QueueDAO().replaceQueue(json.map { Gson().fromJson(it, QueueDTO::class.java) })
    }

    private fun entry(id: Int, visit: String, position: Int, status: String = "QUEUED", speciality: String, extra: String = "") =
        """{"queueEntryId":$id,"visitUuid":"$visit","status":"$status","position":$position,"speciality":"$speciality"$extra}"""

    // --- seeded from tbl_queue ---

    @Test
    fun getCards_beforeAnyNotification_usesNextInQueueRowsFromTable() {
        seedPatient("v1", "p1")
        queue(entry(11, "v1", 1, speciality = "General Physician", extra = ""","etaMinutes":12"""))

        val card = QueueCardUpdater.getCards(context).single()

        assertEquals("11", card.queueNumber)
        assertEquals("Asha Patil", card.patientName)
        assertEquals("OMRS-p1", card.patientId)
        assertEquals("F", card.gender)
        assertEquals(34, card.age)
        assertEquals(listOf("Fever", "Cough"), card.symptoms)
        assertEquals(1, card.position)
        assertEquals(12, card.waitTimeMinutes)
        assertEquals("file:///data/p1.jpg", card.avatarUrl)
        assertEquals("General Physician", card.speciality)
        assertEquals("v1", card.visitUuid)
    }

    @Test
    fun getCards_fromTable_onlyFirstPositionOfQueuedStatuses() {
        seedPatient("v1", "p1", "One")
        seedPatient("v2", "p2", "Two")
        seedPatient("v3", "p3", "Three")
        seedPatient("v4", "p4", "Four")
        queue(
            entry(1, "v1", 1, "QUEUED", "ENT"),
            entry(2, "v2", 2, "QUEUED", "ENT"),          // behind position 1
            entry(3, "v3", 1, "CALL_CONNECTED", "Skin"), // already on a call
            entry(4, "v4", 1, "re_queued", "Cardiology") // re-queued, case-insensitive
        )

        val names = QueueCardUpdater.getCards(context).map { it.patientName }

        assertEquals("one card per speciality, ordered by speciality", listOf("Four Patil", "One Patil"), names)
    }

    @Test
    fun getCards_fromTable_skipsRowsWhosePatientIsNotSynced() {
        queue(entry(1, "unsynced-visit", 1, speciality = "ENT"))
        assertTrue(QueueCardUpdater.getCards(context).isEmpty())
    }

    @Test
    fun getCards_fromTable_waitTimeComesFromEtaAtWhenPresent() {
        seedPatient("v1", "p1")
        val eta = Instant.now().plusSeconds(20 * 60).toString()
        queue(entry(1, "v1", 1, speciality = "ENT", extra = ""","etaAt":"$eta","etaMinutes":99"""))

        val card = QueueCardUpdater.getCards(context).single()

        assertEquals(20, card.waitTimeMinutes)
        assertEquals(eta, card.etaAt)
    }

    @Test
    fun getCards_fromTable_badDateOfBirthDoesNotDropOtherCards() {
        insertPatient(db, "p1", "OMRS-p1", "Bad", "Dob", "F", "15/01/1990")
        insertVisitFor(db, "v1", "p1")
        seedPatient("v2", "p2", "Two")
        seedPatient("v3", "p3", "Three")
        queue(
            entry(1, "v1", 1, speciality = "A-ENT"),
            entry(2, "v2", 1, speciality = "B-Skin"),
            entry(3, "v3", 1, speciality = "C-Cardiology")
        )

        val cards = QueueCardUpdater.getCards(context)

        assertEquals(listOf("Bad Dob", "Two Patil", "Three Patil"), cards.map { it.patientName })
        assertEquals("unparseable date of birth shows age 0", 0, cards[0].age)
        assertEquals(34, cards[1].age)
    }

    @Test
    fun getCards_emptyWhenQueueTableEmpty() {
        assertTrue(QueueCardUpdater.getCards(context).isEmpty())
    }

    @Test
    fun firstNotification_mergesIntoCardsSeededFromTable() {
        seedPatient("v1", "p1", "Seeded")
        queue(entry(1, "v1", 1, speciality = "ENT"))

        QueueCardUpdater.handleQueueNotification(context, mapOf(
            "patientName" to "New", "patientUuid" to "x", "patientId" to "O", "gender" to "M",
            "age" to "5", "avatarUrl" to "a", "symptoms" to "Rash", "position" to "1",
            "speciality" to "Skin"
        ))

        assertTrue(QueueCardUpdater.hasNotificationData(context))
        assertEquals(listOf("Seeded Patil", "New"), QueueCardUpdater.getPersisted(context).map { it.patientName })
    }

    // --- notification filled in from the local DB ---

    @Test
    fun notificationWithOnlyVisitUuid_resolvesPatientAndSymptomsFromDb() {
        seedPatient("v1", "p1")
        QueueCardUpdater.handleQueueNotification(context, mapOf(
            "visitUuid" to "v1", "position" to "1", "speciality" to "ENT", "etaMinutes" to "6"
        ))

        val card = QueueCardUpdater.getPersisted(context).single()
        assertEquals("Asha Patil", card.patientName)
        assertEquals("OMRS-p1", card.patientId)
        assertEquals("F", card.gender)
        assertEquals(34, card.age)
        assertEquals("file:///data/p1.jpg", card.avatarUrl)
        assertEquals(listOf("Fever", "Cough"), card.symptoms)
        assertEquals(6, card.waitTimeMinutes)
        assertEquals("v1", card.visitUuid)
    }

    @Test
    fun notificationWithPatientUuid_fillsOnlyMissingFields() {
        insertPatient(db, "p1", "OMRS-DB", "Db", "Name", "F", dob34, "/data/db.jpg")

        QueueCardUpdater.handleQueueNotification(context, mapOf(
            "patientUuid" to "p1", "patientName" to "From Payload", "gender" to "M",
            "position" to "1", "speciality" to "ENT", "symptoms" to "Rash"
        ))

        val card = QueueCardUpdater.getPersisted(context).single()
        assertEquals("payload wins", "From Payload", card.patientName)
        assertEquals("payload wins", "M", card.gender)
        assertEquals("filled from DB", "OMRS-DB", card.patientId)
        assertEquals("filled from DB", 34, card.age)
        assertEquals("filled from DB", "file:///data/db.jpg", card.avatarUrl)
        assertEquals("payload symptoms are not replaced", listOf("Rash"), card.symptoms)
    }

    @Test
    fun notificationForUnknownVisit_keepsPayloadAndHasNoSymptoms() {
        QueueCardUpdater.handleQueueNotification(context, mapOf(
            "visitUuid" to "not-synced", "patientName" to "Only Name", "position" to "1", "speciality" to "ENT"
        ))

        val card = QueueCardUpdater.getPersisted(context).single()
        assertEquals("Only Name", card.patientName)
        assertNull(card.patientId)
        assertEquals(0, card.age)
        assertTrue(card.symptoms.isEmpty())
    }

    @Test
    fun notificationLookupWithoutPatientRowLeavesFieldsEmpty() {
        insertVisitFor(db, "v1", "missing-patient")
        QueueCardUpdater.handleQueueNotification(context, mapOf(
            "visitUuid" to "v1", "position" to "1", "speciality" to "ENT"
        ))

        val card = QueueCardUpdater.getPersisted(context).single()
        assertNull(card.patientName)
        assertFalse(card.symptoms.contains(""))
    }
}
