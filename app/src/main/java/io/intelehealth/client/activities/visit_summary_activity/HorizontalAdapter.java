package io.intelehealth.client.activities.visit_summary_activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import io.intelehealth.client.R;

import static android.R.attr.bitmap;


/**
 * Created by vishal on 6/30/2017.
 */

public class HorizontalAdapter extends RecyclerView.Adapter<MyViewHolder> {

    public List<File> list;
    Context context;

    @Override
    public int getItemCount() {
        return list.size();
    }

    public HorizontalAdapter(List<File> list, Context context) {
        this.list = list;
        this.context = context;

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

            final File imageFile = new File(list.get(position).getAbsolutePath());
            Glide.with(context).load(imageFile).into(holder.getImageView());

            //holder.imageView.setImageBitmap(myBitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayImage(imageFile);

                }
            });
        }

    }

    public void displayImage(final File imageFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // context.openFileInput(imageFile.getName())
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
           // BitmapFactory.decodeStream(fileInputStream, null, bitmapOptions);
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bitmapOptions);
            final int imageWidth = bitmapOptions.outWidth;
            final int imageHeight = bitmapOptions.outHeight;

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    ImageView imageView = (ImageView) dialog.findViewById(R.id.confirmationImageView);
                //  float imageWidthInPX = (float) imageView.getWidth();
                 //   LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Math.round(imageWidthInPX),
                  //          Math.round(imageWidthInPX * (float) imageHeight / (float) imageWidth));
                  //  imageView.setLayoutParams(layoutParams);
                    Glide.with(context).load(imageFile)
                            .override(Target.SIZE_ORIGINAL,imageView.getLayoutParams().height).into(imageView);

                }
            });

        dialog.show();




    }
//        holder.imageView.setImageResource(list.get(position));

}
