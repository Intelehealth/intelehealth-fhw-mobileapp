package io.intelehealth.client.activities.searchPatientActivity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.patientDetailActivity.PatientDetailActivity;
import io.intelehealth.client.models.dto.PatientDTO;
import io.intelehealth.client.utilities.DateAndTimeUtils;

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
            int age = DateAndTimeUtils.getAge(patinet.getDateofbirth());
            String dob = DateAndTimeUtils.SimpleDatetoLongDate(patinet.getDateofbirth());
            String body = String.format(context.getString(R.string.id_number) + ": %s \n" +
                            context.getString(R.string.identification_screen_prompt_phone_number) + ": %s\n" +
                            context.getString(R.string.identification_screen_prompt_birthday) +
                            ": %s (" + context.getString(R.string.identification_screen_prompt_age) + " %d)", patinet.getOpenmrsId(), patinet.getPhonenumber(),
                    dob, age);
            holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname());
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

        public Myholder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
            linearLayout = itemView.findViewById(R.id.searchlinear);
        }
    }

}
