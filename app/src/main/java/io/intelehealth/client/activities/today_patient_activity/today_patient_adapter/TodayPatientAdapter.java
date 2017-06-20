package io.intelehealth.client.activities.today_patient_activity.today_patient_adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.R;
import io.intelehealth.client.activities.patient_detail_activity.PatientDetailActivity;
import io.intelehealth.client.objects.TodayPatientModel;

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
        String header = String.format("%s %s - ID: %s", todayPatientModel.getFirst_name(),
                todayPatientModel.getLast_name(), todayPatientModel.getPatient_id());
        int age = HelperMethods.getAge(todayPatientModel.getDate_of_birth());
        String body = String.format("ID Number: %s \n " +
                        "Phone Number: %s\n" +
                        "Date of Birth: %s (Age %d)", todayPatientModel.getPatient_id(), todayPatientModel.getPhone_number(),
                todayPatientModel.getDate_of_birth(), age);
        holder.getHeadTextView().setText(header);
        holder.getBodyTextView().setText(body);
        if (todayPatientModel.getEnd_datetime() == null) {
            Drawable drawable = DrawableHelper
                    .withContext(context)
                    //TODO: Dummy color. Change color before release.
                    .withColor(R.color.colorAccent)
                    .withDrawable(R.drawable.circle)
                    .tint()
                    .get();
            holder.getIndicatorImageView().setImageDrawable(drawable);
        } else {
            Drawable drawable = DrawableHelper
                    .withContext(context)
                    //TODO: Dummy color. Change color before release.
                    .withColor(R.color.colorPrimary)
                    .withDrawable(R.drawable.circle)
                    .tint()
                    .get();
            holder.getIndicatorImageView().setImageDrawable(drawable);
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
