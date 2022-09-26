package org.intelehealth.app.activities.searchPatientActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
            //  1. Age
            String age = DateAndTimeUtils.getAge_FollowUp(model.getDateofbirth(), context);
            holder.search_gender.setText(model.getGender() + " " + age);

            //  2. Name
            holder.search_name.setText(model.getFirstname() + " " + model.getLastname());

            //  3. Priority Tag
            if (model.isEmergency())
                holder.priority_tag_imgview.setVisibility(View.VISIBLE);
            else
                holder.priority_tag_imgview.setVisibility(View.GONE);

            //  4. Visit Start Date else No visit created text display.
            if (model.getVisit_startdate() != null) {
                holder.fu_item_calendar.setVisibility(View.VISIBLE);
                holder.search_date_relative.setText(model.getVisit_startdate());
            } else {
                holder.fu_item_calendar.setVisibility(View.GONE);
                holder.search_date_relative.setText(R.string.no_visit_created);
            }

            //  5. Prescription received/pending tag display.
            if (model.isPrescription_exists())
                holder.presc_tag_imgview.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_presc_received));
            else
                holder.presc_tag_imgview.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_presc_pending));

        }
    }

    @Override
    public int getItemCount() {
        return patientDTOS.size();
    }

    public class SearchHolderView extends RecyclerView.ViewHolder {
        TextView search_gender, search_name, search_date_relative;
        ImageView priority_tag_imgview, fu_item_calendar, presc_tag_imgview;

        public SearchHolderView(@NonNull View itemView) {
            super(itemView);

            search_gender = itemView.findViewById(R.id.search_gender);
            search_name = itemView.findViewById(R.id.search_name);
            priority_tag_imgview = itemView.findViewById(R.id.priority_tag_imgview);
            fu_item_calendar = itemView.findViewById(R.id.fu_item_calendar);
            search_date_relative = itemView.findViewById(R.id.search_date_relative);
            presc_tag_imgview = itemView.findViewById(R.id.presc_tag_imgview);
        }
    }
}
