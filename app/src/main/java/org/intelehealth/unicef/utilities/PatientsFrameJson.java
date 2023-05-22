package org.intelehealth.unicef.utilities;

import android.util.Log;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.appointment.dao.AppointmentDAO;
import org.intelehealth.unicef.appointment.model.BookAppointmentRequest;
import org.intelehealth.unicef.database.dao.EncounterDAO;
import org.intelehealth.unicef.database.dao.ObsDAO;
import org.intelehealth.unicef.database.dao.PatientsDAO;
import org.intelehealth.unicef.database.dao.ProviderDAO;
import org.intelehealth.unicef.database.dao.VisitsDAO;
import org.intelehealth.unicef.models.dto.EncounterDTO;
import org.intelehealth.unicef.models.dto.ObsDTO;
import org.intelehealth.unicef.models.dto.PatientDTO;
import org.intelehealth.unicef.models.dto.ProviderDTO;
import org.intelehealth.unicef.models.dto.VisitDTO;
import org.intelehealth.unicef.models.pushRequestApiCall.Address;
import org.intelehealth.unicef.models.pushRequestApiCall.Attribute;
import org.intelehealth.unicef.models.pushRequestApiCall.Encounter;
import org.intelehealth.unicef.models.pushRequestApiCall.EncounterProvider;
import org.intelehealth.unicef.models.pushRequestApiCall.Identifier;
import org.intelehealth.unicef.models.pushRequestApiCall.Name;
import org.intelehealth.unicef.models.pushRequestApiCall.Ob;
import org.intelehealth.unicef.models.pushRequestApiCall.Patient;
import org.intelehealth.unicef.models.pushRequestApiCall.Person;
import org.intelehealth.unicef.models.pushRequestApiCall.Provider;
import org.intelehealth.unicef.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.unicef.models.pushRequestApiCall.Visit;
import org.intelehealth.unicef.utilities.exception.DAOException;

public class PatientsFrameJson {
    private static final String TAG = "PatientsFrameJson";
    private PatientsDAO patientsDAO = new PatientsDAO();
    private SessionManager session;
    private VisitsDAO visitsDAO = new VisitsDAO();
    private EncounterDAO encounterDAO = new EncounterDAO();
    private ObsDAO obsDAO = new ObsDAO();
    private ProviderDAO providerDAO = new ProviderDAO();

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
        }
        List<VisitDTO> visitDTOList = visitsDAO.unsyncedVisits();
        List<EncounterDTO> encounterDTOList = encounterDAO.unsyncedEncounters();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();
        List<Visit> visitList = new ArrayList<>();
        List<Encounter> encounterList = new ArrayList<>();
        List<Provider> providersList = new ArrayList<>();

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
            if (visitDTO.getAttributes().size() > 0) {
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
            Log.d("DTO", "DTO:frame " + encounterProvider.getProvider());
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
        }

        if (providerDetailsDTOList != null && providerDetailsDTOList.size() > 0) {
            Log.d(TAG, "frameJson:providerDetailsDTOList size:  " + providerDetailsDTOList.size());
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
            Log.d("TAG", "frameJson:providerDetailsDTOList is null  ");
        }


        pushRequestApiCall.setAppointments(appointmentRequestList);
        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);
        pushRequestApiCall.setVisits(visitList);
        pushRequestApiCall.setEncounters(encounterList);
        pushRequestApiCall.setProviders(providersList);

        Gson gson = new Gson();
        String value = gson.toJson(pushRequestApiCall);
        Log.d("OBS: ", "OBS: " + value);


        return pushRequestApiCall;
    }


    /**
     * @param uuid the visit uuid of the patient visit records is passed to the function.
     * @return boolean value will be returned depending upon if the row exists in the tbl_visit_attribute tbl
     */
    private boolean speciality_row_exist_check(String uuid) {
        boolean isExists = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=?",
                new String[]{uuid});

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                isExists = true;
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return isExists;
    }
}
