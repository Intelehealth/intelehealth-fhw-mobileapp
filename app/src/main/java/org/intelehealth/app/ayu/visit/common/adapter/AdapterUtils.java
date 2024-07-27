package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import android.os.Handler;
import org.intelehealth.app.utilities.CustomLog;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.github.ybq.android.spinkit.style.ThreeBounce;

import org.intelehealth.app.R;

public class AdapterUtils {
    private static final String TAG = AdapterUtils.class.getSimpleName();

    public interface OnFinishActionListener {
        void onFinish();

        //void onStart();
    }

    public static void buttonProgressAnimation(Context context, Button button, boolean isSubmitType, OnFinishActionListener onFinishActionListener) {
        ThreeBounce mWaveDrawable = new ThreeBounce();
        mWaveDrawable.setBounds(0, 0, 100, 100);
        //noinspection deprecation
        mWaveDrawable.setColor(ContextCompat.getColor(context,R.color.gray_4));
        button.setCompoundDrawables(null, null, mWaveDrawable, null);
        button.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg);
        button.setTextColor(ContextCompat.getColor(context,R.color.gray_4));
        button.setEnabled(false);
        mWaveDrawable.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
                button.setTextColor(ContextCompat.getColor(context,R.color.white));
                /*if (!isSubmitType)
                    button.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                else*/
                button.setBackgroundResource(R.drawable.ui2_common_primary_bg);
                //button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, isSubmitType ? R.drawable.ic_baseline_check_18_white : 0, 0);
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
                onFinishActionListener.onFinish();
            }
        }, 400);

    }

    /**
     * @param context
     * @param button
     * @param onFinishActionListener
     */
    public static void buttonProgressAnimationAndChecked(Context context, Button button, OnFinishActionListener onFinishActionListener) {
        ThreeBounce mWaveDrawable = new ThreeBounce();
        mWaveDrawable.setBounds(0, 0, 100, 100);
        mWaveDrawable.setColor(ContextCompat.getColor(context,R.color.gray_4));
        button.setCompoundDrawables(null, null, mWaveDrawable, null);
        button.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg);
        button.setTextColor(ContextCompat.getColor(context,R.color.gray_4));
        button.setEnabled(false);
        mWaveDrawable.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
                button.setTextColor(ContextCompat.getColor(context,R.color.white));
                button.setBackgroundResource(R.drawable.ui2_common_primary_bg);
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
                onFinishActionListener.onFinish();
            }
        }, 400);

    }

    public static void setToDefault(Button button) {
        CustomLog.v(TAG, "setToDefault");
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        button.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
        button.setClickable(true);
        button.setEnabled(true);
    }

    public static void setToDisable(Button button) {
        CustomLog.v(TAG, "setToDisable");
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        button.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg_1);
        button.setClickable(false);
        button.setEnabled(false);
    }
}
