package org.intelehealth.abdm.common.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackBarUtil {

    /**
     * Show a short Snackbar message.
     *
     * @param view The view to find a parent from.
     * @param message The message to display in the Snackbar.
     */
    fun showShortSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Show a long SnackBar message.
     *
     * @param view The view to find a parent from.
     * @param message The message to display in the Snackbar.
     */
    fun showLongSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Show a Snack bar message with custom duration.
     *
     * @param view The view to find a parent from.
     * @param message The message to display in the Snackbar.
     * @param duration The length of time to display the message. Either Snackbar.LENGTH_SHORT, Snackbar.LENGTH_LONG, or Snackbar.LENGTH_INDEFINITE.
     */
    fun showSnackBar(view: View, message: String, duration: Int) {
        Snackbar.make(view, message, duration).show()
    }

    /**
     * Show a Snackbar with an action button.
     *
     * @param view The view to find a parent from.
     * @param message The message to display in the Snackbar.
     * @param actionText The text for the action button.
     * @param action The action to perform when the button is clicked.
     */
    fun showSnackBarWithAction(view: View, message: String, actionText: String, action: (View) -> Unit) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(actionText) { action(view) }
            .show()
    }

    /**
     * Show an indefinite Snackbar with an action button.
     *
     * @param view The view to find a parent from.
     * @param message The message to display in the Snackbar.
     * @param actionText The text for the action button.
     * @param action The action to perform when the button is clicked.
     */
    fun showIndefiniteSnackBarWithAction(view: View, message: String, actionText: String, action: (View) -> Unit) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(actionText) { action(view) }
            .show()
    }
}
