package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 06/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MobileLoginOnOTPVerifiedResponse implements Serializable {

    @SerializedName("txnId")
    @Expose
    private String txnId;
    @SerializedName("authResult")
    @Expose
    private String authResult;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("refreshToken")
    @Expose
    private String refreshToken;
    @SerializedName("expiresIn")
    @Expose
    private Integer expiresIn;
    @SerializedName("accounts")
    @Expose
    private List<Account> accounts;
    @SerializedName("users")
    @Expose
    private List<LoginOtpVerifyUser> users;


    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getAuthResult() {
        return authResult;
    }

    public void setAuthResult(String authResult) {
        this.authResult = authResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<LoginOtpVerifyUser> getUsers() {
        return users;
    }

    public void setUsers(List<LoginOtpVerifyUser> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "MobileLoginOnOTPVerifiedResponse{" +
                "txnId='" + txnId + '\'' +
                ", authResult='" + authResult + '\'' +
                ", message='" + message + '\'' +
                ", token='" + token + '\'' +
                ", expiresIn=" + expiresIn +
                ", accounts=" + accounts +
                ", users=" + users +
                '}';
    }
}