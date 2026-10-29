package org.intelehealth.app.ayu.visit.hba1c;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.klivekit.utils.Constants;

import java.util.ArrayList;

import biosense.sreyasvpariyath.com.biosenselib.helper.Communicator;
import biosense.sreyasvpariyath.com.biosenselib.helper.ControlCentre;

public class MeasurementActivity extends AppCompatActivity implements Communicator {
    ControlCentre controlCentre;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestRuntimePermissions();

        // Get selected device from Intent (passed by DeviceAdapter)
        String deviceAddress = getIntent().getStringExtra("DEVICE_ADDRESS");
        String deviceName = getIntent().getStringExtra("DEVICE_NAME");
        if (deviceAddress == null) deviceAddress = "";
        if (deviceName == null) deviceName = "HbA1c";

        controlCentre = new ControlCentre(
                this,                   // Communicator
                this,                   // Context
                this,                   // Activity
                deviceAddress,
                Constants.devId_A1Chek, // based on device type
                deviceName
        );

        controlCentre.startReceiver();
    }

    private void requestRuntimePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
            }, 100);
        } else {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controlCentre != null) {
            controlCentre.stopReceiver();
        }
    }

    @Override
    public void setHbA1cReading(String reading, String date, String time, String srno) {
        Log.d("Biosense", "HbA1c: " + reading);
    }

    @Override
    public void setHB(String s) {

    }

    @Override
    public void setBPReading(String systolic, String diastolic, String pulse) {
        Log.d("Biosense", "BP: " + systolic + "/" + diastolic);
    }

    @Override
    public void onBpDeviceError() {

    }

    @Override
    public boolean go(String s) {
        return false;
    }

    @Override
    public void setGlucoseReading(String text) {
        Log.d("Biosense", "Glucose: " + text);
    }

    @Override
    public void testStarted(boolean b) {

    }

    @Override
    public void stopNotiFication() {

    }

    @Override
    public void setConnectionStatus(String s, boolean b) {

    }

    @Override
    public void setSwitchActivity() {

    }

    @Override
    public void setBatteryLevel(int i) {

    }

    @Override
    public void setManufacturerName(String s) {

    }

    @Override
    public void setSerialNumber(String s) {

    }

    @Override
    public void setModelNumber(String s) {

    }

    @Override
    public void getOfflineResults(ArrayList<String> arrayList) {

    }
}
