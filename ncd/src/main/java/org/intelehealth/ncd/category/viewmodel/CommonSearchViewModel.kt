package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommonSearchViewModel : ViewModel() {
    private val _searchText = MutableLiveData("")
    val searchText: LiveData<String> = _searchText

    fun updateSearchText(query: String) {
        _searchText.value = query
    }
}