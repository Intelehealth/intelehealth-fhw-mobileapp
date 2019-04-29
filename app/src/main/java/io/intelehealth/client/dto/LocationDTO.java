
package io.intelehealth.client.dto;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "tbl_location")
public class LocationDTO {

    @SerializedName("name")
    @Expose
    private String name;
    @PrimaryKey
    @NonNull
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