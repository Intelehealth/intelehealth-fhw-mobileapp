package org.intelehealth.ncd.linelisting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.category.viewmodel.CommonSearchViewModel
import org.intelehealth.ncd.databinding.LayoutNcdPatientCategoryBinding
import org.intelehealth.ncd.linelisting.PatientVisitPagingAdapter
import org.intelehealth.ncd.linelisting.adapter.PatientLoadStateAdapter
import org.intelehealth.ncd.linelisting.datasource.PatientVisitDataSource
import org.intelehealth.ncd.linelisting.datasource.PatientVisitRepository
import org.intelehealth.ncd.linelisting.datasource.ProtocolViewModelFactory
import org.intelehealth.ncd.linelisting.viewmodels.ProtocolScreenViewModel
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.utils.CategorySegregationUtils
import org.intelehealth.ncd.utils.PatientNavigationUtils
import kotlinx.coroutines.launch

class ProtocolScreenFragment : Fragment(), PatientClickedListener {

    private var _binding: LayoutNcdPatientCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProtocolScreenViewModel
    private lateinit var adapter: PatientVisitPagingAdapter

    private var category: String = ""
    private var age: Int = 0
    private val searchVM: CommonSearchViewModel by activityViewModels()
    private var latestQuery: String = ""

    companion object {
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_AGE = "arg_age"

        fun newInstance(category: String, age: Int): ProtocolScreenFragment {
            val fragment = ProtocolScreenFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_CATEGORY, category)
                putInt(ARG_AGE, age)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_CATEGORY, "") ?: ""
            age = it.getInt(ARG_AGE, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutNcdPatientCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupAdapter()
        observeSearchQuery()
        observePatients()
    }

    private fun setupViewModel() {
        val visitDao = CategoryDatabase.getInstance(requireContext()).patientVisitDao()

        val dataSource = PatientVisitDataSource(visitDao)
        val repository = PatientVisitRepository(dataSource)
        val utils = CategorySegregationUtils(resources)

        viewModel = ViewModelProvider(
            this,
            ProtocolViewModelFactory(repository, utils)
        )[ProtocolScreenViewModel::class.java]
    }

    private fun setupAdapter() {
        adapter = PatientVisitPagingAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            footer = PatientLoadStateAdapter(
                retry = { adapter.retry() },
                mainAdapterItemCount = { adapter.itemCount },
            )
        )
        adapter.addLoadStateListener { loadState ->
            val refresh = loadState.refresh
            val append = loadState.append
            if (refresh is LoadState.NotLoading && append.endOfPaginationReached) {
                binding.noDataLayout.isVisible = adapter.itemCount == 0
            }
        }


        adapter.addOnPagesUpdatedListener {
            if (adapter.itemCount > 0) {
                binding.noDataLayout.isVisible = false
            }
        }



    }

    /* private fun setupSearch() {
         val searchBar = requireActivity().findViewById<EditText>(R.id.search_txt_enter)
         searchBar?.addTextChangedListener { editable ->
             viewModel.setSearchQuery(editable?.toString() ?: "")
         }
     }*/

    private fun observeSearchQuery() {
        // Only the visible tab is RESUMED; off-screen tabs skip DB/paging work (ViewPager keeps them STARTED).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                searchVM.searchTextFlow.collectLatest { q ->
                    latestQuery = q
                }
            }
        }
    }

    private fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getPatientsPagedNew(
                    category = category,
                    searchQueryFlow = searchVM.searchTextFlow,
                    skipCategorySegregation = false
                ).collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

    }
    override fun onPatientClicked(patient: PatientVisitDetails) {
        PatientNavigationUtils.openPatientDetail(
            requireContext(),
            patient,
            category
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}