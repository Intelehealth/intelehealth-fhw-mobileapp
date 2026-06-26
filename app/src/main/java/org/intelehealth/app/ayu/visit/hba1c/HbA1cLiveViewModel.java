package org.intelehealth.app.ayu.visit.hba1c;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * HbA1cLiveViewModel
 *
 * Activity-scoped ViewModel that acts as the single source of truth
 * for the BLE HbA1c pipeline.
 *
 * Lifecycle contract:
 *   VisitCreationActivity.onCreate()
 *     → getViewModel()
 *     → startBle(deviceAddress)          ← auto-starts on known address
 *
 *   Any Fragment (DiagnosticsCollectionFragment, SummaryFragment, …)
 *     → getViewModel().hba1cReading().observe(viewLifecycleOwner, …)
 *     → field auto-populated whenever a new reading arrives
 *
 *   VisitCreationActivity.onDestroy()
 *     → getViewModel().stopBle()         ← ViewModel.onCleared() also calls this
 *
 * Threading model (mirrors ConnectedThread):
 *   BleConnectedThread (BLE callback / poll thread)
 *     → mHandler.obtainMessage(MSG_HBA1C_READ, reading).sendToTarget()
 *   mHandler (Main Looper — inner static class, no leak)
 *     → hba1cReading.setValue(reading)   ← LiveData notifies observers on Main thread
 */
public class HbA1cLiveViewModel extends AndroidViewModel {

    private static final String TAG = "HbA1cLiveViewModel";

    // ── Exposed LiveData ──────────────────────────────────────────────────
    private final MutableLiveData<String>  mHba1cReading   = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mConnected      = new MutableLiveData<>(false);
    private final MutableLiveData<String>  mLastUpdatedAt  = new MutableLiveData<>();

    /** Latest validated HbA1c string, e.g. "6.2". Null until first reading. */
    public LiveData<String>  hba1cReading()  { return mHba1cReading;  }

    /** True while GATT is in STATE_CONNECTED. */
    public LiveData<Boolean> connected()     { return mConnected;     }

    /** "HH:mm:ss" timestamp of the last reading. */
    public LiveData<String>  lastUpdatedAt() { return mLastUpdatedAt; }

    // ── BLE thread + handler ──────────────────────────────────────────────
    private BleConnectedThread mBleThread;
    private final BleHandler   mHandler = new BleHandler(this);

    // ── Static handler — no ViewModel leak ───────────────────────────────
    private static final class BleHandler extends Handler {
        private final HbA1cLiveViewModel mVm;

        BleHandler(HbA1cLiveViewModel vm) {
            super(Looper.getMainLooper());
            mVm = vm;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BleConnectedThread.MSG_HBA1C_READ:
                    String reading = (String) msg.obj;
                    mVm.mHba1cReading.setValue(reading);

                    // Timestamp on Main thread
                    String ts = new java.text.SimpleDateFormat(
                            "HH:mm:ss", java.util.Locale.getDefault())
                            .format(new java.util.Date());
                    mVm.mLastUpdatedAt.setValue(ts);
                    Log.d(TAG, "BleHandler: hba1c=" + reading + " at " + ts);
                    break;

                case BleConnectedThread.MSG_CONN_STATUS:
                    boolean connected = (boolean) msg.obj;
                    mVm.mConnected.setValue(connected);
                    Log.d(TAG, "BleHandler: connected=" + connected);
                    break;
            }
        }
    }

    public HbA1cLiveViewModel(@NonNull Application app) {
        super(app);
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Start BLE connection to a known device address.
     * Safe to call multiple times — cancels the previous thread first.
     * Called from VisitCreationActivity once a device address is available.
     *
     * @param deviceAddress  MAC address string from BleScanActivity / preferences
     */
    public void startBle(String deviceAddress) {
        stopBle();  // cancel any existing thread first

        android.bluetooth.BluetoothAdapter adapter =
                android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "startBle: BluetoothAdapter not available");
            return;
        }

        android.bluetooth.BluetoothDevice device;
        try {
            device = adapter.getRemoteDevice(deviceAddress);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "startBle: invalid address — " + deviceAddress);
            return;
        }

        mBleThread = new BleConnectedThread(device, getApplication(), mHandler);
        mBleThread.start();
        Log.d(TAG, "startBle: thread started for " + deviceAddress);
    }

    /**
     * Stop the BLE thread and drain pending handler messages.
     * Called automatically from onCleared() and before starting a new thread.
     */
    public void stopBle() {
        if (mBleThread != null) {
            mBleThread.cancel();
            mBleThread = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "stopBle: done");
    }

    /** True if a BLE thread is currently running. */
    public boolean isBleRunning() {
        return mBleThread != null && mBleThread.isAlive();
    }

    @Override
    protected void onCleared() {
        stopBle();
        super.onCleared();
    }
}