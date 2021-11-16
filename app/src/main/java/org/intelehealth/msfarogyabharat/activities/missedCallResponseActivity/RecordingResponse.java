package org.intelehealth.msfarogyabharat.activities.missedCallResponseActivity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecordingResponse {
    @SerializedName("status")
    @Expose
    public  String status;
    @SerializedName("data")
    @Expose
    public List<Recording> data;
}
