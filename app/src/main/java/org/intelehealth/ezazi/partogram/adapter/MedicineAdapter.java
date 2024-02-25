package org.intelehealth.ezazi.partogram.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.RowItemMedicineBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.partogram.viewholder.MedicineViewHolder;
import org.intelehealth.ezazi.ui.elcg.adapter.CategoryHeaderAdapter;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.LinkedList;

/**
 * Created by Vaghela Mithun R. on 06-09-2023 - 17:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class MedicineAdapter extends CategoryHeaderAdapter {
    public static final int MEDICINE = 10003;
    protected BaseViewHolder.ViewHolderClickListener clickListener;
    protected boolean allowAdminister = false;
    private int expandedItemPosition = -1;

    private PartogramConstants.AccessMode accessMode;

    public void setAllowAdminister(boolean allowAdminister) {
        this.allowAdminister = allowAdminister;
    }

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public void setClickListener(BaseViewHolder.ViewHolderClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Medicine) return MEDICINE;
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

    public MedicineAdapter(@NonNull Context ctx, @NonNull LinkedList<ItemHeader> lists) {
        super(ctx, lists);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MEDICINE) {
            RowItemMedicineBinding binding = RowItemMedicineBinding.inflate(getInflater(), parent, false);
            return new MedicineViewHolder(binding);
        } else return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MedicineViewHolder medicineViewHolder && getItem(position) instanceof Medicine) {
            if (clickListener != null) medicineViewHolder.setViewClickListener(clickListener);
            medicineViewHolder.setAccessMode(accessMode);
            medicineViewHolder.bind((Medicine) getItem(position), allowAdminister);
            medicineViewHolder.expandDetails(expandedItemPosition == position);
        } else super.onBindViewHolder(holder, position);
    }
}
