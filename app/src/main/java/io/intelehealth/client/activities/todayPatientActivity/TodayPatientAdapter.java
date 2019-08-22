package io.intelehealth.client.activities.todayPatientActivity;

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
import io.intelehealth.client.models.TodayPatientModel;
import io.intelehealth.client.utilities.DateAndTimeUtils;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class TodayPatientAdapter extends RecyclerView.Adapter<TodayPatientAdapter.TodayPatientViewHolder> {

    List<TodayPatientModel> todayPatientModelList;
    Context context;
    LayoutInflater layoutInflater;

    public TodayPatientAdapter(List<TodayPatientModel> todayPatientModelList, Context context) {
        this.todayPatientModelList = todayPatientModelList;
        this.context = context;
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
            header = String.format("%s %s - " + context.getString(R.string.visit_summary_heading_id) + ": %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name(), todayPatientModel.getOpenmrs_id());
        } else {
            header = String.format("%s %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name());
        }
        int age = DateAndTimeUtils.getAge(todayPatientModel.getDate_of_birth());
        String dob = DateAndTimeUtils.SimpleDatetoLongDate(todayPatientModel.getDate_of_birth());
        String body = String.format(context.getString(R.string.id_number) + ": %s \n" +
                        context.getString(R.string.identification_screen_prompt_phone_number) + ": %s\n" +
                        context.getString(R.string.identification_screen_prompt_birthday) +
                        ": %s (" + context.getString(R.string.identification_screen_prompt_age) + " %d)", todayPatientModel.getOpenmrs_id(), todayPatientModel.getPhone_number(),
                dob, age);

//        holder.listItemTodayPatientBinding.listItemHeadTextView.setText(header);
//        holder.listItemTodayPatientBinding.listItemBodyTextView.setText(body);
        holder.getHeadTextView().setText(header);
        holder.getBodyTextView().setText(body);
        if (todayPatientModel.getEnddate() == null) {
        /*    Drawable drawable = DrawableHelper
                    .withContext(context)
                    //TODO: Dummy color. Change color before release.
                    .withColor(R.color.green)
                    .withDrawable(R.drawable.circle)
                    .tint()
                    .get();
//            holder.getIndicatorImageView().setImageDrawable(drawable);*/
//            holder.listItemTodayPatientBinding.listItemIndicatorTextView.setText("Active");
        holder.getIndicatorTextView().setText("Active");
//            holder.listItemTodayPatientBinding.listItemIndicatorTextView.setBackgroundColor(Color.GREEN);
        holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
        } else {
        /*    Drawable drawable = DrawableHelper
                    .withContext(context)
                    //TODO: Dummy color. Change color before release.
                    .withColor(R.color.red)
                    .withDrawable(R.drawable.circle)
                    .tint()
                    .get();
            holder.getIndicatorImageView().setImageDrawable(drawable);
            */
//            holder.listItemTodayPatientBinding.listItemIndicatorTextView.setText("Closed");
//            holder.listItemTodayPatientBinding.listItemIndicatorTextView.setBackgroundColor(Color.RED);
            holder.getIndicatorTextView().setText("Closed");
            holder.getIndicatorTextView().setBackgroundColor(Color.RED);
        }
//        holder.listItemTodayPatientBinding.getRoot().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String patientStatus = "returning";
//                Intent intent = new Intent(context, PatientDetailActivity.class);
//                intent.putExtra("patientUuid", todayPatientModel.getPatientuuid());
//                intent.putExtra("status", patientStatus);
//                intent.putExtra("tag", "");
//                context.startActivity(intent);
//            }
//        });
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", todayPatientModel.getPatientuuid());
                intent.putExtra("status", patientStatus);
                intent.putExtra("tag", "");
                context.startActivity(intent);
            }
        });

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
//        ListItemTodayPatientBinding listItemTodayPatientBinding;

        public TodayPatientViewHolder(View itemView) {
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
