package org.intelehealth.hellosaathitraining.models.dailyPerformance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Registrations {
    @SerializedName("date")
    @Expose
    public String registered_date;

    @SerializedName("total")
    @Expose
    public String total_count;
}
