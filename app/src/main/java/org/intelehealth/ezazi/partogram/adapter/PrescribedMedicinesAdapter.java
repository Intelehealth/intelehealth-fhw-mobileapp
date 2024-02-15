package org.intelehealth.ezazi.partogram.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.RowItemMedicinePrescriptionBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.partogram.viewholder.PrescribedMedicineViewHolder;
import org.intelehealth.ezazi.ui.elcg.adapter.CategoryHeaderAdapter;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.LinkedList;

/**
 * Created by Kaveri Zaware on 06-02-2024
 * email - kaveri@intelehealth.org
 **/
public class PrescribedMedicinesAdapter extends CategoryHeaderAdapter {
    private BaseViewHolder.ViewHolderClickListener clickListener;
    private int expandedItemPosition = -1;

    private PartogramConstants.AccessMode accessMode;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public void setClickListener(BaseViewHolder.ViewHolderClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isHeader()) return HEADER;
        else return super.getItemViewType(position);
    }

    public void setExpandedItemPosition(int expandedItemPosition) {
        if (expandedItemPosition == this.expandedItemPosition) {
            this.expandedItemPosition = -1;
            notifyItemChanged(expandedItemPosition);
        } else {
            if (expandedItemPosition > -1)
                notifyItemChanged(this.expandedItemPosition);
            this.expandedItemPosition = expandedItemPosition;
            notifyItemChanged(expandedItemPosition);
        }
    }

    public PrescribedMedicinesAdapter(@NonNull Context ctx, @NonNull LinkedList<ItemHeader> lists) {
        super(ctx, lists);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType != HEADER) {
            RowItemMedicinePrescriptionBinding binding = RowItemMedicinePrescriptionBinding.inflate(getInflater(), parent, false);
            return new PrescribedMedicineViewHolder(binding);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PrescribedMedicineViewHolder medicineViewHolder) {
            medicineViewHolder.setViewClickListener(clickListener);
            medicineViewHolder.setAccessMode(accessMode);
            medicineViewHolder.bind((Medicine) getItem(position));
            medicineViewHolder.expandDetails(expandedItemPosition == position);
        } else super.onBindViewHolder(holder, position);
    }
}
