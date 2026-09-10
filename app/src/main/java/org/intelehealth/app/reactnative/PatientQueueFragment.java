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

import java.util.ArrayList;
import java.util.List;

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
        // "onCall" shows elapsed duration (waited), everyone else shows wait ETA.
        int minutes = "onCall".equals(status) ? row.getWaitedMinutes() : row.getEtaMinutes();
        bundle.putString("time", formatMinutes(minutes));
        return bundle;
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

    /** Split the chief-complaint string into symptom tags. */
    private ArrayList<String> parseSymptoms(@Nullable String chiefComplaint) {
        ArrayList<String> symptoms = new ArrayList<>();
        if (TextUtils.isEmpty(chiefComplaint)) {
            return symptoms;
        }
        for (String part : chiefComplaint.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                symptoms.add(trimmed);
            }
        }
        return symptoms;
    }

    /** Minutes -> "MM:00" to match the RN row's pre-formatted time string. */
    private String formatMinutes(int minutes) {
        int safe = Math.max(0, minutes);
        return String.format(java.util.Locale.ENGLISH, "%02d:00", safe);
    }

    private String orEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }
}
