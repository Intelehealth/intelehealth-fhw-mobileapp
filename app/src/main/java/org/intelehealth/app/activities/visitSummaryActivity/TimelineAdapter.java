package org.intelehealth.app.activities.visitSummaryActivity;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.partogram.PartogramDataCaptureActivity;
import org.intelehealth.app.utilities.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Prajwal Maruti Waingankar on 04-05-2022, 19:14
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    Context context;
    private String patientUuid, patientName, visitUuid;
    ArrayList<EncounterDTO> encounterDTOList;
    ObsDAO obsDAO;
    ObsDTO obsDTO;
    SessionManager sessionManager;

    public TimelineAdapter(Context context, Intent intent, ArrayList<EncounterDTO> encounterDTOList, SessionManager sessionManager) {
        this.context = context;
        this.encounterDTOList = encounterDTOList;
        this.sessionManager = sessionManager;

        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientName = intent.getStringExtra("name");

//            String time = intent.getStringExtra("encounter_time");
//            SimpleDateFormat timeLineTime = new SimpleDateFormat("HH:mm a", Locale.ENGLISH);
//            String timeLineTimeValue = timeLineTime.format(todayDate);
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
        if (encounterDTOList.size() > 0) {
            if (encounterDTOList.get(position).getEncounterTime() != null &&
                    !encounterDTOList.get(position).getEncounterTime().equalsIgnoreCase("")) {

                String time = encounterDTOList.get(position).getEncounterTime();
                SimpleDateFormat longTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm a", Locale.ENGLISH);
                String encounterTimeAmPmFormat = "";
                try {
                    Date timeDateType = longTimeFormat.parse(time);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timeDateType);

                    Log.v("Timeline", "position&CardTime: " + position + "- " + calendar.getTime());
                    if (position % 2 == 0) { // Even
                          calendar.add(Calendar.HOUR, 1);
                          calendar.add(Calendar.MINUTE, 20); // Add 1hr + 20min
                      //  calendar.add(Calendar.MINUTE, 2); // Testing
                        Log.v("Timeline", "calendarTime 1Hr: " + calendar.getTime().toString());
                    } else { // Odd
                         calendar.add(Calendar.MINUTE, 40); // Add 30min + 10min
                       // calendar.add(Calendar.MINUTE, 1); // Testing
                        Log.v("Timeline", "calendarTime 30min: " + calendar.getTime().toString());
                    }

                    if (calendar.after(Calendar.getInstance())) { // ie. eg: 7:20 is after of current (6:30) eg.
                        holder.cardview.setClickable(true);
                        holder.cardview.setEnabled(true);
                      //  holder.cardview.setCardBackgroundColor(context.getResources().getColor(R.color.amber));
                    } else {
                        holder.cardview.setClickable(false);
                        holder.cardview.setEnabled(false);
                        holder.cardview.setCardElevation(0);

                        /* since card is disabled that means the either the user has filled data or has forgotten to fill.
                         We need to check this by using the encounterUuid and checking in obs tbl if any obs is created.
                         If no obs created than create Missed Enc obs for this disabled encounter. */
                        obsDAO = new ObsDAO();
                        boolean isMissed = false;
                        isMissed = obsDAO.checkObsAndCreateMissedObs(encounterDTOList.get(position).getUuid(), sessionManager.getCreatorID());
                        if(isMissed) {
                            holder.summary_textview.setText(context.getResources().getString(R.string.missed_interval));
                          //  holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                        }
                        else {
                            holder.cardview.setCardBackgroundColor(context.getResources().getColor(R.color.black_overlay));
                            holder.summary_textview.setText(context.getResources().getString(R.string.submitted_interval));
                        }
                    }

                    encounterTimeAmPmFormat = timeFormat.format(timeDateType);
                    Log.v("timeline", "AM Format: " + encounterTimeAmPmFormat);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("timeline", "AM Format: " + e.getMessage());
                }

                holder.timeTextview.setText(encounterTimeAmPmFormat);
            }
        }
    }


    @Override
    public int getItemCount() {
        return encounterDTOList.size();
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder {
        CardView cardview;
        TextView timeTextview, summary_textview;
        FrameLayout frame1, frame2, frame3, frame4;
        int index;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);

            cardview = itemView.findViewById(R.id.cardview_parent);
            timeTextview = itemView.findViewById(R.id.time1);
            summary_textview = itemView.findViewById(R.id.summary_textview);
            frame1 = itemView.findViewById(R.id.frame1);

            cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i1 = new Intent(context, PartogramDataCaptureActivity.class);
                    i1.putExtra("patientUuid", patientUuid);
                    i1.putExtra("name", patientName);
                    i1.putExtra("visitUuid", visitUuid);
                    i1.putExtra("encounterUuid", encounterDTOList.get(getAdapterPosition()).getUuid());
                    i1.putExtra("type", getAdapterPosition() % 2 != 0 ? HALF_HOUR : HOURLY);
//                    i1.putExtra("Stage1_Hr1_1_En", stage1Hr1_1_EncounterUuid);
//                    i1.putExtra("Stage1_Hr1_2_En", stage1Hr1_2_EncounterUuid);
                    context.startActivity(i1);
                }
            });
        }
    }

    private static final int HOURLY = 0;
    private static final int HALF_HOUR = 1;
    private static final int FIFTEEN_MIN = 2;
}
