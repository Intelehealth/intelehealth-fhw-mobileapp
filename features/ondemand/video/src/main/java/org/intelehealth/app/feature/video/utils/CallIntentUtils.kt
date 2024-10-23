package org.intelehealth.app.feature.video.utils

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.ajalt.timberkt.Timber
import org.intelehealth.core.utils.utility.RTC_ARGS
import org.intelehealth.app.feature.video.RtcEngine
import org.intelehealth.app.feature.video.model.CallArgs
import org.intelehealth.app.feature.video.notification.CallReceiver
import org.intelehealth.app.feature.video.notification.HeadsUpNotificationService
import org.intelehealth.app.feature.video.ui.activity.CoreCallLogActivity
import org.intelehealth.app.feature.video.ui.activity.IDAVideoActivity
import org.intelehealth.app.feature.video.utils.CallConstants.MAX_INT
import kotlin.random.Random

/**
 * Created by Vaghela Mithun R. on 8/30/2021.
 * vaghela@codeglo.com
 */
object CallIntentUtils {

    private const val REQUEST_CODE = 10001
    private const val CALL_RECEIVER_ACTION = "CALL_RECEIVER_ACTION"

    /**
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return Intent ChatCallActivity intent to start
     */
    fun getCallActivityIntent(messageBody: CallArgs, context: Context): Intent {
//        return RtcEngine.getConfig(context)?.callIntentClass?.let {
//            val callClass: Class<*> = Class.forName(it)
//
//        }
        return Intent(context, IDAVideoActivity::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    /**
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return Intent ChatCallActivity intent to start
     */
    fun getHeadsUpNotificationServiceIntent(messageBody: CallArgs, context: Context): Intent {
        return Intent(context, HeadsUpNotificationService::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
        }
    }

    /**
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return Intent ChatViewActivity intent to start
     */
    private fun getCallLogIntent(
        messageBody: CallArgs,
        context: Context
    ): Intent {
        return RtcEngine.getConfig(context)?.let {
            val callClass: Class<*> = Class.forName(it.callLogIntentClass)
            return@let Intent(context, callClass).apply {
                putExtra(RTC_ARGS, messageBody)
            }
        } ?: Intent(context, CoreCallLogActivity::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
        }
    }

    /**
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return ChatCallBroadCastReceiver intent
     */
    private fun getCallBroadcastIntent(
        messageBody: CallArgs,
        context: Context
    ): Intent {
        Timber.d { "getCallBroadcastIntent: ${messageBody.toJson()}" }
        return Intent(context, CallReceiver::class.java).apply {
            this.putExtra(RTC_ARGS, messageBody)
            this.action = getCallReceiverAction(context)
        }
    }

    /**
     * An specific action of notification will perform on pending intent
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    private fun getActionPendingBroadCastIntent(
        context: Context,
        messageBody: CallArgs
    ) = PendingIntent.getBroadcast(
        context,
        Random.nextInt(0, MAX_INT),
        getCallBroadcastIntent(messageBody, context),
        getPendingIntentFlag()
    )

    /**
     * An accept action of incoming call notification
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    fun getAcceptPendingBroadCastIntent(
        context: Context,
        messageBody: CallArgs
    ): PendingIntent {
        return getActionPendingBroadCastIntent(context, messageBody)
    }


    /**
     * A decline action of incoming call notification to notify sender that receiver is busy right now
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    fun getDeclinePendingBroadCastIntent(
        context: Context,
        messageBody: CallArgs
    ): PendingIntent {
        return getActionPendingBroadCastIntent(context, messageBody)
    }


    fun getBroadCastIntent(
        context: Context,
        messageBody: CallArgs
    ): PendingIntent {
        return getActionPendingBroadCastIntent(context, messageBody)
    }


    /**
     * A general action of notification to perform task
     * @param context Context of current scope
     * @param messageBody an instance of CallArgs to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    fun getPendingBroadCastIntent(
        context: Context,
        messageBody: CallArgs
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        Random.nextInt(0, MAX_INT),
        getCallBroadcastIntent(messageBody, context),
        getPendingIntentFlag()
    )


    fun getPendingActivityIntent(
        context: Context,
        messageBody: CallArgs,
        flag: Int = getPendingIntentFlag()
    ): PendingIntent {
        Timber.d { "getPendingActivityIntent -> url = ${messageBody.url}" }
        return getCallActivityIntent(messageBody, context).let {
            return@let getPendingIntentWithParentStack(context, it)
        }
//            ?: PendingIntent.getActivity(
//            context, Random.nextInt(0, MAX_INT),
//            getCallActivityIntent(messageBody, context),
//            flag
//        )
    }

    fun getOnGoingPendingActivityIntent(
        context: Context,
        messageBody: CallArgs,
        flag: Int = getPendingIntentFlag()
    ): PendingIntent {
        Timber.d { "getPendingActivityIntent -> url = ${messageBody.url}" }
        return PendingIntent.getActivity(
            context, Random.nextInt(0, MAX_INT),
            getCallActivityIntent(messageBody, context),
            flag
        )
    }

    fun getCallLogPendingIntent(
        context: Context,
        messageBody: CallArgs
    ): PendingIntent = PendingIntent.getActivity(
        context, Random.nextInt(0, MAX_INT),
        getCallLogIntent(messageBody, context),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )

    fun getOutGoingCallIntent(
        context: Context,
        messageBody: CallArgs
    ): PendingIntent = PendingIntent.getActivity(
        context, Random.nextInt(0, MAX_INT),
        getCallActivityIntent(messageBody.apply {
//            callStatus = OUTGOING_CALL
//            val recId = receiverId
//            receiverId = msgFrom
//            msgFrom = recId

        }, context),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )

    private fun getPendingIntentWithParentStack(context: Context, intent: Intent): PendingIntent {
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addNextIntentWithParentStack(intent)
        return taskStackBuilder.getPendingIntent(
            Random.nextInt(0, MAX_INT),
            getPendingIntentFlag()
        )
    }

    private fun getPendingIntentFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    fun getFlagUpdateCurrent() = getPendingIntentFlag().or(PendingIntent.FLAG_UPDATE_CURRENT)

    fun getCallReceiverAction(context: Context) =
        "${context.applicationContext.packageName}.$CALL_RECEIVER_ACTION"
}