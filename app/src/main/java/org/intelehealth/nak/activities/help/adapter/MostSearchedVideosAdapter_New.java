package org.intelehealth.nak.activities.help.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.nak.R;
import org.intelehealth.nak.activities.help.models.YoutubeVideoList;

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
//        holder.videoCV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri uri = Uri.parse(videosList.get(position).getUrl());
//                intent.setData(uri);
//                context.startActivity(intent);
//            }
//        });
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
            videoCV = itemView.findViewById(R.id.videoCV);
            webView = itemView.findViewById(R.id.youtubeVideo_WV);
            description = itemView.findViewById(R.id.videoDesc);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient()
            {

            });
        }
    }


}
