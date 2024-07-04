package org.intelehealth.app.models.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by - Prajwal W. on 04/07/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
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
