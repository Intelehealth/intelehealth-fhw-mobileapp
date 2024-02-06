package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 01/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class OTPResponse {

    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("message")
    @Expose
    private String message;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}