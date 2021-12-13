package org.intelehealth.ekalhelpline.activities.ClosedVisitsActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import org.intelehealth.ekalhelpline.models.ActivePatientModel;
import org.intelehealth.ekalhelpline.utilities.DateAndTimeUtils;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ClosedAdapter extends RecyclerView.Adapter<ClosedAdapter.Myholder>{

    public List<ActivePatientModel> activePatientModels;
    Context context;
    SessionManager sessionManager;

    public ClosedAdapter(List<ActivePatientModel> activePatientModels, Context context) {
        this.activePatientModels = activePatientModels;
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_closed_patients, parent, false);
        ClosedAdapter.Myholder viewHolder = new ClosedAdapter.Myholder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final ActivePatientModel activePatientModel = activePatientModels.get(position);
        holder.setIsRecyclable(false);
        if(activePatientModel!=null)
        {
            String age = DateAndTimeUtils.getAgeInYearMonth(activePatientModel.getDate_of_birth(), context);
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age + "\n"
                    + context.getString(R.string.mobile_number_search) + activePatientModel.getPhone_number();

            if (activePatientModel.getOpenmrs_id() != null)
                holder.headTextView.setText(activePatientModel.getFirst_name() + " " + activePatientModel.getLast_name()
                        + ", " + activePatientModel.getOpenmrs_id());
            else
                holder.headTextView.setText(activePatientModel.getFirst_name() + " " + activePatientModel.getLast_name());

            holder.bodyTextView.setText(body);

            if (activePatientModel.getEnddate() == null) {
                holder.getIndicatorTextView().setText(R.string.active);
                holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
            } else {
                holder.getIndicatorTextView().setText(R.string.closed);
                holder.getIndicatorTextView().setBackgroundColor(Color.RED);
            }

            if(activePatientModel.getExitsurvey_comments()!=null) {
                if (activePatientModel.getExitsurvey_comments().equalsIgnoreCase("TLD Resolved") || activePatientModel.getExitsurvey_comments().equalsIgnoreCase("TLD Closed")
                 || activePatientModel.getExitsurvey_comments().equalsIgnoreCase("Doctor Resolution Resolved")
                 || activePatientModel.getExitsurvey_comments().equalsIgnoreCase("Doctor Resolution Closed")) {
                    holder.speciality_tag.setText(activePatientModel.getExitsurvey_comments());
                    //this should be set green for always, as the visit can only be closed when prescription is downloaded.
                    holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
                    holder.ivPriscription.setTag("1");
                }
                else if( activePatientModel.getVisit_speciality()!= null && (activePatientModel.getVisit_speciality().equalsIgnoreCase("Agent Resolution") || activePatientModel.getVisit_speciality().equalsIgnoreCase("Curiosity Resolution")))
                {
                    holder.speciality_tag.setText(activePatientModel.getVisit_speciality());
                    //this should be set green for always, as the visit can only be closed when prescription is downloaded.
                    holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_gray));
                    holder.ivPriscription.setTag("0");
                }
                else {
                    holder.speciality_tag.setVisibility(View.GONE); }
            }



            holder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String patientStatus = "returning";
                    Intent intent = new Intent(context, PatientDetailActivity.class);
                    intent.putExtra("patientUuid", activePatientModel.getPatientuuid());
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", "");
                    intent.putExtra("hasPrescription", "true");
                    context.startActivity(intent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return activePatientModels.size();
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

        public TextView getIndicatorTextView() {
            return indicatorTextView;
        }

        public void setIndicatorTextView(TextView indicatorTextView) {
            this.indicatorTextView = indicatorTextView;
        }

        public View getRootView() {
            return rootView;
        }
    }
}
