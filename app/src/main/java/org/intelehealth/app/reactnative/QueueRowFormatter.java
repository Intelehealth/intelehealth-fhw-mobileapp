package org.intelehealth.app.reactnative;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.intelehealth.app.R;
import org.intelehealth.app.models.queue.QueueStatus;
import org.intelehealth.app.ui.queue.model.QueueRow;

import java.time.Instant;
import java.util.Locale;

/**
 * Presentation shaping for a {@link QueueRow}, shared by the Patient's Queue
 * list ({@link PatientQueueFragment}) and the Visit Summary queue banner so both
 * show the same status and wait time for a given queue entry.
 */
public final class QueueRowFormatter {

    private static final String TAG = "QueueRowFormatter";

    // Statuses with their own tab (Next / Waiting / On Call).
    public static final String STATUS_ON_CALL = "onCall";
    public static final String STATUS_NEXT_IN_QUEUE = "nextInQueue";
    public static final String STATUS_WAITING = "waiting";
    // Statuses shown only under "All" (no position, no wait/call time).
    public static final String STATUS_DOCTOR_ASSIGNED = "doctorAssigned";
    public static final String STATUS_CONNECTING = "connecting";
    public static final String STATUS_AWAITING_PRESCRIPTION = "awaitingPrescription";

    private QueueRowFormatter() {
    }

    /**
     * Maps the DB {@link QueueStatus} (+ position) to the RN status union:
     * <ul>
     *   <li>QUEUED / ESCALATED / RE_QUEUED at position 1 -> nextInQueue</li>
     *   <li>QUEUED / ESCALATED / RE_QUEUED at any other position -> waiting</li>
     *   <li>CALL_CONNECTED -> onCall</li>
     *   <li>ASSIGNED -> doctorAssigned</li>
     *   <li>CALL_CONNECTING -> connecting</li>
     *   <li>CALL_COMPLETED -> awaitingPrescription</li>
     * </ul>
     * Positions start at 1; the last four statuses carry no position. Any other
     * status (e.g. SUBMITTED, CANCELLED) or unknown value is left unmapped (empty)
     * and appears only under "All", without a badge.
     */
    public static String mapStatus(@Nullable String dbStatus, int position) {
        QueueStatus status = QueueStatus.fromValue(dbStatus);
        if (status == null) {
            return "";
        }
        switch (status) {
            case QUEUED:
            case ESCALATED:
            case RE_QUEUED:
                return position == 1 ? STATUS_NEXT_IN_QUEUE : STATUS_WAITING;
            case CALL_CONNECTED:
            case CONNECTED:
                return STATUS_ON_CALL;
            case ASSIGNED:
                return STATUS_DOCTOR_ASSIGNED;
            case CALL_CONNECTING:
            case CONNECTING:
                return STATUS_CONNECTING;
            case CALL_COMPLETED:
            case COMPLETED:
                return STATUS_AWAITING_PRESCRIPTION;
            default:
                return "";
        }
    }

    /**
     * Localized status label (string resources), matching the RN QueueListItem
     * STATUS_CONFIG labels. Empty for an unmapped status.
     */
    public static String statusLabel(@NonNull Context context, @Nullable String status) {
        int resId;
        if (STATUS_ON_CALL.equals(status)) {
            resId = R.string.queue_status_on_call;
        } else if (STATUS_NEXT_IN_QUEUE.equals(status)) {
            resId = R.string.queue_status_next_in_queue;
        } else if (STATUS_WAITING.equals(status)) {
            resId = R.string.queue_status_waiting;
        } else if (STATUS_DOCTOR_ASSIGNED.equals(status)) {
            resId = R.string.queue_status_doctor_assigned;
        } else if (STATUS_CONNECTING.equals(status)) {
            resId = R.string.queue_status_connecting;
        } else if (STATUS_AWAITING_PRESCRIPTION.equals(status)) {
            resId = R.string.queue_status_awaiting_prescription;
        } else {
            return "";
        }
        return context.getString(resId);
    }

    /**
     * Localized footer label for a row's time: "Duration" for an on-call entry, "Wait time" for
     * next/waiting, empty for statuses that show no time.
     */
    public static String timeLabel(@NonNull Context context, @Nullable String status) {
        if (STATUS_ON_CALL.equals(status)) {
            return context.getString(R.string.queue_time_duration);
        }
        if (STATUS_NEXT_IN_QUEUE.equals(status) || STATUS_WAITING.equals(status)) {
            return context.getString(R.string.queue_time_wait);
        }
        return "";
    }

    /**
     * Turns the stored patient photo value into a URI React Native's {@code Image}
     * can load. Local filesystem paths get a {@code file://} prefix; values that
     * already carry a scheme ({@code http}, {@code https}, {@code file},
     * {@code content}) are used as-is. Returns "" when there is no photo.
     */
    public static String toAvatarUri(@Nullable String photo) {
        if (TextUtils.isEmpty(photo)) {
            return "";
        }
        String trimmed = photo.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")
                || trimmed.startsWith("file://") || trimmed.startsWith("content://")) {
            return trimmed;
        }
        return "file://" + trimmed;
    }

    /** Localized minute unit: singular for 1, plural otherwise ("1 Min", "8 Mins"). */
    public static String minuteUnit(@NonNull Context context, long minutes) {
        return context.getString(minutes == 1 ? R.string.queue_time_minute : R.string.queue_time_minutes);
    }

    /** Only waiting (count-down to ETA) and on-call (count-up) entries show a time. */
    private static boolean hasQueueTime(@Nullable String status) {
        return STATUS_ON_CALL.equals(status) || STATUS_NEXT_IN_QUEUE.equals(status)
                || STATUS_WAITING.equals(status);
    }

    /**
     * The footer time string for a row:
     * <ul>
     *   <li>{@code onCall} -> elapsed call duration (waited minutes), "MM:00".</li>
     *   <li>{@code nextInQueue}/{@code waiting} -> live wait time counted from the
     *       server {@code etaAt} instant ("MM:SS"); falls back to {@code etaMinutes}
     *       when {@code etaAt} is absent/unparseable.</li>
     * </ul>
     */
    public static String formatTime(@Nullable String status, @NonNull QueueRow row) {
        if (!hasQueueTime(status)) {
            return "";
        }
        if (STATUS_ON_CALL.equals(status)) {
            // Elapsed call duration = now - connectedAt.
            String duration = elapsedSince(row.getConnectedAt());
            return duration != null ? duration : formatMinutes(row.getWaitedMinutes());
        }
        // Wait time for next/waiting = etaAt - now.
        String waitTime = remainingUntil(row.getEtaAt());
        return waitTime != null ? waitTime : formatMinutes(row.getEtaMinutes());
    }

    /**
     * Same time as {@link #formatTime(String, QueueRow)} (same instants and
     * fallbacks), but as "N Mins" (localized unit) for the Visit Summary banner design.
     */
    public static String formatTimeInMinutes(@NonNull Context context, @Nullable String status,
                                             @NonNull QueueRow row) {
        if (!hasQueueTime(status)) {
            return "";
        }
        long millis;
        if (STATUS_ON_CALL.equals(status)) {
            Long elapsed = deltaMillis(row.getConnectedAt(), false);
            millis = elapsed != null ? elapsed : row.getWaitedMinutes() * 60_000L;
        } else {
            Long remaining = deltaMillis(row.getEtaAt(), true);
            millis = remaining != null ? remaining : row.getEtaMinutes() * 60_000L;
        }
        long minutes = Math.max(0, Math.round(millis / 60_000d));
        return minutes + " " + minuteUnit(context, minutes);
    }

    /** Gap between an ISO-8601 instant and now (see formatMmSs); null on error. */
    @Nullable
    private static Long deltaMillis(@Nullable String isoInstant, boolean future) {
        if (TextUtils.isEmpty(isoInstant)) {
            return null;
        }
        try {
            long instantMillis = Instant.parse(isoInstant.trim()).toEpochMilli();
            long now = System.currentTimeMillis();
            return future ? instantMillis - now : now - instantMillis;
        } catch (Exception e) {
            Log.e(TAG, "deltaMillis failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Time remaining from now until a future ISO-8601 instant (server sends UTC,
     * e.g. {@code 2026-09-17T12:14:54.000Z}), "MM:SS", clamped to {@code >= 0}.
     * Returns null when the value is absent/unparseable so the caller can fall back.
     */
    @Nullable
    private static String remainingUntil(@Nullable String isoInstant) {
        return formatMmSs(isoInstant, true);
    }

    /**
     * Time elapsed from a past ISO-8601 instant until now, "MM:SS", clamped to
     * {@code >= 0}. Returns null when the value is absent/unparseable.
     */
    @Nullable
    private static String elapsedSince(@Nullable String isoInstant) {
        return formatMmSs(isoInstant, false);
    }

    /**
     * Formats the gap between {@code isoInstant} and now as "MM:SS", clamped to
     * {@code >= 0}. {@code future=true} counts instant-now (a future ETA),
     * {@code false} counts now-instant (a past connect time). Null on parse error.
     */
    @Nullable
    private static String formatMmSs(@Nullable String isoInstant, boolean future) {
        if (TextUtils.isEmpty(isoInstant)) {
            return null;
        }
        try {
            long instantMillis = Instant.parse(isoInstant.trim()).toEpochMilli();
            long now = System.currentTimeMillis();
            long deltaMillis = future ? instantMillis - now : now - instantMillis;
            long totalSeconds = Math.max(0, deltaMillis / 1000L);
            long hh = totalSeconds / 3600;
            long mm = (totalSeconds % 3600) / 60;
            long ss = totalSeconds % 60;
            // "HH:MM:SS" once it reaches an hour, matching the RN QueueListItem.
            return hh > 0
                    ? String.format(Locale.ENGLISH, "%02d:%02d:%02d", hh, mm, ss)
                    : String.format(Locale.ENGLISH, "%02d:%02d", mm, ss);
        } catch (Exception e) {
            Log.e(TAG, "formatMmSs failed: " + e.getMessage());
            return null;
        }
    }

    /** Minutes -> "MM:00" (or "HH:MM:00" from an hour) to match the RN row's time string. */
    private static String formatMinutes(int minutes) {
        int safe = Math.max(0, minutes);
        return safe >= 60
                ? String.format(Locale.ENGLISH, "%02d:%02d:00", safe / 60, safe % 60)
                : String.format(Locale.ENGLISH, "%02d:00", safe);
    }
}
