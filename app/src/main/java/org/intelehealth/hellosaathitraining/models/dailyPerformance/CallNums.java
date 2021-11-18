package org.intelehealth.hellosaathitraining.models.dailyPerformance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallNums {
    @SerializedName("date")
    @Expose
    public String called_date;

    @SerializedName("total")
    @Expose
    public String total_count;
}
