package org.intelehealth.app.models.rhemos_device;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
public class Bp extends BaseObservable {

    private long ts;
    private int sbp;
    private int dbp;
    private int hr;

    public Bp() {
    }

    public Bp(long ts, int sbp, int dbp, int hr) {
        this.ts = ts;
        this.sbp = sbp;
        this.dbp = dbp;
        this.hr = hr;
    }

    public int getSbp() {
        return sbp;
    }

    public void setSbp(int sbp) {
        this.sbp = sbp;
        notifyChange();
    }

    public int getDbp() {
        return dbp;
    }

    public void setDbp(int dbp) {
        this.dbp = dbp;
        notifyChange();
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
        notifyChange();
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public void reset() {
        Log.e("BP", "reset");
        sbp = 0;
        dbp = 0;
        hr = 0;
        ts = 0L;
        notifyChange();
    }

    public boolean isEmptyData() {
        return sbp == 0 || dbp == 0 || hr == 0 || ts == 0L;
    }

    @NonNull
    @Override
    public String toString() {
        return "Bp{" +
                "sbp=" + sbp +
                ", dbp=" + dbp +
                ", hr=" + hr +
                '}';
    }
}

