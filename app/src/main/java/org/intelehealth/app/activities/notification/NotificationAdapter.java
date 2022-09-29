package org.intelehealth.app.activities.notification;

import android.content.Context;
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
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.OnSwipeTouchListener;

import java.util.List;

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyHolderView> {
    private Context context;
    List<PatientDTO> patientDTOList;
    private NotificationInterface anInterface;

    public NotificationAdapter(Context context, List<PatientDTO> patientDTOList, NotificationInterface anInterface) {
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
        PatientDTO model = patientDTOList.get(position);
        if (model != null) {
            holder.search_name.setText(model.getFirstname() + " " + model.getLastname() + "\'s prescription was received!");

            holder.delete_imgview.setOnClickListener(v -> {
                anInterface.deleteItem(patientDTOList, holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
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
        ImageView delete_imgview;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);
            search_name = itemView.findViewById(R.id.search_name);
            delete_relative = itemView.findViewById(R.id.delete_relative);
            scroll_layout = itemView.findViewById(R.id.scroll_layout);
            scroll_relative = itemView.findViewById(R.id.scroll_relative);
            delete_imgview = itemView.findViewById(R.id.delete_imgview);

            search_name.setOnTouchListener(new OnSwipeTouchListener(context) {
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
