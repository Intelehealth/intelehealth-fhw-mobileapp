package org.intelehealth.app.reactnative;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;

import org.intelehealth.klivekit.data.PreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Bridges an incoming "Next In Queue" FCM notification to the React Native
 * {@code QueueCardModule} shown on the home screen.
 *
 * <p>Two things happen for every queue notification:
 * <ol>
 *   <li>The card payload is persisted to prefs so the home card reflects the
 *       latest queue state the next time it mounts (covers notifications that
 *       arrive while the app is backgrounded and the RN view isn't mounted).</li>
 *   <li>If the React context is already alive (app in foreground), a
 *       {@code QueueCardUpdate} device event is emitted so the mounted card
 *       updates live without a remount.</li>
 * </ol>
 *
 * @see FCMNotificationReceiver
 * @see #EVENT_QUEUE_CARD_UPDATE
 */
public final class QueueCardUpdater {

    private static final String TAG = "QueueCardUpdater";

    /** JS event name the {@code QueueCard} component subscribes to. */
    public static final String EVENT_QUEUE_CARD_UPDATE = "QueueCardUpdate";

    private QueueCardUpdater() {
    }

    /**
     * Parse a "Next In Queue" FCM data payload, persist it, and push it to the
     * live card if the RN context is running.
     */
    public static void handleQueueNotification(Context context, Map<String, String> data) {
        if (context == null || data == null) {
            return;
        }
        PatientData patient = parse(data);
        persist(context, patient);
        emit(patient);
    }

    /**
     * The most recently persisted queue card payload, or {@code null} if no
     * queue notification has been received yet. Used by the home fragment to
     * seed the card on mount.
     */
    @Nullable
    public static PatientData getPersisted(Context context) {
        try {
            String json = new PreferenceHelper(context).getString(PreferenceHelper.QUEUE_CARD_DATA);
            if (TextUtils.isEmpty(json)) {
                return null;
            }
            return new Gson().fromJson(json, PatientData.class);
        } catch (Exception e) {
            Log.e(TAG, "getPersisted failed: " + e.getMessage());
            return null;
        }
    }

    /** Map the FCM string payload onto the card's typed fields. */
    private static PatientData parse(Map<String, String> data) {
        String patientName = data.get("patientName");
        if (TextUtils.isEmpty(patientName)) {
            patientName = join(data.get("patientFirstName"), data.get("patientLastName"));
        }

        String patientId = firstNonEmpty(data.get("patientId"), data.get("patientOpenMrsId"));

        ArrayList<String> symptoms = new ArrayList<>();
        String symptomsRaw = data.get("symptoms");
        if (!TextUtils.isEmpty(symptomsRaw)) {
            for (String s : symptomsRaw.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    symptoms.add(trimmed);
                }
            }
        }

        return new PatientData(
                emptyToNull(data.get("queueNumber")),
                patientName,
                emptyToNull(data.get("gender")),
                parseInt(data.get("age")),
                patientId,
                symptoms,
                parseInt(data.get("position")),
                parseInt(data.get("waitTimeMinutes")),
                emptyToNull(data.get("avatarUrl"))
        );
    }

    private static void persist(Context context, PatientData patient) {
        try {
            new PreferenceHelper(context)
                    .save(PreferenceHelper.QUEUE_CARD_DATA, new Gson().toJson(patient));
        } catch (Exception e) {
            Log.e(TAG, "persist failed: " + e.getMessage());
        }
    }

    /**
     * Emit the card update to JS. A no-op when RN isn't running (app in
     * background); the persisted copy is picked up when the card next mounts.
     */
    private static void emit(PatientData patient) {
        RnEventEmitter.emit(EVENT_QUEUE_CARD_UPDATE, toWritableMap(patient));
    }

    /** Build the JS props map that mirrors the QueueCardProps shape. */
    private static WritableMap toWritableMap(PatientData patient) {
        WritableMap map = Arguments.createMap();
        map.putString("queueNumber", patient.getQueueNumber());
        map.putString("patientName", patient.getPatientName());
        map.putString("gender", patient.getGender());
        map.putInt("age", patient.getAge());
        map.putString("patientId", patient.getPatientId());
        map.putInt("position", patient.getPosition());
        map.putInt("waitTimeMinutes", patient.getWaitTimeMinutes());
        map.putString("avatarUrl", patient.getAvatarUrl());

        WritableArray symptoms = Arguments.createArray();
        if (patient.getSymptoms() != null) {
            for (String symptom : patient.getSymptoms()) {
                symptoms.pushString(symptom);
            }
        }
        map.putArray("symptoms", symptoms);
        return map;
    }

    /**
     * Build the initial-properties {@link Bundle} for the {@code ReactFragment}
     * that hosts the card, mirroring {@link #toWritableMap(PatientData)}.
     */
    public static Bundle toBundle(PatientData patient) {
        Bundle bundle = new Bundle();
        bundle.putString("queueNumber", patient.getQueueNumber());
        bundle.putString("patientName", patient.getPatientName());
        bundle.putString("gender", patient.getGender());
        bundle.putInt("age", patient.getAge());
        bundle.putString("patientId", patient.getPatientId());
        bundle.putStringArrayList("symptoms", patient.getSymptoms());
        bundle.putInt("position", patient.getPosition());
        bundle.putInt("waitTimeMinutes", patient.getWaitTimeMinutes());
        bundle.putString("avatarUrl", patient.getAvatarUrl());
        return bundle;
    }

    private static int parseInt(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String join(@Nullable String first, @Nullable String last) {
        return TextUtils.join(" ", new ArrayList<>(Arrays.asList(
                firstNonEmpty(first, ""), firstNonEmpty(last, ""))))
                .trim();
    }

    @Nullable
    private static String firstNonEmpty(@Nullable String a, @Nullable String b) {
        return !TextUtils.isEmpty(a) ? a : b;
    }

    @Nullable
    private static String emptyToNull(@Nullable String value) {
        return TextUtils.isEmpty(value) ? null : value;
    }
}
