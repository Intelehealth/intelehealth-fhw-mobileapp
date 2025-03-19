package org.intelehealth.app.ui.baseline_survey.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.StaticPatientRegistrationEnabledFieldsHelper
import org.intelehealth.app.databinding.FragmentBaselineSurveyMedicalBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.filter.AllowAllLettersInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.getSelectedData
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.storeReasonIfAnswerIsPositive
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDropDowb

/**
 * Created by Shazzad H Kanon on 06-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/


class BaselineMedicalFragment :
    BaseFragmentBaselineSurvey(R.layout.fragment_baseline_survey_medical) {

    private lateinit var binding: FragmentBaselineSurveyMedicalBinding
    private var isAgeGreaterThan18: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentBaselineSurveyMedicalBinding.bind(view)
        baselineSurveyViewModel.updateBaselineStage(BaselineSurveyStage.MEDICAL)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onBaselineDataLoaded(baselineData: Baseline) {
        super.onBaselineDataLoaded(baselineData)
        fetchMedicalBaselineConfig()
        binding.baseline = baselineData
        binding.baselineEditMode = baselineSurveyViewModel.baselineEditMode
        checkPatientAge()
    }

    private fun checkPatientAge() {
        baselineSurveyViewModel
            .getPatientAge(baselineSurveyViewModel.patientId)
            .observe(viewLifecycleOwner) {
                it ?: return@observe
                baselineSurveyViewModel.handleResponse(it) { age -> setUp18Fields(age) }
            }
    }

    private fun setUp18Fields(age: Int) {
        if (age >= 18) {
            isAgeGreaterThan18 = true
            return
        }

        binding.llHbCheck.visibility = View.GONE
        binding.llBpCheck.visibility = View.GONE
        binding.llSugarCheck.visibility = View.GONE
    }


    private fun fetchMedicalBaselineConfig() {
        val it = getStaticPatientRegistrationFields()
        binding.medicalConfig = PatientRegFieldsUtils.buildMedicalBaselineConfig(it)
        setValues()
        setClickListener()
        setupFilter()
    }

    private fun setupFilter() {
        binding.etSurgeryReasonCheck.apply {
            addFilter(FirstLetterUpperCaseInputFilter())
            addFilter(AllowAllLettersInputFilter())
        }
    }

    private fun getStaticPatientRegistrationFields() =
        StaticPatientRegistrationEnabledFieldsHelper.getEnabledMedicalBaselineFields()

    private fun setValues() {
        setupHbCheck()
        setupBpCheck()
        setupSugarCheck()
        setupSurgeries()
        setInputTextChangedListener()
        setupSmokingHistory()
        setupAlcoholConsumption()
    }

    private fun setupAlcoholConsumption() {
        binding.rgAlcoholHistoryOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioAlcoholHistoryYes.id -> {
                    binding.llAlcoholRate.visibility = View.VISIBLE
                    binding.llAlcoholDuration.visibility = View.VISIBLE
                    binding.llAlcoholFrequency.visibility = View.VISIBLE
                }

                else -> {
                    binding.llAlcoholRate.visibility = View.GONE
                    binding.llAlcoholDuration.visibility = View.GONE
                    binding.llAlcoholFrequency.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSmokingHistory() {
        binding.rgSmokingHistoryOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioSmoker.id -> {
                    binding.llSmokingDuration.visibility = View.VISIBLE
                    binding.llSmokingRate.visibility = View.VISIBLE
                    binding.llSmokingFrequency.visibility = View.VISIBLE
                }

                else -> {
                    binding.llSmokingDuration.visibility = View.GONE
                    binding.llSmokingRate.visibility = View.GONE
                    binding.llSmokingFrequency.visibility = View.GONE
                }
            }
        }
    }

    private fun setInputTextChangedListener() {
        binding.tilSurgeryReasonOption.hideErrorOnTextChang(binding.etSurgeryReasonCheck)
    }

    private fun setupHbCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.hb_check)
        binding.acHbCheck.setAdapter(adapter)

        binding.acHbCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilHbCheckOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acHbCheck.setText(this.getStringArray(R.array.hb_check)[i], false)
            }
        }
    }

    private fun setupBpCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.bp_check)
        binding.acBpCheck.setAdapter(adapter)

        binding.acBpCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilBpCheckOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acBpCheck.setText(this.getStringArray(R.array.bp_check)[i], false)
            }
        }
    }

    private fun setupSugarCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.sugar_check)
        binding.acSugarCheck.setAdapter(adapter)

        binding.acSugarCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilSugarCheckOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acSugarCheck.setText(this.getStringArray(R.array.sugar_check)[i], false)
            }
        }
    }

    private fun setupSurgeries() {
        binding.rgSurgeryOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioSurgeryYes -> {
                    binding.llSurgeryReasonCheck.visibility = View.VISIBLE
                }

                R.id.radioSurgeryNo -> {
                    binding.llSurgeryReasonCheck.visibility = View.GONE
                }
            }
        }
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            BaselineMedicalFragmentDirections.navigationMedicalToGeneral().apply {
                findNavController().navigate(this)
            }
        }
        binding.frag2BtnNext.setOnClickListener {
            validateForm { saveSurveyData() }
        }
    }

    private fun saveSurveyData() {
        baselineSurveyData.apply {

            hbCheck = binding.acHbCheck.text.toString()
            bpCheck = binding.acBpCheck.text.toString()
            sugarCheck = binding.acSugarCheck.text.toString()
            bpValue = binding.rgBpOptions.getSelectedData()
            diabetesValue = binding.rgDiabetesOptions.getSelectedData()
            arthritisValue = binding.rgArthritisOptions.getSelectedData()
            anemiaValue = binding.rgAnemiaOptions.getSelectedData()
            surgeryValue = binding.rgSurgeryOptions.getSelectedData()
            surgeryReason = binding.etSurgeryReasonCheck.text.toString()
                .storeReasonIfAnswerIsPositive(surgeryValue)
            smokingHistory = binding.rgSmokingHistoryOptions.getSelectedData()
            smokingRate = binding.rgSmokingRateOptions.getSelectedData()
            smokingDuration = binding.rgSmokingDurationOptions.getSelectedData()
            smokingFrequency = binding.rgSmokingFrequencyOptions.getSelectedData()
            chewTobacco = binding.rgChewTobaccoOptions.getSelectedData()
            alcoholHistory = binding.rgAlcoholHistoryOptions.getSelectedData()
            alcoholRate = binding.rgAlcoholRateOptions.getSelectedData()
            alcoholDuration = binding.rgAlcoholDurationOptions.getSelectedData()
            alcoholFrequency = binding.rgAlcoholFrequencyOptions.getSelectedData()

            baselineSurveyViewModel.updateBaselineData(this)
            BaselineMedicalFragmentDirections.navigationMedicalToOther().apply {
                findNavController().navigate(this)
            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        val error = R.string.this_field_is_mandatory

        binding.medicalConfig?.let {
            val hbCheck =
                if (it.hbCheck!!.isEnabled && it.hbCheck!!.isMandatory && isAgeGreaterThan18) {
                    binding.tilHbCheckOption.validateDropDowb(binding.acHbCheck, error)
                } else true

            val bpCheck =
                if (it.bpCheck!!.isEnabled && it.bpCheck!!.isMandatory && isAgeGreaterThan18) {
                    binding.tilBpCheckOption.validateDropDowb(binding.acHbCheck, error)
                } else true

            val sugarCheck =
                if (it.sugarCheck!!.isEnabled && it.sugarCheck!!.isMandatory && isAgeGreaterThan18) {
                    binding.tilSugarCheckOption.validateDropDowb(binding.acSugarCheck, error)
                } else true

            val bpValue = if (it.bpValue!!.isEnabled && it.bpValue!!.isMandatory) {
                binding.rgBpOptions.validate()
            } else true

            val diabetesValue =
                if (it.diabetesValue!!.isEnabled && it.diabetesValue!!.isMandatory) {
                    binding.rgDiabetesOptions.validate()
                } else true

            val arthritisValue =
                if (it.arthritisValue!!.isEnabled && it.arthritisValue!!.isMandatory) {
                    binding.rgArthritisOptions.validate()
                } else true

            val anemiaValue = if (it.anemiaValue!!.isEnabled && it.anemiaValue!!.isMandatory) {
                binding.rgAnemiaOptions.validate()
            } else true

            val surgeryValue = if (it.surgeryValue!!.isEnabled && it.surgeryValue!!.isMandatory) {
                binding.rgSurgeryOptions.validate()
            } else true

            val surgeryReason =
                if (it.surgeryReason!!.isEnabled && it.surgeryValue!!.isMandatory && binding.llSurgeryReasonCheck.isVisible) {
                    binding.tilSurgeryReasonOption.validate(binding.etSurgeryReasonCheck, error)
                } else true

            val smokingHistory =
                if (it.smokingHistory!!.isEnabled && it.smokingHistory!!.isMandatory) {
                    binding.rgSmokingHistoryOptions.validate()
                } else true

            val smokingRate =
                if (it.smokingRate!!.isEnabled && it.smokingRate!!.isMandatory && binding.llSmokingRate.isVisible) {
                    binding.rgSmokingRateOptions.validate()
                } else true

            val smokingDuration =
                if (it.smokingHistory!!.isEnabled && it.smokingHistory!!.isMandatory && binding.llSmokingDuration.isVisible) {
                    binding.rgSmokingDurationOptions.validate()
                } else true

            val smokingFrequency =
                if (it.smokingFrequency!!.isEnabled && it.smokingFrequency!!.isMandatory && binding.llSmokingFrequency.isVisible) {
                    binding.rgSmokingFrequencyOptions.validate()
                } else true

            val chewTobacco = if (it.chewTobacco!!.isEnabled && it.chewTobacco!!.isMandatory) {
                binding.rgChewTobaccoOptions.validate()
            } else false

            val alcoholHistory =
                if (it.alcoholHistory!!.isEnabled && it.alcoholHistory!!.isMandatory) {
                    binding.rgAlcoholHistoryOptions.validate()
                } else true

            val alcoholRate =
                if (it.alcoholRate!!.isEnabled && it.alcoholRate!!.isMandatory && binding.llAlcoholRate.isVisible) {
                    binding.rgAlcoholRateOptions.validate()
                } else true

            val alcoholDuration =
                if (it.alcoholDuration!!.isEnabled && it.alcoholDuration!!.isMandatory && binding.llAlcoholDuration.isVisible) {
                    binding.rgAlcoholDurationOptions.validate()
                } else true

            val alcoholFrequency =
                if (it.alcoholFrequency!!.isEnabled && it.alcoholFrequency!!.isMandatory && binding.llAlcoholFrequency.isVisible) {
                    binding.rgAlcoholFrequencyOptions.validate()
                } else true

            if (hbCheck.and(bpCheck).and(sugarCheck).and(bpValue).and(diabetesValue)
                    .and(arthritisValue).and(anemiaValue).and(surgeryValue).and(surgeryReason)
                    .and(smokingHistory).and(smokingRate).and(smokingDuration).and(smokingFrequency)
                    .and(chewTobacco).and(alcoholHistory).and(alcoholRate).and(alcoholDuration)
                    .and(alcoholFrequency)
            ) {
                block.invoke()
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.please_select_all_the_required_fields),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}