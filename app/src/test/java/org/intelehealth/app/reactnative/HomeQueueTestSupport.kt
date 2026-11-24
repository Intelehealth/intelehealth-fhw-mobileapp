package org.intelehealth.app.reactnative

import android.content.Context
import android.preference.PreferenceManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.JavaOnlyArray
import com.facebook.react.bridge.JavaOnlyMap
import org.mockito.MockedStatic
import org.mockito.Mockito

/**
 * [Arguments.createMap]/[Arguments.createArray] build native-backed RN maps that
 * can't load in a JVM test; route them to RN's pure-Java implementations.
 * Close the returned handle after the test.
 */
fun mockReactArguments(): MockedStatic<Arguments> =
    // Every other Arguments method (toBundle, fromBundle, ...) runs for real.
    Mockito.mockStatic(Arguments::class.java) { invocation ->
        when (invocation.method.name) {
            "createMap" -> JavaOnlyMap()
            "createArray" -> JavaOnlyArray()
            else -> invocation.callRealMethod()
        }
    }

/** Same default prefs file klivekit's PreferenceHelper uses. */
@Suppress("DEPRECATION")
fun clearPrefs(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
}
