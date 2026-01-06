package org.intelehealth.ncd.pagination

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.room.dao.GeneralTabDao
import org.intelehealth.ncd.utils.DateAndTimeUtils

class PatientVisitPagingSourceNew (
    private val dataSource: CategoryDataSource,
    private val query: String,
    private val generalTabDao: GeneralTabDao
) : PagingSource<Int, PatientVisitDetails>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientVisitDetails> {
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
