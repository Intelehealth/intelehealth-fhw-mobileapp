package app.intelehealth.client.activities.visitSummaryActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

import app.intelehealth.client.R;
import app.intelehealth.client.app.IntelehealthApplication;


/**
 * Created by vishal on 6/30/2017.
 */

public class HorizontalAdapter extends RecyclerView.Adapter<MyViewHolder> {

    public List<File> list;
    Context context;
    int screen_height;
    int screen_width;

    private Handler mBackgroundHandler;

    @Override
    public int getItemCount() {
        return list.size();
    }

    public HorizontalAdapter(List<File> list, Context context) {
        this.list = list;
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_image_carousel, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        if (list.get(position).exists()) {

            Glide.with(context)
                    .load(list.get(position))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.1f)
                    .into(holder.imageView);
            //Works only if width & height is set in dp


            //holder.imageView.setImageBitmap(myBitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayImage(list.get(position));

                }
            });
        }

    }

    public void displayImage(final File file) {
        //AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogStyle);
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);

        final AlertDialog dialog = alertDialogBuilder.create();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                final ImageView imageView = dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                Glide.with(context)
                        .load(file)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .listener(new RequestListener<File, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, File file, Target<GlideDrawable> target, boolean b) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable glideDrawable, File file, Target<GlideDrawable> target, boolean b, boolean b1) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .override(screen_width, screen_height)
                        .into(imageView);
            }
        });

        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }


}
