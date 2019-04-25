package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.viewModels.requestModels.Patient;

public class IdentificationViewModel extends AndroidViewModel {
    public MutableLiveData<Patient> uuid = new MutableLiveData<>();
    public MutableLiveData<Patient> patientFirstName = new MutableLiveData<>();
    private SessionManager session;
    private MutableLiveData<Patient> userMutableLiveData = new MutableLiveData<>();


    public IdentificationViewModel(@NonNull Application application) {
        super(application);
        session = new SessionManager(application);
    }

    public LiveData<Patient> getPatient() {
        if (userMutableLiveData == null) {
            userMutableLiveData = new MutableLiveData<>();
        }

        return userMutableLiveData;
    }

    public void onPatientCreateClicked() {
        Patient patient = new Patient();

        userMutableLiveData.setValue(patient);


    }
}
