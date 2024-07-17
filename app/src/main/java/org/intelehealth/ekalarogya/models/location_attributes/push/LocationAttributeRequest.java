package org.intelehealth.ekalarogya.models.location_attributes.push;

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
