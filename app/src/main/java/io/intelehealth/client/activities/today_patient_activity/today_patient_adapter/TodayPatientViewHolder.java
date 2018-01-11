package io.intelehealth.client.activities.today_patient_activity.today_patient_adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.intelehealth.client.R;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class TodayPatientViewHolder extends RecyclerView.ViewHolder {

    private TextView headTextView;
    private TextView bodyTextView;
    private TextView indicatorTextView;
    private View rootView;

    public TodayPatientViewHolder(View itemView) {
        super(itemView);
        headTextView = (TextView) itemView.findViewById(R.id.list_item_head_text_view);
        bodyTextView = (TextView) itemView.findViewById(R.id.list_item_body_text_view);
        indicatorTextView = (TextView) itemView.findViewById(R.id.list_item_indicator_text_view);
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
}
