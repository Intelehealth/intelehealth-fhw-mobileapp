package org.intelehealth.app.ui.patient.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentPatientPersonalInfoBinding
import org.intelehealth.app.utilities.SessionManager

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientPersonalInfoFragment : Fragment(R.layout.fragment_patient_personal_info) {
    private lateinit var binding: FragmentPatientPersonalInfoBinding
    private val sessionManager: SessionManager by lazy {
        SessionManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPatientPersonalInfoBinding.bind(view)
        setupGuardianType()
        setupEmContactType()
        setupErroMessageForInputType()
    }

    private fun setupErroMessageForInputType() {
        binding.textInputLayFirstName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayMiddleName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayLastName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayDob.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayAge.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayGuardianType.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayGuardianName.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayEmContactType.error = getString(R.string.this_field_is_mandatory)
        binding.textInputLayEmContactName.error = getString(R.string.this_field_is_mandatory)
    }

    private fun setupGuardianType() {
        val guardianType = "guardian_type_" + sessionManager.appLanguage
        val contactTypes: Int = resources.getIdentifier(
            guardianType,
            "array",
            requireActivity().applicationContext.packageName
        )
        val adapter = ArrayAdapter.createFromResource(
            requireActivity(),
            contactTypes, R.layout.ui2_custome_dropdown_item_view
        )
        binding.autoCompleteGuardianType.setAdapter(adapter)
    }

    private fun setupEmContactType() {
        val countriesLanguage = "contact_type_" + sessionManager.appLanguage
        val contactTypes: Int = resources.getIdentifier(
            countriesLanguage,
            "array",
            requireActivity().applicationContext.packageName
        )
        val adapter = ArrayAdapter.createFromResource(
            requireActivity(),
            contactTypes, R.layout.ui2_custome_dropdown_item_view
        )
        binding.autoCompleteEmContactType.setAdapter(adapter)
    }
}