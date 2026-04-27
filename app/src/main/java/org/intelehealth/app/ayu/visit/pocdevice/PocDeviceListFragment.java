package org.intelehealth.app.ayu.visit.pocdevice;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.ble.Device;
import com.ayudevice.ayusynksdk.ble.constants.DeviceConnectionState;
import com.ayudevice.ayusynksdk.ble.constants.DeviceStrength;
import com.ayudevice.ayusynksdk.ble.listener.AyuDeviceListener;
import com.ayudevice.ayusynksdk.ble.listener.DeviceScanListener;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.pocdevice.adapter.DeviceListAdapter;
import org.intelehealth.app.ayu.visit.pocdevice.adapter.DeviceListObject;
import org.intelehealth.app.databinding.FragmentPocdeviceListBinding;
import org.intelehealth.app.models.VitalsObject;

public class PocDeviceListFragment extends Fragment implements DeviceScanListener, DeviceListAdapter.OnListInteractionListener{

    private FragmentPocdeviceListBinding binding;
    private DeviceListAdapter deviceListAdapter;
    private VisitCreationActionListener mActionListener;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pocdevice_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
    }
    public static PocDeviceListFragment newInstance( boolean isEditMode, VitalsObject vitalsObject) {
        PocDeviceListFragment fragment = new PocDeviceListFragment();
        fragment.mIsEditMode =  isEditMode;
        fragment.mVitalsObject =  vitalsObject;
        return fragment;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceListAdapter = new DeviceListAdapter(getContext(), this);
        binding.deviceList.setAdapter(deviceListAdapter);
        AyuSynk.getBleInstance().startScan(this);
        AyuSynk.getBleInstance().setDeviceScanListener(this);
        AyuSynk.getBleInstance().setAyuDeviceListener(new AyuDeviceListener() {
            @Override
            public void deviceConnectionStrength(DeviceStrength strength) {

            }

            @Override
            public void deviceConnectionState(DeviceConnectionState state) {
                if (state == DeviceConnectionState.DEVICE_CONNECTED)
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }

            @Override
            public void deviceBatteryUpdate(int batteryUpdate) {

            }
        });

        binding.ivBackArrowTerms.setOnClickListener(view1 -> {
            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
        });
    }
    @Override
    public void onScanStart() {

    }

    @Override
    public void onDeviceFound(Device device) {
        Log.d("TAG", "onDeviceFound: "+device);
        requireActivity().runOnUiThread(() -> {
            binding.llprogressbar.setVisibility(View.GONE);
            binding.deviceList.setVisibility(View.VISIBLE);
            deviceListAdapter.addDevice(new DeviceListObject(device.getName(), device.getAddress()));
        });
    }

    @Override
    public void onScanFinish() {

    }

    @Override
    public void onScanFailed(int i) {

    }

    @Override
    public void onBLEConnectClicked(DeviceListObject device) {
        AyuSynk.getBleInstance().connect(device.getBleAddress());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AyuSynk.getBleInstance().stopScan();
        binding = null;
    }
}
