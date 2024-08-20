package org.intelehealth.vikalphelpline.models.ObsImageModel;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created By: Prajwal Waingankar on 23-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class AddImageDownloadResponse {
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
