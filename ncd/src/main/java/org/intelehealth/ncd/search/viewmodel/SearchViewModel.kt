package org.intelehealth.ncd.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.data.search.SearchRepository
import org.intelehealth.ncd.model.PatientWithAttribute
import org.intelehealth.ncd.utils.CategorySegregationUtils

class SearchViewModel(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _searchMutableLiveData = MutableLiveData<List<PatientWithAttribute>>()
    val searchMutableLiveData: LiveData<List<PatientWithAttribute>> = _searchMutableLiveData

    fun queryPatientWithAttributesAndSearchString(
        attribute: String,
        searchString: String,
        phoneNumberAttribute: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var patientsWithAttribute = repository.queryPatientsAndAttributesForSearchString(
                attribute,
                searchString,
                phoneNumberAttribute
            )

            patientsWithAttribute =
                utils.populatePatientDiseaseAttributes(patientsWithAttribute.toMutableList())
            _searchMutableLiveData.postValue(patientsWithAttribute)
        }
    }
}