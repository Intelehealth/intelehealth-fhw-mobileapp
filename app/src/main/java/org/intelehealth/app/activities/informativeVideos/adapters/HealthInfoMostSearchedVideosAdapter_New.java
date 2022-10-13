package org.intelehealth.app.activities.informativeVideos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class HealthInfoMostSearchedVideosAdapter_New extends RecyclerView.Adapter<HealthInfoMostSearchedVideosAdapter_New.MyViewHolder> {
    Context context;

    public HealthInfoMostSearchedVideosAdapter_New(Context context) {
        this.context = context;

    }

    @Override
    public HealthInfoMostSearchedVideosAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_info_videos_searched_ui2, parent, false);
        HealthInfoMostSearchedVideosAdapter_New.MyViewHolder myViewHolder = new HealthInfoMostSearchedVideosAdapter_New.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(HealthInfoMostSearchedVideosAdapter_New.MyViewHolder holder, int position) {

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
