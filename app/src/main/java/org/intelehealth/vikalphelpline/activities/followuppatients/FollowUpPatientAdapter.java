package org.intelehealth.vikalphelpline.activities.followuppatients;
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

import org.intelehealth.vikalphelpline.R;
import org.intelehealth.vikalphelpline.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.vikalphelpline.models.dto.PatientDTO;
import org.intelehealth.vikalphelpline.utilities.DateAndTimeUtils;

import java.util.List;

/**
 * Created by Nishita Goyal on 03/09/21.
 * Github : @nishitagoyal
 */
public class FollowUpPatientAdapter extends RecyclerView.Adapter<FollowUpPatientAdapter.Myholder> {
    List<PatientDTO> patients;
    Context context;

    public FollowUpPatientAdapter(List<PatientDTO> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_today_patient, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final PatientDTO patinet = patients.get(position);
        if (patinet != null) {
            String age = DateAndTimeUtils.getAgeInYearMonth(patinet.getDateofbirth(), context);
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;
            if (patinet.getOpenmrsId() != null)
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname()
                        + ", " + patinet.getOpenmrsId());
            else
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname());
            holder.bodyTextView.setText(body);
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

        public Myholder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.iv_prescription);
            indicatorTextView.setVisibility(View.GONE);
            rootView = itemView;
        }

        public TextView getHeadTextView() {
            return headTextView;
        }

        public void setHeadTextView(TextView headTextView) {
            this.headTextView = headTextView;
        }

        public TextView getBodyTextView() {
            return bodyTextView;
        }

        public void setBodyTextView(TextView bodyTextView) {
            this.bodyTextView = bodyTextView;
        }

        public View getRootView() {
            return rootView;
        }

    }

}