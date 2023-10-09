package org.intelehealth.app.activities.medicationAidActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.prescription.AdvicePrescAdapter;

import java.util.List;

public class MedicationAidAdapter extends RecyclerView.Adapter<MedicationAidAdapter.MyViewHolder> {
    private Context context;
    private List<String> list;

    public MedicationAidAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication_aid, 
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String model = list.get(position);
        holder.tvMedAidName.setText(model);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMedAidName;
        private MaterialCheckBox cb_dispense, cb_administer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMedAidName = itemView.findViewById(R.id.tvMedAidName);
            cb_dispense = itemView.findViewById(R.id.cb_dispense);
            cb_administer = itemView.findViewById(R.id.cb_administer);
        }
    }
}
