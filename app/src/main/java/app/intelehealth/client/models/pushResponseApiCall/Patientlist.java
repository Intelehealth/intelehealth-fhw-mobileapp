
package app.intelehealth.client.models.pushResponseApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Patientlist {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("openmrs_id")
    @Expose
    private String openmrsId;
    @SerializedName("dead")
    @Expose
    private Integer dead;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOpenmrsId() {
        return openmrsId;
    }

    public void setOpenmrsId(String openmrsId) {
        this.openmrsId = openmrsId;
    }

    public Integer getDead() {
        return dead;
    }

    public void setDead(Integer dead) {
        this.dead = dead;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

}
