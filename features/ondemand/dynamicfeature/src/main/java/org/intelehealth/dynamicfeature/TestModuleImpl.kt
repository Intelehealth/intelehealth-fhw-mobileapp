package org.intelehealth.dynamicfeature

import android.content.Context
import org.intelehealth.features.ondemand.mediator.listener.VideoCallListener

/**
 * Created by Vaghela Mithun R. on 23-10-2024 - 13:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
const val TAG = "TestModuleImpl"

class TestModuleImpl : VideoCallListener {
    override fun onIncomingCall(context: Context?, data: HashMap<String, String>) {
        println("$TAG onIncomingCall module function call")
    }

    override fun startCallLogActivity(context: Context?) {
        println("$TAG startCallLogActivity module function call")
    }

    override fun testMethod(): String = TAG
}