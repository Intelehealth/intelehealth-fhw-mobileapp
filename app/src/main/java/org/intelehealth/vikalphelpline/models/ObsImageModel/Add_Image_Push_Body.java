package org.intelehealth.vikalphelpline.models.ObsImageModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created By: Prajwal Waingankar on 23-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Add_Image_Push_Body {
    @SerializedName("patientId")
    @Expose
    private String patientId;
    @SerializedName("obsId")
    @Expose
    private String obsId;
    @SerializedName("imageName")
    @Expose
    private String imageName;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getObsId() {
        return obsId;
    }

    public void setObsId(String obsId) {
        this.obsId = obsId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
