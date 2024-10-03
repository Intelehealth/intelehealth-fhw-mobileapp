package org.intelehealth.app.models;

/**
 * Created by Prajwal Waingankar on 30/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationModel {
    private String uuid;
    private String first_name;
    private String last_name;
    private String patientuuid;
    private String description;
    private String obs_server_modified_date;
    private String notification_type;
    private String sync;

    private String visitUUID;
    private String patient_photo;
    private String visit_startDate;
    private String gender;
    private String age;
    private String encounterUuidVitals;
    private String encounterUuidAdultIntial;
    private String date_of_birth;
    private String followupDate;
    private String openmrsID;
    private String isDeleted;




    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getObs_server_modified_date() {
        return obs_server_modified_date;
    }

    public void setObs_server_modified_date(String obs_server_modified_date) {
        this.obs_server_modified_date = obs_server_modified_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public String getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(String notification_type) {
        this.notification_type = notification_type;
    }

    public String getVisitUUID() {
        return visitUUID;
    }

    public void setVisitUUID(String visitUUID) {
        this.visitUUID = visitUUID;
    }

    public String getPatient_photo() {
        return patient_photo;
    }

    public void setPatient_photo(String patient_photo) {
        this.patient_photo = patient_photo;
    }

    public String getVisit_startDate() {
        return visit_startDate;
    }

    public void setVisit_startDate(String visit_startDate) {
        this.visit_startDate = visit_startDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEncounterUuidVitals() {
        return encounterUuidVitals;
    }

    public void setEncounterUuidVitals(String encounterUuidVitals) {
        this.encounterUuidVitals = encounterUuidVitals;
    }

    public String getEncounterUuidAdultIntial() {
        return encounterUuidAdultIntial;
    }

    public void setEncounterUuidAdultIntial(String encounterUuidAdultIntial) {
        this.encounterUuidAdultIntial = encounterUuidAdultIntial;
    }

    public String getFollowupDate() {
        return followupDate;
    }

    public void setFollowupDate(String followupDate) {
        this.followupDate = followupDate;
    }

    public String getOpenmrsID() {
        return openmrsID;
    }

    public void setOpenmrsID(String openmrsID) {
        this.openmrsID = openmrsID;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
