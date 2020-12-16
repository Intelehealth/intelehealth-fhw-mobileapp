
package app.intelehealth.client.models.loginModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserProperties {

    @SerializedName("loginAttempts")
    @Expose
    private String loginAttempts;

    public String getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(String loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

}
