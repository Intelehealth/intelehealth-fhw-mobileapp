package app.intelehealth.client.widget.materialprogressbar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import app.intelehealth.client.R;


/**
 * CustomProgressDialog
 * <p>
 * Shows custom progress dialog with animation.
 */

public class CustomProgressDialog extends Dialog {

    private TextView mTvProgressTitle;
    private CircleProgressBar mIvAnimation;
    private AnimationDrawable mCloverAnimation;

    public CustomProgressDialog(Context context) {
        super(context, R.style.Theme_Transparent);

        this.setContentView(R.layout.custom_progress_dialog);
        mTvProgressTitle = (TextView) this.findViewById(R.id.tvProgressTitle);
        mTvProgressTitle.setVisibility(View.GONE);
        this.setCancelable(false);
        mIvAnimation = (CircleProgressBar) findViewById(R.id.ivAnim);

    }

    /**
     * Sets custom progress title
     *
     * @param progressTitle
     */
    public void setTitle(final String progressTitle) {
        if (mTvProgressTitle.getVisibility() == View.GONE) {
            mTvProgressTitle.setVisibility(View.VISIBLE);
            mTvProgressTitle.setText(progressTitle);
        }
    }

    /**
     * Shows progress dialog with custom progress title
     *
     * @param progressTitle
     */
    public void show(final String progressTitle) {
        if (mTvProgressTitle.getVisibility() == View.GONE) {
            mTvProgressTitle.setVisibility(View.VISIBLE);
            mTvProgressTitle.setText(progressTitle);
        }
        CustomProgressDialog.this.show();
    }

    @Override
    public void show() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIvAnimation.setVisibility(View.VISIBLE);

            }
        }, 10);
        super.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

}
