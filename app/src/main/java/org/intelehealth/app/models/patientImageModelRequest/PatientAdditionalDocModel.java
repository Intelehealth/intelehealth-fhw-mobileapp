package org.intelehealth.app.models.patientImageModelRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientAdditionalDocModel {
    @SerializedName("file")
    @Expose
    private String file;    // This returns a BASE64 Image file.
    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

}