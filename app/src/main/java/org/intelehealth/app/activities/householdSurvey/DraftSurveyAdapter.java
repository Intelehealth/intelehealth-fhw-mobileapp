package org.intelehealth.app.activities.householdSurvey;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.models.dto.PatientAttributesDTO;

import java.util.List;

/**
 * Created by Prajwal Maruti Waingankar on 28-03-2022, 19:36
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class DraftSurveyAdapter extends RecyclerView.Adapter<DraftSurveyAdapter.DraftSurveyViewHolder> {
    private List<PatientAttributesDTO> patientAttributesDTOList;
    private Context context;

    public DraftSurveyAdapter(List<PatientAttributesDTO> patientAttributesDTOList, Context context) {
        this.patientAttributesDTOList = patientAttributesDTOList;
        this.context = context;
    }

    @NonNull
    @Override
    public DraftSurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DraftSurveyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class DraftSurveyViewHolder extends RecyclerView.ViewHolder {
        public DraftSurveyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
