package org.intelehealth.app.syncModule

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.database.dao.PatientsDAO.getAllPatientsToSyncMpiId
import org.intelehealth.app.models.dto.ResponseDTO
import org.intelehealth.app.utilities.SessionManager
import retrofit2.Call


open class MpiIdSyncHelper(context: Context) {
    var scope = CoroutineScope(Dispatchers.IO)
    var sessionManager = SessionManager(context)
    var patientsDAO = PatientsDAO()
    open fun sync() {
        scope.launch {
            val patients = getAllPatientsToSyncMpiId()
            for(patient in patients){
                //delay(TimeUnit.MINUTES.toMillis(3))
                findRemotePatientObservable(
                    patient.firstname,
                    patient.lastname,
                    patient.gender,
                    patient.phonenumber,
                    patient.dateofbirth?:""
                ).execute().body()?.let { responseDto ->
                    responseDto.data?.let { data ->
                        data.patientDTO?.let { remotePatients ->
                            patientsDAO.insertPatients(remotePatients)
                            remotePatients.map {
                                if(!it.mpiId.isNullOrEmpty()){
                                    patientsDAO.updateMpiIdSyncStatus(it.uuid,true)
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private fun findRemotePatientObservable(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String,
    ): Call<ResponseDTO> {
        val urlBuilder = StringBuilder()
            .append(BuildConfig.SERVER_URL)
            .append("/EMR-Middleware/webapi/pull/pulldata/search?firstname=")
            .append(firstName).append("&gender=").append(gender)
            .append("&pageNo=").append(1).append("&limit=").append(100)

        lastName.ifEmpty { null }?.let { urlBuilder.append("&lastname=").append(lastName) }
        //phone.ifEmpty { null }?.let { urlBuilder.append("&telecom=").append(phone) }
        dob.ifEmpty { null }?.let { urlBuilder.append("&dob=").append(dob) }

        val url = urlBuilder.toString()

        return AppConstants.apiInterface.RESPONSE_DTO_CALL_FOR_FILTER(
            url,
            "Basic " + sessionManager.encoded
        )
    }
}