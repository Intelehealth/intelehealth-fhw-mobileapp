
package app.intelehealth.client.models.pushResponseApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PersonList {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

}
