package org.intelehealth.app.ayu.visit.hba1c;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.RequiresPermission;

// android.util.Log is used directly because Logger.logD/CustomLog.d silently
// drops messages that don't start with "parentNode ".

import java.util.UUID;

public class BleManager {

    private static final String TAG = "BleManager";

    private static final UUID SERVICE_UUID =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID =
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public interface Callback {
        void onValue(String value);
        void onConnection(boolean connected);
    }

    private final Context context;
    private final Callback callback;
    private BluetoothGatt bluetoothGatt;

    public BleManager(Context context, Callback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "connect called with null device");
            return;
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server");
                if (callback != null) callback.onConnection(true);
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
                if (callback != null) callback.onConnection(false);
                gatt.close();
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service discovery failed: " + status);
                return;
            }

            // Diagnostic: dump every service + characteristic the device exposes
            // so we can identify the right UUIDs for this HbA1c device model.
            Log.d(TAG, "=== GATT SERVICES (" + gatt.getServices().size() + ") ===");
            for (BluetoothGattService s : gatt.getServices()) {
                Log.d(TAG, "Service: " + s.getUuid());
                for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                    int p = c.getProperties();
                    StringBuilder props = new StringBuilder();
                    if ((p & BluetoothGattCharacteristic.PROPERTY_READ)   != 0) props.append("READ ");
                    if ((p & BluetoothGattCharacteristic.PROPERTY_WRITE)  != 0) props.append("WRITE ");
                    if ((p & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) props.append("WRITE_NO_RESPONSE ");
                    if ((p & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) props.append("NOTIFY ");
                    if ((p & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) props.append("INDICATE ");
                    Log.d(TAG, "  Char: " + c.getUuid() + " [" + props.toString().trim() + "]");
                }
            }
            Log.d(TAG, "=== END GATT SERVICES ===");

            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) {
                Log.e(TAG, "Service " + SERVICE_UUID + " not found — see GATT dump above");
                return;
            }

            BluetoothGattCharacteristic ch = service.getCharacteristic(CHARACTERISTIC_UUID);
            if (ch == null) {
                Log.e(TAG, "Characteristic " + CHARACTERISTIC_UUID + " not found");
                return;
            }

            // Step 1: tell Android to deliver notifications locally.
            if (!gatt.setCharacteristicNotification(ch, true)) {
                Log.e(TAG, "setCharacteristicNotification failed");
                return;
            }

            // Step 2: write CCCD so the peripheral actually starts sending them.
            BluetoothGattDescriptor cccd = ch.getDescriptor(CCCD_UUID);
            if (cccd == null) {
                Log.e(TAG, "CCCD descriptor missing on characteristic");
                return;
            }

            int props = ch.getProperties();
            byte[] enableValue;
            if ((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                enableValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            } else if ((props & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                enableValue = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
            } else {
                Log.e(TAG, "Characteristic supports neither notify nor indicate");
                return;
            }
            cccd.setValue(enableValue);
            boolean queued = gatt.writeDescriptor(cccd);
            Log.d(TAG, "CCCD write queued=" + queued);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Notifications subscribed; waiting for data...");
            } else {
                Log.e(TAG, "CCCD write failed: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data == null || data.length == 0) return;

            // Use android.util.Log directly — Logger.logD/CustomLog.d silently
            // drops everything that doesn't start with "parentNode ".
            Log.d(TAG, "HEX: " + bytesToHex(data));
            String raw = new String(data).trim();
            Log.d(TAG, "STRING: " + raw);

            String parsed = extractNumericValue(raw);
            if (parsed == null) parsed = parseFromBytes(data);

            Log.d(TAG, "PARSED: " + parsed);
            if (parsed != null && callback != null) {
                callback.onValue(parsed);
            }
        }
    };

    private String extractNumericValue(String raw) {
        if (raw == null) return null;
        raw = raw.replaceAll("[^0-9.]", "");
        if (raw.isEmpty()) return null;
        try {
            double val = Double.parseDouble(raw);
            if (val < 3.0 || val > 16.0) return null;
            return String.valueOf(val);
        } catch (Exception e) {
            return null;
        }
    }

    private String parseFromBytes(byte[] data) {
        try {
            if (data.length < 2) return null;
            int value = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8);
            double result = value / 10.0;
            if (result < 3.0 || result > 16.0) return null;
            return String.valueOf(result);
        } catch (Exception e) {
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString();
    }
}
