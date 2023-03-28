package org.intelehealth.app.models.rhemos_device;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class Bg extends BaseObservable {

    private long ts = 0;
    private double value = 0.0d;

    public Bg() {
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Bindable
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (this.value != value) {
            this.value = value;
        }
    }

    public void reset() {
        value = 0.0d;
        ts = 0L;
        notifyChange();
    }

    public boolean isEmptyData() {
        return value == 0.0d || ts == 0L;
    }

    @NonNull
    @Override
    public String toString() {
        return "Bg{" +
                "ts=" + ts +
                ", value=" + value +
                '}';
    }
}
