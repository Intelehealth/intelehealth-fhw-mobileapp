package org.intelehealth.app.reactnative;

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
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.QueueListDAO;
import org.intelehealth.app.models.queue.QueueItem;
import org.intelehealth.app.models.queue.QueueListData;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Native host for the React Native "Patient's Queue" screen.
 *
 * Embeds the RN component registered as "PatientQueueModule"
 * (see react-native/index.js) via a {@link ReactFragment}, reusing the single
 * ReactHost initialized in IntelehealthApplication. Loaded into the bottom-nav
 * container of HomeScreenActivity_New when the Queue tab is selected.
 *
 * The queue rows come from the queue microservice via
 * {@link QueueListDAO#fetchQueueList}. Because that call is asynchronous, the
 * RN view is only attached once the response arrives (or fails), with the
 * mapped rows supplied as the RN {@code queue} prop. The API row carries only
 * uuids, so patient display fields (name, gender, age, id) are looked up from
 * the local DB by {@code patientUuid}.
 */
public class PatientQueueFragment extends Fragment {

    public static final String TAG = "TAG_PATIENT_QUEUE";

    private static final String RN_COMPONENT_NAME = "PatientQueueModule";

    // Queue list request parameters. Kept here until they are made dynamic
    // (e.g. driven by the tab the user selects on the RN screen).
    private static final String QUEUE_STATUS = "WAITING";
    private static final String QUEUE_SORT = "priority";
    private static final boolean QUEUE_INCLUDE_ETA = true;
    private static final boolean QUEUE_INCLUDE_SCORE = false;
    private static final int QUEUE_LIMIT = 50;
    private static final int QUEUE_OFFSET = 0;

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

        // Only attach once; on re-creation the child FragmentManager restores it.
        if (savedInstanceState == null) {
            loadQueueAndAttach();
        }
    }

    /**
     * Fetches the queue from the server, then attaches the RN view seeded with
     * the mapped rows. On failure the RN view is still attached (with an empty
     * list) so the screen renders instead of staying blank.
     */
    private void loadQueueAndAttach() {
        if (getContext() == null) {
            return;
        }
        QueueListDAO.fetchQueueList(requireContext().getApplicationContext(),
                QUEUE_STATUS, QUEUE_SORT, QUEUE_INCLUDE_ETA, QUEUE_INCLUDE_SCORE,
                QUEUE_LIMIT, QUEUE_OFFSET,
                new QueueListDAO.QueueListCallback() {
                    @Override
                    public void onSuccess(@NonNull QueueListData data) {
                        attachReactFragment(buildQueueProps(data.getItems()));
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "queue list load failed: " + message);
                        attachReactFragment(buildQueueProps(new ArrayList<>()));
                    }
                });
    }

    /** Commit the RN fragment with the given initial properties. */
    private void attachReactFragment(Bundle initialProperties) {
        // The callback runs after the network round-trip, so the host may be
        // gone (user navigated away) or its state already saved — guard both.
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
     * Maps the API {@link QueueItem}s into the prop shape the RN
     * {@code QueueListItem} expects, under the "queue" key (an ArrayList of
     * Bundles). Patient display fields are resolved from the local DB.
     */
    private Bundle buildQueueProps(@Nullable List<QueueItem> items) {
        ArrayList<Bundle> rows = new ArrayList<>();
        if (items != null) {
            for (QueueItem item : items) {
                rows.add(toRowBundle(item));
            }
        }
        Bundle initialProperties = new Bundle();
        initialProperties.putParcelableArrayList("queue", rows);
        return initialProperties;
    }

    /** One API row -> one RN QueueListItem prop bundle. */
    private Bundle toRowBundle(QueueItem item) {
        // Look up patient display fields (name, gender, age, openmrs id) that the
        // queue API does not carry, keyed by patientUuid — same source the queue
        // card uses (PatientsDAO#getQueueCardPatientDetails).
        String openmrsId = "";
        String patientName = "";
        String gender = "";
        int age = 0;
        try {
            Map<String, String> details =
                    new PatientsDAO().getQueueCardPatientDetails(item.getPatientUuid());
            if (details != null && !details.isEmpty()) {
                openmrsId = orEmpty(details.get("openmrs_id"));
                patientName = join(details.get("first_name"), details.get("last_name"));
                gender = orEmpty(details.get("gender"));
                // Guard the context: this runs in the network callback, by which
                // point the fragment may have detached.
                android.content.Context ctx = getContext();
                if (ctx != null) {
                    age = DateAndTimeUtils.getAge(details.get("date_of_birth"), ctx);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "patient lookup failed for " + item.getPatientUuid() + ": " + e.getMessage());
        }

        Bundle row = new Bundle();
        row.putString("queueNumber", openmrsId);
        row.putString("patientName", patientName);
        row.putString("gender", gender);
        row.putInt("age", age);
        row.putString("patientId", openmrsId);
        row.putStringArrayList("symptoms", parseSymptoms(item.getChiefComplaint()));
        row.putInt("position", item.getPosition());
        row.putString("status", mapStatus(item));
        // "onCall" shows elapsed duration (waited), everyone else shows wait ETA.
        int minutes = isOnCall(item) ? item.getWaitedMinutes() : item.getEtaMinutes();
        row.putString("time", formatMinutes(minutes));
        return row;
    }

    /**
     * Maps the queue row to the RN status union ('onCall' | 'nextInQueue' |
     * 'waiting'). Derived from queue position as a placeholder until the server
     * status enum is finalised.
     */
    private String mapStatus(QueueItem item) {
        if (isOnCall(item)) {
            return "onCall";
        }
        if (item.getPosition() == 2) {
            return "nextInQueue";
        }
        return "waiting";
    }

    private boolean isOnCall(QueueItem item) {
        return item.getPosition() <= 1;
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

    private String join(@Nullable String first, @Nullable String last) {
        return (orEmpty(first) + " " + orEmpty(last)).trim();
    }

    private String orEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }
}
