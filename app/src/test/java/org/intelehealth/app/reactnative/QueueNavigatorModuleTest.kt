package org.intelehealth.app.reactnative

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.intelehealth.app.R
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
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** "QueueNavigator" native module the RN queue screens call to navigate and close the banner. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QueueNavigatorModuleTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var arguments: MockedStatic<Arguments>
    private lateinit var activity: Activity
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var bannerContainer: View

    @Before
    fun setUp() {
        clearPrefs(context)
        arguments = mockReactArguments()
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()

        // Minimal stand-in for the home screen: bottom nav + status banner container.
        val themed = ContextThemeWrapper(activity, com.google.android.material.R.style.Theme_MaterialComponents_Light)
        bottomNav = BottomNavigationView(themed).apply {
            id = R.id.bottom_nav_home
            inflateMenu(R.menu.menu_home_footer)
        }
        bannerContainer = FrameLayout(activity).apply { id = R.id.status_banner_container }
        activity.setContentView(LinearLayout(activity).apply {
            addView(bannerContainer)
            addView(bottomNav)
        })
    }

    @After
    fun tearDown() {
        arguments.close()
    }

    private fun module(currentActivity: Activity?): QueueNavigatorModule {
        // Abstract in RN 0.79: a mock built on the real constructor (so it wraps the app
        // context for prefs) with real methods, except the current activity.
        val reactContext = Mockito.mock(
            ReactApplicationContext::class.java,
            Mockito.withSettings().useConstructor(context).defaultAnswer(Mockito.CALLS_REAL_METHODS)
        )
        Mockito.doReturn(currentActivity).`when`(reactContext).currentActivity
        return QueueNavigatorModule(reactContext)
    }

    @Test
    fun name_isQueueNavigator() {
        assertEquals("QueueNavigator", module(activity).name)
    }

    @Test
    fun openQueueDetails_startsDetailsWithTappedRowAsProps() {
        val row = JavaOnlyMap.of("patientName", "Asha Patil", "position", 2.0, "status", "waiting")

        module(activity).openQueueDetails(row)

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(QueueDetailsActivity::class.java.name, intent.component?.className)
        val props = intent.getBundleExtra(QueueDetailsActivity.EXTRA_INITIAL_PROPS)!!
        assertEquals("Asha Patil", props.getString("patientName"))
        assertEquals("waiting", props.getString("status"))
        assertEquals(2.0, props.getDouble("position"), 0.0)
    }

    @Test
    fun openQueueDetails_nullRowStillOpensWithEmptyProps() {
        module(activity).openQueueDetails(null)

        val props: Bundle? = shadowOf(activity).nextStartedActivity
            .getBundleExtra(QueueDetailsActivity.EXTRA_INITIAL_PROPS)
        assertNotNull(props)
        assertTrue(props!!.isEmpty)
    }

    @Test
    fun openQueueDetails_noActivityDoesNothing() {
        module(null).openQueueDetails(JavaOnlyMap.of("patientName", "x"))
        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test
    fun openPatientQueue_selectsQueueTab() {
        module(activity).openPatientQueue()
        assertEquals(R.id.bottom_nav_queue, bottomNav.selectedItemId)
    }

    @Test
    fun openPatientQueue_noActivityIsSafe() {
        module(null).openPatientQueue()
        assertEquals(R.id.bottom_nav_home_menu, bottomNav.selectedItemId)
    }

    @Test
    fun dismissStatusBanner_clearsBannerAndCollapsesContainer() {
        PreferenceHelper(context).save(PreferenceHelper.STATUS_BANNER_DATA, """{"variant":"alert","title":"t"}""")

        module(activity).dismissStatusBanner()

        assertNull(StatusBannerUpdater.getPersisted(context))
        assertEquals(View.GONE, bannerContainer.visibility)
    }

    @Test
    fun dismissStatusBanner_withoutActivityStillClearsBanner() {
        PreferenceHelper(context).save(PreferenceHelper.STATUS_BANNER_DATA, """{"variant":"alert","title":"t"}""")

        module(null).dismissStatusBanner()

        assertNull(StatusBannerUpdater.getPersisted(context))
        assertEquals(View.VISIBLE, bannerContainer.visibility)
    }

    @Test
    fun close_finishesCurrentActivity() {
        module(activity).close()
        assertTrue(activity.isFinishing)
    }
}
