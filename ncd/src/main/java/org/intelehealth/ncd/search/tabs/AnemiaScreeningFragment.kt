package org.intelehealth.ncd.search.tabs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.databinding.LayoutSearchPatientCategoryBinding
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.data.SearchDataSource
import org.intelehealth.ncd.data.SearchRepository
import org.intelehealth.ncd.search.adapter.CategoryRecyclerViewAdapter
import org.intelehealth.ncd.search.viewmodel.AnemiaScreeningViewModel
import org.intelehealth.ncd.search.viewmodel.CategoryViewModelFactory

class AnemiaScreeningFragment : Fragment() {

    private var binding: LayoutSearchPatientCategoryBinding? = null
    private var viewModel: AnemiaScreeningViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutSearchPatientCategoryBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeData()
        setObservers()
        fetchAndSetPatients()
    }

    private fun initializeData() {
        val patientDao: PatientDao = CategoryDatabase.getInstance(requireContext()).patientDao()
        val patientAttributeDao: PatientAttributeDao =
            CategoryDatabase.getInstance(requireContext()).patientAttributeDao()

        val dataSource = SearchDataSource(patientDao, patientAttributeDao)
        val repository = SearchRepository(dataSource)

        viewModel = ViewModelProvider(
            owner = this@AnemiaScreeningFragment,
            factory = CategoryViewModelFactory(repository, resources)
        )[AnemiaScreeningViewModel::class.java]
    }

    private fun setObservers() {
        viewModel?.anemiaScreeningLiveData?.observe(requireActivity()) {
            val adapter = CategoryRecyclerViewAdapter(it, resources, requireContext())

            binding?.recyclerView?.let { rv ->
                rv.adapter = adapter
                rv.layoutManager =
                    LinearLayoutManager(this@AnemiaScreeningFragment.requireContext())
            }
        }
    }

    private fun fetchAndSetPatients() {
        viewModel?.getPatientsForAnemiaScreening(Constants.ANEMIA_EXCLUSION_AGE)
    }
}
