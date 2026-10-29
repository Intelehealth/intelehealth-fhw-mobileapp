package org.intelehealth.app.ayu.visit.hba1c;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<BluetoothDevice> deviceList;
    private Context context;

    private OnDeviceClickListener listener;

    public DeviceAdapter(Context context, List<BluetoothDevice> deviceList, OnDeviceClickListener listener) {
        this.context = context;
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);

        String deviceName = "Unknown Device";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            deviceName = device.getName() != null ? device.getName() : "Unknown Device";
        }

        holder.textViewDeviceName.setText(deviceName);
        holder.textViewDeviceAddress.setText(device.getAddress());

        holder.itemView.setOnClickListener(v -> {
            Log.d("BLE_ADAPTER", "Passing device to fragment");
            if (listener != null) {
                listener.onDeviceClick(device);
            }
        });
          /*  Intent intent = new Intent(context, ResultActivity.class);
            intent.putExtra("DEVICE_ADDRESS", device.getAddress());
            String name = "Unknown Device";
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                name = device.getName() != null ? device.getName() : "Unknown Device";
            }
            intent.putExtra("DEVICE_NAME", name);
            context.startActivity(intent);*/

    }
    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }
    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDeviceName;
        TextView textViewDeviceAddress;

        DeviceViewHolder(View itemView) {
            super(itemView);
            textViewDeviceName = itemView.findViewById(R.id.textViewDeviceName);
            textViewDeviceAddress = itemView.findViewById(R.id.textViewDeviceAddress);
        }
    }
}

