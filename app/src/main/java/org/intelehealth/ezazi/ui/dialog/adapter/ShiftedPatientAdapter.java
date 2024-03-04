package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.RowItemShiftedPatientBinding;
import org.intelehealth.ezazi.models.FamilyMemberRes;
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 11:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702Im
 **/
public class ShiftedPatientAdapter extends BaseRecyclerViewAdapter<FamilyMemberRes> {

    public ShiftedPatientAdapter(Context context, ArrayList<FamilyMemberRes> objectsList) {
        super(context, objectsList);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowItemShiftedPatientBinding binding = RowItemShiftedPatientBinding.inflate(getInflater(), parent, false);
        return new ShiftedPatientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShiftedPatientViewHolder) {
            ShiftedPatientViewHolder patientHolder = (ShiftedPatientViewHolder) holder;
            patientHolder.bind(getItem(position));
            patientHolder.hideDivider(position == getItemCount() - 1);
        }
    }
}

class ShiftedPatientViewHolder extends RecyclerView.ViewHolder {
    private final RowItemShiftedPatientBinding binding;

    public ShiftedPatientViewHolder(@NonNull RowItemShiftedPatientBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(FamilyMemberRes patient) {
        binding.setPatient(patient);
    }

    public void hideDivider(boolean hide) {
        binding.setHideDivider(hide);
    }
}