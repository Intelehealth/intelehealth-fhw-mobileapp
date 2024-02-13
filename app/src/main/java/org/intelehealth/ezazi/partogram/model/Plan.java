package org.intelehealth.ezazi.partogram.model;

import androidx.annotation.NonNull;

import org.intelehealth.klivekit.chat.model.ItemHeader;

public class Plan implements ItemHeader {
    private String planDetails;
    private String obsUuid;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    private String createdDate;

    public String getObsUuid() {
        return obsUuid;
    }

    public void setObsUuid(String obsUuid) {
        this.obsUuid = obsUuid;
    }

    public String getPlanDetails() {
        return planDetails;
    }

    public void setPlanDetails(String planDetails) {
        this.planDetails = planDetails;
    }
    public boolean isValidPlan() {
        return planDetails != null && planDetails.length() > 0 ;
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @NonNull
    @Override
    public String createdDate() {
        return createdDate();
    }
}
