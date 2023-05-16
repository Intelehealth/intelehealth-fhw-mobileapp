package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.PatientChoiceDialogItemBinding;
import org.intelehealth.ezazi.models.FamilyMemberRes;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 11:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PatientMultiChoiceAdapter extends MultiChoiceAdapter<FamilyMemberRes, PatientMultiChoiceViewHolder> {

    public PatientMultiChoiceAdapter(Context context, ArrayList<FamilyMemberRes> objectsList) {
        super(context, objectsList);
    }

    @NonNull
    @Override
    public PatientMultiChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PatientChoiceDialogItemBinding binding = PatientChoiceDialogItemBinding.inflate(inflater, parent, false);
        return new PatientMultiChoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientMultiChoiceViewHolder holder, int position) {
        holder.bind(getItem(position));
        holder.setCheckedItem(isItemSelected(position));
    }
}

class PatientMultiChoiceViewHolder extends RecyclerView.ViewHolder {
    private PatientChoiceDialogItemBinding binding;

    public PatientMultiChoiceViewHolder(@NonNull PatientChoiceDialogItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(FamilyMemberRes patient) {
        binding.setPatient(patient);
        binding.clChoiceRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        binding.cbPatientSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    public void setCheckedItem(boolean isChecked) {
        binding.cbPatientSelection.setChecked(isChecked);
    }
}