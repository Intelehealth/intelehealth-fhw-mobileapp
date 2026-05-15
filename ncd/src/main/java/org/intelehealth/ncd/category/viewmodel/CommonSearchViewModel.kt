package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CommonSearchViewModel : ViewModel() {
    private val _searchText = MutableLiveData("")
    val searchText: LiveData<String> = _searchText

    fun updateSearchText(query: String) {
        _searchText.value = query
    }
    private val _searchTextFlow = MutableStateFlow("")
    val searchTextFlow: StateFlow<String> = _searchTextFlow

    fun updateSearchTextNew(text: String) {
        _searchTextFlow.value = text
        _searchText.value = text
    }
}
