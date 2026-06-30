package org.intelehealth.app.ayu.visit.hba1c;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * HbA1cLiveViewModel
 *
 * Pure data holder — survives fragment transactions, shared by every
 * fragment via the Activity-scoped ViewModelProvider.
 *
 * IMPORTANT: This class does NOT own the BLE connection anymore.
 * ControlCentre (BioSense SDK) requires an Activity reference, which a
 * ViewModel must never hold (leak risk across recreation). The Activity
 * owns ControlCentre, implements Communicator, and pushes results in here
 * via the setters below. No polling thread, no manual GATT, no timeout —
 * the SDK manages its own connection lifecycle.
 */
public class HbA1cLiveViewModel extends AndroidViewModel {

    private final MutableLiveData<String>  mHba1cReading  = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mConnected     = new MutableLiveData<>(false);
    private final MutableLiveData<String>  mLastUpdatedAt = new MutableLiveData<>();

    /** True after the device's 1st button press but before the 2nd —
     *  use this to show "Press the device button again" to the user. */
    private final MutableLiveData<Boolean> mAwaitingSecondPress = new MutableLiveData<>(false);

    public LiveData<String>  hba1cReading()         { return mHba1cReading; }
    public LiveData<Boolean> connected()            { return mConnected; }
    public LiveData<String>  lastUpdatedAt()         { return mLastUpdatedAt; }
    public LiveData<Boolean> awaitingSecondPress()  { return mAwaitingSecondPress; }

    public HbA1cLiveViewModel(@NonNull Application app) {
        super(app);
    }

    // ── Called by the Activity's Communicator callbacks ────────────────────

    public void onHba1cReading(String reading, String time) {
        mHba1cReading.setValue(reading);
        mLastUpdatedAt.setValue(time);
        mAwaitingSecondPress.setValue(false);
    }

    public void onConnectionStatus(boolean isConnected) {
        mConnected.setValue(isConnected);
        if (!isConnected) {
            mAwaitingSecondPress.setValue(false);
        }
    }

    public void onFirstFrameReceived() {
        // go() fired — device sent frame 1 but setHbA1cReading() won't be
        // called until the user presses the button a 2nd time.
        mAwaitingSecondPress.setValue(true);
    }

    /** Call when starting a fresh visit / new device so stale values from
     *  a previous patient never leak into this screen. */
    public void reset() {
        mHba1cReading.setValue(null);
        mLastUpdatedAt.setValue(null);
        mAwaitingSecondPress.setValue(false);
    }
}
