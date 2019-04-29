package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import io.intelehealth.client.R;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import io.intelehealth.client.models.pushResponseApiCall.PushResponseApiCall;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.viewModels.requestModels.Patient;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class IdentificationViewModel extends AndroidViewModel {
    private static final String TAG = IdentificationViewModel.class.getSimpleName();
    public MutableLiveData<Patient> uuid = new MutableLiveData<>();
    public MutableLiveData<String> patientFirstName = new MutableLiveData<>();
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


    public Single<PatientDTO> insertPatient(PatientDTO patient) {
        Logger.logD(TAG, "insertpatinet ");
        Logger.logD(TAG, "firstname" + patientFirstName.getValue());
        return new Single<PatientDTO>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PatientDTO> observer) {
                try (SQLiteDatabase db4 = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()) {
                    ContentValues patientEntries = new ContentValues();
                    patientEntries.put("uuid", AppConstants.NEW_UUID);
                    patientEntries.put("first_name", patient.getFirstname());
                    patientEntries.put("middle_name", patient.getMiddlename());
                    patientEntries.put("last_name", patient.getLastname());
                    patientEntries.put("date_of_birth", patient.getDateofbirth());
                    patientEntries.put("phone_number", patient.getPhonenumber());
                    patientEntries.put("address1", patient.getAddress1());
                    patientEntries.put("address2", patient.getAddress2());
                    patientEntries.put("city_village", patient.getCityvillage());
                    patientEntries.put("state_province", patient.getStateprovince());
                    patientEntries.put("postal_code", patient.getPostalcode());
                    patientEntries.put("country", patient.getCountry());
                    patientEntries.put("gender", patient.getGender());
                    db4.insert(
                            "tbl_patient",
                            null,
                            patientEntries
                    );
                } catch (SQLiteException sqle) {
                    Logger.logE(TAG, "sql exception", sqle);
                }

            }
        };
    }

    public void patinetApiCall() {
        PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();
        String url = "";
        Observable<PushResponseApiCall> pushResponseApiCallObservable = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url, pushRequestApiCall);
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

