package org.intelehealth.ekalarogya.webrtc.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.github.ajalt.timberkt.Timber
import org.intelehealth.ekalarogya.databinding.ActivityChatEkalBinding
import org.intelehealth.ekalarogya.utilities.NotificationUtils
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.DayHeader.Companion.buildHeader
import org.intelehealth.klivekit.chat.model.ItemHeader
import org.intelehealth.klivekit.chat.model.MessageStatus
import org.intelehealth.klivekit.chat.model.MessageType
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity
import org.intelehealth.klivekit.chat.ui.activity.CoreChatActivity
import org.intelehealth.klivekit.chat.ui.adapter.ChatMessageAdapter
import org.intelehealth.klivekit.model.ChatResponse
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.Constants
import org.intelehealth.klivekit.utils.extensions.showToast
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Collections

/**
 * Created by Vaghela Mithun R. on 25-08-2023 - 16:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class EkalChatMessageActivity : CoreChatActivity() {
    private lateinit var binding: ActivityChatEkalBinding
    private lateinit var adapter: ChatMessageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatEkalBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        binding.chatContent.btnSendMessage.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        hideSoftKeyboard()
        if (args.doctorId!!.isEmpty()) {
            showToast(getString(R.string.please_wait_for_doctor))
            return
        }
        val message = binding.chatContent.etMessageInput.text.toString().trim { it <= ' ' }
        if (message.isNotEmpty()) {
            postMessages(message)
        } else {
            Toast.makeText(this, getString(R.string.empty_message_txt), Toast.LENGTH_SHORT).show()
        }
    }

    private fun postMessages(message: String) {
        if (args.doctorId == null || TextUtils.isEmpty(args.doctorId)) {
            Toast.makeText(this, getString(R.string.please_wait_for_doctor), Toast.LENGTH_LONG)
                .show()
            return
        }
        val chatMessage = ChatMessage()
        chatMessage.message = message
        chatMessage.senderId = args.nurseId!!
        chatMessage.patientId = args.patientId
        chatMessage.receiverId = args.doctorId!!
        chatMessage.visitId = args.visitId
        chatMessage.roomName = args.patientName
        chatMessage.receiverName = args.doctorName!!
        chatMessage.type = MessageType.TEXT.value
        chatMessage.messageStatus = MessageStatus.SENDING.value
        sendMessage(chatMessage)
    }

    override fun onMessagesLoad(messages: List<ChatMessage>) {
        updateListAdapter(messages)
        binding.chatContent.emptyView.isVisible = false
    }

    private fun updateListAdapter(chatMessages: List<ChatMessage>) {
        val messages = ArrayList<ItemHeader>()
        var messageDay = ""
        for (i in chatMessages.indices) {
            val message = chatMessages[i]
            message.messageStatus =
                if (message.isRead) MessageStatus.READ.value else MessageStatus.SENT.value
            if (message.senderId == args.nurseId) {
                message.layoutType = Constants.RIGHT_ITEM_HW
            } else {
                message.layoutType = Constants.LEFT_ITEM_DOCT
            }
            val msgDay = message.getMessageDay()
            Timber.d { "updateListAdapter: MessageDay[$i]=>$msgDay" }
            if (msgDay != messageDay) {
                messages.add(buildHeader(message.createdDate()))
                messageDay = msgDay
            }
            messages.add(message)
        }
        sortList(messages)
        updateAdapter(messages)
//        if (!isAlreadySetReadStatus) for (i in response.data.indices) {
//            //Log.v(TAG, "ID=" + mChatList.get(i).getString("id"));
//            if (response.data[i].layoutType == Constants.LEFT_ITEM_DOCT
//                && response.data[i].isRead
//            ) {
//                setReadStatus(response.data[i].id)
//                break
//            }
//        }
    }

    private fun updateAdapter(messages: MutableList<ItemHeader>) {
        adapter = ChatMessageAdapter(this, messages)
        binding.chatContent.rvConversation.layoutManager = LinearLayoutManager(this)
        binding.chatContent.rvConversation.adapter = adapter
    }

    private fun setReadStatus(messageId: Int) {
        val url = Constants.SET_READ_STATUS_OF_MESSAGE_URL + messageId
    }

    @SuppressLint("SimpleDateFormat")
    private fun sortList(messages: List<ItemHeader>) {
        messages.sortedBy { it.createdDate() }
//        Collections.sort(
//            messages
//        ) { o1: ItemHeader, o2: ItemHeader ->
//            try {
//                if (o1.isHeader() || o2.isHeader()) return@sort -1
//                val a = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
//                    .parse(o1.createdDate())
//                val b = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
//                    .parse(o2.createdDate())
//                return@sort b.compareTo(a)
//            } catch (e: ParseException) {
//                return@sort -1
//            }
//        }
    }

    override fun onMessageListEmpty() {}
    fun hideSoftKeyboard() {
        try {
            val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun startEkalChatActivity(context: Context, args: RtcArgs?) {
            val intent = Intent(context, EkalChatMessageActivity::class.java)
            context.startActivity(args(intent, args!!))
        }

        @JvmStatic
        fun getPendingIntent(context: Context?, args: RtcArgs?): PendingIntent {
            val chatIntent = Intent(context, EkalChatMessageActivity::class.java)
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(
                context, 0, args(chatIntent, args!!),
                NotificationUtils.getPendingIntentFlag()
            )
        }
    }
}