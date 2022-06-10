package org.intelehealth.app.activities.searchPatientActivity;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;

public class SearchPatientAdapter extends RecyclerView.Adapter<SearchPatientAdapter.Myholder> {
    List<PatientDTO> patients;
    Context context;
    LayoutInflater layoutInflater;
    VisitsDAO visitsDAO = new VisitsDAO();
    String visitUUID = "";
    String patientfullName = "";

    public SearchPatientAdapter(List<PatientDTO> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_search, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPatientAdapter.Myholder holder, int position) {
        final PatientDTO patinet = patients.get(position);
        if (patinet != null) {
            //int age = DateAndTimeUtils.getAge(patinet.getDateofbirth(),context);
            visitUUID = visitsDAO.fetchVisitUUIDFromPatientUUID(patinet.getUuid());

            String age = DateAndTimeUtils.getAgeInYearMonth(patinet.getDateofbirth(), context);
            //String dob = DateAndTimeUtils.SimpleDatetoLongDate(patinet.getDateofbirth());
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;

            if (patinet.getOpenmrsId() != null)
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname()
                        + "\n" + patinet.getOpenmrsId());
            else
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname());

            holder.bodyTextView.setText(body);

            // For Timeline Title...
            if (patinet.getMiddlename() != null && !patinet.getMiddlename().equalsIgnoreCase("")
                    && !patinet.getMiddlename().isEmpty()) {
                patientfullName = patinet.getFirstname() + " " + patinet.getMiddlename() + " " + patinet.getLastname();
            } else {
                patientfullName = patinet.getFirstname() + " " + patinet.getLastname();
            }
            // end...
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(visitUUID != null && visitUUID.equalsIgnoreCase("")) { // visit is not yet created for this user.
                    Log.d("search adapter", "patientuuid" + patinet.getUuid());
                    String patientStatus = "returning";
                    Intent intent = new Intent(context, PatientDetailActivity.class);
                    intent.putExtra("patientUuid", patinet.getUuid());
                    intent.putExtra("patientName", patinet.getFirstname() + "" + patinet.getLastname());
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", "search");
                    intent.putExtra("hasPrescription", "false");
                    context.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(context, TimelineVisitSummaryActivity.class);
                    intent.putExtra("patientUuid", patinet.getUuid());
                    intent.putExtra("visitUuid", visitUUID);
                    intent.putExtra("name", patinet.getFirstname() + " " + patinet.getLastname());
                    intent.putExtra("patientNameTimeline", patientfullName);
                    intent.putExtra("tag", "exisiting");
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        private TextView headTextView;
        private TextView bodyTextView;

        public Myholder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
            linearLayout = itemView.findViewById(R.id.searchlinear);
        }
    }

}
