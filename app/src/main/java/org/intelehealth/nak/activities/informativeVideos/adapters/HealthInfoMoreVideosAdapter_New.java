package org.intelehealth.nak.activities.informativeVideos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.nak.R;

public class HealthInfoMoreVideosAdapter_New extends RecyclerView.Adapter<HealthInfoMoreVideosAdapter_New.MyViewHolder> {
    Context context;

    public HealthInfoMoreVideosAdapter_New(Context context) {
        this.context = context;

    }

    @Override
    public HealthInfoMoreVideosAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_info_videos_more_ui2, parent, false);
        HealthInfoMoreVideosAdapter_New.MyViewHolder myViewHolder = new HealthInfoMoreVideosAdapter_New.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(HealthInfoMoreVideosAdapter_New.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 30;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
            //name = itemView.findViewById(R.id.tv);

        }
    }


}
