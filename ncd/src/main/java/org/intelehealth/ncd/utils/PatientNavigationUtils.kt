package org.intelehealth.ncd.utils

import android.content.Context
import android.content.Intent
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.PatientVisitDetails

object PatientNavigationUtils {
    fun openPatientDetail(context: Context, patient: PatientVisitDetails) {
        try {
            val intent = Intent(
                context,
                Class.forName("org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2")
            ).apply {
                putExtra(Constants.INTENT_PATIENT_UUID, patient.patientId)
                putExtra(Constants.INTENT_PATIENT_NAME, "${patient.firstName} ${patient.lastName}")
                putExtra(Constants.INTENT_PATIENT_STATUS, "returning")
                putExtra(Constants.INTENT_PATIENT_TAG, "search")
                putExtra(Constants.INTENT_HAS_PRESCRIPTION, "false")
            }
            context.startActivity(intent)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }
}
