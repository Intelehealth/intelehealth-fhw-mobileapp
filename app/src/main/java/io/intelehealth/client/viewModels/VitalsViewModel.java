package io.intelehealth.client.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

public class VitalsViewModel extends AndroidViewModel {

    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public VitalsViewModel(@NonNull Application application) {
        super(application);
    }
}
