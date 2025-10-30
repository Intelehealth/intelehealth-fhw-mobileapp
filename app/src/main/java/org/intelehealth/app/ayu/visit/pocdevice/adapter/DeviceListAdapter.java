package org.intelehealth.app.ayu.visit.pocdevice.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;

    private OnListInteractionListener mlistener;

    private ArrayList<DeviceListObject> devices = new ArrayList<>();

    private boolean disableConnect = false;

    @Override
    public void onClick(View v) {
        disableConnect = true;
        notifyDataSetChanged();
        new Handler().postDelayed(() -> {
            if (context != null && !((Activity) context).isFinishing()) {
                disableConnect = false;
                notifyDataSetChanged();
            }
        }, 5000);
        mlistener.onBLEConnectClicked((DeviceListObject) v.getTag());
    }

    public interface OnListInteractionListener {
        void onBLEConnectClicked(DeviceListObject device);
    }

    static class ViewHolder {
        TextView name, address;
        Button connect;
    }

    public DeviceListAdapter(Context context, OnListInteractionListener interactionListener) {
        this.context = context;
        mlistener = interactionListener;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        DeviceListObject device = devices.get(position);

        if (convertView == null) {
            convertView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_poc_device_list, null);

            viewHolder = new ViewHolder();

            viewHolder.name = convertView.findViewById(R.id.tv_condeviceName);
            viewHolder.address = convertView.findViewById(R.id.tv_deviceAddress);
            viewHolder.connect = convertView.findViewById(R.id.btn_connect);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(device.getName());
        viewHolder.address.setText(device.getBleAddress());

        viewHolder.connect.setTag(device);

        if (disableConnect) {
            viewHolder.connect.setEnabled(false);
            viewHolder.connect.setAlpha(0.5f);
        } else {
            viewHolder.connect.setEnabled(true);
            viewHolder.connect.setAlpha(1f);
        }


        viewHolder.connect.setOnClickListener(this);
        return convertView;
    }

    public void addDevice(DeviceListObject device) {
        devices.add(device);
        notifyDataSetChanged();
    }

    public void clear() {
        devices.clear();
        notifyDataSetChanged();
    }
}
