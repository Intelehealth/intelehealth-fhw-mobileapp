package org.intelehealth.app.ui.baseline_survey.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.StaticPatientRegistrationEnabledFieldsHelper
import org.intelehealth.app.databinding.FragmentBaselineSurveyOtherBinding
import org.intelehealth.app.ui.baseline_survey.constants.Constants
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.extensions.getSelectedCheckboxes
import org.intelehealth.app.utilities.extensions.getSelectedData
import org.intelehealth.app.utilities.extensions.getSelectedDataInEnglishLocale
import org.intelehealth.app.utilities.extensions.getTextIfVisible
import org.intelehealth.app.utilities.extensions.getTextInEnglish
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateCheckboxes
import org.intelehealth.app.utilities.extensions.validateDropDowb
import org.intelehealth.app.utilities.extensions.validateIntegerDataLimits
import org.intelehealth.app.utilities.extensions.validateNumberOfUsualMembers

/**
 * Created by Shazzad H Kanon on 06-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/
class BaselineOtherFragment : BaseFragmentBaselineSurvey(R.layout.fragment_baseline_survey_other) {

    private lateinit var binding: FragmentBaselineSurveyOtherBinding

    private var isLandlessOptionChosen: Boolean = false
    private var isHeadOfHouseholdLockedToNo: Boolean = false
    private var isUpdatingHohSelection: Boolean = false
    private var isViewInitialized: Boolean = false
    private var isSavingSurvey: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaselineSurveyOtherBinding.inflate(layoutInflater)
        setCheckboxes()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        baselineSurveyViewModel.updateBaselineStage(BaselineSurveyStage.OTHER)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onBaselineDataLoaded(baselineData: Baseline) {
        super.onBaselineDataLoaded(baselineData)
        if (isSavingSurvey) return
        binding.baseline = baselineData
        binding.baselineEditMode = baselineSurveyViewModel.baselineEditMode
        if (!isViewInitialized) {
            fetchOtherBaselineConfig()
            isViewInitialized = true
        }
    }

    private fun fetchOtherBaselineConfig() {
        val it = getStaticPatientRegistrationFields()
        binding.otherConfig = PatientRegFieldsUtils.buildOtherBaselineConfig(it)
        setValues()
        setClickListener()
        binding.root.post { applyHeadOfHouseholdRestrictionIfNeeded() }
    }

    private fun getStaticPatientRegistrationFields() =
        StaticPatientRegistrationEnabledFieldsHelper.getEnabledOtherBaselineFields()

    private fun setValues() {
        setupRadioButtons()
        setupHohCheck()
        setupEconomicStatus()
        setupReligion()
        setupElectricityCheck()
        setUpWaterCheck()
        setupNumberOfToiletFacilities()
        setupHouseStructure()
        setupCultivableLand()
        setOnTextChangeListener()
    }

    private fun setCheckboxes() {
        setupSourceOfWater()
        setupSourceOfLight()
        setupFuelType()
        setupHandWashPractice()
        setupWaterSafeguarding()
    }

    private fun setOnTextChangeListener() {
        binding.tilTotalMemberOption.hideErrorOnTextChang(binding.textInputTotalHHMembers)
        binding.tilUsualMemberOption.hideErrorOnTextChang(binding.textInputUsualHHMembers)
        binding.tilNumberOfSmartphonesOption.hideErrorOnTextChang(binding.textInputNoOfSmartPhones)
        binding.tilNumberOfFeaturePhoneOption.hideErrorOnTextChang(binding.textInputNoOfFeaturePhones)
        binding.tilNumberOfEarningMembersOption.hideErrorOnTextChang(binding.textInputEarningMembers)
        binding.tilLoadShedingHoursOption.hideErrorOnTextChang(binding.textInputloadSheddingHours)
        binding.tilLoadShedingDaysOption.hideErrorOnTextChang(binding.textInputloadSheddingDays)
        binding.tilWaterAvailabilityHoursOption.hideErrorOnTextChang(binding.textInputWaterAvailabilityHours)
        binding.tilWaterAvailabilityDaysOption.hideErrorOnTextChang(binding.textInputWaterAvailabilityDays)
        binding.tilCultivableLandValue.hideErrorOnTextChang(binding.textInputCultivableLandValue)
    }

    private fun setUpWaterCheck() {
        binding.rgWaterCheckOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioWaterCheckYes.id -> {
                    binding.llWaterAvailability.visibility = View.VISIBLE
                }

                else -> {
                    binding.llWaterAvailability.visibility = View.GONE
                }
            }
        }
    }

    private fun setupElectricityCheck() {
        binding.rgElectricityOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioElectricityYes.id -> {
                    binding.llLoadShedding.visibility = View.VISIBLE
                }

                else -> {
                    binding.llLoadShedding.visibility = View.GONE
                }
            }
        }
    }

    /**
     * If another household member is already head, force "No" and lock the question.
     */
    private fun applyHeadOfHouseholdRestrictionIfNeeded() {
        val patientId = baselineSurveyViewModel.patientId
        if (patientId.isBlank()) return
        if (!baselineSurveyViewModel.householdHasAnotherHead(patientId)) return

        isHeadOfHouseholdLockedToNo = true
        baselineSurveyData.headOfHousehold = "No"
        binding.baseline = baselineSurveyData

        isUpdatingHohSelection = true
        binding.radioHOHNo.isChecked = true
        isUpdatingHohSelection = false
        updateHohSections(isHeadOfHousehold = false)

        binding.rgHOHOptions.isEnabled = false
        binding.radioHOHYes.isEnabled = false
        binding.radioHOHNo.isEnabled = false
        binding.llHeadOfHousehold.alpha = 0.6f
    }

    private fun enforceHeadOfHouseholdLockedState() {
        if (!isHeadOfHouseholdLockedToNo) return
        updateHohSections(isHeadOfHousehold = false)
        binding.rgHOHOptions.isEnabled = false
        binding.radioHOHYes.isEnabled = false
        binding.radioHOHNo.isEnabled = false
        binding.llHeadOfHousehold.alpha = 0.6f
    }

    private fun updateHohSections(isHeadOfHousehold: Boolean) {
        binding.llHohYes.visibility = if (isHeadOfHousehold) View.VISIBLE else View.GONE
        binding.llRelationWithHOH.visibility = if (isHeadOfHousehold) View.GONE else View.VISIBLE
    }

    private fun setupHohCheck() {
        binding.rgHOHOptions.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingHohSelection) return@setOnCheckedChangeListener
            if (isHeadOfHouseholdLockedToNo) {
                updateHohSections(isHeadOfHousehold = false)
                return@setOnCheckedChangeListener
            }
            when (checkedId) {
                binding.radioHOHYes.id -> {
                    updateHohSections(isHeadOfHousehold = true)
                }

                else -> {
                    updateHohSections(isHeadOfHousehold = false)
                }
            }
        }
    }

    private fun setupSourceOfWater() {
        createCheckboxes(binding.cgSourceOfWater, R.array.source_of_water)
    }

    private fun setupSourceOfLight() {
        createCheckboxes(binding.cgSourceOfLight, R.array.source_of_light)
    }

    private fun setupFuelType() {
        createCheckboxes(binding.cgFuelType, R.array.fuel_type)
    }

    private fun setupHandWashPractice() {
        createCheckboxes(binding.cgHandWashPractices, R.array.hand_wash_practice)
    }

    private fun setupWaterSafeguarding() {
        createCheckboxes(binding.cgSafeguardWater, R.array.safeguard_water)
    }

    private fun createCheckboxes(container: LinearLayout, arrayResId: Int) {
        val typedArray = resources.obtainTypedArray(arrayResId)

        for (i in 0 until typedArray.length()) {
            val resId = typedArray.getResourceId(i, 0)
            if (resId != 0) {
                val localizedLabel = getString(resId) // current locale
                val checkBox = CheckBox(container.context).apply {
                    text = localizedLabel
                    tag = resId
                }
                container.addView(checkBox)
            }
        }

        typedArray.recycle()
    }


    private fun setupEconomicStatus() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.economic)
        binding.acEconomicStatusCheck.setAdapter(adapter)

        binding.acEconomicStatusCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilEconomicStatusOption.hideError()
            binding.acEconomicStatusCheck.setText(
                resources.getStringArray(R.array.economic)[i],
                false
            )
        }
    }

    private fun setupReligion() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.baseline_religion)
        binding.acReligion.setAdapter(adapter)

        binding.acReligion.setOnItemClickListener { _, _, i, _ ->
            binding.tilReligionOption.hideError()
            binding.acReligion.setText(
                resources.getStringArray(R.array.baseline_religion)[i],
                false
            )
        }
    }

    private fun setupNumberOfToiletFacilities() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(
            requireContext(),
            R.array.baseline_toilet_facilities
        )

        binding.acToiletFacility.setAdapter(adapter)

        binding.acToiletFacility.setOnItemClickListener { _, _, i, _ ->
            binding.tilToiletFacilityOption.hideError()
            binding.acToiletFacility.setText(
                resources.getStringArray(R.array.baseline_toilet_facilities)[i],
                false
            )
        }
    }

    private fun setupHouseStructure() {
        val adapter =
            ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.baseline_house_structure)
        binding.acHouseStructure.setAdapter(adapter)

        binding.acHouseStructure.setOnItemClickListener { _, _, i, _ ->
            binding.tilHouseStructureOption.hideError()
            binding.acHouseStructure.setText(
                resources.getStringArray(R.array.baseline_house_structure)[i],
                false
            )
        }
    }

    private fun setupCultivableLand() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(
            requireContext(),
            R.array.baseline_cultivable_land
        )

        binding.acCultivableLand.setAdapter(adapter)
        binding.acCultivableLand.setOnItemClickListener { _, _, i, _ ->
            binding.tilCultivableLandOption.hideError()
            binding.acCultivableLand.setText(
                resources.getStringArray(R.array.baseline_cultivable_land)[i],
                false
            )
        }

        binding.acCultivableLand.doOnTextChanged { text, start, before, count ->
            val value = getString(R.string.cultivable_land_landless)
            if (text?.isEmpty() == true || value == text.toString()) {
                binding.tilCultivableLandValue.visibility = View.GONE
                isLandlessOptionChosen = true
            } else {
                binding.tilCultivableLandValue.visibility = View.VISIBLE
                isLandlessOptionChosen = false
            }
        }
    }

    private fun setClickListener() {
        binding.frag3BtnBack.setOnClickListener {
            BaselineOtherFragmentDirections.navigationOtherToMedical().apply {
                findNavController().navigate(this)
            }
        }
        binding.frag3BtnNext.setOnClickListener {
            validateFields { saveSurveyData() }
        }
    }

    private fun validateFields(block: () -> Unit) {
        val isHeadOfHousehold = binding.llHohYes.isVisible
        val isElectricityAvailable = binding.radioElectricityYes.isChecked
        val isRunningWaterAvailable = binding.radioWaterCheckYes.isChecked

        val error = R.string.this_field_is_mandatory
        val usualMembersError =
            R.string.error_number_of_people_living_cannot_be_greater_than_the_total_number_of_members_in_the_household
        val hoursError = R.string.load_shedding_hours_error
        val daysError = R.string.load_shedding_days_error

        binding.otherConfig?.let {
            val headOfHousehold =
                if (isHeadOfHouseholdLockedToNo) {
                    true
                } else if (it.headOfHousehold!!.isEnabled && it.headOfHousehold!!.isMandatory) {
                    binding.rgHOHOptions.validate()
                } else true

            val rationCardCheck =
                if (it.rationCardCheck!!.isEnabled && it.rationCardCheck!!.isMandatory && isHeadOfHousehold) {
                    binding.rgRationOptions.validate()
                } else true

            val economicStatus =
                if (it.economicStatus!!.isEnabled && it.economicStatus!!.isMandatory && isHeadOfHousehold) {
                    binding.tilEconomicStatusOption.validateDropDowb(
                        binding.acEconomicStatusCheck,
                        error
                    )
                } else true

            val religion =
                if (it.religion!!.isEnabled && it.religion!!.isMandatory && isHeadOfHousehold) {
                    binding.tilReligionOption.validateDropDowb(binding.acReligion, error)
                } else true

            val totalHouseholdMembers =
                if (it.totalHouseholdMembers!!.isEnabled && it.totalHouseholdMembers!!.isMandatory && isHeadOfHousehold) {
                    binding.tilTotalMemberOption.validate(binding.textInputTotalHHMembers, error)
                } else true

            val usualHouseholdMembers =
                if (it.usualHouseholdMembers!!.isEnabled && it.usualHouseholdMembers!!.isMandatory && isHeadOfHousehold) {
                    binding.tilUsualMemberOption.validate(binding.textInputUsualHHMembers, error)
                    binding.tilUsualMemberOption.validateNumberOfUsualMembers(
                        binding.textInputUsualHHMembers,
                        binding.textInputTotalHHMembers,
                        usualMembersError
                    )
                } else true

            val numberOfSmartphones =
                if (it.numberOfSmartphones!!.isEnabled && it.numberOfSmartphones!!.isMandatory && isHeadOfHousehold) {
                    binding.tilNumberOfSmartphonesOption.validate(
                        binding.textInputNoOfSmartPhones,
                        error
                    )
                } else true

            val numberOfFeaturePhones =
                if (it.numberOfFeaturePhones!!.isEnabled && it.numberOfFeaturePhones!!.isMandatory && isHeadOfHousehold) {
                    binding.tilNumberOfFeaturePhoneOption.validate(
                        binding.textInputNoOfFeaturePhones,
                        error
                    )
                } else true

            val numberOfEarningMembers =
                if (it.numberOfEarningMembers!!.isEnabled && it.numberOfEarningMembers!!.isMandatory && isHeadOfHousehold) {
                    binding.tilNumberOfEarningMembersOption.validate(
                        binding.textInputEarningMembers,
                        error
                    )
                } else true

            val electricityCheck =
                if (it.electricityCheck!!.isEnabled && it.electricityCheck!!.isMandatory && isHeadOfHousehold) {
                    binding.rgElectricityOptions.validate()
                } else true

            val loadSheddingHours =
                if (it.loadSheddingHours!!.isEnabled && it.loadSheddingHours!!.isMandatory && isHeadOfHousehold && isElectricityAvailable) {
                    binding.tilLoadShedingHoursOption.validate(
                        binding.textInputloadSheddingHours,
                        error
                    )
                    binding.tilLoadShedingHoursOption.validateIntegerDataLimits(
                        binding.textInputloadSheddingHours,
                        Constants.LIMIT_START_HOURS,
                        Constants.LIMIT_END_HOURS,
                        hoursError
                    )
                } else true

            val loadSheddingDays =
                if (it.loadSheddingDays!!.isEnabled && it.loadSheddingDays!!.isMandatory && isHeadOfHousehold && isElectricityAvailable) {
                    binding.tilLoadShedingDaysOption.validate(
                        binding.textInputloadSheddingDays,
                        error
                    )

                    binding.tilLoadShedingDaysOption.validateIntegerDataLimits(
                        binding.textInputloadSheddingDays,
                        Constants.LIMIT_START_DAY,
                        Constants.LIMIT_END_DAY,
                        daysError
                    )
                } else true

            val waterCheck =
                if (it.waterCheck!!.isEnabled && it.waterCheck!!.isMandatory && isHeadOfHousehold) {
                    binding.rgWaterCheckOptions.validate()
                } else true

            val waterAvailabilityHours =
                if (it.waterAvailabilityHours!!.isEnabled && it.waterAvailabilityHours!!.isMandatory && isHeadOfHousehold && isRunningWaterAvailable) {
                    binding.tilWaterAvailabilityHoursOption.validate(
                        binding.textInputWaterAvailabilityHours,
                        error
                    )
                    binding.tilWaterAvailabilityHoursOption.validateIntegerDataLimits(
                        binding.textInputWaterAvailabilityHours,
                        Constants.LIMIT_START_HOURS,
                        Constants.LIMIT_END_HOURS,
                        hoursError
                    )
                } else true

            val waterAvailabilityDays =
                if (it.waterAvailabilityDays!!.isEnabled && it.waterAvailabilityDays!!.isMandatory && isHeadOfHousehold && isRunningWaterAvailable) {
                    binding.tilWaterAvailabilityDaysOption.validate(
                        binding.textInputWaterAvailabilityDays,
                        error
                    )

                    binding.tilWaterAvailabilityDaysOption.validateIntegerDataLimits(
                        binding.textInputWaterAvailabilityDays,
                        Constants.LIMIT_START_DAY,
                        Constants.LIMIT_END_DAY,
                        daysError
                    )
                } else true

            val sourceOfWater =
                if (it.sourceOfWater!!.isEnabled && it.sourceOfWater!!.isMandatory && isHeadOfHousehold) {
                    binding.cgSourceOfWater.validateCheckboxes()
                } else true

            val safeguardWater =
                if (it.safeguardWater!!.isEnabled && it.sourceOfWater!!.isMandatory && isHeadOfHousehold) {
                    binding.cgSafeguardWater.validateCheckboxes()
                } else true

            val distanceFromWater =
                if (it.distanceFromWater!!.isEnabled && it.distanceFromWater!!.isMandatory && isHeadOfHousehold) {
                    binding.rgDistanceFromWaterOptions.validate()
                } else true

            val toiletFacility =
                if (it.toiletFacility!!.isEnabled && it.toiletFacility!!.isMandatory && isHeadOfHousehold) {
                    binding.tilToiletFacilityOption.validateDropDowb(
                        binding.acToiletFacility,
                        error
                    )
                } else true

            val houseStructure =
                if (it.houseStructure!!.isEnabled && it.houseStructure!!.isMandatory && isHeadOfHousehold) {
                    binding.tilHouseStructureOption.validateDropDowb(
                        binding.acHouseStructure,
                        error
                    )
                } else true

            val cultivableLand =
                if (it.cultivableLand!!.isEnabled && it.cultivableLand!!.isMandatory && isHeadOfHousehold) {
                    binding.tilCultivableLandOption.validateDropDowb(
                        binding.acCultivableLand,
                        error
                    )
                } else true

            val cultivableLandValue =
                if (it.cultivableLandValue!!.isEnabled && it.cultivableLandValue!!.isMandatory && isHeadOfHousehold && !isLandlessOptionChosen) {
                    binding.tilCultivableLandValue.validate(
                        binding.textInputCultivableLandValue,
                        error
                    )
                } else true

            val averageIncome =
                if (it.averageIncome!!.isEnabled && it.averageIncome!!.isMandatory && isHeadOfHousehold) {
                    binding.rgAverageIncomeOptions.validate()
                } else true

            val fuelType =
                if (it.fuelType!!.isEnabled && it.fuelType!!.isMandatory && isHeadOfHousehold) {
                    binding.cgFuelType.validateCheckboxes()
                } else true

            val sourceOfLight =
                if (it.sourceOfLight!!.isEnabled && it.sourceOfLight!!.isMandatory && isHeadOfHousehold) {
                    binding.cgSourceOfLight.validateCheckboxes()
                } else true

            val handWashPractices =
                if (it.handWashPractices!!.isEnabled && it.handWashPractices!!.isMandatory && isHeadOfHousehold) {
                    binding.cgHandWashPractices.validateCheckboxes()
                } else true

            val ekalServiceCheck =
                if (it.ekalServiceCheck!!.isEnabled && it.ekalServiceCheck!!.isMandatory && isHeadOfHousehold) {
                    binding.rgEkalServiceCheckOptions.validate()
                } else true

            val relationWithHousehold =
                if (it.relationWithHousehold!!.isEnabled && it.relationWithHousehold!!.isMandatory && !isHeadOfHousehold) {
                    binding.rgRelationWithHohOptions.validate()
                } else true

            if (headOfHousehold.and(rationCardCheck).and(rationCardCheck).and(economicStatus)
                    .and(religion).and(totalHouseholdMembers).and(usualHouseholdMembers)
                    .and(numberOfSmartphones).and(numberOfFeaturePhones).and(numberOfEarningMembers)
                    .and(electricityCheck).and(loadSheddingHours).and(loadSheddingDays)
                    .and(waterCheck).and(waterAvailabilityHours).and(waterAvailabilityDays)
                    .and(sourceOfWater).and(safeguardWater).and(distanceFromWater)
                    .and(toiletFacility).and(houseStructure).and(cultivableLand)
                    .and(cultivableLandValue).and(averageIncome).and(fuelType).and(sourceOfLight)
                    .and(handWashPractices).and(ekalServiceCheck).and(relationWithHousehold)
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

    private fun saveSurveyData() {
        val isHeadOfHousehold = binding.llHohYes.isVisible

        baselineSurveyData.apply {
            headOfHousehold = binding.rgHOHOptions.getSelectedDataInEnglishLocale(requireContext())

            if (!isHeadOfHousehold) {
                relationWithHousehold = binding
                    .rgRelationWithHohOptions
                    .getSelectedDataInEnglishLocale(requireContext())

                setOptionalFieldsInOtherWithHyphen()
            } else {
                rationCardCheck = binding
                    .rgRationOptions
                    .getSelectedDataInEnglishLocale(requireContext())

                economicStatus = binding.acEconomicStatusCheck.getTextInEnglish(
                    requireContext(),
                    R.array.economic
                )
                religion = binding.acReligion.getTextInEnglish(
                    requireContext(),
                    R.array.baseline_religion
                )

                totalHouseholdMembers = binding.textInputTotalHHMembers.text.toString()
                usualHouseholdMembers = binding.textInputUsualHHMembers.text.toString()
                numberOfSmartphones = binding.textInputNoOfSmartPhones.text.toString()
                numberOfFeaturePhones = binding.textInputNoOfFeaturePhones.text.toString()
                numberOfEarningMembers = binding.textInputEarningMembers.text.toString()

                electricityCheck = binding
                    .rgElectricityOptions
                    .getSelectedDataInEnglishLocale(requireContext())

                loadSheddingHours = binding
                    .llLoadShedding
                    .getTextIfVisible(binding.textInputloadSheddingHours)

                loadSheddingDays = binding
                    .llLoadShedding
                    .getTextIfVisible(binding.textInputloadSheddingDays)

                waterCheck = binding
                    .rgWaterCheckOptions
                    .getSelectedDataInEnglishLocale(requireContext())

                waterAvailabilityHours = binding
                    .llWaterAvailability
                    .getTextIfVisible(binding.textInputWaterAvailabilityHours)

                waterAvailabilityDays = binding
                    .llWaterAvailability
                    .getTextIfVisible(binding.textInputWaterAvailabilityDays)

                sourceOfWater = binding.cgSourceOfWater.getSelectedCheckboxes()
                safeguardWater = binding.cgSafeguardWater.getSelectedCheckboxes()

                distanceFromWater = binding
                    .rgDistanceFromWaterOptions
                    .getSelectedDataInEnglishLocale(requireContext())

                toiletFacility = binding
                    .acToiletFacility
                    .getTextInEnglish(requireContext(), R.array.baseline_toilet_facilities)

                houseStructure = binding
                    .acHouseStructure
                    .getTextInEnglish(requireContext(), R.array.baseline_house_structure)

                cultivableLand = binding
                    .acCultivableLand
                    .getTextInEnglish(requireContext(), R.array.baseline_cultivable_land)

                cultivableLandValue = binding
                    .tilCultivableLandValue
                    .getTextIfVisible(binding.textInputCultivableLandValue)

                averageIncome = binding
                    .rgAverageIncomeOptions
                    .getSelectedDataInEnglishLocale(requireContext())

                fuelType = binding.cgFuelType.getSelectedCheckboxes()

                sourceOfLight = binding.cgSourceOfLight.getSelectedCheckboxes()

                handWashPractices = binding.cgHandWashPractices.getSelectedCheckboxes()

                ekalServiceCheck = binding
                    .rgEkalServiceCheckOptions
                    .getSelectedDataInEnglishLocale(requireContext())
            }

            isSavingSurvey = true
            enforceHeadOfHouseholdLockedState()
            baselineSurveyViewModel.updateBaselineData(this)
            baselineSurveyViewModel.savePatient().observe(viewLifecycleOwner) {
                it ?: run {
                    isSavingSurvey = false
                    return@observe
                }
                baselineSurveyViewModel.handleResponse(it) { result ->
                    isSavingSurvey = false
                    if (result) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.baseline_survey_completed),
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToPatientDetailsScreen()
                    }
                }
            }
        }
    }

    private fun navigateToPatientDetailsScreen() {
        val tabName = requireActivity().intent.getStringExtra(org.intelehealth.ncd.constants.Constants.INTENT_NCD_CATEGORY) ?: "general"

        val also = BaselineOtherFragmentDirections.navigationOtherToPatientDetails(
            baselineSurveyViewModel.patientId, "searchPatient", "false", tabName
        ).also {
            findNavController().navigate(it)
            requireActivity().finish()
        }
    }

    private fun setupRadioButtons() {
        binding.radioHOHYes.tag = R.string.yes
        binding.radioHOHNo.tag = R.string.no

        binding.radioRationYes.tag = R.string.yes
        binding.radioRationNo.tag = R.string.no
        binding.radioRationNotSure.tag = R.string.generic_not_sure

        binding.radioElectricityYes.tag = R.string.yes
        binding.radioElectricityNo.tag = R.string.no

        binding.radioWaterCheckYes.tag = R.string.yes
        binding.radioWaterCheckNo.tag = R.string.no

        binding.radioDistanceFromWaterYes.tag = R.string.yes
        binding.radioDistanceFromWaterNo.tag = R.string.no

        binding.radioAverageIncome1.tag = R.string.zero_thirty_thousand
        binding.radioAverageIncome2.tag = R.string.thirty_fifty_thousand
        binding.radioAverageIncome3.tag = R.string.fifty_thousand_one_lakh
        binding.radioAverageIncome4.tag = R.string.one_lakh_two_lakh_fifty_thousand
        binding.radioAverageIncome5.tag = R.string.more_than_two_lakh_fifty_thousand

        binding.radioEkalServiceYes.tag = R.string.yes
        binding.radioEkalServiceNo.tag = R.string.no

        binding.radioSpouse.tag = R.string.spouse
        binding.radioSonDaughter.tag = R.string.son_or_daughter
        binding.radioSonDaughterInLaw.tag = R.string.son_or_daughter_in_law
        binding.radioGrandchild.tag = R.string.grandchild
        binding.radioFatherMother.tag = R.string.father_mother
        binding.radioFatherMotherInLaw.tag = R.string.father_mother_in_law
        binding.radioBrotherSister.tag = R.string.brother_sister
        binding.radioBrotherSisterInLaw.tag = R.string.brother_sister_in_law
        binding.radioNieceNephew.tag = R.string.niece_nephew
        binding.radioGrandparent.tag = R.string.grandparent
        binding.radioOther.tag = R.string.other_relative
        binding.radioAdopted.tag = R.string.adopted
        binding.radioServant.tag = R.string.servant
        binding.radioOtherNotRelated.tag = R.string.other_not_related
        binding.radioNotStated.tag = R.string.not_stated
    }
}