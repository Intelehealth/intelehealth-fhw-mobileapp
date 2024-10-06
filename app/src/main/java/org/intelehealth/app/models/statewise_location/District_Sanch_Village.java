package org.intelehealth.ekalarogya.models.statewise_location;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Prajwal Waingankar
 * on 27-Jan-2021.
 * Github: prajwalmw
 */

public class District_Sanch_Village {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("display")
    @Expose
    private String display;
    @SerializedName("childLocations")
    @Expose
    private List<ChildLocation> childLocations = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public List<ChildLocation> getChildLocations() {
        return childLocations;
    }

    public void setChildLocations(List<ChildLocation> childLocations) {
        this.childLocations = childLocations;
    }

}
