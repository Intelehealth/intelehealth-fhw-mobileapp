package org.intelehealth.abdm.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

public class WindowUtils {
    public static void setStatusBarColor(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        activity.getWindow().setStatusBarColor(Color.WHITE);
    }
}
