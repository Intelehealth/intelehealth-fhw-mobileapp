package org.intelehealth.fcm.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Created by Vaghela Mithun R. on 20-12-2023 - 16:44.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object FcmRemoteConfig {
    @JvmStatic
    fun getRemoteConfig(context: Context, onConfigFetched: (FirebaseRemoteConfig) -> Unit) {
        FirebaseApp.initializeApp(context)
        val instance = FirebaseRemoteConfig.getInstance()
        val config = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        instance.setConfigSettingsAsync(config)
        instance.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) onConfigFetched.invoke(instance)
        }
    }
}