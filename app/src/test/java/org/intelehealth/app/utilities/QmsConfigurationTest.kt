package org.intelehealth.app.utilities

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.intelehealth.app.BuildConfig
import org.intelehealth.config.network.response.ConfigResponse
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.config.room.entity.FeatureActiveStatus
import org.intelehealth.klivekit.data.PreferenceHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * QMS (Queue Management System) is switched on per deployment by the server
 * config's top-level `qms_section` key. It flows:
 * config JSON -> [FeatureActiveStatus.activeQms] -> [ConfigFlagsHelper] ->
 * the `isQmsConfigure` pref that gates the home queue card / status banner,
 * the Queue bottom-nav tab, RN warm-up and the `addToQueue` push flag.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QmsConfigurationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var prefs: PreferenceHelper

    @Before
    fun setUp() {
        @Suppress("DEPRECATION")
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        prefs = PreferenceHelper(context)
        // ConfigDatabase is a process-wide singleton, but Robolectric gives every test a
        // fresh app (and data dir); drop the cached instance so each test opens its own.
        ConfigDatabase.resetInstanceForTesting()
        onWorkerThread { ConfigDatabase.getInstance(context).clearAllTables() }
    }

    private fun isQmsConfigured(): Boolean = prefs.get(PreferenceHelper.IS_QMS_CONFIGURE, false)

    private fun status(qms: Boolean, editVisit: Boolean = false) = FeatureActiveStatus(
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
    ).apply {
        activeQms = qms
        activeEditVisit = editVisit
    }

    /** Room rejects queries on the main thread, which Robolectric tests run on. */
    private fun onWorkerThread(block: () -> Unit) {
        var error: Throwable? = null
        val thread = Thread { try { block() } catch (t: Throwable) { error = t } }
        thread.start()
        thread.join()
        error?.let { throw it }
    }

    // --- server config parsing ---

    @Test
    fun configResponse_readsQmsSectionTrue() {
        val config = Gson().fromJson("""{"qms_section": true}""", ConfigResponse::class.java)
        assertTrue(config.activeQms)
    }

    @Test
    fun configResponse_readsQmsSectionFalse() {
        val config = Gson().fromJson("""{"qms_section": false}""", ConfigResponse::class.java)
        assertFalse(config.activeQms)
    }

    @Test
    fun configResponse_qmsOffWhenKeyMissing() {
        val config = Gson().fromJson("""{"webrtc_section": true}""", ConfigResponse::class.java)
        assertFalse(config.activeQms)
    }

    @Test
    fun featureActiveStatus_qmsOffByDefault() {
        val parsed = Gson().fromJson("""{"notes_section": true}""", FeatureActiveStatus::class.java)
        assertFalse(parsed.activeQms)
    }

    // --- default state ---

    @Test
    fun qmsIsOffWhenNeverConfigured() {
        assertFalse(isQmsConfigured())
    }

    // --- applyFlags ---

    @Test
    fun applyFlags_enablesQms() {
        ConfigFlagsHelper.applyFlags(prefs, status(qms = true))
        assertTrue(isQmsConfigured())
    }

    @Test
    fun applyFlags_disablesQms() {
        ConfigFlagsHelper.applyFlags(prefs, status(qms = false))
        assertFalse(isQmsConfigured())
    }

    @Test
    fun applyFlags_turnsQmsOffAfterItWasOn() {
        ConfigFlagsHelper.applyFlags(prefs, status(qms = true))
        ConfigFlagsHelper.applyFlags(prefs, status(qms = false))
        assertFalse(isQmsConfigured())
    }

    @Test
    fun applyFlags_qmsIsIndependentOfEditVisit() {
        ConfigFlagsHelper.applyFlags(prefs, status(qms = true, editVisit = false))
        assertTrue(isQmsConfigured())
        assertFalse(prefs.get(PreferenceHelper.IS_EDIT_VISIT_CONFIGURE, true))

        ConfigFlagsHelper.applyFlags(prefs, status(qms = false, editVisit = true))
        assertFalse(isQmsConfigured())
        assertTrue(prefs.get(PreferenceHelper.IS_EDIT_VISIT_CONFIGURE, false))
    }

    // --- refresh after app update ---

    @Test
    fun isRefreshPending_trueBeforeAnyConfigSync() {
        assertTrue(ConfigFlagsHelper.isRefreshPending(prefs))
    }

    @Test
    fun isRefreshPending_trueWhenSyncedOnAnotherAppVersion() {
        prefs.save(PreferenceHelper.CONFIG_SYNCED_APP_VERSION, BuildConfig.VERSION_CODE - 1)
        assertTrue(ConfigFlagsHelper.isRefreshPending(prefs))
    }

    @Test
    fun isRefreshPending_falseWhenSyncedOnThisAppVersion() {
        prefs.save(PreferenceHelper.CONFIG_SYNCED_APP_VERSION, BuildConfig.VERSION_CODE)
        assertFalse(ConfigFlagsHelper.isRefreshPending(prefs))
    }

    // --- onConfigSynced (reads the stored config row) ---

    @Test
    fun onConfigSynced_enablesQmsFromStoredConfig() {
        runBlocking { ConfigDatabase.getInstance(context).featureActiveStatusDao().add(status(qms = true)) }

        onWorkerThread { ConfigFlagsHelper.onConfigSynced(context) }

        assertTrue(isQmsConfigured())
        assertFalse(ConfigFlagsHelper.isRefreshPending(prefs))
    }

    @Test
    fun onConfigSynced_disablesQmsWhenServerTurnsItOff() {
        ConfigFlagsHelper.applyFlags(prefs, status(qms = true))
        runBlocking { ConfigDatabase.getInstance(context).featureActiveStatusDao().add(status(qms = false)) }

        onWorkerThread { ConfigFlagsHelper.onConfigSynced(context) }

        assertFalse(isQmsConfigured())
    }

    @Test
    fun onConfigSynced_keepsCurrentFlagWhenNoConfigRowStored() {
        ConfigFlagsHelper.applyFlags(prefs, status(qms = true))

        onWorkerThread { ConfigFlagsHelper.onConfigSynced(context) }

        assertTrue(isQmsConfigured())
        assertEquals(
            BuildConfig.VERSION_CODE,
            prefs.get(PreferenceHelper.CONFIG_SYNCED_APP_VERSION, 0)
        )
    }
}
