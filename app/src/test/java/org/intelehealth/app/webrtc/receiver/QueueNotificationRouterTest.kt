package org.intelehealth.app.webrtc.receiver

import org.intelehealth.app.webrtc.receiver.QueueNotificationRouter.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which "Queue Update" FCM payloads reach the home status banner and queue card. */
class QueueNotificationRouterTest {

    private fun route(vararg pairs: Pair<String, String?>) = QueueNotificationRouter.route(mapOf(*pairs))

    @Test
    fun doctorStatus_goesToStatusBanner() {
        assertEquals(Route.STATUS_BANNER, route("type" to "Queue Update", "event" to "doctor:status"))
    }

    @Test
    fun doctorStatus_winsEvenWithPositionOne() {
        assertEquals(Route.STATUS_BANNER,
            route("type" to "Queue Update", "event" to "doctor:status", "position" to "1"))
    }

    @Test
    fun queuedAtPositionOne_goesToQueueCard() {
        assertEquals(Route.QUEUE_CARD, route("type" to "Queue Update", "event" to "queue:queued", "position" to "1"))
    }

    @Test
    fun movedUpToPositionOne_goesToQueueCard() {
        assertEquals(Route.QUEUE_CARD, route("type" to "Queue Update", "event" to "queue:position", "position" to " 1 "))
    }

    @Test
    fun queueEventsBehindFirstPosition_areIgnored() {
        assertEquals(Route.IGNORED, route("type" to "Queue Update", "event" to "queue:queued", "position" to "2"))
        assertEquals(Route.IGNORED, route("type" to "Queue Update", "event" to "queue:position", "position" to "10"))
        assertEquals(Route.IGNORED, route("type" to "Queue Update", "event" to "queue:position", "position" to "01"))
    }

    @Test
    fun queueEventsWithoutPosition_areIgnored() {
        assertEquals(Route.IGNORED, route("type" to "Queue Update", "event" to "queue:queued"))
        assertEquals(Route.IGNORED, route("type" to "Queue Update", "event" to "queue:queued", "position" to null))
        assertEquals(Route.IGNORED, route("type" to "Queue Update", "event" to "queue:queued", "position" to ""))
    }

    @Test
    fun otherQueueEvents_areIgnored() {
        listOf("queue:completed", "queue:cancelled", "queue:assigned", "", null).forEach {
            assertEquals("event=$it", Route.IGNORED,
                route("type" to "Queue Update", "event" to it, "position" to "1"))
        }
    }

    @Test
    fun matchingIsCaseInsensitive() {
        assertEquals(Route.STATUS_BANNER, route("type" to "queue update", "event" to "DOCTOR:STATUS"))
        assertEquals(Route.QUEUE_CARD, route("type" to "QUEUE UPDATE", "event" to "Queue:Queued", "position" to "1"))
    }

    @Test
    fun nonQueueNotifications_areLeftToOtherHandlers() {
        assertEquals(Route.NOT_QUEUE_UPDATE, route("type" to "video_call", "event" to "doctor:status"))
        assertEquals(Route.NOT_QUEUE_UPDATE, route("type" to "visit_started"))
        assertEquals(Route.NOT_QUEUE_UPDATE, route("event" to "queue:queued", "position" to "1"))
        assertEquals(Route.NOT_QUEUE_UPDATE, route())
    }

    @Test
    fun isQueueUpdate_onlyForQueueUpdateType() {
        assertTrue(QueueNotificationRouter.isQueueUpdate(mapOf("type" to "Queue Update")))
        assertFalse(QueueNotificationRouter.isQueueUpdate(mapOf("type" to "QueueUpdate")))
        assertFalse(QueueNotificationRouter.isQueueUpdate(mapOf("type" to null)))
        assertFalse(QueueNotificationRouter.isQueueUpdate(emptyMap()))
    }
}
