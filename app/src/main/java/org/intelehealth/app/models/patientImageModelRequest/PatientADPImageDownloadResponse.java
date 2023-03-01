package org.intelehealth.app.models.patientImageModelRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PatientADPImageDownloadResponse {

    @SerializedName("personimages")
    @Expose
    private List<String> personimages;

    public List<String> getPersonimages() {
        return personimages;
    }

    public void setPersonimages(List<String> personimages) {
        this.personimages = personimages;
    }

}