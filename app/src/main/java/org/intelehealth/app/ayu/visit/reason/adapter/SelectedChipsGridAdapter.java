package org.intelehealth.app.ayu.visit.reason.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SelectedChipsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<ReasonData> mItemList = new ArrayList<ReasonData>();

    public interface OnItemSelection {
        public void onSelect(ReasonData data);
        public void onRemoved(ReasonData data);
    }

    private OnItemSelection mOnItemSelection;

    public SelectedChipsGridAdapter(RecyclerView recyclerView, Context context, List<ReasonData> itemList, OnItemSelection onItemSelection) {
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
                .inflate(R.layout.ui2_selected_chips_item_view, parent, false);
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
            genericViewHolder.index = position;
            genericViewHolder.tvName.setText(mItemList.get(position).getReasonNameLocalized());


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        int index;
        ImageView removeImageView;

        GenericViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            removeImageView = itemView.findViewById(R.id.im_remove);
            removeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mOnItemSelection.onSelect(tvName.getText().toString());
                    mOnItemSelection.onRemoved(mItemList.get(index));
                    mItemList.remove(mItemList.get(index));
                    notifyDataSetChanged();

                }
            });

        }


    }


}

