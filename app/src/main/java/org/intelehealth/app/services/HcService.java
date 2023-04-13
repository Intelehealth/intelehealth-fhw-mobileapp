package org.intelehealth.app.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.linktop.constant.BluetoothState;
import com.linktop.constant.UUIDConfig;
import com.linktop.constant.WareType;
import com.linktop.infs.OnDeviceVersionListener;
import com.linktop.infs.OnSendCodeToDevCallback;
import com.linktop.whealthService.BleDevManager;
import com.linktop.whealthService.MeasureType;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class HcService extends Service {

    public final static int BLE_STATE = 66;
    public final static int DATA_QUERY_SOFTWARE_VER = 0;
    public final static int DATA_QUERY_HARDWARE_VER = 1;
    public final static int DATA_QUERY_FIRMWARE_VER = 2;
    public final static int DATA_QUERY_CONFIRM_ECG_MODULE_EXIST = 3;
    public final static int DATA_QUERY_CONFIRM_BG_MODULE_EXIST = 4;
    public final static int DATA_QUERY_ID_AND_KEY = 5;
    public final static int DATA_QUERY_BATTERY_INFO = 6;
    private final static String TAG = HcService.class.getSimpleName();
    private LocalBinder mBinder;
    private BleDevManager mBleDevManager;
    private boolean isSupportBLE;
    private BluetoothAdapter mAdapter;
    public boolean isConnected;
    private BluetoothGatt mGatt;
    private Handler mHandler;
    private int mState;
    private Timer mBatteryQueryTimer;
    private BluetoothGattCharacteristic mHRMEnabledChara;
    private BluetoothGattCharacteristic mHRMChara;
    private OnDeviceVersionListener mOnDeviceVersionListener;
    @MeasureType
    private int mType;

    @Override
    public void onCreate() {
        mBinder = new LocalBinder();
        //TODO Create a BleDevManager object
        mBleDevManager = new BleDevManager();
        mBleDevManager.initHC(this);
        super.onCreate();
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void setBleState(int state) {
        if (mState != state) {
            mState = state;
            if (mHandler != null) {
                Message.obtain(mHandler, BLE_STATE, state).sendToTarget();
            }
        }
    }

    public void initBluetooth() {
        isSupportBLE = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (!isSupportBLE) {
            Toast.makeText(getBaseContext(), "Mobile phone not support BLE!", Toast.LENGTH_SHORT).show();
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mAdapter = bluetoothManager.getAdapter();
                if (mAdapter.isEnabled()) {
                    setBleState(BluetoothState.BLE_OPENED_AND_DISCONNECT);
                } else {
                    setBleState(BluetoothState.BLE_CLOSED);
                }
            }
        }
    }

    public int isBluetoothEnable() {
        if (mAdapter == null || !isSupportBLE) {
            return -1;
        } else {
            return mAdapter.isEnabled() ? 1 : 0;
        }
    }

    public void quicklyConnect() {
        if (mAdapter != null) {
            if (mAdapter.isEnabled()) {

                //Normally,the bluetooth name of Health monitor is start with "HC02" or other custom prefix.
                //You can use the prefix name to filter all the scanned bluetooth devices to reduce the size of the scanning list.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            final BluetoothDevice device = result.getDevice();
                            final String name = device.getName();
                            if (!TextUtils.isEmpty(name) && name.startsWith("HC02")) {
                                Log.e("onScanResult", "dev name:" + name);
                                mAdapter.getBluetoothLeScanner().stopScan(this);
                                connect(device);
                            }
                            super.onScanResult(callbackType, result);
                        }

                        @Override
                        public void onBatchScanResults(List<ScanResult> results) {
                            super.onBatchScanResults(results);
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            super.onScanFailed(errorCode);
                        }
                    });
                } else {
                    mAdapter.startLeScan(null, new BluetoothAdapter.LeScanCallback() {
                        @Override
                        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                            final String name = device.getName();
                            if (!TextUtils.isEmpty(name) && name.startsWith("HC02")) {
                                Log.e("onLeScan", "dev name:" + name);
                                mAdapter.stopLeScan(this);
                                connect(device);
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(getBaseContext(), "Please enable the mobile phone's bluetooth!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void connect(BluetoothDevice device) {
        setBleState(BluetoothState.BLE_CONNECTING_DEVICE);
        Log.e("dataQueryStep()", "Address:" + device.getAddress());
        mGatt = device.connectGatt(this, false, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                final BluetoothDevice device = gatt.getDevice();
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        mBleDevManager.setCmdThread(new CmdSendThread());
                        isConnected = true;
                        try {
                            Thread.sleep(200L);
                            final boolean discoverServices = gatt.discoverServices();
                            Log.e("BluetoothGattCallback", "Now Ble connect to device, name:" + device.getName() + ", address:" + device.getAddress() + ", discoverServices ? " + discoverServices);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setBleState(BluetoothState.BLE_CONNECTED_DEVICE);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        isConnected = false;
                        if (mHRMEnabledChara != null) {
                            final int charaProp = mHRMEnabledChara.getProperties();
//                            setCharacteristicNotification(mHRMEnabledChara, false);
//                            mHRMChara = null;
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                setCharacteristicNotification(mHRMEnabledChara, false);
                                mHRMChara = null;
                                mGatt.readCharacteristic(mHRMEnabledChara);
                            }
                        }
                        mHRMEnabledChara = null;
                        //TODO Bluetooth disconnect,reset some params.
                        mBleDevManager.getEcgTask().setModuleExist(false);
                        mBleDevManager.destroy();
                        if (mGatt != null) {
                            mGatt.close();
                            mGatt = null;
                        }
                        if (mBatteryQueryTimer != null) {
                            mBatteryQueryTimer.cancel();
                            mBatteryQueryTimer = null;
                        }
                        setBleState(BluetoothState.BLE_OPENED_AND_DISCONNECT);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.e("dataQueryStep()", "onServicesDiscovered - BluetoothGatt.GATT_SUCCESS");
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final boolean isEnabled = enableHRNotification();
                    setBleState(isEnabled ? BluetoothState.BLE_NOTIFICATION_ENABLED : BluetoothState.BLE_NOTIFICATION_DISABLED);
                }
                super.onServicesDiscovered(gatt, status);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    final UUID charaUuid = characteristic.getUuid();
                    final String s = new String(characteristic.getValue());
                    if (UUID.fromString(UUIDConfig.DEV_INFO_SOFTWARE_REV_UUID).equals(charaUuid)) {
                        Log.w(TAG, "SoftwareVer:" + s);
                        if (mOnDeviceVersionListener != null)
                            mOnDeviceVersionListener.onDeviceVersion(WareType.VER_SOFTWARE, s);
                    } else if (UUID.fromString(UUIDConfig.DEV_INFO_HARDWARE_PCB_UUID).equals(charaUuid)) {
                        Log.w(TAG, "HardwareVer:" + s);
                        if (mOnDeviceVersionListener != null)
                            mOnDeviceVersionListener.onDeviceVersion(WareType.VER_HARDWARE, s);
                    } else if (UUID.fromString(UUIDConfig.DEV_INFO_FIRMWARE_REV_UUID).equals(charaUuid)) {
                        Log.w(TAG, "FirmwareVer:" + s);
                        if (mOnDeviceVersionListener != null)
                            mOnDeviceVersionListener.onDeviceVersion(WareType.VER_FIRMWARE, s);
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                final byte[] value = characteristic.getValue();
                /*<*********debug data***********<*/
//                Log.e(TAG, "onCharacteristicChanged,bytes length:" + value.length + ", bytes=" + Arrays.toString(value));
                /*>********debug data***********>*/
                //Parsing data packet what received from the device.
                mBleDevManager.getCommunicate().packageParse(value);

            }
        });
        mGatt.connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public HcService getService() {
            return HcService.this;
        }
    }

    /**
     * TODO
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mAdapter == null || mGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        final boolean notificationEnable = mGatt.setCharacteristicNotification(characteristic, enabled);
        if (!notificationEnable) return false;

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(UUIDConfig.CCC));
        // This is specific to Heart Rate Measurement.
        if (UUIDConfig.HEART_RATE_MEASUREMENT_CHARA.equals(characteristic.getUuid().toString()) && enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mGatt.writeDescriptor(descriptor);
    }

    /**
     * TODO
     * Get a Bluetooth Gatt Service by uuid {@link UUIDConfig#HRP_SERVICE},and then use this service to
     * get a heart rate write characteristic by uuid {@link UUIDConfig#HEART_RATE_WRITE_CHARA}
     * and a heart rate measurement characteristic by uuid {@link UUIDConfig#HEART_RATE_MEASUREMENT_CHARA}.
     * When this boolean method return true, it means that you can communicate with device.
     */
    private boolean enableHRNotification() {
        boolean isEnabled = false;
        if (mGatt == null) return false;
        BluetoothGattService mHRP = mGatt.getService(UUID.fromString(UUIDConfig.HRP_SERVICE));
        if (mHRP == null) {
            Log.e(TAG, "===HRP service not found!");
            return false;
        }
        mHRMChara = mHRP.getCharacteristic(UUID.fromString(UUIDConfig.HEART_RATE_WRITE_CHARA));
        if (mHRMChara == null) {
            Log.e(TAG, "=======characteristic1 not found!");
            return false;
        }
        mHRMEnabledChara = mHRP.getCharacteristic(UUID.fromString(UUIDConfig.HEART_RATE_MEASUREMENT_CHARA));
        if (mHRMEnabledChara != null) {
            isEnabled = setCharacteristicNotification(mHRMEnabledChara, true);
            Log.e(TAG, "setCharacteristicNotification Enabled?" + isEnabled);
        } else {
            Log.e(TAG, "===========characteristic4 not found!");
        }
        return isEnabled;
    }

    /**
     * TODO
     * Get the firmware version, hardware version and software version from the device by
     * a device information service by uuid {@link UUIDConfig#DEV_INFO_SER_UUID}.Use this
     * service,you can get a characteristic by uuid
     * {@link UUIDConfig#DEV_INFO_FIRMWARE_REV_UUID,UUIDConfig#DEV_INFO_HARDWARE_PCB_UUID,UUIDConfig#DEV_INFO_SOFTWARE_REV_UUID},
     * and then read this characteristic you have got.
     *
     * @param wareType {@link WareType#VER_FIRMWARE} firmware version
     *                 {@link WareType#VER_HARDWARE} hardware version
     *                 {@link WareType#VER_SOFTWARE} software version
     */
    private void getDevWareVersion(@WareType int wareType) {
        if (mGatt == null)
            return;
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BluetoothGattService deviceInfoService = mGatt.getService(UUID.fromString(UUIDConfig.DEV_INFO_SER_UUID));
        if (deviceInfoService == null) {
            Log.e(TAG, "deviceInfoService service not found!");
            return;
        }
        String uuidStr = "";
        switch (wareType) {
            case WareType.VER_FIRMWARE:
                uuidStr = UUIDConfig.DEV_INFO_FIRMWARE_REV_UUID;
                break;
            case WareType.VER_HARDWARE:
                uuidStr = UUIDConfig.DEV_INFO_HARDWARE_PCB_UUID;
                break;
            case WareType.VER_SOFTWARE:
                uuidStr = UUIDConfig.DEV_INFO_SOFTWARE_REV_UUID;
                break;
        }
        final UUID uuid = UUID.fromString(uuidStr);
        BluetoothGattCharacteristic wareCharacteristic = deviceInfoService.getCharacteristic(uuid);
        if (wareCharacteristic == null) {
            Log.e(TAG, "wareType " + wareType + " Characteristic not found!");
            return;
        }
        mGatt.readCharacteristic(wareCharacteristic);
        Log.e(TAG, "Read wareType " + wareType);
    }

    public void disConnect() {
        if (mGatt != null) {
            mGatt.disconnect();
        }
    }

    public BleDevManager getBleDevManager() {
        return mBleDevManager;
    }

    public void setOnDeviceVersionListener(OnDeviceVersionListener listener) {
        this.mOnDeviceVersionListener = listener;
    }

    /**
     * TODO
     * Send code to read device's battery information.
     * The interface will return information Including whether it is charging, or whether it is full, or the remaining power.
     */
    private void readDevBatteryInfo() {
        mBatteryQueryTimer = new Timer();
        mBatteryQueryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mBleDevManager.getBatteryTask().batteryQuery();
            }
        }, 100L, 300 * 1000L);
    }

    /**
     * when the {@link #enableHRNotification()} return true,you can start to loop this method by step to read some device information.
     * What information you want to get from the device, you can define them by step to send command code like this.
     * But whatever the order will be defined, {@link #readDevBatteryInfo();} could be best to do in the last step.
     */
    public void dataQuery(int step) {
        switch (step) {
            case DATA_QUERY_SOFTWARE_VER:
                getDevWareVersion(WareType.VER_SOFTWARE);
                break;
            case DATA_QUERY_HARDWARE_VER:
                getDevWareVersion(WareType.VER_HARDWARE);
                break;
            case DATA_QUERY_FIRMWARE_VER:
                getDevWareVersion(WareType.VER_FIRMWARE);
                break;
            case DATA_QUERY_CONFIRM_ECG_MODULE_EXIST:
                //TODO To confirm the ECG module of the device is exist.
                mBleDevManager.getEcgTask().checkModuleExist(new OnSendCodeToDevCallback() {
                    @Override
                    public void onReceived() {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dataQuery(DATA_QUERY_CONFIRM_BG_MODULE_EXIST);
                    }
                });
                break;
            case DATA_QUERY_CONFIRM_BG_MODULE_EXIST:
                //TODO To confirm the BG module of the device is exist.
                mBleDevManager.getTestPaperTask().checkModuleExist(() -> {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dataQuery(DATA_QUERY_ID_AND_KEY);
                });
                break;
            case DATA_QUERY_ID_AND_KEY:
                //TODO To read device id and key.
                mBleDevManager.getDeviceTask().getDeviceInfo();
                break;
            case DATA_QUERY_BATTERY_INFO:
                readDevBatteryInfo();
                break;
        }
    }

    /**
     * To stop the measure which module is doing.
     */
    public void stopMeasure() {
        switch (mType) {
            case MeasureType.BP:
                mBleDevManager.getBpTask().stop();
                break;
            case MeasureType.BT:
                mBleDevManager.getBtTask().stop();
                break;
            case MeasureType.ECG:
//            case ECG_RAW:
                mBleDevManager.getEcgTask().stop();
                break;
            case MeasureType.SPO2:
                mBleDevManager.getOxTask().stop();
                break;
            case MeasureType.BG:
                break;
        }
    }

    /**
     * To define a class that extends {@link #CmdThread} to send cmd byte array to device.
     */
    private class CmdSendThread extends BleDevManager.CmdThread {

        private CmdSendThread() {
            super();
        }

//        @Override
//        public void run() {
//            Log.e(TAG, "CMD SEND bytes:   " + Arrays.toString(cmd));
//            Log.e(TAG, "CMD SEND mHRMChara:" + mHRMChara + ", mGatt:" + mGatt);
//
//            //TODO clear cmd byte array had been sent to device.
//            mBleDevManager.clearCmdToSend(cmd[Protocol.OFFSET_MSG_TYPE], cmd[Protocol.OFFSET_DATA]);
//        }

        @Override
        protected void dealCmd(byte[] cmd) {
            if (mHRMChara != null && mGatt != null && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                mHRMChara.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mHRMChara.setValue(cmd);
                mGatt.writeCharacteristic(mHRMChara);
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
            } else {
                Log.e(TAG, "send error");
                if (mHRMChara == null)
                    Log.e(TAG, "mHRMChara==null");
                if (mGatt == null)
                    Log.e(TAG, "mGatt==null");
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Log.e(TAG, "BluetoothAdapter not enable");
                }
            }
        }
    }
}

