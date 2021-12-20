package org.intelehealth.app.utilities;

import android.util.Log;

/**
 * Created by Prajwal Maruti Waingankar on 10-12-2021, 11:31
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class TimeRecordUtils {

    public synchronized static void record(String describe, long timemills){
        Log.e("Fu", timemills + "\t" + describe);
    }

}
