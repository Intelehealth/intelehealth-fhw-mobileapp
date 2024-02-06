package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 01/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OTPVerificationRequestBody {

    @SerializedName("otp")
    @Expose
    private String otp;
    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;
    @SerializedName("scope")
    @Expose
    private String scope;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}