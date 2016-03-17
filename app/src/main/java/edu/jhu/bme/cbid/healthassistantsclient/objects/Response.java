package edu.jhu.bme.cbid.healthassistantsclient.objects;

/**
 * Response information class for Gson data serialization
 */
public class Response {
    private String advice;
    private String prescription;
    private String addl_exams;
    private Integer dr_openmrs_id;


    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getAddlExams() {
        return addl_exams;
    }

    public void setAddlExams(String addl_exams) {
        this.addl_exams = addl_exams;
    }

    public Integer getDrOpenmrsId() {
        return dr_openmrs_id;
    }

    public void setDrOpenmrsId(Integer dr_openmrs_id) {
        this.dr_openmrs_id = dr_openmrs_id;
    }
}
