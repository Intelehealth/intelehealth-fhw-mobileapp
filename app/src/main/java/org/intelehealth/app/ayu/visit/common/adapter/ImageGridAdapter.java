package org.intelehealth.app.ayu.visit.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.knowledgeEngine.Node;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<String> mItemList = new ArrayList<String>();

    public interface OnItemSelection {
        public void onSelect(Node data);
    }

    private OnItemSelection mOnItemSelection;

    public ImageGridAdapter(RecyclerView recyclerView, Context context, List<String> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_image_item_vire, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.image = mItemList.get(position);
            genericViewHolder.index = position;
            Glide.with(mContext)
                    .load(genericViewHolder.image)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.1f)
                    .into(genericViewHolder.mainImageView);

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        ImageView mainImageView, addImageView, crossImageView;
        String image;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            mainImageView = itemView.findViewById(R.id.iv_image);
            addImageView = itemView.findViewById(R.id.iv_add_items);
            addImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // start image capture activity
                    openCamera(getImagePath(), image);
                }
            });

        }


    }

    private String getImagePath() {
        File filePath = new File(AppConstants.IMAGE_PATH);
        return filePath.getAbsolutePath();
    }

    public void openCamera(String imagePath, String imageName) {

        Intent cameraIntent = new Intent(mContext, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
            }
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        mContext.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

}

