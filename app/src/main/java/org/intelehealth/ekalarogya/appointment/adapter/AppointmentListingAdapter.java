package org.intelehealth.ekalarogya.appointment.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.appointment.model.AppointmentInfo;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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
            genericViewHolder.dayTextView.setText(StringUtils.getTranslatedDays(genericViewHolder.appointmentInfo.getSlotDay(), new SessionManager(mContext).getAppLanguage()));
            genericViewHolder.statusTextView.setText(StringUtils.getAppointmentBookStatus(genericViewHolder.appointmentInfo.getStatus().toUpperCase(), new SessionManager(mContext).getAppLanguage()));
            genericViewHolder.doctorDetailsTextView.setText(String.format("%s, %s", genericViewHolder.appointmentInfo.getDrName(), getSpeciality(genericViewHolder.appointmentInfo.getSpeciality())));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            String currentDateTime = dateFormat.format(new Date());
            String slottime = genericViewHolder.appointmentInfo.getSlotDate() + " " + genericViewHolder.appointmentInfo.getSlotTime();

            long diff = 0;
            try {
                diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();
                long second = diff / 1000;
                long minutes = second / 60;
                Log.v("AppointmentInfo", "Diff minutes - " + minutes);
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }
    }

    private String getSpeciality(String speciality) {
        if (new SessionManager(mContext).getAppLanguage().equals("or")) {
            if (speciality.equalsIgnoreCase("General Physician")) {
                return "ସାଧାରଣ ଚିକିତ୍ସକ";
            }
        }else if (new SessionManager(mContext).getAppLanguage().equals("hi")) {
            if (speciality.equalsIgnoreCase("General Physician")) {
                return "सामान्य चिकित्सक";
            }
        } else if (new SessionManager(mContext).getAppLanguage().equals("bn")) {
            if (speciality.equalsIgnoreCase("General Physician")) {
                return "জেনারেল ফিজিশিয়ান";
            }
        } else {
            return speciality;
        }
        return speciality;
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

