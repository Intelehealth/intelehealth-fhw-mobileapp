package org.intelehealth.app.ayu.visit.hba1c;

import java.util.UUID;// ── UUIDs ──────────────────────────────────────────────────────────────




import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;

/**
 * BleConnectedThread — auto-updating HbA1c reader.
 *
 * Two complementary delivery modes, both feed the same pipeline:
 *
 *  A) NOTIFY  — device pushes a frame when its button is pressed
 *               (onCharacteristicChanged)
 *  B) POLL    — we pull the characteristic every POLL_INTERVAL_MS
 *               so the UI updates automatically without a button press
 *               (onCharacteristicRead, driven by mPollHandler on a
 *                dedicated HandlerThread)
 *
 * Both paths validate on the BLE/poll thread, then post to the main-
 * looper Handler exactly like ConnectedThread.
 *
 *  ConnectedThread (Classic BT):
 *    run() → mmInStream.read() loop → mHandler.obtainMessage().sendToTarget()
 *
 *  BleConnectedThread (BLE GATT):
 *    run()       → connectGatt()                       (exits immediately)
 *    notify path → onCharacteristicChanged()           (BLE callback thread)
 *    poll path   → mPollRunnable → readCharacteristic  (mPollThread)
 *                → onCharacteristicRead()              (BLE callback thread)
 *    both        → validate → mHandler.obtainMessage().sendToTarget()
 *                → handleMessage() on Main Looper → etvDiabetesHba1c.setText()
 */
public class BleConnectedThread extends Thread {

    private static final String TAG = "BleConnectedThread";

    // ── UUIDs ──────────────────────────────────────────────────────────────
    public static final UUID SERVICE_UUID =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_UUID    =
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID   =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // ── Handler message types ──────────────────────────────────────────────
    public static final int MSG_HBA1C_READ  = 1;   // obj = String reading
    public static final int MSG_CONN_STATUS = 2;   // obj = Boolean isConnected

    // ── Polling ────────────────────────────────────────────────────────────
    /** How often to pull the characteristic when connected (ms). */
    private static final long POLL_INTERVAL_MS = 2_000L;

    // ── Valid range ────────────────────────────────────────────────────────
    private static final double HBA1C_MIN = 3.0;
    private static final double HBA1C_MAX = 16.0;

    // ── State ──────────────────────────────────────────────────────────────
    private final BluetoothDevice mDevice;
    private final Context         mContext;
    private final Handler         mMainHandler;   // main-looper Handler from Fragment

    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mCharacteristic;

    /** Dedicated thread that drives the read-poll loop. */
    private HandlerThread mPollThread;
    private Handler       mPollHandler;

    private volatile boolean mRunning =  true;

    // ── Constructor ────────────────────────────────────────────────────────

    public BleConnectedThread(BluetoothDevice device, Context context, Handler mainHandler) {
        super("BleConnectedThread");
        mDevice      = device;
        mContext     = context.getApplicationContext();
        mMainHandler = mainHandler;
    }

    // ── Thread entry ───────────────────────────────────────────────────────

    @Override
    public void run() {
        Log.d(TAG, "run: connecting → " + mDevice.getAddress());

        // ── Permission guard ───────────────────────────────────────────────
        // On API 31+ connectGatt() requires BLUETOOTH_CONNECT at runtime.
        // 'this' is a Thread, NOT a Context — use mContext here.
        // The actual grant must be requested by the Activity before starting
        // this thread; we abort cleanly if it was somehow missed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    mContext,                           // ← mContext, never 'this'
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "run: BLUETOOTH_CONNECT not granted — aborting");
                postStatus(false);
                return;
            }
        }

        // Start the poll HandlerThread before connecting so it is ready
        // when onServicesDiscovered fires.
        mPollThread = new HandlerThread("BlePollThread");
        mPollThread.start();
        mPollHandler = new Handler(mPollThread.getLooper());

        mGatt = mDevice.connectGatt(
                mContext,
                false,
                mGattCallback,
                BluetoothDevice.TRANSPORT_LE
        );
        // Thread exits here; GATT callbacks + poll loop drive everything else.
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /** Stop polling, disconnect GATT, release resources. Thread-safe. */
    public void cancel() {
        mRunning = false;

        // Stop poll loop first so no more readCharacteristic() calls fire
        if (mPollHandler != null) {
            mPollHandler.removeCallbacksAndMessages(null);
        }
        if (mPollThread != null) {
            mPollThread.quitSafely();
            mPollThread = null;
        }

        if (mGatt != null) {
            try {
                mGatt.disconnect();
                mGatt.close();
            } catch (Exception e) {
                Log.w(TAG, "cancel: close error — " + e.getMessage());
            }
            mGatt = null;
        }

        mCharacteristic = null;
        Log.d(TAG, "cancel: done");
    }

    // ── GATT callbacks ─────────────────────────────────────────────────────

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /** Step 1 — connection state. Post status; trigger service discovery. */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            boolean connected = (newState == BluetoothProfile.STATE_CONNECTED);
            Log.d(TAG, "onConnectionStateChange: connected=" + connected);
            postStatus(connected);

            if (connected) {
                gatt.discoverServices();
            } else {
                stopPoll();
                gatt.close();
            }
        }

        /**
         * Step 2 — services discovered.
         * Find FFE1, enable NOTIFY via CCCD, cache the characteristic,
         * then start the auto-poll loop.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered: failed status=" + status);
                return;
            }

            android.bluetooth.BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) { Log.e(TAG, "FFE0 service not found"); return; }

            mCharacteristic = service.getCharacteristic(CHAR_UUID);
            if (mCharacteristic == null) { Log.e(TAG, "FFE1 char not found"); return; }

            // Enable server-side notifications (device → app push)
            gatt.setCharacteristicNotification(mCharacteristic, true);
            BluetoothGattDescriptor cccd = mCharacteristic.getDescriptor(CCCD_UUID);
            if (cccd != null) {
                cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(cccd);
            }

            // Start automatic polling regardless of notify support
            startPoll();
            Log.d(TAG, "onServicesDiscovered: polling started every " + POLL_INTERVAL_MS + " ms");
        }

        /**
         * Step 3A — NOTIFY path.
         * Device pushed a frame (e.g. button press). Same validation as poll path.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (!mRunning) return;
            Log.d(TAG, "onCharacteristicChanged (notify)");
            processPayload(characteristic.getValue(), "notify");
        }

        /**
         * Step 3B — POLL path.
         * We called gatt.readCharacteristic(); result lands here.
         * Feeds the same validate → post pipeline as notify.
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (!mRunning) return;
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onCharacteristicRead: GATT error status=" + status);
                return;
            }
            Log.d(TAG, "onCharacteristicRead (poll)");
            processPayload(characteristic.getValue(), "poll");
        }
    };

    // ── Poll loop ──────────────────────────────────────────────────────────

    /**
     * Runnable that issues a GATT read and reschedules itself.
     * Runs on mPollThread — never on Main Looper.
     *
     * Equivalent to the while(true) { mmInStream.read(); } loop in
     * ConnectedThread, but event-driven instead of blocking.
     */
    private final Runnable mPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRunning || mGatt == null || mCharacteristic == null) return;

            // Issue the read; result arrives in onCharacteristicRead() on
            // the BLE callback thread — same as read() returning in ConnectedThread.
            boolean queued = mGatt.readCharacteristic(mCharacteristic);
            Log.d(TAG, "mPollRunnable: readCharacteristic queued=" + queued);

            // Reschedule regardless of queued result — device may be mid-transfer
            if (mRunning && mPollHandler != null) {
                mPollHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        }
    };

    private void startPoll() {
        if (mPollHandler == null) return;
        mPollHandler.removeCallbacks(mPollRunnable);
        mPollHandler.postDelayed(mPollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPoll() {
        if (mPollHandler != null) {
            mPollHandler.removeCallbacks(mPollRunnable);
        }
    }

    // ── Shared payload pipeline ────────────────────────────────────────────

    /**
     * Validate and post — shared by both notify and poll paths.
     * Runs on a BLE/poll background thread. Never touches UI directly.
     *
     * @param raw    raw bytes from the characteristic
     * @param source "notify" or "poll" (for logging only)
     */
    private void processPayload(byte[] raw, String source) {
        if (raw == null || raw.length == 0) {
            Log.d(TAG, "processPayload [" + source + "]: empty — skipped");
            return;
        }

        // Explicit charset — no platform-default assumption
        String reading = new String(raw, 0, raw.length, StandardCharsets.UTF_8).trim();
        Log.d(TAG, "processPayload [" + source + "]: raw=\"" + reading + "\"");

        if (reading.isEmpty()) return;

        // Range validation on background thread — keeps Main Looper work minimal
        try {
            double value = Double.parseDouble(reading);
            if (value < HBA1C_MIN || value > HBA1C_MAX) {
                Log.d(TAG, "processPayload: " + value + " out of [" + HBA1C_MIN + "," + HBA1C_MAX + "] — skipped");
                return;
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, "processPayload: parse failed — skipped");
            return;
        }

        // Post to Main Looper — mirrors ConnectedThread:
        //   mHandler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget()
        postReading(reading);
    }

    // ── Post helpers ───────────────────────────────────────────────────────

    private void postReading(String reading) {
        if (mMainHandler == null) return;
        mMainHandler.obtainMessage(MSG_HBA1C_READ, reading).sendToTarget();
        Log.d(TAG, "postReading → " + reading);
    }

    private void postStatus(boolean connected) {
        if (mMainHandler == null) return;
        mMainHandler.obtainMessage(MSG_CONN_STATUS, connected).sendToTarget();
    }
}