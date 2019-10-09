package io.intelehealth.client.activities.activePatientsActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.patientDetailActivity.PatientDetailActivity;
import io.intelehealth.client.models.ActivePatientModel;
import io.intelehealth.client.utilities.DateAndTimeUtils;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class ActivePatientAdapter extends RecyclerView.Adapter<ActivePatientAdapter.ActivePatientViewHolder> {

    List<ActivePatientModel> activePatientModels;
    Context context;
    LayoutInflater layoutInflater;

    public ActivePatientAdapter(List<ActivePatientModel> activePatientModels, Context context) {
        this.activePatientModels = activePatientModels;
        this.context = context;
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
            header = String.format("%s %s - " + context.getString(R.string.visit_summary_heading_id) + ": %s", activePatientModel.getFirst_name(),
                    activePatientModel.getLast_name(), activePatientModel.getOpenmrs_id());
        } else {
            header = String.format("%s %s", activePatientModel.getFirst_name(),
                    activePatientModel.getLast_name());
        }
        int age = DateAndTimeUtils.getAge(activePatientModel.getDate_of_birth());
        String dob = DateAndTimeUtils.SimpleDatetoLongDate(activePatientModel.getDate_of_birth());
        String body = String.format(context.getString(R.string.id_number) + ": %s \n " +
                        context.getString(R.string.identification_screen_prompt_phone_number) + ": %s\n" +
                        context.getString(R.string.identification_screen_prompt_birthday) +
                        ": %s (" + context.getString(R.string.identification_screen_prompt_age) + " %d)", activePatientModel.getOpenmrs_id(), activePatientModel.getPhone_number(),
                dob, age);

        holder.getHeadTextView().setText(header);
        holder.getBodyTextView().setText(body);
        if (activePatientModel.getEnddate() == null) {
            holder.getIndicatorTextView().setText("Active");
            holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
        } else {
            holder.getIndicatorTextView().setText("Closed");
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
                context.startActivity(intent);
            }
        });
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

        public ActivePatientViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
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
    }

}
