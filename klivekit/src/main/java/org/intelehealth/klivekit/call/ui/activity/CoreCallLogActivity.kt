package org.intelehealth.klivekit.call.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.call.ui.viewmodel.CallLogViewModel

/**
 * Created by Vaghela Mithun R. on 16-10-2023 - 12:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class CoreCallLogActivity : AppCompatActivity() {
    private val callLogViewModel by viewModels<CallLogViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callLogViewModel.getCallLogs().observe(this) { onLogs(it) }
    }

    abstract fun onLogs(logs: List<RtcCallLog>)

    fun clearLog() = callLogViewModel.clearLogs()

}