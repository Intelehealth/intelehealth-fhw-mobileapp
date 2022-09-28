package org.intelehealth.app.help.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class HelpVideosAdapterVerticle_New extends RecyclerView.Adapter<HelpVideosAdapterVerticle_New.MyViewHolder> {
    Context context;

    public HelpVideosAdapterVerticle_New(Context context) {
        this.context = context;

    }

    @Override
    public HelpVideosAdapterVerticle_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_most_searched_videos_vertical_ui2, parent, false);
        HelpVideosAdapterVerticle_New.MyViewHolder myViewHolder = new HelpVideosAdapterVerticle_New.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(HelpVideosAdapterVerticle_New.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 30;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
            //name = itemView.findViewById(R.id.staff_name);

        }
    }


}
