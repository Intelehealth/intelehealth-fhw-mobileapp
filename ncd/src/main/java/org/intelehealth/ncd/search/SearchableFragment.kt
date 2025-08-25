package org.intelehealth.ncd.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import org.intelehealth.ncd.category.viewmodel.CommonSearchViewModel

abstract class SearchableFragment<VM : ViewModel> : Fragment() {


    protected abstract val viewModel: VM
    protected abstract fun onSearchQueryChanged(query: String)

    protected val searchViewModel: CommonSearchViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel.searchText.observe(viewLifecycleOwner) { query ->
            onSearchQueryChanged(query)
        }
    }

    override fun onResume() {
        super.onResume()
        // Trigger the current query again when fragment becomes visible
        searchViewModel.searchText.value?.let {
            onSearchQueryChanged(it)
        }
    }
}
