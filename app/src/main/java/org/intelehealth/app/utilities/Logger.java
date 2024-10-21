package org.intelehealth.app.utilities;

import com.google.firebase.crashlytics.FirebaseCrashlytics;


/**
 * Updated by mahiti on 24/02/16.
 */
public class Logger {
    /**
     * Default constructors
     */
    private Logger() {
        // This Constructor is not Used
    }

    /**
     * function to use in catch block....
     *
     * @param tag
     * @param desc
     * @param e
     */
    public static void logE(String tag, String desc, Exception e) {
        CustomLog.e(tag, desc, e);
        FirebaseCrashlytics.getInstance().recordException(e);
    }

    /**
     * function to use for debug and showing in console..
     *
     * @param tag
     * @param desc
     */
    public static void logD(String tag, String desc) {
        CustomLog.d(tag, "" + desc);
    }

    /**
     * function to use for debug and showing in console....
     *
     * @param tag
     * @param desc
     */
    public static void logV(String tag, String desc) {
        CustomLog.v(tag, "" + desc);
    }

}
