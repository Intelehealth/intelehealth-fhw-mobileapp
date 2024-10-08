package org.intelehealth.app.activities.location_survey

import android.os.Bundle
import android.webkit.URLUtil
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.R
import org.intelehealth.app.activities.setupActivity.LocationArrayAdapter
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.NewLocationDao
import org.intelehealth.app.databinding.ActivityLocationSurveyBinding
import org.intelehealth.app.models.Location
import org.intelehealth.app.models.locationAttributes.pull.PullLocationAttributesData
import org.intelehealth.app.models.locationAttributes.pull.PullLocationAttributesRoot
import org.intelehealth.app.models.statewise_location.Setup_LocationModel
import org.intelehealth.app.networkApiCalls.ApiClient
import org.intelehealth.app.networkApiCalls.ApiInterface
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.exception.DAOException


class LocationSurveyActivity : AppCompatActivity() {

    private var binding: ActivityLocationSurveyBinding? = null

    private var url: String? = null
    private var isLocationFetched: Boolean = false
    private var newLocationDao: NewLocationDao? = null
    private var selectedState: String? = ""
    private var selectedDistrict: String? = ""
    private var selectedSanch: String? = ""
    private var selectedPrimaryVillage: String? = ""
    private var selectedSecondaryVillage: String? = ""

    private var villageNameHashMap: HashMap<String?, String?> = HashMap()
    private var sessionManager: SessionManager? = null

    private var districtArrayAdapter: LocationArrayAdapter? = null
    private var sanchArrayAdapter: LocationArrayAdapter? = null
    private var primaryVillageArrayAdapter: LocationArrayAdapter? = null
    private var secondaryVillageArrayAdapter: LocationArrayAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSurveyBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        sessionManager = SessionManager(this)

        fetchIntentData()
        setListeners()
        initializeButtons()
        initializeAutoTextViewDropDowns()
        fetchLocations()
    }

    private fun setLocationDataIfPresent() {
        if (sessionManager?.stateName?.isBlank() == false) {
            binding?.autotvSelectState?.setText(sessionManager?.stateName, false)
            binding?.autotvSelectState?.isEnabled = true
        }

        if (sessionManager?.districtName?.isBlank() == false) {
            binding?.autotvSelectDistrict?.setText(sessionManager?.districtName, false)
            binding?.autotvSelectDistrict?.isEnabled = true
            binding?.autotvSelectDistrict?.setAdapter(getDistrictArrayAdapter())
        }

        if (sessionManager?.sanchName?.isBlank() == false) {
            binding?.autotvSelectSanch?.setText(sessionManager?.sanchName, false)
            binding?.autotvSelectSanch?.isEnabled = true
            binding?.autotvSelectSanch?.setAdapter(getSanchArrayAdapter())
        }

        if (sessionManager?.currentLocationName?.isBlank() == false) {
            binding?.autotvSelectPrimaryVillage?.setText(sessionManager?.currentLocationName, false)
            binding?.autotvSelectPrimaryVillage?.isEnabled = true
            binding?.autotvSelectPrimaryVillage?.setAdapter(getPrimaryVillageArrayAdapter())
            fetchAndSetLocationAttributes(sessionManager?.currentLocationUuid)
        }

        if (sessionManager?.secondaryLocationName?.isBlank() == false) {
            binding?.autotvSelectSecondaryVillage?.setText(
                sessionManager?.secondaryLocationName,
                false
            )
            binding?.autotvSelectSecondaryVillage?.isEnabled = true
            binding?.autotvSelectSecondaryVillage?.setAdapter(getSecondaryVillageArrayAdapter())
        }
    }

    private fun getDistrictArrayAdapter(): LocationArrayAdapter? {
        val districtLocationList: MutableList<String>? =
            newLocationDao?.getDistrictList(
                sessionManager?.stateName,
                this@LocationSurveyActivity
            )

        return if (districtLocationList != null && districtLocationList.size > 1) {
            getLocationArrayAdapter(districtLocationList)
        } else {
            null
        }
    }

    private fun getSanchArrayAdapter(): LocationArrayAdapter? {
        val sanchLocationList: MutableList<String>? =
            newLocationDao?.getSanchList(
                sessionManager?.stateName,
                sessionManager?.districtName,
                this@LocationSurveyActivity
            )

        return if (sanchLocationList != null && sanchLocationList.size > 1) {
            getLocationArrayAdapter(sanchLocationList)
        } else {
            null
        }
    }

    private fun getPrimaryVillageArrayAdapter(): LocationArrayAdapter? {
        val primaryVillageLocationsList: MutableList<String>? =
            newLocationDao?.getVillageList(
                sessionManager?.stateName,
                sessionManager?.districtName,
                sessionManager?.sanchName,
                this@LocationSurveyActivity,
                "primary"
            )

        return if (primaryVillageLocationsList != null && primaryVillageLocationsList.size > 1) {
            getLocationArrayAdapter(primaryVillageLocationsList)
        } else {
            null
        }
    }

    private fun getSecondaryVillageArrayAdapter(): LocationArrayAdapter? {
        val secondaryVillageLocationsList: MutableList<String>? =
            newLocationDao?.getVillageList(
                sessionManager?.stateName,
                sessionManager?.districtName,
                sessionManager?.sanchName,
                this@LocationSurveyActivity,
                "secondary"
            )

        secondaryVillageLocationsList?.removeAt(
            secondaryVillageLocationsList.indexOf(
                sessionManager?.currentLocationName
            )
        )

        return if (secondaryVillageLocationsList != null && secondaryVillageLocationsList.size > 1) {
            getLocationArrayAdapter(secondaryVillageLocationsList)
        } else {
            null
        }
    }

    private fun getLocationArrayAdapter(list: MutableList<String>?): LocationArrayAdapter =
        LocationArrayAdapter(
            this@LocationSurveyActivity,
            list
        )

    private fun initializeAutoTextViewDropDowns() {
        binding?.autotvSelectState?.isEnabled = false
        binding?.autotvSelectDistrict?.isEnabled = false
        binding?.autotvSelectSanch?.isEnabled = false
        binding?.autotvSelectPrimaryVillage?.isEnabled = false
        binding?.autotvSelectSecondaryVillage?.isEnabled = false
    }

    private fun setListeners() {
        binding?.autotvSelectState?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                if (position != 0) {
                    selectedState = parent?.getItemAtPosition(position)?.toString()
                    sessionManager?.stateName = selectedState

                    districtArrayAdapter = getDistrictArrayAdapter()

                    if (districtArrayAdapter != null) {
                        setDropdownValuesToDefault("state")
                        binding?.autotvSelectDistrict?.setEnabled(true)
                        binding?.autotvSelectDistrict?.setAlpha(1.0f)
                        binding?.autotvSelectDistrict?.setAdapter(districtArrayAdapter)
                        isLocationFetched = true
                    } else {
                        emptySpinner("state")
                    }
                } else {
                    sessionManager?.stateName = ""
                }
            }

        binding?.autotvSelectDistrict?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (position != 0) {
                    selectedDistrict = parent?.getItemAtPosition(position)?.toString()
                    sessionManager?.districtName = selectedDistrict

                    sanchArrayAdapter = getSanchArrayAdapter()

                    if (sanchArrayAdapter != null) {
                        setDropdownValuesToDefault("district")
                        binding?.autotvSelectSanch?.setEnabled(true)
                        binding?.autotvSelectSanch?.setAlpha(1.0f)
                        binding?.autotvSelectSanch?.setAdapter(sanchArrayAdapter)
                        isLocationFetched = true
                    } else {
                        emptySpinner("district")
                    }
                } else {
                    sessionManager?.districtName = ""
                    emptySpinner("district")
                }
            }

        binding?.autotvSelectSanch?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                if (position != 0) {
                    selectedSanch = parent?.getItemAtPosition(position)?.toString()
                    sessionManager?.sanchName = selectedSanch

                    primaryVillageArrayAdapter = getPrimaryVillageArrayAdapter()

                    if (primaryVillageArrayAdapter != null) {
                        setDropdownValuesToDefault("sanch")
                        binding?.autotvSelectPrimaryVillage?.setEnabled(true)
                        binding?.autotvSelectPrimaryVillage?.setAlpha(1.0f)
                        binding?.autotvSelectPrimaryVillage?.setAdapter(primaryVillageArrayAdapter)
                        isLocationFetched = true
                    } else {
                        emptySpinner("sanch")
                    }
                } else {
                    sessionManager?.sanchName = ""
                    emptySpinner("sanch")
                }
                unselectExistingRadioButtons()
            }

        binding?.autotvSelectPrimaryVillage?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (position != 0) {
                    selectedPrimaryVillage = parent?.getItemAtPosition(position)?.toString()
                    sessionManager?.villageName = selectedPrimaryVillage
                    sessionManager?.currentLocationName = selectedPrimaryVillage


                    val primaryVillageUuid: String? = newLocationDao?.getVillageUuid(
                        sessionManager?.stateName,
                        sessionManager?.districtName,
                        sessionManager?.sanchName,
                        sessionManager?.villageName
                    )

                    sessionManager?.currentLocationUuid = primaryVillageUuid

                    val tempPrimaryVillageHashMap: HashMap<String?, String?> = HashMap()
                    tempPrimaryVillageHashMap[primaryVillageUuid] = selectedPrimaryVillage
                    villageNameHashMap = tempPrimaryVillageHashMap

                    secondaryVillageArrayAdapter = getSecondaryVillageArrayAdapter()

                    if (secondaryVillageArrayAdapter != null) {
                        setDropdownValuesToDefault("village")
                        binding?.autotvSelectSecondaryVillage?.setEnabled(true)
                        binding?.autotvSelectSecondaryVillage?.setAlpha(1.0f)
                        binding?.autotvSelectSecondaryVillage?.setAdapter(
                            secondaryVillageArrayAdapter
                        )
                        isLocationFetched = true
                    } else {
                        emptySpinner("village")
                    }

                    unselectExistingRadioButtons()
                    fetchAndSetLocationAttributes(primaryVillageUuid)
                } else {
                    sessionManager?.villageName = ""
                    sessionManager?.currentLocationName = ""
                    sessionManager?.currentLocationUuid = ""
                }
            }

        binding?.autotvSelectSecondaryVillage?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                if (position != 0) {
                    selectedSecondaryVillage = parent?.getItemAtPosition(position)?.toString()
                    val secondaryVillageUuid: String? = newLocationDao?.getVillageUuid(
                        selectedState,
                        selectedDistrict,
                        selectedSanch,
                        selectedPrimaryVillage
                    )

                    sessionManager?.secondaryLocationName = selectedSecondaryVillage
                    sessionManager?.secondaryLocationUuid = secondaryVillageUuid
                    isLocationFetched = true
                } else {
                    sessionManager?.secondaryLocationName = ""
                    sessionManager?.secondaryLocationUuid = ""
                }
            }
    }

    private fun setDropdownValuesToDefault(changedField: String) {
        when (changedField) {
            "state" -> {
                binding?.autotvSelectDistrict?.setText(resources.getString(R.string.select_district))
                binding?.autotvSelectSanch?.setText(resources.getString(R.string.select_sanch))
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }

            "district" -> {
                binding?.autotvSelectSanch?.setText(resources.getString(R.string.select_sanch))
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }

            "sanch" -> {
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }

            "village" -> {
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }
        }
    }

    private fun fetchIntentData() {
        url = intent.getStringExtra(AppConstants.INTENT_SERVER_URL)
    }

    private fun fetchLocations() {
        isLocationFetched = false
        val baseUrl = "https://$url:3004/api/openmrs/"

        if (URLUtil.isValidUrl(baseUrl) && !isLocationFetched) {
            ApiClient.changeApiBaseUrl(baseUrl)
            val apiService: ApiInterface = ApiClient.createService(ApiInterface::class.java)

            try {
                val resultsObservable: Observable<Setup_LocationModel> =
                    apiService.SETUP_LOCATIONOBSERVABLE()

                resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableObserver<Setup_LocationModel>() {
                        override fun onNext(location: Setup_LocationModel) {
                            if (location.states != null) {
                                try {
                                    newLocationDao = NewLocationDao()
                                    newLocationDao?.insertSetupLocations(location)
                                    newLocationDao?.getStateList(this@LocationSurveyActivity)
                                        ?.let { stateLocations ->

                                            if (stateLocations.size != 0) {
                                                val locationArrayAdapter = LocationArrayAdapter(
                                                    this@LocationSurveyActivity,
                                                    stateLocations
                                                )

                                                binding?.autotvSelectState?.setEnabled(true)
                                                binding?.autotvSelectState?.setAlpha(1.0f)
                                                binding?.autotvSelectState?.setAdapter(
                                                    locationArrayAdapter
                                                )
                                                isLocationFetched = true
                                                setLocationDataIfPresent()
                                            } else {
                                                emptySpinner("state")
                                            }
                                        }
                                } catch (exception: DAOException) {
                                    exception.printStackTrace()
                                    isLocationFetched = false
                                }
                            }
                        }

                        override fun onError(e: Throwable) {
                            isLocationFetched = false
                            if (e.localizedMessage?.contains("Unable to resolve host") == true) {
                                Toast.makeText(
                                    this@LocationSurveyActivity,
                                    getString(R.string.url_invalid),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@LocationSurveyActivity,
                                    e.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onComplete() {
                            isLocationFetched = true
                        }
                    })
            } catch (exception: IllegalArgumentException) {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        } else
            Toast.makeText(
                this@LocationSurveyActivity,
                getString(R.string.url_invalid),
                Toast.LENGTH_SHORT
            ).show()
    }

    private fun emptySpinner(value: String) {
        unselectExistingRadioButtons()

        when (value) {

            "state" -> {
                binding?.autotvSelectDistrict?.setEnabled(false)
                binding?.autotvSelectDistrict?.setAlpha(0.4f)
                binding?.autotvSelectDistrict?.setText(resources.getString(R.string.select_district))

                binding?.autotvSelectSanch?.setEnabled(false)
                binding?.autotvSelectSanch?.setAlpha(0.4f)
                binding?.autotvSelectSanch?.setText(resources.getString(R.string.select_sanch))

                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))

                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))

            }

            "district" -> {
                binding?.autotvSelectSanch?.setEnabled(false)
                binding?.autotvSelectSanch?.setAlpha(0.4f)
                binding?.autotvSelectSanch?.setText(resources.getString(R.string.select_sanch))

                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))

                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }

            "sanch" -> {
                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))

                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }

            "village" -> {
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }

            else -> {
                binding?.autotvSelectState?.setEnabled(false)
                binding?.autotvSelectState?.setAlpha(0.4f)
                binding?.autotvSelectState?.setText(resources.getString(R.string.select_state))

                binding?.autotvSelectDistrict?.setEnabled(false)
                binding?.autotvSelectDistrict?.setAlpha(0.4f)
                binding?.autotvSelectDistrict?.setText(resources.getString(R.string.select_district))

                binding?.autotvSelectSanch?.setEnabled(false)
                binding?.autotvSelectSanch?.setAlpha(0.4f)
                binding?.autotvSelectSanch?.setText(resources.getString(R.string.select_sanch))

                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))

                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))
            }
        }
    }

    private fun unselectExistingRadioButtons() {
        binding?.cbScDistance?.clearCheck()
        binding?.cbPhcDistance?.clearCheck()
        binding?.cbChcDistance?.clearCheck()
        binding?.cbDhDistance?.clearCheck()
        binding?.cbMsDistance?.clearCheck()
        binding?.cbPlDistance?.clearCheck()
        binding?.cbPcDistance?.clearCheck()
        binding?.cbPcamDistance?.clearCheck()
        binding?.cbJjyDistance?.clearCheck()
    }

    private fun getLocationStringList(locationList: List<Location>): List<String> {
        val list: MutableList<String> = ArrayList()

        try {
            locationList.forEach {
                list.add(it.display)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    private fun initializeButtons() {
        binding?.backBtn?.setOnClickListener {
            storeSurveyDataAndGoBack()
        }

        binding?.btnBack?.setOnClickListener {
            storeSurveyDataAndGoBack()
        }

        binding?.btnSave?.setOnClickListener {
            storeSurveyDataAndGoBack()
        }
    }

    private fun storeSurveyDataAndGoBack() {
        storeSurveyData()
        finish()
    }

    private fun storeSurveyData() {
        val subCentreDistance: String? = binding?.cbScDistance?.checkedChipId?.let { chipId ->
            binding?.cbScDistance?.findViewById<Chip>(chipId)?.text?.toString()
        }

        sessionManager?.subCentreDistance = subCentreDistance

        val primaryHealthCenterDistance: String? =
            binding?.cbPhcDistance?.checkedChipId?.let { chipId ->
                binding?.cbPhcDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.primaryHealthCentreDistance = primaryHealthCenterDistance

        val communityHealthCenterDistance: String? =
            binding?.cbChcDistance?.checkedChipId?.let { chipId ->
                binding?.cbChcDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.communityHealthCentreDistance = communityHealthCenterDistance

        val districtHospital: String? =
            binding?.cbDhDistance?.checkedChipId?.let { chipId ->
                binding?.cbDhDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.districtHospitalDistance = districtHospital

        val medicalStore: String? =
            binding?.cbMsDistance?.checkedChipId?.let { chipId ->
                binding?.cbMsDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.medicalStoreDistance = medicalStore

        val pathologicalLab: String? =
            binding?.cbPlDistance?.checkedChipId?.let { chipId ->
                binding?.cbPlDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.pathologicalLabDistance = pathologicalLab

        val privateClinicWithMbbsDoctorDistance: String? =
            binding?.cbPcDistance?.checkedChipId?.let { chipId ->
                binding?.cbPcDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.privateClinicWithMbbsDoctorDistance = privateClinicWithMbbsDoctorDistance

        val privateClinicWithAlternateMedicalRadioGroup: String? =
            binding?.cbPcamDistance?.checkedChipId?.let { chipId ->
                binding?.cbPcamDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.privateClinicWithAlternateDoctorDistance =
            privateClinicWithAlternateMedicalRadioGroup

        val jalJeevanYojana: String? =
            binding?.cbJjyDistance?.checkedChipId?.let { chipId ->
                binding?.cbJjyDistance?.findViewById<Chip>(chipId)?.text?.toString()
            }

        sessionManager?.jalJeevanYojanaScheme = jalJeevanYojana
    }

    private fun fetchAndSetLocationAttributes(villageUuid: String?) {
        val finalURL = "https://${url}/locattribs/${villageUuid}"
        val pullLocationAttributesRootObservable: Observable<PullLocationAttributesRoot> =
            AppConstants.apiInterface.PULL_LOCATION_ATTRIBUTES(finalURL)

        pullLocationAttributesRootObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PullLocationAttributesRoot> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }

                override fun onNext(pullLocationAttributesRoot: PullLocationAttributesRoot) {
                    if (pullLocationAttributesRoot.attributesDataList.isNotEmpty()) {
                        setLocationSurveyData(pullLocationAttributesRoot.attributesDataList);
                    }
                }

            })

    }

    private fun setLocationSurveyData(attributesDataList: List<PullLocationAttributesData>) {
        for (data in attributesDataList) {
            val distanceData: String = data.attributeValue

            when (data.attributeName) {
                AppConstants.DISTANCE_TO_SUB_CENTRE_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbScDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_PRIMARY_HEALTHCARE_CENTRE_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbPhcDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_NEAREST_COMMUNITY_HEALTHCARE_CENTRE_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbChcDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_NEAREST_DISTRICT_HOSPITAL_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbDhDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_NEAREST_MEDICAL_STORE_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbMsDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_NEAREST_PATHOLOGICAL_LAB_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbPlDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_NEAREST_PRIVATE_CLINIC_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbPcDistance,
                    distanceData
                )

                AppConstants.DISTANCE_TO_NEAREST_PRIVATE_CLINIC_WITH_ALTERNATIVE_MEDICINE_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbPcamDistance,
                    distanceData
                )

                AppConstants.JAL_JEEVAN_YOJANA_UUID_TEXT -> checkChipInsideChipGroup(
                    binding?.cbJjyDistance,
                    distanceData
                )
            }
        }
    }

    private fun checkChipInsideChipGroup(chipGroup: ChipGroup?, distanceText: String) {
        chipGroup?.childCount?.let {
            for (i in 0 until it) {
                val currentChip: Chip = chipGroup.getChildAt(i) as Chip
                if (currentChip.text.toString().equals(distanceText, ignoreCase = true)) {
                    currentChip.isChecked = true
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}