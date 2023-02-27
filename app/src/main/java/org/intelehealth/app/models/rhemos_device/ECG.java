package org.intelehealth.app.models.rhemos_device;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;

public class ECG extends BaseObservable implements Serializable {

    private long ts;
    private long duration;
    private int r2r;//R-R interval
    private int hr;//Heart rate
    private int hrv;//Heart rate variability
    private int mood;
    private int rr;//Respiratory rate
    private int ha;//Heart age
    private int hb;// Heart beat
    private int rhr;//Robust heart rate
    private int stress;//Stress
    private String wave;

    public ECG() {
    }

    public ECG(long ts, long duration, int r2r, int hr, int hrv, int mood, int rr, String wave) {
        this.ts = ts;
        this.duration = duration;
        this.r2r = r2r;
        this.hr = hr;
        this.hrv = hrv;
        this.mood = mood;
        this.rr = rr;
        this.wave = wave;
    }

    @Bindable
    public int getR2r() {
        return r2r;
    }

    public void setR2r(int r2r) {
        if (this.r2r != r2r) {
            this.r2r = r2r;
        }
    }

    @Bindable
    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        if (this.hr != hr) {
            this.hr = hr;
        }
    }

    @Bindable
    public int getHrv() {
        return hrv;
    }

    public void setHrv(int hrv) {
        if (this.hrv != hrv) {
            this.hrv = hrv;
        }
    }

    @Bindable
    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        if (this.mood != mood) {
            this.mood = mood;
        }
    }

    @Bindable
    public int getRr() {
        return rr;
    }

    public void setRr(int rr) {
        if (this.rr != rr) {
            this.rr = rr;
        }
    }

    @Bindable
    public int getHa() {
        return ha;
    }

    public void setHa(int ha) {
        if (this.ha != ha) {
            this.ha = ha;
        }
    }

    @Bindable
    public int getHb() {
        return hb;
    }

    public void setHb(int hb) {
        if (this.hb != hb) {
            this.hb = hb;
        }
    }

    @Bindable
    public int getRhr() {
        return rhr;
    }

    public void setRhr(int rhr) {
        if (this.rhr != rhr) {
            this.rhr = rhr;
        }
    }

    @Bindable
    public int getStress() {
        return stress;
    }

    public void setStress(int stress) {
        if (this.stress != stress) {
            this.stress = stress;
        }
    }

    @Bindable
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        if (this.duration != duration) {
            this.duration = duration;
        }
    }

    public void setWave(String wave) {
        this.wave = wave;
    }

    public String getWave() {
        return wave;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public void reset() {
        r2r = 0;
        hr = 0;
        hrv = 0;
        mood = 0;
        rr = 0;
        ha = 0;
        hb = 0;
        rhr = 0;
        stress = 0;
        duration = 0L;
        ts = 0L;
        wave = "";
        notifyChange();
    }

    public boolean isEmptyData() {
        return r2r == 0 ||
                hr == 0 ||
                hrv == 0 ||
                mood == 0 ||
                rr == 0 ||
                duration == 0L ||
                ts == 0L ||
                TextUtils.isEmpty(wave);
    }

}

