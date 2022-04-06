package org.intelehealth.app.activities.identificationActivity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HealthIssues {

    //    private String householdMemberName;
    @SerializedName("healthIssueReported")
    @Expose
    private String healthIssueReported = "";
    @SerializedName("numberOfEpisodesInTheLastYear")
    @Expose
    private String numberOfEpisodesInTheLastYear = "";
    @SerializedName("primaryHealthcareProviderValue")
    @Expose
    private String primaryHealthcareProviderValue = "";
    @SerializedName("firstLocationOfVisit")
    @Expose
    private String firstLocationOfVisit = "";
    @SerializedName("referredTo")
    @Expose
    private String referredTo = "";
    @SerializedName("modeOfTransportation")
    @Expose
    private String modeOfTransportation = "";
    @SerializedName("averageCostOfTravelAndStayPerEpisode")
    @Expose
    private String averageCostOfTravelAndStayPerEpisode = "";
    @SerializedName("averageCostOfConsultation")
    @Expose
    private String averageCostOfConsultation = "";
    @SerializedName("averageCostOfMedicine")
    @Expose
    private String averageCostOfMedicine = "";
    @SerializedName("scoreForExperienceOfTreatment")
    @Expose
    private String scoreForExperienceOfTreatment = "";

    public String getHealthIssueReported() {
        return healthIssueReported;
    }

    public void setHealthIssueReported(String healthIssueReported) {
        this.healthIssueReported = healthIssueReported;
    }

    public String getNumberOfEpisodesInTheLastYear() {
        return numberOfEpisodesInTheLastYear;
    }

    public void setNumberOfEpisodesInTheLastYear(String numberOfEpisodesInTheLastYear) {
        this.numberOfEpisodesInTheLastYear = numberOfEpisodesInTheLastYear;
    }

    public String getPrimaryHealthcareProviderValue() {
        return primaryHealthcareProviderValue;
    }

    public void setPrimaryHealthcareProviderValue(String primaryHealthcareProviderValue) {
        this.primaryHealthcareProviderValue = primaryHealthcareProviderValue;
    }

    public String getFirstLocationOfVisit() {
        return firstLocationOfVisit;
    }

    public void setFirstLocationOfVisit(String firstLocationOfVisit) {
        this.firstLocationOfVisit = firstLocationOfVisit;
    }

    public String getReferredTo() {
        return referredTo;
    }

    public void setReferredTo(String referredTo) {
        this.referredTo = referredTo;
    }

    public String getModeOfTransportation() {
        return modeOfTransportation;
    }

    public void setModeOfTransportation(String modeOfTransportation) {
        this.modeOfTransportation = modeOfTransportation;
    }

    public String getAverageCostOfTravelAndStayPerEpisode() {
        return averageCostOfTravelAndStayPerEpisode;
    }

    public void setAverageCostOfTravelAndStayPerEpisode(String averageCostOfTravelAndStayPerEpisode) {
        this.averageCostOfTravelAndStayPerEpisode = averageCostOfTravelAndStayPerEpisode;
    }

    public String getAverageCostOfConsultation() {
        return averageCostOfConsultation;
    }

    public void setAverageCostOfConsultation(String averageCostOfConsultation) {
        this.averageCostOfConsultation = averageCostOfConsultation;
    }

    public String getAverageCostOfMedicine() {
        return averageCostOfMedicine;
    }

    public void setAverageCostOfMedicine(String averageCostOfMedicine) {
        this.averageCostOfMedicine = averageCostOfMedicine;
    }

    public String getScoreForExperienceOfTreatment() {
        return scoreForExperienceOfTreatment;
    }

    public void setScoreForExperienceOfTreatment(String scoreForExperienceOfTreatment) {
        this.scoreForExperienceOfTreatment = scoreForExperienceOfTreatment;
    }
}