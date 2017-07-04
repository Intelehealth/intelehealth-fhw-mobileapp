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

import java.io.File;
import java.util.List;

import io.intelehealth.client.R;


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
            final Bitmap myBitmap = BitmapFactory.decodeFile(list.get(position).getAbsolutePath());

            holder.imageView.setImageBitmap(myBitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   displayImage(myBitmap);

                }
            });
        }

    }

    public void displayImage(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                ImageView imageView = (ImageView) dialog.findViewById(R.id.confirmationImageView);
                float imageWidthInPX = (float) imageView.getWidth();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Math.round(imageWidthInPX),
                        Math.round(imageWidthInPX * (float) bitmap.getHeight() / (float) bitmap.getWidth()));
                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
            }
        });

        dialog.show();

    }
//        holder.imageView.setImageResource(list.get(position));

}
