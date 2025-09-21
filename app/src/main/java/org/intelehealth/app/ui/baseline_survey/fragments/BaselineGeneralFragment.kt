package org.intelehealth.app.ui.baseline_survey.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.i
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.StaticPatientRegistrationEnabledFieldsHelper
import org.intelehealth.app.databinding.FragmentBaselineSurveyGeneralBinding
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.patient.fragment.PatientPersonalInfoFragmentDirections
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.extensions.getSelectedData
import org.intelehealth.app.utilities.extensions.getSelectedDataInEnglishLocale
import org.intelehealth.app.utilities.extensions.getTextInEnglish
import org.intelehealth.app.utilities.extensions.hideDigitErrorOnTextChang
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDigit
import org.intelehealth.app.utilities.extensions.validateDropDowb
import org.intelehealth.app.utilities.extensions.validateIllogicalPhoneNumber

/**
 * Created by Shazzad H Kanon on 06-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/


class BaselineGeneralFragment :
    BaseFragmentBaselineSurvey(R.layout.fragment_baseline_survey_general) {

    private lateinit var binding: FragmentBaselineSurveyGeneralBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentBaselineSurveyGeneralBinding.bind(view)
        baselineSurveyViewModel.updateBaselineStage(BaselineSurveyStage.GENERAL)
        super.onViewCreated(view, savedInstanceState)
    }

    /*override fun onBaselineDataLoaded(baselineData: Baseline) {
        super.onBaselineDataLoaded(baselineData)
        fetchGeneralBaselineConfig()
        binding.baseline = baselineData
        Log.d("TAG", "onBaselineDataLoaded: baselineData : "+Gson().toJson(baselineData))
        //setTitleAsPerSelectedOption(binding.tvWhatsappNumberLabel, baselineData.familyWhatsApp)
        binding.baselineEditMode = baselineSurveyViewModel.baselineEditMode
    }*/

    private fun fetchGeneralBaselineConfig() {
        val it = getStaticPatientRegistrationFields()
        binding.generalConfig = PatientRegFieldsUtils.buildGeneralBaselineConfig(it)
        setValues()
        setClickListener()
    }

    private fun getStaticPatientRegistrationFields() =
        StaticPatientRegistrationEnabledFieldsHelper.getEnabledGeneralBaselineFields()

    private fun setValues() {
        setupOccupationCheck()
        setupCasteCheck()
        setupEducationCheck()
        setupPhoneOwnershipCheck()
        initializeRadioButtonTags()
        manageWhatsappQuestions()
       // setTitleAsPerSelectedOptionForDbValue(binding.tvWhatsappNumberLabel, binding.baseline?.familyWhatsApp, "dbload")
    }

    private fun initializeRadioButtonTags() {
        binding.radioACYes.tag = R.string.yes
        binding.radioACNo.tag = R.string.no
        binding.radioACNotSure.tag = R.string.generic_not_sure

        binding.radioMCYes.tag = R.string.yes
        binding.radioMCNo.tag = R.string.no
        binding.radioMCNotSure.tag = R.string.generic_not_sure

        binding.radioBAYes.tag = R.string.yes
        binding.radioBANo.tag = R.string.no
        binding.radioBANotSure.tag = R.string.decline_to_answer

        binding.radioPersonal.tag = R.string.generic_yes_personal
        binding.radioFamilyMember.tag = R.string.generic_yes_family
        binding.radioFamilyWhatsappNo.tag = R.string.no

        binding.radioMarried.tag = R.string.married
        binding.radioUnmarried.tag = R.string.unmarried
        binding.radioWidowed.tag = R.string.widowed

       // binding.cbWhatsappNumberUnknown.tag = R.string.yes

    }

    private fun setupOccupationCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.occupation)
        binding.acOccupation.setAdapter(adapter)

        binding.acOccupation.setOnItemClickListener { _, _, i, _ ->
            binding.tilOccupationOption.hideError()
            binding.acOccupation.setText(resources.getStringArray(R.array.occupation)[i], false)
        }
    }

    private fun setupCasteCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.caste)
        binding.acCaste.setAdapter(adapter)

        binding.acCaste.setOnItemClickListener { _, _, i, _ ->
            binding.tilCasteOption.hideError()
            binding.acCaste.setText(resources.getStringArray(R.array.caste)[i], false)
        }
    }

    private fun setupEducationCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.education)
        binding.acEducation.setAdapter(adapter)

        binding.acEducation.setOnItemClickListener { _, _, i, _ ->
            binding.tilEducationOption.hideError()
            binding.acEducation.setText(resources.getStringArray(R.array.education)[i], false)
        }
    }

    private fun setupPhoneOwnershipCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.phone_ownership)
        binding.acPhoneOwnership.setAdapter(adapter)

        binding.acPhoneOwnership.setOnItemClickListener { _, _, i, _ ->
            binding.tilPhoneOwnershipOption.hideError()
            binding.acPhoneOwnership.setText(
                resources.getStringArray(R.array.phone_ownership)[i],
                false
            )
        }
    }

    private fun setClickListener() {
        binding.btnGeneralBaselineNext.setOnClickListener {
            validateForm {
                isInitializing = true
                confirmEkalCanSendSummaryOnWhatsapp() }
        }
    }

    private fun saveSurveyData(canEkalSendFreeWhatsAppMessageForVisitSummaryValue: String) {
        baselineSurveyData.apply {
            occupation = binding.acOccupation.getTextInEnglish(requireContext(), R.array.occupation)
            caste = binding.acCaste.getTextInEnglish(requireContext(), R.array.caste)
            education = binding.acEducation.getTextInEnglish(requireContext(), R.array.education)
            ayushmanCard = binding.rgACOptions.getSelectedDataInEnglishLocale(requireContext())
            mgnregaCard = binding.rgMCOptions.getSelectedDataInEnglishLocale(requireContext())
            bankAccount = binding.rgBAOptions.getSelectedDataInEnglishLocale(requireContext())
            phoneOwnership = binding.acPhoneOwnership.getTextInEnglish(
                requireContext(),
                R.array.phone_ownership
            )
            familyWhatsApp =
                binding.rgFamilyWhatsappOptions.getSelectedDataInEnglishLocale(requireContext())
            martialStatus =
                binding.rgMaritalStatusOptions.getSelectedDataInEnglishLocale(requireContext())
            selfOrFamilyWhatsappNumber =getWhatsappNumberForDb()
            canEkalSendFreeWhatsAppMessageForVisitSummary = canEkalSendFreeWhatsAppMessageForVisitSummaryValue

            Log.d("kkgeneral", "saveSurveyData: this to db1 : "+this)
            Log.d("kkgeneral", "saveSurveyData: this to db2 : "+Gson().toJson(this))
            Log.d("kkgeneral", "saveSurveyData: canEkalSendFreeWhatsAppMessageForVisitSummary : "+canEkalSendFreeWhatsAppMessageForVisitSummary)

            baselineSurveyViewModel.updateBaselineData(this)
            BaselineGeneralFragmentDirections.navigationGeneralToMedical().apply {
                findNavController().navigate(this)
            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        val error = R.string.this_field_is_mandatory

        binding.generalConfig?.let {
            val bOccupation = if (it.occupation!!.isEnabled && it.occupation!!.isMandatory) {
                binding.tilOccupationOption.validateDropDowb(binding.acOccupation, error)
            } else true

            val bCaste = if (it.caste!!.isEnabled && it.caste!!.isMandatory) {
                binding.tilCasteOption.validateDropDowb(binding.acCaste, error)
            } else true

            val bEducation = if (it.education!!.isEnabled && it.education!!.isMandatory) {
                binding.tilEducationOption.validateDropDowb(binding.acEducation, error)
            } else true

            val bAyushmanCard = if (it.ayushmanCard!!.isEnabled && it.ayushmanCard!!.isMandatory) {
                binding.rgACOptions.validate()
            } else true

            val bMgnrega = if (it.mgnrega!!.isEnabled && it.mgnrega!!.isMandatory) {
                binding.rgMCOptions.validate()
            } else true

            val bBankAc = if (it.bankAccount!!.isEnabled && it.bankAccount!!.isMandatory) {
                binding.rgBAOptions.validate()
            } else true

            val phoneOwnership =
                if (it.phoneOwnership!!.isEnabled && it.phoneOwnership!!.isMandatory) {
                    binding.tilPhoneOwnershipOption.validateDropDowb(
                        binding.acPhoneOwnership,
                        error
                    )
                } else true

            val familyWhatsApp =
                if (it.familyWhatsapp!!.isEnabled && it.familyWhatsapp!!.isMandatory) {
                    binding.rgFamilyWhatsappOptions.validate()
                } else true

            val maritalStatus =
                if (it.maritalStatus!!.isEnabled && it.maritalStatus!!.isMandatory) {
                    binding.rgMaritalStatusOptions.validate()
                } else true

            val whatsAppNumberValid = if (
                it.selfOrFamilyWhatsappNumber!!.isEnabled &&
                it.selfOrFamilyWhatsappNumber!!.isMandatory &&
                binding.layoutWhatsappNumber.isVisible // only if Yes selected
            ) {
                if (binding.cbWhatsappNumberUnknown.isChecked) {
                    true // Skip number validation if "I don’t know" is checked
                } else {
                    binding.tilWhatsappNumber.validate(binding.etWhatsappNumber, error)
                        .and(
                            binding.tilWhatsappNumber.validateDigit(
                                binding.etWhatsappNumber,
                                R.string.enter_10_digits,
                                10
                            )
                        )
                        .and(
                            binding.tilWhatsappNumber.validateIllogicalPhoneNumber(
                                binding.etWhatsappNumber,
                                R.string.enter_valid_phone_number
                            )
                        )
                }
            } else true

            Log.d("TAG", "validateForm: whatsAppNumberValid : " +whatsAppNumberValid)


            if (bOccupation.and(bCaste).and(bEducation).and(bAyushmanCard)
                    .and(bMgnrega).and(bBankAc).and(phoneOwnership)
                    .and(familyWhatsApp).and(maritalStatus).and(whatsAppNumberValid)
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

    private fun getWhatsappNumberForDb(): String {
        return if (binding.cbWhatsappNumberUnknown.isChecked) {
            "I don't know"
        } else {
            binding.etWhatsappNumber.text.toString()
        }
    }

    private fun confirmEkalCanSendSummaryOnWhatsapp() {
        DialogUtils.patientRegistrationDialog(
            requireContext(),
            ContextCompat.getDrawable(requireContext(), R.drawable.info_svg),
            resources.getString(R.string.send_summary_on_whatsApp),
            resources.getString(R.string.can_ekal_send_a_summary_of_visit_to_you),
            resources.getString(R.string.yes),
            resources.getString(R.string.no)
        ) { action ->
            if (action == CustomDialogListener.POSITIVE_CLICK) {
                saveSurveyData( "Yes")
            } else if (action == CustomDialogListener.NEGATIVE_CLICK) {
                saveSurveyData("No")
            }
        }
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
        Log.d("kk", "setTitleAsPerSelectedOption: textView.text : ${textView.text}")
    }
    private fun manageWhatsappQuestions(){
        binding.tilWhatsappNumber.hideDigitErrorOnTextChang(binding.etWhatsappNumber, 10
        )
        binding.rgFamilyWhatsappOptions.setOnCheckedChangeListener { group, checkedId ->

            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedValue = selectedRadioButton?.text?.toString()

            when (checkedId) {
                R.id.radioPersonal, R.id.radioFamilyMember -> {
                    binding.layoutWhatsappNumber.visibility = View.VISIBLE
                    binding.etWhatsappNumber.isEnabled = !binding.cbWhatsappNumberUnknown.isChecked

                    setTitleAsPerSelectedOption(binding.tvWhatsappNumberLabel, selectedValue)
                }
                R.id.radioFamilyWhatsappNo -> {
                    binding.layoutWhatsappNumber.visibility = View.GONE
                    binding.tvWhatsappNumberLabel.text = ""
                }
            }
            if (isInitializing) {
                isInitializing = false
                return@setOnCheckedChangeListener
            }
            // reset the etWhatsappNumber and uncheck the cbWhatsappNumberUnknown
            binding.etWhatsappNumber.text = null
            binding.cbWhatsappNumberUnknown.isChecked = false


        }

        // Manage "I don't know" checkbox state
        binding.cbWhatsappNumberUnknown.setOnCheckedChangeListener { _, isChecked ->
           /* if (isInitializing) {
                isInitializing = false
                return@setOnCheckedChangeListener
            }*/
            if (isChecked) {
                binding.etWhatsappNumber.isEnabled = false
                binding.etWhatsappNumber.text = null
            } else {
                binding.etWhatsappNumber.isEnabled = true
            }
        }

    }
    private var isInitializing = false
    override fun onBaselineDataLoaded(baselineData: Baseline) {
        super.onBaselineDataLoaded(baselineData)
        fetchGeneralBaselineConfig()
        // prevent listeners from reacting to programmatic population
        isInitializing = true
        binding.baseline = baselineData
        Log.d("TAG", "onBaselineDataLoaded: baselineData : "+Gson().toJson(baselineData))
        //setTitleAsPerSelectedOption(binding.tvWhatsappNumberLabel, baselineData.familyWhatsApp)
        binding.baselineEditMode = baselineSurveyViewModel.baselineEditMode
        // now turn off initialization mode, then run any UI logic that should happen after load
        //isInitializing = false
        setTitleAsPerSelectedOption(binding.tvWhatsappNumberLabel, baselineData.familyWhatsApp)
    }
}