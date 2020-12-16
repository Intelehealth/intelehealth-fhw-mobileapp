
package app.intelehealth.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationDTO {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("locationuuid")
    @Expose
    private String locationuuid;
    @SerializedName("retired")
    @Expose
    private Integer retired;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationuuid() {
        return locationuuid;
    }

    public void setLocationuuid(String locationuuid) {
        this.locationuuid = locationuuid;
    }

    public Integer getRetired() {
        return retired;
    }

    public void setRetired(Integer retired) {
        this.retired = retired;
    }


}