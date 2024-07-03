package org.intelehealth.app.abdm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SetAbhaAddressResponse {
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("healthIdNumber")
    @Expose
    private String healthIdNumber;
    @SerializedName("preferredAbhaAddress")
    @Expose
    private String preferredAbhaAddress;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getHealthIdNumber() {
        return healthIdNumber;
    }

    public void setHealthIdNumber(String healthIdNumber) {
        this.healthIdNumber = healthIdNumber;
    }

    public String getPreferredAbhaAddress() {
        return preferredAbhaAddress;
    }

    public void setPreferredAbhaAddress(String preferredAbhaAddress) {
        this.preferredAbhaAddress = preferredAbhaAddress;
    }
}
