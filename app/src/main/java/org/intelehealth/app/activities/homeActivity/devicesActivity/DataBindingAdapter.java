package org.intelehealth.app.activities.homeActivity.devicesActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linktop.MonitorDataTransmissionManager;
import com.linktop.whealthService.OnBLEService;

import com.smartcaredoc.app.R;

import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class DataBindingAdapter extends RecyclerView.Adapter<DataBindingAdapter.BindingViewHolder> {
    private Context context;
    private List<OnBLEService.DeviceSort> mList;
    private ItemClickListener mClickListener;


    public DataBindingAdapter(Context context, List<OnBLEService.DeviceSort> mList) {
        this.context = context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.language_list_item_view, parent, false);
        return new DataBindingAdapter.BindingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
     //   holder.binding.setVariable(BR.item, getItem(position));
        OnBLEService.DeviceSort title = mList.get(position);
        holder.text_tv.setText(title.bleDevice.getName());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setItems(List<OnBLEService.DeviceSort> deviceList) {
        mList.clear();
        mList.addAll(deviceList);
        notifyDataSetChanged();
    }

    public class BindingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView text_tv;
        private ImageView status_imv;

        public BindingViewHolder(View itemView) {
            super(itemView);
            //  this.binding = binding;
            text_tv = itemView.findViewById(R.id.text_tv);
            status_imv = itemView.findViewById(R.id.status_imv);
            status_imv.setImageDrawable(context.getResources().getDrawable(R.drawable.user_online_green_indicator));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(mList.get(getAdapterPosition()));
        }
    }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(OnBLEService.DeviceSort deviceSort);
    }
}
