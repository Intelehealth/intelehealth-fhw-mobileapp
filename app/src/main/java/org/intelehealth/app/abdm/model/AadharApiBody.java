package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 01/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AadharApiBody {

    @SerializedName("aadhar")
    @Expose
    private String aadhar;

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

}
