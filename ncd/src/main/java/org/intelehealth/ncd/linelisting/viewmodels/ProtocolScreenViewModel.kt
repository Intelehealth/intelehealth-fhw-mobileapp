package org.intelehealth.ncd.linelisting.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.linelisting.datasource.PatientVisitRepository
import org.intelehealth.ncd.linelisting.utils.NoopListCallback
import org.intelehealth.ncd.linelisting.utils.PatientVisitDetailsDiffCallback
import org.intelehealth.ncd.linelisting.utils.ProtocolParserHelper
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.CategorySegregationUtils

class ProtocolScreenViewModel(
     val repository: PatientVisitRepository,
    val categorySegregationUtils: CategorySegregationUtils
) : ViewModel() {

    //private val searchQuery = MutableStateFlow("")

   /* fun setSearchQuery(query: String) {
        searchQuery.value = query
    }*/

    fun getPatientsPaged(
        category: String,
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String? = null,
        searchQueryFlow: Flow<String>,
        skipCategorySegregation: Boolean = false
    ): Flow<PagingData<PatientVisitDetails>> {

        return searchQueryFlow
            .debounce(300)
            .flatMapLatest { query ->

                Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = {
                        // Step 1: fetch latest ended visit per patient (paged)
                        repository.getPagedVisits(
                            age = age,
                            attributeTypeUuid = attributeTypeUuid,
                            visitNoteEncounterUuid = visitNoteEncounterUuid,
                            patientUuid = patientUuid,
                            searchQuery = query
                        )
                    }
                ).flow

                    // Step 2: enrich each patient with protocol flags
                    .map { pagingData ->
                        pagingData.map { baseVisit ->

                            Log.d("TAGkz", "Base visit from main query: $baseVisit")

                            // Fetch all visits for the patient (including non-ended)
                            val allVisits = repository.getAllVisitsForPatient(baseVisit.patientId)
                            Log.d("TAGkz", "All visits for patient ${baseVisit.patientId}: $allVisits")

                            // Parse protocol flags from all visits (non-ended included)
                            val flagsOnlyPatient = ProtocolParserHelper.parsePatientHistory(allVisits)
                            Log.d("TAGkz", "getPatientsPaged: flagsOnlyPatient 1 : ${flagsOnlyPatient}")
                            Log.d("TAGkz", "getPatientsPaged: flagsOnlyPatient 2 : "+Gson().toJson(flagsOnlyPatient))

                            // Merge base visit info + protocol flags
                            val finalPatient = flagsOnlyPatient.copy(
                                // Base patient info
                                patientId = baseVisit.patientId,
                                patientPhoto = baseVisit.patientPhoto,
                                age = baseVisit.age,
                                firstName = baseVisit.firstName,
                                middleName = baseVisit.middleName,
                                lastName = baseVisit.lastName,
                                openmrsId = baseVisit.openmrsId,
                                gender = baseVisit.gender,
                                dateOfBirth = baseVisit.dateOfBirth,
                                patientPhoneNumber = baseVisit.patientPhoneNumber,
                                personAttributeTypeUuid = baseVisit.personAttributeTypeUuid,

                                // Base visit info
                                visitId = baseVisit.visitId,
                                startDate = baseVisit.startDate,
                                visitEndDate = baseVisit.visitEndDate,
                                isPrescriptionExist = baseVisit.isPrescriptionExist,
                                visitSpeciality = baseVisit.visitSpeciality,
                                isNcdVisit = flagsOnlyPatient.isNcdVisit ?: baseVisit.isNcdVisit,

                                // Protocol flag fields
                                value = flagsOnlyPatient.value ?: baseVisit.value,
                                chiefComplaintData = flagsOnlyPatient.chiefComplaintData
                                    ?: baseVisit.chiefComplaintData,
                                isHypertensionFollowupGiven = flagsOnlyPatient.isHypertensionFollowupGiven,
                                isHypertensionFollowupTodayOrLater = flagsOnlyPatient.isHypertensionFollowupTodayOrLater
                            )

                            Log.d("TAGkz", "Merged patient with protocol flags: $finalPatient")
                            finalPatient
                        }
                    }

                    // Step 3: apply category segregation filter
                    .map { pagingData ->
                        if (skipCategorySegregation) {
                            Log.d("TAG", "getPatientsPaged: in skip : "+skipCategorySegregation)
                            pagingData // skip segregation
                        } else {
                            Log.d("TAG", "getPatientsPaged: in non skip : "+skipCategorySegregation)

                            pagingData.filter { parsedPatient ->
                                val resultList = categorySegregationUtils
                                    .segregateAndFetchPatientVisitDetails(listOf(parsedPatient), category)
                                resultList.isNotEmpty()
                            }
                        }
                    }
                    /*.map { pagingData ->
                        pagingData.filter { parsedPatient ->
                            val resultList = categorySegregationUtils
                                .segregateAndFetchPatientVisitDetails(listOf(parsedPatient), category)

                            Log.d(
                                "TAGkz",
                                "Category segregation for patient ${parsedPatient.patientId}: $resultList"
                            )

                            resultList.isNotEmpty() // keep only matching patients
                        }
                    }*/
            }
            .cachedIn(viewModelScope)
    }
}