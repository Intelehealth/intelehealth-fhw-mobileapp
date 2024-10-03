package org.intelehealth.app.activities.visit.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visit.model.PastVisitData;

import java.util.ArrayList;
import java.util.List;


public class PastVisitListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<PastVisitData> mItemList = new ArrayList<PastVisitData>();

    public interface OnItemSelected {
        void onItemSelected(PastVisitData pastVisitData);


    }

    private OnItemSelected mOnItemSelected;

    public PastVisitListingAdapter(RecyclerView recyclerView, Context context, List<PastVisitData> itemList, OnItemSelected onItemSelected) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelected = onItemSelected;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
        CustomLog.v("ImageGridAdapter", "itemList.size - " + mItemList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.past_visit_list_item, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CustomLog.v("ImageGridAdapter", "onBindViewHolder - " + position);
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;

            genericViewHolder.pastVisitData = mItemList.get(position);
            genericViewHolder.index = position;
            genericViewHolder.chiefComplaintTextView.setText(genericViewHolder.pastVisitData.getChiefComplain());

            String hideVisitUUID = genericViewHolder.pastVisitData.getVisitUUID();
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            genericViewHolder.visitUUIDTextView.setText(mContext.getString(R.string.visitID) + ": XXXX" + hideVisitUUID);
            genericViewHolder.visitDateTextView.setText(mContext.getString(R.string.visit_date) + " :" + genericViewHolder.pastVisitData.getVisitDate());
            if (genericViewHolder.index == mItemList.size() - 1) {
                genericViewHolder.separator.setVisibility(View.GONE);
            } else {
                genericViewHolder.separator.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView chiefComplaintTextView, visitUUIDTextView, visitDateTextView;
        PastVisitData pastVisitData;
        View separator;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            separator = itemView.findViewById(R.id.separator);
            chiefComplaintTextView = itemView.findViewById(R.id.chief_complaint_txt);
            visitUUIDTextView = itemView.findViewById(R.id.visitID_tv);
            visitDateTextView = itemView.findViewById(R.id.visit_date_tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemSelected.onItemSelected(pastVisitData);
                }
            });


        }


    }


}

