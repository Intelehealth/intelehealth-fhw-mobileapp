package org.intelehealth.ncd.data.category

import android.os.SystemClock
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.pagination.PatientVisitPagingSource
import org.intelehealth.ncd.pagination.PatientVisitPagingSourceNew
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.room.dao.GeneralTabDao
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CategoryRepository(private val dataSource: CategoryDataSource) {
    private var lastQuery: String? = null
    private var lastPagerFlow: Flow<PagingData<PatientVisitDetails>>? = null

    companion object {
        private const val LOG_TAG = "Pooja"
    }

    suspend fun getPatientVisitDetails(age: Int, attributeTypeUuid: String, visitNoteEncounterUuid: String): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawData(age, attributeTypeUuid, visitNoteEncounterUuid)
        val result = buildPatientVisitDetails(rawDataList)
        result.forEachIndexed { index, item ->
        }
        return result
    }

    private fun buildPatientVisitDetails(
        rawDataList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return rawDataList.map { data ->
            val formattedStartDate = data.startDate?.let {
                DateAndTimeUtils.formatStartVisitDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                    "dd MMM 'at' hh:mm a"
                )
            }

            val isFollowupFromObs = data.chiefComplaintData?.let {
                checkFollowUpFlag(it)}
            val isFollowUpGivenToPatient = data.chiefComplaintData?.let { checkIfFollowupDateGivenToPatient(it)}
            // Check follow-up flags
            var isHypertensionFollowupGiven: Boolean? = null
            var isAnemiaFollowupGiven: Boolean? = null
            var isDiabetesFollowupGiven: Boolean? = null
            var isHypertensionFollowupTodayOrLater: Boolean? = null
            var isAnemiaFollowupTodayOrLater: Boolean? = null
            var isDiabetesFollowupTodayOrLater: Boolean? = null

            data.chiefComplaintData?.let { complaintData ->
                val tempModel = PatientVisitDetails()
                setFollowUpFlags(complaintData, tempModel)
                isHypertensionFollowupGiven = tempModel.isHypertensionFollowupGiven
                isAnemiaFollowupGiven = tempModel.isAnemiaFollowupGiven
                isDiabetesFollowupGiven = tempModel.isDiabetesFollowupGiven
            }


            val tempModel = PatientVisitDetails()
            setFollowUpFlags(data.chiefComplaintData, tempModel)
            isHypertensionFollowupGiven = tempModel.isHypertensionFollowupGiven
            isAnemiaFollowupGiven = tempModel.isAnemiaFollowupGiven
            isDiabetesFollowupGiven = tempModel.isDiabetesFollowupGiven
            isHypertensionFollowupTodayOrLater = tempModel.isHypertensionFollowupTodayOrLater
            isAnemiaFollowupTodayOrLater = tempModel.isAnemiaFollowupTodayOrLater
            isDiabetesFollowupTodayOrLater = tempModel.isDiabetesFollowupTodayOrLater


            PatientVisitDetails(
                patientId = data.patientId,
                visitId = data.visitId,
                firstName = data.firstName,
                middleName = data.middleName,
                lastName = data.lastName,
                dateOfBirth = data.dateOfBirth,
                gender = data.gender,
                openmrsId = data.openmrsId,
                value = data.value,
                personAttributeTypeUuid = data.personAttributeTypeUuid,
                startDate = formattedStartDate,
                patientPhoto = data.patientPhoto,
                isPrescriptionExist = data.isPrescriptionExist,
                isNcdVisit = data.isNcdVisit,
                chiefComplaintData = data.chiefComplaintData,
                followUpFromProtocol = isFollowupFromObs,
                visitEndDate = data.visitEndDate,
                isFollowUpDateGivenToPatient = isFollowUpGivenToPatient,
                isHypertensionFollowupGiven = isHypertensionFollowupGiven,
                isAnemiaFollowupGiven = isAnemiaFollowupGiven,
                isDiabetesFollowupGiven = isDiabetesFollowupGiven,
                age = data.age,
                patientPhoneNumber = data.patientPhoneNumber,
                isHypertensionFollowupTodayOrLater = isHypertensionFollowupTodayOrLater,
                isAnemiaFollowupTodayOrLater = isAnemiaFollowupTodayOrLater,
                isDiabetesFollowupTodayOrLater = isDiabetesFollowupTodayOrLater
                )
        }
    }
    suspend fun getPatientVisitDetailsForFollowup(age: Int, attributeTypeUuid: String, visitNoteEncounterUuid: String, patientUuid: String): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawDataForFollowup(age, attributeTypeUuid, visitNoteEncounterUuid, patientUuid)
        val result = buildPatientVisitDetails(rawDataList)
        return result
    }

    fun getPagedPatients(query: String,generalTabDao: GeneralTabDao): Flow<PagingData<PatientVisitDetails>> {
        val safeQuery = query.trim()
        val t0 = SystemClock.elapsedRealtime()

        if (safeQuery == lastQuery && lastPagerFlow != null) {
            Log.d(LOG_TAG, "CategoryRepository.getPagedPatients CACHE_HIT queryLen=${safeQuery.length} +${SystemClock.elapsedRealtime() - t0}ms")
            return lastPagerFlow!!
        }

        lastQuery = safeQuery

        Log.d(
            LOG_TAG,
            "CategoryRepository.getPagedPatients NEW_PAGER queryLen=${safeQuery.length} pageSize=10 initialLoadSize=20 elapsedMs=$t0"
        )
        lastPagerFlow = Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PatientVisitPagingSource(
                    dataSource,
                    safeQuery,
                    Constants.PATIENT_PHONE
                )
            }
        ).flow

        Log.d(LOG_TAG, "CategoryRepository.getPagedPatients NEW_PAGER built +${SystemClock.elapsedRealtime() - t0}ms thread=${Thread.currentThread().name}")
        return lastPagerFlow!!
       /* return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PatientVisitPagingSource(dataSource,query, Constants.PATIENT_PHONE) }
        ).flow*/
    }


    private fun extractComplaintBlocks(data: String): List<String> {
        val normalized = data.replace("\n", "")
            .replace("\\u003c", "<")
            .replace("\\u003e", ">")

        val splitter = "►<b>".toRegex()

        return normalized.split(splitter)
            .filter { it.isNotBlank() }
            .map { "►<b>$it" }
    }


    private fun checkFollowUpFlag(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false

        return try {
            val blocks = extractComplaintBlocks(chiefComplaintData)
            val today = Calendar.getInstance().time
            val inputFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH)

            blocks.any { block ->
                val regex = "Next Follow Up Date -\\s*([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})".toRegex()
                val match = regex.find(block)
                val dateStr = match?.groupValues?.getOrNull(1) ?: return@any false
                val followUpDate = inputFormat.parse(dateStr) ?: return@any false
                !today.before(followUpDate) // today >= followUpDate
            }
        } catch (e: Exception) {
            false
        }
    }
    private fun checkIfFollowupDateGivenToPatient(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false

        val blocks = extractComplaintBlocks(chiefComplaintData)
        val regex = "Next Follow Up Date -\\s*([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})".toRegex()

        return blocks.any { regex.containsMatchIn(it) }
    }
    private fun setFollowUpFlags(
        chiefComplaintData: String?,
        model: PatientVisitDetails
    ) {
        if (chiefComplaintData.isNullOrBlank()) return

        val blocks = extractComplaintBlocks(chiefComplaintData)

        val complaintRegex = "<b>(.*?)</b>".toRegex()
        val followUpDateRegex =
            "Next Follow Up Date -\\s*([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})".toRegex()

        val inputFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH)
        val today = Calendar.getInstance().time

        blocks.forEach { block ->

            val complaint = complaintRegex.find(block)?.groupValues?.getOrNull(1)
                ?.trim()?.lowercase() ?: return@forEach

            val dateStr = followUpDateRegex.find(block)?.groupValues?.getOrNull(1)

            // Original flag: does follow-up exist at all
            val followUpGiven = dateStr != null

            // New flag: follow-up today or earlier
            val followUpTodayOrLater = dateStr?.let { !today.before(inputFormat.parse(it)) } ?: false

            when (complaint) {

                "hypertension screening", "hypertension followup" -> {
                    model.isHypertensionFollowupGiven = followUpGiven
                    model.isHypertensionFollowupTodayOrLater = followUpTodayOrLater
                }

                "anemia screening", "anemia followup" -> {
                    model.isAnemiaFollowupGiven = followUpGiven
                    model.isAnemiaFollowupTodayOrLater = followUpTodayOrLater
                }

                "diabetes screening", "diabetes followup" -> {
                    model.isDiabetesFollowupGiven = followUpGiven
                    model.isDiabetesFollowupTodayOrLater = followUpTodayOrLater
                }
            }
        }
    }
    private fun checkFollowUpFlagForSingleChiefComplaint(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false

        return try {
            // 1. Extract the date after "Next Follow Up Date - "
            val regex = "Next Follow Up Date - ([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})".toRegex()
            val match = regex.find(chiefComplaintData) ?: return false
            val dateStr = match.groupValues[1] // e.g., "30/Oct/2025"
            val inputFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH)
            val followUpDate = inputFormat.parse(dateStr) ?: return false

            // 3. Check if today's date is equal to or after follow-up date
            val today = Calendar.getInstance().time
            !today.before(followUpDate) // true if today >= follow-up date, false otherwise
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun checkIfFollowupDateGivenToPatientForSingleChiefComplaint(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false
        val regex = "Next Follow Up Date -\\s*(.+)".toRegex()
        val match = regex.find(chiefComplaintData)
        val dateStr = match?.groupValues?.getOrNull(1)
        return !dateStr.isNullOrBlank()
    }
    private fun setFollowUpFlagsForSingleChiefComplaint(chiefComplaintData: String?, model: PatientVisitDetails, data: PatientVisitDetails) {
        if (chiefComplaintData.isNullOrBlank()) return

        // 1. Extract the complaint name from <b>...</b>
        val complaintRegex = "<b>(.*?)</b>".toRegex()
        val complaintMatch = complaintRegex.find(chiefComplaintData)
        val complaintNameRaw = complaintMatch?.groupValues?.getOrNull(1)?.trim() ?: return
        val complaintName = complaintNameRaw
            .lowercase()
            .replace("followup", "follow_up")
            .replace("[^a-z0-9]+".toRegex(), "_")
            .trim('_')

        // 2. Check if a "Next Follow Up Date" exists
        val followUpRegex = "Next Follow Up Date -\\s*(.+)".toRegex()
        val match = followUpRegex.find(chiefComplaintData)
        val isFollowUpGiven = !match?.groupValues?.getOrNull(1).isNullOrBlank()

        // 3. Set the appropriate flag using constants
        when (complaintName) {
            Constants.HYPERTENSION_SCREENING, Constants.HYPERTENSION_FOLLOW_UP -> model.isHypertensionFollowupGiven = isFollowUpGiven
            Constants.ANEMIA_SCREENING, Constants.ANEMIA_FOLLOW_UP -> model.isAnemiaFollowupGiven = isFollowUpGiven
            Constants.DIABETES_SCREENING, Constants.DIABETES_FOLLOW_UP -> model.isDiabetesFollowupGiven = isFollowUpGiven
        }
    }
}