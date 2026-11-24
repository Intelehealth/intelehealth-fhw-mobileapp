package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import org.intelehealth.app.database.dao.QueueDAO
import org.intelehealth.app.models.dto.QueueDTO
import org.intelehealth.app.testutil.TestAppDatabase
import org.intelehealth.klivekit.data.PreferenceHelper
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

/** Logout must remove every trace of the previous health worker's queue. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueDataCleanerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: SQLiteDatabase
    private lateinit var prefs: PreferenceHelper

    @Before
    fun setUp() {
        clearPrefs(context)
        db = TestAppDatabase.install(context)
        prefs = PreferenceHelper(context)
    }

    @After
    fun tearDown() {
        TestAppDatabase.uninstall()
    }

    private fun seedPreviousUser() {
        prefs.save(PreferenceHelper.QUEUE_CARD_DATA, """[{"patientName":"Previous user's patient"}]""")
        prefs.save(PreferenceHelper.STATUS_BANNER_DATA, """{"variant":"alert","title":"Doctor is on Break"}""")
        TestAppDatabase.onWorkerThread {
            QueueDAO().replaceQueue(listOf(
                Gson().fromJson("""{"queueEntryId":1,"visitUuid":"v1","position":1}""", QueueDTO::class.java)
            ))
        }
    }

    private fun queueCount(): Int =
        db.rawQuery("SELECT COUNT(*) FROM tbl_queue", null).use { it.moveToFirst(); it.getInt(0) }

    @Test
    fun clearOnLogout_removesCardBannerAndQueue() {
        seedPreviousUser()
        assertEquals(1, queueCount())

        QueueDataCleaner.clearOnLogout(context)

        assertNull(prefs.getString(PreferenceHelper.QUEUE_CARD_DATA))
        assertNull(prefs.getString(PreferenceHelper.STATUS_BANNER_DATA))
        assertEquals(0, queueCount())
    }

    @Test
    fun clearOnLogout_nextLoginStartsFromQueueTableNotOldNotifications() {
        seedPreviousUser()
        assertTrue(QueueCardUpdater.hasNotificationData(context))

        QueueDataCleaner.clearOnLogout(context)

        assertFalse(QueueCardUpdater.hasNotificationData(context))
        assertTrue(QueueCardUpdater.getPersisted(context).isEmpty())
        assertNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun clearOnLogout_keepsQmsConfiguration() {
        prefs.save(PreferenceHelper.IS_QMS_CONFIGURE, true)
        seedPreviousUser()

        QueueDataCleaner.clearOnLogout(context)

        assertTrue("QMS is a deployment setting, not user data",
            prefs.get(PreferenceHelper.IS_QMS_CONFIGURE, false))
    }

    @Test
    fun clearOnLogout_isSafeWhenNothingStored() {
        QueueDataCleaner.clearOnLogout(context)
        assertEquals(0, queueCount())
        assertNull(prefs.getString(PreferenceHelper.QUEUE_CARD_DATA))
    }
}
