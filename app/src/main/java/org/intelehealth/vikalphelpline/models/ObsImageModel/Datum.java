package org.intelehealth.vikalphelpline.models.ObsImageModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created By: Prajwal Waingankar on 23-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Datum {

    @SerializedName("imageName")
    @Expose
    private String imageName;

    public String getObsId() {
        return obsId;
    }

    public void setObsId(String obsId) {
        this.obsId = obsId;
    }

    @SerializedName("obsId")
    @Expose
    private String obsId;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
