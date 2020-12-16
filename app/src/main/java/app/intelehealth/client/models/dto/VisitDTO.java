
package app.intelehealth.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisitDTO {

    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("visit_type_uuid")
    @Expose
    private String visitTypeUuid;
    @SerializedName("startdate")
    @Expose
    private String startdate;
    @SerializedName("enddate")
    @Expose
    private String enddate;
    @SerializedName("locationuuid")
    @Expose
    private String locationuuid;
    @SerializedName("creator_uuid")
    @Expose
    private String creatoruuid;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;

    private List<VisitAttributeDTO> visitAttributeDTOS;

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisitTypeUuid() {
        return visitTypeUuid;
    }

    public void setVisitTypeUuid(String visitTypeUuid) {
        this.visitTypeUuid = visitTypeUuid;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getLocationuuid() {
        return locationuuid;
    }

    public void setLocationuuid(String locationuuid) {
        this.locationuuid = locationuuid;
    }

    public String getCreatoruuid() {
        return creatoruuid;
    }

    public void setCreatoruuid(String creatoruuid) {
        this.creatoruuid = creatoruuid;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

    public List<VisitAttributeDTO> getVisitAttributeDTOS() {
        return visitAttributeDTOS;
    }

    public void setVisitAttributeDTOS(List<VisitAttributeDTO> visitAttributeDTOS) {
        this.visitAttributeDTOS = visitAttributeDTOS;
    }
}