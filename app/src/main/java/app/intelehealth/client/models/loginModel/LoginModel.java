
package app.intelehealth.client.models.loginModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginModel {

    @SerializedName("sessionId")
    @Expose
    private String sessionId;
    @SerializedName("authenticated")
    @Expose
    private Boolean authenticated;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("locale")
    @Expose
    private String locale;
    @SerializedName("allowedLocales")
    @Expose
    private List<String> allowedLocales = null;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<String> getAllowedLocales() {
        return allowedLocales;
    }

    public void setAllowedLocales(List<String> allowedLocales) {
        this.allowedLocales = allowedLocales;
    }

}
