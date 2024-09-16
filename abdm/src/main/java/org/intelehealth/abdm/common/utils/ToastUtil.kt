package org.intelehealth.abdm.common.utils

import android.content.Context
import android.widget.Toast

object ToastUtil {

    /**
     * Show a short Toast message.
     *
     * @param context The context to use. Usually your Application or Activity object.
     * @param message The message to display in the Toast.
     */
    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a long Toast message.
     *
     * @param context The context to use. Usually your Application or Activity object.
     * @param message The message to display in the Toast.
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show a Toast message with custom duration.
     *
     * @param context The context to use. Usually your Application or Activity object.
     * @param message The message to display in the Toast.
     * @param duration The length of time to display the message. Either Toast.LENGTH_SHORT or Toast.LENGTH_LONG.
     */
    fun showToast(context: Context, message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }
}
