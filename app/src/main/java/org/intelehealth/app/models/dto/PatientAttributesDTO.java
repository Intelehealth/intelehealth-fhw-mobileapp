
package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PatientAttributesDTO implements Serializable {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("person_attribute_type_uuid")
    @Expose
    private String personAttributeTypeUuid;
    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPersonAttributeTypeUuid() {
        return personAttributeTypeUuid;
    }

    public void setPersonAttributeTypeUuid(String personAttributeTypeUuid) {
        this.personAttributeTypeUuid = personAttributeTypeUuid;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    @Override
    public String toString() {
        return "PatientAttributesDTO{" +
                "uuid='" + uuid + '\'' +
                ", value='" + value + '\'' +
                ", personAttributeTypeUuid='" + personAttributeTypeUuid + '\'' +
                ", patientuuid='" + patientuuid + '\'' +
                '}';
    }

    public enum Column {
        TELEPHONE("Telephone Number"),
        ECONOMIC_STATUS("Economic Status"),
        EDUCATION("Education Level"),
        PROVIDER_ID("providerUUID"),
        OCCUPATION("occupation"),
        SWD("Son/wife/daughter"),
        NATIONAL_ID("NationalID"),
        PROFILE_IMG_TIMESTAMP("ProfileImageTimestamp"),
        CAST("Caste"),
        CREATED_DATE("createdDate"),
        TMH_CASE_NUMBER("TMH Case Number"),
        REQUEST_ID("Request ID"),
        RELATIVE_PHONE_NUMBER("Relative Phone Number"),
        DISCIPLINE("Discipline"),
        DEPARTMENT("Department"),
        PROVINCES("Provinces"),
        CITIES("Cities"),
        REGISTRATION_ADDRESS_OF_HF("Registration address of health facility"),
        INN("INN"),
        CODE_OF_HEALTH_FACILITY("Code of the Health Facility"),
        HEALTH_FACILITY_NAME("Health facility name"),
        CODE_OF_DEPARTMENT("Code of the Department"),
        EMERGENCY_CONTACT_NAME("Emergency Contact Name"),
        EMERGENCY_CONTACT_NUMBER("Emergency Contact Number"),
        EMERGENCY_CONTACT_TYPE("Emergency Contact Type");

        public final String value;

        Column(String value) {
            this.value = value;
        }
    }
}
