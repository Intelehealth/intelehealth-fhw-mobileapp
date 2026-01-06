package org.intelehealth.app.ui.baseline_survey.data

import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.Gson
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.ImagesPushDAO
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.NetworkConnection

class BaselineRepository(
    private val patientsDAO: PatientsDAO,
    private val sqlHelper: SQLiteOpenHelper
) {
    fun createPatientAttributes(baseline: Baseline, patientId: String): Boolean {
        var flag = false
        val generalAttributesList = bindGeneralBaselinePatientAttributes(
            baseline,
            patientId,
            patientsDAO
        )

        val medicalAttributesList = bindMedicalBaselinePatientAttributes(
            baseline,
            patientId,
            patientsDAO
        )
        Log.d("TAG", "createPatientAttributes: medicalAttributesList : "+Gson().toJson(medicalAttributesList))

        val otherAttributesList = bindOtherBaselinePatientAttributes(
            baseline,
            patientId,
            patientsDAO
        )

        flag = patientsDAO.patientAttributes(generalAttributesList)
        flag = patientsDAO.patientAttributes(medicalAttributesList)
        flag = patientsDAO.patientAttributes(otherAttributesList)
        patientsDAO.updatePatientSync(false, patientId)
        syncOnServer()
        return flag
    }

    fun getPatientAttributes(patientId: String): Baseline {
        return patientsDAO.getPatientAttributesForBaseline(patientId).let {
            PatientAttributeToBaseline(patientsDAO).getBaselineData(it)
        }
    }

    fun getPatientAge(patientId: String): Int {
        val dateOfBirth = patientsDAO.getPatientDob(patientId)
        return DateAndTimeUtils.getAgeInYears(dateOfBirth)
    }

    fun syncOnServer() {
        if (NetworkConnection.isOnline(IntelehealthApplication.getAppContext())) {
            val syncDAO = SyncDAO()
            val imagesPushDAO = ImagesPushDAO()
            syncDAO.pushDataApi()
            imagesPushDAO.patientProfileImagesPush()
        }
    }
}