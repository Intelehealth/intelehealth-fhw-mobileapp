package org.intelehealth.klivekit.chat.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import io.socket.emitter.Emitter
import org.intelehealth.klivekit.chat.ChatClient
import org.intelehealth.klivekit.utils.AwsS3Utils

/**
 * Created by Vaghela Mithun R. on 18-07-2023 - 23:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatViewModel(private val chatClient: ChatClient) : ViewModel() {

    private val fileUploadBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    private fun emitter(event: String) = Emitter.Listener {

    }

    fun registerReceivers(context: Context) {
        IntentFilter().apply {
            addAction(AwsS3Utils.ACTION_FILE_UPLOAD_DONE)
            ContextCompat.registerReceiver(
                context,
                fileUploadBroadcastReceiver,
                this,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    fun unregisterBroadcast(context: Context) {
        context.unregisterReceiver(fileUploadBroadcastReceiver)
    }

    fun connect(url: String) {
        if (chatClient.isConnected().not()) chatClient.connect(url)
    }

}