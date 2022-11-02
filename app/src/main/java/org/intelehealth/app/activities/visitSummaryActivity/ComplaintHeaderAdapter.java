package org.intelehealth.app.activities.visitSummaryActivity;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.List;
/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class ComplaintHeaderAdapter extends RecyclerView.Adapter<ComplaintHeaderAdapter.MyHolder> {
    private Context context;
    private List<String> stringList;

    public ComplaintHeaderAdapter(Context context, List<String> stringList) {
        this.context = context;
        this.stringList = stringList;
    }

    @NonNull
    @Override
    public ComplaintHeaderAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cc_recycler_listitem, parent, false);
        return new MyHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintHeaderAdapter.MyHolder holder, int position) {
        String complaint = stringList.get(position);

        if (complaint != null && !complaint.isEmpty() && !complaint.equalsIgnoreCase("")) {
            holder.textView.setText(Html.fromHtml(complaint));
        }
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.cc_textview);
        }
    }
}
