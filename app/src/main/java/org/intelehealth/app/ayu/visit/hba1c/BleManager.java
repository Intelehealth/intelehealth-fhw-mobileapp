package org.intelehealth.app.ayu.visit.hba1c;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import org.intelehealth.app.utilities.Logger;

import java.util.UUID;

public class BleManager {

    private static final String TAG = "BleManager";

    private Context context;
    private BluetoothGatt bluetoothGatt;
    private Callback callback;

    private boolean isConnected = false;

    // ⚠️ Replace with your device UUIDs
    private static final UUID SERVICE_UUID =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");

    private static final UUID CHARACTERISTIC_UUID =
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    public interface Callback {
        void onValue(String value);
        void onConnection(boolean connected);
    }

    public BleManager(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    // 🔗 Connect to BLE device
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect(BluetoothDevice device) {
        if (device == null) return;

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    // ❌ Disconnect
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        isConnected = false;
    }

    // 🔥 GATT CALLBACK
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server");
                isConnected = true;

                if (callback != null) callback.onConnection(true);

                gatt.discoverServices();

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
                isConnected = false;

                if (callback != null) callback.onConnection(false);
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status != BluetoothGatt.GATT_SUCCESS) return;

            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) {
                Log.e(TAG, "Service not found!");
                return;
            }

            BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(CHARACTERISTIC_UUID);

            if (characteristic == null) {
                Log.e(TAG, "Characteristic not found!");
                return;
            }

            gatt.setCharacteristicNotification(characteristic, true);

            Log.d(TAG, "Notifications enabled");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            byte[] data = characteristic.getValue();

            if (data == null || data.length == 0) return;

            // 🔥 Log RAW data
            Logger.logD(" onCharacteristicChanged: : " ,  bytesToHex(data));
            // Try ASCII parsing first
            String raw = new String(data).trim();
            Logger.logD(" STRING: " ,  raw);

            String parsed = extractNumericValue(raw);

            // If ASCII fails → try HEX parsing
            if (parsed == null) {
                parsed = parseFromBytes(data);
            }

            if (parsed != null && callback != null) {
                Logger.logD(" FINAL VALUE: " ,  parsed);
                callback.onValue(parsed);
            }
        }
    };

    // 🔧 Extract numbers from string (fix for "7.2\r\n", "OK7.2", etc.)
    private String extractNumericValue(String raw) {

        if (raw == null) return null;

        raw = raw.replaceAll("[^0-9.]", "");

        if (raw.isEmpty()) return null;

        try {
            double val = Double.parseDouble(raw);

            // Ignore junk values
            if (val <= 0.0) return null;
            if (val < 3.0 || val > 16.0) return null;
            Logger.logD("returnValue: " , String.valueOf(val));

            return String.valueOf(val);

        } catch (Exception e) {
            return null;
        }
    }

    // 🔧 HEX parser (for devices sending bytes instead of string)
    private String parseFromBytes(byte[] data) {

        try {
            if (data.length < 2) return null;

            int value = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8);

            double result = value / 10.0; // adjust based on device

            if (result <= 0.0) return null;
            if (result < 3.0 || result > 16.0) return null;
            Logger.logD("returnResult: " , String.valueOf(result));

            return String.valueOf(result);

        } catch (Exception e) {
            return null;
        }
    }

    // 🔧 Debug helper
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}