package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AssociateSymptomsQueryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();

    public interface OnItemSelection {
        public void onSelect(Node data);
    }

    private OnItemSelection mOnItemSelection;

    public AssociateSymptomsQueryAdapter(RecyclerView recyclerView, Context context, List<Node> itemList, OnItemSelection onItemSelection) {
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
                .inflate(R.layout.ui2_associate_symptoms_list_item, parent, false);
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

            genericViewHolder.questionTextView.setText((position + 1) + ". " + genericViewHolder.node.getText());
            //genericViewHolder.recyclerView.setAdapter(reasonChipsGridAdapter);

            if (mItemList.get(position).isNoSelected()) {
                genericViewHolder.noTextView.setTextColor(mContext.getResources().getColor(R.color.white));
                genericViewHolder.noTextView.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                genericViewHolder.noTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_close_18_white, 0, 0, 0);
            } else {
                genericViewHolder.noTextView.setTextColor(mContext.getResources().getColor(R.color.ui2_black_text_color));
                genericViewHolder.noTextView.setBackgroundResource(R.drawable.edittext_border_blue);
                genericViewHolder.noTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_close_18_green, 0, 0, 0);
            }
            genericViewHolder.currentRootOptionList = mItemList.get(position).getOptionsList();

            if (mItemList.get(position).isSelected()) {
                genericViewHolder.yesTextView.setTextColor(mContext.getResources().getColor(R.color.white));
                genericViewHolder.yesTextView.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                genericViewHolder.yesTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_18_white, 0, 0, 0);

                Log.v("OptionList", new Gson().toJson(mItemList.get(position).getOptionsList()));
                if (mItemList.get(position).getOptionsList() != null && mItemList.get(position).getOptionsList().size() > 0) {
                    genericViewHolder.recyclerView.setVisibility(View.VISIBLE);
                    genericViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    genericViewHolder.questionsListingAdapter = new QuestionsListingAdapter(genericViewHolder.recyclerView, mContext, false, null, 1, new QuestionsListingAdapter.OnItemSelection() {
                        @Override
                        public void onSelect(Node node) {

                        }

                        @Override
                        public void needTitleChange(String title) {

                        }

                        @Override
                        public void onAllAnswered(boolean isAllAnswered) {

                        }

                        @Override
                        public void onCameraRequest() {

                        }
                    });
                    genericViewHolder.recyclerView.setAdapter(genericViewHolder.questionsListingAdapter);
                    //for (int i = 0; i <genericViewHolder.currentRootOptionList.size(); i++) {
                    // genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position).getOptionsList().get(i));
                    genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position));

                    //}
                } else {
                    genericViewHolder.recyclerView.setVisibility(View.GONE);
                }
            } else {
                genericViewHolder.yesTextView.setTextColor(mContext.getResources().getColor(R.color.ui2_black_text_color));
                genericViewHolder.yesTextView.setBackgroundResource(R.drawable.edittext_border_blue);
                genericViewHolder.yesTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_18_green, 0, 0, 0);
                genericViewHolder.recyclerView.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, yesTextView, noTextView;
        Node node;
        int index;
        RecyclerView recyclerView;
        QuestionsListingAdapter questionsListingAdapter;
        int currentComplainNodeOptionsIndex;
        List<Node> currentRootOptionList = new ArrayList<>();

        GenericViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rcv_container);
            //recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));

            questionTextView = itemView.findViewById(R.id.tv_question);
            yesTextView = itemView.findViewById(R.id.tv_yes);
            noTextView = itemView.findViewById(R.id.tv_no);
            yesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemList.get(index).setNoSelected(false);
                    mItemList.get(index).setSelected(true);
                    if (mItemList.get(index).getAssociated_symptoms() == 1) {
                        mItemList.get(index).setAssociated_symptoms(0);
                    } else {
                        mItemList.get(index).setAssociated_symptoms(1);
                    }
                    notifyDataSetChanged();
                }
            });

            noTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemList.get(index).isNoSelected()) {
                        mItemList.get(index).setNoSelected(false);

                    } else {
                        mItemList.get(index).setNoSelected(true);
                    }
                    mItemList.get(index).setUnselected();


                    if (mItemList.get(index).getAssociated_symptoms() == -1) {
                        mItemList.get(index).setAssociated_symptoms(0);
                    } else {
                        mItemList.get(index).setAssociated_symptoms(-1);
                    }
                    notifyDataSetChanged();
                }
            });

        }


    }


}

