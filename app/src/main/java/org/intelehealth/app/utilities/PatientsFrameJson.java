package org.intelehealth.app.utilities;

import android.util.Log;

import org.intelehealth.app.utilities.CustomLog;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.models.pushRequestApiCall.Address;
import org.intelehealth.app.models.pushRequestApiCall.Attribute;
import org.intelehealth.app.models.pushRequestApiCall.Encounter;
import org.intelehealth.app.models.pushRequestApiCall.EncounterProvider;
import org.intelehealth.app.models.pushRequestApiCall.Identifier;
import org.intelehealth.app.models.pushRequestApiCall.Name;
import org.intelehealth.app.models.pushRequestApiCall.Ob;
import org.intelehealth.app.models.pushRequestApiCall.Patient;
import org.intelehealth.app.models.pushRequestApiCall.Person;
import org.intelehealth.app.models.pushRequestApiCall.Provider;
import org.intelehealth.app.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.app.models.pushRequestApiCall.Visit;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class PatientsFrameJson {
    private static final String TAG = "PatientsFrameJson";

    /**
     * OpenMRS PatientIdentifierType UUIDs for the ABHA identifiers. These are per-instance records,
     * not global constants: they must exist on the target server or the patient push is rejected.
     */
    private static final String ABHA_ADDRESS_IDENTIFIER_TYPE_UUID = "59077d8f-8bee-4a6f-a1a8-64365a297da6";
    private static final String ABHA_NUMBER_IDENTIFIER_TYPE_UUID = "6ad4e308-33aa-4afc-9879-6033d1984876";

    private PatientsDAO patientsDAO = new PatientsDAO();
    private SessionManager session;
    private VisitsDAO visitsDAO = new VisitsDAO();
    private EncounterDAO encounterDAO = new EncounterDAO();
    private ObsDAO obsDAO = new ObsDAO();
    private ProviderDAO providerDAO = new ProviderDAO();
    private VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();

    public PushRequestApiCall frameJson() {
        session = new SessionManager(IntelehealthApplication.getAppContext());

        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();
        List<BookAppointmentRequest> appointmentRequestList = new ArrayList<>();

        List<PatientDTO> patientDTOList = null;
        try {
            patientDTOList = patientsDAO.unsyncedPatients();
            appointmentRequestList = new AppointmentDAO().getUnsyncedAppointments();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG,e.getMessage());
        }
        List<VisitDTO> visitDTOList = visitsDAO.unsyncedVisits();
        List<EncounterDTO> encounterDTOList = encounterDAO.unsyncedEncounters();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();
        List<Visit> visitList = new ArrayList<>();
        List<Encounter> encounterList = new ArrayList<>();
        List<Provider> providersList = new ArrayList<>();
        Log.d(TAG, "frameJson: visitDTOList : "+new Gson().toJson(visitDTOList));

        if (patientDTOList != null) {
            for (int i = 0; i < patientDTOList.size(); i++) {

                Person person = new Person();
                person.setBirthdate(patientDTOList.get(i).getDateofbirth());
                person.setGender(patientDTOList.get(i).getGender());
                person.setUuid(patientDTOList.get(i).getUuid());
                person.setUuid(patientDTOList.get(i).getUuid());

                personList.add(person);

                List<Name> nameList = new ArrayList<>();
                Name name = new Name();
                name.setFamilyName(patientDTOList.get(i).getLastname());
                name.setGivenName(patientDTOList.get(i).getFirstname());
                name.setMiddleName(patientDTOList.get(i).getMiddlename());
                nameList.add(name);

                List<Address> addressList = new ArrayList<>();
                Address address = new Address();
                address.setAddress1(patientDTOList.get(i).getAddress1());
                address.setAddress2(patientDTOList.get(i).getAddress2());
                address.setCityVillage(patientDTOList.get(i).getCityvillage());
                address.setCountry(patientDTOList.get(i).getCountry());
                address.setPostalCode(patientDTOList.get(i).getPostalcode());
                address.setStateProvince(patientDTOList.get(i).getStateprovince());
                address.setAddress3(patientDTOList.get(i).getAddress3());
                address.setDistrict(patientDTOList.get(i).getDistrict());
                address.setAddress6(patientDTOList.get(i).getAddress6());
                addressList.add(address);


                List<Attribute> attributeList = new ArrayList<>();
                attributeList.clear();
                try {
                    attributeList = patientsDAO.getPatientAttributes(patientDTOList.get(i).getUuid());
                    Log.d("Patient Attribute", "frameJson: PatientAttribute : "+ new Gson().toJson(attributeList));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG,e.getMessage());
                }


                person.setNames(nameList);
                person.setAddresses(addressList);
                person.setAttributes(attributeList);

                Patient patient = new Patient();
                patient.setPerson(patientDTOList.get(i).getUuid());

                List<Identifier> identifierList = new ArrayList<>();

                // Only request a brand-new identifier for patients that aren't registered
                // on the server yet. An already-registered patient (has an openmrs_id)
                // being re-synced after a demographic edit already has one - sending
                // another "assign a new preferred identifier" request for them risks a
                // duplicate/preferred-identifier conflict server-side that can cause the
                // whole Person update (including the edited birthdate) to be rejected.
                String openmrsId = patientDTOList.get(i).getOpenmrsId();
                if (openmrsId == null || openmrsId.trim().isEmpty()) {
                    Identifier identifier = new Identifier();
                    identifier.setIdentifierType("05a29f94-c0ed-11e2-94be-8c13b969e334");
                    identifier.setLocation(session.getLocationUuid());
                    identifier.setPreferred(true);
                    identifierList.add(identifier);
                }

                String abhaAddress = patientDTOList.get(i).getAbhaAddress();
                if (abhaAddress != null && !abhaAddress.isEmpty() && !abhaAddress.equalsIgnoreCase("NA")) {
                    Identifier abhaAddressIdentifier = new Identifier();
                    abhaAddressIdentifier.setIdentifierType(ABHA_ADDRESS_IDENTIFIER_TYPE_UUID);
                    abhaAddressIdentifier.setLocation(session.getLocationUuid());
                    abhaAddressIdentifier.setIdentifier(abhaAddress);
                    identifierList.add(abhaAddressIdentifier);
                }

                String abhaNumber = patientDTOList.get(i).getAbhaNumber();
                if (abhaNumber != null && !abhaNumber.isEmpty() && !abhaNumber.equalsIgnoreCase("NA")) {
                    Identifier abhaNumberIdentifier = new Identifier();
                    abhaNumberIdentifier.setIdentifierType(ABHA_NUMBER_IDENTIFIER_TYPE_UUID);
                    abhaNumberIdentifier.setLocation(session.getLocationUuid());
                    abhaNumberIdentifier.setIdentifier(abhaNumber);
                    identifierList.add(abhaNumberIdentifier);
                }

                patient.setIdentifiers(identifierList);
                patientList.add(patient);


            }
        }
        for (VisitDTO visitDTO : visitDTOList) {
            Visit visit = new Visit();
           /* Multiple visit attributes getting sync - when we restrict to sync multiple visit
            attributes for same visit then this condition not allowing to sync visit with 0 attributes.*/


           /* if (visitDTO.getAttributes().size() > 0) {*/

            //this condition is changed for visit is not closing even we close the visit
            if (!visitDTO.getAttributes().isEmpty() || visitDTO.getEnddate() != null) {
                visit.setLocation(visitDTO.getLocationuuid());
                visit.setPatient(visitDTO.getPatientuuid());
                visit.setStartDatetime(visitDTO.getStartdate());
                visit.setUuid(visitDTO.getUuid());
                visit.setVisitType(visitDTO.getVisitTypeUuid());
                visit.setStopDatetime(visitDTO.getEnddate());
                visit.setAttributes(visitDTO.getAttributes());
                visitList.add(visit);
         }

        }

        for (EncounterDTO encounterDTO : encounterDTOList) {
            Encounter encounter = new Encounter();

            encounter = new Encounter();
            encounter.setUuid(encounterDTO.getUuid());
            encounter.setEncounterDatetime(encounterDTO.getEncounterTime());//visit start time
            encounter.setEncounterType(encounterDTO.getEncounterTypeUuid());//right know it is static
            encounter.setPatient(visitsDAO.patientUuidByViistUuid(encounterDTO.getVisituuid()));
            encounter.setVisit(encounterDTO.getVisituuid());
            encounter.setVoided(encounterDTO.getVoided());

            List<EncounterProvider> encounterProviderList = new ArrayList<>();
            EncounterProvider encounterProvider = new EncounterProvider();
            encounterProvider.setEncounterRole("73bbb069-9781-4afc-a9d1-54b6b2270e04");
            //  encounterProvider.setProvider(session.getProviderID());
            encounterProvider.setProvider(encounterDTO.getProvideruuid());
            CustomLog.d("DTO", "DTO:frame " + encounterProvider.getProvider());
            encounterProviderList.add(encounterProvider);
            encounter.setEncounterProviders(encounterProviderList);

            if (!encounterDTO.getEncounterTypeUuid().equalsIgnoreCase(UuidDictionary.EMERGENCY)) {
                List<Ob> obsList = new ArrayList<>();
                List<ObsDTO> obsDTOList = obsDAO.obsDTOList(encounterDTO.getUuid());
                CustomLog.d("OBS: ", "OBS: obsDTOList" + new Gson().toJson(obsDTOList));
                Ob ob = new Ob();
                for (ObsDTO obs : obsDTOList) {
                    if (obs != null && obs.getValue() != null) {
                        if (!obs.getValue().isEmpty()) {
                            ob = new Ob();
                            //Do not set obs uuid in case of emergency encounter type .Some error occuring in open MRS if passed

                            ob.setUuid(obs.getUuid());
                            ob.setConcept(obs.getConceptuuid());
                            ob.setValue(obs.getValue());
                            ob.setComments(obs.getComments());
                            obsList.add(ob);
                            CustomLog.d("OBS: ", "OBS: " + new Gson().toJson(ob));
                        }
                    }
                }
                encounter.setObs(obsList);
            }

            encounter.setLocation(session.getLocationUuid());

            // encounterList.add(encounter);
            if (speciality_row_exist_check(encounter.getVisit())) {
                encounterList.add(encounter);
            }

        }


        //ui2.0 - for provider profile details
        List<ProviderDTO> providerDetailsDTOList = null;
        try {
            providerDetailsDTOList = providerDAO.unsyncedProviderDetails(session.getProviderID());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG,e.getMessage());
        }

        if (providerDetailsDTOList != null && providerDetailsDTOList.size() > 0) {
            CustomLog.d(TAG, "frameJson:providerDetailsDTOList size:  " + providerDetailsDTOList.size());
            for (ProviderDTO providerDTO : providerDetailsDTOList) {
                Provider provider = new Provider();
                // if (visitDTO.getAttributes().size() > 0) {
                provider.setFamilyName(providerDTO.getFamilyName());
                provider.setGivenName(providerDTO.getGivenName());
                provider.setEmailId(providerDTO.getEmailId());
                provider.setDateofbirth(providerDTO.getDateofbirth());
                provider.setGender(providerDTO.getGender());
                provider.setTelephoneNumber(providerDTO.getTelephoneNumber());
                provider.setProviderId(providerDTO.getProviderId());
                provider.setCountryCode(providerDTO.getCountryCode());
                provider.setMiddle_name(providerDTO.getMiddle_name());

                /*provider info is not updating using the apis in MyProfileActivity
                thus there is no need to attach any details in push request model.
                Refer ticket IDA-913 for more details.*/

                //providersList.add(provider);
                //}

            }
        } else {
            CustomLog.d("TAG", "frameJson:providerDetailsDTOList is null  ");
        }


        pushRequestApiCall.setAppointments(appointmentRequestList);
        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);
        pushRequestApiCall.setVisits(visitList);
        pushRequestApiCall.setEncounters(encounterList);
        pushRequestApiCall.setProviders(providersList);

        Gson gson = new Gson();
        String value = gson.toJson(pushRequestApiCall);
        CustomLog.d("OBS: ", "OBSpushRequestApiCall: " + value);


        return pushRequestApiCall;
    }


    /**
     * Whether a specialization has been chosen for this visit, which is what holds an encounter back
     * from the push until the visit has been submitted at Visit Summary.
     *
     * Matched on the SPECIALITY attribute type specifically. This used to ask whether the visit had
     * *any* visit attribute, which held only while every attribute was written by the upload action.
     * The ABHA address is written at visit creation instead, so on an ABHA-linked patient the check
     * passed from the moment the visit existed and that visit's encounters went up before a
     * specialization had been picked.
     *
     * Delegating to isAttributeExistForVisit puts this on the same predicate as the identically named
     * check in VisitSummaryActivity_New, which decides whether the screen offers the dropdown. The two
     * must agree, or an encounter can be pushed for a visit the screen still considers unsubmitted.
     *
     * @param uuid the visit uuid of the patient visit records is passed to the function.
     * @return whether a SPECIALITY attribute exists for the visit
     */
    private boolean speciality_row_exist_check(String uuid) {
        if (uuid == null) return false;
        return visitAttributeListDAO.isAttributeExistForVisit(uuid, UuidDictionary.SPECIALITY);
    }
}
