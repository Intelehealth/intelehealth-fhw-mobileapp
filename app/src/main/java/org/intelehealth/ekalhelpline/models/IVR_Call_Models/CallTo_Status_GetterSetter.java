package org.intelehealth.ekalhelpline.models.IVR_Call_Models;

/**
 * Created By: Prajwal Waingankar on 20-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class CallTo_Status_GetterSetter {
    private String callTo;
    private String status;


    public CallTo_Status_GetterSetter(String callTo, String status) {
        this.callTo = callTo;
        this.status = status;
    }

    public String getCallTo() {
        return callTo;
    }

    public void setCallTo(String callTo) {
        this.callTo = callTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

