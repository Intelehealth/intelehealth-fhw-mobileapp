package io.intelehealth.android.objects;

/**
 * Observation information class for Gson data serialization
 */
public class Obs {
    private Integer id;
    private Integer patient_id;
    private Integer visit_id;
    private String value;
    private Integer concept_id;
    private Integer creator;
    private Integer openmrs_encounter_id;
    private Integer openmrs_obs_id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPatientId() {
        return patient_id;
    }

    public void setPatientId(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public Integer getVisitId() {
        return visit_id;
    }

    public void setVisitId(Integer visit_id) {
        this.visit_id = visit_id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getConceptId() {
        return concept_id;
    }

    public void setConceptId(Integer concept_id) {
        this.concept_id = concept_id;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Integer getOpenmrsEncounterId() {
        return openmrs_encounter_id;
    }

    public void setOpenmrsEncounterId(Integer openmrs_encounter_id) {
        this.openmrs_encounter_id = openmrs_encounter_id;
    }

    public Integer getOpenmrsObsId() {
        return openmrs_obs_id;
    }

    public void setOpenmrsObsId(Integer openmrs_obs_id) {
        this.openmrs_obs_id = openmrs_obs_id;
    }
}
