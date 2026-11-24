package org.intelehealth.app.reactnative;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.intelehealth.app.R;
import org.intelehealth.app.ui.queue.model.QueueRow;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.ArrayList;

/**
 * Builds the React Native props for the Patient's Queue screen
 * ({@link PatientQueueFragment}): one bundle per {@link QueueRow} plus the
 * screen-level labels and status banner. Kept out of the fragment so it can be
 * unit tested.
 */
public final class QueueListProps {

    private static final String TAG = "QueueListProps";

    private QueueListProps() {
    }

    /**
     * Wraps the built RN rows under the "queue" key and seeds the status banner.
     */
    @NonNull
    public static Bundle screenProps(@NonNull Context ctx, @NonNull ArrayList<Bundle> rows) {
        Bundle initialProperties = new Bundle();
        initialProperties.putParcelableArrayList("queue", rows);

        // Seed the status banner from the same persisted "queue_status" FCM
        // payload that drives the home banner (StatusBannerUpdater), so both
        // screens show identical queue status. Absent while no doctor is away or
        // after the banner was closed on either screen; the RN side shows none then.
        StatusBannerData banner = StatusBannerUpdater.getPersisted(ctx);
        if (banner != null) {
            initialProperties.putBundle("banner", StatusBannerUpdater.toBundle(banner));
        }

        // Localized screen labels (tabs, count, search); the RN components fall back to
        // English only when these are absent.
        Bundle labels = new Bundle();
        labels.putString("tabAll", ctx.getString(R.string.queue_tab_all));
        labels.putString("tabNext", ctx.getString(R.string.queue_tab_next));
        labels.putString("tabWaiting", ctx.getString(R.string.queue_tab_waiting));
        labels.putString("tabOnCall", ctx.getString(R.string.queue_tab_on_call));
        labels.putString("countLabel", ctx.getString(R.string.queue_count_label));
        labels.putString("searchPlaceholder", ctx.getString(R.string.queue_search_placeholder));
        initialProperties.putBundle("labels", labels);
        return initialProperties;
    }

    /**
     * One {@link QueueRow} -> one RN QueueListItem prop bundle. Patient display
     * fields (openmrs id, name, gender, date_of_birth) already come joined on
     * the row, so there is no separate lookup here.
     */
    @NonNull
    public static Bundle rowBundle(@NonNull Context ctx, @NonNull QueueRow row) {
        String openmrsId = orEmpty(row.getOpenmrsId());
        String patientName = orEmpty(row.getPatientName());
        String gender = orEmpty(row.getGender());
        int age = 0;
        try {
            age = DateAndTimeUtils.getAge(row.getDateOfBirth(), ctx);
        } catch (Exception e) {
            Log.e(TAG, "age calc failed: " + e.getMessage());
        }

        int position = row.getPosition();
        String status = QueueRowFormatter.mapStatus(row.getStatus(), position);

        Bundle bundle = new Bundle();
        bundle.putString("queueNumber", openmrsId);
        bundle.putString("patientName", patientName);
        bundle.putString("gender", gender);
        bundle.putInt("age", age);
        bundle.putString("patientId", openmrsId);
        // Same parsing as the home queue card and the visit summary screen, so the list
        // shows the complaint header names (e.g. "Fever", "Cough") rather than a naive
        // comma split of the raw blob.
        bundle.putStringArrayList("symptoms", QueueCardUpdater.extractComplaintNames(row.getChiefComplaint()));
        bundle.putInt("position", position);
        bundle.putString("status", status);
        // Localized labels (string resources); the RN row falls back to English only when absent.
        bundle.putString("statusLabel", QueueRowFormatter.statusLabel(ctx, status));
        bundle.putString("timeLabel", QueueRowFormatter.timeLabel(ctx, status));
        bundle.putString("overdueLabel", ctx.getString(R.string.queue_time_overdue));
        // Template with %1$d for the "+N More" symptom chip (N is computed on the RN side).
        bundle.putString("moreLabel", ctx.getString(R.string.queue_more_symptoms));
        // Pre-formatted snapshot used as a fallback (and initial paint) when the
        // row has no timestamp to tick from.
        bundle.putString("time", QueueRowFormatter.formatTime(status, row));
        // Raw instants so the RN screen can tick the wait time / duration every
        // second on the JS side (no DB re-read, no bridge push). Empty when absent.
        bundle.putString("etaAt", orEmpty(row.getEtaAt()));
        bundle.putString("connectedAt", orEmpty(row.getConnectedAt()));
        // Patient profile pic. patient_photo holds an absolute local file path
        // (AppConstants.IMAGE_PATH + uuid + ".jpg"); RN's <Image> needs a URI
        // scheme, so wrap a bare path in file://. Empty when absent, so the RN
        // row falls back to its placeholder avatar.
        bundle.putString("avatarUrl", QueueRowFormatter.toAvatarUri(row.getPatientPhoto()));
        return bundle;
    }

    private static String orEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }
}
