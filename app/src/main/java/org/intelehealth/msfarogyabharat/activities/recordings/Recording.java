package org.intelehealth.msfarogyabharat.activities.recordings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Recording {
    @SerializedName("RecordingURL")
    @Expose
    public String RecordingURL;

    @SerializedName("Caller")
    @Expose
    public String Caller;

    @SerializedName("CallStartTime")
    @Expose
    public String time;
}
