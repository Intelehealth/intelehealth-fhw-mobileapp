package org.intelehealth.feature.chat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.feature.chat.databinding.ItemRowDateHeaderBinding;
import org.intelehealth.feature.chat.model.DayHeader;
import org.intelehealth.feature.chat.model.ItemHeader;

import java.util.List;


/**
 * Created by Vaghela Mithun R. on 03-08-2023 - 18:45.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class DateHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected LayoutInflater inflater;
    protected Context mContext;
    protected List<ItemHeader> mItemList;

    public DateHeaderAdapter(Context context, List<ItemHeader> list) {
        this.inflater = LayoutInflater.from(context);
        this.mItemList = list;
        mContext = context;
    }

    public static final int DATE_HEADER = 1000;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DATE_HEADER) {
            ItemRowDateHeaderBinding binding = ItemRowDateHeaderBinding.inflate(inflater, parent, false);
            return new DateHeaderHolder(binding);
        } else return createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == DATE_HEADER && holder instanceof DateHeaderHolder) {
            DayHeader header = (DayHeader) mItemList.get(position);
            ((DateHeaderHolder) holder).bind(header.displayFormat());
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}

class DateHeaderHolder extends RecyclerView.ViewHolder {
    private final ItemRowDateHeaderBinding binding;

    public DateHeaderHolder(@NonNull ItemRowDateHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String date) {
        binding.setDateString(date);
    }
}
