package org.intelehealth.ekalhelpline.models.dailyPerformance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubscriptionsResponse {
    @SerializedName("data")
    @Expose
    public List<Subscriptions> data;
}
