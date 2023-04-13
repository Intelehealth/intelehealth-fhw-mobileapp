package org.intelehealth.app.models.statewise_location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Setup_LocationModel {
    @SerializedName("states")
    @Expose
    private List<Setup_StateModel> states=null;
    @SerializedName("message")
    @Expose
    private String message;

    public List<Setup_StateModel> getStates() {
        return states;
    }

    public void setStates(List<Setup_StateModel> states) {
        this.states = states;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
