package org.intelehealth.ezazi.activities.homeActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.SingleChoiceDialogItemBinding;
import org.intelehealth.ezazi.ui.search.SearchableAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 17:09.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceAdapter extends SearchableAdapter<String, SingleChoiceViewHolder> {
    private LayoutInflater inflater;
    private List<String> choices;

    private int selected = -1;

    private View.OnClickListener clickListener;

    public SingleChoiceAdapter(Context context, List<String> choices, View.OnClickListener clickListener) {
        super(choices);
        inflater = LayoutInflater.from(context);
        this.choices = choices;
        this.clickListener = clickListener;
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
        holder.setSelected(position == selected);
        holder.setClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    public String getItem(int position) {
        return choices.get(position);
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        if (this.selected > -1)
            notifyItemChanged(this.selected);

        this.selected = selected;

        if (selected != -1)
            notifyItemChanged(selected);
    }

    @Override
    protected String searchableValue(int position) {
        return searchableList.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResultSearch(ArrayList<String> results) {
        choices = results;
        notifyDataSetChanged();
    }
}

class SingleChoiceViewHolder extends RecyclerView.ViewHolder {
    private final SingleChoiceDialogItemBinding binding;

    public SingleChoiceViewHolder(@NonNull SingleChoiceDialogItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String choice) {
        binding.setValue(choice);
    }

    public void setSelected(boolean isSelected) {
        binding.tvChoice.setSelected(isSelected);
    }

    public void setClickListener(View.OnClickListener listener) {
        binding.tvChoice.setTag(getAdapterPosition());
        binding.tvChoice.setOnClickListener(listener);
    }
}
