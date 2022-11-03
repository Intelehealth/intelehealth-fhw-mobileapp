package org.intelehealth.app.activities.visit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;


/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.Myholder> {
    private Context context;

    public VisitAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public VisitAdapter.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item, parent, false);
        return new VisitAdapter.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitAdapter.Myholder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class Myholder extends RecyclerView.ViewHolder {
        private CardView fu_cardview_item;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item);

            fu_cardview_item.setOnClickListener(v -> {
                Intent intent = new Intent(context, PrescriptionActivity.class);
                context.startActivity(intent);
            });
        }
    }
}
