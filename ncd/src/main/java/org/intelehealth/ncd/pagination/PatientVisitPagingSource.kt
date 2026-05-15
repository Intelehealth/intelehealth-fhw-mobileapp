package org.intelehealth.ncd.pagination

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitPagingSource(
    private val dataSource: CategoryDataSource,
    private val query: String,
    private val patientPhoneNoAttribute: String
) : PagingSource<Int, PatientVisitDetails>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientVisitDetails> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val trimmedQuery = query.trim()

            val patients = dataSource.getPatientsAndVisitsPage(limit, offset, patientPhoneNoAttribute)
            if (offset != 0) {
                delay(0)
            }

            val visitIds = patients.mapNotNull { it.visitId }

            val prescriptions = dataSource.getPrescriptionExistsBatch(Constants.ENCOUNTER_VISIT_COMPLETE, visitIds)
                .associateBy { it.visitId }

            val attributes = dataSource.getVisitAttributesBatch(visitIds)
                .groupBy { it.visitId }

            patients.forEach { patient ->
                patient.isPrescriptionExist = prescriptions[patient.visitId]?.prescriptionExists
                val attrList = attributes[patient.visitId].orEmpty()
                patient.isNcdVisit =
                    attrList.find { it.typeUuid == Constants.IS_NCD_VISIT_ATTRIBUTE }?.value ?: ""
                patient.visitSpeciality =
                    attrList.find { it.typeUuid == Constants.SPECIALITY }?.value ?: ""

                patient.startDate = patient.startDate
            }

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

}
