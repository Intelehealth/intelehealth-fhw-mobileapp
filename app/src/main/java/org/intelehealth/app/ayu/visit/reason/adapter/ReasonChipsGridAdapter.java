package org.intelehealth.app.ayu.visit.reason.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ReasonChipsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<ReasonData> mItemList = new ArrayList<ReasonData>();

    public interface OnItemSelection {
        public void onSelect(ReasonData data);
    }

    private OnItemSelection mOnItemSelection;

    public ReasonChipsGridAdapter(RecyclerView recyclerView, Context context, List<ReasonData> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_chips_for_reason_item_view, parent, false);
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
            genericViewHolder.index = position;
            genericViewHolder.tvName.setText(mItemList.get(position).getReasonNameLocalized());
            if (mItemList.get(position).isSelected()) {
                genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_common_primary_bg);
                genericViewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.white));
            } else {

                if (mItemList.get(position).isEnabled()) {
                    genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_chip_type_1_bg);
                    genericViewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.ui2_black_text_color));
                } else {
                    genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg);
                    genericViewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.gray_2));
                }
            }


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemList.get(index).isEnabled()) {
                        mOnItemSelection.onSelect(mItemList.get(index));
                        mItemList.get(index).setSelected(true);
                        notifyItemChanged(index);
                    }
                }
            });

        }


    }


}

