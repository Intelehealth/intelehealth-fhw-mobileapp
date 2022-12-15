package org.intelehealth.app.ayu.visit.reason.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class OptionsChipsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();

    public interface OnItemSelection {
        public void onSelect(Node data);
    }

    private OnItemSelection mOnItemSelection;

    public OptionsChipsGridAdapter(RecyclerView recyclerView, Context context, List<Node> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
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
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.index = position;
            genericViewHolder.tvName.setText(mItemList.get(position).getText());
            if (genericViewHolder.node.isSelected()) {
                genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                genericViewHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                genericViewHolder.tvName.setBackgroundResource(R.drawable.edittext_border_blue);
                genericViewHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.ui2_black_text_color));
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Node node;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemList.get(index).setSelected(true);
                    mOnItemSelection.onSelect(mItemList.get(index));
                    tvName.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                    tvName.setTextColor(mContext.getResources().getColor(R.color.white));
                }
            });

        }


    }


}

