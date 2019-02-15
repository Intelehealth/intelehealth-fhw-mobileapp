package io.intelehealth.client.objects;

import io.intelehealth.client.activities.vitals_activity.VitalsActivity;

/**
 * Container for patient vitals {@link VitalsActivity}
 * <p>
 * Table exam information class for Gson data serialization
 */
public class TableExam {
    private int patientId;
    private String height;
    private String weight;
    private String bmi;
    private String bpsys;
    private String bpdia;
    private String pulse;
    private String temperature;
    private String spo2;


    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String bmi) {
        this.bmi = bmi;
    }

    public String getBpsys() {
        return bpsys;
    }

    public void setBpsys(String bpsys) {
        this.bpsys = bpsys;
    }

    public String getBpdia() {
        return bpdia;
    }

    public void setBpdia(String bpdia) {
        this.bpdia = bpdia;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getSpo2() {
        return spo2;
    }

    public void setSpo2(String spo2) {
        this.spo2 = spo2;
    }
}
