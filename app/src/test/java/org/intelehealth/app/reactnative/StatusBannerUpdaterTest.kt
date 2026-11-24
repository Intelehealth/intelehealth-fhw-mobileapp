package org.intelehealth.app.reactnative

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.Arguments
import org.intelehealth.klivekit.data.PreferenceHelper
import org.junit.After
import org.junit.Assert.assertEquals
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

/** Home screen "Doctor is on Break" status banner driven by doctor:status notifications. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class StatusBannerUpdaterTest {

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

    private fun handle(vararg pairs: Pair<String, String>) =
        StatusBannerUpdater.handleBannerNotification(context, mapOf(*pairs))

    @Test
    fun away_persistsBannerWithPayloadTitle() {
        handle("doctorStatus" to "away", "title" to "A doctor is away — Pediatrician")

        val banner = StatusBannerUpdater.getPersisted(context)!!
        assertEquals("alert", banner.variant)
        assertEquals("A doctor is away — Pediatrician", banner.title)
        assertEquals("Queue Paused", banner.subtitle)
        assertEquals("View Queue", banner.actionLabel)
    }

    @Test
    fun away_withoutTitleFallsBackToDoctorOnBreak() {
        handle("doctorStatus" to "away")
        assertEquals("Doctor is on Break", StatusBannerUpdater.getPersisted(context)!!.title)

        handle("doctorStatus" to "away", "title" to "")
        assertEquals("Doctor is on Break", StatusBannerUpdater.getPersisted(context)!!.title)
    }

    @Test
    fun away_isCaseInsensitive() {
        handle("doctorStatus" to "AWAY")
        assertNotNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun otherStatusRemovesBanner() {
        handle("doctorStatus" to "away")
        assertNotNull(StatusBannerUpdater.getPersisted(context))

        handle("doctorStatus" to "online")
        assertNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun missingStatusRemovesBanner() {
        handle("doctorStatus" to "away")
        handle("title" to "something")
        assertNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun everyNotificationPostsBannerReceived() {
        val before = System.currentTimeMillis()
        handle("doctorStatus" to "away")
        val shown = StatusBannerUpdater.getBannerReceived().value!!
        assertTrue(shown >= before)

        handle("doctorStatus" to "online")
        assertTrue(StatusBannerUpdater.getBannerReceived().value!! >= shown)
    }

    @Test
    fun dismiss_clearsPersistedBanner() {
        handle("doctorStatus" to "away")
        StatusBannerUpdater.dismiss(context)
        assertNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun nullArgumentsAreIgnored() {
        StatusBannerUpdater.handleBannerNotification(null, mapOf("doctorStatus" to "away"))
        StatusBannerUpdater.handleBannerNotification(context, null)
        StatusBannerUpdater.dismiss(null)
        assertNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun getPersisted_nullWhenNothingStoredOrCorrupt() {
        assertNull(StatusBannerUpdater.getPersisted(context))
        PreferenceHelper(context).save(PreferenceHelper.STATUS_BANNER_DATA, "{broken")
        assertNull(StatusBannerUpdater.getPersisted(context))
    }

    @Test
    fun toBundle_mirrorsBannerFields() {
        val bundle = StatusBannerUpdater.toBundle(
            StatusBannerData("alert", "Title", "Subtitle", "Action")
        )
        assertEquals("alert", bundle.getString("variant"))
        assertEquals("Title", bundle.getString("title"))
        assertEquals("Subtitle", bundle.getString("subtitle"))
        assertEquals("Action", bundle.getString("actionLabel"))
    }
}
