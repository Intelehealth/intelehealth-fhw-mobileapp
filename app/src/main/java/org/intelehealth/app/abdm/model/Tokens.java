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

public class Tokens implements Serializable {

    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("expiresIn")
    @Expose
    private Integer expiresIn;
    @SerializedName("refreshToken")
    @Expose
    private String refreshToken;
    @SerializedName("refreshExpiresIn")
    @Expose
    private Integer refreshExpiresIn;

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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(Integer refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    @Override
    public String toString() {
        return "Tokens{" +
                "token='" + token + '\'' +
                ", expiresIn=" + expiresIn +
                ", refreshToken='" + refreshToken + '\'' +
                ", refreshExpiresIn=" + refreshExpiresIn +
                '}';
    }
}
