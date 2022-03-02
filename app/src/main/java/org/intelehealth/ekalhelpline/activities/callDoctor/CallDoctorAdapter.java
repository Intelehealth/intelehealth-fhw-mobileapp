package org.intelehealth.ekalhelpline.activities.callDoctor;

import android.content.Context;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.models.CallDoctorModel;
import org.intelehealth.ekalhelpline.models.DoctorAttributes;
import org.intelehealth.ekalhelpline.models.DoctorDetailsModel;

import java.util.List;

public class CallDoctorAdapter extends RecyclerView.Adapter<CallDoctorAdapter.Myholder> {

    List<DoctorDetailsModel> doctorDetailsModelList;
    Context context;

    public CallDoctorAdapter(List<DoctorDetailsModel> doctorDetailsModelList, Context context) {
        this.doctorDetailsModelList = doctorDetailsModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_doctor, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final DoctorDetailsModel doctorDetailsModel = doctorDetailsModelList.get(position);
        DoctorAttributes doctorAttributes = doctorDetailsModel.getDoctorAtributesList();
        holder.setIsRecyclable(false);
        if(doctorDetailsModel!=null)
        {
            holder.nameTextView.setText(doctorDetailsModel.getDoctorName() + " (" + doctorDetailsModel.getDoctorGender() + ") ");
            holder.specialityTextView.setText(doctorAttributes.getDoctorQualification() + ", " + doctorAttributes.getDoctorSpecialization());
            //TODO: Timings and status to be added.

            if(doctorAttributes.getDoctorPhoneNo()!=null && !doctorAttributes.getDoctorPhoneNo().isEmpty())
            {
                holder.callImageView.setVisibility(View.VISIBLE);
                holder.callImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (context instanceof CallDoctorActivity) {
                            ((CallDoctorActivity)context).callDoctorApi(doctorDetailsModel.getDoctorAtributesList().getDoctorPhoneNo());
                        }
                    }
                });
            }

            if(doctorDetailsModel.getDoctorAtributesList().getDoctorWhatsApp()!=null)
            {
                holder.whatsAppImageView.setVisibility(View.VISIBLE);
                holder.whatsAppImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (context instanceof CallDoctorActivity) {
                            ((CallDoctorActivity)context).whatsAppDoctor(doctorDetailsModel.getDoctorAtributesList().getDoctorPhoneNo());
                        }
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return doctorDetailsModelList.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        private TextView nameTextView;
        private TextView specialityTextView;
        private TextView timingTextView;
        private ImageView statusImageView;
        private ImageView callImageView;
        private ImageView whatsAppImageView;

        public Myholder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.list_item_name);
            specialityTextView = itemView.findViewById(R.id.list_item_speciality);
            linearLayout = itemView.findViewById(R.id.doctorDetailsLinear);
            timingTextView = itemView.findViewById(R.id.list_item_timing);
            statusImageView = itemView.findViewById(R.id.list_item_status);
            callImageView = itemView.findViewById(R.id.list_item_call);
            whatsAppImageView = itemView.findViewById(R.id.list_item_whatsapp);
        }
    }
}
