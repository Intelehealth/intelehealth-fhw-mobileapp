package org.intelehealth.ncd.search.viewmodel

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.ncd.data.SearchRepository

@Suppress("UNCHECKED_CAST")
class CategoryViewModelFactory(
    private val repository: SearchRepository,
    private val resources: Resources
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AnemiaScreeningViewModel::class.java -> AnemiaScreeningViewModel(repository, resources)
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        } as T
    }
}