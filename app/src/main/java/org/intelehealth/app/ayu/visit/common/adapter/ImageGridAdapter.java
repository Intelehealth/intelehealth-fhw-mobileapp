package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();

    public interface OnItemSelection {
        public void onSelect(Node data);
    }

    private OnItemSelection mOnItemSelection;

    public ImageGridAdapter(RecyclerView recyclerView, Context context, List<Node> itemList, OnItemSelection onItemSelection) {
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
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.index = position;


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        ImageView mainImageView, addImageView, crossImageView;
        Node node;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            mainImageView = itemView.findViewById(R.id.iv_image);
            addImageView = itemView.findViewById(R.id.iv_add_items);
            addImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // start image capture activity
                }
            });

        }


    }


}

