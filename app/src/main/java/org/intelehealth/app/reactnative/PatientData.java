package org.intelehealth.app.reactnative;

import java.util.ArrayList;

public class PatientData {
    private String queueNumber;
    private String patientName;
    private String gender;
    private int age;
    private String patientId;
    private ArrayList<String> symptoms;
    private int position;
    private int waitTimeMinutes;
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
