package org.intelehealth.hellosaathitraining.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrescriptionUrl {

//    @SerializedName("status")
//    @Expose
//    private String status;
//
//    @SerializedName("message")
//    @Expose
//    private String message;

    @SerializedName("data")
    @Expose
    private ShortUrlData data = null;

//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }


    public ShortUrlData getData() {
        return data;
    }

    public void setData(ShortUrlData data) {
        this.data = data;
    }
}

