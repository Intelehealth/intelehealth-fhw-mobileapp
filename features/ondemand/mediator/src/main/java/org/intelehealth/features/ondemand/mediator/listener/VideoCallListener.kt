package org.intelehealth.features.ondemand.mediator.listener

import android.content.Context

/**
 * Created by Vaghela Mithun R. on 26-09-2024 - 17:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface VideoCallListener {
    fun onIncomingCall(context: Context?, data: HashMap<String, String>)
    fun startCallLogActivity(context: Context?)
    fun testMethod():String
}