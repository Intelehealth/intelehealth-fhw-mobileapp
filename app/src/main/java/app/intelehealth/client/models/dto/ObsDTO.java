
package app.intelehealth.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ObsDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("encounteruuid")
    @Expose
    private String encounteruuid;
    @SerializedName("conceptuuid")
    @Expose
    private String conceptuuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("obsServerModifiedDate")
    @Expose
    private String obsServerModifiedDate;
    @SerializedName("creator")
    @Expose
    private String creator;
    @SerializedName("voided")
    @Expose
    private Integer voided;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncounteruuid() {
        return encounteruuid;
    }

    public void setEncounteruuid(String encounteruuid) {
        this.encounteruuid = encounteruuid;
    }

    public String getConceptuuid() {
        return conceptuuid;
    }

    public void setConceptuuid(String conceptuuid) {
        this.conceptuuid = conceptuuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public String getObsServerModifiedDate() {
        return obsServerModifiedDate;
    }

    public void setObsServerModifiedDate(String obsServerModifiedDate) {
        this.obsServerModifiedDate = obsServerModifiedDate;
    }
}
