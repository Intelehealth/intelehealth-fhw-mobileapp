package org.intelehealth.app.activities.help.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.models.QuestionModel;
import org.intelehealth.app.activities.help.models.YoutubeVideoList;

import java.util.List;

public class MostSearchedVideosAdapter_New extends RecyclerView.Adapter<MostSearchedVideosAdapter_New.MyViewHolder> {
    Context context;
    List<YoutubeVideoList> videosList;

    public MostSearchedVideosAdapter_New(Context context, List<YoutubeVideoList> videosList) {
        this.context = context;
        this.videosList = videosList;

    }

    @Override
    public MostSearchedVideosAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_most_searched_videos_ui2, parent, false);
        MostSearchedVideosAdapter_New.MyViewHolder myViewHolder = new MostSearchedVideosAdapter_New.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.webView.loadData(videosList.get(position).getUrl(), "text/html" , "utf-8");
        holder.description.setText(videosList.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView videoCV;
        WebView webView;
        TextView description;
        public MyViewHolder(View itemView) {
            super(itemView);
            videoCV = itemView.findViewById(R.id.cvMostSearchedVideosItem);
            webView = itemView.findViewById(R.id.wvMostSearchedVideosItem);
            description = itemView.findViewById(R.id.tvDescMostSearchedVideosItem);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient()
            {

            });
        }
    }


}
