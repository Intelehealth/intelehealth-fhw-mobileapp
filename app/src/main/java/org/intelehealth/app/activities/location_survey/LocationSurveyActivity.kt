package org.intelehealth.app.activities.location_survey

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import org.intelehealth.app.utilities.LocationValidationUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.app.utilities.extensions.checkChipBySelectedText
import org.intelehealth.app.utilities.extensions.getSelectedChipTextInEnglishLocale


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
        initializeChipTags()
        initializeButtons()
        initializeAutoTextViewDropDowns()
        fetchLocations()
    }

    private fun initializeChipTags() {
        binding?.chipScWithinFiveMins?.tag = R.string.within_5_minutes
        binding?.chipScFiveFifteenMins?.tag = R.string.five_fifteen_minutes
        binding?.chipScFifteenThirtyMins?.tag = R.string.fifteen_thirty_minutes
        binding?.chipScMoreThanThirtyMins?.tag = R.string.more_than_thirty_minutes

        binding?.chipPhcWithinOneKm?.tag = R.string.within_1_km
        binding?.chipPhcOneThreeKm?.tag = R.string.one_three_km
        binding?.chipPhcThreeFiveKm?.tag = R.string.three_five_km
        binding?.chipScMoreThanFiveKm?.tag = R.string.more_than_five_km

        binding?.chipChcWithinThreeKm?.tag = R.string.within_three_km
        binding?.chipChcThreeSixKm?.tag = R.string.three_six_km
        binding?.chipChcSixTenKm?.tag = R.string.six_ten_km
        binding?.chipChcMoreThanTenKm?.tag = R.string.more_than_ten_km

        binding?.chipChcWithinThreeKm?.tag = R.string.within_three_km
        binding?.chipChcThreeSixKm?.tag = R.string.three_six_km
        binding?.chipChcSixTenKm?.tag = R.string.six_ten_km
        binding?.chipChcMoreThanTenKm?.tag = R.string.more_than_ten_km

        binding?.chipDhWithinFiveKm?.tag = R.string.within_five_km
        binding?.chipDhFiveTenKm?.tag = R.string.five_ten_km
        binding?.chipDhTenTwentyKm?.tag = R.string.ten_twenty_km
        binding?.chipDhTwentyThirtyKm?.tag = R.string.twenty_thirty_km
        binding?.chipDhMoreThanThirtyKm?.tag = R.string.more_than_thirty_km

        binding?.chipMsWithinThreeKm?.tag = R.string.within_three_km
        binding?.chipMsThreeSixKm?.tag = R.string.three_six_km
        binding?.chipMsSixTenKm?.tag = R.string.six_ten_km
        binding?.chipMsMoreThanTenKm?.tag = R.string.more_than_ten_km

        binding?.chipPlWithinThreeKm?.tag = R.string.within_three_km
        binding?.chipPlThreeSixKm?.tag = R.string.three_six_km
        binding?.chipPlSixTenKm?.tag = R.string.six_ten_km
        binding?.chipPlMoreThanTenKm?.tag = R.string.more_than_ten_km

        binding?.chipPcWithinThreeKm?.tag = R.string.within_three_km
        binding?.chipPcThreeSixKm?.tag = R.string.three_six_km
        binding?.chipPcSixTenKm?.tag = R.string.six_ten_km
        binding?.chipPcMoreThanTenKm?.tag = R.string.more_than_ten_km

        binding?.chipPcamWithinThreeKm?.tag = R.string.within_three_km
        binding?.chipPcamThreeSixKm?.tag = R.string.three_six_km
        binding?.chipPcamSixTenKm?.tag = R.string.six_ten_km
        binding?.chipPcamMoreThanTenKm?.tag = R.string.more_than_ten_km

        binding?.chipJjyYes?.tag = R.string.generic_yes
        binding?.chipJjyNo?.tag = R.string.generic_no
    }

    private fun setLocationDataIfPresent() {
        if (sessionManager?.stateName?.isBlank() == false) {
            binding?.autotvSelectState?.setText(sessionManager?.stateName, false)
            binding?.autotvSelectState?.isEnabled = true
            binding?.autotvSelectDistrict?.setAdapter(getDistrictArrayAdapter())
            binding?.autotvSelectDistrict?.isEnabled = true
        }

        if (sessionManager?.districtName?.isBlank() == false) {
            binding?.autotvSelectDistrict?.setText(sessionManager?.districtName, false)
            binding?.autotvSelectDistrict?.isEnabled = true
            binding?.autotvSelectSanch?.setAdapter(getSanchArrayAdapter())
            binding?.autotvSelectSanch?.isEnabled = true
        }

        if (sessionManager?.sanchName?.isBlank() == false) {
            binding?.autotvSelectSanch?.setText(sessionManager?.sanchName, false)
            binding?.autotvSelectSanch?.isEnabled = true
            binding?.autotvSelectPrimaryVillage?.setAdapter(getPrimaryVillageArrayAdapter())
            binding?.autotvSelectPrimaryVillage?.isEnabled = true
        }

        if (sessionManager?.currentLocationName?.isBlank() == false) {
            binding?.autotvSelectPrimaryVillage?.setText(sessionManager?.currentLocationName, false)
            binding?.autotvSelectPrimaryVillage?.isEnabled = true
            fetchAndSetLocationAttributes(sessionManager?.currentLocationUuid)
            binding?.autotvSelectSecondaryVillage?.setAdapter(getSecondaryVillageArrayAdapter())
            binding?.autotvSelectSecondaryVillage?.isEnabled = true
        }

        if (sessionManager?.secondaryLocationName?.isBlank() == false) {
            binding?.autotvSelectSecondaryVillage?.setText(
                sessionManager?.secondaryLocationName,
                false
            )
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
                    sessionManager?.stateName = ""
                    sessionManager?.districtName = ""
                    sessionManager?.sanchName = ""
                    sessionManager?.currentLocationName = ""
                    sessionManager?.secondaryLocationName = ""

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
                    emptySpinner("state")
                }
                unselectExistingRadioButtons()
            }

        binding?.autotvSelectDistrict?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (position != 0) {
                    sessionManager?.sanchName = ""
                    sessionManager?.currentLocationName = ""
                    sessionManager?.secondaryLocationName = ""

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
                unselectExistingRadioButtons()
            }

        binding?.autotvSelectSanch?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                if (position != 0) {
                    sessionManager?.currentLocationName = ""
                    sessionManager?.secondaryLocationName = ""

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
                    sessionManager?.secondaryLocationName = ""
                    selectedPrimaryVillage = parent?.getItemAtPosition(position)?.toString()
                    sessionManager?.villageName = selectedPrimaryVillage
                    sessionManager?.currentLocationName = selectedPrimaryVillage

                    val primaryVillageUuid: String? = newLocationDao?.getVillageUuid(
                        sessionManager?.stateName,
                        sessionManager?.districtName,
                        sessionManager?.sanchName,
                        sessionManager?.villageName
                    )

                    sessionManager?.setCurrentLocationUuid(primaryVillageUuid)

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
                        selectedSecondaryVillage
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

                sessionManager?.districtName = ""
                sessionManager?.sanchName = ""
                sessionManager?.currentLocationName = ""
                sessionManager?.secondaryLocationName = ""
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

                sessionManager?.sanchName = ""
                sessionManager?.currentLocationName = ""
                sessionManager?.secondaryLocationName = ""
            }

            "sanch" -> {
                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectPrimaryVillage?.setText(resources.getString(R.string.select_primary_village))

                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))

                sessionManager?.currentLocationName = ""
                sessionManager?.secondaryLocationName = ""
            }

            "village" -> {
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                binding?.autotvSelectSecondaryVillage?.setText(resources.getString(R.string.select_secondary_village))

                sessionManager?.secondaryLocationName = ""
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

                sessionManager?.stateName = ""
                sessionManager?.districtName = ""
                sessionManager?.sanchName = ""
                sessionManager?.currentLocationName = ""
                sessionManager?.secondaryLocationName = ""
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
            storeSurveyData()

            sessionManager?.let {
                if (LocationValidationUtils.areLocationFieldsValid(it)) {
                    val intent = Intent()
                    intent.putExtra(
                        AppConstants.INTENT_PRIMARY_VILLAGE,
                        sessionManager?.currentLocationName
                    )
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@LocationSurveyActivity,
                        getString(R.string.please_select_all_the_required_fields),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun storeSurveyDataAndGoBack() {
        storeSurveyData()
        finish()
    }

    private fun storeSurveyData() {
        sessionManager?.subCentreDistance =
            binding?.cbScDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.primaryHealthCentreDistance =
            binding?.cbPhcDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.communityHealthCentreDistance =
            binding?.cbChcDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.districtHospitalDistance =
            binding?.cbDhDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.medicalStoreDistance =
            binding?.cbMsDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.pathologicalLabDistance =
            binding?.cbPlDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.privateClinicWithMbbsDoctorDistance =
            binding?.cbPcDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.privateClinicWithAlternateDoctorDistance =
            binding?.cbPcamDistance?.getSelectedChipTextInEnglishLocale(this)

        sessionManager?.jalJeevanYojanaScheme =
            binding?.cbJjyDistance?.getSelectedChipTextInEnglishLocale(this)
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

                AppConstants.DISTANCE_TO_SUB_CENTRE_UUID_TEXT -> binding?.cbScDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_PRIMARY_HEALTHCARE_CENTRE_UUID_TEXT -> binding?.cbPhcDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_NEAREST_COMMUNITY_HEALTHCARE_CENTRE_UUID_TEXT -> binding?.cbChcDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_NEAREST_DISTRICT_HOSPITAL_UUID_TEXT -> binding?.cbDhDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_NEAREST_MEDICAL_STORE_UUID_TEXT -> binding?.cbMsDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_NEAREST_PATHOLOGICAL_LAB_UUID_TEXT -> binding?.cbPlDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_NEAREST_PRIVATE_CLINIC_UUID_TEXT -> binding?.cbPcDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.DISTANCE_TO_NEAREST_PRIVATE_CLINIC_WITH_ALTERNATIVE_MEDICINE_UUID_TEXT -> binding?.cbPcamDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )

                AppConstants.JAL_JEEVAN_YOJANA_UUID_TEXT -> binding?.cbJjyDistance?.checkChipBySelectedText(
                    distanceData,
                    this
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}