
package app.intelehealth.client.models.uploadSurvey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ob {

    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("value")
    @Expose
    private String value;

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

}
