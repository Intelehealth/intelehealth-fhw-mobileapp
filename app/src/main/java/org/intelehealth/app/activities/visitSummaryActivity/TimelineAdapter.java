package org.intelehealth.app.activities.visitSummaryActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

/**
* Created by Prajwal Maruti Waingankar on 04-05-2022, 19:14
* Copyright (c) 2021 . All rights reserved.
* Email: prajwalwaingankar@gmail.com
* Github: prajwalmw
*/

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_listitem, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 12;
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder {

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
