package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Intent;
import android.support.annotation.NonNull;

import io.intelehealth.client.views.activites.VitalsActivity;

public class PatientDetailViewModel extends AndroidViewModel {


    public PatientDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void newVisit() {

        Intent i = new Intent(getApplication(), VitalsActivity.class);
        getApplication().startActivity(i);
    }
}
