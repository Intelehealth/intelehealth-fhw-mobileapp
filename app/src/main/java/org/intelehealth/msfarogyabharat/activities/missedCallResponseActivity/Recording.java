package org.intelehealth.msfarogyabharat.activities.missedCallResponseActivity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Recording {
    @SerializedName("RecordingURL")
    @Expose
    public String RecordingURL;

    @SerializedName("Caller")
    @Expose
    public String Caller;

    @SerializedName("dateofcall")
    @Expose
    public String dateofcall;

 @SerializedName("id")
    @Expose
    public String id;
 @SerializedName("followupdone")
    @Expose
    public String followupdone;
}
