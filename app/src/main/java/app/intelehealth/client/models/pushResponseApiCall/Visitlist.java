
package app.intelehealth.client.models.pushResponseApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Visitlist {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("creator")
    @Expose
    private Integer creator;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

}
