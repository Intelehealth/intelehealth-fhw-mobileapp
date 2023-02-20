package org.intelehealth.app.models.rhemos_device;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class Bt extends BaseObservable {

    private long ts = 0;
    private double temp = 0.0d;

    public Bt() {
    }

    public Bt(long ts, double temp) {
        this.ts = ts;
        this.temp = temp;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Bindable
    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        if (this.temp != temp) {
            this.temp = temp;
         //   notifyPropertyChanged(BR.temp);
        }
    }

    public void reset() {
        Log.e("BT", "reset");
        temp = 0.0d;
        ts = 0L;
        notifyChange();
    }

    public boolean isEmptyData() {
        return temp == 0.0d || ts == 0L;
    }

    @NonNull
    @Override
    public String toString() {
        return "Bt{" +
                "ts=" + ts +
                ", temp=" + temp +
                '}';
    }
}
