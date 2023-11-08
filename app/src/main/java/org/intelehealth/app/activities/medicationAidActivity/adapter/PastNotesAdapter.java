package org.intelehealth.app.activities.medicationAidActivity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.ItemPastnotesBinding;
import org.intelehealth.app.models.dispenseAdministerModel.PastNotesModel;

import java.util.List;

/**
 * Created by - Prajwal W. on 08/11/23.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class PastNotesAdapter extends RecyclerView.Adapter<PastNotesAdapter.MyViewHolder> {
    private List<PastNotesModel> pastNotesModelList;
    private Context context;

    public PastNotesAdapter(Context context, List<PastNotesModel> pastNotesModelList) {
        this.context = context;
        this.pastNotesModelList = pastNotesModelList;
    }

    @NonNull
    @Override
    public PastNotesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pastnotes, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastNotesAdapter.MyViewHolder holder, int position) {
        PastNotesModel model = pastNotesModelList.get(position);
        holder.binding.tvPastnotes.setText(model.getNotes());
        holder.binding.tvPastnotesDatetime.setText(model.getDateTime());
    }

    @Override
    public int getItemCount() {
        return pastNotesModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemPastnotesBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPastnotesBinding.bind(itemView);
        }
    }
}
