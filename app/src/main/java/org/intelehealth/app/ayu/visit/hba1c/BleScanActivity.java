package org.intelehealth.app.ayu.visit.hba1c;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;

public class BleScanActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private boolean isScanning = false;

    private Handler handler = new Handler();
    private static final long SCAN_PERIOD = 10000;

    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        ListView listView = findViewById(R.id.listDevices);
        Button btnScan = findViewById(R.id.btnScan);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();

        btnScan.setOnClickListener(v -> startScan());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position);

            Intent result = new Intent();
            result.putExtra("device_address", device.getAddress());
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void startScan() {
        if (isScanning) return;

        deviceList.clear();
        adapter.clear();

        scanner = bluetoothAdapter.getBluetoothLeScanner();

        handler.postDelayed(() -> {
            isScanning = false;
            scanner.stopScan(callback);
        }, SCAN_PERIOD);

        isScanning = true;
        scanner.startScan(callback);
    }

    private final ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            runOnUiThread(() -> {
                BluetoothDevice device = result.getDevice();
                if (device.getName() == null) return;

                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                    adapter.add(device.getName() + "\n" + device.getAddress());
                }
            });
        }
    };
}