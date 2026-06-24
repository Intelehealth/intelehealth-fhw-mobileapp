package org.intelehealth.app.activities.filterPatientActivity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.MatchGrade
import org.intelehealth.app.models.MatchSource
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.models.dto.PatientSearchDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.app.utilities.getUUID
import org.json.JSONArray
import org.json.JSONObject

class FilterResultActivity : BaseActivity(), FilterResultAdapter.AdapterClickListener {
    private var isFullyLoaded: Boolean = false
    private var isDataLoading: Boolean = false
    private lateinit var loadingDialog: AlertDialog
    private lateinit var filterSuccessLayout: LinearLayout
    private val defaultPageSize: Int = 50
    private var offset = 0
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private lateinit var filterRecyclerView: RecyclerView
    private var patientList = mutableListOf<PatientSearchResult>()
    private var firstName=""
    private var lastName=""
    private var phone=""
    private var dob=""
    private var gender=""
    private val patientAdapter = FilterResultAdapter(patientList, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_filter_result)
        filterRecyclerView = findViewById(R.id.filter_patient_container)
        filterRecyclerView.layoutManager = LinearLayoutManager(this@FilterResultActivity)
        filterRecyclerView.adapter = patientAdapter
        filterSuccessLayout = findViewById(R.id.filter_patient_success_ll)

        // changing status bar color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.WHITE
        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this@FilterResultActivity,
            getString(R.string.loading),
            getString(R.string.please_wait),
        ).apply {
            dismiss()
        }

        findViewById<ImageView>(R.id.iv_back_arrow)?.setOnClickListener {
            finish()
        }
         firstName = intent.getStringExtra("firstname")?: ""
         lastName = intent.getStringExtra("lastName")?: ""
         dob = intent.getStringExtra("dob")?: ""
         phone = intent.getStringExtra("phone")?: ""
         gender = intent.getStringExtra("gender")?: ""
        // Example usage
        Log.e("FilterResult", "Name: $firstName $lastName")
        Log.e("FilterResult", "DOB: $dob")
        Log.e("FilterResult", "Phone: $phone")
        Log.e("FilterResult", "Gender: $gender")
        doFilter(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            gender = gender,
            dob = dob)

    }
    private fun initRecyclerScrollListener(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String
    ) {
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (isFullyLoaded) {
                    return
                }

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (layoutManager.findLastVisibleItemPosition() == patientAdapter.itemCount - 1) {
                    Toast.makeText(
                        this@FilterResultActivity,
                        R.string.loading_more,
                        Toast.LENGTH_SHORT
                    ).show()

                    offset += defaultPageSize
                    loadMorePatientsAndUpdateAdapter(firstName, lastName, phone)
                }
            }
        }

        filterRecyclerView.removeOnScrollListener(scrollListener)
        filterRecyclerView.addOnScrollListener(scrollListener)
    }
    private fun loadMorePatientsAndUpdateAdapter(
        firstName: String,
        lastName: String,

        phone: String,

        ) {
        isDataLoading = true
        val patients = PatientsDAO.getFilteredPatients(
            firstName,
            lastName,

            phone,

            offset,
            defaultPageSize
        )

        if (patients.size < defaultPageSize) {
            isFullyLoaded = true
        }

        patientAdapter.addMorePatients(patients)
        isDataLoading = false
    }
    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }
    private fun doFilter1(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String
    ) {
        val genderText = when (gender?.uppercase()) {
            "M", "MALE" -> "Male"
            "F", "FEMALE" -> "Female"
            else -> "Other"
        }

        offset = 0
        isFullyLoaded = false

        subscriptions.add(

            Single.fromCallable {
                PatientsDAO.getFilteredPatients1(
                    firstName,
                    lastName,
                    gender,
                    phone,
                    dob,
                    offset,
                    defaultPageSize
                )
            }.flatMap { localList ->

                Single.zip(
                    fetchOpenMRSPatients(firstName, lastName, phone, dob,genderText,0),
                    fetchFhirPatients(firstName, lastName, phone,dob,genderText, 0),
                    { remoteList: List<PatientSearchResult>,
                      fhirList: List<PatientSearchResult> ->

                        val finalList = mutableListOf<PatientSearchResult>()

                        finalList.addAll(localList)
                        finalList.addAll(remoteList)
                        finalList.addAll(fhirList)

                        finalList
                    }
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loadingDialog.show() }
                .doFinally { loadingDialog.dismiss() }
                .subscribe(
                    { finalList ->

                        val sortedList = finalList
                            .distinctBy { it.patient?.uuid ?: it.patient?.phonenumber }
                            .sortedByDescending { it.score }
                            .toMutableList()

                        updatePatientsAdapter(sortedList)
                    },
                    { error ->
                        Log.e("FILTER_ERROR", error.message ?: "Unknown Error")
                        ToastUtil.showLongToast(
                            this@FilterResultActivity,
                            "Error finding patients"
                        )
                    }
                )
        )
    }
    private fun doFilter(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String
    ) {
        val genderText = when (gender.uppercase()) {
            "M", "MALE" -> "Male"
            "F", "FEMALE" -> "Female"
            else -> "Other"
        }

        offset = 0
        isFullyLoaded = false

        subscriptions.add(
            Single.fromCallable {
                PatientsDAO.getFilteredPatients1(
                    firstName,
                    lastName,
                    gender,
                    phone,
                    dob,
                    offset,
                    defaultPageSize
                )
            }.flatMap { localList ->

                Single.zip(
                    fetchOpenMRSPatients(firstName, lastName, phone, dob, genderText, 0),
                    fetchFhirPatients(firstName, lastName, phone, dob, genderText, 0),
                    { remoteList: List<PatientSearchResult>,
                      fhirList: List<PatientSearchResult> ->

                        val finalList = mutableListOf<PatientSearchResult>()

                        finalList.addAll(localList)
                        finalList.addAll(remoteList)
                        finalList.addAll(fhirList)

                        finalList
                    }
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loadingDialog.show() }
                .doFinally { loadingDialog.dismiss() }
                .subscribe(
                    { finalList ->

                        // 🔥 MERGE LOGIC START
                        val mergedMap = mutableMapOf<String, PatientSearchResult>()

                        finalList.forEach { item ->
                            val key = item.patient?.uuid
                                ?: item.patient?.phonenumber
                                ?: return@forEach

                            val existing = mergedMap[key]

                            if (existing == null) {
                                mergedMap[key] = item
                            } else {
                                mergedMap[key] = mergePatient(existing, item)
                            }
                        }

                        val sortedList = mergedMap.values
                            .sortedByDescending { it.score }
                            .toMutableList()

                        updatePatientsAdapter(sortedList)
                    },
                    { error ->
                        Log.e("FILTER_ERROR", error.message ?: "Unknown Error")
                        ToastUtil.showLongToast(
                            this@FilterResultActivity,
                            "Error finding patients"
                        )
                    }
                )
        )
    }
    private fun mergePatient(
        old: PatientSearchResult,
        new: PatientSearchResult
    ): PatientSearchResult {

        val oldPatient = old.patient
        val newPatient = new.patient

        oldPatient?.apply {

            firstname = firstname ?: newPatient?.firstname
            lastname = lastname ?: newPatient?.lastname
            middlename = middlename ?: newPatient?.middlename
            gender = gender ?: newPatient?.gender
            dateofbirth = dateofbirth ?: newPatient?.dateofbirth
            phonenumber = phonenumber ?: newPatient?.phonenumber
            openmrsId = openmrsId ?: newPatient?.openmrsId
            mpiId = mpiId ?: newPatient?.mpiId
            sourceId = sourceId ?: newPatient?.sourceId

            address1 = address1 ?: newPatient?.address1
            address2 = address2 ?: newPatient?.address2
            address3 = address3 ?: newPatient?.address3
            address6 = address6 ?: newPatient?.address6
            cityvillage = cityvillage ?: newPatient?.cityvillage
            country = country ?: newPatient?.country
            postalcode = postalcode ?: newPatient?.postalcode
        }

        return old.apply {

            patient = oldPatient

            // best overall score
            score = maxOf(old.score, new.score)

            // IH Network
            if (new.isIHNetwork) {
                isIHNetwork = true
                ihscore = maxOf(old.ihscore, new.ihscore)
            }

            // NR Network
            if (new.isNRNetwork) {
                isNRNetwork = true
                nrscore = maxOf(old.nrscore, new.nrscore)
            }

            // field scores
            firstNameScore = maxOf(old.firstNameScore, new.firstNameScore)
            lastNameScore = maxOf(old.lastNameScore, new.lastNameScore)

            // flags
            phoneMatched = old.phoneMatched || new.phoneMatched
            dobMatched = old.dobMatched || new.dobMatched
        }
    }

    private fun mergePatient1(
        old: PatientSearchResult,
        new: PatientSearchResult
    ): PatientSearchResult {

        return old.apply {

            // best score
            score = maxOf(old.score, new.score)

            // IH merge
            if (new.isIHNetwork) {
                isIHNetwork = true
                ihscore = new.ihscore
            }

            // NR merge
            if (new.isNRNetwork) {
                isNRNetwork = true
                nrscore = new.nrscore
                patient?.sourceId = new.patient?.sourceId
            }

            // extra scores
            firstNameScore = maxOf(old.firstNameScore, new.firstNameScore)
            lastNameScore = maxOf(old.lastNameScore, new.lastNameScore)

            // flags
            phoneMatched = old.phoneMatched || new.phoneMatched
            dobMatched = old.dobMatched || new.dobMatched
        }
    }
    private fun fetchOpenMRSPatients(
        firstName: String,
        lastName: String,
        phone: String,
        dob: String,
        genders: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientOpenMRSObservable(
            firstName,
            lastName,
            phone,
            dob,
            genders,
            pageNo
        ).map { body ->

            val resultList = ArrayList<PatientSearchResult>()

            val entries = body.entry ?: emptyList()

            for (item in entries) {

                val resource = item.resource

                val name = resource?.name?.firstOrNull()
                val id = resource?.id
                val genders = resource?.gender
                val telecom = resource?.telecom?.firstOrNull()
                val phones = telecom?.value
                val dob = resource?.birthDate
                val address = resource?.address?.firstOrNull()

                val given = name?.given?.firstOrNull().orEmpty()
                val family = name?.family.orEmpty()

                val patient = PatientDTO().apply {
                    uuid = id
                    firstname = given
                    lastname = family
                    gender = genders ?: ""
                    phonenumber = phones ?: ""
                    dateofbirth = dob ?: ""
                    address1 = address?.text ?: ""
                }

                val result = PatientSearchResult().apply {
                    this.patient = patient
                    this.source = MatchSource.OPENMRS
                    this.score = item.search?.score ?: 0.0
                    this.grade = MatchGrade.CERTAIN
                    this.localDbResult = false
                    this.isIHNetwork=true
                    this.ihscore=item.search?.score ?: 0.0
                }

                resultList.add(result)
            }

            resultList
        }
    }
    private fun searchPatientOpenMRSObservable(
        firstName: String,
        lastName: String,
        phone: String,
        dob: String,
        gender: String,
        pageNo: Int
    ): Single<PatientSearchDTO> {

        Log.e("TEST", "FUNCTION ENTERED");
        val offset = pageNo * defaultPageSize

        val jsonObject = JSONObject()

        val parameterArray = JSONArray()

        // resource parameter
        val resourceObject = JSONObject()
        resourceObject.put("resourceType", "Patient")

        // name
        val nameArray = JSONArray()
        val nameObject = JSONObject()

        if (!lastName.isNullOrEmpty()) {
            nameObject.put("family", lastName)
        }

        val givenArray = JSONArray()
        givenArray.put(firstName)

        nameObject.put("given", givenArray)
        nameArray.put(nameObject)

        resourceObject.put("name", nameArray)

         // gender
         if (gender.isNotEmpty()) {
             resourceObject.put("gender", gender.lowercase())
         }

         // dob
         if (dob.isNotEmpty()) {
             resourceObject.put("birthDate", dob)
         }

        // telecom
        val telecomArray = JSONArray()
        val telecomObject = JSONObject()
        telecomObject.put("system", "phone")
        telecomObject.put("value", phone)

        telecomArray.put(telecomObject)
        resourceObject.put("telecom", telecomArray)

        val resourceParam = JSONObject()
        resourceParam.put("name", "resource")
        resourceParam.put("resource", resourceObject)

        parameterArray.put(resourceParam)

        // resourceType
        val resourceTypeParam = JSONObject()
        resourceTypeParam.put("name", "resourceType")
        resourceTypeParam.put("valueString", "Patient")

        parameterArray.put(resourceTypeParam)

        // count
        val countParam = JSONObject()
        countParam.put("name", "count")
        countParam.put("valueInteger", defaultPageSize)

        parameterArray.put(countParam)

        // offset
        val offsetParam = JSONObject()
        offsetParam.put("name", "offset")
        offsetParam.put("valueInteger", offset)

        parameterArray.put(offsetParam)

        // onlyCertainMatches
        val matchParam = JSONObject()
        matchParam.put("name", "onlyCertainMatches")
        matchParam.put("valueBoolean", false)

        parameterArray.put(matchParam)

        jsonObject.put("resourceType", "Parameters")
        jsonObject.put("parameter", parameterArray)

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val auth = Credentials.basic("admin", "apple@1Mango")
        val url = (BuildConfig.SERVER_URL + "openmrs/ws/rest/v1/ihmodule/patient/\$match")
        return AppConstants.apiInterface.searchPatientOpenMRS(
            url,
            auth,
            requestBody
        )
    }

    private fun fetchFhirPatients(
        firstName: String,
        lastName: String,
        phone: String,
        dob: String,
        genders: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientFhirObservable(firstName, lastName, phone, dob,genders,pageNo)
            .map { body ->
                val resultList = ArrayList<PatientSearchResult>()

                val entries = body.entry ?: emptyList()

                for (item in entries) {

                    val resource = item.resource ?: continue
                    //  Skip GOLDEN_RECORD patients
                    val isGoldenRecord = resource.meta?.tag?.any { tag ->
                        tag.code == "${BundleKeys.GOLDEN_RECORD}"
                    } == true

                    if (isGoldenRecord) {
                        continue
                    }

                    val name = resource.name?.firstOrNull()
                    val given = name?.given?.firstOrNull().orEmpty()
                    val family = name?.family.orEmpty()

                    val patient = PatientDTO().apply {
                        uuid = resource.identifier
                            ?.firstOrNull { identifier ->
                                !identifier.system.isNullOrEmpty() &&
                                        !identifier.value.isNullOrEmpty() &&
                                        identifier.id == null &&
                                        identifier.use == null
                            }
                            ?.value
                       // uuid =  getUUID(resource.identifier, systemUrl)
                        sourceId=resource.id
                        firstname = given
                        lastname = family
                        gender = resource.gender ?: ""
                        phonenumber = resource.telecom?.firstOrNull()?.value ?: ""
                        dateofbirth = resource.birthDate ?: ""
                        address1 = resource.address?.firstOrNull()?.text ?: ""
                    }

                    resultList.add(
                        PatientSearchResult().apply {
                            this.patient = patient
                            this.source = MatchSource.FHIR
                            this.score = item.search?.score ?: 0.0
                            this.grade = MatchGrade.CERTAIN
                            this.localDbResult = false
                            this.isNRNetwork=true
                            this.nrscore=item.search?.score ?: 0.0
                        }
                    )
                }

                resultList.toList()   //  IMPORTANT FIX
            }
            .subscribeOn(Schedulers.io())
    }


    private fun searchPatientFhirObservable(
        firstName: String,
        lastName: String,
        phone: String,
        dob: String,
        gender: String,
        pageNo: Int
    ): Single<PatientSearchDTO> {

        Log.e("TEST", "FUNCTION ENTERED");
        val offset = pageNo * defaultPageSize

        val jsonObject = JSONObject()

        val parameterArray = JSONArray()

        // resource parameter
        val resourceObject = JSONObject()
        resourceObject.put("resourceType", "Patient")

        // name
        val nameArray = JSONArray()
        val nameObject = JSONObject()

        if (!lastName.isNullOrEmpty()) {
            nameObject.put("family", lastName)
        }

        val givenArray = JSONArray()
        givenArray.put(firstName)
        if (!firstName.isNullOrEmpty()) {

            nameObject.put("given", givenArray)}
        nameArray.put(nameObject)

        resourceObject.put("name", nameArray)
        // gender
        if (gender.isNotEmpty()) {
            resourceObject.put("gender", gender.lowercase())
        }

        // dob
        if (dob.isNotEmpty()) {
            resourceObject.put("birthDate", dob)
        }

        // telecom
        val telecomArray = JSONArray()
        val telecomObject = JSONObject()
        if (!phone.isNullOrEmpty()) {
            telecomObject.put("system", "phone")
            telecomObject.put("value", phone)

            telecomArray.put(telecomObject)
            resourceObject.put("telecom", telecomArray)}

        val resourceParam = JSONObject()
        resourceParam.put("name", "resource")
        resourceParam.put("resource", resourceObject)

        parameterArray.put(resourceParam)

        // resourceType
        val resourceTypeParam = JSONObject()
        resourceTypeParam.put("name", "resourceType")
        resourceTypeParam.put("valueString", "Patient")

        parameterArray.put(resourceTypeParam)

        // count
        val countParam = JSONObject()
        countParam.put("name", "count")
        countParam.put("valueInteger", defaultPageSize)

        parameterArray.put(countParam)

        // offset
        val offsetParam = JSONObject()
        offsetParam.put("name", "offset")
        offsetParam.put("valueInteger", offset)

        parameterArray.put(offsetParam)

        // onlyCertainMatches
        val matchParam = JSONObject()
        matchParam.put("name", "onlyCertainMatches")
        matchParam.put("valueBoolean", false)

        parameterArray.put(matchParam)

        jsonObject.put("resourceType", "Parameters")
        jsonObject.put("parameter", parameterArray)
        // %24 = $ encoding
        val urlfhir="http://192.168.19.152:6001/fhir/\$mdm-match"


        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val auth = Credentials.basic("fhir_app", "Admin123")
        return AppConstants.apiInterface.searchPatientFhir(
            urlfhir,
            auth,
            requestBody
        )
    }
    private fun updatePatientsAdapter(patients: MutableList<PatientSearchResult>) {
        if (patients.isNotEmpty()) {
            //selectedPatient = null
            //goWithSelectedButton.isEnabled = false

            patientAdapter.updatePatientList(patients)
            filterSuccessLayout.visibility = View.VISIBLE

        } else {
            filterSuccessLayout.visibility = View.GONE

        }
    }

    override fun onItemClick(selectedItem: Any) {
        TODO("Not yet implemented")

    }
}
