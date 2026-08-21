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

import org.apache.commons.lang3.StringUtils;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.klivekit.data.PreferenceHelper;

import java.time.Instant;
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
        PatientData patient = parse(context, data);
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
    private static PatientData parse(Context context, Map<String, String> data) {
        String patientName = data.get("patientName");
        if (TextUtils.isEmpty(patientName)) {
            patientName = join(data.get("patientFirstName"), data.get("patientLastName"));
        }

        String patientId = firstNonEmpty(data.get("patientId"), data.get("patientOpenMrsId"));
        String gender = data.get("gender");
        int age = parseInt(data.get("age"));

        // A "Queue update" notification identifies the patient only by UUID, so
        // look the display fields (name, id, gender, age) up from the local DB
        // for any the payload didn't already carry.
        String patientUuid = data.get("patientUuid");
        if (!TextUtils.isEmpty(patientUuid) && (TextUtils.isEmpty(patientName)
                || TextUtils.isEmpty(patientId) || TextUtils.isEmpty(gender) || age == 0)) {
            try {
                Map<String, String> details = new PatientsDAO().getQueueCardPatientDetails(patientUuid);
                if (!details.isEmpty()) {
                    if (TextUtils.isEmpty(patientName)) {
                        patientName = join(details.get("first_name"), details.get("last_name"));
                    }
                    if (TextUtils.isEmpty(patientId)) {
                        patientId = emptyToNull(details.get("openmrs_id"));
                    }
                    if (TextUtils.isEmpty(gender)) {
                        gender = details.get("gender");
                    }
                    if (age == 0) {
                        age = DateAndTimeUtils.getAge(details.get("date_of_birth"), context);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "patient lookup failed: " + e.getMessage());
            }
        }

        // The "Queue update" payload carries an ISO-8601 ETA (etaTime) instead
        // of a plain minute count; fall back to it when waitTimeMinutes is absent.
        int waitTimeMinutes = data.containsKey("waitTimeMinutes")
                ? parseInt(data.get("waitTimeMinutes"))
                : minutesUntil(data.get("etaTime"));

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

        // A "Queue update" notification carries no symptom list, only the
        // visitUuid. The symptoms shown on the card are that visit's chief
        // complaint, stored as an OBS; pull and parse them from the local DB.
        if (symptoms.isEmpty()) {
            symptoms = fetchSymptoms(data.get("visitUuid"));
        }

        return new PatientData(
                emptyToNull(data.get("queueNumber")),
                emptyToNull(patientName),
                emptyToNull(gender),
                age,
                patientId,
                symptoms,
                parseInt(data.get("position")),
                waitTimeMinutes,
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

    /**
     * The symptom tags for the card: the chief complaint recorded against the
     * given visit. {@link EncounterDAO#getChiefComplaint(String)} returns the
     * raw complaint blob (e.g. {@code Fever:...►Cough:...}); this extracts just
     * the complaint names, mirroring how the visit details screen renders them.
     * Returns an empty list when the visit or complaint isn't available locally.
     */
    private static ArrayList<String> fetchSymptoms(@Nullable String visitUuid) {
        ArrayList<String> symptoms = new ArrayList<>();
        if (TextUtils.isEmpty(visitUuid)) {
            return symptoms;
        }
        try {
            String raw = EncounterDAO.getChiefComplaint(visitUuid);
            if (TextUtils.isEmpty(raw)) {
                return symptoms;
            }
            raw = raw.replace("?<b>", Node.bullet_arrow);
            for (String part : StringUtils.split(raw, Node.bullet_arrow)) {
                if (part == null) {
                    continue;
                }
                int colon = part.indexOf(':');
                String name = (colon >= 0 ? part.substring(0, colon) : part)
                        .replaceAll("<b>", "")
                        .replaceAll("</b>", "")
                        .replaceAll(Node.ASSOCIATE_SYMPTOMS, "")
                        .trim();
                if (!name.isEmpty() && !symptoms.contains(name)) {
                    symptoms.add(name);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchSymptoms failed: " + e.getMessage());
        }
        return symptoms;
    }

    /**
     * Minutes from now until an ISO-8601 instant — the {@code etaTime} the
     * server sends via {@code Date.toISOString()} (always UTC, e.g.
     * {@code 2026-08-21T12:30:00.000Z}). Clamped to {@code >= 0}; returns 0 when
     * absent or unparseable.
     */
    private static int minutesUntil(@Nullable String isoTime) {
        if (TextUtils.isEmpty(isoTime)) {
            return 0;
        }
        try {
            long deltaMillis = Instant.parse(isoTime.trim()).toEpochMilli()
                    - System.currentTimeMillis();
            long minutes = Math.round(deltaMillis / 60000d);
            return (int) Math.max(0, minutes);
        } catch (Exception e) {
            Log.e(TAG, "minutesUntil failed: " + e.getMessage());
            return 0;
        }
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
