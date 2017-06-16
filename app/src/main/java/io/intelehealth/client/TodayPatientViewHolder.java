package io.intelehealth.client;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

/**
 * This class has all get() and set() methods for fields in RecyclerView.
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

    /**
     *
     * @return TextView variable of type TextView
     */
    public TextView getHeadTextView() {
        return headTextView;
    }

    /**
     *
     * @param headTextView variable of type TextView
     *   @return                  void
     */
    public void setHeadTextView(TextView headTextView) {
        this.headTextView = headTextView;
    }


    /**
     *
     * @return TextView variable of type TextView
     */
    public TextView getBodyTextView() {
        return bodyTextView;
    }

    /**
     *
     * @param bodyTextView  variable of type TextView
     * @return              void
     */
    public void setBodyTextView(TextView bodyTextView) {
        this.bodyTextView = bodyTextView;
    }


    /**
     *
     * @return ImageView variable of type ImageView
     */
    public ImageView getIndicatorImageView() {
        return indicatorImageView;
    }

    /**
     *
     * @param indicatorImageView variable of type ImageView
     *@return                    void
     */

    public void setIndicatorImageView(ImageView indicatorImageView) {
        this.indicatorImageView = indicatorImageView;
    }

    /**
     *
     * @return variable of type View
     */
    public View getRootView() {
        return rootView;
    }
}
