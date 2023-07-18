package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import android.os.Handler;
import android.widget.Button;

import com.github.ybq.android.spinkit.style.ThreeBounce;

import org.intelehealth.app.R;

public class AdapterUtils {
    public interface OnFinishActionListener {
        void onFinish();

        //void onStart();
    }

    public static void buttonProgressAnimation(Context context, Button button, boolean isSubmitType, OnFinishActionListener onFinishActionListener) {
        ThreeBounce mWaveDrawable = new ThreeBounce();
        mWaveDrawable.setBounds(0, 0, 100, 100);
        //noinspection deprecation
        mWaveDrawable.setColor(context.getResources().getColor(R.color.gray_4));
        button.setCompoundDrawables(null, null, mWaveDrawable, null);
        button.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg);
        button.setTextColor(context.getColor(R.color.gray_4));
        button.setEnabled(false);
        mWaveDrawable.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
                button.setTextColor(context.getColor(R.color.white));
                if (!isSubmitType)
                    button.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                else
                    button.setBackgroundResource(R.drawable.ui2_common_primary_bg);
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, isSubmitType ? R.drawable.ic_baseline_check_18_white : 0, 0);
                onFinishActionListener.onFinish();
            }
        }, 1000);

    }
}
