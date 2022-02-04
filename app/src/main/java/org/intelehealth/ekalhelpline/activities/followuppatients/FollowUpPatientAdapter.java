package org.intelehealth.ekalhelpline.activities.followuppatients;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalhelpline.models.FollowUpModel;
import org.intelehealth.ekalhelpline.models.dto.PatientDTO;
import org.intelehealth.ekalhelpline.utilities.DateAndTimeUtils;

import java.util.List;

/**
 * Created by Nishita Goyal on 27/09/21.
 * Github : @nishitagoyal
 */

public class FollowUpPatientAdapter extends RecyclerView.Adapter<FollowUpPatientAdapter.Myholder>{

    List<FollowUpModel> patients;
    Context context;

    public FollowUpPatientAdapter(List<FollowUpModel> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }


    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_followup_patient, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final FollowUpModel patinet = patients.get(position);
        holder.setIsRecyclable(false);
        if (patinet != null) {

            String age = DateAndTimeUtils.getAgeInYearMonth(patinet.getDate_of_birth(), context);
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age + "\n"
                    + context.getString(R.string.mobile_number_search) + patinet.getPhone_number();

            if (patinet.getOpenmrs_id() != null)
                holder.headTextView.setText(patinet.getFirst_name() + " " + patinet.getLast_name()
                        + ", " + patinet.getOpenmrs_id());
            else
                holder.headTextView.setText(patinet.getFirst_name() + " " + patinet.getLast_name());

            holder.bodyTextView.setText(body);

            if(patinet.getFollowUpType() == -1) {
                holder.linearLayout.setVisibility(View.VISIBLE);
                holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.lite_red));
                holder.indicatorTextView.setText(context.getResources().getString(R.string.due_on) + " " + patinet.getFollowup_date().substring(0, 10));
            }
            //commenting this as per Gobinda's instruction date should be visible instead of "Today" in all the cases.
//            else if(patinet.getFollowUpType() == 0)
//            {
//                holder.linearLayout.setVisibility(View.VISIBLE);
//                holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.green));
//                holder.indicatorTextView.setText("Follow-Up Scheduled for Today");
//            }
            else
            {
                holder.linearLayout.setVisibility(View.VISIBLE);
                holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.green));
                holder.indicatorTextView.setText(context.getResources().getString(R.string.follow_up_scheduled_on) + " " + patinet.getFollowup_date().substring(0, 10));
            }
            if(patinet.getVisit_speciality().equalsIgnoreCase("TLD Query") || patinet.getVisit_speciality().equalsIgnoreCase("Curiosity Resolution") ||
                    patinet.getVisit_speciality().contains("Gynecologist") || patinet.getVisit_speciality().contains("Ayurvedic Physician"))
                holder.speciality_tag.setText(patinet.getVisit_speciality());
            else
                holder.speciality_tag.setText("Agent Resolution");
        }

        holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
        holder.ivPriscription.setTag("1");
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String patientStatus = "returning";
                    Intent intent = new Intent(context, PatientDetailActivity.class);
                    intent.putExtra("patientUuid", patinet.getUuid());
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", "");
                    intent.putExtra("hasPrescription", "true");
                    context.startActivity(intent);
                }
            });
        }

    @Override
    public int getItemCount()
    {
        return patients.size();
    }

        class Myholder extends RecyclerView.ViewHolder {

            private LinearLayout linearLayout;
            private TextView headTextView;
            private TextView bodyTextView;
            private TextView indicatorTextView;
            private View rootView;
            private ImageView ivPriscription;
            private TextView speciality_tag;


            public Myholder(View itemView) {
                super(itemView);

                linearLayout = itemView.findViewById(R.id.dueDateLL);
                headTextView = itemView.findViewById(R.id.list_item_head_text_view);
                bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
                indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
                ivPriscription = itemView.findViewById(R.id.iv_prescription);
                speciality_tag = itemView.findViewById(R.id.speciality_tag);
                rootView = itemView;
            }

            public View getRootView() {
                return rootView;
            }
        }
 }

