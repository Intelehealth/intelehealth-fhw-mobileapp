package org.intelehealth.app.utilities

import android.content.Context
import android.widget.Toast

object ToastUtil {

    private var toast: Toast? = null
    @JvmStatic
    fun showLongToast(context: Context, message: String) {
        // Cancel any previous toast before showing a new one
        cancelToast()
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast?.show()
    }

    @JvmStatic
    fun showShortToast(context: Context, message: String) {
        // Cancel any previous toast before showing a new one
        cancelToast()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }


    private fun cancelToast() {
        toast?.cancel()
        toast = null
    }
}
