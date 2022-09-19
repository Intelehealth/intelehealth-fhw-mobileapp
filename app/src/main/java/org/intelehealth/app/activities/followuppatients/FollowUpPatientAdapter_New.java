package org.intelehealth.app.activities.followuppatients;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.List;

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class FollowUpPatientAdapter_New extends RecyclerView.Adapter<FollowUpPatientAdapter_New.Myholder> {
    List<FollowUpModel> patients;
    Context context;

    public FollowUpPatientAdapter_New(List<FollowUpModel> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }

    @NonNull
    @Override
    public FollowUpPatientAdapter_New.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item, parent, false);
        return new FollowUpPatientAdapter_New.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowUpPatientAdapter_New.Myholder holder, int position) {
        final FollowUpModel model = patients.get(position);
        holder.setIsRecyclable(false);

        if (model != null) {

            // Patient Photo
            if (model.getPatient_photo() != null) {
                Glide.with(context)
                        .load(model.getPatient_photo())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.profile_image);
            }
            else {
                holder.profile_image.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }

            // Patient Name section
            if (model.getOpenmrs_id() != null) {
                holder.fu_patname_txtview.setText(model.getFirst_name() + " " + model.getLast_name() + ", " + model.getOpenmrs_id());
            }
            else {
                holder.fu_patname_txtview.setText(model.getFirst_name() + " " + model.getLast_name());
            }

        // Followup Date section
        if (!model.getFollowup_date().equalsIgnoreCase("null") || !model.getFollowup_date().isEmpty()) {
            String followupDate = model.getFollowup_date();
            followupDate = DateAndTimeUtils.followup_dates_formatter(followupDate, "dd-MM-yyyy", "dd MMMM");
            holder.fu_date_txtview.setText("Follow up on " + followupDate);
        }

        // Emergency/Priority tag code.
            if (model.isEmergency())
                holder.fu_priority_tag.setVisibility(View.VISIBLE);
            else
                holder.fu_priority_tag.setVisibility(View.GONE);
        }

        // Patient Age
        String age = DateAndTimeUtils.getAge_FollowUp(model.getDate_of_birth(), context);

        holder.cardView.setOnClickListener(v -> {
            Intent i = new Intent(context, FollowUp_VisitDetails.class);
            i.putExtra("patientname", model.getFirst_name() + " " + model.getLast_name().substring(0,1) + "."); // Eg. Prajwal W.
            i.putExtra("gender", model.getGender());
            i.putExtra("age", age);
            i.putExtra("openmrsID", model.getOpenmrs_id());
            i.putExtra("chief_complaint", "-"); // TODO: need to fetch this...
            i.putExtra("priority_tag", model.isEmergency());
            i.putExtra("visit_ID", model.getUuid());
            i.putExtra("visit_startDate", model.getVisit_start_date());
            i.putExtra("visit_speciality", model.getVisit_speciality());
            i.putExtra("followup_date", model.getFollowup_date());
            i.putExtra("patient_photo", model.getPatient_photo());
            i.putExtra("chief_complaint", model.getChiefComplaint());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        CardView cardView;
        private View rootView;
        TextView fu_patname_txtview, fu_date_txtview;
        ImageView fu_priority_tag, profile_image;

        public Myholder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.fu_cardview_item);
            fu_patname_txtview = itemView.findViewById(R.id.fu_patname_txtview);
            fu_date_txtview = itemView.findViewById(R.id.fu_date_txtview);
            fu_priority_tag = itemView.findViewById(R.id.fu_priority_tag);
            profile_image = itemView.findViewById(R.id.profile_image);
            rootView = itemView;
        }

        public View getRootView() {
            return rootView;
        }
    }
}
