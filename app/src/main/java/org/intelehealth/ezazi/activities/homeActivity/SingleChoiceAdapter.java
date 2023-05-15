package org.intelehealth.ezazi.activities.homeActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.SingleChoiceDialogItemBinding;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 17:09.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceAdapter extends RecyclerView.Adapter<SingleChoiceViewHolder> {
    private LayoutInflater inflater;
    private List<String> choices;

    public SingleChoiceAdapter(Context context, List<String> choices) {
        inflater = LayoutInflater.from(context);
        this.choices = choices;
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
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    private String getItem(int position) {
        return choices.get(position);
    }
}

class SingleChoiceViewHolder extends RecyclerView.ViewHolder {
    private SingleChoiceDialogItemBinding binding;

    public SingleChoiceViewHolder(@NonNull SingleChoiceDialogItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String choice) {
        binding.setValue(choice);
    }
}
