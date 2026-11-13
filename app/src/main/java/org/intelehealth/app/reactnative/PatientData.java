package org.intelehealth.app.reactnative;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * "Next In Queue" card payload shared between the native host and the React
 * Native {@code QueueCardModule}, and persisted as JSON (see
 * {@link QueueCardUpdater}).
 *
 * <p>{@link Keep} and the explicit {@link SerializedName} names pin the JSON
 * keys so the persisted format survives R8 field renaming in release builds and
 * stays stable across app updates — otherwise a payload written by one build
 * could fail to map when read back by the next.
 */
@Keep
public class PatientData {
    @SerializedName("queueNumber")
    private String queueNumber;
    @SerializedName("patientName")
    private String patientName;
    @SerializedName("gender")
    private String gender;
    @SerializedName("age")
    private int age;
    @SerializedName("patientId")
    private String patientId;
    @SerializedName("symptoms")
    private ArrayList<String> symptoms;
    @SerializedName("position")
    private int position;
    @SerializedName("waitTimeMinutes")
    private int waitTimeMinutes;
    @SerializedName("avatarUrl")
    private String avatarUrl;
    // Queue the patient is waiting in; one card is kept per speciality.
    @SerializedName("speciality")
    private String speciality;
    @SerializedName("visitUuid")
    private String visitUuid;
    // ISO-8601 instant the patient is expected to be seen (server "etaAt"), the
    // same absolute instant the queue list uses to count the live wait time down.
    // Carried so the home card and the queue listing show the same value instead
    // of a static minutes snapshot.
    @SerializedName("etaAt")
    private String etaAt;

    // Constructor
    public PatientData(String queueNumber, String patientName, String gender, int age,
                       String patientId, ArrayList<String> symptoms, int position,
                       int waitTimeMinutes, String avatarUrl) {
        this(queueNumber, patientName, gender, age, patientId, symptoms, position,
                waitTimeMinutes, avatarUrl, null, null, null);
    }

    public PatientData(String queueNumber, String patientName, String gender, int age,
                       String patientId, ArrayList<String> symptoms, int position,
                       int waitTimeMinutes, String avatarUrl, String speciality,
                       String visitUuid) {
        this(queueNumber, patientName, gender, age, patientId, symptoms, position,
                waitTimeMinutes, avatarUrl, speciality, visitUuid, null);
    }

    public PatientData(String queueNumber, String patientName, String gender, int age,
                       String patientId, ArrayList<String> symptoms, int position,
                       int waitTimeMinutes, String avatarUrl, String speciality,
                       String visitUuid, String etaAt) {
        this.queueNumber = queueNumber;
        this.patientName = patientName;
        this.gender = gender;
        this.age = age;
        this.patientId = patientId;
        this.symptoms = symptoms;
        this.position = position;
        this.waitTimeMinutes = waitTimeMinutes;
        this.avatarUrl = avatarUrl;
        this.speciality = speciality;
        this.visitUuid = visitUuid;
        this.etaAt = etaAt;
    }

    // Getters
    public String getQueueNumber() { return queueNumber; }
    public String getPatientName() { return patientName; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getPatientId() { return patientId; }
    public ArrayList<String> getSymptoms() { return symptoms; }
    public int getPosition() { return position; }
    public int getWaitTimeMinutes() { return waitTimeMinutes; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getSpeciality() { return speciality; }
    public String getVisitUuid() { return visitUuid; }
    public String getEtaAt() { return etaAt; }
}
