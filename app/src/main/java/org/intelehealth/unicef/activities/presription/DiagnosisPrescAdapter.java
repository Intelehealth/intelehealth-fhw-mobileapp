package org.intelehealth.unicef.activities.presription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;

import java.util.List;

/**
 * Created by Prajwal Maruti Waingankar on 23-12-2021, 01:20
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class DiagnosisPrescAdapter extends RecyclerView.Adapter<DiagnosisPrescAdapter.PrescViewModel> {
    Context context;
    List<PrescDataModel> prescDataModels;

    public DiagnosisPrescAdapter(Context context, List<PrescDataModel> prescDataModels) {
        this.context = context;
        this.prescDataModels = prescDataModels;
    }

    public DiagnosisPrescAdapter(Context presContext) {
        this.context = presContext;
    }

    @Override
    public PrescViewModel onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.presc_selection_tab, parent, false);

        return new PrescViewModel(view);
    }

    @Override
    public void onBindViewHolder(PrescViewModel holder, int position) {
        if(prescDataModels.size() > 0)
            holder.userSelectionValueTextview.setText(prescDataModels.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return prescDataModels.size();
    }

    public class PrescViewModel extends RecyclerView.ViewHolder {
        TextView userSelectionValueTextview;
        ImageView deleteImageButton;
        String uuid;

        public PrescViewModel(View itemView) {
            super(itemView);
            userSelectionValueTextview = itemView.findViewById(R.id.userSelectionValueTextview);
            deleteImageButton = itemView.findViewById(R.id.deleteImageButton);

            deleteImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(prescDataModels.size() > 0)
                        uuid = prescDataModels.get(getAdapterPosition()).getUuid();
                    // Call api here for Delete...

                }
            });
        }
    }
}
