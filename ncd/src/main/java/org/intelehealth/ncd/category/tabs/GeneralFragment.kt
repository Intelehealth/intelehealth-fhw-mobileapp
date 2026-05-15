package org.intelehealth.ncd.category.tabs

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
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

    companion object {
        private const val LOG_TAG = "Pooja"
    }

    private var binding: LayoutNcdPatientCategoryBinding? = null
    override lateinit var viewModel: GeneralViewModel

    /** Used to correlate `repeatOnLifecycle(RESUMED)` delay with lifecycle. */
    private var onStartElapsedMs: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        val t0 = SystemClock.elapsedRealtime()
        Log.d(LOG_TAG, "GeneralFragment.onCreate START elapsedMs=$t0")
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "GeneralFragment.onCreate END +${SystemClock.elapsedRealtime() - t0}ms")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val t0 = SystemClock.elapsedRealtime()
        Log.d(LOG_TAG, "GeneralFragment.onCreateView START elapsedMs=$t0")
        binding = LayoutNcdPatientCategoryBinding.inflate(layoutInflater)
        val root = binding!!.root
        Log.d(LOG_TAG, "GeneralFragment.onCreateView END inflate +${SystemClock.elapsedRealtime() - t0}ms")
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val t0 = SystemClock.elapsedRealtime()
        Log.d(LOG_TAG, "GeneralFragment.onViewCreated START elapsedMs=$t0")
        val tSuper = SystemClock.elapsedRealtime()
        super.onViewCreated(view, savedInstanceState)
        Log.d(LOG_TAG, "GeneralFragment.onViewCreated after super.onViewCreated +${SystemClock.elapsedRealtime() - tSuper}ms (total +${SystemClock.elapsedRealtime() - t0}ms)")
        val tInit = SystemClock.elapsedRealtime()
        initializeData()
        Log.d(LOG_TAG, "GeneralFragment.onViewCreated after initializeData +${SystemClock.elapsedRealtime() - tInit}ms (total +${SystemClock.elapsedRealtime() - t0}ms)")
        val tObs = SystemClock.elapsedRealtime()
        setObservers()
        Log.d(LOG_TAG, "GeneralFragment.onViewCreated after setObservers +${SystemClock.elapsedRealtime() - tObs}ms (total +${SystemClock.elapsedRealtime() - t0}ms)")
        Log.d(LOG_TAG, "GeneralFragment.onViewCreated END total +${SystemClock.elapsedRealtime() - t0}ms")
    }

    override fun onStart() {
        onStartElapsedMs = SystemClock.elapsedRealtime()
        val t0 = onStartElapsedMs
        Log.d(LOG_TAG, "GeneralFragment.onStart START elapsedMs=$t0")
        super.onStart()
        Log.d(LOG_TAG, "GeneralFragment.onStart END +${SystemClock.elapsedRealtime() - t0}ms")
    }

    override fun onResume() {
        val t0 = SystemClock.elapsedRealtime()
        val gapFromOnStart = t0 - onStartElapsedMs
        Log.d(LOG_TAG, "GeneralFragment.onResume START elapsedMs=$t0 onStart→onResumeGap=${gapFromOnStart}ms (if large: ViewPager2/activity delay until RESUMED, not DB)")
        super.onResume()
        Log.d(LOG_TAG, "GeneralFragment.onResume END +${SystemClock.elapsedRealtime() - t0}ms")
    }

    private fun initializeData() {
        val t0 = SystemClock.elapsedRealtime()
        Log.d(LOG_TAG, "GeneralFragment.initializeData START elapsedMs=$t0")
        val context = requireContext()
        val tDb = SystemClock.elapsedRealtime()
        val database = CategoryDatabase.getInstance(context)
        Log.d(LOG_TAG, "GeneralFragment.initializeData CategoryDatabase.getInstance +${SystemClock.elapsedRealtime() - tDb}ms")
        val tDaos = SystemClock.elapsedRealtime()
        val patientDao = database.patientDao()
        val patientAttributeDao = database.patientAttributeDao()
        val visitsDao = database.visitDao()
        val generalTabDao = database.generalTabDao()
        Log.d(LOG_TAG, "GeneralFragment.initializeData daos +${SystemClock.elapsedRealtime() - tDaos}ms")

        val tDs = SystemClock.elapsedRealtime()
        val dataSource = CategoryDataSource(patientDao, patientAttributeDao, visitsDao, generalTabDao)
        val repository = CategoryRepository(dataSource)
        val utils = CategorySegregationUtils(resources)
        Log.d(LOG_TAG, "GeneralFragment.initializeData repository+utils +${SystemClock.elapsedRealtime() - tDs}ms")

        val tVm = SystemClock.elapsedRealtime()
        viewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(repository, utils)
        )[GeneralViewModel::class.java]
        Log.d(LOG_TAG, "GeneralFragment.initializeData ViewModelProvider +${SystemClock.elapsedRealtime() - tVm}ms")
        Log.d(LOG_TAG, "GeneralFragment.initializeData END total +${SystemClock.elapsedRealtime() - t0}ms")
    }

    override fun onSearchQueryChanged(query: String) {
        val t0 = SystemClock.elapsedRealtime()
        viewModel.onSearchQueryChanged(query)
        val dt = SystemClock.elapsedRealtime() - t0
        if (dt > 5) {
            Log.d(LOG_TAG, "GeneralFragment.onSearchQueryChanged len=${query.length} viewModel.onSearchQueryChanged took ${dt}ms")
        }
    }


    override fun onDestroyView() {
        val t0 = SystemClock.elapsedRealtime()
        Log.d(LOG_TAG, "GeneralFragment.onDestroyView START elapsedMs=$t0")
        super.onDestroyView()
        binding = null
        Log.d(LOG_TAG, "GeneralFragment.onDestroyView END +${SystemClock.elapsedRealtime() - t0}ms")
    }
    override fun onPatientClicked(patient: PatientVisitDetails) {
        PatientNavigationUtils.openPatientDetail(requireContext(), patient, Constants.GENERAL)
    }

    private fun setObservers() {
        val t0 = SystemClock.elapsedRealtime()
        Log.d(LOG_TAG, "GeneralFragment.setObservers START elapsedMs=$t0")
        val recyclerView = binding?.recyclerView ?: run {
            Log.d(LOG_TAG, "GeneralFragment.setObservers ABORT recyclerView null")
            return
        }

        val tRv = SystemClock.elapsedRealtime()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = null // disable default change animation
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            isNestedScrollingEnabled = false // avoid nested scroll conflicts
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        Log.d(LOG_TAG, "GeneralFragment.setObservers recyclerView setup +${SystemClock.elapsedRealtime() - tRv}ms")

        val tAd = SystemClock.elapsedRealtime()
        val adapter = PatientPagingAdapter(
            resources = resources,
            context = requireContext(),
            listener = this
        )
        Log.d(LOG_TAG, "GeneralFragment.setObservers PatientPagingAdapter ctor +${SystemClock.elapsedRealtime() - tAd}ms")

        val tSetAd = SystemClock.elapsedRealtime()
        recyclerView.adapter = adapter.withLoadStateFooter(
            footer = PatientLoadStateAdapter { adapter.retry() }
        )
        Log.d(LOG_TAG, "GeneralFragment.setObservers adapter+footer set +${SystemClock.elapsedRealtime() - tSetAd}ms")

        val context = requireContext()
        val tDb2 = SystemClock.elapsedRealtime()
        val database = CategoryDatabase.getInstance(context)
        val generalTabDao = database.generalTabDao()
        Log.d(LOG_TAG, "GeneralFragment.setObservers second getInstance+generalTabDao +${SystemClock.elapsedRealtime() - tDb2}ms")

        val tLaunch = SystemClock.elapsedRealtime()
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(
                LOG_TAG,
                "GeneralFragment coroutine launched +${SystemClock.elapsedRealtime() - tLaunch}ms after lifecycleScope.launch (since setObserversStart +${SystemClock.elapsedRealtime() - t0}ms)"
            )
            val tRepeat = SystemClock.elapsedRealtime()
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Log.d(
                    LOG_TAG,
                    "GeneralFragment repeatOnLifecycle(RESUMED) entered +${SystemClock.elapsedRealtime() - tRepeat}ms after repeatOnLifecycle call"
                )
                val tCollectStart = SystemClock.elapsedRealtime()
                Log.d(LOG_TAG, "GeneralFragment getPatientFlow.collectLatest START elapsedMs=$tCollectStart")
                viewModel.getPatientFlow(generalTabDao)
                    .collectLatest { pagingData ->
                        val tEmission = SystemClock.elapsedRealtime()
                        Log.d(
                            LOG_TAG,
                            "GeneralFragment collectLatest emission (first page/update) +${tEmission - tCollectStart}ms since collect start"
                        )
                        val tSubmit = SystemClock.elapsedRealtime()
                        adapter.submitData(pagingData)
                        val tAfterSubmit = SystemClock.elapsedRealtime()
                        Log.d(
                            LOG_TAG,
                            "GeneralFragment collectLatest adapter.submitData took ${tAfterSubmit - tSubmit}ms (since emission start +${tAfterSubmit - tEmission}ms)"
                        )
                    }
            }
            Log.d(LOG_TAG, "GeneralFragment repeatOnLifecycle block exited (paused/destroyed) elapsedMs=${SystemClock.elapsedRealtime()}")
        }
        Log.d(LOG_TAG, "GeneralFragment.setObservers END lifecycleScope.launch scheduled total +${SystemClock.elapsedRealtime() - t0}ms")
    }

}
