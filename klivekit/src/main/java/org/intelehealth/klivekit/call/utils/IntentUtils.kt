package org.intelehealth.klivekit.call.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.torvis.pavo.chat.view.activity.ChatAgoraActivity
import com.torvis.pavo.home.view.fragment.chat.model.ChatActivityIntent
import com.torvis.pavo.home.view.fragment.chat.view.broadcastreceiver.ChatCallBroadCastReceiver
import com.torvis.pavo.util.*
import org.intelehealth.klivekit.call.notification.HeadsUpNotificationService
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.RTC_ARGS
import kotlin.random.Random

/**
 * Created by Vaghela Mithun R. on 8/30/2021.
 * vaghela@codeglo.com
 */
object IntentUtils {

    /**
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return Intent ChatCallActivity intent to start
     */
    fun getHeadsUpNotificationServiceIntent(messageBody: RtcArgs, context: Context): Intent {
        return Intent(context, HeadsUpNotificationService::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
        }
    }

    /**
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return Intent ChatViewActivity intent to start
     */
    fun getChatViewActivityIntent(
        messageBody: RtcArgs,
        context: Context
    ): Intent {

        return Intent(context, ChatAgoraActivity::class.java).apply {
            putExtra(CALL_MSG_BODY, messageBody)
            putExtra(CHAT_ACT_INTENT_MODEL, getChatActivityModel(messageBody))
        }
    }

    /**
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return ChatCallBroadCastReceiver intent
     */
    fun getChatBroadCastIntent(
        messageBody: RtcArgs,
        context: Context
    ): Intent {
        return Intent(context, ChatCallBroadCastReceiver::class.java).apply {
            putExtra(CALL_MSG_BODY, messageBody)
        }
    }

    /**
     * An specific action of notification will perform on pending intent
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    private fun getActionPendingBroadCastIntent(
        context: Context,
        messageBody: RtcArgs
    ) = PendingIntent.getBroadcast(
        context,
        Random.nextInt(0, MAX_INT),
        getChatBroadCastIntent(messageBody, context),
        0
    )

    /**
     * An accept action of incoming call notification
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    fun getAcceptPendingBroadCastIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent {
        return getActionPendingBroadCastIntent(context, messageBody)
    }


    /**
     * A decline action of incoming call notification to notify sender that receiver is busy right now
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    fun getDeclinePendingBroadCastIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent {
        return getActionPendingBroadCastIntent(context, messageBody)
    }


    fun getBroadCastIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent {
        return getActionPendingBroadCastIntent(context, messageBody)
    }


    /**
     * A general action of notification to perform task
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    fun getPendingBroadCastIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        Random.nextInt(0, MAX_INT),
        getChatBroadCastIntent(messageBody, context),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }


    )


    fun getPendingActivityIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent = PendingIntent.getActivity(
        context, Random.nextInt(0, MAX_INT),
        getCallActivityIntent(messageBody, context),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )

    fun getChatViewPendingActivityIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent = PendingIntent.getActivity(
        context, Random.nextInt(0, MAX_INT),
        getChatViewActivityIntent(messageBody, context),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )

    fun getOutGoingCallIntent(
        context: Context,
        messageBody: RtcArgs
    ): PendingIntent = PendingIntent.getActivity(
        context, Random.nextInt(0, MAX_INT),
        getCallActivityIntent(messageBody.apply {
            callStatus = OUTGOING_CALL
            val recId = receiverId
            receiverId = msgFrom
            msgFrom = recId

        }, context),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )


    /**
     * To get the value of chat view model
     * @return [ChatActivityIntent] model
     *
     **/

    fun getChatActivityModel(messageBody: RtcArgs) = ChatActivityIntent(
        receiverId = messageBody.msgFrom ?: "",
        name1 = "${messageBody.receiverId}-${messageBody.msgFrom}",
        name2 = "${messageBody.msgFrom}-${messageBody.receiverId}",
        receiverName = messageBody.username ?: "",
        age = messageBody.age.toString(),
        profilePicture = messageBody.profilePicture ?: "",
        matchScreen = false,
        searchScreen = false
    )

}