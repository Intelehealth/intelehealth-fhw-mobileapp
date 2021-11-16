package org.intelehealth.ekalhelpline.activities.recordings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Recording {
    @SerializedName("RecordingURL")
    @Expose
    public String RecordingURL;

    @SerializedName("Caller")
    @Expose
    public String Caller;

    @SerializedName("language")
    @Expose
    public String language;

    @SerializedName("Time")
    @Expose
    public String time;
}
