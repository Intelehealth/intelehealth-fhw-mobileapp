package org.intelehealth.msfarogyabharat.activities.missedCallResponseActivity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateRecordingCallerBodyModel {
    @SerializedName("callid")
    @Expose
    private String callid;

    public String getCallid() {
        return callid;
    }

    public void setCallid(String callid) {
        this.callid = callid;
    }
}
