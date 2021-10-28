package org.intelehealth.msfarogyabharat.activities.visitSummaryActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.msfarogyabharat.R;

import java.util.List;

/**
 * Created By: Prajwal Waingankar on 22-Oct-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitSummaryAdapter extends RecyclerView.Adapter<VisitSummaryAdapter.VisitSummaryViewHolder> {
    Context context;
    List<String> complaintList;
    List<String> visitUuidList;
    List<String> physexamList;
    boolean allVisitsEnded = false;
    String currentvisituuid;
    String complaint, physexam, visitid;

    public VisitSummaryAdapter(Context context, List<String> visitUuidList,
                               List<String> complaintList, List<String> physexamList,
                               boolean allVisitsEnded, String currentvisituuid) {
        this.context = context;
        this.visitUuidList = visitUuidList;
        this.complaintList = complaintList;
        this.physexamList = physexamList;
        this.allVisitsEnded = allVisitsEnded;
        this.currentvisituuid = currentvisituuid;
        Log.v("main","allvisitsended: "+ this.allVisitsEnded);
    }

    @Override
    public VisitSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_visitsummary, parent, false);

        return new VisitSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VisitSummaryAdapter.VisitSummaryViewHolder holder, int position) {
        complaint = complaintList.get(position);
        physexam = physexamList.get(position);

        holder.textView_caseTitle.setText("Case " + (position + 1));
        holder.textView_content_complaint.setText(complaint);
        holder.textView_content_physexam.setText(physexam);
    }

    @Override
    public int getItemCount() {
        return visitUuidList.size();
    }

    public class VisitSummaryViewHolder extends RecyclerView.ViewHolder {
        TextView textView_caseTitle, textView_content_complaint, textView_content_physexam;
        ImageButton imagebutton_edit_complaint, imagebutton_edit_physexam;
        LinearLayout linearlayout_body;

        public VisitSummaryViewHolder(View itemView) {
            super(itemView);
            textView_caseTitle = itemView.findViewById(R.id.textView_caseTitle);
            textView_content_complaint = itemView.findViewById(R.id.textView_content_complaint);
            textView_content_physexam = itemView.findViewById(R.id.textView_content_physexam);
            imagebutton_edit_complaint = itemView.findViewById(R.id.imagebutton_edit_complaint);
            imagebutton_edit_physexam = itemView.findViewById(R.id.imagebutton_edit_physexam);
            linearlayout_body = itemView.findViewById(R.id.linearlayout_body);

            Animation slide_down = AnimationUtils.loadAnimation(context, R.anim.slide_down);
            Animation slide_up = AnimationUtils.loadAnimation(context, R.anim.slide_up);

            textView_caseTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(linearlayout_body.getVisibility() == View.VISIBLE) {
                        linearlayout_body.startAnimation(slide_up);
                        linearlayout_body.setVisibility(View.GONE);
                    }
                    else {
                        linearlayout_body.startAnimation(slide_down);
                        linearlayout_body.setVisibility(View.VISIBLE);
                    }

                    //edit icons visibility handled.
                    //is all visits are Ended for a patient then hide the edit pencil icon too.
                    if(allVisitsEnded) {
                        imagebutton_edit_complaint.setVisibility(View.GONE);
                        imagebutton_edit_physexam.setVisibility(View.GONE);
                    }
                    else {
                        //any one visit is ended i.e. the latest visit is Active so show edit for only that visit ie. the last visit.
                        visitid = visitUuidList.get(getAdapterPosition());
                        if(visitid.equalsIgnoreCase(currentvisituuid) && !allVisitsEnded) {
                            Log.v("main", "position: "+ getAdapterPosition() + ":");
                            // Toast.makeText(context, "good", Toast.LENGTH_SHORT).show();
                            imagebutton_edit_complaint.setVisibility(View.VISIBLE);
                            imagebutton_edit_physexam.setVisibility(View.VISIBLE);
                        }
                        else {
                            // Toast.makeText(context, "bad", Toast.LENGTH_SHORT).show();
                            imagebutton_edit_complaint.setVisibility(View.GONE);
                            imagebutton_edit_physexam.setVisibility(View.GONE);
                        }
                    }
                }
            });

        }
    }
}
