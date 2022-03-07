package org.intelehealth.ekalhelpline.activities.callDoctor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.models.DoctorAttributes;
import org.intelehealth.ekalhelpline.models.DoctorDetailsModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CallDoctorAdapter extends RecyclerView.Adapter<CallDoctorAdapter.Myholder> {

    List<DoctorDetailsModel> doctorDetailsModelList;
    Context context;

    public CallDoctorAdapter(List<DoctorDetailsModel> doctorDetailsModelList, Context context) {
        this.doctorDetailsModelList = doctorDetailsModelList;
        this.context = context;
    }

    // method for filtering our recyclerview items.
    public void filterList(List<DoctorDetailsModel> filterllist) {
        doctorDetailsModelList = filterllist;
        notifyDataSetChanged();
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
        boolean callFlag=false;
        if(doctorDetailsModel!=null) {
            holder.nameTextView.setText(doctorDetailsModel.getDoctorName() + " (" + ((!TextUtils.isEmpty(doctorDetailsModel.getDoctorGender())) ? doctorDetailsModel.getDoctorGender() : "N/A") + ") ");
            holder.specialityTextView.setText(((!TextUtils.isEmpty(doctorAttributes.getDoctorQualification())) ? doctorAttributes.getDoctorQualification() : "N/A") + ", " + ((!TextUtils.isEmpty(doctorAttributes.getDoctorSpecialization())) ? doctorAttributes.getDoctorSpecialization() : "N/A"));
            if (doctorAttributes.getDoctorStartTimings() != null && !doctorAttributes.getDoctorStartTimings().isEmpty()
            && doctorAttributes.getDoctorEndTimings() != null && !doctorAttributes.getDoctorEndTimings().isEmpty()) {
                holder.timingTextView.setText("Timings: " + doctorAttributes.getDoctorStartTimings()+"-"+doctorAttributes.getDoctorEndTimings());
                String startDate = doctorAttributes.getDoctorStartTimings();
                String endDate = doctorAttributes.getDoctorEndTimings();
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa");
                    Date d1 = formatter.parse(startDate);
                    Date d2 = formatter.parse(endDate);

                    String currentTime=formatter.format(Calendar.getInstance().getTime());
                    Date date = formatter.parse(currentTime);
                    if (date.compareTo(d1) > 0 && date.compareTo(d2) < 0) {
                        //boolean val1 = date.compareTo(d1) > 0;
                        //boolean val2 = date.compareTo(d2) < 0;
                        //System.out.println(val1 + "/" + val2);
                        holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.online_green));
                        callFlag=true;
                    }else{
                        holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.offline_red));
                        callFlag=false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callFlag=false;
                }
            } else {
                holder.timingTextView.setText("Timings: N/A");
                holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.offline_red));
                callFlag=false;
            }
            /*if(doctorDetailsModel.getDoctorStatus().equalsIgnoreCase("active"))
                holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.online_green));
            else
                holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.offline_red));
*/
            if(doctorAttributes.getDoctorPhoneNo()!=null && !doctorAttributes.getDoctorPhoneNo().isEmpty() && callFlag==true)
            {
                holder.callImageView.setBackgroundResource(R.drawable.ic_baseline_call_24);
                holder.callImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (context instanceof CallDoctorActivity) {
                            ((CallDoctorActivity)context).callDoctorApi(doctorDetailsModel.getDoctorAtributesList().getDoctorPhoneNo());
                        }
                    }
                });
            }else{
                Drawable res = context.getResources().getDrawable(R.drawable.ic_baseline_call_inactive_24);
                holder.callImageView.setImageDrawable(res);
            }

            //commenting out code as not required for now.
//            if(doctorDetailsModel.getDoctorAtributesList().getDoctorWhatsApp()!=null)
//            {
//                holder.whatsAppImageView.setVisibility(View.VISIBLE);
//                holder.whatsAppImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (context instanceof CallDoctorActivity) {
//                            ((CallDoctorActivity)context).whatsAppDoctor(doctorDetailsModel.getDoctorAtributesList().getDoctorPhoneNo());
//                        }
//                    }
//                });
//            }
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
