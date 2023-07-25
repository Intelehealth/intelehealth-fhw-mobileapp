
package org.intelehealth.ezazi.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.utilities.StringUtils;

import java.io.Serializable;
import java.util.UUID;

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

    public enum Columns {
        BED_NUMBER("Bed Number"),
        ADMISSION_DATE("Admission_Date"),
        ADMISSION_TIME("Admission_Time"),
        PARITY("Parity"),
        LABOR_ONSET("Labor Onset"),
        ACTIVE_LABOR_DIAGNOSED("Active Labor Diagnosed"),
        MEMBRANE_RUPTURED_TIMESTAMP("Membrane Ruptured Timestamp"),
        RISK_FACTORS("Risk factors"),
        HOSPITAL_MATERNITY("Hospital_Maternity"),
        PRIMARY_DOCTOR("PrimaryDoctor"),
        SECONDARY_DOCTOR("SecondaryDoctor"),
        REGISTRATION_NUMBER("Ezazi Registration Number"),
        PROFILE_IMG_TIMESTAMP("ProfileImageTimestamp"),
        ALTERNATE_NO("AlternateNo");
        public String value;

        Columns(String value) {
            this.value = value;
        }
    }
}