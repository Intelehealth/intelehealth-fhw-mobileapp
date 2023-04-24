package org.intelehealth.app.activities.notification;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.visit.PrescriptionActivity;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.OnSwipeTouchListener;

import java.util.List;

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyHolderView> {
    private Context context;
    List<NotificationModel> patientDTOList;
    private AdapterInterface anInterface;

    public NotificationAdapter(Context context, List<NotificationModel> patientDTOList, AdapterInterface anInterface) {
        this.context = context;
        this.patientDTOList = patientDTOList;
        this.anInterface = anInterface;
    }

    @NonNull
    @Override
    public NotificationAdapter.MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.notification_list_item, parent, false);
        return new NotificationAdapter.MyHolderView(row);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.MyHolderView holder, int position) {
        NotificationModel model = patientDTOList.get(position);
        if (model != null) {
            holder.search_name.setText(model.getFirst_name() + " " + model.getLast_name() + context.getString(R.string.prescription_received));

            holder.delete_imgview.setOnClickListener(v -> {
                anInterface.deleteNotifi_Item(patientDTOList, holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
            });


            holder.open_presc_btn.setOnClickListener(v -> {
                Intent intent = new Intent(context, PrescriptionActivity.class);
                intent.putExtra("patientname", model.getFirst_name() + " " + model.getLast_name());
                intent.putExtra("patientUuid", model.getPatientuuid());
                intent.putExtra("tag", "Notification screen");
                intent.putExtra("patient_photo", model.getPatient_photo());
                intent.putExtra("visit_ID", model.getVisitUUID());
                intent.putExtra("visit_startDate", model.getVisit_startDate());
                intent.putExtra("gender", model.getGender());
                intent.putExtra("openmrsID", model.getOpenmrsID());

                String vitalsUUID = fetchEncounterUuidForEncounterVitals(model.getVisitUUID());
                String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(model.getVisitUUID());
                model.setEncounterUuidVitals(vitalsUUID);
                model.setEncounterUuidAdultIntial(adultInitialUUID);

                intent.putExtra("encounterUuidVitals", vitalsUUID);
                intent.putExtra("encounterUuidAdultIntial", adultInitialUUID);

                String age = DateAndTimeUtils.getAge_FollowUp(model.getDate_of_birth(), context);
                model.setAge(age);
                intent.putExtra("age", age);

                String followupDate = getFollowupDataForVisitUUID(model.getVisitUUID());
                model.setFollowupDate(followupDate);
                intent.putExtra("followupDate", followupDate);

                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return patientDTOList.size();
    }

    public class MyHolderView extends RecyclerView.ViewHolder {
        TextView search_name;
        LinearLayout scroll_layout;
        CardView fu_cardview_item;
        RelativeLayout delete_relative, scroll_relative;
        ImageView delete_imgview, open_presc_btn;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);
            search_name = itemView.findViewById(R.id.search_name);
            delete_relative = itemView.findViewById(R.id.delete_relative);
            scroll_layout = itemView.findViewById(R.id.scroll_layout);
            scroll_relative = itemView.findViewById(R.id.scroll_relative);
            delete_imgview = itemView.findViewById(R.id.delete_imgview);
            open_presc_btn = itemView.findViewById(R.id.open_presc_btn);

            scroll_relative.setOnTouchListener(new OnSwipeTouchListener(context) {
                @Override
                public void onSwipeLeft() {
                    super.onSwipeLeft();
                    delete_relative.setVisibility(View.VISIBLE);
                    scroll_relative.setTranslationX(-100);
//                    user is scroll towards left side from right side.
                }
                @Override
                public void onSwipeRight() {
                    super.onSwipeRight();
                    delete_relative.setVisibility(View.GONE);
                    scroll_relative.setTranslationX(10);
//                    Toast.makeText(MainActivity.this, "Swipe Right gesture detected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
