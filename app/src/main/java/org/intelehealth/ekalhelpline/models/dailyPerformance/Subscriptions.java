package org.intelehealth.ekalhelpline.models.dailyPerformance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Subscriptions {
    @SerializedName("subscribedon")
    @Expose
    public String subscribed_date;

    @SerializedName("total")
    @Expose
    public String total_count;
}
