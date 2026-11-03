package org.intelehealth.app.ayu.visit.hba1c;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class HbA1cLiveViewModel extends AndroidViewModel {

    private final MutableLiveData<String>  mHba1cReading   = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mConnected      = new MutableLiveData<>(false);
    private final MutableLiveData<String>  mLastUpdatedAt  = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mReadyToReceive = new MutableLiveData<>(false);

    public LiveData<String>  hba1cReading()   { return mHba1cReading; }
    public LiveData<Boolean> connected()       { return mConnected; }
    public LiveData<String>  lastUpdatedAt()   { return mLastUpdatedAt; }
    public LiveData<Boolean> readyToReceive()  { return mReadyToReceive; }

    public HbA1cLiveViewModel(@NonNull Application app) {
        super(app);
    }

    // ── Called by the Activity's Communicator callbacks ──────────────────────

    public void onHba1cReading(String reading, String time) {
        mHba1cReading.setValue(reading);
        mLastUpdatedAt.setValue(time);
    }

    public void onConnectionStatus(boolean isConnected) {
        mConnected.setValue(isConnected);
        if (!isConnected) {
            mReadyToReceive.setValue(false);
        }
    }

    public void onReadyToReceive(boolean ready) {
        mReadyToReceive.setValue(ready);
    }

    /**
     * FULL reset — call ONLY when starting a brand new patient visit.
     * This clears the reading, timestamp, connected and readyToReceive.
     * Do NOT call during reconnects — it wipes the captured reading.
     */
    public void reset() {
        mHba1cReading.setValue(null);
        mLastUpdatedAt.setValue(null);
        mConnected.setValue(false);
        mReadyToReceive.setValue(false);
    }

    /**
     * CONNECTION-ONLY reset — call during reconnects.
     * Clears only connected + readyToReceive flags.
     * The reading and timestamp are preserved so they survive
     * all reconnect attempts and Activity recreation.
     */
    public void resetConnectionOnly() {
        mConnected.setValue(false);
        mReadyToReceive.setValue(false);
    }
}