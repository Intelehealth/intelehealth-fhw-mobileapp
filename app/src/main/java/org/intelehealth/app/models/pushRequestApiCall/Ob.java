
package org.intelehealth.app.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ob {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("value")
    @Expose
    private String value;

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    @SerializedName("comments")
    @Expose
    private String comments;

    @SerializedName("interpretation")
    @Expose
    private String interpretation;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
