package org.intelehealth.ezazi.partogram.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.RowItemOxtocinPrescriptionBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medication;
import org.intelehealth.ezazi.partogram.viewholder.PrescribedIvFluidViewHolder;
import org.intelehealth.ezazi.partogram.viewholder.PrescribedOxytocinViewHolder;
import org.intelehealth.ezazi.ui.elcg.adapter.StageHeaderAdapter;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.LinkedList;

/**
 * Created by Kaveri Zaware on 09-02-2024
 * email - kaveri@intelehealth.org
 **/
public class PrescribedOxytocinAdapter extends StageHeaderAdapter {
    private BaseViewHolder.ViewHolderClickListener clickListener;
    private int expandedItemPosition = -1;

    private PartogramConstants.AccessMode accessMode;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public void setClickListener(BaseViewHolder.ViewHolderClickListener clickListener) {
        this.clickListener = clickListener;
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

    public PrescribedOxytocinAdapter(@NonNull Context ctx, @NonNull LinkedList<ItemHeader> lists) {
        super(ctx, lists);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowItemOxtocinPrescriptionBinding binding = RowItemOxtocinPrescriptionBinding.inflate(getInflater(), parent, false);
        return new PrescribedOxytocinViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PrescribedOxytocinViewHolder oxytocinViewHolder) {
            oxytocinViewHolder.setViewClickListener(clickListener);
            oxytocinViewHolder.setAccessMode(accessMode);
            oxytocinViewHolder.bind((Medication) getItem(position));
            //ivFluidViewHolder.expandDetails(expandedItemPosition == position);
        } else super.onBindViewHolder(holder, position);
    }
}
