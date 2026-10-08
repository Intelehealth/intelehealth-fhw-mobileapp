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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.klivekit.data.PreferenceHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bridges an incoming "Next In Queue" FCM notification to the React Native
 * {@code QueueCardModule} shown on the home screen.
 *
 * <p>A notification can carry several patients as a JSON array under
 * {@link #KEY_PATIENTS}; each element uses the same keys as a single-patient
 * payload. A payload without that key is treated as a single patient. The
 * home screen shows the list as a slideshow of cards.
 *
 * <p>Two things happen for every queue notification:
 * <ol>
 *   <li>The patient list is persisted to prefs so the home card reflects the
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

    /** FCM data key holding the JSON array of queue patients. */
    public static final String KEY_PATIENTS = "patients";

    /** Props / event key under which the patient list is passed to JS. */
    private static final String PROP_PATIENTS = "patients";

    private QueueCardUpdater() {
    }

    /**
     * Parse a "Next In Queue" FCM data payload into its patient list, persist
     * it (replacing the previous list), and push it to the live card if the RN
     * context is running.
     */
    public static void handleQueueNotification(Context context, Map<String, String> data) {
        if (context == null || data == null) {
            return;
        }
        ArrayList<PatientData> patients = new ArrayList<>();
        for (Map<String, String> item : splitPatients(data)) {
            patients.add(parse(context, item));
        }
        persist(context, patients);
        emit(patients);
    }

    /**
     * The most recently persisted queue patient list, or an empty list if no
     * queue notification has been received yet. Used by the home fragment to
     * seed the slideshow on mount.
     */
    public static List<PatientData> getPersisted(Context context) {
        try {
            String json = new PreferenceHelper(context).getString(PreferenceHelper.QUEUE_CARD_DATA);
            if (TextUtils.isEmpty(json)) {
                return new ArrayList<>();
            }
            Gson gson = new Gson();
            // Builds before the slideshow persisted a single patient object.
            if (json.trim().startsWith("{")) {
                ArrayList<PatientData> legacy = new ArrayList<>();
                legacy.add(gson.fromJson(json, PatientData.class));
                return legacy;
            }
            List<PatientData> patients = gson.fromJson(json,
                    new TypeToken<ArrayList<PatientData>>() {}.getType());
            return patients != null ? patients : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "getPersisted failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Split the FCM payload into one string map per patient. When
     * {@link #KEY_PATIENTS} holds a JSON array, each element becomes a map
     * (arrays such as {@code symptoms} are joined with commas, matching the
     * single-patient format); otherwise the whole payload is one patient.
     */
    private static List<Map<String, String>> splitPatients(Map<String, String> data) {
        List<Map<String, String>> items = new ArrayList<>();
        String raw = data.get(KEY_PATIENTS);
        if (TextUtils.isEmpty(raw)) {
            items.add(data);
            return items;
        }
        try {
            JsonArray array = JsonParser.parseString(raw).getAsJsonArray();
            for (JsonElement element : array) {
                if (element != null && element.isJsonObject()) {
                    items.add(toStringMap(element.getAsJsonObject()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "patients parse failed: " + e.getMessage());
        }
        return items;
    }

    private static Map<String, String> toStringMap(JsonObject object) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value == null || value.isJsonNull()) {
                continue;
            }
            if (value.isJsonPrimitive()) {
                map.put(entry.getKey(), value.getAsString());
            } else if (value.isJsonArray()) {
                ArrayList<String> parts = new ArrayList<>();
                for (JsonElement part : value.getAsJsonArray()) {
                    if (part != null && part.isJsonPrimitive()) {
                        parts.add(part.getAsString());
                    }
                }
                map.put(entry.getKey(), TextUtils.join(",", parts));
            } else {
                map.put(entry.getKey(), value.toString());
            }
        }
        return map;
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

    private static void persist(Context context, List<PatientData> patients) {
        try {
            new PreferenceHelper(context)
                    .save(PreferenceHelper.QUEUE_CARD_DATA, new Gson().toJson(patients));
        } catch (Exception e) {
            Log.e(TAG, "persist failed: " + e.getMessage());
        }
    }

    /**
     * Emit the patient list to JS. A no-op when RN isn't running (app in
     * background); the persisted copy is picked up when the card next mounts.
     */
    private static void emit(List<PatientData> patients) {
        WritableArray array = Arguments.createArray();
        for (PatientData patient : patients) {
            array.pushMap(toWritableMap(patient));
        }
        WritableMap map = Arguments.createMap();
        map.putArray(PROP_PATIENTS, array);
        RnEventEmitter.emit(EVENT_QUEUE_CARD_UPDATE, map);
    }

    /** Build the JS map for one card, mirroring the QueuePatientData shape. */
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
     * that hosts the slideshow: the patient list under {@code patients}, each
     * entry mirroring {@link #toWritableMap(PatientData)}.
     */
    public static Bundle toBundle(List<PatientData> patients) {
        ArrayList<Bundle> items = new ArrayList<>();
        for (PatientData patient : patients) {
            items.add(toBundle(patient));
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PROP_PATIENTS, items);
        return bundle;
    }

    private static Bundle toBundle(PatientData patient) {
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
        if (TextUtils.isEmpty(visitUuid)) {
            return new ArrayList<>();
        }
        try {
            return extractComplaintNames(EncounterDAO.getChiefComplaint(visitUuid));
        } catch (Exception e) {
            Log.e(TAG, "fetchSymptoms failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Extracts just the chief-complaint header names from the raw complaint blob
     * (e.g. {@code ►<b>Fever</b>: …►<b>Cough</b>: …}), mirroring how the visit
     * summary screen renders its complaint chips: split on {@link Node#bullet_arrow},
     * take the text before the first {@code :}, strip the {@code <b>}/{@code </b>}
     * markup and the associated-symptoms header, and de-dupe. Returns an empty
     * list for null/blank input.
     *
     * <p>Shared so the patient queue list ({@code PatientQueueFragment}) shows the
     * same symptom tags as the home queue card and the visit summary, instead of
     * a naive comma split.
     */
    public static ArrayList<String> extractComplaintNames(@Nullable String raw) {
        ArrayList<String> symptoms = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) {
            return symptoms;
        }
        String normalized = raw.replace("?<b>", Node.bullet_arrow);
        for (String part : StringUtils.split(normalized, Node.bullet_arrow)) {
            if (part == null || isAssociatedSymptomsChunk(part)) {
                continue;
            }
            int colon = part.indexOf(':');
            String name = (colon >= 0 ? part.substring(0, colon) : part)
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .trim();
            if (!name.isEmpty() && !symptoms.contains(name)) {
                symptoms.add(name);
            }
        }
        return symptoms;
    }

    /**
     * True for the associated-symptoms block of the complaint blob, which must
     * not be shown as a chief-complaint tag. Matches both formats the visit
     * summary screen handles: the HTML "Associated symptoms" header and the
     * question/answer "Do you have the following symptom(s)?" / "Patient
     * denies -" form, in each supported language.
     */
    private static boolean isAssociatedSymptomsChunk(String part) {
        String text = part.replaceAll("<.*?>", "").trim().toLowerCase();
        if (text.startsWith(Node.ASSOCIATE_SYMPTOMS.toLowerCase())
                || text.contains("patient reports -")) {
            return true;
        }
        for (String lang : ASSOCIATED_SYMPTOM_LANGS) {
            if (text.contains(VisitUtils.getTranslatedAssociatedSymptomQString(lang).toLowerCase())
                    || text.contains(VisitUtils.getTranslatedPatientDenies(lang).toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static final String[] ASSOCIATED_SYMPTOM_LANGS = {"en", "hi", "or"};

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
