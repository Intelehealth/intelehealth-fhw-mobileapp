package edu.jhu.bme.cbid.healthassistantsclient.objects;

/**
 * Visit information class for Gson data serialization
 */
public class Visit  {
    private String id;
    private Integer patient_id;
    private String start_datetime;
    private String end_datetime;
    private Integer visit_type_id;
    private Integer visit_location_id;
    private Integer visit_creator;
    private Integer openmrs_visit_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPatientId() {
        return patient_id;
    }

    public void setPatientId(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public String getStartDatetime() {
        return start_datetime;
    }

    public void setStartDatetime(String start_datetime) {
        this.start_datetime = start_datetime;
    }

    public String getEndDatetime() {
        return end_datetime;
    }

    public void setEndDatetime(String end_datetime) {
        this.end_datetime = end_datetime;
    }

    public Integer getVisitTypeId() {
        return visit_type_id;
    }

    public void setVisitTypeId(Integer visit_type_id) {
        this.visit_type_id = visit_type_id;
    }

    public Integer getVisitLocationId() {
        return visit_location_id;
    }

    public void setVisitLocationId(Integer visit_location_id) {
        this.visit_location_id = visit_location_id;
    }

    public Integer getVisitCreator() {
        return visit_creator;
    }

    public void setVisitCreator(Integer visit_creator) {
        this.visit_creator = visit_creator;
    }

    public Integer getOpenmrsVisitId() {
        return openmrs_visit_id;
    }

    public void setOpenmrsVisitId(Integer openmrs_visit_id) {
        this.openmrs_visit_id = openmrs_visit_id;
    }
}
