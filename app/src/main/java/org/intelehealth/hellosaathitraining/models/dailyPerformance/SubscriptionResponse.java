package org.intelehealth.hellosaathitraining.models.dailyPerformance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubscriptionResponse {
    @SerializedName("data")
    @Expose
    public List<Subscriptions> data;
}
