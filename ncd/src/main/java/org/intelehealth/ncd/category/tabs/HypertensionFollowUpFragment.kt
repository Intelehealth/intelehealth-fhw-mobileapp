package org.intelehealth.ncd.category.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
import org.intelehealth.ncd.category.viewmodel.HypertensionFollowUpViewModel
import org.intelehealth.ncd.category.viewmodel.factory.CategoryViewModelFactory
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.dao.VisitDao
import org.intelehealth.ncd.search.SearchableFragment
import org.intelehealth.ncd.utils.CategorySegregationUtils
import org.intelehealth.ncd.utils.PatientNavigationUtils

class HypertensionFollowUpFragment : SearchableFragment<HypertensionFollowUpViewModel>(), PatientClickedListener {

    private var binding: LayoutNcdPatientCategoryBinding? = null

    override lateinit var viewModel: HypertensionFollowUpViewModel

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
        fetchAndSetPatients()
    }

    private fun initializeData() {
        val patientDao: PatientDao = CategoryDatabase.getInstance(requireContext()).patientDao()
        val patientAttributeDao: PatientAttributeDao = CategoryDatabase.getInstance(requireContext()).patientAttributeDao()
        val visitsDao: VisitDao = CategoryDatabase.getInstance(requireContext()).visitDao()

        val dataSource = CategoryDataSource(patientDao, patientAttributeDao, visitsDao)
        val repository = CategoryRepository(dataSource)
        val utils = CategorySegregationUtils(resources)

        viewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(repository, utils)
        )[HypertensionFollowUpViewModel::class.java]
    }

    private fun setObservers() {
        viewModel.hypertensionFollowUpLiveData.observe(viewLifecycleOwner) {
            val adapter = CategoryRecyclerViewAdapter(it, resources, requireContext(), this)

            binding?.recyclerView?.let { rv ->
                rv.adapter = adapter
                rv.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun fetchAndSetPatients() {
        viewModel.getPatientsForHypertensionFollowUp()
    }

    override fun onSearchQueryChanged(query: String) {
        viewModel.searchPatient(query)
    }

   /* override fun onPatientClicked(patientVisitDetails: PatientVisitDetails) {
        try {
            val intent = Intent(
                requireActivity(),
                Class.forName("org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2")
            )

            val status = "returning"
            val tag = "search"
            val hasPrescription = "false"

            intent.putExtra(Constants.INTENT_PATIENT_UUID, patientVisitDetails.patientId)
            intent.putExtra(Constants.INTENT_PATIENT_NAME, "${patientVisitDetails.firstName} ${patientVisitDetails.lastName}")
            intent.putExtra(Constants.INTENT_PATIENT_STATUS, status)
            intent.putExtra(Constants.INTENT_PATIENT_TAG, tag)
            intent.putExtra(Constants.INTENT_HAS_PRESCRIPTION, hasPrescription)

            startActivity(intent)
        } catch (exception: ClassNotFoundException) {
            exception.printStackTrace()
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
    override fun onPatientClicked(patient: PatientVisitDetails) {
        PatientNavigationUtils.openPatientDetail(requireContext(), patient)
    }
}
