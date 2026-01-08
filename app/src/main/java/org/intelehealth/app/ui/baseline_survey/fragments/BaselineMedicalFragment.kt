package org.intelehealth.app.ui.baseline_survey.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.StaticPatientRegistrationEnabledFieldsHelper
import org.intelehealth.app.databinding.FragmentBaselineSurveyMedicalBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.ui.baseline_survey.config.MedicalBaselineConfig
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.filter.AllowAllLettersInputFilter
import org.intelehealth.app.ui.filter.LettersNumbersSelectedSymbolsInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.getSelectedData
import org.intelehealth.app.utilities.extensions.getSelectedDataInEnglishLocale
import org.intelehealth.app.utilities.extensions.getTextInEnglish
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.storeReasonIfAnswerIsPositive
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDropDowb
import java.util.Locale

/**
 * Created by Shazzad H Kanon on 06-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/


class BaselineMedicalFragment :
    BaseFragmentBaselineSurvey(R.layout.fragment_baseline_survey_medical) {

    private lateinit var binding: FragmentBaselineSurveyMedicalBinding
    private var isAgeGreaterThan18: Boolean = false
    var selectedBPNoMedicationReason: String = ""
    var selectedAnemiaNoMedicationReason: String = ""
    private var isAgeGreaterThan11: Boolean = false
    var selectedDiabetesNoMedicationReason: String = ""
    private var isAgeGreaterThan20: Boolean = false

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

        val reasons = R.array.reason_for_not_taking_bp_medication

        setupAutoCompleteWithOther(
            autoCompleteTextView = binding.layoutBpMedication.autotvReasonForNotTakingBpMedication,
            layoutOtherReason = binding.layoutBpMedication.layoutBpOtherReasonNotTaking,
            savedValue = baselineData.reasonForNotTakingBPMedication,
            stringArrayResId = reasons,
            editText = binding.layoutBpMedication.etBpMedicationNotTakingOtherReason
        ){ englishValue ->
            selectedBPNoMedicationReason = englishValue
            baselineData.reasonForNotTakingBPMedication = englishValue
        }
        setupAutoCompleteWithOther(
            autoCompleteTextView = binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication,
            layoutOtherReason = binding.layoutAnemiaMedication.layoutAnemiaOtherReasonNotTaking,
            savedValue = baselineData.reasonForNotTakingAnemiaMedication,
            stringArrayResId = reasons,
            editText = binding.layoutAnemiaMedication.etAnemiaMedicationNotTakingOtherReason
        ){ englishValue ->
            selectedAnemiaNoMedicationReason = englishValue
            baselineData.reasonForNotTakingAnemiaMedication = englishValue
        }
        setupAutoCompleteWithOther(
            autoCompleteTextView = binding.layoutDiabetesMedication.autotvReasonForNotTakingDiabetesMedication,
            layoutOtherReason = binding.layoutDiabetesMedication.layoutDiabetesOtherReasonNotTaking,
            savedValue = baselineData.reasonForNotTakingDiabetesMedication,
            stringArrayResId = reasons,
            editText = binding.layoutDiabetesMedication.etDiabetesMedicationNotTakingOtherReason
        ) { englishValue ->
            selectedDiabetesNoMedicationReason = englishValue
            baselineData.reasonForNotTakingDiabetesMedication = englishValue
        }

    }

    private fun checkPatientAge() {
        baselineSurveyViewModel
            .getPatientAge(baselineSurveyViewModel.patientId)
            .observe(viewLifecycleOwner) {
                it ?: return@observe
                baselineSurveyViewModel.handleResponse(it) { age ->
                    setUp18Fields(age)
                    setUp11Fields(age)
                    setUp20Fields(age)
                }
            }
    }


    private fun setUp18Fields(age: Int) {
        if (age >= 18) {
            isAgeGreaterThan18 = true
            return
        }

        //binding.llHbCheck.visibility = View.GONE
        binding.llBpCheck.visibility = View.GONE
        //binding.llSugarCheck.visibility = View.GONE
        binding.layoutBpMedication.llBPLabel.visibility = View.GONE
    }


    private fun fetchMedicalBaselineConfig() {
        val it = getStaticPatientRegistrationFields()
        binding.medicalConfig = PatientRegFieldsUtils.buildMedicalBaselineConfig(it)
        setValues()
        setClickListener()
        setupFilter()
        initializeRadioButtonTags()
    }

    private fun initializeRadioButtonTags() {
        //binding.radioBpYes.tag = R.string.yes
        ///binding.radioBpNo.tag = R.string.no

        //binding.radioDiabetesYes.tag = R.string.yes
        //binding.radioDiabetesNo.tag = R.string.no

        binding.radioArthritisYes.tag = R.string.yes
        binding.radioArthritisNo.tag = R.string.no

        binding.layoutAnemiaMedication.radioAnemiaYes.tag = R.string.yes
        binding.layoutAnemiaMedication.radioAnemiaNo.tag = R.string.no

        binding.radioSurgeryYes.tag = R.string.yes
        binding.radioSurgeryNo.tag = R.string.no

        binding.radioSmoker.tag = R.string. smoker
        binding.radioNonSmoker.tag = R.string.non_smoker
        binding.radioDeclined.tag = R.string.denied_to_answer

        binding.radioSmokingRate1.tag = R.string.less_than_5_bidis_or_cigarette
        binding.radioSmokingRate2.tag = R.string.five_to_ten_bidis_or_cigarette
        binding.radioSmokingRate3.tag = R.string.more_than_10_bidis_or_cigarette

        binding.radioSmokingDuration1.tag = R.string.less_than_a_year
        binding.radioSmokingDuration2.tag = R.string.from_1_year_to_5_years
        binding.radioSmokingDuration3.tag = R.string.from_5_year_to_10_years
        binding.radioSmokingDuration4.tag = R.string.more_than_10_years

        binding.radioSmokingFrequency1.tag = R.string.frequency_daily
        binding.radioSmokingFrequency2.tag = R.string.frequency_once_a_week
        binding.radioSmokingFrequency3.tag = R.string.frequency_twice_a_week
        binding.radioSmokingFrequency4.tag = R.string.frequency_occasionally

        binding.radioChewYes.tag = R.string.yes
        binding.radioChewNo.tag = R.string.no
        binding.radioChewDeclined.tag = R.string.denied_to_answer

        binding.radioAlcoholHistoryYes.tag = R.string.yes
        binding.radioAlcoholHistoryNo.tag = R.string.no
        binding.radioAlcoholHistoryDeclined.tag = R.string.denied_to_answer

        binding.radioAlcoholHistoryYes.tag = R.string.yes
        binding.radioAlcoholHistoryNo.tag = R.string.no
        binding.radioAlcoholHistoryDeclined.tag = R.string.denied_to_answer

        binding.radioAlcoholRate1.tag = R.string.zero_fifty_ml
        binding.radioAlcoholRate2.tag = R.string.fifty_hundred_ml
        binding.radioAlcoholRate3.tag = R.string.hundred_two_hundred_fifty_ml
        binding.radioAlcoholRate4.tag = R.string.two_fifty_five_hundred_ml
        binding.radioAlcoholRate5.tag = R.string.more_than_five_hundred_ml

        binding.radioAlcoholDuration1.tag = R.string.less_than_a_year
        binding.radioAlcoholDuration2.tag = R.string.from_1_year_to_5_years
        binding.radioAlcoholDuration3.tag = R.string.from_5_year_to_10_years
        binding.radioAlcoholDuration4.tag = R.string.more_than_10_years

        binding.radioAlcoholFrequency1.tag = R.string.daily
        binding.radioAlcoholFrequency2.tag = R.string.frequency_once_a_week
        binding.radioAlcoholFrequency3.tag = R.string.frequency_twice_a_week
        binding.radioAlcoholFrequency4.tag = R.string.frequency_occasionally

        binding.layoutAnemiaMedication.radioTakingMedicationAnemiaYes.tag = R.string.yes
        binding.layoutAnemiaMedication.radioTakingMedicationAnemiaNo.tag = R.string.no

        binding.layoutAnemiaMedication.radioAnemiaSeenByHwYes.tag = R.string.yes
        binding.layoutAnemiaMedication.radioAnemiaSeenByHwNo.tag = R.string.no

        binding.layoutBpMedication.radioBPYes.tag = R.string.yes
        binding.layoutBpMedication.radioBPNo.tag = R.string.no

        binding.layoutBpMedication.radioTakingMedicationBpYes.tag = R.string.yes
        binding.layoutBpMedication.radioTakingMedicationBpNo.tag = R.string.no

        binding.layoutBpMedication.radioBpSeenByHwYes.tag = R.string.yes
        binding.layoutBpMedication.radioBpSeenByHwNo.tag = R.string.no

        binding.layoutDiabetesMedication.radioDiabetesYes.tag = R.string.yes
        binding.layoutDiabetesMedication.radioDiabetesNo.tag = R.string.no

        binding.layoutDiabetesMedication.radioTakingMedicationDiabetesYes.tag = R.string.yes
        binding.layoutDiabetesMedication.radioTakingMedicationDiabetesNo.tag = R.string.no

        binding.layoutDiabetesMedication.radioDiabetesSeenByHwYes.tag = R.string.yes
        binding.layoutDiabetesMedication.radioDiabetesSeenByHwNo.tag = R.string.no


    }

    private fun setupFilter() {
        binding.etSurgeryReasonCheck.apply {
            addFilter(FirstLetterUpperCaseInputFilter())
            addFilter(AllowAllLettersInputFilter())
        }
        binding.layoutAnemiaMedication.etAnemiaMedicationNotTakingOtherReason.apply {
            addFilter(FirstLetterUpperCaseInputFilter())
            addFilter(LettersNumbersSelectedSymbolsInputFilter())
        }
        binding.layoutBpMedication.etBpMedicationNotTakingOtherReason.apply {
            addFilter(FirstLetterUpperCaseInputFilter())
            addFilter(LettersNumbersSelectedSymbolsInputFilter())
        }

        binding.layoutDiabetesMedication.etDiabetesMedicationNotTakingOtherReason.apply {
            addFilter(FirstLetterUpperCaseInputFilter())
            addFilter(LettersNumbersSelectedSymbolsInputFilter())
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
        anemiaHistory()
        bpHistory()
        diabetesHistory()
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
            binding.acHbCheck.setText(resources.getStringArray(R.array.hb_check)[i], false)
        }
    }

    private fun setupBpCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.bp_check)
        binding.acBpCheck.setAdapter(adapter)

        binding.acBpCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilBpCheckOption.hideError()
            binding.acBpCheck.setText(resources.getStringArray(R.array.bp_check)[i], false)
        }
    }

    private fun setupSugarCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.sugar_check)
        binding.acSugarCheck.setAdapter(adapter)

        binding.acSugarCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilSugarCheckOption.hideError()
            binding.acSugarCheck.setText(resources.getStringArray(R.array.sugar_check)[i], false)
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

            hbCheck = binding.acHbCheck.getTextInEnglish(requireContext(), R.array.hb_check)
            bpCheck = binding.acBpCheck.getTextInEnglish(requireContext(), R.array.bp_check)
            sugarCheck = binding
                .acSugarCheck
                .getTextInEnglish(requireContext(), R.array.sugar_check)

           // bpValue = binding.rgBpOptions.getSelectedDataInEnglishLocale(requireContext())
           /* diabetesValue =
                binding.rgDiabetesOptions.getSelectedDataInEnglishLocale(requireContext())*/
            diabetesValue = binding.layoutDiabetesMedication.rgDiabetesOptions.getSelectedDataInEnglishLocale(requireContext())

            arthritisValue =
                binding.rgArthritisOptions.getSelectedDataInEnglishLocale(requireContext())
            anemiaValue = binding.layoutAnemiaMedication.rgAnemiaOptions.getSelectedDataInEnglishLocale(requireContext())
            surgeryValue = binding.rgSurgeryOptions.getSelectedDataInEnglishLocale(requireContext())
            surgeryReason = binding.etSurgeryReasonCheck.text.toString()
                .storeReasonIfAnswerIsPositive(surgeryValue)

            smokingHistory =
                binding.rgSmokingHistoryOptions.getSelectedDataInEnglishLocale(requireContext())
            smokingRate =
                binding.rgSmokingRateOptions.getSelectedDataInEnglishLocale(requireContext())
            smokingDuration =
                binding.rgSmokingDurationOptions.getSelectedDataInEnglishLocale(requireContext())
            smokingFrequency =
                binding.rgSmokingFrequencyOptions.getSelectedDataInEnglishLocale(requireContext())

            chewTobacco =
                binding.rgChewTobaccoOptions.getSelectedDataInEnglishLocale(requireContext())
            alcoholHistory =
                binding.rgAlcoholHistoryOptions.getSelectedDataInEnglishLocale(requireContext())
            alcoholRate =
                binding.rgAlcoholRateOptions.getSelectedDataInEnglishLocale(requireContext())
            alcoholDuration =
                binding.rgAlcoholDurationOptions.getSelectedDataInEnglishLocale(requireContext())
            alcoholFrequency =
                binding.rgAlcoholFrequencyOptions.getSelectedDataInEnglishLocale(requireContext())

            //newly added for ncd protocols
            takingAnyMedicationForAnemia =  binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.getSelectedDataInEnglishLocale(requireContext())
            haveYouSeenToHWinPastOneYearForAnemia = binding.layoutAnemiaMedication.rgAnemiaSeenByHwMedicationYes.getSelectedDataInEnglishLocale(requireContext())
            reasonForNotTakingAnemiaMedication = getNotTakingMedicationReasonForDb()

            bpValue = binding.layoutBpMedication.rgBpOptions.getSelectedDataInEnglishLocale(requireContext())
            takingAnyMedicationForBP =  binding.layoutBpMedication.rgBpTakingMedicationOptions.getSelectedDataInEnglishLocale(requireContext())
            haveYouSeenToHWinPastOneYearForBP = binding.layoutBpMedication.rgBpSeenByHwMedicationYes.getSelectedDataInEnglishLocale(requireContext())
            reasonForNotTakingBPMedication = getNotTakingBpMedicationReasonForDb()

            takingAnyMedicationForDiabetes = binding.layoutDiabetesMedication.rgDiabetesTakingMedicationOptions.getSelectedDataInEnglishLocale(requireContext())
            haveYouSeenToHWinPastOneYearForDiabetes = binding.layoutDiabetesMedication.rgDiabetesSeenByHwMedicationYes.getSelectedDataInEnglishLocale(requireContext())
            reasonForNotTakingDiabetesMedication = getNotTakingDiabetesMedicationReasonForDb()

            Log.d("kktest", "saveSurveyData: baseline data : "+Gson().toJson(this))

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
                if (it.hbCheck!!.isEnabled && it.hbCheck!!.isMandatory && isAgeGreaterThan11) {
                    binding.tilHbCheckOption.validateDropDowb(binding.acHbCheck, error)
                } else true

            val bpCheck =
                if (it.bpCheck!!.isEnabled && it.bpCheck!!.isMandatory && isAgeGreaterThan18) {
                    binding.tilBpCheckOption.validateDropDowb(binding.acBpCheck, error)
                } else true

            val sugarCheck =
                if (it.sugarCheck!!.isEnabled && it.sugarCheck!!.isMandatory && isAgeGreaterThan20) {
                    binding.tilSugarCheckOption.validateDropDowb(binding.acSugarCheck, error)
                } else true

           /* val bpValue = if (it.bpValue!!.isEnabled && it.bpValue!!.isMandatory) {
                binding.rgBpOptions.validate()
            } else true*/

           /* val diabetesValue =
                if (it.diabetesValue!!.isEnabled && it.diabetesValue!!.isMandatory) {
                    binding.rgDiabetesOptions.validate()
                } else true*/

            val arthritisValue =
                if (it.arthritisValue!!.isEnabled && it.arthritisValue!!.isMandatory) {
                    binding.rgArthritisOptions.validate()
                } else true

         /*   val anemiaValue = if (it.anemiaValue!!.isEnabled && it.anemiaValue!!.isMandatory) {
                binding.layoutAnemiaMedication.rgAnemiaOptions.validate()
            } else true*/

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

       /*     //BP

            // BP - Q1: Do you have bp?
            val bpValue = if (it.bpValue!!.isEnabled && it.bpValue!!.isMandatory && isAgeGreaterThan18) {
                binding.layoutBpMedication.rgBpOptions.validate()
            } else true

            // BP - Q2: Taking any medication?
            val takingAnyMedicationForBP =
                if (it.takingAnyMedicationForBP!!.isEnabled &&
                    it.takingAnyMedicationForBP!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpMedication.isVisible
                ) {
                    binding.layoutBpMedication.rgBpTakingMedicationOptions.validate()
                } else true

            // BP - Q3: Seen by health worker in past year?
            val haveYouSeenToHWinPastOneYearForBP =
                if (it.haveYouSeenToHWinPastOneYearForBP!!.isEnabled &&
                    it.haveYouSeenToHWinPastOneYearForBP!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpSeenByHw.isVisible
                ) {
                    binding.layoutBpMedication.rgBpSeenByHwMedicationYes.validate()
                } else true

            // BP - Q4: Reason for not taking medication
            val reasonForNotTakingBpMedication =
                if (it.reasonForNotTakingBPMedication!!.isEnabled &&
                    it.reasonForNotTakingBPMedication!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpReasonNotTaking.isVisible
                ) {
                    val isValidDropdown = binding.layoutBpMedication
                        .autotvlayoutReasonForNotTakingBpMedication
                        .validateDropDowb(
                            binding.layoutBpMedication.autotvReasonForNotTakingBpMedication,
                            error
                        )

                    if (isValidDropdown) {
                        val selectedValue = binding.layoutBpMedication
                            .autotvReasonForNotTakingBpMedication.text?.toString()?.trim()

                        if (selectedBPNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
                            // If "Unknown / Other" → EditText should not be empty
                            val otherReason = binding.layoutBpMedication.etBpMedicationNotTakingOtherReason.text?.toString()?.trim()

                            if (otherReason.isNullOrEmpty()) {
                                binding.layoutBpMedication.tilBpMedicationNotTakingOtherReason.error =getString(R.string.please_enter_reason_txt)
                                false
                            } else true
                        } else {
                            // For any other option, no need to check EditText
                            true
                        }
                    } else false
                } else true
*/

            // Anemia

            // Anemia - Q1: Do you have anemia?
            val anemiaValue = if (it.anemiaValue!!.isEnabled && it.anemiaValue!!.isMandatory && isAgeGreaterThan11) {
                binding.layoutAnemiaMedication.rgAnemiaOptions.validate()
            } else true

         /*   // Anemia - Q2: Taking any medication?
            val takingAnyMedicationForAnemia =
                if (it.takingAnyMedicationForAnemia!!.isEnabled &&
                    it.takingAnyMedicationForAnemia!!.isMandatory &&
                    binding.layoutAnemiaMedication.layoutAnemiaMedication.isVisible
                ) {
                    binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.validate()
                } else true*/
            // Anemia - Q2: Taking any medication?
            val takingAnyMedicationForAnemia =
                if (!isAgeGreaterThan11) {
                    true
                } else if (
                    it.takingAnyMedicationForAnemia!!.isEnabled &&
                    it.takingAnyMedicationForAnemia!!.isMandatory &&
                    binding.layoutAnemiaMedication.layoutAnemiaMedication.isVisible
                ) {
                    binding.layoutAnemiaMedication
                        .rgAnemiaTakingMedicationOptions
                        .validate()
                } else {
                    true
                }


            // Anemia - Q3: Seen by health worker in past year?
            val haveYouSeenToHWinPastOneYearForAnemia =
                if (it.haveYouSeenToHWinPastOneYearForAnemia!!.isEnabled &&
                    it.haveYouSeenToHWinPastOneYearForAnemia!!.isMandatory &&
                    binding.layoutAnemiaMedication.layoutAnemiaSeenByHw.isVisible
                ) {
                    binding.layoutAnemiaMedication.rgAnemiaSeenByHwMedicationYes.validate()
                } else true

            // Anemia - Q4: Reason for not taking medication
            val reasonForNotTakingAnemiaMedication =
                if (it.reasonForNotTakingAnemiaMedication!!.isEnabled &&
                    it.reasonForNotTakingAnemiaMedication!!.isMandatory &&
                    binding.layoutAnemiaMedication.layoutAnemiaReasonNotTaking.isVisible
                ) {
                    val isValidDropdown = binding.layoutAnemiaMedication
                        .autotvlayoutReasonForNotTakingAnemiaMedication
                        .validateDropDowb(
                            binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication,
                            error
                        )

                    if (isValidDropdown) {
                        val selectedValue = binding.layoutAnemiaMedication
                            .autotvReasonForNotTakingAnemiaMedication.text?.toString()?.trim()

                        if (selectedAnemiaNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
                            // If "Unknown / Other" → EditText should not be empty
                            val otherReason = binding.layoutAnemiaMedication
                                .etAnemiaMedicationNotTakingOtherReason.text?.toString()?.trim()

                            if (otherReason.isNullOrEmpty()) {
                                binding.layoutAnemiaMedication
                                    .tilAnemiaMedicationNotTakingOtherReason.error =
                                    getString(R.string.please_enter_reason_txt)
                                false
                            } else true
                        } else {
                            // For any other option, no need to check EditText
                            true
                        }
                    } else false
                } else true


            // bp

            // bp - Q1: Do you have bp?
            val bpValue = if (it.bpValue!!.isEnabled && it.bpValue!!.isMandatory && isAgeGreaterThan18) {
                binding.layoutBpMedication.rgBpOptions.validate()
            } else true

            // bp - Q2: Taking any medication?
           /* val takingAnyMedicationForBP =
                if (it.takingAnyMedicationForBP!!.isEnabled &&
                    it.takingAnyMedicationForBP!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpMedication.isVisible
                ) {
                    binding.layoutBpMedication.rgBpTakingMedicationOptions.validate()
                } else true*/
            val takingAnyMedicationForBP =
                if (!isAgeGreaterThan18) {
                    true
                } else if (
                    it.takingAnyMedicationForBP!!.isEnabled &&
                    it.takingAnyMedicationForBP!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpMedication.isVisible
                ) {
                    binding.layoutBpMedication
                        .rgBpTakingMedicationOptions
                        .validate()
                } else {
                    true
                }


            // bp - Q3: Seen by health worker in past year?
            val haveYouSeenToHWinPastOneYearForBP =
                if (it.haveYouSeenToHWinPastOneYearForBP!!.isEnabled &&
                    it.haveYouSeenToHWinPastOneYearForBP!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpSeenByHw.isVisible
                ) {
                    binding.layoutBpMedication.rgBpSeenByHwMedicationYes.validate()
                } else true

            // bp - Q4: Reason for not taking medication
            val reasonForNotTakingBPMedication =
                if (it.reasonForNotTakingBPMedication!!.isEnabled &&
                    it.reasonForNotTakingBPMedication!!.isMandatory &&
                    binding.layoutBpMedication.layoutBpReasonNotTaking.isVisible
                ) {
                    val isValidDropdown = binding.layoutBpMedication
                        .autotvlayoutReasonForNotTakingBpMedication
                        .validateDropDowb(
                            binding.layoutBpMedication.autotvReasonForNotTakingBpMedication,
                            error
                        )

                    if (isValidDropdown) {
                        val selectedValue = binding.layoutBpMedication
                            .autotvReasonForNotTakingBpMedication.text?.toString()?.trim()

                        if (selectedBPNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
                            // If "Unknown / Other" → EditText should not be empty
                            val otherReason = binding.layoutBpMedication
                                .etBpMedicationNotTakingOtherReason.text?.toString()?.trim()

                            if (otherReason.isNullOrEmpty()) {
                                binding.layoutBpMedication
                                    .tilBpMedicationNotTakingOtherReason.error =
                                    getString(R.string.please_enter_reason_txt)
                                false
                            } else true
                        } else {
                            // For any other option, no need to check EditText
                            true
                        }
                    } else false
                } else true

            //val isDiabetesValid = validateDiabetesSection(it)
            // Diabetes - Q1: Do you have diabetes?
            val diabetesValue =
                if (it.diabetesValue!!.isEnabled && it.diabetesValue!!.isMandatory && isAgeGreaterThan20) {
                    binding.layoutDiabetesMedication.rgDiabetesOptions.validate()
                } else true

            // Diabetes - Q2: Taking any medication?
           /* val takingAnyMedicationForDiabetes =
                if (it.takingAnyMedicationForDiabetes!!.isEnabled &&
                    it.takingAnyMedicationForDiabetes!!.isMandatory &&
                    binding.layoutDiabetesMedication.layoutDiabetesMedication.isVisible
                ) {
                    binding.layoutDiabetesMedication.rgDiabetesTakingMedicationOptions.validate()
                } else true*/
            val takingAnyMedicationForDiabetes =
                if (!isAgeGreaterThan20) {
                    true
                } else if (
                    it.takingAnyMedicationForDiabetes!!.isEnabled &&
                    it.takingAnyMedicationForDiabetes!!.isMandatory &&
                    binding.layoutDiabetesMedication.layoutDiabetesMedication.isVisible
                ) {
                    binding.layoutDiabetesMedication
                        .rgDiabetesTakingMedicationOptions
                        .validate()
                } else {
                    true
                }


            // Diabetes - Q3: Seen by health worker in past year?
            val haveYouSeenToHWinPastOneYearForDiabetes =
                if (it.haveYouSeenToHWinPastOneYearForDiabetes!!.isEnabled &&
                    it.haveYouSeenToHWinPastOneYearForDiabetes!!.isMandatory &&
                    binding.layoutDiabetesMedication.layoutDiabetesSeenByHw.isVisible
                ) {
                    binding.layoutDiabetesMedication.rgDiabetesSeenByHwMedicationYes.validate()
                } else true

            // Diabetes - Q4: Reason for not taking medication
            val reasonForNotTakingDiabetesMedication =
                if (it.reasonForNotTakingDiabetesMedication!!.isEnabled &&
                    it.reasonForNotTakingDiabetesMedication!!.isMandatory &&
                    binding.layoutDiabetesMedication.layoutDiabetesReasonNotTaking.isVisible
                ) {
                    val isValidDropdown = binding.layoutDiabetesMedication
                        .autotvlayoutReasonForNotTakingDiabetesMedication
                        .validateDropDowb(
                            binding.layoutDiabetesMedication.autotvReasonForNotTakingDiabetesMedication,
                            error
                        )

                    if (isValidDropdown) {
                        val selectedValue = binding.layoutDiabetesMedication
                            .autotvReasonForNotTakingDiabetesMedication.text?.toString()?.trim()

                        if (selectedDiabetesNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
                            val otherReason = binding.layoutDiabetesMedication
                                .etDiabetesMedicationNotTakingOtherReason.text?.toString()?.trim()

                            if (otherReason.isNullOrEmpty()) {
                                binding.layoutDiabetesMedication
                                    .tilDiabetesMedicationNotTakingOtherReason.error =
                                    getString(R.string.please_enter_reason_txt)
                                false
                            } else true
                        } else {
                            true
                        }
                    } else false
                } else true


            if (hbCheck.and(bpCheck).and(sugarCheck)
                    .and(arthritisValue).and(anemiaValue).and(surgeryValue).and(surgeryReason)
                    .and(smokingHistory).and(smokingRate).and(smokingDuration).and(smokingFrequency)
                    .and(chewTobacco).and(alcoholHistory).and(alcoholRate).and(alcoholDuration)
                    .and(alcoholFrequency).and(bpValue)
                    .and(takingAnyMedicationForBP)
                    .and(haveYouSeenToHWinPastOneYearForBP)
                    .and(reasonForNotTakingBPMedication)
                    .and(takingAnyMedicationForAnemia).and(haveYouSeenToHWinPastOneYearForAnemia).and(reasonForNotTakingAnemiaMedication).and(diabetesValue)
                    .and(takingAnyMedicationForDiabetes).and(haveYouSeenToHWinPastOneYearForDiabetes).and(reasonForNotTakingDiabetesMedication)
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
    private fun anemiaHistory() {
        with(binding.layoutAnemiaMedication) {

            // Q1: Do you have anemia?
            rgAnemiaOptions.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == radioAnemiaYes.id) {
                    layoutAnemiaMedication.visibility = View.VISIBLE
                } else {
                    // Clear values
                    rgAnemiaTakingMedicationOptions.clearCheck()
                    rgAnemiaSeenByHwMedicationYes.clearCheck()
                    autotvReasonForNotTakingAnemiaMedication.text = null
                    etAnemiaMedicationNotTakingOtherReason.setText("")

                    layoutAnemiaMedication.visibility = View.GONE
                    layoutAnemiaSeenByHw.visibility = View.GONE
                    layoutAnemiaReasonNotTaking.visibility = View.GONE
                    layoutAnemiaOtherReasonNotTaking.visibility = View.GONE


                }
            }

            // Q2: Taking any medication?
            rgAnemiaTakingMedicationOptions.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == radioTakingMedicationAnemiaYes.id) {
                    layoutAnemiaSeenByHw.visibility = View.VISIBLE
                    layoutAnemiaReasonNotTaking.visibility = View.GONE
                    layoutAnemiaOtherReasonNotTaking.visibility = View.GONE
                    autotvReasonForNotTakingAnemiaMedication.text = null
                    etAnemiaMedicationNotTakingOtherReason.setText("")
                } else if (checkedId == radioTakingMedicationAnemiaNo.id) {
                    layoutAnemiaReasonNotTaking.visibility = View.VISIBLE
                    layoutAnemiaSeenByHw.visibility = View.GONE
                    rgAnemiaSeenByHwMedicationYes.clearCheck()
                }
            }

            // Dropdown adapter
            val adapter = ArrayAdapterUtils.getArrayAdapter(
                requireContext(),
                R.array.reason_for_not_taking_bp_medication
            )
            autotvReasonForNotTakingAnemiaMedication.setAdapter(adapter)

            // Handle dropdown selection
            autotvReasonForNotTakingAnemiaMedication.setOnItemClickListener { _, _, i, _ ->
                autotvlayoutReasonForNotTakingAnemiaMedication.hideError()

                val selectedText = resources.getStringArray(R.array.reason_for_not_taking_bp_medication)[i]
                autotvReasonForNotTakingAnemiaMedication.setText(selectedText, false)

                if (selectedText.equals("Unknown / Other", ignoreCase = true)) {
                    //  Show "Other reason" input
                    layoutAnemiaOtherReasonNotTaking.visibility = View.VISIBLE
                } else {
                    //  Hide & clear "Other reason" input
                    layoutAnemiaOtherReasonNotTaking.visibility = View.GONE
                    etAnemiaMedicationNotTakingOtherReason.setText("")
                }
            }
        }
    }

    private fun getNotTakingMedicationReasonForDb(): String {
       // val selected = binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication.text.toString()
        return if (selectedAnemiaNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
            "Unknown / Other:reason ${binding.layoutAnemiaMedication.etAnemiaMedicationNotTakingOtherReason.text}"
        } else
            selectedAnemiaNoMedicationReason

    }
    private fun bpHistory() {
        with(binding.layoutBpMedication) {

            // Q1: Do you have anemia?
            rgBpOptions.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == radioBPYes.id) {
                    layoutBpMedication.visibility = View.VISIBLE
                } else {
                    // Clear values
                    rgBpTakingMedicationOptions.clearCheck()
                    rgBpSeenByHwMedicationYes.clearCheck()
                    autotvReasonForNotTakingBpMedication.text = null
                    etBpMedicationNotTakingOtherReason.setText("")

                    layoutBpMedication.visibility = View.GONE
                    layoutBpSeenByHw.visibility = View.GONE
                    layoutBpReasonNotTaking.visibility = View.GONE
                    layoutBpOtherReasonNotTaking.visibility = View.GONE


                }
            }

            // Q2: Taking any medication?
            rgBpTakingMedicationOptions.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == radioTakingMedicationBpYes.id) {
                    layoutBpSeenByHw.visibility = View.VISIBLE
                    layoutBpReasonNotTaking.visibility = View.GONE
                    layoutBpOtherReasonNotTaking.visibility = View.GONE
                    autotvReasonForNotTakingBpMedication.text = null
                    etBpMedicationNotTakingOtherReason.setText("")
                } else if (checkedId == radioTakingMedicationBpNo.id) {
                    layoutBpReasonNotTaking.visibility = View.VISIBLE
                    layoutBpSeenByHw.visibility = View.GONE
                    rgBpSeenByHwMedicationYes.clearCheck()
                }
            }

            // Dropdown adapter
            val adapter = ArrayAdapterUtils.getArrayAdapter(
                requireContext(),
                R.array.reason_for_not_taking_bp_medication
            )
            autotvReasonForNotTakingBpMedication.setAdapter(adapter)

            // Handle dropdown selection
            autotvReasonForNotTakingBpMedication.setOnItemClickListener { _, _, i, _ ->
                autotvlayoutReasonForNotTakingBpMedication.hideError()

                val selectedText = resources.getStringArray(R.array.reason_for_not_taking_bp_medication)[i]
                autotvReasonForNotTakingBpMedication.setText(selectedText, false)

                if (selectedText.equals("Unknown / Other", ignoreCase = true)) {
                    //  Show "Other reason" input
                    layoutBpOtherReasonNotTaking.visibility = View.VISIBLE
                } else {
                    //  Hide & clear "Other reason" input
                    layoutBpOtherReasonNotTaking.visibility = View.GONE
                    etBpMedicationNotTakingOtherReason.setText("")
                }
            }
        }
    }
    fun getNotTakingBpMedicationReasonForDb(): String {
        //val selected = binding.layoutBpMedication.autotvReasonForNotTakingBpMedication.text.toString()
        return if (selectedBPNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
            "Unknown / Other:reason ${binding.layoutBpMedication.etBpMedicationNotTakingOtherReason.text}"
        } else
            selectedBPNoMedicationReason
    }
    fun setupAutoCompleteWithOther(
        autoCompleteTextView: AutoCompleteTextView,
        layoutOtherReason: LinearLayout,
        savedValue: String?,           // e.g., "Unknown / Other:my reason"
        stringArrayResId: Int,
        editText: TextInputEditText,
        onSave: (String) -> Unit       // callback gives you English value to save
    ) {
        val context = autoCompleteTextView.context

        // Split saved value into main + other
        val parts = savedValue?.split(":", limit = 2)
        val mainValue = parts?.getOrNull(0)?.trim()
        var otherValue = parts?.getOrNull(1)?.trim()

        //  Remove leading "reason" if present in saved value
        if (!otherValue.isNullOrEmpty() && otherValue.startsWith("reason", ignoreCase = true)) {
            otherValue = otherValue.removePrefix("reason").trim()
        }

        // English + localized arrays
        val englishResources = LanguageUtils.getSpecificLocalResource(context, "en")
        val englishArray = englishResources.getStringArray(stringArrayResId)

        val localizedResources = LanguageUtils.getSpecificLocalResource(
            context,
            Locale.getDefault().language
        )
        val localizedArray = localizedResources.getStringArray(stringArrayResId)

        // --- Set initial selection ---
        mainValue?.let { data ->
            val index = englishArray.indexOf(data)
            if (index != -1) {
                autoCompleteTextView.setText(localizedArray[index], false)
            } else {
                autoCompleteTextView.setText(data, false)
            }
        }

        // --- Set Other reason field ---
        if (mainValue?.contains("Unknown / Other", ignoreCase = true) == true) {
            layoutOtherReason.visibility = View.VISIBLE
            editText.setText(otherValue ?: "")
        } else {
            layoutOtherReason.visibility = View.GONE
        }

        // --- Always call onSave for prefilled case (in English) ---
        if (mainValue != null) {
            val englishValue = englishArray.find { it.equals(mainValue, ignoreCase = true) } ?: mainValue
            val finalValue = if (englishValue.contains("Unknown / Other", ignoreCase = true)) {
                if (!otherValue.isNullOrEmpty()) "$englishValue:$otherValue" else englishValue
            } else {
                englishValue
            }
            onSave(finalValue)
        }

        // --- Handle user selection dynamically ---
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocalized = parent.getItemAtPosition(position).toString()
            val index = localizedArray.indexOf(selectedLocalized)

            if (index != -1) {
                val englishValue = englishArray[index]
                if (englishValue.contains("Unknown / Other", ignoreCase = true)) {
                    layoutOtherReason.visibility = View.VISIBLE
                } else {
                    editText.setText("")
                    layoutOtherReason.visibility = View.GONE
                }

                // Always return English value to save
                val finalValue = if (englishValue.contains("Unknown / Other", ignoreCase = true)) {
                    var other = editText.text.toString().trim()

                    // Remove leading "reason" if user typed it
                    if (other.startsWith("reason", ignoreCase = true)) {
                        other = other.removePrefix("reason").trim()
                    }

                    if (other.isNotEmpty()) "$englishValue:$other" else englishValue
                } else {
                    englishValue
                }
                onSave(finalValue)
            }
        }
    }

    private fun setUp11Fields(age: Int) {
        if (age > 11) {
            isAgeGreaterThan11 = true
            return
        }
        binding.llHbCheck.visibility = View.GONE
        binding.layoutAnemiaMedication.llAnemiaLabel.visibility =View.GONE

    }
    private fun diabetesHistory() {
        with(binding.layoutDiabetesMedication) {

            // Q1: Do you have diabetes?
            rgDiabetesOptions.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == radioDiabetesYes.id) {
                    layoutDiabetesMedication.visibility = View.VISIBLE
                } else {
                    // Clear values
                    rgDiabetesTakingMedicationOptions.clearCheck()
                    rgDiabetesSeenByHwMedicationYes.clearCheck()
                    autotvReasonForNotTakingDiabetesMedication.text = null
                    etDiabetesMedicationNotTakingOtherReason.setText("")

                    layoutDiabetesMedication.visibility = View.GONE
                    layoutDiabetesSeenByHw.visibility = View.GONE
                    layoutDiabetesReasonNotTaking.visibility = View.GONE
                    layoutDiabetesOtherReasonNotTaking.visibility = View.GONE
                }
            }

            // Q2: Taking any medication?
            rgDiabetesTakingMedicationOptions.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == radioTakingMedicationDiabetesYes.id) {
                    layoutDiabetesSeenByHw.visibility = View.VISIBLE
                    layoutDiabetesReasonNotTaking.visibility = View.GONE
                    layoutDiabetesOtherReasonNotTaking.visibility = View.GONE
                    autotvReasonForNotTakingDiabetesMedication.text = null
                    etDiabetesMedicationNotTakingOtherReason.setText("")
                } else if (checkedId == radioTakingMedicationDiabetesNo.id) {
                    layoutDiabetesReasonNotTaking.visibility = View.VISIBLE
                    layoutDiabetesSeenByHw.visibility = View.GONE
                    rgDiabetesSeenByHwMedicationYes.clearCheck()
                }
            }

            // Dropdown adapter
            val adapter = ArrayAdapterUtils.getArrayAdapter(
                requireContext(),
                R.array.reason_for_not_taking_bp_medication
            )
            autotvReasonForNotTakingDiabetesMedication.setAdapter(adapter)

            // Handle dropdown selection
            autotvReasonForNotTakingDiabetesMedication.setOnItemClickListener { _, _, i, _ ->
                autotvlayoutReasonForNotTakingDiabetesMedication.hideError()

                val selectedText = resources.getStringArray(R.array.reason_for_not_taking_bp_medication)[i]
                autotvReasonForNotTakingDiabetesMedication.setText(selectedText, false)

                if (selectedText.equals("Unknown / Other", ignoreCase = true)) {
                    //  Show "Other reason" input
                    layoutDiabetesOtherReasonNotTaking.visibility = View.VISIBLE
                } else {
                    //  Hide & clear "Other reason" input
                    layoutDiabetesOtherReasonNotTaking.visibility = View.GONE
                    etDiabetesMedicationNotTakingOtherReason.setText("")
                }
            }
        }
    }
    fun getNotTakingDiabetesMedicationReasonForDb(): String {
        return if (selectedDiabetesNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
            "Unknown / Other:reason ${binding.layoutDiabetesMedication.etDiabetesMedicationNotTakingOtherReason.text}"
        } else {
            selectedDiabetesNoMedicationReason
        }
    }

    private fun validateDiabetesSection(it: MedicalBaselineConfig): Boolean {

        val error = R.string.this_field_is_mandatory

        // Diabetes - Q1: Do you have diabetes?
        val diabetesValue =
            if (it.diabetesValue!!.isEnabled && it.diabetesValue!!.isMandatory && isAgeGreaterThan20) {
                binding.layoutDiabetesMedication.rgDiabetesOptions.validate()
            } else true

        // Diabetes - Q2: Taking any medication?
        val takingAnyMedicationForDiabetes =
            if (it.takingAnyMedicationForDiabetes!!.isEnabled &&
                it.takingAnyMedicationForDiabetes!!.isMandatory &&
                binding.layoutDiabetesMedication.layoutDiabetesMedication.isVisible
            ) {
                binding.layoutDiabetesMedication.rgDiabetesTakingMedicationOptions.validate()
            } else true

        // Diabetes - Q3: Seen by health worker in past year?
        val haveYouSeenToHWinPastOneYearForDiabetes =
            if (it.haveYouSeenToHWinPastOneYearForDiabetes!!.isEnabled &&
                it.haveYouSeenToHWinPastOneYearForDiabetes!!.isMandatory &&
                binding.layoutDiabetesMedication.layoutDiabetesSeenByHw.isVisible
            ) {
                binding.layoutDiabetesMedication.rgDiabetesSeenByHwMedicationYes.validate()
            } else true

        // Diabetes - Q4: Reason for not taking medication
        val reasonForNotTakingDiabetesMedication =
            if (it.reasonForNotTakingDiabetesMedication!!.isEnabled &&
                it.reasonForNotTakingDiabetesMedication!!.isMandatory &&
                binding.layoutDiabetesMedication.layoutDiabetesReasonNotTaking.isVisible
            ) {
                val isValidDropdown = binding.layoutDiabetesMedication
                    .autotvlayoutReasonForNotTakingDiabetesMedication
                    .validateDropDowb(
                        binding.layoutDiabetesMedication.autotvReasonForNotTakingDiabetesMedication,
                        error
                    )

                if (isValidDropdown) {
                    val selectedValue = binding.layoutDiabetesMedication
                        .autotvReasonForNotTakingDiabetesMedication.text?.toString()?.trim()

                    if (selectedDiabetesNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
                        val otherReason = binding.layoutDiabetesMedication
                            .etDiabetesMedicationNotTakingOtherReason.text?.toString()?.trim()

                        if (otherReason.isNullOrEmpty()) {
                            binding.layoutDiabetesMedication
                                .tilDiabetesMedicationNotTakingOtherReason.error =
                                getString(R.string.please_enter_reason_txt)
                            false
                        } else true
                    } else {
                        true
                    }
                } else false
            } else true

        return diabetesValue &&
                takingAnyMedicationForDiabetes &&
                haveYouSeenToHWinPastOneYearForDiabetes &&
                reasonForNotTakingDiabetesMedication
    }
    private fun setUp20Fields(age: Int) {
        if (age >= 20) {
            isAgeGreaterThan20 = true
            return
        }
        binding.llSugarCheck.visibility = View.GONE
        binding.layoutDiabetesMedication.llDiabetesLabel.visibility = View.GONE
    }
}