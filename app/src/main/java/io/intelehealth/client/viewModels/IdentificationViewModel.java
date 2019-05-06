package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dao.PatientsDAO;
import io.intelehealth.client.dto.PatientAttributesDTO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.models.pushRequestApiCall.Address;
import io.intelehealth.client.models.pushRequestApiCall.Attribute;
import io.intelehealth.client.models.pushRequestApiCall.Identifier;
import io.intelehealth.client.models.pushRequestApiCall.Name;
import io.intelehealth.client.models.pushRequestApiCall.Patient;
import io.intelehealth.client.models.pushRequestApiCall.Person;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import io.intelehealth.client.models.pushResponseApiCall.PushResponseApiCall;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.StringUtils;
import io.intelehealth.client.views.activites.PatientDetailActivity;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;

public class IdentificationViewModel extends AndroidViewModel {
    private static final String TAG = IdentificationViewModel.class.getSimpleName();
    private SessionManager session;
    public MutableLiveData<String> firstname = new MediatorLiveData<>();
    public MutableLiveData<String> middlename = new MediatorLiveData<>();
    public MutableLiveData<String> lastname = new MediatorLiveData<>();
    public MutableLiveData<String> gender = new MediatorLiveData<>();
    public MutableLiveData<String> dateofbirth = new MediatorLiveData<>();
    public MutableLiveData<String> phonenumber = new MediatorLiveData<>();
    public MutableLiveData<String> country = new MediatorLiveData<>();
    public MutableLiveData<String> state = new MediatorLiveData<>();
    public MutableLiveData<String> village = new MediatorLiveData<>();
    public MutableLiveData<String> address = new MediatorLiveData<>();
    public MutableLiveData<String> address2 = new MediatorLiveData<>();
    public MutableLiveData<String> postalcode = new MediatorLiveData<>();
    public MutableLiveData<String> sondaughter = new MediatorLiveData<>();
    public MutableLiveData<String> occupation = new MediatorLiveData<>();
    public MutableLiveData<String> caste = new MediatorLiveData<>();
    public MutableLiveData<String> economicstatus = new MediatorLiveData<>();
    public MutableLiveData<String> educationlevel = new MediatorLiveData<>();
    public MutableLiveData<PatientDTO> userMutableLiveData = new MediatorLiveData<>();
    PatientsDAO patientsDAO = new PatientsDAO();
    String uuid = "";
    PatientDTO patientdto = new PatientDTO();

    public IdentificationViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application);
    }

    public LiveData<PatientDTO> getPatient() {
        if (userMutableLiveData == null) {
            userMutableLiveData = new MutableLiveData<>();
        }

        return userMutableLiveData;
    }

    public void onPatientCreateClicked() {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = UUID.randomUUID().toString();

        patientdto.setUuid(uuid);
        Gson gson = new Gson();

        patientdto.setFirstname(StringUtils.getValue(firstname.getValue()));
        patientdto.setMiddlename(StringUtils.getValue(middlename.getValue()));
        patientdto.setLastname(StringUtils.getValue(lastname.getValue()));
        patientdto.setPhonenumber(StringUtils.getValue(phonenumber.getValue()));
        patientdto.setDateofbirth(StringUtils.getValue(DateAndTimeUtils.formatDateFromOnetoAnother(dateofbirth.getValue(), "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd")));
        patientdto.setAddress1(StringUtils.getValue(address.getValue()));
        patientdto.setAddress2(StringUtils.getValue(address2.getValue()));
        patientdto.setCityvillage(StringUtils.getValue(village.getValue()));
        patientdto.setPostalcode(StringUtils.getValue(postalcode.getValue()));
        patientdto.setCountry(StringUtils.getValue(country.getValue()));
//                patientdto.setEconomic(StringUtils.getValue(m));
        patientdto.setStateprovince(StringUtils.getValue(state.getValue()));
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
        patientAttributesDTO.setValue(StringUtils.getValue(caste.getValue()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
        patientAttributesDTO.setValue(StringUtils.getValue(phonenumber.getValue()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
        patientAttributesDTO.setValue(StringUtils.getValue(sondaughter.getValue()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
        patientAttributesDTO.setValue(StringUtils.getValue(occupation.getValue()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
        patientAttributesDTO.setValue(StringUtils.getValue(economicstatus.getValue()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
        patientAttributesDTO.setValue(StringUtils.getValue(educationlevel.getValue()));
        patientAttributesDTOList.add(patientAttributesDTO);
        Logger.logD(TAG, "PatientAttribute list" + patientAttributesDTOList.size());
        patientdto.setPatientAttributesDTOList(patientAttributesDTOList);
        patientdto.setSyncd(false);
        Logger.logD("patient json : ", "Json : " + gson.toJson(patientdto, PatientDTO.class));

//        patient.setGender(gender.getValue());
//        patient.setCountry(country.getValue());
//        patient.setEconomic(economicstatus.getValue());
//        patient.setEducation(educationlevel.getValue());
        userMutableLiveData.setValue(patientdto);
//        insertPatient(patient);
        try {
            Logger.logD(TAG, "insertpatinet ");
            Boolean b = patientsDAO.insertPatientToDB(patientdto, uuid);
            if (b) {
                Logger.logD(TAG, "inserted");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirstname() + " " + patientdto.getLastname());
                i.putExtra("tag", "newPatient");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            }
            if (NetworkConnection.isOnline(getApplication())) {
                patientApiCall();
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

    }

    public void onCountrySelectItem(AdapterView<?> parent, View view, int pos, long id) {
        Logger.logD(TAG, "onitem selected" + parent.getSelectedItem());
        country.setValue(parent.getSelectedItem().toString());
        //pos                                 get selected item position
        //view.getText()                      get lable of selected item
        //parent.getAdapter().getItem(pos)    get item by pos
        //parent.getAdapter().getCount()      get item count
        //parent.getCount()                   get item count
        //parent.getSelectedItem()            get selected item
        //and other...
    }

    public void onStateSelectItem(AdapterView<?> parent, View view, int pos, long id) {
        Logger.logD(TAG, "onitem selected" + parent.getSelectedItem());
        state.setValue(parent.getSelectedItem().toString());
        //pos                                 get selected item position
        //view.getText()                      get lable of selected item
        //parent.getAdapter().getItem(pos)    get item by pos
        //parent.getAdapter().getCount()      get item count
        //parent.getCount()                   get item count
        //parent.getSelectedItem()            get selected item
        //and other...
    }

    public void onCasteSelectItem(AdapterView<?> parent, View view, int pos, long id) {
        Logger.logD(TAG, "onitem selected" + parent.getSelectedItem());
        caste.setValue(parent.getSelectedItem().toString());
        //pos                                 get selected item position
        //view.getText()                      get lable of selected item
        //parent.getAdapter().getItem(pos)    get item by pos
        //parent.getAdapter().getCount()      get item count
        //parent.getCount()                   get item count
        //parent.getSelectedItem()            get selected item
        //and other...
    }

    public void onEconomicSelectItem(AdapterView<?> parent, View view, int pos, long id) {
        Logger.logD(TAG, "onitem selected" + parent.getSelectedItem());
        economicstatus.setValue(parent.getSelectedItem().toString());
        //pos                                 get selected item position
        //view.getText()                      get lable of selected item
        //parent.getAdapter().getItem(pos)    get item by pos
        //parent.getAdapter().getCount()      get item count
        //parent.getCount()                   get item count
        //parent.getSelectedItem()            get selected item
        //and other...
    }

    public void onEducationSelectItem(AdapterView<?> parent, View view, int pos, long id) {
        Logger.logD(TAG, "onitem selected" + parent.getSelectedItem());
        educationlevel.setValue(parent.getSelectedItem().toString());
        //pos                                 get selected item position
        //view.getText()                      get lable of selected item
        //parent.getAdapter().getItem(pos)    get item by pos
        //parent.getAdapter().getCount()      get item count
        //parent.getCount()                   get item count
        //parent.getSelectedItem()            get selected item
        //and other...
    }

    public void onSplitTypeChanged(RadioButton radioGroup, int id) {
        // ...
        Logger.logD(TAG, "description" + radioGroup + id);

    }

    public void patientApiCall() {
        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();

        Person person = new Person();
        person.setBirthdate(patientdto.getDateofbirth());
        person.setGender(gender.getValue());
        person.setUuid(uuid);
        personList.add(person);

        List<Name> nameList = new ArrayList<>();
        Name name = new Name();
        name.setFamilyName(patientdto.getLastname());
        name.setGivenName(patientdto.getFirstname());
        name.setMiddleName(patientdto.getMiddlename());
        nameList.add(name);

        List<Address> addressList = new ArrayList<>();
        Address address = new Address();
        address.setAddress1(patientdto.getAddress1());
        address.setAddress2(patientdto.getAddress2());
        address.setCityVillage(patientdto.getCityvillage());
        address.setCountry(patientdto.getCountry());
        address.setPostalCode(patientdto.getPostalcode());
        address.setStateProvince(patientdto.getStateprovince());
        addressList.add(address);

        Attribute attribute = new Attribute();
        List<Attribute> attributeList = new ArrayList<>();
        try {
            attributeList = patientsDAO.getPatientAttributes(uuid);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if (addressList != null && addressList.size() != 0) {
            for (int i = 0; i < addressList.size(); i++) {
                attribute.setAttributeType(attributeList.get(i).getAttributeType());
                attribute.setValue(attributeList.get(i).getValue());
                attributeList.add(attribute);
            }
        }


        person.setNames(nameList);
        person.setAddresses(addressList);
        person.setAttributes(attributeList);
        Patient patient = new Patient();

        patient.setPerson(uuid);

        List<Identifier> identifierList = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setIdentifierType("05a29f94-c0ed-11e2-94be-8c13b969e334");
        identifier.setLocation(session.getLocationUuid());
        identifier.setPreferred(true);
        identifierList.add(identifier);

        patient.setIdentifiers(identifierList);

        patientList.add(patient);

        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);

        String encoded = session.getEncoded();

        String url = "http://" + session.getServerUrl() + ":8080/EMR-Middleware/webapi/push/pushdata";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Single<PushResponseApiCall> pushResponseApiCallObservable = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url, "Basic " + encoded, pushRequestApiCall);
        pushResponseApiCallObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<PushResponseApiCall>() {
                    @Override
                    public void onSuccess(PushResponseApiCall pushResponseApiCall) {
                        Logger.logD(TAG, "sucess" + pushResponseApiCall);
                        for (int i = 0; i < pushResponseApiCall.getData().getPatientlist().size(); i++) {
                            try {
                                patientsDAO.updateOpemmrsId(pushResponseApiCall.getData().getPatientlist().get(i).getOpenmrsId(), pushResponseApiCall.getData().getPatientlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Onerror " + e.getMessage());
                    }
                });
    }

}

