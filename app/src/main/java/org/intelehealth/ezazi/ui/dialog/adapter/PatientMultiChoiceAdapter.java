package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.PatientChoiceDialogItemBinding;
import org.intelehealth.ezazi.models.FamilyMemberRes;
import org.intelehealth.ezazi.ui.dialog.model.MultiChoiceItem;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 11:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702Im
 **/
public class PatientMultiChoiceAdapter extends SelectAllHeaderAdapter {

    private static final int ITEM = 1001;

    public PatientMultiChoiceAdapter(Context context, ArrayList<MultiChoiceItem> objectsList) {
        super(context, objectsList);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isHeader()) return ITEM_HEADER;
        else return ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM) {
            PatientChoiceDialogItemBinding binding = PatientChoiceDialogItemBinding.inflate(inflater, parent, false);
            return new PatientMultiChoiceViewHolder(binding);
        } else return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PatientMultiChoiceViewHolder && !getItem(position).isHeader()) {
            PatientMultiChoiceViewHolder patientHolder = (PatientMultiChoiceViewHolder) holder;
            patientHolder.bind((FamilyMemberRes) getItem(position));
            patientHolder.setCheckedItem(isItemSelected(position));
            patientHolder.setClickListener(this);
        } else super.onBindViewHolder(holder, position);
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
    }

    public void setClickListener(View.OnClickListener listener) {
        binding.clChoiceRoot.setTag(binding.cbPatientSelection);
        binding.clChoiceRoot.setTag(R.id.clChoiceRoot, getAdapterPosition());
        binding.clChoiceRoot.setOnClickListener(listener);

        binding.cbPatientSelection.setTag(binding.cbPatientSelection);
        binding.cbPatientSelection.setTag(R.id.cbPatientSelection, getAdapterPosition());
        binding.cbPatientSelection.setOnClickListener(listener);
    }

    public void setCheckedItem(boolean isChecked) {
        binding.cbPatientSelection.setChecked(isChecked);
        binding.cbPatientSelection.setActivated(true);
        binding.cbPatientSelection.setSelected(true);
    }
}