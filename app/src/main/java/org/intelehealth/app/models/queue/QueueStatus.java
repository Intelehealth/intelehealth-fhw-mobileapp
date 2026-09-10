package org.intelehealth.app.models.queue;

import androidx.annotation.Nullable;

/**
 * Lifecycle status of a queue entry, as stored in {@code tbl_queue.status} and
 * delivered by the queue API. {@link #SUBMITTED} is the server-side default.
 */
public enum QueueStatus {
    SUBMITTED,
    QUEUED,
    ESCALATED,
    ASSIGNED,
    CONNECTING,
    CONNECTED,
    COMPLETED,
    CANCELLED,
    RE_QUEUED;

    /**
     * Null-/unknown-safe parse of a DB status string. Matching is
     * case-insensitive and trims surrounding whitespace. Returns {@code null}
     * when the value is null, blank, or not one of the known statuses.
     */
    @Nullable
    public static QueueStatus fromValue(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (QueueStatus status : values()) {
            if (status.name().equalsIgnoreCase(trimmed)) {
                return status;
            }
        }
        return null;
    }
}
