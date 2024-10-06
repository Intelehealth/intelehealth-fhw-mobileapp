package org.intelehealth.app.activities.location_survey

import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.R
import org.intelehealth.app.activities.setupActivity.LocationArrayAdapter
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.NewLocationDao
import org.intelehealth.app.databinding.ActivityLocationSurveyBinding
import org.intelehealth.app.models.Location
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
    private var selectedSanch: kotlin.String? = ""
    private var selectedPrimaryVillage: String? = ""
    private var selectedSecondaryVillage: String? = ""

    private var villageNameHashMap: HashMap<String?, String?> = HashMap()
    private var sessionManager: SessionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSurveyBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        sessionManager = SessionManager(this)

        fetchIntentData()
        setListeners()
        initializeButtons()
        fetchLocations()
    }

    private fun setListeners() {
        binding?.autotvSelectState?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        selectedState = parent?.getItemAtPosition(position)?.toString()
                        sessionManager?.stateName = selectedState

                        val districtLocationList: MutableList<String>? =
                            newLocationDao?.getDistrictList(
                                selectedState,
                                this@LocationSurveyActivity
                            )

                        if (districtLocationList != null && districtLocationList.size > 1) {
                            val adapter = LocationArrayAdapter(
                                this@LocationSurveyActivity,
                                districtLocationList
                            )

                            binding?.autotvSelectDistrict?.setEnabled(true)
                            binding?.autotvSelectDistrict?.setAlpha(1.0f)
                            binding?.autotvSelectDistrict?.setAdapter(adapter)
                            isLocationFetched = true
                        } else {
                            emptySpinner("state");
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding?.autotvSelectDistrict?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        selectedDistrict = parent?.getItemAtPosition(position)?.toString()
                        sessionManager?.districtName = selectedDistrict

                        val sanchLocationList: MutableList<String>? =
                            newLocationDao?.getSanchList(
                                selectedState,
                                selectedDistrict,
                                this@LocationSurveyActivity
                            )

                        if (sanchLocationList != null && sanchLocationList.size > 1) {
                            val adapter = LocationArrayAdapter(
                                this@LocationSurveyActivity,
                                sanchLocationList
                            )

                            binding?.autotvSelectSanch?.setEnabled(true)
                            binding?.autotvSelectSanch?.setAlpha(1.0f)
                            binding?.autotvSelectSanch?.setAdapter(adapter)
                            isLocationFetched = true
                        } else {
                            emptySpinner("district");
                        }
                    } else {
                        emptySpinner("district");
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding?.autotvSelectSanch?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        selectedSanch = parent?.getItemAtPosition(position)?.toString()
                        sessionManager?.sanchName = selectedSanch

                        val primaryVillageLocationsList: MutableList<String>? =
                            newLocationDao?.getVillageList(
                                selectedState,
                                selectedDistrict,
                                selectedSanch,
                                this@LocationSurveyActivity,
                                "primary"
                            )

                        if (primaryVillageLocationsList != null && primaryVillageLocationsList.size > 1) {
                            val adapter = LocationArrayAdapter(
                                this@LocationSurveyActivity,
                                primaryVillageLocationsList
                            )

                            binding?.autotvSelectPrimaryVillage?.setEnabled(true)
                            binding?.autotvSelectPrimaryVillage?.setAlpha(1.0f)
                            binding?.autotvSelectPrimaryVillage?.setAdapter(adapter)
                            isLocationFetched = true
                        } else {
                            emptySpinner("sanch");
                        }
                    } else {
                        emptySpinner("sanch");
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding?.autotvSelectPrimaryVillage?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        selectedPrimaryVillage = parent?.getItemAtPosition(position)?.toString()

                        val primaryVillageUuid: String? = newLocationDao?.getVillageUuid(
                            selectedState,
                            selectedDistrict,
                            selectedSanch,
                            selectedPrimaryVillage
                        )

                        sessionManager?.villageName = selectedPrimaryVillage
                        sessionManager?.currentLocationName = selectedPrimaryVillage
                        sessionManager?.currentLocationUuid = primaryVillageUuid

                        val tempPrimaryVillageHashMap: HashMap<String?, String?> = HashMap()
                        tempPrimaryVillageHashMap[primaryVillageUuid] = selectedPrimaryVillage
                        villageNameHashMap = tempPrimaryVillageHashMap

                        val secondaryVillageLocationsList: MutableList<String>? =
                            newLocationDao?.getVillageList(
                                selectedState,
                                selectedDistrict,
                                selectedSanch,
                                this@LocationSurveyActivity,
                                "secondary"
                            )

                        secondaryVillageLocationsList?.removeAt(
                            secondaryVillageLocationsList.indexOf(
                                selectedPrimaryVillage
                            )
                        )

                        if (secondaryVillageLocationsList != null && secondaryVillageLocationsList.size > 1) {
                            val adapter = LocationArrayAdapter(
                                this@LocationSurveyActivity,
                                secondaryVillageLocationsList
                            )

                            binding?.autotvSelectSecondaryVillage?.setEnabled(true)
                            binding?.autotvSelectSecondaryVillage?.setAlpha(1.0f)
                            binding?.autotvSelectSecondaryVillage?.setAdapter(adapter)
                            isLocationFetched = true
                        } else {
                            emptySpinner("village");
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding?.autotvSelectSecondaryVillage?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
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
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun fetchIntentData() {
        url = intent.getStringExtra(AppConstants.INTENT_SERVER_URL)
    }

    private fun fetchLocations() {
        isLocationFetched = false;
        val baseUrl = "http://$url/openmrs/ws/rest/v1/";

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
                                            } else {
                                                emptySpinner("state");
                                            }
                                        }
                                } catch (exception: DAOException) {
                                    exception.printStackTrace();
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
                                ).show();
                            } else {
                                Toast.makeText(
                                    this@LocationSurveyActivity,
                                    e.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        override fun onComplete() {
                            isLocationFetched = true
                        }
                    })
            } catch (exception: IllegalArgumentException) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
        } else
            Toast.makeText(
                this@LocationSurveyActivity,
                getString(R.string.url_invalid),
                Toast.LENGTH_SHORT
            ).show();
    }

    private fun emptySpinner(value: String) {
        unselectExistingRadioButtons()

        when (value) {

            "state" -> {
                val listDistrict: MutableList<String> = ArrayList()
                listDistrict.add(resources.getString(R.string.setup_select_district_str))
                binding?.autotvSelectDistrict?.setEnabled(false)
                binding?.autotvSelectDistrict?.setAlpha(0.4f)
                val districtAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, listDistrict)
                binding?.autotvSelectDistrict?.setAdapter(districtAdapter)

                val sanchList: MutableList<String> = ArrayList()
                sanchList.add(resources.getString(R.string.setup_select_sanch_str))
                binding?.autotvSelectSanch?.setEnabled(false)
                binding?.autotvSelectSanch?.setAlpha(0.4f)
                val sanchAdapter = LocationArrayAdapter(this@LocationSurveyActivity, sanchList)
                binding?.autotvSelectSanch?.setAdapter(sanchAdapter)

                val primaryVillageList: MutableList<String> = ArrayList()
                primaryVillageList.add(resources.getString(R.string.setup_select_primary_village_str))
                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                val villageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, primaryVillageList)
                binding?.autotvSelectPrimaryVillage?.setAdapter(villageAdapter)

                val secondaryVillageList: MutableList<String> = ArrayList()
                secondaryVillageList.add(resources.getString(R.string.setup_select_secondary_village_str))
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                val secondaryVillageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, secondaryVillageList)
                binding?.autotvSelectSecondaryVillage?.setAdapter(secondaryVillageAdapter)

            }

            "district" -> {
                val sanchList: MutableList<String> = ArrayList()
                sanchList.add(resources.getString(R.string.setup_select_sanch_str))
                binding?.autotvSelectSanch?.setEnabled(false)
                binding?.autotvSelectSanch?.setAlpha(0.4f)
                val sanchAdapter = LocationArrayAdapter(this@LocationSurveyActivity, sanchList)
                binding?.autotvSelectSanch?.setAdapter(sanchAdapter)

                val primaryVillageList: MutableList<String> = ArrayList()
                primaryVillageList.add(resources.getString(R.string.setup_select_primary_village_str))
                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                val villageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, primaryVillageList)
                binding?.autotvSelectPrimaryVillage?.setAdapter(villageAdapter)

                val secondaryVillageList: MutableList<String> = ArrayList()
                secondaryVillageList.add(resources.getString(R.string.setup_select_secondary_village_str))
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                val secondaryVillageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, secondaryVillageList)
                binding?.autotvSelectSecondaryVillage?.setAdapter(secondaryVillageAdapter)
            }

            "sanch" -> {
                val primaryVillageList: MutableList<String> = ArrayList()
                primaryVillageList.add(resources.getString(R.string.setup_select_primary_village_str))
                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                val villageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, primaryVillageList)
                binding?.autotvSelectPrimaryVillage?.setAdapter(villageAdapter)

                val secondaryVillageList: MutableList<String> = ArrayList()
                secondaryVillageList.add(resources.getString(R.string.setup_select_secondary_village_str))
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                val secondaryVillageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, secondaryVillageList)
                binding?.autotvSelectSecondaryVillage?.setAdapter(secondaryVillageAdapter)
            }

            "village" -> {
                val secondaryVillageList: MutableList<String> = ArrayList()
                secondaryVillageList.add(resources.getString(R.string.setup_select_secondary_village_str))
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                val secondaryVillageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, secondaryVillageList)
                binding?.autotvSelectSecondaryVillage?.setAdapter(secondaryVillageAdapter)
            }

            else -> {
                val stateList: MutableList<String> = ArrayList()
                stateList.add(resources.getString(R.string.setup_select_state_str))
                binding?.autotvSelectState?.setEnabled(false)
                binding?.autotvSelectState?.setAlpha(0.4f)
                val stateAdapter = LocationArrayAdapter(this@LocationSurveyActivity, stateList)
                binding?.autotvSelectState?.setAdapter(stateAdapter)

                val listDistrict: MutableList<String> = ArrayList()
                listDistrict.add(resources.getString(R.string.setup_select_district_str))
                binding?.autotvSelectDistrict?.setEnabled(false)
                binding?.autotvSelectDistrict?.setAlpha(0.4f)
                val districtAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, listDistrict)
                binding?.autotvSelectDistrict?.setAdapter(districtAdapter)

                val sanchList: MutableList<String> = ArrayList()
                sanchList.add(resources.getString(R.string.setup_select_sanch_str))
                binding?.autotvSelectSanch?.setEnabled(false)
                binding?.autotvSelectSanch?.setAlpha(0.4f)
                val sanchAdapter = LocationArrayAdapter(this@LocationSurveyActivity, sanchList)
                binding?.autotvSelectSanch?.setAdapter(sanchAdapter)

                val primaryVillageList: MutableList<String> = ArrayList()
                primaryVillageList.add(resources.getString(R.string.setup_select_primary_village_str))
                binding?.autotvSelectPrimaryVillage?.setEnabled(false)
                binding?.autotvSelectPrimaryVillage?.setAlpha(0.4f)
                val villageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, primaryVillageList)
                binding?.autotvSelectPrimaryVillage?.setAdapter(villageAdapter)

                val secondaryVillageList: MutableList<String> = ArrayList()
                secondaryVillageList.add(resources.getString(R.string.setup_select_secondary_village_str))
                binding?.autotvSelectSecondaryVillage?.setEnabled(false)
                binding?.autotvSelectSecondaryVillage?.setAlpha(0.4f)
                val secondaryVillageAdapter =
                    LocationArrayAdapter(this@LocationSurveyActivity, secondaryVillageList)
                binding?.autotvSelectSecondaryVillage?.setAdapter(secondaryVillageAdapter)
            }
        }
    }

    private fun unselectExistingRadioButtons() {
        TODO("Not yet implemented")
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}