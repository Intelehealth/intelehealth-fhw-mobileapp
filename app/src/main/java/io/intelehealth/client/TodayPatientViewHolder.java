package io.intelehealth.client;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class TodayPatientViewHolder extends RecyclerView.ViewHolder {

    private TextView headTextView;
    private TextView bodyTextView;
    private ImageView indicatorImageView;
    private View rootView;

    public TodayPatientViewHolder(View itemView) {
        super(itemView);
        headTextView = (TextView) itemView.findViewById(R.id.list_item_head_text_view);
        bodyTextView = (TextView) itemView.findViewById(R.id.list_item_body_text_view);
        indicatorImageView = (ImageView) itemView.findViewById(R.id.list_item_indicator_image_view);
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

    public ImageView getIndicatorImageView() {
        return indicatorImageView;
    }

    public void setIndicatorImageView(ImageView indicatorImageView) {
        this.indicatorImageView = indicatorImageView;
    }

    public View getRootView() {
        return rootView;
    }
}
