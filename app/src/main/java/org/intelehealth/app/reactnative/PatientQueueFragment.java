package org.intelehealth.app.reactnative;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.react.ReactFragment;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.QueueModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Native host for the React Native "Patient's Queue" screen.
 *
 * Embeds the RN component registered as "PatientQueueModule"
 * (see react-native/index.js) via a {@link ReactFragment}, reusing the single
 * ReactHost initialized in IntelehealthApplication. Loaded into the bottom-nav
 * container of HomeScreenActivity_New when the Queue tab is selected.
 *
 * TEMPORARY: pulls visit rows from {@link VisitsDAO#queueVisits(int, int)} and
 * passes them as the RN {@code queue} prop so the list shows real data. Swap
 * for the proper queue feed later.
 */
public class PatientQueueFragment extends Fragment {

    public static final String TAG = "TAG_PATIENT_QUEUE";

    private static final String RN_COMPONENT_NAME = "PatientQueueModule";

    // Temporary paging window for the duplicated older-visits query.
    private static final int QUEUE_LIMIT = 50;
    private static final int QUEUE_OFFSET = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Only attach once; on re-creation the child FragmentManager restores it.
        if (savedInstanceState == null) {
            ReactFragment reactFragment = new ReactFragment.Builder()
                    .setComponentName(RN_COMPONENT_NAME)
                    .setLaunchOptions(buildQueueProps())
                    .build();

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.patient_queue_container, reactFragment)
                    .commit();
        }
    }

    /**
     * Fetches the queue rows and maps each {@link QueueModel} into the prop shape
     * the RN {@code QueueListItem} expects. The list is passed under the "queue"
     * key as an array of objects (ArrayList of Bundles).
     */
    private Bundle buildQueueProps() {
        List<QueueModel> queue = VisitsDAO.queueVisits(QUEUE_LIMIT, QUEUE_OFFSET);

        ArrayList<Bundle> items = new ArrayList<>();
        int position = 1;
        for (QueueModel model : queue) {
            Bundle item = new Bundle();
            item.putString("queueNumber", model.getOpenmrsId());
            item.putString("patientName", model.getFullName());
            item.putString("gender", model.getGender());
            item.putInt("age", DateAndTimeUtils.getAge(model.getDob(), requireContext()));
            item.putString("patientId", model.getOpenmrsId());
            item.putStringArrayList("symptoms",
                    new ArrayList<>(Arrays.asList("Abdominal Pain", "Nausea", "Fever"))); // temp hardcoded
            item.putInt("position", position++);
            item.putString("status", "waiting"); // temp default until status is derived
            item.putString("time", "2:39"); // temp hardcoded until wait time is computed
            item.putString("avatarUrl", model.getPatientPhoto());
            items.add(item);
        }

        Bundle initialProperties = new Bundle();
        initialProperties.putParcelableArrayList("queue", items);
        return initialProperties;
    }
}
