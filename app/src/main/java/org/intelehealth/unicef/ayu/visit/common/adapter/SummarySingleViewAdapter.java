package org.intelehealth.unicef.ayu.visit.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SummarySingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<String> mItemList = new ArrayList<String>();

    public interface OnItemSelection {
        public void onSelect(String data);
    }

    private OnItemSelection mOnItemSelection;

    public SummarySingleViewAdapter(RecyclerView recyclerView, Context context, List<String> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_summary_qa_row_item_view_single_item, parent, false);
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
            genericViewHolder.summaryData = mItemList.get(position);
            genericViewHolder.index = position;
            genericViewHolder.questionTextView.setText(genericViewHolder.summaryData);


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, answerTextView;
        String summaryData;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.tv_question_label);
            answerTextView = itemView.findViewById(R.id.tv_answer_value);
        }
    }
}

