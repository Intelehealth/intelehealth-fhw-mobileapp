package org.intelehealth.ncd.pagination

import android.os.SystemClock
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.DateAndTimeUtils

class PatientVisitPagingSource(
    private val dataSource: CategoryDataSource,
    private val query: String,
    private val patientPhoneNoAttribute: String
) : PagingSource<Int, PatientVisitDetails>() {

    companion object {
        private const val LOG_TAG = "Pooja"
    }

   /* override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientVisitDetails> {
        return try {
            val offset = params.key ?: 0
            val limit = params.loadSize

            val data = generalTabDao.getPagedPatientsSql(query, Constants.ENCOUNTER_VISIT_COMPLETE, Constants.IS_NCD_VISIT_ATTRIBUTE, Constants.OTHER_MEDICAL_HISTORY, Constants.PATIENT_PHONE, limit, offset)

            LoadResult.Page(
                data = data,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = if (data.size < limit) null else offset + limit
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }*/

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientVisitDetails> {
        val loadStart = SystemClock.elapsedRealtime()
        val offset = params.key ?: 0
        val limit = params.loadSize
        Log.d(
            LOG_TAG,
            "PatientVisitPagingSource.load START loadType=${params::class.java.simpleName} offset=$offset limit=$limit queryLen=${query.trim().length} thread=${Thread.currentThread().name} elapsedMs=$loadStart"
        )
        return try {
            val trimmedQuery = query.trim()

            // 1. Load main patient + latest visit
            val patients = dataSource.getPatientsAndVisitsPage(limit, offset, patientPhoneNoAttribute)
            if (offset != 0) {
                delay(0)
            }

            val visitIds = patients.mapNotNull { it.visitId }

            val tRx = SystemClock.elapsedRealtime()
            val prescriptions = dataSource.getPrescriptionExistsBatch(Constants.ENCOUNTER_VISIT_COMPLETE, visitIds)
                .associateBy { it.visitId }
            val dtRx = SystemClock.elapsedRealtime() - tRx

            val tAttr = SystemClock.elapsedRealtime()
            val attributes = dataSource.getVisitAttributesBatch(visitIds)
                .groupBy { it.visitId }
            val dtAttr = SystemClock.elapsedRealtime() - tAttr

            val tMap = SystemClock.elapsedRealtime()
            patients.forEach { patient ->
                patient.isPrescriptionExist = prescriptions[patient.visitId]?.prescriptionExists
                val attrList = attributes[patient.visitId].orEmpty()
                patient.isNcdVisit =
                    attrList.find { it.typeUuid == Constants.IS_NCD_VISIT_ATTRIBUTE }?.value ?: ""
                patient.visitSpeciality =
                    attrList.find { it.typeUuid == Constants.SPECIALITY }?.value ?: ""

                patient.startDate = patient.startDate
                /* patient.startDate = patient.startDate?.let { rawDate ->
                    formatVisitDateSafely(rawDate)
                }*/
            }
            val dtMap = SystemClock.elapsedRealtime() - tMap

            // 5. Apply search query filter (keep logic unchanged otherwise)
            val filteredPatients = if (query.isBlank()) {
                patients
            } else {
                patients.filter {
                    val name = "${it.firstName} ${it.middleName.orEmpty()} ${it.lastName.orEmpty()}".trim()
                    val openmrsId = it.openmrsId ?: ""
                    val phone = it.patientPhoneNumber ?: ""
                    name.contains(query, ignoreCase = true) || openmrsId.contains(query, ignoreCase = true) || phone.contains(query, ignoreCase = true)
                }
            }

            // 6. Compute next/prev keys
            val nextKey = if (patients.size < limit) null else offset + limit
            val prevKey = if (offset == 0) null else offset - limit

            Log.d(
                LOG_TAG,
                "PatientVisitPagingSource.load END browsePath rows=${patients.size} getPatients+visits +${tRx }ms rxBatch +${dtRx}ms attrBatch +${dtAttr}ms map +${dtMap}ms totalLoad +${SystemClock.elapsedRealtime() - loadStart}ms"
            )
            LoadResult.Page(
                data = filteredPatients,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (e: Exception) {
            Log.e(LOG_TAG, "PatientVisitPagingSource.load ERROR +${SystemClock.elapsedRealtime() - loadStart}ms", e)
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, PatientVisitDetails>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(state.config.pageSize)
        }
    }

    // 🟩 NEW
    private fun formatVisitDateSafely(rawDate: String): String {
        val formatsToTry = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (format in formatsToTry) {
            try {
                return DateAndTimeUtils.formatStartVisitDate(rawDate, format, "dd MMM 'at' hh:mm a")
                    .toString()
            } catch (_: Exception) {
                // try next format
            }
        }

        return rawDate // fallback if no format matched
    }

}
