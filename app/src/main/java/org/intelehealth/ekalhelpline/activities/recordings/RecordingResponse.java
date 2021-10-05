package org.intelehealth.ekalhelpline.activities.recordings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecordingResponse {
    @SerializedName("data")
    @Expose
    public List<Recording> data;
}
