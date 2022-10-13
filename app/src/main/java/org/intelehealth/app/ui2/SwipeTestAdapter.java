package org.intelehealth.app.ui2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.List;

public class SwipeTestAdapter extends RecyclerView.Adapter<SwipeTestAdapter.MyViewHolder> {
    Context context;
    List<String> dataList;

    public SwipeTestAdapter(Context context, List<String> dataList) {
        this.context = context;
        this.dataList = dataList;


    }

    @Override
    public SwipeTestAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_test_rv_swipe, parent, false);
        SwipeTestAdapter.MyViewHolder myViewHolder = new SwipeTestAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(SwipeTestAdapter.MyViewHolder holder, int position) {
        holder.name1.setText(dataList.get(position));

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name1;

        public MyViewHolder(View itemView) {
            super(itemView);
            name1 = itemView.findViewById(R.id.name1);
            TextView name2 = itemView.findViewById(R.id.name2);

        }
    }

    public void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }
}
