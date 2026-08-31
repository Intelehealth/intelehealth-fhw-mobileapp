package org.intelehealth.app.models;

/**
 * Lightweight data holder for a single row of the Patient's Queue screen.
 *
 * TEMPORARY: populated by {@code VisitsDAO.queueVisits(...)} (a duplicate of
 * {@code olderNotEndedVisits}) purely to wire real visit data into the RN
 * "PatientQueueModule" list while the proper queue feed is being built. The
 * fields mirror the columns the query selects; the native fragment maps them
 * into the props the RN {@code QueueListItem} expects.
 */
public class QueueModel {
    private String patientUuid;
    private String visitUuid;
    private String patientPhoto;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;
    private String gender;
    private String dob;
    private String openmrsId;
    private String visitStartDate;

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getPatientPhoto() {
        return patientPhoto;
    }

    public void setPatientPhoto(String patientPhoto) {
        this.patientPhoto = patientPhoto;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getOpenmrsId() {
        return openmrsId;
    }

    public void setOpenmrsId(String openmrsId) {
        this.openmrsId = openmrsId;
    }

    public String getVisitStartDate() {
        return visitStartDate;
    }

    public void setVisitStartDate(String visitStartDate) {
        this.visitStartDate = visitStartDate;
    }

    /** Convenience: first + middle + last, skipping blanks. */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            sb.append(firstName.trim());
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(middleName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName.trim());
        }
        return sb.toString();
    }
}
