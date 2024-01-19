package org.intelehealth.app.syncModule;

import androidx.lifecycle.LiveData;

public class SyncProgress extends LiveData<Integer> {

    public void updateProgress(int progress) {
        postValue(progress);
    }
}
