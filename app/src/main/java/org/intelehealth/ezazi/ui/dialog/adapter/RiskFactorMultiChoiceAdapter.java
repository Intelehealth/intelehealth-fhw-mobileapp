package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.PatientChoiceDialogItemBinding;
import org.intelehealth.ezazi.databinding.SelectAllDialogItemHeaderBinding;
import org.intelehealth.ezazi.models.FamilyMemberRes;
import org.intelehealth.ezazi.ui.dialog.model.MultiChoiceItem;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 11:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702Im
 **/
public class RiskFactorMultiChoiceAdapter extends MultiChoiceAdapter<String, RecyclerView.ViewHolder> {

    public RiskFactorMultiChoiceAdapter(Context context, ArrayList<String> objectsList) {
        super(context, objectsList);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SelectAllDialogItemHeaderBinding binding = SelectAllDialogItemHeaderBinding.inflate(inflater, parent, false);
        return new RiskFactorMultiChoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RiskFactorMultiChoiceViewHolder patientHolder = (RiskFactorMultiChoiceViewHolder) holder;
        patientHolder.bind(getItem(position));
        patientHolder.setCheckedItem(isItemSelected(position));
        patientHolder.setClickListener(this);
    }
}

class RiskFactorMultiChoiceViewHolder extends RecyclerView.ViewHolder {
    private SelectAllDialogItemHeaderBinding binding;

    public RiskFactorMultiChoiceViewHolder(@NonNull SelectAllDialogItemHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.binding.cbSelectAll.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

    public void bind(String factor) {
        binding.setHeader(factor);
    }

    public void setClickListener(View.OnClickListener listener) {
        binding.cbSelectAll.setTag(binding.cbSelectAll);
        binding.cbSelectAll.setTag(R.id.cbSelectAll, getAdapterPosition());
        binding.cbSelectAll.setOnClickListener(listener);
    }

    public void setCheckedItem(boolean isChecked) {
        binding.cbSelectAll.setChecked(isChecked);
    }
}