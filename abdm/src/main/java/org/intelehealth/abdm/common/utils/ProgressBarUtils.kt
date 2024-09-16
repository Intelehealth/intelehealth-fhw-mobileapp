package org.intelehealth.abdm.common.utils

import android.content.Context
import com.tashila.pleasewait.PleaseWaitDialog

class ProgressBarUtils(context: Context) {
    private var progressDialog: PleaseWaitDialog

    init {
        progressDialog = PleaseWaitDialog(context = context)
    }

    fun showCircularProgressbar(title: String = "", message: String = "") {
        if (!progressDialog.isAdded && !progressDialog.isVisible) {
            if (title.isBlank()) {
                progressDialog.setTitle(title)
            }
            if (message.isBlank()) {
                progressDialog.setMessage(message)
            }
            progressDialog.show()
        }
    }

    fun showLinearProgressbar(title: String = "", message: String = "") {
        if (!progressDialog.isAdded && !progressDialog.isVisible) {
            if (title.isBlank()) {
                progressDialog.setTitle(title)
            }
            if (message.isBlank()) {
                progressDialog.setMessage(message)
            }
            progressDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR)

            progressDialog.show()
        }
    }

    fun dismissProgressBar() {
        progressDialog.dismiss()
    }
}