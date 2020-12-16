package app.intelehealth.client.models;

public class VitalsObject {

    private String patientUuid;
    private String height;
    private String weight;
    private String bmi;
    private String bpsys;
    private String bpdia;
    private String pulse;
    private String temperature;
    private String spo2;
    private String resp;

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
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

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
    }
}
