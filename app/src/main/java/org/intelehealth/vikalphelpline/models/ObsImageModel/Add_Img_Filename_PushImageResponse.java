package org.intelehealth.vikalphelpline.models.ObsImageModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created By: Prajwal Waingankar on 23-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Add_Img_Filename_PushImageResponse {
    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("status")
    @Expose
    private String status;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


