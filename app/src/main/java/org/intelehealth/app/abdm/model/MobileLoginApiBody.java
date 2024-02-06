package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 06/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class MobileLoginApiBody {

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("scope")
    @Expose
    private String scope;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
