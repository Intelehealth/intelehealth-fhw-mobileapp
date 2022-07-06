package org.intelehealth.ezazi.partogram;

import android.os.CountDownTimer;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PartogramCounterJob extends CountDownTimer {

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public PartogramCounterJob(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String currentTime = dtf.format(new Date());
        Log.v("PartogramCounterJob", "onTick - " + currentTime);
        CardGenerationEngine.scanForNewCardEligibility();
    }

    @Override
    public void onFinish() {
        Log.v("PartogramCounterJob", "onFinish");
    }
}
