package org.intelehealth.features.ondemand.mediator

import android.content.Context
import android.util.Log

/**
 * Created by Vaghela Mithun R. on 04-10-2024 - 18:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

const val TAG = "MediatorProvider"
const val VIDEO_CALL_IMPL_CLASS = "org.intelehealth.video.impl.VideoCallListenerImpl"

fun <T> createInstance(implClass: String): T? {
    try {
        return Class.forName(implClass).getConstructor().newInstance() as T
    } catch (e: ClassNotFoundException) {
        Log.e(TAG, "Provider class not found", e)
        return null
    }
}