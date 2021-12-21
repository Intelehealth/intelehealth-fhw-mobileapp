package org.intelehealth.unicef.appointment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.appointment.model.AppointmentInfo;
import org.intelehealth.unicef.appointment.model.SlotInfo;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AppointmentListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<AppointmentInfo> mItemList = new ArrayList<AppointmentInfo>();

    public interface OnItemSelection {
        public void onSelect(AppointmentInfo appointmentInfo);
    }

    private OnItemSelection mOnItemSelection;

    public AppointmentListingAdapter(RecyclerView recyclerView, Context context, List<AppointmentInfo> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_listing_view, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.appointmentInfo = mItemList.get(position);
            genericViewHolder.patientInfoTextView.setText(String.format("%s, %s", genericViewHolder.appointmentInfo.getPatientName(), genericViewHolder.appointmentInfo.getOpenMrsId()));
            genericViewHolder.dateTimeTextView.setText(String.format("%s %s", genericViewHolder.appointmentInfo.getSlotDate(), genericViewHolder.appointmentInfo.getSlotTime()));
            genericViewHolder.dayTextView.setText(genericViewHolder.appointmentInfo.getSlotDay());
            genericViewHolder.statusTextView.setText(genericViewHolder.appointmentInfo.getStatus().toUpperCase());
            genericViewHolder.doctorDetailsTextView.setText(String.format("%s, %s", genericViewHolder.appointmentInfo.getDrName(), genericViewHolder.appointmentInfo.getSpeciality()));

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView patientInfoTextView, dateTimeTextView, dayTextView, statusTextView, doctorDetailsTextView;
        AppointmentInfo appointmentInfo;

        GenericViewHolder(View itemView) {
            super(itemView);

            patientInfoTextView = itemView.findViewById(R.id.tv_patentName_openMRSID);
            dateTimeTextView = itemView.findViewById(R.id.tvDateTime);
            dayTextView = itemView.findViewById(R.id.tvDay);
            statusTextView = itemView.findViewById(R.id.tvStatus);
            doctorDetailsTextView = itemView.findViewById(R.id.tvDoctor);

        }


    }


}

