package org.intelehealth.app.webrtc.receiver

/**
 * Decides where a "Queue Update" FCM data payload goes on the home screen.
 * Pure (no Android / DB access) so the routing rules can be unit tested;
 * [FCMNotificationReceiver] performs the actual update.
 */
object QueueNotificationRouter {

    enum class Route {
        /** Doctor status change -> home status banner ("Doctor is on Break"). */
        STATUS_BANNER,

        /** Patient now at the front of a queue -> home "Next In Queue" card. */
        QUEUE_CARD,

        /** A queue update the home screen doesn't react to. */
        IGNORED,

        /** Not a queue update at all; handled by the receiver's other branches. */
        NOT_QUEUE_UPDATE
    }

    private const val TYPE_QUEUE_UPDATE = "Queue Update"
    private const val EVENT_DOCTOR_STATUS = "doctor:status"
    private const val EVENT_QUEUED = "queue:queued"
    private const val EVENT_POSITION = "queue:position"

    @JvmStatic
    fun isQueueUpdate(data: Map<String, String?>): Boolean =
        data["type"].equals(TYPE_QUEUE_UPDATE, ignoreCase = true)

    /**
     * - A doctor status change ("doctor:status") refreshes the home status banner only.
     *   Checked first, so it never reaches the queue card.
     * - Only a patient at the front of the queue (position 1) refreshes the "Next In
     *   Queue" card, whether newly queued ("queue:queued") or moved up ("queue:position").
     * - Every other queue update is ignored here.
     */
    @JvmStatic
    fun route(data: Map<String, String?>): Route {
        if (!isQueueUpdate(data)) return Route.NOT_QUEUE_UPDATE
        val event = data["event"]
        if (event.equals(EVENT_DOCTOR_STATUS, ignoreCase = true)) return Route.STATUS_BANNER
        val cardEvent = event.equals(EVENT_QUEUED, ignoreCase = true)
                || event.equals(EVENT_POSITION, ignoreCase = true)
        return if (cardEvent && data["position"]?.trim() == "1") Route.QUEUE_CARD else Route.IGNORED
    }
}
