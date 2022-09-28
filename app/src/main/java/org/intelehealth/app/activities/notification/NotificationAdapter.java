package org.intelehealth.app.activities.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.intelehealth.app.R;
import org.intelehealth.app.models.dto.PatientDTO;
import java.util.List;

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyHolderView> {
    private Context context;
    List<PatientDTO> patientDTOList;

    public NotificationAdapter(Context context, List<PatientDTO> patientDTOList) {
        this.context = context;
        this.patientDTOList = patientDTOList;
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
        PatientDTO model = patientDTOList.get(position);
        if (model != null) {
            holder.search_name.setText(model.getFirstname() + " " + model.getLastname() + "\'s prescription was received!");
        }
    }

    @Override
    public int getItemCount() {
        return patientDTOList.size();
    }

    public class MyHolderView extends RecyclerView.ViewHolder {
        TextView search_name;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);
            search_name = itemView.findViewById(R.id.search_name);
        }
    }
}
