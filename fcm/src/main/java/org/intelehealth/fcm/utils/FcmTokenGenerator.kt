package org.intelehealth.fcm.utils

import android.os.Handler
import android.os.Looper
import com.google.firebase.messaging.FirebaseMessaging


/**
 * Created by Vaghela Mithun R. on 20-01-2023 - 14:51.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
object FcmTokenGenerator {
    @JvmStatic
    fun getDeviceToken(onNewToken: (String) -> Unit) {
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                if (!it.result.isNullOrEmpty()) {
                    onNewToken(it.result)
                }
            } else {
                Handler(Looper.getMainLooper()).postDelayed({ getDeviceToken(onNewToken) }, 500)
            }
        }
    }
}