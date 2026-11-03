package org.intelehealth.app.ayu.visit.common

import android.app.Activity
import android.content.Context
import android.util.Log
import org.intelehealth.app.R
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.utilities.DialogUtils

class DiscardIncompleteVisitUtil {
    fun showConfirmationDialog(context: Context, visitUuid: String) {
        Log.d("TAG", "showConfirmationDialog: visitUuid : $visitUuid")

        val dialogUtils = DialogUtils()
        dialogUtils.showCommonDialog(
            context,
            R.drawable.fingerprint_dialog_error,
            context.getString(R.string.confirm_discard_changes_title),
            context.getString(R.string.confirm_discard_changes_content_on_sync),
            false,
            context.getString(R.string.confirm_continue_changes_button_dialog),
            context.getString(R.string.confirm_discard_changes_button_dialog)
        ) { action ->
            if (action == DialogUtils.CustomDialogListener.NEGATIVE_CLICK) {
                VisitsDAO().deleteAllDataForOngoingIncompleteVisit(visitUuid)
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }

}