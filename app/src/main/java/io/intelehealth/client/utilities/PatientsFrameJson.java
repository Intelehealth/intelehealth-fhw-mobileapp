package io.intelehealth.client.utilities;

import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.database.dao.EmergencyEncounterDAO;
import io.intelehealth.client.database.dao.EncounterDAO;
import io.intelehealth.client.database.dao.ObsDAO;
import io.intelehealth.client.database.dao.PatientsDAO;
import io.intelehealth.client.database.dao.VisitsDAO;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.models.dto.PatientDTO;
import io.intelehealth.client.models.dto.VisitDTO;
import io.intelehealth.client.models.pushRequestApiCall.Address;
import io.intelehealth.client.models.pushRequestApiCall.Attribute;
import io.intelehealth.client.models.pushRequestApiCall.Encounter;
import io.intelehealth.client.models.pushRequestApiCall.EncounterProvider;
import io.intelehealth.client.models.pushRequestApiCall.Identifier;
import io.intelehealth.client.models.pushRequestApiCall.Name;
import io.intelehealth.client.models.pushRequestApiCall.Ob;
import io.intelehealth.client.models.pushRequestApiCall.Patient;
import io.intelehealth.client.models.pushRequestApiCall.Person;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import io.intelehealth.client.models.pushRequestApiCall.Visit;
import io.intelehealth.client.utilities.exception.DAOException;

public class PatientsFrameJson {
    PatientsDAO patientsDAO = new PatientsDAO();
    private SessionManager session;
    VisitsDAO visitsDAO = new VisitsDAO();
    EncounterDAO encounterDAO = new EncounterDAO();
    ObsDAO obsDAO = new ObsDAO();
    EmergencyEncounterDAO emergencyEncounterDAO = new EmergencyEncounterDAO();
    SQLiteDatabase db = null;

    public PushRequestApiCall frameJson() {
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        session = new SessionManager(IntelehealthApplication.getAppContext());

        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();

        List<PatientDTO> patientDTOList = patientsDAO.unsyncedPatients();
        List<VisitDTO> visitDTOList = visitsDAO.unsyncedVisits();
        List<EncounterDTO> encounterDTOList = encounterDAO.unsyncedEncounters();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();
        List<Visit> visitList = new ArrayList<>();
        List<Encounter> encounterList = new ArrayList<>();

        for (int i = 0; i < patientDTOList.size(); i++) {

            Person person = new Person();
            person.setBirthdate(patientDTOList.get(i).getDateofbirth());
            person.setGender(patientDTOList.get(i).getGender());
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
            addressList.add(address);


            List<Attribute> attributeList = new ArrayList<>();
            attributeList.clear();
            try {
                attributeList = patientsDAO.getPatientAttributes(patientDTOList.get(i).getUuid());
            } catch (DAOException e) {
                Crashlytics.getInstance().core.logException(e);
            }


            person.setNames(nameList);
            person.setAddresses(addressList);
            person.setAttributes(attributeList);
            Patient patient = new Patient();

            patient.setPerson(patientDTOList.get(i).getUuid());

            List<Identifier> identifierList = new ArrayList<>();
            Identifier identifier = new Identifier();
            identifier.setIdentifierType("05a29f94-c0ed-11e2-94be-8c13b969e334");
            identifier.setLocation(session.getLocationUuid());
            identifier.setPreferred(true);
            identifierList.add(identifier);

            patient.setIdentifiers(identifierList);
            patientList.add(patient);


        }
        for (VisitDTO visitDTO : visitDTOList) {
            Visit visit = new Visit();
            visit.setLocation(visitDTO.getLocationuuid());
            visit.setPatient(visitDTO.getPatientuuid());
            visit.setStartDatetime(visitDTO.getStartdate());
            visit.setUuid(visitDTO.getUuid());
            visit.setVisitType(visitDTO.getVisitTypeUuid());
            visit.setStopDatetime(visitDTO.getEnddate());
            visitList.add(visit);

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
            encounterProvider.setProvider(session.getProviderID());
            encounterProviderList.add(encounterProvider);
            encounter.setEncounterProviders(encounterProviderList);

            List<Ob> obsList = new ArrayList<>();
            List<ObsDTO> obsDTOList = obsDAO.obsDTOList(encounterDTO.getUuid());
            Ob ob = new Ob();
            for (ObsDTO obs : obsDTOList) {
                if (obs != null && obs.getValue() != null) {
                    if (!obs.getValue().isEmpty()) {
                        ob = new Ob();
                        //Do not set obs uuid in case of emergency encounter type .Some error occuring in open MRS if passed
                        if (!encounterDTO.getEncounterTypeUuid().equalsIgnoreCase(UuidDictionary.EMERGENCY)) {

                            ob.setUuid(obs.getUuid());
                        }
                        ob.setConcept(obs.getConceptuuid());
                        ob.setValue(obs.getValue());
                        obsList.add(ob);
                    }
                }
            }
            encounter.setObs(obsList);
            encounter.setLocation(session.getLocationUuid());

            encounterList.add(encounter);
        }


        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);
        pushRequestApiCall.setVisits(visitList);
        pushRequestApiCall.setEncounters(encounterList);

//        EmergencyEncounterDAO emergencyEncounterDAO=new EmergencyEncounterDAO();
//        emergencyEncounterDAO.checkEmergency();

        return pushRequestApiCall;
    }
}
