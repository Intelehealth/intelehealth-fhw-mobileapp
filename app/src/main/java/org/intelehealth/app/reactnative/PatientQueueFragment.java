package org.intelehealth.app.reactnative;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.react.ReactFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.intelehealth.app.R;
import org.intelehealth.app.models.queue.QueueStatus;
import org.intelehealth.app.ui.queue.factory.QueueViewModelFactory;
import org.intelehealth.app.ui.queue.model.QueueRow;
import org.intelehealth.app.ui.queue.viewmodel.QueueViewModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Native host for the React Native "Patient's Queue" screen.
 *
 * Embeds the RN component registered as "PatientQueueModule"
 * (see react-native/index.js) via a {@link ReactFragment}, reusing the single
 * ReactHost initialized in IntelehealthApplication. Loaded into the bottom-nav
 * container of HomeScreenActivity_New when the Queue tab is selected.
 *
 * The queue rows come from the local DB through {@link QueueViewModel} (backed
 * by QueueRepository -> QueueDAO), which reads the queue joined with patient
 * details in a single query. The fragment observes the ViewModel and attaches
 * the RN view once the rows arrive, mapping each {@link QueueRow} into the RN
 * {@code queue} prop.
 */
public class PatientQueueFragment extends Fragment {

    public static final String TAG = "TAG_PATIENT_QUEUE";

    private static final String RN_COMPONENT_NAME = "PatientQueueModule";

    private QueueViewModel queueViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_queue, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Keep the Queue bottom-nav item selected, mirroring MyAchievementsFragment.
        // The host activity's onResume() re-checks the Home item, so (e.g. when
        // returning from QueueDetailsActivity) the Queue fragment must reassert its
        // own selection here.
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
            if (bottomNav != null) {
                bottomNav.getMenu().findItem(R.id.bottom_nav_queue).setChecked(true);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queueViewModel = QueueViewModelFactory.create(this);
        // The ViewModel reads off the main thread and posts the rows here; the
        // list survives config changes, so we observe always but load only once.
        queueViewModel.getQueue().observe(getViewLifecycleOwner(), this::onQueueLoaded);
        if (savedInstanceState == null) {
            queueViewModel.loadQueue();
        }
    }

    /**
     * Builds the RN rows from the loaded queue and attaches the RN view. On an
     * empty list the view is still attached so the screen renders instead of
     * staying blank. Runs on the main thread (LiveData callback), but does no
     * DB/IO — just an in-memory map of already-joined rows into prop bundles.
     */
    private void onQueueLoaded(@Nullable List<QueueRow> rows) {
        Context ctx = getContext();
        if (ctx == null) {
            return;
        }
        Context appContext = ctx.getApplicationContext();
        ArrayList<Bundle> bundles = new ArrayList<>(rows != null ? rows.size() : 0);
        if (rows != null) {
            for (QueueRow row : rows) {
                bundles.add(toRowBundle(appContext, row));
            }
        }
        attachReactFragment(buildQueueProps(appContext, bundles));
    }

    /** Commit the RN fragment with the given initial properties. */
    private void attachReactFragment(Bundle initialProperties) {
        // The callback runs asynchronously, so the host may be gone (user
        // navigated away) or its state already saved — guard both.
        if (!isAdded() || isRemoving() || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (getChildFragmentManager().findFragmentById(R.id.patient_queue_container) != null) {
            return; // already attached
        }

        ReactFragment reactFragment = new ReactFragment.Builder()
                .setComponentName(RN_COMPONENT_NAME)
                .setLaunchOptions(initialProperties)
                .build();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.patient_queue_container, reactFragment)
                .commitAllowingStateLoss();
    }

    /**
     * Wraps the built RN rows under the "queue" key and seeds the status banner.
     */
    private Bundle buildQueueProps(@NonNull Context ctx, @NonNull ArrayList<Bundle> rows) {
        Bundle initialProperties = new Bundle();
        initialProperties.putParcelableArrayList("queue", rows);

        // Seed the status banner from the same persisted "queue_status" FCM
        // payload that drives the home banner (StatusBannerUpdater), so both
        // screens show identical queue status. Absent until a notification has
        // been received; the RN side falls back to the default banner then.
        StatusBannerData banner = StatusBannerUpdater.getPersisted(ctx);
        if (banner != null) {
            initialProperties.putBundle("banner", StatusBannerUpdater.toBundle(banner));
        }
        return initialProperties;
    }

    /**
     * One {@link QueueRow} -> one RN QueueListItem prop bundle. Patient display
     * fields (openmrs id, name, gender, date_of_birth) already come joined on
     * the row, so there is no separate lookup here.
     */
    private Bundle toRowBundle(@NonNull Context ctx, @NonNull QueueRow row) {
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
        String status = mapStatus(row.getStatus(), position);

        Bundle bundle = new Bundle();
        bundle.putString("queueNumber", openmrsId);
        bundle.putString("patientName", patientName);
        bundle.putString("gender", gender);
        bundle.putInt("age", age);
        bundle.putString("patientId", openmrsId);
        bundle.putStringArrayList("symptoms", parseSymptoms(row.getChiefComplaint()));
        bundle.putInt("position", position);
        bundle.putString("status", status);
        // Pre-formatted snapshot used as a fallback (and initial paint) when the
        // row has no timestamp to tick from.
        bundle.putString("time", formatTime(status, row));
        // Raw instants so the RN screen can tick the wait time / duration every
        // second on the JS side (no DB re-read, no bridge push). Empty when absent.
        bundle.putString("etaAt", orEmpty(row.getEtaAt()));
        bundle.putString("connectedAt", orEmpty(row.getConnectedAt()));
        // Patient profile pic. patient_photo holds an absolute local file path
        // (AppConstants.IMAGE_PATH + uuid + ".jpg"); RN's <Image> needs a URI
        // scheme, so wrap a bare path in file://. Empty when absent, so the RN
        // row falls back to its placeholder avatar.
        bundle.putString("avatarUrl", toAvatarUri(row.getPatientPhoto()));
        return bundle;
    }

    /**
     * Turns the stored patient photo value into a URI React Native's {@code Image}
     * can load. Local filesystem paths get a {@code file://} prefix; values that
     * already carry a scheme ({@code http}, {@code https}, {@code file},
     * {@code content}) are used as-is. Returns "" when there is no photo.
     */
    private String toAvatarUri(@Nullable String photo) {
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
    private String mapStatus(@Nullable String dbStatus, int position) {
        QueueStatus status = QueueStatus.fromValue(dbStatus);
        if (status == null) {
            return "";
        }
        switch (status) {
            case CONNECTED:
                return "onCall";
            case QUEUED:
                return position == 0 ? "nextInQueue" : "waiting";
            case RE_QUEUED:
                return position > 0 ? "waiting" : "";
            default:
                return "";
        }
    }

    /**
     * Chief-complaint string -> symptom tags. Uses the same parsing as the home
     * queue card and the visit summary screen ({@link QueueCardUpdater#extractComplaintNames(String)}),
     * so the list shows the complaint header names (e.g. "Fever", "Cough") rather
     * than a naive comma split of the raw blob.
     */
    private ArrayList<String> parseSymptoms(@Nullable String chiefComplaint) {
        return QueueCardUpdater.extractComplaintNames(chiefComplaint);
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
    private String formatTime(@Nullable String status, @NonNull QueueRow row) {
        if ("onCall".equals(status)) {
            // Elapsed call duration = now - connectedAt.
            String duration = elapsedSince(row.getConnectedAt());
            return duration != null ? duration : formatMinutes(row.getWaitedMinutes());
        }
        // Wait time for next/waiting = etaAt - now.
        String waitTime = remainingUntil(row.getEtaAt());
        return waitTime != null ? waitTime : formatMinutes(row.getEtaMinutes());
    }

    /**
     * Time remaining from now until a future ISO-8601 instant (server sends UTC,
     * e.g. {@code 2026-09-17T12:14:54.000Z}), "MM:SS", clamped to {@code >= 0}.
     * Returns null when the value is absent/unparseable so the caller can fall back.
     */
    @Nullable
    private String remainingUntil(@Nullable String isoInstant) {
        return formatMmSs(isoInstant, true);
    }

    /**
     * Time elapsed from a past ISO-8601 instant until now, "MM:SS", clamped to
     * {@code >= 0}. Returns null when the value is absent/unparseable.
     */
    @Nullable
    private String elapsedSince(@Nullable String isoInstant) {
        return formatMmSs(isoInstant, false);
    }

    /**
     * Formats the gap between {@code isoInstant} and now as "MM:SS", clamped to
     * {@code >= 0}. {@code future=true} counts instant-now (a future ETA),
     * {@code false} counts now-instant (a past connect time). Null on parse error.
     */
    @Nullable
    private String formatMmSs(@Nullable String isoInstant, boolean future) {
        if (TextUtils.isEmpty(isoInstant)) {
            return null;
        }
        try {
            long instantMillis = Instant.parse(isoInstant.trim()).toEpochMilli();
            long now = System.currentTimeMillis();
            long deltaMillis = future ? instantMillis - now : now - instantMillis;
            long totalSeconds = Math.max(0, deltaMillis / 1000L);
            long mm = totalSeconds / 60;
            long ss = totalSeconds % 60;
            return String.format(Locale.ENGLISH, "%02d:%02d", mm, ss);
        } catch (Exception e) {
            Log.e(TAG, "formatMmSs failed: " + e.getMessage());
            return null;
        }
    }

    /** Minutes -> "MM:00" to match the RN row's pre-formatted time string. */
    private String formatMinutes(int minutes) {
        int safe = Math.max(0, minutes);
        return String.format(Locale.ENGLISH, "%02d:00", safe);
    }

    private String orEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }
}
