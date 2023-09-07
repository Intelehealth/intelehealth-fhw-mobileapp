package org.intelehealth.ezazi.partogram.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.RowItemMedicineBinding;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.partogram.viewholder.MedicineViewHolder;
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 06-09-2023 - 17:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class MedicineAdapter extends BaseRecyclerViewAdapter<Medicine> {
    private BaseViewHolder.ViewHolderClickListener clickListener;

    public void setClickListener(BaseViewHolder.ViewHolderClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public MedicineAdapter(@NonNull Context ctx, @NonNull List<Medicine> lists) {
        super(ctx, lists);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowItemMedicineBinding binding = RowItemMedicineBinding.inflate(getInflater(), parent, false);
        return new MedicineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MedicineViewHolder) {
            MedicineViewHolder medicineViewHolder = (MedicineViewHolder) holder;
            medicineViewHolder.setViewClickListener(clickListener);
            medicineViewHolder.bind(getItem(position));
        }
    }
}
