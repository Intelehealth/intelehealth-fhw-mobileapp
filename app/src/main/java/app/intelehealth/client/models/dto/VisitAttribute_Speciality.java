package app.intelehealth.client.models.dto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by Prajwal Waingankar
 * on 16-Jul-20.
 * Github: prajwalmw
 */

public class VisitAttribute_Speciality {

//    @SerializedName("attributeType")
//    @Expose
//    private String attributeType;

    @SerializedName("uuid")
    @Expose
    private String uuid;

    @SerializedName("visit_attribute_type_uuid")
    @Expose
    private String visitAttributeTypeUuid;

    @SerializedName("value")
    @Expose
    private String value;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisitAttributeTypeUuid() {
        return visitAttributeTypeUuid;
    }

    public void setVisitAttributeTypeUuid(String visitAttributeTypeUuid) {
        this.visitAttributeTypeUuid = visitAttributeTypeUuid;
    }

//    public String getAttributeType() {
//        return attributeType;
//    }
//
//    public void setAttributeType(String attributeType) {
//        this.attributeType = attributeType;
//    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
