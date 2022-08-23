package org.intelehealth.app.models.dto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by Prajwal Waingankar
 * on 14-Jul-20.
 * Github: prajwalmw
 */


public class ProviderAttributeListDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("provideruuid")
    @Expose
    private String provideruuid;
    @SerializedName("attributetypeuuid")
    @Expose
    private String attributetypeuuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("voided")
    @Expose
    private Long voided;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProvideruuid() {
        return provideruuid;
    }

    public void setProvideruuid(String provideruuid) {
        this.provideruuid = provideruuid;
    }

    public String getAttributetypeuuid() {
        return attributetypeuuid;
    }

    public void setAttributetypeuuid(String attributetypeuuid) {
        this.attributetypeuuid = attributetypeuuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getVoided() {
        return voided;
    }

    public void setVoided(Long voided) {
        this.voided = voided;
    }
}
