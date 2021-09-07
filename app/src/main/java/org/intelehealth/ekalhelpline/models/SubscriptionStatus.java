package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubscriptionStatus {
    @SerializedName("data")
    @Expose
    public String data;
    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("userdata")
    @Expose
    public List<UserData> userdata;

    public static class UserData {
        @SerializedName("bucketsubscribedto")
        @Expose
        public int bucketsubscribedto;
        @SerializedName("slotselected")
        @Expose
        public String slotselected;
        @SerializedName("gender")
        @Expose
        public String genderselected;
    }
}
