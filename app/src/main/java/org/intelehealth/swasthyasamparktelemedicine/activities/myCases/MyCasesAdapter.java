package org.intelehealth.swasthyasamparktelemedicine.activities.myCases;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.swasthyasamparktelemedicine.R;
import org.intelehealth.swasthyasamparktelemedicine.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.swasthyasamparktelemedicine.activities.searchPatientActivity.SearchPatientAdapter;
import org.intelehealth.swasthyasamparktelemedicine.activities.todayPatientActivity.TodayPatientAdapter;
import org.intelehealth.swasthyasamparktelemedicine.models.MyCasesModel;
import org.intelehealth.swasthyasamparktelemedicine.models.dto.PatientDTO;
import org.intelehealth.swasthyasamparktelemedicine.utilities.DateAndTimeUtils;

import java.util.List;

public class MyCasesAdapter extends RecyclerView.Adapter<MyCasesAdapter.MyCasesViewHolder>{

    List<MyCasesModel> patients;
    Context context;

    public MyCasesAdapter(List<MyCasesModel> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }

    @NonNull
    @Override
    public MyCasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_search, parent, false);
        return new MyCasesAdapter.MyCasesViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCasesViewHolder holder, int position) {

        final MyCasesModel patient = patients.get(position);
        if (patient != null) {
            String phone_no = patient.getPhone_number();
            String age = DateAndTimeUtils.getAgeInYearMonth(patient.getDate_of_birth(), context);
            String body =  context.getString(R.string.row_phone_number) + ": " + phone_no + "\n" +
                    context.getString(R.string.identification_screen_prompt_age) + " " + age;

            if (patient.getOpenmrs_id() != null)
                holder.headTextView.setText(patient.getFirst_name() + " " + patient.getLast_name()
                        + ", " + patient.getOpenmrs_id());
            else
                holder.headTextView.setText(patient.getFirst_name() + " " + patient.getLast_name());

            holder.bodyTextView.setText(body);
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", patient.getUuid());
                intent.putExtra("patientName", patient.getFirst_name() + "" + patient.getLast_name());
                intent.putExtra("status", patientStatus);
                intent.putExtra("tag", "search");
                intent.putExtra("hasPrescription", "false");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }


    class MyCasesViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        private TextView headTextView;
        private TextView bodyTextView;

        public MyCasesViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
            linearLayout = itemView.findViewById(R.id.searchlinear);
        }
    }
}
