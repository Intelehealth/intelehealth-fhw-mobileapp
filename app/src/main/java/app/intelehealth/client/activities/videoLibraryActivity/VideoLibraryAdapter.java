package app.intelehealth.client.activities.videoLibraryActivity;

import android.content.Context;
import android.content.Intent;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import app.intelehealth.client.R;


/**
 * Created by Dexter Barretto on 6/14/17.
 * Github : @dbarretto
 */

public class VideoLibraryAdapter extends RecyclerView.Adapter<VideoLibraryAdapter.VideoLibraryHolder> {

    List<File> videoList;
    Context context;

    public VideoLibraryAdapter(List<File> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @Override
    public VideoLibraryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_library_list_item, parent, false);
        VideoLibraryHolder viewHolder = new VideoLibraryHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VideoLibraryHolder holder, final int position) {
        //set Thumbnail

        if(videoList.get(position).isDirectory()) holder.image.setImageResource
                (R.drawable.ic_action_folder_open);
        else if(videoList.get(position).isFile()) holder.image.setImageBitmap(
                ThumbnailUtils.createVideoThumbnail(videoList.get(position).getAbsolutePath(),
                        MediaStore.Images.Thumbnails.MINI_KIND));

        //Set Text
        holder.text.setText(videoList.get(position).getAbsolutePath().substring
                (videoList.get(position).getAbsolutePath().lastIndexOf(File.separator)+1));

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoList.get(position).isDirectory()) {
                    VideoLibraryActivity videoLibraryActivity = (VideoLibraryActivity) context;
                    ((VideoLibraryActivity) context).openFragmentAddToBackstack(videoList.get(position).getAbsolutePath());
                }else{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoList.get(position).getAbsolutePath()));
                    intent.setDataAndType(Uri.parse(videoList.get(position).getAbsolutePath()), "video/*");
                    context.startActivity(intent);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoLibraryHolder extends RecyclerView.ViewHolder {

        protected ImageView image;
        protected TextView text;
        protected View rootView;

        public VideoLibraryHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            image = itemView.findViewById(R.id.video_thumbnail_ImageView);
            text = itemView.findViewById(R.id.video_name_TextView);
        }

        public ImageView getImage() {
            return image;
        }

        public void setImage(ImageView image) {
            this.image = image;
        }

        public TextView getText() {
            return text;
        }

        public void setText(TextView text) {
            this.text = text;
        }

        public View getRootView() {
            return rootView;
        }
    }


}
