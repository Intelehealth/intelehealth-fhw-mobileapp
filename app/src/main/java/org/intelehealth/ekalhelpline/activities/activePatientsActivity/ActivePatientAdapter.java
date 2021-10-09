package org.intelehealth.ekalhelpline.activities.activePatientsActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.database.dao.PatientsDAO;
import org.intelehealth.ekalhelpline.models.ActivePatientModel;
import org.intelehealth.ekalhelpline.models.TodayPatientModel;
import org.intelehealth.ekalhelpline.utilities.DateAndTimeUtils;

import org.intelehealth.ekalhelpline.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import static org.intelehealth.ekalhelpline.utilities.StringUtils.*;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class ActivePatientAdapter extends RecyclerView.Adapter<ActivePatientAdapter.ActivePatientViewHolder> {

    List<ActivePatientModel> activePatientModels, activePatient_speciality;
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<String> listPatientUUID;
    SessionManager sessionManager;

    public ActivePatientAdapter(List<ActivePatientModel> activePatientModels, Context context, ArrayList<String> _listPatientUUID) {
        this.activePatientModels = activePatientModels;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
        sessionManager = new SessionManager(context);
    }

    public ActivePatientAdapter(List<ActivePatientModel> activePatientModels, Context context, ArrayList<String> _listPatientUUID,
                                List<ActivePatientModel> activePatient_speciality) {
        this.activePatientModels = activePatientModels;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
        this.activePatient_speciality = activePatient_speciality;
        sessionManager = new SessionManager(context);
    }

    @Override
    public ActivePatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_active_patient, parent, false);
        ActivePatientViewHolder viewHolder = new ActivePatientViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ActivePatientViewHolder holder, int position) {
        final ActivePatientModel activePatientModel = activePatientModels.get(position);
        String header;
        if (activePatientModel.getOpenmrs_id() != null) {
            if (activePatientModel.getLast_name() != null)
                header = String.format("%s %s, %s", activePatientModel.getFirst_name(),
                        activePatientModel.getLast_name(), activePatientModel.getOpenmrs_id());
            else
                header = String.format("%s, %s", activePatientModel.getFirst_name(), activePatientModel.getOpenmrs_id());
        } else {
            if (activePatientModel.getLast_name() != null)
                header = String.format("%s %s", activePatientModel.getFirst_name(),
                        activePatientModel.getLast_name());
            else
                header = String.format("%s", activePatientModel.getFirst_name());
        }

        if (activePatientModel.getSync().equalsIgnoreCase("0")) {
            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
        } else {
            holder.getTv_not_uploaded().setVisibility(View.GONE);
        }


//        int age = DateAndTimeUtils.getAge(activePatientModel.getDate_of_birth());

        //get date of birth and convert it into years and months
        String age = DateAndTimeUtils.getAgeInYearMonth(activePatientModel.getDate_of_birth(), context);
        String dob = DateAndTimeUtils.SimpleDatetoLongDate(activePatientModel.getDate_of_birth());
        String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;


        holder.getHeadTextView().setText(header);
        holder.getBodyTextView().setText(body);
        if (activePatientModel.getEnddate() == null) {
            holder.getIndicatorTextView().setText(R.string.active);
            holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
        } else {
            holder.getIndicatorTextView().setText(R.string.closed);
            holder.getIndicatorTextView().setBackgroundColor(Color.RED);
        }
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", activePatientModel.getPatientuuid());
                intent.putExtra("status", patientStatus);
                intent.putExtra("tag", "");

                if (holder.ivPriscription.getTag().equals("1")) {
                    intent.putExtra("hasPrescription", "true");
                } else {
                    intent.putExtra("hasPrescription", "false");
                }
                context.startActivity(intent);
            }
        });

        for (int i = 0; i < listPatientUUID.size(); i++) {
            if (activePatientModels.get(position).getPatientuuid().equalsIgnoreCase(listPatientUUID.get(i))) {
                holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
                holder.ivPriscription.setTag("1");
            }
        }

        //TLD Query - start
        //to check only if visit is uploaded then show the tag...
        if(activePatient_speciality != null) {
        for (int i = 0; i < activePatient_speciality.size(); i++) {
            if (activePatientModel.getPatientuuid().equalsIgnoreCase(activePatient_speciality.get(i).getPatientuuid())) {

                if (activePatientModel.getSync() != null && (activePatientModel.getSync().equalsIgnoreCase("1") ||
                        activePatientModel.getSync().toLowerCase().equalsIgnoreCase("true"))) { //if visit is uploaded.

                    if (activePatient_speciality.get(i).getVisit_speciality() != null &&
                            activePatient_speciality.get(i).getVisit_speciality().equalsIgnoreCase("TLD Query")) { //TLD Query as speciality.

                        if (activePatientModel.getEnddate() == null) { // visit is NOT Ended/Active

                            //Reason for Call - Start
                            if(sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                                holder.tld_reason_for_call.setText(switch_hi_Reason_for_Call_TAG(
                                        PatientsDAO.getReason_for_Call(activePatient_speciality.get(i).getPatientuuid())));
                            }
                            else {
                                holder.tld_reason_for_call.setText(PatientsDAO.getReason_for_Call(activePatient_speciality.get(i).getPatientuuid()));
                            }
                            holder.tld_reason_for_call.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                            holder.tld_reason_for_call.setBackgroundColor(context.getResources().getColor(R.color.tld_tag_bgcolor));
                            //Reason for Call - End


                            if (holder.ivPriscription.getTag() != null && holder.ivPriscription.getTag().equals("1")) { //Prescription is Given
                                holder.tld_query_tag.setText(R.string.tld_query_answered_tag); //Prescription is GIVEN
                                holder.tld_query_tag.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                holder.tld_query_tag.setBackgroundColor(context.getResources().getColor(R.color.tld_tag_bgcolor));
                            } else {
                                holder.tld_query_tag.setText(R.string.tld_query_asked_tag); //Prescription is NOT GIVEN
                                holder.tld_query_tag.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                holder.tld_query_tag.setBackgroundColor(context.getResources().getColor(R.color.tld_tag_bgcolor));
                            }
                        }
                    } else {
                        // Specilaity is not TLD Query and Agent Resolution
                        if (activePatient_speciality.get(i).getVisit_speciality() != null &&
                                !activePatient_speciality.get(i).getVisit_speciality().equalsIgnoreCase("Agent Resolution")) {

                            if (holder.ivPriscription.getTag() != null && holder.ivPriscription.getTag().equals("1")) { //Prescription is Given
                                // do nothing //Prescription is GIVEN
                            } else {
                                holder.tld_query_tag.setText(R.string.doctor_visit_scheduled); //Prescription is NOT GIVEN
                                holder.tld_query_tag.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                holder.tld_query_tag.setBackgroundColor(context.getResources().getColor(R.color.tld_tag_bgcolor));
                            }
                        }
                        else {
                            //do nothing...
                            holder.tld_query_tag.setText("");
                        }
                    }
                } else {
                    //  holder.tld_query_tag.setVisibility(View.GONE); // If visit is not uploaded then.
                }

            } else {
                //do nothing...
            }
        }
    }
        //TLD Query - end
        if (holder.tld_query_tag.getText().toString().equalsIgnoreCase("")) {
            holder.tld_query_tag.setVisibility(View.GONE);
        }

        if (holder.tld_reason_for_call.getText().toString().equalsIgnoreCase("")) {
            holder.tld_reason_for_call.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return activePatientModels.size();
    }

    public class ActivePatientViewHolder extends RecyclerView.ViewHolder {
        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;
        private TextView tld_query_tag, tld_reason_for_call;

        public ActivePatientViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.iv_prescription);
            tv_not_uploaded = (TextView) itemView.findViewById(R.id.tv_not_uploaded);
            tld_query_tag = (TextView) itemView.findViewById(R.id.tld_query_tag);
            tld_reason_for_call = itemView.findViewById(R.id.reason_for_call_tag);
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

        public TextView getIndicatorTextView() {
            return indicatorTextView;
        }

        public void setIndicatorTextView(TextView indicatorTextView) {
            this.indicatorTextView = indicatorTextView;
        }

        public View getRootView() {
            return rootView;
        }

        public TextView getTv_not_uploaded() {
            return tv_not_uploaded;
        }

        public void setTv_not_uploaded(TextView tv_not_uploaded) {
            this.tv_not_uploaded = tv_not_uploaded;
        }
    }

}
