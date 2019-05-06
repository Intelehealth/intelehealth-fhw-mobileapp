package io.intelehealth.client.utilities;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.dao.PatientsDAO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.models.pushRequestApiCall.Address;
import io.intelehealth.client.models.pushRequestApiCall.Attribute;
import io.intelehealth.client.models.pushRequestApiCall.Identifier;
import io.intelehealth.client.models.pushRequestApiCall.Name;
import io.intelehealth.client.models.pushRequestApiCall.Patient;
import io.intelehealth.client.models.pushRequestApiCall.Person;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;

public class PatientsFrameJson {
    PatientsDAO patientsDAO = new PatientsDAO();
    private SessionManager session;

    public PushRequestApiCall frameJson() {
        session = new SessionManager(IntelehealthApplication.getAppContext());

        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();
        List<PatientDTO> patientDTOList = patientsDAO.unsyncedPatients();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();
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
                e.printStackTrace();
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
        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);

        return pushRequestApiCall;
    }
}
