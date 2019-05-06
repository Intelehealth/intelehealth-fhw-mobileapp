package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

import java.text.DecimalFormat;

import io.intelehealth.client.R;

public class VitalsViewModel extends AndroidViewModel {
    public VitalsViewModel(@NonNull Application application) {
        super(application);
    }
}
