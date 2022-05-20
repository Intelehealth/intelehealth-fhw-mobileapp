package org.intelehealth.app.activities.visitSummaryActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Prajwal Maruti Waingankar on 04-05-2022, 19:14
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    Context context;
    private String patientUuid, patientName, visitUuid, stage1Hr1_1_EncounterUuid, stage1Hr1_2_EncounterUuid;
    ArrayList<String> timeList;

    public TimelineAdapter(Context context, Intent intent, ArrayList<String> timeList, SessionManager sessionManager) {
        this.context = context;
        this.timeList = timeList;
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientName = intent.getStringExtra("name");
            stage1Hr1_1_EncounterUuid = intent.getStringExtra("Stage1_Hr1_1_En");
            stage1Hr1_2_EncounterUuid = intent.getStringExtra("Stage1_Hr1_2_En");
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
        if(timeList.size() > 0) {
            holder.frame1.setVisibility(View.VISIBLE);
            holder.time1.setText(timeList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder {
        CardView c1hr, c15min, c30min, c45min;
        TextView time1, time2, time3, time4;
        FrameLayout frame1, frame2, frame3, frame4;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);

            c1hr = itemView.findViewById(R.id.cardview_parent);
            c15min = itemView.findViewById(R.id.cardview_parent1);
            c30min = itemView.findViewById(R.id.cardview_parent2);
            c45min = itemView.findViewById(R.id.cardview_parent3);
            time1 = itemView.findViewById(R.id.time1);
            time2 = itemView.findViewById(R.id.time2);
            time3 = itemView.findViewById(R.id.time3);
            time4 = itemView.findViewById(R.id.time4);
            frame1 = itemView.findViewById(R.id.frame1);
            frame2 = itemView.findViewById(R.id.frame2);
            frame3 = itemView.findViewById(R.id.frame3);
            frame4 = itemView.findViewById(R.id.frame4);

            c1hr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i1 = new Intent(context, PastMedicalHistoryActivity.class);
                    i1.putExtra("patientUuid", patientUuid);
                    i1.putExtra("name", patientName);
                    i1.putExtra("visitUuid", visitUuid);
                    i1.putExtra("Stage1_Hr1_1_En", stage1Hr1_1_EncounterUuid);
                    i1.putExtra("Stage1_Hr1_2_En", stage1Hr1_2_EncounterUuid);
                    context.startActivity(i1);
                }
            });
        }
    }
}
