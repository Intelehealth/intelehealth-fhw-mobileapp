package org.intelehealth.ekalarogya.webrtc.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.ekalarogya.databinding.ActivityCallLogBinding
import org.intelehealth.ekalarogya.webrtc.adapter.CallLogAdapter
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.call.ui.activity.CoreCallLogActivity

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 14:44.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class EkalCoreCallLogActivity : CoreCallLogActivity() {

    private lateinit var binding: ActivityCallLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallLogBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
    }

    override fun onLogs(logs: List<RtcCallLog>) {
        binding.callLogContent.rvCallLogs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CallLogAdapter(context, logs)
        }
    }
}