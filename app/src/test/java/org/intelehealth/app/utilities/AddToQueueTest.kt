package org.intelehealth.app.utilities

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import org.intelehealth.app.database.dao.QueueDAO
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.models.dto.QueueDTO
import org.intelehealth.app.models.dto.VisitDTO
import org.intelehealth.app.models.pushRequestApiCall.Visit
import org.intelehealth.app.testutil.TestAppDatabase
import org.intelehealth.app.testutil.TestAppDatabase.insertVisit
import org.intelehealth.app.testutil.TestAppDatabase.isSubmitted
import org.intelehealth.app.testutil.TestAppDatabase.onWorkerThread
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The QMS "addToQueue" flag on a visit push: sent only on the first submission
 * of an open visit when QMS is on, and never for a visit the server already queued.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AddToQueueTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: SQLiteDatabase
    private val queueDAO = QueueDAO()

    @Before
    fun setUp() {
        db = TestAppDatabase.install(context)
    }

    @After
    fun tearDown() {
        TestAppDatabase.uninstall()
    }

    private fun visitDto(uuid: String, endDate: String? = null) = VisitDTO().apply {
        setUuid(uuid)
        setEnddate(endDate)
    }

    private fun queue(visitUuid: String) = onWorkerThread {
        queueDAO.replaceQueue(listOf(
            Gson().fromJson("""{"queueEntryId":1,"visitUuid":"$visitUuid","position":1}""", QueueDTO::class.java)
        ))
    }

    private fun flagFor(dto: VisitDTO, qms: Boolean = true): Boolean? =
        Visit().also { PatientsFrameJson.applyQueueFlag(it, dto, qms, queueDAO) }.addToQueue

    @Test
    fun newOpenVisitWithQms_isAddedToQueue() {
        insertVisit(db, "v1")
        assertEquals(true, flagFor(visitDto("v1")))
    }

    @Test
    fun qmsOff_neverAddsToQueue() {
        insertVisit(db, "v1")
        assertNull(flagFor(visitDto("v1"), qms = false))
    }

    @Test
    fun endedVisit_isNotAddedToQueue() {
        insertVisit(db, "v1", endDate = "2026-11-23T11:00:00Z")
        assertNull(flagFor(visitDto("v1", endDate = "2026-11-23T11:00:00Z")))
    }

    @Test
    fun alreadySubmittedVisit_isNotQueuedAgainOnRePush() {
        insertVisit(db, "v1", submitted = true)
        assertNull("note edit / end visit re-push must not re-enqueue", flagFor(visitDto("v1")))
    }

    @Test
    fun visitAlreadyInPulledQueue_isMarkedSubmittedInsteadOfSentAgain() {
        insertVisit(db, "v1")
        queue("v1")

        assertNull(flagFor(visitDto("v1")))
        assertTrue("lost push response: record as submitted", isSubmitted(db, "v1"))
    }

    @Test
    fun addingToQueue_doesNotItselfMarkSubmitted() {
        insertVisit(db, "v1")
        flagFor(visitDto("v1"))
        assertFalse("only a successful push marks it submitted", isSubmitted(db, "v1"))
    }

    @Test
    fun unknownVisit_isTreatedAsNotSubmitted() {
        assertEquals(true, flagFor(visitDto("not-in-db")))
    }

    // --- submitted flag ---

    @Test
    fun markVisitSubmitted_setsFlagReadByIsVisitSubmitted() {
        insertVisit(db, "v1")
        assertFalse(VisitsDAO.isVisitSubmitted("v1"))

        VisitsDAO.markVisitSubmitted("v1")

        assertTrue(VisitsDAO.isVisitSubmitted("v1"))
    }

    @Test
    fun markVisitSubmitted_touchesOnlyThatVisit() {
        insertVisit(db, "v1")
        insertVisit(db, "v2")
        VisitsDAO.markVisitSubmitted("v1")
        assertFalse(VisitsDAO.isVisitSubmitted("v2"))
    }

    // --- payload ---

    @Test
    fun addToQueueFalseIsOmittedFromPayload() {
        val visit = Visit().apply { setAddToQueue(false) }
        assertNull(visit.addToQueue)
        assertFalse(Gson().toJson(visit).contains("addToQueue"))
        assertTrue(Gson().toJson(Visit().apply { setAddToQueue(true) }).contains("\"addToQueue\":true"))
    }
}
