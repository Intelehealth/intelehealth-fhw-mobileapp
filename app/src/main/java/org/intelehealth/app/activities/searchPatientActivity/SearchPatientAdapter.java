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
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;

public class SearchPatientAdapter extends RecyclerView.Adapter<SearchPatientAdapter.Myholder> {
    List<PatientDTO> patients;
    Context context;
    LayoutInflater layoutInflater;

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
            if (patinet.getOpenmrsId() != null)
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname() + ", " + patinet.getOpenmrsId());
            else
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname());

            String age = "";
            if (patinet.getDateofbirth() != null && !patinet.getDateofbirth().isEmpty())
                age = DateAndTimeUtils.getAgeInYearMonth(patinet.getDateofbirth(), context);

            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;
            holder.bodyTextView.setText(body);
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        //  private TextView indicator;


        public Myholder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
            // indicator = itemView.findViewById(R.id.indicator);

            linearLayout = itemView.findViewById(R.id.searchlinear);
        }
    }
}
