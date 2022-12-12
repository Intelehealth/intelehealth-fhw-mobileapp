package org.intelehealth.msfarogyabharat.followuppatients;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.msfarogyabharat.models.FollowUpModel;
import org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class FollowUpPatientAdapter extends RecyclerView.Adapter<FollowUpPatientAdapter.Myholder> {
    List<FollowUpModel> patients = new ArrayList<>();
    Context context;
    LayoutInflater layoutInflater;

    public FollowUpPatientAdapter(List<FollowUpModel> patients, Context context) {
        this.patients.addAll(patients);
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_search, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final FollowUpModel patinet = patients.get(position);

        if (patinet != null) {
            String age = DateAndTimeUtils.getAgeInYearMonth(patinet.getDate_of_birth(), context);
            //String dob = DateAndTimeUtils.SimpleDatetoLongDate(patinet.getDateofbirth());
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;

            if (patinet.getOpenmrs_id() != null)
                holder.headTextView.setText(patinet.getFirst_name() + " " + patinet.getLast_name() + ", " + patinet.getOpenmrs_id());
            else
                holder.headTextView.setText(patinet.getFirst_name() + " " + patinet.getLast_name());

            holder.bodyTextView.setText(body);
            if (TextUtils.isEmpty(patinet.getComment()) && patinet.getComment() == null && patinet.getComment().equalsIgnoreCase("null")) {
                holder.commentTextView.setVisibility(View.GONE);
            } else {
                holder.commentTextView.setText(Html.fromHtml(patinet.getComment()));
                holder.commentTextView.setVisibility(View.VISIBLE);
            }
            if (patinet.getValue() == 0 || patinet.getValue() == 1) {
                holder.dueDateTextView.setVisibility(View.GONE);
            } else {
                if (patinet.getFollowup_date() != null) {
                    holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + " " + patinet.getFollowup_date().substring(0, 10));
                    holder.dueDateTextView.setVisibility(View.VISIBLE);
                }
            }
        }

//        if (!patinet.getVisitStartDate().equalsIgnoreCase("null")) {
//
//            String[] arrSplit_2 = patinet.getComment().split("-");
//            String mValue = arrSplit_2[arrSplit_2.length - 1];
//            String visitDateStartDate = patinet.getVisitStartDate();
//            SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//            Date startDate = null;
//            try {
//                startDate = sd1.parse(visitDateStartDate);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//
//            String newStartDate = new SimpleDateFormat("dd-MM-yyyy").format(startDate);
//
//            int days=mGetDaysAccording(newStartDate);
//
//
//            if (days > 0) {
//                if (days % 2 == 0) {
//                    if (mValue.trim().equalsIgnoreCase("Mild.") || mValue.trim().equalsIgnoreCase("Moderate.") ||mValue.trim().contains("Moderate.") ||mValue.trim().contains("Mild."))
//                    {
//                        holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + "" +newStartDate);
//                    }
//                    else  if(mValue.trim().equalsIgnoreCase("Severe.")){
//                        holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + "Today" + newStartDate);
//
//                    }else{
////                        holder.linearLayout.setVisibility(View.GONE);
////                        patients.remove(patinet);
////                        notifyItemRemoved(position);
////                        notifyItemRangeChanged(position, patients.size());
//                        holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + "remove" + newStartDate);
//                    }
//                }
//                else {
//                    if(mValue.equalsIgnoreCase("Severe.")) {
//                        holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + getCurrentDate());
//                    }
//                    if(mValue.trim().equalsIgnoreCase("Mild.") || mValue.trim().equalsIgnoreCase("Moderate.")||mValue.trim().contains("Moderate.") ||mValue.trim().contains("Mild."))
//                    {
//                        holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + "R" + newStartDate);
//                    }
//                    else{
//
//                        holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + "???" + newStartDate);
////                        patients.remove(patinet);
//////                        notifyItemRemoved(position);
////                        notifyItemRangeChanged(position, patients.size());
////                        patients.remove(patinet);
//                    }
////                patients.remove(position);
////                notifyDataSetChanged();
////                    holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + patinet.getFollowup_date());
//
//                    //todo remove from list===========
//                }
//
//
//            } else {
//                holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + patinet.getVisitStartDate());
//
//
//            }
////            holder.linearLayout.setVisibility(View.VISIBLE);
//            holder.dueDateTextView.setVisibility(View.VISIBLE);
////          holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.lite_red));
////            holder.dueDateTextView.setText(context.getResources().getString(R.string.due_on) + " " + patinet.getFollowup_date().substring(0, 10));
//
//        }
//        else {
//            holder.linearLayout.setVisibility(View.GONE);
//            holder.dueDateTextView.setVisibility(View.GONE);
//        }

        holder.linearLayout.setOnClickListener(v -> {
            Log.d("search adapter", "patientuuid" + patinet.getUuid());
            String patientStatus = "returning";
            Intent intent = new Intent(context, PatientDetailActivity.class);
            intent.putExtra("patientUuid", patinet.getPatientuuid());
            intent.putExtra("patientName", patinet.getFirst_name() + "" + patinet.getLast_name());
            intent.putExtra("status", patientStatus);
            intent.putExtra("tag", "search");
            intent.putExtra("intentTag2", "findPatient");
            intent.putExtra("hasPrescription", "false");
            intent.putExtra(PatientDetailActivity.EXTRA_SHOW_MEDICAL_ADVICE, true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class Myholder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        private TextView headTextView;
        private TextView bodyTextView;
        private TextView commentTextView, dueDateTextView;

        public Myholder(View itemView) {
            super(itemView);
            Log.d("Adapter", "Current thread: " + Thread.currentThread().getName());
            headTextView = itemView.findViewById(R.id.list_item_head);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
            commentTextView = itemView.findViewById(R.id.list_item_comment);
            dueDateTextView = itemView.findViewById(R.id.list_item_DueDate);
            linearLayout = itemView.findViewById(R.id.searchlinear);
        }
    }
}
