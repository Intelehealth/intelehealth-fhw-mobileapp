package org.intelehealth.app.widget.materialprogressbar.rhemos_widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Locale;

public class CountDownTextView extends AppCompatTextView {

    private CountDownTimer mCountDownTimer;
    private long totalSecs;
    private long interval;
    private boolean isRunning;
    private OnCountDownFinishCallback mOnCountDownFinishCallback;

    public CountDownTextView(@NonNull Context context) {
        super(context);
    }

    public CountDownTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CountDownTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setVisibility(INVISIBLE);
    }

    public void setCountDownParams(long totalSecs) {
        setCountDownParams(totalSecs, 1);
    }

    public void setCountDownParams(long totalSecs, long interval) {
        this.totalSecs = totalSecs;
        this.interval = interval;
        formatText(totalSecs);
    }

    public void setOnCountDownFinishCallback(OnCountDownFinishCallback callback) {
        this.mOnCountDownFinishCallback = callback;
    }

    public void start() {
        if (!isRunning) {
            setVisibility(VISIBLE);
            mCountDownTimer = new CountDownTimer(totalSecs * 1000L
                    , interval * 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    formatText(millisUntilFinished / 1000L);
                }

                @Override
                public void onFinish() {
                    cancel();
                    if (mOnCountDownFinishCallback != null) {
                        mOnCountDownFinishCallback.onCountDownFinish();
                    }
                }
            };
            mCountDownTimer.start();
            isRunning = true;
        }
    }

    public void cancel() {
        if (isRunning) {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
                isRunning = false;
            }
            setVisibility(INVISIBLE);
        }
    }

    private void formatText(long count) {
        setText(String.format(Locale.getDefault(), "%d sec", count % 60));
    }

    public interface OnCountDownFinishCallback {

        void onCountDownFinish();
    }
}
