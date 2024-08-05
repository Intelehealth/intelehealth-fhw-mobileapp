package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;


public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<String> mItemList = new ArrayList<String>();

    public interface OnImageAction {
        void onImageRemoved(int index, String image);

        void onNewImageRequest();
    }

    private OnImageAction mOnImageAction;

    public ImageGridAdapter(RecyclerView recyclerView, Context context, List<String> itemList, OnImageAction onImageAction) {
        mContext = context;
        mItemList = itemList;
        mOnImageAction = onImageAction;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
        CustomLog.v("ImageGridAdapter", "itemList.size - " + mItemList.size());
    }

    public void addNull() {
        //mItemList.add(null);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_image_item_vire, parent, false);
        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position1) {

        if (holder instanceof GenericViewHolder) {

            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            int position = holder.getAbsoluteAdapterPosition();
            CustomLog.v("ImageGridAdapter", "onBindViewHolder - " + position);
            if (position < mItemList.size()) {
                genericViewHolder.image = mItemList.get(position);
            } else {
                genericViewHolder.image = null;
            }
            genericViewHolder.index = position;
            CustomLog.v("ImageGridAdapter", "genericViewHolder.image - " + genericViewHolder.image);
            if (genericViewHolder.image != null && !genericViewHolder.image.isEmpty()) {
                Glide.with(mContext)
                        .load(genericViewHolder.image)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(genericViewHolder.mainImageView);
                genericViewHolder.addImageView.setVisibility(View.GONE);
                genericViewHolder.crossImageView.setVisibility(View.VISIBLE);
                genericViewHolder.mainImageView.setBackgroundResource(R.drawable.edittext_border_blue);
            } else {
                genericViewHolder.addImageView.setVisibility(View.VISIBLE);
                genericViewHolder.crossImageView.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(R.drawable.edittext_border_blue_dotted)
                        .skipMemoryCache(true)
                        .into(genericViewHolder.mainImageView);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size() + 1;
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        ImageView mainImageView, addImageView, crossImageView;
        String image;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            mainImageView = itemView.findViewById(R.id.iv_image);
            addImageView = itemView.findViewById(R.id.iv_add_items);
            crossImageView = itemView.findViewById(R.id.iv_image_delete);
            addImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // start image capture activity
                    mOnImageAction.onNewImageRequest();
                }
            });
            crossImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // start image capture activity
                    mOnImageAction.onImageRemoved(index, image);
                }
            });

        }


    }


}

