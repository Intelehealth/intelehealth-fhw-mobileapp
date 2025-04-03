package org.intelehealth.klivekit.call.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.intelehealth.klivekit.call.data.CallLogRepository
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.call.ui.viewmodel.CallLogViewModel
import org.intelehealth.klivekit.room.WebRtcDatabase
import org.intelehealth.klivekit.utils.extensions.viewModelByFactory

/**
 * Created by Vaghela Mithun R. on 16-10-2023 - 12:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class CoreCallLogActivity : AppCompatActivity() {
    private val callLogViewModel: CallLogViewModel by viewModelByFactory {
        CallLogViewModel(CallLogRepository(WebRtcDatabase.getInstance(this).rtcCallLogDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callLogViewModel.getCallLogs().observe(this) { onLogs(it) }
        setupActionBar()
    }

    abstract fun onLogs(logs: List<RtcCallLog>)

    fun clearLog() = callLogViewModel.clearLogs()

    open fun setupActionBar() {
    }

}