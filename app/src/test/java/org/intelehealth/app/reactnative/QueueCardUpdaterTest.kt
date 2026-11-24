package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.Arguments
import org.intelehealth.klivekit.data.PreferenceHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

/**
 * Home screen "Next In Queue" card: FCM payload parsing, merge into the persisted
 * slideshow list, persistence, and the props handed to the RN card.
 *
 * Notifications are handled with the card list already seeded ("[]" or real
 * cards) so the tbl_queue fallback is not hit, and payloads carry every patient
 * field (incl. patientUuid / symptoms) so no local DB lookup is needed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueCardUpdaterTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var arguments: MockedStatic<Arguments>

    @Before
    fun setUp() {
        clearPrefs(context)
        arguments = mockReactArguments()
    }

    @After
    fun tearDown() {
        arguments.close()
    }

    private fun seedCards(json: String) {
        PreferenceHelper(context).save(PreferenceHelper.QUEUE_CARD_DATA, json)
    }

    private fun payload(
        speciality: String? = "General Physician",
        name: String = "Asha Patil",
        extra: Map<String, String> = emptyMap()
    ): Map<String, String> = buildMap {
        put("queueNumber", "Q-1")
        put("patientName", name)
        put("patientId", "OMRS-1")
        put("patientUuid", "patient-1")
        put("gender", "F")
        put("age", "34")
        put("avatarUrl", "https://x.org/a.jpg")
        put("symptoms", "Fever, Cough")
        put("position", "1")
        put("waitTimeMinutes", "12")
        speciality?.let { put("speciality", it) }
        putAll(extra)
    }

    private fun handle(data: Map<String, String>) =
        QueueCardUpdater.handleQueueNotification(context, data)

    // --- handleQueueNotification ---

    @Test
    fun handle_ignoresNullContextOrData() {
        QueueCardUpdater.handleQueueNotification(null, payload())
        QueueCardUpdater.handleQueueNotification(context, null)
        assertFalse(QueueCardUpdater.hasNotificationData(context))
    }

    @Test
    fun handle_singlePatientPayloadIsPersistedWithAllFields() {
        seedCards("[]")
        handle(payload(extra = mapOf("etaAt" to "2026-11-23T10:00:00Z", "visitUuid" to "visit-1")))

        val card = QueueCardUpdater.getPersisted(context).single()
        assertEquals("Q-1", card.queueNumber)
        assertEquals("Asha Patil", card.patientName)
        assertEquals("OMRS-1", card.patientId)
        assertEquals("F", card.gender)
        assertEquals(34, card.age)
        assertEquals("https://x.org/a.jpg", card.avatarUrl)
        assertEquals(listOf("Fever", "Cough"), card.symptoms)
        assertEquals(1, card.position)
        assertEquals(12, card.waitTimeMinutes)
        assertEquals("General Physician", card.speciality)
        assertEquals("visit-1", card.visitUuid)
        assertEquals("2026-11-23T10:00:00Z", card.etaAt)
        assertTrue(QueueCardUpdater.hasNotificationData(context))
    }

    @Test
    fun handle_buildsNameFromFirstAndLastAndFallsBackToAlternateKeys() {
        seedCards("[]")
        val data = payload().toMutableMap().apply {
            remove("patientName"); remove("patientId"); remove("queueNumber")
            put("patientFirstName", "Ravi")
            put("patientLastName", "Kumar")
            put("patientOpenMrsId", "OMRS-9")
            put("queueEntryId", "entry-9")
        }
        handle(data)

        val card = QueueCardUpdater.getPersisted(context).single()
        assertEquals("Ravi Kumar", card.patientName)
        assertEquals("OMRS-9", card.patientId)
        assertEquals("entry-9", card.queueNumber)
    }

    @Test
    fun handle_waitTimeFallsBackToEtaMinutesThenEtaAt() {
        seedCards("[]")
        val noWait = payload().toMutableMap().apply { remove("waitTimeMinutes") }

        handle(noWait + ("etaMinutes" to "9"))
        assertEquals(9, QueueCardUpdater.getPersisted(context).single().waitTimeMinutes)

        val eta = Instant.now().plusSeconds(30 * 60).toString()
        handle(noWait + ("etaAt" to eta))
        assertEquals(30, QueueCardUpdater.getPersisted(context).single().waitTimeMinutes)

        handle(noWait + ("etaTime" to Instant.now().minusSeconds(600).toString()))
        assertEquals("past ETA clamps to 0", 0, QueueCardUpdater.getPersisted(context).single().waitTimeMinutes)
    }

    @Test
    fun handle_invalidNumbersBecomeZero() {
        seedCards("[]")
        handle(payload(extra = mapOf("position" to "abc", "waitTimeMinutes" to "")))

        val card = QueueCardUpdater.getPersisted(context).single()
        assertEquals(0, card.position)
        assertEquals(0, card.waitTimeMinutes)
    }

    @Test
    fun handle_sameSpecialityReplacesExistingCard() {
        seedCards("[]")
        handle(payload(speciality = "Pediatrician", name = "First"))
        handle(payload(speciality = "pediatrician", name = "Second"))

        val cards = QueueCardUpdater.getPersisted(context)
        assertEquals(1, cards.size)
        assertEquals("Second", cards[0].patientName)
    }

    @Test
    fun handle_differentSpecialityAddsNewSlide() {
        seedCards("[]")
        handle(payload(speciality = "Pediatrician", name = "Child"))
        handle(payload(speciality = "Cardiologist", name = "Heart"))

        assertEquals(listOf("Child", "Heart"), QueueCardUpdater.getPersisted(context).map { it.patientName })
    }

    @Test
    fun handle_withoutSpecialityMatchesOnVisitUuid() {
        seedCards("[]")
        handle(payload(speciality = null, name = "A", extra = mapOf("visitUuid" to "VISIT-1")))
        handle(payload(speciality = null, name = "B", extra = mapOf("visitUuid" to "visit-1")))
        handle(payload(speciality = null, name = "C", extra = mapOf("visitUuid" to "visit-2")))

        assertEquals(listOf("B", "C"), QueueCardUpdater.getPersisted(context).map { it.patientName })
    }

    @Test
    fun handle_patientsArrayProducesOneCardPerEntry() {
        seedCards("[]")
        val patients = """
            [
              {"patientName":"One","patientUuid":"p1","patientId":"O1","gender":"M","age":40,
               "avatarUrl":"a","speciality":"ENT","symptoms":["Fever"," Cold "],"position":1,"waitTimeMinutes":5},
              {"patientName":"Two","patientUuid":"p2","patientId":"O2","gender":"F","age":22,
               "avatarUrl":"b","speciality":"Skin","symptoms":"Rash","position":2,"waitTimeMinutes":8,"note":null},
              "not-an-object"
            ]
        """.trimIndent()
        handle(mapOf(QueueCardUpdater.KEY_PATIENTS to patients))

        val cards = QueueCardUpdater.getPersisted(context)
        assertEquals(listOf("One", "Two"), cards.map { it.patientName })
        assertEquals(listOf("Fever", "Cold"), cards[0].symptoms)
        assertEquals(listOf("Rash"), cards[1].symptoms)
        assertEquals(40, cards[0].age)
        assertEquals(listOf(5, 8), cards.map { it.waitTimeMinutes })
    }

    @Test
    fun handle_malformedPatientsArrayKeepsExistingCards() {
        seedCards("[]")
        handle(payload(name = "Existing"))
        handle(mapOf(QueueCardUpdater.KEY_PATIENTS to "{broken"))

        assertEquals(listOf("Existing"), QueueCardUpdater.getPersisted(context).map { it.patientName })
    }

    @Test
    fun handle_postsCardsReceived() {
        seedCards("[]")
        val before = System.currentTimeMillis()
        handle(payload())

        val received = QueueCardUpdater.getCardsReceived().value
        assertNotNull(received)
        assertTrue(received!! >= before)
    }

    // --- persisted list ---

    @Test
    fun getPersisted_emptyWhenNothingStored() {
        assertTrue(QueueCardUpdater.getPersisted(context).isEmpty())
        assertFalse(QueueCardUpdater.hasNotificationData(context))
    }

    @Test
    fun getPersisted_readsLegacySingleObject() {
        seedCards("""{"patientName":"Legacy","age":50,"position":1}""")

        val cards = QueueCardUpdater.getPersisted(context)
        assertEquals(1, cards.size)
        assertEquals("Legacy", cards[0].patientName)
        assertEquals(50, cards[0].age)
    }

    @Test
    fun getPersisted_emptyOnCorruptJson() {
        seedCards("[{not json")
        assertTrue(QueueCardUpdater.getPersisted(context).isEmpty())
    }

    @Test
    fun getCards_usesPersistedListOnceNotificationReceived() {
        seedCards("""[{"patientName":"From FCM"}]""")
        assertEquals(listOf("From FCM"), QueueCardUpdater.getCards(context).map { it.patientName })
    }

    // --- bundle for the RN card ---

    @Test
    fun toBundle_mirrorsCardFields() {
        val card = PatientData(
            "Q-7", "Asha", "F", 34, "OMRS-1", arrayListOf("Fever"), 2, 15,
            "file:///p.jpg", "ENT", "visit-1", "2026-11-23T10:00:00Z"
        )

        val bundle = QueueCardUpdater.toBundle(listOf(card))
        @Suppress("DEPRECATION")
        val items = bundle.getParcelableArrayList<Bundle>("patients")!!

        assertEquals(1, items.size)
        val item = items[0]
        assertEquals("Q-7", item.getString("queueNumber"))
        assertEquals("Asha", item.getString("patientName"))
        assertEquals("F", item.getString("gender"))
        assertEquals(34, item.getInt("age"))
        assertEquals("OMRS-1", item.getString("patientId"))
        assertEquals(arrayListOf("Fever"), item.getStringArrayList("symptoms"))
        assertEquals(2, item.getInt("position"))
        assertEquals(15, item.getInt("waitTimeMinutes"))
        assertEquals("file:///p.jpg", item.getString("avatarUrl"))
        assertEquals("ENT", item.getString("speciality"))
        assertEquals("2026-11-23T10:00:00Z", item.getString("etaAt"))
        // visitUuid is internal (used for merging) and not sent to the card.
        assertFalse(item.containsKey("visitUuid"))
    }

    @Test
    fun toBundle_emptyList() {
        @Suppress("DEPRECATION")
        val items = QueueCardUpdater.toBundle(emptyList()).getParcelableArrayList<Bundle>("patients")
        assertTrue(items!!.isEmpty())
    }

    // --- chief complaint -> symptom tags ---

    @Test
    fun extractComplaintNames_emptyForNullOrBlank() {
        assertTrue(QueueCardUpdater.extractComplaintNames(null).isEmpty())
        assertTrue(QueueCardUpdater.extractComplaintNames("").isEmpty())
    }

    @Test
    fun extractComplaintNames_takesHeaderNamesAndStripsMarkup() {
        val raw = "►<b>Fever</b>: <br/>• Duration - 2 days►<b>Cough</b>: <br/>• Dry"
        assertEquals(listOf("Fever", "Cough"), QueueCardUpdater.extractComplaintNames(raw))
    }

    @Test
    fun extractComplaintNames_dedupesAndNormalizesQuestionMarkBullets() {
        val raw = "?<b>Fever</b>: a?<b>Fever</b>: b►<b>Headache</b>: c"
        assertEquals(listOf("Fever", "Headache"), QueueCardUpdater.extractComplaintNames(raw))
    }

    @Test
    fun extractComplaintNames_skipsAssociatedSymptomsBlock() {
        val raw = "►<b>Fever</b>: x" +
            "►<b>Associated symptoms</b>: <br/>Patient reports - Chills" +
            "►Do you have the following symptom(s)? Patient denies - Nausea"
        assertEquals(listOf("Fever"), QueueCardUpdater.extractComplaintNames(raw))
    }

    @Test
    fun extractComplaintNames_skipsTranslatedAssociatedSymptoms() {
        val raw = "►<b>बुखार</b>: x►पेशेंट ने मना कर दिया - उल्टी"
        assertEquals(listOf("बुखार"), QueueCardUpdater.extractComplaintNames(raw))
    }

    @Test
    fun extractComplaintNames_plainNameWithoutColon() {
        assertEquals(listOf("Back pain"), QueueCardUpdater.extractComplaintNames("Back pain"))
    }

    @Test
    fun hasNotificationData_trueOnlyWhenSomethingStored() {
        assertFalse(QueueCardUpdater.hasNotificationData(context))
        seedCards("[]")
        assertTrue(QueueCardUpdater.hasNotificationData(context))
        assertNull(PreferenceHelper(context).getString(PreferenceHelper.STATUS_BANNER_DATA))
    }
}
