package org.intelehealth.klivekit.chat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.ui.viewmodel.ChatViewModel
import org.intelehealth.klivekit.model.RtcArgs

/**
 * Created by Vaghela Mithun R. on 04-09-2023 - 17:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class CoreChatActivity : AppCompatActivity() {
    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }
    protected lateinit var args: RtcArgs

    companion object {
        private const val CHAT_ARG = "chat_args"

        @JvmStatic
        fun args(intent: Intent, args: RtcArgs) = intent.apply {
            putExtra(CHAT_ARG, args)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractArgs()
    }

    private fun extractArgs() {
        intent ?: return
        if (intent.hasExtra(CHAT_ARG)) {
            IntentCompat.getParcelableExtra(intent, CHAT_ARG, RtcArgs::class.java)?.let {
                args = it
                Timber.d { "Args => ${Gson().toJson(args)}" }
                initChatObserver()
            }
            intent.data = null
        }
    }

    private fun initChatObserver() {
        if (::args.isInitialized && args.doctorId != null) {
            Timber.d { "roomId => ${getChatRoomId()}" }
            chatViewModel.getAllMessages(getChatRoomId()).observe(this) {
                it?.let { messages -> onMessagesLoad(messages) } ?: onMessageListEmpty()
            }
        } else onPartnerNotFound()
    }

    protected fun sendMessage(message: ChatMessage) = chatViewModel.sendMessage(message)

    abstract fun onMessagesLoad(messages: List<ChatMessage>)

    abstract fun onMessageListEmpty()

    private fun getChatRoomId() = "${args.nurseId}_${args.patientId}_${args.doctorId}"

    open fun onPartnerNotFound() {

    }
}