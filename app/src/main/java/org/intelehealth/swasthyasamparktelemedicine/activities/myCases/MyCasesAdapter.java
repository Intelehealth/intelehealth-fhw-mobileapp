package org.intelehealth.swasthyasamparktelemedicine.activities.myCases;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.swasthyasamparktelemedicine.R;
import org.intelehealth.swasthyasamparktelemedicine.activities.todayPatientActivity.TodayPatientAdapter;

public class MyCasesAdapter extends RecyclerView.Adapter<MyCasesAdapter.MyCasesViewHolder>{

    @NonNull
    @Override
    public MyCasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyCasesViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    class MyCasesViewHolder extends RecyclerView.ViewHolder {

        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;

        public MyCasesViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.iv_prescription);
            tv_not_uploaded = (TextView) itemView.findViewById(R.id.tv_not_uploaded);
            rootView = itemView;
        }

        public TextView getHeadTextView() {
            return headTextView;
        }

        public void setHeadTextView(TextView headTextView) {
            this.headTextView = headTextView;
        }

        public TextView getBodyTextView() {
            return bodyTextView;
        }

        public void setBodyTextView(TextView bodyTextView) {
            this.bodyTextView = bodyTextView;
        }

        public TextView getIndicatorTextView() {
            return indicatorTextView;
        }

        public void setIndicatorTextView(TextView indicatorTextView) {
            this.indicatorTextView = indicatorTextView;
        }

        public View getRootView() {
            return rootView;
        }

        public TextView getTv_not_uploaded() {
            return tv_not_uploaded;
        }

        public void setTv_not_uploaded(TextView tv_not_uploaded) {
            this.tv_not_uploaded = tv_not_uploaded;
        }
    }
}
