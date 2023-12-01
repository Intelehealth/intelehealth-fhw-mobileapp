package org.intelehealth.ezazi.ui.webviewreader;

import android.util.Log;

/**
 * Created by Vaghela Mithun R. on 01-12-2023 - 17:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class Lt {
    private static String myTag = "war_test";

    private Lt() {
    }

    static void setTag(String tag) {
        myTag = tag;
    }

    public static void d(String msg) {
        // Uncomment line below to turn on debug output
        Log.d(myTag, msg == null ? "(null)" : msg);
    }

    public static void df(String msg) {
        // Forced output, do not comment out - for exceptions etc.
        Log.d(myTag, msg == null ? "(null)" : msg);
    }
}
