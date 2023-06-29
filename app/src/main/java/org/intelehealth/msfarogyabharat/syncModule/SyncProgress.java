package org.intelehealth.msfarogyabharat.syncModule;

import androidx.lifecycle.LiveData;

/**
 * Created By: Prajwal Maruti Waingankar
 * On: 23rd June 2023
 * Email: prajwalwaingankar@gmail.com
 */

public class SyncProgress extends LiveData<Integer> {

    public void updateProgress(int progress) {
        postValue(progress);
    }
}
