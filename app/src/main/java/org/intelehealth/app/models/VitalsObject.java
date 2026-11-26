package org.intelehealth.app.models;

import org.intelehealth.app.ayu.visit.ncd.BpReading;
import org.intelehealth.app.ayu.visit.ncd.BpScreeningRules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VitalsObject implements Serializable {

    private String patientUuid;
    private String height;
    private String weight;
    private String bmi;
    private String bpsys;
    private String bpdia;
    // Readings 2 and 3 of an NCD screening visit; bpsys/bpdia hold reading 1
    private String bpsys2;
    private String bpdia2;
    private String bpsys3;
    private String bpdia3;
    private String pulse;
    private String temperature;
    private String spo2;
    private String resp;
    private String bloodGroup;

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

    public String getBpsys2() {
        return bpsys2;
    }

    public void setBpsys2(String bpsys2) {
        this.bpsys2 = bpsys2;
    }

    public String getBpdia2() {
        return bpdia2;
    }

    public void setBpdia2(String bpdia2) {
        this.bpdia2 = bpdia2;
    }

    public String getBpsys3() {
        return bpsys3;
    }

    public void setBpsys3(String bpsys3) {
        this.bpsys3 = bpsys3;
    }

    public String getBpdia3() {
        return bpdia3;
    }

    public void setBpdia3(String bpdia3) {
        this.bpdia3 = bpdia3;
    }

    // Entered BP readings in order
    public List<BpReading> getBpReadings() {
        List<BpReading> readings = new ArrayList<>();
        addIfEntered(readings, BpReading.of(1, bpsys, bpdia));
        addIfEntered(readings, BpReading.of(2, bpsys2, bpdia2));
        addIfEntered(readings, BpReading.of(3, bpsys3, bpdia3));
        return readings;
    }

    // The last reading taken, which is the final BP; null without BP
    public BpReading getFinalBp() {
        return BpScreeningRules.lastReading(getBpReadings());
    }

    private static void addIfEntered(List<BpReading> readings, BpReading reading) {
        if (reading != null) readings.add(reading);
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

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
}
