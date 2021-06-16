package org.intelehealth.swasthyasamparktelemedicine.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetPassword {
    @SerializedName("uname")
    @Expose
    public String username;

    @SerializedName("password")
    @Expose
    public String password;

    @SerializedName("status")
    @Expose
    public String status;
}

