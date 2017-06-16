package io.intelehealth.client.objects;

/**
 * Image data class for Gson data serialization
 */
public class PatientImage {
    private int patientId;
    private String encodedString;

    public PatientImage (int id, String string) {
        this.patientId = id;
        this.encodedString = string;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getEncodedString() {
        return encodedString;
    }

    public void setEncodedString(String encodedString) {
        this.encodedString = encodedString;
    }
}
