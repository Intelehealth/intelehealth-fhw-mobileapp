package org.intelehealth.app.ayu.visit.pocdevice;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity.patientUuid;
import static org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity.visitUuid;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.ayudevices.cardiosynksdk.AyuDevice;
import com.ayudevices.cardiosynksdk.ble.Device;
import com.ayudevices.cardiosynksdk.ble.constants.DeviceConnectionState;
import com.ayudevices.cardiosynksdk.ble.constants.DeviceStrength;
import com.ayudevices.cardiosynksdk.ble.listener.AyuDeviceListener;
import com.ayudevices.cardiosynksdk.ble.listener.DeviceScanListener;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.databinding.FragmentConnectPocdeviceBinding;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.Set;

public class ConnectPocDeviceFragment extends Fragment implements AyuDeviceListener, DeviceScanListener {

    private FragmentConnectPocdeviceBinding binding;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;

    //String selectedType = "";
    private String selectedType = "";
    private OnDigitalScopeCompleteListener listener;

    private String type;
    private ArrayList<String> sounds;
    private String patientUuid, visitUuid, encounterUuid;


    public static ConnectPocDeviceFragment newInstance(
            String type,
            ArrayList<String> sounds,
            String patientUuid,
            String visitUuid,
            String encounterUuid) {

        ConnectPocDeviceFragment fragment = new ConnectPocDeviceFragment();

        Bundle args = new Bundle();
        args.putString("type", type);
        args.putStringArrayList("sounds", sounds);
        args.putString("patientUuid", patientUuid);
        args.putString("visitUuid", visitUuid);
        args.putString("encounterUuid", encounterUuid);

        fragment.setArguments(args);
        return fragment;
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
        Bundle args = getArguments();
        if (args == null) throw new RuntimeException("Arguments missing");

        type = args.getString("type", "heart");
        sounds = args.getStringArrayList("sounds");
        patientUuid = args.getString("patientUuid", "");
        visitUuid = args.getString("visitUuid", "");
        encounterUuid = args.getString("encounterUuid", "");

        if (sounds == null) sounds = new ArrayList<>();

        binding.btBleConnectdevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToConnectScreen(ConnectPocDeviceFragment.this);
            }
        });

       /* binding.cvSelect.setOnClickListener(view1 -> {
            binding.imgSelect.setVisibility(View.VISIBLE);
            navigateToConnectScreen(ConnectPocDeviceFragment.this);
        });*/
        binding.btBleDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VisitCreationActivity activity =
                        (VisitCreationActivity) getActivity();

                if (activity == null) return;

                // ✅ CHECK BOTH
                if (!activity.isHeartRecorded() || !activity.isLungRecorded()) {

                    Toast.makeText(getContext(),
                            "Please record both Heart and Lung",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ ALL DONE → PROCEED
                if (listener != null) {
                    listener.onDigitalScopeCompleted();
                }
            }
        });
        binding.btnDisconnect.setOnClickListener(view1 -> {
            AyuDevice.getBleInstance().disconnect();
        });

        binding.btnTryagain.setOnClickListener(view1 -> {
            //navigate_pocListfragment();
           // AyuDevice.getBleInstance().startScan();
            setBluetoothOn();
        });
        binding.llHeartSounds.setOnClickListener(view1 -> {
            selectedType = "heart";
           // setSelectedUI(true);
            binding.btBleStartRecording.setVisibility(VISIBLE);
        });

        binding.llLungSounds.setOnClickListener(view1 -> {
            selectedType = "lung";
           // setSelectedUI(false);
            binding.btBleStartRecording.setVisibility(VISIBLE);
        });

        binding.btBleStartRecording.setOnClickListener(v -> {
            openSoundScreen();
         /*   if (selectedType.isEmpty()) {
                Toast.makeText(getContext(), "Please select Heart or Lung", Toast.LENGTH_SHORT).show();
                return;
            }*/

         //   startRecording(selectedType);
        });


    }
    private void openSoundScreen() {

        ArrayList<String> filteredSounds = new ArrayList<>();

        if (selectedType.equals("heart")) {
            filteredSounds.add("Aortic");
            filteredSounds.add("Pulmonic");
            filteredSounds.add("Tricuspid");
            filteredSounds.add("Mitral");
        } else {
            filteredSounds.add("Upper Left");
            filteredSounds.add("Upper Right");
            filteredSounds.add("Lower Left");
            filteredSounds.add("Lower Right");
        }

        /*Bundle args = new Bundle();
        args.putString("type", type);
        args.putStringArrayList("sounds", sounds);
        args.putString("patientUuid", patientUuid);
        args.putString("visitUuid", visitUuid);
        args.putString("encounterUuid", encounterUuid);*/

       /* SoundFragment fragment = SoundFragment.newInstance(args);

        // ✅ IMPORTANT: SET TARGET
        fragment.setTargetFragment(getParentFragment(), 101);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_steps_body, fragment)
                .addToBackStack("sound")
                .commit();*/
        SoundDialogFragment dialog = SoundDialogFragment.newInstance(
                selectedType,
                sounds,
                patientUuid,
                visitUuid,
                encounterUuid
        );

        dialog.setListener(() -> {
            // After all sounds saved, pop this fragment so the user returns to
            // PhysicalExaminationFragment. The "sound_done" fragment result was
            // posted by SoundDialogFragment and will deliver to PhysicalExaminationFragment's
            // listener once it becomes STARTED again, which advances to the next question.
            if (isAdded() && !isStateSaved()) {
                getParentFragmentManager().popBackStack();
            }
        });

        dialog.show(getParentFragmentManager(), "sound_dialog");

    }

    private void startRecording(String type) {
        if ("heart".equals(type)) {
            mActionListener.onFormSubmitted(
                    VisitCreationActivity.SELECT_HEART,
                    mIsEditMode,
                    mVitalsObject
            );
        } else if ("lung".equals(type)) {
            mActionListener.onFormSubmitted(
                    VisitCreationActivity.SELECT_LUNG,
                    mIsEditMode,
                    mVitalsObject
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "onResume: " + AyuDevice.getBleInstance().isDeviceConnected());
        AyuDevice.getBleInstance().setAyuDeviceListener(this);
        if (AyuDevice.getBleInstance().isDeviceConnected() == DeviceConnectionState.DEVICE_CONNECTED) {
            onDeviceConnected();
        } else {
            onDeviceDisconnected();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AyuDevice.getBleInstance().setAyuDeviceListener(null);
    }


    private void onDeviceConnected() {
        setDeviceStrength(AyuDevice.getBleInstance().getDeviceStrength());
        deviceBatteryUpdate(AyuDevice.getBleInstance().getCurrentBatteryLevel());
        binding.cvSelect.setTag(2);
        // binding.cvSelect.setVisibility(View.GONE);
        binding.btBleConnectdevice.setVisibility(GONE);
        binding.btBleStartRecording.setVisibility(VISIBLE);
        binding.lltryagain.setVisibility(GONE);
        binding.lldeviceConnected.setVisibility(VISIBLE);
        //binding.llrecordType.setVisibility(View.VISIBLE);
    }

    private void onDeviceDisconnected() {
        binding.cvSelect.setTag(1);
        // binding.cvSelect.setVisibility(View.VISIBLE);
        binding.btBleConnectdevice.setVisibility(VISIBLE);
        binding.btBleStartRecording.setVisibility(GONE);
        binding.lldeviceConnected.setVisibility(GONE);
        // binding.llrecordType.setVisibility(View.GONE);
        binding.imgSelect.setVisibility(View.INVISIBLE);
    }
  /*  private void openHeartScreen(ArrayList<String> heartSounds) {
        VisitCreationActivity activity = (VisitCreationActivity) requireActivity();
        RecordHeartSoundsFragment fragment =
                RecordHeartSoundsFragment.newInstance(
                        null, // or commonVisitData
                        false,
                        null,
                        activity.visitUuid,
                        heartSounds
                );

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_steps_summary, fragment)
                .addToBackStack("heart")
                .commit();
    }*/
    private void setDeviceStrength(DeviceStrength strength) {
        if (binding != null) {
            if (strength == DeviceStrength.DEVICE_SIGNAL_WEAK)
                binding.tvdeviceStrength.setText(getResources().getString(R.string.weak));
            else
                binding.tvdeviceStrength.setText(getResources().getString(R.string.strong));
        }
    }

    void setBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(getContext(),
                    getContext().getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            binding.lltryagain.setVisibility(VISIBLE);
            binding.imgSelect.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(),
                    getContext().getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            binding.lltryagain.setVisibility(GONE);
            AyuDevice.getBleInstance().startScan(this);
            AyuDevice.getBleInstance().setDeviceScanListener(this);

        }
    }

    void navigate_pocListfragment() {
       // mActionListener.onFormSubmitted(VisitCreationActivity.STEP_12_DEVICE_LIST, mIsEditMode, mVitalsObject);
        mActionListener.onFormSubmitted(
                VisitCreationActivity.STEP_12_DEVICE_LIST,
                mIsEditMode,
                mVitalsObject
        );
    }

    public static ConnectPocDeviceFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, VitalsObject vitalsObject) {
        ConnectPocDeviceFragment fragment = new ConnectPocDeviceFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.mVitalsObject = vitalsObject;
        return fragment;
    }

    void navigateToConnectScreen(Fragment fragment) {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                if (!AyuDevice.getBleInstance().isAllBluetoothPermissionGranted())
                    AyuDevice.getBleInstance().requestBluetoothPermission(getActivity(), 11);
                else
                    setBluetoothOn();

            }
        } else {
            if (!AyuDevice.getBleInstance().isAllBluetoothPermissionGranted()) {
                AyuDevice.getBleInstance().requestBluetoothPermission(getActivity(), 11);

            } else {
                setBluetoothOn();
            }


        }
    }

    private boolean checkLocationServices() {
        if (!AyuDevice.getBleInstance().isLocationEnabled()) {
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
        Log.d("TAG", "devicestate" + "deviceConnectionState: " + state);
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
    public void onScanStart() {

    }

    @Override
    public void onDeviceFound(Device device) {
        if (device != null && device.getAddress() != null) {
            AyuDevice.getBleInstance().connect(device.getAddress());
            AyuDevice.getBleInstance().stopScan();
        }
    }

    @Override
    public void onScanFinish() {

    }

    @Override
    public void onScanFailed(int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AyuDevice.getBleInstance().stopScan();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDigitalScopeCompleteListener) {
            listener = (OnDigitalScopeCompleteListener) context;
        }
    }

    public interface OnDigitalScopeCompleteListener {
        void onDigitalScopeCompleted();
    }


}
