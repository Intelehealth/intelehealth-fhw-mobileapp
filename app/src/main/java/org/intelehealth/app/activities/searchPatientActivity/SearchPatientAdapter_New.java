package org.intelehealth.app.activities.searchPatientActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientAdapter_New;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.List;

/**
 * Created by Prajwal Waingankar on 20/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class SearchPatientAdapter_New extends RecyclerView.Adapter<SearchPatientAdapter_New.SearchHolderView> {
   private Context context;
   private List<PatientDTO> patientDTOS;

    public SearchPatientAdapter_New(Context context, List<PatientDTO> patientDTOS) {
        this.context = context;
        this.patientDTOS = patientDTOS;
    }

    @NonNull
    @Override
    public SearchPatientAdapter_New.SearchHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.search_listitem_layout, parent, false);
        return new SearchPatientAdapter_New.SearchHolderView(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPatientAdapter_New.SearchHolderView holder, int position) {
        final PatientDTO model = patientDTOS.get(position);
        if (model != null) {
            //1. Age
            String age = DateAndTimeUtils.getAge_FollowUp(model.getDateofbirth(), context);
            holder.search_gender.setText(model.getGender() + " " + age);

            //2. Name
            holder.search_name.setText(model.getFirstname() + " " + model.getLastname());

        }
    }

    @Override
    public int getItemCount() {
        return patientDTOS.size();
    }

    public class SearchHolderView extends RecyclerView.ViewHolder {
        TextView search_gender, search_name;

        public SearchHolderView(@NonNull View itemView) {
            super(itemView);

            search_gender = itemView.findViewById(R.id.search_gender);
            search_name = itemView.findViewById(R.id.search_name);
        }
    }
}
