package org.intelehealth.ekalarogya.activities.identificationActivity.data_classes;

public class AlcoholConsumptionHistory {
    private String historyOfAlcoholConsumption = "-";
    private String rateOfAlcoholConsumption = "-";
    private String durationOfAlcoholConsumption = "-";

    public String getHistoryOfAlcoholConsumption() {
        return historyOfAlcoholConsumption;
    }

    public void setHistoryOfAlcoholConsumption(String historyOfAlcoholConsumption) {
        this.historyOfAlcoholConsumption = historyOfAlcoholConsumption;
    }

    public String getRateOfAlcoholConsumption() {
        return rateOfAlcoholConsumption;
    }

    public void setRateOfAlcoholConsumption(String rateOfAlcoholConsumption) {
        this.rateOfAlcoholConsumption = rateOfAlcoholConsumption;
    }

    public String getDurationOfAlcoholConsumption() {
        return durationOfAlcoholConsumption;
    }

    public void setDurationOfAlcoholConsumption(String durationOfAlcoholConsumption) {
        this.durationOfAlcoholConsumption = durationOfAlcoholConsumption;
    }
}