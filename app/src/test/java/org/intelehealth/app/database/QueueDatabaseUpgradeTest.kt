package org.intelehealth.app.database

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.testutil.TestAppDatabase
import org.intelehealth.app.testutil.TestAppDatabase.insertEncounter
import org.intelehealth.app.testutil.TestAppDatabase.insertVisitFor
import org.intelehealth.app.testutil.TestAppDatabase.isSubmitted
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * DB upgrades for QMS: v6 added tbl_queue; v7 marks visits the server already has
 * as submitted, so updating the app doesn't send "addToQueue" for them again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueDatabaseUpgradeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: SQLiteDatabase
    private lateinit var helper: InteleHealthDatabaseHelper

    @Before
    fun setUp() {
        db = TestAppDatabase.install(context)
        helper = org.intelehealth.app.app.IntelehealthApplication.inteleHealthDatabaseHelper
    }

    @After
    fun tearDown() {
        TestAppDatabase.uninstall()
    }

    private fun upgradeFrom(oldVersion: Int) = helper.onUpgrade(db, oldVersion, AppConstants.DATABASE_VERSION)

    @Test
    fun currentVersionIsSeven() {
        assertEquals(7, AppConstants.DATABASE_VERSION)
    }

    @Test
    fun v6To7_marksSyncedVisitsSubmittedForEverySyncSpelling() {
        insertVisitFor(db, "lower", "p", sync = "true")
        insertVisitFor(db, "upper", "p", sync = "TRUE")
        insertVisitFor(db, "one", "p", sync = "1")

        upgradeFrom(6)

        assertTrue(isSubmitted(db, "lower"))
        assertTrue(isSubmitted(db, "upper"))
        assertTrue(isSubmitted(db, "one"))
    }

    @Test
    fun v6To7_marksVisitSubmittedWhenAnyEncounterSynced() {
        // Visit's own sync flag reset by a pending note edit / end visit.
        insertVisitFor(db, "edited", "p", sync = "false")
        insertEncounter(db, "e1", "edited", sync = "false")
        insertEncounter(db, "e2", "edited", sync = "true")

        upgradeFrom(6)

        assertTrue(isSubmitted(db, "edited"))
    }

    @Test
    fun v6To7_leavesNeverSentVisitsUnsubmitted() {
        insertVisitFor(db, "draft", "p", sync = "false")
        insertVisitFor(db, "draftWithLocalEncounter", "p", sync = "false")
        insertEncounter(db, "e1", "draftWithLocalEncounter", sync = "false")

        upgradeFrom(6)

        assertFalse(isSubmitted(db, "draft"))
        assertFalse(isSubmitted(db, "draftWithLocalEncounter"))
    }

    @Test
    fun v5To7_createsQueueTableAndMarksSubmitted() {
        db.execSQL("DROP TABLE tbl_queue")
        insertVisitFor(db, "synced", "p", sync = "true")

        upgradeFrom(5)

        val queueTableExists = db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name='tbl_queue'", null
        ).use { it.moveToFirst() }
        assertTrue(queueTableExists)
        assertTrue(isSubmitted(db, "synced"))
    }

    @Test
    fun newInstall_visitsStartUnsubmitted() {
        insertVisitFor(db, "fresh", "p", sync = "false")
        assertFalse(isSubmitted(db, "fresh"))
    }

    @Test(expected = IllegalStateException::class)
    fun unknownOldVersionFails() {
        helper.onUpgrade(db, 99, AppConstants.DATABASE_VERSION)
    }
}
