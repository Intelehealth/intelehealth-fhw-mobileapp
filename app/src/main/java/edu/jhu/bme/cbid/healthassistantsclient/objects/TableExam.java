package edu.jhu.bme.cbid.healthassistantsclient.objects;

/**
 * Table exam information class for Gson data serialization
 */
public class TableExam {
    private int patientId;
    private double height;
    private double weight;
    private double bmi;
    private double bpsys;
    private double bpdia;
    private double temperature;
    private double spo2;


    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getBpsys() {
        return bpsys;
    }

    public void setBpsys(double bpsys) {
        this.bpsys = bpsys;
    }

    public double getBpdia() {
        return bpdia;
    }

    public void setBpdia(double bpdia) {
        this.bpdia = bpdia;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getSpo2() {
        return spo2;
    }

    public void setSpo2(double spo2) {
        this.spo2 = spo2;
    }
}
