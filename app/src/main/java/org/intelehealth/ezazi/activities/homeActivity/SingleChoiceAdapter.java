package org.intelehealth.ezazi.activities.homeActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.SingleChoiceDialogItemBinding;
import org.intelehealth.ezazi.ui.dialog.adapter.BaseSelectedRecyclerViewAdapter;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.ui.search.SearchableAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 17:09.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceAdapter extends BaseSelectedRecyclerViewAdapter<SingChoiceItem, SingleChoiceViewHolder> {
    private View.OnClickListener clickListener;

    public SingleChoiceAdapter(Context context, List<SingChoiceItem> choices, View.OnClickListener clickListener) {
        super(context, choices);
        inflater = LayoutInflater.from(context);
        this.clickListener = clickListener;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemIndex();
    }

    @NonNull
    @Override
    public SingleChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleChoiceDialogItemBinding binding = SingleChoiceDialogItemBinding.inflate(inflater, parent, false);
        return new SingleChoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleChoiceViewHolder holder, int position) {
        holder.bind(getItem(position));
        holder.setSelected(isItemSelected(position));
        holder.setClickListener(this);
    }

    @Override
    protected String searchableValue(int position) {
        return searchableList.get(position).getItem();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResultSearch(ArrayList<SingChoiceItem> results) {
        updateItems(results);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(v);
    }
}

class SingleChoiceViewHolder extends RecyclerView.ViewHolder {
    private final SingleChoiceDialogItemBinding binding;

    public SingleChoiceViewHolder(@NonNull SingleChoiceDialogItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SingChoiceItem choice) {
        binding.setValue(choice);
        binding.tvChoice.setTag(choice);
    }

    public void setSelected(boolean isSelected) {
        binding.tvChoice.setSelected(isSelected);
    }

    public void setClickListener(View.OnClickListener listener) {
        binding.tvChoice.setTag(R.id.tvChoice, getAbsoluteAdapterPosition());
        binding.tvChoice.setOnClickListener(listener);
    }
}
