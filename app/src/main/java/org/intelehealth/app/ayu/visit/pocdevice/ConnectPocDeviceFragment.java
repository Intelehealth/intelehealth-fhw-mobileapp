package org.intelehealth.app.ayu.visit.pocdevice;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.ble.Device;
import com.ayudevice.ayusynksdk.ble.constants.DeviceConnectionState;
import com.ayudevice.ayusynksdk.ble.constants.DeviceStrength;
import com.ayudevice.ayusynksdk.ble.listener.AyuDeviceListener;
import com.ayudevice.ayusynksdk.ble.listener.DeviceScanListener;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.diagnostics.DiagnosticsCollectionFragment;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.pocdevice.adapter.DeviceListAdapter;
import org.intelehealth.app.ayu.visit.pocdevice.adapter.DeviceListObject;
import org.intelehealth.app.databinding.FragmentConnectPocdeviceBinding;
import org.intelehealth.app.models.DiagnosticsModel;
import org.intelehealth.app.models.VitalsObject;

public class ConnectPocDeviceFragment extends Fragment implements AyuDeviceListener{

    private FragmentConnectPocdeviceBinding binding;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;
    private String visitUuid;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect_pocdevice, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cvSelect.setOnClickListener(view1 -> {
            binding.imgSelect.setVisibility(View.VISIBLE);
            navigateToConnectScreen(ConnectPocDeviceFragment.this);
        });

        binding.btnDisconnect.setOnClickListener(view1 -> {
            AyuSynk.getBleInstance().disconnect();
        });

        binding.btnTryagain.setOnClickListener(view1 -> {
            navigate_pocListfragment();
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "onResume: "+AyuSynk.getBleInstance().isDeviceConnected());
        AyuSynk.getBleInstance().setAyuDeviceListener(this);
        if (AyuSynk.getBleInstance().isDeviceConnected() == DeviceConnectionState.DEVICE_CONNECTED) {
            onDeviceConnected();
        } else {
            onDeviceDisconnected();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AyuSynk.getBleInstance().setAyuDeviceListener(null);
    }


    private void onDeviceConnected() {
        setDeviceStrength(AyuSynk.getBleInstance().getDeviceStrength());
        deviceBatteryUpdate(AyuSynk.getBleInstance().getCurrentBatteryLevel());
        binding.cvSelect.setTag(2);
        binding.cvSelect.setVisibility(View.GONE);
        binding.lltryagain.setVisibility(View.GONE);
        binding.lldeviceConnected.setVisibility(View.VISIBLE);
        binding.llrecordType.setVisibility(View.VISIBLE);
    }

    private void onDeviceDisconnected() {
        binding.cvSelect.setTag(1);
        binding.cvSelect.setVisibility(View.VISIBLE);
        binding.lldeviceConnected.setVisibility(View.GONE);
        binding.llrecordType.setVisibility(View.GONE);
        binding.imgSelect.setVisibility(View.INVISIBLE);
    }
    private void setDeviceStrength(DeviceStrength strength) {
        if (binding != null) {
            if (strength == DeviceStrength.DEVICE_SIGNAL_WEAK)
                binding.tvdeviceStrength.setText(getResources().getString(R.string.weak));
            else
                binding.tvdeviceStrength.setText(getResources().getString(R.string.strong));
        }
    }
    void setBluetoothOn(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(getContext(),
                    getContext().getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            binding.lltryagain.setVisibility(View.VISIBLE);
            binding.imgSelect.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(),
                    getContext().getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            binding.lltryagain.setVisibility(View.GONE);
            navigate_pocListfragment();
            // Bluetooth is Turned ON.

        }
    }

    void navigate_pocListfragment(){
        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_12_DEVICE_LIST, mIsEditMode, mVitalsObject);
    }
    public static ConnectPocDeviceFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode,VitalsObject vitalsObject) {
        ConnectPocDeviceFragment fragment = new ConnectPocDeviceFragment();
        fragment.mIsEditMode =  isEditMode;
        fragment.mVitalsObject =  vitalsObject;
        return fragment;
    }


    void navigateToConnectScreen(Fragment fragment) {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                if (!AyuSynk.getBleInstance().isAllBluetoothPermissionGranted())
                    AyuSynk.getBleInstance().requestBluetoothPermission(getActivity(), 11);
                else
                    setBluetoothOn();

            }
        }else{
            if (!AyuSynk.getBleInstance().isAllBluetoothPermissionGranted()){
                AyuSynk.getBleInstance().requestBluetoothPermission(getActivity(), 11);

            }else{
                setBluetoothOn();
            }


        }
    }
    private boolean checkLocationServices() {
        if (!AyuSynk.getBleInstance().isLocationEnabled()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void deviceConnectionStrength(DeviceStrength deviceStrength) {
        setDeviceStrength(deviceStrength);
    }

    @Override
    public void deviceConnectionState(DeviceConnectionState state) {
        Log.d("TAG", "devicestate"+"deviceConnectionState: "+state);
        if (state != DeviceConnectionState.DEVICE_CONNECTED) {
            onDeviceDisconnected();
        } else {
            onDeviceConnected();
        }
    }

    @Override
    public void deviceBatteryUpdate(int batteryUpdate) {
        binding.customBatteryMeter.setBatteryLevel(batteryUpdate);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AyuSynk.getBleInstance().stopScan();
    }
}
