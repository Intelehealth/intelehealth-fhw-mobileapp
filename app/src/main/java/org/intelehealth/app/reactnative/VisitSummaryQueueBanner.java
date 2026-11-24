package org.intelehealth.app.reactnative;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.intelehealth.app.R;
import org.intelehealth.app.ui.queue.model.QueueRow;

/**
 * Rules and props for the queue status banner at the top of the Visit Summary
 * screen (e.g. "Position #2 / Next in Queue" with the wait time as a trailing
 * pill). Kept out of {@code VisitSummaryActivity_New} so it can be unit tested.
 */
public final class VisitSummaryQueueBanner {

    private VisitSummaryQueueBanner() {
    }

    /**
     * The banner shows only when QMS is configured, the visit has been submitted
     * (Send Visit adds the speciality attribute) and the visit is not ended.
     */
    public static boolean shouldShow(boolean isQmsConfigured, boolean isVisitSubmitted,
                                     boolean isVisitEnded) {
        return isQmsConfigured && isVisitSubmitted && !isVisitEnded;
    }

    /**
     * Launch options / update payload for the RN {@code VisitSummaryStatusBannerModule}.
     * Status and wait time come from {@link QueueRowFormatter}, so they match this
     * visit's row on the Patient's Queue list; the raw etaAt/connectedAt instants
     * let the RN banner tick the time live, like the list item.
     */
    @NonNull
    public static Bundle buildProps(@NonNull Context context, @NonNull QueueRow row) {
        int position = row.getPosition();
        String status = QueueRowFormatter.mapStatus(row.getStatus(), position);
        // Queue positions start at 1; 0 means the entry has no waiting position (e.g. on call),
        // so show "In Queue" rather than "Position #0". The patient's id isn't repeated here -
        // this is already that patient's visit.
        String title = position > 0
                ? context.getString(R.string.queue_banner_position, position)
                : context.getString(R.string.queue_banner_in_queue);

        Bundle bannerProps = new Bundle();
        bannerProps.putString("variant", "warning");
        bannerProps.putString("title", title);
        bannerProps.putString("subtitle", QueueRowFormatter.statusLabel(context, status));
        bannerProps.putString("status", status);
        // Pre-formatted snapshot (initial paint / fallback when no instant).
        bannerProps.putString("time", QueueRowFormatter.formatTimeInMinutes(context, status, row));
        // Localized minute unit for the banner's live (JS-side) tick, which re-formats the time.
        bannerProps.putString("minuteLabel", QueueRowFormatter.minuteUnit(context, 1));
        bannerProps.putString("minutesLabel", QueueRowFormatter.minuteUnit(context, 2));
        bannerProps.putString("etaAt", row.getEtaAt() != null ? row.getEtaAt() : "");
        bannerProps.putString("connectedAt", row.getConnectedAt() != null ? row.getConnectedAt() : "");
        return bannerProps;
    }
}
