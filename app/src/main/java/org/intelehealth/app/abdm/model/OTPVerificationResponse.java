package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 01/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OTPVerificationResponse implements Serializable {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("tokens")
    @Expose
    private Tokens tokens;
    @SerializedName("ABHAProfile")
    @Expose
    private ABHAProfile aBHAProfile;
    @SerializedName("isNew")
    @Expose
    private Boolean isNew;

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

    public Tokens getTokens() {
        return tokens;
    }

    public void setTokens(Tokens tokens) {
        this.tokens = tokens;
    }

    public ABHAProfile getABHAProfile() {
        return aBHAProfile;
    }

    public void setABHAProfile(ABHAProfile aBHAProfile) {
        this.aBHAProfile = aBHAProfile;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public String toString() {
        return "OTPVerificationResponse{" +
                "message='" + message + '\'' +
                ", txnId='" + txnId + '\'' +
                ", tokens=" + tokens +
                ", aBHAProfile=" + aBHAProfile +
                ", isNew=" + isNew +
                '}';
    }
}
