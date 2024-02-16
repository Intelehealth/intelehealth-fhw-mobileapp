package org.intelehealth.ezazi.partogram.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.RowItemPrescriptionFollowedBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.viewholder.PrescriptionFollowedPlanHolder;
import org.intelehealth.ezazi.ui.elcg.adapter.StageHeaderAdapter;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.LinkedList;

public class AssessmentsByHealthWorkerAdapter extends StageHeaderAdapter {
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

    public AssessmentsByHealthWorkerAdapter(@NonNull Context ctx, @NonNull LinkedList<ItemHeader> lists) {
        super(ctx, lists);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowItemPrescriptionFollowedBinding binding = RowItemPrescriptionFollowedBinding.inflate(getInflater(), parent, false);
        return new PrescriptionFollowedPlanHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PrescriptionFollowedPlanHolder prescriptionPlanHolder) {
            prescriptionPlanHolder.setViewClickListener(clickListener);
            //prescriptionPlanHolder.setAccessMode(accessMode);
            prescriptionPlanHolder.bind((ObsDTO) getItem(position));
            //prescriptionPlanHolder.expandDetails(expandedItemPosition == position);
        } else super.onBindViewHolder(holder, position);
    }

}
