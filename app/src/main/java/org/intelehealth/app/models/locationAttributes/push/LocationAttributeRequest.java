package org.intelehealth.app.models.locationAttributes.push;

import java.util.List;

public class LocationAttributeRequest {

    private List<LocationAttributes> locationAttributes;

    public List<LocationAttributes> getLocationAttributes() {
        return locationAttributes;
    }

    public void setLocationAttributes(List<LocationAttributes> locationAttributes) {
        this.locationAttributes = locationAttributes;
    }
}
