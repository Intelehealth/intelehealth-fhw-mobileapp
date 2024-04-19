package org.intelehealth.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DialogSimpleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<SimpleItemData> mItemList = new ArrayList<SimpleItemData>();
    private boolean mIsSupportLocalLang = true;

    public interface OnItemSelection {
        public void onSelect(SimpleItemData data);
    }

    private OnItemSelection mOnItemSelection;

    public DialogSimpleListAdapter(RecyclerView recyclerView, Context context, List<SimpleItemData> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_simple_dialog_list_item_view_2, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.index = holder.getAbsoluteAdapterPosition();
            genericViewHolder.simpleItemData = mItemList.get(genericViewHolder.index);

            if (genericViewHolder.simpleItemData.getTitleLocal() != null) {
                genericViewHolder.titleView.setText(genericViewHolder.simpleItemData.getTitleLocal());

            } else {
                genericViewHolder.titleView.setText(genericViewHolder.simpleItemData.getTitle());

            }

            if (genericViewHolder.simpleItemData.getSubTitleLocal() != null) {
                genericViewHolder.subTitleTextView.setText(genericViewHolder.simpleItemData.getSubTitleLocal());

            } else {
                genericViewHolder.subTitleTextView.setText("");

            }


            if (genericViewHolder.index == mItemList.size() - 1) {
                genericViewHolder.lineView.setVisibility(View.INVISIBLE);
            } else {
                genericViewHolder.lineView.setVisibility(View.VISIBLE);
            }


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, subTitleTextView;
        ImageView statusImageView;
        View lineView;
        SimpleItemData simpleItemData;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title_tv);
            subTitleTextView = itemView.findViewById(R.id.sub_title_tv);
            statusImageView = itemView.findViewById(R.id.tv_check_status);
            lineView = itemView.findViewById(R.id.line_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemSelection.onSelect(simpleItemData);
                }
            });
        }
    }
}

