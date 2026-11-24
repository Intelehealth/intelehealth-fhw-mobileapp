package org.intelehealth.app.queue

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.config.room.entity.FeatureActiveStatus
import org.intelehealth.klivekit.data.PreferenceHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device checks for the QMS switch on the home screen: the Queue tab replaces
 * Help when QMS is configured, and "View Queue" from the Visit Submitted popup
 * (EXTRA_OPEN_QUEUE) opens the Queue tab directly.
 *
 * Needs a device/emulator with a logged-in account (the home screen expects a
 * signed-in session); without one the tests are skipped. Each test changes only
 * the stored feature-status row and the QMS / config-sync prefs, and restores
 * them afterwards.
 */
@RunWith(AndroidJUnit4::class)
class HomeQmsNavigationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = PreferenceHelper(context)
    private val dao get() = ConfigDatabase.getInstance(context).featureActiveStatusDao()

    private val touchedPrefs = listOf(
        PreferenceHelper.IS_QMS_CONFIGURE,
        PreferenceHelper.CONFIG_SYNCED_APP_VERSION
    )
    private var savedStatus: FeatureActiveStatus? = null
    private val savedPrefs = mutableMapOf<String, Any?>()

    @Before
    fun requireLoginAndSnapshot() {
        val session = SessionManager(context)
        assumeTrue(
            "Log in to the app on this device first: the home screen needs a signed-in session",
            !session.isLogout && session.providerID.isNotEmpty()
        )
        savedStatus = dao.getRecordNow()
        touchedPrefs.forEach { savedPrefs[it] = prefs.get<Any?>(it) }
    }

    @After
    fun restore() {
        if (savedPrefs.isEmpty()) return // skipped before the snapshot
        val saved = savedStatus
        if (saved != null) {
            runBlocking { dao.add(saved) } // REPLACE on the same id
        } else {
            ConfigDatabase.getInstance(context).openHelper.writableDatabase
                .execSQL("DELETE FROM tbl_feature_active_status")
        }
        savedPrefs.forEach { (key, value) ->
            if (value == null) prefs.clear(key) else prefs.save(key, value)
        }
    }

    /**
     * Sets QMS on/off both in the prefs and in the stored config row (every other
     * flag kept as it was), and marks the config as synced for this app version so
     * no refresh overrides it on launch.
     */
    private fun configureQms(enabled: Boolean) {
        // A second read gives a separate object to modify; savedStatus stays intact for restore.
        val status = dao.getRecordNow()?.apply { activeQms = enabled } ?: featureStatus(enabled)
        runBlocking { dao.add(status) }
        prefs.save(PreferenceHelper.IS_QMS_CONFIGURE, enabled)
        prefs.save(PreferenceHelper.CONFIG_SYNCED_APP_VERSION, BuildConfig.VERSION_CODE)
    }

    private fun featureStatus(qms: Boolean) = FeatureActiveStatus(
        id = 1,
        visitSummeryNote = true,
        visitSummeryAttachment = true,
        visitSummeryDoctorSpeciality = true,
        visitSummeryPriorityVisit = true,
        visitSummeryAppointment = true,
        visitSummerySeverityOfCase = true,
        visitSummeryFacilityToVisit = true,
        visitSummeryHwFollowUp = true,
        generateBillButton = false,
        restrictEndVisit = false,
        printUsingThermalPrinter = false
    ).apply { activeQms = qms }

    private fun launchHome(openQueue: Boolean = false): ActivityScenario<HomeScreenActivity_New> {
        val intent = Intent(context, HomeScreenActivity_New::class.java)
        if (openQueue) intent.putExtra(HomeScreenActivity_New.EXTRA_OPEN_QUEUE, true)
        return ActivityScenario.launch(intent)
    }

    private fun ActivityScenario<HomeScreenActivity_New>.bottomNav(check: (BottomNavigationView) -> Unit) =
        onActivity { check(it.findViewById(R.id.bottom_nav_home)) }

    @Test
    fun qmsOn_showsQueueTabInsteadOfHelp() {
        configureQms(true)
        launchHome().use { scenario ->
            scenario.bottomNav { nav ->
                assertTrue(nav.menu.findItem(R.id.bottom_nav_queue).isVisible)
                assertFalse(nav.menu.findItem(R.id.bottom_nav_help).isVisible)
            }
        }
    }

    @Test
    fun qmsOff_showsHelpTabInsteadOfQueue() {
        configureQms(false)
        launchHome().use { scenario ->
            scenario.bottomNav { nav ->
                assertFalse(nav.menu.findItem(R.id.bottom_nav_queue).isVisible)
                assertTrue(nav.menu.findItem(R.id.bottom_nav_help).isVisible)
            }
        }
    }

    @Test
    fun viewQueueFromVisitSubmitted_opensQueueTabWhenQmsOn() {
        configureQms(true)
        launchHome(openQueue = true).use { scenario ->
            scenario.bottomNav { nav -> assertEquals(R.id.bottom_nav_queue, nav.selectedItemId) }
        }
    }

    @Test
    fun viewQueueExtraIsIgnoredWhenQmsOff() {
        configureQms(false)
        launchHome(openQueue = true).use { scenario ->
            scenario.bottomNav { nav -> assertEquals(R.id.bottom_nav_home_menu, nav.selectedItemId) }
        }
    }

    @Test
    fun selectingQueueTab_opensPatientQueueScreen() {
        configureQms(true)
        launchHome().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<BottomNavigationView>(R.id.bottom_nav_home).selectedItemId = R.id.bottom_nav_queue
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val queueFragment = activity.supportFragmentManager
                    .findFragmentByTag(org.intelehealth.app.reactnative.PatientQueueFragment.TAG)
                assertTrue("Patient's Queue fragment should be shown", queueFragment != null)
            }
        }
    }
}
