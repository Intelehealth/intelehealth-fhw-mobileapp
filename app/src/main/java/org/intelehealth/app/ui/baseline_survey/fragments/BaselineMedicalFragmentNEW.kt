package org.intelehealth.app.ui.baseline_survey.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.activities.patientDetailActivity.StaticPatientRegistrationEnabledFieldsHelper
import org.intelehealth.app.databinding.LayoutMissingBaselineDataNewBinding
import org.intelehealth.app.shared.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.filter.LettersNumbersSelectedSymbolsInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.getSelectedDataInEnglishLocale
import org.intelehealth.app.utilities.extensions.hideDigitErrorOnTextChang
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDigit
import org.intelehealth.app.utilities.extensions.validateDropDowb
import org.intelehealth.app.utilities.extensions.validateIllogicalPhoneNumber
import org.intelehealth.config.room.entity.PatientRegistrationFields
import java.util.Locale

class BaselineMedicalFragmentNEW :
    BaseFragmentBaselineSurvey(R.layout.layout_missing_baseline_data_new) {

    private lateinit var binding: LayoutMissingBaselineDataNewBinding
    private var selectedBPNoMedicationReason: String = ""
    private var selectedAnemiaNoMedicationReason: String = ""
    private var selectedDiabetesNoMedicationReason: String = ""
    private var isAgeGreaterThan20: Boolean = false
    private var isAgeGreaterThan11: Boolean = false
    private var isAgeGreaterThan18: Boolean = false
    private var patientId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = LayoutMissingBaselineDataNewBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        patientId = arguments?.getString("patientUuid").toString()

    }
    override fun onBaselineDataLoaded(baselineData: Baseline) {
        super.onBaselineDataLoaded(baselineData)
        Log.d("baselineflowMissing", "onBaselineDataLoaded: baselineData : "+baselineData)
        binding.baseline = baselineData

        fetchMedicalBaselineConfig()
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
        binding.layoutBpMedication.llBPLabel.visibility = View.GONE
    }

    private fun fetchMedicalBaselineConfig() {
        val it = getStaticPatientRegistrationFields()
        binding.medicalConfig = PatientRegFieldsUtils.buildMedicalBaselineConfig(it)
        binding.generalConfig = PatientRegFieldsUtils.buildGeneralBaselineConfig(it)

        setValues()
        setClickListener()
        setupFilter()
        initializeRadioButtonTags()
    }

    private fun initializeRadioButtonTags() {
        binding.layoutAnemiaMedication.radioAnemiaYes.tag = R.string.yes
        binding.layoutAnemiaMedication.radioAnemiaNo.tag = R.string.no

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

        binding.layoutWhatsappGeneral.radioPersonal.tag = R.string.generic_yes_personal
        binding.layoutWhatsappGeneral.radioFamilyMember.tag = R.string.generic_yes_family
        binding.layoutWhatsappGeneral.radioFamilyWhatsappNo.tag = R.string.no
    }

    private fun setupFilter() {
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

    private fun getStaticPatientRegistrationFields(): List<PatientRegistrationFields> {
        val medicalFields =
            StaticPatientRegistrationEnabledFieldsHelper.getEnabledMedicalBaselineFields()

        val generalFields =
            StaticPatientRegistrationEnabledFieldsHelper.getEnabledGeneralBaselineFields()

        return medicalFields + generalFields
    }


    private fun setValues() {
        setInputTextChangedListener()
        anemiaHistory()
        bpHistory()
        diabetesHistory()
        manageWhatsappQuestions()
    }

    private fun setInputTextChangedListener() {
    }

    private fun setClickListener() {
        binding.frag2BtnNext.setOnClickListener {
            validateForm { saveSurveyData() }
        }
    }

    private fun saveSurveyData() {
        baselineSurveyData.apply {

            diabetesValue = binding.layoutDiabetesMedication.rgDiabetesOptions.getSelectedDataInEnglishLocale(requireContext())

            anemiaValue = binding.layoutAnemiaMedication.rgAnemiaOptions.getSelectedDataInEnglishLocale(requireContext())

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
            familyWhatsApp = binding.layoutWhatsappGeneral.rgFamilyWhatsappOptions.getSelectedDataInEnglishLocale(requireContext())
            selfOrFamilyWhatsappNumber =getWhatsappNumberForDb()

            Log.d("kktest", "saveSurveyData: baseline data : "+ Gson().toJson(this))

            baselineSurveyViewModel.savePatient().observe(requireActivity()) { response ->
                response ?: return@observe
                baselineSurveyViewModel.handleResponse(response) { result ->
                    if (result) {
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.data_saved),
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToNextScreen()
                    }
                }
            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        val error = R.string.this_field_is_mandatory

        binding.medicalConfig?.let {
            // Anemia

            // Anemia - Q1: Do you have anemia?
            val anemiaValue = if (it.anemiaValue!!.isEnabled && it.anemiaValue!!.isMandatory && isAgeGreaterThan11) {
                binding.layoutAnemiaMedication.rgAnemiaOptions.validate()
            } else true

            // Anemia - Q2: Taking any medication?
         /*   val takingAnyMedicationForAnemia =
                if (it.takingAnyMedicationForAnemia!!.isEnabled &&
                    it.takingAnyMedicationForAnemia!!.isMandatory &&
                    binding.layoutAnemiaMedication.layoutAnemiaMedication.isVisible
                ) {
                    binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.validate()
                } else true*/
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

           /* // bp - Q2: Taking any medication?
            val takingAnyMedicationForBP =
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
            val isGeneralValid = validateGeneralConfig()


            if (anemiaValue
                    .and(bpValue)
                    .and(takingAnyMedicationForBP)
                    .and(haveYouSeenToHWinPastOneYearForBP)
                    .and(reasonForNotTakingBPMedication)
                    .and(takingAnyMedicationForAnemia).and(haveYouSeenToHWinPastOneYearForAnemia).and(reasonForNotTakingAnemiaMedication).and(diabetesValue)
                    .and(takingAnyMedicationForDiabetes).and(haveYouSeenToHWinPastOneYearForDiabetes).and(reasonForNotTakingDiabetesMedication)
                    .and(isGeneralValid)
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
        val hideAnemiaSectionInitially = baselineSurveyData.anemiaValue.equals(getString(R.string.medical_history_no), true)

        with(binding.layoutAnemiaMedication) {

            // Q1: Do you have anemia?
            rgAnemiaOptions.setOnCheckedChangeListener { _, checkedId ->
                if (hideAnemiaSectionInitially) {
                    llAnemiaLabel.visibility =View.GONE
                    return@setOnCheckedChangeListener
                }
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
            disableAnemiaHistoryIfAnsweredAlready()
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
       val hideBpSectionInitially = baselineSurveyData.bpValue.equals(getString(R.string.medical_history_no), true)

        with(binding.layoutBpMedication) {

            // 🔹 PRE-SELECT & DISABLE FIRST QUESTION IF ALREADY ANSWERED


            // Q1: Do you have anemia?
            rgBpOptions.setOnCheckedChangeListener { _, checkedId ->

                if (hideBpSectionInitially) {
                    llBPLabel.visibility =View.GONE
                    return@setOnCheckedChangeListener
                }
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
            disableBpHistoryIfAnsweredAlready()
        }
    }

    private fun disableBpHistoryIfAnsweredAlready() {
        val bpHistoryValue = baselineSurveyData.bpValue

        with(binding.layoutBpMedication) {

            if (bpHistoryValue.isAnsweredYesOrNo()) {
                when {
                    bpHistoryValue.equals(
                        getString(R.string.medical_history_yes),
                        true
                    ) -> rgBpOptions.check(radioBPYes.id)

                    bpHistoryValue.equals(
                        getString(R.string.medical_history_no),
                        true
                    ) -> rgBpOptions.check(radioBPNo.id)
                }
                // Disable the first question
                rgBpOptions.isEnabled = false
                radioBPYes.isEnabled = false
                radioBPNo.isEnabled = false
            }
        }
    }

    private fun getNotTakingBpMedicationReasonForDb(): String {
        //val selected = binding.layoutBpMedication.autotvReasonForNotTakingBpMedication.text.toString()
        return if (selectedBPNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
            "Unknown / Other:reason ${binding.layoutBpMedication.etBpMedicationNotTakingOtherReason.text}"
        } else
            selectedBPNoMedicationReason
    }

    private fun setUp11Fields(age: Int) {
        if (age > 11) {
            isAgeGreaterThan11 = true
            return
        }
        binding.layoutAnemiaMedication.llAnemiaLabel.visibility = View.GONE

    }
    private fun diabetesHistory() {
        val hideDiabetesSectionInitially = baselineSurveyData.diabetesValue.equals(getString(R.string.medical_history_no), true)
        with(binding.layoutDiabetesMedication) {

            // Q1: Do you have diabetes?
            rgDiabetesOptions.setOnCheckedChangeListener { _, checkedId ->

                if (hideDiabetesSectionInitially) {
                    llDiabetesLabel.visibility =View.GONE
                    return@setOnCheckedChangeListener
                }
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
            disableDiabetesHistoryIfAnsweredAlready()
        }
    }
    private fun getNotTakingDiabetesMedicationReasonForDb(): String {
        return if (selectedDiabetesNoMedicationReason.equals("Unknown / Other", ignoreCase = true)) {
            "Unknown / Other:reason ${binding.layoutDiabetesMedication.etDiabetesMedicationNotTakingOtherReason.text}"
        } else {
            selectedDiabetesNoMedicationReason
        }
    }
    private fun setUp20Fields(age: Int) {
        if (age >= 20) {
            isAgeGreaterThan20 = true
            return
        }
        binding.layoutDiabetesMedication.llDiabetesLabel.visibility = View.GONE
    }

    private fun navigateToNextScreen() {
        val intent = Intent(
            requireActivity(),
            PatientDetailActivity2::class.java
        )
        intent.putExtra("patientUuid", patientId)
        startActivity(intent)
        requireActivity().finish()

    }

    private fun manageWhatsappQuestions(){
        val hideWhatsappSectionSectionInitially = baselineSurveyData.familyWhatsApp.equals(getString(R.string.medical_history_no), true
            ) || (baselineSurveyData.familyWhatsApp.hasValidAnswer() && baselineSurveyData.selfOrFamilyWhatsappNumber.hasValidAnswer())

        with(binding.layoutWhatsappGeneral) {

            tilWhatsappNumber.hideDigitErrorOnTextChang(etWhatsappNumber, 10)

            rgFamilyWhatsappOptions.setOnCheckedChangeListener { group, checkedId ->

                disableIDs()

                when (checkedId) {
                    R.id.radioPersonal, R.id.radioFamilyMember -> {
                        layoutWhatsappNumber.visibility = View.VISIBLE
                        etWhatsappNumber.isEnabled = !cbWhatsappNumberUnknown.isChecked

                        val englishValue = when (checkedId) {
                            R.id.radioPersonal -> getEnglishString(requireContext(), R.string.generic_yes_personal)
                            R.id.radioFamilyMember -> getEnglishString(requireContext(), R.string.generic_yes_family)
                            else -> ""
                        }

                        setTitleAsPerSelectedOption(tvWhatsappNumberLabel, englishValue)
                    }
                    R.id.radioFamilyWhatsappNo -> {
                        layoutWhatsappNumber.visibility = View.GONE
                        tvWhatsappNumberLabel.text = ""
                    }
                }
                //if parent question answered yes/no then disable
                disableWhatsappQuestionIfAnswered()

                //if parent ques no
                if (hideWhatsappSectionSectionInitially) {
                    root.visibility =View.GONE
                    hideTitleForGeneral()
                    return@setOnCheckedChangeListener
                }
            }
            cbWhatsappNumberUnknown.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    etWhatsappNumber.isEnabled = false
                    etWhatsappNumber.text = null
                } else {
                    etWhatsappNumber.isEnabled = true
                }
            }
        }
    }

    private fun disableIDs() {
        with(binding.layoutWhatsappGeneral){

            radioPersonal.isEnabled= false
            radioFamilyMember.isEnabled =false
            radioFamilyWhatsappNo.isEnabled = false

            rgFamilyWhatsappOptions.setOnCheckedChangeListener(null)

        }
    }

    private fun hideTitleForGeneral() {
        binding.tvHeadingGeneral.visibility =View.GONE
    }

    private fun setTitleAsPerSelectedOption(
        textView: TextView,
        selectedValue: String?
    ) {
        if (selectedValue.isNullOrBlank()) {
            textView.text = ""
            return
        }

        val appResources = textView.context.resources
        val generalConfig = binding.generalConfig

        var baseText = when {
            selectedValue.contains("family", ignoreCase = true) -> {
                appResources.getString(
                    R.string.what_is_the_phone_number_associated_with_your_family_member_whatsapp_account
                )
            }
            selectedValue.contains("personal", ignoreCase = true) -> {
                appResources.getString(
                    R.string.what_is_the_phone_number_associated_with_your_personal_whatsapp_account
                )
            }
            else -> ""
        }

        // Append "*" if the field is mandatory
        if (generalConfig?.selfOrFamilyWhatsappNumber?.isMandatory == true) {
            baseText += " *"
        }

        textView.text = baseText
    }
    private fun getEnglishString(context: Context, resId: Int): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        return context.createConfigurationContext(config).resources.getString(resId)
    }
    private fun getWhatsappNumberForDb(): String {
        return if (binding.layoutWhatsappGeneral.cbWhatsappNumberUnknown.isChecked) {
            "I don't know"
        } else {
            binding.layoutWhatsappGeneral.etWhatsappNumber.text.toString()
        }
    }
    private fun validateWhatsappNumber(): Boolean {
        val it = binding.generalConfig ?: return true

        return if (
            it.selfOrFamilyWhatsappNumber!!.isEnabled &&
            it.selfOrFamilyWhatsappNumber!!.isMandatory &&
            binding.layoutWhatsappGeneral.layoutWhatsappNumber.isVisible
        ) {
            if (binding.layoutWhatsappGeneral.cbWhatsappNumberUnknown.isChecked) {
                true // Skip number validation if "I don’t know" is checked
            } else {
                binding.layoutWhatsappGeneral.tilWhatsappNumber.validate(binding.layoutWhatsappGeneral.etWhatsappNumber, R.string.please_select_all_the_required_fields)
                    .and(
                        binding.layoutWhatsappGeneral.tilWhatsappNumber.validateDigit(
                            binding.layoutWhatsappGeneral.etWhatsappNumber,
                            R.string.enter_10_digits,
                            10
                        )
                    )
                    .and(
                        binding.layoutWhatsappGeneral.tilWhatsappNumber.validateIllogicalPhoneNumber(
                            binding.layoutWhatsappGeneral.etWhatsappNumber,
                            R.string.enter_valid_phone_number
                        )
                    )
            }
        } else true
    }
    private fun validateFamilyWhatsapp(): Boolean {
        val it = binding.generalConfig ?: return true

        return if (it.familyWhatsapp!!.isEnabled && it.familyWhatsapp!!.isMandatory) {
            binding.layoutWhatsappGeneral.rgFamilyWhatsappOptions.validate()
        } else true
    }
    private fun validateGeneralConfig(): Boolean {
        val familyValid = validateFamilyWhatsapp()
        val numberValid = validateWhatsappNumber()
        return familyValid && numberValid
    }
    private fun setupAutoCompleteWithOther(
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
    private fun String?.isAnsweredYesOrNo(): Boolean {
        return !this.isNullOrBlank() && this != "-"
    }
    private fun disableDiabetesHistoryIfAnsweredAlready() {
        val diabetesHistoryValue = baselineSurveyData.diabetesValue

        with(binding.layoutDiabetesMedication) {

            if (diabetesHistoryValue.isAnsweredYesOrNo()) {
                when {
                    diabetesHistoryValue.equals(
                        getString(R.string.medical_history_yes),
                        true
                    ) -> rgDiabetesOptions.check(radioDiabetesYes.id)

                    diabetesHistoryValue.equals(
                        getString(R.string.medical_history_no),
                        true
                    ) -> rgDiabetesOptions.check(radioDiabetesNo.id)
                }
                // Disable the first question
                rgDiabetesOptions.isEnabled = false
                radioDiabetesYes.isEnabled = false
                radioDiabetesNo.isEnabled = false
            }
        }
    }
    private fun disableAnemiaHistoryIfAnsweredAlready() {
        val anemiaHistoryValue = baselineSurveyData.anemiaValue

        with(binding.layoutAnemiaMedication) {

            if (anemiaHistoryValue.isAnsweredYesOrNo()) {
                when {
                    anemiaHistoryValue.equals(
                        getString(R.string.medical_history_yes),
                        true
                    ) -> rgAnemiaOptions.check(radioAnemiaYes.id)

                    anemiaHistoryValue.equals(
                        getString(R.string.medical_history_no),
                        true
                    ) -> rgAnemiaOptions.check(radioAnemiaNo.id)
                }
                // Disable the first question
                rgAnemiaOptions.isEnabled = false
                radioAnemiaYes.isEnabled = false
                radioAnemiaNo.isEnabled = false
            }
        }
    }
    private fun disableWhatsappQuestionIfAnswered() {
        val whatsappValue = baselineSurveyData.familyWhatsApp
        if (whatsappValue.isNullOrBlank() || whatsappValue == "-") return

        with(binding.layoutWhatsappGeneral) {

            when {
                whatsappValue.equals(
                    getString(R.string.generic_yes_personal),
                    true
                ) -> rgFamilyWhatsappOptions.check(R.id.radioPersonal)

                whatsappValue.equals(
                    getString(R.string.generic_yes_family),
                    true
                ) -> rgFamilyWhatsappOptions.check(R.id.radioFamilyMember)

                whatsappValue.equals(
                    getString(R.string.medical_history_no),
                    true
                ) -> rgFamilyWhatsappOptions.check(R.id.radioFamilyWhatsappNo)
            }

            // 🔒 Disable only the first question
            rgFamilyWhatsappOptions.isEnabled = false
            radioPersonal.isEnabled = false
            radioFamilyMember.isEnabled = false
            radioFamilyWhatsappNo.isEnabled = false
        }
    }
    private fun String?.hasValidAnswer(): Boolean {
        return !this.isNullOrBlank() && this != "-"
    }

}