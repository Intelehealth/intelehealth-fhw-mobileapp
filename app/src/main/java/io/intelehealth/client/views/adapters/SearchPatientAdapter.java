package io.intelehealth.client.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.databinding.SearchitemBinding;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.views.activites.PatientDetailActivity;

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
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        SearchitemBinding searchitemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.searchitem, parent, false);
        return new Myholder(searchitemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPatientAdapter.Myholder holder, int position) {
        final PatientDTO patinet = patients.get(position);
        holder.searchitemBinding.listItemBody.setText(patinet.getFirstname() + " " + patinet.getLastname());
        holder.searchitemBinding.listItemHead.setText(patinet.getOpenmrsId());
        holder.searchitemBinding.searchlinear.setOnClickListener(new View.OnClickListener() {
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
        SearchitemBinding searchitemBinding;

        public Myholder(SearchitemBinding searchitemBinding1) {
            super(searchitemBinding1.getRoot());
            this.searchitemBinding = searchitemBinding1;
        }
    }

}
