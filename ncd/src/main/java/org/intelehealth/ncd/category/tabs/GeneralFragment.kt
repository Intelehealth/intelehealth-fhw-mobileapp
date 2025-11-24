package org.intelehealth.ncd.category.tabs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.databinding.LayoutNcdPatientCategoryBinding
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.category.adapter.CategoryRecyclerViewAdapter
import org.intelehealth.ncd.category.viewmodel.GeneralViewModel
import org.intelehealth.ncd.category.viewmodel.factory.CategoryViewModelFactory
import org.intelehealth.ncd.pagination.PatientLoadStateAdapter
import org.intelehealth.ncd.pagination.PatientPagingAdapter
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.dao.VisitDao
import org.intelehealth.ncd.search.SearchableFragment
import org.intelehealth.ncd.utils.CategorySegregationUtils
import org.intelehealth.ncd.utils.PatientNavigationUtils

class GeneralFragment : SearchableFragment<GeneralViewModel>(), PatientClickedListener {

    private var binding: LayoutNcdPatientCategoryBinding? = null
    override lateinit var viewModel: GeneralViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutNcdPatientCategoryBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeData()
        setObservers()
        //fetchAndSetPatients()
    }

    private fun initializeData() {
        val context = requireContext()
        val database = CategoryDatabase.getInstance(context)
        val patientDao = database.patientDao()
        val patientAttributeDao = database.patientAttributeDao()
        val visitsDao = database.visitDao()
        val generalTabDao = database.generalTabDao()

        val dataSource = CategoryDataSource(patientDao, patientAttributeDao, visitsDao, generalTabDao)
        val repository = CategoryRepository(dataSource)
        val utils = CategorySegregationUtils(resources)

        viewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(repository, utils)
        )[GeneralViewModel::class.java]
    }

    override fun onSearchQueryChanged(query: String) {
        viewModel.onSearchQueryChanged(query) // just call the setter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
    override fun onPatientClicked(patient: PatientVisitDetails) {
        PatientNavigationUtils.openPatientDetail(requireContext(), patient, Constants.GENERAL)
    }

    private fun setObservers() {
        val recyclerView = binding?.recyclerView ?: return

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = null // disable default change animation
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            isNestedScrollingEnabled = false // avoid nested scroll conflicts
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val adapter = PatientPagingAdapter(
            resources = resources,
            context = requireContext(),
            listener = this
        )

        recyclerView.adapter = adapter.withLoadStateFooter(
            footer = PatientLoadStateAdapter { adapter.retry() }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getPatientFlow(Constants.ENCOUNTER_VISIT_COMPLETE)
                    .collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
            }
        }

    }

}
