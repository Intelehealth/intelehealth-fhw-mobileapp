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

    // Constructor
    public PatientData(String queueNumber, String patientName, String gender, int age,
                       String patientId, ArrayList<String> symptoms, int position,
                       int waitTimeMinutes, String avatarUrl) {
        this.queueNumber = queueNumber;
        this.patientName = patientName;
        this.gender = gender;
        this.age = age;
        this.patientId = patientId;
        this.symptoms = symptoms;
        this.position = position;
        this.waitTimeMinutes = waitTimeMinutes;
        this.avatarUrl = avatarUrl;
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
}
