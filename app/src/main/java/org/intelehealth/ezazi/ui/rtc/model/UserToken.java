package org.intelehealth.ezazi.ui.rtc.model;

/**
 * Created by Vaghela Mithun R. on 06-07-2023 - 13:55.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class UserToken {
    private String token;
    private String appToken;
    private boolean success;

    public UserToken(String token, String appToken, boolean success) {
        this.token = token;
        this.appToken = appToken;
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
