package org.intelehealth.app.ayu.visit.pocdevice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class HeartAndLungRecordingsAdapter extends RecyclerView.Adapter<HeartAndLungRecordingsAdapter.ViewHolder> {
    @NonNull
    @Override
    public HeartAndLungRecordingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heart_and_lung_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeartAndLungRecordingsAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSaveTitile,txtSaveTime;
        ImageView img_save_paly,img_save_delete,img_save_repat;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSaveTitile = itemView.findViewById(R.id.txtSaveTitile);
            txtSaveTime = itemView.findViewById(R.id.txtSaveTime);
            img_save_paly = itemView.findViewById(R.id.img_save_paly);
            img_save_delete = itemView.findViewById(R.id.img_save_delete);
            img_save_repat = itemView.findViewById(R.id.img_save_repat);
        }
    }
}
