package org.intelehealth.app.ayu.visit.hba1c;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;

public class BleSearchActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private Button btnStartSearch;
    private TextView textViewSearchHeader;

    // We will just use a simple ArrayAdapter and ListView or RecyclerView
    // Wait, we defined a RecyclerView in activity_ble_search.xml.
    // Let's implement a quick Adapter for it.
    private RecyclerView recyclerViewDevices;
    private DeviceAdapter deviceAdapter;
    private List<BluetoothDevice> deviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_search);
        btnStartSearch = findViewById(R.id.btnStartSearch);
        textViewSearchHeader = findViewById(R.id.textViewSearchHeader);
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices);

        handler = new Handler();
        deviceList = new ArrayList<>();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        recyclerViewDevices.setLayoutManager(new LinearLayoutManager(this));

     //   deviceAdapter = new DeviceAdapter(this, deviceList);
        recyclerViewDevices.setAdapter(deviceAdapter);

        btnStartSearch.setOnClickListener(v -> {
            if (hasPermissions()) {
                if (isScanning) {
                    stopScan();
                } else {
                    startScan();
                }
            } else {
                requestPermissions();
            }
        });
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan();
        }
    }

    private void startScan() {
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "Bluetooth not supported or enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();

        isScanning = true;
        btnStartSearch.setText("Stop Search");
        textViewSearchHeader.setText("Scanning...");

        handler.postDelayed(this::stopScan, SCAN_PERIOD);
        bluetoothLeScanner.startScan(scanCallback);
    }

    private void stopScan() {
        if (!isScanning || bluetoothLeScanner == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        isScanning = false;
        btnStartSearch.setText("Start Search");
        textViewSearchHeader.setText("Scan Complete");
        bluetoothLeScanner.stopScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (!deviceList.contains(device)) {
                deviceList.add(device);
                deviceAdapter.notifyItemInserted(deviceList.size() - 1);
                textViewSearchHeader.setText("Found: " + deviceList.size() + " devices");
            }
        }
    };
}