package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.R;
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
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.StringUtils;
import io.intelehealth.client.views.activites.PatientDetailActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

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
        ArrayList<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<PatientAttributesDTO>();
        String uuid = UUID.randomUUID().toString();
        PatientDTO patientdto = new PatientDTO();
        patientdto.setUuid(uuid);
        Gson gson = new Gson();

        patientdto.setFirstname(StringUtils.getValue(firstname.getValue()));
        patientdto.setMiddlename(StringUtils.getValue(middlename.getValue()));
        patientdto.setLastname(StringUtils.getValue(lastname.getValue()));
        patientdto.setPhonenumber(StringUtils.getValue(phonenumber.getValue()));
        patientdto.setDateofbirth(StringUtils.getValue(dateofbirth.getValue()));
        patientdto.setAddress1(StringUtils.getValue(address.getValue()));
        patientdto.setAddress2(StringUtils.getValue(address2.getValue()));
        patientdto.setCityvillage(StringUtils.getValue(village.getValue()));
        patientdto.setPostalcode(StringUtils.getValue(postalcode.getValue()));
//                patientdto.setEconomic(StringUtils.getValue(m));
        patientdto.setStateprovince(StringUtils.getValue(state.getValue()));
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
        patientAttributesDTO.setValue(StringUtils.getValue("OBC"));
//                patientAttributesDTO.setSycd(false);
        patientAttributesDTOList.add(patientAttributesDTO);
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
            Boolean b = patientsDAO.insertPatientToDB(patientdto);
            if (b) {
                Logger.logD(TAG, "inserted");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplication().startActivity(i);
            }
            if (NetworkConnection.isOnline(getApplication())) {
                patientApiCall();
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

    }

    public Adapter countryAdapter() {
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(getApplication(),
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerCountry.setAdapter(countryAdapter);
        return countryAdapter;
    }

    public Adapter casteAdapter() {
        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(getApplication(),
                R.array.caste, android.R.layout.simple_spinner_item);
        casteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.spinnerCaste.setAdapter(casteAdapter);
        return casteAdapter;
    }


//    public Single<PatientDTO> insertPatient(PatientDTO patient) {
//
//        Logger.logD(TAG, "firstname" + userMutableLiveData.getValue());
//        return new Single<PatientDTO>() {
//            @Override
//            protected void subscribeActual(SingleObserver<? super PatientDTO> observer) {
//                try (SQLiteDatabase db4 = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()) {
//                    ContentValues patientEntries = new ContentValues();
//                    patientEntries.put("uuid", AppConstants.NEW_UUID);
//                    patientEntries.put("first_name", patient.getFirstname());
//                    patientEntries.put("middle_name", patient.getMiddlename());
//                    patientEntries.put("last_name", patient.getLastname());
//                    patientEntries.put("date_of_birth", patient.getDateofbirth());
//                    patientEntries.put("phone_number", patient.getPhonenumber());
//                    patientEntries.put("address1", patient.getAddress1());
//                    patientEntries.put("address2", patient.getAddress2());
//                    patientEntries.put("city_village", patient.getCityvillage());
//                    patientEntries.put("state_province", patient.getStateprovince());
//                    patientEntries.put("postal_code", patient.getPostalcode());
//                    patientEntries.put("country", patient.getCountry());
//                    patientEntries.put("gender", patient.getGender());
//                    long a = db4.insert(
//                            "tbl_patient",
//                            null,
//                            patientEntries
//                    );
//                    Logger.logD(TAG, "insertion " + a);
//                } catch (SQLiteException sqle) {
//                    Logger.logE(TAG, "sql exception", sqle);
//                }
//
//            }
//        };
//    }

    public void patientApiCall() {
        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();

        Person person = new Person();
        person.setBirthdate("");
        person.setGender("");
        person.setUuid("");

        List<Name> nameList = new ArrayList<>();
        Name name = new Name();
        name.setFamilyName("");
        name.setGivenName("");
        name.setMiddleName("");
        nameList.add(name);

        List<Address> addressList = new ArrayList<>();
        Address address = new Address();
        address.setAddress1("");
        address.setAddress2("");
        address.setCityVillage("");
        address.setCountry("");
        address.setPostalCode("");
        address.setStateProvince("");
        addressList.add(address);

        List<Attribute> attributeList = new ArrayList<>();
        Attribute attribute = new Attribute();
        attribute.setAttributeType("");
        attribute.setValue("");
        attributeList.add(attribute);

        person.setNames(nameList);
        person.setAddresses(addressList);
        person.setAttributes(attributeList);
        Patient patient = new Patient();

        patient.setPerson("8ee4484b-8718-45c8-8e41-8b9d3ce28e4f");

        List<Identifier> identifierList = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setIdentifierType("05a29f94-c0ed-11e2-94be-8c13b969e334");
        identifier.setLocation(session.getLocationUuid());
        identifier.setPreferred(true);
        identifierList.add(identifier);

        patient.setIdentifiers(identifierList);


        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);

        String encoded = session.getEncoded();

        String url = "http://142.93.221.37:8080/EMR-Middleware/webapi/push/pushdata";
        Observable<PushResponseApiCall> pushResponseApiCallObservable = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url, "Basic " + encoded, pushRequestApiCall);
        pushResponseApiCallObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<PushResponseApiCall>() {
                    @Override
                    public void onNext(PushResponseApiCall pushResponseApiCall) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


}

