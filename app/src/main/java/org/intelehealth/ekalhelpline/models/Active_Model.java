package org.intelehealth.ekalhelpline.models;

/**
 * Created By: Prajwal Waingankar on 12-Oct-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Active_Model {
    private String patient_uuid;
    private String visit_uuid;

    public Active_Model(String patient_uuid, String visit_uuid) {
        this.patient_uuid = patient_uuid;
        this.visit_uuid = visit_uuid;
    }

    public String getPatient_uuid() {
        return patient_uuid;
    }

    public void setPatient_uuid(String patient_uuid) {
        this.patient_uuid = patient_uuid;
    }

    public String getVisit_uuid() {
        return visit_uuid;
    }

    public void setVisit_uuid(String visit_uuid) {
        this.visit_uuid = visit_uuid;
    }


}
