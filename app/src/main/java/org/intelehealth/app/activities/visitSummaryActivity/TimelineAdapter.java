package org.intelehealth.app.activities.visitSummaryActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;

/**
 * Created by Prajwal Maruti Waingankar on 04-05-2022, 19:14
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    Context context;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "", patientUuid = "";

    public TimelineAdapter(Context context, Intent intent) {
        this.context = context;
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
        }
    }

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
        CardView c1hr, c15min, c30min, c45min;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);

            c1hr = itemView.findViewById(R.id.cardview_parent);
            c15min = itemView.findViewById(R.id.cardview_parent1);
            c30min = itemView.findViewById(R.id.cardview_parent2);
            c45min = itemView.findViewById(R.id.cardview_parent3);

            c1hr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i1 = new Intent(context, PastMedicalHistoryActivity.class);
                    i1.putExtra("patientUuid", patientUuid);
                    i1.putExtra("encounterUuidAdultIntial", "");
                    i1.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
                    context.startActivity(i1);
                }
            });
        }
    }
}
