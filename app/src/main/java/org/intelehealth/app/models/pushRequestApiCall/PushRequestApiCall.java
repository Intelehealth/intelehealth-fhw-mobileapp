package org.intelehealth.app.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.app.appointment.model.BookAppointmentRequest;

import java.util.List;

public class PushRequestApiCall {

    @SerializedName("appointments")
    @Expose
    private List<BookAppointmentRequest> appointments = null;

    @SerializedName("persons")
    @Expose
    private List<Person> persons = null;
    @SerializedName("patients")
    @Expose
    private List<Patient> patients = null;
    @SerializedName("visits")
    @Expose
    private List<Visit> visits = null;

    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    @SerializedName("encounters")
    @Expose
    private List<Encounter> encounters = null;
    @SerializedName("providers")
    @Expose
    private List<Provider> providers = null;


    public List<BookAppointmentRequest> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<BookAppointmentRequest> appointments) {
        this.appointments = appointments;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }

}
