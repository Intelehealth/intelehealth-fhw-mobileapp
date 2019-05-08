package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.dao.PatientsDAO;
import io.intelehealth.client.dao.PullDataDAO;
import io.intelehealth.client.dto.PatientAttributesDTO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NetworkConnection;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.StringUtils;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.views.activites.PatientDetailActivity;

public class IdentificationViewModel extends AndroidViewModel {
    private static final String TAG = IdentificationViewModel.class.getSimpleName();
    private SessionManager session;
    public MutableLiveData<String> firstname = new MediatorLiveData<>();
    public MutableLiveData<String> firstnameerror = new MutableLiveData<>();
    public MutableLiveData<String> middlename = new MediatorLiveData<>();
    public MutableLiveData<String> lastname = new MediatorLiveData<>();
    public MutableLiveData<String> lastnameerror = new MediatorLiveData<>();
    public MutableLiveData<String> gender = new MediatorLiveData<>();
    public MutableLiveData<String> gendererror = new MediatorLiveData<>();
    public MutableLiveData<String> dateofbirth = new MediatorLiveData<>();
    public MutableLiveData<String> dateofbirtherror = new MediatorLiveData<>();
    public MutableLiveData<String> phonenumber = new MediatorLiveData<>();
    public MutableLiveData<String> country = new MediatorLiveData<>();
    public MutableLiveData<String> state = new MediatorLiveData<>();
    public MutableLiveData<String> village = new MediatorLiveData<>();
    public MutableLiveData<String> villageerror = new MediatorLiveData<>();
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
        patientdto.setGender(StringUtils.getValue(gender.getValue()));
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

        if (firstname.getValue() == null) {
            firstnameerror.setValue("First name is missing");
            return;
        } else {
            firstnameerror.setValue("");
        }
        if (lastname.getValue() == null) {
            lastnameerror.setValue("last name is missing");
            return;
        } else {
            lastnameerror.setValue("");
        }
        if (dateofbirth.getValue() == null) {
            dateofbirtherror.setValue("Date of birth missing");
            return;
        } else
            dateofbirtherror.setValue("");
        if (gender.getValue() == null) {
            gendererror.setValue("gender is missing");
            return;
        } else
            gendererror.setValue("");
        if (village.getValue() == null) {
            villageerror.setValue("village is missing");
            return;
        } else
            villageerror.setValue("");
        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean b = patientsDAO.insertPatientToDB(patientdto, uuid);

            if (NetworkConnection.isOnline(getApplication())) {
//                patientApiCall();
//                frameJson();
                PullDataDAO pullDataDAO=new PullDataDAO();
                pullDataDAO.pushDataApi();
            }
            if (b) {
                Logger.logD(TAG, "inserted");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirstname() + " " + patientdto.getLastname());
                i.putExtra("tag", "newPatient");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

    }
    public void onPatientUpdateClicked(Patient patientdto) {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = patientdto.getUuid();

        patientdto.setUuid(uuid);
        Gson gson = new Gson();

        patientdto.setFirst_name(StringUtils.getValue(patientdto.getFirst_name()));
        patientdto.setMiddle_name(StringUtils.getValue(patientdto.getMiddle_name()));
        patientdto.setLast_name(StringUtils.getValue(patientdto.getLast_name()));
        patientdto.setPhone_number(StringUtils.getValue(patientdto.getPhone_number()));
        patientdto.setGender(StringUtils.getValue(patientdto.getGender()));
        patientdto.setDate_of_birth(StringUtils.getValue(DateAndTimeUtils.formatDateFromOnetoAnother(patientdto.getDate_of_birth(), "yyyy-MM-dd", "dd-mm-yyyy")));
        patientdto.setAddress1(StringUtils.getValue(patientdto.getAddress1()));
        patientdto.setAddress2(StringUtils.getValue(patientdto.getAddress2()));
        patientdto.setCity_village(StringUtils.getValue(patientdto.getCity_village()));
        patientdto.setPostal_code(StringUtils.getValue(patientdto.getPostal_code()));
        patientdto.setCountry(StringUtils.getValue(patientdto.getCountry()));
//                patientdto.setEconomic(StringUtils.getValue(m));
        patientdto.setState_province(StringUtils.getValue(patientdto.getState_province()));
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
        //patientdto.setPatientAttributesDTOList(patientAttributesDTOList);

        Logger.logD("patient json onPatientUpdateClicked : ", "Json : " + gson.toJson(patientdto, Patient.class));

        if (firstname.getValue() == null) {
            firstnameerror.setValue("First name is missing");
            return;
        } else {
            firstnameerror.setValue("");
        }
        if (lastname.getValue() == null) {
            lastnameerror.setValue("last name is missing");
            return;
        } else {
            lastnameerror.setValue("");
        }
        if (dateofbirth.getValue() == null) {
            dateofbirtherror.setValue("Date of birth missing");
            return;
        } else
            dateofbirtherror.setValue("");
        if (village.getValue() == null) {
            villageerror.setValue("village is missing");
            return;
        } else
            villageerror.setValue("");
        try {
            Logger.logD(TAG, "update ");
            boolean b = patientsDAO.updatePatientToDB(patientdto, uuid,patientAttributesDTOList);

            if (NetworkConnection.isOnline(getApplication())) {
                PullDataDAO pullDataDAO=new PullDataDAO();
                pullDataDAO.pushDataApi();
            }
            if (b) {
                Logger.logD(TAG, "updated");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirst_name() + " " + patientdto.getLast_name());
                i.putExtra("tag", "newPatient");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

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

}

