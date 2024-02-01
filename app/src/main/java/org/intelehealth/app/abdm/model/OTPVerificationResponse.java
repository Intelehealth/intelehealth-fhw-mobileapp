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

public class OTPVerificationResponse implements Parcelable {

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

    protected OTPVerificationResponse(Parcel in) {
        message = in.readString();
        txnId = in.readString();
        byte tmpIsNew = in.readByte();
        isNew = tmpIsNew == 0 ? null : tmpIsNew == 1;
    }

    public static final Creator<OTPVerificationResponse> CREATOR = new Creator<OTPVerificationResponse>() {
        @Override
        public OTPVerificationResponse createFromParcel(Parcel in) {
            return new OTPVerificationResponse(in);
        }

        @Override
        public OTPVerificationResponse[] newArray(int size) {
            return new OTPVerificationResponse[size];
        }
    };

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(txnId);
        dest.writeByte((byte) (isNew == null ? 0 : isNew ? 1 : 2));
    }
}
