package org.intelehealth.app.activities.additionalDocumentsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.notification.AdapterInterface;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.models.DocumentObject;

import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentAdapter extends RecyclerView.Adapter<AdditionalDocumentViewHolder> {

    int screen_height;
    int screen_width;

    private List<DocumentObject> documentList = new ArrayList<>();
    private Context context;
    private String filePath;
    String mEncounterUUID;
    ImagesDAO imagesDAO = new ImagesDAO();
    private AdapterInterface anInterface;
    private AdditionalDocumentViewHolder rcv;
    private boolean fromVisitDetails;

    private static final String TAG = AdditionalDocumentAdapter.class.getSimpleName();

    public AdditionalDocumentAdapter(Context context, String edult,
                                     List<DocumentObject> documentList, String filePath,
                                     AdapterInterface anInterface, boolean fromVisitDetails) {
        this.documentList = documentList;
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;
        mEncounterUUID = edult;
        this.filePath = filePath;
        this.anInterface = anInterface;
        this.fromVisitDetails = fromVisitDetails;
    }

    @Override
    public AdditionalDocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.additional_docs_listitem, null);
        rcv = new AdditionalDocumentViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final AdditionalDocumentViewHolder holder, int position) {
        holder.getDocumentNameTextView().setText
                (holder.itemView.getContext().getString(R.string.document_) + (position + 1));

        final File image = new File(documentList.get(position).getDocumentPhoto());
        RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                .asDrawable().sizeMultiplier(0.1f);

        Glide.with(context)
                .load(image)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(requestBuilder)
                .into(holder.getDocumentPhotoImageView());

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImage(image);
            }
        });

        holder.hideCancel(fromVisitDetails);
//        if (fromVisitDetails)
//            holder.getDeleteDocumentImageView().setVisibility(View.GONE);
//        else
//            holder.getDeleteDocumentImageView().setVisibility(View.VISIBLE);

        holder.getDeleteDocumentImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image.exists()) image.delete();

                anInterface.deleteAddDoc_Item(documentList, holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
                notifyItemRangeChanged(holder.getLayoutPosition(), documentList.size());
                notifyDataSetChanged(); // this line is imp else it leaves empty space on delete.
                String imageName = holder.getDocumentNameTextView().getText().toString();


                try {
                    List<String> imageList = imagesDAO.isImageListObsExists(mEncounterUUID, UuidDictionary.COMPLEX_IMAGE_AD);
                    for (String images : imageList) {
                        CustomLog.d(TAG, "image= " + images);

                    }
                    imagesDAO.deleteImageFromDatabase(imageList.get(position));


                } catch (DAOException e) {

                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
//                    imagesDAO.deleteImageFromDatabase(StringUtils.getFileNameWithoutExtensionString(imageName));

            }
        });
    }


    public void add(DocumentObject doc) {
        boolean bool = documentList.add(doc);
        if (bool) CustomLog.d(TAG, "add: Item added to list");
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.documentList.size();
    }


    public void displayImage(final File file) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);


        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                ImageView imageView = dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                if (imageView != null) {
                    Glide.with(context)
                            .load(file)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }
                            })
                            .override(screen_width, screen_height)
                            .into(imageView);
                }
            }
        });

        dialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public void hideCancelBtnAddDoc(boolean flag) {
        fromVisitDetails = flag;
        notifyItemRangeChanged(0, getItemCount());
//        if (rcv != null && rcv.getDeleteDocumentImageView() != null) {
//            if (flag)
//                rcv.getDeleteDocumentImageView().setVisibility(View.GONE);
//            else
//                rcv.getDeleteDocumentImageView().setVisibility(View.VISIBLE);
//        }

    }


}
