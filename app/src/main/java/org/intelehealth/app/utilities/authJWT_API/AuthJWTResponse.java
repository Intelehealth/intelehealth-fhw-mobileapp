package org.intelehealth.app.utilities.authJWT_API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthJWTResponse {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("token")
    @Expose
    private String token;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AuthJWTResponse{" +
                "status=" + status +
                ", token='" + token + '\'' +
                '}';
    }
}