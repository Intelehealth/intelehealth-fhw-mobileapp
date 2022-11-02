package org.intelehealth.app.activities.visit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class EndVisitAdapter extends RecyclerView.Adapter<EndVisitAdapter.Myholder> {

    private Context context;

    public EndVisitAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public EndVisitAdapter.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item, parent, false);
        return new EndVisitAdapter.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull EndVisitAdapter.Myholder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class Myholder extends RecyclerView.ViewHolder {
        ImageButton end_visit_btn;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            end_visit_btn = itemView.findViewById(R.id.end_visit_btn);
            end_visit_btn.setVisibility(View.VISIBLE);
        }
    }
}
