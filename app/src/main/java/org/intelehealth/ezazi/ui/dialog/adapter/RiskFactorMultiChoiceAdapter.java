package org.intelehealth.ezazi.ui.dialog.adapter;

import static org.intelehealth.ezazi.ui.dialog.adapter.RiskFactorMultiChoiceAdapter.OPTION_NONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.SelectAllDialogItemHeaderBinding;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 11:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702Im
 **/
public class RiskFactorMultiChoiceAdapter extends MultiChoiceAdapter<String, RecyclerView.ViewHolder> {

    private boolean enableAll = true;
    public static final String OPTION_NONE = "None";

    public RiskFactorMultiChoiceAdapter(Context context, ArrayList<String> objectsList) {
        super(context, objectsList);
    }

    @Override
    protected String searchableValue(int position) {
        return getItem(position);
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
        patientHolder.enableAll(enableAll);
        patientHolder.setClickListener(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view.getTag();
            int checkedPosition = (int) view.getTag(view.getId());
            if (checkBox.getText().toString().equalsIgnoreCase(OPTION_NONE) && checkedPosition == 0) {
                enableAll = !checkBox.isChecked();
                clearSelection();
                selectItem(checkedPosition);
                notifyDataSetChanged();
            }
            super.onClick(view);
        }
    }
}

class RiskFactorMultiChoiceViewHolder extends RecyclerView.ViewHolder {
    private final SelectAllDialogItemHeaderBinding binding;

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

    public void enableAll(boolean disable) {
        if (binding.getHeader().trim().equalsIgnoreCase(OPTION_NONE))
            binding.cbSelectAll.setEnabled(true);
        else binding.cbSelectAll.setEnabled(disable);
    }
}