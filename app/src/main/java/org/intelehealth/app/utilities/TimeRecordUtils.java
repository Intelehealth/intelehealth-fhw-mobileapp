package org.intelehealth.app.utilities;

import android.util.Log;

public class TimeRecordUtils {
    public synchronized static void record(String describe, long timemills){
        Log.e("Fu", timemills + "\t" + describe);
    }
}
