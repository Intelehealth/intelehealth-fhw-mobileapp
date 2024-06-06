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
import org.intelehealth.ncd.utils.CategorySegregationUtils

class HypertensionFollowUpFragment : Fragment(), PatientClickedListener {

    private var binding: LayoutNcdPatientCategoryBinding? = null
    private var viewModel: HypertensionFollowUpViewModel? = null

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
        val patientAttributeDao: PatientAttributeDao =
            CategoryDatabase.getInstance(requireContext()).patientAttributeDao()

        val dataSource = CategoryDataSource(patientDao, patientAttributeDao)
        val repository = CategoryRepository(dataSource)
        val utils = CategorySegregationUtils(resources)

        viewModel = ViewModelProvider(
            owner = this@HypertensionFollowUpFragment,
            factory = CategoryViewModelFactory(repository, utils)
        )[HypertensionFollowUpViewModel::class.java]
    }

    private fun setObservers() {
        viewModel?.hypertensionFollowUpLiveData?.observe(requireActivity()) {
            val adapter = CategoryRecyclerViewAdapter(it, resources, requireContext(), this)

            binding?.recyclerView?.let { rv ->
                rv.adapter = adapter
                rv.layoutManager =
                    LinearLayoutManager(this@HypertensionFollowUpFragment.requireContext())
            }
        }
    }

    private fun fetchAndSetPatients() {
        viewModel?.getPatientsForHypertensionFollowUp(Constants.HYPERTENSION_EXCLUSION_AGE)
    }

    override fun onPatientClicked(patient: Patient) {
        try {
            val intent = Intent(
                requireActivity(),
                Class.forName("org.intelehealth.ekalarogya.activities.patientDetailActivity.PatientDetailActivity")
            )

            val status = "returning"
            val tag = "search"
            val hasPrescription = "false"

            intent.putExtra(Constants.INTENT_PATIENT_UUID, patient.uuid)
            intent.putExtra(
                Constants.INTENT_PATIENT_NAME,
                "${patient.firstName} ${patient.lastname}"
            )
            intent.putExtra(Constants.INTENT_PATIENT_STATUS, status)
            intent.putExtra(Constants.INTENT_PATIENT_TAG, tag)
            intent.putExtra(Constants.INTENT_HAS_PRESCRIPTION, hasPrescription)

            startActivity(intent)
        } catch (exception: ClassNotFoundException) {
            exception.printStackTrace()
        }
    }

}
