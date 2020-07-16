package app.intelehealth.client.models.dto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by Prajwal Waingankar
 * on 16-Jul-20.
 * Github: prajwalmw
 */

public class VisitAttribute_Speciality {

    @SerializedName("attributeType")
    @Expose
    private String attributeType;
    @SerializedName("value")
    @Expose
    private String value;

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
