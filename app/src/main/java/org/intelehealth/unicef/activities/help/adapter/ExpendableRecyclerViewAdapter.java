package org.intelehealth.unicef.activities.help.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.help.models.Animations;
import org.intelehealth.unicef.activities.help.models.QuestionModel;

import java.util.List;

public class ExpendableRecyclerViewAdapter extends RecyclerView.Adapter<ExpendableRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ExpendableRecyclerViewA";
    Context context;
    List<QuestionModel> questionsList;

    public ExpendableRecyclerViewAdapter(Context context, List<QuestionModel> questionsList) {

        this.context = context;
        this.questionsList = questionsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_item_faq_ui2, viewGroup, false);
        ViewHolder myViewHolder = new ViewHolder(view);

        return myViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int i) {

        holder.tvQuestion.setText(questionsList.get(i).getQuestion());
        holder.tvDesc.setText(questionsList.get(i).getDescription());


        holder.ivViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean show = toggleLayout(!questionsList.get(i).isExpanded(), v, holder.layoutExpand);
                questionsList.get(i).setExpanded(show);
            }
        });

    }

    @Override
    public int getItemCount() {
        return questionsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvDesc;
        ImageView ivViewMore;
        LinearLayout layoutExpand;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            ivViewMore = itemView.findViewById(R.id.iv_view_more);
            layoutExpand = itemView.findViewById(R.id.layout_expand);
            tvDesc = itemView.findViewById(R.id.tv_desc);
        }
    }

    private boolean toggleLayout(boolean isExpanded, View v, LinearLayout layoutExpand) {
        Animations.toggleArrow(v, isExpanded);
        if (isExpanded) {
            layoutExpand.setVisibility(View.VISIBLE);

        } else {
            layoutExpand.setVisibility(View.GONE);

        }

        return isExpanded;

    }
}