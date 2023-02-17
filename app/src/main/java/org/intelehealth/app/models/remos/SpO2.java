package org.intelehealth.app.models.remos;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class SpO2 extends BaseObservable {

    private long ts = 0L;
    private int value = 0;
    private int hr = 0;

    public SpO2() {
    }

    public SpO2(long ts, int value, int hr) {
        this.ts = ts;
        this.value = value;
        this.hr = hr;
    }

    @Bindable
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (this.value != value) {
            this.value = value;
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

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public void reset() {
        value = 0;
        hr = 0;
        ts = 0L;
        notifyChange();
    }

    @NonNull
    @Override
    public String toString() {
        return "SpO2{" +
                "value=" + value +
                ", hr=" + hr +
                ", ts=" + ts +
                '}';
    }

    public boolean isEmptyData() {
        return value == 0 || hr == 0 || ts == 0L;
    }
}
