package org.intelehealth.app.ui.patient.activity.filter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.MatchGrade
import org.intelehealth.app.models.MatchSource
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.Extension
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.models.dto.PatientSearchDTO
import org.intelehealth.app.ui2.utils.CheckInternetAvailability
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.ToastUtil
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class PatientSearchingActivity : AppCompatActivity() {

    private lateinit var progressBarIH: ProgressBar
    private lateinit var progressBarNational: ProgressBar
    private lateinit var nameTv: TextView
    private lateinit var infoTv: TextView
    private lateinit var ihNetworkTv: TextView
    private lateinit var totalNetworkTv: TextView
    private lateinit var nrNetworkTv: TextView

    private var isIHApiDone = false
    private var isNationalApiDone = false
    private var isLocalDone = false
    private var isFhirDone = false
    private var patientDTO: PatientDTO? = null
    private val defaultPageSize: Int = 50
    private lateinit var loadingDialog: AlertDialog
    private var filteredPatientList = mutableListOf<PatientSearchResult>()
    private var filteredPatientFhirList = mutableListOf<PatientSearchResult>()
    private var filteredPatientFinalList = mutableListOf<PatientSearchResult>()
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_searching)
       /* loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait),
        ).apply {
            dismiss()
        }*/

        progressBarIH = findViewById(R.id.progressBarIH)
        progressBarNational = findViewById(R.id.progressBarNational)
        nameTv = findViewById(R.id.nameTv)
        infoTv = findViewById(R.id.infoTv)
        ihNetworkTv = findViewById(R.id.ihNetworkTv)
        totalNetworkTv = findViewById(R.id.totalTv)
        nrNetworkTv = findViewById(R.id.nrNetworkTv)
        patientDTO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "patientDTO",
                PatientDTO::class.java
            )
        } else {
            intent.getSerializableExtra("patientDTO") as? PatientDTO
        }

        val name ="${patientDTO?.firstname} ${patientDTO?.middlename} ${patientDTO?.lastname}"
        val gender = patientDTO?.gender
        val dob =patientDTO?.dateofbirth;
        val  phone=patientDTO?.phonenumber
        nameTv.text=name
        infoTv.text = "$gender . DOB $dob. $phone"

        findViewById<ImageView>(R.id.iv_back_arrow)?.setOnClickListener {
            finish()
        }
        callIHNetworkApi()
        callNationalRegistryApi()

    }
    private fun callIHNetworkApi() {

        progressBarIH.visibility = View.VISIBLE
        isIHApiDone = false

        doFilterLocal(
            patientDTO?.firstname ?: "",
            patientDTO?.lastname ?: "",
            patientDTO?.gender ?: "",
            patientDTO?.phonenumber ?: "",
            patientDTO?.dateofbirth ?: ""
        )
    }
    private fun callIHNetworkApis() {

        progressBarIH.visibility = View.VISIBLE

        // Example API Delay
        Handler(Looper.getMainLooper()).postDelayed({

            progressBarIH.visibility = View.GONE

            isIHApiDone = true
            doFilterLocal(
                patientDTO?.firstname?:"",
                patientDTO?.lastname?:"",
                patientDTO?.gender?:"",
                patientDTO?.phonenumber?:"",
                patientDTO?.dateofbirth?:""
            )

           // checkAllApisCompleted()

        }, 3000)
    }
    private fun getFullGenderStr(gender: String): String {
        return when (gender.lowercase(Locale.getDefault())) {
            "f" -> "Female"
            "m" -> "Male"
            else -> "Other"
        }
    }
    private fun doFilterFhir(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {

        subscriptions.add(
            fetchFhirPatients(firstName, lastName, phone, gender, dob, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->

                    progressBarNational.visibility = View.GONE  // ✅ STOP

                    filteredPatientFhirList.clear()
                    filteredPatientFhirList.addAll(list)

                    isFhirDone = true

                    nrNetworkTv.text =
                        if (list.isEmpty()) "No match found"
                        else "Matches found"

                    checkAndMerge()

                }, {

                    progressBarNational.visibility = View.GONE

                    isFhirDone = true
                    nrNetworkTv.text = "No match found"

                    checkAndMerge()
                })
        )
    }
    private fun doFilterFhirs(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {

        nrNetworkTv.visibility = View.VISIBLE

        subscriptions.add(

            Single.fromCallable {

                val isNetworkAvailable =
                    CheckInternetAvailability.isNetworkAvailable(this)

                // NO INTERNET
                if (!isNetworkAvailable) {

                    return@fromCallable emptyList<PatientSearchResult>()
                }

                // FHIR API CALL
                fetchFhirPatients(
                    firstName,
                    lastName,
                    phone,
                    getFullGenderStr(gender),
                    dob,
                    0
                )

                    // API FAIL → EMPTY LIST
                    .onErrorReturn {

                        Log.e(
                            "FHIR_API",
                            "FHIR API failed",
                            it
                        )

                        emptyList()
                    }

                    .blockingGet()
            }

                .subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe({ patientList ->

                    filteredPatientFhirList.clear()
                    filteredPatientFhirList.addAll(patientList)

                    isFhirDone = true

                    nrNetworkTv.text =
                        if (filteredPatientFhirList.isEmpty()) {
                            "No match found"
                        } else {
                            "Matches found"
                        }

                    checkAndMerge()

                }, {

                    filteredPatientFhirList.clear()

                    isFhirDone = true

                    nrNetworkTv.text = "No match found"

                    checkAndMerge()

                    ToastUtil.showLongToast(
                        this@PatientSearchingActivity,
                        "Error finding patients"
                    )
                })
        )
    }

    private fun checkAndMerge() {

        if (isLocalDone && isFhirDone) {

            mergeFinalPatientList()

            loadingDialog.dismiss()
        }
    }
    private fun mergeFinalPatientList() {

        val map = LinkedHashMap<String, PatientSearchResult>()

        var nullIdCounter = 0

        fun generateKey(item: PatientSearchResult): String {
            return item.patient?.uuid ?: "NULL_${nullIdCounter++}"
        }

        // =========================
        // LOCAL FIRST
        // =========================
        filteredPatientList.forEach { local ->

            val key = local.patient?.uuid ?: generateKey(local)

            map[key] = local
        }

        // =========================
        // FHIR MERGE (REMOTE PRIORITY)
        // =========================
        filteredPatientFhirList.forEach { fhir ->

            val key = fhir.patient?.uuid ?: generateKey(fhir)

            val local = map[key]

            map[key] = if (local != null) {

                // SAME PATIENT → MERGE
                fhir.copy(
                    patient = fhir.patient, // REMOTE PRIORITY DATA

                    // LOCAL DATA KEEP
                    isIHNetwork = local.isIHNetwork,
                    ihscore = local.ihscore,

                    // FHIR DATA KEEP
                    isNRNetwork = fhir.isNRNetwork,
                    nrscore = fhir.nrscore
                )

            } else {
                // NEW PATIENT
                fhir
            }
        }

        // =========================
        // FINAL LIST
        // =========================
        filteredPatientFinalList.clear()

        filteredPatientFinalList.addAll(map.values)
        val sortedList = filteredPatientFinalList
            .distinctBy { it.patient?.uuid ?: it.patient?.phonenumber }
            .sortedByDescending { it.score }
            .toMutableList()
        filteredPatientFinalList.addAll(sortedList)

        totalNetworkTv.visibility = View.VISIBLE
        totalNetworkTv.text = "${filteredPatientFinalList.size} matches found"

        checkAllApisCompleted()
    }
    private fun mergeFinalPatientLists1() {

        val map = LinkedHashMap<String, PatientSearchResult>()

        filteredPatientList.forEach {
            it.patient?.uuid?.let { id -> map[id] = it }
        }

        filteredPatientFhirList.forEach { fhir ->
            val id = fhir.patient?.uuid ?: return@forEach

            val local = map[id]

            map[id] = if (local != null) {
                fhir.copy(
                    phoneMatched = local.phoneMatched,
                    isIHNetwork = local.isIHNetwork,
                    ihscore = local.ihscore,
                    isNRNetwork = fhir.isNRNetwork,
                    nrscore = fhir.nrscore
                )
            } else {
                fhir
            }
        }

        filteredPatientFinalList.clear()
        filteredPatientFinalList.addAll(map.values)

        totalNetworkTv.visibility = View.VISIBLE
        totalNetworkTv.text = "${filteredPatientFinalList.size} matches found"

        checkAllApisCompleted()
    }
    private fun mergeFinalPatientLists() {

        val map = LinkedHashMap<String, PatientSearchResult>()

        // LOCAL FIRST
        filteredPatientList.forEach { localItem ->

            val uuid = localItem.patient?.uuid ?: return@forEach

            map[uuid] = localItem
        }

        // FHIR MERGE
        filteredPatientFhirList.forEach { fhirItem ->

            val uuid = fhirItem.patient?.uuid ?: return@forEach

            val localItem = map[uuid]

            if (localItem != null) {
                // UUID MATCH → FIELD SPLIT MERGE

                val merged = fhirItem.copy(
                    patient = fhirItem.patient, // FHIR patient data (FULL DETAILS)

                    //  FROM LOCAL LIST
                    phoneMatched = localItem.phoneMatched,
                    isIHNetwork = localItem.isIHNetwork,
                    ihscore = localItem.ihscore,

                    //  FROM FHIR LIST
                    isNRNetwork = fhirItem.isNRNetwork,
                    nrscore = fhirItem.nrscore
                )

                map[uuid] = merged

            } else {
                // ONLY FHIR PATIENT
                map[uuid] = fhirItem
            }
        }
        // FINAL LIST
        filteredPatientFinalList.clear()
        filteredPatientFinalList.addAll(map.values)

        totalNetworkTv.visibility = View.VISIBLE
        totalNetworkTv.text =
            "${filteredPatientFinalList.size} matches found"

        Log.d(
            "FINAL_MERGE",
            "Final Count = ${filteredPatientFinalList.size}"
        )

        checkAllApisCompleted()
    }
    private fun mergeFinalPatientList1() {

        val map = LinkedHashMap<String, PatientSearchResult>()

        // LOCAL LIST
        filteredPatientList.forEach { item ->

            val uuid = item.patient?.uuid

            if (!uuid.isNullOrEmpty()) {

                map[uuid] = item
            }
        }

        // FHIR LIST OVERRIDE
        filteredPatientFhirList.forEach { item ->

            val uuid = item.patient?.uuid

            if (!uuid.isNullOrEmpty()) {

                // SAME UUID হলে FHIR DATA নিবে
                map[uuid] = item
            }
        }

        // FINAL LIST
        filteredPatientFinalList.clear()

        filteredPatientFinalList.addAll(map.values)
        totalNetworkTv.visibility=View.VISIBLE
        totalNetworkTv.text = "${filteredPatientFinalList.size} matches found"

        Log.d(
            "FINAL_MERGE",
            "Final Count = ${filteredPatientList.size}"
        )
        checkAllApisCompleted()
    }
    private fun fetchFhirPatients(
        firstName: String,
        lastName: String,
        phone: String,
        genders: String,
        dob: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientFhirObservable(firstName, lastName, phone,genders,dob, pageNo)
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

                    val genders = resource?.gender
                    val telecom = resource?.telecom?.firstOrNull()
                    val phones = telecom?.value
                    val dob = resource?.birthDate
                    val address = resource?.address?.firstOrNull()
                    val extension = resource?.extension


                    val patient = PatientDTO().apply {
                        uuid = resource.identifier
                            ?.firstOrNull { identifier ->
                                !identifier.system.isNullOrEmpty() &&
                                        !identifier.value.isNullOrEmpty() &&
                                        identifier.id == null &&
                                        identifier.use == null
                            }
                            ?.value
                        sourceId=resource.id
                        firstname = given
                        lastname = family
                        gender = resource.gender ?: ""
                        phonenumber = resource.telecom?.firstOrNull()?.value ?: ""
                        dateofbirth = resource.birthDate ?: ""

                        firstname = given
                        lastname = family
                        gender = genders ?: ""
                        phonenumber = phones ?: ""
                        dateofbirth = dob ?: ""
                        country=address?.country?:""
                        postalcode=address?.postalCode?:""
                        stateprovince=address?.state
                        city=address?.city
                        address1=address?.line.toString()
                        contactType =getExtensionValueByEndPoint(extension,"Emergency-Contact-Type")
                        education =getExtensionValueByEndPoint(extension,"Education-Level")
                        economic =getExtensionValueByEndPoint(extension,"Economic-Status")
                        caste =getExtensionValueByEndPoint(extension,"Caste")
                        syncd=true
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
        gender: String,
        dob: String,
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

        if (lastName.isNotEmpty()) {
            nameObject.put("family", lastName)
        }

        val givenArray = JSONArray()
        givenArray.put(firstName)
        if (firstName.isNotEmpty()) {

            nameObject.put("given", givenArray)}
        nameArray.put(nameObject)

        resourceObject.put("name", nameArray)


        // telecom
        val telecomArray = JSONArray()
        val telecomObject = JSONObject()
        if (phone.isNotEmpty()) {
            telecomObject.put("system", "phone")
            telecomObject.put("value", phone)

            telecomArray.put(telecomObject)
            resourceObject.put("telecom", telecomArray)}
        // gender
        if (gender.isNotEmpty()) {
            resourceObject.put("gender", gender.lowercase())
        }

        // dob
        if (dob.isNotEmpty()) {
            resourceObject.put("birthDate", dob)
        }

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
        val urlfhirLocal = "http://192.168.19.152:6001/fhir/%24mdm-match"
        val urlfhir="https://openhim-intelehealth.mpower-social.com:6001/fhir/\$mdm-match"

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val auth = Credentials.basic("fhir_app", "Admin123")
        return AppConstants.apiInterface.searchPatientFhir(
            urlfhir,
            auth,
            requestBody
        )
    }
    private fun doFilterLocal(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {

        subscriptions.add(
            Single.fromCallable {
                PatientsDAO.getFilteredPatients1(
                    firstName,
                    lastName,
                    gender,
                    phone,
                    dob,
                    0,
                    defaultPageSize
                )
            }
                .subscribeOn(Schedulers.io())
                .flatMap { localList ->

                    val isNetwork = CheckInternetAvailability.isNetworkAvailable(this)

                    if (!isNetwork) {
                        Single.just(localList)
                    } else {
                        fetchOpenMRSPatients(
                            firstName, lastName, phone,
                            getFullGenderStr(gender), dob, 0
                        )
                            .map { remote ->
                                mergePatientList(localList, remote)
                            }
                            .onErrorReturn { localList }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ patientList ->

                    progressBarIH.visibility = View.GONE   // ✅ STOP LOADING

                    filteredPatientList.clear()
                    filteredPatientList.addAll(patientList)

                    isLocalDone = true

                    ihNetworkTv.text =
                        if (patientList.isEmpty()) "No match found"
                        else "Matches found"

                    checkAndMerge()

                }, {

                    progressBarIH.visibility = View.GONE

                    isLocalDone = true
                    ihNetworkTv.text = "No match found"

                    checkAndMerge()
                })
        )
    }
    /*private fun doFilterLocals(
        firstName: String,
        lastName: String,
        gender: String,
        phone: String,
        dob: String
    ) {

        ihNetworkTv.visibility = View.VISIBLE

        subscriptions.add(

            Single.fromCallable {

                // LOCAL DB
                PatientsDAO.getFilteredPatients1(
                    firstName,
                    lastName,
                    gender,
                    phone,
                    dob,
                    0,
                    defaultPageSize
                )
            }

                .subscribeOn(Schedulers.io())

                .flatMap { localList ->

                    val isNetworkAvailable =
                        CheckInternetAvailability.isNetworkAvailable(this)

                    if (!isNetworkAvailable) {

                        // OFFLINE → ONLY LOCAL
                        Single.just(localList)

                    } else {

                        // ONLINE → REMOTE + LOCAL MERGE
                        fetchOpenMRSPatients(
                            firstName,
                            lastName,
                            phone,
                            getFullGenderStr(gender),
                            dob,
                            0
                        )

                            .map { remoteList ->

                                mergePatientList(
                                    localList,
                                    remoteList
                                )
                            }

                            // API FAIL → LOCAL ONLY
                            .onErrorReturn {

                                Log.e(
                                    "LOCAL_API",
                                    "Remote failed, using local",
                                    it
                                )

                                localList
                            }
                    }
                }

                .observeOn(AndroidSchedulers.mainThread())

                .doOnSubscribe {
                    loadingDialog.show()
                }

                .subscribe({ patientList ->

                    filteredPatientList.clear()
                    filteredPatientList.addAll(patientList)

                    isLocalDone = true

                    ihNetworkTv.text =
                        if (filteredPatientList.isEmpty()) {
                            "No match found"
                        } else {
                            "matches found"
                        }

                    checkAndMerge()

                }, {

                    filteredPatientList.clear()

                    isLocalDone = true

                    ihNetworkTv.text = "No match found"

                    checkAndMerge()

                    ToastUtil.showLongToast(
                        this@PatientSearchingActivity,
                        "Error finding patients"
                    )
                })
        )
    }*/
    private fun mergePatientList(
        localList: List<PatientSearchResult>,
        remoteList: List<PatientSearchResult>
    ): List<PatientSearchResult> {

        val map = LinkedHashMap<String, PatientSearchResult>()

        // LOCAL FIRST
        localList.forEach { item ->
            val uuid = item.patient?.uuid
            if (!uuid.isNullOrEmpty()) {
                map[uuid] = item
            }
        }

        // REMOTE OVERRIDE
        remoteList.forEach { item ->
            val uuid = item.patient?.uuid
            if (!uuid.isNullOrEmpty()) {
                map[uuid] = item
            }
        }

        return map.values.toList()
    }
    private fun fetchOpenMRSPatients(
        firstName: String,
        lastName: String,
        phone: String,
        genderss: String,
        dob: String,
        pageNo: Int
    ): Single<List<PatientSearchResult>> {

        return searchPatientOpenMRSObservable(
            firstName,
            lastName,
            phone,
            genderss,
            dob,
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
                val extension = resource?.extension

                val patient = PatientDTO().apply {
                    uuid = id
                    firstname = given
                    lastname = family
                    gender = genders ?: ""
                    phonenumber = phones ?: ""
                    dateofbirth = dob ?: ""
                    country=address?.country?:""
                    postalcode=address?.postalCode?:""
                    stateprovince=address?.state
                    city=address?.city
                    address1=address?.line.toString()
                    contactType =getExtensionValueByEndPoint(extension,"Emergency-Contact-Type")
                    education =getExtensionValueByEndPoint(extension,"Education-Level")
                    economic =getExtensionValueByEndPoint(extension,"Economic-Status")
                    caste =getExtensionValueByEndPoint(extension,"Caste")
                    syncd=false
                }

                val result = PatientSearchResult().apply {
                    this.patient = patient
                    this.source = MatchSource.OPENMRS
                    this.score = item.search?.score ?: 0.0
                    this.grade = MatchGrade.CERTAIN
                    this.localDbResult = false
                    this.isIHNetwork=true
                    this.ihscore = item.search?.score ?: 0.0
                }

                resultList.add(result)
            }

            resultList
        }
    }
    fun getExtensionValueByEndPoint(
        extensions: List<Extension>?,
        endPoint: String
    ): String? {

        if (extensions.isNullOrEmpty()) return null

        return extensions.firstOrNull { ext ->

            val url = ext.url ?: return@firstOrNull false

            val lastSegment = url.substringAfterLast("/")

            lastSegment.equals(endPoint, ignoreCase = true)

        }?.valueString
    }
    private fun searchPatientOpenMRSObservable(
        firstName: String,
        lastName: String,
        phone: String,
        gender: String,
        dob: String,
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

        if (lastName.isNotEmpty()) {
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

        return AppConstants.apiInterface.searchPatientOpenMRS(
            auth,
            requestBody
        )
    }
    private fun callNationalRegistryApi() {

        progressBarNational.visibility = View.VISIBLE
        isNationalApiDone = false

        doFilterFhir(
            patientDTO?.firstname ?: "",
            patientDTO?.lastname ?: "",
            patientDTO?.gender ?: "",
            patientDTO?.phonenumber ?: "",
            patientDTO?.dateofbirth ?: ""
        )}
            private fun callNationalRegistryApis() {

        progressBarNational.visibility = View.VISIBLE

        // Example API Delay
        Handler(Looper.getMainLooper()).postDelayed({

            progressBarNational.visibility = View.GONE

            isNationalApiDone = true

            doFilterFhir(
                patientDTO?.firstname?:"",
                patientDTO?.lastname?:"",
                patientDTO?.gender?:"",
                patientDTO?.phonenumber?:"",
                patientDTO?.dateofbirth?:""
            )
            //checkAllApisCompleted()

        }, 5000)
    }
    private fun checkAllApisCompleted() {

        if (isLocalDone && isFhirDone) {

            val intent = Intent(
                this,
                MatchResultActivity::class.java
            )

            intent.putExtra("patientDTO", patientDTO)
            intent.putExtra(
                "finalPatientList",
                ArrayList(filteredPatientFinalList)
            )

            startActivity(intent)
            finish()
        }
    }

    private fun checkAllApisCompleteds() {

        if (isLocalDone && isFhirDone) {

            lifecycleScope.launch {

                val intent = Intent(
                    this@PatientSearchingActivity,
                    MatchResultActivity::class.java
                )
                intent.putExtra("patientDTO", patientDTO)
                //  PASS FINAL LIST
                intent.putExtra(
                    "finalPatientList",
                    ArrayList(filteredPatientFinalList)
                )

                startActivity(intent)

                finish()
            }
        }
    }
}