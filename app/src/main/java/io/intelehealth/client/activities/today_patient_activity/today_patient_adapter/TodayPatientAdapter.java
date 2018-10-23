package io.intelehealth.client.activities.today_patient_activity.today_patient_adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.patient_detail_activity.PatientDetailActivity;
import io.intelehealth.client.objects.TodayPatientModel;
import io.intelehealth.client.utilities.HelperMethods;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class TodayPatientAdapter extends RecyclerView.Adapter<TodayPatientViewHolder> {

    List<TodayPatientModel> todayPatientModelList;
    Context context;


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
        if(todayPatientModel.getOpenmrs_id()!= null) {
            header = String.format("%s %s - " + context.getString(R.string.visit_summary_heading_id) + ": %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name(), todayPatientModel.getOpenmrs_id());
        }else{
            header = String.format("%s %s" , todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name());
        }
        int age = HelperMethods.getAge(todayPatientModel.getDate_of_birth());
        //for converting Date format in dd-MMMM-yyyy
        String dob=HelperMethods.SimpleDatetoLongDate(todayPatientModel.getDate_of_birth());
        String body = String.format(context.getString(R.string.id_number) + ": %s \n " +
                        context.getString(R.string.identification_screen_prompt_phone_number) + ": %s\n" +
                        context.getString(R.string.identification_screen_prompt_birthday)+
                        ": %s ("+context.getString(R.string.identification_screen_prompt_age)+" %d)", todayPatientModel.getOpenmrs_id(), todayPatientModel.getPhone_number(),
                dob, age);
        holder.getHeadTextView().setText(header);
        holder.getBodyTextView().setText(body);
        if (todayPatientModel.getEnd_datetime() == null) {
        /*    Drawable drawable = DrawableHelper
                    .withContext(context)
                    //TODO: Dummy color. Change color before release.
                    .withColor(R.color.green)
                    .withDrawable(R.drawable.circle)
                    .tint()
                    .get();
            holder.getIndicatorImageView().setImageDrawable(drawable);*/

        holder.getIndicatorTextView().setText("Active");
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
            holder.getIndicatorTextView().setText("Closed");
            holder.getIndicatorTextView().setBackgroundColor(Color.RED);
        }

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientID", todayPatientModel.getPatient_id());
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
}
