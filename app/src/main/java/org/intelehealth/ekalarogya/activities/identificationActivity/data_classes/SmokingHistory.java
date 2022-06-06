package org.intelehealth.ekalarogya.activities.identificationActivity.data_classes;

public class SmokingHistory {
    private String smokingStatus = "-";
    private String rateOfSmoking = "-";
    private String durationOfSmoking = "-";
    private String frequencyOfSmoking = "-";

    public String getSmokingStatus() {
        return smokingStatus;
    }

    public void setSmokingStatus(String smokingStatus) {
        this.smokingStatus = smokingStatus;
    }

    public String getRateOfSmoking() {
        return rateOfSmoking;
    }

    public void setRateOfSmoking(String rateOfSmoking) {
        this.rateOfSmoking = rateOfSmoking;
    }

    public String getDurationOfSmoking() {
        return durationOfSmoking;
    }

    public void setDurationOfSmoking(String durationOfSmoking) {
        this.durationOfSmoking = durationOfSmoking;
    }

    public String getFrequencyOfSmoking() {
        return frequencyOfSmoking;
    }

    public void setFrequencyOfSmoking(String frequencyOfSmoking) {
        this.frequencyOfSmoking = frequencyOfSmoking;
    }
}