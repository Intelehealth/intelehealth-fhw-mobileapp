package org.intelehealth.ncd.category.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.databinding.LayoutNcdPatientCategoryBinding
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.category.viewmodel.GeneralViewModel
import org.intelehealth.ncd.category.viewmodel.factory.CategoryViewModelFactory
import org.intelehealth.ncd.pagination.PatientLoadStateAdapter
import org.intelehealth.ncd.pagination.PatientPagingAdapter
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.search.SearchableFragment
import org.intelehealth.ncd.utils.CategorySegregationUtils
import org.intelehealth.ncd.utils.PatientNavigationUtils

class GeneralFragment : SearchableFragment<GeneralViewModel>(), PatientClickedListener {

    private var binding: LayoutNcdPatientCategoryBinding? = null
    override lateinit var viewModel: GeneralViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
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
        viewModel.onSearchQueryChanged(query)
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
            itemAnimator = null
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            isNestedScrollingEnabled = false
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

        val context = requireContext()
        val database = CategoryDatabase.getInstance(context)
        val generalTabDao = database.generalTabDao()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getPatientFlow(generalTabDao)
                    .collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
            }
        }
    }
}
