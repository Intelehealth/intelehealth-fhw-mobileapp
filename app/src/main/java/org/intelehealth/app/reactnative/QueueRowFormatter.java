package org.intelehealth.app.reactnative;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public static final String STATUS_ON_CALL = "onCall";
    public static final String STATUS_NEXT_IN_QUEUE = "nextInQueue";
    public static final String STATUS_WAITING = "waiting";

    private QueueRowFormatter() {
    }

    /**
     * Maps the DB {@link QueueStatus} (+ position) to the RN status union used
     * by the tabs ('onCall' | 'nextInQueue' | 'waiting'):
     * <ul>
     *   <li>CONNECTED -> onCall</li>
     *   <li>QUEUED at position 0 -> nextInQueue</li>
     *   <li>QUEUED at any other position -> waiting</li>
     *   <li>RE_QUEUED at position &gt; 0 -> waiting</li>
     * </ul>
     * Any other status (or unknown) is left unmapped (empty), so the row matches
     * no specific tab and appears only under "All".
     */
    public static String mapStatus(@Nullable String dbStatus, int position) {
        QueueStatus status = QueueStatus.fromValue(dbStatus);
        if (status == null) {
            return "";
        }
        switch (status) {
            case CONNECTED:
                return STATUS_ON_CALL;
            case QUEUED:
                return position == 0 ? STATUS_NEXT_IN_QUEUE : STATUS_WAITING;
            case RE_QUEUED:
                return position > 0 ? STATUS_WAITING : "";
            default:
                return "";
        }
    }

    /**
     * Status badge label, matching the RN QueueListItem STATUS_CONFIG labels.
     * Empty for an unmapped status.
     */
    public static String statusLabel(@Nullable String status) {
        if (STATUS_ON_CALL.equals(status)) {
            return "On Call";
        }
        if (STATUS_NEXT_IN_QUEUE.equals(status)) {
            return "Next in Queue";
        }
        if (STATUS_WAITING.equals(status)) {
            return "Waiting";
        }
        return "";
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
     * fallbacks), but as "N Mins" for the Visit Summary banner design.
     */
    public static String formatTimeInMinutes(@Nullable String status, @NonNull QueueRow row) {
        long millis;
        if (STATUS_ON_CALL.equals(status)) {
            Long elapsed = deltaMillis(row.getConnectedAt(), false);
            millis = elapsed != null ? elapsed : row.getWaitedMinutes() * 60_000L;
        } else {
            Long remaining = deltaMillis(row.getEtaAt(), true);
            millis = remaining != null ? remaining : row.getEtaMinutes() * 60_000L;
        }
        long minutes = Math.max(0, Math.round(millis / 60_000d));
        return minutes + (minutes == 1 ? " Min" : " Mins");
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
