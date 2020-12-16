package app.intelehealth.client.utilities;

import android.util.Log;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.EncounterDAO;
import app.intelehealth.client.database.dao.ObsDAO;
import app.intelehealth.client.database.dao.PatientsDAO;
import app.intelehealth.client.database.dao.VisitsDAO;
import app.intelehealth.client.models.dto.EncounterDTO;
import app.intelehealth.client.models.dto.ObsDTO;
import app.intelehealth.client.models.dto.PatientDTO;
import app.intelehealth.client.models.dto.VisitDTO;
import app.intelehealth.client.models.pushRequestApiCall.Address;
import app.intelehealth.client.models.pushRequestApiCall.Attribute;
import app.intelehealth.client.models.pushRequestApiCall.Encounter;
import app.intelehealth.client.models.pushRequestApiCall.EncounterProvider;
import app.intelehealth.client.models.pushRequestApiCall.Identifier;
import app.intelehealth.client.models.pushRequestApiCall.Name;
import app.intelehealth.client.models.pushRequestApiCall.Ob;
import app.intelehealth.client.models.pushRequestApiCall.Patient;
import app.intelehealth.client.models.pushRequestApiCall.Person;
import app.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import app.intelehealth.client.models.pushRequestApiCall.Visit;
import app.intelehealth.client.utilities.exception.DAOException;

public class PatientsFrameJson {
    private PatientsDAO patientsDAO = new PatientsDAO();
    private SessionManager session;
    private VisitsDAO visitsDAO = new VisitsDAO();
    private EncounterDAO encounterDAO = new EncounterDAO();
    private ObsDAO obsDAO = new ObsDAO();

    public PushRequestApiCall frameJson() {
        session = new SessionManager(IntelehealthApplication.getAppContext());

        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();

        List<PatientDTO> patientDTOList = null;
        try {
            patientDTOList = patientsDAO.unsyncedPatients();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        List<VisitDTO> visitDTOList = visitsDAO.unsyncedVisits();
        List<EncounterDTO> encounterDTOList = encounterDAO.unsyncedEncounters();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();
        List<Visit> visitList = new ArrayList<>();
        List<Encounter> encounterList = new ArrayList<>();

        if (patientDTOList != null) {
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
                    FirebaseCrashlytics.getInstance().recordException(e);
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
          //  encounterProvider.setProvider(session.getProviderID());
            encounterProvider.setProvider(encounterDTO.getProvideruuid());
            Log.d("DTO","DTO:frame "+ encounterProvider.getProvider());
            encounterProviderList.add(encounterProvider);
            encounter.setEncounterProviders(encounterProviderList);

            if (!encounterDTO.getEncounterTypeUuid().equalsIgnoreCase(UuidDictionary.EMERGENCY)) {
                List<Ob> obsList = new ArrayList<>();
                List<ObsDTO> obsDTOList = obsDAO.obsDTOList(encounterDTO.getUuid());
                Ob ob = new Ob();
                for (ObsDTO obs : obsDTOList) {
                    if (obs != null && obs.getValue() != null) {
                        if (!obs.getValue().isEmpty()) {
                            ob = new Ob();
                            //Do not set obs uuid in case of emergency encounter type .Some error occuring in open MRS if passed

                            ob.setUuid(obs.getUuid());
                            ob.setConcept(obs.getConceptuuid());
                            ob.setValue(obs.getValue());
                            obsList.add(ob);

                        }
                    }
                }
                encounter.setObs(obsList);
            }

            encounter.setLocation(session.getLocationUuid());

            encounterList.add(encounter);
        }


        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);
        pushRequestApiCall.setVisits(visitList);
        pushRequestApiCall.setEncounters(encounterList);
        Gson gson = new Gson();
        Log.d("OBS: ","OBS: "+gson.toJson(encounterList));


        return pushRequestApiCall;
    }
}
