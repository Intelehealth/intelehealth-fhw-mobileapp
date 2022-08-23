package org.intelehealth.app.activities.homeActivity.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.List;

/**
* Created by Prajwal Maruti Waingankar on 21-06-2022, 18:36
* Copyright (c) 2021 . All rights reserved.
* Email: prajwalwaingankar@gmail.com
* Github: prajwalmw
*/


public class BTAdapter extends RecyclerView.Adapter<BTAdapter.ViewHolder> {

    private List<BluetoothDevice> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;

    // data is passed into the constructor
    public BTAdapter(Context context, List<BluetoothDevice> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.language_list_item_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mData.get(position).getName();
        if(name.trim().equalsIgnoreCase("HCSE03012122100009"))
            name = mContext.getString(R.string.medical_device_1);
        else if(name.trim().equalsIgnoreCase("HCSE03012122100017"))
            name = mContext.getString(R.string.medical_device_2);
        else if(name.trim().equalsIgnoreCase("HCSE03012122100039"))
            name = mContext.getString(R.string.medical_device_3);
        Log.v("Blue", "Ble: " + name);
        holder.myTextView.setText(name);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<BluetoothDevice> getDevices() {
        return mData;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ImageView status_imv;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.text_tv);
            status_imv = itemView.findViewById(R.id.status_imv);
            status_imv.setImageDrawable(mContext.getDrawable(R.drawable.user_online_green_indicator));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(mData.get(getAdapterPosition()));
        }
    }


    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(BluetoothDevice bluetoothDevice);
    }
}
