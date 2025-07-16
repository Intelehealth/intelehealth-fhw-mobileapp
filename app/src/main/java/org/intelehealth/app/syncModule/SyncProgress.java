package org.intelehealth.app.syncModule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SyncProgress {

    private final MutableLiveData<Integer> liveData = new MutableLiveData<>();

    public LiveData<Integer> getLiveData() {
        return liveData;
    }

    public void updateProgress(int progress) {
        liveData.postValue(progress);
    }
}
