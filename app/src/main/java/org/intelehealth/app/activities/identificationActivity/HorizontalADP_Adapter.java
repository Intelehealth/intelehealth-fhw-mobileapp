package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.database.dao.ImagesDAO.deleteADPImages;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.MyViewHolder;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on March 2023.
 * Github: prajwalmw
 */
public class HorizontalADP_Adapter extends RecyclerView.Adapter<MyViewHolder> {

    public List<File> list;
    Context context;
    int screen_height;
    int screen_width;

    private Handler mBackgroundHandler;

    @Override
    public int getItemCount() {
        return list.size();
    }

    public HorizontalADP_Adapter(List<File> list, Context context) {
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

            holder.delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // displayImage(list.get(position));
                    deleteCurrentImage(list.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                    Toast.makeText(context, R.string.image_deleted_successfully, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void deleteCurrentImage(File file, int adapterPosition) {
        // 1. local image delete - start
        if (file.exists())
            file.delete();
        list.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        notifyItemRangeChanged(adapterPosition, list.size());
        // local image delete - end

        // 2. local image delete - database - start
        String filename = file.getName();
        Log.v("ADP", "ADP: " + "deleted filename: " + filename + "\n" + "filepath: "  + file.getPath());

        // Deleting from local db is not requried as since image is deleted ie. no base64 image to push ie. on server end no image receivd
        // ie. it wont show the deleted images as new images send altogether are overwritten. However, I m deleting it from local storage
        // as its not a good practise to send empty base64 image and cause error on server end also avoiding pushing empty images too.
        deleteADPImages(file.getPath());

       /* try {

            List<String> imageList = imagesDAO.isImageListObsExists(mEncounterUUID, UuidDictionary.COMPLEX_IMAGE_AD);
            for (String images : imageList) {
                Log.d(TAG,"image= "+images);

            }
            imagesDAO.deleteImageFromDatabase(imageList.get(position));
        } catch (DAOException e) {

            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }*/
        // local image delete - database - end

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

    public List<File> getADPList() {
        return list;
    }
}
