package org.intelehealth.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DialogListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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

    public DialogListAdapter(RecyclerView recyclerView, Context context, List<SimpleItemData> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_simple_dialog_list_item_view, parent, false);
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

            genericViewHolder.titleView.setText(genericViewHolder.simpleItemData.getTitle());

            if (genericViewHolder.simpleItemData.isSelected()) {
                genericViewHolder.statusImageView.setVisibility(View.VISIBLE);
            } else {
                genericViewHolder.statusImageView.setVisibility(View.INVISIBLE);
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
        TextView titleView;
        ImageView statusImageView;
        View lineView;
        SimpleItemData simpleItemData;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.tvTitleSimpleDialogListItem);
            statusImageView = itemView.findViewById(R.id.ivCheckSimpleDialogListItem);
            lineView = itemView.findViewById(R.id.viewDividerSimpleDialogListItem);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemSelection.onSelect(simpleItemData);
                }
            });
        }
    }
}

