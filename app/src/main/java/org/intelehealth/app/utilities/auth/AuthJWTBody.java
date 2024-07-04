package org.intelehealth.app.utilities.auth;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by - Prajwal W. on 04/07/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class AuthJWTBody {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("rememberme")
    @Expose
    private Boolean rememberme;

    public AuthJWTBody(String username, String password, Boolean rememberme) {
        this.username = username;
        this.password = password;
        this.rememberme = rememberme;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberme() {
        return rememberme;
    }

    public void setRememberme(Boolean rememberme) {
        this.rememberme = rememberme;
    }

}
