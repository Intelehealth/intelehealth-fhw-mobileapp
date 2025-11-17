package org.intelehealth.ncd.pagination

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import kotlinx.coroutines.delay
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.DateAndTimeUtils

class PatientVisitPagingSource(
    private val dataSource: CategoryDataSource,
    private val visitEncounterNoteAttr: String,
    private val query: String
) : PagingSource<Int, PatientVisitDetails>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientVisitDetails> {
        return try {
            val offset = params.key ?: 0
            val limit = params.loadSize

            // 1. Load main patient + latest visit
            val patients = dataSource.getPatientsAndVisitsPage(limit, offset)
            if (offset != 0) {
                delay(500)
            }

            val visitIds = patients.mapNotNull { it.visitId }

            // 2. Load prescriptionExists in batch
            val prescriptions = dataSource.getPrescriptionExistsBatch(visitEncounterNoteAttr, visitIds)
                .associateBy { it.visitId }

            // 3. Visit attributes in batch
            val attributes = dataSource.getVisitAttributesBatch(visitIds)
                .groupBy { it.visitId }

            // 4. Map extra fields to patients
            patients.forEach { patient ->
                patient.isPrescriptionExist = prescriptions[patient.visitId]?.prescriptionExists
                val attrList = attributes[patient.visitId].orEmpty()
                patient.isNcdVisit =
                    attrList.find { it.typeUuid == Constants.IS_NCD_VISIT_ATTRIBUTE }?.value ?: ""
                patient.visitSpeciality =
                    attrList.find { it.typeUuid == Constants.SPECIALITY }?.value ?: ""

                patient.startDate = patient.startDate?.let { rawDate ->
                    formatVisitDateSafely(rawDate)
                }
            }

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

            LoadResult.Page(
                data = filteredPatients,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (e: Exception) {
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
