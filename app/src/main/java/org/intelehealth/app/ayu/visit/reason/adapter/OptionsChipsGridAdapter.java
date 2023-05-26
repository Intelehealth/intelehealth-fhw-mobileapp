package org.intelehealth.app.ayu.visit.reason.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.List;


public class OptionsChipsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private Node mParentNode;
    private List<Node> mItemList = new ArrayList<Node>();

    public interface OnItemSelection {
        public void onSelect(Node data);
    }

    private OnItemSelection mOnItemSelection;

    public OptionsChipsGridAdapter(RecyclerView recyclerView, Context context, Node parentNode, List<Node> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mParentNode = parentNode;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.index = position;
            genericViewHolder.tvName.setText(mItemList.get(position).findDisplay());

            //Log.v("node", String.valueOf(genericViewHolder.node.isSelected()));

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
                    Log.v("node", "isMultiChoice - " + mParentNode.isMultiChoice());
                    Log.v("node", "isExcludedFromMultiChoice - " + node.isExcludedFromMultiChoice());
                    Log.v("node", "enableExclusiveOption - " + node.isEnableExclusiveOption());
                    Log.v("node", "isExclusiveOption - " + node.isExclusiveOption());
                    if (!mParentNode.isMultiChoice()) {
                        if (mParentNode.isEnableExclusiveOption()) {
                            if (!node.isExclusiveOption()) {
                                for (int i = 0; i < mItemList.size(); i++) {
                                    if (!mItemList.get(i).isExclusiveOption())
                                        mItemList.get(i).setSelected(false);
                                }
                            }
                            mItemList.get(index).setSelected(!mItemList.get(index).isSelected());
                        } else {
                            for (int i = 0; i < mItemList.size(); i++) {
                                mItemList.get(i).setSelected(i == index);
                            }
                        }
                    } else {
                        if (node.isExcludedFromMultiChoice()) {
                            for (int i = 0; i < mItemList.size(); i++) {
                                if (i != index)
                                    mItemList.get(i).setSelected(false);
                            }
                        } else {
                            for (int i = 0; i < mItemList.size(); i++) {
                                if (mItemList.get(i).isExcludedFromMultiChoice())
                                    mItemList.get(i).setSelected(false);
                            }
                        }
                        mItemList.get(index).setSelected(!mItemList.get(index).isSelected());
                    }
                    mOnItemSelection.onSelect(mItemList.get(index));
                    notifyDataSetChanged();
                }
            });

        }


    }


}

