package org.intelehealth.ekalhelpline.activities.todayPatientActivity;

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
import org.intelehealth.ekalhelpline.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalhelpline.models.TodayPatientModel;
import org.intelehealth.ekalhelpline.utilities.DateAndTimeUtils;


/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class TodayPatientAdapter extends RecyclerView.Adapter<TodayPatientAdapter.TodayPatientViewHolder> {

    List<TodayPatientModel> todayPatientModelList, todayPatient_exitsurvey_commentsList;
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<String> listPatientUUID;

    public TodayPatientAdapter(List<TodayPatientModel> todayPatientModelList, Context context, ArrayList<String> _listPatientUUID) {
        this.todayPatientModelList = todayPatientModelList;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
    }

    public TodayPatientAdapter(List<TodayPatientModel> todayPatientModelList, Context context,
                               ArrayList<String> _listPatientUUID, List<TodayPatientModel> todayPatient_exitsurvey_commentsList) {
        this.todayPatientModelList = todayPatientModelList;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
        this.todayPatient_exitsurvey_commentsList = todayPatient_exitsurvey_commentsList;
    }

    @Override
    public TodayPatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_today_patient, parent, false);
        TodayPatientViewHolder viewHolder = new TodayPatientViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TodayPatientViewHolder holder, int position) {
        final TodayPatientModel todayPatientModel = todayPatientModelList.get(position);
        String header;
        if (todayPatientModel.getOpenmrs_id() != null) {
            if (todayPatientModel.getLast_name() != null)
                header = String.format("%s %s, %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name(), todayPatientModel.getOpenmrs_id());
            else
                header = String.format("%s, %s", todayPatientModel.getFirst_name(), todayPatientModel.getOpenmrs_id());
        } else {
            if (todayPatientModel.getLast_name() != null)
                header = String.format("%s %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name());
            else
                header = String.format("%s", todayPatientModel.getFirst_name());
        }
//        int age = DateAndTimeUtils.getAge(todayPatientModel.getDate_of_birth());
        String age = DateAndTimeUtils.getAgeInYearMonth(todayPatientModel.getDate_of_birth(), context);
        String dob = DateAndTimeUtils.SimpleDatetoLongDate(todayPatientModel.getDate_of_birth());
        String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;

        if (todayPatientModel.getSync().equalsIgnoreCase("0")){
            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
        } else {
            holder.getTv_not_uploaded().setVisibility(View.GONE);
        }

        holder.getHeadTextView().setText(header);
        holder.getBodyTextView().setText(body);

        if (todayPatientModel.getEnddate() == null) {
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
                intent.putExtra("patientUuid", todayPatientModel.getPatientuuid());
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
            if (todayPatientModelList.get(position).getPatientuuid().equalsIgnoreCase(listPatientUUID.get(i))) {
                holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
                holder.ivPriscription.setTag("1");
            }
        }

        //TLD Query - start
        //to check only if visit is uploaded then show the tag...
        if(todayPatientModel.getSync() != null && (todayPatientModel.getSync().equalsIgnoreCase("1") ||
                todayPatientModel.getSync().toLowerCase().equalsIgnoreCase("true"))) { //if visit is uploaded.

            if (todayPatientModel.getVisit_speciality() != null &&
                    todayPatientModel.getVisit_speciality().equalsIgnoreCase("TLD Query")) { //TLD Query as speciality.

                if (todayPatientModel.getEnddate() == null) { // visit is NOT Ended/Active

                     if (holder.ivPriscription.getTag() != null && holder.ivPriscription.getTag().equals("1")) { //Prescription is Given
                    holder.tld_query_tag.setText("TLD QUERY ANSWERED"); //Prescription is GIVEN
                          }
                     else {
                    holder.tld_query_tag.setText("TLD QUERY ASKED"); //Prescription is NOT GIVEN
                }
            }
             else {
                    //check the spinner value for this from the exit survey selection and then
                    // based on that checking add the text.
                    for (int i = 0; i < todayPatient_exitsurvey_commentsList.size(); i++) {
                        if (todayPatientModel.getPatientuuid().equalsIgnoreCase(todayPatient_exitsurvey_commentsList.get(i).getPatientuuid())) {
                            //check for TLD Closed and TLD Resolved
                            if(todayPatient_exitsurvey_commentsList.get(i).getExitsurvey_comments()
                                    .equalsIgnoreCase("TLD Closed")) {
                                holder.tld_query_tag.setText("TLD CLOSED");
                            }
                            else if(todayPatient_exitsurvey_commentsList.get(i).getExitsurvey_comments()
                                    .equalsIgnoreCase("TLD Resolved")) {
                                holder.tld_query_tag.setText("TLD RESOLVED");
                            }
                        }
                    }
                }
            }
            else {
                holder.tld_query_tag.setVisibility(View.GONE); // If visit speciality is not TLD Query then.
            }
        }
        else {
            holder.tld_query_tag.setVisibility(View.GONE); // If visit is not uploaded then.
        }
        //TLD Query - end


    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return todayPatientModelList.size();
    }

    class TodayPatientViewHolder extends RecyclerView.ViewHolder {

        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;
        private TextView tld_query_tag;

        public TodayPatientViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.iv_prescription);
            tv_not_uploaded = (TextView) itemView.findViewById(R.id.tv_not_uploaded);
            tld_query_tag = (TextView) itemView.findViewById(R.id.tld_query_tag);
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
