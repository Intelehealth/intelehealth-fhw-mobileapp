package org.intelehealth.app.utilities;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import org.intelehealth.app.app.IntelehealthApplication;

public class DeviceUtils {
    public static int getOptimalBatchSize() {
        ActivityManager am =
                (ActivityManager) IntelehealthApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);

        if (am != null && am.isLowRamDevice()) {
            return 15;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            return 20;
        }

        return 50; // default
    }
}
