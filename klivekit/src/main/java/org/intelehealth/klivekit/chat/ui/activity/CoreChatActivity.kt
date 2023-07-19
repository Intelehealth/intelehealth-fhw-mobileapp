package org.intelehealth.klivekit.chat.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import org.intelehealth.klivekit.chat.ui.adapter.ChatListingAdapter
import org.intelehealth.klivekit.chat.ui.viewmodel.ChatViewModel
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.utils.RTC_ARGS
import org.intelehealth.klivekit.utils.extensions.viewModelByFactory

/**
 * Created by Vaghela Mithun R. on 18-07-2023 - 23:39.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class CoreChatActivity : AppCompatActivity() {
    protected lateinit var args: RtcArgs

    companion object {
        fun args(rtcArg: RtcArgs) = Bundle().apply {
            putParcelable(RTC_ARGS, rtcArg)
        }
    }

    protected val chatViewModel: ChatViewModel by viewModelByFactory {
        args = IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
            ?: throw NullPointerException("args is null!")
        ChatViewModel(SocketManager.instance)
    }

    protected lateinit var chatListingAdapter: ChatListingAdapter

    protected open fun setupActionBar() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(args.patientName)
        }
    }
}