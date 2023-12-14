package org.intelehealth.kf.models.hwprofile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("person")
    @Expose
    private PersonProfile person;
    @SerializedName("attributes")
    @Expose
    private List<PersonAttributes> attributes = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public PersonProfile getPerson() {
        return person;
    }

    public void setPerson(PersonProfile person) {
        this.person = person;
    }

    public List<PersonAttributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersonAttributes> attributes) {
        this.attributes = attributes;
    }
}
