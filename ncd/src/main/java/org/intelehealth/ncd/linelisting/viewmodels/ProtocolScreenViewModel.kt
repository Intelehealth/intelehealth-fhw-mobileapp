package org.intelehealth.ncd.linelisting.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.intelehealth.ncd.linelisting.datasource.PatientVisitRepository
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
        searchQueryFlow: Flow<String>,
        skipCategorySegregation: Boolean = false
    ): Flow<PagingData<PatientVisitDetails>> {

        return searchQueryFlow
            .debounce(300)
            .flatMapLatest { query ->

                Pager(
                    config = PagingConfig(
                        pageSize = 10,
                        initialLoadSize = 20,
                        prefetchDistance = 5,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        // Step 1: fetch latest ended visit per patient (paged)
                        repository.getPagedVisits(
                            searchQuery = query
                        )
                    }
                ).flow

                    // Step 2: enrich each patient with protocol flags
                    .map { pagingData ->
                        pagingData.map { baseVisit ->

                            // Fetch all visits for the patient (including non-ended)
                            val allVisits = repository.getAllVisitsForPatient(baseVisit.patientId)
                            ///val allVisits = repository.getAllVisitsForPatientNew(baseVisit.patientId)

                            // Parse protocol flags from all visits (non-ended included)
                            val flagsOnlyPatient =
                                ProtocolParserHelper.parsePatientHistory(allVisits)

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

                                // Base visit info      `               
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
                            finalPatient
                        }
                    }
                    .map { pagingData ->
                        /* if (skipCategorySegregation) {
                            Log.d("TAG", "getPatientsPaged: in skip : "+skipCategorySegregation)
                            pagingData // skip segregation
                        } else {*/

                        pagingData.filter { parsedPatient ->

                            val resultList = categorySegregationUtils
                                .segregateAndFetchPatientVisitDetails(
                                    listOf(parsedPatient),
                                    category
                                )
                            resultList.isNotEmpty()
                            //  }
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

    fun getPatientsPagedNew(
        category: String,
        searchQueryFlow: Flow<String>,
        skipCategorySegregation: Boolean = false
    ): Flow<PagingData<PatientVisitDetails>> {

        return searchQueryFlow
            .debounce(300)
            .flatMapLatest { searchQuery ->

                Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        initialLoadSize = 20,
                        prefetchDistance = 5,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        repository.getPagedVisits(searchQuery)
                    }
                ).flow
                    .map { pagingData ->
                        pagingData.map { patient ->

                            val patientId = patient.patientId

                            val allVisits =
                                if (patientId != null) {
                                    repository.getAllVisitsForPatientNew(
                                        patientUuid = listOf(patientId)
                                    )
                                } else {
                                    emptyList()
                                }

                            // base patient
                            ProtocolParserHelper.parsePatientHistoryNew(
                                basePatient = patient,
                                allVisits = allVisits
                            )
                        }
                    }
                    .map { pagingData ->
                        if (skipCategorySegregation) {
                            pagingData
                        } else {
                            pagingData.filter { patient ->
                                categorySegregationUtils
                                    .segregateAndFetchPatientVisitDetails(
                                        listOf(patient),
                                        category
                                    )
                                    .isNotEmpty()
                            }
                        }
                    }
            }
            .cachedIn(viewModelScope)
    }
}