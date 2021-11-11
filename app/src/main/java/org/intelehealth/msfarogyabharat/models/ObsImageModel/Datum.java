package org.intelehealth.msfarogyabharat.models.ObsImageModel;

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

    @SerializedName("obsId")
    @Expose
    private String obsId;

    @SerializedName("encounteruuid")
    @Expose
    private String encounteruuid;

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

    public String getEncounteruuid() {
        return encounteruuid;
    }

    public void setEncounteruuid(String encounteruuid) {
        this.encounteruuid = encounteruuid;
    }
}
