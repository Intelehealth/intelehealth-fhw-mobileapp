package org.intelehealth.unicef.models;

/**
 * Created by Prajwal Waingankar on 07/11/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class PrescriptionModel {
    String encounterUuid;
    String visitUuid;
    String patientUuid;
    String openmrs_id;
    String first_name;
    String last_name;
    String phone_number;
    String gender;
    String dob;
    String visit_start_date;
    String visit_speciality;
    String followup_date;
    String sync;
    boolean emergency;
    String patient_photo;
    String chiefComplaint;
    boolean hasPrescription = false;
    String obsservermodifieddate = "";

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getOpenmrs_id() {
        return openmrs_id;
    }

    public void setOpenmrs_id(String openmrs_id) {
        this.openmrs_id = openmrs_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getVisit_start_date() {
        return visit_start_date;
    }

    public void setVisit_start_date(String visit_start_date) {
        this.visit_start_date = visit_start_date;
    }

    public String getVisit_speciality() {
        return visit_speciality;
    }

    public void setVisit_speciality(String visit_speciality) {
        this.visit_speciality = visit_speciality;
    }

    public String getFollowup_date() {
        return followup_date;
    }

    public void setFollowup_date(String followup_date) {
        this.followup_date = followup_date;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public String getPatient_photo() {
        return patient_photo;
    }

    public void setPatient_photo(String patient_photo) {
        this.patient_photo = patient_photo;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public boolean isHasPrescription() {
        return hasPrescription;
    }

    public void setHasPrescription(boolean hasPrescription) {
        this.hasPrescription = hasPrescription;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getObsservermodifieddate() {
        return obsservermodifieddate;
    }

    public void setObsservermodifieddate(String obsservermodifieddate) {
        this.obsservermodifieddate = obsservermodifieddate;
    }
}
