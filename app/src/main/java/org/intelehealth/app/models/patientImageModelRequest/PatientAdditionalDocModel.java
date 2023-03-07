package org.intelehealth.app.models.patientImageModelRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on March 2023.
 * Github: prajwalmw
 */
public class PatientAdditionalDocModel {
    @SerializedName("images")
    @Expose
    private List<ADPImageModel> images;

    public List<ADPImageModel> getImages() {
        return images;
    }

    public void setImages(List<ADPImageModel> images) {
        this.images = images;
    }
}