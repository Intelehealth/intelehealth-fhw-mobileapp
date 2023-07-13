package org.intelehealth.ezazi.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.executor.TaskCompleteListener;
import org.intelehealth.ezazi.executor.TaskExecutor;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.models.dto.VisitAttribute_Speciality;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.models.pushRequestApiCall.Address;
import org.intelehealth.ezazi.models.pushRequestApiCall.Attribute;
import org.intelehealth.ezazi.models.pushRequestApiCall.Encounter;
import org.intelehealth.ezazi.models.pushRequestApiCall.EncounterProvider;
import org.intelehealth.ezazi.models.pushRequestApiCall.Identifier;
import org.intelehealth.ezazi.models.pushRequestApiCall.Name;
import org.intelehealth.ezazi.models.pushRequestApiCall.Ob;
import org.intelehealth.ezazi.models.pushRequestApiCall.Patient;
import org.intelehealth.ezazi.models.pushRequestApiCall.Person;
import org.intelehealth.ezazi.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.ezazi.models.pushRequestApiCall.Visit;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class PatientsFrameJson {
    private static final String TAG = "PatientsFrameJson";
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

        Log.e("PushRequestApiCall", "frameJson: " + new Gson().toJson(encounterList));

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

            if (encounterDTO.getEncounterTypeUuid() != null && !encounterDTO.getEncounterTypeUuid().isEmpty()) {
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
                            // if (!obs.getValue().isEmpty()) {  // commented for the case - if user update any value with empty field then it should proceed
                            ob = new Ob();
                            //Do not set obs uuid in case of emergency encounter type .Some error occuring in open MRS if passed

                            ob.setUuid(obs.getUuid());
                            ob.setConcept(obs.getConceptuuid());
                            ob.setValue(obs.getValue());
                            ob.setComment(obs.getComment());
                            obsList.add(ob);

                            //   }
                        }
                    }
                    encounter.setObs(obsList);
                }

                encounter.setLocation(session.getLocationUuid());

                // encounterList.add(encounter);
                //if (speciality_row_exist_check(encounter.getVisit())){
                encounterList.add(encounter);
                //}
            }

        }

        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);
        pushRequestApiCall.setVisits(visitList);
        pushRequestApiCall.setEncounters(encounterList);
        Gson gson = new Gson();
        Log.v("PushPayload: ", "PushPayload: " + gson.toJson(pushRequestApiCall));


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
