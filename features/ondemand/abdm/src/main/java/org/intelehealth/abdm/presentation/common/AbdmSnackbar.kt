package org.intelehealth.abdm.presentation.common

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.intelehealth.abdm.R

/**
 * Tone-coloured snackbar shared across abdm screens: green for success, red for failure
 * (ports legacy SnackBarUtils colouring). Reusable — wiring another screen (e.g. Create) is just a
 * single call on its root view, e.g. `binding.main.showAbdmSnackbar(message, isSuccess)`.
 */
internal fun View.showAbdmSnackbar(message: String, isSuccess: Boolean) {
    val colorRes = if (isSuccess) R.color.abdm_snackbar_success else R.color.abdm_snackbar_error
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).apply {
        view.setBackgroundColor(ContextCompat.getColor(context, colorRes))
    }.show()
}
