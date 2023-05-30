package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.SelectAllDialogItemHeaderBinding;
import org.intelehealth.ezazi.ui.dialog.model.MultiChoiceItem;
import org.intelehealth.ezazi.ui.dialog.model.SelectAllMultiChoice;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 11:23.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class SelectAllHeaderAdapter extends MultiChoiceAdapter<MultiChoiceItem, RecyclerView.ViewHolder>
        implements CompoundButton.OnCheckedChangeListener {

    protected static final int ITEM_HEADER = 1000;

    public SelectAllHeaderAdapter(Context context, ArrayList<MultiChoiceItem> objectsList) {
        super(context, objectsList);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_HEADER) {
            SelectAllDialogItemHeaderBinding binding = SelectAllDialogItemHeaderBinding.inflate(inflater, parent, false);
            return new SelectAllViewHolder(binding);
        } else return super.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItem(position).isHeader() && holder instanceof SelectAllViewHolder) {
            SelectAllViewHolder selectAllViewHolder = (SelectAllViewHolder) holder;
            selectAllViewHolder.bind((SelectAllMultiChoice) getItem(position));
            selectAllViewHolder.setCheckedAll(getSelectedItems().size() == getItems().size());
            selectAllViewHolder.setCheckedListener(this);
        } else super.bindViewHolder(holder, position);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Log.d("SelectAllHeaderAdapter", "Select all clicked");
        if (compoundButton.getId() == R.id.cbSelectAll) {
            if (b) selectAllItem();
            else clearSelection();
        }
    }
}

class SelectAllViewHolder extends RecyclerView.ViewHolder {
    private final SelectAllDialogItemHeaderBinding binding;

    public SelectAllViewHolder(@NonNull SelectAllDialogItemHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SelectAllMultiChoice item) {
        binding.setHeader(item.getHeader());
        binding.setHeader("Select All");

    }

    public void setCheckedAll(boolean isCheckedAll) {
        Log.d("SelectAllViewHolder", "Select all clicked=>" + isCheckedAll);
        binding.cbSelectAll.setChecked(isCheckedAll);
    }

    public void setCheckedListener(CompoundButton.OnCheckedChangeListener listener) {
        binding.cbSelectAll.setTag(binding);
        binding.cbSelectAll.setOnCheckedChangeListener(listener);
    }
}
