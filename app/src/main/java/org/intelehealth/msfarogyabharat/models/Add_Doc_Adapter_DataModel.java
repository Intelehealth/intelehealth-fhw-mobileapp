package org.intelehealth.msfarogyabharat.models;

/**
 * Created By: Prajwal Waingankar on 27-Oct-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Add_Doc_Adapter_DataModel {
    private String encounteruid;
    private String patientuuid;
    private String visituuid;
    private String patientname;
    private float float_ageYear_Month;

    public Add_Doc_Adapter_DataModel(String encounteruid, String patientuuid, String visituuid, String patientname, float float_ageYear_Month) {
        this.encounteruid = encounteruid;
        this.patientuuid = patientuuid;
        this.visituuid = visituuid;
        this.patientname = patientname;
        this.float_ageYear_Month = float_ageYear_Month;
    }

    public String getEncounteruid() {
        return encounteruid;
    }

    public void setEncounteruid(String encounteruid) {
        this.encounteruid = encounteruid;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getVisituuid() {
        return visituuid;
    }

    public void setVisituuid(String visituuid) {
        this.visituuid = visituuid;
    }

    public String getPatientname() {
        return patientname;
    }

    public void setPatientname(String patientname) {
        this.patientname = patientname;
    }

    public float getFloat_ageYear_Month() {
        return float_ageYear_Month;
    }

    public void setFloat_ageYear_Month(float float_ageYear_Month) {
        this.float_ageYear_Month = float_ageYear_Month;
    }
}
