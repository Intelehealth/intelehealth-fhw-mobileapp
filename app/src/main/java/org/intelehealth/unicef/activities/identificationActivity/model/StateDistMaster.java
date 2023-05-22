package org.intelehealth.unicef.activities.identificationActivity.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class StateDistMaster implements Serializable {
    @Expose
    @SerializedName("states")
    private List<StateData> stateDataList;

    public List<StateData> getStateDataList() {
        return stateDataList;
    }

    public void setStateDataList(List<StateData> stateDataList) {
        this.stateDataList = stateDataList;
    }
}
