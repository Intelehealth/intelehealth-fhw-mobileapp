package io.intelehealth.client.objects;

/**
 * Container for {@link io.intelehealth.client.TodayPatientActivity}
 * <p>
 * Created by Dexter Barretto on 5/22/17.
 * Github : @dbarretto
 */

public class TodayPatientModel {

    Integer _id;
    String patient_id;
    String start_datetime;
    String end_datetime;
    String openmrs_uuid;
    String first_name;
    String middle_name;
    String last_name;
    String date_of_birth;
    String phone_number;

    public TodayPatientModel(Integer _id, String patient_id, String start_datetime,
                             String end_datetime, String openmrs_uuid, String first_name,
                             String middle_name, String last_name, String date_of_birth,
                             String phone_number) {
        this._id = _id;
        this.patient_id = patient_id;
        this.start_datetime = start_datetime;
        this.end_datetime = end_datetime;
        this.openmrs_uuid = openmrs_uuid;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.date_of_birth = date_of_birth;
        this.phone_number = phone_number;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getStart_datetime() {
        return start_datetime;
    }

    public void setStart_datetime(String start_datetime) {
        this.start_datetime = start_datetime;
    }

    public String getEnd_datetime() {
        return end_datetime;
    }

    public void setEnd_datetime(String end_datetime) {
        this.end_datetime = end_datetime;
    }

    public String getOpenmrs_uuid() {
        return openmrs_uuid;
    }

    public void setOpenmrs_uuid(String openmrs_uuid) {
        this.openmrs_uuid = openmrs_uuid;
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

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }
}
