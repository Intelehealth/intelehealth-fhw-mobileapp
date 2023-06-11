package org.intelehealth.ezazi.activities.searchPatientActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;

import org.intelehealth.ezazi.activities.patientDetailActivity.PatientDetailActivity;

public class SearchPatientAdapter extends RecyclerView.Adapter<SearchPatientAdapter.Myholder> {
    List<PatientDTO> patients;
    private Context context;

    public SearchPatientAdapter(List<PatientDTO> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_patient_history, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPatientAdapter.Myholder holder, int position) {
        final PatientDTO patinet = patients.get(position);
        if (patinet != null) {
            holder.bind(patinet);
        }
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class Myholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ConstraintLayout linearLayout;
        private TextView tvPatientName;
        private TextView tvPatientId;
        private TextView tvPatientAge;
        private TextView tvPatientBedNo;
        private TextView tvPatientNoOfAlert;
        private TextView tvPatientStage;

        public Myholder(View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientId = itemView.findViewById(R.id.tvPatientId);
            tvPatientAge = itemView.findViewById(R.id.tvPatientAge);
            tvPatientBedNo = itemView.findViewById(R.id.tvBedNumber);
            tvPatientNoOfAlert = itemView.findViewById(R.id.tvNoOfAlert);
            tvPatientStage = itemView.findViewById(R.id.tvStageName);
            linearLayout = itemView.findViewById(R.id.searchlinear);
        }

        @SuppressLint("SetTextI18n")
        public void bind(PatientDTO patient) {
            linearLayout.setTag(patient);
            linearLayout.setOnClickListener(this);

            String age = DateAndTimeUtils.getAgeInYears(patient.getDateofbirth(), context);
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;
            String patientName = patient.getFirstname() + " " + patient.getLastname();

            tvPatientAge.setText(body);
            tvPatientName.setText(patientName);
            tvPatientId.setVisibility(View.VISIBLE);
            tvPatientBedNo.setText("Bed No: " + patient.getBedNo());
            tvPatientStage.setText(patient.getStage());
            if (patient.getOpenmrsId() != null)
                tvPatientId.setText(patient.getOpenmrsId());
            else
                tvPatientId.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            PatientDTO patient = (PatientDTO) view.getTag();
            onPatientClicked(patient);
        }

        private void onPatientClicked(PatientDTO patient) {
            String visitId = getVisitUuid(patient);
            String patientName = getPatientName(patient);
            startNextActivity(patient, visitId, patientName);
        }

        private String getVisitUuid(PatientDTO patient) {
            VisitsDAO visitsDAO = new VisitsDAO();
            return visitsDAO.fetchVisitUUIDFromPatientUUID(patient.getUuid());
        }

        private String getPatientName(PatientDTO patient) {
            if (patient.getMiddlename() != null && !patient.getMiddlename().equalsIgnoreCase("")
                    && !patient.getMiddlename().isEmpty()) {
                return patient.getFirstname() + " " + patient.getMiddlename() + " " + patient.getLastname();
            } else {
                return patient.getFirstname() + " " + patient.getLastname();
            }
        }

        private void startNextActivity(PatientDTO patient, String visitUUID, String patientName) {
            if (visitUUID != null && visitUUID.equalsIgnoreCase("")) { // visit is not yet created for this user.
                Log.d("search adapter", "patientuuid" + patient.getUuid());
                String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", patient.getUuid());
                intent.putExtra("patientName", patient.getFirstname() + "" + patient.getLastname());
                intent.putExtra("status", patientStatus);
                intent.putExtra("tag", "search");
                intent.putExtra("hasPrescription", "false");
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, TimelineVisitSummaryActivity.class);
                intent.putExtra("patientUuid", patient.getUuid());
                intent.putExtra("visitUuid", visitUUID);
                intent.putExtra("name", patient.getFirstname() + " " + patient.getLastname());
                intent.putExtra("patientNameTimeline", patientName);
                intent.putExtra("tag", "exisiting");
                context.startActivity(intent);
            }
        }
    }

}
