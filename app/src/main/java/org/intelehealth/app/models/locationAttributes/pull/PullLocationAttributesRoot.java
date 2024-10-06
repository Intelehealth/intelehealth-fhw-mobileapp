package org.intelehealth.app.models.locationAttributes.pull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PullLocationAttributesRoot {

    @SerializedName("data")
    private List<PullLocationAttributesData> attributesDataList;

    public List<PullLocationAttributesData> getAttributesDataList() {
        return attributesDataList;
    }

    public void setAttributesDataList(List<PullLocationAttributesData> attributesDataList) {
        this.attributesDataList = attributesDataList;
    }
}
