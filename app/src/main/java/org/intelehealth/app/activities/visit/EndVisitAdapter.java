package org.intelehealth.app.activities.visit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.models.PrescriptionModel;

import java.util.List;

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class EndVisitAdapter extends RecyclerView.Adapter<EndVisitAdapter.Myholder> {
    private Context context;
    private List<PrescriptionModel> arrayList;

    public EndVisitAdapter(Context context, List<PrescriptionModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
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
        PrescriptionModel model = arrayList.get(position);
        if (model != null) {

        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
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
