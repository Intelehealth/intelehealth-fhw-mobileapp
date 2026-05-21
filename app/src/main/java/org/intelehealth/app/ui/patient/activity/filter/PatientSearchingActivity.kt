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
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this,
            getString(R.string.loading),
            getString(R.string.please_wait),
        ).apply {
            dismiss()
        }

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
        val  address=patientDTO?.address1
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

                resultList.toList()   // 🔥 IMPORTANT FIX
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
    }
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
                }

                val result = PatientSearchResult().apply {
                    this.patient = patient
                    this.source = MatchSource.OPENMRS
                    this.score = item.search?.score ?: 0.0
                    this.grade = MatchGrade.CERTAIN
                    this.localDbResult = false
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

            lifecycleScope.launch {

                delay(1000)

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