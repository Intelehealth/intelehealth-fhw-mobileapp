package org.intelehealth.app.ui.baseline_survey.fragments

import android.os.Bundle
import android.util.Log
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
import org.intelehealth.app.utilities.extensions.getSelectedDataInEnglishLocale
import org.intelehealth.app.utilities.extensions.getTextInEnglish
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
        Log.d("TAG", "onBaselineDataLoaded: baseline from db  : "+Gson().toJson(baselineData))
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
        initializeRadioButtonTags()
    }

    private fun initializeRadioButtonTags() {
        binding.radioBpYes.tag = R.string.yes
        binding.radioBpNo.tag = R.string.no

        binding.radioDiabetesYes.tag = R.string.yes
        binding.radioDiabetesNo.tag = R.string.no

        binding.radioArthritisYes.tag = R.string.yes
        binding.radioArthritisNo.tag = R.string.no

        binding.layoutAnemiaMedication.radioAnemiaYes.tag = R.string.yes
        binding.layoutAnemiaMedication.radioAnemiaNo.tag = R.string.no

        binding.radioSurgeryYes.tag = R.string.yes
        binding.radioSurgeryNo.tag = R.string.no

        binding.radioSmoker.tag = R.string.smoker
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
        anemiaHistory()
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

            bpValue = binding.rgBpOptions.getSelectedDataInEnglishLocale(requireContext())
            diabetesValue =
                binding.rgDiabetesOptions.getSelectedDataInEnglishLocale(requireContext())
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
            reasonForNotTakingAnemiaMedication =  binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication.text.toString()
            otherReasonForNotTakingAnemiaMedication  = binding.layoutAnemiaMedication.etAnemiaMedicationNotTakingOtherReason.text.toString()
            //reason - add
            Log.d("kktest", "saveSurveyData: baseline data : " + this)
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
                binding.layoutAnemiaMedication.rgAnemiaOptions.validate()
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

           //check age  - here in excel age is 11 for anemia
//            val takingAnyMedicationForAnemia =
//                if (it.takingAnyMedicationForAnemia!!.isEnabled && it.takingAnyMedicationForAnemia!!.isMandatory && isAgeGreaterThan18) {
//                    binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.validate()
//                } else true
//
//            val haveYouSeenToHWinPastOneYearForAnemia =
//                if (it.haveYouSeenToHWinPastOneYearForAnemia!!.isEnabled && it.haveYouSeenToHWinPastOneYearForAnemia!!.isMandatory && isAgeGreaterThan18) {
//                    binding.layoutAnemiaMedication.rgAnemiaSeenByHwMedicationYes.validate()
//                } else true
//
//            val reasonForNotTakingAnemiaMedication =
//                if (it.reasonForNotTakingAnemiaMedication!!.isEnabled && it.reasonForNotTakingAnemiaMedication!!.isMandatory && isAgeGreaterThan18) {
//                    binding.layoutAnemiaMedication.autotvlayoutReasonForNotTakingAnemiaMedication.validateDropDowb(binding.acSugarCheck, error)
//                } else true


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

            // 🔹 Dropdown adapter
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

                if (selectedText.equals(getString(R.string.unknown_other), ignoreCase = true)) {
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

    /*  private fun anemiaHistory() {
          with(binding.layoutAnemiaMedication) {

              // Q1: Do you have anemia?
              rgAnemiaOptions.setOnCheckedChangeListener { _, checkedId ->
                  if (checkedId == radioAnemiaYes.id) {
                      //  Show medication container
                      layoutAnemiaMedication.visibility = View.VISIBLE
                  } else {
                      //  Hide everything else
                      layoutAnemiaMedication.visibility = View.GONE
                      layoutAnemiaSeenByHw.visibility = View.GONE
                      layoutAnemiaReasonNotTaking.visibility = View.GONE

                      // Clear values
                      rgAnemiaTakingMedicationOptions.clearCheck()
                      rgAnemiaSeenByHwMedicationYes.clearCheck()
                      autotvReasonForNotTakingAnemiaMedication.text = null
                      etAnemiaMedicationNotTakingOtherReason.setText("")
                  }
              }

              // Q2: Taking any medication?
              rgAnemiaTakingMedicationOptions.setOnCheckedChangeListener { _, checkedId ->
                  if (checkedId == radioTakingMedicationAnemiaYes.id) {
                      //  Show "Seen by HW" container
                      layoutAnemiaSeenByHw.visibility = View.VISIBLE

                      //  Hide & clear "Reason not taking" container
                      layoutAnemiaReasonNotTaking.visibility = View.GONE
                      autotvReasonForNotTakingAnemiaMedication.text = null
                      etAnemiaMedicationNotTakingOtherReason.setText("")
                  } else if (checkedId == radioTakingMedicationAnemiaNo.id) {
                      //Show "Reason not taking" container
                      layoutAnemiaReasonNotTaking.visibility = View.VISIBLE

                      //  Hide & clear "Seen by HW"
                      layoutAnemiaSeenByHw.visibility = View.GONE
                      rgAnemiaSeenByHwMedicationYes.clearCheck()
                  }
              }

              //  Dropdown adapter
              val adapter = ArrayAdapterUtils.getArrayAdapter(
                  requireContext(),
                  R.array.reason_for_not_taking_bp_medication
              )
              autotvReasonForNotTakingAnemiaMedication.setAdapter(adapter)

              // Handle dropdown selection
              autotvReasonForNotTakingAnemiaMedication.setOnItemClickListener { _, _, i, _ ->
                  autotvlayoutReasonForNotTakingAnemiaMedication.hideError()
                  autotvReasonForNotTakingAnemiaMedication.setText(
                      resources.getStringArray(R.array.reason_for_not_taking_bp_medication)[i],
                      false
                  )
              }
          }
      }
  */
  /*  private fun anemiaHistory() {
        //taking any medication ques- on click on bp medical history yes option
        binding.layoutAnemiaMedication.rgAnemiaOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.layoutAnemiaMedication.radioAnemiaYes.id -> {
                    binding.layoutAnemiaMedication.tvAnemiaMedicationQues.visibility = View.VISIBLE
                    binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.visibility = View.VISIBLE
                }

                else -> {
                    binding.layoutAnemiaMedication.tvAnemiaMedicationQues.visibility = View.GONE
                    binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.visibility = View.GONE
                    binding.layoutAnemiaMedication.tvAnemiaNotTakingMedicationNoOption.visibility = View.GONE
                    binding.layoutAnemiaMedication.autotvlayoutReasonForNotTakingAnemiaMedication.visibility = View.GONE

                }
            }
        }
        //taking any medication options - on click on yes
        binding.layoutAnemiaMedication.rgAnemiaTakingMedicationOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.layoutAnemiaMedication.radioTakingMedicationAnemiaYes.id -> {
                    binding.layoutAnemiaMedication.tvAnemiaYesOptionSeenByHwMedicationYes.visibility = View.VISIBLE
                    binding.layoutAnemiaMedication.rgAnemiaSeenByHwMedicationYes.visibility = View.VISIBLE
                    binding.layoutAnemiaMedication.tvAnemiaNotTakingMedicationNoOption.visibility = View.GONE
                    binding.layoutAnemiaMedication.autotvlayoutReasonForNotTakingAnemiaMedication.visibility = View.GONE
                }

                else -> {
                    binding.layoutAnemiaMedication.tvAnemiaYesOptionSeenByHwMedicationYes.visibility = View.GONE
                    binding.layoutAnemiaMedication.rgAnemiaSeenByHwMedicationYes.visibility = View.GONE
                    binding.layoutAnemiaMedication.tvAnemiaNotTakingMedicationNoOption.visibility = View.VISIBLE
                    binding.layoutAnemiaMedication.autotvlayoutReasonForNotTakingAnemiaMedication.visibility = View.VISIBLE
                }
            }
        }
        //last ques radio handling pending -db part

        //set values to not taking medication dropdown
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.reason_for_not_taking_bp_medication)
        binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication.setAdapter(adapter)

        binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication.setOnItemClickListener { _, _, i, _ ->
            binding.layoutAnemiaMedication.autotvlayoutReasonForNotTakingAnemiaMedication.hideError()
            binding.layoutAnemiaMedication.autotvReasonForNotTakingAnemiaMedication.setText(resources.getStringArray(R.array.reason_for_not_taking_bp_medication)[i], false)
        }
    }*/

}