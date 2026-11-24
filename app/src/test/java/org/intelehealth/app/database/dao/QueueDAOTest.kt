package org.intelehealth.app.database.dao

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import org.intelehealth.app.models.dto.QueueDTO
import org.intelehealth.app.testutil.TestAppDatabase
import org.intelehealth.app.testutil.TestAppDatabase.onWorkerThread
import org.intelehealth.app.utilities.SessionManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** tbl_queue writes from the pull sync and the lookups the queue screens rely on. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueDAOTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: SQLiteDatabase
    private val dao = QueueDAO()

    @Before
    fun setUp() {
        db = TestAppDatabase.install(context)
    }

    @After
    fun tearDown() {
        TestAppDatabase.uninstall()
    }

    private fun dto(json: String): QueueDTO = Gson().fromJson(json, QueueDTO::class.java)

    private fun entry(id: Long, visit: String, position: Int = 1, extra: String = ""): QueueDTO =
        dto("""{"queueEntryId":$id,"visitUuid":"$visit","status":"QUEUED","position":$position$extra}""")

    /** replaceQueue writes inline only off the main thread (else on a shared executor). */
    private fun replace(list: List<QueueDTO>?) = onWorkerThread { dao.replaceQueue(list) }

    private fun queueIds(): List<Long> =
        db.rawQuery("SELECT queueEntryId FROM tbl_queue ORDER BY queueEntryId", null).use { c ->
            buildList { while (c.moveToNext()) add(c.getLong(0)) }
        }

    private fun column(id: Long, col: String): String? =
        db.rawQuery("SELECT $col FROM tbl_queue WHERE queueEntryId = ?", arrayOf(id.toString())).use {
            if (it.moveToFirst() && !it.isNull(0)) it.getString(0) else null
        }

    // --- replaceQueue ---

    @Test
    fun replaceQueue_insertsSnapshot() {
        replace(listOf(entry(1, "v1"), entry(2, "v2", position = 2)))
        assertEquals(listOf(1L, 2L), queueIds())
    }

    @Test
    fun replaceQueue_dropsEntriesNoLongerOnServer() {
        replace(listOf(entry(1, "v1"), entry(2, "v2"), entry(3, "v3")))
        replace(listOf(entry(2, "v2", position = 1)))

        assertEquals(listOf(2L), queueIds())
        assertEquals("1", column(2, "position"))
    }

    @Test
    fun replaceQueue_emptyListClearsTable() {
        replace(listOf(entry(1, "v1")))
        replace(emptyList())
        assertTrue(queueIds().isEmpty())
    }

    @Test
    fun replaceQueue_nullLeavesTableUntouched() {
        replace(listOf(entry(1, "v1")))
        replace(null)
        assertEquals(listOf(1L), queueIds())
    }

    @Test
    fun replaceQueue_skippedAfterLogout() {
        replace(listOf(entry(1, "v1")))
        SessionManager(context).setLogout(true)

        replace(listOf(entry(9, "v9")))

        assertEquals("a pull finishing after logout must not refill the queue", listOf(1L), queueIds())
    }

    @Test
    fun replaceQueue_signalsQueueChanged() {
        val before = QueueDAO.getQueueChanged().value
        Thread.sleep(2)
        replace(listOf(entry(1, "v1")))
        val after = QueueDAO.getQueueChanged().value
        assertTrue(after != null && after != before)
    }

    @Test
    fun replaceQueue_storesBooleansAsIntsAndVitalsAsJson() {
        replace(listOf(entry(1, "v1", extra = ""","escalated":true,"flagged":false,"heartbeatFlagged":true,"vitals":{"bp":"120/80"}""")))

        assertEquals("1", column(1, "escalated"))
        assertEquals("0", column(1, "flagged"))
        assertEquals("1", column(1, "heartbeatFlagged"))
        assertEquals("""{"bp":"120/80"}""", column(1, "vitals"))
    }

    @Test
    fun replaceQueue_missingVitalsStoredAsNull() {
        replace(listOf(entry(1, "v1")))
        assertNull(column(1, "vitals"))
    }

    // --- etaAt resolution ---

    @Test
    fun etaAt_keepsServerValue() {
        replace(listOf(entry(1, "v1", extra = ""","etaAt":"2026-11-23T10:00:00Z","queuedAt":"2026-11-23T09:00:00Z","etaMinutes":5""")))
        assertEquals("2026-11-23T10:00:00Z", column(1, "etaAt"))
    }

    @Test
    fun etaAt_derivedFromQueuedAtPlusEtaMinutes() {
        replace(listOf(entry(1, "v1", extra = ""","queuedAt":"2026-11-23T09:00:00Z","etaMinutes":25""")))
        assertEquals("2026-11-23T09:25:00Z", column(1, "etaAt"))
    }

    @Test
    fun etaAt_blankServerValueFallsBackToQueuedAt() {
        replace(listOf(entry(1, "v1", extra = ""","etaAt":"  ","queuedAt":"2026-11-23T09:00:00Z","etaMinutes":10""")))
        assertEquals("2026-11-23T09:10:00Z", column(1, "etaAt"))
    }

    @Test
    fun etaAt_nullWhenNoUsableInstant() {
        replace(listOf(
            entry(1, "v1", extra = ""","etaMinutes":10"""),
            entry(2, "v2", extra = ""","queuedAt":"yesterday","etaMinutes":10""")
        ))
        assertNull(column(1, "etaAt"))
        assertNull(column(2, "etaAt"))
    }

    // --- createQueueMap ---

    @Test
    fun createQueueMap_doesNotStoreChiefComplaint() {
        val map = dao.createQueueMap(entry(1, "v1", extra = ""","chiefComplaint":"Fever""""))
        assertFalse(map.containsKey("chiefComplaint"))
        assertEquals("v1", map["visitUuid"])
        assertEquals(0, map["escalated"])
    }

    // --- isVisitInQueue ---

    @Test
    fun isVisitInQueue_matchesIgnoringCase() {
        replace(listOf(entry(1, "Visit-ABC")))

        assertTrue(dao.isVisitInQueue("visit-abc"))
        assertTrue(dao.isVisitInQueue("VISIT-ABC"))
        assertFalse(dao.isVisitInQueue("visit-other"))
    }

    @Test
    fun isVisitInQueue_falseForNullOrEmpty() {
        replace(listOf(entry(1, "v1")))
        assertFalse(dao.isVisitInQueue(null))
        assertFalse(dao.isVisitInQueue(""))
    }

    // --- deleteAll ---

    @Test
    fun deleteAll_clearsQueueAndSignalsChange() {
        replace(listOf(entry(1, "v1"), entry(2, "v2")))
        val before = QueueDAO.getQueueChanged().value
        Thread.sleep(2)

        dao.deleteAll()

        assertTrue(queueIds().isEmpty())
        assertNotEquals(before, QueueDAO.getQueueChanged().value)
    }
}
