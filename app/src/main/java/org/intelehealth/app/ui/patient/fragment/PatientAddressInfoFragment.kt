package org.intelehealth.app.ui.patient.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.identificationActivity.model.DistData
import org.intelehealth.app.activities.identificationActivity.model.StateData
import org.intelehealth.app.databinding.FragmentPatientAddressInfoBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.hideDigitErrorOnTextChang
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDigit
import org.intelehealth.app.utilities.extensions.validateDropDowb

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientAddressInfoFragment : BasePatientFragment(R.layout.fragment_patient_address_info) {

    private lateinit var binding: FragmentPatientAddressInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPatientAddressInfoBinding.bind(view)
        binding.textInputLayDistrict.isEnabled = false
        patientViewModel.updatePatientStage(PatientRegStage.ADDRESS)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupCountries() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.countries)
        binding.autoCompleteCountry.setAdapter(adapter)
        if (patient.country != null && patient.country.isNotEmpty()) {
//            binding.autoCompleteCountry.setSelection(adapter.getPosition(patient.country))
            binding.autoCompleteCountry.setText(patient.country, false)
        } else {
            val defaultValue = getString(R.string.default_country)
            Timber.d { "default $defaultValue index[${adapter.getPosition(defaultValue)}]" }
//            binding.autoCompleteCountry.setSelection(adapter.getPosition(defaultValue))
            binding.autoCompleteCountry.setText(defaultValue, false)
        }
        binding.textInputLayCountry.isEnabled = false
        binding.autoCompleteCountry.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayCountry.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.country = this.getStringArray(R.array.countries)[i]
            }
        }
    }


    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        binding.patient = patient
        binding.isEditMode = patientViewModel.isEditMode
        fetchPersonalInfoConfig()
    }

    private fun fetchPersonalInfoConfig() {
        patientViewModel.fetchAddressRegFields().observe(viewLifecycleOwner) {
            binding.addressInfoConfig = PatientRegFieldsUtils.buildPatientAddressInfoConfig(it)
            Timber.d { "Address Config => ${Gson().toJson(binding.addressInfoConfig)}" }
            setupCountries()
            setupStates()
            applyFilter()
            setInputTextChangListener()
            setClickListener()
        }
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener { findNavController().popBackStack() }
        binding.frag2BtnNext.setOnClickListener {
            validateForm { savePatient() }
        }
    }

    private fun savePatient() {
        patient.postalcode = binding.textInputPostalCode.text?.toString()
        val village = binding.textInputCityVillage.text?.toString()
        patient.cityvillage =
            if (patient.district.isNullOrEmpty().not()) "${patient.district}:$village"
            else village
        patient.address1 = binding.textInputAddress1.text?.toString()
        patient.address2 = binding.textInputAddress2.text?.toString()

        var navDirection = PatientAddressInfoFragmentDirections.navigationAddressToOther()
        if (patientViewModel.activeStatusOtherSection.not()) {
            navDirection = PatientAddressInfoFragmentDirections.navigationAddressToDetails()
        }
        findNavController().navigate(navDirection)
        patientViewModel.updatedPatient(patient)
    }

    private fun applyFilter() {
        binding.textInputCityVillage.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputAddress1.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputAddress2.addFilter(FirstLetterUpperCaseInputFilter())
    }

    private fun setInputTextChangListener() {
        binding.textInputLayCityVillage.hideErrorOnTextChang(binding.textInputCityVillage)
        binding.textInputLayAddress1.hideErrorOnTextChang(binding.textInputAddress1)
        binding.textInputLayAddress2.hideErrorOnTextChang(binding.textInputAddress2)
        binding.textInputLayPostalCode.hideDigitErrorOnTextChang(binding.textInputPostalCode, 6)
    }

    private fun setupStates() {
        LanguageUtils.getStateList()?.let {
            binding.textInputLayState.tag = it
            val adapter: ArrayAdapter<StateData> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), it
            )
            binding.autoCompleteState.setAdapter(adapter)
            if (patient.stateprovince != null && patient.stateprovince.isNotEmpty()) {
                val state = LanguageUtils.getState(patient.stateprovince)
                if (state != null && adapter.getPosition(state) > 0) {
                    binding.autoCompleteState.setText(state.toString(), false)
                } else binding.autoCompleteState.setText("", false)
            }

            binding.autoCompleteState.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayState.hideError()
                val list: List<StateData> = binding.textInputLayState.tag as List<StateData>
                val selectedState = list[i]
                patient.stateprovince = selectedState.state
                setupDistricts(selectedState)
            }
        }

    }

    private fun setupDistricts(stateData: StateData) {
        binding.textInputLayDistrict.isEnabled = true
        val adapter: ArrayAdapter<DistData> = ArrayAdapterUtils.getObjectArrayAdapter(
            requireContext(), stateData.distDataList
        )
        binding.autoCompleteDistrict.setAdapter(adapter)
        binding.textInputLayDistrict.tag = stateData.distDataList
        if (patient.district != null && patient.district.isNotEmpty()) {
            val selected = LanguageUtils.getDistrict(stateData, patient.district)
            if (selected != null && adapter.getPosition(selected) > 0) {
                binding.autoCompleteDistrict.setText(selected.toString(), false)
            } else binding.autoCompleteDistrict.setText("", false)
        }

        binding.autoCompleteDistrict.setOnItemClickListener { adapterView, _, i, _ ->
            binding.textInputLayDistrict.hideError()
            val dList: List<DistData> = binding.textInputLayDistrict.tag as List<DistData>
            patient.district = dList[i].name
        }
    }

    private fun validateForm(block: () -> Unit) {
        Timber.d { "Final patient =>${Gson().toJson(patient)}" }
        val error = R.string.this_field_is_mandatory
        binding.addressInfoConfig?.let {
            val bPostalCode = if (it.postalCode!!.isEnabled && it.postalCode!!.isMandatory) {
                val invalid = R.string.postal_code_6_dig_invalid_txt
                binding.textInputLayPostalCode.validateDigit(
                    binding.textInputPostalCode,
                    invalid,
                    6
                )
            } else true


            val bCountry = if (it.country!!.isEnabled && it.country!!.isMandatory) {
                binding.textInputLayCountry.validateDropDowb(
                    binding.autoCompleteCountry,
                    error
                )
            } else true

            val bState = if (it.state!!.isEnabled && it.state!!.isMandatory) {
                binding.textInputLayState.validateDropDowb(
                    binding.autoCompleteState,
                    error
                )
            } else true

            val bDistrict = if (it.district!!.isEnabled && it.district!!.isMandatory) {
                binding.textInputLayDistrict.validateDropDowb(
                    binding.autoCompleteState,
                    error
                )
            } else true

            val bCityVillage = if (it.cityVillage!!.isEnabled && it.cityVillage!!.isMandatory) {
                binding.textInputLayCityVillage.validate(binding.textInputCityVillage, error)
            } else true


            val bAddress1 = if (it.address1!!.isEnabled && it.address1!!.isMandatory) {
                binding.textInputLayAddress1.validate(binding.textInputAddress1, error)
            } else true

            val bAddress2 = if (it.address2!!.isEnabled && it.address2!!.isMandatory) {
                binding.textInputLayAddress1.validate(binding.textInputAddress1, error)
            } else true


            if (bPostalCode.and(bCountry).and(bState).and(bDistrict).and(bCityVillage)
                    .and(bAddress1).and(bAddress2)
            ) block.invoke()
        }
    }
}